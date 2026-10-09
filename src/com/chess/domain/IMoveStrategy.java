package com.chess.domain;

import java.util.List;

public interface IMoveStrategy {
    List<Position> getValidMoves(Board board, Position currentPosition);
}
