package com.chess.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.chess.domain.model.Position;
import com.chess.domain.port.IGameListener;
import com.chess.domain.port.IGameQueries;

/**
Adapts the events of the game to sounds. Reads only the public ports of the domain: the rules, commands, and 
saved games do not know about the audio system.
*/
public final class GameSoundListener implements IGameListener {
    private final IGameQueries game;
    private final ISoundPlayer sounds;
    private int capturedBefore;
    private Set<Position> boxesBefore;

    public GameSoundListener(IGameQueries game, ISoundPlayer sounds) {
        this.game = Objects.requireNonNull(game, "game must not be null");
        this.sounds = Objects.requireNonNull(sounds, "sounds must not be null");
        rememberState();
    }

    @Override
    public void moveMade() {
        playMoveResult(false);
    }

    @Override
    public void powerUsed() {
        playMoveResult(true);
    }

    @Override
    public void moveUndone() {
        rememberState();
        sounds.play(SoundEffect.UNDO);
    }

    @Override
    public void moveRedone() {
        rememberState();
        sounds.play(game.isFinished() ? SoundEffect.FINISH : SoundEffect.REDO);
    }

    // gameFinished is announced AFTER moveMade/powerUsed/moveRedone;
    // the final result is evaluated here once for each move, to avoid playing two sounds.
    private void playMoveResult(boolean isPower) {
        int capturedNow = game.capturedPieces().size();
        Set<Position> boxesNow = new HashSet<>(game.mysteryBoxes());
        boolean openedBox = boxesBefore.stream().anyMatch(box -> !boxesNow.contains(box));
        boolean captured = capturedNow > capturedBefore;
        List<String> codes = game.moveCodes();
        boolean promotion = !isPower && !codes.isEmpty() && codes.get(codes.size() - 1).length() > 4;

        SoundEffect effect;
        if (game.isFinished()) {
            effect = SoundEffect.FINISH;
        } else if (game.isInCheck(game.turn())) {
            effect = SoundEffect.CHECK;
        } else if (isPower) {
            effect = SoundEffect.POWER;
        } else if (openedBox) {
            effect = SoundEffect.BOX;
        } else if (promotion) {
            effect = SoundEffect.PROMOTION;
        } else if (captured) {
            effect = SoundEffect.CAPTURE;
        } else {
            effect = SoundEffect.MOVE;
        }
        rememberState();
        sounds.play(effect);
    }

    private void rememberState() {
        capturedBefore = game.capturedPieces().size();
        boxesBefore = new HashSet<>(game.mysteryBoxes());
    }
}
