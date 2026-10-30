package com.chess.domain.draw;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.chess.domain.model.Board;
import com.chess.domain.model.Piece;
import com.chess.domain.model.PieceRoles;
import com.chess.domain.model.PieceType;
import com.chess.domain.model.Position;

/**
 * Tablas cuando nadie puede dar jaque mate: R contra R, R+A contra R, R+C contra R, y reyes con alfiles que están
 * todos en casillas del mismo color. Cualquier otra pieza, incluidas las definidas posteriormente, cuenta como
 * material suficiente.
 */
public final class InsufficientMaterialRule implements IDrawRule {

    private final PieceRoles roles;

    public InsufficientMaterialRule(PieceRoles roles) {
        this.roles = Objects.requireNonNull(roles, "roles must not be null");
    }

    @Override
    public boolean isDraw(Board board, DrawCounters counters) {
        List<Position> minorOrOther = nonKingPositions(board);
        if (minorOrOther.isEmpty()) {
            return true;
        }
        if (minorOrOther.size() == 1 && isType(board, minorOrOther.get(0), roles.knight())) {
            return true;
        }
        return allBishopsOnSameColor(board, minorOrOther);
    }

    private List<Position> nonKingPositions(Board board) {
        List<Position> result = new ArrayList<>();
        for (Position position : board.positions()) {
            Piece piece = board.pieceAt(position);
            if (piece != null && piece.type() != roles.king()) {
                result.add(position);
            }
        }
        return result;
    }

    private boolean allBishopsOnSameColor(Board board, List<Position> positions) {
        int firstSquareColor = squareColor(positions.get(0));
        for (Position position : positions) {
            if (!isType(board, position, roles.bishop()) || squareColor(position) != firstSquareColor) {
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