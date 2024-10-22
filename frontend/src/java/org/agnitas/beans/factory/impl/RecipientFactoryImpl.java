/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.factory.impl;

import org.agnitas.beans.Recipient;
import org.agnitas.beans.factory.BindingEntryFactory;
import org.agnitas.beans.factory.RecipientFactory;
import org.agnitas.beans.impl.RecipientImpl;
import org.agnitas.emm.core.blacklist.service.BlacklistService;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.ComBindingEntryDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.emm.core.service.RecipientFieldService;


public class RecipientFactoryImpl implements RecipientFactory {

    protected ComRecipientDao recipientDao;
    protected BlacklistService blacklistService;
    protected RecipientFieldService recipientFieldService;
    protected BindingEntryFactory bindingEntryFactory;
    protected ComBindingEntryDao bindingEntryDao;

	@Required
    public void setRecipientDao(ComRecipientDao recipientDao) {
        this.recipientDao = recipientDao;
    }

    @Required
    public void setRecipientFieldService(RecipientFieldService recipientFieldService) {
        this.recipientFieldService = recipientFieldService;
    }

    @Required
    public void setBindingEntryFactory(final BindingEntryFactory factory) {
    	this.bindingEntryFactory = factory;
    }

    @Required
    public void setBindingEntryDao(ComBindingEntryDao bindingEntryDao) {
		this.bindingEntryDao = bindingEntryDao;
	}

	@Required
    public void setBlacklistService(BlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    @Override
    public Recipient newRecipient() {
    	return newRecipient(0);
    }

    @Override
    public Recipient newRecipient(final int companyID) {
        RecipientImpl recipient = new RecipientImpl();

        recipient.setRecipientDao(recipientDao);
        recipient.setRecipientFieldService(recipientFieldService);
        recipient.setBindingEntryFactory(bindingEntryFactory);
        recipient.setBindingEntryDao(bindingEntryDao);
        recipient.setBlacklistService(blacklistService);
        recipient.setRecipientFactory(this);

        if (companyID > 0) {
        	recipient.setCompanyID(companyID);
        	recipient.loadCustDBStructure();
        }

        return recipient;
    }
}
