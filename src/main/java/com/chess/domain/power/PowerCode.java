package com.chess.domain.power;

import java.util.Objects;

/**
 * Distingue los códigos de uso de poder ("@e4e7") de los de jugada ("e2e4"). Lo que sigue al prefijo tiene el
 * mismo formato que una jugada sin promoción: la casilla de la pieza y la del objetivo.
 */
public final class PowerCode {

    private PowerCode() {
    }

    public static boolean isPowerCode(String code) {
        Objects.requireNonNull(code, "code must not be null");
        return code.startsWith(PowerCommand.CODE_PREFIX);
    }

    /** El código sin el prefijo, listo para leerse como origen y destino. */
    public static String squares(String code) {
        if (!isPowerCode(code)) {
            throw new IllegalArgumentException("Not a power code: '" + code + "'");
        }
        return code.substring(PowerCommand.CODE_PREFIX.length());
    }
}
