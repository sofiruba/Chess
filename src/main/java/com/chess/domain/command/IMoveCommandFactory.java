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
 * Creates the command for a plain move. Special moves are created by their own {@link IMoveRule}.
 */
public interface IMoveCommandFactory {

    ICommand create(Board board, Position from, Position to);
}