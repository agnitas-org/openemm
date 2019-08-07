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
	int TYPE_ATTACHMENT = 3;
	int TYPE_PERSONALIZED_ATTACHMENT = 4;
	int TYPE_HOSTED_IMAGE = 5;
	int TYPE_IMAGE = 1;
	int TYPE_TEMPLATE = 0;
	int TYPE_PREC_ATTACHMENT = 7;
	int TYPE_THUMBNAIL_IMAGE = 8;

	boolean loadContentFromURL();

	byte[] getBinaryBlock();

	int getId();

	String getComponentName();

	String getComponentNameUrlEncoded();

	String getEmmBlock();

	String getMimeType();

	int getTargetID();

	int getType();

	void setBinaryBlock(byte[] binaryBlock, String mimeType);

	void setCompanyID(@VelocityCheck int companyID);

	void setId(int id);

	void setComponentName(String componentName);

	void setEmmBlock(String emmBlock, String mimeType);

	void setMailingID(int mailingID);

	void setTargetID(int targetID);

	void setType(int type);

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
}
