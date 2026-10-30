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

import com.chess.domain.model.Board;
import com.chess.domain.rule.CastlingRights;
import com.chess.domain.rule.CastlingRule;
import com.chess.domain.draw.DrawCounters;
import com.chess.domain.rule.EnPassantRule;
import com.chess.domain.game.GameSnapshot;
import com.chess.domain.command.ICommand;
import com.chess.domain.port.IGameQueries;
import com.chess.domain.game.IMoveHistory;
import com.chess.domain.command.IMoveInfo;
import com.chess.domain.rule.IMoveRule;
import com.chess.domain.command.MoveCommand;
import com.chess.domain.game.MoveHistory;
import com.chess.domain.rule.MoveRuleContext;
import com.chess.domain.draw.PositionKey;
import com.chess.domain.rule.PromotionRule;

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
    void theHistoryExposesReadOnlyViewsOfTheMoves() throws NoSuchMethodException {
        Method lastMove = IMoveHistory.class.getMethod("lastMove");
        Method moves = IMoveHistory.class.getMethod("moves");

        assertEquals("java.util.Optional<com.chess.domain.command.IMoveInfo>", lastMove.getGenericReturnType().getTypeName());
        assertEquals("java.util.List<com.chess.domain.command.IMoveInfo>", moves.getGenericReturnType().getTypeName());
    }

    @Test
    void theHistoryHasNoLastMoveBeforeTheFirstMove() {
        MoveHistory history = new MoveHistory();

        assertEquals(Optional.empty(), history.lastMove());
        assertTrue(history.moves().isEmpty());
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

        assertEquals(Optional.of(at("a3")), new MoveCommand(board, at("a2"), at("a4")).enPassantSquare());
        assertEquals(Optional.empty(), new MoveCommand(board, at("a2"), at("a3")).enPassantSquare());
        assertEquals(Optional.empty(), new MoveCommand(board, at("e1"), at("e2")).enPassantSquare());
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