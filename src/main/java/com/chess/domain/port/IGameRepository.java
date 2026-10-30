package com.chess.domain.port;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

/**
 * Output port for the game history. Decision: only the history of games is stored, there are no users.
 */
public interface IGameRepository {
    /**
     * @param record a record with id {@link GameRecord#UNSAVED_ID}
     * @return the stored record, with the id the repository assigned
     */
    GameRecord save(GameRecord record);

    /** Readable games, newest first, and the number of stored rows that could not be read. */
    SavedGames findAll();
}