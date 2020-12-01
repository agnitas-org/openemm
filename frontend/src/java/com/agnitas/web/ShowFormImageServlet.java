/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.component.service.ComponentService;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.TimeoutLRUMap;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.beans.FormComponent;
import com.agnitas.beans.FormComponent.FormComponentType;

/**
 * This servlet loads and shows a image from the form_component_tbl.
 * The images are cached.
 * If the image cannot be found, a text is shown, which is also cached.
 * The caching is done for performance reasons and the caching timeout is set in emm.properties.
 * The caching can be disabled by the optional item "/nc" for preview-reasons etc.
 * 
 * The parameter <licenseID> is also optional.
 * The licenceID will not be used by now.
 * The company_id is not unique within the various licenses.
 * In a later version of rdir the licenceId is used for switching between the datasources and CDN which contain the images for each company.
 * 
 * Normal Link syntax:
 *   http://<rdir-domain>/formImage/<licenseID>/<companyID>/<formID>/<imageFileName>
 * 
 * NoCaching Link syntax:<br />
 *   http://<rdir-domain>/formImage/nc/<licenseID>/<companyID>/<formID>/<imageFileName>
 * 
 * Example form image htmllink:
 *   <img src="http://rdir.de/formImage/1/610/105141/logo.jpg" />
 */
public class ShowFormImageServlet extends HttpServlet {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8121518755028038107L;

	/** The Constant logger. */
	private static final transient Logger logger = Logger.getLogger(ShowFormImageServlet.class);

	/** The Constant CLEAR_IMAGE_CACHE_HTTP_PARAMETER_NAME. */
	public static final String CLEAR_IMAGE_CACHE_HTTP_PARAMETER_NAME = "clearFormImageCache";
	
	/** The image cache. */
	protected TimeoutLRUMap<String, CachedImageData> imageCache;

	/** The form componentService. */
	protected ComponentService componentService;
	
	protected ConfigService configService;

	/**
	 * Sets the  component service.
	 *
	 * @param componentService the new componentService
	 */
	public void setComponentService(ComponentService componentService) {
		this.componentService = componentService;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	/**
	 * Gets the ComponentService.
	 *
	 * @return the ComponentService
	 */
	private ComponentService getComponentService() {
		if (componentService == null) {
			componentService = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("componentService", ComponentService.class);
		}
		
		return componentService;
	}

    private ConfigService getConfigService() {
		if (configService == null) {
			configService = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("ConfigService", ConfigService.class);
		}
		return configService;
	}

	/**
	 * Return existing cache or create new one.
	 *
	 * @return the image cache
	 */
	private TimeoutLRUMap<String, CachedImageData> getImageCache() {
		if (imageCache == null) {
			imageCache = new TimeoutLRUMap<>(getConfigService().getIntegerValue(ConfigValue.HostedImageMaxCache), getConfigService().getLongValue(ConfigValue.HostedImageMaxCacheTimeMillis));
		}

		return imageCache;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			if (request.getAttribute(CLEAR_IMAGE_CACHE_HTTP_PARAMETER_NAME) != null) {
				if (request.getAttribute(CLEAR_IMAGE_CACHE_HTTP_PARAMETER_NAME).toString().equals("doClear")) {
					getImageCache().clean();
				}
				request.removeAttribute(CLEAR_IMAGE_CACHE_HTTP_PARAMETER_NAME);
				return;
			}

			String companyIdString = null;
			String formIdString = null;
			String imageFileName = null;
			String licenseIdString = "0";

			String[] uriParts = request.getRequestURI().split("/");
			if (uriParts.length >= 3 && AgnUtils.isNumber(uriParts[uriParts.length - 3]) && AgnUtils.isNumber(uriParts[uriParts.length - 2]) && StringUtils.isNotBlank(uriParts[uriParts.length - 1])) {
				companyIdString = uriParts[uriParts.length - 3];
				formIdString = uriParts[uriParts.length - 2];
				imageFileName = uriParts[uriParts.length - 1];
			}
			if (uriParts.length >= 4 && AgnUtils.isNumber(uriParts[uriParts.length - 4])) {
				licenseIdString = uriParts[uriParts.length - 4];
			}
			
			if (imageFileName != null) {
				if (imageFileName.contains(";")) {
					// remove ";jsessionid..."
					imageFileName = imageFileName.substring(0, imageFileName.indexOf(";"));
				}
				imageFileName = AgnUtils.decodeURL(imageFileName);
			}

			// check for noCaching
			boolean thumbnail = uriParts.length >= 4 && "thb".equalsIgnoreCase(uriParts[uriParts.length - 4]) || uriParts.length >= 5 && "thb".equalsIgnoreCase(uriParts[uriParts.length - 5]);
			
			// check for noCaching
			boolean noCache = uriParts.length >= 4 && "nc".equalsIgnoreCase(uriParts[uriParts.length - 4]) || uriParts.length >= 5 && "nc".equalsIgnoreCase(uriParts[uriParts.length - 5]);

			// Check mandatory servlet parameters
			// On missing parameter don't show any data
			if ((StringUtils.isNotEmpty(companyIdString) && StringUtils.isNotEmpty(formIdString) && StringUtils.isNotEmpty(imageFileName))) {
				CachedImageData image = getImage(request, Integer.parseInt(licenseIdString), Integer.parseInt(companyIdString), Integer.parseInt(formIdString), imageFileName, noCache, thumbnail);
				writeImageToResponse(request, response, image);
			}
		} catch (Exception e) {
			logger.error("Exception: " + e.getMessage(), e);
			throw new ServletException("Error occurred when loading image", e);
		}
	}

	/**
	 * Look up image data in cache and DB/CDN. If found there, it will be
	 * cached.
	 *
	 * @param request the request
	 * @param licenseID the license id
	 * @param companyID the company id
	 * @param formID the form id
	 * @param imageFileName the image file name
	 * @param noCache the no cache
	 * @return the image
	 * @throws Exception the exception
	 */
	private CachedImageData getImage(HttpServletRequest request, int licenseID, @VelocityCheck int companyID, int formID, String imageFileName, boolean noCache, boolean thumbnail) throws Exception {
		if (thumbnail) {
			FormComponent thumbnailFormComponent = getComponentService().getFormComponent(formID, companyID, imageFileName, FormComponentType.THUMBNAIL);
			CachedImageData thumbnailImage = new CachedImageData();
			thumbnailImage.mimeType = thumbnailFormComponent.getMimeType();
			thumbnailImage.imageData = thumbnailFormComponent.getData();
			return thumbnailImage;
		} else {
			// Generate cache lookup key
			String cacheKey = generateCachingKey(licenseID, companyID, formID, imageFileName);
			// Look up image in cache
			CachedImageData image = getImageCache().get(cacheKey);
			if (image != null && !noCache) {
				return image;
			} else {
				// Load image component from database
				FormComponent formComponent = null;
				try {
					formComponent = getComponentService().getFormComponent(formID, companyID, imageFileName, FormComponentType.IMAGE);
				} catch (Exception e) {
					logger.error("Exception: " + e.getMessage(), e);
				}
	
				if (formComponent == null || formComponent.getData() == null || formComponent.getData().length == 0) {
					throw new Exception("FormImage not found");
				} else {
					// Valid imagedata found
					CachedImageData newImage = new CachedImageData();
					newImage.mimeType = formComponent.getMimeType();
					newImage.imageData = formComponent.getData();
					getImageCache().put(cacheKey, newImage);
					if (logger.isDebugEnabled()) {
						logger.debug("image added to cache: " + cacheKey);
					}
					return newImage;
				}
			}
		}
	}

	/**
	 * Write imagedata to output stream.
	 *
	 * @param request the request
	 * @param response the response
	 * @param image the image
	 */
	private void writeImageToResponse(HttpServletRequest request, HttpServletResponse response, CachedImageData image) {
		try {
			response.setContentType(image.mimeType);
			try(ServletOutputStream out = response.getOutputStream()) {
				out.write(image.imageData);
				out.flush();
			}
		} catch (ClientAbortException e) {
			logger.info("Error writing image data (client ip " + request.getRemoteAddr() + "): " + e.getMessage(), e);
		} catch (Exception e) {
			logger.error("Error writing image data: " + e.getMessage(), e);
		}
	}

	/**
	 * Generate key for lookup in cache.
	 *
	 * @param licenseID the license id
	 * @param companyID the company id
	 * @param formID the form id
	 * @param name the name
	 * @return the string
	 * @throws Exception             on missing parameters
	 */
	private String generateCachingKey(int licenseID, @VelocityCheck int companyID, int formID, String name) throws Exception {
		StringBuilder cacheKeyBuilder = new StringBuilder();
		cacheKeyBuilder.append(licenseID);
		cacheKeyBuilder.append("-");
		cacheKeyBuilder.append(companyID);
		cacheKeyBuilder.append("-");
		cacheKeyBuilder.append(formID);
		cacheKeyBuilder.append("-");
		cacheKeyBuilder.append(name);
		return cacheKeyBuilder.toString();
	}

	/**
	 * The Class CachedImageData.
	 */
	private class CachedImageData {
		/** The image data. */
		public byte[] imageData;
		
		/** The mimeType. */
		public String mimeType;
	}
}
