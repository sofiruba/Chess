package com.chess.domain.port;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import com.chess.domain.model.GameResult;

/**
 * Una partida finalizada o interrumpida tal como se almacena: cuándo se guardó, cómo estaba y los códigos de jugada
 * necesarios para reproducirla. Un id de {@link #UNSAVED_ID} indica que el repositorio aún no la almacenó.
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
