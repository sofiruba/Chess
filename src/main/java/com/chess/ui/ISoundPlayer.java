package com.chess.ui;

/** Contract for playing back effects without coupling listeners to the audio device. */
public interface ISoundPlayer extends AutoCloseable {
    void play(SoundEffect effect);
    void setEnabled(boolean enabled);
    boolean isEnabled();

    @Override
    void close();
}
