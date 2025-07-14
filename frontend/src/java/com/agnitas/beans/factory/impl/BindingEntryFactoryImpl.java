/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.factory.impl;

import com.agnitas.beans.BindingEntry;
import com.agnitas.beans.factory.BindingEntryFactory;
import com.agnitas.beans.impl.BindingEntryImpl;

import com.agnitas.dao.BindingEntryDao;


public class BindingEntryFactoryImpl implements BindingEntryFactory {

    private BindingEntryDao bindingEntryDao;

    @Override
	public BindingEntryDao getBindingEntryDao() {
        return bindingEntryDao;
    }

    @Override
	public void setBindingEntryDao(BindingEntryDao bindingEntryDao) {
        this.bindingEntryDao = bindingEntryDao;
    }

    @Override
    public BindingEntry newBindingEntry() {
        BindingEntryImpl bindingEntry = new BindingEntryImpl();
        bindingEntry.setBindingEntryDao(bindingEntryDao);
        return bindingEntry;
    }
}
