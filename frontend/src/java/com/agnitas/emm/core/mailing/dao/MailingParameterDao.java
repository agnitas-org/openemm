/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.dao;

import com.agnitas.beans.IntEnum;
import com.agnitas.emm.core.mailing.bean.MailingParameter;
import com.agnitas.emm.core.mailing.dao.impl.MailingParameterNotFoundException;
import com.agnitas.emm.core.mailing.forms.MailingParamOverviewFilter;
import com.agnitas.beans.impl.PaginatedListImpl;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public interface MailingParameterDao {

    enum ReservedMailingParam {
        INTERVAL("interval"),
        ERROR("error"),
        NEXT_START("next_start");

        private final String name;

        ReservedMailingParam(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static boolean isReservedParam(String paramName) {
            return Stream.of(values())
                    .anyMatch(p -> p.getName().equals(paramName));
        }
    }

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

    List<MailingParameter> getAllParameters(int companyID);

    List<MailingParameter> getMailingParameters(int companyID, int mailingID);

    PaginatedListImpl<MailingParameter> getParameters(MailingParamOverviewFilter filter, int companyID);

    MailingParameter getParameter(int mailingInfoID);

    boolean insertParameter(MailingParameter parameter);

    boolean updateParameter(MailingParameter parameter);

    boolean updateParameters(int companyID, int mailingID, List<MailingParameter> parameterList, int adminId);

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
    MailingParameter getParameterByName(String parameterName, int mailingId, int companyId) throws MailingParameterNotFoundException;

    String getIntervalParameter(int mailingID);

    void updateNextStartParameter(int mailingID, Date nextStart);

    void insertMailingError(int companyId, int mailingID, String errorText);
}
