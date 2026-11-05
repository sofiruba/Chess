# Sound Effect System

## Overview

This feature adds 16 sound effects to Chess, providing audio feedback for game events and user interface interactions.

## Sound Effects

### Game Events

| Sound: Event |
|---|---|
| 1. box.wav: Opening a mystery box |
| 2. capture.wav: Capturing an opponent's piece |
| 3. check.wav: Putting the opponent's king in check |
| 4. finish.wav: Game ending (checkmate or draw) |
| 5. move.wav: Normal piece movement |
| 6. power.wav: Using a special power |
| 7. promotion.wav: Pawn promotion |
| 8. undo.wav: Undoing a move |
| 9. redo.wav: Redoing a previously undone move |

### User Interface

| Sound: Event |
|---|---|
| error.wav: Invalid selection or action |
| home.wav: Returning to the home screen |
| menu.wav: Menu button interactions |
| navigate.wav: Navigating history, tabs, or saved games |
| rules.wav: Opening the rules |
| save.wav: Successfully saving a game |
| select.wav: Selecting a piece |

## Architecture

The audio system follows object-oriented design principles and uses the Observer pattern.

- **SoundEffect:** Enum defining the available sound effects.
- **ISoundPlayer:** Interface defining audio playback operations.
- **ClipSoundPlayer:** Concrete implementation using Java Sound API.
- **GameSoundListener:** Implements IGameListener to observe game events and trigger the corresponding sound FX.
- **ChessApplication:** Integrates audio with the main interface.
- **SidePanel:** Handles audio feedback for side panel interactions.

### Design Principles

- **Observer Pattern:** GameSoundListener reacts to game events without modifying game logic.
- **Dependency Inversion Principle:** Components depend on the ISoundPlayer interface rather than its concrete implementation.
- **Dependency Injection:** The ISoundPlayer instance is passed to SidePanel through its constructor.
- **Separation of Concerns:** Audio playback remains separate from chess rules and game logic.

## Sound Priority

When multiple events occur simultaneously, only the highest priority sound is played.

Said priority order works as follows:

FINISH > CHECK > POWER > BOX > PROMOTION > CAPTURE > MOVE

## Audio Control

Users can enable or disable all sound effects using the SONIDO: SI / SONIDO: NO button.

Audio clips are loaded on demand and cached for reuse.
Resources are released when the application closes.

## Testing

Two automated test classes verify the audio system:

1. **GameSoundListenerTest:** Uses a test double to verify sound selection without actual audio playback.
2. **SoundEffectResourcesTest**: Checks that all WAV files are present and contain readable audio data.

Run all tests:

```bash
mvn test
```
Run the application:

```bash
mvn clean javafx:run
```

## Resources

Audio files are located in:

`src/main/resources/com/chess/ui/assets/sounds/`

The implementation uses Java's built-in audio API and does not require additional Maven dependencies.
