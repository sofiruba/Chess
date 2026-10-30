package com.chess.domain.port;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * A finished or interrupted game as it is stored: when it was saved, how it stood and the move codes needed to
 * replay it. An id of {@link #UNSAVED_ID} means the repository has not stored it yet.
 */
public record GameRecord(long id, Instant playedAt, GameResult result, List<String> moveCodes) {

    public static final long UNSAVED_ID = 0;

    public GameRecord {
        if (id < 0) {
            throw new IllegalArgumentException("id must not be negative: " + id);
        }
        Objects.requireNonNull(playedAt, "playedAt must not be null");
        Objects.requireNonNull(result, "result must not be null");
        Objects.requireNonNull(moveCodes, "moveCodes must not be null");
        moveCodes = List.copyOf(moveCodes);
    }

    public static GameRecord unsaved(Instant playedAt, GameResult result, List<String> moveCodes) {
        return new GameRecord(UNSAVED_ID, playedAt, result, moveCodes);
    }
}