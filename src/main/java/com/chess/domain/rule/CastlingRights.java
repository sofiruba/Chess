package com.chess.domain.rule;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

public record CastlingRights(
        int boardSize,
        boolean whiteKingSide,
        boolean whiteQueenSide,
        boolean blackKingSide,
        boolean blackQueenSide) {

    public static CastlingRights initial(int boardSize) {
        return new CastlingRights(boardSize, true, true, true, true);
    }

    public boolean canCastle(Color color, int direction) {
        if (color == Color.WHITE) {
            return direction > 0 ? whiteKingSide : whiteQueenSide;
        }
        return direction > 0 ? blackKingSide : blackQueenSide;
    }

    public CastlingRights after(IMoveInfo move) {
        CastlingRights updated = this;
        for (IMoveInfo.MovedPiece moved : move.movedPieces()) {
            updated = updated.afterPieceMoved(moved.piece(), moved.from());
        }
        Piece captured = move.capturedPiece();
        if (captured != null && isHomeRook(captured, move.to())) {
            updated = updated.withoutRook(captured.color(), move.to().x());
        }
        return updated;
    }

    private CastlingRights afterPieceMoved(Piece piece, Position from) {
        if (piece.type() == PieceType.KING) {
            return withoutKing(piece.color());
        }
        if (isHomeRook(piece, from)) {
            return withoutRook(piece.color(), from.x());
        }
        return this;
    }

    private CastlingRights withoutKing(Color color) {
        if (color == Color.WHITE) {
            return new CastlingRights(boardSize, false, false, blackKingSide, blackQueenSide);
        }
        return new CastlingRights(boardSize, whiteKingSide, whiteQueenSide, false, false);
    }

    private CastlingRights withoutRook(Color color, int file) {
        boolean queenSide = file == 0;
        if (color == Color.WHITE) {
            return new CastlingRights(
                    boardSize,
                    whiteKingSide && queenSide,
                    whiteQueenSide && !queenSide,
                    blackKingSide,
                    blackQueenSide);
        }
        return new CastlingRights(
                boardSize,
                whiteKingSide,
                whiteQueenSide,
                blackKingSide && queenSide,
                blackQueenSide && !queenSide);
    }

    private boolean isHomeRook(Piece piece, Position position) {
        int lastIndex = boardSize - 1;
        return piece.type() == PieceType.ROOK
                && (position.x() == 0 || position.x() == lastIndex)
                && position.y() == (piece.color() == Color.WHITE ? 0 : lastIndex);
    }
}