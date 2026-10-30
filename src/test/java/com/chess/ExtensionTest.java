package com.chess;

import static com.chess.TestGames.at;
import static com.chess.TestGames.play;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.chess.configuration.DefaultMoveCommandFactory;
import com.chess.configuration.StandardGameFactory;
import com.chess.domain.model.Board;
import com.chess.domain.model.Color;
import com.chess.domain.model.GameResult;
import com.chess.domain.port.IGameService;
import com.chess.domain.model.IMoveStrategy;
import com.chess.domain.model.PieceFactory;
import com.chess.domain.model.PieceType;
import com.chess.domain.model.Position;
import com.chess.domain.model.StandardBoardSetup;

/**
 * Extensiones realizadas solo con configuración: una pieza definida en este archivo y tableros de otros tamaños.
 */
final class ExtensionTest {

    private static final PieceType CAMEL = new PieceType("CAMEL", "c");
    private static final String EMPTY_TEN = "..........";

    /** Pieza saltadora que avanza tres casillas en una dirección y una en la otra. Solo existe en esta prueba. */
    private static final class CamelMoveStrategy implements IMoveStrategy {

        private static final int[][] OFFSETS = {
                {3, 1}, {3, -1}, {-3, 1}, {-3, -1}, {1, 3}, {1, -3}, {-1, 3}, {-1, -3}
        };

        @Override
        public List<Position> getValidMoves(Board board, Position currentPosition) {
            Color color = board.pieceAt(currentPosition).color();
            List<Position> moves = new ArrayList<>();
            for (int[] offset : OFFSETS) {
                int x = currentPosition.x() + offset[0];
                int y = currentPosition.y() + offset[1];
                if (board.contains(x, y) && isFreeOrEnemy(board, new Position(x, y), color)) {
                    moves.add(new Position(x, y));
                }
            }
            return moves;
        }

        private boolean isFreeOrEnemy(Board board, Position position, Color color) {
            return board.pieceAt(position) == null || board.pieceAt(position).color() != color;
        }
    }

    @Test
    void newPieceStartsOnTheBoardAndMovesWithItsOwnStrategy() {
        IGameService game = camelFactory(camelBackRank()).create();

        assertEquals(CAMEL, game.pieceAt(at("b1")).type());
        assertEquals(Set.of(at("a4"), at("c4")), new HashSet<>(game.legalMoves(at("b1"))));
        play(game, "b1c4");
        assertEquals(CAMEL, game.pieceAt(at("c4")).type());
    }

    @Test
    void newPieceGivesCheckWithoutTouchingTheCheckDetector() {
        Board board = new Board(8);
        PieceFactory pieces = new PieceFactory(camelDefinitions());
        board.placePiece(pieces.create(Color.WHITE, CAMEL), at("a1"));
        board.placePiece(pieces.create(Color.WHITE, PieceType.KING), at("h8"));
        board.placePiece(pieces.create(Color.BLACK, PieceType.KING), at("d2"));

        IGameService game = camelFactory(camelBackRank()).create(board, Color.BLACK);

        assertTrue(game.isInCheck(Color.BLACK));
        assertFalse(game.isFinished());
    }

    @Test
    void newPieceCountsAsEnoughMaterial() {
        Board board = new Board(8);
        PieceFactory pieces = new PieceFactory(camelDefinitions());
        board.placePiece(pieces.create(Color.WHITE, CAMEL), at("a1"));
        board.placePiece(pieces.create(Color.WHITE, PieceType.KING), at("e1"));
        board.placePiece(pieces.create(Color.BLACK, PieceType.KING), at("e8"));

        IGameService game = camelFactory(camelBackRank()).create(board, Color.WHITE);

        assertFalse(game.isFinished());
    }

    @Test
    void tenByTenBoardIsBuiltFromTheBackRank() {
        IGameService game = tenFactory().create();

        assertEquals(10, game.boardSize());
        assertEquals(PieceType.KING, game.pieceAt(at("f1")).type());
        assertEquals(PieceType.KING, game.pieceAt(at("f10")).type());
        assertEquals(PieceType.ROOK, game.pieceAt(at("j10")).type());
        assertEquals(PieceType.PAWN, game.pieceAt(at("a2")).type());
        assertEquals(PieceType.PAWN, game.pieceAt(at("j9")).type());
        assertNull(game.pieceAt(at("a3")));
    }

    @Test
    void pawnsDoubleStepFromTheirRankOnTenByTen() {
        IGameService game = tenFactory().create();

        play(game, "a2a4", "a9a7");

        assertEquals(PieceType.PAWN, game.pieceAt(at("a4")).type());
        assertEquals(PieceType.PAWN, game.pieceAt(at("a7")).type());
    }

    @Test
    void promotionHappensOnTheLastRankOfTenByTen() {
        IGameService game = tenGameFromDiagram(Color.WHITE,
                ".....k....",
                "P.........",
                EMPTY_TEN, EMPTY_TEN, EMPTY_TEN, EMPTY_TEN, EMPTY_TEN, EMPTY_TEN, EMPTY_TEN,
                ".....K....");
        assertTrue(game.requiresPromotion(at("a9"), at("a10")));

        play(game, "a9a10r");

        assertEquals(PieceType.ROOK, game.pieceAt(at("a10")).type());
    }

    @Test
    void castlingUsesTheCornersOfTenByTen() {
        IGameService game = tenGameFromDiagram(Color.WHITE,
                ".....k....",
                EMPTY_TEN, EMPTY_TEN, EMPTY_TEN, EMPTY_TEN, EMPTY_TEN, EMPTY_TEN, EMPTY_TEN, EMPTY_TEN,
                "R....K...R");
        assertTrue(game.legalMoves(at("f1")).contains(at("h1")));
        assertTrue(game.legalMoves(at("f1")).contains(at("d1")));

        play(game, "f1h1");

        assertEquals(PieceType.KING, game.pieceAt(at("h1")).type());
        assertEquals(PieceType.ROOK, game.pieceAt(at("g1")).type());
        assertNull(game.pieceAt(at("j1")));
    }

    @Test
    void drawRulesWorkOnOtherSizes() {
        IGameService game = tenGameFromDiagram(Color.WHITE,
                ".....k....",
                EMPTY_TEN, EMPTY_TEN, EMPTY_TEN, EMPTY_TEN, EMPTY_TEN, EMPTY_TEN, EMPTY_TEN, EMPTY_TEN,
                ".....K....");

        assertTrue(game.isFinished());
        assertEquals(GameResult.DRAW, game.result());
    }

    @Test
    void sixBySixBoardOnlyAllowsCastlingWhereThereIsRoom() {
        StandardGameFactory factory = new StandardGameFactory(
                new DefaultMoveCommandFactory(),
                StandardGameFactory.standardPieceDefinitions(),
                List.of(PieceType.ROOK, PieceType.KNIGHT, PieceType.QUEEN, PieceType.KING, PieceType.KNIGHT,
                        PieceType.ROOK),
                List.of());
        Board board = TestGames.board(
                "...k..",
                "......",
                "......",
                "......",
                "......",
                "R..K.R");
        IGameService game = factory.create(board, Color.WHITE);

        assertEquals(6, game.boardSize());
        assertTrue(game.legalMoves(at("d1")).contains(at("b1")));
        assertFalse(game.legalMoves(at("d1")).contains(at("f1")));
    }

    @Test
    void boardRejectsPositionsOutsideItsSize() {
        Position far = new Position(8, 0);

        assertThrows(IllegalArgumentException.class, () -> new Board(8).pieceAt(far));
        assertNull(new Board(10).pieceAt(far));
    }

    @Test
    void setupRejectsBackRanksThatDoNotFit() {
        PieceFactory pieces = new PieceFactory(StandardGameFactory.standardPieceDefinitions());

        IllegalArgumentException tooShort = assertThrows(IllegalArgumentException.class,
                () -> new StandardBoardSetup(pieces, List.of(PieceType.ROOK, PieceType.KING, PieceType.ROOK)));
        IllegalArgumentException noKing = assertThrows(IllegalArgumentException.class,
                () -> new StandardBoardSetup(pieces, List.of(PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP,
                        PieceType.QUEEN, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK, PieceType.ROOK)));
        IllegalArgumentException misplacedKing = assertThrows(IllegalArgumentException.class,
                () -> new StandardBoardSetup(pieces, List.of(PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP,
                        PieceType.QUEEN, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK, PieceType.KING)));

        assertTrue(tooShort.getMessage().contains("at least 4"));
        assertTrue(noKing.getMessage().contains("exactly one king"));
        assertTrue(misplacedKing.getMessage().contains("column 4"));
    }

    @Test
    void unregisteredPiecesAreRejectedByTheFactory() {
        PieceFactory pieces = new PieceFactory(StandardGameFactory.standardPieceDefinitions());

        assertThrows(IllegalArgumentException.class, () -> pieces.create(Color.WHITE, CAMEL));
    }

    private Map<PieceType, List<IMoveStrategy>> camelDefinitions() {
        Map<PieceType, List<IMoveStrategy>> definitions =
                new LinkedHashMap<>(StandardGameFactory.standardPieceDefinitions());
        definitions.put(CAMEL, List.of(new CamelMoveStrategy()));
        return definitions;
    }

    private List<PieceType> camelBackRank() {
        return List.of(PieceType.ROOK, CAMEL, PieceType.BISHOP, PieceType.QUEEN, PieceType.KING, PieceType.BISHOP,
                PieceType.KNIGHT, PieceType.ROOK);
    }

    private StandardGameFactory camelFactory(List<PieceType> backRank) {
        return new StandardGameFactory(new DefaultMoveCommandFactory(), camelDefinitions(), backRank, List.of());
    }

    private List<PieceType> tenBackRank() {
        return List.of(PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.KNIGHT, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.BISHOP, PieceType.ROOK);
    }

    private StandardGameFactory tenFactory() {
        return new StandardGameFactory(
                new DefaultMoveCommandFactory(),
                StandardGameFactory.standardPieceDefinitions(),
                tenBackRank(),
                List.of());
    }

    private IGameService tenGameFromDiagram(Color turn, String... rows) {
        return tenFactory().create(TestGames.board(rows), turn);
    }
}
