/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.referencetable.beans;

import java.util.Date;

public interface ReferenceTable {

	int getId();

	void setId(int id);

	int getCompanyID();

	void setCompanyID(int companyID);
	
	String getName();

	void setName(String name);

	String getTableName();

	void setTableName(String tableName);

	String getDbTableName();

	void setDbTableName(String dbTableName);

	String getDescription();

	void setDescription(String description);
	
	String getRefSource();

	void setRefSource(String refSource);

	String getKeyColumn();

	void setKeyColumn(String keyColumn);

	String getBackRef();

	void setBackRef(String backRef);
	
	Date getCreationDate();

	void setCreationDate(Date creationDate);

	Date getChangeDate();

	void setChangeDate(Date changeDate);

	String getJoinCondition();

	void setJoinCondition(String joinCondition);

	boolean isLocked();

	void setLocked(boolean locked);

	boolean isVoucher();

	void setVoucher(boolean voucher);

	int getVoucherLimitPercent();

	void setVoucherLimitPercent(int voucherLimitPercent);

	String getVoucherLimitInterval();

	void setVoucherLimitInterval(String voucherLimitIntervalString);

	String getVoucherLimitEmailList();

	void setVoucherLimitEmailList(String voucherLimitEmailList);

	Date getVoucherLimitNextCheckDate();

	void setVoucherLimitNextCheckDate(Date voucherLimitNextCheckDate);
	
	Date getVoucherLimitLastCheckDate();

	void setVoucherLimitLastCheckDate(Date voucherLimitLastCheckDate);

	boolean isVoucherRenew();

	void setVoucherRenew(boolean voucherRenew);

	long getDbTableSize();

	void setDbTableSize(long dbTableSize);

	String getTableSizeHumanReadable();

	String getContentViewKeyColumn();

	void setContentViewKeyColumn(String contentViewKeyColumn);

    int getRowsCount();

    void setRowsCount(int rowsCount);
}
