package com.chess.ui;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Consumer;

import com.chess.domain.model.Color;
import com.chess.domain.model.GameResult;
import com.chess.domain.port.GameRecord;
import com.chess.domain.port.IGameArchive;
import com.chess.domain.port.IGameListener;
import com.chess.domain.port.IGameQueries;
import com.chess.domain.port.MoveHistoryEntry;
import com.chess.domain.port.SavedGames;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class SidePanel implements IGameListener {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    private final IGameArchive archive;
    private final IGameQueries game;
    private final PieceSymbols symbols;
    private final SavedGameTracker savedGameTracker;
    private final Consumer<GameRecord> loadHandler;
    private final Runnable newGameHandler;
    private final PieceArtwork artwork = new PieceArtwork();
    private final Label status = new Label();
    private final ImageView turnArtwork = new ImageView();
    private final HBox turnIndicator = new HBox(10, turnArtwork, status);
    private final HBox captured = new HBox(4);
    private final Label unreadableNotice = new Label();
    private final Button saveButton = new Button("Guardar partida");
    private final Button newGameButton = new Button("Nueva partida");
    private final ListView<MoveHistoryEntry> moves = new ListView<>();
    private final ListView<GameRecord> savedGames = new ListView<>();
    private final Image heroesArtwork = loadArtwork("/com/chess/ui/assets/heroes.png");
    private final Image villainsArtwork = loadArtwork("/com/chess/ui/assets/villains.png");

    public SidePanel(
            IGameArchive archive,
            IGameQueries game,
            PieceSymbols symbols,
            SavedGameTracker savedGameTracker,
            Consumer<GameRecord> loadHandler,
            Runnable newGameHandler) {
        this.archive = Objects.requireNonNull(archive, "archive must not be null");
        this.game = Objects.requireNonNull(game, "game must not be null");
        this.symbols = Objects.requireNonNull(symbols, "symbols must not be null");
        this.savedGameTracker = Objects.requireNonNull(savedGameTracker, "savedGameTracker must not be null");
        this.loadHandler = Objects.requireNonNull(loadHandler, "loadHandler must not be null");
        this.newGameHandler = Objects.requireNonNull(newGameHandler, "newGameHandler must not be null");
        saveButton.setOnAction(event -> save());
        newGameButton.setOnAction(event -> newGameHandler.run());
        unreadableNotice.getStyleClass().add("error-notice");
        moves.setCellFactory(list -> new MoveCell());
        savedGames.setCellFactory(list -> new SavedGameCell());
        moves.getStyleClass().add("game-list");
        savedGames.getStyleClass().add("game-list");
        moves.setPrefHeight(180);
        savedGames.setPrefHeight(130);
        saveButton.getStyleClass().addAll("game-button", "secondary-button");
        newGameButton.getStyleClass().addAll("game-button", "primary-button", "new-game-button");
        turnIndicator.getStyleClass().add("turn-indicator");
        turnArtwork.setFitWidth(56);
        turnArtwork.setFitHeight(56);
        turnArtwork.setPreserveRatio(true);
        turnArtwork.setAccessibleRole(javafx.scene.AccessibleRole.IMAGE_VIEW);
        turnArtwork.setAccessibleText("Héroes");
        status.setWrapText(true);
        HBox.setHgrow(status, Priority.ALWAYS);
    }

    public VBox createView() {
        Button load = new Button("Cargar");
        load.setOnAction(event -> loadSelected());
        load.getStyleClass().addAll("game-button", "primary-button");
        captured.getStyleClass().add("captured-pieces");
        VBox panel = new VBox(
                10,
                sectionTitle("PARTIDA"),
                turnIndicator,
                sectionTitle("MOVIMIENTOS"),
                moves,
                sectionTitle("CAPTURADAS"),
                captured,
                newGameButton,
                saveButton,
                sectionTitle("PARTIDAS GUARDADAS"),
                savedGames,
                unreadableNotice,
                load);
        panel.setPadding(new Insets(12));
        panel.setMinWidth(250);
        panel.setPrefWidth(320);
        panel.getStyleClass().add("side-panel");
        VBox.setVgrow(moves, Priority.ALWAYS);
        VBox.setVgrow(savedGames, Priority.ALWAYS);
        refresh();
        refreshSavedGames();
        return panel;
    }

    public void refresh() {
        status.setText(statusText());
        moves.getItems().setAll(game.moveHistory());
        refreshTurnArtwork();
        captured.getChildren().clear();
        game.capturedPieces().forEach(piece -> artwork.imageFor(piece).ifPresentOrElse(
                image -> {
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(30);
                    imageView.setFitHeight(30);
                    imageView.setPreserveRatio(true);
                    imageView.setAccessibleText(symbols.symbolFor(piece));
                    captured.getChildren().add(imageView);
                },
                () -> captured.getChildren().add(new Label(symbols.symbolFor(piece)))));
        updateSaveButton();
    }

    @Override
    public void moveMade() {
        refresh();
    }

    @Override
    public void moveUndone() {
        refresh();
    }

    @Override
    public void moveRedone() {
        refresh();
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
        String player = game.turn() == Color.WHITE ? "los héroes" : "los villanos";
        return game.kingInCheckPosition() == null
                ? "Turno de " + player
                : "Jaque. Turno de " + player;
    }

    private void refreshTurnArtwork() {
        boolean heroes = switch (game.result()) {
            case WHITE_WINS -> true;
            case BLACK_WINS -> false;
            case DRAW, IN_PROGRESS -> game.turn() == Color.WHITE;
        };
        turnArtwork.setImage(heroes ? heroesArtwork : villainsArtwork);
        turnArtwork.setAccessibleText(heroes ? "Héroes" : "Villanos");
    }

    private void save() {
        GameRecord saved;
        try {
            saved = archive.save(game);
        } catch (IllegalStateException exception) {
            status.setText("No se pudo guardar la partida.");
            return;
        }
        savedGameTracker.markSaved(saved.moveCodes());
        updateSaveButton();
        refreshSavedGames();
        status.setText("Partida guardada");
    }

    private void updateSaveButton() {
        saveButton.setDisable(!savedGameTracker.canSave(game.moveCodes()));
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
            SavedGames saved = archive.findAll();
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

    private static Label sectionTitle(String text) {
        Label title = new Label(text);
        title.getStyleClass().add("section-title");
        return title;
    }

    private static String resultText(GameResult result) {
        return switch (result) {
            case WHITE_WINS -> "Ganan blancas";
            case BLACK_WINS -> "Ganan negras";
            case DRAW -> "Tablas";
            case IN_PROGRESS -> "En curso";
        };
    }

    private static Image loadArtwork(String resourcePath) {
        var resource = SidePanel.class.getResource(resourcePath);
        if (resource == null) {
            throw new IllegalStateException("Missing game artwork: " + resourcePath);
        }
        Image image = new Image(resource.toExternalForm());
        if (image.isError()) {
            throw new IllegalStateException("Could not load game artwork: " + resourcePath, image.getException());
        }
        return image;
    }

    private final class MoveCell extends ListCell<MoveHistoryEntry> {

        @Override
        protected void updateItem(MoveHistoryEntry entry, boolean empty) {
            super.updateItem(entry, empty);
            if (empty || entry == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            var icon = artwork.imageFor(entry.movingPiece());
            ImageView image = icon.map(ImageView::new).orElse(null);
            if (image != null) {
                image.setFitWidth(28);
                image.setFitHeight(28);
                image.setPreserveRatio(true);
                image.setAccessibleText(symbols.symbolFor(entry.movingPiece()));
            }
            Label notation = new Label(entry.notation());
            notation.getStyleClass().add("move-notation");
            HBox row = new HBox(8, image == null
                    ? new Label(symbols.symbolFor(entry.movingPiece()))
                    : image, notation);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            setText(null);
            setGraphic(row);
        }
    }

    private static final class SavedGameCell extends ListCell<GameRecord> {

        @Override
        protected void updateItem(GameRecord record, boolean empty) {
            super.updateItem(record, empty);
            setText(empty || record == null ? null : describe(record));
        }
    }
}