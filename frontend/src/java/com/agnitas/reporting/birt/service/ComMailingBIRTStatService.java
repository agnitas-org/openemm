/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.agnitas.beans.Company;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.beans.impl.SimpleKeyValueBean;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.FulltextSearchQueryException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.beans.TargetLight;
import com.agnitas.dao.ComAdminDao;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.commons.database.fulltext.FulltextSearchQueryGenerator;

public class ComMailingBIRTStatService {

	/** DAO accessing target groups. */
	private ComTargetDao targetDao;
	private ComMailingDao mailingDao;
	private MailinglistDao mailinglistDao;
	private ComCompanyDao companyDao;
	private ComAdminDao adminDao;
	private String drilldownURLPrefix;
	private FulltextSearchQueryGenerator fulltextSearchQueryGenerator;

	public void setDrilldownURLPrefix(String drilldownURLPrefix) {
		this.drilldownURLPrefix = drilldownURLPrefix;
	}

	public List<SimpleKeyValueBean> getAvailableMailinglistsAsKeyValue(@VelocityCheck int companyID) {
		List<SimpleKeyValueBean> valueBeanList = new ArrayList<>();
		List<Mailinglist> mailinglistList = mailinglistDao.getMailingListsNames(companyID);
		for (Mailinglist list : mailinglistList) {
			SimpleKeyValueBean bean = new SimpleKeyValueBean(Integer.toString(list.getId()), list.getShortname());
			valueBeanList.add(bean);
		}

		return valueBeanList;
	}

	public List<SimpleKeyValueBean> extractTargetsFromList(List<SimpleKeyValueBean> allTargets, String[] targetIDs) {
		return filterTargetsFromList(allTargets, targetIDs);
	}

	public List<SimpleKeyValueBean> filterTargetsFromList(List<SimpleKeyValueBean> allTargets, String[] targetIDs) {
		List<SimpleKeyValueBean> tmpList = new ArrayList<>();
		for (SimpleKeyValueBean target : allTargets) {
			for (String targetID : targetIDs) {
				if (targetID.equals(target.getKey())) {
					tmpList.add(target);
				}
			}
		}
		return tmpList;
	}

	public String[] extractTargetIDs(List<SimpleKeyValueBean> targets) {
		List<String> tmpList = new ArrayList<>();
		for (SimpleKeyValueBean target : targets) {
			tmpList.add(target.getKey());
		}
		return tmpList.toArray(new String[0]);
	}

	public List<SimpleKeyValueBean> getAvailableTargetsAsKeyValue(@VelocityCheck int companyID) {
		List<TargetLight> targetsList = targetDao.getTargetLights(companyID);
		List<SimpleKeyValueBean> simpleTargets = new ArrayList<>();
		for (TargetLight target : targetsList) {
			SimpleKeyValueBean tmp = new SimpleKeyValueBean(Integer.toString(target.getId()), target.getTargetName());
			simpleTargets.add(tmp);
		}
		return simpleTargets;
	}

	public boolean useMailtracking(@VelocityCheck int companyID) {
		Company company = companyDao.getCompany(companyID);
		if (company != null) {
			return (company.getMailtracking() == 1);
		}
		return false;
	}

	public boolean deepTracking(int adminID, @VelocityCheck int companyID) {
		ComAdmin admin = adminDao.getAdmin(adminID, companyID);
		boolean existTables = companyDao.existTrackingTables(companyID);
		return (admin != null && admin.permissionAllowed(Permission.DEEPTRACKING) && existTables);
	}

	public PaginatedListImpl<Map<String, Object>> getMailingStats(@VelocityCheck int companyId, String searchQuery,
																  boolean searchName, boolean searchDescription,
																  String sortCriteria, String sortDirection,
																  int pageNumber, int pageSize) throws FulltextSearchQueryException {
		if (StringUtils.isNotBlank(searchQuery) && (searchName || searchDescription)) {
			searchQuery = fulltextSearchQueryGenerator.generateSpecificQuery(searchQuery);
		}
		boolean sortAscending = AgnUtils.sortingDirectionToBoolean(sortDirection, false);
		return mailingDao.getMailingShortList(companyId, searchQuery, searchName, searchDescription, sortCriteria, sortAscending, pageNumber, pageSize);
	}

	public PaginatedListImpl<Map<String, Object>> getMailingStats(@VelocityCheck int companyId, MailingsListProperties props) {
		if (companyId <= 0 || Objects.isNull(props)) {
			return new PaginatedListImpl<>();
		}
		return mailingDao.getMailingList(companyId, props);
	}

    public boolean showNumStat(int mailingID, @VelocityCheck int companyID){
        return mailingDao.showNumStat(mailingID, companyID);
    }

    public boolean showAlphaStat(int mailingID, @VelocityCheck int companyID){
        return mailingDao.showAlphaStat(mailingID, companyID);
    }

    public boolean showSimpleStat(int mailingID, @VelocityCheck int companyID){
        return mailingDao.showSimpleStat(mailingID, companyID);
    }

	/**
	 * get the startdate of a mailing. Return null if the mailing hasn't been
	 * send
	 */
	public Timestamp getMailingStartDate(int mailingID) {
		return mailingDao.getLastSendDate(mailingID);
	}

	public String getMailingShortName(int mailingID, @VelocityCheck int companyID) {
		return mailingDao.getMailing(mailingID, companyID).getShortname();
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

	public void setMailingDao(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	public void setMailinglistDao(MailinglistDao mailinglistDao) {
		this.mailinglistDao = mailinglistDao;
	}

	public void setCompanyDao(ComCompanyDao companyDao) {
		this.companyDao = companyDao;
	}

	public void setAdminDao(ComAdminDao adminDao) {
		this.adminDao = adminDao;
	}

	public void setFulltextSearchQueryGenerator(FulltextSearchQueryGenerator fulltextSearchQueryGenerator) {
		this.fulltextSearchQueryGenerator = fulltextSearchQueryGenerator;
	}

	public String getDrilldownURLPrefix() {
		return drilldownURLPrefix;
	}
}
