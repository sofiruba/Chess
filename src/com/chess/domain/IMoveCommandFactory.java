package com.chess.domain;

public interface IMoveCommandFactory {
    MoveCommand create(Board board, Position from, Position to);
}
