package com.chess.domain.draw;

import com.chess.domain.model.*;


import java.util.ArrayList;
import java.util.List;

/**
 * Draw when nobody can deliver checkmate: K vs K, K+B vs K, K+N vs K, and kings with bishops that all stand on
 * squares of the same color. Any other piece, including pieces defined later, counts as enough material.
 */
public final class InsufficientMaterialRule implements IDrawRule {

    @Override
    public boolean isDraw(Board board, DrawCounters counters) {
        List<Position> minorOrOther = nonKingPositions(board);
        if (minorOrOther.isEmpty()) {
            return true;
        }
        if (minorOrOther.size() == 1 && isType(board, minorOrOther.get(0), PieceType.KNIGHT)) {
            return true;
        }
        return allBishopsOnSameColor(board, minorOrOther);
    }

    private List<Position> nonKingPositions(Board board) {
        List<Position> result = new ArrayList<>();
        for (Position position : board.positions()) {
            Piece piece = board.pieceAt(position);
            if (piece != null && piece.type() != PieceType.KING) {
                result.add(position);
            }
        }
        return result;
    }

    private boolean allBishopsOnSameColor(Board board, List<Position> positions) {
        int firstSquareColor = squareColor(positions.get(0));
        for (Position position : positions) {
            if (!isType(board, position, PieceType.BISHOP) || squareColor(position) != firstSquareColor) {
                return false;
            }
        }
        return true;
    }

    private boolean isType(Board board, Position position, PieceType type) {
        return board.pieceAt(position).type() == type;
    }

    private int squareColor(Position position) {
        return (position.x() + position.y()) % 2;
    }
}
