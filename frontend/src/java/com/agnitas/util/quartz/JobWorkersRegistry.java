/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util.quartz;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.agnitas.service.JobWorkerBase;
import com.agnitas.util.quartz.component.JobWorkerScanner;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class JobWorkersRegistry implements InitializingBean {

    private final Map<String, Class<? extends JobWorkerBase>> workersStorage = new HashMap<>();

    private final JobWorkerScanner jobWorkerScanner;

    public JobWorkersRegistry(JobWorkerScanner jobWorkerScanner) {
        this.jobWorkerScanner = jobWorkerScanner;
    }

    public Optional<Class<? extends JobWorkerBase>> findWorker(String workerName) {
        return Optional.ofNullable(workersStorage.get(workerName));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        workersStorage.putAll(jobWorkerScanner.scan("com.agnitas"));
    }

}
