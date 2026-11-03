package com.chess.domain.power;

import java.util.List;

import com.chess.domain.model.Board;
import com.chess.domain.model.Position;

/**
 * Pluma: la pieza vuela a una casilla vacía. Es el único poder que mueve a quien lo usa, por lo que un rey o una
 * torre que vuelan pierden el derecho a enrocar, igual que si se hubieran movido.
 */
final class FeatherCommand extends PowerCommand {

    FeatherCommand(Board board, Position holder, Position destination) {
        super(board, holder, destination);
    }

    @Override
    protected void apply() {
        if (board.pieceAt(to()) != null) {
            throw new IllegalStateException("The feather needs an empty square but " + to() + " is occupied");
        }
        board.movePiece(from(), to());
    }

    @Override
    protected void revert() {
        board.movePiece(to(), from());
    }

    @Override
    public List<MovedPiece> movedPieces() {
        return List.of(new MovedPiece(holder(), from()));
    }

    @Override
    public String notation() {
        return "Pluma " + Squares.name(from()) + "-" + Squares.name(to());
    }
}
