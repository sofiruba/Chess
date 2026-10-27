package com.chess.domain;

import java.util.ArrayList;
import java.util.List;

public final class MoveHistory implements IMoveHistory {

    private final List<Entry> executedMoves = new ArrayList<>();
    private final List<Entry> undoneMoves = new ArrayList<>();

    @Override
    public void record(
            ICommand command,
            IGameState previousState,
            CastlingRights previousCastlingRights,
            DrawCounters previousDrawCounters) {
        executedMoves.add(new Entry(command, previousState, previousCastlingRights, previousDrawCounters));
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
    public ICommand lastCommand() {
        if (executedMoves.isEmpty()) {
            return null;
        }
        return executedMoves.get(executedMoves.size() - 1).command();
    }

    @Override
    public List<ICommand> commands() {
        return executedMoves.stream().map(Entry::command).toList();
    }

}