/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.converter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.recipient.dto.RecipientBindingDto;
import com.agnitas.emm.core.recipient.dto.RecipientBindingsDto;
import com.agnitas.emm.core.recipient.forms.RecipientBindingListForm;

@Component
public class RecipientBindingListFormToRecipientBindingsListDto implements Converter<RecipientBindingListForm, RecipientBindingsDto> {
    @Override
    public RecipientBindingsDto convert(RecipientBindingListForm form) {
        RecipientBindingsDto bindingsDto = new RecipientBindingsDto();

        List<RecipientBindingDto> bindings = new ArrayList<>();

        form.getMailinglistBindings()
                .forEach((mlId, bindingsByMediaType) ->
                        bindingsByMediaType.getMediatypeBindings().forEach((mediaType, dto) -> {
                            dto.setMailinglistId(mlId);
                            bindings.add(dto);
                        }));

        bindingsDto.setBindings(bindings);
        return bindingsDto;
    }
}
