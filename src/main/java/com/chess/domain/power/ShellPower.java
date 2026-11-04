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
 * Caparazón: se tira en línea recta hacia arriba, abajo, izquierda o derecha y elimina a la primera pieza que
 * encuentra si es enemiga. No atraviesa piezas: si la primera es propia, un rey (inmune por su tamaño) o una pieza
 * inmune, el caparazón se detiene sin efecto. Solo se ofrecen las direcciones en las que elimina a alguien, y el
 * objetivo es la casilla de esa pieza. La pieza que lo tira se queda en su lugar.
 */
public final class ShellPower implements IPower {

    private static final int[][] DIRECTIONS = {{0, 1}, {0, -1}, {-1, 0}, {1, 0}};

    private final PieceRoles roles;

    public ShellPower(PieceRoles roles) {
        this.roles = Objects.requireNonNull(roles, "roles must not be null");
    }

    @Override
    public PowerType type() {
        return PowerType.SHELL;
    }

    @Override
    public List<Position> targets(PowerContext context, Position holder) {
        Piece user = context.board().pieceAt(holder);
        List<Position> targets = new ArrayList<>();
        for (int[] direction : DIRECTIONS) {
            Position hit = firstPieceFrom(context.board(), holder, direction);
            if (hit != null && canKnockOut(context, user, context.board().pieceAt(hit))) {
                targets.add(hit);
            }
        }
        return targets;
    }

    @Override
    public IPowerCommand createCommand(PowerContext context, Position holder, Position target) {
        return new ShellCommand(context.board(), holder, target);
    }

    private Position firstPieceFrom(Board board, Position origin, int[] direction) {
        int x = origin.x() + direction[0];
        int y = origin.y() + direction[1];
        while (board.contains(x, y)) {
            Position candidate = new Position(x, y);
            if (board.pieceAt(candidate) != null) {
                return candidate;
            }
            x += direction[0];
            y += direction[1];
        }
        return null;
    }

    private boolean canKnockOut(PowerContext context, Piece user, Piece hit) {
        return hit.color() != user.color()
                && hit.type() != roles.king()
                && context.powers().canBeCaptured(hit);
    }
}
