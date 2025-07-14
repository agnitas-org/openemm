/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.LinkProperty.PropertyType;
import com.agnitas.beans.TrackableLink;
import com.agnitas.beans.TrackableLinkListItem;
import com.agnitas.beans.impl.TrackableLinkImpl;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.TrackableLinkDao;
import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.mailtracking.service.ClickTrackingService;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.core.trackablelinks.common.LinkTrackingMode;
import com.agnitas.web.exception.ClearLinkExtensionsException;
import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import com.agnitas.dao.impl.mapper.TrackableLinkListItemRowMapper;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.util.AgnUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrackableLinkDaoImpl extends BaseDaoImpl implements TrackableLinkDao {
	
	private ConfigService configService;

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Override
    public final TrackableLink getTrackableLink(final int linkID, final int companyID, final boolean includeDeleted) {
		if (linkID <= 0 || companyID <= 0) {
			return null;
		} else {
			//keep SELECT * because "usage" is a reserved word on mariaDB
			final String sql = includeDeleted
					? "SELECT * FROM rdir_url_tbl WHERE url_id = ? AND company_id = ?"
					: "SELECT * FROM rdir_url_tbl WHERE url_id = ? AND company_id = ? AND deleted <= 0";
			
			final List<TrackableLink> linkList = select(sql, new TrackableLink_RowMapper(), linkID, companyID);
			if (linkList == null || linkList.size() < 1) {
				return null;
			} else {
				TrackableLink link = linkList.get(0);
				link.setProperties(getLinkProperties(link));
				return link;
			}
		}
	}

	@Override
	public TrackableLink getTrackableLink(int linkID, int companyID) {
		return getTrackableLink(linkID, companyID, false);
	}

	@Override
	public TrackableLink getTrackableLink(String url, int companyID, int mailingID) {
		if (StringUtils.isBlank(url) || companyID <= 0 || mailingID <= 0) {
			return null;
		} else {
			//keep SELECT * because "usage" is a reserved word on mariaDB
			String sql = "SELECT * FROM rdir_url_tbl WHERE full_url = ? AND company_id = ? AND mailing_id = ? AND deleted <= 0";
			List<TrackableLink> linkList = select(sql, new TrackableLink_RowMapper(), url, companyID, mailingID);
			if (linkList == null || linkList.size() < 1) {
				return null;
			} else {
				TrackableLink link = linkList.get(0);
				link.setProperties(getLinkProperties(link));
				return link;
			}
		}
	}

	@Override
    public List<TrackableLink> getTrackableLinks(int companyID, int mailingID) {
	    return getTrackableLinks(mailingID, companyID, false);
    }

    @Override
    public Map<String, TrackableLink> getTrackableLinksMap(int mailingId, int companyId, boolean includeDeleted) {
        final List<TrackableLink> trackableLinks = getTrackableLinks(mailingId, companyId, includeDeleted);
        final Map<String, TrackableLink> map = new HashMap<>(trackableLinks.size());

        for (final TrackableLink trackableLink : trackableLinks) {
            map.put(trackableLink.getFullUrl(), trackableLink);
        }
        return map;
    }

    @Override
    public List<TrackableLink> getTrackableLinks(int mailingId, int companyId, boolean includeDeleted) {
    	if (companyId <= 0 || mailingId <= 0) {
			return Collections.emptyList();
		} else {
	        //keep SELECT * because "usage" is a reserved word on mariaDB
			List<TrackableLink> links = select("SELECT * FROM rdir_url_tbl WHERE company_id = ? AND mailing_id = ?" + (includeDeleted ? "" : " AND deleted <= 0"), new TrackableLink_RowMapper(), companyId, mailingId);
			
			String sql = "SELECT url_id, param_type, param_key, param_value FROM rdir_url_param_tbl WHERE url_id IN (SELECT url_id FROM rdir_url_tbl WHERE mailing_id = ?) ORDER BY url_id, param_type, param_key, param_value";
			List<Map<String, Object>> allLinkParamsResult = select(sql, mailingId);
			Map<Integer, List<LinkProperty>> allLinkParams = new HashMap<>();
			for (Map<String, Object> linkParamWithUrlID : allLinkParamsResult) {
				int linkID = ((Number) linkParamWithUrlID.get("url_id")).intValue();
				
				PropertyType type;
				try {
					type = PropertyType.parseString((String) linkParamWithUrlID.get("param_type"));
				} catch (Exception e) {
					throw new RuntimeException("Error when reading link properties param_type", e);
				}
				String paramKey = (String) linkParamWithUrlID.get("param_key");
				String paramValue = (String) linkParamWithUrlID.get("param_value");
				LinkProperty linkProperty = new LinkProperty(type, paramKey, paramValue);
				
				List<LinkProperty> linkProperties = allLinkParams.get(linkID);
				if (linkProperties == null) {
					linkProperties = new ArrayList<>();
				}
				linkProperties.add(linkProperty);
				
				allLinkParams.put(linkID, linkProperties);
			}
			
			for (TrackableLink link : links) {
				List<LinkProperty> linkProperties = allLinkParams.get(link.getId());
				if (linkProperties != null) {
					link.setProperties(linkProperties);
				}
			}
			
			return links;
		}
    }

    @Override
	@DaoUpdateReturnValueCheck
	public int saveTrackableLink(TrackableLink link) {
		if (link == null || link.getCompanyID() <= 0) {
			return 0;
		}
		
		if (link.getFullUrl() != null && link.getFullUrl().contains("##")) {
			// Links with Hash tags must be measurable
			link.setUsage(LinkTrackingMode.TEXT_AND_HTML.getMode());
		}
		
		if (link.getId() != 0) {
			int existingLinkCount = selectInt("SELECT COUNT(url_id) FROM rdir_url_tbl WHERE company_id = ? AND mailing_id = ? AND url_id = ?", link.getCompanyID(), link.getMailingID(), link.getId());
			// if link exist in db - update it, else we set it's id=0 that means link not saved
			String usage = isOracleDB() ? "usage" : "`usage`";
			if (existingLinkCount > 0) {
				validateLinkShortname(link.getShortname());

				String sql = "UPDATE rdir_url_tbl SET action_id = ?, " + usage + " = ?, deep_tracking = ?, shortname = ?, full_url = ?, original_url = ?, alt_text = ?, admin_link = ?, extend_url = ?, static_value=?, measured_separately = ?, create_substitute_link=? WHERE company_id = ? AND mailing_id = ? AND url_id = ?";
				int linkId = link.getId();
				update(sql,
						link.getActionID(),
						link.getUsage(),
						link.getDeepTracking(),
						AgnUtils.emptyToNull(link.getShortname()),
						AgnUtils.emptyToNull(link.getFullUrl()),
						AgnUtils.emptyToNull(link.getOriginalUrl()),
						AgnUtils.emptyToNull(link.getAltText()),
						link.isAdminLink() ? 1 : 0,
						link.isExtendByMailingExtensions() ? 1 : 0,
						link.isStaticValue() ? 1 : 0,
						link.isMeasureSeparately() ? 1 : 0,
						link.isCreateSubstituteLinkForAgnDynMulti() ? 1 : 0,
						link.getCompanyID(),
						link.getMailingID(),
						linkId);

				storeLinkProperties(linkId, link.getProperties());
			} else {
				link.setId(0);
			}
		} else {
			// Try to reactivate an existing link that was signed as deleted
			if (!reactivateDeletedLink(link)) {
				String linkFullUrl = link.getFullUrl();
				boolean isAdminLink = linkFullUrl != null && (linkFullUrl.contains("form.do") || linkFullUrl.contains("form.action"));

				link.setAdminLink(isAdminLink);

				validateLinkShortname(link.getShortname());

				if (isOracleDB()) {
					link.setId(selectInt("SELECT rdir_url_tbl_seq.NEXTVAL FROM DUAL"));
					String sql = "INSERT INTO rdir_url_tbl (url_id, company_id, mailing_id, action_id, usage, deep_tracking, shortname, full_url, alt_text, admin_link, extend_url, from_mailing, static_value, measured_separately, create_substitute_link) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, ?, ?)";
					int touchedLines = update(sql,
							link.getId(),
							link.getCompanyID(),
							link.getMailingID(),
							link.getActionID(),
							link.getUsage(),
							link.getDeepTracking(),
							link.getShortname(),
							link.getFullUrl(),
							link.getAltText(),
							link.isAdminLink() ? 1 : 0,
							link.isExtendByMailingExtensions(),
							link.isStaticValue() ? 1 : 0,
							link.isMeasureSeparately() ? 1 : 0,
							link.isCreateSubstituteLinkForAgnDynMulti() ? 1 : 0
							);
					if (touchedLines != 1) {
						logger.error("Invalid update result in TrackableLinkDaoImpl.saveTrackableLink: " + touchedLines);
					}
				} else {
					String insertStatement = "INSERT INTO rdir_url_tbl (company_id, mailing_id, action_id, `usage`, deep_tracking, shortname, full_url, alt_text, admin_link, extend_url, from_mailing, static_value, measured_separately, create_substitute_link) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, ?, ?)";

					Object[] paramsWithNext = new Object[13];
					paramsWithNext[0] = link.getCompanyID();
					paramsWithNext[1] = link.getMailingID();
					paramsWithNext[2] = link.getActionID();
					paramsWithNext[3] = link.getUsage();
					paramsWithNext[4] = link.getDeepTracking();
					paramsWithNext[5] = AgnUtils.emptyToNull(link.getShortname());
					paramsWithNext[6] = AgnUtils.emptyToNull(link.getFullUrl());
					paramsWithNext[7] = AgnUtils.emptyToNull(link.getAltText());
					paramsWithNext[8] = link.isAdminLink() ? 1 : 0;
					paramsWithNext[9] = link.isExtendByMailingExtensions() ? 1 : 0;
					paramsWithNext[10] = link.isStaticValue() ? 1 : 0;
					paramsWithNext[11] = link.isMeasureSeparately() ? 1 : 0;
					paramsWithNext[12] = link.isCreateSubstituteLinkForAgnDynMulti() ? 1 : 0;

					int linkID = insertIntoAutoincrementMysqlTable("url_id", insertStatement, paramsWithNext);
					link.setId(linkID);
				}
			}

			storeLinkProperties(link.getId(), link.getProperties());
		}

		return link.getId();
	}
	
	@Override
	public void batchSaveTrackableLinks(int companyID, int mailingId, Map<String, TrackableLink> trackableLinkMap, boolean removeUnusedLinks) {
		List<TrackableLink> trackableLinks = retrieveNonNullLinks(trackableLinkMap.values());

		Set<Integer> trackableLinkIdsInUse = batchSaveTrackableLinks(companyID, mailingId, trackableLinks);

		if (removeUnusedLinks) {
			deleteTrackableLinksExceptIds(companyID, mailingId, trackableLinkIdsInUse);
		}
	}

	@DaoUpdateReturnValueCheck
	public boolean deleteTrackableLink(int linkID, int companyID) {
		return update("UPDATE rdir_url_tbl SET deleted = 1 WHERE url_id = ? AND company_id = ?", linkID, companyID) > 0;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteTrackableLinksReally(int companyID) {
		int touchedLines = update("DELETE FROM rdir_url_tbl WHERE company_id = ?", companyID);
		if(touchedLines > 0) {
			return true;
		} else {
			int remainingUrls = selectInt("SELECT COUNT(*) FROM rdir_url_tbl WHERE company_id = ?", companyID);
			return remainingUrls == 0;
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteTrackableLinksExceptIds(int companyID, int mailingID, Collection<Integer> ids) {
		String sqlSetDeleted = "UPDATE rdir_url_tbl SET deleted = 1 " +
				"WHERE company_id = ? AND mailing_id = ? AND from_mailing = 1";

		if (!ids.isEmpty()) {
			sqlSetDeleted += " AND " + makeBulkNotInClauseForInteger("url_id", ids);
		}

		int rows = update(sqlSetDeleted, companyID, mailingID);
		if (logger.isDebugEnabled()) {
			logger.debug("TrackableLinkDaoImpl - removeUnusedTrackableLinks touched rows: " + rows);
		}
	}

	/**
	 * Do not use this method directly for click tracking!
	 * 
	 * Use {@link ClickTrackingService#trackLinkClick(ExtensibleUID, String, DeviceClass, int, int)} instead. This
	 * method respects the tracking settings of the customer.
	 * 
	 * @see ClickTrackingService#trackLinkClick(ExtensibleUID, String, DeviceClass, int, int)
	 */
	@Override
	@DaoUpdateReturnValueCheck
	public boolean logClickInDB(TrackableLink link, int customerID, String remoteAddr, DeviceClass deviceClass, int deviceID, int clientID) {
		String sql = "INSERT INTO rdirlog_" + link.getCompanyID() + "_tbl "
				+ "(customer_id, url_id, company_id, timestamp, ip_adr, mailing_id, device_class_id, device_id, client_id) "
				+ "VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?)";
		try {
        	if (customerID == 0) {
        		// Fallback for anonymous recipients
        		remoteAddr = null;
        	}
        	
			update(sql, customerID, link.getId(), link.getCompanyID(), remoteAddr, link.getMailingID(), deviceClass.getId(), deviceID, clientID);

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
		if (customerID != 0) {
			try {
				String updateLastOpenAndClickStatement = "UPDATE customer_" + companyID + "_tbl SET lastopen_date = CURRENT_TIMESTAMP, lastclick_date = CURRENT_TIMESTAMP WHERE customer_id = ?";
				update(updateLastOpenAndClickStatement, customerID);
			} catch (Exception e) {
				if (logger.isInfoEnabled()) {
					logger.info(String.format("Error updating last click date for customer %d of company %d", customerID, companyID), e);
				}
			}
		}
	}

    @Override
	@DaoUpdateReturnValueCheck
    public void deleteAdminAndTestClicks(int mailingID, int companyID) {
		String sqlClicks = "DELETE FROM rdirlog_" + companyID + "_tbl"
			+ " WHERE mailing_id = ?"
				+ " AND customer_id IN (SELECT customer_id FROM customer_" + companyID + "_binding_tbl"
					+ " WHERE user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id = ?))";
		update(sqlClicks, mailingID, mailingID);
    }

	@Override
	@DaoUpdateReturnValueCheck
	public void storeLinkProperties(int linkId, List<LinkProperty> properties) {
		update("DELETE FROM rdir_url_param_tbl WHERE url_id = ?", linkId);
		if (CollectionUtils.isNotEmpty(properties)) {
			String insertSql = "INSERT INTO rdir_url_param_tbl (url_id, param_type, param_key, param_value) VALUES (?, ?, ?, ?)";
			List<Object[]> batchParameters = new ArrayList<>();
			for (LinkProperty property : properties) {
				String propertyName = property.getPropertyName();
				if (propertyName == null) {
					propertyName = "";
				}
				String propertyValue = property.getPropertyValue();
				if (propertyValue == null) {
					propertyValue = "";
				}
				if (StringUtils.isNotEmpty(propertyName) || StringUtils.isNotEmpty(propertyValue)) {
					batchParameters.add(new Object[] { linkId, property.getPropertyType().toString(), propertyName, propertyValue });
				}
			}
			batchupdate(insertSql, batchParameters);
		}
	}

	@Override
	public List<LinkProperty> getLinkProperties(TrackableLink link) {
		String sql = "SELECT param_type, param_key, param_value FROM rdir_url_param_tbl WHERE url_id = ? ORDER BY param_type, param_key, param_value";
		return select(sql, new TrackableLinkProperty_RowMapper(), link.getId());
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteRdirUrlsByMailing(int mailingID) {
		String deleteSQL = "DELETE from rdir_url_tbl WHERE mailing_id = ?";
		int affectedRows = update(deleteSQL, mailingID);
		return affectedRows > 0;
	}

	private static class TrackableLink_RowMapper implements RowMapper<TrackableLink> {
		@Override
		public TrackableLink mapRow(ResultSet resultSet, int row) throws SQLException {
			final TrackableLink trackableLink = new TrackableLinkImpl();
			trackableLink.setId(resultSet.getInt("url_id"));
			trackableLink.setCompanyID(resultSet.getInt("company_id"));
			trackableLink.setMailingID(resultSet.getInt("mailing_id"));
			trackableLink.setActionID(resultSet.getInt("action_id"));
			trackableLink.setUsage(resultSet.getInt("usage"));
			trackableLink.setFullUrl(AgnUtils.emptyToNull(resultSet.getString("full_url")));
			trackableLink.setOriginalUrl(AgnUtils.emptyToNull(resultSet.getString("original_url")));
			trackableLink.setShortname(AgnUtils.emptyToNull(resultSet.getString("shortname")));
			trackableLink.setAltText(AgnUtils.emptyToNull(resultSet.getString("alt_text")));
			trackableLink.setDeepTracking(resultSet.getInt("deep_tracking"));
			trackableLink.setAdminLink(resultSet.getInt("admin_link") > 0);
			trackableLink.setDeleted(resultSet.getInt("deleted") > 0);
			trackableLink.setExtendByMailingExtensions(resultSet.getInt("extend_url") > 0);
			trackableLink.setMeasureSeparately(resultSet.getInt("measured_separately") > 0);
			trackableLink.setCreateSubstituteLinkForAgnDynMulti(resultSet.getInt("create_substitute_link") > 0);

			final int staticValueFlag = resultSet.getInt("static_value");
			trackableLink.setStaticValue(!resultSet.wasNull() && staticValueFlag == 1);
			
			return trackableLink;
		}
	}

	private static class TrackableLinkProperty_RowMapper implements RowMapper<LinkProperty> {
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
			int existingLinkID = selectInt("SELECT MAX(url_id) AS max_url_id FROM rdir_url_tbl WHERE company_id = ? AND mailing_id = ? AND full_url = ?", link.getCompanyID(), link.getMailingID(), link.getFullUrl());
			if (existingLinkID <= 0) {
				if (logger.isDebugEnabled()) {
					logger.debug("No deleted link found to reactivate for mailing " + link.getMailingID() + " with URL " + link.getFullUrl());
				}
				return false;
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("Found a deleted link to reactivate for mailing " + link.getMailingID() + " with URL " + link.getFullUrl() + ". Reactivating link ID " + existingLinkID);
				}

				update("UPDATE rdir_url_tbl SET deleted = 0, measured_separately = 0 WHERE company_id = ? AND mailing_id = ? AND url_id = ?", link.getCompanyID(), link.getMailingID(), existingLinkID);

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
		update(sql, companyID, mailingID);
	}

	@Override
	public List<TrackableLinkListItem> listTrackableLinksForMailing(int companyID, int mailingID) {
		String sql = "SELECT url_id, full_url, shortname, alt_text, original_url FROM rdir_url_tbl WHERE company_id=? AND mailing_id=? AND deleted=0";
		
		try {
			return select(sql, TrackableLinkListItemRowMapper.INSTANCE, companyID, mailingID);
		} catch(Exception e) {
			logger.error("Error listing trackable links", e);
		}

		return null;
	}

	@Override
	public boolean isTrackingOnEveryPositionAvailable(int companyId, int mailingId) {
		return selectInt("SELECT COUNT(url_id) FROM rdir_url_tbl WHERE measured_separately = 0 AND deleted = 0 " +
				" AND mailing_id = ? AND company_id = ?", mailingId, companyId) > 0;
	}

	@Override
	public void removeGlobalAndIndividualLinkExtensions(int companyId, int mailingId) {
		update("DELETE FROM rdir_url_param_tbl WHERE url_id IN (SELECT url_id FROM rdir_url_tbl WHERE mailing_id = ? AND company_id = ?)", mailingId, companyId);
	}

    @Override
    public void bulkClearExtensions(final int mailingId, final int companyId, final Set<Integer> bulkIds)
            throws ClearLinkExtensionsException {
        try {
			update(
                    getBulkClearExtensionsSqlQuery(bulkIds),
                    getBulkClearExtensionsSqlParams(mailingId, companyId, bulkIds));
        } catch (Exception e) {
            logger.error("Error deleting link extensions for mailing " + mailingId, e);
            throw new ClearLinkExtensionsException("Can't clear trackablelink extensions of links: " + bulkIds, e);
        }
    }

	@Override
	public void removeLinkExtensionsByCompany(int companyID) {
		update("DELETE FROM rdir_url_param_tbl WHERE url_id IN (SELECT url_id FROM rdir_url_tbl WHERE company_id = ?)", companyID);
	}
	
	@Override
	public Map<Integer, String> getTrackableLinkUrl(int companyId, int mailingId, List<Integer> linkIds) {
		Map<Integer, String> trackableLinkUrlMap = new HashMap<>();
		if (CollectionUtils.isNotEmpty(linkIds)) {
			String sql = "SELECT url_id, full_url FROM rdir_url_tbl WHERE company_id = ? AND mailing_id = ? AND " + makeBulkInClauseForInteger("url_id", linkIds);
			query(sql, new LinkUrlMapCallback(trackableLinkUrlMap), companyId, mailingId);
		}
		return trackableLinkUrlMap;
	}
	
	@Override
	public final Optional<TrackableLink> findLinkByFullUrl(final String fullUrl, final int mailingID, final int companyID) {
		final String sql = "SELECT * FROM rdir_url_tbl WHERE company_id=? AND mailing_id=? AND full_url=?";

		final List<TrackableLink> result = select(sql, new TrackableLink_RowMapper(), companyID, mailingID, fullUrl);
		
		return result.isEmpty()
				? Optional.empty()
				: Optional.of(result.get(0));
	}

	private List<TrackableLink> retrieveNonNullLinks(Collection<TrackableLink> inputLinks) {
		return inputLinks.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	private Set<Integer> batchSaveTrackableLinks(int companyId, int mailingId, List<TrackableLink> trackableLinks) {
		Set<Integer> trackableLinkIdsInUse = new HashSet<>();

		if (trackableLinks.isEmpty()) {
			return trackableLinkIdsInUse;
		}

		List<Integer> existingLinkIds = select(
				"SELECT DISTINCT url_id FROM rdir_url_tbl WHERE company_id = ? AND mailing_id = ?", IntegerRowMapper.INSTANCE, companyId, mailingId);
		
		List<TrackableLink> update = new ArrayList<>();
		List<TrackableLink> create = new ArrayList<>();
		
		trackableLinks
				.forEach(link -> {
					link.setCompanyID(companyId);
					link.setMailingID(mailingId);

					distributeTrackableLink(link, update, create, existingLinkIds);
				});
		
		updateTrackableLinks(companyId, update, trackableLinkIdsInUse);
		insertTrackableLinks(companyId, mailingId, create, trackableLinkIdsInUse);
		
		return trackableLinkIdsInUse;
	}

	private void distributeTrackableLink(TrackableLink link, List<TrackableLink> listForUpdate, List<TrackableLink> listForCreate, List<Integer> existingLinkIds) {
		if (link.getId() > 0 && existingLinkIds.contains(link.getId())) {
			listForUpdate.add(link);
		} else {
			link.setAdminLink(link.isAdminLink() || StringUtils.contains(link.getFullUrl(), "/form.action"));
			listForCreate.add(link);
		}
	}

	private void updateTrackableLinks(int companyId, List<TrackableLink> trackableLinks, Set<Integer> trackableLinkIdsInUse) {
		if (trackableLinks.isEmpty()) {
			return;
		}

		String usage = isOracleDB() ? "usage" : "`usage`";
		String sql = "UPDATE rdir_url_tbl SET action_id = ?, " + usage + " = ?, " +
				"deep_tracking = ?, shortname = ?, full_url = ?, original_url = ?, alt_text = ?, " +
				"admin_link = ?, extend_url = ?, static_value= ?, measured_separately = ?, create_substitute_link = ? WHERE company_id = ? AND url_id = ? AND mailing_id = ?";

		List<Object[]> paramsList = new ArrayList<>();
		for (TrackableLink link : trackableLinks) {
			validateLinkShortname(link.getShortname());

			paramsList.add(new Object[] {
					link.getActionID(),
					link.getUsage(),
					link.getDeepTracking(),
					AgnUtils.emptyToNull(link.getShortname()),
					AgnUtils.emptyToNull(link.getFullUrl()),
					AgnUtils.emptyToNull(link.getOriginalUrl()),
					AgnUtils.emptyToNull(link.getAltText()),
					link.isAdminLink() ? 1 : 0,
					link.isExtendByMailingExtensions() ? 1 : 0,
					link.isStaticValue() ? 1 : 0,
					link.isMeasureSeparately() ? 1 : 0,
					link.isCreateSubstituteLinkForAgnDynMulti() ? 1 : 0,
					companyId,
					link.getId(),
					link.getMailingID()
			});
		}

		batchupdate(sql, paramsList);

		trackableLinks.forEach(link -> {
			int linkId = link.getId();
			trackableLinkIdsInUse.add(linkId);
			storeLinkProperties(linkId, link.getProperties());
		});
	}

	private void insertTrackableLinks(int companyId, int mailingId, final List<TrackableLink> trackableLinks, Set<Integer> trackableLinkIdsInUse) {
		if (trackableLinks.isEmpty()) {
			return;
		}

		// Try to reactivate an existing link that was signed as deleted
		Map<String, Integer> reactivatedLinks = reactivateLinks(companyId, mailingId, trackableLinks);

		List<TrackableLink> linksForInsertion = new ArrayList<>();

		for (TrackableLink link : trackableLinks) {
			tryReactivateLink(reactivatedLinks, linksForInsertion, link);

			if (link.getFullUrl() != null && link.getFullUrl().contains("##")) {
				// Links with Hash tags must be measurable
				link.setUsage(LinkTrackingMode.TEXT_AND_HTML.getMode());
			}
		}

		if (isOracleDB()) {
			String sql = "INSERT INTO rdir_url_tbl (url_id, company_id, action_id, usage, deep_tracking, shortname, full_url, alt_text, admin_link, extend_url, from_mailing, static_value, measured_separately, create_substitute_link, deleted, mailing_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, ?, ?, ?, ?)";

			for (TrackableLink link: linksForInsertion) {
				int linkId = selectInt("SELECT rdir_url_tbl_seq.NEXTVAL FROM DUAL");
				link.setId(linkId);
			}

			List<Object[]> paramsList = new ArrayList<>();
			for (TrackableLink link : linksForInsertion) {
				validateLinkShortname(link.getShortname());

				paramsList.add(new Object[] {
						link.getId(),
						companyId,
						link.getActionID(),
						link.getUsage(),
						link.getDeepTracking(),
						link.getShortname(),
						link.getFullUrl(),
						link.getAltText(),
						link.isAdminLink() ? 1 : 0,
						link.isExtendByMailingExtensions(),
						link.isStaticValue() ? 1 : 0,
						link.isMeasureSeparately() ? 1 : 0,
						link.isCreateSubstituteLinkForAgnDynMulti() ? 1 : 0,
						link.isDeleted() ? 1 : 0,
						link.getMailingID()
				});
			}

			batchupdate(sql, paramsList);
		} else {
			String insertStatement = "INSERT INTO rdir_url_tbl (company_id, action_id, `usage`, deep_tracking, shortname, full_url, alt_text, admin_link, extend_url, from_mailing, static_value, measured_separately, create_substitute_link, deleted, mailing_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, ?, ?, ?, ?)";

			for (TrackableLink link: linksForInsertion) {
				validateLinkShortname(link.getShortname());

				Object[] paramsWithNext = new Object[] {
						companyId,
						link.getActionID(),
						link.getUsage(),
						link.getDeepTracking(),
						AgnUtils.emptyToNull(link.getShortname()),
						AgnUtils.emptyToNull(link.getFullUrl()),
						AgnUtils.emptyToNull(link.getAltText()),
						link.isAdminLink() ? 1 : 0,
						link.isExtendByMailingExtensions() ? 1 : 0,
						link.isStaticValue() ? 1 : 0,
						link.isMeasureSeparately() ? 1 : 0,
						link.isCreateSubstituteLinkForAgnDynMulti() ? 1 : 0,
						link.isDeleted() ? 1 : 0,
						link.getMailingID()
				};
				int linkID = insertIntoAutoincrementMysqlTable("url_id", insertStatement, paramsWithNext);
				link.setId(linkID);
			}
		}

		trackableLinks.forEach(link -> {
			int linkId = link.getId();
			trackableLinkIdsInUse.add(linkId);
			storeLinkProperties(linkId, link.getProperties());
		});
	}

	private void tryReactivateLink(Map<String, Integer> reactivatedLinks, List<TrackableLink> linksForInsertion, TrackableLink link) {
		int reactivatedId = reactivatedLinks.getOrDefault(link.getFullUrl(), 0);
		if (reactivatedId == 0) {
			linksForInsertion.add(link);
		} else {
			link.setId(reactivatedId);
		}
	}

	private void validateLinkShortname(String name) {
		if (name != null && name.length() > 1000) {
			throw new RuntimeException("Value for rdir_url_tbl.shortname is to long (Maximum: 1000, Current: " + name.length() + ")");
		}
	}

	private Map<String, Integer> reactivateLinks(int companyId, int mailingId, final List<TrackableLink> trackableLinks) {
		String sql = "SELECT MAX(url_id) AS max_url_id, full_url FROM rdir_url_tbl WHERE company_id = ? AND mailing_id = ? GROUP BY full_url";
		
		Map<String, Integer> existingLinksMap = new HashMap<>();
		query(sql, new LinksMapCallback(existingLinksMap), companyId, mailingId);
		
		Map<String, Integer> linksForReActivation = new HashMap<>();
		
		for (TrackableLink link : trackableLinks) {
			int id = existingLinksMap.getOrDefault(link.getFullUrl(), 0);
			if (id > 0) {
				linksForReActivation.put(link.getFullUrl(), id);
			}
		}
		
		if (linksForReActivation.isEmpty()) {
			return new HashMap<>();
		}
		
		try {
			update("UPDATE rdir_url_tbl SET deleted = 0 WHERE company_id = ? AND mailing_id = ? " +
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

    private Object[] getBulkClearExtensionsSqlParams(final int mailingId, final int companyId, final Set<Integer> ids) {
        List<Object> params = new ArrayList<>();
        params.add(mailingId);
        params.add(companyId);
        params.addAll(ids);
        return params.toArray();
    }

    private String getBulkClearExtensionsSqlQuery(final Set<Integer> bulkIds) {
        return String.format("DELETE FROM rdir_url_param_tbl " +
                        "WHERE url_id IN " +
                        "(SELECT url_id FROM rdir_url_tbl WHERE mailing_id = ? AND company_id = ? AND url_id IN (%s))",
                IntStream.range(0, bulkIds.size()).mapToObj(e -> "?").collect(Collectors.joining(", "))
        );
    }

	@Override
	public final void reactivateLink(final TrackableLink link) {
		final String sql = "UPDATE rdir_url_tbl SET deleted = 0 WHERE url_id = ? AND company_id = ?";
		
		this.update(sql, link.getId(), link.getCompanyID());
		
		link.setDeleted(false);
	}
}
