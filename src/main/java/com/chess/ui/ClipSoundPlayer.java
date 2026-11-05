package com.chess.ui;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
Player for short WAV clips, with no external audio dependencies.
Clips are loaded the first time they are used; if there is no audio output, the game continues to function normally.
*/
public final class ClipSoundPlayer implements ISoundPlayer{
    
    private final Map<SoundEffect, Clip> clips = new EnumMap<>(SoundEffect.class);
    private boolean enabled = true;
    private boolean unavailable;

    @Override
    public void play(SoundEffect effect) {
        Objects.requireNonNull(effect, "effect must not be null");
        if (!enabled || unavailable) {
            return;
        }
        try {
            Clip clip = clips.get(effect);
            if (clip == null) {
                clip = loadClip(effect);
                clips.put(effect, clip);
            }
            // Allows listening to consecutive moves even if the previous clip hasn't finished.
            clip.stop();
            clip.setFramePosition(0);
            clip.start();
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException
                | IllegalArgumentException | SecurityException exception) {
            // El audio es opcional: ausencia de dispositivo o un WAV inválido no deben romper la partida.
            unavailable = true;
            close();
            System.err.println("Audio deshabilitado: " + exception.getMessage());
        }
    }

    private Clip loadClip(SoundEffect effect)
            throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        var resource = getClass().getResourceAsStream(effect.resourcePath());
        if (resource == null) {
            throw new IOException("Falta el sonido " + effect.resourcePath());
        }
        try (var buffered = new BufferedInputStream(resource);
                AudioInputStream stream = AudioSystem.getAudioInputStream(buffered)) {
            Clip clip = AudioSystem.getClip();
            try {
                clip.open(stream);
                return clip;
            } catch (IOException | LineUnavailableException | RuntimeException exception) {
                clip.close();
                throw exception;
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            clips.values().forEach(Clip::stop);
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void close() {
        clips.values().forEach(Clip::close);
        clips.clear();
    }

}
