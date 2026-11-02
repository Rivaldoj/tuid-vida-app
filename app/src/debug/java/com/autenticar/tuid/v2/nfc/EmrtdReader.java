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
    }

    /**
     * @param can           codigo de acceso de 6 digitos (opcional; si es nulo o vacio se usan los datos de la MRZ)
     * @param docNumber     numero de documento tal como figura en la MRZ (sin espacios)
     * @param birthYyMmDd   fecha de nacimiento AAMMDD
     * @param expiryYyMmDd  fecha de vencimiento AAMMDD
     */
    public Result read(IsoDep isoDep, String can, String docNumber, String birthYyMmDd, String expiryYyMmDd)
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
        try {
            InputStream in = service.getInputStream(PassportService.EF_CARD_ACCESS, PassportService.DEFAULT_MAX_BLOCKSIZE);
            CardAccessFile cardAccess = new CardAccessFile(in);
            for (SecurityInfo info : cardAccess.getSecurityInfos()) {
                if (info instanceof PACEInfo) {
                    PACEInfo paceInfo = (PACEInfo) info;
                    PACEKeySpec key = hayCan ? PACEKeySpec.createCANKey(can.trim()) : PACEKeySpec.createMRZKey(bacKey);
                    service.doPACE(key, paceInfo.getObjectIdentifier(),
                            PACEInfo.toParameterSpec(paceInfo.getParameterId()), paceInfo.getParameterId());
                    paceOk = true;
                    r.protocolo = hayCan ? "PACE-CAN" : "PACE-MRZ";
                    break;
                }
            }
        } catch (Exception e) {
            paceOk = false; // el chip no ofrece PACE: se prueba BAC
        }

        service.sendSelectApplet(paceOk);
        if (!paceOk) {
            service.doBAC(bacKey);
            r.protocolo = "BAC";
        }

        // DG1: datos de la MRZ
        InputStream in1 = service.getInputStream(PassportService.EF_DG1, PassportService.DEFAULT_MAX_BLOCKSIZE);
        MRZInfo mrz = new DG1File(in1).getMRZInfo();
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
        try {
            InputStream in2 = service.getInputStream(PassportService.EF_DG2, PassportService.DEFAULT_MAX_BLOCKSIZE);
            List<FaceInfo> faces = new DG2File(in2).getFaceInfos();
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
        service.close();
        return r;
    }
}
