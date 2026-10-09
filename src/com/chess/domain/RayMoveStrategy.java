package com.chess.domain;

import java.util.ArrayList;
import java.util.List;

final class RayMoveStrategy {

    private RayMoveStrategy() {
    }

    static List<Position> collect(Board board, Position origin, int[][] directions) {
        Piece originPiece = board.pieceAt(origin);
        if (originPiece == null) {
            throw new IllegalArgumentException("There is no piece at " + origin);
        }
        List<Position> moves = new ArrayList<>();
        for (int[] direction : directions) {
            int x = origin.x() + direction[0];
            int y = origin.y() + direction[1];
            while (x >= 0 && x < Board.SIZE && y >= 0 && y < Board.SIZE) {
                Position candidate = new Position(x, y);
                Piece occupant = board.pieceAt(candidate);
                if (occupant == null) {
                    moves.add(candidate);
                } else {
                    if (occupant.color() != originPiece.color()) {
                        moves.add(candidate);
                    }
                    break;
                }
                x += direction[0];
                y += direction[1];
            }
        }
        return List.copyOf(moves);
    }
}
