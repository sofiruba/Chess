package com.chess.domain.command;

import com.chess.domain.model.*;

import com.chess.domain.rule.*;


/**
 * Creates the command for a plain move. Special moves are created by their own {@link IMoveRule}.
 */
public interface IMoveCommandFactory {

    ICommand create(Board board, Position from, Position to);
}