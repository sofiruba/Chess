package com.chess.domain.rule;

import com.chess.domain.model.*;


public final class CheckDetector implements ICheckDetector {

    private final PieceRoles roles;

    public CheckDetector() {
        this(PieceRoles.standard());
    }

    public CheckDetector(PieceRoles roles) {
        this.roles = java.util.Objects.requireNonNull(roles, "roles must not be null");
    }

    @Override
    public boolean isInCheck(Board board, Color color) {
        return kingInCheckPosition(board, color) != null;
    }

    @Override
    public Position kingInCheckPosition(Board board, Color color) {
        Position kingPosition = board.findPiece(color, roles.king());
        if (kingPosition != null && isSquareAttacked(board, kingPosition, color.opposite())) {
            return kingPosition;
        }
        return null;
    }

    @Override
    public boolean isSquareAttacked(Board board, Position target, Color attackingColor) {
        for (Position position : board.positions()) {
            if (attacks(board, position, attackingColor, target)) {
                return true;
            }
        }
        return false;
    }

    private boolean attacks(Board board, Position from, Color attackingColor, Position target) {
        Piece piece = board.pieceAt(from);
        return piece != null
                && piece.color() == attackingColor
                && piece.getAttackedMoves(board, from).contains(target);
    }
}
