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
import com.agnitas.service.JobWorker;

public class DiskSpaceCheckJobWorker extends JobWorker {

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

            javaMailService.sendEmail(0, emails, "Remaining disk space", emailText, emailText);
        }

        return null;
    }

}
