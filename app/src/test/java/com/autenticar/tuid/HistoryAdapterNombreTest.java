package com.autenticar.tuid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class HistoryAdapterNombreTest {
    @Test
    public void pasa_de_apellido_coma_nombre_a_nombre_apellido() {
        assertEquals("ANA MARIA PRUEBA", HistoryAdapter.nombreYApellido("PRUEBA, ANA MARIA"));
        assertEquals("JUAN PEREZ", HistoryAdapter.nombreYApellido("  PEREZ,JUAN "));
    }

    @Test
    public void sin_coma_lo_deja_igual_y_vacio_es_null() {
        assertEquals("JUAN PEREZ", HistoryAdapter.nombreYApellido("JUAN PEREZ"));
        assertNull(HistoryAdapter.nombreYApellido(null));
        assertNull(HistoryAdapter.nombreYApellido("   "));
    }
}
