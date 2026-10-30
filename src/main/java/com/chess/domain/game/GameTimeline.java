package com.chess.domain.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.chess.domain.command.ICommand;
import com.chess.domain.command.IMoveInfo;
import com.chess.domain.model.Board;
import com.chess.domain.state.IGameState;

public final class GameTimeline implements IGameTimeline {

    private final List<Entry> executedMoves = new ArrayList<>();
    private final List<Entry> undoneMoves = new ArrayList<>();
    private GameSnapshot current;

    public GameTimeline(GameSnapshot initial) {
        this.current = Objects.requireNonNull(initial, "initial must not be null");
    }

    @Override
    public GameSnapshot current() {
        return current;
    }

    @Override
    public void advance(ICommand command, Board board) {
        Objects.requireNonNull(command, "command must not be null");
        GameSnapshot next = executeOnCurrent(command, board);
        executedMoves.add(new Entry(command, current));
        undoneMoves.clear();
        current = next;
    }

    @Override
    public void undo() {
        if (executedMoves.isEmpty()) {
            throw new IllegalStateException("There are no moves to undo");
        }
        Entry entry = executedMoves.remove(executedMoves.size() - 1);
        entry.command().undo();
        undoneMoves.add(entry);
        current = entry.previous();
    }

    @Override
    public void redo(Board board) {
        if (undoneMoves.isEmpty()) {
            throw new IllegalStateException("There are no moves to redo");
        }
        Entry entry = undoneMoves.get(undoneMoves.size() - 1);
        GameSnapshot next = executeOnCurrent(entry.command(), board);
        undoneMoves.remove(undoneMoves.size() - 1);
        executedMoves.add(entry);
        current = next;
    }

    @Override
    public void replaceState(IGameState state) {
        current = current.withState(state);
    }

    @Override
    public Optional<IMoveInfo> lastMove() {
        if (executedMoves.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(executedMoves.get(executedMoves.size() - 1).command());
    }

    @Override
    public List<IMoveInfo> moves() {
        return executedMoves.stream().<IMoveInfo>map(Entry::command).toList();
    }

    private GameSnapshot executeOnCurrent(ICommand command, Board board) {
        IGameState nextState = current.state().handleMove(command);
        return current.after(command, board, nextState);
    }

    private record Entry(ICommand command, GameSnapshot previous) {
    }
}