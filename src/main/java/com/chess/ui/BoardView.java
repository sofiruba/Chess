package com.chess.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.chess.domain.model.Piece;
import com.chess.domain.model.Position;
import com.chess.domain.port.IGameListener;
import com.chess.domain.port.IGameQueries;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class BoardView implements IGameListener {
    private static final double MAX_SQUARE_SIZE = 64;
    private static final double COORDINATE_SIZE = 24;
    private final IGameQueries game;
    private final PieceSymbols symbols;
    private final PieceArtwork artwork = new PieceArtwork();
    private final Consumer<Position> squareClickHandler;
    private final GridPane squares = new GridPane();
    private final List<Button> squareButtons = new ArrayList<>();
    private final List<Label> fileLabels = new ArrayList<>();
    private final List<Label> rankLabels = new ArrayList<>();
    private double squareSize = MAX_SQUARE_SIZE;
    private Position selected;
    private List<Position> legalMoves = List.of();

    public BoardView(IGameQueries game, PieceSymbols symbols, Consumer<Position> squareClickHandler) {
        this.game = game;
        this.symbols = symbols;
        this.squareClickHandler = squareClickHandler;
    }

    public BorderPane createView() {
        BorderPane view = new BorderPane(squares);
        view.setMinSize(0, 0);
        view.getStyleClass().add("board-frame");
        view.setTop(fileLabels());
        view.setLeft(rankLabels());
        render();

        StackPane area = new StackPane(view);
        area.getStyleClass().add("game-board-area");
        area.setMinSize(0, 0);
        area.widthProperty().addListener((observable, oldValue, newValue) -> resizeBoard(view, area));
        area.heightProperty().addListener((observable, oldValue, newValue) -> resizeBoard(view, area));
        return new BorderPane(area);
    }

    public void showSelection(Position selected, List<Position> legalMoves) {
        this.selected = selected;
        this.legalMoves = List.copyOf(legalMoves);
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
    public void moveUndone() {
        render();
    }

    @Override
    public void moveRedone() {
        render();
    }

    private HBox fileLabels() {
        HBox labels = new HBox();
        Region coordinateSpacer = new Region();
        coordinateSpacer.setMinWidth(COORDINATE_SIZE);
        coordinateSpacer.setPrefWidth(COORDINATE_SIZE);
        coordinateSpacer.setMaxWidth(COORDINATE_SIZE);
        labels.getChildren().add(coordinateSpacer);
        for (int x = 0; x < game.boardSize(); x++) {
            Label label = new Label(String.valueOf((char) ('a' + x)));
            label.getStyleClass().add("coordinate-label");
            fileLabels.add(label);
            labels.getChildren().add(label);
        }
        labels.getStyleClass().add("file-coordinates");
        return labels;
    }

    private VBox rankLabels() {
        VBox labels = new VBox();
        for (int rank = game.boardSize(); rank >= 1; rank--) {
            Label label = new Label(String.valueOf(rank));
            label.getStyleClass().add("coordinate-label");
            rankLabels.add(label);
            labels.getChildren().add(label);
        }
        labels.getStyleClass().add("rank-coordinates");
        return labels;
    }

    private void addSquare(Position position) {
        Piece piece = game.pieceAt(position);
        Button square = new Button();
        squareButtons.add(square);
        square.getStyleClass().add("board-square");
        if (piece != null) {
            artwork.imageFor(piece).ifPresentOrElse(
                    image -> {
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(squareSize - 6);
                        imageView.setFitHeight(squareSize - 6);
                        imageView.setPreserveRatio(true);
                        square.setGraphic(imageView);
                    },
                    () -> square.setText(symbols.symbolFor(piece)));
        }
        square.getStyleClass().add(squareColorClass(position));
        if (position.equals(selected)) {
            square.getStyleClass().add("selected-square");
        } else if (legalMoves.contains(position)) {
            square.getStyleClass().add("legal-square");
        }
        if (position.equals(game.kingInCheckPosition())) {
            square.getStyleClass().add("check-square");
        }
        square.setAccessibleText(squareLabel(position, piece));
        square.setMinSize(squareSize, squareSize);
        square.setPrefSize(squareSize, squareSize);
        square.setMaxSize(squareSize, squareSize);
        square.setOnAction(event -> squareClickHandler.accept(position));
        squares.add(square, position.x(), game.boardSize() - 1 - position.y());
    }

    private void resizeBoard(BorderPane view, StackPane area) {
        double availableWidth = area.getWidth() - COORDINATE_SIZE - 32;
        double availableHeight = area.getHeight() - COORDINATE_SIZE - 32;
        double nextSize = Math.min(MAX_SQUARE_SIZE,
                Math.min(availableWidth / game.boardSize(), availableHeight / game.boardSize()));
        if (nextSize < 20) {
            return;
        }
        squareSize = nextSize;
        squareButtons.forEach(button -> {
            button.setMinSize(squareSize, squareSize);
            button.setPrefSize(squareSize, squareSize);
            button.setMaxSize(squareSize, squareSize);
            if (button.getGraphic() instanceof ImageView image) {
                image.setFitWidth(squareSize - 6);
                image.setFitHeight(squareSize - 6);
            }
        });
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
        double boardSize = squareSize * game.boardSize();
        squares.setPrefSize(boardSize, boardSize);
        view.setPrefSize(boardSize + COORDINATE_SIZE + 18, boardSize + COORDINATE_SIZE + 18);
        view.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    }

    private static String squareColorClass(Position position) {
        return (position.x() + position.y()) % 2 == 0 ? "cream-square" : "meadow-square";
    }

    private static String squareLabel(Position position, Piece piece) {
        String description = piece == null ? "casilla vacía" : piece.color() + " " + piece.type().name();
        return description + ", " + (char) ('a' + position.x()) + (position.y() + 1);
    }
}