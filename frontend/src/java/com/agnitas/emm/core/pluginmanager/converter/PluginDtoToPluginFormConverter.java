/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.pluginmanager.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.pluginmanager.dto.PluginDto;
import com.agnitas.emm.core.pluginmanager.form.PluginForm;

@Component
public class PluginDtoToPluginFormConverter implements Converter<PluginDto, PluginForm> {
	
    @Override
    public PluginForm convert(PluginDto pluginDto) {
        PluginForm form = new PluginForm();
        form.setId(pluginDto.getId());
        form.setName(pluginDto.getName());
        form.setDescription(pluginDto.getDescription());
        form.setVendor(pluginDto.getVendor());
        form.setVersion(pluginDto.getVersion());
        form.setDependingPluginIds(pluginDto.getDependingPluginIds());
        form.setSystem(pluginDto.isSystem());
        form.setActive(pluginDto.isActive());
        return form;
    }
}
