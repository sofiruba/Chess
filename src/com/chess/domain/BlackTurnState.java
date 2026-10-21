package com.chess.domain;

import java.util.Objects;

public final class BlackTurnState implements IGameState {

    private final IGameState nextState;

    public BlackTurnState(IGameState nextState) {
        this.nextState = Objects.requireNonNull(nextState, "nextState must not be null");
    }

    @Override
    public IGameState handleMove(MoveCommand move) {
        Objects.requireNonNull(move, "move must not be null");
        if (move.movingColor() != Color.BLACK) {
            throw new IllegalArgumentException("It is black's turn");
        }
        move.execute();
        return nextState;
    }
}
