package com.chess;

import static com.chess.TestGames.at;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.chess.configuration.DefaultMoveCommandFactory;
import com.chess.configuration.StandardGameFactory;
import com.chess.domain.model.Color;
import com.chess.domain.movement.DiagonalMoveStrategy;
import com.chess.domain.port.IGameService;
import com.chess.domain.model.IMoveStrategy;
import com.chess.domain.movement.LShapeMoveStrategy;
import com.chess.domain.model.PieceType;

/**
 * Las opciones de promoción provienen de un único lugar. Se ofrece y puede elegirse una pieza que solo existe aquí.
 */
final class PromotionOptionsTest {

    private static final PieceType ARCHBISHOP = new PieceType("ARCHBISHOP", "a");
    private static final String EMPTY = "........";

    @Test
    void aStandardGameOffersTheFourStandardPieces() {
        IGameService game = TestGames.standard();

        assertEquals(
                List.of(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT),
                game.promotionOptions());
    }

    @Test
    void aPieceDefinedOnlyInTheTestIsOffered() {
        IGameService game = archbishopGame();

        assertEquals(List.of(PieceType.QUEEN, ARCHBISHOP), game.promotionOptions());
    }

    @Test
    void thePieceDefinedOnlyInTheTestCanBeChosen() {
        IGameService game = archbishopGame();

        game.playMove(at("a7"), at("a8"), ARCHBISHOP);

        assertEquals(ARCHBISHOP, game.pieceAt(at("a8")).type());
        assertEquals(List.of("a7a8a"), game.moveCodes());
    }

    @Test
    void thePieceDefinedOnlyInTheTestCanBeChosenFromAMoveCode() {
        IGameService game = archbishopGame();

        game.playMoveCode("a7a8a");

        assertEquals(ARCHBISHOP, game.pieceAt(at("a8")).type());
    }

    @Test
    void aStandardPieceThatIsNotOfferedIsRejected() {
        IGameService game = archbishopGame();

        assertThrows(IllegalArgumentException.class, () -> game.playMove(at("a7"), at("a8"), PieceType.ROOK));
        assertThrows(IllegalArgumentException.class, () -> game.playMoveCode("a7a8r"));
    }

    private IGameService archbishopGame() {
        Map<PieceType, List<IMoveStrategy>> definitions =
                new LinkedHashMap<>(StandardGameFactory.standardPieceDefinitions());
        definitions.put(ARCHBISHOP, List.of(new DiagonalMoveStrategy(), new LShapeMoveStrategy()));
        StandardGameFactory factory = new StandardGameFactory(
                new DefaultMoveCommandFactory(),
                definitions,
                StandardGameFactory.standardBackRank(),
                List.of(),
                List.of(PieceType.QUEEN, ARCHBISHOP));
        return factory.create(TestGames.board(
                "....k...",
                "P.......",
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                "....K..."), Color.WHITE);
    }
}
