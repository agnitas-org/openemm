/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.linkcheck.service;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.agnitas.backend.AgnTag;
import com.agnitas.beans.BaseTrackableLink;
import com.agnitas.beans.Company;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.TagDetails;
import com.agnitas.beans.TrackableLink;
import com.agnitas.beans.impl.TagDetailsImpl;
import com.agnitas.beans.impl.TrackableLinkImpl;
import com.agnitas.dao.CompanyDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.hashtag.HashTagContext;
import com.agnitas.emm.core.hashtag.service.HashTagEvaluationService;
import com.agnitas.emm.core.linkcheck.exception.LinkScanException;
import com.agnitas.emm.core.linkcheck.exception.ParseLinkException;
import com.agnitas.emm.core.linkcheck.service.LinkService.LinkWarning.WarningType;
import com.agnitas.emm.core.mailing.bean.MailingParameter;
import com.agnitas.emm.core.mailing.dao.MailingParameterDao;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.trackablelinks.exceptions.DependentTrackableLinkException;
import com.agnitas.emm.core.workflow.service.WorkflowService;
import com.agnitas.emm.grid.grid.beans.GridCustomPlaceholderType;
import com.agnitas.service.AgnTagService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.Caret;
import com.agnitas.util.DeepTrackingToken;
import com.agnitas.util.LinkUtils;
import com.agnitas.util.PubID;
import com.agnitas.util.StringUtil;
import com.agnitas.util.UnclosedTagException;
import com.agnitas.util.backend.Decrypt;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LinkServiceImpl implements LinkService {
	
	private static final Logger logger = LogManager.getLogger(LinkServiceImpl.class);

	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("^\\{\\d+\\}$");
	private static final Pattern HASHTAG_PATTERN = Pattern.compile("##([^#\\n\\s]+)##");
	private static final Pattern AGNTAG_PATTERN = Pattern.compile("\\[agn[^\\]]+]");
	private static final Pattern GRIDTAG_PATTERN = Pattern.compile("\\[gridPH[^\\]]+]");
	private static final Pattern DOCTYPE_PATTERN = Pattern.compile("<!DOCTYPE [^>]*>", Pattern.CASE_INSENSITIVE);
	private static final Pattern ACTIONID_PATTERN = Pattern.compile("\\s+actionid\\s*=\\s*((\"[0-9]+\")|('[0-9]+')|([0-9]+))", Pattern.CASE_INSENSITIVE);
	private static final Pattern TITLE_PATTERN = Pattern.compile("\\s+title\\s*=\\s*((\"[ 0-9A-Z_.+-]+\")|('[ 0-9A-Z_.+-]+')|([0-9A-Z_.+-]+))", Pattern.CASE_INSENSITIVE);
	private static final String RDIRLINK_SEARCH_STRING = "r.html?uid=";
	private static final Pattern INVALID_HREF_WITH_WHITESPACE_PATTERN = Pattern.compile("\\shref\\s*=\\s*(\"|')\\s", Pattern.CASE_INSENSITIVE);
    private static final int MAX_LINK_LENGTH = 2000;
	
	/**
	 * Pattern that matches the subtext before an URL.
	 * Subtext must end with &quot;xmlns=&quot; or &quot;xmlns:&lt;somename&gt;=&quot; followed by an
	 * optional &quot; or &apos;.
	 */
	private static final Pattern XMLNS_PATTERN = Pattern.compile(".*xmlns(?::\\p{Alnum}+)?\\s*=\\s*(?:\"|')?$", Pattern.MULTILINE + Pattern.DOTALL);

	/**
	 * Pattern for URL protocol schema.
	 * 
	 * <ul>
	 *   <li>\p{alpha} &#8594; The protocol including trailing colon</li>
	 * </ul>
	 */
	static final Pattern PROTOCOL_SCHEMA_PATTERN = Pattern.compile("^(\\p{Alpha}+):.*$");
	
	/**
	 * Pattern for HTTP urls embedded in a <i>href</i>, <i>src<i> or <i>background</i> attribute.
	 * URLs is either detected by the leading attribute or by leading protocol.
	 * 
	 * <ul>
	 *   <li>((?:href|src|background)=) &#8594; leading attribute name including &quot;=&quot;
	 *   <li>(?:https?://)*https?:// &#8594; The HTTP/HTTPS protocol schema followed by a colon and two slashes. This regex also detect a sequence
	 *   of more than one protocol (used for error detection)</li>
	 *   <li>[0-9A-Z_.+-]+ &#8594; host name</li>
	 *   <li>(?::[0-9]+)? optional port number including heading colon</li>
	 * </ul>
	 */
	private static final Pattern URL_DETECTION_PATTERN = Pattern.compile("((?:href|src|background)=)|((?:https?://)*https?://[0-9A-Z_.+-]+(?::[0-9]+)?)", Pattern.CASE_INSENSITIVE);

	/**
	 * Pattern to detect multiple protocol schemata. This is used to detect erroneous links.
	 * 
	 * <ul>
	 *   <li>(\\p{Alpha}+://) &#8594; The HTTP/HTTPS protocol schema followed by a colon and two slashes.
	 * </ul>
	 */
	private static final Pattern DOUBLE_PROTOCOL_SCHEMA_PATTERN = Pattern.compile("^(\\p{Alpha}+://)(\\p{Alpha}+://).*$");

	
	private ConfigService configService;
	
	private AgnTagService agnTagService;

	private CompanyDao companyDao;
	
	private MailingDao mailingDao;
	
	private MailingParameterDao mailingParameterDao;
	
	private HashTagEvaluationService hashTagEvaluationService;
	
	private WorkflowService workflowService;
	
    private TargetService targetService;

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	public void setAgnTagService(AgnTagService agnTagService) {
		this.agnTagService = agnTagService;
	}

	public void setCompanyDao(CompanyDao companyDao) {
		this.companyDao = companyDao;
	}
	
	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}
	
	public void setMailingParameterDao(MailingParameterDao mailingParameterDao) {
		this.mailingParameterDao = mailingParameterDao;
	}
	
	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}
    
    public void setTargetService(TargetService targetService) {
        this.targetService = targetService;
    }
	
	public final void setHashTagEvaluationService(final HashTagEvaluationService service) {
		this.hashTagEvaluationService = Objects.requireNonNull(service, "HashTagEvaluationSerice is null");
	}
	
	@Override
	public String personalizeLink(final TrackableLink link, final String agnUidString, final int customerID, final String referenceTableRecordSelector, final boolean applyLinkExtensions, final String encryptedStaticValueMap) {
		final String fullUrlWithHashTags = link.getFullUrl();
		final Map<String, Object> staticValueMap = link.isStaticValue() ? decryptStaticValueMap(link.getCompanyID(), customerID, encryptedStaticValueMap) : Collections.emptyMap();

		String fullUrl = replaceHashTags(link, agnUidString, customerID, referenceTableRecordSelector, fullUrlWithHashTags, staticValueMap);
		
		if (applyLinkExtensions) {
			if (link.getProperties() != null && !link.getProperties().isEmpty()) {
				// Create redirect Url with new extensions
				for (LinkProperty linkProperty : link.getProperties()) {
					if (LinkUtils.isExtension(linkProperty)) {
						String propertyName = replaceHashTags(link, agnUidString, customerID, referenceTableRecordSelector, linkProperty.getPropertyName(), staticValueMap);
						String propertyValue = replaceHashTags(link, agnUidString, customerID, referenceTableRecordSelector, linkProperty.getPropertyValue(), staticValueMap);
						
						// Extend link properly (watch out for html-anchors etc.)
						// UrlEncoding is done separately before to prevent ##-tags from becomming encoded (=> encodingCharSet = null)
						fullUrl = AgnUtils.addUrlParameter(fullUrl, propertyName, propertyValue == null ? "" : propertyValue, StandardCharsets.UTF_8);
					}
				}
			}
		}
		
		return fullUrl;
	}
	
	private Map<String, Object> decryptStaticValueMap(final int companyID, final int customerID, final String encryptedStaticValueMap) {
		final Company company = this.companyDao.getCompany(companyID);
		
		try {
			final Decrypt decrypt = new Decrypt(company.getSecretKey());
			return decrypt.decryptAndDecode(encryptedStaticValueMap, customerID);
		} catch(final Exception e) {
			logger.error("Error decrypting static value map", e);
			
			return Collections.emptyMap();
		}
	}

	private String replaceHashTags(final TrackableLink link, final String agnUidString, final int customerID, final String referenceTableRecordSelector, final String textWithHashTags, final Map<String, Object> staticValueMap) {
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
				// TODO Move to own hashtag class and add to hashtag registry
				hashTagReplacement = agnUidString;
			} else if (hashTagContent.equals("MAILING_ID")) {
				// TODO Move to own hashtag class and add to hashtag registry
				hashTagReplacement = Integer.toString(link.getMailingID());
			} else if (hashTagContent.equals("URL_ID")) {
				// TODO Move to own hashtag class and add to hashtag registry
				hashTagReplacement = Integer.toString(link.getId());
			} else if (hashTagContent.equals("PUBID") || hashTagContent.startsWith("PUBID:")) {
				// TODO Move to own hashtag classes and add to hashtag registry
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
				// TODO Move to own hashtag class and add to hashtag registry
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
				// TODO Move to own hashtag classes and add to hashtag registry
				try {
					String paramName = hashTagContent.substring(hashTagContent.indexOf(':') + 1);
					MailingParameter parameter = mailingParameterDao.getParameterByName(paramName, link.getMailingID(), link.getCompanyID());
					
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
			} else {
				// Handle hash tag by registry and evaluation service here
				try {
					hashTagReplacement = this.hashTagEvaluationService.evaluateHashTag(hashTagContext, hashTagContent);
				} catch(final Exception e) {
					logger.error("Error processing tag string: " + hashTagContent + " (company " + hashTagContext.getCompanyID() + ", link " + hashTagContext.getCurrentTrackableLink().getId() + ")", e);

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
	 *
	 */
	// TODO: Check availablility and mimetype of image links
	@Override
	public LinkScanResult scanForLinks(String text, int mailingID, int mailinglistID, int companyID) {
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
	 *
	 */
	// TODO: Check availablility and mimetype of image links
	@Override
	public LinkScanResult scanForLinks(String textWithDoc, int companyID) {
		if (StringUtils.isBlank(textWithDoc)) {
			return new LinkScanResult();
		}

		// Remove doctype-tag for scan
		Matcher aMatch = DOCTYPE_PATTERN.matcher(textWithDoc);
		final String text = aMatch.find() ? textWithDoc.substring(aMatch.end()) : textWithDoc;
		final String textWithReplacedHashTags = getTextWithReplacedAgnTags(text, "x");

		final List<TrackableLink> foundTrackableLinks = new ArrayList<>();
		final List<String> foundImages = new ArrayList<>();
		final List<String> foundNotTrackableLinks = new ArrayList<>();
		final List<ErroneousLink> foundErroneousLinks = new ArrayList<>();
		final List<ErroneousLink> localLinks = new ArrayList<>();
		final List<LinkWarning> linkWarnings = new ArrayList<>();
		
		try {
			findAllLinks(textWithReplacedHashTags, (start, end) -> {
				final LinkScanContext context = new LinkScanContext(text, textWithReplacedHashTags, start, end, foundTrackableLinks, foundImages, foundNotTrackableLinks, foundErroneousLinks, localLinks, linkWarnings);
				doLinkChecks(context);
			});
		} catch (RuntimeException e) {
			throw new LinkScanException(e);
		}

		return new LinkScanResult(foundTrackableLinks, foundImages, foundNotTrackableLinks, foundErroneousLinks, localLinks, linkWarnings);
	}
	
	private void doLinkChecks(final LinkScanContext context) {
		// Do not check URLs that are values of attributes "xmlns" or "xmlns:<...>"
		if (linkCheckIsNamespace(context)) {
			return;
		}
		
		// Skip links with schema no starting with "http:" or "https:"
		if (linkCheckIsProtocolSchemaPresent(context) && !linkCheckIsHttpOrHttpsSchema(context)) {
			return;
		}
		
		if (linkCheckIsLocalUrl(context)) {
			context.getLocalLinks().add(new ErroneousLink("error.mailing.url.local", context.getStart(), context.getLinkUrl()));
			return;
		}

        if (!isValidLinkLength(context)) {
            context.getFoundErroneousLinks().add(new ErroneousLink("error.mailing.url.maxlength", context.getStart(), context.getLinkUrl()));
            return;
        }
		
		if (linkCheckUrlContainsBlanks(context)) {
			context.getFoundErroneousLinks().add(new ErroneousLink("error.mailing.url.blank", context.getStart(), context.getLinkUrl()));
			return;
		}
		
		if (linkCheckHasMultipleProtocols(context)) {
			context.getFoundErroneousLinks().add(new ErroneousLink("error.mailing.url.multipleProtocols", context.getStart(), context.getLinkUrl()));
			return;
		}
		
		// Checks depending on whether link refers to an image or not
		if (linkCheckIsImageUrl(context)) {
			// URLs refers to image
			doImageLinkCheck(context);
		} else {
			// URLs refers to non-image
			doNonImageLinkCheck(context);
		}
	}
	
	private void doNonImageLinkCheck(final LinkScanContext context) {
		if (linkCheckContainsAgnFormTag(context)) { // In some cases, the agnFORM tags cannot be resolved, so we must exclude them here
			
		} else if (linkCheckContainsAgnTagOrGridPhTag(context)) {
			context.getFoundNotTrackableLinks().add(context.getLinkUrl());
		} else if (linkCheckIsHttpOrHttpsSchema(context)) {
			final TrackableLink link = new TrackableLinkImpl();
			link.setFullUrl(context.getLinkUrl());
			link.setActionID(getActionIdForLink(context));
			link.setAltText(getTitleForLink(context));
			link.setShortname(getTitleForLink(context));
			context.getFoundTrackableLinks().add(link);
			
			if (linkCheckIsInsecureProtocol(context)) {
				context.getLinkWarnings().add(new LinkWarning(WarningType.INSECURE, context.getLinkUrl()));
			}
		} else if(isPlaceholderUrl(context)) {
			// Does nothing but accepting this url
		} else if (!linkCheckIsAnchor(context)) {
			context.getLocalLinks().add(new ErroneousLink("error.mailing.url.local", context.getStart(), context.getLinkUrl()));
		}
	}
	
	private void doImageLinkCheck(final LinkScanContext context) {
		if (linkCheckContainsHashTag(context)) {
			// No hash tags in image links allowed
			context.getFoundErroneousLinks().add(new ErroneousLink("error.mailing.imagelink.hash", context.getStart(), context.getLinkUrl()));
		} else {
			if (!linkCheckIsProtocolSchemaPresent(context) || linkCheckIsHttpOrHttpsSchema(context)) {
				context.getFoundImages().add(context.getLinkUrl());
				
				if (linkCheckIsInsecureProtocol(context)) {
					context.getLinkWarnings().add(new LinkWarning(WarningType.INSECURE, context.getLinkUrl()));
				}
			} else if (linkCheckContainsAgnTagOrGridPhTag(context)) {
				context.getFoundImages().add(context.getLinkUrl());

				if (linkCheckIsInsecureProtocol(context)) {
					context.getLinkWarnings().add(new LinkWarning(WarningType.INSECURE, context.getLinkUrl()));
				}
			} else {
				// No HTTP/HTTPS protocol and no AGN tag? -> Treat at local link
				context.getLocalLinks().add(new ErroneousLink("error.mailing.url.local", context.getStart(), context.getLinkUrl()));
			}
		}
	}
	
	private boolean isPlaceholderUrl(final LinkScanContext context) {
		return PLACEHOLDER_PATTERN.matcher(context.getLinkUrl()).matches();
	}
	
	private boolean linkCheckIsNamespace(final LinkScanContext context) {
		final String textBefore = context.getFullText().substring(0, context.getStart());
		final Matcher m = XMLNS_PATTERN.matcher(textBefore);

		return m.matches();
	}
	
	private boolean linkCheckIsLocalUrl(final LinkScanContext context) {
		final String schema = context.getProtocolSchema();
		
		if(schema == null) {
			return false;		// No schema present. Treat as non-local.
		}
		
		if (schema.length() < 2) {
			return true;		// Schema present, but to short. Treat as local
		}
		
		if("file".equalsIgnoreCase(schema)) {
			return true;		// "file:" schema. Treat as local
		}

		return false;			// Treat as non-local
	}
	
	private boolean linkCheckIsProtocolSchemaPresent(final LinkScanContext context) {
		return context.getProtocolSchema() != null;
	}
	
	private boolean linkCheckIsHttpOrHttpsSchema(final LinkScanContext context) {
		return "http".equalsIgnoreCase(context.getProtocolSchema())
				|| "https".equalsIgnoreCase(context.getProtocolSchema());
	}
	
	private boolean linkCheckIsInsecureProtocol(final LinkScanContext context) {
		return "http".equalsIgnoreCase(context.getProtocolSchema());
	}
	
	private boolean linkCheckUrlContainsBlanks(final LinkScanContext context) {
		// Check on URLs with AGN tags replaced, because AGN tags are allowed to contain blanks.
		return context.getLinkUrlWithAgnTagsReplaced().trim().contains(" ");
	}

    private boolean isValidLinkLength(final LinkScanContext context) {
        return context.getLinkUrl().trim().length() <= MAX_LINK_LENGTH;
    }
	
	private boolean linkCheckHasMultipleProtocols(final LinkScanContext context) {
		final Matcher matcher = DOUBLE_PROTOCOL_SCHEMA_PATTERN.matcher(context.getLinkUrl());

		return matcher.matches();
	}
	
	private boolean linkCheckIsImageUrl(final LinkScanContext context) {
		return AgnUtils.checkPreviousTextEquals(context.getFullText(), context.getStart(), "src=", ' ', '\'', '"', '\n', '\r', '\t')
				|| AgnUtils.checkPreviousTextEquals(context.getFullText(), context.getStart(), "background=", ' ', '\'', '"', '\n', '\r', '\t')
				|| AgnUtils.checkPreviousTextEquals(context.getFullText(), context.getStart(), "url(", ' ', '\'', '"', '\n', '\r', '\t');
	}
	
	private boolean linkCheckContainsHashTag(final LinkScanContext context) {
		return HASHTAG_PATTERN.matcher(context.getLinkUrl()).find();
	}
	
	private boolean linkCheckContainsAgnFormTag(final LinkScanContext context) {
		return context.getLinkUrl().contains("[agnFORM");
	}
	
	private boolean linkCheckContainsAgnTagOrGridPhTag(final LinkScanContext context) {
		return context.getLinkUrl().contains("[agn")
				|| context.getLinkUrl().contains("[gridPH");
	}
	
	private boolean linkCheckIsAnchor(final LinkScanContext context) {
		return context.getLinkUrl().startsWith("#");
	}

	@Override
	public final void findAllLinks(final String text, final BiConsumer<Integer, Integer> consumer) {
		int position = 0;
		final Matcher matcher = URL_DETECTION_PATTERN.matcher(text);
		while (matcher.find(position)) {
			int end; // Position of the last character of the URL (quotes not included)
			int start = matcher.start(); // Position of the first character of the URL (quotes not included)
			
			if(matcher.group(1) != null) { // Matches starting with "src=", "href=" or "background="
				final int attributeLength = matcher.group(1).length();
				start += attributeLength;	// Skip attribute

				// Skip leading blanks and tabs
				while(Character.isWhitespace(text.charAt(start))) {
					start++;
				}
							
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
					end = AgnUtils.getNextIndexOf(text, start, ' ', '>', '\n', '\r', '\t');
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

	/**
	 * Replace agnTags
	 * 
	 * AgnTag [agnPROFILE] will be resolved.
	 * AgnTag [agnUNSUBSCRIBE] will be resolved.
	 * AgnTag [agnFORM] will be resolved.
	 * 
	 */
	private String resolveAgnTags(String text, int mailingID, int mailinglistID, int companyID) {
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
		
		int latestAgnFormTagPosition = -1;
		while (text.contains("[agnFORM")) {
			try {
				TagDetails tag;
				int startIndexOfAgnFormTag = text.indexOf("[agnFORM");

				if (latestAgnFormTagPosition >= 0 && latestAgnFormTagPosition == startIndexOfAgnFormTag) {
					logger.error("Error while detecting agnFORM tags: deadlock");
					break;
				}
				latestAgnFormTagPosition = startIndexOfAgnFormTag;
				
				int endIndexOfAgnFormTag = text.indexOf("]", startIndexOfAgnFormTag + 8);

				if (endIndexOfAgnFormTag == -1) {
					// if the Tag-Closing Bracket is missing, throw an exception
					throw new UnclosedTagException(0, "agnFORM");
				}
				
				endIndexOfAgnFormTag++;
				tag = new TagDetailsImpl();
				tag.setTagName("agnFORM");
				tag.setStartPos(startIndexOfAgnFormTag);
				tag.setEndPos(endIndexOfAgnFormTag);
				tag.setFullText(text.substring(startIndexOfAgnFormTag, endIndexOfAgnFormTag));

				int startIndexOfFormName = StringUtil.firstIndexOf(tag.getFullText(), 8, '\'', '"');
				
				if (startIndexOfFormName == -1) {
					// if the name attribute value is missing, throw an exception
					throw new RuntimeException("agnFORM name attribute start is missing or invalid");
				}
				
				char nameAttributeEndChar = tag.getFullText().charAt(startIndexOfFormName);
				
				startIndexOfFormName++;
				
				int endIndexOfFormName = StringUtil.firstIndexOf(tag.getFullText(), startIndexOfFormName, nameAttributeEndChar);
				
				if (endIndexOfFormName == -1) {
					// if the name attribute value is missing, throw an exception
					throw new RuntimeException("agnFORM name attribute end is missing or invalid");
				}
				
				tag.setName(tag.getFullText().substring(startIndexOfFormName, endIndexOfFormName));
				text = text.replaceAll("\\[agnFORM\\s+name=(\"" + tag.getName() + "\"|'" + tag.getName() + "')\\s*/?\\]", agnTagService.resolve(tag, companyID, mailingID, mailinglistID, 0));
			} catch (Exception e) {
				logger.error("scanForLinks", e);
			}
		}

		if (text.contains("[agnFORM")) {
			logger.error(String.format("scanForLinks: Html-text has an unresolved agnFORM-Tag [%s]", text));
		}

		for (AgnTag fullviewTag : List.of(AgnTag.FULLVIEW, AgnTag.WEBVIEW)) {
			if (text.contains("[" + fullviewTag.getName() + "]")) {
				TagDetails tag = new TagDetailsImpl();
				tag.setTagName(fullviewTag.getName());
				text = text.replaceAll("\\[" + fullviewTag.getName() + "\\]", agnTagService.resolve(tag, companyID, mailingID, mailinglistID, 0));
			}
		}

		return text;
	}

	/**
	 * Create DeepTrackingData-Tuple
	 * First = deepTracking Http-Url Params
	 * Second = Http-Cookie data string
	 * 
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

	private int getActionIdForLink(final LinkScanContext context) {
		return getActionIdForLink(context.getFullText(), context.getStart(), context.getLinkUrl());
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

	private String getTitleForLink(final LinkScanContext context) {
		return getTitleForLink(context.getFullText(), context.getStart(), context.getLinkUrl());
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
	public Integer getLineNumberOfFirstRdirLink(final String rdirDomain, String text) {
		final int indexOfRdirLink = text.indexOf(RDIRLINK_SEARCH_STRING);
		final int indexOfRdirLinkNewFormat = text.indexOf(rdirDomain + "/r/");
		
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
	public int getLineNumberOfFirstInvalidLink(String text) {
		Matcher matcher = INVALID_HREF_WITH_WHITESPACE_PATTERN.matcher(text);
		if (matcher.find()) {
			return AgnUtils.getLineNumberOfTextposition(text, matcher.start());
		} else {
			return -1;
		}
	}

	public static String getTextWithReplacedAgnTags(String text, String replaceTo) {
		String textWithReplacements = text;
		
		textWithReplacements = replaceInText(HASHTAG_PATTERN, textWithReplacements, replaceTo);
		textWithReplacements = replaceInText(AGNTAG_PATTERN, textWithReplacements, replaceTo);
		textWithReplacements = replaceInText(GRIDTAG_PATTERN, textWithReplacements, replaceTo);

		return textWithReplacements;
	}
	
	/**
	 * Replaces patterns in given text by a text of same length.
	 * 
	 * @param pattern pattern to replace
	 * @param in original text
	 * @param replaceBy (fragment) of replacement text
	 * 
	 * @return text with replaced patterns
	 */
	private static String replaceInText(final Pattern pattern, final String in, final String replaceBy) {
		// Find all HashTags and replace them by x...x to allow standard html link scan but preserve any occurring errors and their real positions
		
		int currentPosition = 0;
		String text = in;
		final Matcher matcher = pattern.matcher(text);
		
		while (matcher.find(currentPosition)) {
			final String matchingString = text.substring(matcher.start(), matcher.end());
			text = text.replace(matchingString, AgnUtils.repeatString(replaceBy, (matchingString.length())));
			currentPosition = matcher.end();
		}
		
		return text;
	}

	@Override
	public String validateLink(int companyId, String link, GridCustomPlaceholderType type) {
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

	@Override
	public String replaceLinks(String content, Map<String, String> replacementsMap) {
		StringBuilder changedContent = new StringBuilder(content);
		AtomicInteger accumulatedDiff = new AtomicInteger(0);

		findAllLinks(content, (start, end) -> {
			String url = content.substring(start, end);
			String replacement = replacementsMap.get(url);

			if (replacement != null) {
                changedContent.replace(start + accumulatedDiff.get(), end + accumulatedDiff.get(), replacement);
				accumulatedDiff.addAndGet(replacement.length() - url.length());
			}
		});

		return changedContent.toString();
	}

	@Override
	public String addNumbersToLinks(Map<String, Integer> linksCounters, List<String> newLinks, String originContent) {
		StringBuilder changedContent = new StringBuilder(originContent);
		AtomicInteger accumulatedDiff = new AtomicInteger(0);

		findAllLinks(originContent, (start, end) -> {
			String linkUrlToReplace = originContent.substring(start, end);
			Integer count = linksCounters.get(linkUrlToReplace);

			if (count != null){
				String linkNumber = "#" + count;
				changedContent.insert(end + accumulatedDiff.get(), linkNumber);
				accumulatedDiff.addAndGet(linkNumber.length());
				linksCounters.put(linkUrlToReplace, count + 1);
				newLinks.add(linkUrlToReplace + linkNumber);
			}
		});
		return changedContent.toString();
	}

	@Override
    public List<LinkProperty> getDefaultExtensions(int companyId) {
        String defaultExtension = configService.getValue(ConfigValue.DefaultLinkExtension, companyId);
        return LinkUtils.parseLinkExtension(defaultExtension);
    }

	private ParseLinkException createParseException(String message, Matcher matcher, String text) {
		Caret caret = Caret.at(text, matcher.start());
		return new ParseLinkException(message + caret, matcher.group(), caret);
	}
	
	@Override
    public void assertChangedOrDeletedLinksNotDepended(
            Collection<TrackableLink> oldLinks,
            Collection<TrackableLink> newLinks) throws DependentTrackableLinkException {
	    
        List<Integer> newLinksIds = newLinks.stream()
                .map(BaseTrackableLink::getId)
                .toList();
        
        List<TrackableLink> changedOrDeletedLinks = oldLinks.stream()
                .filter(link -> !newLinksIds.contains(link.getId()))
                .toList();
        List<String> usedInActiveWorkflowLinks = changedOrDeletedLinks.stream().filter(link
                -> workflowService.isLinkUsedInActiveWorkflow(link))
                .map(BaseTrackableLink::getFullUrl)
                .collect(Collectors.toList());
        List<String> usedInTargetLinks = changedOrDeletedLinks.stream()
                .filter(link -> targetService.isLinkUsedInTarget(link))
                .map(BaseTrackableLink::getFullUrl)
                .collect(Collectors.toList());
        if (isNotEmpty(usedInActiveWorkflowLinks) || isNotEmpty(usedInTargetLinks)) {
            throw new DependentTrackableLinkException(usedInActiveWorkflowLinks, usedInTargetLinks);
        }
    }

	@Override
	public String getRdirDomain(int companyID) {
		final Company company = companyDao.getCompany(companyID);
		return company.getRdirDomain();
	}
}
