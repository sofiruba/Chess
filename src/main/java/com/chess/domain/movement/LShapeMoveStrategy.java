package com.chess.domain.movement;

import com.chess.domain.model.*;

import java.util.ArrayList;
import java.util.List;

public final class LShapeMoveStrategy implements IMoveStrategy {

    private static final int[][] OFFSETS = {
            {1, 2}, {2, 1}, {2, -1}, {1, -2},
            {-1, -2}, {-2, -1}, {-2, 1}, {-1, 2}
    };

    @Override
    public List<Position> getValidMoves(Board board, Position currentPosition) {
        Piece originPiece = board.pieceAt(currentPosition);
        if (originPiece == null) {
            throw new IllegalArgumentException("There is no piece at " + currentPosition);
        }
        List<Position> moves = new ArrayList<>();
        for (int[] offset : OFFSETS) {
            int x = currentPosition.x() + offset[0];
            int y = currentPosition.y() + offset[1];
            if (board.contains(x, y) && isReachable(board, originPiece, new Position(x, y))) {
                moves.add(new Position(x, y));
            }
        }
        return List.copyOf(moves);
    }

    private boolean isReachable(Board board, Piece originPiece, Position candidate) {
        Piece occupant = board.pieceAt(candidate);
        return occupant == null || occupant.color() != originPiece.color();
    }
}
