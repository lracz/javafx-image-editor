import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Stack;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.application.Platform;

public class StateController {
    private ImageView imageView;
    private Image originalImage;
    private Image currentImage;
    private final Stack<Image> undoStack = new Stack<>();
    private final Stack<Image> redoStack = new Stack<>();

    public StateController(ImageView imageView) {
        this.imageView = imageView;
    }

    public Image getCurrentImage() { 
        return currentImage; 
    }
    
    public void setCurrentImage(Image newImage) {
        this.currentImage = newImage;
        Platform.runLater(() -> imageView.setImage(currentImage));
    }

    public void saveStateToUndo() {
        if (currentImage != null) {
            undoStack.push(currentImage);
            redoStack.clear();
        }
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(currentImage);
            setCurrentImage(undoStack.pop());
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(currentImage);
            setCurrentImage(redoStack.pop());
        }
    }

    public void reset() {
        if (originalImage != null) {
            saveStateToUndo();
            setCurrentImage(originalImage);
        }
    }

    public void loadImage(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Image");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp"));
        File file = chooser.showOpenDialog(stage);
        
        if (file != null) {
            originalImage = new Image(file.toURI().toString());
            setCurrentImage(originalImage);
            undoStack.clear();
            redoStack.clear();
        }
    }

    public void saveImage(Stage stage) {
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
                System.err.println("Failed to save image: " + e.getMessage());
            }
        }
    }
}
