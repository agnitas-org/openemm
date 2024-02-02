package com.agnitas.emm.core.mediatypes.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.agnitas.beans.MediaTypeStatus;
import org.agnitas.emm.core.mediatypes.dao.MediatypesDao;
import org.agnitas.emm.core.mediatypes.dao.MediatypesDaoException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mediatype;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.mediatypes.service.MediaTypesService;

@Service("MediaTypesService")
public class MediaTypesServiceImplOpenemm implements MediaTypesService {
    private static final Logger logger = LogManager.getLogger(MediaTypesServiceImplOpenemm.class);
    
    private MediatypesDao mediatypesDao;
    
    public MediaTypesServiceImplOpenemm(MediatypesDao mediatypesDao) {
        this.mediatypesDao = mediatypesDao;
    }
    
    @Override
    public List<MediaTypes> getAllowedMediaTypes(Admin admin) {
        return Collections.singletonList(MediaTypes.EMAIL);
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
        return false;
    }
    
    @Override
    public void saveMediatypes(int companyID, int mailingId, Map<Integer, Mediatype> mediatypes) throws Exception {
	    mediatypesDao.saveMediatypes(companyID, mailingId, mediatypes);
    }
}
