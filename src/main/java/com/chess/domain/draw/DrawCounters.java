package com.chess.domain.draw;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.chess.domain.command.IMoveInfo;
import com.chess.domain.model.PieceRoles;
import com.chess.domain.rule.CastlingRights;

/**
 * Contadores inmutables que leen las reglas de tablas: medias jugadas desde el último movimiento de peón o captura,
 * y cuántas veces ocurrió cada posición. Como {@link CastlingRights}, se crea una instancia nueva después de cada
 * jugada y la anterior se conserva en el historial, para que deshacer y rehacer sean consistentes.
 */
public record DrawCounters(
        int halfMoveClock,
        Map<PositionKey, Integer> seenPositions,
        PositionKey current,
        PieceRoles roles) {

    public DrawCounters {
        if (halfMoveClock < 0) {
            throw new IllegalArgumentException("halfMoveClock must not be negative: " + halfMoveClock);
        }
        Objects.requireNonNull(seenPositions, "seenPositions must not be null");
        Objects.requireNonNull(current, "current must not be null");
        Objects.requireNonNull(roles, "roles must not be null");
        seenPositions = Map.copyOf(seenPositions);
    }

    public static DrawCounters initial(PositionKey position, PieceRoles roles) {
        return new DrawCounters(0, Map.of(position, 1), position, roles);
    }

    /**
     * @param move la jugada recién ejecutada
     * @param newPosition la posición alcanzada por esa jugada
     */
    public DrawCounters after(IMoveInfo move, PositionKey newPosition) {
        Map<PositionKey, Integer> updated = new HashMap<>(seenPositions);
        updated.merge(newPosition, 1, Integer::sum);
        return new DrawCounters(nextHalfMoveClock(move), updated, newPosition, roles);
    }

    public int timesCurrentPositionSeen() {
        return seenPositions.getOrDefault(current, 0);
    }

    private int nextHalfMoveClock(IMoveInfo move) {
        return isIrreversible(move) ? 0 : halfMoveClock + 1;
    }

    private boolean isIrreversible(IMoveInfo move) {
        return move.movingPiece().type() == roles.pawn() || move.capturedPiece() != null;
    }
}