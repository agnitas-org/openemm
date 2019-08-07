/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.beans.Company;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.Tuple;

import com.agnitas.beans.ComCompany;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.company.bean.CompanyEntry;

public interface ComCompanyDao {
	
	boolean initTables(int cid);
	
	ComCompany getCompany(@VelocityCheck int companyID);
	
	/**
	 * Writes changes to the given company to the database or creates a new one if needed.
	 * 
	 * @param comp the company to save.
	 * @throws Exception if something went wrong.
	 */
	void saveCompany(Company comp) throws Exception;
	
	boolean existTrackingTables(@VelocityCheck int companyID);

	/**
	 * returns true, if mailtracking for this companyID is active.
	 */
	boolean isMailtrackingActive(@VelocityCheck int companyID);

	/**
	 * get the success_xx_tbl data lifetime (number of days)
	 */
	int getSuccessDataExpirePeriod(@VelocityCheck int companyID);

	/**
	 * get the rdir_domain value for this companyId
	 */
	String getRedirectDomain(@VelocityCheck int companyId);

	List<ComCompany> getCreatedCompanies(@VelocityCheck int companyId);
		
	int getMaxAdminMails(@VelocityCheck int companyID);
	
	List<ComCompany> getAllActiveCompaniesWithoutMasterCompany();
	
	/**
	 * Deletes the given company from the database.
	 * 
	 * @param comp the company to delete.
	 */
	void deleteCompany(Company comp);
	
	void deleteCompany(@VelocityCheck int companyID);
	
	void updateCompanyStatus(int companyID, String status);

	/**
	 * This method gets a list with all NOT DELETED companies IDs from DB.
	 */
	List<Integer> getAllActiveCompaniesIdsWithoutMasterCompany();
	
	List<ComCompany> getActiveCompaniesWithoutMasterCompanyFromStart(int startCompany);

	boolean createHistoryTables(int companyID);

	PaginatedListImpl<CompanyEntry> getCompanyList(@VelocityCheck int companyID, String sort, String direction, int page, int rownums);

	PaginatedListImpl<CompanyEntry> getCompanyListNew(@VelocityCheck int companyID, String sort, String direction, int page, int rownums);

    List<ComCompany> getAllActiveCompanies();
    
    boolean checkDeeptrackingAutoActivate(@VelocityCheck int companyID);
    
    int getCompanyDatasource(@VelocityCheck int companyID);

	int getMaximumNumberOfCustomers();

	int getMaximumNumberOfProfileFields() throws Exception;
	
	/**
	 * Get number of active and not deleted companies
	 */
	int getNumberOfCompanies();
	
	int getNumberOfProfileFields(int companyID) throws Exception;
	
	boolean initCustomerTables(int companyID) throws SQLException;

	void createRdirValNumTable(int newCompanyId) throws Exception;

	void copySampleMailings(int newCompanyId, int mailinglistID, String rdirDomain) throws Exception;

	boolean addExecutiveAdmin(int companyID, int executiveAdminID);

	//get all active companies
	List<CompanyEntry> getActiveCompaniesLight();
		
	//get only own company and companies created by own company + status = active
	List<CompanyEntry> getActiveOwnCompaniesLight(@VelocityCheck int companyId);

	CompanyEntry getCompanyLight(int id);

	Set<Permission> getCompanyPermissions(int companyID);

	void setupPremiumFeaturePermissions(Set<String> allowedPremiumFeatures, Set<String> unAllowedPremiumFeatures2);

	void createCompanyPermission(int companyID, Permission permission);

	boolean hasCompanyPermission(int companyID, Permission permission);

	void deleteCompanyPermission(int companyID, Permission permission);

	boolean deleteAllCompanyPermission(int companyID);

    List<Tuple<String,String>> getCompanyInfo(int companyID);

    Map<String, Object> getCompanySettings(int companyID);

	List<Map<String, Object>> getReferenceTableSettings(int companyID);

	void changeFeatureRights(String featureName, int companyID, boolean activate);

	int getPriorityCount(@VelocityCheck int companyId);

	void setPriorityCount(@VelocityCheck int companyId, int value);

    boolean isCompanyExist(int companyId);

    String getShortName(@VelocityCheck int companyId);

	void deactivateExtendedCompanies();
	
	boolean isCompanyNameUnique(String shortname);
}
