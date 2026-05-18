import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.SepiaTone;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.FileInputStream;
import java.util.Properties;

public class Main extends Application {

    private ImageView imageView;
    private StateController stateController;
    private double zoomLevel = 1.0;
    private double currentRotation = 0.0;
    
    private ColorAdjust colorAdjust;
    private GaussianBlur gaussianBlur;
    private SepiaTone sepiaTone;
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Image Editor Pro - Final Assignment v2.0");

        BorderPane mainPanel = new BorderPane();
        mainPanel.setId("mainPanel");

        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        stateController = new StateController(imageView);
        
        initializeEffects();
        
        mainPanel.setLeft(createLeftMenu(primaryStage));
        mainPanel.setCenter(createMainContent());

        Scene scene = new Scene(mainPanel, 1200, 800);
        try {
            java.net.URL cssUrl = getClass().getResource("style.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
        } catch (Exception ex) {
            System.err.println("CSS Error: " + ex.getMessage());
        }
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeEffects() {
        colorAdjust = new ColorAdjust();
        gaussianBlur = new GaussianBlur(0);
        sepiaTone = new SepiaTone(0);
        
        sepiaTone.setInput(gaussianBlur);
        colorAdjust.setInput(sepiaTone);
        imageView.setEffect(colorAdjust);
    }

    private ScrollPane createLeftMenu(Stage primaryStage) {
        VBox leftMenu = new VBox(15);
        leftMenu.setId("leftMenu");
        leftMenu.setPadding(new Insets(20));
        leftMenu.setPrefWidth(300);
        leftMenu.setAlignment(Pos.TOP_CENTER);

        Button btnOpen = createStyledButton("Open Image", "btn-open");
        Button btnSave = createStyledButton("Save Image", "btn-save");
        Button btnUndo = createStyledButton("Undo", "btn-undo");
        Button btnRedo = createStyledButton("Redo", "btn-redo");
        HBox historyBox = new HBox(10, btnUndo, btnRedo);
        historyBox.setAlignment(Pos.CENTER);
        
        btnOpen.setOnAction(e -> stateController.loadImage(primaryStage));
        btnSave.setOnAction(e -> stateController.saveImage(primaryStage));
        btnUndo.setOnAction(e -> stateController.undo());
        btnRedo.setOnAction(e -> stateController.redo());

        Label lblZoom = createStyledLabel("Zoom Level:");
        Slider sliderZoom = new Slider(0.1, 3.0, 1.0);
        sliderZoom.valueProperty().addListener((obs, oldV, newV) -> {
            zoomLevel = newV.doubleValue();
            imageView.setScaleX(zoomLevel * Math.signum(imageView.getScaleX()));
            imageView.setScaleY(zoomLevel * Math.signum(imageView.getScaleY()));
        });

        Button btnFlip = createStyledButton("Flip Horizontal", "btn-flip");
        Button btnRotate = createStyledButton("Rotate 90°", "btn-rotate");
        HBox geometryBox = new HBox(10, btnFlip, btnRotate);
        geometryBox.setAlignment(Pos.CENTER);
        
        btnFlip.setOnAction(e -> applyFilterAndSave(() -> ImageFilters.applyHorizontalFlip(stateController.getCurrentImage())));
        btnRotate.setOnAction(e -> {
            currentRotation += 90;
            imageView.setRotate(currentRotation);
        });

        Label lblBrightness = createStyledLabel("Brightness:");
        Slider sliderBrightness = new Slider(-1, 1, 0);
        sliderBrightness.valueProperty().addListener((obs, oldV, newV) -> colorAdjust.setBrightness(newV.doubleValue()));
        
        Label lblContrast = createStyledLabel("Contrast:");
        Slider sliderContrast = new Slider(-1, 1, 0);
        sliderContrast.valueProperty().addListener((obs, oldV, newV) -> colorAdjust.setContrast(newV.doubleValue()));
        
        Label lblBlur = createStyledLabel("Blur (Radius):");
        Slider sliderBlur = new Slider(0, 20, 0);
        sliderBlur.valueProperty().addListener((obs, oldV, newV) -> gaussianBlur.setRadius(newV.doubleValue()));

        Button btnBW = createStyledButton("Black & White", "btn-bw");
        Button btnSepia = createStyledButton("Sepia Filter", "btn-sepia");
        Button btnNegative = createStyledButton("Negative", "btn-negative");
        
        btnBW.setOnAction(e -> { colorAdjust.setSaturation(-1.0); sepiaTone.setLevel(0); });
        btnSepia.setOnAction(e -> { sepiaTone.setLevel(1.0); colorAdjust.setSaturation(0); });
        btnNegative.setOnAction(e -> applyFilterAndSave(() -> ImageFilters.applyNegative(stateController.getCurrentImage())));

        Label lblAdvanced = createStyledLabel("Advanced Algorithms:");
        lblAdvanced.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffaa00;");
        Button btnHistogram = createStyledButton("Histogram Equalization", "btn-advanced");
        Button btnMedian = createStyledButton("Median Filter (Noise)", "btn-advanced");
        Button btnSobel = createStyledButton("Sobel Edge Detection", "btn-advanced");
        Button btnEdge = createStyledButton("Laplace Edge", "btn-edge");
        Button btnSharpen = createStyledButton("Unsharp Masking", "btn-sharpen");
        Button btnUpscale = createStyledButton("2x Upscale (Pixel Art)", "btn-advanced");
        
        btnHistogram.setOnAction(e -> applyFilterAndSave(() -> ImageFilters.applyHistogramEqualization(stateController.getCurrentImage())));
        btnMedian.setOnAction(e -> applyFilterAndSave(() -> ImageFilters.applyMedianFilter(stateController.getCurrentImage())));
        btnSobel.setOnAction(e -> applyFilterAndSave(() -> ImageFilters.applySobelEdgeDetection(stateController.getCurrentImage())));
        btnEdge.setOnAction(e -> applyFilterAndSave(() -> ImageFilters.applyConvolution(stateController.getCurrentImage(), ImageFilters.getLaplacianMatrix())));
        btnSharpen.setOnAction(e -> applyFilterAndSave(() -> ImageFilters.applyConvolution(stateController.getCurrentImage(), ImageFilters.getUnsharpMaskingMatrix())));
        btnUpscale.setOnAction(e -> applyFilterAndSave(() -> ImageFilters.applyUpscale(stateController.getCurrentImage())));

        Label lblAi = createStyledLabel("Cloud AI Features:");
        lblAi.setStyle("-fx-font-weight: bold; -fx-text-fill: #00ffaa;");
        Button btnRemoveBg = createStyledButton("Remove.bg (API)", "btn-ai");
        Button btnSketch = createStyledButton("Sketch (Local AI)", "btn-ai");
        
        btnRemoveBg.setOnAction(e -> {
            String key = getApiKey();
            if (key == null || key.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "API Error", "Hiányzó API Kulcs. Kérlek állítsd be a .env fájlban: REMOVE_BG_API_KEY=");
                return;
            }
            executeAiTask(() -> AIClient.removeBackground(stateController.getCurrentImage(), key), btnRemoveBg);
        });
        btnSketch.setOnAction(e -> applyFilterAndSave(() -> ImageFilters.applySketchFilter(stateController.getCurrentImage())));

        Button btnReset = createStyledButton("Reset All", "btn-reset");
        btnReset.setOnAction(e -> {
            sliderBrightness.setValue(0); sliderContrast.setValue(0);
            sliderBlur.setValue(0); sliderZoom.setValue(1.0);
            colorAdjust.setBrightness(0); colorAdjust.setContrast(0); colorAdjust.setSaturation(0);
            sepiaTone.setLevel(0); gaussianBlur.setRadius(0);
            imageView.setRotate(0); currentRotation = 0;
            imageView.setScaleX(1); imageView.setScaleY(1);
            stateController.reset();
        });

        leftMenu.getChildren().addAll(
            btnOpen, btnSave, historyBox, new Separator(),
            lblZoom, sliderZoom, geometryBox, new Separator(),
            lblBrightness, sliderBrightness, lblContrast, sliderContrast, lblBlur, sliderBlur, new Separator(),
            btnBW, btnSepia, btnNegative, new Separator(),
            lblAdvanced, btnHistogram, btnMedian, btnSobel, btnEdge, btnSharpen, btnUpscale, new Separator(),
            lblAi, btnRemoveBg, btnSketch, new Separator(),
            btnReset
        );

        ScrollPane leftScroll = new ScrollPane(leftMenu);
        leftScroll.setFitToWidth(true);
        leftScroll.setStyle("-fx-background-color: transparent;");
        return leftScroll;
    }

    private ScrollPane createMainContent() {
        StackPane imageContainer = new StackPane(imageView);
        imageContainer.setAlignment(Pos.CENTER);
        ScrollPane scrollPane = new ScrollPane(imageContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);
        return scrollPane;
    }

    private String getApiKey() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(".env"));
            return props.getProperty("REMOVE_BG_API_KEY");
        } catch (Exception ex) {
            return System.getenv("REMOVE_BG_API_KEY");
        }
    }

    private Button createStyledButton(String text, String cssClass) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().addAll("control-button", cssClass);
        return btn;
    }

    private Label createStyledLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("control-label");
        return lbl;
    }

    private void applyFilterAndSave(java.util.function.Supplier<Image> filterLogic) {
        if (stateController.getCurrentImage() != null) {
            stateController.saveStateToUndo();
            Image result = filterLogic.get();
            if (result != null) {
                stateController.setCurrentImage(result);
            }
        }
    }

    private void executeAiTask(java.util.concurrent.Callable<Image> aiLogic, Button buttonSource) {
        if (stateController.getCurrentImage() == null) return;
        
        String originalText = buttonSource.getText();
        buttonSource.setText("Processing... (Wait)");
        buttonSource.setDisable(true);

        new Thread(() -> {
            try {
                Image result = aiLogic.call();
                Platform.runLater(() -> {
                    stateController.saveStateToUndo();
                    stateController.setCurrentImage(result);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "AI Image processing completed!");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "API Error", ex.getMessage()));
            } finally {
                Platform.runLater(() -> {
                    buttonSource.setText(originalText);
                    buttonSource.setDisable(false);
                });
            }
        }).start();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
