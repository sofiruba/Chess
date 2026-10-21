package com.chess.domain;

final class Notation {

    private Notation() {
    }

    static String square(Position position) {
        return String.valueOf((char) ('a' + position.x())) + (position.y() + 1);
    }
}