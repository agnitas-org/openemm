/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.velocity.scriptvalidator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VelocityDirectiveScriptValidator implements ScriptValidator {

    private final Pattern directivePattern = Pattern.compile("^.*(#(?:include|parse)).*$", Pattern.DOTALL);

    @Override
    public void validateScript(String script) throws ScriptValidationException {
        Matcher m = directivePattern.matcher(script);

        if (m.matches()) {
            throw new IllegalVelocityDirectiveException(m.group(1));
        }
    }

}
