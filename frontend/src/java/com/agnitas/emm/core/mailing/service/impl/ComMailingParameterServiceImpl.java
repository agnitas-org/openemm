/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.mailing.bean.ComMailingParameter;
import com.agnitas.emm.core.mailing.dao.ComMailingParameterDao;
import com.agnitas.emm.core.mailing.forms.MailingParamOverviewFilter;
import com.agnitas.emm.core.mailing.service.ComMailingParameterService;
import com.agnitas.emm.core.mailing.service.MailingParameterLogService;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ComMailingParameterServiceImpl implements ComMailingParameterService {
	
	private ComMailingParameterDao mailingParameterDao;

	private MailingParameterLogService mailingParameterLogService;
	
	@Required
	public void setMailingParameterLogService(MailingParameterLogService mailingParameterLogService) {
		this.mailingParameterLogService = mailingParameterLogService;
	}
	
	@Required
	public void setMailingParameterDao(ComMailingParameterDao comMailingParameterDao) {
		mailingParameterDao = comMailingParameterDao;
	}

	@Override
	public List<ComMailingParameter> getMailingParameters(int companyId, int mailingId) {
		if (companyId > 0 && mailingId > 0) {
			return mailingParameterDao.getMailingParameters(companyId, mailingId);
		}
		return Collections.emptyList();
	}

	@Override
	public PaginatedListImpl<ComMailingParameter> getParametersBySearchQuery(MailingParamOverviewFilter filter, int companyID) {
		return mailingParameterDao.getParameters(filter, companyID);
	}

	@Override
	public ComMailingParameter getParameter(int mailingInfoID, final Admin admin) {
		if (mailingInfoID > 0) {
			return this.mailingParameterDao.getParameter(mailingInfoID);
		}

		//error message not found
		return null;
	}
	
    @Override
    public boolean saveParameter(ComMailingParameter parameter, final Admin admin) {
        if (parameter.getMailingInfoID() > 0) {
            return updateParameter(parameter, admin);
        } else {
            return insertParameter(parameter, admin);
        }
    }	
	
	@Override
    public boolean insertParameter(ComMailingParameter parameter, final Admin admin) {
        if (parameter != null && parameter.getMailingInfoID() == 0) {
            return this.mailingParameterDao.insertParameter(parameter);
        }

		//error message parameter is null
		return false;
    }

    @Override
    public boolean updateParameter(ComMailingParameter parameter, final Admin admin) {
        if (parameter != null && parameter.getMailingInfoID() > 0) {
            return this.mailingParameterDao.updateParameter(parameter);
        }

		//error message parameter is null
		return false;
    }

    @Override
    public boolean deleteParameter(int mailingInfoID, final Admin admin) {
        if (mailingInfoID > 0) {
			return this.mailingParameterDao.deleteParameter(mailingInfoID);
        }

		//error message id = 0
		return false;
    }

	@Override
	public boolean updateParameters(int companyID, int mailingID, List<ComMailingParameter> parameterList, int adminId) {
		return mailingParameterDao.updateParameters(companyID, mailingID, parameterList, adminId);
	}

	@Override
	public boolean updateParameters(int companyId, int mailingId, List<ComMailingParameter> parameterList, int adminId, List<UserAction> userActions) {
		Map<Integer, ComMailingParameter> parametersOld = getParametersMap(companyId, mailingId);
		boolean success = updateParameters(companyId, mailingId, parameterList, adminId);
		Map<Integer, ComMailingParameter> parametersNew = getParametersMap(companyId, mailingId);
		
		userActions.addAll(mailingParameterLogService.getMailingParametersChangeLog(mailingId, parametersOld, parametersNew));

		return success;
	}
	
	private Map<Integer, ComMailingParameter> getParametersMap(int companyId, int mailingId) {
		return getMailingParameters(companyId, mailingId)
				.stream()
				.collect(Collectors.toMap(ComMailingParameter::getMailingInfoID, Function.identity()));
	}

	@Override
	public List<String> getNames(Set<Integer> ids, Admin admin) {
		return ids.stream()
				.map(id -> getParameter(id, admin))
				.filter(Objects::nonNull)
				.map(ComMailingParameter::getName)
				.collect(Collectors.toList());
	}

	@Override
	public void delete(Set<Integer> ids, Admin admin) {
		ids.forEach(id -> deleteParameter(id, admin));
	}
}
