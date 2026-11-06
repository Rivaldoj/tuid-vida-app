package com.autenticar.tuid.usuarios;

import static org.junit.Assert.assertEquals;

import com.autenticar.tuid.model.CheckRegistrationResponse;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AlDiaTest {

    private static CheckRegistrationResponse.Proxima p(String empresa, String fecha) {
        CheckRegistrationResponse.Proxima x = new CheckRegistrationResponse.Proxima();
        x.empresa = empresa;
        x.proxima = fecha;
        return x;
    }

    @Test
    public void una_entidad() {
        List<CheckRegistrationResponse.Proxima> l = new ArrayList<>();
        l.add(p("BANCO NACION   ", "15/12/2026"));
        assertEquals("Usted está al día.\nSu próxima certificación para BANCO NACION la debe realizar el día 15/12/2026.",
                AlDia.mensaje(l));
    }

    @Test
    public void varias_entidades_una_linea_cada_una() {
        List<CheckRegistrationResponse.Proxima> l = new ArrayList<>();
        l.add(p("BANCO A", "01/02/2027"));
        l.add(p("CAJA B", "01/03/2027"));
        String m = AlDia.mensaje(l);
        assertEquals("Usted está al día.\nSu próxima certificación para BANCO A la debe realizar el día 01/02/2027."
                + "\nSu próxima certificación para CAJA B la debe realizar el día 01/03/2027.", m);
    }

    @Test
    public void sin_fecha_no_se_inventa_ninguna() {
        List<CheckRegistrationResponse.Proxima> l = new ArrayList<>();
        l.add(p("BANCO A", null));
        assertEquals("Usted está al día.", AlDia.mensaje(l));
        assertEquals("Usted está al día.", AlDia.mensaje(null));
    }
}
