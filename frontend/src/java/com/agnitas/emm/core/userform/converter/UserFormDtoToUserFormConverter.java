/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.converter;

import com.agnitas.emm.core.userform.dto.ResultSettings;
import com.agnitas.emm.core.userform.dto.UserFormDto;
import com.agnitas.userform.bean.UserForm;
import com.agnitas.userform.bean.impl.UserFormImpl;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserFormDtoToUserFormConverter implements Converter<UserFormDto, UserForm> {
	
	@Override
	public UserForm convert(UserFormDto dto) {
		UserForm form = new UserFormImpl();
		form.setId(dto.getId());
		form.setFormName(dto.getName());
		form.setDescription(dto.getDescription());
		
		form.setActive(dto.isActive());

		ResultSettings successSettings = dto.getSuccessSettings();
		ResultSettings errorSettings = dto.getErrorSettings();

		form.setEndActionID(successSettings.getFinalActionId());
		form.setStartActionID(successSettings.getStartActionId());
		form.setSuccessTemplate(successSettings.getTemplate());
		form.setSuccessUseUrl(successSettings.isUseUrl());
		form.setSuccessUrl(successSettings.getUrl());

		form.setErrorTemplate(errorSettings.getTemplate());
		form.setErrorUrl(errorSettings.getUrl());
		form.setErrorUseUrl(errorSettings.isUseUrl());

		form.setSuccessFormBuilderJson(successSettings.getFormBuilderJson());
		form.setErrorFormBuilderJson(errorSettings.getFormBuilderJson());

		return form;
	}
}
