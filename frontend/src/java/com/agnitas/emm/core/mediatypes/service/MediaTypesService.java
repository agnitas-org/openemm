/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mediatypes.service;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.Mediatype;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;

import java.util.List;
import java.util.Map;

public interface MediaTypesService {
    
    List<MediaTypes> getAllowedMediaTypes(Admin admin);
    
    Mediatype getActiveMediaType(int companyId, int mailingId);

    boolean saveEncryptedState(int mailingId, int companyId, boolean isEncryptedSend);

    void saveMediatypes(int companyID, int mailingId, Map<Integer, Mediatype> mediatypes) throws Exception;

    List<MediaTypes> getActiveMediaTypes(Mailing mailing);
}
