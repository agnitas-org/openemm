/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import static com.agnitas.util.ImageUtils.MOBILE_IMAGE_PREFIX;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.MailingComponentType;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.TrackableLink;
import com.agnitas.beans.factory.MailingComponentFactory;
import com.agnitas.beans.impl.TrackableLinkImpl;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.MailingComponentDao;
import com.agnitas.dao.TrackableLinkDao;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import com.agnitas.dao.impl.mapper.StringRowMapper;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.components.form.MailingImagesOverviewFilter;
import com.agnitas.emm.util.html.HtmlChecker;
import com.agnitas.emm.util.html.HtmlCheckerException;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DbUtilities;
import com.agnitas.web.CdnImage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

public class MailingComponentDaoImpl extends PaginatedBaseDaoImpl implements MailingComponentDao {

	/**
	 * Factory to create new mailing components.
	 */
	protected MailingComponentFactory mailingComponentFactory;
	
	private TrackableLinkDao trackableLinkDao;
	
	private ConfigService configService;
	
	/**
	 * Set factory to create new mailing components.
	 *
	 * @param mailingComponentFactory factory to create new mailing components
	 */
	public void setMailingComponentFactory(MailingComponentFactory mailingComponentFactory) {
		this.mailingComponentFactory = mailingComponentFactory;
	}

	public void setTrackableLinkDao(TrackableLinkDao trackableLinkDao) {
		this.trackableLinkDao = trackableLinkDao;
	}
	
	public void setConfigService(ConfigService service) {
		this.configService = Objects.requireNonNull(service, "configService");
	}

	public List<MailingComponent> getMailingComponents(int mailingID, int companyID, MailingComponentType componentType) {
		return getMailingComponents(mailingID, companyID, componentType, true);
	}

	@Override
	public List<MailingComponent> getMailingComponents(int mailingID, int companyID, MailingComponentType componentType, boolean includeContent) {
		String sqlGetComponents = "SELECT company_id, mailing_id, component_id, compname, comppresent, comptype, mtype, target_id, url_id, description, timestamp" +
				(includeContent ? ", emmblock, binblock " : " ") +
				"FROM component_tbl " +
				"WHERE company_id = ? AND mailing_id = ? AND comptype = ? " +
				"ORDER BY compname ASC";

		return select(sqlGetComponents, new MailingComponentRowMapper(includeContent), companyID, mailingID, componentType.getCode());
	}

	@Override
	public List<MailingComponent> getMailingComponents(int mailingID, int companyID) {
		return getMailingComponents(mailingID, companyID, true);
	}

	@Override
	public List<MailingComponent> getMailingComponents(int mailingID, int companyID, boolean includeContent) {
		String componentSelect = "SELECT company_id, mailing_id, component_id, compname, comptype, comppresent, mtype, target_id, url_id, description, timestamp" +
				(includeContent ? ", emmblock, binblock " : " ") +
				" FROM component_tbl WHERE company_id = ? AND mailing_id = ? ORDER BY compname ASC";

		List<MailingComponent> mailingComponentList = select(componentSelect, new MailingComponentRowMapper(includeContent), companyID, mailingID);

		// Sort results (mobile components after their base components)
		mailingComponentList.sort((mailingComponent1, mailingComponent2) -> {
			String name1 = mailingComponent1.getComponentName();
			if (name1.startsWith(MOBILE_IMAGE_PREFIX)) {
				name1 = name1.substring(MOBILE_IMAGE_PREFIX.length());
			}
			String name2 = mailingComponent2.getComponentName();
			if (name2.startsWith(MOBILE_IMAGE_PREFIX)) {
				name2 = name2.substring(MOBILE_IMAGE_PREFIX.length());
			}
			if (name1.equals(name2)) {
				if (mailingComponent1.getComponentName().startsWith(MOBILE_IMAGE_PREFIX)) {
					return mailingComponent2.getComponentName().startsWith(MOBILE_IMAGE_PREFIX) ? 0 : 1;
				} else {
					return mailingComponent2.getComponentName().startsWith(MOBILE_IMAGE_PREFIX) ? -1 : 0;
				}
			} else {
				return name1.compareTo(name2);
			}
		});

		return mailingComponentList;
	}

	@Override
    public List<MailingComponent> getMailingComponents(int companyID, int mailingID, Set<Integer> componentIds) {
		String sqlGetComponents = "SELECT company_id, mailing_id, component_id, compname, comppresent, comptype, mtype, target_id, url_id, description, timestamp, emmblock, binblock " +
				"FROM component_tbl " +
				"WHERE company_id = ? AND mailing_id = ? AND " + makeBulkInClauseForInteger("component_id", componentIds) +
				"ORDER BY compname ASC";

		return select(sqlGetComponents, new MailingComponentRowMapper(true), companyID, mailingID);
    }

	@Override
	public MailingComponent getMailingComponent(int compID, int companyID) {
		if (companyID == 0) {
			return null;
		}

		String componentSelect = "SELECT company_id, mailing_id, component_id, compname, comptype, comppresent, emmblock, binblock, mtype, target_id, url_id, description, timestamp FROM component_tbl WHERE component_id = ? AND company_id = ? ORDER BY compname ASC";

		try {
			return selectObjectDefaultNull(componentSelect, new MailingComponentRowMapper(), compID, companyID);
		} catch (Exception e) {
			logger.error("Cannot read MailingComponent: " + e.getMessage(), e);
			return mailingComponentFactory.newMailingComponent();
		}
	}

	@Override
	public MailingComponent getMailingComponent(int mailingId, int componentId, int companyId) {
		String sqlGetComponent = "SELECT company_id, mailing_id, component_id, compname, comptype, comppresent, emmblock, binblock, mtype, target_id, url_id, description, timestamp " +
				"FROM component_tbl WHERE mailing_id = ? AND component_id = ? AND company_id = ?";

		return selectObjectDefaultNull(sqlGetComponent, new MailingComponentRowMapper(), mailingId, componentId, companyId);
	}

	@Override
	public MailingComponent getMailingComponentByName(int mailingID, int companyID, String name) {
		if (companyID == 0) {
			return null;
		}

		String componentSelect = "SELECT company_id, mailing_id, component_id, compname, comptype, comppresent, emmblock, binblock, mtype, target_id, url_id, description, timestamp FROM component_tbl WHERE (mailing_id = ? OR mailing_id = 0) AND company_id = ? AND compname = ? ORDER BY component_id DESC";

		try {
			List<MailingComponent> components = select(componentSelect, new MailingComponentRowMapper(), mailingID, companyID, name);
			if (!components.isEmpty()) {
				// return the first of all results
				return components.get(0);
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error("Error getting component " + name + " for mailing " + mailingID, e);
			return mailingComponentFactory.newMailingComponent();
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void saveMailingComponent(MailingComponent mailingComponent) {
		// TODO: What are these defaultvalues for? They are only written to DB on the first insert and will not be read again
		int mailtemplateID = 0;
		int comppresent = 1;
		
		// Check for unallowed html tags
		try {
			HtmlChecker.checkForNoHtmlTags(mailingComponent.getComponentName());
		} catch (HtmlCheckerException e) {
			throw new IllegalArgumentException("Mailing component name contains unallowed HTML tags");
		}
		try {
			HtmlChecker.checkForUnallowedHtmlTags(mailingComponent.getDescription(), false);
		} catch (HtmlCheckerException e) {
			throw new IllegalArgumentException("Mailing component description contains unallowed HTML tags");
		}
		try {
			HtmlChecker.checkForUnallowedHtmlTags(mailingComponent.getEmmBlock(), true);
		} catch (HtmlCheckerException e) {
			throw new IllegalArgumentException("Mailing component description contains unallowed HTML tags");
		}

		if (mailingComponent.getType() != MailingComponentType.Template && StringUtils.isNotBlank(mailingComponent.getLink()) && mailingComponent.getUrlID() == 0) {
			Set<String> existingLinkUrls = trackableLinkDao.getTrackableLinks(mailingComponent.getCompanyID(), mailingComponent.getMailingID())
				.stream().map(TrackableLink::getFullUrl).collect(Collectors.toSet());
			// Only create new link if its url does not exist by now
			if (!existingLinkUrls.contains(mailingComponent.getLink())) {
				final TrackableLink newTrackableLink = new TrackableLinkImpl();
				newTrackableLink.setCompanyID(mailingComponent.getCompanyID());
				newTrackableLink.setFullUrl(mailingComponent.getLink());
				newTrackableLink.setMailingID(mailingComponent.getMailingID());
				newTrackableLink.setUsage(this.configService.getIntegerValue(ConfigValue.TrackableLinkDefaultTracking, mailingComponent.getCompanyID()));
				newTrackableLink.setActionID(0);
				trackableLinkDao.saveTrackableLink(newTrackableLink);
				mailingComponent.setUrlID(newTrackableLink.getId());
			} else {
				final TrackableLink existingTrackableLink = trackableLinkDao.getTrackableLink(mailingComponent.getLink(), mailingComponent.getCompanyID(), mailingComponent.getMailingID());
				mailingComponent.setUrlID(existingTrackableLink.getId());
			}
		}

		validateDescription(mailingComponent.getDescription());

		if (mailingComponent.getId() == 0 || !exists(mailingComponent.getMailingID(), mailingComponent.getCompanyID(), mailingComponent.getId())) {
			mailingComponent.setTimestamp(new Date());

			if (isOracleDB()) {
				int newID = selectInt("SELECT component_tbl_seq.NEXTVAL FROM DUAL");
				String sql = "INSERT INTO component_tbl (component_id, mailing_id, company_id, compname, comptype, mtype, target_id, url_id, mailtemplate_id, comppresent, timestamp, description) VALUES (" + AgnUtils.repeatString("?", 12, ", ") + ")";
				int touchedLines = update(sql, newID, mailingComponent.getMailingID(), mailingComponent.getCompanyID(), mailingComponent.getComponentName(), mailingComponent.getType().getCode(), mailingComponent.getMimeType(), mailingComponent.getTargetID(), mailingComponent.getUrlID(), mailtemplateID, comppresent, mailingComponent.getTimestamp(), mailingComponent.getDescription());
				if (touchedLines != 1) {
					throw new IllegalStateException("Illegal insert result");
				} else {
					try {
						updateBlob("UPDATE component_tbl SET binblock = ? WHERE component_id = ?", mailingComponent.getBinaryBlock(), newID);
						updateClob("UPDATE component_tbl SET emmblock = ? WHERE component_id = ?", mailingComponent.getEmmBlock(), newID);
					} catch (Exception e) {
						logger.error(String.format("Error saving mailing component %d (mailing ID %d, company ID %d)", mailingComponent.getId(), mailingComponent.getMailingID(), mailingComponent.getCompanyID()), e);
						update("DELETE FROM component_tbl WHERE component_id = ?", newID);
						throw e;
					}
				}

				mailingComponent.setId(newID);
			} else {
				String insertStatement = "INSERT INTO component_tbl (mailing_id, company_id, compname, comptype, mtype, target_id, url_id, mailtemplate_id, comppresent, timestamp, description) VALUES (" + AgnUtils.repeatString("?", 11, ", ") + ")";
				int newID = insert("component_id", insertStatement, mailingComponent.getMailingID(), mailingComponent.getCompanyID(), mailingComponent.getComponentName(), mailingComponent.getType().getCode(), mailingComponent.getMimeType(), mailingComponent.getTargetID(), mailingComponent.getUrlID(), mailtemplateID, comppresent, mailingComponent.getTimestamp(), mailingComponent.getDescription());
				try {
					updateBlob("UPDATE component_tbl SET binblock = ? WHERE component_id = ?", mailingComponent.getBinaryBlock(), newID);
					updateClob("UPDATE component_tbl SET emmblock = ? WHERE component_id = ?", mailingComponent.getEmmBlock(), newID);
				} catch (Exception e) {
					update("DELETE FROM component_tbl WHERE component_id = ?", newID);
					throw e;
				}
				mailingComponent.setId(newID);
			}

		} else {
			mailingComponent.setTimestamp(new Date());

			String sql = "UPDATE component_tbl SET mailing_id = ?, company_id = ?, compname = ?, comptype = ?, mtype = ?, target_id = ?, url_id = ?, comppresent = ?, timestamp = ?, description = ? WHERE component_id = ?";
			int touchedLines = update(sql, mailingComponent.getMailingID(), mailingComponent.getCompanyID(), mailingComponent.getComponentName(), mailingComponent.getType().getCode(), mailingComponent.getMimeType(), mailingComponent.getTargetID(), mailingComponent.getUrlID(), comppresent, mailingComponent.getTimestamp(), mailingComponent.getDescription(), mailingComponent.getId());
			if (touchedLines != 1) {
				throw new IllegalStateException("Illegal update result");
			} else {
				updateBlob("UPDATE component_tbl SET binblock = ? WHERE component_id = ?", mailingComponent.getBinaryBlock(), mailingComponent.getId());
				updateClob("UPDATE component_tbl SET emmblock = ? WHERE component_id = ?", mailingComponent.getEmmBlock(), mailingComponent.getId());
			}
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteMailingComponent(MailingComponent comp) {
		String sql = "DELETE FROM component_tbl WHERE component_id = ?";
		try {
			update(sql, comp.getId());
		} catch (Exception e) {
			logger.error("Error deleting component " + comp.getId(), e);
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteMailingComponents(List<MailingComponent> components) {
		if (!components.isEmpty()) {
			String sql = "DELETE FROM component_tbl WHERE component_id IN (" + AgnUtils.repeatString("?", components.size(), ", ") + ")";
			Object[] componentsIds = components.stream()
					.map(MailingComponent::getId)
					.toArray();

			update(sql, componentsIds);
		}
	}

	@Override
	public Map<Integer, Integer> getImageSizes(int companyID, int mailingID) {
		Map<Integer, Integer> map = new HashMap<>();
		Object[] sqlParameters = new Object[] { companyID, mailingID, MailingComponentType.Image.getCode(), MailingComponentType.HostedImage.getCode() };
		String sql;

		if (isOracleDB()) {
			sql = "SELECT COALESCE(DBMS_LOB.GETLENGTH(binblock), 0) image_size, component_id FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND comptype IN (?, ?)";
		} else {
			sql = "SELECT COALESCE(OCTET_LENGTH(binblock), 0) image_size, component_id FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND comptype IN (?, ?)";
		}

		query(sql, rs -> map.put(rs.getInt("component_id"), rs.getInt("image_size")), sqlParameters);
		return map;
	}

	@Override
	public Map<Integer, String> getImageNames(int companyId, int mailingId, boolean includeExternalImages) {
		if (companyId <= 0 || mailingId <= 0) {
			return Collections.emptyMap();
		}

		String sqlGetNames = "SELECT component_id, compname FROM component_tbl WHERE company_id = ? AND mailing_id = ?";
		List<Object> sqlParameters = new ArrayList<>(Arrays.asList(companyId, mailingId));

		if (includeExternalImages) {
			sqlGetNames += " AND comptype IN (?, ?)";
			sqlParameters.add(MailingComponentType.Image.getCode());
			sqlParameters.add(MailingComponentType.HostedImage.getCode());
		} else {
			sqlGetNames += " AND comptype = ?";
			sqlParameters.add(MailingComponentType.HostedImage.getCode());
		}

		Map<Integer, String> map = new HashMap<>();
		query(sqlGetNames, rs -> map.put(rs.getInt("component_id"), rs.getString("compname")), sqlParameters.toArray());
		return map;
	}

	@Override
	public Date getComponentTime(int companyID, int mailingID, String name) {
		String sql = "SELECT timestamp FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND compname = ?";
		try {
			return select(sql, Date.class, companyID, mailingID, name);
		} catch (Exception e) {
			logger.error("Error getting time of component " + name, e);
			return null;
		}
	}

	@Override
	public List<MailingComponent> getMailingComponentsByType(MailingComponentType componentType, int companyID) {
		String componentSelect = "SELECT company_id, mailing_id, component_id, compname, comptype, comppresent, emmblock, binblock, mtype, target_id, url_id, description, timestamp FROM component_tbl WHERE company_id = ? AND comptype = ? ORDER BY compname ASC";
		return select(componentSelect, new MailingComponentRowMapper(), companyID, componentType.getCode());
	}

	private boolean exists(int mailingID, int companyID, int componentID) {
		String sql = "SELECT COUNT(component_id) FROM component_tbl WHERE mailing_id = ? AND company_id = ? AND component_id = ?";
		int total = selectInt(sql, mailingID, companyID, componentID);
		return total > 0;
	}

	@Override
	public boolean attachmentExists(int companyId, int mailingId, String name, int targetId) {
		return selectInt(
				"""
						SELECT COUNT(*)
						FROM component_tbl
						WHERE (comptype = ? OR comptype = ?)
						  AND compname = ?
						  AND target_id = ?
						  AND mailing_id = ?
						  AND company_id = ?
						""",
				MailingComponentType.Attachment.getCode(),
				MailingComponentType.PersonalizedAttachment.getCode(),
				name, targetId, mailingId, companyId) > 0;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteMailingComponentsByCompanyID(int companyID) {
		String deleteSQL = "DELETE FROM component_tbl WHERE company_id = ?";
		int affectedRows = update(deleteSQL, companyID);
		if(affectedRows > 0) {
			return true;
		} else {
			int remainingComponents = selectInt("SELECT COUNT(*) FROM component_tbl WHERE company_id = ?", companyID);
			return remainingComponents == 0;
		}
	}

	@Override
	public MailingComponent findComponent(int id, int companyId) {
		String query = "SELECT company_id, mailing_id, component_id, compname, comppresent, comptype, mtype, target_id, url_id, description, timestamp " +
				"FROM component_tbl " +
				"WHERE component_id = ? AND company_id = ?";

		return selectObjectDefaultNull(query, new MailingComponentRowMapper(false), id, companyId);
	}

	@Override
	public List<Integer> filterComponentsOfNotSentMailings(List<Integer> components) {
		if (components.isEmpty()) {
			return Collections.emptyList();
		}

		String componentsIdsInClause = makeBulkInClauseForInteger("c.component_id", components);

		String query = "SELECT c.component_id " +
				"FROM mailing_tbl m " +
				"         INNER JOIN component_tbl c ON c.mailing_id = m.mailing_id " +
				"WHERE m.work_status != ? " +
				"  AND " + componentsIdsInClause;

		return select(query, IntegerRowMapper.INSTANCE, MailingStatus.SENT.getDbKey());
	}

	@Override
	public List<Integer> findTargetDependentMailingsComponents(int targetGroupId, int companyId) {
		String sql = "SELECT c.component_id " +
				"FROM mailing_tbl m INNER JOIN component_tbl c " +
				"    ON c.mailing_id = m.mailing_id " +
				"WHERE m.company_id = ? AND m.deleted = 0 AND c.target_id = ?";

		return select(sql, IntegerRowMapper.INSTANCE, companyId, targetGroupId);
	}

	@Override
	public void deleteMailingComponentsByMailing(int mailingID) {
		String deleteSQL = "DELETE FROM component_tbl WHERE mailing_id = ?";
		update(deleteSQL, mailingID);
	}

	@Override
	public boolean deleteImages(int companyId, int mailingId, Set<Integer> bulkIds) {
		if (CollectionUtils.isEmpty(bulkIds)) {
			return false;
		}

		String sqlDeleteImages = "DELETE FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND comptype in (?, ?) AND component_id IN (" + StringUtils.join(bulkIds, ", ") + ")";

		return update(sqlDeleteImages, companyId, mailingId, MailingComponentType.HostedImage.getCode(), MailingComponentType.Image.getCode()) > 0;
	}

	@Override
	public List<String> getImagesNames(int mailingId, Set<Integer> ids, int companyID) {
		String query = "SELECT compname FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND comptype in (?, ?)";
		if (CollectionUtils.isNotEmpty(ids)) {
			query += " AND " + makeBulkInClauseForInteger("component_id", ids);
		}

		return select(query, StringRowMapper.INSTANCE, companyID, mailingId, MailingComponentType.HostedImage.getCode(), MailingComponentType.Image.getCode());
	}

	@Override
	public PaginatedList<MailingComponent> getImagesOverview(int companyID, int mailingID, MailingImagesOverviewFilter filter) {
		StringBuilder query = new StringBuilder("SELECT company_id, mailing_id, component_id, compname, comptype, comppresent, emmblock, binblock, mtype, target_id, url_id, description, timestamp, ");

		if (isOracleDB()) {
			query.append("COALESCE(DBMS_LOB.GETLENGTH(binblock), 0) comp_size");
		} else {
			query.append("COALESCE(OCTET_LENGTH(binblock), 0) comp_size");
		}

		query.append(" FROM component_tbl");

		List<Object> params = applyOverviewFilter(filter, mailingID, companyID, query);
		String sortTable = "comp_size".equals(filter.getSort()) ? "" : "component_tbl";

		if (StringUtils.isBlank(filter.getSort())) {
			query.append(" ORDER BY comptype DESC, compname ASC");
		}

		PaginatedList<MailingComponent> list = selectPaginatedList(query.toString(), sortTable, filter,
				new MailingComponentRowMapper(), params.toArray());

		if (filter.isUiFiltersSet()) {
			list.setNotFilteredFullListSize(getTotalUnfilteredCountForOverview(mailingID, companyID));
		}

		return list;
	}

	@Override
	public List<String> getMailingImagesNamesForMobileAlternative(int mailingId, int companyId) {
		StringBuilder query = new StringBuilder("SELECT compname FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND comptype IN (?, ?)")
				.append(" AND compname NOT LIKE '").append(MOBILE_IMAGE_PREFIX).append("%' ORDER BY comptype DESC, compname ASC");
		return select(query.toString(), StringRowMapper.INSTANCE, companyId, mailingId, MailingComponentType.HostedImage.getCode(), MailingComponentType.Image.getCode());
	}

	private List<Object> applyOverviewFilter(MailingImagesOverviewFilter filter, int mailingId, int companyId, StringBuilder query) {
		List<Object> params = applyRequiredOverviewFilter(query, mailingId, companyId);

		if (StringUtils.isNotBlank(filter.getFileName())) {
			query.append(getPartialSearchFilterWithAnd("compname"));
			params.add(filter.getFileName());
		}

		query.append(getDateRangeFilterWithAnd("timestamp", filter.getUploadDate(), params));

		if (CollectionUtils.isNotEmpty(filter.getMimetypes())) {
			query.append(" AND mtype IN (").append(AgnUtils.csvQMark(filter.getMimetypes().size())).append(")");
			params.addAll(filter.getMimetypes());
		}

		if (filter.getMobile() != null) {
			query.append(" AND compname ");
			if (!filter.getMobile()) {
				query.append("NOT ");
			}
			query.append("LIKE '").append(MOBILE_IMAGE_PREFIX).append("%'");
		}

		return params;
	}

	private int getTotalUnfilteredCountForOverview(int mailingId, int companyId) {
		StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM component_tbl");
		List<Object> params = applyRequiredOverviewFilter(query, mailingId, companyId);

		return selectIntWithDefaultValue(query.toString(), 0, params.toArray());
	}

	private List<Object> applyRequiredOverviewFilter(StringBuilder query, int mailingId, int companyId) {
		query.append(" WHERE company_id = ? AND mailing_id = ? AND comptype IN (?, ?)");
		return new ArrayList<>(List.of(companyId, mailingId, MailingComponentType.HostedImage.getCode(), MailingComponentType.Image.getCode()));
	}

	@Override
    public List<MailingComponent> getMailingComponentsByType(int companyID, int mailingID, List<MailingComponentType> types) {
		if (CollectionUtils.isEmpty(types)) {
			return new ArrayList<>();
		}
        String componentSelect = "SELECT company_id, mailing_id, component_id, compname, comptype, comppresent, emmblock, binblock, mtype, target_id, url_id, description, timestamp FROM component_tbl WHERE company_id = ? AND mailing_id = ?"
        	+ " AND comptype IN (" + types.stream().map(e -> Integer.toString(e.getCode())).collect(Collectors.joining(", ")) + ") ORDER BY comptype DESC, compname ASC";

		return select(componentSelect, new MailingComponentRowMapper(), companyID, mailingID);
    }

	protected class MailingComponentRowMapper implements RowMapper<MailingComponent> {

		private final boolean includeContent;

		public MailingComponentRowMapper() {
			this(true);
		}

		public MailingComponentRowMapper(boolean includeContent) {
			this.includeContent = includeContent;
		}

		@Override
		public MailingComponent mapRow(ResultSet resultSet, int index) throws SQLException {
			MailingComponent component = mailingComponentFactory.newMailingComponent();

			component.setCompanyID(resultSet.getInt("company_id"));
			component.setMailingID(resultSet.getInt("mailing_id"));
			component.setId(resultSet.getInt("component_id"));
			component.setComponentName(resultSet.getString("compname"));
			component.setType(MailingComponentType.getMailingComponentTypeByCode(resultSet.getInt("comptype")));
			component.setPresent(resultSet.getInt("comppresent"));
			component.setTargetID(resultSet.getInt("target_id"));
			component.setUrlID(resultSet.getInt("url_id"));
			component.setDescription(resultSet.getString("description"));
			component.setTimestamp(resultSet.getTimestamp("timestamp"));

			if (includeContent) {
				if (isPostgreSQL()) { // TODO: 6533: Try to use this block for mariaDB and OracleDB in future
					byte[] bytes = resultSet.getBytes("binblock");

					// binblock sometimes contains an array "byte[1] = {0}", which also signals empty binary data

					// Only store one of type of data: emmblock or binblock
					// Exemption: Personalized PDF attachments require emmblock and binblock to be filled with different files

					if (bytes != null && bytes.length > 1) {
						component.setBinaryBlock(bytes, resultSet.getString("mtype"));
					}

					if (bytes == null || bytes.length <= 1 || "application/pdf".equalsIgnoreCase(resultSet.getString("mtype"))) {
						component.setEmmBlock(resultSet.getString("emmblock"), resultSet.getString("mtype"));
					}
				} else {
					Blob blob = resultSet.getBlob("binblock");
					// binblock sometimes contains an array "byte[1] = {0}", which also signals empty binary data

					// Only store one of type of data: emmblock or binblock
					// Exemption: Personalized PDF attachments require emmblock and binblock to be filled with different files

					if (blob != null && blob.length() > 1) {
						try (InputStream dataStream = blob.getBinaryStream()) {
							byte[] data = IOUtils.toByteArray(dataStream);
							component.setBinaryBlock(data, resultSet.getString("mtype"));
						} catch (Exception ex) {
							logger.error("Error:" + ex, ex);
						}
					}

					if (blob == null || blob.length() <= 1 || "application/pdf".equalsIgnoreCase(resultSet.getString("mtype"))) {
						component.setEmmBlock(resultSet.getString("emmblock"), resultSet.getString("mtype"));
					}
				}
			}

			if (DbUtilities.resultsetHasColumn(resultSet, "comp_size")) {
				component.setSize(resultSet.getInt("comp_size"));
			}

			return component;
		}
	}

	@Override
    public int getImageComponent(int companyId, int mailingId, MailingComponentType componentType) {
		String query = "SELECT component_id FROM component_tbl " +
				"WHERE company_id = ? AND mailing_id = ? AND comptype = ? " +
				"ORDER BY timestamp DESC";

        return selectInt(addRowLimit(query, 1), companyId, mailingId, componentType.getCode());
    }

	@Override
	public List<MailingComponent> getPreviewHeaderComponents(int mailingID, int companyID) {
		// Using "SELECT * ...", because of flexible used fields in RowMapper
		return select("SELECT * FROM component_tbl WHERE (comptype = ? OR comptype = ?) AND mailing_id = ? AND company_id = ? ORDER BY component_id", new MailingComponentRowMapper(),
				MailingComponentType.Attachment.getCode(), MailingComponentType.PersonalizedAttachment.getCode(), mailingID, companyID);
	}

	@Override
	public void updateHostImage(int mailingID, int companyID, int componentID, byte[] imageBytes) {
		try {
			String sql = "UPDATE component_tbl SET timestamp = ? WHERE component_id = ?";
			int touchedLines = update(sql, new Date(), componentID);
			if (touchedLines != 1) {
				throw new IllegalStateException("Illegal insert result");
			} else {
				updateBlob("UPDATE component_tbl SET binblock = ? WHERE component_id = ?", imageBytes, componentID);
			}
		} catch (Exception e) {
			logger.error("Error saving component {}", componentID, e);
		}
	}

	@Override
	public CdnImage getCdnImage(int companyID, int mailingID, String imageName, boolean mobile) {
		if (companyID == 0) {
			return null;
		}
		
		List<Map<String, Object>> results = null;
		if (mobile) {
			if (isOracleDB()) {
				results = select("SELECT component_id, cdn_id, DBMS_LOB.GETLENGTH(binblock) AS blobsize FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND compname = ?", companyID, mailingID, MOBILE_IMAGE_PREFIX + imageName);
			} else {
				results = select("SELECT component_id, cdn_id, OCTET_LENGTH(binblock) AS blobsize FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND compname = ?", companyID, mailingID, MOBILE_IMAGE_PREFIX + imageName);
			}
		}
		if (results == null || results.isEmpty()) {
			if (isOracleDB()) {
				results = select("SELECT component_id, cdn_id, DBMS_LOB.GETLENGTH(binblock) AS blobsize FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND compname = ?", companyID, mailingID, imageName);
			} else {
				results = select("SELECT component_id, cdn_id, OCTET_LENGTH(binblock) AS blobsize FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND compname = ?", companyID, mailingID, imageName);
			}
		}
		if (results.isEmpty()) {
			return null;
		} else {
			Map<String, Object> result = results.get(0);
			String cdnID = (String) result.get("cdn_id");
			if (cdnID == null) {
				int componentID = ((Number) result.get("component_id")).intValue();
				cdnID = AgnUtils.generateNewUUID().toString().replace("-", "").toUpperCase();
				int touchedLines = update("UPDATE component_tbl SET cdn_id = ? WHERE company_id = ? AND mailing_id = ? AND component_id = ? AND cdn_id IS NULL", cdnID, companyID, mailingID, componentID);
				if (touchedLines == 0) {
					if (mobile) {
						if (isOracleDB()) {
							results = select("SELECT component_id, cdn_id, DBMS_LOB.GETLENGTH(binblock) AS blobsize FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND compname = ?", companyID, mailingID, MOBILE_IMAGE_PREFIX + imageName);
						} else {
							results = select("SELECT component_id, cdn_id, OCTET_LENGTH(binblock) AS blobsize FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND compname = ?", companyID, mailingID, MOBILE_IMAGE_PREFIX + imageName);
						}
					}
					if (results == null || results.isEmpty()) {
						if (isOracleDB()) {
							results = select("SELECT component_id, cdn_id, DBMS_LOB.GETLENGTH(binblock) AS blobsize FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND compname = ?", companyID, mailingID, imageName);
						} else {
							results = select("SELECT component_id, cdn_id, OCTET_LENGTH(binblock) AS blobsize FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND compname = ?", companyID, mailingID, imageName);
						}
					}
					result = results.get(0);
					cdnID = (String) result.get("cdn_id");
				}
			}
	
			CdnImage cdnImage = new CdnImage();
			cdnImage.name = imageName;
			cdnImage.cdnId = cdnID;
			cdnImage.imageDatalength = ((Number) result.get("blobsize")).intValue();
			return cdnImage;
		}
	}

	@Override
	public MailingComponent getComponentByCdnID(String cdnID) {
		return selectObjectDefaultNull("SELECT company_id, mailing_id, component_id, compname, comptype, comppresent, emmblock, binblock, mtype, target_id, url_id, description, timestamp FROM component_tbl WHERE cdn_id = ?", new MailingComponentRowMapper(), cdnID);
	}

	@Override
	public int setUnPresentComponentsForMailing(int mailingId, List<MailingComponent> presentComponents) {
		if(presentComponents == null || presentComponents.isEmpty()) {
			return 0;
		}

		StringBuilder idBuilder = new StringBuilder();

        for (int i = 0; i < presentComponents.size(); i++) {
            idBuilder.append(presentComponents.get(i).getId());

            if (i < presentComponents.size() - 1) {
                idBuilder.append(", ");
            }
        }

		String sql = "UPDATE component_tbl SET comppresent = 0 WHERE mailing_id = ? AND component_id NOT IN (" + idBuilder.toString() + ")";
        return update(sql, mailingId);
	}

	@Override
	public boolean updateBinBlockBulk(int companyId, Collection<Integer> mailingIds, MailingComponentType componentType, Collection<String> namePatterns, byte[] value) {
		if (companyId < 0 || CollectionUtils.isEmpty(mailingIds) || CollectionUtils.isEmpty(namePatterns)) {
			return false;
		}

		List<Object> sqlParameters = new ArrayList<>();

		sqlParameters.add(companyId);
		sqlParameters.add(componentType.getCode());
		sqlParameters.addAll(namePatterns);

		String sqlFilterByMailingId = DbUtilities.makeBulkInClauseWithDelimiter(isOracleDB(), "mailing_id", mailingIds, null, true);
		String sqlFilterByName = AgnUtils.repeatString("compname LIKE ?", namePatterns.size(), " OR ");
		
		updateBlob("UPDATE component_tbl SET binblock = ? WHERE company_id = ? AND comptype = ? AND " + sqlFilterByMailingId + " AND (" + sqlFilterByName + ")", value, sqlParameters.toArray());
		
		return update("UPDATE component_tbl SET timestamp = CURRENT_TIMESTAMP WHERE company_id = ? AND comptype = ? AND " + sqlFilterByMailingId + " AND (" + sqlFilterByName + ")", sqlParameters.toArray()) > 0;
	}

	private void validateDescription(String description) {
		if (description != null && description.length() > 200) {
			throw new IllegalArgumentException("Value for component_tbl.description is to long (Maximum: 200, Current: " + description.length() + ")");
		}
	}

}
