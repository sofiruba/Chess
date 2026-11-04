package com.chess.configuration;

/**
 * Cada cuántos turnos aparecen cajas misteriosas nuevas, cuántas en cada mitad del tablero y cuántas puede haber como
 * máximo al mismo tiempo (contando las que todavía no se abrieron). Un turno es la jugada de un jugador: con
 * {@code everyTurns = 5} se intenta agregar cajas después de las jugadas 5, 10, 15...
 */
public record BoxSpawning(int everyTurns, int boxesPerSide, int maxOnBoard) {

    /** No aparecen cajas nuevas durante la partida. */
    public static final BoxSpawning NONE = new BoxSpawning(0, 0, 0);

    public BoxSpawning {
        if (everyTurns < 0 || boxesPerSide < 0 || maxOnBoard < 0) {
            throw new IllegalArgumentException("Box spawning values must not be negative");
        }
        if ((everyTurns == 0) != (boxesPerSide == 0)) {
            throw new IllegalArgumentException("Use BoxSpawning.NONE to disable new boxes");
        }
        if (everyTurns > 0 && maxOnBoard < 2) {
            throw new IllegalArgumentException("maxOnBoard must leave room for at least one mirrored pair");
        }
    }

    /** Sin límite de cajas simultáneas. */
    public BoxSpawning(int everyTurns, int boxesPerSide) {
        this(everyTurns, boxesPerSide, everyTurns == 0 ? 0 : Integer.MAX_VALUE);
    }

    public boolean enabled() {
        return everyTurns > 0;
    }
}
