package com.chess;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedInputStream;
import javax.sound.sampled.AudioSystem;

import org.junit.jupiter.api.Test;

import com.chess.ui.SoundEffect;

/**
Verifies that all effects have valid WAV resources.
*/
class SoundEffectResourcesTest {
    @Test
    void allSoundsAreBundledAndDecodable() {
        for (SoundEffect effect : SoundEffect.values()) {
            assertDoesNotThrow(() -> {
                var resource = getClass().getResourceAsStream(
                    effect.resourcePath()
                );

                assertNotNull(
                    resource,
                    "Missing resource: " + effect.resourcePath()
                );

                try (var input = new BufferedInputStream(resource);
                     var audio = AudioSystem.getAudioInputStream(input)) {

                    assertTrue(
                        audio.getFrameLength() > 0,
                        "Empty sound: " + effect
                    );

                    assertTrue(
                        audio.getFormat().getSampleRate() > 0,
                        "Invalid audio: " + effect
                    );
                }
            }, "Could not read " + effect);
        }
    }
}
