/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.dao;

import java.util.Date;
import java.util.List;

import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.IntEnum;
import com.agnitas.emm.core.mailing.bean.ComMailingParameter;
import com.agnitas.emm.core.mailing.dao.impl.MailingParameterNotFoundException;

public interface ComMailingParameterDao {
    String PARAMETERNAME_INTERVAL = "interval";
    String PARAMETERNAME_ERROR = "error";
    String PARAMETERNAME_NEXT_START = "next_start";

    enum IntervalType implements IntEnum {
        None(0),
        Weekly(1),
        TwoWeekly(2),
        Monthly(3),
    	Weekdaily(4),
    	Short(5);

        private int id;

        public static IntervalType fromId(int id) {
            return IntEnum.fromId(IntervalType.class, id);
        }

        public static IntervalType fromId(int id, boolean safe) {
            return IntEnum.fromId(IntervalType.class, id, safe);
        }

        IntervalType(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }
    }

    List<ComMailingParameter> getAllParameters(@VelocityCheck int companyID);

    List<ComMailingParameter> getMailingParameters(@VelocityCheck int companyID, int mailingID);

    List<ComMailingParameter> getParametersBySearchQuery(int companyID, String searchQuery, int mailingIdStartsWith);

    ComMailingParameter getParameter(int mailingInfoID);

    boolean insertParameter(ComMailingParameter parameter);

    boolean updateParameter(ComMailingParameter parameter);

    boolean updateParameters(@VelocityCheck int companyID, int mailingID, List<ComMailingParameter> parameterList, int adminId);

    boolean deleteParameter(int mailingInfoID);

    int deleteParameterByCompanyID(int companyID);

    /**
     * Reads a mailing parameter by its name.
     *
     * @param parameterName name of mailing parameter
     * @param mailingId     mailing ID
     * @param companyId     company ID
     * @return mailing parameter
     *
     * @throws MailingParameterNotFoundException if the mailing parameter was not found
     */
    ComMailingParameter getParameterByName(String parameterName, int mailingId, @VelocityCheck int companyId) throws MailingParameterNotFoundException;

    String getIntervalParameter(int mailingID);

    void updateNextStartParameter(int mailingID, Date nextStart);

    void insertMailingError(@VelocityCheck int companyId, int mailingID, String errorText);
}
