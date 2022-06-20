/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.factory.impl;

import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.factory.BindingEntryFactory;
import org.agnitas.beans.impl.BindingEntryImpl;

import com.agnitas.dao.ComBindingEntryDao;


public class BindingEntryFactoryImpl implements BindingEntryFactory {

    private ComBindingEntryDao bindingEntryDao;

    @Override
	public ComBindingEntryDao getBindingEntryDao() {
        return bindingEntryDao;
    }

    @Override
	public void setBindingEntryDao(ComBindingEntryDao bindingEntryDao) {
        this.bindingEntryDao = bindingEntryDao;
    }

    @Override
    public BindingEntry newBindingEntry() {
        BindingEntryImpl bindingEntry = new BindingEntryImpl();
        bindingEntry.setBindingEntryDao(bindingEntryDao);
        return bindingEntry;
    }
}
