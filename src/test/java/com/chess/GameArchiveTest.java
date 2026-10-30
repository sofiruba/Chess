package com.chess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.chess.domain.game.GameArchive;
import com.chess.domain.model.GameResult;
import com.chess.domain.port.GameRecord;
import com.chess.domain.port.IGameRepository;
import com.chess.domain.port.IGameService;
import com.chess.domain.port.SavedGames;

final class GameArchiveTest {

    private static final Instant NOW = Instant.parse("2026-10-30T12:00:00Z");
    private static final long GENERATED_ID = 7;

    private final FakeRepository repository = new FakeRepository();
    private final GameArchive archive = new GameArchive(repository, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void savesTheGameWithTheClockTimeItsResultAndItsMoveCodes() {
        IGameService game = TestGames.standard();
        TestGames.play(game, "e2e4", "e7e5");

        GameRecord stored = archive.save(game);

        GameRecord requested = repository.saved.get(0);
        assertEquals(GameRecord.UNSAVED_ID, requested.id());
        assertEquals(NOW, requested.playedAt());
        assertEquals(GameResult.IN_PROGRESS, requested.result());
        assertEquals(List.of("e2e4", "e7e5"), requested.moveCodes());
        assertEquals(GENERATED_ID, stored.id());
    }

    @Test
    void listsTheGamesOfTheRepository() {
        SavedGames expected = new SavedGames(List.of(), 2);
        repository.listing = expected;

        assertSame(expected, archive.findAll());
    }

    @Test
    void rejectsMissingCollaborators() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

        assertThrows(NullPointerException.class, () -> new GameArchive(null, clock));
        assertThrows(NullPointerException.class, () -> new GameArchive(repository, null));
        assertThrows(NullPointerException.class, () -> archive.save(null));
    }

    @Test
    void theScreenClassesDoNotReceiveTheRepository() throws ClassNotFoundException {
        for (String className : List.of("com.chess.ui.ChessApplication", "com.chess.ui.SidePanel")) {
            Class<?> type = Class.forName(className, false, GameArchiveTest.class.getClassLoader());
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                List<Class<?>> parameters = Arrays.asList(constructor.getParameterTypes());
                assertFalse(parameters.contains(IGameRepository.class), className + " must not receive the repository");
            }
        }
    }

    private static final class FakeRepository implements IGameRepository {

        private final List<GameRecord> saved = new ArrayList<>();
        private SavedGames listing = new SavedGames(List.of(), 0);

        @Override
        public GameRecord save(GameRecord record) {
            saved.add(record);
            return new GameRecord(GENERATED_ID, record.playedAt(), record.result(), record.moveCodes());
        }

        @Override
        public SavedGames findAll() {
            return listing;
        }
    }
}