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
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserFormToUserFormDtoConverter implements Converter<UserForm, UserFormDto> {
	
	@Override
	public UserFormDto convert(UserForm userForm) {
		UserFormDto dto = new UserFormDto();
		dto.setId(userForm.getId());
		dto.setName(userForm.getFormName());
		dto.setDescription(userForm.getDescription());
		
		dto.setActive(userForm.isActive());
		
		dto.setCreationDate(userForm.getCreationDate());
		dto.setChangeDate(userForm.getChangeDate());
		
		ResultSettings successForm = dto.getSuccessSettings();
		successForm.setTemplate(userForm.getSuccessTemplate());
		successForm.setUrl(userForm.getSuccessUrl());
		successForm.setUseUrl(userForm.isSuccessUseUrl());
		successForm.setStartActionId(userForm.getStartActionID());
		successForm.setFinalActionId(userForm.getEndActionID());
		successForm.setFormBuilderJson(userForm.getSuccessFormBuilderJson());
		
		ResultSettings errorForm = dto.getErrorSettings();
		errorForm.setTemplate(userForm.getErrorTemplate());
		errorForm.setUrl(userForm.getErrorUrl());
		errorForm.setUseUrl(userForm.isErrorUseUrl());
		errorForm.setFormBuilderJson(userForm.getErrorFormBuilderJson());
		
		return dto;
	}
}
