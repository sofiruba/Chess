package com.chess.domain.game;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

import java.util.List;
import java.util.Optional;

public interface IMoveHistory {

    /**
     * @param previous the snapshot before the command was executed; undo gives it back
     */
    void record(ICommand command, GameSnapshot previous);

    Entry undoLast();

    Entry redoLast();

    /** Read-only view of the last executed move; empty before the first move. */
    Optional<IMoveInfo> lastMove();

    /** Read-only view of the executed moves, oldest first. */
    List<IMoveInfo> moves();

    record Entry(ICommand command, GameSnapshot previous) {
    }
}