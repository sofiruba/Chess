package com.chess.domain.game;

import java.time.Clock;
import java.util.Objects;

import com.chess.domain.port.GameRecord;
import com.chess.domain.port.IGameArchive;
import com.chess.domain.port.IGameQueries;
import com.chess.domain.port.IGameRepository;
import com.chess.domain.port.SavedGames;

/**
 * Guarda una partida en el repositorio con la fecha de un {@link Clock} inyectado, por lo que se puede probar sin
 * depender de la hora real.
 */
public final class GameArchive implements IGameArchive {

    private final IGameRepository repository;
    private final Clock clock;

    public GameArchive(IGameRepository repository, Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public GameRecord save(IGameQueries game) {
        Objects.requireNonNull(game, "game must not be null");
        return repository.save(GameRecord.unsaved(clock.instant(), game.result(), game.moveCodes()));
    }

    @Override
    public SavedGames findAll() {
        return repository.findAll();
    }
}