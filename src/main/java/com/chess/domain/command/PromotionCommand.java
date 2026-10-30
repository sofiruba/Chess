package com.chess.domain.command;

import com.chess.domain.model.*;


import java.util.Objects;

public final class PromotionCommand implements ICommand {

    private final Board board;
    private final Position from;
    private final Position to;
    private final Piece pawn;
    private final Piece promotedPiece;
    private Piece capturedPiece;
    private boolean executed;

    public PromotionCommand(Board board, Position from, Position to, Piece promotedPiece) {
        this.board = Objects.requireNonNull(board, "board must not be null");
        this.from = Objects.requireNonNull(from, "from must not be null");
        this.to = Objects.requireNonNull(to, "to must not be null");
        this.pawn = Objects.requireNonNull(board.pieceAt(from), "pawn must not be null");
        this.promotedPiece = Objects.requireNonNull(promotedPiece, "promotedPiece must not be null");
    }

    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException("Command has already been executed");
        }
        capturedPiece = board.movePiece(from, to);
        board.removePiece(to);
        board.placePiece(promotedPiece, to);
        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) {
            throw new IllegalStateException("Command has not been executed");
        }
        board.removePiece(to);
        board.placePiece(pawn, to);
        board.movePiece(to, from);
        if (capturedPiece != null) {
            board.placePiece(capturedPiece, to);
        }
        executed = false;
    }

    @Override
    public Position from() {
        return from;
    }

    @Override
    public Position to() {
        return to;
    }

    @Override
    public Piece movingPiece() {
        return pawn;
    }

    @Override
    public Piece capturedPiece() {
        return capturedPiece;
    }

    @Override
    public String notation() {
        return moveCode();
    }

    @Override
    public String moveCode() {
        return Notation.square(from) + Notation.square(to) + promotedPiece.type().notationCode();
    }
}