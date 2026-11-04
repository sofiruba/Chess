package com.chess.domain.power;

import com.chess.domain.command.ICommand;
import com.chess.domain.model.Piece;

/**
 * El uso de un poder. Es una jugada más ({@link ICommand}): ocupa el turno, se guarda en el historial y se puede
 * deshacer y rehacer. Además de lo que hace en el tablero, puede dejar un efecto que dura más allá de la jugada
 * (congelar, proteger), que se aplica sobre el {@link PowerState} y no sobre el tablero.
 */
public interface IPowerCommand extends ICommand {

    /** La pieza que usó el poder; lo pierde al usarlo. */
    Piece holder();

    /** Efecto duradero del poder. Por defecto no deja ninguno. */
    default PowerState applyEffect(PowerState powers) {
        return powers;
    }
}
