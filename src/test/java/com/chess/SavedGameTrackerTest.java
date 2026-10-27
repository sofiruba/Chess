package com.chess;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.chess.ui.SavedGameTracker;

final class SavedGameTrackerTest {

    @Test
    void anEmptyGameCannotBeSavedWhenNothingWasSavedBefore() {
        SavedGameTracker tracker = new SavedGameTracker();

        boolean canSave = tracker.canSave(List.of());

        assertFalse(canSave);
    }

    @Test
    void anEmptyGameCannotBeSavedAfterAnotherGameWasSaved() {
        SavedGameTracker tracker = new SavedGameTracker();
        tracker.markSaved(List.of("e2e4"));

        boolean canSave = tracker.canSave(List.of());

        assertFalse(canSave);
    }

    @Test
    void anEmptyGameCannotBeSavedEvenIfAnEmptyGameWasMarkedAsSaved() {
        SavedGameTracker tracker = new SavedGameTracker();
        tracker.markSaved(List.of());

        boolean canSave = tracker.canSave(List.of());

        assertFalse(canSave);
    }

    @Test
    void aGameWithMovesThatWasNeverSavedCanBeSaved() {
        SavedGameTracker tracker = new SavedGameTracker();

        boolean canSave = tracker.canSave(List.of("e2e4"));

        assertTrue(canSave);
    }

    @Test
    void aGameAlreadySavedCannotBeSavedAgain() {
        SavedGameTracker tracker = new SavedGameTracker();
        tracker.markSaved(List.of("e2e4", "e7e5"));

        boolean canSave = tracker.canSave(List.of("e2e4", "e7e5"));

        assertFalse(canSave);
    }

    @Test
    void aGameWithMoreMovesThanTheSavedOneCanBeSaved() {
        SavedGameTracker tracker = new SavedGameTracker();
        tracker.markSaved(List.of("e2e4"));

        boolean canSave = tracker.canSave(List.of("e2e4", "e7e5"));

        assertTrue(canSave);
    }

    @Test
    void aGameWithFewerMovesThanTheSavedOneCanBeSaved() {
        SavedGameTracker tracker = new SavedGameTracker();
        tracker.markSaved(List.of("e2e4", "e7e5"));

        boolean canSave = tracker.canSave(List.of("e2e4"));

        assertTrue(canSave);
    }

    @Test
    void aGameWithTheSameMovesInAnotherOrderCanBeSaved() {
        SavedGameTracker tracker = new SavedGameTracker();
        tracker.markSaved(List.of("e2e4", "e7e5"));

        boolean canSave = tracker.canSave(List.of("e7e5", "e2e4"));

        assertTrue(canSave);
    }

    @Test
    void goingBackToTheSavedMovesAfterAnUndoCannotBeSavedAgain() {
        SavedGameTracker tracker = new SavedGameTracker();
        tracker.markSaved(List.of("e2e4", "e7e5"));
        boolean afterUndo = tracker.canSave(List.of("e2e4"));

        boolean afterRedo = tracker.canSave(List.of("e2e4", "e7e5"));

        assertTrue(afterUndo);
        assertFalse(afterRedo);
    }

    @Test
    void markingANewGameReplacesThePreviousOne() {
        SavedGameTracker tracker = new SavedGameTracker();
        tracker.markSaved(List.of("e2e4"));
        tracker.markSaved(List.of("d2d4"));

        boolean previous = tracker.canSave(List.of("e2e4"));
        boolean latest = tracker.canSave(List.of("d2d4"));

        assertTrue(previous);
        assertFalse(latest);
    }

    @Test
    void changingTheListAfterMarkingItDoesNotChangeWhatWasSaved() {
        SavedGameTracker tracker = new SavedGameTracker();
        List<String> moves = new ArrayList<>(List.of("e2e4"));
        tracker.markSaved(moves);
        moves.add("e7e5");

        boolean originalMoves = tracker.canSave(List.of("e2e4"));
        boolean changedMoves = tracker.canSave(moves);

        assertFalse(originalMoves);
        assertTrue(changedMoves);
    }
}