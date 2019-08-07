/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.pluginmanager.converter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.agnitas.util.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.agnitas.emm.core.pluginmanager.dto.PluginUploadDto;
import com.agnitas.emm.core.pluginmanager.form.PluginUploadForm;

@Component
public class PluginUploadFormToPluginUploadDtoConverter implements Converter<PluginUploadForm, PluginUploadDto> {
	
    private static final Logger logger = Logger.getLogger(PluginUploadFormToPluginUploadDtoConverter.class);
	
    private static final String TMP_PLUGIN_FILE_PREFIX = "emm-jpf-plugin-";

	
	@Override
	public PluginUploadDto convert(PluginUploadForm pluginUploadForm) {
		PluginUploadDto plugin = new PluginUploadDto();
		
		MultipartFile multipartFile = pluginUploadForm.getUploadPluginFile();
		
		try(InputStream is = multipartFile.getInputStream()) {
			File tmpFile = FileUtils.streamToTemporaryFile(is, multipartFile.getSize(), TMP_PLUGIN_FILE_PREFIX);
			plugin.setFile(tmpFile);
		} catch (IOException e) {
			logger.warn("Cannot create temporary file from uploaded plugin file");
			throw new RuntimeException("Cannot convert uploaded file", e);
		}
		
		return plugin;
	}
}
