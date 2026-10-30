package com.chess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.chess.domain.port.GameRecord;
import com.chess.domain.model.GameResult;
import com.chess.domain.port.SavedGames;

final class SavedGamesTest {

    private static final Instant DATE = Instant.parse("2026-10-01T10:15:30Z");

    @Test
    void keepsTheRecordsAndTheNumberOfUnreadableRows() {
        GameRecord record = GameRecord.unsaved(DATE, GameResult.DRAW, List.of("e2e4"));

        SavedGames saved = new SavedGames(List.of(record), 3);

        assertEquals(List.of(record), saved.records());
        assertEquals(3, saved.unreadableRows());
    }

    @Test
    void aNegativeNumberOfUnreadableRowsIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new SavedGames(List.of(), -1));
    }

    @Test
    void missingRecordsAreRejected() {
        assertThrows(NullPointerException.class, () -> new SavedGames(null, 0));
    }

    @Test
    void changingTheOriginalListDoesNotChangeTheSavedGames() {
        List<GameRecord> records = new ArrayList<>();
        SavedGames saved = new SavedGames(records, 0);

        records.add(GameRecord.unsaved(DATE, GameResult.DRAW, List.of("e2e4")));

        assertEquals(List.of(), saved.records());
    }
}