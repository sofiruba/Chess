package com.chess.domain;

import java.util.List;

public final class DiagonalMoveStrategy implements IMoveStrategy {
    @Override
    public List<Position> getValidMoves(Board board, Position currentPosition) {
        return RayMoveStrategy.collect(board, currentPosition,
                new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}});
    }
}
