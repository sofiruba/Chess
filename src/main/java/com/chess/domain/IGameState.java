package com.chess.domain;

public interface IGameState {
    Color activePlayerColor();
    boolean isFinished();
    GameResult result();
    IGameState handleMove(ICommand move);
}
