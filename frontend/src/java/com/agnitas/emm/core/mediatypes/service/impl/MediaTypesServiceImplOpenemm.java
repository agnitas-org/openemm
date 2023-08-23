package com.agnitas.emm.core.mediatypes.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.agnitas.emm.core.mediatypes.dao.MediatypesDao;
import org.springframework.stereotype.Service;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mediatype;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.mediatypes.service.MediaTypesService;

@Service("MediaTypesService")
public class MediaTypesServiceImplOpenemm implements MediaTypesService {
    private MediatypesDao mediatypesDao;
    
    @Override
    public List<MediaTypes> getAllowedMediaTypes(Admin admin) {
        return Collections.singletonList(MediaTypes.EMAIL);
    }
    
    @Override
    public MediaTypes getActiveMediaType(int companyId, int mailingId) {
        return MediaTypes.EMAIL;
    }

    @Override
    public boolean saveEncryptedState(int mailingId, int companyId, boolean isEncryptedSend) {
        return false;
    }
    
    @Override
    public void saveMediatypes(int companyID, int mailingId, Map<Integer, Mediatype> mediatypes) throws Exception {
	    mediatypesDao.saveMediatypes(companyID, mailingId, mediatypes);
    }
}
