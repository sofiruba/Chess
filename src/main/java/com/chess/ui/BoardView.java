package com.chess.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.chess.domain.model.Piece;
import com.chess.domain.model.Position;
import com.chess.domain.model.PowerType;
import com.chess.domain.port.IGameListener;
import com.chess.domain.port.IGameQueries;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;

public final class BoardView implements IGameListener {
    private static final double MAX_SQUARE_SIZE = 64;
    private static final double COORDINATE_SIZE = 28;
    private static final double FRAME_INSETS = 24;
    private final IGameQueries game;
    private final PieceSymbols symbols;
    private final PowerNames powerNames;
    private final PieceArtwork artwork = new PieceArtwork();
    private final Consumer<Position> squareClickHandler;
    private final GridPane squares = new GridPane();
    private final GridPane boardLayout = new GridPane();
    private final List<Button> squareButtons = new ArrayList<>();
    private final List<Label> fileLabels = new ArrayList<>();
    private final List<Label> rankLabels = new ArrayList<>();
    private double squareSize = MAX_SQUARE_SIZE;
    private Position selected;
    private List<Position> legalMoves = List.of();
    private List<Position> powerTargets = List.of();

    public BoardView(
            IGameQueries game,
            PieceSymbols symbols,
            PowerNames powerNames,
            Consumer<Position> squareClickHandler) {
        this.game = game;
        this.symbols = symbols;
        this.powerNames = powerNames;
        this.squareClickHandler = squareClickHandler;
    }

    public BorderPane createView() {
        BorderPane frame = new BorderPane(boardLayout);
        frame.setMinSize(0, 0);
        frame.getStyleClass().add("board-frame");
        boardLayout.setAlignment(Pos.CENTER);
        boardLayout.setHgap(0);
        boardLayout.setVgap(0);
        boardLayout.getColumnConstraints().add(new ColumnConstraints(COORDINATE_SIZE));
        boardLayout.getRowConstraints().add(new RowConstraints(COORDINATE_SIZE));
        for (int index = 0; index < game.boardSize(); index++) {
            ColumnConstraints column = new ColumnConstraints(squareSize);
            column.setMinWidth(squareSize);
            column.setMaxWidth(squareSize);
            boardLayout.getColumnConstraints().add(column);
            RowConstraints row = new RowConstraints(squareSize);
            row.setMinHeight(squareSize);
            row.setMaxHeight(squareSize);
            boardLayout.getRowConstraints().add(row);
        }
        addCoordinates();
        GridPane.setColumnIndex(squares, 1);
        GridPane.setRowIndex(squares, 1);
        GridPane.setColumnSpan(squares, game.boardSize());
        GridPane.setRowSpan(squares, game.boardSize());
        boardLayout.add(squares, 1, 1);
        render();

        StackPane area = new StackPane(frame);
        area.getStyleClass().add("game-board-area");
        area.setMinSize(0, 0);
        area.widthProperty().addListener((observable, oldValue, newValue) -> resizeBoard(frame, area));
        area.heightProperty().addListener((observable, oldValue, newValue) -> resizeBoard(frame, area));
        return new BorderPane(area);
    }

    public void showSelection(Position selected, List<Position> legalMoves) {
        this.selected = selected;
        this.legalMoves = List.copyOf(legalMoves);
        this.powerTargets = List.of();
        render();
    }

    /** Resalta los objetivos del poder de la pieza seleccionada en lugar de sus jugadas. */
    public void showPowerTargets(Position holder, List<Position> targets) {
        this.selected = holder;
        this.legalMoves = List.of();
        this.powerTargets = List.copyOf(targets);
        render();
    }

    public void clearSelection() {
        showSelection(null, List.of());
    }

    public void render() {
        squares.getChildren().clear();
        squareButtons.clear();
        squares.getStyleClass().setAll("chess-grid");
        for (int y = game.boardSize() - 1; y >= 0; y--) {
            for (int x = 0; x < game.boardSize(); x++) {
                addSquare(new Position(x, y));
            }
        }
    }

    @Override
    public void moveMade() {
        render();
    }

    @Override
    public void powerUsed() {
        render();
    }

    @Override
    public void moveUndone() {
        render();
    }

    @Override
    public void moveRedone() {
        render();
    }

    private void addCoordinates() {
        for (int x = 0; x < game.boardSize(); x++) {
            Label label = new Label(String.valueOf((char) ('a' + x)));
            label.getStyleClass().add("coordinate-label");
            label.setMinSize(squareSize, COORDINATE_SIZE);
            label.setPrefSize(squareSize, COORDINATE_SIZE);
            label.setMaxSize(squareSize, COORDINATE_SIZE);
            fileLabels.add(label);
            boardLayout.add(label, x + 1, 0);
        }
        for (int row = 0; row < game.boardSize(); row++) {
            int rank = game.boardSize() - row;
            Label label = new Label(String.valueOf(rank));
            label.getStyleClass().add("coordinate-label");
            label.setMinSize(COORDINATE_SIZE, squareSize);
            label.setPrefSize(COORDINATE_SIZE, squareSize);
            label.setMaxSize(COORDINATE_SIZE, squareSize);
            rankLabels.add(label);
            boardLayout.add(label, 0, row + 1);
        }
    }

    private void addSquare(Position position) {
        Piece piece = game.pieceAt(position);
        boolean hasBox = game.mysteryBoxes().contains(position);
        Button square = new Button();
        squareButtons.add(square);
        square.getStyleClass().add("board-square");
        square.setGraphic(squareContent(position, piece, hasBox));
        square.getStyleClass().add(squareColorClass(position));
        if (hasBox) {
            square.getStyleClass().add("box-square");
        }
        if (piece != null && game.isFrozen(position)) {
            square.getStyleClass().add("frozen-square");
        }
        if (piece != null && game.isImmune(position)) {
            square.getStyleClass().add("immune-square");
        }
        if (position.equals(selected)) {
            square.getStyleClass().add("selected-square");
        } else if (legalMoves.contains(position)) {
            square.getStyleClass().add("legal-square");
        } else if (powerTargets.contains(position)) {
            square.getStyleClass().add("power-target-square");
        }
        if (position.equals(game.kingInCheckPosition())) {
            square.getStyleClass().add("check-square");
        }
        square.setAccessibleText(squareLabel(position, piece) + squareStatus(position, piece));
        square.setMinSize(squareSize, squareSize);
        square.setPrefSize(squareSize, squareSize);
        square.setMaxSize(squareSize, squareSize);
        square.setOnAction(event -> squareClickHandler.accept(position));
        squares.add(square, position.x(), game.boardSize() - 1 - position.y());
    }

    /** La pieza (o el bloque "?" si la casilla tiene una caja y está vacía) y la insignia de su poder. */
    private StackPane squareContent(Position position, Piece piece, boolean hasBox) {
        StackPane content = new StackPane();
        content.setMouseTransparent(true);
        if (piece == null) {
            if (hasBox) {
                Label box = new Label("?");
                box.getStyleClass().add("mystery-box");
                double side = squareSize * 0.72;
                box.setMinSize(side, side);
                box.setPrefSize(side, side);
                box.setMaxSize(side, side);
                box.setStyle("-fx-font-size: " + Math.round(squareSize * 0.46) + "px;");
                content.getChildren().add(box);
            }
            return content;
        }
        content.getChildren().add(artwork.imageFor(piece)
                .<javafx.scene.Node>map(image -> {
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(squareSize - 6);
                    imageView.setFitHeight(squareSize - 6);
                    imageView.setPreserveRatio(true);
                    return imageView;
                })
                .orElseGet(() -> new Label(symbols.symbolFor(piece))));
        PowerType power = game.powerAt(position);
        if (power != null) {
            Label badge = new Label(powerNames.badge(power));
            badge.getStyleClass().addAll("power-badge", powerNames.styleClass(power));
            double side = Math.max(14, squareSize * 0.34);
            badge.setMinSize(side, side);
            badge.setPrefSize(side, side);
            badge.setMaxSize(side, side);
            badge.setStyle("-fx-font-size: " + Math.round(side * 0.62) + "px;");
            StackPane.setAlignment(badge, Pos.TOP_RIGHT);
            content.getChildren().add(badge);
        }
        return content;
    }

    private void resizeBoard(BorderPane frame, StackPane area) {
        double availableWidth = area.getWidth() - COORDINATE_SIZE - FRAME_INSETS - 32;
        double availableHeight = area.getHeight() - COORDINATE_SIZE - FRAME_INSETS - 32;
        double nextSize = Math.min(MAX_SQUARE_SIZE,
                Math.min(availableWidth / game.boardSize(), availableHeight / game.boardSize()));
        if (nextSize <= 0) {
            return;
        }
        squareSize = nextSize;
        fileLabels.forEach(label -> {
            label.setMinSize(squareSize, COORDINATE_SIZE);
            label.setPrefSize(squareSize, COORDINATE_SIZE);
            label.setMaxSize(squareSize, COORDINATE_SIZE);
        });
        rankLabels.forEach(label -> {
            label.setMinSize(COORDINATE_SIZE, squareSize);
            label.setPrefSize(COORDINATE_SIZE, squareSize);
            label.setMaxSize(COORDINATE_SIZE, squareSize);
        });
        boardLayout.getColumnConstraints().get(0).setPrefWidth(COORDINATE_SIZE);
        boardLayout.getColumnConstraints().stream().skip(1).forEach(column -> {
            column.setMinWidth(squareSize);
            column.setPrefWidth(squareSize);
            column.setMaxWidth(squareSize);
        });
        boardLayout.getRowConstraints().get(0).setPrefHeight(COORDINATE_SIZE);
        boardLayout.getRowConstraints().stream().skip(1).forEach(row -> {
            row.setMinHeight(squareSize);
            row.setPrefHeight(squareSize);
            row.setMaxHeight(squareSize);
        });
        double boardSize = squareSize * game.boardSize();
        squares.setPrefSize(boardSize, boardSize);
        boardLayout.setPrefSize(boardSize + COORDINATE_SIZE, boardSize + COORDINATE_SIZE);
        frame.setPrefSize(
                boardSize + COORDINATE_SIZE + FRAME_INSETS,
                boardSize + COORDINATE_SIZE + FRAME_INSETS);
        frame.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        render();
    }

    private static String squareColorClass(Position position) {
        return (position.x() + position.y()) % 2 == 0 ? "cream-square" : "meadow-square";
    }

    private static String squareLabel(Position position, Piece piece) {
        String description = piece == null ? "casilla vacía" : piece.color() + " " + piece.type().name();
        return description + ", " + (char) ('a' + position.x()) + (position.y() + 1);
    }

    private String squareStatus(Position position, Piece piece) {
        StringBuilder status = new StringBuilder();
        if (game.mysteryBoxes().contains(position)) {
            status.append(", caja misteriosa");
        }
        PowerType power = piece == null ? null : game.powerAt(position);
        if (power != null) {
            status.append(", tiene ").append(powerNames.name(power));
        }
        if (piece != null && game.isFrozen(position)) {
            status.append(", congelada");
        }
        if (piece != null && game.isImmune(position)) {
            status.append(", inmune");
        }
        return status.toString();
    }
}