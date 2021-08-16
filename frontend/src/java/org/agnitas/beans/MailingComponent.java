/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.util.Date;

import org.agnitas.emm.core.velocity.VelocityCheck;

public interface MailingComponent {
	boolean loadContentFromURL();

	byte[] getBinaryBlock();

	int getId();

	String getComponentName();

	String getComponentNameUrlEncoded();

	String getEmmBlock();

	String getMimeType();

	int getTargetID();

	MailingComponentType getType();

	int getPresent();

	void setBinaryBlock(byte[] binaryBlock, String mimeType);

	void setCompanyID(@VelocityCheck int companyID);

	void setId(int id);

	void setComponentName(String componentName);

	void setEmmBlock(String emmBlock, String mimeType);

	void setMailingID(int mailingID);

	void setTargetID(int targetID);

	void setType(MailingComponentType type);

	void setPresent(int present);

	int getMailingID();

	int getCompanyID();

	Date getTimestamp();

	void setTimestamp(Date timestamp);

	String getLink();

	void setLink(String link);

	int getUrlID();

	void setUrlID(int urlID);

	String getDescription();

	void setDescription(String description);

	Date getStartDate();

	void setStartDate(Date startDate);

	Date getEndDate();

	void setEndDate(Date endDate);
	
	boolean isSourceComponent();
	
	boolean isMobileImage();
}
