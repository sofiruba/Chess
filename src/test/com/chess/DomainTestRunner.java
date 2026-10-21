package com.chess;

import com.chess.configuration.DefaultMoveCommandFactory;
import com.chess.domain.BlackTurnState;
import com.chess.domain.Board;
import com.chess.domain.Color;
import com.chess.domain.Game;
import com.chess.domain.IGameState;
import com.chess.domain.MoveCommand;
import com.chess.domain.OneStepMoveStrategy;
import com.chess.domain.Piece;
import com.chess.domain.Position;
import com.chess.domain.WhiteTurnState;

import java.util.ArrayList;
import java.util.List;

public final class DomainTestRunner {

    private DomainTestRunner() {
    }

    public static void main(String[] args) {
        moveCommandCanBeUndone();
        gameChangesAndRestoresTurn();
        System.out.println("All domain tests passed.");
    }

    private static void moveCommandCanBeUndone() {
        Board board = new Board();
        Piece whitePiece = new Piece(Color.WHITE, List.of(new OneStepMoveStrategy()));
        Position from = new Position(3, 3);
        Position to = new Position(4, 4);
        board.placePiece(whitePiece, from);

        MoveCommand command = new MoveCommand(board, from, to);
        command.execute();
        require(board.pieceAt(to) == whitePiece, "The piece should be moved");

        command.undo();
        require(board.pieceAt(from) == whitePiece, "Undo should restore the piece");
    }

    private static void gameChangesAndRestoresTurn() {
        Board board = new Board();
        Piece whitePiece = new Piece(Color.WHITE, List.of(new OneStepMoveStrategy()));
        Position from = new Position(3, 3);
        Position to = new Position(4, 4);
        board.placePiece(whitePiece, from);

        IGameState terminalState = move -> {
            throw new IllegalStateException("No further turn is configured");
        };
        IGameState blackTurn = new BlackTurnState(terminalState);
        IGameState whiteTurn = new WhiteTurnState(blackTurn);
        Game game = new Game(board, whiteTurn, new DefaultMoveCommandFactory(),
                new ArrayList<>(), new ArrayList<>());

        game.playMove(from, to);
        require(game.currentState() == blackTurn, "The turn should change to black");

        game.undoLastMove();
        require(game.currentState() == whiteTurn, "Undo should restore the previous turn");
        require(board.pieceAt(from) == whitePiece, "Undo should restore the board");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
