package com.chess.domain;

import java.util.Objects;

/**
 * Final state of a game drawn by one of the {@link IDrawRule}s.
 */
public final class DrawState implements IGameState {

    private final Color playerToMove;

    public DrawState(Color playerToMove) {
        this.playerToMove = Objects.requireNonNull(playerToMove, "playerToMove must not be null");
    }

    @Override
    public Color activePlayerColor() {
        return playerToMove;
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
        throw new IllegalStateException("The game is over by draw");
    }
}
