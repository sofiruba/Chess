package com.chess.domain.power;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.chess.domain.model.Board;
import com.chess.domain.model.Position;

/** Estrella del rey: elimina a las piezas enemigas indicadas, que son las que rodean al rey. */
final class StarBlastCommand extends PowerCommand {

    private final List<Position> victims;
    private final List<CapturedPiece> removed = new ArrayList<>();

    StarBlastCommand(Board board, Position king, List<Position> victims) {
        super(board, king, king);
        this.victims = List.copyOf(Objects.requireNonNull(victims, "victims must not be null"));
    }

    @Override
    protected void apply() {
        removed.clear();
        for (Position victim : victims) {
            removed.add(new CapturedPiece(board.removePiece(victim), victim));
        }
    }

    @Override
    protected void revert() {
        for (CapturedPiece captured : removed) {
            board.placePiece(captured.piece(), captured.position());
        }
        removed.clear();
    }

    @Override
    public List<CapturedPiece> capturedPieces() {
        return List.copyOf(removed);
    }

    @Override
    public String notation() {
        return "Estrella " + Squares.name(from()) + " (x" + victims.size() + ")";
    }
}
