/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.mailinglist.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.impl.MailinglistImpl;
import org.agnitas.dao.MailingDao;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.emm.core.mailinglist.service.MailinglistModel;
import org.agnitas.emm.core.mailinglist.service.MailinglistNotExistException;
import org.agnitas.emm.core.mailinglist.service.MailinglistService;
import org.agnitas.emm.core.validator.annotation.Validate;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.dao.ComBindingEntryDao;
import com.agnitas.dao.ComCompanyDao;

public class MailinglistServiceImpl implements MailinglistService {
	private static final Logger LOGGER = Logger.getLogger(MailinglistServiceImpl.class);
	
	@Resource(name="MailinglistDao")
	private MailinglistDao mailinglistDao;
	@Resource(name="MailingDao")
	private MailingDao mailingDao;
	@Resource(name="BindingEntryDao")
	private ComBindingEntryDao bindingDao;
	@Resource(name="CompanyDao")
	private ComCompanyDao companyDao;

	@Override
	@Transactional
	@Validate(groups = MailinglistModel.AddGroup.class)
	public int addMailinglist(MailinglistModel model) {
        Mailinglist mailinglist = new MailinglistImpl();
        mailinglist.setCompanyID(model.getCompanyId());
        mailinglist.setShortname(model.getShortname());
        mailinglist.setDescription(model.getDescription());
		return mailinglistDao.saveMailinglist(mailinglist);
	}

	@Override
	@Transactional
	@Validate(groups = MailinglistModel.UpdateGroup.class)
    public void updateMailinglist(MailinglistModel model) throws MailinglistException {
		Mailinglist mailinglist = getMailinglist(model);
		mailinglist.setShortname(model.getShortname());
		mailinglist.setDescription(model.getDescription());
		mailinglistDao.saveMailinglist(mailinglist);
    }

    @Override
    @Transactional
    @Validate(groups =  MailinglistModel.GetGroup.class)
    public Mailinglist getMailinglist(MailinglistModel model) throws MailinglistException {
        Mailinglist mailingList = mailinglistDao.getMailinglist(model.getMailinglistId(), model.getCompanyId());
        if (mailingList == null) {
        	throw new MailinglistNotExistException(model.getMailinglistId(), model.getCompanyId());
        }
        return mailingList;
    }

	@Override
	@Transactional
	@Validate(groups =  MailinglistModel.GetGroup.class)
	public boolean deleteMailinglist(MailinglistModel model) throws MailinglistException {
		int mailingListId = model.getMailinglistId();
		int companyId = model.getCompanyId();
		if (!mailinglistDao.exist(mailingListId, companyId)) {
			throw new MailinglistNotExistException(mailingListId, companyId);
		} else if(mailinglistDao.checkMailinglistInUse(mailingListId, companyId)){
			throw new MailinglistInUseException(mailingListId, companyId);
		} else {
				// delete bindings, only if no mailing refers to this mailinglist

				// EMM-5636: Removing bindings is done by a separate process in background
				// mailinglistDao.deleteBindings(model.getMailinglistId(), model.getCompanyId());
				return mailinglistDao.deleteMailinglist(mailingListId, companyId);
		}
	}

	@Override
	@Transactional
	public List<Mailinglist> listMailinglists(final int companyId) {
		try {
			return mailinglistDao.getMailinglists(companyId);
		} catch (Exception e) {
			LOGGER.error(String.format("Error reading mailing lists of company %d", companyId), e);
		}

		return new ArrayList<>();
	}
}
