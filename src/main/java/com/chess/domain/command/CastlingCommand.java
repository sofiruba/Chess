package com.chess.domain.command;

import com.chess.domain.model.*;


import java.util.List;
import java.util.Objects;

public final class CastlingCommand implements ICommand {

    private final Board board;
    private final Position kingFrom;
    private final Position kingTo;
    private final Position rookFrom;
    private final Position rookTo;
    private final Piece king;
    private final Piece rook;
    private boolean executed;

    public CastlingCommand(Board board, Position kingFrom, Position kingTo) {
        this.board = Objects.requireNonNull(board, "board must not be null");
        this.kingFrom = Objects.requireNonNull(kingFrom, "kingFrom must not be null");
        this.kingTo = Objects.requireNonNull(kingTo, "kingTo must not be null");
        int direction = Integer.signum(kingTo.x() - kingFrom.x());
        int rookFile = direction > 0 ? board.size() - 1 : 0;
        this.rookFrom = new Position(rookFile, kingFrom.y());
        this.rookTo = new Position(kingFrom.x() + direction, kingFrom.y());
        this.king = Objects.requireNonNull(board.pieceAt(kingFrom), "king must not be null");
        this.rook = Objects.requireNonNull(board.pieceAt(rookFrom), "rook must not be null");
    }

    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException("Command has already been executed");
        }
        board.movePiece(kingFrom, kingTo);
        board.movePiece(rookFrom, rookTo);
        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) {
            throw new IllegalStateException("Command has not been executed");
        }
        board.movePiece(rookTo, rookFrom);
        board.movePiece(kingTo, kingFrom);
        executed = false;
    }

    @Override
    public Position from() {
        return kingFrom;
    }

    @Override
    public Position to() {
        return kingTo;
    }

    @Override
    public Piece movingPiece() {
        return king;
    }

    @Override
    public Piece capturedPiece() {
        return null;
    }

    @Override
    public String notation() {
        return kingTo.x() > kingFrom.x() ? "O-O" : "O-O-O";
    }

    @Override
    public List<MovedPiece> movedPieces() {
        return List.of(new MovedPiece(king, kingFrom), new MovedPiece(rook, rookFrom));
    }
}
