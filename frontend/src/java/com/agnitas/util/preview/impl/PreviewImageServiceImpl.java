/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util.preview.impl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.emm.core.mailing.web.MailingPreviewHelper;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.mediatypes.service.MediaTypesService;
import com.agnitas.util.preview.PreviewImageGenerationQueue;
import com.agnitas.util.preview.PreviewImageGenerationTask;
import com.agnitas.util.preview.PreviewImageService;
import com.agnitas.web.ComMailingBaseAction;
import cz.vutbr.web.css.MediaSpec;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.MailingComponentType;
import org.agnitas.beans.impl.MailingComponentImpl;
import org.agnitas.dao.MailingComponentDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.MailingSendAction;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.io.DOMSource;
import org.fit.cssbox.io.DefaultDOMSource;
import org.fit.cssbox.io.DefaultDocumentSource;
import org.fit.cssbox.io.DocumentSource;
import org.fit.cssbox.layout.BrowserCanvas;
import org.springframework.beans.factory.annotation.Required;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class PreviewImageServiceImpl implements PreviewImageService {
    /** The logger */
    private static final Logger logger = Logger.getLogger(PreviewImageServiceImpl.class);
    
	public static final String PREVIEW_FILE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "Preview";

	protected static final int GRID_TEMPLATE_THUMBNAIL_WIDTH = 300;
	protected static final int GRID_TEMPLATE_THUMBNAIL_HEIGHT = 300;

    protected static final int DIV_CONTAINER_THUMBNAIL_WIDTH = 150;
    protected static final int DIV_CONTAINER_THUMBNAIL_HEIGHT = 100;

    protected ConfigService configService;
    private ComRecipientDao recipientDao;
    private MediaTypesService mediaTypesService;
    private MailingComponentDao mailingComponentDao;
    protected PreviewImageGenerationQueue queue;

    @Override
    public void generateMailingPreview(ComAdmin admin, String sessionId, int mailingId, boolean async) {
        int customerId = recipientDao.getPreviewRecipient(admin.getCompanyID(), mailingId);
        MediaTypes activeMediaType = mediaTypesService.getActiveMediaType(admin.getCompanyID(), mailingId);
        if (customerId > 0) {
            queue.enqueue(new MailingPreviewTask(sessionId, admin.getCompanyID(), mailingId, customerId, activeMediaType), async);
        } else {
            logger.error("Cannot create mailing preview: no test or admin recipient found");
        }
    }

    protected byte[] generatePreview(String url, Dimension maxSize, boolean isMailingPreview) throws Exception {
        ByteArrayOutputStream outputStream = null;
        try {
            BufferedImage image;
            if (StringUtils.isNotBlank(configService.getValue(ConfigValue.WkhtmlToImageToolPath))) {
            	// Use wkhtmltoimage for rendering
            	image = renderDocumentWithWkhtml(url);
            } else {
            	// Use cssBox for rendering
            	image = renderDocumentWithCssBox(url);
            }
            
            if (image != null) {
                image = resizePreview(image, maxSize, isMailingPreview);

                outputStream = new ByteArrayOutputStream();
                ImageIO.write(image, "png", outputStream);

                return outputStream.toByteArray();
            }
        } catch (SAXException e) {
            logger.error("Error occurred while rendering an html page preview. URL: " + url, e);
        } catch (IOException e) {
            logger.error("Error occurred while saving preview-image. URL: " + url, e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    logger.error("Error occurred: " + e.getMessage(), e);
                }
            }
        }
        return null;
    }

	private BufferedImage renderDocumentWithWkhtml(String url) throws Exception, IOException, InterruptedException {
		if (!new File(configService.getValue(ConfigValue.WkhtmlToImageToolPath)).exists()) {
			throw new Exception("Preview generation via wkhtmltoimage failed: " + configService.getValue(ConfigValue.WkhtmlToImageToolPath) + " does not exist");
		}
		
		File imageTempFile = File.createTempFile("preview_", ".png", AgnUtils.createDirectory(PREVIEW_FILE_DIRECTORY));
		
		String[] command = new String[] {
			configService.getValue(ConfigValue.WkhtmlToImageToolPath),
//			"--crop-x", Integer.toString(0),
//			"--crop-y", Integer.toString(0),
//			"--crop-w", Integer.toString(maxWidth),
//			"--crop-h", Integer.toString(maxHeight),
//			"--format, png",
            "--quality", Integer.toString(50),
			url,
			imageTempFile.getAbsolutePath()};
		
		Process process = Runtime.getRuntime().exec(command);
		process.waitFor();
		
		if (!imageTempFile.exists() || imageTempFile.length() == 0) {
			throw new Exception("Preview generation via wkhtmltoimage failed: \n" + StringUtils.join(command, "\n"));
		}
		
		BufferedImage image = ImageIO.read(imageTempFile);
		
		imageTempFile.delete();
		
		return image;
	}

    private BufferedImage renderDocumentWithCssBox(String url) throws IOException, SAXException {
    	DocumentSource docSource = new DefaultDocumentSource(ensureUrlHasProtocol(url));
        
        final Dimension windowSize = new Dimension(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        final String mediaType = "screen";

        // Parse the input document
        DOMSource parser = new DefaultDOMSource(docSource);
        Document doc = parser.parse();

        // Create the media specification
        MediaSpec media = new MediaSpec(mediaType);
        media.setDimensions(windowSize.width, windowSize.height);
        media.setDeviceDimensions(windowSize.width, windowSize.height);

        // Create the CSS analyzer
        DOMAnalyzer analyzer = new DOMAnalyzer(doc, docSource.getURL());

        analyzer.setMediaSpec(media);

        // Convert the HTML presentation attributes to inline styles
        analyzer.attributesToStyles();

        // Use the standard style sheet
        analyzer.addStyleSheet(null, CSSNorm.stdStyleSheet(), DOMAnalyzer.Origin.AGENT);

        // Use the additional style sheet
        analyzer.addStyleSheet(null, CSSNorm.userStyleSheet(), DOMAnalyzer.Origin.AGENT);

        // Render form fields using css
        analyzer.addStyleSheet(null, CSSNorm.formsStyleSheet(), DOMAnalyzer.Origin.AGENT);

        // Load the author style sheets
        analyzer.getStyleSheets();

        BrowserCanvas contentCanvas = new BrowserCanvas(analyzer.getRoot(), analyzer, docSource.getURL());
        // We have a correct media specification, do not update
        contentCanvas.setAutoMediaUpdate(false);
        contentCanvas.getConfig().setClipViewport(false);
        contentCanvas.getConfig().setLoadImages(true);
        contentCanvas.getConfig().setLoadBackgroundImages(true);
        contentCanvas.getConfig().setImageLoadTimeout((int) TimeUnit.SECONDS.toMillis(IMAGE_LOADING_TIMEOUT));

        contentCanvas.createLayout(windowSize);

        return contentCanvas.getImage();
    }

    private BufferedImage resizePreview(BufferedImage image, Dimension maxSize, boolean isMailingPreview) {
        if (logger.isInfoEnabled()) {
            logger.info("Preview image rescaling started");
        }

        Dimension sourceSize = new Dimension(image.getWidth(), image.getHeight());
        Dimension previewSize = getPreviewSize(maxSize, sourceSize, isMailingPreview);

        if (sourceSize.width < previewSize.width) {
            if (sourceSize.height > previewSize.height) {
                // Crop: Y
                image = image.getSubimage(0, 0, sourceSize.width, previewSize.height);
            }
            if (isMailingPreview) {
                // Align center
                image = expandImage(image, previewSize.width, previewSize.height, new Color(0,0,0,0));
            }
        } else {
            // Scale: X (and Y with same factor)

            double scale = ((double) previewSize.width / (double) sourceSize.width);
            int paddingY = 0;

            // Scaled size
            sourceSize = new Dimension(previewSize.width, (int) Math.round(sourceSize.height * scale));

            Image scaledImage = image.getScaledInstance(sourceSize.width, sourceSize.height, Image.SCALE_SMOOTH);
            image = new BufferedImage(previewSize.width, previewSize.height, BufferedImage.TYPE_INT_ARGB);

            if (sourceSize.height < previewSize.height && isMailingPreview) {
                paddingY = (previewSize.height - sourceSize.height) / 2;
            }

            Graphics2D graphics = image.createGraphics();
            graphics.setBackground(new Color(0,0,0,0));
            graphics.clearRect(0, 0, previewSize.width, previewSize.height);
            graphics.drawImage(scaledImage, 0, paddingY, null);
            graphics.dispose();
        }

        if (logger.isInfoEnabled()) {
            logger.info("Preview image rescaling is done");
        }

        return image;
    }

    private Dimension getPreviewSize(Dimension maxSize, Dimension sourceSize, boolean isMailingPreview) {
        if (isMailingPreview) {
            return maxSize;
        }

        double scaleX = maxSize.getWidth() / sourceSize.getWidth();
        double scaleY = maxSize.getHeight() / sourceSize.getHeight();

        if (scaleX == scaleY) {
            return maxSize;
        }

        if (scaleX < scaleY) {
            return new Dimension(maxSize.width, (int) Math.round(scaleY * sourceSize.height));
        } else {
            return new Dimension((int) Math.round(scaleX * sourceSize.width), maxSize.height);
        }
    }

    private BufferedImage expandImage(BufferedImage image, int newWidth, int newHeight, Color frameColor) {
        newWidth = Math.max(image.getWidth(), newWidth);
        newHeight = Math.max(image.getHeight(), newHeight);

        int paddingX = (newWidth - image.getWidth()) / 2;
        int paddingY = (newHeight - image.getHeight()) / 2;

        if (paddingX == 0 && paddingY == 0) {
            return image;
        }

        BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = newImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setBackground(frameColor);
        graphics.clearRect(0, 0, newWidth, newHeight);
        graphics.drawImage(image, paddingX, paddingY, null);
        graphics.dispose();

        return newImage;
    }

    private String ensureUrlHasProtocol(String url) {
        final String[] expectedProtocols = new String[] {"http:", "https:", "ftp:", "file:"};

        boolean protocolOmitted = true;

        for (String protocol : expectedProtocols) {
            if (url.startsWith(protocol)) {
                protocolOmitted = false;
                break;
            }
        }

        if (protocolOmitted) {
            return "http://" + url;
        }
        return url;
    }

    @Required
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    @Required
    public void setRecipientDao(ComRecipientDao recipientDao) {
        this.recipientDao = recipientDao;
    }

    @Required
    public void setMailingComponentDao(MailingComponentDao mailingComponentDao) {
        this.mailingComponentDao = mailingComponentDao;
    }

    @Required
    public void setPreviewImageGenerationQueue(PreviewImageGenerationQueue previewImageGenerationQueue) {
        this.queue = previewImageGenerationQueue;
    }
    
    @Required
    public void setMediaTypesService(MediaTypesService mediaTypesService) {
        this.mediaTypesService = mediaTypesService;
    }
    
    private class MailingPreviewTask implements PreviewImageGenerationTask {
        private String sessionId;
        private int companyId;
        private int mailingId;
        private int customerId;
        private MediaTypes mediaType;

        MailingPreviewTask(String sessionId, int companyId, int mailingId, int customerId, MediaTypes mediaType) {
            this.sessionId = sessionId;
            this.companyId = companyId;
            this.mailingId = mailingId;
            this.customerId = customerId;
            this.mediaType = mediaType;
        }

        @Override
        public String getId() {
            return "MAILING#" + companyId + "#" + mailingId + "#" + customerId + "#" + mediaType;
        }

        @Override
        public void run() {
            try {
                Dimension maxSize = new Dimension(ComMailingBaseAction.MAILING_PREVIEW_WIDTH, ComMailingBaseAction.MAILING_PREVIEW_HEIGHT);
                byte[] preview = generatePreview(getPreviewUrl(), maxSize, true);

                List<MailingComponent> components = mailingComponentDao.getMailingComponents(mailingId, companyId, MailingComponentType.ThumbnailImage.getCode(), false);
                if (components.size() > 1) {
                	// Clean up all thumbnails and create a single thumbnail afterwards
                	mailingComponentDao.deleteMailingComponents(components);
                	components = new ArrayList<>();
                }
                
                if (components.isEmpty()) {
                	// Create a single thumbnail
                    MailingComponent component = new MailingComponentImpl();
                    component.setCompanyID(companyId);
                    component.setMailingID(mailingId);
                    component.setType(MailingComponentType.ThumbnailImage.getCode());
                    component.setDescription("Mailing preview Image");
                    component.setComponentName("THUMBNAIL.png");
                    component.setBinaryBlock(preview, "image/png");
                    mailingComponentDao.saveMailingComponent(component);
                } else {
                	// Fill the single thumbnail with new data
                	for (MailingComponent component : components) {
	                    component.setDescription("Mailing preview Image");
	                    component.setComponentName("THUMBNAIL.png");
	                    component.setBinaryBlock(preview, "image/png");
	                    mailingComponentDao.saveMailingComponent(component);
                	}
                }
            } catch (Exception e) {
                logger.error("Error generating preview", e);
            }
        }

        private String getPreviewUrl() {
            String baseUrl = configService.getValue(ConfigValue.PreviewUrl);

            if (StringUtils.isBlank(baseUrl)) {
                baseUrl = configService.getValue(ConfigValue.SystemUrl);
            }

            return baseUrl + "/mailingsend.do" + ";jsessionid=" + sessionId + "?action=" + MailingSendAction.ACTION_PREVIEW +
                    "&mailingID=" + mailingId +
                    "&previewForm.format=" + (mediaType == null ? MailingPreviewHelper.INPUT_TYPE_HTML : mediaType.getMediaCode() + 1) +
                    "&previewForm.customerID=" + customerId;
        }
    }

}
