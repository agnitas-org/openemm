package com.agnitas.emm.restful.target;

import com.agnitas.dao.RecipientDao;
import com.agnitas.dao.TargetDao;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.useractivitylog.dao.RestfulUserActivityLogDao;
import com.agnitas.service.ColumnInfoService;
import org.springframework.stereotype.Component;

@Component("RestfulServiceHandler_target")
public class TargetRestfulServiceHandlerOpenemm extends TargetRestfulServiceHandler {

    public TargetRestfulServiceHandlerOpenemm(RestfulUserActivityLogDao userActivityLogDao, TargetService targetService, TargetDao targetDao, RecipientDao recipientDao, ColumnInfoService columnInfoService, ConfigService configService) {
        super(userActivityLogDao, targetService, targetDao, recipientDao, columnInfoService, configService);
    }

}
