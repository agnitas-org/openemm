/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.database.fulltext.operator.factory;

import java.util.HashMap;
import java.util.Map;

import com.agnitas.emm.core.commons.database.fulltext.operator.Operator;
import com.agnitas.emm.core.commons.database.fulltext.operator.impl.mysql.MySqlConjunction;
import com.agnitas.emm.core.commons.database.fulltext.operator.impl.mysql.MySqlDisjunction;

public class OperatorFactoryImpl implements OperatorFactory {

    @Override
    public Map<String, Operator> createOperators() {
        Map<String, Operator> operators = new HashMap<>();
        operators.put("+", new MySqlConjunction());
        operators.put(" ", new MySqlDisjunction());
        return operators;
    }
}
