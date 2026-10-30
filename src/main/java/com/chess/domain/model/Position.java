package com.chess.domain.model;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

import java.util.Objects;

/**
 * A coordinate. It only knows it cannot be negative; whether it fits is up to the {@link Board} that receives it.
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
