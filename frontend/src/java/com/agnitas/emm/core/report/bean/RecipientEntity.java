/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.bean;

import java.util.Map;

public interface RecipientEntity {

    int getId();

    void setId(int id);

    int getSalutation();

    void setSalutation(int salutation);

    String getTitle();

    void setTitle(String title);

    String getFirstName();

    void setFirstName(String firstName);

    String getLastName();

    void setLastName(String lastName);

    String getEmail();

    void setEmail(String email);

    boolean isTrackingVeto();

    void setTrackingVeto(boolean trackingVeto);

    int getMailFormat();

    void setMailFormat(int mailFormat);

    Map<String, Object> getOtherRecipientData();

    void setOtherRecipientData(Map<String, Object> otherRecipientData);
}
