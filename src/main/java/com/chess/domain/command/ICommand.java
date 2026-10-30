package com.chess.domain.command;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

/**
 * A move that can be executed and undone on the board. Everything that only reads a move uses {@link IMoveInfo}.
 */
public interface ICommand extends IMoveInfo {

    void execute();

    void undo();
}