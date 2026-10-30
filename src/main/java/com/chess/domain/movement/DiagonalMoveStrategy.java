package com.chess.domain.movement;

import java.util.List;

import com.chess.domain.model.Board;
import com.chess.domain.model.IMoveStrategy;
import com.chess.domain.model.Position;

public final class DiagonalMoveStrategy implements IMoveStrategy {
    @Override
    public List<Position> getValidMoves(Board board, Position currentPosition) {
        return RayScanner.collect(board, currentPosition,
                new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}});
    }
}