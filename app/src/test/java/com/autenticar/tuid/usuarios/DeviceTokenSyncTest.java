package com.autenticar.tuid.usuarios;

import static org.junit.Assert.assertEquals;

import com.autenticar.tuid.usuarios.DeviceTokenSync.Decision;

import org.junit.Test;

/** Cuando avisar al servidor que Firebase renovo el token de notificaciones (decision pura, sin Android ni red). */
public class DeviceTokenSyncTest {

    private static final long AHORA = 1_800_000_000_000L;
    private static final long HORA = 60L * 60L * 1000L;

    @Test
    public void sin_token_actual_no_hace_nada() {
        assertEquals(Decision.NADA, DeviceTokenSync.decidir("viejo", null, true, AHORA, 0));
        assertEquals(Decision.NADA, DeviceTokenSync.decidir("viejo", "", true, AHORA, 0));
    }

    @Test
    public void la_primera_vez_toma_el_token_actual_como_punto_de_partida() {
        // esta version nunca guardo un token: no hay con que probar ante el servidor, se corrige en la proxima Fe de Vida
        assertEquals(Decision.PUNTO_DE_PARTIDA, DeviceTokenSync.decidir(null, "nuevo", true, AHORA, 0));
        assertEquals(Decision.PUNTO_DE_PARTIDA, DeviceTokenSync.decidir("", "nuevo", true, AHORA, 0));
    }

    @Test
    public void si_el_token_no_cambio_no_hace_nada() {
        assertEquals(Decision.NADA, DeviceTokenSync.decidir("igual", "igual", true, AHORA, 0));
        assertEquals(Decision.NADA, DeviceTokenSync.decidir("igual", "igual", false, AHORA, AHORA));
    }

    @Test
    public void si_cambio_y_hay_personas_en_el_telefono_avisa_con_el_token_anterior() {
        assertEquals(Decision.REFRESCAR, DeviceTokenSync.decidir("viejo", "nuevo", true, AHORA, 0));
    }

    @Test
    public void si_cambio_pero_no_hay_personas_solo_actualiza_el_punto_de_partida() {
        assertEquals(Decision.PUNTO_DE_PARTIDA, DeviceTokenSync.decidir("viejo", "nuevo", false, AHORA, 0));
    }

    @Test
    public void despues_de_una_falla_espera_antes_de_reintentar() {
        long haceUnaHora = AHORA - HORA;
        long haceCincoHoras = AHORA - 5 * HORA;
        long haceSeisHoras = AHORA - 6 * HORA;
        long haceUnDia = AHORA - 24 * HORA;
        assertEquals(Decision.ESPERAR, DeviceTokenSync.decidir("viejo", "nuevo", true, AHORA, haceUnaHora));
        assertEquals(Decision.ESPERAR, DeviceTokenSync.decidir("viejo", "nuevo", true, AHORA, haceCincoHoras));
        assertEquals(Decision.REFRESCAR, DeviceTokenSync.decidir("viejo", "nuevo", true, AHORA, haceSeisHoras));
        assertEquals(Decision.REFRESCAR, DeviceTokenSync.decidir("viejo", "nuevo", true, AHORA, haceUnDia));
    }
}
