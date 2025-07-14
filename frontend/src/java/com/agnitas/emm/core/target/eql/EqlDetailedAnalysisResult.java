/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.agnitas.emm.core.target.eql.parser.EqlSyntaxError;

public class EqlDetailedAnalysisResult {

    private final EqlAnalysisResult analysisResult;
    private final List<EqlSyntaxError> syntaxErrors;

    public EqlDetailedAnalysisResult(EqlAnalysisResult analysisResult, List<EqlSyntaxError> syntaxErrors) {
        this.analysisResult = analysisResult;
        this.syntaxErrors = syntaxErrors;
    }

    public boolean isAnaliseSuccess() {
        return CollectionUtils.isEmpty(syntaxErrors) && analysisResult != null;
    }

    public boolean isMailTrackingRequired() {
        return analysisResult.isMailTrackingRequired();
    }

    public List<EqlSyntaxError> getSyntaxErrors() {
        return syntaxErrors;
    }
}
