package com.chess.domain.power;

import java.util.List;

import com.chess.domain.model.Board;
import com.chess.domain.model.Piece;
import com.chess.domain.model.Position;

/** Caparazón: la pieza se queda en su lugar y la pieza enemiga alcanzada sale del tablero. */
final class ShellCommand extends PowerCommand {

    private Piece hit;

    ShellCommand(Board board, Position holder, Position target) {
        super(board, holder, target);
    }

    @Override
    protected void apply() {
        hit = board.removePiece(to());
        if (hit == null) {
            throw new IllegalStateException("There is no piece for the shell to hit at " + to());
        }
    }

    @Override
    protected void revert() {
        board.placePiece(hit, to());
        hit = null;
    }

    @Override
    public List<CapturedPiece> capturedPieces() {
        return hit == null ? List.of() : List.of(new CapturedPiece(hit, to()));
    }

    @Override
    public String notation() {
        return "Caparazón " + Squares.name(to());
    }
}
