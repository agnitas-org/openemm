/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.agnitas.beans.MailingComponent;
import org.agnitas.dao.MailingComponentDao;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.web.CdnImage;

public interface ComMailingComponentDao extends MailingComponentDao {
	Date getComponentTime(@VelocityCheck int companyID, int mailingID, String name);

	Map<Integer, Integer> getImageComponentsSizes(@VelocityCheck int companyID, int mailingID);

	Map<Integer, Date> getImageComponentsTimestamps(@VelocityCheck int companyID, int mailingID);

	List<MailingComponent> getMailingComponentsByType(int type, @VelocityCheck int companyID);

    Map<Integer, Integer> getImageSizes(@VelocityCheck int companyID, int mailingID);

	Map<Integer, String> getImageNames(@VelocityCheck int companyId, int mailingId, boolean includeExternalImages);

	boolean exists(int mailingID, int companyID, int componentID);
	
	boolean deleteMailingComponentsByCompanyID(int companyID);
	
	void deleteMailingComponentsByMailing(int mailingID);

    int getImageComponent(@VelocityCheck int companyId, int mailingId, int componentType);

    List<MailingComponent> getMailingComponentsByType(@VelocityCheck int companyID, int mailingID, List<Integer> type);

    CdnImage getCdnImage(@VelocityCheck int companyID, int mailingID, String imageName, boolean isMobileRequest);

	MailingComponent getComponentByCdnID(String cdnID);

    /**
     * Method for marking unpresent components of some mailing
     *
     * @param mailingId mailing with components which will be marked as unpresent
     * @param presentComponents components which will not be marked as unpresent
     *
     * @return count of updated rows (unpresent components)
     */
	int setUnPresentComponentsForMailing(int mailingId, List<MailingComponent> presentComponents);

	boolean updateBinBlockBulk(@VelocityCheck int companyId, Collection<Integer> mailingIds, int componentType, Collection<String> namePatterns, byte[] value);
}
