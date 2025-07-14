/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.thumbnails.service;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javax.imageio.ImageIO;

import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.MailingComponentType;
import com.agnitas.beans.impl.MailingComponentImpl;
import com.agnitas.util.AgnUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.ServletContextAware;

import com.agnitas.dao.MailingComponentDao;

import jakarta.servlet.ServletContext;

/**
 * Implementation of {@link ThumbnailService}.
 */
public final class ThumbnailServiceImpl implements ThumbnailService, ServletContextAware {
	
	/** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(ThumbnailServiceImpl.class);

	/** Image type of thumbnail. */
	public static final String THUMBNAIL_IMAGE_TYPE = "png";
	
	/** Mimetype of thumbnail. */
	public static final String THUMBNAIL_MIMETYPE = "image/png";
	
	public static final String FALLBACK_DUMMY_THUMBNAIL_PNG_ZIPPED = "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";

	/** DAO accessing mailing component data. */
	private MailingComponentDao componentDao;
	
	private ServletContext servletContext;
	
	/**
	 * The current implementation replaces the the mailing thumbnail by
	 * a dummy thumbnail.
	 * 
	 * This implementation has been chosen due to the fact, that
	 * the current code of thumbnail generation requires a valid session ID
	 * with a valid EMM user. Both is not available in webservice context.
	 */
	@Override
	public void updateMailingThumbnailByWebservice(final int companyID, final int mailingID) throws Exception {
		final MailingComponent mailingComponent = readOrCreateThumbnailComponent(companyID, mailingID);
		updateThumbnailData(mailingComponent);
		
		this.componentDao.saveMailingComponent(mailingComponent);
	}
	
	/**
	 * Reads existing mailing component containing a thumbnail or creates a new one.
	 * 
	 * @param companyID company ID
	 * @param mailingID mailing ID
	 * 
	 * @return mailing component for thumbnails
	 */
	private final MailingComponent readOrCreateThumbnailComponent(final int companyID, final int mailingID) {
		final List<MailingComponent> list = this.componentDao.getMailingComponentsByType(companyID, mailingID, List.of(MailingComponentType.ThumbnailImage));
		
		return list.isEmpty()
				? createEmptyComponent(companyID, mailingID)
				: list.get(0);
	}
	
	/**
	 * Creates an empty mailing component for storing thumbnails.
	 * 
	 * @param companyID company ID
	 * @param mailingID mailing ID
	 * 
	 * @return empty mailing component for storing thumbnail
	 */
	private final MailingComponent createEmptyComponent(final int companyID, final int mailingID) {
		final MailingComponent mailingComponent = new MailingComponentImpl();
		mailingComponent.setCompanyID(companyID);
		mailingComponent.setComponentName("THUMBNAIL.png");
		mailingComponent.setDescription("Mailing preview image");
		mailingComponent.setMailingID(mailingID);
		mailingComponent.setType(MailingComponentType.ThumbnailImage);
		
		return mailingComponent;
	}
	
	/**
	 * Updates the thumbnail data in mailing component.
	 * 
	 * @param mailingComponent mailing component
	 * 
	 * @throws IOException on errors creating thumbnail
	 */
	private final void updateThumbnailData(final MailingComponent mailingComponent) throws IOException {
		assert mailingComponent.getType() == MailingComponentType.ThumbnailImage;	// Ensured by this service
		
		final Dimension maxThumbailSize = readMaxThumbnailSize(mailingComponent.getCompanyID());
		
		final byte[] imageData = thumbnailDataAsArray(maxThumbailSize);
		mailingComponent.setBinaryBlock(imageData, THUMBNAIL_MIMETYPE);
	}

	/**
	 * Reads the maximum thumbnail size from config service.
	 * 
	 * @param companyID company ID
	 * 
	 * @return maximum thumbnail size as configured
	 */
	private final Dimension readMaxThumbnailSize(final int companyID) {
		return new Dimension(
				ThumbnailService.MAILING_THUMBNAIL_WIDTH,
				ThumbnailService.MAILING_THUMBNAIL_HEIGHT);
	}
	
	/**
	 * Returns thumbnails as byte array.
	 * 
	 * @param maxThumbnailSize maximum thumbnail size
	 * 
	 * @return thumbnail as byte array
	 * 
	 * @throws IOException on errors creating the thumbnail
	 */
	private final byte[] thumbnailDataAsArray(final Dimension maxThumbnailSize) throws IOException {
		final BufferedImage thumbnailImage = createThumbnailImage(maxThumbnailSize);
		final BufferedImage scaledImage = scaleAndCropImage(thumbnailImage, maxThumbnailSize);
		
		try(final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			ImageIO.write(scaledImage, THUMBNAIL_IMAGE_TYPE, out);
			
			return out.toByteArray();
		}
	}
	
	/**
	 * Creates the thumbnail image.
	 * 
	 * @param maxThumbnailSize maximum thumbnail size
	 * 
	 * @return thumbnail image
	 * @throws IOException
	 */
	private final BufferedImage createThumbnailImage(final Dimension maxThumbnailSize) throws IOException {
		File imageFile = null;
		try {
			imageFile = new File(servletContext.getRealPath("images/webservice-dummy-thumbnail.png"));
		} catch (Exception e) {
			// Do nothing
		}
		
		if (imageFile != null && imageFile.exists()) {
			final Image thumbnailImage = ImageIO.read(imageFile);
			
			final BufferedImage image = new BufferedImage(thumbnailImage.getWidth(null), thumbnailImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g = (Graphics2D) image.getGraphics();
			g.drawImage(thumbnailImage, 0, 0, null);
			
			return image;
		} else {
			if (imageFile == null) {
				LOGGER.error("Cannot find file by context: images/webservice-dummy-thumbnail.png");
			} else {
				LOGGER.error(String.format("File '%s for dummy thumbnail not fond", imageFile.getAbsolutePath()));
			}
			
			try {
				byte[] fallbackImageData = AgnUtils.decodeZippedBase64(FALLBACK_DUMMY_THUMBNAIL_PNG_ZIPPED);
				final Image thumbnailImage = ImageIO.read(new ByteArrayInputStream(fallbackImageData));
				final BufferedImage image = new BufferedImage(thumbnailImage.getWidth(null), thumbnailImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
				final Graphics2D g = (Graphics2D) image.getGraphics();
				g.drawImage(thumbnailImage, 0, 0, null);
				return image;
			} catch (IOException e) {
				LOGGER.error("Cannot create webservice-dummy-thumbnail", e);
				
				final BufferedImage image = new BufferedImage(maxThumbnailSize.width, maxThumbnailSize.height, BufferedImage.TYPE_INT_ARGB);
				
				final Graphics2D g = (Graphics2D) image.getGraphics();
				g.setColor(Color.DARK_GRAY);
				g.fillRect(0, 0, maxThumbnailSize.width, maxThumbnailSize.height);
				
				g.setColor(Color.LIGHT_GRAY);
				g.drawLine(0, 0, maxThumbnailSize.width, maxThumbnailSize.height);
				g.drawLine(0, maxThumbnailSize.height, maxThumbnailSize.width, 0);
				g.drawArc(0, 0, maxThumbnailSize.width, maxThumbnailSize.height, 0, 360);
				
				g.setColor(Color.BLACK);
				g.drawRect(0, 0, maxThumbnailSize.width, maxThumbnailSize.height);
				
				return image;
			}
		}
	}
	
	/**
	 * Scales and crops source image according to match maximum thumbnail size.
	 * 
	 * @param sourceImage source image
	 * @param maxThumbnailSize maximum thumbnail size
	 * 
	 * @return scaled and cropped copy of source image
	 */
	private final BufferedImage scaleAndCropImage(final BufferedImage sourceImage, final Dimension maxThumbnailSize) {
		final int sourceWidth = sourceImage.getWidth();
		
		if(sourceWidth == 0) {
			return sourceImage;
		}
		
		// Compute scale factor
		final double scaleFactor = maxThumbnailSize.getWidth() / sourceWidth;
		
		// Define affine transform
		final AffineTransform scaleTransform = new AffineTransform();
		scaleTransform.scale(scaleFactor, scaleFactor);
		final AffineTransformOp scaleTransformOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BILINEAR);

		// Create destination image
		final BufferedImage scaledAndCroppedImage = new BufferedImage(maxThumbnailSize.width, maxThumbnailSize.height, BufferedImage.TYPE_INT_ARGB);
		
		// Apply transform
		scaleTransformOp.filter(sourceImage, scaledAndCroppedImage);
		
		return scaledAndCroppedImage;
	}

	/**
	 * Set DAO accessing mailing component data.
	 * 
	 * @param dao DAO accessing mailing component data
	 */
	public final void setMailingComponentDao(final MailingComponentDao dao) {
		this.componentDao = Objects.requireNonNull(dao, "mailingComponentDao is null");
	}

	@Override
	public final void setServletContext(final ServletContext servletContext) {
		this.servletContext = Objects.requireNonNull(servletContext, "servletContext is null");
	}

}
