package com.chess.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.chess.domain.GameRecord;
import com.chess.domain.GameResult;
import com.chess.domain.IGameRepository;

/**
 * SQLite adapter of the game history. Design decision: only the history of games is persisted, there are no users.
 * The database file and the table are created when the repository is built. A row that cannot be read (bad date or
 * result) is left out of {@link #findAll()} so one damaged row does not hide the others.
 */
public final class SqliteGameRepository implements IGameRepository {

    private static final String MOVE_SEPARATOR = " ";
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS games ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "played_at TEXT NOT NULL, "
            + "result TEXT NOT NULL, "
            + "moves TEXT NOT NULL)";
    private static final String INSERT = "INSERT INTO games (played_at, result, moves) VALUES (?, ?, ?)";
    private static final String SELECT_ALL = "SELECT id, played_at, result, moves FROM games ORDER BY id DESC";

    private final String url;

    public SqliteGameRepository(String databasePath) {
        Objects.requireNonNull(databasePath, "databasePath must not be null");
        this.url = "jdbc:sqlite:" + databasePath;
        createTable();
    }

    @Override
    public GameRecord save(GameRecord record) {
        Objects.requireNonNull(record, "record must not be null");
        if (record.id() != GameRecord.UNSAVED_ID) {
            throw new IllegalArgumentException("Only an unsaved record can be saved, but its id is " + record.id());
        }
        try (Connection connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, record);
            statement.executeUpdate();
            return withGeneratedId(statement, record);
        } catch (SQLException exception) {
            throw new IllegalStateException("Cannot save the game", exception);
        }
    }

    @Override
    public List<GameRecord> findAll() {
        try (Connection connection = DriverManager.getConnection(url);
                Statement statement = connection.createStatement();
                ResultSet rows = statement.executeQuery(SELECT_ALL)) {
            return readRows(rows);
        } catch (SQLException exception) {
            throw new IllegalStateException("Cannot read the saved games", exception);
        }
    }

    private void createTable() {
        try (Connection connection = DriverManager.getConnection(url);
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_TABLE);
        } catch (SQLException exception) {
            throw new IllegalStateException("Cannot initialize the database", exception);
        }
    }

    private void bind(PreparedStatement statement, GameRecord record) throws SQLException {
        statement.setString(1, record.playedAt().toString());
        statement.setString(2, record.result().name());
        statement.setString(3, String.join(MOVE_SEPARATOR, record.moveCodes()));
    }

    private GameRecord withGeneratedId(PreparedStatement statement, GameRecord record) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new IllegalStateException("The database did not return the id of the saved game");
            }
            return new GameRecord(keys.getLong(1), record.playedAt(), record.result(), record.moveCodes());
        }
    }

    private List<GameRecord> readRows(ResultSet rows) throws SQLException {
        List<GameRecord> records = new ArrayList<>();
        while (rows.next()) {
            toRecord(rows).ifPresent(records::add);
        }
        return List.copyOf(records);
    }

    private Optional<GameRecord> toRecord(ResultSet row) throws SQLException {
        try {
            return Optional.of(new GameRecord(
                    row.getLong("id"),
                    Instant.parse(row.getString("played_at")),
                    GameResult.valueOf(row.getString("result")),
                    splitMoves(row.getString("moves"))));
        } catch (IllegalArgumentException | DateTimeException exception) {
            return Optional.empty();
        }
    }

    private List<String> splitMoves(String moves) {
        if (moves == null || moves.isBlank()) {
            return List.of();
        }
        return List.of(moves.trim().split("\\s+"));
    }
}