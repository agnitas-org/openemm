/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.mailing.bean.MailingParameter;
import com.agnitas.emm.core.mailing.dao.MailingParameterDao;
import com.agnitas.emm.core.mailing.forms.MailingParamOverviewFilter;
import com.agnitas.emm.core.mailing.service.MailingParameterLogService;
import com.agnitas.emm.core.mailing.service.MailingParameterService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;

public class MailingParameterServiceImpl implements MailingParameterService {
	
	private MailingParameterDao mailingParameterDao;

	private MailingParameterLogService mailingParameterLogService;
	
	public void setMailingParameterLogService(MailingParameterLogService mailingParameterLogService) {
		this.mailingParameterLogService = mailingParameterLogService;
	}
	
	public void setMailingParameterDao(MailingParameterDao mailingParameterDao) {
		this.mailingParameterDao = mailingParameterDao;
	}

	@Override
	public List<MailingParameter> getMailingParameters(int companyId, int mailingId) {
		if (companyId > 0 && mailingId > 0) {
			return mailingParameterDao.getMailingParameters(companyId, mailingId);
		}
		return Collections.emptyList();
	}

	@Override
	public PaginatedList<MailingParameter> getOverview(MailingParamOverviewFilter filter, int companyID) {
		return mailingParameterDao.getParameters(filter, companyID);
	}

	@Override
	public MailingParameter getParameter(int mailingInfoID, final Admin admin) {
		if (mailingInfoID > 0) {
			return this.mailingParameterDao.getParameter(mailingInfoID);
		}

		//error message not found
		return null;
	}
	
    @Override
    public boolean saveParameter(MailingParameter parameter) {
        if (parameter.getMailingInfoID() > 0) {
            return updateParameter(parameter);
        } else {
            return insertParameter(parameter);
        }
    }	
	
    private boolean insertParameter(MailingParameter parameter) {
        if (parameter != null && parameter.getMailingInfoID() == 0) {
            return this.mailingParameterDao.insertParameter(parameter);
        }

		return false;
    }

    private boolean updateParameter(MailingParameter parameter) {
        if (parameter != null && parameter.getMailingInfoID() > 0) {
            return this.mailingParameterDao.updateParameter(parameter);
        }

		return false;
    }

    private boolean deleteParameter(int mailingInfoID) {
        if (mailingInfoID > 0) {
			return this.mailingParameterDao.deleteParameter(mailingInfoID);
        }

		//error message id = 0
		return false;
    }

	@Override
	public boolean updateParameters(int companyID, int mailingID, List<MailingParameter> parameterList, int adminId) {
		return mailingParameterDao.updateParameters(companyID, mailingID, parameterList, adminId);
	}

	@Override
	public boolean updateParameters(int companyId, int mailingId, List<MailingParameter> parameterList, int adminId, List<UserAction> userActions) {
		Map<Integer, MailingParameter> parametersOld = getParametersMap(companyId, mailingId);
		boolean success = updateParameters(companyId, mailingId, parameterList, adminId);
		Map<Integer, MailingParameter> parametersNew = getParametersMap(companyId, mailingId);
		
		userActions.addAll(mailingParameterLogService.getMailingParametersChangeLog(mailingId, parametersOld, parametersNew));

		return success;
	}
	
	private Map<Integer, MailingParameter> getParametersMap(int companyId, int mailingId) {
		return getMailingParameters(companyId, mailingId)
				.stream()
				.collect(Collectors.toMap(MailingParameter::getMailingInfoID, Function.identity()));
	}

	@Override
	public List<String> getNames(Set<Integer> ids, Admin admin) {
		return ids.stream()
				.map(id -> getParameter(id, admin))
				.filter(Objects::nonNull)
				.map(MailingParameter::getName)
				.toList();
	}

	@Override
	public void delete(Set<Integer> ids) {
		ids.forEach(this::deleteParameter);
	}
}
