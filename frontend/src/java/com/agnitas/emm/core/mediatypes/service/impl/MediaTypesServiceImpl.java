/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mediatypes.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.Mediatype;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.mediatypes.service.MediaTypesService;
import org.agnitas.beans.MediaTypeStatus;
import org.agnitas.emm.core.mediatypes.dao.MediatypesDao;
import org.agnitas.emm.core.mediatypes.dao.MediatypesDaoException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MediaTypesServiceImpl implements MediaTypesService {

    private static final Logger logger = LogManager.getLogger(MediaTypesServiceImpl.class);

    protected final MediatypesDao mediatypesDao;

    public MediaTypesServiceImpl(MediatypesDao mediatypesDao) {
        this.mediatypesDao = mediatypesDao;
    }

    @Override
    public List<MediaTypes> getAllowedMediaTypes(Admin admin) {
        return Arrays.stream(MediaTypes.valuesSortedByDefaultValuePriority())
                .filter(type -> type == MediaTypes.EMAIL || admin.permissionAllowed(type.getRequiredPermission()))
                .collect(Collectors.toList());
    }

    @Override
    public Mediatype getActiveMediaType(int companyId, int mailingId) {
        if (mailingId == 0 || companyId == 0) {
            return null;
        }
        return tryGetMailingMediatype(companyId, mailingId);
    }

    private Mediatype tryGetMailingMediatype(int companyId, int mailingId) {
        try {
            return mediatypesDao.loadMediatypes(mailingId, companyId)
                    .entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue)
                    .filter(mediatype -> mediatype.getStatus() == MediaTypeStatus.Active.getCode())
                    .findFirst().orElse(null);
        } catch (MediatypesDaoException e) {
            logger.warn("Could not load media types for mailing ID {}", mailingId, e);
            return null;
        }
    }

    @Override
    public boolean saveEncryptedState(int mailingId, int companyId, boolean isEncryptedSend) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveMediatypes(int companyID, int mailingId, Map<Integer, Mediatype> mediatypes) throws Exception {
        mediatypesDao.saveMediatypes(companyID, mailingId, mediatypes);
    }

    @Override
    public List<MediaTypes> getActiveMediaTypes(Mailing mailing) {
        return mailing.getMediatypes().entrySet().stream()
                .filter(entry -> entry.getValue().getStatus() == MediaTypeStatus.Active.getCode())
                .map(entry -> MediaTypes.getMediaTypeForCode(entry.getKey()))
                .collect(Collectors.toList());
    }
}
