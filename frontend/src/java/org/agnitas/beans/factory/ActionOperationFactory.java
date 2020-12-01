/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package org.agnitas.beans.factory;

import java.util.List;

import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;


public interface ActionOperationFactory {
	
	AbstractActionOperationParameters newActionOperation(String typeName);

    AbstractActionOperationParameters newActionOperation(ActionOperationType type);

    List<ActionOperationType> getTypesList();

    String[] getTypeNames();

    //Required since old implementations stored in db hasn't such method
    String getType(ActionOperationParameters actionOperation);
    
}
