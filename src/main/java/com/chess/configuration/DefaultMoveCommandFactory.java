package com.chess.configuration;

import com.chess.domain.model.Board;
import com.chess.domain.command.IMoveCommandFactory;
import com.chess.domain.command.MoveCommand;
import com.chess.domain.model.Position;

public final class DefaultMoveCommandFactory implements IMoveCommandFactory {

    @Override
    public MoveCommand create(Board board, Position from, Position to) {
        return new MoveCommand(board, from, to);
    }

}
