/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.serverstatus.service.job;

import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.emm.core.serverstatus.service.ServerStatusService;
import com.agnitas.service.JobWorkerBase;
import com.agnitas.util.quartz.JobWorker;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@JobWorker("DiskSpaceCheck")
public class DiskSpaceCheckJobWorker extends JobWorkerBase {

    private static final Logger logger = LogManager.getLogger(DiskSpaceCheckJobWorker.class);

    private static final int DISK_SPACE_THRESHOLD_PERCENTAGE = 25;

    @Override
    public String runJob() {
        ServerStatusService serverStatusService = serviceLookupFactory.getBeanServerStatusService();
        JavaMailService javaMailService = serviceLookupFactory.getBeanJavaMailService();
        CompanyService companyService = serviceLookupFactory.getBeanCompanyService();

        int percentage = serverStatusService.calcDiskSpaceFreePercentage();

        if (percentage < DISK_SPACE_THRESHOLD_PERCENTAGE) {
            String emails = companyService.getTechnicalContact(1);
            String emailText = "There is only " + percentage + "% disk space left, please check and extend if necessary.";

            if (StringUtils.isBlank(emails)) {
                logger.error("Technical contact not defined for companyID: 1 in client settings! Unable to send email about remaining disk space:\n'{}'", emailText);
            } else {
                javaMailService.sendEmail(0, emails, "Remaining disk space", emailText, emailText);
            }
        }

        return null;
    }
}
