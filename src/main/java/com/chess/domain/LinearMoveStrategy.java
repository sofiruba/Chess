package com.chess.domain;

import java.util.List;

public final class LinearMoveStrategy implements IMoveStrategy {
    @Override
    public List<Position> getValidMoves(Board board, Position currentPosition) {
        return RayMoveStrategy.collect(board, currentPosition,
                new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}});
    }
}
