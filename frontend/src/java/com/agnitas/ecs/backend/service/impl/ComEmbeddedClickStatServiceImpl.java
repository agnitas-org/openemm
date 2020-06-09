/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.ecs.backend.service.impl;

import org.agnitas.ecs.backend.service.EmbeddedClickStatService;
import org.agnitas.ecs.backend.service.UrlMaker;
import org.agnitas.ecs.backend.service.impl.EmbeddedClickStatServiceImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.preview.PreviewImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

/**
 * Implementation of {@link EmbeddedClickStatService} interface
 */
public class ComEmbeddedClickStatServiceImpl extends EmbeddedClickStatServiceImpl {
	private ConfigService configService;

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Override
	public String getMailingContent(int mailingId, int recipientId) {
		PreviewImpl preview = (PreviewImpl) previewFactory.createPreview();
		String proxy = configService.getValue(ConfigValue.HeatmapProxy);
		String output = preview.makePreviewForHeatmap(mailingId, recipientId, proxy);
		preview.done();
		return StringUtils.defaultString(output);
	}

    @Override
    public UrlMaker getURLMaker(String program, int mailingId, String option) throws Exception {
        return new UrlMaker();
    }
}
