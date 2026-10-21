package com.chess.domain;

import java.util.Objects;

public final class MoveCommand implements ICommand {

    private final Board board;
    private final Position from;
    private final Position to;
    private final Piece movingPiece;
    private Piece capturedPiece;
    private boolean executed;

    public MoveCommand(Board board, Position from, Position to) {
        this.board = Objects.requireNonNull(board, "board must not be null");
        this.from = Objects.requireNonNull(from, "from must not be null");
        this.to = Objects.requireNonNull(to, "to must not be null");
        this.movingPiece = Objects.requireNonNull(board.pieceAt(from), "moving piece must not be null");
    }

    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException("Command has already been executed");
        }
        capturedPiece = board.movePiece(from, to);
        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) {
            throw new IllegalStateException("Command has not been executed");
        }
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
        return movingPiece;
    }

    @Override
    public Piece capturedPiece() {
        return capturedPiece;
    }

    @Override
    public String notation() {
        return Notation.square(from) + Notation.square(to);
    }
}