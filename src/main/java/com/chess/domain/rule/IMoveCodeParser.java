package com.chess.domain.rule;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

/**
 * Turns a persisted move code ("e2e4", "e7e8q") back into a move request for the game.
 */
public interface IMoveCodeParser {

    /**
     * @throws IllegalArgumentException if the text is not a well-formed move code
     */
    MoveRequest parse(String moveCode);
}
