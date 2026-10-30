package com.chess.domain.rule;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

import java.util.Objects;


public record MoveRequest(Position from, Position to, PieceType promotionType) {

    public MoveRequest {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
    }
}