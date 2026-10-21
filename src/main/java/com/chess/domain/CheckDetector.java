package com.chess.domain;

public final class CheckDetector implements ICheckDetector {

    @Override
    public boolean isInCheck(Board board, Color color) {
        return kingInCheckPosition(board, color) != null;
    }

    @Override
    public Position kingInCheckPosition(Board board, Color color) {
        Position kingPosition = board.findPiece(color, PieceType.KING);
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
