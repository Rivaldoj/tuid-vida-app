package com.autenticar.tuid.usuarios;

import com.autenticar.tuid.model.CheckRegistrationResponse;

import java.util.List;

/** Mensaje para quien tiene su Fe de Vida al dia: no hace falta abrir la camara. */
public final class AlDia {
    private AlDia() {
    }

    /**
     * "Usted esta al dia. Su proxima certificacion para [empresa] la debe realizar el dia [fecha]."
     * Una linea por entidad. Si el servidor no informa la fecha, se omite esa parte y no se inventa ninguna.
     */
    public static String mensaje(List<CheckRegistrationResponse.Proxima> proximas) {
        StringBuilder sb = new StringBuilder("Usted está al día.");
        if (proximas != null) {
            for (CheckRegistrationResponse.Proxima p : proximas) {
                if (p == null || p.empresa == null || p.empresa.trim().isEmpty()) continue;
                if (p.proxima == null || p.proxima.trim().isEmpty()) continue;
                sb.append("\nSu próxima certificación para ").append(p.empresa.trim())
                        .append(" la debe realizar el día ").append(p.proxima.trim()).append(".");
            }
        }
        return sb.toString();
    }
}
