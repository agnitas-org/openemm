/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.service;

import java.util.Collection;
import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.grid.grid.beans.ComGridTemplate;
import com.agnitas.emm.grid.grid.beans.ComTemplateSettings;
import com.agnitas.emm.grid.grid.service.MailingCreationOptions;

/**
 * This class is wrapper for all grid services.
 *  All implemented methods contain verification if grid service bean is autowired
 */
public interface GridServiceWrapper {
    
    int getGridTemplateIdByMailingId(int mailingId);

    Map<Integer, Integer> getGridTemplateIdsByMailingIds(int companyId, Collection<Integer> mailingIds);
    
    ComGridTemplate getGridTemplate(int companyId, int templateId);
    
    Map<String, Object> getMailingGridInfo(int companyId, int mailingId);
    
    void saveMailingGridInfo(int mailingId, int companyId, Map<String, Object> data);
    
    ComTemplateSettings getGridTemplateSettings(int templateId, Admin admin);
    
    Mailing createGridMailing(Admin admin, int templateId, MailingCreationOptions creationOptions) throws Exception;
    
    void saveUndoGridMailing(int mailingId, int gridTemplateId, int adminId);
    
    void restoreGridMailingUndo(int undoId, Mailing mailing);
    
}
