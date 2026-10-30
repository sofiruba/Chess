package com.chess.configuration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.chess.domain.draw.IDrawRule;
import com.chess.domain.model.IMoveStrategy;
import com.chess.domain.model.PieceRoles;
import com.chess.domain.model.PieceType;
import com.chess.domain.movement.DiagonalMoveStrategy;
import com.chess.domain.movement.LShapeMoveStrategy;
import com.chess.domain.movement.LinearMoveStrategy;
import com.chess.domain.movement.OneStepMoveStrategy;
import com.chess.domain.movement.PawnMoveStrategy;
import com.chess.domain.rule.IMoveRule;

/**
 * Todo lo que se puede cambiar de una partida sin tocar el núcleo: las piezas y su movimiento, la fila trasera (su
 * longitud es el tamaño del tablero), las piezas de promoción, los roles de las piezas y las reglas extra de
 * movimiento y de tablas. Es inmutable: cada {@code with...} devuelve una copia con ese único cambio.
 *
 * @param pieceDefinitions registro de tipos de pieza a estrategias de movimiento
 * @param backRank piezas de la fila trasera desde la columna 0
 * @param promotionTypes piezas en las que puede convertirse un peón, en el orden ofrecido; la primera es la
 *     predeterminada
 * @param roles las piezas que las reglas tratan como rey, torre, peón, alfil y caballo
 * @param additionalMoveRules reglas de movimiento que se suman a enroque, captura al paso y promoción
 * @param additionalDrawRules reglas de tablas que se suman a material insuficiente, repetición y 50 jugadas
 */
public record GameSetup(
        Map<PieceType, List<IMoveStrategy>> pieceDefinitions,
        List<PieceType> backRank,
        List<PieceType> promotionTypes,
        PieceRoles roles,
        List<IMoveRule> additionalMoveRules,
        List<IDrawRule> additionalDrawRules) {

    public GameSetup {
        Objects.requireNonNull(pieceDefinitions, "pieceDefinitions must not be null");
        Objects.requireNonNull(backRank, "backRank must not be null");
        Objects.requireNonNull(promotionTypes, "promotionTypes must not be null");
        Objects.requireNonNull(roles, "roles must not be null");
        Objects.requireNonNull(additionalMoveRules, "additionalMoveRules must not be null");
        Objects.requireNonNull(additionalDrawRules, "additionalDrawRules must not be null");
        pieceDefinitions = Collections.unmodifiableMap(new LinkedHashMap<>(pieceDefinitions));
        backRank = List.copyOf(backRank);
        promotionTypes = List.copyOf(promotionTypes);
        additionalMoveRules = List.copyOf(additionalMoveRules);
        additionalDrawRules = List.copyOf(additionalDrawRules);
    }

    public static GameSetup standard() {
        PieceRoles roles = PieceRoles.standard();
        return new GameSetup(
                standardPieceDefinitions(roles),
                standardBackRank(roles),
                standardPromotionTypes(roles),
                roles,
                List.of(),
                List.of());
    }

    public static Map<PieceType, List<IMoveStrategy>> standardPieceDefinitions() {
        return standardPieceDefinitions(PieceRoles.standard());
    }

    public static Map<PieceType, List<IMoveStrategy>> standardPieceDefinitions(PieceRoles roles) {
        Map<PieceType, List<IMoveStrategy>> definitions = new LinkedHashMap<>();
        definitions.put(roles.king(), List.of(new OneStepMoveStrategy()));
        definitions.put(PieceType.QUEEN, List.of(new LinearMoveStrategy(), new DiagonalMoveStrategy()));
        definitions.put(roles.rook(), List.of(new LinearMoveStrategy()));
        definitions.put(roles.bishop(), List.of(new DiagonalMoveStrategy()));
        definitions.put(roles.knight(), List.of(new LShapeMoveStrategy()));
        definitions.put(roles.pawn(), List.of(new PawnMoveStrategy()));
        return definitions;
    }

    public static List<PieceType> standardBackRank(PieceRoles roles) {
        return List.of(
                roles.rook(),
                roles.knight(),
                roles.bishop(),
                PieceType.QUEEN,
                roles.king(),
                roles.bishop(),
                roles.knight(),
                roles.rook());
    }

    public static List<PieceType> standardPromotionTypes(PieceRoles roles) {
        return List.of(PieceType.QUEEN, roles.rook(), roles.bishop(), roles.knight());
    }

    public GameSetup withPieceDefinitions(Map<PieceType, List<IMoveStrategy>> newDefinitions) {
        return new GameSetup(newDefinitions, backRank, promotionTypes, roles, additionalMoveRules, additionalDrawRules);
    }

    public GameSetup withBackRank(List<PieceType> newBackRank) {
        return new GameSetup(pieceDefinitions, newBackRank, promotionTypes, roles, additionalMoveRules,
                additionalDrawRules);
    }

    public GameSetup withPromotionTypes(List<PieceType> newPromotionTypes) {
        return new GameSetup(pieceDefinitions, backRank, newPromotionTypes, roles, additionalMoveRules,
                additionalDrawRules);
    }

    public GameSetup withRoles(PieceRoles newRoles) {
        return new GameSetup(pieceDefinitions, backRank, promotionTypes, newRoles, additionalMoveRules,
                additionalDrawRules);
    }

    public GameSetup withAdditionalMoveRules(List<IMoveRule> newRules) {
        return new GameSetup(pieceDefinitions, backRank, promotionTypes, roles, newRules, additionalDrawRules);
    }

    public GameSetup withAdditionalDrawRules(List<IDrawRule> newRules) {
        return new GameSetup(pieceDefinitions, backRank, promotionTypes, roles, additionalMoveRules, newRules);
    }
}