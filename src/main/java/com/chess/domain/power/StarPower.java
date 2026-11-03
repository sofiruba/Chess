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
 * Estrella. En cualquier pieza menos el rey, la vuelve inmune durante el siguiente turno del rival: no puede ser
 * capturada, congelada ni alcanzada por un caparazón. En el rey, en cambio, explota: elimina a todas las piezas
 * enemigas de las ocho casillas que lo rodean (salvo las que sean inmunes). El objetivo es siempre la casilla de la
 * propia pieza.
 */
public final class StarPower implements IPower {

    private final PieceRoles roles;

    public StarPower(PieceRoles roles) {
        this.roles = Objects.requireNonNull(roles, "roles must not be null");
    }

    @Override
    public PowerType type() {
        return PowerType.STAR;
    }

    @Override
    public List<Position> targets(PowerContext context, Position holder) {
        return List.of(holder);
    }

    @Override
    public IPowerCommand createCommand(PowerContext context, Position holder, Position target) {
        if (!holder.equals(target)) {
            throw new IllegalArgumentException("The star is used on the piece itself, at " + holder);
        }
        Piece user = context.board().pieceAt(holder);
        if (user.type() == roles.king()) {
            return new StarBlastCommand(context.board(), holder, blastVictims(context, holder, user));
        }
        return new ImmunityCommand(context.board(), holder);
    }

    private List<Position> blastVictims(PowerContext context, Position king, Piece user) {
        Board board = context.board();
        List<Position> victims = new ArrayList<>();
        for (Position position : board.positions()) {
            Piece piece = board.pieceAt(position);
            if (piece != null
                    && king.isAdjacentTo(position)
                    && piece.color() != user.color()
                    && piece.type() != roles.king()
                    && context.powers().canBeCaptured(piece)) {
                victims.add(position);
            }
        }
        return victims;
    }
}
