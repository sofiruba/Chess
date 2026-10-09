package com.chess.domain;

import java.util.Objects;

public final class WhiteTurnState implements IGameState {

    private final IGameState nextState;

    public WhiteTurnState(IGameState nextState) {
        this.nextState = Objects.requireNonNull(nextState, "nextState must not be null");
    }

    @Override
    public IGameState handleMove(MoveCommand move) {
        Objects.requireNonNull(move, "move must not be null");
        if (move.movingColor() != Color.WHITE) {
            throw new IllegalArgumentException("It is white's turn");
        }
        move.execute();
        return nextState;
    }
}
