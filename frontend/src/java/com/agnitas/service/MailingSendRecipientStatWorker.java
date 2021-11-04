/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Callable;

import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.Mailing;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.MailingStatisticsDao;

public class MailingSendRecipientStatWorker implements Callable<Map<Integer, Integer>>, Serializable {
	private static final long serialVersionUID = 1866393658794265093L;

	private ComMailingDao mailingDao;
	private MailingStatisticsDao mailingStatisticsDao;
    private int mailingId;
    private int companyId;

    public MailingSendRecipientStatWorker(ComMailingDao mailingDao, MailingStatisticsDao mailingStatisticsDao, int mailingId, @VelocityCheck int companyId) {
        this.mailingDao = mailingDao;
        this.mailingStatisticsDao = mailingStatisticsDao;
        this.mailingId = mailingId;
        this.companyId = companyId;
    }

    @Override
	public Map<Integer, Integer> call() throws Exception {
        Mailing mailing = mailingDao.getMailing(mailingId, companyId);
        return mailingStatisticsDao.getSendStats(mailing, companyId);
    }
}
