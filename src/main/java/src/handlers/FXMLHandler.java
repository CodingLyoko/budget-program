package src.handlers;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import lombok.Getter;
import lombok.Setter;
import src.shared.FXMLFilenames;

@Setter
@Getter
public class FXMLHandler {

    @Getter
    private static Map<FXMLFilenames, FXMLInstance> fxmlInstances = new EnumMap<>(FXMLFilenames.class);

    @Getter
    private static final FXMLFilenames[] FXML_FILENAMES = FXMLFilenames.values();

    private FXMLHandler() {
    }

    public static Parent getFXMLRoot(FXMLFilenames fxmlFilename) {
        return fxmlInstances.get(fxmlFilename).root;
    }

    public static Object getFxmlController(FXMLFilenames fxmlFilename) {
        return fxmlInstances.get(fxmlFilename).loader.getController();
    }

    public static class FXMLInstance {

        protected Parent root;
        protected FXMLLoader loader;

        public FXMLInstance(String fxmlFilename) throws IOException {
            this.loader = new FXMLLoader(FXMLHandler.class.getResource("/fxml/" + fxmlFilename + ".fxml"));
            this.root = loader.load();
        }
    }
}
