package com.chess.domain.model;

import java.util.Objects;

/**
 * Una coordenada. Solo sabe que no puede ser negativa; corresponde al {@link Board} que la recibe determinar si cabe.
 */
public record Position(int x, int y) {

    public Position {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Position must not be negative: (" + x + ", " + y + ")");
        }
    }

    public Position translate(int deltaX, int deltaY) {
        return new Position(x + deltaX, y + deltaY);
    }

    public boolean isAdjacentTo(Position other) {
        Objects.requireNonNull(other, "other must not be null");
        return Math.max(Math.abs(x - other.x), Math.abs(y - other.y)) == 1;
    }
}
