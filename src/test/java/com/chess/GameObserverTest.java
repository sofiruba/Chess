package com.chess;

import static com.chess.TestGames.at;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.chess.domain.port.IGameListener;
import com.chess.domain.port.IGameService;

final class GameObserverTest {

    /** Anota los eventos en el orden en que llegan. */
    private static final class Recorder implements IGameListener {
        final List<String> events = new ArrayList<>();

        @Override
        public void moveMade() {
            events.add("moveMade");
        }

        @Override
        public void moveUndone() {
            events.add("moveUndone");
        }

        @Override
        public void moveRedone() {
            events.add("moveRedone");
        }

        @Override
        public void gameFinished() {
            events.add("gameFinished");
        }
    }

    @Test
    void aPlayedMoveIsNotified() {
        IGameService game = TestGames.standard();
        Recorder recorder = new Recorder();
        game.addListener(recorder);

        game.playMove(at("e2"), at("e4"));

        assertEquals(List.of("moveMade"), recorder.events);
    }

    @Test
    void undoAndRedoAreNotified() {
        IGameService game = TestGames.standard();
        game.playMove(at("e2"), at("e4"));
        Recorder recorder = new Recorder();
        game.addListener(recorder);

        game.undoLastMove();
        game.redoLastMove();

        assertEquals(List.of("moveUndone", "moveRedone"), recorder.events);
    }

    @Test
    void theListenerSeesTheNewStateWhenNotified() {
        IGameService game = TestGames.standard();
        List<Integer> movesSeen = new ArrayList<>();
        game.addListener(new IGameListener() {
            @Override
            public void moveMade() {
                movesSeen.add(game.moveCodes().size());
            }
        });

        game.playMove(at("e2"), at("e4"));

        assertEquals(List.of(1), movesSeen);
    }

    @Test
    void finishingTheGameIsNotifiedAfterTheLastMove() {
        IGameService game = TestGames.standard();
        Recorder recorder = new Recorder();
        game.addListener(recorder);

        game.playMove(at("f2"), at("f3"));
        game.playMove(at("e7"), at("e5"));
        game.playMove(at("g2"), at("g4"));
        game.playMove(at("d8"), at("h4"));

        assertEquals(
                List.of("moveMade", "moveMade", "moveMade", "moveMade", "gameFinished"),
                recorder.events);
    }

    @Test
    void anIllegalMoveOrAFailedUndoNotifiesNothing() {
        IGameService game = TestGames.standard();
        Recorder recorder = new Recorder();
        game.addListener(recorder);

        assertThrows(IllegalArgumentException.class, () -> game.playMove(at("e2"), at("e5")));
        assertThrows(IllegalStateException.class, game::undoLastMove);

        assertEquals(List.of(), recorder.events);
    }

    @Test
    void aRemovedListenerStopsReceivingEvents() {
        IGameService game = TestGames.standard();
        Recorder recorder = new Recorder();
        game.addListener(recorder);
        game.removeListener(recorder);

        game.playMove(at("e2"), at("e4"));

        assertEquals(List.of(), recorder.events);
    }

    @Test
    void addingTheSameListenerTwiceNotifiesItOnce() {
        IGameService game = TestGames.standard();
        Recorder recorder = new Recorder();
        game.addListener(recorder);
        game.addListener(recorder);

        game.playMove(at("e2"), at("e4"));

        assertEquals(List.of("moveMade"), recorder.events);
    }
}