package com.chess.domain.movement;

import com.chess.domain.model.*;


import java.util.List;

public final class LinearMoveStrategy implements IMoveStrategy {
    @Override
    public List<Position> getValidMoves(Board board, Position currentPosition) {
        return RayScanner.collect(board, currentPosition,
                new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}});
    }
}