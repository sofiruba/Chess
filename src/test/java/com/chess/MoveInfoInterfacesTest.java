package com.chess;

import static com.chess.TestGames.at;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.chess.domain.command.ICommand;
import com.chess.domain.command.IMoveInfo;
import com.chess.domain.command.MoveCommand;
import com.chess.domain.draw.DrawCounters;
import com.chess.domain.draw.PositionKey;
import com.chess.domain.game.GameSnapshot;
import com.chess.domain.game.GameTimeline;
import com.chess.domain.game.IGameTimeline;
import com.chess.domain.model.Board;
import com.chess.domain.model.Color;
import com.chess.domain.model.PieceRoles;
import com.chess.domain.port.IGameQueries;
import com.chess.domain.rule.CastlingRights;
import com.chess.domain.rule.CastlingRule;
import com.chess.domain.rule.EnPassantRule;
import com.chess.domain.rule.IMoveRule;
import com.chess.domain.rule.MoveRuleContext;
import com.chess.domain.rule.PromotionRule;
import com.chess.domain.state.ITurnStateFactory;
import com.chess.domain.state.TurnStateFactory;

final class MoveInfoInterfacesTest {

    private static final String COMMAND = "com.chess.domain.command.ICommand";

    @Test
    void aMoveInfoCannotExecuteNorUndo() {
        Set<String> names = methodNames(IMoveInfo.class);

        assertFalse(names.contains("execute"));
        assertFalse(names.contains("undo"));
    }

    @Test
    void aCommandIsAMoveInfoThatAddsOnlyExecuteAndUndo() {
        assertTrue(IMoveInfo.class.isAssignableFrom(ICommand.class));
        assertEquals(Set.of("execute", "undo"), declaredMethodNames(ICommand.class));
    }

    @Test
    void whatOnlyReadsMovesNeverReceivesACommand() {
        List<Class<?>> readers = List.of(
                MoveRuleContext.class,
                IMoveRule.class,
                EnPassantRule.class,
                CastlingRule.class,
                PromotionRule.class,
                CastlingRights.class,
                DrawCounters.class,
                PositionKey.class,
                GameSnapshot.class,
                IGameQueries.class);

        for (Class<?> reader : readers) {
            assertFalse(takesACommand(reader), reader.getSimpleName() + " must not take an ICommand");
        }
    }

    @Test
    void theRuleContextExposesTheLastMoveAsReadOnlyAndOptional() {
        RecordComponent lastMove = Arrays.stream(MoveRuleContext.class.getRecordComponents())
                .filter(component -> component.getName().equals("lastMove"))
                .findFirst()
                .orElseThrow();

        assertEquals("java.util.Optional<com.chess.domain.command.IMoveInfo>", lastMove.getGenericType().getTypeName());
    }

    @Test
    void theTimelineExposesReadOnlyViewsOfTheMoves() throws NoSuchMethodException {
        Method lastMove = IGameTimeline.class.getMethod("lastMove");
        Method moves = IGameTimeline.class.getMethod("moves");
        String optionalOfMoveInfo = "java.util.Optional<com.chess.domain.command.IMoveInfo>";
        String listOfMoveInfo = "java.util.List<com.chess.domain.command.IMoveInfo>";

        assertEquals(optionalOfMoveInfo, lastMove.getGenericReturnType().getTypeName());
        assertEquals(listOfMoveInfo, moves.getGenericReturnType().getTypeName());
    }

    @Test
    void theTimelineHasNoLastMoveBeforeTheFirstMove() {
        Board board = TestGames.board("....k...", "........", "........", "........",
                "........", "........", "........", "....K...");
        ITurnStateFactory states = new TurnStateFactory(new StubMoveGenerator(List.of()));
        GameSnapshot start = GameSnapshot.initial(board, states.forColor(Color.WHITE), PieceRoles.standard());
        GameTimeline timeline = new GameTimeline(start);

        assertEquals(Optional.empty(), timeline.lastMove());
        assertTrue(timeline.moves().isEmpty());
    }

    @Test
    void aPawnDoubleStepSkipsTheEnPassantSquare() {
        Board board = TestGames.board(
                "....k...",
                "........",
                "........",
                "........",
                "........",
                "........",
                "P.......",
                "....K...");

        PieceRoles roles = PieceRoles.standard();

        assertEquals(Optional.of(at("a3")), new MoveCommand(board, at("a2"), at("a4")).enPassantSquare(roles));
        assertEquals(Optional.empty(), new MoveCommand(board, at("a2"), at("a3")).enPassantSquare(roles));
        assertEquals(Optional.empty(), new MoveCommand(board, at("e1"), at("e2")).enPassantSquare(roles));
    }

    private static boolean takesACommand(Class<?> type) {
        for (Method method : type.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers()) && mentionsCommand(method.getGenericParameterTypes())) {
                return true;
            }
        }
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            if (Modifier.isPublic(constructor.getModifiers())
                    && mentionsCommand(constructor.getGenericParameterTypes())) {
                return true;
            }
        }
        return false;
    }

    private static boolean mentionsCommand(Type[] types) {
        return Arrays.stream(types).anyMatch(type -> type.getTypeName().contains(COMMAND));
    }

    private static Set<String> methodNames(Class<?> type) {
        return Arrays.stream(type.getMethods()).map(Method::getName).collect(Collectors.toSet());
    }

    private static Set<String> declaredMethodNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods()).map(Method::getName).collect(Collectors.toSet());
    }
}