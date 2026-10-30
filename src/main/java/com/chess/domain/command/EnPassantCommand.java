package com.chess.domain.command;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

import java.util.Objects;

public final class EnPassantCommand implements ICommand {

    private final Board board;
    private final Position from;
    private final Position to;
    private final Position capturedPawnPosition;
    private final Piece movingPiece;
    private Piece capturedPiece;
    private boolean executed;

    public EnPassantCommand(Board board, Position from, Position to) {
        this.board = Objects.requireNonNull(board, "board must not be null");
        this.from = Objects.requireNonNull(from, "from must not be null");
        this.to = Objects.requireNonNull(to, "to must not be null");
        this.capturedPawnPosition = new Position(to.x(), from.y());
        this.movingPiece = Objects.requireNonNull(board.pieceAt(from), "moving piece must not be null");
    }

    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException("Command has already been executed");
        }
        capturedPiece = board.removePiece(capturedPawnPosition);
        if (capturedPiece == null) {
            throw new IllegalStateException("There is no pawn to capture en passant");
        }
        board.movePiece(from, to);
        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) {
            throw new IllegalStateException("Command has not been executed");
        }
        board.movePiece(to, from);
        board.placePiece(capturedPiece, capturedPawnPosition);
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
        return moveCode() + "e";
    }
}