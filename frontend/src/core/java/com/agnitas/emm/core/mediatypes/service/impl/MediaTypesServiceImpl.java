/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.mediatypes.service.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MediaTypeStatus;
import com.agnitas.beans.Mediatype;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.mediatypes.dao.MediatypesDao;
import com.agnitas.emm.core.mediatypes.service.MediaTypesService;

public class MediaTypesServiceImpl implements MediaTypesService {

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
    public List<MediaTypes> getActiveMediaTypeTypes(int mailingId, int companyId) {
        return getActiveMediaTypes(mailingId, companyId)
                .stream()
                .map(Mediatype::getMediaType)
                .toList();
    }

    private List<Mediatype> getActiveMediaTypes(int mailingId, int companyId) {
        Map<Integer, Mediatype> mediatypes = mediatypesDao.loadMediatypes(mailingId, companyId);
        return getActiveMediaTypes(mediatypes);
    }

    private List<Mediatype> getActiveMediaTypes(Map<Integer, Mediatype> mediatypesMap) {
        return mediatypesMap.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(entry -> MediaTypes.getMediaTypeForCode(entry.getKey()).getDefaultTypePriority()))
                .map(Map.Entry::getValue)
                .filter(mediatype -> mediatype.getStatus() == MediaTypeStatus.Active.getCode())
                .toList();
    }

    @Override
    public Mediatype getActiveMediaType(int companyId, int mailingId) {
        if (mailingId == 0 || companyId == 0) {
            return null;
        }

        return getActiveMediaTypes(mailingId, companyId)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Mediatype getActiveMediaType(Mailing mailing) {
        return getActiveMediaTypes(mailing.getMediatypes())
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean saveEncryptedState(int mailingId, int companyId, boolean isEncryptedSend) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveMediatypes(int companyID, int mailingId, Map<Integer, Mediatype> mediatypes) {
        mediatypesDao.saveMediatypes(companyID, mailingId, mediatypes);
    }

    @Override
    public List<MediaTypes> getActiveMediaTypes(Mailing mailing) {
        return mailing.getMediatypes().entrySet().stream()
                .filter(entry -> entry.getValue().getStatus() == MediaTypeStatus.Active.getCode())
                .map(entry -> MediaTypes.getMediaTypeForCode(entry.getKey()))
                .toList();
    }
}
