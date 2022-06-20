/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface ProfileRecipientFields extends Serializable {

    public String getEmail();

    public void setEmail(String email);

    public String getGender();

    public void setGender(String gender);

    public String getMailtype();

    public void setMailtype(String mailtype);

    public String getFirstname();

    public void setFirstname(String firstname);

    public String getLastname();

    public void setLastname(String lastname);

    public String getCreation_date();

    public void setCreation_date(String creation_date);

    public String getChange_date();

    public void setChange_date(String change_date);

    public String getTitle();

    public void setTitle(String title);

    public String getTemporaryId();

    public void setTemporaryId(String customer_id);

    public List<Integer> getUpdatedIds();

    public void addUpdatedIds(Integer updatedId);

    public Map<String, String> getCustomFields();

    public void setCustomFields(Map<String, String> customFields);

    public String getMailtypeDefined();

    public void setMailtypeDefined(String isMailtypeDefined);
}
