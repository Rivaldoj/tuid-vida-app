package com.autenticar.tuid.v2.nfc;

import android.nfc.tech.IsoDep;

import net.sf.scuba.smartcards.CardService;

import org.jmrtd.BACKey;
import org.jmrtd.PACEKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.CardAccessFile;
import org.jmrtd.lds.PACEInfo;
import org.jmrtd.lds.SecurityInfo;
import org.jmrtd.lds.icao.DG1File;
import org.jmrtd.lds.icao.DG2File;
import org.jmrtd.lds.icao.MRZInfo;
import org.jmrtd.lds.iso19794.FaceImageInfo;
import org.jmrtd.lds.iso19794.FaceInfo;

import java.io.InputStream;
import java.util.List;

/**
 * Lee el chip de un documento electronico de viaje (OACI 9303): pasaporte o nuevo DNI.
 * Autenticacion con PACE (clave = CAN o datos de la MRZ) y, si el chip no lo ofrece, BAC.
 *
 * SIN VERIFICAR contra un DNI real todavia (probado con pasaporte). No valida la firma del chip
 * (Passive Authentication): eso se agrega antes de usar el resultado en produccion.
 */
public class EmrtdReader {

    private final android.content.Context ctx;

    public EmrtdReader(android.content.Context ctx) {
        this.ctx = ctx;
    }

    public static class EmrtdException extends Exception {
        public final String etapa;
        public final String detallePace;
        public EmrtdException(String etapa, String detallePace, Throwable causa) {
            super(causa);
            this.etapa = etapa;
            this.detallePace = detallePace;
        }
    }

    public static class Result {
        public String protocolo;      // PACE-CAN | PACE-MRZ | BAC
        public String tipoDocumento;
        public String paisEmisor;
        public String apellidos;
        public String nombres;
        public String numeroDocumento;
        public String nacionalidad;
        public String fechaNacimiento; // yyMMdd
        public String sexo;
        public String fechaVencimiento; // yyMMdd
        public int fotoBytes;
        public String fotoMime;
        public int fotoAncho;
        public int fotoAlto;
        public byte[] foto; // sin tratar (JPEG o JPEG 2000)
        public PassiveAuth.Info autenticidad;
    }

    /**
     * @param can           codigo de acceso de 6 digitos (opcional; si es nulo o vacio se usan los datos de la MRZ)
     * @param docNumber     numero de documento tal como figura en la MRZ (sin espacios)
     * @param birthYyMmDd   fecha de nacimiento AAMMDD
     * @param expiryYyMmDd  fecha de vencimiento AAMMDD
     */
    public Result read(IsoDep isoDep, String can, String docNumber, String birthYyMmDd, String expiryYyMmDd)
            throws EmrtdException {
        etapa = "abrir chip";
        detallePace = "no intentado";
        try {
            return readInterno(isoDep, can, docNumber, birthYyMmDd, expiryYyMmDd);
        } catch (Exception e) {
            throw new EmrtdException(etapa, detallePace, e);
        }
    }

    private String etapa;
    private String detallePace;

    private static String resumenError(Throwable e) {
        String m = e.getMessage();
        if (m != null && m.length() > 120) m = m.substring(0, 120);
        return e.getClass().getSimpleName() + (m != null ? " (" + m + ")" : "");
    }

    private Result readInterno(IsoDep isoDep, String can, String docNumber, String birthYyMmDd, String expiryYyMmDd)
            throws Exception {
        isoDep.setTimeout(10000);
        CardService cardService = CardService.getInstance(isoDep);
        cardService.open();
        PassportService service = new PassportService(cardService,
                PassportService.NORMAL_MAX_TRANCEIVE_LENGTH,
                PassportService.DEFAULT_MAX_BLOCKSIZE, false, false);
        service.open();

        Result r = new Result();
        boolean hayCan = can != null && can.trim().length() > 0;
        BACKey bacKey = new BACKey(docNumber, birthYyMmDd, expiryYyMmDd);

        boolean paceOk = false;
        etapa = "PACE (CardAccess)";
        StringBuilder diag = new StringBuilder();
        try {
            InputStream in = service.getInputStream(PassportService.EF_CARD_ACCESS, PassportService.DEFAULT_MAX_BLOCKSIZE);
            CardAccessFile cardAccess = new CardAccessFile(in);
            int nPace = 0;
            for (SecurityInfo info : cardAccess.getSecurityInfos()) {
                if (!(info instanceof PACEInfo)) {
                    diag.append("[").append(info.getClass().getSimpleName()).append("] ");
                    continue;
                }
                nPace++;
                PACEInfo paceInfo = (PACEInfo) info;
                diag.append("[PACE param=").append(paceInfo.getParameterId()).append("] ");
                // Se prueba cada variante anunciada por el chip; si una falla se sigue con la siguiente
                try {
                    PACEKeySpec key = hayCan ? PACEKeySpec.createCANKey(can.trim()) : PACEKeySpec.createMRZKey(bacKey);
                    service.doPACE(key, paceInfo.getObjectIdentifier(),
                            PACEInfo.toParameterSpec(paceInfo.getParameterId()), paceInfo.getParameterId());
                    paceOk = true;
                    r.protocolo = hayCan ? "PACE-CAN" : "PACE-MRZ";
                    break;
                } catch (Exception e) {
                    diag.append("fallo: ").append(resumenError(e)).append(" ");
                }
            }
            if (nPace == 0) diag.append("el chip no anuncia PACE");
        } catch (Exception e) {
            diag.append("no se pudo leer EF.CardAccess: ").append(resumenError(e));
        }
        detallePace = (hayCan ? "clave=CAN. " : "clave=MRZ. ") + diag;

        etapa = "seleccionar aplicacion";
        service.sendSelectApplet(paceOk);
        if (!paceOk) {
            etapa = "BAC (clave de la MRZ)";
            service.doBAC(bacKey);
            r.protocolo = "BAC";
        }

        // DG1: datos de la MRZ
        etapa = "leer DG1 (datos)";
        InputStream in1 = service.getInputStream(PassportService.EF_DG1, PassportService.DEFAULT_MAX_BLOCKSIZE);
        DG1File dg1 = new DG1File(in1);
        java.util.Map<Integer, byte[]> crudos = new java.util.HashMap<>();
        crudos.put(1, dg1.getEncoded());
        MRZInfo mrz = dg1.getMRZInfo();
        r.tipoDocumento = mrz.getDocumentCode();
        r.paisEmisor = mrz.getIssuingState();
        r.apellidos = mrz.getPrimaryIdentifier();
        r.nombres = mrz.getSecondaryIdentifier();
        r.numeroDocumento = mrz.getDocumentNumber();
        r.nacionalidad = mrz.getNationality();
        r.fechaNacimiento = mrz.getDateOfBirth();
        r.sexo = String.valueOf(mrz.getGender());
        r.fechaVencimiento = mrz.getDateOfExpiry();

        // DG2: foto del rostro (no se decodifica: suele ser JPEG 2000)
        etapa = "leer DG2 (foto)";
        try {
            InputStream in2 = service.getInputStream(PassportService.EF_DG2, PassportService.DEFAULT_MAX_BLOCKSIZE);
            DG2File dg2 = new DG2File(in2);
            crudos.put(2, dg2.getEncoded());
            List<FaceInfo> faces = dg2.getFaceInfos();
            if (!faces.isEmpty() && !faces.get(0).getFaceImageInfos().isEmpty()) {
                FaceImageInfo img = faces.get(0).getFaceImageInfos().get(0);
                r.fotoBytes = img.getImageLength();
                r.fotoMime = img.getMimeType();
                r.fotoAncho = img.getWidth();
                r.fotoAlto = img.getHeight();
                java.io.DataInputStream di = new java.io.DataInputStream(img.getImageInputStream());
                byte[] data = new byte[img.getImageLength()];
                di.readFully(data);
                r.foto = data;
            }
        } catch (Exception e) {
            r.fotoMime = "no se pudo leer DG2: " + e.getClass().getSimpleName();
        }
        etapa = "autenticidad (SOD)";
        try {
            InputStream inSod = service.getInputStream(PassportService.EF_SOD, PassportService.DEFAULT_MAX_BLOCKSIZE);
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n;
            while ((n = inSod.read(buf)) > 0) bos.write(buf, 0, n);
            byte[] sodBruto = bos.toByteArray();
            org.jmrtd.lds.SODFile sod = new org.jmrtd.lds.SODFile(new java.io.ByteArrayInputStream(sodBruto));
            r.autenticidad = PassiveAuth.verificar(ctx, sod, sodBruto, crudos);
        } catch (Exception e) {
            PassiveAuth.Info i = new PassiveAuth.Info();
            i.detalle = "  no se pudo leer/verificar el SOD: " + e.getClass().getSimpleName() + "\n";
            r.autenticidad = i;
        }
        service.close();
        return r;
    }
}
