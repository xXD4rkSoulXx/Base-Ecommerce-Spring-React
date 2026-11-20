package com.d4rksoul.base.ecommerce.spring.react.storage;

import com.d4rksoul.base.ecommerce.spring.react.constant.Constants;
import com.d4rksoul.base.ecommerce.spring.react.enums.ImageFormat;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidImageException;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;

@Component
public class ImageOptimizer {

    public OptimizedImage optimize(byte[] content, ImageFormat format) {
        if (format == ImageFormat.WEBP) {
            return new OptimizedImage(content, format);
        }

        BufferedImage image = resize(read(content));

        if (hasTransparency(image)) {
            return new OptimizedImage(writePng(image), ImageFormat.PNG);
        }

        return new OptimizedImage(writeJpeg(toRgb(image)), ImageFormat.JPEG);
    }

    private BufferedImage read(byte[] content) {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);

            if (!readers.hasNext()) {
                throw new InvalidImageException("The image could not be read.");
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(input);

                if ((long) reader.getWidth(0) * reader.getHeight(0) > Constants.IMAGE_MAX_SOURCE_PIXELS) {
                    throw new InvalidImageException("The image dimensions are too large.");
                }

                return reader.read(0);
            } finally {
                reader.dispose();
            }
        } catch (IOException _) {
            throw new InvalidImageException("The image could not be read.");
        }
    }

    private BufferedImage resize(BufferedImage source) {
        if (source.getWidth() <= Constants.IMAGE_MAX_WIDTH) {
            return source;
        }

        int height = Math.round((float) source.getHeight() * Constants.IMAGE_MAX_WIDTH / source.getWidth());
        BufferedImage target = new BufferedImage(Constants.IMAGE_MAX_WIDTH, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = target.createGraphics();

        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(source, 0, 0, Constants.IMAGE_MAX_WIDTH, height, null);
        } finally {
            graphics.dispose();
        }

        return target;
    }

    private boolean hasTransparency(BufferedImage image) {
        if (!image.getColorModel().hasAlpha()) {
            return false;
        }

        int width = image.getWidth();
        int[] row = new int[width];

        for (int y = 0; y < image.getHeight(); y++) {
            image.getRGB(0, y, width, 1, row, 0, width);

            for (int pixel : row) {
                if ((pixel >>> 24) != 0xFF) {
                    return true;
                }
            }
        }

        return false;
    }

    private BufferedImage toRgb(BufferedImage image) {
        BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rgb.createGraphics();

        try {
            graphics.drawImage(image, 0, 0, null);
        } finally {
            graphics.dispose();
        }

        return rgb;
    }

    private byte[] writePng(BufferedImage image) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, "png", output);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }

        return output.toByteArray();
    }

    private byte[] writeJpeg(BufferedImage image) {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam parameters = writer.getDefaultWriteParam();
        parameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        parameters.setCompressionQuality(Constants.IMAGE_JPEG_QUALITY);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (ImageOutputStream stream = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(stream);
            writer.write(null, new IIOImage(image, null, null), parameters);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        } finally {
            writer.dispose();
        }

        return output.toByteArray();
    }
}
