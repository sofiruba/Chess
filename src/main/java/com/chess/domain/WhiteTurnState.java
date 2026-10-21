package com.chess.domain;

import java.util.Objects;

public final class WhiteTurnState implements IGameState {

    private final ITurnStateFactory stateFactory;

    public WhiteTurnState(ITurnStateFactory stateFactory) {
        this.stateFactory = Objects.requireNonNull(stateFactory, "stateFactory must not be null");
    }

    @Override
    public Color activePlayerColor() {
        return Color.WHITE;
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public GameResult result() {
        return GameResult.IN_PROGRESS;
    }

    @Override
    public IGameState handleMove(ICommand move) {
        Objects.requireNonNull(move, "move must not be null");
        if (move.movingColor() != activePlayerColor()) {
            throw new IllegalArgumentException("It is white's turn");
        }
        move.execute();
        return stateFactory.forColor(Color.BLACK);
    }
}