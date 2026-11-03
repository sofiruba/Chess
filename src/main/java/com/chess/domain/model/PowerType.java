package com.chess.domain.model;

import java.util.Objects;

/**
 * Identifica un poder que puede salir de una caja misteriosa. Igual que {@link PieceType}, es un tipo abierto y no
 * un enum, para poder definir un poder nuevo fuera de esta clase. La igualdad es por identidad: cada tipo se crea una
 * vez y se comparte la instancia.
 */
public final class PowerType {

    public static final PowerType ICE_FLOWER = new PowerType("ICE_FLOWER", "i");
    public static final PowerType FEATHER = new PowerType("FEATHER", "f");
    public static final PowerType STAR = new PowerType("STAR", "s");
    public static final PowerType SHELL = new PowerType("SHELL", "t");

    private final String name;
    private final String code;

    /**
     * @param name nombre único y legible del poder
     * @param code letra usada para guardar el contenido de las cajas, como "i" en "a5=i"
     */
    public PowerType(String name, String code) {
        this.name = requireText(name, "name");
        this.code = requireText(code, "code");
    }

    public String name() {
        return name;
    }

    public String code() {
        return code;
    }

    @Override
    public String toString() {
        return name;
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
