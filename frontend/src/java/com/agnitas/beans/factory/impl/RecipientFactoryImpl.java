/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.factory.impl;

import com.agnitas.beans.Recipient;
import com.agnitas.beans.factory.BindingEntryFactory;
import com.agnitas.beans.factory.RecipientFactory;
import com.agnitas.beans.impl.RecipientImpl;
import com.agnitas.emm.core.blacklist.service.BlacklistService;
import com.agnitas.dao.BindingEntryDao;
import com.agnitas.dao.RecipientDao;
import com.agnitas.emm.core.service.RecipientFieldService;


public class RecipientFactoryImpl implements RecipientFactory {

    protected RecipientDao recipientDao;
    protected BlacklistService blacklistService;
    protected RecipientFieldService recipientFieldService;
    protected BindingEntryFactory bindingEntryFactory;
    protected BindingEntryDao bindingEntryDao;

    public void setRecipientDao(RecipientDao recipientDao) {
        this.recipientDao = recipientDao;
    }

    public void setRecipientFieldService(RecipientFieldService recipientFieldService) {
        this.recipientFieldService = recipientFieldService;
    }

    public void setBindingEntryFactory(final BindingEntryFactory factory) {
    	this.bindingEntryFactory = factory;
    }

    public void setBindingEntryDao(BindingEntryDao bindingEntryDao) {
		this.bindingEntryDao = bindingEntryDao;
	}

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
