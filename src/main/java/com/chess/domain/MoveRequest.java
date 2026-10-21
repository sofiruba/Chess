package com.chess.domain;

import java.util.Objects;


public record MoveRequest(Position from, Position to, PieceType promotionType) {

    public MoveRequest {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
    }
}