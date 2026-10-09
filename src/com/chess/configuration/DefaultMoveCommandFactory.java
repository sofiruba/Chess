package com.chess.configuration;

import com.chess.domain.Board;
import com.chess.domain.IMoveCommandFactory;
import com.chess.domain.MoveCommand;
import com.chess.domain.Position;

public final class DefaultMoveCommandFactory implements IMoveCommandFactory {

    @Override
    public MoveCommand create(Board board, Position from, Position to) {
        return new MoveCommand(board, from, to);
    }
}
