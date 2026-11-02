package com.chess.ui;

import java.util.List;
import java.util.Objects;

import com.chess.domain.model.PieceType;
import com.chess.domain.model.Position;
import com.chess.domain.port.GameRecord;
import com.chess.domain.port.IGameArchive;
import com.chess.domain.port.IGameFactory;
import com.chess.domain.port.IGameService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class ChessApplication {
    private final IGameArchive archive;
    private final IGameFactory gameFactory;
    private final PieceSymbols symbols;
    private final IPromotionChooser promotionChooser;
    private final SavedGameTracker savedGameTracker = new SavedGameTracker();
    private final BorderPane root = new BorderPane();
    private IGameService game;
    private BoardView boardView;
    private SidePanel sidePanel;
    private Position selected;
    private List<Position> legalMoves = List.of();

    public ChessApplication(
            IGameArchive archive,
            IGameFactory gameFactory,
            PieceSymbols symbols,
            IPromotionChooser promotionChooser) {
        this.archive = Objects.requireNonNull(archive, "archive must not be null");
        this.gameFactory = Objects.requireNonNull(gameFactory, "gameFactory must not be null");
        this.symbols = Objects.requireNonNull(symbols, "symbols must not be null");
        this.promotionChooser = Objects.requireNonNull(promotionChooser, "promotionChooser must not be null");
    }

    public Pane createView() {
        root.getStyleClass().add("app-root");
        var stylesheet = getClass().getResource("chess-theme.css");
        if (stylesheet == null) {
            throw new IllegalStateException("Missing chess UI stylesheet.");
        }
        root.getStylesheets().add(stylesheet.toExternalForm());
        showHome();
        return root;
    }

    private void showHome() {
        root.setTop(null);
        root.setRight(null);
        root.setBottom(null);

        var logoResource = getClass().getResource("/com/chess/ui/assets/chess-title.png");
        if (logoResource == null) {
            throw new IllegalStateException("Missing chess title artwork.");
        }
        Image logo = new Image(logoResource.toExternalForm(), 500, 300, true, true);
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(500);
        logoView.setFitHeight(300);
        logoView.setPreserveRatio(true);
        logoView.setAccessibleText("CHESSS");

        Label eyebrow = new Label("ESTRATEGIA · AVENTURA · DIVERSIÓN");
        eyebrow.getStyleClass().add("home-eyebrow");
        Label subtitle = new Label("Cada jugada abre un nuevo camino.");
        subtitle.getStyleClass().add("home-subtitle");

        Button fresh = new Button("Nueva partida");
        fresh.setOnAction(event -> resetGame());
        fresh.getStyleClass().addAll("game-button", "primary-button");
        Button load = new Button("Cargar partida");
        load.setOnAction(event -> openSavedGames());
        load.getStyleClass().addAll("game-button", "secondary-button");
        fresh.setMaxWidth(Double.MAX_VALUE);
        load.setMaxWidth(Double.MAX_VALUE);

        VBox actions = new VBox(12, fresh, load);
        actions.setMaxWidth(280);
        actions.setAlignment(Pos.CENTER);
        actions.getStyleClass().add("home-actions");

        VBox content = new VBox(8, eyebrow, logoView, subtitle, actions);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(36));
        content.getStyleClass().add("home-card");

        StackPane screen = new StackPane(content);
        screen.setPadding(new Insets(30));
        screen.getStyleClass().add("home-screen");
        root.setCenter(screen);
    }

    private void resetGame() {
        startNewGame();
        showGame();
    }

    private void openSavedGames() {
        startNewGame();
        showGame();
        sidePanel.showMessage("Selecciona una partida guardada para continuar.");
    }

    private void startNewGame() {
        installGame(gameFactory.create());
    }

    private void installGame(IGameService newGame) {
        game = newGame;
        selected = null;
        legalMoves = List.of();
        boardView = new BoardView(game, symbols, this::click);
        sidePanel = new SidePanel(archive, game, symbols, savedGameTracker, this::loadGame);
        game.addListener(boardView);
        game.addListener(sidePanel);
    }

    private void showGame() {
        root.setCenter(boardView.createView());
        root.setRight(sidePanel.createView());
        root.setTop(gameHeader());

        Button fresh = new Button("Nueva partida");
        fresh.setOnAction(event -> resetGame());
        fresh.getStyleClass().addAll("game-button", "primary-button");
        Button undo = new Button("Deshacer");
        undo.setOnAction(event -> undo());
        undo.getStyleClass().addAll("game-button", "secondary-button");
        Button redo = new Button("Rehacer");
        redo.setOnAction(event -> redo());
        redo.getStyleClass().addAll("game-button", "secondary-button");
        HBox actions = new HBox(10, fresh, undo, redo);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(14, 24, 18, 30));
        root.setBottom(actions);
        BorderPane.setMargin(root.getCenter(), new Insets(20, 10, 10, 20));
        BorderPane.setMargin(root.getRight(), new Insets(20, 24, 14, 14));
    }

    private HBox gameHeader() {
        Label brand = new Label("CHESSS");
        brand.getStyleClass().add("game-brand");
        Label hint = new Label("Una jugada a la vez");
        hint.getStyleClass().add("header-hint");
        VBox identity = new VBox(2, brand, hint);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Button home = new Button("Inicio");
        home.setOnAction(event -> showHome());
        home.getStyleClass().addAll("game-button", "home-button");

        HBox header = new HBox(18, identity, spacer, home);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 24, 14, 30));
        header.getStyleClass().add("game-header");
        return header;
    }

    private void loadGame(GameRecord record) {
        IGameService loaded;
        try {
            loaded = gameFactory.create(record.moveCodes());
        } catch (IllegalArgumentException exception) {
            sidePanel.showMessage("La partida guardada está dañada y no se puede cargar.");
            return;
        }
        installGame(loaded);
        savedGameTracker.markSaved(loaded.moveCodes());
        showGame();
        sidePanel.showMessage("Partida cargada");
    }

    private void click(Position position) {
        String message = null;
        if (selected == null) {
            if (!select(position)) {
                message = "Click inválido: selecciona una pieza propia.";
            }
        } else if (legalMoves.contains(position)) {
            try {
                playSelectedMove(position);
            } catch (IllegalArgumentException exception) {
                message = exception.getMessage();
                clearSelection();
            }
        } else if (game.belongsToActivePlayer(position)) {
            select(position);
        } else {
            message = "Destino inválido: la pieza no puede moverse allí.";
            clearSelection();
        }
        if (message != null) {
            sidePanel.showMessage(message);
        } else {
            sidePanel.refresh();
        }
    }

    private boolean select(Position position) {
        if (!game.belongsToActivePlayer(position)) {
            return false;
        }
        selected = position;
        legalMoves = game.legalMoves(position);
        boardView.showSelection(selected, legalMoves);
        return true;
    }

    private void playSelectedMove(Position destination) {
        if (game.requiresPromotion(selected, destination)) {
            PieceType chosen = promotionChooser.choosePiece(game.turn(), game.promotionOptions());
            game.playMove(selected, destination, chosen);
        } else {
            game.playMove(selected, destination);
        }
        clearSelection();
    }

    private void undo() {
        try {
            game.undoLastMove();
            clearSelection();
        } catch (IllegalStateException exception) {
            sidePanel.showMessage("No hay movimientos para deshacer.");
        }
    }

    private void redo() {
        try {
            game.redoLastMove();
            clearSelection();
        } catch (IllegalStateException exception) {
            sidePanel.showMessage("No hay movimientos para rehacer.");
        }
    }

    private void clearSelection() {
        selected = null;
        legalMoves = List.of();
        boardView.clearSelection();
    }
}