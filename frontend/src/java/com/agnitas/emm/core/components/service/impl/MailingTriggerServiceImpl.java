/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.service.impl;

import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.components.service.MailingTriggerService;
import org.agnitas.util.MailoutClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service("MailingTriggerService")
public class MailingTriggerServiceImpl implements MailingTriggerService {

    private static final Logger logger = LogManager.getLogger(MailingTriggerServiceImpl.class);

    @Override
    public boolean triggerMailing(int maildropStatusID, MailingType mailingType) throws Exception {
        try {
            if (maildropStatusID <= 0) {
                logger.warn( "maildropStatisID is 0");
                throw new Exception("maildropStatusID is 0");
            }

            // Interval Mailings are only triggered by an Jobqueue Worker
            if (mailingType != MailingType.INTERVAL) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Before Mailgun");
                }

                MailoutClient aClient = new MailoutClient();
                aClient.invoke("fire", Integer.toString(maildropStatusID));

                if (logger.isDebugEnabled()) {
                    logger.debug("After Mailgun");
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
