package com.chess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.chess.domain.Color;
import com.chess.domain.GameRecord;
import com.chess.domain.GameResult;
import com.chess.domain.IGameService;
import com.chess.domain.PieceType;
import com.chess.persistence.SqliteGameRepository;

final class SqliteGameRepositoryTest {

    private static final Instant FIRST_DATE = Instant.parse("2026-10-01T10:15:30Z");
    private static final Instant SECOND_DATE = Instant.parse("2026-10-02T18:00:00Z");

    @TempDir
    Path directory;

    private Path databaseFile;
    private SqliteGameRepository repository;

    @BeforeEach
    void setUp() {
        databaseFile = directory.resolve("chess-test.db");
        repository = new SqliteGameRepository(databaseFile.toString());
    }

    @Test
    void theDatabaseFileIsCreatedWhenTheRepositoryStarts() {
        assertTrue(Files.exists(databaseFile));
    }

    @Test
    void aNewRepositoryHasNoGames() {
        assertEquals(List.of(), repository.findAll());
    }

    @Test
    void savedGameGetsAnIdAndKeepsItsData() {
        GameRecord unsaved = GameRecord.unsaved(FIRST_DATE, GameResult.DRAW, List.of("e2e4", "e7e5"));

        GameRecord saved = repository.save(unsaved);

        assertTrue(saved.id() > GameRecord.UNSAVED_ID);
        assertEquals(FIRST_DATE, saved.playedAt());
        assertEquals(GameResult.DRAW, saved.result());
        assertEquals(List.of("e2e4", "e7e5"), saved.moveCodes());
    }

    @Test
    void gamesAreListedNewestFirstWithTheirData() {
        GameRecord first = repository.save(GameRecord.unsaved(FIRST_DATE, GameResult.IN_PROGRESS, List.of("e2e4")));
        GameRecord second = repository.save(GameRecord.unsaved(SECOND_DATE, GameResult.WHITE_WINS, List.of("d2d4")));

        List<GameRecord> games = repository.findAll();

        assertEquals(List.of(second, first), games);
    }

    @Test
    void aGameWithoutMovesIsStoredAndRead() {
        repository.save(GameRecord.unsaved(FIRST_DATE, GameResult.IN_PROGRESS, List.of()));

        List<GameRecord> games = repository.findAll();

        assertEquals(1, games.size());
        assertEquals(List.of(), games.get(0).moveCodes());
    }

    @Test
    void aSavedGameCanBeLoadedAndItsMoveCodesReplayed() {
        IGameService original = TestGames.standard();
        TestGames.play(original, "e2e4", "e7e5", "g1f3");
        repository.save(GameRecord.unsaved(FIRST_DATE, original.result(), original.moveCodes()));

        GameRecord loaded = repository.findAll().get(0);
        IGameService replayed = TestGames.standard();
        loaded.moveCodes().forEach(replayed::playMoveCode);

        assertEquals(List.of("e2e4", "e7e5", "g1f3"), replayed.moveCodes());
        assertEquals(Color.BLACK, replayed.turn());
        assertEquals(PieceType.KNIGHT, replayed.pieceAt(TestGames.at("f3")).type());
    }

    @Test
    void aReplayedGameCanBeUndoneAndRedone() {
        repository.save(GameRecord.unsaved(FIRST_DATE, GameResult.IN_PROGRESS, List.of("e2e4", "e7e5")));
        IGameService replayed = TestGames.standard();
        repository.findAll().get(0).moveCodes().forEach(replayed::playMoveCode);

        replayed.undoLastMove();
        replayed.redoLastMove();

        assertEquals(List.of("e2e4", "e7e5"), replayed.moveCodes());
    }

    @Test
    void gamesSurviveOpeningTheDatabaseAgain() {
        repository.save(GameRecord.unsaved(FIRST_DATE, GameResult.DRAW, List.of("e2e4")));

        SqliteGameRepository reopened = new SqliteGameRepository(databaseFile.toString());

        assertEquals(1, reopened.findAll().size());
    }

    @Test
    void aRowWithAnUnknownResultIsLeftOutOfTheList() throws SQLException {
        repository.save(GameRecord.unsaved(FIRST_DATE, GameResult.DRAW, List.of("e2e4")));
        insertRawRow("2026-10-03T00:00:00Z", "NOT_A_RESULT", "e2e4");

        List<GameRecord> games = repository.findAll();

        assertEquals(1, games.size());
        assertEquals(FIRST_DATE, games.get(0).playedAt());
    }

    @Test
    void aRowWithAnInvalidDateIsLeftOutOfTheList() throws SQLException {
        insertRawRow("yesterday", "DRAW", "e2e4");

        assertEquals(List.of(), repository.findAll());
    }

    @Test
    void aRowWithOldDisplayNotationIsListedButCannotBeReplayed() throws SQLException {
        insertRawRow("2026-10-03T00:00:00Z", "DRAW", "e2e4 O-O");
        GameRecord damaged = repository.findAll().get(0);
        IGameService game = TestGames.standard();

        assertThrows(IllegalArgumentException.class, () -> damaged.moveCodes().forEach(game::playMoveCode));
    }

    @Test
    void anAlreadySavedRecordCannotBeSavedAgain() {
        GameRecord saved = repository.save(GameRecord.unsaved(FIRST_DATE, GameResult.DRAW, List.of("e2e4")));

        assertThrows(IllegalArgumentException.class, () -> repository.save(saved));
    }

    @Test
    void anUnreachableDatabaseFailsWithAClearMessage() {
        String missingFolder = directory.resolve("missing").resolve("chess.db").toString();

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> new SqliteGameRepository(missingFolder));

        assertEquals("Cannot initialize the database", exception.getMessage());
    }

    private void insertRawRow(String playedAt, String result, String moves) throws SQLException {
        String sql = "INSERT INTO games (played_at, result, moves) VALUES ('"
                + playedAt + "', '" + result + "', '" + moves + "')";
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
}