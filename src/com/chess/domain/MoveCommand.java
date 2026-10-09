package com.chess.domain;

import java.util.Objects;

public final class MoveCommand implements ICommand {

    private final Board board;
    private final Position from;
    private final Position to;
    private Piece movingPiece;
    private Piece capturedPiece;
    private boolean executed;

    public MoveCommand(Board board, Position from, Position to) {
        this.board = Objects.requireNonNull(board, "board must not be null");
        this.from = Objects.requireNonNull(from, "from must not be null");
        this.to = Objects.requireNonNull(to, "to must not be null");
    }

    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException("Command has already been executed");
        }
        movingPiece = board.pieceAt(from);
        if (movingPiece == null) {
            throw new IllegalArgumentException("There is no piece at " + from);
        }
        if (!movingPiece.getValidMoves(board, from).contains(to)) {
            throw new IllegalArgumentException("Illegal movement from " + from + " to " + to);
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

    public Color movingColor() {
        Piece piece = movingPiece == null ? board.pieceAt(from) : movingPiece;
        if (piece == null) {
            throw new IllegalStateException("There is no moving piece at " + from);
        }
        return piece.color();
    }

    public Position from() {
        return from;
    }

    public Position to() {
        return to;
    }
}
