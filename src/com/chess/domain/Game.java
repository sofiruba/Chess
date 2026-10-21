package com.chess.domain;

import java.util.List;
import java.util.Objects;

public final class Game {

    private final Board board;
    private IGameState currentState;
    private final List<ICommand> moveHistory;
    private final List<IGameState> stateHistory;
    private final IMoveCommandFactory commandFactory;

    public Game(
            Board board,
            IGameState initialState,
            IMoveCommandFactory commandFactory,
            List<ICommand> moveHistory,
            List<IGameState> stateHistory) {
        this.board = Objects.requireNonNull(board, "board must not be null");
        this.currentState = Objects.requireNonNull(initialState, "initialState must not be null");
        this.commandFactory = Objects.requireNonNull(commandFactory, "commandFactory must not be null");
        this.moveHistory = Objects.requireNonNull(moveHistory, "moveHistory must not be null");
        this.stateHistory = Objects.requireNonNull(stateHistory, "stateHistory must not be null");
    }

    public void playMove(Position from, Position to) {
        MoveCommand command = commandFactory.create(board, from, to);
        IGameState previousState = currentState;
        IGameState nextState = currentState.handleMove(command);
        moveHistory.add(command);
        stateHistory.add(previousState);
        currentState = nextState;
    }

    public void undoLastMove() {
        if (moveHistory.isEmpty()) {
            throw new IllegalStateException("There are no moves to undo");
        }
        int lastIndex = moveHistory.size() - 1;
        moveHistory.remove(lastIndex).undo();
        currentState = stateHistory.remove(lastIndex);
    }

    public Board board() {
        return board;
    }

    public IGameState currentState() {
        return currentState;
    }

    public List<ICommand> moveHistory() {
        return List.copyOf(moveHistory);
    }
}
