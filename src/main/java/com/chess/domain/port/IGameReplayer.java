package com.chess.domain.port;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

import java.util.List;

/**
 * Rebuilds a game from the move codes that were stored for it. It exists so that the UI can load a saved game
 * without knowing how a game is created or how move codes are parsed.
 */
public interface IGameReplayer {

    /**
     * @param moveCodes the move codes in the order they were played; an empty list gives a new game
     * @return a game in which all the moves have been played and can be undone and redone
     * @throws IllegalArgumentException if a move code is malformed or is not legal at its turn
     */
    IGameService replay(List<String> moveCodes);
}
