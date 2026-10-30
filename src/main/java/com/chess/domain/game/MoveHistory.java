package com.chess.domain.game;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.chess.domain.command.ICommand;
import com.chess.domain.command.IMoveInfo;

public final class MoveHistory implements IMoveHistory {

    private final List<Entry> executedMoves = new ArrayList<>();
    private final List<Entry> undoneMoves = new ArrayList<>();

    @Override
    public void record(ICommand command, GameSnapshot previous) {
        executedMoves.add(new Entry(command, previous));
        undoneMoves.clear();
    }

    @Override
    public Entry undoLast() {
        if (executedMoves.isEmpty()) {
            throw new IllegalStateException("There are no moves to undo");
        }
        Entry entry = executedMoves.remove(executedMoves.size() - 1);
        entry.command().undo();
        undoneMoves.add(entry);
        return entry;
    }

    @Override
    public Entry redoLast() {
        if (undoneMoves.isEmpty()) {
            throw new IllegalStateException("There are no moves to redo");
        }
        Entry entry = undoneMoves.remove(undoneMoves.size() - 1);
        executedMoves.add(entry);
        return entry;
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
}