package com.autenticar.tuid.usuarios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.autenticar.tuid.model.CUIL;
import com.autenticar.tuid.model.HistoryResponse;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistorialSyncTest {

    private static HistoryResponse.Certificacion cert(long id, String fecha, String... empresas) {
        HistoryResponse.Certificacion c = new HistoryResponse.Certificacion();
        c.id = id;
        c.fecha = fecha;
        c.empresas = Arrays.asList(empresas);
        c.direccion = "Calle " + id;
        return c;
    }

    @Test
    public void invierte_el_orden_para_cargar_de_la_mas_antigua_a_la_mas_reciente() {
        // el servidor manda la mas reciente primero
        List<HistoryResponse.Certificacion> servidor = Arrays.asList(
                cert(3, "2026-03-01T10:00:00.000Z", "BANCO A"),
                cert(2, "2026-02-01T10:00:00.000Z", "BANCO A"),
                cert(1, "2026-01-01T10:00:00.000Z", "BANCO A"));
        List<CUIL> local = HistorialSync.convertir(servidor);
        assertEquals(3, local.size());
        assertEquals("Calle 1", local.get(0).getDiretion_cuil());   // la mas antigua se carga primero
        assertEquals("Calle 3", local.get(2).getDiretion_cuil());   // la mas reciente queda con el id mayor
    }

    @Test
    public void no_guarda_mas_de_20() {
        List<HistoryResponse.Certificacion> servidor = new ArrayList<>();
        for (int i = 30; i >= 1; i--) servidor.add(cert(i, "2026-01-01T10:00:00.000Z", "X"));
        List<CUIL> local = HistorialSync.convertir(servidor);
        assertEquals(20, local.size());
        // se quedan las 20 mas recientes (ids 30..11)
        assertEquals("Calle 11", local.get(0).getDiretion_cuil());
        assertEquals("Calle 30", local.get(19).getDiretion_cuil());
    }

    @Test
    public void une_las_entidades_y_tolera_campos_vacios() {
        HistoryResponse.Certificacion c = cert(1, "fecha rara", "BANCO A", "BANCO B");
        c.latitud = null;
        c.direccion = null;
        CUIL l = HistorialSync.convertir(Arrays.asList(c)).get(0);
        assertEquals("BANCO A, BANCO B", l.getEntidad_pagadora_cuil());
        assertEquals("fecha rara", l.getDate_cuil());
        assertEquals("", l.getDiretion_cuil());
        assertTrue(l.getLatitude_cuil() != null);
    }
}
