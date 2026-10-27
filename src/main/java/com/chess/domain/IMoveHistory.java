package com.chess.domain;

import java.util.List;

public interface IMoveHistory {
    void record(ICommand command, IGameState previousState, CastlingRights previousRights,
            DrawCounters previousCounters);

    Entry undoLast();

    Entry redoLast();

    ICommand lastCommand();

    List<ICommand> commands();

    record Entry(ICommand command, IGameState previousState, CastlingRights previousCastlingRights,
            DrawCounters previousDrawCounters) {
    }
}
