package com.chess.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.chess.domain.model.PieceType;
import com.chess.domain.model.Position;
import com.chess.domain.port.GameRecord;
import com.chess.domain.port.IGameArchive;
import com.chess.domain.port.IGameFactory;
import com.chess.domain.port.IGameService;
import com.chess.domain.port.SavedGames;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.transform.Rotate;
import javafx.scene.text.Font;
import javafx.util.StringConverter;
import javafx.util.Duration;

public final class ChessApplication {
    private final IGameArchive archive;
    private final IGameFactory gameFactory;
    private final PieceSymbols symbols;
    private final IPromotionChooser promotionChooser;
    private final SavedGameTracker savedGameTracker = new SavedGameTracker();
    private final BorderPane root = new BorderPane();
    private final List<Animation> homeAnimations = new ArrayList<>();
    private IGameService game;
    private BoardView boardView;
    private SidePanel sidePanel;
    private StackPane gameBoardSlot;
    private HBox gameCenter;
    private Position selected;
    private List<Position> legalMoves = List.of();
    private boolean gameScreenVisible;
    private Stage rulesStage;

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
        root.setPrefSize(1100, 760);
        root.setMinSize(600, 500);
        root.widthProperty().addListener((observable, oldValue, newValue) -> updateGameLayout());
        loadUiFont("/com/chess/ui/assets/titles1.ttf", 28);
        loadUiFont("/com/chess/ui/assets/titles2.ttf", 18);
        loadUiFont("/com/chess/ui/assets/text1.otf", 16);
        var stylesheet = getClass().getResource("chess-theme.css");
        if (stylesheet == null) {
            throw new IllegalStateException("Missing chess UI stylesheet.");
        }
        root.getStylesheets().add(stylesheet.toExternalForm());
        showHome();
        return root;
    }

    private void showHome() {
        stopHomeAnimations();
        gameScreenVisible = false;
        root.setTop(null);
        root.setRight(null);
        root.setBottom(null);

        StackPane screen = new StackPane();
        screen.getStyleClass().add("home-screen");

        Image logo = loadUiImage("/com/chess/ui/assets/chess-title.png");
        ImageView logoView = new ImageView(logo);
        logoView.fitWidthProperty().bind(Bindings.min(
                760,
                Bindings.min(screen.widthProperty().multiply(0.82), screen.heightProperty().multiply(0.33))));
        logoView.fitHeightProperty().bind(screen.heightProperty().multiply(0.33));
        logoView.setPreserveRatio(true);
        logoView.setAccessibleText("Chess Quest");

        PerspectiveBoard board = new PerspectiveBoard();
        board.prefWidthProperty().bind(Bindings.min(720, screen.widthProperty().multiply(0.72)));
        board.prefHeightProperty().bind(Bindings.min(310, screen.heightProperty().multiply(0.39)));
        board.setMinSize(300, 150);
        board.getStyleClass().add("preview-board");

        Button fresh = new Button("Nueva partida");
        fresh.setOnAction(event -> resetGame());
        fresh.getStyleClass().addAll("game-button", "home-action-button", "red-button");
        Button load = new Button("Cargar partida");
        load.setOnAction(event -> openSavedGames());
        load.getStyleClass().addAll("game-button", "home-action-button", "blue-button");
        for (Button button : List.of(fresh, load)) {
            button.prefWidthProperty().bind(Bindings.max(
                    150,
                    Bindings.min(270, screen.widthProperty().multiply(0.39))));
            button.setMaxWidth(Double.MAX_VALUE);
        }

        FlowPane actions = new FlowPane(16, 12, fresh, load);
        actions.setAlignment(Pos.CENTER);
        actions.prefWrapLengthProperty().bind(screen.widthProperty().multiply(0.86));
        actions.getStyleClass().add("home-actions");

        VBox content = new VBox(0, logoView, board, actions);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(12, 24, 22, 24));
        content.prefWidthProperty().bind(screen.widthProperty());
        content.prefHeightProperty().bind(screen.heightProperty());
        content.getStyleClass().add("home-content");

        Pane coinLayer = new Pane();
        coinLayer.setMouseTransparent(true);
        Image coin = loadUiImage("/com/chess/ui/assets/coin.png", 64, 64);
        addFloatingCoin(coinLayer, screen, coin, 0.10, 0.17, 0);
        addFloatingCoin(coinLayer, screen, coin, 0.90, 0.16, 0.35);
        addFloatingCoin(coinLayer, screen, coin, 0.07, 0.76, 0.7);
        addFloatingCoin(coinLayer, screen, coin, 0.93, 0.76, 1.05);
        screen.getChildren().addAll(content, coinLayer);
        root.setCenter(screen);
    }

    private void addFloatingCoin(
            Pane layer,
            StackPane screen,
            Image coinImage,
            double x,
            double y,
            double delay) {
        ImageView coin = new ImageView(coinImage);
        coin.setFitWidth(64);
        coin.setFitHeight(64);
        coin.setPreserveRatio(true);
        StackPane token = new StackPane(coin);
        token.setAccessibleRole(AccessibleRole.IMAGE_VIEW);
        token.setAccessibleText("Moneda dorada");
        token.setPrefSize(64, 64);
        token.setMinSize(64, 64);
        token.setMaxSize(64, 64);
        token.layoutXProperty().bind(screen.widthProperty().multiply(x).subtract(32));
        token.layoutYProperty().bind(screen.heightProperty().multiply(y).subtract(32));
        layer.getChildren().add(token);

        RotateTransition spin = new RotateTransition(Duration.seconds(2.4 + delay), token);
        spin.setAxis(Rotate.Y_AXIS);
        spin.setFromAngle(0);
        spin.setToAngle(360);
        spin.setCycleCount(Animation.INDEFINITE);
        spin.setDelay(Duration.seconds(delay));

        TranslateTransition bob = new TranslateTransition(Duration.seconds(1.3 + delay / 2), token);
        bob.setByY(-10);
        bob.setAutoReverse(true);
        bob.setCycleCount(Animation.INDEFINITE);
        bob.setDelay(Duration.seconds(delay));

        homeAnimations.add(spin);
        homeAnimations.add(bob);
        spin.play();
        bob.play();
    }

    private static void loadUiFont(String resourcePath, double size) {
        try (var stream = ChessApplication.class.getResourceAsStream(resourcePath)) {
            if (stream == null || Font.loadFont(stream, size) == null) {
                throw new IllegalStateException("Could not load UI font: " + resourcePath);
            }
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Could not load UI font: " + resourcePath, exception);
        }
    }

    private static Image loadUiImage(String resourcePath) {
        var resource = ChessApplication.class.getResource(resourcePath);
        if (resource == null) {
            throw new IllegalStateException("Missing UI image: " + resourcePath);
        }
        Image image = new Image(resource.toExternalForm());
        if (image.isError()) {
            throw new IllegalStateException("Could not load UI image: " + resourcePath, image.getException());
        }
        return image;
    }

    private static Image loadUiImage(String resourcePath, double width, double height) {
        var resource = ChessApplication.class.getResource(resourcePath);
        if (resource == null) {
            throw new IllegalStateException("Missing UI image: " + resourcePath);
        }
        Image image = new Image(resource.toExternalForm(), width, height, true, true);
        if (image.isError()) {
            throw new IllegalStateException("Could not load UI image: " + resourcePath, image.getException());
        }
        return image;
    }

    private static final class PerspectiveBoard extends Pane {
        private static final String[] BACK_RANK = {
                "rook", "knight", "bishop", "queen", "king", "bishop", "knight", "rook"
        };
        private static final String[] COLORS = {"#fdfdd0", "#2e8a57"};
        private final Polygon platform = new Polygon();
        private final Polygon boardBacking = new Polygon();
        private final List<Polygon> squares = new ArrayList<>(64);
        private final ImageView[][] pieces = new ImageView[8][8];
        private final Map<String, Image> pieceImages = new HashMap<>();

        private PerspectiveBoard() {
            platform.setFill(Color.web("#8a4c26"));
            platform.setStroke(Color.web("#4d2c20"));
            platform.setStrokeWidth(4);
            boardBacking.setFill(Color.web("#fbc02d"));
            boardBacking.setStroke(Color.web("#3f2a1c"));
            boardBacking.setStrokeWidth(3);
            getChildren().addAll(platform, boardBacking);

            for (int row = 0; row < 8; row++) {
                for (int column = 0; column < 8; column++) {
                    Polygon square = new Polygon();
                    square.setFill(Color.web(COLORS[(row + column) % 2]));
                    square.setStroke(Color.web("#253329"));
                    square.setStrokeWidth(1);
                    squares.add(square);
                    getChildren().add(square);

                    String piece = pieceAt(row, column);
                    if (piece != null) {
                        boolean white = row >= 6;
                        String color = white ? "white" : "black";
                        String imageName = color + "-" + piece;
                        Image imageData = pieceImages.computeIfAbsent(
                                imageName,
                                name -> loadUiImage("/com/chess/ui/assets/" + name + ".png", 90, 90));
                        ImageView image = new ImageView(imageData);
                        image.setPreserveRatio(true);
                        image.setMouseTransparent(true);
                        pieces[row][column] = image;
                    }
                }
            }
            for (ImageView[] row : pieces) {
                for (ImageView piece : row) {
                    if (piece != null) {
                        getChildren().add(piece);
                    }
                }
            }
            getStyleClass().add("perspective-board");
        }

        @Override
        protected void layoutChildren() {
            double width = getWidth();
            double height = getHeight();
            if (width <= 0 || height <= 0) {
                return;
            }

            double center = width / 2;
            double planeTop = height * 0.06;
            double planeHeight = height * 0.76;
            double topWidth = width * 0.70;
            double bottomWidth = width * 0.98;
            setPoints(platform,
                    center - topWidth / 2 - 7, planeTop - 5,
                    center + topWidth / 2 + 7, planeTop - 5,
                    center + bottomWidth / 2 + 13, planeTop + planeHeight + height * 0.13,
                    center - bottomWidth / 2 - 13, planeTop + planeHeight + height * 0.13);
            setPoints(boardBacking,
                    center - topWidth / 2 - 4, planeTop - 3,
                    center + topWidth / 2 + 4, planeTop - 3,
                    center + bottomWidth / 2 + 5, planeTop + planeHeight + 4,
                    center - bottomWidth / 2 - 5, planeTop + planeHeight + 4);

            for (int row = 0; row < 8; row++) {
                double front = row / 8.0;
                double back = (row + 1) / 8.0;
                double rowTop = planeTop + Math.pow(front, 1.25) * planeHeight;
                double rowBottom = planeTop + Math.pow(back, 1.25) * planeHeight;
                double rowTopWidth = topWidth + (bottomWidth - topWidth) * front;
                double rowBottomWidth = topWidth + (bottomWidth - topWidth) * back;
                double topLeft = center - rowTopWidth / 2;
                double bottomLeft = center - rowBottomWidth / 2;
                double topCellWidth = rowTopWidth / 8;
                double bottomCellWidth = rowBottomWidth / 8;
                double cellHeight = rowBottom - rowTop;

                for (int column = 0; column < 8; column++) {
                    Polygon square = squares.get(row * 8 + column);
                    setPoints(square,
                            topLeft + topCellWidth * column, rowTop,
                            topLeft + topCellWidth * (column + 1), rowTop,
                            bottomLeft + bottomCellWidth * (column + 1), rowBottom,
                            bottomLeft + bottomCellWidth * column, rowBottom);

                    ImageView piece = pieces[row][column];
                    if (piece != null) {
                        double cellWidth = (topCellWidth + bottomCellWidth) / 2;
                        double pieceSize = Math.min(cellWidth * 0.76, Math.max(30, cellHeight * 1.42));
                        double pieceCenter = (topLeft + topCellWidth * (column + 0.5)
                                + bottomLeft + bottomCellWidth * (column + 0.5)) / 2;
                        piece.setFitWidth(pieceSize);
                        piece.setFitHeight(pieceSize);
                        piece.relocate(pieceCenter - pieceSize / 2, rowBottom - pieceSize * 0.88);
                    }
                }
            }
        }

        private static String pieceAt(int row, int column) {
            if (row == 0) {
                return BACK_RANK[column];
            }
            if (row == 1) {
                return "pawn";
            }
            if (row == 6) {
                return "pawn";
            }
            if (row == 7) {
                return BACK_RANK[column];
            }
            return null;
        }

        private static void setPoints(Polygon polygon, double... points) {
            polygon.getPoints().setAll(
                    java.util.Arrays.stream(points).boxed().toList());
        }
    }

    private void stopHomeAnimations() {
        homeAnimations.forEach(Animation::stop);
        homeAnimations.clear();
    }

    private void resetGame() {
        startNewGame();
        showGame();
    }

    private void openSavedGames() {
        SavedGames savedGames;
        try {
            savedGames = archive.findAll();
        } catch (IllegalStateException exception) {
            showArchiveNotice(Alert.AlertType.ERROR, "No se pudieron leer las partidas guardadas.");
            return;
        }
        if (savedGames.records().isEmpty()) {
            String message = savedGames.unreadableRows() == 0
                    ? "Todavía no hay partidas guardadas."
                    : "No hay partidas guardadas disponibles. "
                            + savedGames.unreadableRows() + " no se pudieron leer.";
            showArchiveNotice(Alert.AlertType.INFORMATION, message);
            return;
        }

        ComboBox<GameRecord> games = new ComboBox<>();
        games.getItems().setAll(savedGames.records());
        games.setConverter(new StringConverter<>() {
            @Override
            public String toString(GameRecord record) {
                return record == null ? "" : describeSavedGame(record);
            }

            @Override
            public GameRecord fromString(String value) {
                return null;
            }
        });
        games.getSelectionModel().selectFirst();
        games.setMaxWidth(Double.MAX_VALUE);

        if (root.getStylesheets().isEmpty()) {
            throw new IllegalStateException("Missing chess UI stylesheet.");
        }
        Label title = new Label("CARGAR PARTIDA");
        title.getStyleClass().add("saved-game-title");
        Label prompt = new Label("Elegí una partida guardada para continuar");
        prompt.getStyleClass().add("saved-game-prompt");
        games.getStyleClass().add("saved-game-combo");
        Button cancel = new Button("CANCELAR");
        cancel.getStyleClass().addAll("game-button", "text-button", "saved-game-cancel-button");
        Button load = new Button("CARGAR");
        load.getStyleClass().addAll("game-button", "text-button", "saved-game-confirm-button");
        HBox actions = new HBox(10, cancel, load);
        actions.setAlignment(Pos.CENTER_RIGHT);
        VBox selector = new VBox(16, title, prompt, games, actions);
        selector.getStyleClass().add("saved-game-window");
        selector.setPrefWidth(480);
        selector.setPadding(new Insets(24));

        Stage selectorStage = new Stage();
        selectorStage.setTitle("Cargar partida");
        selectorStage.initModality(Modality.WINDOW_MODAL);
        if (root.getScene() != null && root.getScene().getWindow() != null) {
            selectorStage.initOwner(root.getScene().getWindow());
        }
        Scene selectorScene = new Scene(selector);
        selectorScene.getStylesheets().addAll(root.getStylesheets());
        selectorStage.setScene(selectorScene);
        selectorStage.setMinWidth(440);
        selectorStage.setMinHeight(230);
        cancel.setOnAction(event -> selectorStage.close());
        load.setOnAction(event -> {
            GameRecord record = games.getValue();
            selectorStage.close();
            if (record != null) {
                loadGame(record);
            }
        });
        selectorStage.show();
    }

    private void startNewGame() {
        installGame(gameFactory.create());
    }

    private void installGame(IGameService newGame) {
        game = newGame;
        selected = null;
        legalMoves = List.of();
        boardView = new BoardView(game, symbols, this::click);
        sidePanel = new SidePanel(archive, game, symbols, savedGameTracker, this::loadGame, this::resetGame);
        game.addListener(boardView);
        game.addListener(sidePanel);
    }

    private void showGame() {
        stopHomeAnimations();
        gameScreenVisible = true;
        VBox sidePanelView = sidePanel.createView();
        gameBoardSlot = new StackPane(boardView.createView());
        gameBoardSlot.setMinSize(0, 0);
        HBox.setHgrow(gameBoardSlot, javafx.scene.layout.Priority.ALWAYS);
        gameCenter = new HBox(14, gameBoardSlot, sidePanel.moveHistoryView());
        gameCenter.setMinSize(0, 0);
        root.setCenter(gameCenter);
        root.setRight(sidePanelView);
        root.setTop(null);
        updateGameLayout();

        Button home = new Button("Inicio");
        home.setText("INICIO");
        home.setOnAction(event -> showHome());
        home.getStyleClass().addAll("game-button", "home-button", "text-button");
        Button undo = new Button("Deshacer");
        undo.setText("DESHACER");
        undo.setOnAction(event -> undo());
        undo.getStyleClass().addAll("game-button", "secondary-button", "text-button", "undo-button");
        Button redo = new Button("Rehacer");
        redo.setText("REHACER");
        redo.setOnAction(event -> redo());
        redo.getStyleClass().addAll("game-button", "secondary-button", "text-button", "redo-button");
        Button information = new Button("i");
        information.setAccessibleText("Información");
        information.setTooltip(new Tooltip("Información"));
        information.setOnAction(event -> showRulesBook());
        information.getStyleClass().addAll("game-button", "text-button", "information-button");
        HBox actions = new HBox(10, home, undo, redo, information);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(14, 24, 18, 30));
        root.setBottom(actions);
        BorderPane.setMargin(root.getCenter(), new Insets(20, 10, 10, 20));
        BorderPane.setMargin(root.getRight(), new Insets(20, 24, 14, 14));
    }

    private void updateGameLayout() {
        if (!gameScreenVisible || sidePanel == null) {
            return;
        }
        boolean compact = root.getWidth() < 1320;
        sidePanel.setCompactLayout(compact);
        sidePanel.moveHistoryView().setVisible(!compact);
        sidePanel.moveHistoryView().setManaged(!compact);
    }

    private void loadGame(GameRecord record) {
        IGameService loaded;
        try {
            loaded = gameFactory.create(record.moveCodes());
        } catch (IllegalArgumentException exception) {
            String message = "La partida guardada está dañada y no se puede cargar.";
            if (gameScreenVisible && sidePanel != null) {
                sidePanel.showMessage(message);
            } else {
                showArchiveNotice(Alert.AlertType.ERROR, message);
            }
            return;
        }
        installGame(loaded);
        savedGameTracker.markSaved(loaded.moveCodes());
        showGame();
        sidePanel.showMessage("Partida cargada");
    }

    private void showRulesBook() {
        if (rulesStage != null && rulesStage.isShowing()) {
            rulesStage.toFront();
            return;
        }

        Image leftPageImage = loadUiImage("/com/chess/ui/assets/chess-rules1.png");
        Image rightPageImage = loadUiImage("/com/chess/ui/assets/chess-rules2.png");
        StackPane left = rulesPage(leftPageImage, "Reglas del ajedrez, página 1");
        StackPane right = rulesPage(rightPageImage, "Reglas del ajedrez, página 2");
        left.getStyleClass().addAll("rules-page", "rules-page-left");
        right.getStyleClass().addAll("rules-page", "rules-page-right");
        left.setMinSize(0, 0);
        right.setMinSize(0, 0);
        left.setPrefSize(440, 590);
        right.setPrefSize(440, 590);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        Region spine = new Region();
        spine.getStyleClass().add("rules-book-spine");
        HBox pages = new HBox(left, spine, right);
        pages.getStyleClass().add("rules-book-pages");
        VBox book = new VBox(12, new Label("REGLAS DEL JUEGO"), pages);
        book.getStyleClass().add("rules-book");
        VBox.setVgrow(pages, Priority.ALWAYS);

        Stage stage = new Stage();
        stage.setTitle("Reglas del ajedrez");
        if (root.getScene() != null && root.getScene().getWindow() != null) {
            stage.initOwner(root.getScene().getWindow());
        }
        Scene scene = new Scene(book, 920, 680);
        scene.setCamera(new PerspectiveCamera());
        if (root.getStylesheets().isEmpty()) {
            throw new IllegalStateException("Missing chess UI stylesheet.");
        }
        scene.getStylesheets().addAll(root.getStylesheets());
        stage.setScene(scene);
        stage.setMinWidth(640);
        stage.setMinHeight(500);
        stage.setOnCloseRequest(event -> rulesStage = null);
        rulesStage = stage;
        stage.show();

        RotateTransition leftOpen = new RotateTransition(Duration.millis(430), left);
        leftOpen.setAxis(Rotate.Y_AXIS);
        leftOpen.setFromAngle(-82);
        leftOpen.setToAngle(0);
        leftOpen.setInterpolator(Interpolator.EASE_OUT);
        RotateTransition rightOpen = new RotateTransition(Duration.millis(430), right);
        rightOpen.setAxis(Rotate.Y_AXIS);
        rightOpen.setFromAngle(82);
        rightOpen.setToAngle(0);
        rightOpen.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(leftOpen, rightOpen).play();
    }

    private static StackPane rulesPage(Image image, String description) {
        StackPane container = new StackPane();
        ImageView page = new ImageView(image);
        page.setPreserveRatio(true);
        page.setAccessibleRole(AccessibleRole.IMAGE_VIEW);
        page.setAccessibleText(description);
        page.fitWidthProperty().bind(container.widthProperty().subtract(20));
        page.fitHeightProperty().bind(container.heightProperty().subtract(20));
        container.getChildren().add(page);
        return container;
    }

    private void showArchiveNotice(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Partidas guardadas");
        alert.setHeaderText(null);
        if (root.getScene() != null && root.getScene().getWindow() != null) {
            alert.initOwner(root.getScene().getWindow());
        }
        alert.showAndWait();
    }

    private static String describeSavedGame(GameRecord record) {
        String result = switch (record.result()) {
            case WHITE_WINS -> "Ganan héroes";
            case BLACK_WINS -> "Ganan villanos";
            case DRAW -> "Tablas";
            case IN_PROGRESS -> "En curso";
        };
        return java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(java.time.ZoneId.systemDefault())
                .format(record.playedAt()) + " — " + result;
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