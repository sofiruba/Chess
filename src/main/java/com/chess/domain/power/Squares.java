package com.chess.domain.power;

import com.chess.domain.model.Position;

/** Nombres de casillas en notación algebraica ("e4") para la notación y los códigos de los poderes. */
final class Squares {

    private Squares() {
    }

    static String name(Position position) {
        return String.valueOf((char) ('a' + position.x())) + (position.y() + 1);
    }
}
