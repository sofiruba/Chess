package com.chess.domain.rule;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

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
