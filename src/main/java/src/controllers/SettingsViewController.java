package src.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import src.handlers.SoundHandler;

public class SettingsViewController extends FXMLControllerTemplate {

    @FXML
    private Slider soundEffectSlider;

    @FXML
    public void onSoundEffectSliderChange() {
        SoundHandler.setSoundEffectVolume(soundEffectSlider.getValue());
    }
}
