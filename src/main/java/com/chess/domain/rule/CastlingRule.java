package com.chess.domain.rule;

import com.chess.domain.model.*;

import com.chess.domain.command.*;


import java.util.ArrayList;
import java.util.List;

/**
 * Enroque con el rey en la columna size / 2 y las torres en las esquinas. El rey siempre se mueve dos casillas, por
 * lo que debe haber al menos tres columnas entre el rey y la torre; los tableros más pequeños no tienen enroque.
 */
public final class CastlingRule implements IMoveRule {

    private static final int[] DIRECTIONS = {-1, 1};
    private static final int MIN_KING_ROOK_DISTANCE = 3;

    @Override
    public List<Position> extraMoves(MoveRuleContext context, Position from) {
        Board board = context.board();
        Piece king = board.pieceAt(from);
        if (!isKingOnHomeSquare(board, king, from) || context.checkDetector().isInCheck(board, king.color())) {
            return List.of();
        }
        List<Position> destinations = new ArrayList<>();
        for (int direction : DIRECTIONS) {
            if (canCastle(context, king, from, direction)) {
                destinations.add(from.translate(2 * direction, 0));
            }
        }
        return destinations;
    }

    @Override
    public boolean appliesTo(MoveRuleContext context, MoveRequest request) {
        Piece piece = context.board().pieceAt(request.from());
        return piece != null
                && piece.type() == PieceType.KING
                && Math.abs(request.to().x() - request.from().x()) == 2;
    }

    @Override
    public ICommand createCommand(MoveRuleContext context, MoveRequest request) {
        return new CastlingCommand(context.board(), request.from(), request.to());
    }

    private boolean isKingOnHomeSquare(Board board, Piece piece, Position position) {
        return piece != null
                && piece.type() == PieceType.KING
                && position.x() == board.size() / 2
                && position.y() == homeRank(board, piece.color());
    }

    private boolean canCastle(MoveRuleContext context, Piece king, Position from, int direction) {
        Board board = context.board();
        Color color = king.color();
        Position rookSquare = rookSquare(board, from, direction);
        return context.castlingRights().canCastle(color, direction)
                && hasRoomToCastle(from, rookSquare)
                && hasOwnRook(board, color, rookSquare)
                && isPathClear(board, from, rookSquare, direction)
                && isKingPathSafe(context, from, direction, color.opposite());
    }

    private Position rookSquare(Board board, Position king, int direction) {
        return new Position(direction < 0 ? 0 : board.size() - 1, king.y());
    }

    private boolean hasRoomToCastle(Position king, Position rookSquare) {
        return Math.abs(rookSquare.x() - king.x()) >= MIN_KING_ROOK_DISTANCE;
    }

    private boolean hasOwnRook(Board board, Color color, Position square) {
        Piece piece = board.pieceAt(square);
        return piece != null && piece.type() == PieceType.ROOK && piece.color() == color;
    }

    private boolean isPathClear(Board board, Position king, Position rookSquare, int direction) {
        for (int x = king.x() + direction; x != rookSquare.x(); x += direction) {
            if (board.pieceAt(new Position(x, king.y())) != null) {
                return false;
            }
        }
        return true;
    }

    private boolean isKingPathSafe(MoveRuleContext context, Position king, int direction, Color enemy) {
        Position middle = king.translate(direction, 0);
        Position destination = king.translate(2 * direction, 0);
        return !context.checkDetector().isSquareAttacked(context.board(), middle, enemy)
                && !context.checkDetector().isSquareAttacked(context.board(), destination, enemy);
    }

    private int homeRank(Board board, Color color) {
        return color == Color.WHITE ? 0 : board.size() - 1;
    }
}
