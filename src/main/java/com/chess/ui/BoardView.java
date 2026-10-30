package com.chess.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import com.chess.domain.port.IGameQueries;
import com.chess.domain.model.Piece;
import com.chess.domain.model.Position;

import java.util.List;
import java.util.function.Consumer;

public final class BoardView {
    private static final int SQUARE_SIZE = 64;
    private final IGameQueries game;
    private final PieceSymbols symbols;
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
        for (int y = game.boardSize() - 1; y >= 0; y--) {
            for (int x = 0; x < game.boardSize(); x++) {
                addSquare(new Position(x, y));
            }
        }
    }

    private HBox fileLabels() {
        HBox labels = new HBox();
        for (int x = 0; x < game.boardSize(); x++) {
            Label label = new Label(String.valueOf((char) ('a' + x)));
            label.setMinWidth(SQUARE_SIZE);
            label.setStyle("-fx-alignment: center;");
            labels.getChildren().add(label);
        }
        return labels;
    }

    private VBox rankLabels() {
        VBox labels = new VBox();
        for (int rank = game.boardSize(); rank >= 1; rank--) {
            Label label = new Label(String.valueOf(rank));
            label.setMinSize(22, SQUARE_SIZE);
            label.setStyle("-fx-alignment: center;");
            labels.getChildren().add(label);
        }
        return labels;
    }

    private void addSquare(Position position) {
        Piece piece = game.pieceAt(position);
        Button square = new Button(symbols.symbolFor(piece));
        square.setMinSize(SQUARE_SIZE, SQUARE_SIZE);
        square.setMaxSize(SQUARE_SIZE, SQUARE_SIZE);
        square.setStyle("-fx-font-size: 32; -fx-background-color: " + backgroundColor(position) + ";");
        square.setOnAction(event -> squareClickHandler.accept(position));
        squares.add(square, position.x(), game.boardSize() - 1 - position.y());
    }

    private String backgroundColor(Position position) {
        if (position.equals(selected)) {
            return "#f6f669";
        }
        if (legalMoves.contains(position)) {
            return "#8fbc8f";
        }
        if (position.equals(game.kingInCheckPosition())) {
            return "#e57373";
        }
        return (position.x() + position.y()) % 2 == 0 ? "#f0d9b5" : "#b58863";
    }
}