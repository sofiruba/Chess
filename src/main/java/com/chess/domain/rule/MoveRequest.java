package com.chess.domain.rule;

import java.util.Objects;

import com.chess.domain.model.PieceType;
import com.chess.domain.model.Position;


public record MoveRequest(Position from, Position to, PieceType promotionType) {

    public MoveRequest {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
    }
}