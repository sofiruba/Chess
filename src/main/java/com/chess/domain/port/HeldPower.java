package com.chess.domain.port;

import java.util.Objects;

import com.chess.domain.model.Piece;
import com.chess.domain.model.Position;
import com.chess.domain.model.PowerType;

/** Un poder que una pieza tiene guardado, para mostrar a ambos jugadores qué poderes hay en juego. */
public record HeldPower(Piece piece, Position position, PowerType power) {

    public HeldPower {
        Objects.requireNonNull(piece, "piece must not be null");
        Objects.requireNonNull(position, "position must not be null");
        Objects.requireNonNull(power, "power must not be null");
    }
}
