package src.handlers;

import java.net.URISyntaxException;

import org.tinylog.Logger;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import src.shared.SoundFiles;

public final class SoundHandler {

    private static String FILE_PATH;
    private static final double DEFAULT_VOLUME_LEVEL = 0.75;

    // Default sound effect
    private static Media soundEffect;

    // Used to play audio for sound effects
    private static MediaPlayer soundEffectPlayer;

    private SoundHandler() {
    }

    public static void initialize() {
        try {
            FILE_PATH = SoundHandler.class.getResource("/sounds/").toURI().toString();

            // Sets the default value of the soundEffect Media (breaks if set to null)
            soundEffect = new Media(FILE_PATH + SoundFiles.EXAMPLE_SOUND_FILE);
            // Associates the Media with the MediaPlayer
            soundEffectPlayer = new MediaPlayer(soundEffect);

        } catch (URISyntaxException _) {
            Logger.error("Could not initialize the FILE_PATH variable.");
        }

        soundEffectPlayer.setVolume(DEFAULT_VOLUME_LEVEL);
    }

    /**
     * Plays a specified audio file. The file must already be in the application.
     * 
     * @param soundFilename - The audio filename, defined in an Enum
     */
    public static void playSound(SoundFiles soundFilename) {

        // Update the Media in the MediaPlayer to the specified audio File
        soundEffect = new Media(FILE_PATH + soundFilename.toString());
        soundEffectPlayer.play();

        // Resets the MediaPlayer so it can play another sound later after it finishes
        // playing the current sound
        soundEffectPlayer.setOnEndOfMedia(() -> soundEffectPlayer.stop());
    }

    // TODO: Implement method that interrupts any BGM playing (vs. playing a sound
    // effect on top of it)
    // Will need multiple MediaPlayers, each responsible for a specific type of
    // sound (e.g., effects, music, etc.)
    public static void playSoundBGMInterrupt() {

    }

    public static void setSoundEffectVolume(double newVolumeLevel) {
        Logger.info("Setting sound effect volume to {}", newVolumeLevel);
        soundEffectPlayer.setVolume(newVolumeLevel);
    }
}
