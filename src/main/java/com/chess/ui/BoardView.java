package com.chess.ui;

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
import javafx.scene.layout.VBox;

public final class BoardView implements IGameListener {
    private static final int SQUARE_SIZE = 64;
    private final IGameQueries game;
    private final PieceSymbols symbols;
    private final PieceArtwork artwork = new PieceArtwork();
    private final Consumer<Position> squareClickHandler;
    private final GridPane squares = new GridPane();
    private Position selected;
    private List<Position> legalMoves = List.of();

    public BoardView(IGameQueries game, PieceSymbols symbols, Consumer<Position> squareClickHandler) {
        this.game = game;
        this.symbols = symbols;
        this.squareClickHandler = squareClickHandler;
    }

    public BorderPane createView() {
        BorderPane view = new BorderPane(squares);
        view.getStyleClass().add("board-frame");
        view.setTop(fileLabels());
        view.setLeft(rankLabels());
        render();
        return view;
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
        for (int x = 0; x < game.boardSize(); x++) {
            Label label = new Label(String.valueOf((char) ('a' + x)));
            label.setMinSize(SQUARE_SIZE, 24);
            label.setMaxWidth(SQUARE_SIZE);
            label.getStyleClass().add("coordinate-label");
            labels.getChildren().add(label);
        }
        labels.getStyleClass().add("file-coordinates");
        return labels;
    }

    private VBox rankLabels() {
        VBox labels = new VBox();
        for (int rank = game.boardSize(); rank >= 1; rank--) {
            Label label = new Label(String.valueOf(rank));
            label.setMinSize(24, SQUARE_SIZE);
            label.getStyleClass().add("coordinate-label");
            labels.getChildren().add(label);
        }
        labels.getStyleClass().add("rank-coordinates");
        return labels;
    }

    private void addSquare(Position position) {
        Piece piece = game.pieceAt(position);
        Button square = new Button();
        square.getStyleClass().add("board-square");
        if (piece != null) {
            artwork.imageFor(piece).ifPresentOrElse(
                    image -> {
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(58);
                        imageView.setFitHeight(58);
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
        square.setMinSize(SQUARE_SIZE, SQUARE_SIZE);
        square.setMaxSize(SQUARE_SIZE, SQUARE_SIZE);
        square.setOnAction(event -> squareClickHandler.accept(position));
        squares.add(square, position.x(), game.boardSize() - 1 - position.y());
    }

    private static String squareColorClass(Position position) {
        return (position.x() + position.y()) % 2 == 0 ? "cream-square" : "meadow-square";
    }

    private static String squareLabel(Position position, Piece piece) {
        String description = piece == null ? "casilla vacía" : piece.color() + " " + piece.type().name();
        return description + ", " + (char) ('a' + position.x()) + (position.y() + 1);
    }
}