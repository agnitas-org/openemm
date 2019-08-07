/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.pluginmanager.converter;

import java.util.ArrayList;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.pluginmanager.dto.PluginDto;
import com.agnitas.emm.core.pluginmanager.form.PluginForm;

@Component
public class PluginFormToPluginDtoConverter implements Converter<PluginForm, PluginDto> {
 
	@Override
    public PluginDto convert(PluginForm form) {
        PluginDto pluginDto = new PluginDto();
        pluginDto.setId(form.getId());
        pluginDto.setName(form.getName());
        pluginDto.setDescription(form.getDescription());
        pluginDto.setVendor(form.getVendor());
        pluginDto.setVersion(form.getVersion());
        pluginDto.setActive(form.isActive());
        pluginDto.setDependingPluginIds(new ArrayList<>(form.getDependingPluginIds()));
        pluginDto.setSystem(form.isSystem());
        return pluginDto;
    }
}
