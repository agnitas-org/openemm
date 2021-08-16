/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.beans.DynamicTagContent;
import com.agnitas.beans.Mailing;
import org.agnitas.beans.impl.DynamicTagContentImpl;
import org.agnitas.dao.DynamicTagContentDao;
import org.agnitas.dao.MailingDao;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.NetworkUtil;
import org.agnitas.util.XmlUtilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.agnitas.beans.ComContentSource;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.TargetLight;
import com.agnitas.beans.impl.DynamicTagImpl;
import com.agnitas.beans.impl.TargetLightImpl;
import com.agnitas.dao.ComTargetDao;

public class ContentSourceImporter implements ApplicationContextAware {
	
	/** The logger. */
	private static final Logger logger = Logger.getLogger(ContentSourceImporter.class);

	private static final String SCHEMA_DIRECTORYPATH = new File(ContentSourceImporter.class.getResource("/").getFile() + "/../../schemas").getAbsolutePath();
	private static final String RSS_ARTICLE_BASENAME = "RssFeedArticle_";

	protected ApplicationContext applicationContext;
	protected MailingDao mailingDao;
	protected ComTargetDao targetDao;
	private DynamicTagContentDao dynamicTagContentDao;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	/**
	 * Set DAO accessing mailings.
	 * 
	 * @param mailingDao DAO accessing mailings
	 */
	@Required
	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	@Required
	public void setDynamicTagContentDao(DynamicTagContentDao dynamicTagContentDao) {
		this.dynamicTagContentDao = dynamicTagContentDao;
	}

	/**
	 * Set DAO accessing target groups.
	 * 
	 * @param targetDao DAO accessing target groups
	 */
	@Required
	public void setTargetDao(ComTargetDao targetDao) {
		this.targetDao = targetDao;
	}

	public void importContent(ComContentSource contentSource, Mailing aMailing, Date minDate) throws Exception {
		// Read all available tagetgroups
		Map<Integer, TargetLight> allTargets = targetDao.getAllowedTargetLights(aMailing.getCompanyID());
		if (allTargets == null) {
			logger.error("importContent: could not read targets");
			return;
		} else {
			// Add the default targetgroup
			TargetLight aTarget = new TargetLightImpl();
			aTarget.setCompanyID(aMailing.getCompanyID());
			aTarget.setId(0);
			aTarget.setTargetName("All Subscribers");
			allTargets.put(new Integer(0), aTarget);
		}

		// Read all dyntags from mailing
		Map<String, DynamicTag> allDynTags = aMailing.getDynTags();

		// Outputvariable for content elements to be deleted afterwards
		Set<Integer> dynContentToDelete = new HashSet<>();
		
		// Read document and create or replace content elements
		try {
			byte[] data = NetworkUtil.loadUrlData(contentSource.getUrl());
			
			// Check for xml contentsource and import data
			Document xmlSourceDocument;
			try {
				xmlSourceDocument = XmlUtilities.parseXMLDataAndXSDVerifyByDOM(data, null, SCHEMA_DIRECTORYPATH + "/dynamicalContent.xsd", true);
			} catch (Exception e) {
				xmlSourceDocument = null;
			}
			
			if (xmlSourceDocument != null) {
				importXmlContents(xmlSourceDocument, allDynTags, allTargets, minDate, aMailing, dynContentToDelete);
			} else {
				// Check for rss 2.0 feed contentsource and import data
				Document rssSourceDocument;
				try {
					data = new String(data, "UTF-8").replace("<content:encoded>", "<contentEncoded>").replace("</content:encoded>", "</contentEncoded>").getBytes("UTF-8");
					rssSourceDocument = XmlUtilities.parseXMLDataAndXSDVerifyByDOM(data, null, SCHEMA_DIRECTORYPATH + "/rss2_0.xsd");
				} catch (Exception e) {
					throw new XmlDataException("Invalid dataformat found for content source import: " + contentSource.getUrl() + "\nDetail: " + e.getMessage(), e);
				}
			
				if (rssSourceDocument != null) {
					importRssContents(contentSource.getId(), rssSourceDocument, allDynTags, allTargets, minDate, aMailing, dynContentToDelete);
				} else {
					throw new Exception("Invalid dataformat found for content source import: " + contentSource.getUrl());
				}
			}
		} catch (XmlDataException se) {
			logger.error("importContent: " + se.getMessage(), se);
			throw se;
		} catch (Exception se) {
			logger.error("importContent: " + se.getMessage(), se);
			// using any error page?
			throw new Exception("importContent: " + se.getMessage(), se);
		}

		for (DynamicTag aTag : allDynTags.values()) {
			Map<Integer, DynamicTagContent> allContent = aTag.getDynContent();
			if (allContent != null) {
				for (DynamicTagContent dynamicTagContent : allContent.values()) {
					aMailing.scanForLinks(dynamicTagContent.getDynContent(), null, applicationContext, null, null);
				}
			}
		}
		
		mailingDao.saveMailing(aMailing, false);

		// Delete content elements
		for (int id : dynContentToDelete) {
			dynamicTagContentDao.deleteContent(aMailing.getCompanyID(), id);
		}
	}

	private void importXmlContents(Document xmlSourceDocument, Map<String, DynamicTag> allDynTags, Map<Integer, TargetLight> allTargets, Date minDate, Mailing mailing, Set<Integer> dynContentToDelete) {
		NodeList blockNodes = xmlSourceDocument.getElementsByTagName("BLOCK");
		for (int i = 0; i < blockNodes.getLength(); i++) {
			Node blockNode = blockNodes.item(i);
			Map<String, String> subValues = XmlUtilities.getSimpleValuesOfNode(blockNode);
			String blockItemName = subValues.get("NAME");
			String blockItemTargetIDString = subValues.get("TARGET_ID");
			String blockItemBody = subValues.get("BODY");
			String blockItemTimestamp = subValues.get("TIMESTAMP");
			// If DELETE-Tag is found we delete the DynamicTag (DELETE-Tag may have no text value)
			String blockItemDelete = subValues.get("DELETE");
			
			// Check for block's timestamp
			if (minDate != null && StringUtils.isNotBlank(blockItemTimestamp)) {
				try {
					Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(blockItemTimestamp);
					if (date != null && date.getTime() < minDate.getTime()) {
						// This block is expired and should not be imported
						continue;
					}
				} catch (Exception e) {
					logger.error("Invalid timestamp in XmlContentSource", e);
				}
			}
			
			DynamicTag dynamicTag = getOrCreateDynamicTag(allDynTags, mailing.getCompanyID(), blockItemName);
			
			// Search DynamicTagContent to update
			DynamicTagContent dynamicTagContentToUpdate = null;
			Map<Integer, DynamicTagContent> dynContent = dynamicTag.getDynContent();
			int targetID = 0;
			if (dynContent != null) {
				for (DynamicTagContent dynamicTagContent : dynContent.values()) {
					targetID = dynamicTagContent.getTargetID();
					if (StringUtils.isNotBlank(blockItemTargetIDString)) {
						try {
							int blockItemTargetID = Integer.parseInt(blockItemTargetIDString.trim());
							if (blockItemTargetID == targetID) {
								dynamicTagContentToUpdate = dynamicTagContent;
								break;
							}
						} catch (NumberFormatException numE) {
							// Target_ID must contain a number string
						}
					}
				}
			}
			
			// Execute import of current block data
			if (blockItemBody != null) {
				// do update or insert
				if (dynamicTagContentToUpdate != null) {
					// update the content with body
					if (StringUtils.isNotBlank(blockItemBody)) {
						dynamicTagContentToUpdate.setDynContent(blockItemBody);
					} else {
						dynamicTagContentToUpdate.setDynContent("");
					}
				} else {
					// insert the body to be its content
					if (StringUtils.isNotBlank(blockItemTargetIDString)) {
						int blockItemTargetID = Integer.parseInt(blockItemTargetIDString.trim());
						try {
							targetID = blockItemTargetID;
						} catch (Exception e) {
							targetID = 0;
						}
						if (allTargets.get(targetID) == null) {
							logger.warn("setXMLContents: Target for insert does not exist!");
							targetID = 0;
						}
					} else {
						targetID = 0;
					}

					if (!StringUtils.isEmpty(blockItemBody)) {
						dynamicTagContentToUpdate = new DynamicTagContentImpl();
						dynamicTagContentToUpdate.setDynOrder(dynamicTag.getMaxOrder() + 1);
						dynamicTagContentToUpdate.setCompanyID(dynamicTag.getCompanyID());
						dynamicTagContentToUpdate.setMailingID(dynamicTag.getMailingID());
						dynamicTagContentToUpdate.setDynName(dynamicTag.getDynName());
						dynamicTagContentToUpdate.setDynNameID(dynamicTag.getId());
						dynamicTagContentToUpdate.setDynContent(blockItemBody);
						dynamicTagContentToUpdate.setTargetID(targetID);
						dynamicTag.addContent(dynamicTagContentToUpdate);
					} else {
						// Don't create empty DynamicTagContent
						if (logger.isInfoEnabled()) {
							logger.info("Body of content block '" + dynamicTag.getDynName() + "' (" + dynamicTag.getId() + ") is empty. Do not create content block");
						}
					}
				}
			} else if (blockItemDelete != null) {
				// DELETE existing DynamicTagContent
				if (StringUtils.isNotBlank(blockItemTargetIDString)) {
					int blockItemTargetID = Integer.parseInt(blockItemTargetIDString.trim());

					Set<Integer> dynamicTagContentIdsToRemove = new HashSet<>();
					for (DynamicTagContent content : dynamicTag.getDynContent().values()) {
						if (content.getTargetID() == blockItemTargetID) {
							dynamicTagContentIdsToRemove.add(content.getId());
						}
					}
	
					for (int id : dynamicTagContentIdsToRemove) {
						dynamicTag.removeContent(id);
					}
	
					dynContentToDelete.addAll(dynamicTagContentIdsToRemove);
				} else {
					logger.error("Invalid target_id for deletion of DynamicTagContent");
				}
			}
		}
	}

	private void importRssContents(int contentSourceID, Document rssSourceDocument, Map<String, DynamicTag> allDynTags, Map<Integer, TargetLight> allTargets, Date minDate, Mailing mailing, Set<Integer> dynContentToDelete) throws Exception {
		String rssArticleBasenameAndContentSourceID = RSS_ARTICLE_BASENAME + contentSourceID + "_";
		
		StringBuilder rssItemHtmlText = new StringBuilder();
		NodeList itemNodes = rssSourceDocument.getElementsByTagName("item");
		
		List<Node> itemsToUse = new ArrayList<>();
		
		for (int i = 0; i < itemNodes.getLength(); i++) {
			Node itemNode = itemNodes.item(i);
			Map<String, String> subValues = XmlUtilities.getSimpleValuesOfNode(itemNode);
			if (minDate == null) {
				// Use all items anyway
				itemsToUse.add(itemNode);
			} else {
				String itemPubDateString = subValues.get("pubDate");
				if (StringUtils.isNotBlank(itemPubDateString)) {
					// Only parse the pubDate if it is really needed for maybe date string problems
					Date itemPubDate = new SimpleDateFormat(DateUtilities.RFC822FORMAT, DateUtilities.RFC822FORMAT_LOCALE).parse(itemPubDateString);
					if (!minDate.after(itemPubDate)) {
						// Use this item, because its pubDate matches
						itemsToUse.add(itemNode);
					}
				}
			}
		}
		
		// Remove DynamicTags for old articlenumbers that were not newly delivered
		for (DynamicTag dynamicTag : allDynTags.values()) {
			String dynamicTagName = dynamicTag.getDynName();
			if (dynamicTagName.startsWith(rssArticleBasenameAndContentSourceID) && dynamicTagName.substring(rssArticleBasenameAndContentSourceID.length()).contains("_")) {
				int articleNumber = Integer.parseInt(dynamicTagName.substring(rssArticleBasenameAndContentSourceID.length(), dynamicTagName.indexOf("_", rssArticleBasenameAndContentSourceID.length()))) ;
				if (articleNumber >= itemsToUse.size()) {
					Set<Integer> dynamicTagContentIdsToRemove = new HashSet<>();
					for (DynamicTagContent content : dynamicTag.getDynContent().values()) {
						dynamicTagContentIdsToRemove.add(content.getId());
					}
	
					for (int id : dynamicTagContentIdsToRemove) {
						dynamicTag.removeContent(id);
					}
					dynContentToDelete.addAll(dynamicTagContentIdsToRemove);
				}
			}
		}
		
		int nextArticleNumber = 1;
		for (Node itemNode : itemsToUse) {
			Map<String, String> subValues = XmlUtilities.getSimpleValuesOfNode(itemNode);
			String itemTitle = subValues.get("title");
			String itemDescription = subValues.get("description");
			String itemLink = subValues.get("link");
						
			// Check if DynamicTags for this item already exists
			String itemNameTitle = rssArticleBasenameAndContentSourceID + nextArticleNumber + "_title";
			DynamicTag dynamicTagRssTitle = getOrCreateDynamicTag(allDynTags, mailing.getCompanyID(), itemNameTitle);

			String itemNameDescription = rssArticleBasenameAndContentSourceID + nextArticleNumber + "_description";
			DynamicTag dynamicTagRssDescription = getOrCreateDynamicTag(allDynTags, mailing.getCompanyID(), itemNameDescription);

			String itemNameLink = rssArticleBasenameAndContentSourceID + nextArticleNumber + "_link";
			DynamicTag dynamicTagRssLink = getOrCreateDynamicTag(allDynTags, mailing.getCompanyID(), itemNameLink);
			
			insertOrUpdateDynamicContentTag(itemTitle, dynamicTagRssTitle);
			
			insertOrUpdateDynamicContentTag(itemDescription, dynamicTagRssDescription);

			insertOrUpdateDynamicContentTag(itemLink, dynamicTagRssLink);
			
			rssItemHtmlText.append("<a href=\"[agnDYN name=\"" + itemNameLink + "\" /]\"><b>[agnDYN name=\"" + itemNameTitle + "\" /]</b></a><br />\n");
			rssItemHtmlText.append("[agnDYN name=\"" + itemNameDescription + "\" /]<br />\n");
			rssItemHtmlText.append("Link: <a href=\"[agnDYN name=\"" + itemNameLink + "\" /]\">[agnDYN name=\"" + itemNameLink + "\" /]</a><br />\n<br />\n");
			
			String itemContent = subValues.get("contentEncoded");
			if (itemContent != null) {
				String itemNameContent = rssArticleBasenameAndContentSourceID + nextArticleNumber + "_content";
				DynamicTag dynamicTagRssContent = getOrCreateDynamicTag(allDynTags, mailing.getCompanyID(), itemNameContent);
				insertOrUpdateDynamicContentTag(itemContent, dynamicTagRssContent);
			}

			// Work optional data items
			String itemImage = subValues.get("image");
			if (itemImage != null) {
				String itemNameImage = rssArticleBasenameAndContentSourceID + nextArticleNumber + "_image";
				DynamicTag dynamicTagRssImage = getOrCreateDynamicTag(allDynTags, mailing.getCompanyID(), itemNameImage);
				insertOrUpdateDynamicContentTag(itemImage, dynamicTagRssImage);
				rssItemHtmlText.append("<img src=\"[agnDYN name=\"" + itemNameImage + "\" /]\" /><br />\n<br />\n");
			}
			
			String itemEnclosure = subValues.get("enclosure");
			if (itemEnclosure != null) {
				String url = XmlUtilities.getAttributeValue(XmlUtilities.getSubNodeByName(itemNode, "enclosure"), "url");
				String itemNameEnclosure = rssArticleBasenameAndContentSourceID + nextArticleNumber + "_enclosure_url";
				DynamicTag dynamicTagRssEnclosure = getOrCreateDynamicTag(allDynTags, mailing.getCompanyID(), itemNameEnclosure);
				insertOrUpdateDynamicContentTag(url, dynamicTagRssEnclosure);
				rssItemHtmlText.append("Enclosure Link: <a href=\"[agnDYN name=\"" + itemNameEnclosure + "\" /]\">[agnDYN name=\"" + itemNameEnclosure + "\" /]</a><br />\n<br />\n");
			}
			
			String itemEnclosure720 = subValues.get("enclosure_square_720");
			if (itemEnclosure720 != null) {
				String url = XmlUtilities.getAttributeValue(XmlUtilities.getSubNodeByName(itemNode, "enclosure_square_720"), "url");
				String itemNameEnclosure720 = rssArticleBasenameAndContentSourceID + nextArticleNumber + "_enclosure_square_720_url";
				DynamicTag dynamicTagRssEnclosure720 = getOrCreateDynamicTag(allDynTags, mailing.getCompanyID(), itemNameEnclosure720);
				insertOrUpdateDynamicContentTag(url, dynamicTagRssEnclosure720);
				rssItemHtmlText.append("Enclosure Square 720 Link: <a href=\"[agnDYN name=\"" + itemNameEnclosure720 + "\" /]\">[agnDYN name=\"" + itemNameEnclosure720 + "\" /]</a><br />\n<br />\n");
			}
			
			nextArticleNumber++;
		}
		
		// Write text to Html component if it is empty
		DynamicTag htmlComponentDynamicTag = allDynTags.get("HTML-Version");
		if (htmlComponentDynamicTag != null) {
			Collection<DynamicTagContent> dynamicTagContentsHtml = htmlComponentDynamicTag.getDynContent().values();
			if (dynamicTagContentsHtml.size() > 0) {
				// update the content
				for (DynamicTagContent dynamicTagContentToUpdate : dynamicTagContentsHtml) {
					// Create helping text until there are more than 10 characters in Html component
					if (dynamicTagContentToUpdate.getDynContent().length() < 10) {
						dynamicTagContentToUpdate.setDynContent(dynamicTagContentToUpdate.getDynContent() + "\n<br />\n" + rssItemHtmlText.toString());
					}
				}
			} else {
				// insert the content
				DynamicTagContent dynamicTagContentToUpdate = new DynamicTagContentImpl();
				dynamicTagContentToUpdate.setDynOrder(htmlComponentDynamicTag.getMaxOrder() + 1);
				dynamicTagContentToUpdate.setCompanyID(htmlComponentDynamicTag.getCompanyID());
				dynamicTagContentToUpdate.setMailingID(htmlComponentDynamicTag.getMailingID());
				dynamicTagContentToUpdate.setDynName(htmlComponentDynamicTag.getDynName());
				dynamicTagContentToUpdate.setDynNameID(htmlComponentDynamicTag.getId());
				dynamicTagContentToUpdate.setDynContent(rssItemHtmlText.toString());
				dynamicTagContentToUpdate.setTargetID(0);
				htmlComponentDynamicTag.addContent(dynamicTagContentToUpdate);
			}
		}
	}

	private DynamicTag getOrCreateDynamicTag(Map<String, DynamicTag> allDynTags, int companyID, String tagName) {
		DynamicTag dynamicTagRssLink = allDynTags.get(tagName);
		if (dynamicTagRssLink == null) {
			dynamicTagRssLink = new DynamicTagImpl();
			dynamicTagRssLink.setCompanyID(companyID);
			dynamicTagRssLink.setDynName(tagName);
			allDynTags.put(tagName, dynamicTagRssLink);
		}
		return dynamicTagRssLink;
	}

	private void insertOrUpdateDynamicContentTag(String content, DynamicTag dynamicTag) {
		Collection<DynamicTagContent> dynamicTagContentsRssTitle = dynamicTag.getDynContent().values();
		if (dynamicTagContentsRssTitle.size() > 0) {
			// update the content
			for (DynamicTagContent dynamicTagContentToUpdate : dynamicTagContentsRssTitle) {
				dynamicTagContentToUpdate.setDynContent(content);
			}
		} else {
			// insert the content
			DynamicTagContent dynamicTagContentToUpdate = new DynamicTagContentImpl();
			dynamicTagContentToUpdate.setDynOrder(dynamicTag.getMaxOrder() + 1);
			dynamicTagContentToUpdate.setCompanyID(dynamicTag.getCompanyID());
			dynamicTagContentToUpdate.setMailingID(dynamicTag.getMailingID());
			dynamicTagContentToUpdate.setDynName(dynamicTag.getDynName());
			dynamicTagContentToUpdate.setDynNameID(dynamicTag.getId());
			dynamicTagContentToUpdate.setDynContent(content);
			dynamicTagContentToUpdate.setTargetID(0);
			dynamicTag.addContent(dynamicTagContentToUpdate);
		}
	}
}
