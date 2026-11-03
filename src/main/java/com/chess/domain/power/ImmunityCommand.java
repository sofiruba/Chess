package com.chess.domain.power;

import com.chess.domain.model.Board;
import com.chess.domain.model.Position;

/** Estrella de una pieza que no es el rey: no cambia el tablero, solo la vuelve inmune. */
final class ImmunityCommand extends PowerCommand {

    ImmunityCommand(Board board, Position holder) {
        super(board, holder, holder);
    }

    @Override
    protected void apply() {
    }

    @Override
    protected void revert() {
    }

    @Override
    public PowerState applyEffect(PowerState powers) {
        return powers.withImmune(holder());
    }

    @Override
    public String notation() {
        return "Estrella " + Squares.name(from());
    }
}
