package com.chess.domain;

import java.util.Objects;

public final class CheckmateState implements IGameState {

    private final Color checkedPlayer;

    public CheckmateState(Color checkedPlayer) {
        this.checkedPlayer = Objects.requireNonNull(checkedPlayer, "checkedPlayer must not be null");
    }

    @Override
    public Color activePlayerColor() {
        return checkedPlayer;
    }

    @Override
    public boolean isFinished() {
        return true;
    }

    @Override
    public GameResult result() {
        return checkedPlayer == Color.WHITE ? GameResult.BLACK_WINS : GameResult.WHITE_WINS;
    }

    @Override
    public IGameState handleMove(ICommand move) {
        throw new IllegalStateException("The game is over by checkmate");
    }
}