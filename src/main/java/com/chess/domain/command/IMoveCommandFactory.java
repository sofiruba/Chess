package com.chess.domain.command;

import com.chess.domain.model.Board;
import com.chess.domain.model.Position;
import com.chess.domain.rule.IMoveRule;


/**
 * Crea el comando para una jugada simple. Las jugadas especiales son creadas por su propia {@link IMoveRule}.
 */
public interface IMoveCommandFactory {

    ICommand create(Board board, Position from, Position to);
}
