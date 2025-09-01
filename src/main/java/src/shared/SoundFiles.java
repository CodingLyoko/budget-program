package src.shared;

public enum SoundFiles {
    EXAMPLE_SOUND_FILE("example_sound_file.mp3");

    private final String soundFile;

    private SoundFiles(String soundFile) {
        this.soundFile = soundFile;
    }

    @Override
    public String toString() {
        return soundFile;
    }
}
