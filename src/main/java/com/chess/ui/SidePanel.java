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
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
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
    private final HBox turnPlate = new HBox(status);
    private final StackPane turnIndicator = new StackPane(turnPlate, turnArtwork);
    private final ImageView titleArtwork = new ImageView(loadArtwork("/com/chess/ui/assets/chess-title1.png"));
    private final HBox captured = new HBox(4);
    private final Label unreadableNotice = new Label();
    private final Button saveButton = new Button("Guardar partida");
    private final Button newGameButton = new Button("Nueva partida");
    private final ListView<MoveHistoryEntry> moves = new ListView<>();
    private final ListView<GameRecord> savedGames = new ListView<>();
    private final TabPane historyTabs = new TabPane();
    private final Tab movesTab = new Tab("Movimientos");
    private final Tab savedGamesTab = new Tab("Guardadas");
    private final VBox moveHistoryPanel = new VBox(8);
    private final StackPane historySlot = new StackPane();
    private final Image heroesArtwork = loadArtwork("/com/chess/ui/assets/heroes.png");
    private final Image villainsArtwork = loadArtwork("/com/chess/ui/assets/villains.png");
    private VBox savedGamesContent;
    private boolean compactLayout = true;

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
        moves.setMinHeight(0);
        savedGames.setMinHeight(0);
        saveButton.getStyleClass().addAll("game-button", "secondary-button");
        newGameButton.getStyleClass().addAll("game-button", "primary-button", "new-game-button");
        turnIndicator.getStyleClass().add("turn-indicator");
        turnPlate.getStyleClass().add("turn-plate");
        turnPlate.setAlignment(Pos.CENTER_LEFT);
        turnArtwork.setFitWidth(92);
        turnArtwork.setFitHeight(92);
        turnArtwork.setPreserveRatio(true);
        turnArtwork.setAccessibleRole(javafx.scene.AccessibleRole.IMAGE_VIEW);
        turnArtwork.setAccessibleText("Héroes");
        StackPane.setAlignment(turnArtwork, Pos.CENTER_RIGHT);
        StackPane.setMargin(turnArtwork, new Insets(-9, -8, -9, 0));
        titleArtwork.setViewport(new Rectangle2D(50, 649, 1536, 307));
        titleArtwork.setPreserveRatio(true);
        titleArtwork.setFitHeight(58);
        titleArtwork.setAccessibleText("Chess Quest");
        status.setWrapText(true);
        HBox.setHgrow(status, Priority.ALWAYS);
        movesTab.setClosable(false);
        savedGamesTab.setClosable(false);
        historyTabs.getStyleClass().add("history-tabs");
        moveHistoryPanel.getStyleClass().add("game-history-panel");
        VBox.setVgrow(moves, Priority.ALWAYS);
    }

    public VBox createView() {
        Button load = new Button("Cargar");
        load.setOnAction(event -> loadSelected());
        load.getStyleClass().addAll("game-button", "primary-button", "text-button");
        captured.getStyleClass().add("captured-pieces");
        savedGamesContent = new VBox(8,
                sectionTitle("PARTIDAS GUARDADAS"),
                savedGames,
                unreadableNotice,
                load);
        savedGamesContent.setMinHeight(0);
        VBox.setVgrow(savedGames, Priority.ALWAYS);

        movesTab.setContent(moves);
        savedGamesTab.setContent(savedGamesContent);
        historyTabs.getTabs().setAll(movesTab, savedGamesTab);

        StackPane titleBanner = new StackPane(titleArtwork);
        titleBanner.setMinHeight(58);
        VBox panel = new VBox(
                10,
                titleBanner,
                sectionTitle("PARTIDA"),
                turnIndicator,
                newGameButton,
                saveButton,
                sectionTitle("CAPTURADAS"),
                captured,
                historySlot);
        panel.setPadding(new Insets(12));
        panel.setMinWidth(250);
        panel.setPrefWidth(320);
        panel.getStyleClass().add("side-panel");
        titleBanner.prefWidthProperty().bind(panel.widthProperty());
        titleArtwork.fitWidthProperty().bind(panel.widthProperty().multiply(0.78));
        VBox.setVgrow(historySlot, Priority.ALWAYS);
        historySlot.getChildren().setAll(historyTabs);
        refresh();
        refreshSavedGames();
        return panel;
    }

    public VBox moveHistoryView() {
        return moveHistoryPanel;
    }

    public void setCompactLayout(boolean compact) {
        if (compactLayout == compact) {
            return;
        }
        compactLayout = compact;
        historySlot.getChildren().clear();
        if (compact) {
            moveHistoryPanel.getChildren().remove(moves);
            movesTab.setContent(moves);
            savedGamesTab.setContent(savedGamesContent);
            historyTabs.getTabs().setAll(movesTab, savedGamesTab);
            historySlot.getChildren().setAll(historyTabs);
        } else {
            movesTab.setContent(null);
            savedGamesTab.setContent(null);
            historyTabs.getTabs().setAll(savedGamesTab);
            historySlot.getChildren().setAll(savedGamesContent);
            moveHistoryPanel.getChildren().setAll(sectionTitle("MOVIMIENTOS"), moves);
        }
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