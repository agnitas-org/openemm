/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.Date;
import java.util.Set;

public interface MaildropEntry {

	int getAdminTestTargetID();
	
	void setAdminTestTargetID( int targetID);
	
	String getMailGenerationOptimization();
	
	void setMailGenerationOptimization(final String mode);

    int getBlocksize();

    int getCompanyID();

    Date getGenChangeDate();

    Date getGenDate();

    int getGenStatus();
    
    int getId();

    int getMailingID();

    Date getSendDate();

    char getStatus();

    int getStepping();

    void setBlocksize(int blocksize);

    void setCompanyID(int companyID);

    void setGenChangeDate(Date genChangeDate);

    void setGenDate(Date genDate);

    void setGenStatus(int genStatus);

    void setId(int id);

    void setMailingID(int mailingID);

    void setSendDate(Date sendDate);

    void setStatus(char status);

    void setStepping(int stepping);
    
    int getMaxRecipients();
    
	void setMaxRecipients(int maxRecipients);

    int getOverwriteTestRecipient();

    void setOverwriteTestRecipient(int overwriteTestRecipient);

    void setAltgIds(Set<Integer> altgIds);

    Set<Integer> getAltgIds();

}
