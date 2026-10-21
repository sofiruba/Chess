package com.chess.domain;

import java.util.Objects;

public final class StalemateState implements IGameState {

    private final Color playerWithoutMoves;

    public StalemateState(Color playerWithoutMoves) {
        this.playerWithoutMoves = Objects.requireNonNull(playerWithoutMoves, "playerWithoutMoves must not be null");
    }

    @Override
    public Color activePlayerColor() {
        return playerWithoutMoves;
    }

    @Override
    public boolean isFinished() {
        return true;
    }

    @Override
    public GameResult result() {
        return GameResult.DRAW;
    }

    @Override
    public IGameState handleMove(ICommand move) {
        throw new IllegalStateException("The game is over by stalemate");
    }
}