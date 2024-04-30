/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import com.agnitas.beans.Mailing;
import org.agnitas.util.DynTagException;

import java.util.Set;

public interface ComMailingContentService {
    boolean isGenerationAvailable(Mailing mailing);
    void generateTextContent(Mailing mailing) throws DynTagException;
    String generateDynName(String sourceName, Set<String> namesInUse);
}
