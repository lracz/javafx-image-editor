import javafx.scene.image.Image;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

public class AIClient {
    
    public static Image removeBackground(Image inputImage, String apiKey) throws Exception {
        return callMultipartApi(inputImage, apiKey, "https://api.remove.bg/v1.0/removebg", "image_file", "X-Api-Key");
    }

    public static Image colorizeImage(Image inputImage, String apiKey) throws Exception {
        return callMultipartApi(inputImage, apiKey, "https://api.deepai.org/api/colorizer", "image", "api-key");
    }

    private static Image callMultipartApi(Image inputImage, String apiKey, String endpoint, String fileParamName, String authHeaderName) throws Exception {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new Exception("Missing API Key.");
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(SwingFXUtils.fromFXImage(inputImage, null), "png", os);
        byte[] imageBytes = os.toByteArray();

        String boundary = "---" + UUID.randomUUID().toString();
        
        ByteArrayOutputStream bodyStream = new ByteArrayOutputStream();
        
        if (endpoint.contains("remove.bg")) {
            String sizeParam = "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"size\"\r\n\r\n" +
                    "preview\r\n";
            bodyStream.write(sizeParam.getBytes());
        }

        String header = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"" + fileParamName + "\"; filename=\"image.png\"\r\n" +
                "Content-Type: image/png\r\n\r\n";
        bodyStream.write(header.getBytes());
        bodyStream.write(imageBytes);
        bodyStream.write(("\r\n--" + boundary + "--\r\n").getBytes());

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header(authHeaderName, apiKey)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(bodyStream.toByteArray()))
                .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        
        if (response.statusCode() == 200) {
            return new Image(new ByteArrayInputStream(response.body()));
        } else {
            String errorMsg = new String(response.body());
            throw new Exception("API Error (" + response.statusCode() + "): " + errorMsg);
        }
    }
}
