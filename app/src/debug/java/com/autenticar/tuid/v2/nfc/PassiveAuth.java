package com.autenticar.tuid.v2.nfc;

import android.content.Context;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.SignedData;
import org.bouncycastle.asn1.cms.SignerInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jmrtd.lds.SODFile;
import org.jmrtd.lds.SignedDataUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Passive Authentication (OACI 9303, parte 11): comprueba que los datos leidos del chip
 *  1) coinciden con los hashes firmados en el SOD (integridad),
 *  2) el SOD esta firmado por el certificado del emisor (Document Signer),
 *  3) ese certificado fue emitido por la autoridad del pais (CSCA) que figura en la lista de confianza.
 *
 * NO detecta un chip clonado (para eso hace falta Chip/Active Authentication).
 * Solo para pruebas: la lista de confianza es un recurso de debug.
 */
public final class PassiveAuth {

    private PassiveAuth() { }

    public static class Info {
        public boolean hashesOk;
        public boolean firmaOk;
        public boolean cadenaOk;
        public String detalle = "";

        public boolean autentico() { return hashesOk && firmaOk && cadenaOk; }
    }

    /**
     * @param sodBruto contenido de EF.SOD tal como se leyo del chip (con la cabecera 0x77)
     * @param archivos mapa numero de grupo de datos -> bytes tal como se leyeron del chip (DG1, DG2)
     */
    public static Info verificar(Context ctx, SODFile sod, byte[] sodBruto, Map<Integer, byte[]> archivos) {
        Info r = new Info();
        StringBuilder d = new StringBuilder();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Provider bc = new BouncyCastleProvider(); // instancia propia: no depende del "BC" recortado de Android

        // 1) Integridad: hash de cada grupo de datos leido contra el que firmo el emisor
        try {
            String alg = sod.getDigestAlgorithm();
            Map<Integer, byte[]> firmados = sod.getDataGroupHashes();
            boolean todos = !archivos.isEmpty();
            for (Map.Entry<Integer, byte[]> e : archivos.entrySet()) {
                byte[] esperado = firmados.get(e.getKey());
                byte[] real = MessageDigest.getInstance(alg).digest(e.getValue());
                boolean ok = esperado != null && MessageDigest.isEqual(esperado, real);
                d.append("  DG").append(e.getKey()).append(": ").append(ok ? "coincide" : "NO COINCIDE").append('\n');
                todos &= ok;
            }
            r.hashesOk = todos;
        } catch (Exception e) {
            d.append("  error en hashes: ").append(e.getClass().getSimpleName()).append('\n');
        }

        // 2) Firma del SOD con el certificado del emisor (Document Signer)
        X509Certificate dsc = null;
        try {
            X509Certificate original = sod.getDocSigningCertificate();
            if (original != null) {
                // se vuelve a leer con BouncyCastle para soportar curvas como brainpool
                dsc = (X509Certificate) CertificateFactory.getInstance("X.509", bc)
                        .generateCertificate(new ByteArrayInputStream(original.getEncoded()));
                r.firmaOk = firmaSodValida(sod, sodBruto, dsc, bc, d);
                d.append("  Document Signer: ").append(nombre(dsc.getIssuerX500Principal().getName("RFC1779")))
                        .append(" | vigencia ").append(fmt.format(dsc.getNotBefore())).append(" a ")
                        .append(fmt.format(dsc.getNotAfter())).append('\n');
            } else {
                d.append("  el SOD no incluye el certificado del emisor\n");
            }
        } catch (Exception e) {
            d.append("  error en firma: ").append(e.getClass().getSimpleName()).append(' ').append(e.getMessage()).append('\n');
        }

        // 3) Cadena de confianza: algun CSCA de la lista debe haber firmado el certificado del emisor
        if (dsc != null) {
            try {
                List<X509Certificate> csca = cargarCsca(ctx, bc);
                String emisor = dsc.getIssuerX500Principal().getName();
                int candidatos = 0;
                for (X509Certificate c : csca) {
                    if (!c.getSubjectX500Principal().getName().equals(emisor)) continue;
                    candidatos++;
                    try {
                        dsc.verify(c.getPublicKey(), bc);
                        r.cadenaOk = true;
                        d.append("  Cadena OK con CSCA ").append(nombre(c.getSubjectX500Principal().getName("RFC1779")))
                                .append(" (vigente hasta ").append(fmt.format(c.getNotAfter())).append(")\n");
                        break;
                    } catch (Exception ignored) { /* probar con el siguiente CSCA del mismo nombre */ }
                }
                if (!r.cadenaOk) {
                    d.append(candidatos == 0 ? "  Sin CSCA en la lista de confianza para este emisor\n"
                            : "  Ningun CSCA con ese nombre valida el certificado del emisor\n");
                }
                if (new Date().after(dsc.getNotAfter())) {
                    d.append("  (el certificado del emisor ya vencio: es normal si el documento se firmo antes)\n");
                }
            } catch (Exception e) {
                d.append("  error en cadena: ").append(e.getClass().getSimpleName()).append(' ').append(e.getMessage()).append('\n');
            }
        }
        r.detalle = d.toString();
        return r;
    }

    /** Verifica la firma CMS del SOD (atributos firmados + hash del contenido). */
    private static boolean firmaSodValida(SODFile sod, byte[] sodBruto, X509Certificate dsc, Provider bc, StringBuilder d)
            throws Exception {
        // EF.SOD = 0x77 <longitud> <ContentInfo>: se descarta la cabecera 0x77
        int i = 1;
        int b = sodBruto[i++] & 0xFF;
        if (b >= 0x80) i += (b & 0x7F);
        SignedData sd = SignedDataUtil.readSignedData(
                new ByteArrayInputStream(sodBruto, i, sodBruto.length - i));
        SignerInfo si = SignerInfo.getInstance(sd.getSignerInfos().getObjectAt(0));
        String digest = nombreDigest(si.getDigestAlgorithm().getAlgorithm().getId());
        // Contenido firmado EXACTAMENTE como viene en el SOD (no el que la libreria vuelve a codificar)
        byte[] eContent = ((ASN1OctetString) sd.getEncapContentInfo().getContent()).getOctets();
        byte[] firmado;
        ASN1Set attrs = si.getAuthenticatedAttributes();
        if (attrs != null) {
            // el atributo messageDigest debe ser el hash del contenido del SOD
            byte[] esperado = null;
            ASN1ObjectIdentifier md = new ASN1ObjectIdentifier("1.2.840.113549.1.9.4");
            for (int k = 0; k < attrs.size(); k++) {
                Attribute a = Attribute.getInstance(attrs.getObjectAt(k));
                if (md.equals(a.getAttrType())) {
                    ASN1Encodable v = a.getAttrValues().getObjectAt(0);
                    esperado = ((ASN1OctetString) v).getOctets();
                }
            }
            byte[] real = MessageDigest.getInstance(digest).digest(eContent);
            if (esperado == null || !MessageDigest.isEqual(esperado, real)) {
                d.append("  el hash del contenido del SOD no coincide con el firmado\n");
                return false;
            }
            firmado = attrs.getEncoded(ASN1Encoding.DER);
        } else {
            firmado = eContent;
        }
        String encOid = si.getDigestEncryptionAlgorithm().getAlgorithm().getId();
        Signature s = Signature.getInstance(nombreFirma(encOid, digest), bc);
        if ("1.2.840.113549.1.1.10".equals(encOid)) {
            AlgorithmParameterSpec p = sod.getDigestEncryptionAlgorithmParams();
            if (p != null) s.setParameter(p);
        }
        s.initVerify(dsc.getPublicKey());
        s.update(firmado);
        boolean ok = s.verify(si.getEncryptedDigest().getOctets());
        if (!ok) d.append("  la firma del SOD no es valida con el certificado del emisor\n");
        return ok;
    }

    private static String nombreDigest(String oid) {
        switch (oid) {
            case "1.3.14.3.2.26": return "SHA-1";
            case "2.16.840.1.101.3.4.2.4": return "SHA-224";
            case "2.16.840.1.101.3.4.2.1": return "SHA-256";
            case "2.16.840.1.101.3.4.2.2": return "SHA-384";
            case "2.16.840.1.101.3.4.2.3": return "SHA-512";
            default: return "SHA-256";
        }
    }

    private static String nombreFirma(String encOid, String digest) {
        String h = digest.replace("-", ""); // SHA256
        switch (encOid) {
            case "1.2.840.113549.1.1.1": return h + "withRSA";
            case "1.2.840.113549.1.1.5": return "SHA1withRSA";
            case "1.2.840.113549.1.1.14": return "SHA224withRSA";
            case "1.2.840.113549.1.1.11": return "SHA256withRSA";
            case "1.2.840.113549.1.1.12": return "SHA384withRSA";
            case "1.2.840.113549.1.1.13": return "SHA512withRSA";
            case "1.2.840.113549.1.1.10": return "RSASSA-PSS";
            case "1.2.840.10045.4.1": return "SHA1withECDSA";
            case "1.2.840.10045.4.3.1": return "SHA224withECDSA";
            case "1.2.840.10045.4.3.2": return "SHA256withECDSA";
            case "1.2.840.10045.4.3.3": return "SHA384withECDSA";
            case "1.2.840.10045.4.3.4": return "SHA512withECDSA";
            default: return h + "withRSA";
        }
    }

    private static String nombre(String dn) {
        // muestra solo el pais (C=) y la organizacion, sin datos adicionales
        String c = "?", o = "";
        for (String p : dn.split(",")) {
            p = p.trim();
            if (p.startsWith("C=")) c = p.substring(2);
            if (p.startsWith("O=")) o = p.substring(2);
        }
        return c + (o.isEmpty() ? "" : " / " + o);
    }

    private static List<X509Certificate> cargarCsca(Context ctx, Provider bc) throws Exception {
        List<X509Certificate> lista = new ArrayList<>();
        InputStream in = ctx.getAssets().open("csca-master-list.pem");
        byte[] todo = new byte[in.available()];
        int off = 0;
        while (off < todo.length) {
            int n = in.read(todo, off, todo.length - off);
            if (n < 0) break;
            off += n;
        }
        in.close();
        CertificateFactory cf = CertificateFactory.getInstance("X.509", bc);
        for (Object o : cf.generateCertificates(new ByteArrayInputStream(todo))) lista.add((X509Certificate) o);
        return lista;
    }
}
