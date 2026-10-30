package com.chess.domain;

import java.util.List;
import java.util.Optional;

/**
 * En passant is derived from the last move only, so it needs no state and stays consistent with undo/redo.
 */
public final class EnPassantRule implements IMoveRule {

    @Override
    public List<Position> extraMoves(MoveRuleContext context, Position from) {
        Position target = enPassantTarget(context, from);
        return target == null ? List.of() : List.of(target);
    }

    @Override
    public boolean appliesTo(MoveRuleContext context, MoveRequest request) {
        return request.to().equals(enPassantTarget(context, request.from()));
    }

    @Override
    public ICommand createCommand(MoveRuleContext context, MoveRequest request) {
        return new EnPassantCommand(context.board(), request.from(), request.to());
    }

    private Position enPassantTarget(MoveRuleContext context, Position from) {
        Piece pawn = context.board().pieceAt(from);
        if (pawn == null || pawn.type() != PieceType.PAWN) {
            return null;
        }
        return context.lastMove().flatMap(lastMove -> capturableSquare(lastMove, pawn, from)).orElse(null);
    }

    private Optional<Position> capturableSquare(IMoveInfo lastMove, Piece pawn, Position from) {
        boolean capturable = lastMove.movingColor() != pawn.color() && isBeside(lastMove.to(), from);
        return capturable ? lastMove.enPassantSquare() : Optional.empty();
    }

    private boolean isBeside(Position enemyPawn, Position from) {
        return enemyPawn.y() == from.y() && Math.abs(enemyPawn.x() - from.x()) == 1;
    }
}