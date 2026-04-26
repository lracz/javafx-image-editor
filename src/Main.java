import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.SepiaTone;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Stack;

public class Main extends Application {

    // Global UI and Image variables
    private ImageView imageView;
    private Image originalImage;
    private Image currentImage;

    // Zoom and Rotate state variables
    private double zoomLevel = 1.0;
    private double currentRotation = 0.0;

    // Undo / Redo Stacks
    private final Stack<Image> undoStack = new Stack<>();
    private final Stack<Image> redoStack = new Stack<>();

    // JavaFX Built-in Effects
    private ColorAdjust colorAdjust;
    private GaussianBlur gaussianBlur;
    private SepiaTone sepiaTone;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Image Editor App - Final Assignment");

        // Main Layout Container
        BorderPane mainPanel = new BorderPane();
        mainPanel.setId("mainPanel");

        // --- LEFT MENU (Controls) ---
        VBox leftMenu = new VBox(15);
        leftMenu.setId("leftMenu");
        leftMenu.setPadding(new Insets(20));
        leftMenu.setPrefWidth(260);
        leftMenu.setAlignment(Pos.TOP_CENTER);

        // File Operations
        Button btnOpen = createStyledButton("Open Image", "btn-open");
        Button btnSave = createStyledButton("Save Image", "btn-save");

        // Undo / Redo Buttons
        Button btnUndo = createStyledButton("Undo", "btn-undo");
        Button btnRedo = createStyledButton("Redo", "btn-redo");
        HBox historyBox = new HBox(10, btnUndo, btnRedo);
        historyBox.setAlignment(Pos.CENTER);

        // Sliders (Brightness, Contrast, Blur, Zoom)
        Label lblBrightness = createStyledLabel("Brightness:");
        Slider sliderBrightness = new Slider(-1, 1, 0);

        Label lblContrast = createStyledLabel("Contrast:");
        Slider sliderContrast = new Slider(-1, 1, 0);

        Label lblBlur = createStyledLabel("Blur (Radius):");
        Slider sliderBlur = new Slider(0, 20, 0);

        Label lblZoom = createStyledLabel("Zoom Level:");
        Slider sliderZoom = new Slider(0.1, 3.0, 1.0);
        sliderZoom.setShowTickMarks(true);

        // Advanced Pixel Effects
        Button btnBW = createStyledButton("Black & White", "btn-bw");
        Button btnSepia = createStyledButton("Sepia Filter", "btn-sepia");
        Button btnNegative = createStyledButton("Negative", "btn-negative");

        // Advanced Filters
        Button btnEdge = createStyledButton("Edge Detect (Laplace)", "btn-edge");
        Button btnSharpen = createStyledButton("Sharpen (Unsharp Mask)", "btn-sharpen");

        // Geometry / Transformations
        Button btnFlip = createStyledButton("Flip Horizontal", "btn-flip");
        Button btnRotate = createStyledButton("Rotate 90°", "btn-rotate");
        HBox geometryBox = new HBox(10, btnFlip, btnRotate);
        geometryBox.setAlignment(Pos.CENTER);

        // RGB Channels
        Label lblChannels = createStyledLabel("Color Channels:");
        HBox channelBox = new HBox(5);
        channelBox.setAlignment(Pos.CENTER);
        Button btnRed = createStyledButton("R", "btn-red");
        Button btnGreen = createStyledButton("G", "btn-green");
        Button btnBlue = createStyledButton("B", "btn-blue");
        HBox.setHgrow(btnRed, Priority.ALWAYS);
        HBox.setHgrow(btnGreen, Priority.ALWAYS);
        HBox.setHgrow(btnBlue, Priority.ALWAYS);
        channelBox.getChildren().addAll(btnRed, btnGreen, btnBlue);

        Button btnReset = createStyledButton("Reset All", "btn-reset");

        // Add all elements to Left Menu
        leftMenu.getChildren().addAll(
                btnOpen, btnSave, historyBox,
                new Separator(),
                lblZoom, sliderZoom,
                geometryBox,
                new Separator(),
                lblBrightness, sliderBrightness,
                lblContrast, sliderContrast,
                lblBlur, sliderBlur,
                new Separator(),
                btnBW, btnSepia, btnNegative,
                btnEdge, btnSharpen,
                lblChannels, channelBox,
                new Separator(),
                btnReset
        );

        // --- CENTER AREA (Image Display) ---
        imageView = new ImageView();
        imageView.setPreserveRatio(true);

        StackPane imageContainer = new StackPane(imageView);
        imageContainer.setId("imageContainer");
        imageContainer.setAlignment(Pos.CENTER);

        ScrollPane scrollPane = new ScrollPane(imageContainer);
        scrollPane.setId("scrollPane");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);

        mainPanel.setLeft(leftMenu);
        mainPanel.setCenter(scrollPane);

        // --- INITIALIZE EFFECTS & EVENT HANDLERS --- //

        colorAdjust = new ColorAdjust();
        gaussianBlur = new GaussianBlur(0);
        sepiaTone = new SepiaTone(0);

        sepiaTone.setInput(gaussianBlur);
        colorAdjust.setInput(sepiaTone);
        imageView.setEffect(colorAdjust);

        sliderBrightness.valueProperty().addListener((obs, oldVal, newVal) -> colorAdjust.setBrightness(newVal.doubleValue()));
        sliderContrast.valueProperty().addListener((obs, oldVal, newVal) -> colorAdjust.setContrast(newVal.doubleValue()));
        sliderBlur.valueProperty().addListener((obs, oldVal, newVal) -> gaussianBlur.setRadius(newVal.doubleValue()));

        sliderZoom.valueProperty().addListener((obs, oldVal, newVal) -> {
            zoomLevel = newVal.doubleValue();
            imageView.setScaleX(zoomLevel * Math.signum(imageView.getScaleX()));
            imageView.setScaleY(zoomLevel * Math.signum(imageView.getScaleY()));
        });

        // Eseménykezelők
        btnOpen.setOnAction(e -> loadImage(primaryStage));
        btnSave.setOnAction(e -> saveImage(primaryStage));

        btnUndo.setOnAction(e -> undo());
        btnRedo.setOnAction(e -> redo());

        btnBW.setOnAction(e -> {
            colorAdjust.setSaturation(-1.0);
            sepiaTone.setLevel(0);
        });
        btnSepia.setOnAction(e -> {
            sepiaTone.setLevel(1.0);
            colorAdjust.setSaturation(0);
        });

        btnNegative.setOnAction(e -> applyNegative());
        btnEdge.setOnAction(e -> applyConvolution(getLaplacianMatrix()));
        btnSharpen.setOnAction(e -> applyConvolution(getUnsharpMaskingMatrix()));

        btnFlip.setOnAction(e -> applyHorizontalFlip());
        btnRotate.setOnAction(e -> {
            currentRotation += 90;
            imageView.setRotate(currentRotation);
        });

        btnRed.setOnAction(e -> applyColorChannel("red"));
        btnGreen.setOnAction(e -> applyColorChannel("green"));
        btnBlue.setOnAction(e -> applyColorChannel("blue"));

        btnReset.setOnAction(e -> {
            sliderBrightness.setValue(0);
            sliderContrast.setValue(0);
            sliderBlur.setValue(0);
            sliderZoom.setValue(1.0);

            colorAdjust.setBrightness(0);
            colorAdjust.setContrast(0);
            colorAdjust.setSaturation(0);
            sepiaTone.setLevel(0);
            gaussianBlur.setRadius(0);

            imageView.setRotate(0);
            currentRotation = 0;
            imageView.setScaleX(1);
            imageView.setScaleY(1);

            if (originalImage != null) {
                saveStateToUndo();
                currentImage = originalImage;
                imageView.setImage(currentImage);
            }
        });

        Scene scene = new Scene(mainPanel, 1100, 750);

        // NullPointerException hiba elkerülése
        try {
            URL cssUrl = getClass().getResource("style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("Warning: style.css not found.");
            }
        } catch (Exception ex) {
            System.err.println("Error loading CSS: " + ex.getMessage());
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- UI HELPER METHODS ---

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

    // --- LOGIC: FILE HANDLING ---

    private void loadImage(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Image");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp"));
        File file = chooser.showOpenDialog(stage);

        if (file != null) {
            originalImage = new Image(file.toURI().toString());
            currentImage = originalImage;
            imageView.setImage(currentImage);

            undoStack.clear();
            redoStack.clear();
        }
    }

    private void saveImage(Stage stage) {
        if (imageView.getImage() == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Image");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG file", "*.png"));
        File file = chooser.showSaveDialog(stage);

        if (file != null) {
            try {
                SnapshotParameters params = new SnapshotParameters();
                WritableImage snapshot = imageView.snapshot(params, null);
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
            } catch (IOException e) {
                // printStackTrace helyett robusztusabb logolás (IntelliJ javaslat)
                System.err.println("Failed to save image: " + e.getMessage());
            }
        }
    }

    // --- LOGIC: UNDO / REDO ---

    private void saveStateToUndo() {
        if (currentImage != null) {
            undoStack.push(currentImage);
            redoStack.clear();
        }
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(currentImage);
            currentImage = undoStack.pop();
            imageView.setImage(currentImage);
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(currentImage);
            currentImage = redoStack.pop();
            imageView.setImage(currentImage);
        }
    }


    @FunctionalInterface
    private interface PixelAction {
        void process(int x, int y, int width, int height, PixelReader reader, PixelWriter writer);
    }

    private void processAllPixels(PixelAction action) {
        if (currentImage == null) return;
        saveStateToUndo();

        int width = (int) currentImage.getWidth();
        int height = (int) currentImage.getHeight();

        WritableImage newImage = new WritableImage(width, height);
        PixelReader reader = currentImage.getPixelReader();
        PixelWriter writer = newImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                action.process(x, y, width, height, reader, writer);
            }
        }
        currentImage = newImage;
        imageView.setImage(currentImage);
    }

    // --- LOGIC: PIXEL ALGORITHMS ---

    // 1. Negative Filter
    private void applyNegative() {
        processAllPixels((x, y, w, h, reader, writer) -> {
            int argb = reader.getArgb(x, y);
            int a = (argb >> 24) & 0xff;
            int r = 255 - ((argb >> 16) & 0xff);
            int g = 255 - ((argb >> 8) & 0xff);
            int b = 255 - (argb & 0xff);
            writer.setArgb(x, y, (a << 24) | (r << 16) | (g << 8) | b);
        });
    }

    // 3. Color Channels Extraction
    private void applyColorChannel(String channel) {
        processAllPixels((x, y, w, h, reader, writer) -> {
            int argb = reader.getArgb(x, y);
            int a = (argb >> 24) & 0xff;
            int r = (argb >> 16) & 0xff;
            int g = (argb >> 8) & 0xff;
            int b = argb & 0xff;

            switch (channel) {
                case "red" -> { g = 0; b = 0; }
                case "green" -> { r = 0; b = 0; }
                case "blue" -> { r = 0; g = 0; }
            }

            writer.setArgb(x, y, (a << 24) | (r << 16) | (g << 8) | b);
        });
    }

    // 4. Horizontal Flip
    private void applyHorizontalFlip() {
        processAllPixels((x, y, w, h, reader, writer) -> {
            int argb = reader.getArgb(x, y);
            int flippedX = w - 1 - x;
            writer.setArgb(flippedX, y, argb); // Visszafele írjuk be
        });
    }

    // 2. Convolution Filter Application
    private void applyConvolution(float[][] matrix) {
        if (currentImage == null) return;
        saveStateToUndo();

        int width = (int) currentImage.getWidth();
        int height = (int) currentImage.getHeight();

        WritableImage newImage = new WritableImage(width, height);
        PixelReader reader = currentImage.getPixelReader();
        PixelWriter writer = newImage.getPixelWriter();

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {

                float sumR = 0, sumG = 0, sumB = 0;

                for (int cy = -1; cy <= 1; cy++) {
                    for (int cx = -1; cx <= 1; cx++) {
                        int argb = reader.getArgb(x + cx, y + cy);
                        int r = (argb >> 16) & 0xff;
                        int g = (argb >> 8) & 0xff;
                        int b = argb & 0xff;

                        float weight = matrix[cy + 1][cx + 1];

                        sumR += r * weight;
                        sumG += g * weight;
                        sumB += b * weight;
                    }
                }

                int finalR = Math.min(255, Math.max(0, (int) sumR));
                int finalG = Math.min(255, Math.max(0, (int) sumG));
                int finalB = Math.min(255, Math.max(0, (int) sumB));

                int originalArgb = reader.getArgb(x, y);
                int a = (originalArgb >> 24) & 0xff;

                writer.setArgb(x, y, (a << 24) | (finalR << 16) | (finalG << 8) | finalB);
            }
        }
        currentImage = newImage;
        imageView.setImage(currentImage);
    }

    private float[][] getLaplacianMatrix() {
        return new float[][] {
                { -1, -1, -1 },
                { -1,  8, -1 },
                { -1, -1, -1 }
        };
    }

    private float[][] getUnsharpMaskingMatrix() {
        return new float[][] {
                { -1, -1, -1 },
                { -1,  9, -1 },
                { -1, -1, -1 }
        };
    }
}
