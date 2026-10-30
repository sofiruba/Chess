package com.chess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.chess.domain.port.IGameActions;
import com.chess.domain.port.IGameQueries;
import com.chess.domain.port.IGameService;

final class GameInterfacesTest {

    @Test
    void queriesCannotChangeTheGame() {
        Set<String> names = methodNames(IGameQueries.class);

        assertFalse(names.contains("playMove"));
        assertFalse(names.contains("playMoveCode"));
        assertFalse(names.contains("undoLastMove"));
        assertFalse(names.contains("redoLastMove"));
    }

    @Test
    void actionsAreOnlyTheOnesThatChangeTheGame() {
        assertEquals(
                Set.of("playMove", "playMoveCode", "undoLastMove", "redoLastMove"),
                methodNames(IGameActions.class));
    }

    @Test
    void theServiceIsTheUnionOfBoth() {
        assertTrue(IGameQueries.class.isAssignableFrom(IGameService.class));
        assertTrue(IGameActions.class.isAssignableFrom(IGameService.class));
        assertEquals(0, IGameService.class.getDeclaredMethods().length);
        assertTrue(IGameQueries.class.isInstance(TestGames.standard()));
        assertTrue(IGameActions.class.isInstance(TestGames.standard()));
    }

    @Test
    void theBoardViewAsksOnlyForQueries() throws ClassNotFoundException {
        assertTakesQueriesNotActions("com.chess.ui.BoardView");
    }

    @Test
    void theSidePanelAsksOnlyForQueries() throws ClassNotFoundException {
        assertTakesQueriesNotActions("com.chess.ui.SidePanel");
    }

    private static void assertTakesQueriesNotActions(String className) throws ClassNotFoundException {
        Class<?> type = Class.forName(className, false, GameInterfacesTest.class.getClassLoader());
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            List<Class<?>> parameters = Arrays.asList(constructor.getParameterTypes());
            assertTrue(parameters.contains(IGameQueries.class), className + " must receive IGameQueries");
            assertFalse(parameters.contains(IGameService.class), className + " must not receive IGameService");
            assertFalse(parameters.contains(IGameActions.class), className + " must not receive IGameActions");
        }
    }

    private static Set<String> methodNames(Class<?> type) {
        return Arrays.stream(type.getMethods()).map(Method::getName).collect(Collectors.toSet());
    }
}