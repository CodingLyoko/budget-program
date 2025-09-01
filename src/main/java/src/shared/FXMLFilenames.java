package src.shared;

public enum FXMLFilenames {
    EXAMPLE_FXML_FILE("example_fxml_file");
    

    private final String fxmlFilename;

    private FXMLFilenames(String fxmlFilename) {
        this.fxmlFilename = fxmlFilename;
    }

    @Override
    public String toString() {
        return fxmlFilename;
    }
}
