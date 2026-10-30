package com.chess.ui;

import java.util.List;
import java.util.Objects;

import com.chess.domain.port.GameRecord;
import com.chess.domain.port.IGameFactory;
import com.chess.domain.port.IGameReplayer;
import com.chess.domain.port.IGameRepository;
import com.chess.domain.port.IGameService;
import com.chess.domain.model.PieceType;
import com.chess.domain.model.Position;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public final class ChessApplication {
    private final IGameRepository repository;
    private final IGameFactory gameFactory;
    private final IGameReplayer gameReplayer;
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
            IGameRepository repository,
            IGameFactory gameFactory,
            IGameReplayer gameReplayer,
            PieceSymbols symbols,
            IPromotionChooser promotionChooser) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.gameFactory = Objects.requireNonNull(gameFactory, "gameFactory must not be null");
        this.gameReplayer = Objects.requireNonNull(gameReplayer, "gameReplayer must not be null");
        this.symbols = Objects.requireNonNull(symbols, "symbols must not be null");
        this.promotionChooser = Objects.requireNonNull(promotionChooser, "promotionChooser must not be null");
    }

    public Pane createView() {
        startNewGame();
        showGame();
        Button fresh = new Button("Nueva partida");
        fresh.setOnAction(event -> resetGame());
        Button undo = new Button("Deshacer");
        undo.setOnAction(event -> undo());
        Button redo = new Button("Rehacer");
        redo.setOnAction(event -> redo());
        root.setBottom(new HBox(8, fresh, undo, redo));
        BorderPane.setMargin(root.getBottom(), new Insets(10));
        return root;
    }

    private void resetGame() {
        startNewGame();
        showGame();
    }

    private void startNewGame() {
        installGame(gameFactory.create());
    }

    private void installGame(IGameService newGame) {
        game = newGame;
        selected = null;
        legalMoves = List.of();
        boardView = new BoardView(game, symbols, this::click);
        sidePanel = new SidePanel(repository, game, symbols, savedGameTracker, this::loadGame);
    }

    private void showGame() {
        root.setCenter(boardView.createView());
        root.setRight(sidePanel.createView());
    }

    private void loadGame(GameRecord record) {
        IGameService loaded;
        try {
            loaded = gameReplayer.replay(record.moveCodes());
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
        } else if (game.pieceAt(position) != null && game.pieceAt(position).color() == game.turn()) {
            select(position);
        } else {
            message = "Destino inválido: la pieza no puede moverse allí.";
            clearSelection();
        }
        refresh();
        if (message != null) {
            sidePanel.showMessage(message);
        }
    }

    private boolean select(Position position) {
        if (game.pieceAt(position) == null || game.pieceAt(position).color() != game.turn()) {
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
            refresh();
        } catch (IllegalStateException exception) {
            sidePanel.showMessage("No hay movimientos para deshacer.");
        }
    }

    private void redo() {
        try {
            game.redoLastMove();
            clearSelection();
            refresh();
        } catch (IllegalStateException exception) {
            sidePanel.showMessage("No hay movimientos para rehacer.");
        }
    }

    private void clearSelection() {
        selected = null;
        legalMoves = List.of();
        boardView.clearSelection();
    }

    private void refresh() {
        boardView.render();
        sidePanel.refresh();
    }
}