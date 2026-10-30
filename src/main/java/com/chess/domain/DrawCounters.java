package com.chess.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable counters the draw rules read: half-moves since the last pawn move or capture, and how many times each
 * position has occurred. Like {@link CastlingRights}, a new instance is produced after every move and the previous
 * one is kept in the history, so undo and redo stay consistent.
 */
public record DrawCounters(int halfMoveClock, Map<PositionKey, Integer> seenPositions, PositionKey current) {

    public DrawCounters {
        if (halfMoveClock < 0) {
            throw new IllegalArgumentException("halfMoveClock must not be negative: " + halfMoveClock);
        }
        Objects.requireNonNull(seenPositions, "seenPositions must not be null");
        Objects.requireNonNull(current, "current must not be null");
        seenPositions = Map.copyOf(seenPositions);
    }

    public static DrawCounters initial(PositionKey position) {
        return new DrawCounters(0, Map.of(position, 1), position);
    }

    /**
     * @param move the move just executed
     * @param newPosition the position reached by that move
     */
    public DrawCounters after(IMoveInfo move, PositionKey newPosition) {
        Map<PositionKey, Integer> updated = new HashMap<>(seenPositions);
        updated.merge(newPosition, 1, Integer::sum);
        return new DrawCounters(nextHalfMoveClock(move), updated, newPosition);
    }

    public int timesCurrentPositionSeen() {
        return seenPositions.getOrDefault(current, 0);
    }

    private int nextHalfMoveClock(IMoveInfo move) {
        return isIrreversible(move) ? 0 : halfMoveClock + 1;
    }

    private boolean isIrreversible(IMoveInfo move) {
        return move.movingPiece().type() == PieceType.PAWN || move.capturedPiece() != null;
    }
}