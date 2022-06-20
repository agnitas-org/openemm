/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.agnitas.beans.Recipient;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ProfileField;
import com.agnitas.dao.ComProfileFieldDao;
import com.agnitas.dao.ComRecipientDao;

/**
 * wrapper for a long sql query. It will be used for asynchronous tasks
 */
public class RecipientBeanQueryWorker implements Callable<PaginatedListImpl<DynaBean>> {
	private static final transient Logger logger = LogManager.getLogger(RecipientBeanQueryWorker.class);
	
	protected ComRecipientDao recipientDao;
	protected ComAdmin admin;
	protected ComProfileFieldDao profileFieldDao;
	protected String sqlStatementForData;
	protected Object[] sqlParametersForData;
	protected String sortCriterion;
	protected boolean sortedAscending;
	protected int pageNumber;
	protected int rownums;
	protected int companyID;
	protected Set<String> columns;
	protected Exception error;

	public RecipientBeanQueryWorker(ComRecipientDao recipientDao, ComProfileFieldDao profileFieldDao, ComAdmin admin, @VelocityCheck int companyID, Set<String> columns, String sqlStatementForData, Object[] sqlParametersForData, String sortCriterion, boolean sortedAscending, int pageNumber, int rownums) {
		this.recipientDao = recipientDao;
		this.profileFieldDao = profileFieldDao;
		this.admin = admin;
		this.sqlStatementForData = sqlStatementForData;
		this.sqlParametersForData = sqlParametersForData;
		this.sortCriterion = sortCriterion;
		this.sortedAscending = sortedAscending;
		this.pageNumber = pageNumber;
		this.rownums = rownums;
		this.companyID = companyID;
		this.columns = columns;
		
		// TODO: EMM-4435: Debug logging, when "customer_id" column is missing.
		if(!columns.contains("customer_id")) {
			try {
				throw new RuntimeException("EMM-4435: customer_id is missing");
			} catch(Exception e) {
				logger.error(String.format("Missing customer_id property (company ID %d, page %d, %d rows, sorting criterion \"%s\", sorting %s, sql \"%s\", column \"%s\")",
						companyID,
						pageNumber,
						rownums,
						sortCriterion,
						sortedAscending ? "asceding" : "descending",
						sqlStatementForData,
						columns), e);
			}
		}
	}

	@Override
	public PaginatedListImpl<DynaBean> call() throws Exception {
		try {
			PaginatedListImpl<Recipient> recipientPaginatedList = recipientDao.getRecipients(companyID, columns, sqlStatementForData, sqlParametersForData, sortCriterion, sortedAscending, pageNumber, rownums);
			
			// Convert PaginatedListImpl of Recipient into PaginatedListImpl of DynaBean
			List<DynaBean> partialListOfDynaBeans = convertPaginatedListToDynaBean(companyID, recipientPaginatedList);
			
			return new PaginatedListImpl<>(partialListOfDynaBeans,
					recipientPaginatedList.getFullListSize(),
					recipientPaginatedList.getPageSize(),
					recipientPaginatedList.getPageNumber(),
					recipientPaginatedList.getSortCriterion(),
					recipientPaginatedList.getSortDirection());
		} catch (Exception e) {
			logger.error("Error executing RecipientBeanQueryWorker", e);
			error = e;
			return null;
		}
	}
	
	protected List<DynaBean> convertPaginatedListToDynaBean(int companyIdParam, PaginatedListImpl<Recipient> recipientPaginatedList) throws Exception {
		List<DynaBean> partialListOfDynaBeans = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(recipientPaginatedList.getList())) {
			DynaProperty[] properties = new DynaProperty[columns.size()];
			int i = 0;
			for (String column : columns) {
				properties[i++] = new DynaProperty(column.toLowerCase(), String.class);
			}
			BasicDynaClass dynaClass = new BasicDynaClass("recipient", null, properties);
			List<ProfileField> profileFields = profileFieldDao.getProfileFields(companyIdParam);

			for (Recipient recipient : recipientPaginatedList.getList()) {
				DynaBean bean = dynaClass.newInstance();
				for (String column : columns) {
					SimpleDataType simpleDataType = null;
					for (ProfileField profileField : profileFields) {
						if (profileField.getColumn().equalsIgnoreCase(column)) {
							simpleDataType = profileField.getSimpleDataType();
						}
					}

					// All values come here as String although the map is of type <String, Object>
					String value = (String) recipient.getCustParameters().get(column.toUpperCase());
					
					if (simpleDataType == null || StringUtils.isBlank(value)) {
						bean.set(column.toLowerCase(), value);
					} else {
						switch(simpleDataType) {
							case Characters:
								bean.set(column.toLowerCase(), value);
								break;
							case Date:
								if (StringUtils.isBlank(value)) {
									bean.set(column.toLowerCase(), "");
								} else {
									bean.set(column.toLowerCase(), admin.getDateFormat().format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(value)));
								}
								break;
							case DateTime:
								if (StringUtils.isBlank(value)) {
									bean.set(column.toLowerCase(), "");
								} else {
									bean.set(column.toLowerCase(), admin.getDateTimeFormat().format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(value)));
								}
								break;
							case Float:
								DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(admin.getLocale());
								DecimalFormat floatFormat = new DecimalFormat("###0.###", decimalFormatSymbols);
								bean.set(column.toLowerCase(), floatFormat.format(Double.parseDouble(value)));
								break;
							case Numeric:
								bean.set(column.toLowerCase(), value);
								break;
							case Blob:
							default:
								bean.set(column.toLowerCase(), value);
								break;
						}
					}
				}
				partialListOfDynaBeans.add(bean);
			}
		}
		return partialListOfDynaBeans;
	}

	public Exception getError() {
		return error;
	}
}
