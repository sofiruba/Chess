package com.chess.domain;

import java.util.Objects;

public record Position(int x, int y) {

    public Position {
        if (x < 0 || x >= Board.SIZE || y < 0 || y >= Board.SIZE) {
            throw new IllegalArgumentException("Position must be inside the board: (" + x + ", " + y + ")");
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
