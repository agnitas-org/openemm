/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

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
import java.util.Set;
import java.util.stream.Collectors;

import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.MailingComponentType;
import org.agnitas.beans.TrackableLink;
import org.agnitas.beans.factory.MailingComponentFactory;
import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.impl.ComTrackableLinkImpl;
import com.agnitas.dao.ComMailingComponentDao;
import com.agnitas.dao.ComTrackableLinkDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.util.ImageUtils;
import com.agnitas.web.CdnImage;

public class ComMailingComponentDaoImpl extends BaseDaoImpl implements ComMailingComponentDao {
	/**
	 * The logger.
	 */
	private static final transient Logger logger = Logger.getLogger(ComMailingComponentDaoImpl.class);
	
	/**
	 * Factory to create new mailing components.
	 */
	protected MailingComponentFactory mailingComponentFactory;
	
	private ComTrackableLinkDao comTrackableLinkDao;
	
	/**
	 * Set factory to create new mailing components.
	 *
	 * @param mailingComponentFactory factory to create new mailing components
	 */
	public void setMailingComponentFactory(MailingComponentFactory mailingComponentFactory) {
		this.mailingComponentFactory = mailingComponentFactory;
	}

	@Required
	public void setTrackableLinkDao(ComTrackableLinkDao comTrackableLinkDao) {
		this.comTrackableLinkDao = comTrackableLinkDao;
	}

	@Override
	public List<MailingComponent> getMailingComponents(int mailingID, @VelocityCheck int companyID, MailingComponentType componentType) {
		return getMailingComponents(mailingID, companyID, componentType, true);
	}

	@Override
	public List<MailingComponent> getMailingComponents(int mailingID, @VelocityCheck int companyID, MailingComponentType componentType, boolean includeContent) {
		String sqlGetComponents = "SELECT company_id, mailing_id, component_id, compname, comppresent, comptype, mtype, target_id, url_id, description, timestamp" +
				(includeContent ? ", emmblock, binblock " : " ") +
				"FROM component_tbl " +
				"WHERE company_id = ? AND mailing_id = ? AND comptype = ? " +
				"ORDER BY compname ASC";

		return select(logger, sqlGetComponents, new MailingComponentRowMapper(includeContent), companyID, mailingID, componentType.getCode());
	}

	@Override
	public List<MailingComponent> getMailingComponents(int mailingID, @VelocityCheck int companyID) {
		return getMailingComponents(mailingID, companyID, true);
	}

	@Override
	public List<MailingComponent> getMailingComponents(int mailingID, @VelocityCheck int companyID, boolean includeContent) {
		String componentSelect = "SELECT company_id, mailing_id, component_id, compname, comptype, comppresent, mtype, target_id, url_id, description, timestamp" +
				(includeContent ? ", emmblock, binblock " : " ") +
				" FROM component_tbl WHERE company_id = ? AND mailing_id = ? ORDER BY compname ASC";

		List<MailingComponent> mailingComponentList = select(logger, componentSelect, new MailingComponentRowMapper(includeContent), companyID, mailingID);

		// Sort results (mobile components after their base components)
		Collections.sort(mailingComponentList, (mailingComponent1, mailingComponent2) -> {
			String name1 = mailingComponent1.getComponentName();
			if (name1.startsWith(ImageUtils.MOBILE_IMAGE_PREFIX)) {
				name1 = name1.substring(ImageUtils.MOBILE_IMAGE_PREFIX.length());
			}
			String name2 = mailingComponent2.getComponentName();
			if (name2.startsWith(ImageUtils.MOBILE_IMAGE_PREFIX)) {
				name2 = name2.substring(ImageUtils.MOBILE_IMAGE_PREFIX.length());
			}
			if (name1.equals(name2)) {
				if (mailingComponent1.getComponentName().startsWith(ImageUtils.MOBILE_IMAGE_PREFIX)) {
					return mailingComponent2.getComponentName().startsWith(ImageUtils.MOBILE_IMAGE_PREFIX) ? 0 : 1;
				} else {
					return mailingComponent2.getComponentName().startsWith(ImageUtils.MOBILE_IMAGE_PREFIX) ? -1 : 0;
				}
			} else {
				return name1.compareTo(name2);
			}
		});

		return mailingComponentList;
	}

	@Override
    public List<MailingComponent> getMailingComponents(@VelocityCheck int companyID, int mailingID, Set<Integer> componentIds) {
		String sqlGetComponents = "SELECT company_id, mailing_id, component_id, compname, comppresent, comptype, mtype, target_id, url_id, description, timestamp, emmblock, binblock " +
				"FROM component_tbl " +
				"WHERE company_id = ? AND mailing_id = ? AND " + makeBulkInClauseForInteger("component_id", componentIds) +
				"ORDER BY compname ASC";
		
		return select(logger, sqlGetComponents, new MailingComponentRowMapper(true), companyID, mailingID);
    }
    
    @Override
	public MailingComponent getMailingComponent(int compID, @VelocityCheck int companyID) {
		if (companyID == 0) {
			return null;
		}
		
		String componentSelect = "SELECT company_id, mailing_id, component_id, compname, comptype, comppresent, emmblock, binblock, mtype, target_id, url_id, description, timestamp FROM component_tbl WHERE component_id = ? AND company_id = ? ORDER BY compname ASC";
		
		try {
			return selectObjectDefaultNull(logger, componentSelect, new MailingComponentRowMapper(), compID, companyID);
		} catch (Exception e) {
			logger.error("Cannot read MailingComponent: " + e.getMessage(), e);
			javaMailService.sendExceptionMail("SQL: " + componentSelect, e);
			return mailingComponentFactory.newMailingComponent();
		}
	}

	@Override
	public MailingComponent getMailingComponent(int mailingId, int componentId, @VelocityCheck int companyId) {
		String sqlGetComponent = "SELECT company_id, mailing_id, component_id, compname, comptype, comppresent, emmblock, binblock, mtype, target_id, url_id, description, timestamp " +
				"FROM component_tbl " +
				"WHERE mailing_id = ? AND component_id = ? AND company_id = ?";

		return selectObjectDefaultNull(logger, sqlGetComponent, new MailingComponentRowMapper(), mailingId, componentId, companyId);
	}

	@Override
	public MailingComponent getMailingComponentByName(int mailingID, @VelocityCheck int companyID, String name) {
		if (companyID == 0) {
			return null;
		}
		
		String componentSelect = "SELECT company_id, mailing_id, component_id, compname, comptype, comppresent, emmblock, binblock, mtype, target_id, url_id, description, timestamp FROM component_tbl WHERE (mailing_id = ? OR mailing_id = 0) AND company_id = ? AND compname = ? ORDER BY component_id DESC";
		
		try {
			List<MailingComponent> components = select(logger, componentSelect, new MailingComponentRowMapper(), mailingID, companyID, name);
			if (components.size() > 0) {
				// return the first of all results
				return components.get(0);
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error("Error getting component " + name + " for mailing " + mailingID, e);
			javaMailService.sendExceptionMail("SQL: " + componentSelect, e);
			return mailingComponentFactory.newMailingComponent();
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void saveMailingComponent(MailingComponent mailingComponent) throws Exception {
		// TODO: What are these defaultvalues for? They are only written to DB on the first insert and will not be read again
		int mailtemplateID = 0;
		int comppresent = 1;

		try {
			if (mailingComponent.getType() != MailingComponentType.Template && StringUtils.isNotBlank(mailingComponent.getLink()) && mailingComponent.getUrlID() == 0) {
				Set<String> existingLinkUrls = comTrackableLinkDao.getTrackableLinks(mailingComponent.getCompanyID(), mailingComponent.getMailingID())
					.stream().map(ComTrackableLink::getFullUrl).collect(Collectors.toSet());
				// Only create new link if its url does not exist by now
				if (!existingLinkUrls.contains(mailingComponent.getLink())) {
					final ComTrackableLink newTrackableLink = new ComTrackableLinkImpl();
					newTrackableLink.setCompanyID(mailingComponent.getCompanyID());
					newTrackableLink.setFullUrl(mailingComponent.getLink());
					newTrackableLink.setMailingID(mailingComponent.getMailingID());
					newTrackableLink.setUsage(TrackableLink.TRACKABLE_TEXT_HTML);
					newTrackableLink.setActionID(0);
					comTrackableLinkDao.saveTrackableLink(newTrackableLink);
					mailingComponent.setUrlID(newTrackableLink.getId());
				} else {
					final ComTrackableLink existingTrackableLink = comTrackableLinkDao.getTrackableLink(mailingComponent.getLink(), mailingComponent.getCompanyID(), mailingComponent.getMailingID());
					mailingComponent.setUrlID(existingTrackableLink.getId());
				}
			}
			
			if (mailingComponent.getId() == 0 || !exists(mailingComponent.getMailingID(), mailingComponent.getCompanyID(), mailingComponent.getId())) {
                mailingComponent.setTimestamp(new Date());
                
                if (isOracleDB()) {
                	int newID = selectInt(logger, "SELECT component_tbl_seq.NEXTVAL FROM DUAL");
                	String sql = "INSERT INTO component_tbl (component_id, mailing_id, company_id, compname, comptype, mtype, target_id, url_id, mailtemplate_id, comppresent, timestamp, description) VALUES (" + AgnUtils.repeatString("?", 12, ", ") + ")";
                    int touchedLines = update(logger, sql, newID, mailingComponent.getMailingID(), mailingComponent.getCompanyID(), mailingComponent.getComponentName(), mailingComponent.getType().getCode(), mailingComponent.getMimeType(), mailingComponent.getTargetID(), mailingComponent.getUrlID(), mailtemplateID, comppresent, mailingComponent.getTimestamp(), mailingComponent.getDescription());
                    if (touchedLines != 1) {
                        throw new RuntimeException("Illegal insert result");
                    } else {
						try {
							updateBlob(logger, "UPDATE component_tbl SET binblock = ? WHERE component_id = ?", mailingComponent.getBinaryBlock(), newID);
							updateClob(logger, "UPDATE component_tbl SET emmblock = ? WHERE component_id = ?", mailingComponent.getEmmBlock(), newID);
						} catch (Exception e) {
							logger.error(String.format("Error saving mailing component %d (mailing ID %d, company ID %d)", mailingComponent.getId(), mailingComponent.getMailingID(), mailingComponent.getCompanyID()), e);
							
							update(logger, "DELETE FROM component_tbl WHERE component_id = ?", newID);
							throw e;
						}
					}

                    mailingComponent.setId(newID);
                } else {
                	String insertStatement = "INSERT INTO component_tbl (mailing_id, company_id, compname, comptype, mtype, target_id, url_id, mailtemplate_id, comppresent, timestamp, description) VALUES (" + AgnUtils.repeatString("?", 11, ", ") + ")";
                    int newID = insertIntoAutoincrementMysqlTable(logger, "component_id", insertStatement, mailingComponent.getMailingID(), mailingComponent.getCompanyID(), mailingComponent.getComponentName(), mailingComponent.getType().getCode(), mailingComponent.getMimeType(), mailingComponent.getTargetID(), mailingComponent.getUrlID(), mailtemplateID, comppresent, mailingComponent.getTimestamp(), mailingComponent.getDescription());
					try {
						updateBlob(logger, "UPDATE component_tbl SET binblock = ? WHERE component_id = ?", mailingComponent.getBinaryBlock(), newID);
						updateClob(logger, "UPDATE component_tbl SET emmblock = ? WHERE component_id = ?", mailingComponent.getEmmBlock(), newID);
					} catch (Exception e) {
						update(logger, "DELETE FROM component_tbl WHERE component_id = ?", newID);
						throw e;
					}
                    mailingComponent.setId(newID);
                }

			} else {
                mailingComponent.setTimestamp(new Date());
				
				String sql = "UPDATE component_tbl SET mailing_id = ?, company_id = ?, compname = ?, comptype = ?, mtype = ?, target_id = ?, url_id = ?, comppresent = ?, timestamp = ?, description = ? WHERE component_id = ?";
				int touchedLines = update(logger, sql, mailingComponent.getMailingID(), mailingComponent.getCompanyID(), mailingComponent.getComponentName(), mailingComponent.getType().getCode(), mailingComponent.getMimeType(), mailingComponent.getTargetID(), mailingComponent.getUrlID(), comppresent, mailingComponent.getTimestamp(), mailingComponent.getDescription(), mailingComponent.getId());
				if (touchedLines != 1) {
					throw new RuntimeException("Illegal update result");
				} else {
					updateBlob(logger, "UPDATE component_tbl SET binblock = ? WHERE component_id = ?", mailingComponent.getBinaryBlock(), mailingComponent.getId());
					updateClob(logger, "UPDATE component_tbl SET emmblock = ? WHERE component_id = ?", mailingComponent.getEmmBlock(), mailingComponent.getId());
				}
			}
		} catch (Exception e) {
			logger.error("Error saving component " + mailingComponent.getId() + " for mailing " + mailingComponent.getMailingID(), e);
			throw e;
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteMailingComponent(MailingComponent comp) {
		String sql = "DELETE FROM component_tbl WHERE component_id = ?";
		try {
			update(logger, sql, comp.getId());
		} catch (Exception e) {
			logger.error("Error deleting component " + comp.getId(), e);

			javaMailService.sendExceptionMail("SQL: " + sql + ", " + comp.getId(), e);
		}
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public void deleteMailingComponents(List<MailingComponent> components) {
		if (components.size() > 0) {
			String sql = "DELETE FROM component_tbl WHERE component_id IN (" + AgnUtils.repeatString("?", components.size(), ", ") + ")";
			List<Integer> componentsIds = components.stream().map(MailingComponent::getId).collect(Collectors.toList());
			update(logger, sql, componentsIds.toArray());
		}
	}

	@Override
	public Map<Integer, Integer> getImageSizes(@VelocityCheck int companyID, int mailingID) {
		return getImageComponentsSizes(companyID, mailingID);
	}

	@Override
	public Map<Integer, String> getImageNames(@VelocityCheck int companyId, int mailingId, boolean includeExternalImages) {
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
		query(logger, sqlGetNames, rs -> map.put(rs.getInt("component_id"), rs.getString("compname")), sqlParameters.toArray());
		return map;
	}

	@Override
	public Map<Integer, Integer> getImageComponentsSizes(@VelocityCheck int companyID, int mailingID) {
		Object[] sqlParameters = new Object[] { companyID, mailingID, MailingComponentType.Image.getCode(), MailingComponentType.HostedImage.getCode() };
		String sql;
		if (isOracleDB()) {
			sql = "SELECT COALESCE(DBMS_LOB.GETLENGTH(binblock), 0) image_size, component_id FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND comptype IN (?, ?)";
		} else {
			sql = "SELECT COALESCE(OCTET_LENGTH(binblock), 0) image_size, component_id FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND comptype IN (?, ?)";
		}

		Map<Integer, Integer> map = new HashMap<>();
		query(logger, sql, rs -> map.put(rs.getInt("component_id"), rs.getInt("image_size")), sqlParameters);
		return map;
	}

	@Override
	public Map<Integer, Date> getImageComponentsTimestamps(@VelocityCheck int companyID, int mailingID) {
		String sql = "SELECT timestamp, component_id FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND comptype IN (?, ?)";
		Object[] sqlParameters = new Object[]{companyID, mailingID, MailingComponentType.Image.getCode(), MailingComponentType.HostedImage.getCode()};

		Map<Integer, Date> map = new HashMap<>();
		query(logger, sql, rs -> map.put(rs.getInt("component_id"), rs.getTimestamp("timestamp")), sqlParameters);
		return map;
	}

	@Override
	public Date getComponentTime(@VelocityCheck int companyID, int mailingID, String name) {
		String sql = "SELECT timestamp FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND compname = ?";
		try {
			return select(logger, sql, Date.class, companyID, mailingID, name);
		} catch (Exception e) {
			logger.error("Error getting time of component " + name, e);
			javaMailService.sendExceptionMail("SQL: " + sql + ", " + companyID + ", " + mailingID + ", " + name, e);
			return null;
		}
	}

	@Override
	public List<MailingComponent> getMailingComponentsByType(MailingComponentType componentType, @VelocityCheck int companyID) {
		String componentSelect = "SELECT company_id, mailing_id, component_id, compname, comptype, comppresent, emmblock, binblock, mtype, target_id, url_id, description, timestamp FROM component_tbl WHERE company_id = ? AND comptype = ? ORDER BY compname ASC";
		return select(logger, componentSelect, new MailingComponentRowMapper(), companyID, componentType.getCode());
	}

	@Override
	public boolean exists(int mailingID, int companyID, int componentID) {
		String sql = "SELECT COUNT(component_id) FROM component_tbl WHERE mailing_id = ? AND company_id = ? AND component_id = ?";
		int total = selectInt(logger, sql, mailingID, companyID, componentID);
		return total > 0;
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteMailingComponentsByCompanyID(int companyID) {
		String deleteSQL = "DELETE FROM component_tbl WHERE company_id = ?";
		int affectedRows = update(logger, deleteSQL, companyID);
		if(affectedRows > 0) {
			return true;
		} else {
			int remainingComponents = selectInt(logger, "SELECT COUNT(*) FROM component_tbl WHERE company_id = ?", companyID);
			return remainingComponents == 0;
		}
	}
	
	@Override
	public void deleteMailingComponentsByMailing(int mailingID) {
		String deleteSQL = "DELETE FROM component_tbl WHERE mailing_id = ?";
		update(logger, deleteSQL, mailingID);
	}

	@Override
	public boolean deleteImages(@VelocityCheck int companyId, int mailingId, Set<Integer> bulkIds) {
		if (CollectionUtils.isEmpty(bulkIds)) {
			return false;
		}

		String sqlDeleteImages = "DELETE FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND comptype in (?, ?) AND component_id IN (" + StringUtils.join(bulkIds, ", ") + ")";

		return update(logger, sqlDeleteImages, companyId, mailingId, MailingComponentType.HostedImage.getCode(), MailingComponentType.Image.getCode()) > 0;
	}

    @Override
    public List<MailingComponent> getMailingComponentsByType(@VelocityCheck int companyID, int mailingID, List<MailingComponentType> types) {
		if (CollectionUtils.isEmpty(types)) {
			return new ArrayList<>();
		}
        String componentSelect = "SELECT company_id, mailing_id, component_id, compname, comptype, comppresent, emmblock, binblock, mtype, target_id, url_id, description, timestamp FROM component_tbl WHERE company_id = ? AND mailing_id = ?"
        	+ " AND comptype IN (" + types.stream().map(e -> Integer.toString(e.getCode())).collect(Collectors.joining(", ")) + ") ORDER BY comptype DESC, compname ASC";
        
        List<MailingComponent> mailingComponentList = select(logger, componentSelect, new MailingComponentRowMapper(), companyID, mailingID);

        return mailingComponentList;
    }

	protected class MailingComponentRowMapper implements RowMapper<MailingComponent> {
		private boolean includeContent;

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
			try {
				component.setType(MailingComponentType.getMailingComponentTypeByCode(resultSet.getInt("comptype")));
			} catch (Exception e) {
				throw new SQLException("Invalid mailing component type found: " + resultSet.getInt("comptype"), e);
			}
			component.setPresent(resultSet.getInt("comppresent"));
			component.setTargetID(resultSet.getInt("target_id"));
			component.setUrlID(resultSet.getInt("url_id"));
			component.setDescription(resultSet.getString("description"));
			component.setTimestamp(resultSet.getTimestamp("timestamp"));

			if (includeContent) {
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

			return component;
		}
	}

	@Override
    public int getImageComponent(@VelocityCheck int companyId, int mailingId, MailingComponentType componentType) {
		String sqlGetComponentId = "SELECT component_id FROM component_tbl " +
				"WHERE company_id = ? AND mailing_id = ? AND comptype = ? " +
				"ORDER BY timestamp DESC";

		if (isOracleDB()) {
			sqlGetComponentId = "SELECT component_id FROM (" + sqlGetComponentId + ") WHERE rownum = 1";
		} else {
			sqlGetComponentId += " LIMIT 1";
		}

        return selectInt(logger, sqlGetComponentId, companyId, mailingId, componentType.getCode());
    }

	@Override
	public List<MailingComponent> getPreviewHeaderComponents(int mailingID, @VelocityCheck int companyID) {
		return select(logger, "SELECT * FROM component_tbl WHERE (comptype = ? OR comptype = ?) AND mailing_id = ? AND company_id = ? ORDER BY component_id", new MailingComponentRowMapper(),
				MailingComponentType.Attachment.getCode(), MailingComponentType.PersonalizedAttachment.getCode(), mailingID, companyID);
	}

	@Override
	public void updateHostImage(int mailingID, @VelocityCheck int companyID, int componentID, byte[] imageBytes) {
		try {
			String sql = "UPDATE component_tbl SET timestamp = ? WHERE component_id = ?";
			int touchedLines = update(logger, sql, new Date(), componentID);
			if (touchedLines != 1) {
				throw new RuntimeException("Illegal insert result");
			} else {
				updateBlob(logger, "UPDATE component_tbl SET binblock = ? WHERE component_id = ?", imageBytes, componentID);
			}
		} catch (Exception e) {
			logger.error("Error saving component " + componentID, e);
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
				results = select(logger, "SELECT component_id, cdn_id, DBMS_LOB.GETLENGTH(binblock) AS blobsize FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND compname = ?", companyID, mailingID, ImageUtils.MOBILE_IMAGE_PREFIX + imageName);
			} else {
				results = select(logger, "SELECT component_id, cdn_id, OCTET_LENGTH(binblock) AS blobsize FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND compname = ?", companyID, mailingID, ImageUtils.MOBILE_IMAGE_PREFIX + imageName);
			}
		}
		if (results == null || results.size() == 0) {
			if (isOracleDB()) {
				results = select(logger, "SELECT component_id, cdn_id, DBMS_LOB.GETLENGTH(binblock) AS blobsize FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND compname = ?", companyID, mailingID, imageName);
			} else {
				results = select(logger, "SELECT component_id, cdn_id, OCTET_LENGTH(binblock) AS blobsize FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND compname = ?", companyID, mailingID, imageName);
			}
		}
		if (results.size() <= 0) {
			return null;
		} else {
			Map<String, Object> result = results.get(0);
			String cdnID = (String) result.get("cdn_id");
			if (cdnID == null) {
				int componentID = ((Number) result.get("component_id")).intValue();
				cdnID = AgnUtils.generateNewUUID().toString().replace("-", "").toUpperCase();
				int touchedLines = update(logger, "UPDATE component_tbl SET cdn_id = ? WHERE company_id = ? AND mailing_id = ? AND component_id = ? AND cdn_id IS NULL", cdnID, companyID, mailingID, componentID);
				if (touchedLines == 0) {
					if (mobile) {
						if (isOracleDB()) {
							results = select(logger, "SELECT component_id, cdn_id, DBMS_LOB.GETLENGTH(binblock) AS blobsize FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND compname = ?", companyID, mailingID, ImageUtils.MOBILE_IMAGE_PREFIX + imageName);
						} else {
							results = select(logger, "SELECT component_id, cdn_id, OCTET_LENGTH(binblock) AS blobsize FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND compname = ?", companyID, mailingID, ImageUtils.MOBILE_IMAGE_PREFIX + imageName);
						}
					}
					if (results == null || results.size() == 0) {
						if (isOracleDB()) {
							results = select(logger, "SELECT component_id, cdn_id, DBMS_LOB.GETLENGTH(binblock) AS blobsize FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND compname = ?", companyID, mailingID, imageName);
						} else {
							results = select(logger, "SELECT component_id, cdn_id, OCTET_LENGTH(binblock) AS blobsize FROM component_tbl WHERE company_id = ? AND mailing_id = ? AND compname = ?", companyID, mailingID, imageName);
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
		return selectObjectDefaultNull(logger, "SELECT company_id, mailing_id, component_id, compname, comptype, comppresent, emmblock, binblock, mtype, target_id, url_id, description, timestamp FROM component_tbl WHERE cdn_id = ?", new MailingComponentRowMapper(), cdnID);
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
        return update(logger, sql, mailingId);
	}

	@Override
	public boolean updateBinBlockBulk(@VelocityCheck int companyId, Collection<Integer> mailingIds, MailingComponentType componentType, Collection<String> namePatterns, byte[] value) throws Exception {
		if (companyId < 0 || CollectionUtils.isEmpty(mailingIds) || CollectionUtils.isEmpty(namePatterns)) {
			return false;
		}

		List<Object> sqlParameters = new ArrayList<>();

		sqlParameters.add(companyId);
		sqlParameters.add(componentType.getCode());
		sqlParameters.addAll(namePatterns);

		String sqlFilterByMailingId = DbUtilities.makeBulkInClauseWithDelimiter(isOracleDB(), "mailing_id", mailingIds, null);
		String sqlFilterByName = AgnUtils.repeatString("compname LIKE ?", namePatterns.size(), " OR ");
		
		updateBlob(logger, "UPDATE component_tbl SET binblock = ? WHERE company_id = ? AND comptype = ? AND " + sqlFilterByMailingId + " AND (" + sqlFilterByName + ")", value, sqlParameters.toArray());
		
		return update(logger, "UPDATE component_tbl SET timestamp = CURRENT_TIMESTAMP WHERE company_id = ? AND comptype = ? AND " + sqlFilterByMailingId + " AND (" + sqlFilterByName + ")", sqlParameters.toArray()) > 0;
	}
}
