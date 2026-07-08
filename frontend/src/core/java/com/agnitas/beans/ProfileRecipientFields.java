/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface ProfileRecipientFields extends Serializable {

    String getEmail();

    void setEmail(String email);

    String getGender();

    void setGender(String gender);

    String getMailtype();

    void setMailtype(String mailtype);

    String getFirstname();

    void setFirstname(String firstname);

    String getLastname();

    void setLastname(String lastname);

    String getCreation_date();

    void setCreation_date(String creationDate);

    String getChange_date();

    void setChange_date(String changeDate);

    String getTitle();

    void setTitle(String title);

    String getTemporaryId();

    void setTemporaryId(String customerId);

    List<Integer> getUpdatedIds();

    void addUpdatedIds(Integer updatedId);

    Map<String, String> getCustomFields();

    void setCustomFields(Map<String, String> customFields);

    String getMailtypeDefined();

    void setMailtypeDefined(String isMailtypeDefined);

}
