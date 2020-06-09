/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.agnitas.util.Caret;
import org.agnitas.beans.TagDetails;
import org.agnitas.beans.impl.TagDetailsImpl;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.uid.builder.impl.exception.RequiredInformationMissingException;
import org.agnitas.emm.core.commons.uid.builder.impl.exception.UIDStringBuilderException;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.NetworkUtil;
import org.agnitas.util.PubID;
import org.agnitas.util.TimeoutLRUMap;
import org.agnitas.util.UnclosedTagException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComCompany;
import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.LinkProperty.PropertyType;
import com.agnitas.beans.impl.ComTrackableLinkImpl;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.commons.uid.UIDFactory;
import com.agnitas.emm.core.hashtag.HashTag;
import com.agnitas.emm.core.hashtag.HashTagContext;
import com.agnitas.emm.core.hashtag.service.HashTagEvaluationService;
import com.agnitas.emm.core.mailing.bean.ComMailingParameter;
import com.agnitas.emm.core.mailing.dao.ComMailingParameterDao;
import com.agnitas.emm.grid.grid.beans.GridCustomPlaceholderType;
import com.agnitas.service.AgnTagService;
import com.agnitas.util.DeepTrackingToken;
import com.agnitas.util.backend.Decrypt;

public class LinkServiceImpl implements LinkService {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(LinkServiceImpl.class);

	private static final Pattern HASHTAG_PATTERN = Pattern.compile("##([^#]+)##");
	private static final Pattern AGNTAG_PATTERN = Pattern.compile("\\[agn[^\\]]+]");
	private static final Pattern GRIDTAG_PATTERN = Pattern.compile("\\[gridPH[^\\]]+]");
	private static final Pattern DOCTYPE_PATTERN = Pattern.compile("<!DOCTYPE [^>]*>", Pattern.CASE_INSENSITIVE);
	private static final Pattern ACTIONID_PATTERN = Pattern.compile("\\s+actionid\\s*=\\s*((\"[0-9]+\")|('[0-9]+')|([0-9]+))", Pattern.CASE_INSENSITIVE);
	private static final Pattern TITLE_PATTERN = Pattern.compile("\\s+title\\s*=\\s*((\"[ 0-9A-Z_.+-]+\")|('[ 0-9A-Z_.+-]+')|([0-9A-Z_.+-]+))", Pattern.CASE_INSENSITIVE);
	private static final String RDIRLINK_SEARCH_STRING = "r.html?uid=";
	private static final Pattern INVALID_HREF_WITH_WHITESPACE_PATTERN = Pattern.compile("\\shref\\s*=\\s*(\"|')\\s", Pattern.CASE_INSENSITIVE);
	private static final Pattern INVALID_SRC_WITH_WHITESPACE_PATTERN = Pattern.compile("\\ssrc\\s*=\\s*(\"|')\\s", Pattern.CASE_INSENSITIVE);
	
	/**
	 * Http and Https url pattern.
	 * "https?://" = mandatory protocol part
	 * "[0-9A-Za-z_.+-]+" = mandatory hostname part
	 * "(:[0-9]+)?" = optional port part
	 * "(/[^ \t\n\r<>\"']*)?" = optional context part
	 */
//	private static final Pattern URL_PATTERN = Pattern.compile("https?://[0-9A-Za-z_.+-]+(:[0-9]+)?(/[^ \t\n\r>\"']*)?");

	private static final Pattern PROTOCOL_SCHEMA_PATTERN = Pattern.compile("^(\\p{Alpha}+):.*$");
	private static final Pattern HTTP_PATTERN = Pattern.compile("https?://[0-9A-Z_.+-]+(:[0-9]+)?", Pattern.CASE_INSENSITIVE);

	private static final Pattern URL_DETECTION_PATTERN = Pattern.compile("((?:href|src|background)=)|(https?://[0-9A-Z_.+-]+(:[0-9]+)?)", Pattern.CASE_INSENSITIVE);
	
	private ConfigService configService;
	
	private ExtensibleUIDService extensibleUIDService;

	private AgnTagService agnTagService;

	private ComCompanyDao companyDao;
	
	private ComMailingDao mailingDao;
	
	private ComMailingParameterDao mailingParameterDao;
	
	private HashTag unencodedProfileFieldHashTag;
	
	private HashTagEvaluationService hashTagEvaluationService;
	

	/**
	 * Map cache from mailingID to baseUrl
	 */
	private TimeoutLRUMap<Integer, String> baseUrlCache = null;

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Required
	public void setExtensibleUIDService(ExtensibleUIDService extensibleUIDService) {
		this.extensibleUIDService = extensibleUIDService;
	}

	@Required
	public void setAgnTagService(AgnTagService agnTagService) {
		this.agnTagService = agnTagService;
	}

	@Required
	public void setCompanyDao(ComCompanyDao companyDao) {
		this.companyDao = companyDao;
	}
	
	@Required
	public void setMailingDao(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}
	
	@Required
	public void setMailingParameterDao(ComMailingParameterDao mailingParameterDao) {
		this.mailingParameterDao = mailingParameterDao;
	}
	
	@Required
	public final void setUnencodedProfileFieldHashTag(final HashTag hashTag) {
		this.unencodedProfileFieldHashTag = hashTag;
	}
	
	@Required
	public final void setHashTagEvaluationService(final HashTagEvaluationService service) {
		this.hashTagEvaluationService = Objects.requireNonNull(service, "HashTagEvaluationSerice is null");
	}
	
	private TimeoutLRUMap<Integer, String> getBaseUrlCache() {
		if (baseUrlCache == null) {
			baseUrlCache = new TimeoutLRUMap<>(configService.getIntegerValue(ConfigValue.RedirectKeysMaxCache), configService.getIntegerValue(ConfigValue.RedirectKeysMaxCacheTimeMillis));
		}
		return baseUrlCache;
	}

	@Override
	public String personalizeLink(final ComTrackableLink link, final String agnUidString, final int customerID, final String referenceTableRecordSelector, final boolean applyLinkExtensions, final String encryptedStaticValueMap) {
		final String fullUrlWithHashTags = link.getFullUrl();
		final Map<String, Object> staticValueMap = link.isStaticValue() ? decryptStaticValueMap(link.getCompanyID(), customerID, encryptedStaticValueMap) : Collections.emptyMap();

		String fullUrl = replaceHashTags(link, agnUidString, customerID, referenceTableRecordSelector, fullUrlWithHashTags, staticValueMap);
		
		if (applyLinkExtensions) {
			if (link.getProperties() != null && link.getProperties().size() > 0) {
				// Create redirect Url with new extensions
				for (LinkProperty linkProperty : link.getProperties()) {
					if (linkProperty.getPropertyType() == PropertyType.LinkExtension) {
						String propertyName = replaceHashTags(link, agnUidString, customerID, referenceTableRecordSelector, linkProperty.getPropertyName(), staticValueMap);
						String propertyValue = replaceHashTags(link, agnUidString, customerID, referenceTableRecordSelector, linkProperty.getPropertyValue(), staticValueMap);
						
						// Extend link properly (watch out for html-anchors etc.)
						try {
							// UrlEncoding is done separately before to prevent ##-tags from becomming encoded (=> encodingCharSet = null)
							fullUrl = AgnUtils.addUrlParameter(fullUrl, propertyName, propertyValue == null ? "" : propertyValue, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							throw new RuntimeException("Cannot add link extension: " + e.getMessage(), e);
						}
					}
				}
			}
		}
		
		return fullUrl;
	}
	
	private final Map<String, Object> decryptStaticValueMap(final int companyID, final int customerID, final String encryptedStaticValueMap) {
		final ComCompany company = this.companyDao.getCompany(companyID);
		
		try {
			final Decrypt decrypt = new Decrypt(company.getSecretKey());
			final Map<String, Object> staticValueMap = decrypt.decryptAndDecode(encryptedStaticValueMap, customerID);
	
			return staticValueMap;
		} catch(final Exception e) {
			logger.error("Error decrypting static value map", e);
			
			return Collections.emptyMap();
		}
	}

	private String replaceHashTags(final ComTrackableLink link, final String agnUidString, final int customerID, final String referenceTableRecordSelector, final String textWithHashTags, final Map<String, Object> staticValueMap) {
		Matcher matcher = HASHTAG_PATTERN.matcher(textWithHashTags);
		StringBuffer returnLinkString = new StringBuffer(textWithHashTags.length());
		
		PubID pubid = null;

		
		// Find all hash-tags
		int currentPosition = 0;
		while (matcher.find(currentPosition)) {
			String hashTagContent = matcher.group(1);
			int hashTagStart = matcher.start();
			int hashTagEnd = matcher.end();
			String hashTagReplacement;
			
			final HashTagContext hashTagContext = new HashTagContext(link, customerID, agnUidString, referenceTableRecordSelector, staticValueMap);

			// Prefix the prolog of the url, which does not contain other hash-tags
			if (currentPosition < hashTagStart) {
				returnLinkString.append(textWithHashTags.substring(currentPosition, hashTagStart));
			}
			currentPosition = hashTagEnd;
			
			if (hashTagContent.equals("AGNUID")) {
				hashTagReplacement = agnUidString;
			} else if (hashTagContent.equals("MAILING_ID")) {
				hashTagReplacement = Integer.toString(link.getMailingID());
			} else if (hashTagContent.equals("URL_ID")) {
				hashTagReplacement = Integer.toString(link.getId());
			} else if (hashTagContent.equals("PUBID") || hashTagContent.startsWith("PUBID:")) {
				String[] parts = hashTagContent.split(":", 3);

				if (pubid == null) {
					pubid = new PubID();
					pubid.setMailingID(link.getMailingID());
					pubid.setCustomerID(customerID);
				}
				
				if (parts.length > 1) {
					pubid.setSource(parts[1].length() > 0 ? parts[1] : null);
					if (parts.length > 2) {
						pubid.setParm(parts[2]);
					} else {
						pubid.setParm(null);
					}
				} else {
					pubid.setSource(null);
					pubid.setParm(null);
				}
				
				hashTagReplacement = pubid.createID();
			} else if (hashTagContent.startsWith("SENDDATE-UNENCODED:")) {
				String format = hashTagContent.substring(hashTagContent.indexOf(':') + 1);
				
				try {
					Date sendDate = mailingDao.getSendDate(link.getCompanyID(), link.getMailingID());
					if (sendDate != null) {
						// Legacy: old examples in documentation said "mm" stands for month-2digits (db convention), so it must be replaced for Java format conventions into "MM"
						// Legacy: old examples in documentation said "DD" stands for day-2digits (db convention), so it must be replaced for Java format conventions into "dd"
						hashTagReplacement = new SimpleDateFormat(format.replace("mm", "MM").replace("DD", "dd")).format(sendDate);
					} else {
						hashTagReplacement = "";
					}
				} catch (Exception e) {
					hashTagReplacement = "";
				}
				
				if (hashTagReplacement == null) {
					hashTagReplacement = "";
				}
			} else if (hashTagContent.startsWith("MAILING:") || hashTagContent.startsWith("MAILING-UNENCODED:")) {
				try {
					String paramName = hashTagContent.substring(hashTagContent.indexOf(':') + 1);
					ComMailingParameter parameter = mailingParameterDao.getParameterByName(paramName, link.getMailingID(), link.getCompanyID());
					
					if (parameter != null && parameter.getValue() != null) {
						hashTagReplacement = parameter.getValue();
						
						// If tag is "MAILING:" then URL-encode content
						if (hashTagContent.startsWith("MAILING:")) {
							hashTagReplacement = URLEncoder.encode(hashTagReplacement, "UTF8");
						}
					} else {
						hashTagReplacement = "";
					}
				} catch (Exception e) {
					logger.warn("Error processing ##MAILING:...#", e);
					hashTagReplacement = "";
				}
			} else if (hashTagContent.startsWith("UNENCODED:")) {	  // TODO: Replace this with a registry, that selects the correct hash tag. (-> AGNEMM-2731)
				hashTagReplacement = executeHashTag(this.unencodedProfileFieldHashTag, hashTagContent, hashTagContext);
			} else {
				// Handle hash tag by registry and evaluation service here
				try {
					hashTagReplacement = this.hashTagEvaluationService.evaluateHashTag(hashTagContext, hashTagContent);
				} catch(final Exception e) {
					logger.error("Error processing tag string: " + hashTagContent + " (company " + hashTagContext.getCompanyID() + ", link " + hashTagContext.getCurrentTrackableLink().getId() + ")", e);

					hashTagReplacement = "";
				}
				
            	try {
            		hashTagReplacement = URLEncoder.encode(hashTagReplacement, "UTF8");
            	} catch(UnsupportedEncodingException e) {
            		logger.error("Error while URL-encoding", e);
            		hashTagReplacement = "";
            	}
			}
			
			if (hashTagReplacement != null) {
				returnLinkString.append(hashTagReplacement);
			}
		}
		
		if (currentPosition < textWithHashTags.length()) {
			// Append the rest of the url, which does not contain other hash-tags
			returnLinkString.append(textWithHashTags.substring(currentPosition));
		}
		
		return returnLinkString.toString();
	}

	@Deprecated // Removed without replacement
	private static final String executeHashTag(final HashTag tag, final String tagString, final HashTagContext context) {
		try {
			return tag.handle(context, tagString);
		} catch(final Exception e) {
			logger.error("Error processing tag string: " + tagString + " (company " + context.getCompanyID() + ", link " + context.getCurrentTrackableLink().getId() + ", tag class " + tag.getClass().getCanonicalName() + ")", e);
			return "";
		}
	}
	
	/**
	 * Scan a text for http and https links.
	 * First list includes simple trackable links.
	 * Second list includes image links.
	 * Third list includes simple NOT trackable links.
	 * DOCTYPE-Links will be ignored.
	 * Link strings may include Agnitas specific HashTags (##tagname: parameter='list'##).
	 * 
	 * AgnTag [agnPROFILE] will be resolved.
	 * AgnTag [agnUNSUBSCRIBE] will be resolved.
	 * AgnTag [agnFORM] will be resolved.
	 * @throws Exception
	 */
	// TODO: Check availablility and mimetype of image links
	@Override
	public LinkScanResult scanForLinks(String text, int mailingID, int mailinglistID, int companyID) throws Exception {
		if (StringUtils.isBlank(text)) {
			return new LinkScanResult();
		}
		
		return scanForLinks(resolveAgnTags(text, mailingID, mailinglistID, companyID), companyID);
	}

	/**
	 * Scan a text for http and https links.
	 * First list includes simple trackable links.
	 * Second list includes image links.
	 * Third list includes simple NOT trackable links.
	 * DOCTYPE-Links will be ignored.
	 * Link strings may include Agnitas specific HashTags (##tagname: parameter='list'##).
	 *
	 * AgnTag [agnPROFILE] ARE NOT resolved.
	 * AgnTag [agnUNSUBSCRIBE] ARE NOT resolved.
	 * AgnTag [agnFORM] ARE NOT resolved.
	 * @throws Exception
	 */
	// TODO: Check availablility and mimetype of image links
	@Override
	public LinkScanResult scanForLinks(String textWithDoc, int companyID) throws Exception {
		if (StringUtils.isBlank(textWithDoc)) {
			return new LinkScanResult();
		}

		// Remove doctype-tag for scan
		Matcher aMatch = DOCTYPE_PATTERN.matcher(textWithDoc);
		final String text = aMatch.find() ? textWithDoc.substring(aMatch.end()) : textWithDoc;
		String textWithReplacedHashTags = getTextWithReplacedAgnTags(text, "x");

		final List<ComTrackableLink> foundTrackableLinks = new ArrayList<>();
		final List<String> foundImages = new ArrayList<>();
		final List<String> foundNotTrackableLinks = new ArrayList<>();
		final List<ErrorneousLink> foundErrorneousLinks = new ArrayList<>();
		final List<ErrorneousLink> localLinks = new ArrayList<>();

		try {
			final String finalTextWithReplacedHashTags = textWithReplacedHashTags;
			findAllLinks(companyID, textWithReplacedHashTags, (start, end) -> {
				final String linkUrl = text.substring(start, end).trim();

				final Matcher schemaMatcher = PROTOCOL_SCHEMA_PATTERN.matcher(linkUrl);
				final String schema = schemaMatcher.matches() ? schemaMatcher.group(1) : null;
				
				if (schema != null && schema.length() < 2) { // Schema present, but length too short -> Treat as local
					localLinks.add(new ErrorneousLink("error.mailing.url.local", start, linkUrl));
				} else if (schema != null && ("file".equalsIgnoreCase(schema))) {	// "file:" is always local
					localLinks.add(new ErrorneousLink("error.mailing.url.local", start, linkUrl));
				} else if (schema != null && !"http".equalsIgnoreCase(schema) && !"https".equalsIgnoreCase(schema)) {
					// Skip links with schema no starting with "http:" or "https:"
				} else if (finalTextWithReplacedHashTags.substring(start, end).trim().contains(" ")) {
					// HASH-Tags and AgnTags may contain blanks, but the resulting html link with the replaced tags may not
					foundErrorneousLinks.add(new ErrorneousLink("error.mailing.url.blank", start, linkUrl));
				} else {
					// Check for image link or normal href link
					if (AgnUtils.checkPreviousTextEquals(text, start, "src=", ' ', '\'', '"', '\n', '\r', '\t')
							|| AgnUtils.checkPreviousTextEquals(text, start, "background=", ' ', '\'', '"', '\n', '\r', '\t')
							|| AgnUtils.checkPreviousTextEquals(text, start, "url(", ' ', '\'', '"', '\n', '\r', '\t')) {
						if (HASHTAG_PATTERN.matcher(linkUrl).find()) {
							// No HASH-Tags in image links allowed
							foundErrorneousLinks.add(new ErrorneousLink("error.mailing.imagelink.hash", start, linkUrl));
						} else {
							if (isHttpUrl(linkUrl.toLowerCase())) {
								foundImages.add(linkUrl);
							} else if (containsDynTag(linkUrl)) {
								foundImages.add(linkUrl);
							} else {
								localLinks.add(new ErrorneousLink("error.mailing.url.local", start, linkUrl));
							}
						}
					} else {
						if (containsDynTag(linkUrl)) {
							foundNotTrackableLinks.add(linkUrl);
						} else if (isHttpUrl(linkUrl.toLowerCase())) {
							ComTrackableLink link = new ComTrackableLinkImpl();
							link.setFullUrl(linkUrl);
							link.setActionID(getActionIdForLink(text, start, linkUrl));
							link.setAltText(getTitleForLink(text, start, linkUrl));
							link.setShortname(getTitleForLink(text, start, linkUrl));
							foundTrackableLinks.add(link);
						} else if (!linkUrl.startsWith("#")) {
							localLinks.add(new ErrorneousLink("error.mailing.url.local", start, linkUrl));
						}
					}
				}
			});
		} catch (RuntimeException e) {
			throw new Exception(e);
		}

		return new LinkScanResult(foundTrackableLinks, foundImages, foundNotTrackableLinks, foundErrorneousLinks, localLinks);
	}

	private boolean isHttpUrl(String url) {
		return url.startsWith("http://") || url.startsWith("https://");
	}

	private boolean containsDynTag(String url) {
		return url.contains("[agn") || url.contains("[gridPH");
	}

	@Override
	// TODO EMM-6128: After complete rollout, replace this method by findAllLinksNews
	public final void findAllLinks(final int companyID, String text, BiConsumer<Integer, Integer> consumer) {
		if(configService.getBooleanValue(ConfigValue.Linkchecker_LocalUrlDetection, companyID)) {
			findAllLinksNew(text, consumer);
		} else {
			findAllLinksLegacy(text, consumer);
		}
	}
	
	// TODO EMM-6128: After complete rollout, rename to "findAllLinks"
	private final void findAllLinksNew(final String text, final BiConsumer<Integer, Integer> consumer) {
		int position = 0;
		final Matcher matcher = URL_DETECTION_PATTERN.matcher(text);
		while (matcher.find(position)) {
			int end; // Position of the last character of the URL (quotes not included)
			int start = matcher.start(); // Position of the first character of the URL (quotes not included)
			
			if(matcher.group(1) != null) { // Matches starting with "src=", "href=" or "background="
				final int attributeLength = matcher.group(1).length();
				start += attributeLength;	// Skip attribute
							
				switch(text.charAt(start)) {
				case '\'':
					start++; // Skip quote
					end = text.indexOf('\'', start);
					break;
					
				case '"':
					start++;	// Skip quote
					end = text.indexOf('\"', start);
					break;
					
				default:
					end = AgnUtils.getNextIndexOf(text, matcher.start(), ' ', '>', '\n', '\r', '\t');
					if (end == -1) {
						// Link does not end before eof
						end = text.length();
					}
					break;
				}
				
				if (end < 0) {
					throw createParseException("Missing closing apostrophe char for link url at position: ", matcher, text);
				}
			} else { // Matches starting with http:// or https://
				final char previousChar = AgnUtils.getCharacterBefore(text, matcher.start());
				if (previousChar == 0) {
					end = AgnUtils.getNextIndexOf(text, matcher.start(), ' ', '>', '\n', '\r', '\t');
					if (end == -1) {
						// Link does not end before eof
						end = text.length();
					}
				} else if (previousChar == '\'') {
					end = text.indexOf('\'', matcher.start());
					if (end < 0) {
						throw createParseException("Missing closing apostrophe char for link url at position: ", matcher, text);
					}
				} else if (previousChar == '"') {
					end = text.indexOf('"', matcher.start());
					if (end < 0) {
						throw createParseException("Missing closing quote char for link url at position: ", matcher, text);
					}
				} else if (previousChar == '(') {
					end = text.indexOf(')', matcher.start());
					if (end < 0) {
						throw createParseException("Missing closing bracket char for link url at position: ", matcher, text);
					}
				} else if (previousChar == '>') {
					end = text.indexOf('<', matcher.start());
					if (end < 0) {
						throw createParseException("Missing enclosing tag end for link url at position: ", matcher, text);
					}
				} else {
					end = AgnUtils.getNextIndexOf(text, matcher.start(), ' ', '>', '\n', '\r', '\t');
					if (end == -1) {
						// Link does not end before eof
						end = text.length();
					}
				}
			}

			consumer.accept(start, end);

			if (end >= text.length()) {
				return;
			} else {
				position = end;
			}
		}
	}

	// TODO EMM-6128: After complete rollout, remove this method
	private final void findAllLinksLegacy(final String text, final BiConsumer<Integer, Integer> consumer) {
		int position = 0;
		Matcher matcher = HTTP_PATTERN.matcher(text);
		while (matcher.find(position)) {
			int end;
			char previousChar = AgnUtils.getCharacterBefore(text, matcher.start());
			if (previousChar == 0) {
				end = AgnUtils.getNextIndexOf(text, matcher.start(), ' ', '>', '\n', '\r', '\t');
				if (end == -1) {
					// Link does not end before eof
					end = text.length();
				}
			} else if (previousChar == '\'') {
				end = text.indexOf('\'', matcher.start());
				if (end < 0) {
					throw createParseException("Missing closing apostrophe char for link url at position: ", matcher, text);
				}
			} else if (previousChar == '"') {
				end = text.indexOf('"', matcher.start());
				if (end < 0) {
					throw createParseException("Missing closing quote char for link url at position: ", matcher, text);
				}
			} else if (previousChar == '(') {
				end = text.indexOf(')', matcher.start());
				if (end < 0) {
					throw createParseException("Missing closing bracket char for link url at position: ", matcher, text);
				}
			} else if (previousChar == '>') {
				end = text.indexOf('<', matcher.start());
				if (end < 0) {
					throw createParseException("Missing enclosing tag end for link url at position: ", matcher, text);
				}
			} else {
				end = AgnUtils.getNextIndexOf(text, matcher.start(), ' ', '>', '\n', '\r', '\t');
				if (end == -1) {
					// Link does not end before eof
					end = text.length();
				}
			}

			consumer.accept(matcher.start(), end);

			if (end >= text.length()) {
				return;
			} else {
				position = end;
			}
		}
	}

	/**
	 * Replace agnTags
	 * 
	 * AgnTag [agnPROFILE] will be resolved.
	 * AgnTag [agnUNSUBSCRIBE] will be resolved.
	 * AgnTag [agnFORM] will be resolved.
	 * 
	 * @param text
	 * @param mailingID
	 * @param mailinglistID
	 * @param companyID
	 * @return
	 */
	@Override
	public String resolveAgnTags(String text, int mailingID, int mailinglistID, int companyID) {
		if (text.contains("[agnPROFILE]")) {
			TagDetails tag = new TagDetailsImpl();
			tag.setTagName("agnPROFILE");
			text = text.replaceAll("\\[agnPROFILE\\]", agnTagService.resolve(tag, companyID, mailingID, mailinglistID, 0));
		}

		if (text.contains("[agnUNSUBSCRIBE]")) {
			TagDetails tag = new TagDetailsImpl();
			tag.setTagName("agnUNSUBSCRIBE");
			text = text.replaceAll("\\[agnUNSUBSCRIBE\\]", agnTagService.resolve(tag, companyID, mailingID, mailinglistID, 0));
		}
		
		// TODO: Resolve all agnForm-Tags with their different formnames separately
		if (text.contains("[agnFORM")) {
			try {
				TagDetails tag;
				int startIndexOfAgnFormTag = text.indexOf("[agnFORM");
				int endIndexOfAgnFormTag = text.indexOf("]", startIndexOfAgnFormTag + 8);

				if (endIndexOfAgnFormTag == -1) {
					// if the Tag-Closing Bracket is missing, throw an exception
					throw new UnclosedTagException(0, "agnFORM");
				} else {
					endIndexOfAgnFormTag++;
					tag = new TagDetailsImpl();
					tag.setTagName("agnFORM");
					tag.setStartPos(startIndexOfAgnFormTag);
					tag.setEndPos(endIndexOfAgnFormTag);
					tag.setFullText(text.substring(startIndexOfAgnFormTag, endIndexOfAgnFormTag));
				}
				int startIndexOfFormName = tag.getFullText().indexOf('"') + 1;
				int endIndexOfFormName = tag.getFullText().indexOf('"', startIndexOfFormName);
				tag.setName(tag.getFullText().substring(startIndexOfFormName, endIndexOfFormName));
				text = text.replaceAll("\\[agnFORM name=\".*?\"\\]", agnTagService.resolve(tag, companyID, mailingID, mailinglistID, 0));

				if (text.contains("[agnFORM")) {
					logger.error("scanForLinks: Html-text has an unresolved agnFORM-Tag");
				}
			} catch (Exception e) {
				logger.error("scanForLinks", e);
			}
		}
		return text;
	}

	@SuppressWarnings("unused")
	private String getMimeTypeOfLinkdata(String linkUrl) {
		GetMethod get = null;
		try {
			HttpClient httpClient = new HttpClient();
			NetworkUtil.setHttpClientProxyFromSystem(httpClient, linkUrl);
			httpClient.getParams().setParameter("http.connection.timeout", 5000);

			get = new GetMethod(linkUrl);
			get.setFollowRedirects(true);

			int returnCode = httpClient.executeMethod(get);
			if (returnCode == 200) {
				return get.getResponseHeader("Content-Type").getValue();
			} else {
				return "Unknown due to: ReturnCode " + returnCode;
			}
		} catch (Exception e) {
			logger.error("getMimeTypeOfLinkdata: " + e.getMessage(), e);
			return "Unknown MimeType due to: " + e.getMessage();
		} finally {
			if (get != null) {
				get.releaseConnection();
			}
		}
	}

	@Override
	public String encodeTagStringLinkTracking(int companyID, int mailingID, int linkID, int customerID) {
		String rdirDomain = getBaseUrlCache().get(mailingID);
		if (rdirDomain == null) {
			try {
				rdirDomain = mailingDao.getMailingRdirDomain(mailingID, companyID);
				getBaseUrlCache().put(mailingID, rdirDomain);
			} catch (Exception e) {
				logger.error("encodeTagStringLinkTracking", e);
				return "";
			}
		}

		try {
			final int licenseID = configService.getLicenseID();
		
			final ComExtensibleUID uid = UIDFactory.from(licenseID, companyID, customerID, mailingID, linkID);

			String uidString;
			try {
				uidString = extensibleUIDService.buildUIDString(uid);
			} catch (UIDStringBuilderException e) {
				logger.error("makeUIDString", e);
				uidString = "";
			} catch (RequiredInformationMissingException e) {
				logger.error("makeUIDString", e);
				uidString = "";
			}
			
			if (configService.getBooleanValue(ConfigValue.UseRdirContextLinks, companyID)) {
				return rdirDomain + "/r/" + uidString;
			} else {
				return rdirDomain + "/r.html?uid=" + uidString;
			}
		} catch (Exception e) {
			logger.error("Exception in UID", e);
			return "";
		}
	}

	/**
	 * Create DeepTrackingData-Tuple
	 * First = deepTracking Http-Url Params
	 * Second = Http-Cookie data string
	 * 
	 * @param companyID
	 * @param mailingID
	 * @param linkID
	 * @param customerID
	 * @return
	 */
	@Override
	public String createDeepTrackingUID(int companyID, int mailingID, int linkID, int customerID) {
		try {
			return new DeepTrackingToken(companyID, mailingID, customerID, linkID).createTokenString();
		} catch (Exception e) {
			logger.error("Error while URL-encoding", e);
			return null;
		}
	}

	private int getActionIdForLink(String text, int linkStartIndex, String linkUrl) {
		int tagStartIndex = text.substring(0, linkStartIndex).lastIndexOf("<");
		if (tagStartIndex < 0) {
			return 0;
		}
		String prefix = text.substring(tagStartIndex, linkStartIndex);
		Matcher actionIdMatcher = ACTIONID_PATTERN.matcher(prefix);
		if (actionIdMatcher.find()) {
			String actionIdString = prefix.substring(actionIdMatcher.start(), actionIdMatcher.end());
			actionIdString = actionIdString.substring(actionIdString.indexOf("=") + 1).trim();
			if ((actionIdString.startsWith("\"") && actionIdString.endsWith("\""))
				|| (actionIdString.startsWith("'") && actionIdString.endsWith("'"))) {
				actionIdString = actionIdString.substring(1, actionIdString.length() - 1);
			} else {
				actionIdString = actionIdString.trim();
			}
			
			if (AgnUtils.isNumber(actionIdString)) {
				return Integer.parseInt(actionIdString);
			} else {
				return 0;
			}
		} else {
			int tagEndIndex = text.indexOf(">", linkStartIndex + linkUrl.length());
			if (tagEndIndex < 0) {
				return 0;
			}
			String postfix = text.substring(linkStartIndex + linkUrl.length(), tagEndIndex);
			actionIdMatcher = ACTIONID_PATTERN.matcher(postfix);
			if (actionIdMatcher.find()) {
				String actionIdString = postfix.substring(actionIdMatcher.start(), actionIdMatcher.end());
				actionIdString = actionIdString.substring(actionIdString.indexOf("=") + 1).trim();
				if ((actionIdString.startsWith("\"") && actionIdString.endsWith("\""))
					|| (actionIdString.startsWith("'") && actionIdString.endsWith("'"))) {
					actionIdString = actionIdString.substring(1, actionIdString.length() - 1);
				} else {
					actionIdString = actionIdString.trim();
				}
				
				if (AgnUtils.isNumber(actionIdString)) {
					return Integer.parseInt(actionIdString);
				} else {
					return 0;
				}
			} else {
				return 0;
			}
		}
	}

	private String getTitleForLink(String text, int linkStartIndex, String linkUrl) {
		int tagStartIndex = text.substring(0, linkStartIndex).lastIndexOf("<");
		if (tagStartIndex < 0) {
			return "";
		}
		String prefix = text.substring(tagStartIndex, linkStartIndex);
		Matcher titleMatcher = TITLE_PATTERN.matcher(prefix);
		if (titleMatcher.find()) {
			String titleString = prefix.substring(titleMatcher.start(), titleMatcher.end());
			titleString = titleString.substring(titleString.indexOf("=") + 1).trim();
			if ((titleString.startsWith("\"") && titleString.endsWith("\""))
				|| (titleString.startsWith("'") && titleString.endsWith("'"))) {
				titleString = titleString.substring(1, titleString.length() - 1);
			} else {
				titleString = titleString.trim();
			}
			
			return titleString;
		} else {
			int tagEndIndex = text.indexOf(">", linkStartIndex + linkUrl.length());
			if (tagEndIndex < 0) {
				return "";
			}
			String postfix = text.substring(linkStartIndex + linkUrl.length(), tagEndIndex);
			titleMatcher = TITLE_PATTERN.matcher(postfix);
			if (titleMatcher.find()) {
				String titleString = postfix.substring(titleMatcher.start(), titleMatcher.end());
				titleString = titleString.substring(titleString.indexOf("=") + 1).trim();
				if ((titleString.startsWith("\"") && titleString.endsWith("\""))
					|| (titleString.startsWith("'") && titleString.endsWith("'"))) {
					titleString = titleString.substring(1, titleString.length() - 1);
				} else {
					titleString = titleString.trim();
				}
				
				return titleString;
			} else {
				return "";
			}
		}
	}
	
	@Override
	public Integer getLineNumberOfFirstRdirLink(String text) {
		int indexOfRdirLink = text.indexOf(RDIRLINK_SEARCH_STRING);
		int indexOfRdirLinkNewFormat = text.indexOf("/r/");
		if (indexOfRdirLink < 0) {
			if (indexOfRdirLinkNewFormat < 0) {
				return null;
			} else {
				return AgnUtils.getLineNumberOfTextposition(text, indexOfRdirLinkNewFormat);
			}
		} else {
			if (indexOfRdirLinkNewFormat < 0) {
				return AgnUtils.getLineNumberOfTextposition(text, indexOfRdirLink);
			} else {
				return AgnUtils.getLineNumberOfTextposition(text, Math.min(indexOfRdirLink, indexOfRdirLinkNewFormat));
			}
		}
	}

	@Override
	public Integer getLineNumberOfFirstInvalidLink(String text) {
		Matcher matcher = INVALID_HREF_WITH_WHITESPACE_PATTERN.matcher(text);
		if (matcher.find()) {
			return AgnUtils.getLineNumberOfTextposition(text, matcher.start());
		} else {
			return null;
		}
	}

	@Override
	public Integer getLineNumberOfFirstInvalidSrcLink(String text) {
		Matcher matcher = INVALID_SRC_WITH_WHITESPACE_PATTERN.matcher(text);
		if (matcher.find()) {
			return AgnUtils.getLineNumberOfTextposition(text, matcher.start());
		} else {
			return null;
		}
	}

	private String getTextWithReplacedAgnTags(String text, String replaceTo) {
		// todo: there is possible to use replaceAll(regexp, replaceTo)
		// Find all HashTags and replace them by x...x to allow standard html link scan but preserve any occurring errors and their real positions
		String textWithReplacedHashTags = text;
		int currentPosition = 0;
		Matcher hashTagMatcher = HASHTAG_PATTERN.matcher(textWithReplacedHashTags);

		while (hashTagMatcher.find(currentPosition)) {
			String fullHashTagString = text.substring(hashTagMatcher.start(), hashTagMatcher.end());
			textWithReplacedHashTags = textWithReplacedHashTags.replace(fullHashTagString, AgnUtils.repeatString(replaceTo, (fullHashTagString.length())));
			currentPosition = hashTagMatcher.end();
		}

		// Find all AgnTags and replace them by x...x to allow standard html link scan but preserve any occurring errors and their real positions
		currentPosition = 0;
		Matcher agnTagMatcher = AGNTAG_PATTERN.matcher(textWithReplacedHashTags);
		while (agnTagMatcher.find(currentPosition)) {
			String fullAgnTagString = textWithReplacedHashTags.substring(agnTagMatcher.start(), agnTagMatcher.end());
			textWithReplacedHashTags = textWithReplacedHashTags.replace(fullAgnTagString, AgnUtils.repeatString(replaceTo, (fullAgnTagString.length())));
			currentPosition = agnTagMatcher.end();
		}

		// Find all GridPH-Tags and replace them by x...x to allow standard html link scan but preserve any occurring errors and their real positions
		currentPosition = 0;
		Matcher gridTagMatcher = GRIDTAG_PATTERN.matcher(textWithReplacedHashTags);
		while (gridTagMatcher.find(currentPosition)) {
			String fullGridTagString = textWithReplacedHashTags.substring(gridTagMatcher.start(), gridTagMatcher.end());
			textWithReplacedHashTags = textWithReplacedHashTags.replace(fullGridTagString, AgnUtils.repeatString(replaceTo, (fullGridTagString.length())));
			currentPosition = gridTagMatcher.end();
		}

		return textWithReplacedHashTags;
	}

	@Override
	public String validateLink(@VelocityCheck int companyId, String link, GridCustomPlaceholderType type) {
		if (Objects.isNull(type)) {
			return null;
		}
		if (type == GridCustomPlaceholderType.ImageLink && HASHTAG_PATTERN.matcher(link).find()) {
			// No HASH-Tags in image links allowed
			return "error.mailing.image.link.hash";
		}
		String replacedLink = getTextWithReplacedAgnTags(link, "x");
		if (replacedLink.contains(" ")) {
			return "error.mailing.url.blank.param";
		}
		return null;
	}
	
	public static List<LinkProperty> getLinkExtensions(String urlEncodedExtensionString) {
		List<LinkProperty> resultList = new ArrayList<>();
		if (StringUtils.isNotBlank(urlEncodedExtensionString)) {
			if (urlEncodedExtensionString.startsWith("?")) {
				urlEncodedExtensionString = urlEncodedExtensionString.substring(1);
			}
			for (String keyValueParamString : urlEncodedExtensionString.split("&")) {
				String[] parts = keyValueParamString.split("=");
				if (StringUtils.isNotBlank(parts[0])) {
					try {
						if (parts.length > 1 && StringUtils.isNotBlank(parts[1])) {
							resultList.add(new LinkProperty(PropertyType.LinkExtension, URLDecoder.decode(parts[0], "UTF-8"), URLDecoder.decode(parts[1], "UTF-8")));
						} else {
							resultList.add(new LinkProperty(PropertyType.LinkExtension, URLDecoder.decode(parts[0], "UTF-8"), ""));
						}
					} catch (UnsupportedEncodingException e) {
						logger.error("Error occured: " + e.getMessage(), e);
					}
				}
			}
		}
		return resultList;
	}

	private ParseLinkRuntimeException createParseException(String message, Matcher matcher, String text) {
		Caret caret = Caret.at(text, matcher.start());
		return new ParseLinkRuntimeException(message + caret, matcher.group(), caret);
	}
}
