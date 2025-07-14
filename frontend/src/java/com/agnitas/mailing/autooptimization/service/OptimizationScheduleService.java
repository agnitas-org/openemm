/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.autooptimization.service;

import java.util.Map;

import com.agnitas.beans.impl.MaildropDeleteException;
import org.agnitas.emm.core.mailing.MailingAllReadySentException;

import com.agnitas.beans.MailingSendingProperties;
import com.agnitas.mailing.autooptimization.beans.Optimization;

public interface OptimizationScheduleService {

	void scheduleOptimization(Optimization optimization) throws MailingAllReadySentException, OptimizationIsFinishedException, MaildropDeleteException;

    void scheduleOptimization(Optimization optimization, Map<Integer, MailingSendingProperties> properties)
            throws MailingAllReadySentException,
            OptimizationIsFinishedException, MaildropDeleteException;

    void unscheduleOptimization(Optimization optimization) throws MaildropDeleteException;
}
