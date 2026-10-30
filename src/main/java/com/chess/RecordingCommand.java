package com.chess;

import java.util.List;

import com.chess.domain.Color;
import com.chess.domain.ICommand;
import com.chess.domain.OneStepMoveStrategy;
import com.chess.domain.Piece;
import com.chess.domain.PieceType;
import com.chess.domain.Position;

/**
 * Command for state tests that only counts how many times it was executed.
 */
final class RecordingCommand implements ICommand {

    private final Piece movingPiece;
    private int executions;

    RecordingCommand(Color color) {
        this.movingPiece = new Piece(color, PieceType.KING, List.of(new OneStepMoveStrategy()));
    }

    int executions() {
        return executions;
    }

    @Override
    public void execute() {
        executions++;
    }

    @Override
    public void undo() {
        executions--;
    }

    @Override
    public Position from() {
        return new Position(0, 0);
    }

    @Override
    public Position to() {
        return new Position(0, 1);
    }

    @Override
    public Piece movingPiece() {
        return movingPiece;
    }

    @Override
    public Piece capturedPiece() {
        return null;
    }

    @Override
    public String notation() {
        return moveCode();
    }
}
