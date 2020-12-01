/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.xml.validation.XMLReaderFactoryUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ImageUtils {
	private static final Logger logger = Logger.getLogger(ImageUtils.class);

	private static final Set<String> availableImageExtensions = new HashSet<>();

	static {
		availableImageExtensions.add("png");
		availableImageExtensions.add("gif");
		availableImageExtensions.add("jpg");
		availableImageExtensions.add("jpeg");
	}

	public static boolean isValidImageFileExtension(String format) {
		return availableImageExtensions.contains(format.toLowerCase());
	}

	public static Set<String> getValidImageFileExtensions() {
		return SetUtils.unmodifiableSet(availableImageExtensions);
	}

	/**
	 * WARNING: WILL CLOSE YOUR STREAM!!!
	 *
	 * Check image format. Use this instead of checking format by file name.
	 *
	 * @param stream {@link InputStream} File input stream
	 * @return true if validation pass, false in other case
	 */
	public static boolean isValidImage(InputStream stream) {
		if (stream == null) {
			throw new IllegalArgumentException("File stream could not be empty.");
		}
		
		try (ImageInputStream iis = ImageIO.createImageInputStream(stream)) {
			Iterator<ImageReader> iterator = ImageIO.getImageReaders(iis);

			if (iterator.hasNext()) {
				ImageReader reader = iterator.next();
				return isValidImageFileExtension(reader.getFormatName());
			}
		} catch (IOException e) {
			// Do nothing
		}

		return false;
	}

	/**
	 * Check image format. Use this instead of checking format by file name.
	 *
	 * @param data image file content.
	 * @return true if validation pass, false in other case
	 */
	public static boolean isValidImage(byte[] data) {
		boolean valid;
		try (InputStream stream = new ByteArrayInputStream(data)) {
			valid = isValidImage(stream);
		} catch (IOException e) {
			valid = false;
		}

		if(!valid) {
			valid = validateSvgContent(data);
		}
		return valid;
	}

	public static byte[] process(byte[] src, ImageProcessor processor) {
		try (InputStream stream = new ByteArrayInputStream(src)) {
			BufferedImage image = processor.process(ImageIO.read(stream));
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ImageIO.write(image, "png", outputStream);
			return outputStream.toByteArray();
		} catch (IOException e) {
			logger.debug("Error occurred: " + e.getMessage(), e);
		}
		return null;
	}

	public static byte[] resize(byte[] src, Dimension requiredSize) {
		return process(src, image -> resize(image, requiredSize));
	}

	/**
	 * Resize (downscale) an image to provide best fit without violation of an input image aspect ratio.
	 * If an input image has smaller dimensions than required ones then an input image will be simply centered without upscaling.
	 * Taken padding will be filled with a transparent color.
	 *
	 * @param origin input image to be resized.
	 * @return re-sized image data (PNG image format and ARGB pixel format to provide transparency).
	 */
	public static BufferedImage resize(BufferedImage origin, Dimension requiredSize) {
		final Dimension size = new Dimension(origin.getWidth(), origin.getHeight());

		// No scaling required
		if (size.equals(requiredSize) || size.width <= 0 || size.height <= 0) {
			return origin;
		}

		final double factorX = requiredSize.getWidth() / size.getWidth();
		final double factorY = requiredSize.getHeight() / size.getHeight();

		Image scaledOrigin = null;

		// Whether an image should be downscaled at first
		if (size.width > requiredSize.width || size.height > requiredSize.height) {
			if (factorX < factorY) {
				// Scale image to fit width (height will be less than required)
				int height = (int) Math.round(size.height * factorX);
				size.setSize(requiredSize.width, Math.max(height, 1));
			} else if (factorX > factorY) {
				// Scale image to fit height (width will be less than required)
				int width = (int) Math.round(size.width * factorY);
				size.setSize(Math.max(width, 1), requiredSize.height);
			} else {
				// Scale image to fit both dimensions (ideal case)
				size.setSize(requiredSize);
			}

			// Downscale an image
			scaledOrigin = origin.getScaledInstance(size.width, size.height, Image.SCALE_SMOOTH);
		}

		int paddingX = (requiredSize.width - size.width) / 2;
		int paddingY = (requiredSize.height - size.height) / 2;

		BufferedImage result = new BufferedImage(requiredSize.width, requiredSize.height, BufferedImage.TYPE_INT_ARGB);

		// Add padding, align image in the center
		Graphics2D graphics = result.createGraphics();
		graphics.setBackground(new Color(0,0,0,0));
		graphics.clearRect(0, 0, requiredSize.width, requiredSize.height);
		graphics.drawImage(scaledOrigin != null ? scaledOrigin : origin, paddingX, paddingY, null);
		graphics.dispose();

		return result;
	}

	public static Dimension getImageDimension(byte[] data) {
		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
				BufferedImage image = ImageIO.read(inputStream);

			return new Dimension(image.getWidth(), image.getHeight());
		} catch (IOException e) {
			logger.error("Could not get image dimensions" + e.getMessage());
		}

		return new Dimension(0, 0);
	}

	public interface ImageProcessor {
		BufferedImage process(BufferedImage image);
	}

	public static String getFileMask(String basename, String extension) {
		if ("*".equals(extension) || ("*".equals(basename) && StringUtils.isBlank(extension))) {
			return availableImageExtensions.stream()
					.map(e -> basename + "." + e)
					.collect(Collectors.joining("|"));
		} else if (availableImageExtensions.contains(extension)) {
			return basename + "." + extension;
		}

		return null;
	}

	private static boolean validateSvgContent(final byte[] data) {
		if(data == null) {
			return false;
		}
		try {
			XMLReaderFactoryUtils.createXMLReader().parse(new InputSource(new StringReader(new String(data))));
			return true;
		} catch (SAXException | IOException e) {
			return false;
		}
	}
}
