package com.chess;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.chess.domain.model.GameResult;
import com.chess.domain.port.GameRecord;
import com.chess.persistence.SqliteGameRepository;

/** El contenido de las cajas se guarda con la partida, y las bases anteriores a las cajas se siguen leyendo. */
final class SqliteSetupColumnTest {

    private static final Instant DATE = Instant.parse("2026-11-03T10:00:00Z");

    @TempDir
    Path folder;

    @Test
    void theBoxContentsAreSavedAndRead() {
        SqliteGameRepository repository = new SqliteGameRepository(folder.resolve("boxes.db").toString());

        repository.save(GameRecord.unsaved(DATE, GameResult.IN_PROGRESS, List.of("h2h4", "@h4h7"), "a5=i,h4=t"));

        GameRecord read = repository.findAll().records().get(0);
        assertEquals("a5=i,h4=t", read.setupCode());
        assertEquals(List.of("h2h4", "@h4h7"), read.moveCodes());
    }

    @Test
    void aDatabaseCreatedBeforeTheBoxesIsMigrated() throws Exception {
        String path = folder.resolve("old.db").toString();
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + path);
                Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE games (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "played_at TEXT NOT NULL, result TEXT NOT NULL, moves TEXT NOT NULL)");
            statement.executeUpdate("INSERT INTO games (played_at, result, moves) "
                    + "VALUES ('2026-01-01T10:00:00Z', 'DRAW', 'e2e4 e7e5')");
        }

        SqliteGameRepository repository = new SqliteGameRepository(path);
        repository.save(GameRecord.unsaved(DATE, GameResult.WHITE_WINS, List.of("d2d4"), "c5=s"));

        List<GameRecord> records = repository.findAll().records();
        assertEquals(2, records.size());
        assertEquals("c5=s", records.get(0).setupCode());
        assertEquals("", records.get(1).setupCode());
        assertEquals(List.of("e2e4", "e7e5"), records.get(1).moveCodes());
    }
}
