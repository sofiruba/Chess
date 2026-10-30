package com.chess;

import static com.chess.TestGames.at;
import static com.chess.TestGames.play;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.chess.domain.model.Color;
import com.chess.domain.model.Piece;
import com.chess.domain.port.IGameService;

final class TurnTest {

    @Test
    void movingAPieceOfTheOpponentThrowsAndChangesNothing() {
        IGameService game = TestGames.standard();
        Piece blackPawn = game.pieceAt(at("e7"));

        assertThrows(IllegalArgumentException.class, () -> game.playMove(at("e7"), at("e6")));

        assertSame(blackPawn, game.pieceAt(at("e7")));
        assertNull(game.pieceAt(at("e6")));
        assertEquals(Color.WHITE, game.turn());
    }

    @Test
    void movingAnEmptySquareThrowsAndChangesNothing() {
        IGameService game = TestGames.standard();

        assertThrows(IllegalArgumentException.class, () -> game.playMove(at("e3"), at("e4")));

        assertNull(game.pieceAt(at("e3")));
        assertNull(game.pieceAt(at("e4")));
        assertEquals(Color.WHITE, game.turn());
    }

    @Test
    void movingAfterTheGameEndsThrowsAndChangesNothing() {
        IGameService game = TestGames.standard();
        play(game, "f2f3", "e7e5", "g2g4", "d8h4");
        Piece whitePawn = game.pieceAt(at("e2"));
        Color turnAtTheEnd = game.turn();

        assertThrows(IllegalArgumentException.class, () -> game.playMove(at("e2"), at("e3")));

        assertSame(whitePawn, game.pieceAt(at("e2")));
        assertNull(game.pieceAt(at("e3")));
        assertEquals(turnAtTheEnd, game.turn());
    }
}
