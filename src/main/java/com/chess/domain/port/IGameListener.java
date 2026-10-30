package com.chess.domain.port;

/**
 * Quien quiere enterarse de lo que le pasa a una partida sin tener que consultarla a cada rato. Todos los métodos
 * tienen una implementación vacía: cada oyente redefine solo los eventos que le interesan. Los eventos se notifican
 * después de que la partida ya cambió, por lo que consultarla desde aquí devuelve el estado nuevo.
 */
public interface IGameListener {

    /** Se jugó una jugada. */
    default void moveMade() {
    }

    /** Se deshizo la última jugada. */
    default void moveUndone() {
    }

    /** Se rehízo la última jugada deshecha. */
    default void moveRedone() {
    }

    /** La partida terminó (jaque mate o tablas). Se notifica después de {@link #moveMade()} o {@link #moveRedone()}. */
    default void gameFinished() {
    }
}