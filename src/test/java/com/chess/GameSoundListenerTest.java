package com.chess;

import static com.chess.TestGames.at;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.chess.domain.model.Color;
import com.chess.domain.model.PieceType;
import com.chess.domain.port.IGameService;
import com.chess.ui.GameSoundListener;
import com.chess.ui.SoundEffect;
import com.chess.ui.ISoundPlayer;

class GameSoundListenerTest {
    
    // Fake reproducer for verifying the sounds without reproducing audio.
    private static final class RecordingSounds implements ISoundPlayer {

        final List<SoundEffect> played = new ArrayList<>();
        private boolean enabled = true;

        @Override
        public void play(SoundEffect effect) {
            if (enabled) {
                played.add(effect);
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override 
        public void close() {
            // Nothing to close.
        }
    }

        @Test 
        void normalMoveUndoAndRedoProduceTheExpectedSounds() {
            IGameService game = TestGames.standard();
            RecordingSounds sounds = new RecordingSounds();
            game.addListener(new GameSoundListener(game, sounds));

            game.playMove(at("e2"), at("e4"));
            game.undoLastMove();
            game.redoLastMove();

            assertEquals(
                List.of(SoundEffect.MOVE, SoundEffect.UNDO, SoundEffect.REDO),
                sounds.played
            );
        }

        @Test
        void capturesAreDistinguishedFromNormalMoves() {
            IGameService game = TestGames.fromDiagram(Color.WHITE,
                "....k...",
                "........",
                "........",
                "........",
                "...p....",
                "...R....",
                "........",
                "....K...");

                RecordingSounds sounds = new RecordingSounds();
                game.addListener(new GameSoundListener(game, sounds));

                game.playMove(at("d3"), at("d4"));

                assertEquals(List.of(SoundEffect.CAPTURE), sounds.played);
        }

        @Test
        void checkGetsAPrioritySound() {
            IGameService game = TestGames.fromDiagram(Color.WHITE,
                "....k...",
                "........",
                "........",
                "........",
                "........",
                "........",
                "........",
                "R......K");
                
                RecordingSounds sounds = new RecordingSounds();
                game.addListener(new GameSoundListener(game, sounds));
                
                game.playMove(at("a1"), at("e1"));
                
                assertEquals(List.of(SoundEffect.CHECK), sounds.played);
        }

        @Test
        void checkmateProducesOnlyOneFinishingSound() {
            IGameService game = TestGames.standard();
            RecordingSounds sounds = new RecordingSounds();
            game.addListener(new GameSoundListener(game, sounds));

            game.playMove(at("f2"), at("f3"));
            game.playMove(at("e7"), at("e5"));
            game.playMove(at("g2"), at("g4"));
            game.playMove(at("d8"), at("h4"));

            assertEquals(
                List.of(
                    SoundEffect.MOVE,
                    SoundEffect.MOVE,
                    SoundEffect.MOVE,
                    SoundEffect.FINISH
                ),
                sounds.played
            );
        }

        @Test
        void openingAMysteryBoxPlaysThePickupSound() {
            IGameService game = TestGames.withBoxes("c5=i", Color.WHITE,
                "....k...",
                ".......p",
                "........",
                "........",
                "........",
                ".N......",
                "........",
                "....K...");

            RecordingSounds sounds = new RecordingSounds();
            game.addListener(new GameSoundListener(game, sounds));

            game.playMove(at("b3"), at("c5"));

            assertEquals(List.of(SoundEffect.BOX), sounds.played);
        }

        @Test
        void usingAPowerPlaysThePowerSoundAndWorksAfterRedo() {
            IGameService game = TestGames.withBoxes("a1=t", Color.WHITE,
                "....k...",
                "........",
                "p.......",
                "........",
                "........",
                "........",
                "........",
                "R...K...");

            RecordingSounds sounds = new RecordingSounds();
            game.addListener(new GameSoundListener(game, sounds));

            game.usePower(at("a1"), at("a6"));
            game.undoLastMove();
            game.redoLastMove();

            assertEquals(
                List.of(
                    SoundEffect.POWER,
                    SoundEffect.UNDO,
                    SoundEffect.REDO
                ),
                sounds.played
            );
        }

        @Test
        void promotionUsesItsOwnSound() {
            IGameService game = TestGames.fromDiagram(Color.WHITE,
                "........",
                "P.......",
                ".......k",
                "........",
                "........",
                "........",
                "........",
                "....K...");

            RecordingSounds sounds = new RecordingSounds();
            game.addListener(new GameSoundListener(game, sounds));

            game.playMove(at("a7"), at("a8"), PieceType.QUEEN);

            assertEquals(List.of(SoundEffect.PROMOTION), sounds.played);
        }

        @Test
        void mutedPlayerDoesNotProduceSounds() {
            IGameService game = TestGames.standard();
            RecordingSounds sounds = new RecordingSounds();
            game.addListener(new GameSoundListener(game, sounds));

            sounds.setEnabled(false);
            game.playMove(at("e2"), at("e4"));

            assertEquals(List.of(), sounds.played);
        }
    }
