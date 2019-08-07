/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.LinkProperty.PropertyType;
import com.agnitas.beans.TrackableLinkListItem;
import com.agnitas.beans.impl.ComTrackableLinkImpl;
import com.agnitas.dao.ComTrackableLinkDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.mailtracking.service.ClickTrackingService;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import org.agnitas.beans.BindingEntry.UserType;
import org.agnitas.beans.TrackableLink;
import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.dao.impl.mapper.TrackableLinkListItemRowMapper;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

public class ComTrackableLinkDaoImpl extends BaseDaoImpl implements ComTrackableLinkDao {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComTrackableLinkDaoImpl.class);
	
	private ConfigService configService;

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Override
    public final ComTrackableLink getTrackableLink(final int linkID, @VelocityCheck final int companyID, final boolean includeDeleted) {
		if (linkID <= 0 || companyID <= 0) {
			return null;
		} else {
			final String sql = includeDeleted
					? "SELECT * FROM rdir_url_tbl WHERE url_id = ? AND company_id = ?"
					: "SELECT * FROM rdir_url_tbl WHERE url_id = ? AND company_id = ? AND deleted <= 0";
			
			final List<ComTrackableLink> linkList = select(logger, sql, new ComTrackableLink_RowMapper(), linkID, companyID);
			if (linkList == null || linkList.size() < 1) {
				return null;
			} else {
				ComTrackableLink link = linkList.get(0);
				link.setProperties(getLinkProperties(link));
				return link;
			}
		}
		
	}

	@Override
	public TrackableLink getTrackableLink(int linkID, @VelocityCheck int companyID) {
		if (linkID <= 0 || companyID <= 0) {
			return null;
		} else {
			String sql = "SELECT * FROM rdir_url_tbl WHERE url_id = ? AND company_id = ? AND deleted <= 0";
			List<ComTrackableLink> linkList = select(logger, sql, new ComTrackableLink_RowMapper(), linkID, companyID);
			if (linkList == null || linkList.size() < 1) {
				return null;
			} else {
				ComTrackableLink link = linkList.get(0);
				link.setProperties(getLinkProperties(link));
				return link;
			}
		}
	}

	@Override
	public TrackableLink getTrackableLink(String url, @VelocityCheck int companyID, int mailingID) {
		if (StringUtils.isBlank(url) || companyID <= 0 || mailingID <= 0) {
			return null;
		} else {
			String sql = "SELECT * FROM rdir_url_tbl WHERE full_url = ? AND company_id = ? AND mailing_id = ? AND deleted <= 0";
			List<ComTrackableLink> linkList = select(logger, sql, new ComTrackableLink_RowMapper(), url, companyID, mailingID);
			if (linkList == null || linkList.size() < 1) {
				return null;
			} else {
				ComTrackableLink link = linkList.get(0);
				link.setProperties(getLinkProperties(link));
				return link;
			}
		}
	}

	@Override
    public List<ComTrackableLink> getTrackableLinks(@VelocityCheck int companyID, int mailingID) {
    	if (companyID <= 0 || mailingID <= 0) {
			return Collections.emptyList();
		} else {
	    	String sql = "SELECT * FROM rdir_url_tbl WHERE company_id = ? AND mailing_id = ? AND deleted <= 0";
	    	List<ComTrackableLink> trackableLinks = select(logger, sql, new ComTrackableLink_RowMapper(), companyID, mailingID);
	    	for (ComTrackableLink trackableLink : trackableLinks) {
				trackableLink.setProperties(getLinkProperties(trackableLink));
	    	}
	    	return trackableLinks;
		}
    }

	@Override
	@DaoUpdateReturnValueCheck
	public int saveTrackableLink(TrackableLink link) {
		if (link == null || link.getCompanyID() <= 0) {
			return 0;
		}
		
		if (!(link instanceof ComTrackableLink)) {
			return 0; // throw new Exception("Invalid link type for saveTrackableLink");
		}
		
		ComTrackableLink comLink = (ComTrackableLink) link;
		if (comLink.getId() != 0) {
			int existingLinkCount = selectInt(logger, "SELECT COUNT(url_id) FROM rdir_url_tbl WHERE company_id = ? AND mailing_id = ? AND url_id = ?", comLink.getCompanyID(), comLink.getMailingID(), comLink.getId());
			// if link exist in db - update it, else we set it's id=0 that means link not saved
			String usage = isOracleDB() ? "usage" : "`usage`";
			if (existingLinkCount > 0) {
				String sql = "UPDATE rdir_url_tbl SET action_id = ?, " + usage + " = ?, deep_tracking = ?, relevance = ?, shortname = ?, full_url = ?, original_url = ?, alt_text = ?, admin_link = ?, extend_url = ?, static_value=? WHERE company_id = ? AND mailing_id = ? AND url_id = ?";
				int linkId = comLink.getId();
				update(logger, sql,
						comLink.getActionID(),
						comLink.getUsage(),
						comLink.getDeepTracking(),
						0,
						AgnUtils.emptyToNull(comLink.getShortname()),
						AgnUtils.emptyToNull(comLink.getFullUrl()),
						AgnUtils.emptyToNull(comLink.getOriginalUrl()),
						AgnUtils.emptyToNull(comLink.getAltText()),
						comLink.isAdminLink() ? 1 : 0,
						comLink.isExtendByMailingExtensions() ? 1 : 0,
						comLink.isStaticValue() ? 1 : 0,
						comLink.getCompanyID(),
						comLink.getMailingID(),
						linkId);

				storeLinkProperties(linkId, comLink.getProperties());
			} else {
				comLink.setId(0);
			}
		} else {
			// Try to reactivate an existing link that was signed as deleted
			if (!reactivateDeletedLink(comLink)) {
				if (comLink.getFullUrl() != null) {
					comLink.setAdminLink(comLink.getFullUrl().contains("form.do"));
				} else {
					comLink.setAdminLink(false);
				}
				if (isOracleDB()) {
					comLink.setId(selectInt(logger, "SELECT rdir_url_tbl_seq.NEXTVAL FROM DUAL"));
					String sql = "INSERT INTO rdir_url_tbl (url_id, company_id, mailing_id, action_id, usage, deep_tracking, relevance, shortname, full_url, alt_text, admin_link, extend_url, from_mailing, static_value) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?)";
					int touchedLines = update(logger, sql,
							comLink.getId(),
							comLink.getCompanyID(),
							comLink.getMailingID(),
							comLink.getActionID(),
							comLink.getUsage(),
							comLink.getDeepTracking(),
							0,
							comLink.getShortname(),
							comLink.getFullUrl(),
							comLink.getAltText(),
							comLink.isAdminLink() ? 1 : 0,
							comLink.isExtendByMailingExtensions(),
							comLink.isStaticValue() ? 1 : 0);
					if (touchedLines != 1) {
						logger.error("Invalid update result in ComTrackableLinkDaoImpl.saveTrackableLink: " + touchedLines);
					}
				} else {
					String insertStatement = "INSERT INTO rdir_url_tbl (company_id, mailing_id, action_id, `usage`, deep_tracking, relevance, shortname, full_url, alt_text, admin_link, extend_url, from_mailing, static_value) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?)";
					
					Object[] paramsWithNext = new Object[12];
					paramsWithNext[0] = comLink.getCompanyID();
					paramsWithNext[1] = comLink.getMailingID();
					paramsWithNext[2] = comLink.getActionID();
					paramsWithNext[3] = comLink.getUsage();
					paramsWithNext[4] = comLink.getDeepTracking();
					paramsWithNext[5] = 0;
					paramsWithNext[6] = AgnUtils.emptyToNull(comLink.getShortname());
					paramsWithNext[7] = AgnUtils.emptyToNull(comLink.getFullUrl());
					paramsWithNext[8] = AgnUtils.emptyToNull(comLink.getAltText());
					paramsWithNext[9] = comLink.isAdminLink() ? 1 : 0;
					paramsWithNext[10] = comLink.isExtendByMailingExtensions() ? 1 : 0;
					paramsWithNext[11] = comLink.isStaticValue() ? 1 : 0;

					int linkID = insertIntoAutoincrementMysqlTable(logger, "url_id", insertStatement, paramsWithNext);
					comLink.setId(linkID);
				}
			}

			storeLinkProperties(comLink.getId(), comLink.getProperties());
		}

		return comLink.getId();
	}
	
	@Override
	public void batchSaveTrackableLinks(int companyID, int mailingId, Map<String, TrackableLink> trackableLinkMap, boolean removeUnusedLinks) {
		if (trackableLinkMap.isEmpty()) {
			return;
		}
		
		List<TrackableLink> trackableLinks = trackableLinkMap.values().stream()
				.filter(Objects::nonNull)
				.filter(link -> link instanceof ComTrackableLink)
				.collect(Collectors.toList());
		
		if (trackableLinks.isEmpty()) {
			return;
		}
		
		Set<Integer> trackableLinkIdsInUse = batchSaveTrackableLinks(companyID, mailingId, trackableLinks);
		
		if (removeUnusedLinks) {
			deleteTrackableLinksExceptIds(companyID, mailingId, trackableLinkIdsInUse);
		}
		
	}

	@DaoUpdateReturnValueCheck
	public boolean deleteTrackableLink(int linkID, @VelocityCheck int companyID) {
		return update(logger, "UPDATE rdir_url_tbl SET deleted = 1 WHERE url_id = ? AND company_id = ?", linkID, companyID) > 0;
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteTrackableLinksReally(@VelocityCheck int companyID) {
		int touchedLines = update(logger, "DELETE FROM rdir_url_tbl WHERE company_id = ?", companyID);
		if(touchedLines > 0) {
			return true;
		} else {
			int remainingUrls = selectInt(logger, "SELECT COUNT(*) FROM rdir_url_tbl WHERE company_id = ?", companyID);
			return remainingUrls == 0;
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteTrackableLinksExceptIds(@VelocityCheck int companyID, int mailingID, Collection<Integer> ids) {
		String sqlSetDeleted = "UPDATE rdir_url_tbl SET deleted = 1 " +
				"WHERE company_id = ? AND mailing_id = ? AND from_mailing = 1";

		if (ids.size() > 0) {
			sqlSetDeleted += " AND url_id NOT IN (" + StringUtils.join(ids, ',') + ")";
		}

		int rows = update(logger, sqlSetDeleted, companyID, mailingID);
		if (logger.isDebugEnabled()) {
			logger.debug("ComTrackableLinkDaoImpl - removeUnusedTrackableLinks touched rows: " + rows);
		}
	}

	/**
	 * Do not use this method directly for click tracking!
	 * 
	 * Use {@link ClickTrackingService#trackLinkClick(com.agnitas.emm.core.commons.uid.ComExtensibleUID, String, DeviceClass, int, int)} instead. This
	 * method respects the tracking settings of the customer.
	 * 
	 * @see ClickTrackingService#trackLinkClick(com.agnitas.emm.core.commons.uid.ComExtensibleUID, String, DeviceClass, int, int)
	 */
	@Override
	@DaoUpdateReturnValueCheck
	public boolean logClickInDB(TrackableLink link, int customerID, String remoteAddr, DeviceClass deviceClass, int deviceID, int clientID) {
		String curDateString = isOracleDB() ? "sysdate" : "current_timestamp";
		String sql = "INSERT INTO rdirlog_" + link.getCompanyID() + "_tbl (customer_id, url_id, company_id, timestamp, ip_adr, mailing_id, device_class_id, device_id, client_id) VALUES (?, ?, ?, " + curDateString + ", ?, ?, ?, ?, ?)";

		try {
			update(logger, sql, customerID, link.getId(), link.getCompanyID(), remoteAddr, link.getMailingID(), deviceClass.getId(), deviceID, clientID);

			// Update customer entry
			if (configService.getBooleanValue(ConfigValue.WriteCustomerOpenOrClickField, link.getCompanyID())) {
				updateCustomerForClick(link.getCompanyID(), customerID);
			}
			
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private void updateCustomerForClick(int companyID, int customerID) {
		if(customerID != 0) {
			try {
				String updateLastOpenAndClickStatement = "UPDATE customer_" + companyID + "_tbl SET lastopen_date = CURRENT_TIMESTAMP, lastclick_date = CURRENT_TIMESTAMP WHERE customer_id = ?";
				update(logger, updateLastOpenAndClickStatement, customerID);
			} catch (Exception e) {
				if(logger.isInfoEnabled()) {
					logger.info(String.format("Error updating last click date for customer %d of company %d", customerID, companyID), e);
				}
			}
		}
	}

    @Override
	@DaoUpdateReturnValueCheck
    public void deleteAdminAndTestClicks(int mailingId, @VelocityCheck int companyId) {
        String rdirlogTable = "rdirlog_" + companyId + "_tbl";

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("DELETE FROM ");
        queryBuilder.append(rdirlogTable);
        queryBuilder.append(" WHERE ");
        queryBuilder.append(rdirlogTable);
        queryBuilder.append(".mailing_id = ? AND EXISTS (SELECT 1 FROM customer_");
        queryBuilder.append(companyId);
        queryBuilder.append("_binding_tbl bind WHERE bind.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND ");
        queryBuilder.append(rdirlogTable);
        queryBuilder.append(".customer_id = bind.customer_id AND bind.mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id = ?))");

        update(logger, queryBuilder.toString(), mailingId, mailingId);
    }

	@DaoUpdateReturnValueCheck
	public boolean setDeeptracking(int deepTracking, @VelocityCheck int companyID, int mailingID) {
		try {
			String sql = "UPDATE rdir_url_tbl SET deep_tracking = ? WHERE company_id = ? AND mailing_id = ?";
			update(logger, sql, deepTracking, companyID, mailingID);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void storeLinkProperties(int linkId, List<LinkProperty> properties) {
		update(logger, "DELETE FROM rdir_url_param_tbl WHERE url_id = ?", linkId);
		
		if (CollectionUtils.isNotEmpty(properties)) {
			String insertSql = "INSERT INTO rdir_url_param_tbl (url_id, param_type, param_key, param_value) VALUES (?, ?, ?, ?)";
			List<Object[]> batchParameters = properties.stream()
					.map(prop -> new Object[]{
							linkId,
							prop.getPropertyType().toString(),
							prop.getPropertyName(),
							prop.getPropertyValue()
					})
					.collect(Collectors.toList());
			
			batchupdate(logger, insertSql, batchParameters);
		}
	}

	@Override
	public List<LinkProperty> getLinkProperties(ComTrackableLink link) {
		String sql = "SELECT param_type, param_key, param_value FROM rdir_url_param_tbl WHERE url_id = ? ORDER BY param_type, param_key, param_value";
		return select(logger, sql, new TrackableLinkProperty_RowMapper(), link.getId());
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteLinkProperties(int linkID) {
		update(logger, "DELETE FROM rdir_url_param_tbl WHERE url_id = ?", linkID);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteRdirUrlsByMailing(int mailingID) {
		String deleteSQL = "DELETE from rdir_url_tbl WHERE mailing_id = ?";
		int affectedRows = update(logger, deleteSQL, mailingID);
		return affectedRows > 0;
	}

	@Override
	public String getLinkUrl(@VelocityCheck int companyID, int mailingID, int linkID) {
		if (linkID > 0) {
			String sql = "SELECT full_url FROM rdir_url_tbl WHERE company_id = ? AND mailing_id = ? AND url_id = ?";
			List<Map<String, Object>> resultList = select(logger, sql, companyID, mailingID, linkID);
			if (resultList.size() > 0) {
				return (String) resultList.get(0).get("full_url");
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	private class ComTrackableLink_RowMapper implements RowMapper<ComTrackableLink> {
		@Override
		public ComTrackableLink mapRow(ResultSet resultSet, int row) throws SQLException {
			final ComTrackableLink trackableLink = new ComTrackableLinkImpl();
			
			trackableLink.setId(resultSet.getInt("url_id"));
			trackableLink.setCompanyID(resultSet.getInt("company_id"));
			trackableLink.setMailingID(resultSet.getInt("mailing_id"));
			trackableLink.setActionID(resultSet.getInt("action_id"));
			trackableLink.setUsage(resultSet.getInt("usage"));
			trackableLink.setFullUrl(AgnUtils.emptyToNull(resultSet.getString("full_url")));
			trackableLink.setOriginalUrl(AgnUtils.emptyToNull(resultSet.getString("original_url")));
			trackableLink.setShortname(AgnUtils.emptyToNull(resultSet.getString("shortname")));
			trackableLink.setRelevance(0);
			trackableLink.setAltText(AgnUtils.emptyToNull(resultSet.getString("alt_text")));
			trackableLink.setDeepTracking(resultSet.getInt("deep_tracking"));
			trackableLink.setAdminLink(resultSet.getInt("admin_link") > 0);
			trackableLink.setDeleted(resultSet.getInt("deleted") > 0);
			trackableLink.setExtendByMailingExtensions(resultSet.getInt("extend_url") > 0);
			
			final int staticValueFlag = resultSet.getInt("static_value");
			trackableLink.setStaticValue(!resultSet.wasNull() && staticValueFlag == 1);
			
			return trackableLink;
		}
	}

	private class TrackableLinkProperty_RowMapper implements RowMapper<LinkProperty> {
		@Override
		public LinkProperty mapRow(ResultSet resultSet, int row) throws SQLException {
			PropertyType type;
			try {
				type = PropertyType.parseString(resultSet.getString("param_type"));
			} catch (Exception e) {
				throw new SQLException("Error when reading properties param_type", e);
			}
			return new LinkProperty(type, resultSet.getString("param_key"), resultSet.getString("param_value"));
		}
	}
	/**
	 * Try to reactivate links marked as &quot;deleted&quot;.
	 * 
	 * When no link with given full URLL was found, this method return false, which means, that there is no non-deleted link with that URL after returning from that method.
	 * 
	 * When this method return true, that there is a non-deleted link with given URL. The link given as parameter get the ID of the link found in DB. If there are more than one link with matching URL,
	 * the highest URL is used.
	 * 
	 * @param link
	 *            link to use
	 * 
	 * @return true if non-deleted link for given URL exists after returning, otherwise false
	 */
	@DaoUpdateReturnValueCheck
	private boolean reactivateDeletedLink(TrackableLink link) {
		assert link.getId() == 0;
		assert link.getCompanyID() != 0;
		assert link.getMailingID() != 0;
		assert link.getFullUrl() != null;

		try {
			int existingLinkID = selectInt(logger, "SELECT MAX(url_id) AS max_url_id FROM rdir_url_tbl WHERE company_id = ? AND mailing_id = ? AND full_url = ?", link.getCompanyID(), link.getMailingID(), link.getFullUrl());
			if (existingLinkID <= 0) {
				if (logger.isDebugEnabled()) {
					logger.debug("No deleted link found to reactivate for mailing " + link.getMailingID() + " with URL " + link.getFullUrl());
				}
				return false;
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("Found a deleted link to reactivate for mailing " + link.getMailingID() + " with URL " + link.getFullUrl() + ". Reactivating link ID " + existingLinkID);
				}

				update(logger, "UPDATE rdir_url_tbl SET deleted = 0 WHERE company_id = ? AND mailing_id = ? AND url_id = ?", link.getCompanyID(), link.getMailingID(), existingLinkID);

				link.setId(existingLinkID);
				return true;
			}
		} catch (Exception e) {
			logger.error("Error reactivating link " + link.getFullUrl() + ". Creating new record.", e);
			return false;
		}
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public void activateDeeptracking(int companyID, int mailingID) {
		String sql = "UPDATE rdir_url_tbl SET deep_tracking = 1 where company_id = ? and mailing_id = ?";
		update(logger, sql, companyID, mailingID);
	}
	

	@Override
	public List<TrackableLinkListItem> listTrackableLinksForMailing(int companyID, int mailingID) {
		String sql = "SELECT url_id, full_url, shortname, alt_text, original_url FROM rdir_url_tbl WHERE company_id=? AND mailing_id=? AND deleted=0";
		
		try {
			return select(logger, sql, new TrackableLinkListItemRowMapper(), companyID, mailingID);
		} catch(Exception e) {
			logger.error("Error listing trackable links", e);
		}

		return null;
	}

	@Override
	public void removeGlobalAndIndividualLinkExtensions(@VelocityCheck int companyId, int mailingId) throws Exception {
		String sqlDeleteParams = "DELETE FROM rdir_url_param_tbl WHERE url_id IN (SELECT url_id FROM rdir_url_tbl WHERE mailing_id = ? AND company_id = ?)";
		try {
			update(logger, sqlDeleteParams, mailingId, companyId);
		} catch (Exception e) {
			logger.error("Error deleting link extensions for mailing " + mailingId, e);
			throw new Exception("Error deleting link extensions for mailing " + mailingId, e);
		}
	}

	@Override
	public void removeLinkExtensionsByCompany(int companyID) {
		update(logger, "DELETE FROM rdir_url_param_tbl WHERE url_id IN (SELECT url_id FROM rdir_url_tbl WHERE company_id = ?)", companyID);
	}
	
	@Override
	public Map<Integer, String> getTrackableLinkUrl(int companyId, int mailingId, List<Integer> linkIds) {
		Map<Integer, String> trackableLinkUrlMap = new HashMap<>();
		if (CollectionUtils.isNotEmpty(linkIds)) {
			String sql = "SELECT url_id, full_url FROM rdir_url_tbl WHERE company_id = ? AND mailing_id = ? AND " + makeBulkInClauseForInteger("url_id", linkIds);
			query(logger, sql, new LinkUrlMapCallback(trackableLinkUrlMap), companyId, mailingId);
		}
		return trackableLinkUrlMap;
	}
	
	private Set<Integer> batchSaveTrackableLinks(int companyId, int mailingId, List<TrackableLink> trackableLinks) {
		Set<Integer> trackableLinkIdsInUse = new HashSet<>();

		List<Integer> existingLinkIds = select(logger,
				"SELECT DISTINCT url_id FROM rdir_url_tbl WHERE company_id = ? AND mailing_id = ?", new IntegerRowMapper(), companyId, mailingId);
		
		List<ComTrackableLink> update = new ArrayList<>();
		List<ComTrackableLink> create = new ArrayList<>();
		
		trackableLinks.stream()
				.filter(link -> link instanceof ComTrackableLink)
				.map(link -> (ComTrackableLink)link)
				.forEach(comLink -> {
					comLink.setCompanyID(companyId);
					comLink.setMailingID(mailingId);
					if (comLink.getId() > 0 && existingLinkIds.contains(comLink.getId())) {
						update.add(comLink);
					} else {
						comLink.setAdminLink(StringUtils.contains(comLink.getFullUrl(), "form.do"));
						create.add(comLink);
					}
				});
		
		updateTrackableLinks(companyId, mailingId, update, trackableLinkIdsInUse);
		insertTrackableLinks(companyId, mailingId, create, trackableLinkIdsInUse);
		
		return trackableLinkIdsInUse;
	}
	
	private void updateTrackableLinks(int companyId, int mailingId, List<ComTrackableLink> trackableLinks, Set<Integer> trackableLinkIdsInUse) {
		if (trackableLinks.isEmpty()) {
			return;
		}
		
		String usage = isOracleDB() ? "usage" : "`usage`";
		String sql = "UPDATE rdir_url_tbl SET action_id = ?, " + usage + " = ?, " +
			"deep_tracking = ?, relevance = ?, shortname = ?, full_url = ?, original_url = ?, alt_text = ?, " +
			"admin_link = ?, extend_url = ?, static_value= ? WHERE company_id = ? AND mailing_id = ? AND url_id = ?";

		List<Object[]> paramsList = trackableLinks.stream().map(comLink -> new Object[] {
				comLink.getActionID(),
				comLink.getUsage(),
				comLink.getDeepTracking(),
				0,
				AgnUtils.emptyToNull(comLink.getShortname()),
				AgnUtils.emptyToNull(comLink.getFullUrl()),
				AgnUtils.emptyToNull(comLink.getOriginalUrl()),
				AgnUtils.emptyToNull(comLink.getAltText()),
				comLink.isAdminLink() ? 1 : 0,
				comLink.isExtendByMailingExtensions() ? 1 : 0,
				comLink.isStaticValue() ? 1 : 0,
				companyId,
				mailingId,
				comLink.getId()
		}).collect(Collectors.toList());
		
		batchupdate(logger, sql, paramsList);
		
		trackableLinks.forEach(link -> {
			int linkId = link.getId();
			trackableLinkIdsInUse.add(linkId);
			storeLinkProperties(linkId, link.getProperties());
		});
	}
	
	private void insertTrackableLinks(int companyId, int mailingId, final List<ComTrackableLink> trackableLinks, Set<Integer> trackableLinkIdsInUse) {
		if (trackableLinks.isEmpty()) {
			return;
		}
		
		List<ComTrackableLink> linksForInsertion = new ArrayList<>();
		// Try to reactivate an existing link that was signed as deleted
		Map<String, Integer> reactivatedLinks = reactivateLinks(companyId, mailingId, trackableLinks);
		
		for (ComTrackableLink comLink : trackableLinks) {
			int reactivatedId = reactivatedLinks.getOrDefault(comLink.getFullUrl(), 0);
			if (reactivatedId == 0) {
				linksForInsertion.add(comLink);
			} else {
				comLink.setId(reactivatedId);
			}
		}
		
		
		if (isOracleDB()) {
			String sql = "INSERT INTO rdir_url_tbl (url_id, company_id, mailing_id, action_id, usage, deep_tracking, relevance, shortname, full_url, alt_text, admin_link, extend_url, from_mailing, static_value) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?)";
			for (ComTrackableLink comLink: linksForInsertion) {
				int linkId = selectInt(logger, "SELECT rdir_url_tbl_seq.NEXTVAL FROM DUAL");
				comLink.setId(linkId);
			}

			List<Object[]> paramsList = linksForInsertion.stream()
					.map(comLink -> new Object[] {
						comLink.getId(),
						companyId,
						mailingId,
						comLink.getActionID(),
						comLink.getUsage(),
						comLink.getDeepTracking(),
						0,
						comLink.getShortname(),
						comLink.getFullUrl(),
						comLink.getAltText(),
						comLink.isAdminLink() ? 1 : 0,
						comLink.isExtendByMailingExtensions(),
						comLink.isStaticValue() ? 1 : 0
				}).collect(Collectors.toList());
			
			batchupdate(logger, sql, paramsList);
		} else {
			String insertStatement = "INSERT INTO rdir_url_tbl (company_id, mailing_id, action_id, `usage`, deep_tracking, relevance, shortname, full_url, alt_text, admin_link, extend_url, from_mailing, static_value) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?)";
			for (ComTrackableLink comLink: linksForInsertion) {
				Object[] paramsWithNext = new Object[] {
						companyId,
						mailingId,
						comLink.getActionID(),
						comLink.getUsage(),
						comLink.getDeepTracking(),
						0,
						AgnUtils.emptyToNull(comLink.getShortname()),
						AgnUtils.emptyToNull(comLink.getFullUrl()),
						AgnUtils.emptyToNull(comLink.getAltText()),
						comLink.isAdminLink() ? 1 : 0,
						comLink.isExtendByMailingExtensions() ? 1 : 0,
						comLink.isStaticValue() ? 1 : 0
				};
				int linkID = insertIntoAutoincrementMysqlTable(logger, "url_id", insertStatement, paramsWithNext);
				comLink.setId(linkID);
			}
		}
	
		trackableLinks.forEach(link -> {
			int linkId = link.getId();
			trackableLinkIdsInUse.add(linkId);
			storeLinkProperties(linkId, link.getProperties());
		});
	}
	
	private Map<String, Integer> reactivateLinks(int companyId, int mailingId, final List<ComTrackableLink> trackableLinks) {
		String sql = "SELECT MAX(url_id) AS max_url_id, full_url FROM rdir_url_tbl WHERE company_id = ? AND mailing_id = ? GROUP BY full_url";
		
		Map<String, Integer> existingLinksMap = new HashMap<>();
		query(logger, sql, new LinksMapCallback(existingLinksMap), companyId, mailingId);
		
		Map<String, Integer> linksForReActivation = new HashMap<>();
		
		for (ComTrackableLink link : trackableLinks) {
			int id = existingLinksMap.getOrDefault(link.getFullUrl(), 0);
			if (id > 0) {
				linksForReActivation.put(link.getFullUrl(), id);
			}
		}
		
		if (linksForReActivation.isEmpty()) {
			return new HashMap<>();
		}
		
		try {
			update(logger, "UPDATE rdir_url_tbl SET deleted = 0 WHERE company_id = ? AND mailing_id = ? " +
					" AND " + makeBulkInClauseForInteger("url_id", linksForReActivation.values()),
					companyId, mailingId);
			
			return linksForReActivation;
		} catch (Exception e) {
			logger.warn("Could not reactivate links for mailing ID " + mailingId + ", company ID " + companyId);
		}
		
		return new HashMap<>();
	}
	
	private static class LinksMapCallback implements RowCallbackHandler {
		private Map<String, Integer> linkUrlMap;

		public LinksMapCallback(Map<String, Integer> linkUrlMap) {
			this.linkUrlMap = Objects.requireNonNull(linkUrlMap);
		}

		@Override
		public void processRow(ResultSet rs) throws SQLException {
			linkUrlMap.put(rs.getString("full_url"), rs.getInt("max_url_id"));
		}
	}
	
	private static class LinkUrlMapCallback implements RowCallbackHandler {
		private Map<Integer, String> linksMap;

		public LinkUrlMapCallback(Map<Integer, String> linksMap) {
			this.linksMap = Objects.requireNonNull(linksMap);
		}

		@Override
		public void processRow(ResultSet rs) throws SQLException {
			linksMap.put(rs.getInt("url_id"), rs.getString("full_url"));
		}
	}
}
