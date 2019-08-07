/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.generator.factory;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.report.generator.TableGenerator;

@Component("tableGeneratorFactory")
public class TableGeneratorFactory {

    private static final Logger logger = Logger.getLogger(TableGeneratorFactory.class);
    private static final Map<String, TableGenerator> tableGenerators = new HashMap<>();

    public void register(String name, TableGenerator tableGenerator) {
        if (tableGenerators.containsKey(name)) {
            logger.warn(String.format("TableGenerator [%s] already registered and will be ignored", name));
        } else {
            tableGenerators.put(name, tableGenerator);
        }
    }

    public TableGenerator getGenerator(String generatorName) {
        return tableGenerators.get(generatorName);
    }
}
