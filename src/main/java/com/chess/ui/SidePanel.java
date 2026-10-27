package com.chess.ui;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.chess.domain.Color;
import com.chess.domain.GameRecord;
import com.chess.domain.GameResult;
import com.chess.domain.IGameRepository;
import com.chess.domain.IGameQueries;
import com.chess.domain.SavedGames;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

public final class SidePanel {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    private final IGameRepository repository;
    private final IGameQueries game;
    private final PieceSymbols symbols;
    private final SavedGameTracker savedGameTracker;
    private final Consumer<GameRecord> loadHandler;
    private final Label status = new Label();
    private final Label captured = new Label();
    private final Label unreadableNotice = new Label();
    private final Button saveButton = new Button("Guardar partida");
    private final ListView<String> moves = new ListView<>();
    private final ListView<GameRecord> savedGames = new ListView<>();

    public SidePanel(
            IGameRepository repository,
            IGameQueries game,
            PieceSymbols symbols,
            SavedGameTracker savedGameTracker,
            Consumer<GameRecord> loadHandler) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.game = Objects.requireNonNull(game, "game must not be null");
        this.symbols = Objects.requireNonNull(symbols, "symbols must not be null");
        this.savedGameTracker = Objects.requireNonNull(savedGameTracker, "savedGameTracker must not be null");
        this.loadHandler = Objects.requireNonNull(loadHandler, "loadHandler must not be null");
        saveButton.setOnAction(event -> save());
        unreadableNotice.setStyle("-fx-text-fill: #b00020;");
        savedGames.setCellFactory(list -> new SavedGameCell());
    }

    public VBox createView() {
        Button load = new Button("Cargar");
        load.setOnAction(event -> loadSelected());
        VBox panel = new VBox(
                10,
                new Label("Partida"),
                status,
                new Label("Movimientos"),
                moves,
                new Label("Capturadas"),
                captured,
                saveButton,
                new Label("Partidas guardadas"),
                savedGames,
                unreadableNotice,
                load);
        panel.setPadding(new Insets(12));
        refresh();
        refreshSavedGames();
        return panel;
    }

    public void refresh() {
        status.setText(statusText());
        moves.getItems().setAll(game.moveNotation());
        captured.setText(game.capturedPieces().stream().map(symbols::symbolFor).reduce("", String::concat));
        updateSaveButton();
    }

    public void showMessage(String message) {
        status.setText(message);
    }

    private String statusText() {
        if (game.result() == GameResult.WHITE_WINS) {
            return "Jaque mate. Ganan blancas";
        }
        if (game.result() == GameResult.BLACK_WINS) {
            return "Jaque mate. Ganan negras";
        }
        if (game.result() == GameResult.DRAW) {
            return "Tablas";
        }
        String player = game.turn() == Color.WHITE ? "blancas" : "negras";
        return game.kingInCheckPosition() == null ? "Turno: " + player : "Jaque. Turno: " + player;
    }

    private void save() {
        List<String> moveCodes = game.moveCodes();
        try {
            repository.save(GameRecord.unsaved(Instant.now(), game.result(), moveCodes));
        } catch (IllegalStateException exception) {
            status.setText("No se pudo guardar la partida.");
            return;
        }
        savedGameTracker.markSaved(moveCodes);
        updateSaveButton();
        refreshSavedGames();
        status.setText("Partida guardada");
    }

    private void updateSaveButton() {
        saveButton.setDisable(savedGameTracker.isSaved(game.moveCodes()));
    }

    private void loadSelected() {
        GameRecord selected = savedGames.getSelectionModel().getSelectedItem();
        if (selected == null) {
            status.setText("Elegí una partida guardada para cargar.");
            return;
        }
        loadHandler.accept(selected);
    }

    private void refreshSavedGames() {
        try {
            SavedGames saved = repository.findAll();
            savedGames.getItems().setAll(saved.records());
            showUnreadableNotice(saved.unreadableRows());
        } catch (IllegalStateException exception) {
            savedGames.getItems().clear();
            showUnreadableNotice(0);
            status.setText("No se pudieron leer las partidas guardadas.");
        }
    }

    private void showUnreadableNotice(int unreadableRows) {
        boolean visible = unreadableRows > 0;
        unreadableNotice.setText(unreadableNoticeText(unreadableRows));
        unreadableNotice.setVisible(visible);
        unreadableNotice.setManaged(visible);
    }

    private static String unreadableNoticeText(int unreadableRows) {
        if (unreadableRows == 1) {
            return "1 partida guardada no se pudo leer";
        }
        return unreadableRows + " partidas guardadas no se pudieron leer";
    }

    private static String describe(GameRecord record) {
        return DATE_FORMAT.format(record.playedAt()) + " — " + resultText(record.result());
    }

    private static String resultText(GameResult result) {
        return switch (result) {
            case WHITE_WINS -> "Ganan blancas";
            case BLACK_WINS -> "Ganan negras";
            case DRAW -> "Tablas";
            case IN_PROGRESS -> "En curso";
        };
    }

    private static final class SavedGameCell extends ListCell<GameRecord> {

        @Override
        protected void updateItem(GameRecord record, boolean empty) {
            super.updateItem(record, empty);
            setText(empty || record == null ? null : describe(record));
        }
    }
}