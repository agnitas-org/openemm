/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.mailing.bean.LightweightMailing;
import com.agnitas.emm.grid.grid.beans.GridTemplate;
import org.springframework.transaction.annotation.Transactional;

public interface MailingGridService {

    int getGridTemplateId(int mailingID);

    Map<Integer, Integer> getGridTemplateIds(int companyId, Collection<Integer> mailingIds);
    
    Map<String, Object> getMailingGridInfo(int mailingID, int companyID);
    
    void saveMailingGridInfo(int mailingID, int companyID, Map<String, Object> mailingGridInfo);
    
    List<GridTemplate> getReleasedGridTemplates(String searchName, Admin admin, String sort, String direction);

    boolean hasAvailableGridTemplates(Admin admin);

    List<LightweightMailing> getGridMailings(Admin admin);

    @Transactional
    void saveUndoGridMailing(int mailingId, int gridTemplateId, int adminId);
    
    void restoreGridMailingUndo(int undoId, Mailing mailing);

    void clearUndoData(List<Integer> mailingsIds);

}
