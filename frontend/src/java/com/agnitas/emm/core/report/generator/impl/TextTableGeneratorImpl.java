/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.generator.impl;

import java.util.Collection;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.report.generator.TableGenerator;
import com.agnitas.emm.core.report.generator.bean.Table;
import com.agnitas.emm.core.report.generator.parser.GenericTableParser;
import com.agnitas.emm.core.report.generator.printer.GenericTablePrinter;

@Component("txtTableGenerator")
public class TextTableGeneratorImpl implements TableGenerator {
    private final GenericTableParser<Table> parser;
    private final GenericTablePrinter<Table> printer;

    @Autowired
    public TextTableGeneratorImpl(GenericTableParser<Table> parser, @Qualifier("txtTablePrinter") GenericTablePrinter<Table> printer) {
        this.parser = parser;
        this.printer = printer;
    }

    @Override
    public String generate(Collection<?> collection, Locale locale) {
        Table parsedTable = parser.parse(collection);
        return printer.print(parsedTable, locale);
    }
}
