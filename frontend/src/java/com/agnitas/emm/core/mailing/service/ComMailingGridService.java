/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.agnitas.emm.grid.grid.beans.ComGridTemplate;
import org.agnitas.beans.Mailing;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.springframework.transaction.annotation.Transactional;

public interface ComMailingGridService {

    int getGridTemplateId(int mailingID);

    Map<Integer, Integer> getGridTemplateIds(@VelocityCheck int companyId, Collection<Integer> mailingIds);
    
    Map<String, Object> getMailingGridInfo(int mailingID, int companyID);
    
    void saveMailingGridInfo(int mailingID, int companyID, Map<String, Object> mailingGridInfo);
    
    List<ComGridTemplate> getReleasedGridTemplates(String sort, String direction, int pageNumber, int rowNumber, @VelocityCheck int companyId);

    @Transactional
    void saveUndoGridMailing(int mailingId, int gridTemplateId, int adminId);
    
    void deleteUndoDataOverLimit(int mailingId, int gridTemplateId);
    
    void restoreGridMailingUndo(int undoId, Mailing mailing);
}
