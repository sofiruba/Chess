package com.chess.domain.power;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.chess.domain.model.Board;
import com.chess.domain.model.Piece;
import com.chess.domain.model.PieceRoles;
import com.chess.domain.model.Position;
import com.chess.domain.model.PowerType;

/**
 * Pluma: la pieza vuela a cualquier casilla vacía. Como todo uso de poder, no puede dejar al propio rey en jaque
 * (eso cubre el caso del rey que vuela a una casilla atacada). Un peón no puede volar a la primera ni a la última
 * fila, porque ahí no tiene sentido un peón: no se puede promocionar volando.
 */
public final class FeatherPower implements IPower {

    private final PieceRoles roles;

    public FeatherPower(PieceRoles roles) {
        this.roles = Objects.requireNonNull(roles, "roles must not be null");
    }

    @Override
    public PowerType type() {
        return PowerType.FEATHER;
    }

    @Override
    public List<Position> targets(PowerContext context, Position holder) {
        Board board = context.board();
        Piece user = board.pieceAt(holder);
        List<Position> targets = new ArrayList<>();
        for (Position position : board.positions()) {
            if (board.pieceAt(position) == null && allowedFor(user, position, board)) {
                targets.add(position);
            }
        }
        return targets;
    }

    @Override
    public IPowerCommand createCommand(PowerContext context, Position holder, Position target) {
        return new FeatherCommand(context.board(), holder, target);
    }

    private boolean allowedFor(Piece piece, Position destination, Board board) {
        boolean edgeRank = destination.y() == 0 || destination.y() == board.size() - 1;
        return piece.type() != roles.pawn() || !edgeRank;
    }
}
