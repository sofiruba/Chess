package com.chess.configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.chess.domain.model.Board;
import com.chess.domain.rule.CastlingRule;
import com.chess.domain.rule.CheckDetector;
import com.chess.domain.model.Color;
import com.chess.domain.state.DefaultGameEndEvaluator;
import com.chess.domain.movement.DiagonalMoveStrategy;
import com.chess.domain.rule.EnPassantRule;
import com.chess.domain.draw.FiftyMoveRule;
import com.chess.domain.game.Game;
import com.chess.domain.game.GameSnapshot;
import com.chess.domain.rule.ICheckDetector;
import com.chess.domain.draw.IDrawRule;
import com.chess.domain.port.IGameFactory;
import com.chess.domain.port.IGameService;
import com.chess.domain.command.IMoveCommandFactory;
import com.chess.domain.rule.IMoveGenerator;
import com.chess.domain.rule.IMoveRule;
import com.chess.domain.model.IMoveStrategy;
import com.chess.domain.model.IPieceFactory;
import com.chess.domain.state.ITurnStateFactory;
import com.chess.domain.draw.InsufficientMaterialRule;
import com.chess.domain.movement.LShapeMoveStrategy;
import com.chess.domain.movement.LinearMoveStrategy;
import com.chess.domain.rule.MoveCodeParser;
import com.chess.domain.rule.MoveGenerator;
import com.chess.domain.game.MoveHistory;
import com.chess.domain.movement.OneStepMoveStrategy;
import com.chess.domain.movement.PawnMoveStrategy;
import com.chess.domain.model.PieceFactory;
import com.chess.domain.model.PieceType;
import com.chess.domain.rule.PromotionRule;
import com.chess.domain.model.StandardBoardSetup;
import com.chess.domain.draw.ThreefoldRepetitionRule;
import com.chess.domain.state.TurnStateFactory;

/**
 * Configura una partida estándar. Las piezas, fila trasera, reglas de movimiento y reglas de tablas se registran
 * aquí, sin modificar Game ni las reglas existentes. El tamaño del tablero es la longitud de la fila trasera.
 */
public final class StandardGameFactory implements IGameFactory {

    private static final List<PieceType> STANDARD_PROMOTION_TYPES =
            List.of(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT);
    private static final int FIFTY_MOVE_LIMIT = 50;

    private final List<IMoveRule> additionalRules;
    private final List<PieceType> promotionTypes;
    private final IPieceFactory pieceFactory;
    private final StandardBoardSetup boardSetup;
    private final ICheckDetector checkDetector = new CheckDetector();
    private final IMoveGenerator moveGenerator;
    private final ITurnStateFactory stateFactory;

    public StandardGameFactory(IMoveCommandFactory commandFactory) {
        this(commandFactory, List.of());
    }

    public StandardGameFactory(IMoveCommandFactory commandFactory, List<IMoveRule> additionalRules) {
        this(commandFactory, standardPieceDefinitions(), standardBackRank(), additionalRules);
    }

    public StandardGameFactory(
            IMoveCommandFactory commandFactory,
            Map<PieceType, List<IMoveStrategy>> pieceDefinitions,
            List<PieceType> backRank,
            List<IMoveRule> additionalRules) {
        this(commandFactory, pieceDefinitions, backRank, additionalRules, STANDARD_PROMOTION_TYPES);
    }

    /**
     * @param pieceDefinitions registro de tipos de pieza a estrategias de movimiento
     * @param backRank piezas de la fila trasera desde la columna 0; su longitud es el tamaño del tablero
     * @param promotionTypes piezas en las que puede convertirse un peón, en el orden ofrecido; la primera es predeterminada
     */
    public StandardGameFactory(
            IMoveCommandFactory commandFactory,
            Map<PieceType, List<IMoveStrategy>> pieceDefinitions,
            List<PieceType> backRank,
            List<IMoveRule> additionalRules,
            List<PieceType> promotionTypes) {
        Objects.requireNonNull(commandFactory, "commandFactory must not be null");
        this.additionalRules = List.copyOf(Objects.requireNonNull(additionalRules, "additionalRules must not be null"));
        this.promotionTypes = List.copyOf(Objects.requireNonNull(promotionTypes, "promotionTypes must not be null"));
        this.pieceFactory = new PieceFactory(pieceDefinitions);
        this.boardSetup = new StandardBoardSetup(pieceFactory, backRank);
        this.moveGenerator = new MoveGenerator(commandFactory, checkDetector, moveRules());
        this.stateFactory = new TurnStateFactory(moveGenerator);
    }

    public static Map<PieceType, List<IMoveStrategy>> standardPieceDefinitions() {
        Map<PieceType, List<IMoveStrategy>> definitions = new LinkedHashMap<>();
        definitions.put(PieceType.KING, List.of(new OneStepMoveStrategy()));
        definitions.put(PieceType.QUEEN, List.of(new LinearMoveStrategy(), new DiagonalMoveStrategy()));
        definitions.put(PieceType.ROOK, List.of(new LinearMoveStrategy()));
        definitions.put(PieceType.BISHOP, List.of(new DiagonalMoveStrategy()));
        definitions.put(PieceType.KNIGHT, List.of(new LShapeMoveStrategy()));
        definitions.put(PieceType.PAWN, List.of(new PawnMoveStrategy()));
        return definitions;
    }

    public static List<PieceType> standardBackRank() {
        return List.of(
                PieceType.ROOK,
                PieceType.KNIGHT,
                PieceType.BISHOP,
                PieceType.QUEEN,
                PieceType.KING,
                PieceType.BISHOP,
                PieceType.KNIGHT,
                PieceType.ROOK);
    }

    @Override
    public IGameService create() {
        return create(boardSetup.create(), Color.WHITE);
    }

    public IGameService create(Board board, Color firstPlayer) {
        return new Game(
                board,
                GameSnapshot.initial(board, stateFactory.forColor(firstPlayer)),
                checkDetector,
                new MoveHistory(),
                new DefaultGameEndEvaluator(checkDetector, stateFactory, drawRules()),
                moveGenerator,
                new MoveCodeParser(promotionTypes));
    }

    private List<IMoveRule> moveRules() {
        List<IMoveRule> rules = new ArrayList<>();
        rules.add(new CastlingRule());
        rules.add(new EnPassantRule());
        rules.add(new PromotionRule(pieceFactory, promotionTypes));
        rules.addAll(additionalRules);
        return rules;
    }

    private List<IDrawRule> drawRules() {
        return List.of(
                new InsufficientMaterialRule(),
                new ThreefoldRepetitionRule(),
                new FiftyMoveRule(FIFTY_MOVE_LIMIT));
    }
}