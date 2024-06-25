/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.form.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.agnitas.emm.core.target.eql.parser.EqlSyntaxError;
import com.agnitas.exception.DetailedRequestErrorException;
import com.agnitas.messages.Message;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.target.form.TargetEditForm;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.web.mvc.Popups;

@Component
public class TargetEditFormValidator {

    private final ComTargetService targetService;

    public TargetEditFormValidator(ComTargetService targetService) {
        this.targetService = targetService;
    }

    public boolean validate(int companyId, TargetEditForm form, Popups popups) {
        boolean isValid = true;

        if(StringUtils.isBlank(form.getShortname())) {
            isValid = false;
            popups.alert("error.name.is.empty");
        } else if (!targetService.checkIfTargetNameIsValid(form.getShortname())) {
            isValid = false;
            popups.alert("error.target.namenotallowed");
        } else if (targetService.checkIfTargetNameAlreadyExists(companyId, form.getShortname(), form.getTargetId())) {
            popups.alert("error.target.namealreadyexists");
            isValid = false;
        }

        return isValid;
    }

    public void throwEqlValidationException(String eql, EqlSyntaxError error) {
        throw new DetailedRequestErrorException(
                getPositionDetails(error),
                getEqlErrorMsg(eql, error)
        );
    }

    public Map<String, Object> getPositionDetails(EqlSyntaxError error) {
        return Map.of(
                "symbol", error.getSymbol(),
                "line", error.getLine(),
                "column", error.getColumn()
        );
    }

    public Message getEqlErrorMsg(String eql, EqlSyntaxError error) {
        List<Object> msgArgs = new ArrayList<>(Arrays.asList(error.getLine(), error.getColumn(), error.getSymbol()));

        Character notBalancedPairChar = getNotBalancedPairChar(eql);
        if (notBalancedPairChar != null) {
            msgArgs.add(notBalancedPairChar);
            return Message.of("error.target.eql.syntax.char", msgArgs.toArray());
        }
        return Message.of("error.target.eql.syntax", msgArgs.toArray());
    }

    private Character getNotBalancedPairChar(String str) {
        return Stream.of('`', '\'')
                .filter(pairChar -> isPairCharNotBalanced(str, pairChar))
                .findFirst()
                .orElse(null);
    }

    private boolean isPairCharNotBalanced(String str, char ch) {
        return StringUtils.countMatches(str, CharUtils.toString(ch)) % 2 != 0;
    }
}
