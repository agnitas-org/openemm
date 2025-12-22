/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.converter;

import com.agnitas.emm.core.userform.dto.UserFormDto;
import com.agnitas.emm.core.userform.form.UserFormForm;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserFormDtoToUserFormFormConverter implements Converter<UserFormDto, UserFormForm> {
	
	@Override
	public UserFormForm convert(UserFormDto dto) {
		UserFormForm form = new UserFormForm();
		form.setFormId(dto.getId());
		form.setFormName(dto.getName());
		form.setDescription(dto.getDescription());
		form.setSuccessSettings(dto.getSuccessSettings());
		form.setErrorSettings(dto.getErrorSettings());
		return form;
	}
}
