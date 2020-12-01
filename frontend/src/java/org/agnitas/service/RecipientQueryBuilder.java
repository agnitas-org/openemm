/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.util.List;

import org.agnitas.util.SqlPreparedStatementManager;
import org.agnitas.web.RecipientForm;

import com.agnitas.beans.ComAdmin;

public interface RecipientQueryBuilder {
	String createEqlFromForm(final RecipientForm form, final int companyId);

	SqlPreparedStatementManager getRecipientListSQLStatement(ComAdmin admin, RecipientSqlOptions options) throws Exception;

	SqlPreparedStatementManager getDuplicateAnalysisSQLStatement(ComAdmin admin, RecipientDuplicateSqlOptions options, boolean includeBounceLoad) throws Exception;
    SqlPreparedStatementManager getDuplicateAnalysisSQLStatement(ComAdmin admin, RecipientDuplicateSqlOptions options, List<String> selectedColumns, boolean includeBounceLoad) throws Exception;
}
