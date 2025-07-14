/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.agnitas.emm.core.company.form.CompanyListForm;
import com.agnitas.beans.CompaniesConstraints;
import com.agnitas.beans.impl.CompanyStatus;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.util.Tuple;

import com.agnitas.beans.Company;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.company.bean.CompanyEntry;
import com.agnitas.emm.core.servicemail.UnknownCompanyIdException;

public interface CompanyDao {
	
	boolean initTables(int cid);
	
	Company getCompany(int companyID);
	
	/**
	 * Writes changes to the given company to the database or creates a new one if needed.
	 * 
	 * @param comp the company to save.
	 * @throws Exception if something went wrong.
	 */
	void saveCompany(Company comp) throws Exception;
	
	boolean existTrackingTables(int companyID);

	/**
	 * returns true, if mailtracking for this companyID is active.
	 */
	boolean isMailtrackingActive(int companyID);

	boolean isCreatorId(int companyId, int creatorId);

	/**
	 * get the rdir_domain value for this companyId
	 */
	String getRedirectDomain(int companyId);

	List<Company> getCreatedCompanies(int companyId);
	
	List<Company> getAllActiveCompaniesWithoutMasterCompany();
	
	void updateCompanyStatus(int companyID, CompanyStatus status);

    List<Integer> getAllActiveCompaniesIds(boolean includeMasterCompany);
	
	List<Company> getActiveCompaniesWithoutMasterCompanyFromStart(int startCompany);

	boolean createHistoryTables(int companyID);

	// TODO: check usage and remove after EMMGUI-714 will be finished and old design will be removed
	PaginatedListImpl<CompanyEntry> getCompanyList(int companyID, String sort, String direction, int page, int rownums);

    List<Company> getAllActiveCompanies();
    
    boolean checkDeeptrackingAutoActivate(int companyID);
	
	void setAutoDeeptracking(int companyID, boolean active);
    
    int getCompanyDatasource(int companyID);

	int getMaximumNumberOfCustomers();
	
	/**
	 * Get number of active and not deleted companies
	 */
	int getNumberOfCompanies();
	
	int getNumberOfProfileFields(int companyID);
	
	boolean initCustomerTables(int companyID);

	void createRdirValNumTable(int newCompanyId);

	void copySampleMailings(int newCompanyId, int mailinglistID, String rdirDomain) throws Exception;

	boolean addExecutiveAdmin(int companyID, int executiveAdminID);

    PaginatedListImpl<CompanyEntry> getCompanyList(CompanyListForm filter, int companyId);

	//get all active companies
	List<CompanyEntry> getActiveCompaniesLight(boolean allowTransitionStatus);
	
	//get only own company and companies created by own company + status = active
	List<CompanyEntry> getActiveOwnCompaniesLight(int companyId, boolean allowTransitionStatus);

	CompanyEntry getCompanyLight(int id);

	Set<Permission> getCompanyPermissions(int companyID);

	void setupPremiumFeaturePermissions(Set<String> allowedPremiumFeatures, Set<String> unAllowedPremiumFeatures, String comment, int companyID);

	void createCompanyPermission(int companyID, Permission permission, String comment);

	boolean hasCompanyPermission(int companyID, Permission permission);

	void deleteCompanyPermission(int companyID, Permission permission);

	boolean deleteAllCompanyPermission(int companyID);

    List<Tuple<String,String>> getCompanyInfo(int companyID);

    Map<String, Object> getCompanySettings(int companyID);

	List<Map<String, Object>> getReferenceTableSettings(int companyID);
	
	void changeFeatureRights(String featureName, int companyID, boolean activate, String comment);

	int getPriorityCount(int companyId);

	void setPriorityCount(int companyId, int value);

    boolean isCompanyExist(int companyId);

    String getShortName(int companyId);

	void deactivateExtendedCompanies();
	
	int selectForTestCompany();
	
	int selectNumberOfExistingTestCompanies();
	
	boolean isCompanyNameUnique(String shortname);
	
	List<Integer> getOpenEMMCompanyForClosing();
	
	int getParenCompanyId(int companyId);
	
	boolean createFrequencyFields(int companyID);

	Company getCompanyByName(String clientName);

	int getNumberOfCustomers(int companyID);

	void cleanupPremiumFeaturePermissions(int companyID);

	Optional<String> getCompanyToken(int companyID) throws UnknownCompanyIdException;

    List<Integer> getActiveCompanies(CompaniesConstraints constraints);

	void createMissingOpenemmPlusPermissions();

	String getSpamCheckAddress(int companyID);

    List<CompanyEntry> findAllByEmailPart(String email, int companyID);
    List<CompanyEntry> findAllByEmailPart(String email);

	void updateTechnicalContact(String email, int id);

	List<Integer> getCompaniesIds();
}
