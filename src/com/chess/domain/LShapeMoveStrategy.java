package com.chess.domain;

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
            if (x < 0 || x >= Board.SIZE || y < 0 || y >= Board.SIZE) {
                continue;
            }
            Position candidate = new Position(x, y);
            Piece occupant = board.pieceAt(candidate);
            if (occupant == null || occupant.color() != originPiece.color()) {
                moves.add(candidate);
            }
        }
        return List.copyOf(moves);
    }
}
