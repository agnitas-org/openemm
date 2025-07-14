/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.dao.MailingComponentDao;
import com.agnitas.dao.RecipientDao;
import com.agnitas.emm.core.mailing.web.MailingPreviewHelper;
import com.agnitas.emm.core.mediatypes.service.MediaTypesService;
import com.agnitas.emm.core.thumbnails.service.ThumbnailService;
import com.agnitas.util.preview.PreviewImageGenerationQueue;
import com.agnitas.util.preview.PreviewImageGenerationTask;
import com.agnitas.util.preview.PreviewImageService;
import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.MailingComponentType;
import com.agnitas.beans.impl.MailingComponentImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mailing.service.MailingModel;
import com.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

// TODO Move thumbnail generation to new MailingThumbnailService
public class PreviewImageServiceImpl implements PreviewImageService {

    private static final Logger logger = LogManager.getLogger(PreviewImageServiceImpl.class);

	public static final String PREVIEW_FILE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "Preview";
    private static final String PDF_SERVICE_URL = PuppeteerServiceManager.PUPPETEER_SERVICE_URL + "/screenshot";

    protected static final int PUPPETEER_VIEWPORT_WIDTH = 1024;
    private static final int PUPPETEER_TIMEOUT = 60_000; // 1 minute

    protected ConfigService configService;
    private RecipientDao recipientDao;
    private MediaTypesService mediaTypesService;
    private MailingComponentDao mailingComponentDao;
    protected PreviewImageGenerationQueue queue;

    @Override
    public void generateMailingPreview(Admin admin, String sessionId, int mailingId, boolean async) {
        int customerId = recipientDao.getPreviewRecipient(admin.getCompanyID(), mailingId);
        Mediatype activeMediaType = mediaTypesService.getActiveMediaType(admin.getCompanyID(), mailingId);
        queue.enqueue(new MailingPreviewTask(sessionId, admin.getCompanyID(), mailingId, customerId, activeMediaType), async);
    }

    protected byte[] generatePreview(String url, Dimension maxSize, Integer viewportWidth) {
        ByteArrayOutputStream outputStream = null;
        try {
            BufferedImage image = createScreenshotWithPuppeteer(url, viewportWidth);

            if (image != null) {
                image = resizePreview(image, maxSize);

                outputStream = new ByteArrayOutputStream();
                ImageIO.write(image, "png", outputStream);

                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            logger.error("Error occurred while saving preview-image. URL: {}", url, e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    logger.error("Error occurred: {}", e.getMessage(), e);
                }
            }
        }
        return null;
    }

    private BufferedImage createScreenshotWithPuppeteer(String url, Integer viewportWidth) throws IOException {
        File imageTmpFile = File.createTempFile("preview_", ".png", AgnUtils.createDirectory(PREVIEW_FILE_DIRECTORY));
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            JSONObject requestBody = new JSONObject();
            requestBody.put("url", url);
            requestBody.put("path", imageTmpFile.getAbsolutePath());
            if (viewportWidth != null) {
                requestBody.put("width", viewportWidth);
            }
            requestBody.put("timeout", PUPPETEER_TIMEOUT);

            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(PDF_SERVICE_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && imageTmpFile.exists() && imageTmpFile.length() > 0) {
                return ImageIO.read(imageTmpFile);
            } else {
                throw new IOException("Screenshot generation failed. Response: " + response.getBody());
            }
        } catch (Exception e) {
            throw new IOException("Error while creating screenshot with a new puppeteer method - " + e.getMessage());
        } finally {
            Files.deleteIfExists(imageTmpFile.toPath());
        }
    }

    private BufferedImage resizePreview(BufferedImage image, Dimension maxSize) {
        if (logger.isInfoEnabled()) {
            logger.info("Preview image rescaling started");
        }

        Dimension sourceSize = new Dimension(image.getWidth(), image.getHeight());

        if (sourceSize.width < maxSize.width) {
            if (sourceSize.height > maxSize.height) {
                // Crop: Y
                image = image.getSubimage(0, 0, sourceSize.width, maxSize.height);
            }
            // Align center
            image = expandImage(image, maxSize.width, maxSize.height, new Color(0,0,0,0));
        } else {
            // Scale: X (and Y with same factor)

            double scale = ((double) maxSize.width / (double) sourceSize.width);
            int paddingY = 0;

            // Scaled size
            sourceSize = new Dimension(maxSize.width, (int) Math.round(sourceSize.height * scale));

            Image scaledImage = image.getScaledInstance(sourceSize.width, sourceSize.height, Image.SCALE_SMOOTH);
            image = new BufferedImage(maxSize.width, maxSize.height, BufferedImage.TYPE_INT_ARGB);

            if (sourceSize.height < maxSize.height) {
                paddingY = (maxSize.height - sourceSize.height) / 2;
            }

            Graphics2D graphics = image.createGraphics();
            graphics.setBackground(new Color(0,0,0,0));
            graphics.clearRect(0, 0, maxSize.width, maxSize.height);
            graphics.drawImage(scaledImage, 0, paddingY, null);
            graphics.dispose();
        }

        if (logger.isInfoEnabled()) {
            logger.info("Preview image rescaling is done");
        }

        return image;
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

    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    public void setRecipientDao(RecipientDao recipientDao) {
        this.recipientDao = recipientDao;
    }

    public void setMailingComponentDao(MailingComponentDao mailingComponentDao) {
        this.mailingComponentDao = mailingComponentDao;
    }

    public void setPreviewImageGenerationQueue(PreviewImageGenerationQueue previewImageGenerationQueue) {
        this.queue = previewImageGenerationQueue;
    }

    public void setMediaTypesService(MediaTypesService mediaTypesService) {
        this.mediaTypesService = mediaTypesService;
    }

    private class MailingPreviewTask implements PreviewImageGenerationTask {

        private final String sessionId;
        private final int companyId;
        private final int mailingId;
        private final int customerId;
        private final Mediatype mediaType;

        MailingPreviewTask(String sessionId, int companyId, int mailingId, int customerId, Mediatype mediaType) {
            this.sessionId = sessionId;
            this.companyId = companyId;
            this.mailingId = mailingId;
            this.customerId = customerId;
            this.mediaType = mediaType;
        }

        @Override
        public String getId() {
            return "MAILING#" + companyId + "#" + mailingId + "#" + customerId + "#" + mediaType.getMediaType();
        }

        @Override
        public void run() {
            try {
                Dimension maxSize = new Dimension(ThumbnailService.MAILING_THUMBNAIL_WIDTH, ThumbnailService.MAILING_THUMBNAIL_HEIGHT);
                byte[] preview = generatePreview(getPreviewUrl(), maxSize, PUPPETEER_VIEWPORT_WIDTH);

                List<MailingComponent> components = mailingComponentDao.getMailingComponents(mailingId, companyId, MailingComponentType.ThumbnailImage, false);
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
                    component.setType(MailingComponentType.ThumbnailImage);
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

            return baseUrl + "/mailing/preview/view-content.action;jsessionid=" + sessionId
                    + "?internal=true&format=" + getPreviewFormat()
                    + "&customerID=" + customerId
                    + "&mailingId=" + mailingId;
        }

        private int getPreviewFormat() {
            if (mediaType instanceof MediatypeEmail mediatypeEmail && mediatypeEmail.getMailFormat() == MailingModel.Format.TEXT.getCode()) {
                return MailingPreviewHelper.INPUT_TYPE_TEXT; 
            }
            return mediaType == null ? MailingPreviewHelper.INPUT_TYPE_HTML : mediaType.getMediaType().getMediaCode() + 1;
        }
    }
}
