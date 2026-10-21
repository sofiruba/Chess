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
import com.chess.domain.IGameService;

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
    private final IGameService game;
    private final PieceSymbols symbols;
    private final Consumer<GameRecord> loadHandler;
    private final Label status = new Label();
    private final Label captured = new Label();
    private final ListView<String> moves = new ListView<>();
    private final ListView<GameRecord> savedGames = new ListView<>();

    public SidePanel(
            IGameRepository repository,
            IGameService game,
            PieceSymbols symbols,
            Consumer<GameRecord> loadHandler) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.game = Objects.requireNonNull(game, "game must not be null");
        this.symbols = Objects.requireNonNull(symbols, "symbols must not be null");
        this.loadHandler = Objects.requireNonNull(loadHandler, "loadHandler must not be null");
        savedGames.setCellFactory(list -> new SavedGameCell());
    }

    public VBox createView() {
        Button save = new Button("Guardar partida");
        save.setOnAction(event -> save());
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
                save,
                new Label("Partidas guardadas"),
                savedGames,
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
        try {
            repository.save(GameRecord.unsaved(Instant.now(), game.result(), game.moveCodes()));
        } catch (IllegalStateException exception) {
            status.setText("No se pudo guardar la partida.");
            return;
        }
        refreshSavedGames();
        status.setText("Partida guardada");
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
            savedGames.getItems().setAll(repository.findAll());
        } catch (IllegalStateException exception) {
            savedGames.getItems().clear();
            status.setText("No se pudieron leer las partidas guardadas.");
        }
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