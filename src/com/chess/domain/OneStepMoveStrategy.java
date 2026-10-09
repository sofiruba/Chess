package com.chess.domain;

import java.util.ArrayList;
import java.util.List;

public final class OneStepMoveStrategy implements IMoveStrategy {

    @Override
    public List<Position> getValidMoves(Board board, Position currentPosition) {
        Piece originPiece = board.pieceAt(currentPosition);
        if (originPiece == null) {
            throw new IllegalArgumentException("There is no piece at " + currentPosition);
        }
        List<Position> moves = new ArrayList<>();
        for (int deltaX = -1; deltaX <= 1; deltaX++) {
            for (int deltaY = -1; deltaY <= 1; deltaY++) {
                if (deltaX == 0 && deltaY == 0) {
                    continue;
                }
                int x = currentPosition.x() + deltaX;
                int y = currentPosition.y() + deltaY;
                if (x < 0 || x >= Board.SIZE || y < 0 || y >= Board.SIZE) {
                    continue;
                }
                Position candidate = new Position(x, y);
                Piece occupant = board.pieceAt(candidate);
                if (occupant == null || occupant.color() != originPiece.color()) {
                    moves.add(candidate);
                }
            }
        }
        return List.copyOf(moves);
    }
}
