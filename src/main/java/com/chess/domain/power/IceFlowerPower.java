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
 * Flor de hielo: congela a una pieza enemiga durante el siguiente turno de su dueño. Una pieza congelada no puede
 * moverse ni usar su poder, pero sigue amenazando casillas (puede dar jaque) y puede ser capturada. El rey es inmune
 * al hielo, igual que las piezas protegidas por una estrella.
 */
public final class IceFlowerPower implements IPower {

    private final PieceRoles roles;

    public IceFlowerPower(PieceRoles roles) {
        this.roles = Objects.requireNonNull(roles, "roles must not be null");
    }

    @Override
    public PowerType type() {
        return PowerType.ICE_FLOWER;
    }

    @Override
    public List<Position> targets(PowerContext context, Position holder) {
        Board board = context.board();
        Piece user = board.pieceAt(holder);
        List<Position> targets = new ArrayList<>();
        for (Position position : board.positions()) {
            Piece piece = board.pieceAt(position);
            if (piece != null
                    && piece.color() != user.color()
                    && piece.type() != roles.king()
                    && context.powers().canBeCaptured(piece)) {
                targets.add(position);
            }
        }
        return targets;
    }

    @Override
    public IPowerCommand createCommand(PowerContext context, Position holder, Position target) {
        return new FreezeCommand(context.board(), holder, target);
    }
}
