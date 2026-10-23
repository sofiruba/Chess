package com.chess;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.chess.ui.SavedGameTracker;

final class SavedGameTrackerTest {

    @Test
    void whenNothingWasSavedNoGameIsConsideredSaved() {
        SavedGameTracker tracker = new SavedGameTracker();

        boolean withMoves = tracker.isSaved(List.of("e2e4"));
        boolean withoutMoves = tracker.isSaved(List.of());

        assertFalse(withMoves);
        assertFalse(withoutMoves);
    }

    @Test
    void aGameWithTheSameMovesAsTheSavedOneIsSaved() {
        SavedGameTracker tracker = new SavedGameTracker();
        tracker.markSaved(List.of("e2e4", "e7e5"));

        boolean saved = tracker.isSaved(List.of("e2e4", "e7e5"));

        assertTrue(saved);
    }

    @Test
    void aGameWithMoreMovesThanTheSavedOneIsNotSaved() {
        SavedGameTracker tracker = new SavedGameTracker();
        tracker.markSaved(List.of("e2e4"));

        boolean saved = tracker.isSaved(List.of("e2e4", "e7e5"));

        assertFalse(saved);
    }

    @Test
    void aGameWithFewerMovesThanTheSavedOneIsNotSaved() {
        SavedGameTracker tracker = new SavedGameTracker();
        tracker.markSaved(List.of("e2e4", "e7e5"));

        boolean saved = tracker.isSaved(List.of("e2e4"));

        assertFalse(saved);
    }

    @Test
    void aGameWithTheSameMovesInAnotherOrderIsNotSaved() {
        SavedGameTracker tracker = new SavedGameTracker();
        tracker.markSaved(List.of("e2e4", "e7e5"));

        boolean saved = tracker.isSaved(List.of("e7e5", "e2e4"));

        assertFalse(saved);
    }

    @Test
    void goingBackToTheSavedMovesAfterAnUndoIsSavedAgain() {
        SavedGameTracker tracker = new SavedGameTracker();
        tracker.markSaved(List.of("e2e4", "e7e5"));
        boolean afterUndo = tracker.isSaved(List.of("e2e4"));

        boolean afterRedo = tracker.isSaved(List.of("e2e4", "e7e5"));

        assertFalse(afterUndo);
        assertTrue(afterRedo);
    }

    @Test
    void anEmptyGameThatWasSavedIsRecognizedAsSaved() {
        SavedGameTracker tracker = new SavedGameTracker();
        tracker.markSaved(List.of());

        boolean saved = tracker.isSaved(List.of());

        assertTrue(saved);
    }

    @Test
    void markingANewGameReplacesThePreviousOne() {
        SavedGameTracker tracker = new SavedGameTracker();
        tracker.markSaved(List.of("e2e4"));
        tracker.markSaved(List.of("d2d4"));

        boolean previous = tracker.isSaved(List.of("e2e4"));
        boolean latest = tracker.isSaved(List.of("d2d4"));

        assertFalse(previous);
        assertTrue(latest);
    }

    @Test
    void changingTheListAfterMarkingItDoesNotChangeWhatWasSaved() {
        SavedGameTracker tracker = new SavedGameTracker();
        List<String> moves = new ArrayList<>(List.of("e2e4"));
        tracker.markSaved(moves);
        moves.add("e7e5");

        boolean savedWithOriginalMoves = tracker.isSaved(List.of("e2e4"));
        boolean savedWithChangedMoves = tracker.isSaved(moves);

        assertTrue(savedWithOriginalMoves);
        assertFalse(savedWithChangedMoves);
    }
}