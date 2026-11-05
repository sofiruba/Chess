package com.chess.ui;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.chess.domain.model.Color;
import com.chess.domain.model.GameResult;
import com.chess.domain.model.Piece;
import com.chess.domain.model.Position;
import com.chess.domain.port.GameRecord;
import com.chess.domain.port.HeldPower;
import com.chess.domain.port.IGameArchive;
import com.chess.domain.port.IGameListener;
import com.chess.domain.port.IGameQueries;
import com.chess.domain.port.MoveHistoryEntry;
import com.chess.domain.port.SavedGames;

import javafx.beans.binding.Bindings;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class SidePanel implements IGameListener {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    private final IGameArchive archive;
    private final IGameQueries game;
    private final PieceSymbols symbols;
    private final PowerNames powerNames;
    private final SavedGameTracker savedGameTracker;
    private final Consumer<GameRecord> loadHandler;
    private final Runnable newGameHandler;
    private final ISoundPlayer soundPlayer;
    private final PieceArtwork artwork = new PieceArtwork();
    private final Label status = new Label();
    private final ImageView turnArtwork = new ImageView();
    private final HBox turnPlate = new HBox(status);
    private final StackPane turnIndicator = new StackPane(turnPlate, turnArtwork);
    /** El logo completo ("Super Chess Bros."), sin el margen transparente que lo rodea en la imagen. */
    private static final Rectangle2D TITLE_AREA = new Rectangle2D(0, 352, 1620, 940);
    private static final double TITLE_MAX_HEIGHT = 128;
    /** En ventanas bajas el logo se achica para dejarle lugar a la lista de movimientos y partidas. */
    private static final double TITLE_SHARE_OF_PANEL = 0.13;

    private final ImageView titleArtwork = new ImageView(loadArtwork("/com/chess/ui/assets/chess-title1.png"));
    private final VBox captured = new VBox(4);
    private final HBox heroesCaptured = new HBox(4);
    private final HBox villainsCaptured = new HBox(4);
    private final VBox powers = new VBox(6);
    private final Label unreadableNotice = new Label();
    private final Button saveButton = new Button("Guardar partida");
    private final Button newGameButton = new Button("Nueva partida");
    private final ListView<MoveHistoryEntry> moves = new ListView<>();
    private final ListView<GameRecord> savedGames = new ListView<>();
    private final TabPane historyTabs = new TabPane();
    private final Tab movesTab = new Tab("Movimientos");
    private final Tab savedGamesTab = new Tab("Guardadas");
    private final Label savedGamesTitle = sectionTitle("PARTIDAS GUARDADAS");
    private final VBox moveHistoryPanel = new VBox(8);
    private final StackPane historySlot = new StackPane();
    private final Image heroesArtwork = loadArtwork("/com/chess/ui/assets/heroes.png", 34, 34);
    private final Image villainsArtwork = loadArtwork("/com/chess/ui/assets/villains.png", 34, 34);
    /** La cara del turno se muestra a 92 px: se carga al doble para que se vea nítida, también con escalado. */
    private final Image heroesTurnArtwork = loadArtwork("/com/chess/ui/assets/heroes.png", 184, 184);
    private final Image villainsTurnArtwork = loadArtwork("/com/chess/ui/assets/villains.png", 184, 184);
    private VBox savedGamesContent;
    private boolean compactLayout;
    private boolean layoutInitialized;

    public SidePanel(
            IGameArchive archive,
            IGameQueries game,
            PieceSymbols symbols,
            PowerNames powerNames,
            SavedGameTracker savedGameTracker,
            Consumer<GameRecord> loadHandler,
            Runnable newGameHandler,
            ISoundPlayer soundPlayer) {
        this.archive = Objects.requireNonNull(archive, "archive must not be null");
        this.game = Objects.requireNonNull(game, "game must not be null");
        this.symbols = Objects.requireNonNull(symbols, "symbols must not be null");
        this.powerNames = Objects.requireNonNull(powerNames, "powerNames must not be null");
        this.savedGameTracker = Objects.requireNonNull(savedGameTracker, "savedGameTracker must not be null");
        this.loadHandler = Objects.requireNonNull(loadHandler, "loadHandler must not be null");
        this.newGameHandler = Objects.requireNonNull(newGameHandler, "newGameHandler must not be null");
        this.soundPlayer = Objects.requireNonNull(soundPlayer, "soundPlayer must not be null"); //dependency injection
        saveButton.setOnAction(event -> save());
        newGameButton.setOnAction(event -> {
            soundPlayer.play(SoundEffect.MENU);
            newGameHandler.run();
        });
        unreadableNotice.getStyleClass().add("error-notice");
        moves.setCellFactory(list -> new MoveCell());
        savedGames.setCellFactory(list -> new SavedGameCell());
        moves.getStyleClass().add("game-list");
        savedGames.getStyleClass().add("game-list");
        moves.setPrefHeight(180);
        savedGames.setPrefHeight(130);
        moves.setMinHeight(0);
        savedGames.setMinHeight(0);
        saveButton.getStyleClass().addAll("game-button", "save-game-button");
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
        titleArtwork.setViewport(TITLE_AREA);
        titleArtwork.setPreserveRatio(true);
        titleArtwork.setSmooth(true);
        titleArtwork.setAccessibleText("Super Chess Bros.");
        status.setWrapText(true);
        HBox.setHgrow(status, Priority.ALWAYS);
        turnPlate.setMaxWidth(Double.MAX_VALUE);
        turnIndicator.setMaxWidth(Double.MAX_VALUE);
        newGameButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setMaxWidth(Double.MAX_VALUE);
        heroesCaptured.setMaxWidth(Double.MAX_VALUE);
        villainsCaptured.setMaxWidth(Double.MAX_VALUE);
        captured.getChildren().setAll(
                capturedRow(heroesArtwork, "Piezas capturadas por héroes", heroesCaptured),
                capturedRow(villainsArtwork, "Piezas capturadas por villanos", villainsCaptured));
        movesTab.setClosable(false);
        savedGamesTab.setClosable(false);
        historyTabs.getStyleClass().add("history-tabs");
        moveHistoryPanel.getStyleClass().add("game-history-panel");
        VBox.setVgrow(moves, Priority.ALWAYS);
    }

    public VBox createView() {
        Button load = new Button("Cargar");
        load.setOnAction(event -> loadSelected());
        load.setText("CARGAR");
        load.getStyleClass().addAll("game-button", "primary-button", "text-button");
        load.setMaxWidth(Double.MAX_VALUE);
        captured.getStyleClass().add("captured-pieces");
        powers.getStyleClass().add("powers-list");
        savedGamesContent = new VBox(8,
                savedGamesTitle,
                savedGames,
                unreadableNotice,
                load);
        savedGamesContent.setMinHeight(0);
        VBox.setVgrow(savedGames, Priority.ALWAYS);

        movesTab.setContent(moves);
        savedGamesTab.setContent(savedGamesContent);
        historyTabs.getTabs().setAll(movesTab, savedGamesTab);

        StackPane titleBanner = new StackPane(titleArtwork);
        VBox panel = new VBox(
                10,
                titleBanner,
                sectionTitle("PARTIDA"),
                turnIndicator,
                newGameButton,
                saveButton,
                sectionTitle("CAPTURADAS"),
                captured,
                sectionTitle("PODERES"),
                powers,
                historySlot);
        panel.setPadding(new Insets(12));
        panel.setMinWidth(250);
        panel.setPrefWidth(320);
        panel.setFillWidth(true);
        panel.getStyleClass().add("side-panel");
        titleBanner.prefWidthProperty().bind(panel.widthProperty());
        titleArtwork.fitWidthProperty().bind(panel.widthProperty().subtract(32));
        titleArtwork.fitHeightProperty().bind(Bindings.min(
                TITLE_MAX_HEIGHT, panel.heightProperty().multiply(TITLE_SHARE_OF_PANEL)));
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
        if (layoutInitialized && compactLayout == compact) {
            return;
        }
        layoutInitialized = true;
        compactLayout = compact;
        historySlot.getChildren().clear();
        if (compact) {
            savedGamesContent.getChildren().remove(savedGamesTitle);
            moveHistoryPanel.getChildren().remove(moves);
            movesTab.setContent(moves);
            savedGamesTab.setContent(savedGamesContent);
            historyTabs.getTabs().setAll(movesTab, savedGamesTab);
            historySlot.getChildren().setAll(historyTabs);
        } else {
            if (!savedGamesContent.getChildren().contains(savedGamesTitle)) {
                savedGamesContent.getChildren().add(0, savedGamesTitle);
            }
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
        heroesCaptured.getChildren().clear();
        villainsCaptured.getChildren().clear();
        game.capturedPieces().forEach(piece -> {
            HBox row = piece.color() == Color.BLACK ? heroesCaptured : villainsCaptured;
            artwork.imageFor(piece).ifPresentOrElse(
                    image -> {
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(30);
                        imageView.setFitHeight(30);
                        imageView.setPreserveRatio(true);
                        imageView.setAccessibleText(symbols.symbolFor(piece));
                        row.getChildren().add(imageView);
                    },
                    () -> row.getChildren().add(new Label(symbols.symbolFor(piece))));
        });
        refreshPowers();
        updateSaveButton();
    }

    /** Los poderes de ambos equipos: los dos jugadores ven siempre lo que tiene el otro. */
    private void refreshPowers() {
        powers.getChildren().setAll(
                teamPowers("Héroes", Color.WHITE),
                teamPowers("Villanos", Color.BLACK));
        String boxesNote = boxesNote();
        if (boxesNote != null) {
            Label note = new Label(boxesNote);
            note.getStyleClass().add("powers-note");
            powers.getChildren().add(note);
        }
    }

    /** Una sola línea con las cajas sin abrir y cuándo salen más; null si la partida no tiene cajas. */
    private String boxesNote() {
        int boxes = game.mysteryBoxes().size();
        int turns = game.turnsUntilNewBoxes();
        String unopened = boxes == 1 ? "1 caja sin abrir" : boxes + " cajas sin abrir";
        if (turns <= 0) {
            return boxes == 0 ? null : unopened;
        }
        if (boxes + 2 > game.maxMysteryBoxes()) {
            return unopened + "; no salen más por ahora";
        }
        String next = turns == 1 ? "nuevas el próximo turno" : "nuevas en " + turns + " turnos";
        return boxes == 0 ? "Cajas " + next : unopened + ", " + next;
    }

    private HBox teamPowers(String team, Color color) {
        Label name = new Label(team);
        name.getStyleClass().add("powers-team");
        name.setMinWidth(66);
        FlowPane chips = new FlowPane(4, 4);
        HBox.setHgrow(chips, Priority.ALWAYS);
        List<HeldPower> held = game.heldPowers(color);
        if (held.isEmpty()) {
            Label none = new Label("Sin poderes");
            none.getStyleClass().add("powers-note");
            chips.getChildren().add(none);
        }
        for (HeldPower power : held) {
            chips.getChildren().add(powerChip(power));
        }
        HBox row = new HBox(6, name, chips);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox powerChip(HeldPower held) {
        Label badge = new Label(powerNames.badge(held.power()));
        badge.getStyleClass().addAll("power-badge", powerNames.styleClass(held.power()));
        badge.setMinSize(18, 18);
        badge.setPrefSize(18, 18);
        badge.setMaxSize(18, 18);
        Label text = new Label(powerNames.name(held.power()) + " " + squareName(held.position()));
        HBox chip = new HBox(4, badge, text);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.getStyleClass().add("power-chip");
        chip.setAccessibleText(symbols.symbolFor(held.piece()) + " " + text.getText());
        return chip;
    }

    private static String squareName(Position position) {
        return String.valueOf((char) ('a' + position.x())) + (position.y() + 1);
    }

    @Override
    public void moveMade() {
        refresh();
    }

    @Override
    public void powerUsed() {
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
        turnArtwork.setImage(heroes ? heroesTurnArtwork : villainsTurnArtwork);
        turnArtwork.setAccessibleText(heroes ? "Héroes" : "Villanos");
        turnPlate.getStyleClass().removeAll("heroes-turn", "villains-turn");
        turnPlate.getStyleClass().add(heroes ? "heroes-turn" : "villains-turn");
    }

    private void save() {
        GameRecord saved;
        try {
            saved = archive.save(game);
        } catch (IllegalStateException exception) {
            soundPlayer.play(SoundEffect.ERROR);
            status.setText("No se pudo guardar la partida.");
            return;
        }
        soundPlayer.play(SoundEffect.SAVE);
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
            soundPlayer.play(SoundEffect.ERROR);
            status.setText("Elegí una partida guardada para cargar.");
            return;
        }
        soundPlayer.play(SoundEffect.MENU);
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

    private static Image loadArtwork(String resourcePath, double width, double height) {
        var resource = SidePanel.class.getResource(resourcePath);
        if (resource == null) {
            throw new IllegalStateException("Missing game artwork: " + resourcePath);
        }
        Image image = new Image(resource.toExternalForm(), width, height, true, true);
        if (image.isError()) {
            throw new IllegalStateException("Could not load game artwork: " + resourcePath, image.getException());
        }
        return image;
    }

    private static HBox capturedRow(Image teamArtwork, String accessibleText, HBox pieces) {
        ImageView teamIcon = new ImageView(teamArtwork);
        teamIcon.setFitWidth(30);
        teamIcon.setFitHeight(30);
        teamIcon.setPreserveRatio(true);
        teamIcon.setAccessibleText(accessibleText);
        Region divider = new Region();
        divider.getStyleClass().add("captured-divider");
        divider.setMinWidth(2);
        divider.setPrefWidth(2);
        divider.setMaxWidth(2);
        HBox row = new HBox(8, teamIcon, divider, pieces);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(divider, Priority.NEVER);
        HBox.setHgrow(pieces, Priority.ALWAYS);
        return row;
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