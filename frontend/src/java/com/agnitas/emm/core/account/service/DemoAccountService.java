/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.account.service;

import java.util.Map;

public interface DemoAccountService {
    //is check for companyID necessary or could it be a problem?
    Map<String, Integer> resetAccount(int companyId) throws Exception;
    
    Map<String, Integer> resetAccount(int companyId, boolean reset_advanced) throws Exception;
    
    Map<String, Integer> deleteAccount(int companyId);
       
    boolean fillAccountFromMaster(int companyId) throws Exception;
    
    boolean fillAccountFromCompany(int companyId, int fromCompanyID) throws Exception;

    int checkDB();
    
    boolean checkPassword(String user, String password);
    
    boolean deleteUser(int companyID);
    
    boolean sendAuthenticationData(int companyID, int adminID, String clientIp);

	int initJob(int companyID, String method, String supervisorUser);

	void updateJob(int jobID, String status, String result);

	String getJobStatus(int companyID);
}
