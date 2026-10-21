package com.chess.domain;

import java.util.List;

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
        ICommand lastMove = context.lastMove();
        if (pawn == null || pawn.type() != PieceType.PAWN || lastMove == null) {
            return null;
        }
        boolean capturable = lastMove.movingColor() != pawn.color()
                && isPawnDoubleStep(lastMove)
                && isBeside(lastMove.to(), from);
        return capturable ? skippedSquare(lastMove) : null;
    }

    private boolean isPawnDoubleStep(ICommand move) {
        return move.movingPiece().type() == PieceType.PAWN
                && Math.abs(move.to().y() - move.from().y()) == 2;
    }

    private boolean isBeside(Position enemyPawn, Position from) {
        return enemyPawn.y() == from.y() && Math.abs(enemyPawn.x() - from.x()) == 1;
    }

    private Position skippedSquare(ICommand doubleStep) {
        return new Position(doubleStep.to().x(), (doubleStep.from().y() + doubleStep.to().y()) / 2);
    }
}