/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.password;

import org.agnitas.emm.core.commons.password.PasswordCheckHandler;
import org.agnitas.emm.core.commons.password.PasswordCheckImpl;
import org.agnitas.emm.core.commons.password.PasswordConstraintException;
import org.agnitas.emm.core.commons.password.PasswordMatchesCurrentPasswordException;
import org.agnitas.emm.core.commons.password.PasswordUtil;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.supervisor.beans.Supervisor;
import com.agnitas.emm.core.supervisor.common.SupervisorException;
import com.agnitas.emm.core.supervisor.service.ComSupervisorService;
import com.agnitas.service.SimpleServiceResult;

/**
 * Implementation of {@link ComPasswordCheck}.
 */
public class ComPasswordCheckImpl extends PasswordCheckImpl implements ComPasswordCheck {
    /**
     * Service for accessing supervisor data.
     */
    private ComSupervisorService supervisorService;

    @Override
    public boolean checkSupervisorPassword(String password, Supervisor supervisor, PasswordCheckHandler handler) throws SupervisorException {
        try {
            PasswordUtil.checkPasswordConstraints(password);

            // Check that given password differs from current supervisor password
            if (this.supervisorService.isCurrentPassword(supervisor, password)) {
                throw new PasswordMatchesCurrentPasswordException();
            }

            return true;
        } catch (PasswordConstraintException e) {
            handleException(e, handler);

            return false;
        }
    }

    @Override
    public SimpleServiceResult checkSupervisorPassword(String password, Supervisor supervisor) throws SupervisorException {
        try {
            PasswordUtil.checkPasswordConstraints(password);

            // Check that given password differs from current supervisor password
            if (supervisorService.isCurrentPassword(supervisor, password)) {
                throw new PasswordMatchesCurrentPasswordException();
            }

            return new SimpleServiceResult(true);
        } catch (PasswordConstraintException e) {
            return new SimpleServiceResult(false, asMessage(e));
        }
    }

    @Override
    public boolean checkSupervisorPassword(String password, PasswordCheckHandler handler) {
        try {
            PasswordUtil.checkPasswordConstraints(password);
            return true;
        } catch (PasswordConstraintException e) {
            handleException(e, handler);
            return false;
        }
    }

    // ------------------------------------------------------------ Dependency Injection

    /**
     * Set service for accessing supervisor data.
     *
     * @param supervisorService service for accessing supervisor data
     */
    @Required
    public void setSupervisorService(ComSupervisorService supervisorService) {
        this.supervisorService = supervisorService;
    }
}
