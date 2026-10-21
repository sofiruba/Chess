package com.chess.domain;

public interface IGameState {
    IGameState handleMove(MoveCommand move);
}
