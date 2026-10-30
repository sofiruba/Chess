package com.chess.domain.game;


import java.util.List;
import java.util.Optional;

import com.chess.domain.command.ICommand;
import com.chess.domain.command.IMoveInfo;

public interface IMoveHistory {

    /**
     * @param previous la instantánea anterior a ejecutar el comando; deshacer la recupera
     */
    void record(ICommand command, GameSnapshot previous);

    Entry undoLast();

    Entry redoLast();

    /** Vista de solo lectura de la última jugada ejecutada; vacía antes de la primera jugada. */
    Optional<IMoveInfo> lastMove();

    /** Vista de solo lectura de las jugadas ejecutadas, primero las más antiguas. */
    List<IMoveInfo> moves();

    record Entry(ICommand command, GameSnapshot previous) {
    }
}
