/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.agnitas.beans.EmmLayoutBase;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.impl.CompanyStatus;
import org.agnitas.dao.RdirTrafficAmountDao;
import org.agnitas.emm.core.commons.daocache.CompanyDaoCache;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.beans.Company;
import com.agnitas.dao.ComMailingComponentDao;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.core.mobile.service.ComDeviceService;
import com.agnitas.util.ImageUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This servlet loads and shows a image from the mailing_component_tbl.
 * The images are cached. When an image cannot be found a default image is shown which is also cached.
 * If even the default image cannot be found, a text is shown, which is also cached.
 * The caching is done for performance reasons and the caching timeout is set in emm configuration
 */
public class ShowImageServlet extends HttpServlet {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(ShowImageServlet.class);

    /**
     * Dummy object to be stored in cache for not found images.
     * This is needed, because "containsKey" may be "true", but before retrieving the value the cache might have it removed because it was outdated.
     * But we still need the information whether there was no value in the cache or there was a value but it was null(=NOT_FOUND_IMAGE).
     */
	protected static final DeliverableImage NOT_FOUND_IMAGE = new DeliverableImage();

	/** Serial version UID: */
	private static final long serialVersionUID = -595094416663851734L;

	protected ComMailingComponentDao componentDao;
	protected ComDeviceService deviceService;
	protected ImageCache imageCache;
	protected CdnCache cdnCache;
    protected RdirTrafficAmountDao rdirTrafficAmountDao;
    protected CompanyDaoCache companyDaoCache;
	protected ConfigService configService;

	// ----------------------------------------------------------------------------------------------------------------
	// Dependency Injection

	public void setImageCache(ImageCache imageCache) {
		this.imageCache = imageCache;
	}

	public void setComponentDao(ComMailingComponentDao componentDao) {
		this.componentDao = componentDao;
	}

	public void setDeviceService(ComDeviceService deviceService) {
		this.deviceService = deviceService;
	}

	public void setCompanyDaoCache(CompanyDaoCache companyDaoCache) {
		this.companyDaoCache = companyDaoCache;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	// ----------------------------------------------------------------------------------------------------------------
	// Business Logic

	/**
	 * Delivers an image to client.
	 * Once delivered images are cached.
	 * Mobile Device can have alternative images, which are also cached.
	 * If the demanded image is not available this info is also cached.
	 *
	 * We are changing the image links in html mail content, for the purpose of better performance on systems with Kaspersky AV
	 * With this change the following kind of links is allowed now:
	 * 		https://rdir.de/image/1/2270/105141/logo.jpg
	 *
	 * This is new way is optional. The old image linktype is still allowed too:
	 * 		https://rdir.de/image?ci=2270&mi=105141&name=logo.jpg
	 * or
	 * 		https://rdir.de/image?ci=2270&mi=105141&name=logo.jpg&lic=1
	 */
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (request.getAttribute("clearComShowImageCache") != null) {
                if (request.getAttribute("clearComShowImageCache").toString().equals("doClear")) {
                    getImageCache().clean();
                }
                request.removeAttribute("clearComShowImageCache");
                return;
            }


            if (logger.isDebugEnabled()) {
				logger.debug("ShowImageServlet execute start: " + new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS_MS).format(new Date()));
			}
			String companyIdString = request.getParameter("ci");
			String mailingIdString = request.getParameter("mi");
			String imageName = request.getParameter("name");

            // noCaching examples: ...&nc ...&nc= ...&nc=true ...&nc=jhg
            String noCacheString = request.getParameter("nc");
            boolean noCache = noCacheString != null && !"false".equalsIgnoreCase(noCacheString);

			/*
			 * The licenceId will not be used for now.
			 * The company_id is not unique within the various licenses.
			 * In a later version of rdir the licenceId is used for switching between the datasources which contain the images for each company.
			 */
			@SuppressWarnings("unused")
			String licenseId = request.getParameter("lic");


			if (StringUtils.isBlank(companyIdString) && StringUtils.isBlank(mailingIdString) && StringUtils.isBlank(imageName)) {
				// New parameterformat:
				// http://<rdir-domain>/image/nc/1000/1/1/Agnitas-Logo.jpg
				// http://<rdir-domain>/image/1000/1/1/Agnitas-Logo.jpg
				String[] uriParts = request.getRequestURI().split("/");
				if (uriParts.length >= 3 && AgnUtils.isNumber(uriParts[uriParts.length - 3]) && AgnUtils.isNumber(uriParts[uriParts.length - 2]) && StringUtils.isNotBlank(uriParts[uriParts.length - 1])) {
					companyIdString = uriParts[uriParts.length - 3];
					mailingIdString = uriParts[uriParts.length - 2];
					imageName = uriParts[uriParts.length - 1];
				}
				if (uriParts.length >= 4 && AgnUtils.isNumber(uriParts[uriParts.length - 4])) {
					licenseId = uriParts[uriParts.length - 4];
				}

				if (imageName != null) {
					if (imageName.contains(";")) {
						// remove ";jsessionid..."
						imageName = imageName.substring(0, imageName.indexOf(";"));
					}
					imageName = AgnUtils.decodeURL(imageName);
				}

	            noCache = uriParts.length >= 4 && "nc".equalsIgnoreCase(uriParts[uriParts.length - 4])
	            		|| uriParts.length >= 5 && "nc".equalsIgnoreCase(uriParts[uriParts.length - 5]);
			}
			
			int companyID = 0;
			if (StringUtils.isNotBlank(companyIdString)) {
				try {
					companyID = Integer.parseInt(companyIdString);
				} catch (NumberFormatException e) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
					return;
				}
				if (companyID <= 0) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
					return;
				}
			}

			String cdnBaseLink = getConfigService().getValue(ConfigValue.CdnImageRedirectLinkBase, companyID);
			if (noCache || cdnBaseLink == null) {
				// Check mandatory servlet parameters
				// On missing parameter don't show any data
				DeliverableImage image = null;
				if ((StringUtils.isNotEmpty(companyIdString) && StringUtils.isNotEmpty(mailingIdString) && StringUtils.isNotEmpty(imageName))) {
					if (getDeviceService().getDeviceClassForStatistics(request.getHeader("User-Agent")) == DeviceClass.MOBILE) {
						image = getImageForMobileRequest(request, companyID, Integer.parseInt(mailingIdString), imageName, noCache);
						if (image == null && imageName != null) {
							// Try to read image from mediapool
							int elementID;
							try {
								elementID = imageName.contains(".") ? Integer.parseInt(imageName.substring(0, imageName.lastIndexOf("."))) : Integer.parseInt(imageName);
								image = getMediapoolImage(companyID, elementID, noCache, true);
							} catch (Exception e) {
								// do nothing
							}
						}
					} else {
						image = getImageForStandardRequest(request, companyID, Integer.parseInt(mailingIdString), imageName, noCache);
						if (image == null && imageName != null) {
							// Try to read image from mediapool
							int elementID;
							try {
								elementID = imageName.contains(".") ? Integer.parseInt(imageName.substring(0, imageName.lastIndexOf("."))) : Integer.parseInt(imageName);
								image = getMediapoolImage(companyID, elementID, noCache, false);
							} catch (Exception e) {
								// do nothing
							}
						}
					}
				}
	
				if (image != null) {
					if (getConfigService().getBooleanValue(ConfigValue.ImageTrafficMeasuring, companyID)) {
						getRdirTrafficAmountDao().save(companyID, StringUtils.isNotEmpty(mailingIdString) ? Integer.parseInt(mailingIdString) : 0, image.name, image.imageData == null ? 0 : image.imageData.length);
					}
					writeImageToResponse(request, response, image);
				}
			} else {
				boolean mobile = getDeviceService().getDeviceClassForStatistics(request.getHeader("User-Agent")) == DeviceClass.MOBILE;
				int mailingID = Integer.parseInt(mailingIdString);
				String cacheKey = generateCachingKey(companyID, mailingID, (mobile ? ImageUtils.MOBILE_IMAGE_PREFIX : "") + imageName);
				
				CdnImage cdnImage = getCdnCache().get(cacheKey);
				if (cdnImage == null) {
					cdnImage = getComponentDao().getCdnImage(companyID, Integer.parseInt(mailingIdString), imageName, mobile);
					if (cdnImage == null && imageName != null) {
						// Try to read image from mediapool
						int elementID;
						try {
							elementID = imageName.contains(".") ? Integer.parseInt(imageName.substring(0, imageName.lastIndexOf("."))) : Integer.parseInt(imageName);
							cdnImage = getMediapoolCdnImage(companyID, elementID);
						} catch (Exception e) {
							// do nothing
						}
					}
					getCdnCache().put(cacheKey, cdnImage);
				}
				if (cdnImage != null) {
					response.sendRedirect(cdnBaseLink + cdnImage.cdnId);
					if (getConfigService().getBooleanValue(ConfigValue.ImageTrafficMeasuring, companyID)) {
						getRdirTrafficAmountDao().save(companyID, Integer.parseInt(mailingIdString), cdnImage.name, cdnImage.imageDatalength);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception: " + e.getMessage(), e);
			throw new ServletException("Error occurred when loading image", e);
		} finally {
			if (logger.isDebugEnabled()) {
				logger.debug("ShowImageServlet execute end: " + new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS_MS).format(new Date()));
			}
		}
	}

	/**
	 * @param companyID
	 * @param elementID
	 * @param noCache
	 * @param isMobileDevice
	 */
	protected DeliverableImage getMediapoolImage(int companyID, int elementID, boolean noCache, boolean isMobileDevice) {
		return null;
	}

	/**
	 * @param companyID
	 * @param elementID
	 * @param noCache
	 * @param isMobileDevice
	 */
	protected CdnImage getMediapoolCdnImage(int companyID, int elementID) {
		return null;
	}

	/**
	 * Look up mobile image data in cache and DB.
	 * If not found there, it will be looked up with standard name.
	 * If found there, it will be cached as mobile image too for later requests
	 *
	 * @param request
	 * @param companyID
	 * @param mailingID
	 * @param imageName
	 * @return
	 * @throws Exception
	 */
	private DeliverableImage getImageForMobileRequest(HttpServletRequest request, int companyID, int mailingID, String imageName, boolean noCache) throws Exception {
		// Generate cache lookup key for mobile image
		String mobileCacheKey = generateCachingKey(companyID, mailingID, ImageUtils.MOBILE_IMAGE_PREFIX + imageName);

		// Look up image in cache
		DeliverableImage mobileImage = getImageCache().get(mobileCacheKey);
		if (mobileImage != null && !noCache) {
			if (mobileImage == NOT_FOUND_IMAGE) {
				return null;
			} else {
				return mobileImage;
			}
		}  else {
			// Load image component from database
			MailingComponent comp = null;
			try {
				comp = getComponentDao().getMailingComponentByName(mailingID, companyID, ImageUtils.MOBILE_IMAGE_PREFIX + imageName);
			} catch (Exception e) {
				logger.error("Exception: " + e.getMessage(), e);
			}

			// Cache image data
			if (comp != null && checkValidityPeriod(comp) && comp.getBinaryBlock() != null) {
				// Valid imagedata found
				DeliverableImage newImage = new DeliverableImage();
				newImage.mtype = comp.getMimeType();
				newImage.imageData = comp.getBinaryBlock();
				newImage.name = comp.getComponentName();
				if (checkImageCacheSize(newImage)) {
					getImageCache().put(mobileCacheKey, newImage);
					if (logger.isDebugEnabled()) {
						logger.debug("mobile image added to cache: " + mobileCacheKey);
					}
				}

				return newImage;
			} else {
				DeliverableImage image = getImageForStandardRequest(request, companyID, mailingID, imageName, noCache);
				
				if (image == null) {
					getImageCache().put(mobileCacheKey, NOT_FOUND_IMAGE);
				}
				
				return image;
			}
		}
	}

	/**
	 * Look up image data in cache and DB.
	 * If found there, it will be cached.
	 * If requested as mobile image, images will be cached by their mobile name too for later mobile requests
	 *
	 * @param request
	 * @param companyID
	 * @param mailingID
	 * @param imageName
	 * @return
	 * @throws Exception
	 */
	private DeliverableImage getImageForStandardRequest(HttpServletRequest request, int companyID, int mailingID, String imageName, boolean noCache) throws Exception {
		// Generate cache lookup key
		String cacheKey = generateCachingKey(companyID, mailingID, imageName);
		// Look up image in cache
		DeliverableImage image = getImageCache().get(cacheKey); // Get image first, so it may not be removed after "containsKey"
		if (image != null && !noCache) {
			if (image == NOT_FOUND_IMAGE) {
				return null;
			} else {
				return image;
			}
		} else {
			// Load image component from database
			MailingComponent comp = null;
			try {
				comp = getComponentDao().getMailingComponentByName(mailingID, companyID, imageName);
			} catch (Exception e) {
				logger.error("Exception: " + e.getMessage(), e);
			}

			// Create newImage
			DeliverableImage newImage = null;
			if (comp != null && checkValidityPeriod(comp) && comp.getBinaryBlock() != null) {
				// Valid imagedata found
				newImage = new DeliverableImage();
				newImage.mtype = comp.getMimeType();
				newImage.imageData = comp.getBinaryBlock();
				newImage.name = comp.getComponentName();
				if (checkImageCacheSize(newImage)) {
					getImageCache().put(cacheKey, newImage);
					if (logger.isDebugEnabled()) {
						logger.debug("image added to cache: " + cacheKey);
					}
				}
			} else if (comp != null && !checkValidityPeriod(comp)) {
				// Image is outdated and replaced by a default image
				newImage = getDefaultImage(request);
				if (newImage == null) {
					// default image was not found
					newImage = NOT_FOUND_IMAGE;
					getImageCache().put(cacheKey, newImage);

					if (logger.isDebugEnabled()) {
						logger.debug("default image not found: " + cacheKey);
					}
				} else {
					// default image found
					if (checkImageCacheSize(newImage)) {
						getImageCache().put(cacheKey, newImage);
						if (logger.isDebugEnabled()) {
							logger.debug("default image added to cache: " + cacheKey);
						}
					}
				}
			} else {
				Company company = getCompanyDaoCache().getItem(companyID);
				if (company == null) {
					if (logger.isDebugEnabled()) {
						logger.debug("image not found for not existing company: " + cacheKey);
					}
				} else if (!(CompanyStatus.ACTIVE == company.getStatus())) {
					if (logger.isDebugEnabled()) {
						logger.debug("image not found for inactive company: " + cacheKey);
					}
				}
				
				newImage = NOT_FOUND_IMAGE;
				getImageCache().put(cacheKey, newImage);
			}
			
			if (newImage == NOT_FOUND_IMAGE) {
				return null;
			} else {
				return newImage;
			}
		}
	}

	/**
	 * Write imagedata to output stream
	 *
	 * @param request
	 * @param response
	 * @param image
	 */
	private void writeImageToResponse(HttpServletRequest request, HttpServletResponse response, DeliverableImage image) {
		try {
			response.setContentType(image.mtype);
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

	private DeliverableImage getDefaultImage(HttpServletRequest req) throws Exception {
		//Load a default image, which should be displayed, when the validity of the image has expired
		byte[] defaultImageData;
		try {
			String realPath = req.getSession().getServletContext().getRealPath("/");
			EmmLayoutBase aLayout = (EmmLayoutBase) req.getSession().getAttribute("emmLayoutBase");
			String imageUrl = realPath + aLayout.getImagesURL() + "/grid_expire_image.png";
			try {
				defaultImageData = FileUtils.readFileToByteArray(new File(imageUrl));
			} catch (Exception e) {
				logger.error( "Error reading data for default image: " + imageUrl, e);
				defaultImageData = null;
			}
		} catch( Exception e) {
			logger.error( "Error reading data for default image", e);
			defaultImageData = null;
		}
		DeliverableImage newImage = new DeliverableImage();
		if (defaultImageData == null) {
			// Default image data was not found so write some text
			newImage.mtype = "text/html";
			newImage.imageData = "Defaultimage not found".getBytes("UTF-8");
			newImage.name = "";
		} else {
			// Use the default image
			newImage.mtype = "image/png";
			newImage.imageData = defaultImageData;
			newImage.name = "grid_expire_image.png";
		}
		return newImage;
	}

	/**
	 * Generate key for lookup in cache
	 *
	 * @param companyID
	 * @param mailingID
	 * @param name
	 * @return
	 * @throws Exception
	 *             on missing parameters
	 */
	private String generateCachingKey(int companyID, int mailingID, String name) throws Exception {
		StringBuilder cacheKeyBuilder = new StringBuilder();
		cacheKeyBuilder.append(companyID);
		cacheKeyBuilder.append("-");
		cacheKeyBuilder.append(mailingID);
		cacheKeyBuilder.append("-");
		cacheKeyBuilder.append(name);
		return cacheKeyBuilder.toString();
	}

	/**
	 * Check if the optional validity period is valid for now
	 *
	 * @param comp
	 * @return
	 */
	private boolean checkValidityPeriod(MailingComponent comp) {
		Date now = new Date();
		if (comp.getStartDate() != null && now.before(comp.getStartDate())) {
			return false;
		} else if (comp.getEndDate() != null && now.after(comp.getEndDate())) {
			return false;
		}
		return true;
	}

	protected boolean checkImageCacheSize(DeliverableImage image) {
		if (image != null) {
			int maxSize = getConfigService().getIntegerValue(ConfigValue.MaximumCachedImageSize);
			return image.imageData != null && image.imageData.length <= maxSize;
		} else {
			return true;
		}
	}

	/**
	 * Return existing cache or create new one
	 */
	public ImageCache getImageCache() {
		if (imageCache == null) {
			imageCache = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("ImageCache", ImageCache.class);
		}

		return imageCache;
	}

	/**
	 * Return existing cache or create new one
	 */
	private CdnCache getCdnCache() {
		if (cdnCache == null) {
			cdnCache = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("CdnCache", CdnCache.class);
		}

		return cdnCache;
	}

	/**
	 * @return the componentDao
	 */
	private ComMailingComponentDao getComponentDao() {
		if (componentDao == null) {
			componentDao = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("MailingComponentDao", ComMailingComponentDao.class);
		}
		return componentDao;
	}

	/**
	 * @return the deviceService
	 */
	private ComDeviceService getDeviceService() {
		if (deviceService == null) {
			deviceService = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("DeviceService", ComDeviceService.class);
		}
		return deviceService;
	}

    private RdirTrafficAmountDao getRdirTrafficAmountDao() {
		if (rdirTrafficAmountDao == null) {
			rdirTrafficAmountDao = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("RdirTrafficAmountDao", RdirTrafficAmountDao.class);
		}
		return rdirTrafficAmountDao;
	}

    private CompanyDaoCache getCompanyDaoCache() {
		if (companyDaoCache == null) {
			companyDaoCache = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("CompanyDaoCache", CompanyDaoCache.class);
		}
		return companyDaoCache;
	}

    private ConfigService getConfigService() {
		if (configService == null) {
			configService = WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("ConfigService", ConfigService.class);
		}
		return configService;
	}
}
