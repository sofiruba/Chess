package com.chess.domain.power;

import java.util.Objects;

import com.chess.domain.model.Board;
import com.chess.domain.model.Piece;
import com.chess.domain.model.Position;

/** Flor de hielo: no cambia el tablero, solo congela a la pieza objetivo. */
final class FreezeCommand extends PowerCommand {

    private final Piece target;

    FreezeCommand(Board board, Position holder, Position target) {
        super(board, holder, target);
        this.target = Objects.requireNonNull(board.pieceAt(target), "target piece must not be null");
    }

    @Override
    protected void apply() {
    }

    @Override
    protected void revert() {
    }

    @Override
    public PowerState applyEffect(PowerState powers) {
        return powers.withFrozen(target);
    }

    @Override
    public String notation() {
        return "Hielo " + Squares.name(to());
    }
}
