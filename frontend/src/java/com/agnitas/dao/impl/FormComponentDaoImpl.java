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
import java.util.Date;
import java.util.List;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.util.AgnUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.FormComponent;
import com.agnitas.dao.FormComponent.FormComponentType;
import com.agnitas.dao.FormComponentDao;

/**
 * The Class FormComponentDaoImpl.
 */
public class FormComponentDaoImpl extends BaseDaoImpl implements FormComponentDao {
	/** The Constant logger. */
	private static final transient Logger logger = Logger.getLogger(FormComponentDaoImpl.class);
	
	/* (non-Javadoc)
	 * @see com.agnitas.dao.FormComponentDao#exists(int, int, int)
	 */
	@Override
	public boolean exists(int formID, int companyID, int componentID) {
		String sql = "SELECT COUNT(*) FROM form_component_tbl WHERE form_id = ? AND company_id = ? AND id = ?";
		int total = selectInt(logger, sql, formID, companyID, componentID);
		return total > 0;
	}
	
	/* (non-Javadoc)
	 * @see com.agnitas.dao.FormComponentDao#getFormComponentByName(int, int, java.lang.String)
	 */
	@Override
	public FormComponent getFormComponent(int formID, int companyID, String imageFileName, FormComponentType componentType) {
		List<FormComponent> list;
		if (componentType != null) {
			list = select(logger, "SELECT * FROM form_component_tbl WHERE (form_id = ? OR form_id = 0) AND company_id = ? AND name = ? AND type = ? ORDER BY form_id DESC", new FormComponentRowMapper(), formID, companyID, imageFileName, componentType.getId());
		} else {
			list = select(logger, "SELECT * FROM form_component_tbl WHERE (form_id = ? OR form_id = 0) AND company_id = ? AND name = ? ORDER BY form_id DESC", new FormComponentRowMapper(), formID, companyID, imageFileName);
		}
		if (list.size() >= 1) {
			return list.get(0);
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.agnitas.dao.FormComponentDao#save(com.agnitas.dao.FormComponent)
	 */
	@Override
	@DaoUpdateReturnValueCheck
	public boolean saveFormComponent(FormComponent formComponent) {
		try {
			if (formComponent.getFormID() != 0) {
				if (formComponent.getId() == 0 || !exists(formComponent.getFormID(), formComponent.getCompanyID(), formComponent.getId())) {
	                formComponent.setCreationDate(new Date());
	                formComponent.setChangeDate(formComponent.getCreationDate());
	                
	                if (isOracleDB()) {
	                	int newID = selectInt(logger, "SELECT form_component_tbl_seq.NEXTVAL FROM DUAL");
	                	String sql = "INSERT INTO form_component_tbl (id, form_id, company_id, name, type, data_size, width, height, mimetype, description, creation_date, change_date) VALUES (" + AgnUtils.repeatString("?", 12, ", ") + ")";
	                    int touchedLines = update(logger, sql, newID, formComponent.getFormID(), formComponent.getCompanyID(), formComponent.getName(), formComponent.getType().getId(), formComponent.getData().length, formComponent.getWidth(), formComponent.getHeight(), formComponent.getMimeType(), formComponent.getDescription(), formComponent.getCreationDate(), formComponent.getChangeDate());
	                    if (touchedLines != 1) {
	                        throw new RuntimeException("Illegal insert result");
	                    } else {
							updateBlob(logger, "UPDATE form_component_tbl SET data = ? WHERE id = ?", formComponent.getData(), newID);
						}
	
	                    formComponent.setId(newID);
	                } else {
	                	String insertStatement = "INSERT INTO form_component_tbl (form_id, company_id, name, type, data_size, width, height, mimetype, description, creation_date, change_date) VALUES (" + AgnUtils.repeatString("?", 11, ", ") + ")";
	                    int newID = insertIntoAutoincrementMysqlTable(logger, "id", insertStatement, formComponent.getFormID(), formComponent.getCompanyID(), formComponent.getName(), formComponent.getType().getId(), formComponent.getData().length, formComponent.getWidth(), formComponent.getHeight(), formComponent.getMimeType(), formComponent.getDescription(), formComponent.getCreationDate(), formComponent.getChangeDate());
	                    updateBlob(logger, "UPDATE form_component_tbl SET data = ? WHERE id = ?", formComponent.getData(), newID);
	                    formComponent.setId(newID);
	                }
					return true;
				} else {
	                formComponent.setChangeDate(new Date());
					
					String sql = "UPDATE form_component_tbl SET form_id = ?, company_id = ?, name = ?, type = ?, data_size = ?, width = ?, height = ?, mimetype = ?, description = ?, change_date = ? WHERE id = ?";
					int touchedLines = update(logger, sql, formComponent.getFormID(), formComponent.getCompanyID(), formComponent.getName(), formComponent.getType().getId(), formComponent.getData().length, formComponent.getWidth(), formComponent.getHeight(), formComponent.getMimeType(),  formComponent.getDescription(), formComponent.getChangeDate(), formComponent.getId());
					if (touchedLines != 1) {
						throw new RuntimeException("Illegal insert result");
					} else {
						updateBlob(logger, "UPDATE form_component_tbl SET data = ? WHERE id = ?", formComponent.getData(), formComponent.getId());
					}
					return true;
				}
			} else {
				throw new Exception("Cannot save or change globally used images (formID = 0)");
			}
		} catch (Exception e) {
			logger.error("Error saving formcomponent " + formComponent.getId() + "/" + formComponent.getName() + " for form " + formComponent.getFormID(), e);
			return false;
		}
	}

	/**
	 * Gets the component descriptions.
	 * This returns FormComponent items with all fields filled except for the data byte[]
	 *
	 * @param companyID the company id
	 * @param componentType the component type
	 * @return the component descriptions
	 */
	@Override
	public List<FormComponent> getFormComponentDescriptions(int companyID, int formID, FormComponentType componentType) {
		return select(logger, "SELECT id, company_id, form_id, name, type, mimetype, description, data_size, width, height, creation_date, change_date FROM form_component_tbl WHERE company_id = ? AND (form_id = ? OR form_id = 0) AND type = ?", new FormComponentRowMapperWithoutData(), companyID, formID, componentType.getId());
	}
	
	/**
	 * The Class FormComponentRowMapper.
	 */
	protected class FormComponentRowMapper extends FormComponentRowMapperWithoutData {
		/* (non-Javadoc)
		 * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
		 */
		@Override
		public FormComponent mapRow(ResultSet resultSet, int index) throws SQLException {
			FormComponent component = super.mapRow(resultSet, index);
			
			Blob blob = resultSet.getBlob("data");
			if (blob != null) {
				try (InputStream dataStream = blob.getBinaryStream()) {
					byte[] data = IOUtils.toByteArray(dataStream);
					component.setData(data);
				} catch (Exception ex) {
					logger.error("Error:" + ex, ex);
				}
			}

			return component;
		}
	}
	
	/**
	 * The Class FormComponentRowMapper without reading data byte[] (Lite/Snowflake)
	 */
	protected class FormComponentRowMapperWithoutData implements RowMapper<FormComponent> {
		/* (non-Javadoc)
		 * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
		 */
		@Override
		public FormComponent mapRow(ResultSet resultSet, int index) throws SQLException {
			FormComponent component = new FormComponent();

			component.setId(resultSet.getInt("id"));
			component.setCompanyID(resultSet.getInt("company_id"));
			component.setFormID(resultSet.getInt("form_id"));
			component.setName(resultSet.getString("name"));
			component.setType(FormComponentType.fromId(resultSet.getInt("type")));
			component.setMimeType(resultSet.getString("mimetype"));
			component.setDescription(resultSet.getString("description"));
			component.setCreationDate(resultSet.getTimestamp("creation_date"));
			component.setChangeDate(resultSet.getTimestamp("change_date"));
			component.setDataSize(resultSet.getInt("data_size"));
			component.setWidth(resultSet.getInt("width"));
			component.setHeight(resultSet.getInt("height"));

			return component;
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteFormComponent(int companyID, int formID, String componentName, FormComponentType componentType) {
		int touchedLines;
		if (componentType != null) {
			touchedLines = update(logger, "DELETE FROM form_component_tbl WHERE company_id = ? AND form_id = ? AND name = ? AND type = ?", companyID, formID, componentName, componentType.getId());
		} else {
			touchedLines = update(logger, "DELETE FROM form_component_tbl WHERE company_id = ? AND form_id = ? AND name = ?", companyID, formID, componentName);
		}
		return touchedLines > 0;
	}
	
	@Override
	public boolean deleteFormComponentByCompany(int companyID) {
		update(logger, "DELETE FROM form_component_tbl WHERE company_id = ?", companyID);
		return selectInt(logger, "SELECT COUNT(*) FROM form_component_tbl WHERE company_id = ?", companyID) == 0;
	}

	@Override
	public List<FormComponent> getFormComponents(int companyID, int formID) {
		return select(logger, "SELECT id, company_id, form_id, name, type, mimetype, description, data_size, width, height, creation_date, change_date, data FROM form_component_tbl WHERE company_id = ? AND (form_id = ? OR form_id = 0)", new FormComponentRowMapper(), companyID, formID);
	}
}
