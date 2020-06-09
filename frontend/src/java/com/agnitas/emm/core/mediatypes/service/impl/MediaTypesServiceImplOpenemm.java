package com.agnitas.emm.core.mediatypes.service.impl;

import java.util.Collections;
import java.util.List;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.springframework.stereotype.Service;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.mediatypes.service.MediaTypesService;

@Service("MediaTypesService")
public class MediaTypesServiceImplOpenemm implements MediaTypesService {
    @Override
    public List<MediaTypes> getAllowedMediaTypes(ComAdmin admin) {
        return Collections.singletonList(MediaTypes.EMAIL);
    }
    
    @Override
    public MediaTypes getActiveMediaType(@VelocityCheck int companyId, int mailingId) {
        return MediaTypes.EMAIL;
    }
}
