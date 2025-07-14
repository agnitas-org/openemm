/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.factory.impl;

import com.agnitas.dao.impl.DaoLookupFactory;
import com.agnitas.beans.factory.UserActivityLogExportWorkerFactory;
import com.agnitas.service.ActivityLogExportWorker;
import com.agnitas.service.UserActivityLogExportWorker;
import org.springframework.stereotype.Component;

@Component("userActivityLogExportWorkerFactory")
public class UserActivityLogExportWorkerFactoryImpl implements UserActivityLogExportWorkerFactory {
    private DaoLookupFactory daoLookupFactory;

    public UserActivityLogExportWorkerFactoryImpl(DaoLookupFactory daoLookupFactory) {
        this.daoLookupFactory = daoLookupFactory;
    }

	@Override
    public UserActivityLogExportWorker.Builder getBuilderInstance() {
        return UserActivityLogExportWorker.getBuilder(daoLookupFactory.getBeanDataSource());
    }

    @Override
    public ActivityLogExportWorker.Builder getBuilderInstanceRedesigned() {
        return ActivityLogExportWorker.getBuilder(daoLookupFactory.getBeanDataSource());
    }
}
