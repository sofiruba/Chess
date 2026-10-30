package com.chess.domain.rule;

import com.chess.domain.model.*;


import java.util.Objects;


public record MoveRequest(Position from, Position to, PieceType promotionType) {

    public MoveRequest {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
    }
}