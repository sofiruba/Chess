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
import java.util.Objects;

/**
 * The result of reading the game history: the games that could be read, newest first, and how many stored rows
 * had to be left out because they were damaged.
 */
public record SavedGames(List<GameRecord> records, int unreadableRows) {
    public SavedGames {
        Objects.requireNonNull(records, "records must not be null");
        if (unreadableRows < 0) {
            throw new IllegalArgumentException("unreadableRows must not be negative: " + unreadableRows);
        }
        records = List.copyOf(records);
    }
}