package com.chess.configuration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.chess.domain.draw.IDrawRule;
import com.chess.domain.model.IMoveStrategy;
import com.chess.domain.model.PieceRoles;
import com.chess.domain.model.PieceType;
import com.chess.domain.model.Position;
import com.chess.domain.movement.DiagonalMoveStrategy;
import com.chess.domain.movement.LShapeMoveStrategy;
import com.chess.domain.movement.LinearMoveStrategy;
import com.chess.domain.movement.OneStepMoveStrategy;
import com.chess.domain.movement.PawnMoveStrategy;
import com.chess.domain.power.IPower;
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
 * @param mysteryBoxes casillas donde aparecen las cajas misteriosas al empezar; vacío en el ajedrez clásico
 * @param additionalPowers poderes que se suman a flor de hielo, pluma, estrella y caparazón
 * @param boxSpawning cada cuántos turnos aparecen cajas nuevas y cuántas por lado
 */
public record GameSetup(
        Map<PieceType, List<IMoveStrategy>> pieceDefinitions,
        List<PieceType> backRank,
        List<PieceType> promotionTypes,
        PieceRoles roles,
        List<IMoveRule> additionalMoveRules,
        List<IDrawRule> additionalDrawRules,
        List<Position> mysteryBoxes,
        List<IPower> additionalPowers,
        BoxSpawning boxSpawning) {

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
        mysteryBoxes = List.copyOf(Objects.requireNonNull(mysteryBoxes, "mysteryBoxes must not be null"));
        additionalPowers = List.copyOf(Objects.requireNonNull(additionalPowers, "additionalPowers must not be null"));
        Objects.requireNonNull(boxSpawning, "boxSpawning must not be null, use BoxSpawning.NONE");
        if (Set.copyOf(mysteryBoxes).size() != mysteryBoxes.size()) {
            throw new IllegalArgumentException("Two mystery boxes share a square");
        }
    }

    /** Una configuración sin cajas misteriosas. */
    public GameSetup(
            Map<PieceType, List<IMoveStrategy>> pieceDefinitions,
            List<PieceType> backRank,
            List<PieceType> promotionTypes,
            PieceRoles roles,
            List<IMoveRule> additionalMoveRules,
            List<IDrawRule> additionalDrawRules) {
        this(pieceDefinitions, backRank, promotionTypes, roles, additionalMoveRules, additionalDrawRules,
                List.of(), List.of(), BoxSpawning.NONE);
    }

    /**
     * La variante del trabajo: ajedrez estándar con cuatro cajas misteriosas en a5, c5, f4 y h4, y cada 5 turnos
     * hasta dos cajas nuevas en cada mitad del tablero, sin pasar nunca de cuatro cajas al mismo tiempo. Las
     * posiciones son simétricas por una rotación de 180 grados, por lo que ningún jugador tiene ventaja; el
     * contenido de cada caja se sortea por separado.
     */
    public static GameSetup mysteryBoxEdition() {
        return standard()
                .withMysteryBoxes(standardMysteryBoxes())
                .withBoxSpawning(new BoxSpawning(5, 2, 4));
    }

    public static List<Position> standardMysteryBoxes() {
        return List.of(new Position(0, 4), new Position(2, 4), new Position(5, 3), new Position(7, 3));
    }

    /** El ajedrez clásico, sin cajas misteriosas. */
    public static GameSetup standard() {
        PieceRoles roles = PieceRoles.standard();
        return new GameSetup(
                standardPieceDefinitions(roles),
                standardBackRank(roles),
                standardPromotionTypes(roles),
                roles,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                BoxSpawning.NONE);
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
        return new GameSetup(newDefinitions, backRank, promotionTypes, roles, additionalMoveRules, additionalDrawRules,
                mysteryBoxes, additionalPowers, boxSpawning);
    }

    public GameSetup withBackRank(List<PieceType> newBackRank) {
        return new GameSetup(pieceDefinitions, newBackRank, promotionTypes, roles, additionalMoveRules,
                additionalDrawRules, mysteryBoxes, additionalPowers, boxSpawning);
    }

    public GameSetup withPromotionTypes(List<PieceType> newPromotionTypes) {
        return new GameSetup(pieceDefinitions, backRank, newPromotionTypes, roles, additionalMoveRules,
                additionalDrawRules, mysteryBoxes, additionalPowers, boxSpawning);
    }

    public GameSetup withRoles(PieceRoles newRoles) {
        return new GameSetup(pieceDefinitions, backRank, promotionTypes, newRoles, additionalMoveRules,
                additionalDrawRules, mysteryBoxes, additionalPowers, boxSpawning);
    }

    public GameSetup withAdditionalMoveRules(List<IMoveRule> newRules) {
        return new GameSetup(pieceDefinitions, backRank, promotionTypes, roles, newRules, additionalDrawRules,
                mysteryBoxes, additionalPowers, boxSpawning);
    }

    public GameSetup withAdditionalDrawRules(List<IDrawRule> newRules) {
        return new GameSetup(pieceDefinitions, backRank, promotionTypes, roles, additionalMoveRules, newRules,
                mysteryBoxes, additionalPowers, boxSpawning);
    }

    public GameSetup withMysteryBoxes(List<Position> newBoxes) {
        return new GameSetup(pieceDefinitions, backRank, promotionTypes, roles, additionalMoveRules,
                additionalDrawRules, newBoxes, additionalPowers, boxSpawning);
    }

    public GameSetup withAdditionalPowers(List<IPower> newPowers) {
        return new GameSetup(pieceDefinitions, backRank, promotionTypes, roles, additionalMoveRules,
                additionalDrawRules, mysteryBoxes, newPowers, boxSpawning);
    }

    public GameSetup withBoxSpawning(BoxSpawning newSpawning) {
        return new GameSetup(pieceDefinitions, backRank, promotionTypes, roles, additionalMoveRules,
                additionalDrawRules, mysteryBoxes, additionalPowers, newSpawning);
    }
}
