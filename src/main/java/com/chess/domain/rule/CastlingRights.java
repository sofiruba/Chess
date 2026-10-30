package com.chess.domain.rule;

import com.chess.domain.command.IMoveInfo;
import com.chess.domain.model.Color;
import com.chess.domain.model.Piece;
import com.chess.domain.model.PieceRoles;
import com.chess.domain.model.Position;


public record CastlingRights(
        int boardSize,
        boolean whiteKingSide,
        boolean whiteQueenSide,
        boolean blackKingSide,
        boolean blackQueenSide,
        PieceRoles roles) {

    public static CastlingRights initial(int boardSize, PieceRoles roles) {
        return new CastlingRights(boardSize, true, true, true, true, roles);
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
        if (piece.type() == roles.king()) {
            return withoutKing(piece.color());
        }
        if (isHomeRook(piece, from)) {
            return withoutRook(piece.color(), from.x());
        }
        return this;
    }

    private CastlingRights withoutKing(Color color) {
        if (color == Color.WHITE) {
            return new CastlingRights(boardSize, false, false, blackKingSide, blackQueenSide, roles);
        }
        return new CastlingRights(boardSize, whiteKingSide, whiteQueenSide, false, false, roles);
    }

    private CastlingRights withoutRook(Color color, int file) {
        boolean queenSide = file == 0;
        if (color == Color.WHITE) {
            return new CastlingRights(
                    boardSize,
                    whiteKingSide && queenSide,
                    whiteQueenSide && !queenSide,
                    blackKingSide,
                    blackQueenSide,
                    roles);
        }
        return new CastlingRights(
                boardSize,
                whiteKingSide,
                whiteQueenSide,
                blackKingSide && queenSide,
                blackQueenSide && !queenSide,
                roles);
    }

    private boolean isHomeRook(Piece piece, Position position) {
        int lastIndex = boardSize - 1;
        return piece.type() == roles.rook()
                && (position.x() == 0 || position.x() == lastIndex)
                && position.y() == (piece.color() == Color.WHITE ? 0 : lastIndex);
    }
}