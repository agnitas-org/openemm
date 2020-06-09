/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.agnitas.beans.Recipient;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;

import com.agnitas.dao.ComRecipientDao;

/**
 * wrapper for a long sql query. It will be used for asynchronous tasks
 */
public class RecipientBeanQueryWorker implements Callable<PaginatedListImpl<DynaBean>> {
	private static final transient Logger logger = Logger.getLogger(RecipientBeanQueryWorker.class);
	
	protected ComRecipientDao recipientDao;
	protected String sqlStatementForData;
	protected Object[] sqlParametersForData;
	protected String sortCriterion;
	protected boolean sortedAscending;
	protected int pageNumber;
	protected int rownums;
	protected int companyID;
	protected Set<String> columns;
	protected Exception error;

	public RecipientBeanQueryWorker(ComRecipientDao recipientDao, @VelocityCheck int companyID, Set<String> columns, String sqlStatementForData, Object[] sqlParametersForData, String sortCriterion, boolean sortedAscending, int pageNumber, int rownums) {
		this.recipientDao = recipientDao;
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
			List<DynaBean> partialListOfDynaBeans = convertPaginatedListToDynaBean(recipientPaginatedList);
			
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
	
	protected List<DynaBean> convertPaginatedListToDynaBean(PaginatedListImpl<Recipient> recipientPaginatedList) throws InstantiationException, IllegalAccessException {
		List<DynaBean> partialListOfDynaBeans = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(recipientPaginatedList.getList())) {
			DynaProperty[] properties = new DynaProperty[columns.size()];
			int i = 0;
			for (String column : columns) {
				properties[i++] = new DynaProperty(column.toLowerCase(), String.class);
			}
			BasicDynaClass dynaClass = new BasicDynaClass("recipient", null, properties);

			for (Recipient recipient : recipientPaginatedList.getList()) {
				DynaBean bean = dynaClass.newInstance();
				for (String column : columns) {
					bean.set(column.toLowerCase(), recipient.getCustParametersNotNull(column.toUpperCase()));
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
