/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.MailingComponentType;
import org.agnitas.util.NetworkUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.jakartaee.commons.io.IOUtils;

import com.agnitas.util.ImageUtils;

public class MailingComponentImpl implements MailingComponent {
	
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger( MailingComponentImpl.class);

	/** Maximum length of component name. Must match column size in database. */
	public static final int COMPONENT_NAME_MAX_LENGTH = 500;
	
	protected int id;

	protected String mimeType;

	protected String componentName;

	protected String description;

	protected int mailingID;

	protected int companyID;

	protected MailingComponentType type;

	protected String emmBlock;

	protected byte[] binaryBlock;

	protected Date timestamp;
	
	protected String link;
	
	protected int urlID;

	protected int targetID;

    protected Date startDate;

    protected Date endDate;

	private int present;

	public MailingComponentImpl() {
		id = 0;
		componentName = null;
		mimeType = " ";
		mailingID = 0;
		companyID = 0;
		type = MailingComponentType.Image;
		emmBlock = null;
		targetID = 0;
	}

	@Override
	public void setComponentName(String componentName) {
		if (componentName != null) {
			this.componentName = componentName;
		} else {
			this.componentName = "";
		}
	}

	@Override
	public void setType(MailingComponentType type) {
		if (type == null) {
			this.type = MailingComponentType.Image;
		} else {
			this.type = type;
		}
	}

	@Override
	public void setPresent(int present) {
		this.present = present;
	}

	@Override
	public void setId(int componentID) {
		id = componentID;
	}

    @Override
	public String getComponentNameUrlEncoded() {
        try {
            return URLEncoder.encode(getComponentName(), "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            return getComponentName();
        }
    }

	@Override
	public String getComponentName() {
		if (componentName != null) {
			return componentName;
		}

		return "";
	}

	@Override
	public void setMailingID(int mailingID) {
		this.mailingID = mailingID;
	}

	@Override
	public void setCompanyID(int companyID) {
		this.companyID = companyID;
	}

	@Override
	public void setEmmBlock(String emmBlock, String mimeType) {
		// Only store one of type of data: emmblock or binblock
		// Exception: Personalized PDF attachments require emmblock and binblock to be filled with different files
		// Clear datatype only if this is the only set datatype
		// binblock sometimes contains an array "byte[1] = {0}", which also signals empty binary data
		if (StringUtils.isNotEmpty(emmBlock) || (binaryBlock == null || binaryBlock.length <= 1) || "application/pdf".equalsIgnoreCase(mimeType)) {
			this.emmBlock = emmBlock;
			// binaryBlock = null;
			if (mimeType != null) {
				this.mimeType = mimeType;
			} else {
				// TODO EMM-6740: Added this try-catch to get stack trace, if no mimetype is given
				try {
					throw new Exception("setBinaryBlock() called with no Mimetype");
				} catch(final Exception e) {
					logger.error("setBinaryBlock() called with no Mimetype", e);
				}
				
				this.mimeType = "unknown";
			}
		}
	}

	@Override
	public void setBinaryBlock(byte[] binaryBlock, String mimeType) {
		// Only store one of type of data: emmblock or binblock
		// Exemption: Personalized PDF attachments require emmblock and binblock to be filled with different files
		// Clear datatype only if this is the only set datatype
		// binblock sometimes contains an array "byte[1] = {0}", which also signals empty binary data
		if ((binaryBlock != null && binaryBlock.length > 1) || StringUtils.isEmpty(emmBlock) || "application/pdf".equalsIgnoreCase(mimeType)) {
			this.binaryBlock = binaryBlock;
			// emmBlock = null;
			if (mimeType != null) {
				this.mimeType = mimeType;
			} else {
				// TODO EMM-6740: Added this try-catch to get stack trace, if no mimetype is given
				try {
					throw new Exception("setBinaryBlock() called with no Mimetype");
				} catch(final Exception e) {
					logger.error("setBinaryBlock() called with no Mimetype", e);
				}
				
				this.mimeType = "unknown";
			}
		}
	}

	@Override
	public boolean loadContentFromURL() {
		if ((type != MailingComponentType.Image) && (type != MailingComponentType.Attachment)) {
			return false;
		}

		final String encodedURI = encodeURI(componentName);
		
		final RequestConfig.Builder requestConfigBuilder = RequestConfig
				.custom()
				.setConnectTimeout(5000);
		
		final HttpClientBuilder httpClientBuilder = HttpClients.custom()
				.setDefaultRequestConfig(requestConfigBuilder.build());
		
		try(final CloseableHttpClient httpClient = httpClientBuilder.build()) {
			final HttpGet request = new HttpGet(encodedURI);
			
			NetworkUtil.setHttpClientProxyFromSystem(request, encodedURI);
			request.setHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.54 Safari/537.36");	// TODO Use configured user agent
			
			try(final CloseableHttpResponse httpResponse = httpClient.execute(request)) {
				final int httpStatusCode = httpResponse.getStatusLine().getStatusCode();
				
				if(httpStatusCode == HttpStatus.SC_OK) {
					final org.apache.http.Header contentTypeHeader = httpResponse.getFirstHeader("Content-Type");
					final String contentTypeValue = contentTypeHeader != null ? contentTypeHeader.getValue() : "";
					
					final HttpEntity responseEntity = httpResponse.getEntity();
					
					try(final InputStream in = responseEntity.getContent()) {
						try(final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
							IOUtils.copy(in, out);

							setBinaryBlock(out.toByteArray(), contentTypeValue);
							
							return true;
						}
					}
				}
			}
		} catch(final Throwable e) {
			logger.error(String.format("Error loading content from url '%s'", encodedURI), e);
		}
		
		return false;
	}

	@Override
	public String getEmmBlock() {
		return emmBlock;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Getter for property targetID.
	 * 
	 * @return Value of property targetID.
	 */
	@Override
	public int getTargetID() {
		return targetID;
	}

	/**
	 * Setter for property targetID.
	 * 
	 * @param targetID
	 *            New value of property targetID.
	 */
	@Override
	public void setTargetID(int targetID) {
		this.targetID = targetID;
	}

	/**
	 * Getter for property type.
	 * 
	 * @return Value of property type.
	 */
	@Override
	public MailingComponentType getType() {
		return type;
	}

	@Override
	public int getPresent() {
		return present;
	}

	/**
	 * Getter for property binaryBlock.
	 * 
	 * @return Value of property binaryBlock.
	 * 
	 */
	@Override
	public byte[] getBinaryBlock() {
		return binaryBlock;
	}

	/**
	 * Getter for property mailingID.
	 * 
	 * @return Value of property mailingID.
	 */
	@Override
	public int getMailingID() {
		return mailingID;
	}

	/**
	 * Getter for property companyID.
	 * 
	 * @return Value of property companyID.
	 */
	@Override
	public int getCompanyID() {
		return companyID;
	}

	/**
	 * Getter for property timestamp.
	 * 
	 * @return Value of property timestamp.
	 */
	@Override
	public Date getTimestamp() {
		return timestamp;
	}

	@Override
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String getLink() {
		return link;
	}

	@Override
	public void setLink(String link) {
		this.link = link;
	}

	@Override
	public int getUrlID() {
		return urlID;
	}

	@Override
	public void setUrlID(int urlID) {
		this.urlID = urlID;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

    @Override
	public Date getStartDate() {
        return startDate;
    }

    @Override
	public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Override
	public Date getEndDate() {
        return endDate;
    }

    @Override
	public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
	
	@Override
	public boolean isSourceComponent() {
		return type == MailingComponentType.Image ||
				type == MailingComponentType.HostedImage &&
						!isMobileImage();
	}
	
	@Override
	public boolean isMobileImage() {
		return ImageUtils.isMobileImage(componentName);
	}
	
	/**
	 * This method encodes some parts of a URI. If in the given URI a "[", "]", "{" or "}" are found, they
	 * will be replaced by appropriate HEX-Identifiers.
	 * See here for more information:
	 * http://www.ietf.org/rfc/rfc3986.txt
	 * http://stackoverflow.com/questions/40568/square-brackets-in-urls
	 * http://www.blooberry.com/indexdot/html/topics/urlencoding.htm
	 * 
	 * @return "cleaned" URI
     * @throws Exception
	 */
	private String encodeURI(String uri) {
		// TODO Replace this version with a more generic approach. Now only one special
		// case is fixed. This method should convert ALL special characters.
		
		/*
		 * Note: Using a generic method to URL-encode a String may lead to another problem:
		 * The URLs found in the mailing may already be URL encoded (irrespective to some
		 * of the dirty things, that got common practice, like "{" or "}" in URLs), so we
		 * URL-encode an URL-encoded URI.
		 * 
		 * This method should simply "clean" the given URL/URI and do no further encoding.
		 * TODO: In this case, renaming of the method would be a great deal!
		 */
		
		uri = uri.replace("[", "%5B");
		uri = uri.replace("]", "%5D");
		uri = uri.replace("{", "%7B");
		uri = uri.replace("}", "%7D");
		uri = uri.replace("|", "%7C");
		
		return uri;
	}
}
