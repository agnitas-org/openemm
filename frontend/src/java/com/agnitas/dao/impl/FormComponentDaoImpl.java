/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

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
import java.util.List;
import java.util.Set;

import com.agnitas.beans.FormComponent;
import com.agnitas.beans.FormComponent.FormComponentType;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.FormComponentDao;
import com.agnitas.emm.core.userform.form.UserFormImagesOverviewFilter;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.dao.impl.mapper.StringRowMapper;
import com.agnitas.util.AgnUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

/**
 * The Class FormComponentDaoImpl.
 */
public class FormComponentDaoImpl extends PaginatedBaseDaoImpl implements FormComponentDao {
	
	/* (non-Javadoc)
	 * @see com.agnitas.dao.FormComponentDao#exists(int, int, int)
	 */
	@Override
	public boolean exists(int formID, int companyID, int componentID) {
		String sql = "SELECT COUNT(*) FROM form_component_tbl WHERE form_id = ? AND company_id = ? AND id = ?";
		int total = selectInt(sql, formID, companyID, componentID);
		return total > 0;
	}

	@Override
	public boolean exists(int formId, int companyId, String componentName) {
		String sql = "SELECT COUNT(*) FROM form_component_tbl WHERE form_id = ? AND company_id = ? AND name = ?";
		return selectInt(sql, formId, companyId, componentName) > 0;
	}

	/* (non-Javadoc)
	 * @see com.agnitas.dao.FormComponentDao#getFormComponentByName(int, int, java.lang.String)
	 */
	@Override
	public FormComponent getFormComponent(int formID, int companyID, String imageFileName, FormComponentType componentType) {
		List<FormComponent> list;
		if (componentType != null) {
			list = select("SELECT * FROM form_component_tbl WHERE (form_id = ? OR form_id = 0) AND company_id = ? AND name = ? AND type = ? ORDER BY form_id DESC", new FormComponentRowMapper(), formID, companyID, imageFileName, componentType.getId());
		} else {
			list = select("SELECT * FROM form_component_tbl WHERE (form_id = ? OR form_id = 0) AND company_id = ? AND name = ? ORDER BY form_id DESC", new FormComponentRowMapper(), formID, companyID, imageFileName);
		}
		if (!list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public PaginatedListImpl<FormComponent> getFormComponentOverview(UserFormImagesOverviewFilter filter) {
		StringBuilder query = new StringBuilder("SELECT id, company_id, form_id, name, type, mimetype, description, data_size, width, height, creation_date, change_date FROM form_component_tbl");
		List<Object> params = applyOverviewFilter(filter, query);

		PaginatedListImpl<FormComponent> list = selectPaginatedList(query.toString(), "form_component_tbl", filter,
				new FormComponentRowMapperWithoutData(), params.toArray());

		if (filter.isUiFiltersSet()) {
			list.setNotFilteredFullListSize(getTotalUnfilteredCountForOverview(filter));
		}

		return list;
	}

	private List<Object> applyOverviewFilter(UserFormImagesOverviewFilter filter, StringBuilder query) {
		List<Object> params = applyRequiredOverviewFilter(query, filter);

		if (StringUtils.isNotBlank(filter.getFileName())) {
			query.append(getPartialSearchFilterWithAnd("name"));
			params.add(filter.getFileName());
		}

		if (StringUtils.isNotBlank(filter.getDescription())) {
			query.append(getPartialSearchFilterWithAnd("description"));
			params.add(filter.getDescription());
		}

		query.append(getDateRangeFilterWithAnd("creation_date", filter.getUploadDate(), params));

		if (filter.getFileSizeMin() != null && filter.getFileSizeMin() > 0) {
			query.append(" AND data_size >= ?");
			params.add(filter.getFileSizeMin() * 1000);
		}
		if (filter.getFileSizeMax() != null && filter.getFileSizeMax() > 0) {
			query.append(" AND data_size <= ?");
			params.add(filter.getFileSizeMax() * 1000);
		}

		if (filter.getHeightMin() != null && filter.getHeightMin() > 0) {
			query.append(" AND height >= ?");
			params.add(filter.getHeightMin());
		}
		if (filter.getHeightMax() != null && filter.getHeightMax() > 0) {
			query.append(" AND height <= ?");
			params.add(filter.getHeightMax());
		}

		if (filter.getWidthMin() != null && filter.getWidthMin() > 0) {
			query.append(" AND width >= ?");
			params.add(filter.getWidthMin());
		}
		if (filter.getWidthMax() != null && filter.getWidthMax() > 0) {
			query.append(" AND width <= ?");
			params.add(filter.getWidthMax());
		}

		if (CollectionUtils.isNotEmpty(filter.getMimetypes())) {
			query.append(" AND mimetype IN (").append(AgnUtils.csvQMark(filter.getMimetypes().size())).append(")");
			params.addAll(filter.getMimetypes());
		}

		return params;
	}

	private int getTotalUnfilteredCountForOverview(UserFormImagesOverviewFilter filter) {
		StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM form_component_tbl");
		List<Object> params = applyRequiredOverviewFilter(query, filter);

		return selectIntWithDefaultValue(query.toString(), 0, params.toArray());
	}

	private List<Object> applyRequiredOverviewFilter(StringBuilder query, UserFormImagesOverviewFilter filter) {
		query.append(" WHERE company_id = ? AND (form_id = ? OR form_id = 0) AND type = ?");
		return new ArrayList<>(List.of(filter.getCompanyId(), filter.getFormId(), filter.getType().getId()));
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
	protected static class FormComponentRowMapperWithoutData implements RowMapper<FormComponent> {
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
			touchedLines = update("DELETE FROM form_component_tbl WHERE company_id = ? AND form_id = ? AND name = ? AND type = ?", companyID, formID, componentName, componentType.getId());
		} else {
			touchedLines = update("DELETE FROM form_component_tbl WHERE company_id = ? AND form_id = ? AND name = ?", companyID, formID, componentName);
		}
		return touchedLines > 0;
	}

	@Override
	public boolean deleteFormComponentByCompany(int companyID) {
		update("DELETE FROM form_component_tbl WHERE company_id = ?", companyID);
		return selectInt("SELECT COUNT(*) FROM form_component_tbl WHERE company_id = ?", companyID) == 0;
	}

	@Override
	public List<FormComponent> getFormComponents(Set<Integer> ids, UserFormImagesOverviewFilter filter) {
		StringBuilder query = new StringBuilder("SELECT id, company_id, form_id, name, type, mimetype, description, data_size, width, height, creation_date, change_date, data FROM form_component_tbl");
		List<Object> params = applyOverviewFilter(filter, query);

		if (ids != null) {
			query.append(" AND ").append(makeBulkInClauseForInteger("id", ids));
		}

		return select(query.toString(), new FormComponentRowMapper(), params.toArray());
	}

	@Override
	public List<String> getComponentFileNames(Set<Integer> bulkIds, int formId, int companyID) {
		String query = "SELECT name FROM form_component_tbl WHERE (form_id = ? OR form_id = 0) AND company_id = ? AND "
				+ makeBulkInClauseForInteger("id", bulkIds);

		return select(query, StringRowMapper.INSTANCE, formId, companyID);
	}

	@Override
	public void delete(Set<Integer> bulkIds, int formId, int companyID) {
		String query = "DELETE FROM form_component_tbl WHERE company_id = ? AND form_id = ? AND "
				+ makeBulkInClauseForInteger("id", bulkIds);

		update(query, companyID, formId);
	}

	@Override
	public boolean updateDimension(int width, int height, int componentId) {
		return update("UPDATE form_component_tbl SET width = ?, height = ? WHERE id = ?", width, height, componentId) > 0;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean saveFormComponent(int companyId, int formId, FormComponent component, FormComponent thumbnail) {
		if (formId == 0) {
			return false;
		}

		try {
			int componentId = saveComponent(formId, companyId, component);
			if (componentId > 0) {
				saveComponent(formId, companyId, thumbnail);
				return true;
			}
		} catch (Exception e) {
			logger.error("Error saving form component formId: %d, component name: %s".formatted(formId, component.getName()), e);
		}
		return false;
	}

	private int saveComponent(int formId, int companyId, FormComponent component) {
		validateDescription(component.getDescription());
		
		int componentId;
		if (isOracleDB()) {
			componentId = selectInt("SELECT form_component_tbl_seq.NEXTVAL FROM DUAL");
			String sql = "INSERT INTO form_component_tbl " +
					"(id, form_id, company_id, name, type, data_size, width, height, mimetype, description, creation_date, change_date) " +
					"VALUES (" + AgnUtils.repeatString("?", 10, ", ") + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

			int update = update(sql,
					componentId,
					formId,
					companyId,
					component.getName(),
					component.getType().getId(),
					ArrayUtils.getLength(component.getData()),
					component.getWidth(),
					component.getHeight(),
					component.getMimeType(),
					component.getDescription());

			if (update != 1) {
				throw new RuntimeException("Illegal insert result");
			}

		} else {
			String insertStatement = "INSERT INTO form_component_tbl " +
					"(form_id, company_id, name, type, data_size, width, height, mimetype, description, " +
					"creation_date, change_date) " +
					"VALUES (" + AgnUtils.repeatString("?", 9, ", ") + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

			componentId = insertIntoAutoincrementMysqlTable("id", insertStatement,
					formId,
					companyId,
					component.getName(),
					component.getType().getId(),
					ArrayUtils.getLength(component.getData()),
					component.getWidth(),
					component.getHeight(),
					component.getMimeType(),
					component.getDescription());
		}

		if (componentId > 0) {
			updateBlob("UPDATE form_component_tbl SET data = ? WHERE id = ?", component.getData(), componentId);
			component.setId(componentId);
			return componentId;
		}

		return 0;
	}

	private void validateDescription(String description) {
		if (description != null && description.length() > 100) {
			throw new IllegalArgumentException("Value for form_component_tbl.description is to long (Maximum: 100, Current: " + description.length() + ")");
		}
	}
}
