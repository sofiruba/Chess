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
import java.util.Optional;

/**
 * What a rule needs to know about the current position. The last move is read-only and empty before the first move.
 */
public record MoveRuleContext(
        Board board,
        ICheckDetector checkDetector,
        Optional<IMoveInfo> lastMove,
        CastlingRights castlingRights) {

    public MoveRuleContext {
        Objects.requireNonNull(board, "board must not be null");
        Objects.requireNonNull(checkDetector, "checkDetector must not be null");
        Objects.requireNonNull(lastMove, "lastMove must not be null, use Optional.empty()");
        Objects.requireNonNull(castlingRights, "castlingRights must not be null");
    }
}