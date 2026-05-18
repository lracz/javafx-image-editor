import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.util.Arrays;

public class ImageFilters {

    @FunctionalInterface
    public interface PixelAction {
        void process(int x, int y, int width, int height, PixelReader reader, PixelWriter writer);
    }

    public static Image processAllPixels(Image currentImage, PixelAction action) {
        if (currentImage == null) return null;
        
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
        return newImage;
    }

    public static Image applyNegative(Image img) {
        return processAllPixels(img, (x, y, w, h, reader, writer) -> {
            int argb = reader.getArgb(x, y);
            int a = (argb >> 24) & 0xff;
            int r = 255 - ((argb >> 16) & 0xff);
            int g = 255 - ((argb >> 8) & 0xff);
            int b = 255 - (argb & 0xff);
            writer.setArgb(x, y, (a << 24) | (r << 16) | (g << 8) | b);
        });
    }

    public static Image applyColorChannel(Image img, String channel) {
        return processAllPixels(img, (x, y, w, h, reader, writer) -> {
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

    public static Image applyHorizontalFlip(Image img) {
        return processAllPixels(img, (x, y, w, h, reader, writer) -> {
            int argb = reader.getArgb(x, y);
            int flippedX = w - 1 - x;
            writer.setArgb(flippedX, y, argb);
        });
    }

    public static Image applyConvolution(Image currentImage, float[][] matrix) {
        if (currentImage == null) return null;
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
                int a = (reader.getArgb(x, y) >> 24) & 0xff; 
                writer.setArgb(x, y, (a << 24) | (finalR << 16) | (finalG << 8) | finalB);
            }
        }
        return newImage;
    }
    
    public static float[][] getLaplacianMatrix() {
        return new float[][] { { -1, -1, -1 }, { -1,  8, -1 }, { -1, -1, -1 } };
    }

    public static float[][] getUnsharpMaskingMatrix() {
        return new float[][] { { -1, -1, -1 }, { -1,  9, -1 }, { -1, -1, -1 } };
    }

    public static Image applyMedianFilter(Image currentImage) {
        if (currentImage == null) return null;
        int width = (int) currentImage.getWidth();
        int height = (int) currentImage.getHeight();
        WritableImage newImage = new WritableImage(width, height);
        PixelReader reader = currentImage.getPixelReader();
        PixelWriter writer = newImage.getPixelWriter();

        int[] rArray = new int[9];
        int[] gArray = new int[9];
        int[] bArray = new int[9];

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int k = 0;
                for (int cy = -1; cy <= 1; cy++) {
                    for (int cx = -1; cx <= 1; cx++) {
                        int argb = reader.getArgb(x + cx, y + cy);
                        rArray[k] = (argb >> 16) & 0xff;
                        gArray[k] = (argb >> 8) & 0xff;
                        bArray[k] = argb & 0xff;
                        k++;
                    }
                }
                Arrays.sort(rArray);
                Arrays.sort(gArray);
                Arrays.sort(bArray);
                int finalR = rArray[4];
                int finalG = gArray[4];
                int finalB = bArray[4];
                int a = (reader.getArgb(x, y) >> 24) & 0xff;
                writer.setArgb(x, y, (a << 24) | (finalR << 16) | (finalG << 8) | finalB);
            }
        }
        return newImage;
    }

    public static Image applySobelEdgeDetection(Image currentImage) {
        if (currentImage == null) return null;
        int width = (int) currentImage.getWidth();
        int height = (int) currentImage.getHeight();
        WritableImage newImage = new WritableImage(width, height);
        PixelReader reader = currentImage.getPixelReader();
        PixelWriter writer = newImage.getPixelWriter();

        int[][] Gx = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] Gy = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int sumRx = 0, sumGx = 0, sumBx = 0;
                int sumRy = 0, sumGy = 0, sumBy = 0;

                for (int cy = -1; cy <= 1; cy++) {
                    for (int cx = -1; cx <= 1; cx++) {
                        int argb = reader.getArgb(x + cx, y + cy);
                        int r = (argb >> 16) & 0xff;
                        int g = (argb >> 8) & 0xff;
                        int b = argb & 0xff;

                        int wx = Gx[cy + 1][cx + 1];
                        int wy = Gy[cy + 1][cx + 1];

                        sumRx += r * wx; sumGx += g * wx; sumBx += b * wx;
                        sumRy += r * wy; sumGy += g * wy; sumBy += b * wy;
                    }
                }

                int finalR = (int) Math.min(255, Math.sqrt(sumRx * sumRx + sumRy * sumRy));
                int finalG = (int) Math.min(255, Math.sqrt(sumGx * sumGx + sumGy * sumGy));
                int finalB = (int) Math.min(255, Math.sqrt(sumBx * sumBx + sumBy * sumBy));
                int a = (reader.getArgb(x, y) >> 24) & 0xff;

                writer.setArgb(x, y, (a << 24) | (finalR << 16) | (finalG << 8) | finalB);
            }
        }
        return newImage;
    }

    public static Image applyHistogramEqualization(Image currentImage) {
        if (currentImage == null) return null;
        int width = (int) currentImage.getWidth();
        int height = (int) currentImage.getHeight();
        int totalPixels = width * height;
        
        PixelReader reader = currentImage.getPixelReader();
        WritableImage newImage = new WritableImage(width, height);
        PixelWriter writer = newImage.getPixelWriter();

        int[] histR = new int[256];
        int[] histG = new int[256];
        int[] histB = new int[256];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = reader.getArgb(x, y);
                histR[(argb >> 16) & 0xff]++;
                histG[(argb >> 8) & 0xff]++;
                histB[argb & 0xff]++;
            }
        }

        int[] cdfR = new int[256];
        int[] cdfG = new int[256];
        int[] cdfB = new int[256];
        cdfR[0] = histR[0]; cdfG[0] = histG[0]; cdfB[0] = histB[0];
        
        for (int i = 1; i < 256; i++) {
            cdfR[i] = cdfR[i - 1] + histR[i];
            cdfG[i] = cdfG[i - 1] + histG[i];
            cdfB[i] = cdfB[i - 1] + histB[i];
        }

        int minCdfR = 0, minCdfG = 0, minCdfB = 0;
        for (int i = 0; i < 256; i++) { if (cdfR[i] > 0) { minCdfR = cdfR[i]; break; } }
        for (int i = 0; i < 256; i++) { if (cdfG[i] > 0) { minCdfG = cdfG[i]; break; } }
        for (int i = 0; i < 256; i++) { if (cdfB[i] > 0) { minCdfB = cdfB[i]; break; } }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = reader.getArgb(x, y);
                int a = (argb >> 24) & 0xff;
                int r = (argb >> 16) & 0xff;
                int g = (argb >> 8) & 0xff;
                int b = argb & 0xff;

                int eqR = Math.round(((float)(cdfR[r] - minCdfR) / (totalPixels - minCdfR)) * 255);
                int eqG = Math.round(((float)(cdfG[g] - minCdfG) / (totalPixels - minCdfG)) * 255);
                int eqB = Math.round(((float)(cdfB[b] - minCdfB) / (totalPixels - minCdfB)) * 255);

                writer.setArgb(x, y, (a << 24) | (eqR << 16) | (eqG << 8) | eqB);
            }
        }
        return newImage;
    }

    public static Image applySketchFilter(Image input) {
        int width = (int) input.getWidth();
        int height = (int) input.getHeight();
        WritableImage output = new WritableImage(width, height);
        PixelReader reader = input.getPixelReader();
        PixelWriter writer = output.getPixelWriter();

        int[][] Gx = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] Gy = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double gxValue = 0, gyValue = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        Color c = reader.getColor(x + j, y + i);
                        double gray = c.getRed() * 0.299 + c.getGreen() * 0.587 + c.getBlue() * 0.114;
                        gxValue += gray * Gx[i + 1][j + 1];
                        gyValue += gray * Gy[i + 1][j + 1];
                    }
                }
                double magnitude = Math.sqrt(gxValue * gxValue + gyValue * gyValue);
                double sketchColor = 1.0 - Math.min(1.0, magnitude * 2.0);
                writer.setColor(x, y, new Color(sketchColor, sketchColor, sketchColor, 1.0));
            }
        }
        return output;
    }

    public static Image applyUpscale(Image input) {
        int w1 = (int) input.getWidth();
        int h1 = (int) input.getHeight();
        int w2 = w1 * 2;
        int h2 = h1 * 2;
        WritableImage output = new WritableImage(w2, h2);
        PixelReader reader = input.getPixelReader();
        PixelWriter writer = output.getPixelWriter();

        for (int y = 0; y < h2; y++) {
            for (int x = 0; x < w2; x++) {
                writer.setColor(x, y, reader.getColor(x / 2, y / 2));
            }
        }
        return output;
    }
}
