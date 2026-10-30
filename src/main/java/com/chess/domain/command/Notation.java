package com.chess.domain.command;

import com.chess.domain.model.*;


final class Notation {

    private Notation() {
    }

    static String square(Position position) {
        return String.valueOf((char) ('a' + position.x())) + (position.y() + 1);
    }
}