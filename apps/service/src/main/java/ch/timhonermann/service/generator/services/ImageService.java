package ch.timhonermann.service.generator.services;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Service
public class ImageService {
  public BufferedImage resizeImage(String base64Image, int height) {
    BufferedImage resizedImage = null;

    try (var imageInputBuffer = decodeBase64Image(base64Image)) {
      var originalImage = ImageIO.read(imageInputBuffer);
      resizedImage = resizeImage(originalImage, height);
    } catch (Exception ex) {
      System.out.println("Error while resizing image: " + ex.getMessage());
    }

    return resizedImage;
  }

  private ByteArrayInputStream decodeBase64Image(String base64Image) throws IOException {
    var parts = base64Image.split(",");

    // Extract the data part
    var dataPart = parts.length > 1 ? parts[1] : parts[0];
    System.out.println(dataPart);
    var imageBytes = Base64.getDecoder().decode(dataPart);

    return new ByteArrayInputStream(imageBytes);
  }

  private BufferedImage resizeImage(BufferedImage originalImage, int targetHeight) {
    double aspectRatio = (double) originalImage.getWidth() / originalImage.getHeight();
    int targetWidth = (int) (targetHeight * aspectRatio);

    BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());
    resizedImage.getGraphics().drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);

    return resizedImage;
  }
}
