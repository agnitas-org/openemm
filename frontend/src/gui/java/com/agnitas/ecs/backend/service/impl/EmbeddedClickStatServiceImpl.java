/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.ecs.backend.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.agnitas.ecs.backend.beans.ClickStatColor;
import com.agnitas.ecs.backend.beans.ClickStatInfo;
import com.agnitas.ecs.backend.dao.EmbeddedClickStatDao;
import com.agnitas.ecs.backend.service.EmbeddedClickStatService;
import com.agnitas.ecs.service.EcsService;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.ecs.web.HeatmapStatInfo;
import com.agnitas.preview.PreviewFactory;
import com.agnitas.preview.PreviewImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class EmbeddedClickStatServiceImpl implements EmbeddedClickStatService {

    private final EmbeddedClickStatDao ecsDao;
    private final EcsService ecsService;
    private final PreviewFactory previewFactory;
    private final ConfigService configService;

    public EmbeddedClickStatServiceImpl(
            EmbeddedClickStatDao ecsDao,
            EcsService ecsService,
            PreviewFactory previewFactory,
            ConfigService configService
    ) {
        this.ecsDao = ecsDao;
        this.ecsService = ecsService;
        this.previewFactory = previewFactory;
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
    public HeatmapStatInfo getStatsInfo(int viewMode, int mailingId, int companyId, int deviceType) {
        HeatmapStatInfo info = new HeatmapStatInfo();
        // get click statistics and color values for stat-labels
        List<ClickStatColor> rangeColors = ecsService.getClickStatColors(companyId);
        ClickStatInfo clickStatInfo = ecsDao.getClickStatInfo(companyId, mailingId, viewMode, deviceType);
        // create hidden elements containing clicks stats - to be used by javascript to
        // create clicks stat labels above the links
        if (clickStatInfo != null && MapUtils.isNotEmpty(clickStatInfo.getClicks())) {
            List<HeatmapStatInfo.Entry> statEntries = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry : clickStatInfo.getClicks().entrySet()) {
                int clicks = entry.getValue();
                double percent = clickStatInfo.getPercentClicks().get(entry.getKey());
                String color = getColorForPercent(percent, rangeColors);
                statEntries.add(getClickStatInfo(Integer.toString(entry.getKey()), clicks, clickStatInfo.getClicksOverall().get(entry.getKey()), percent, color));
            }
	    for (Map.Entry<String, Integer> entry : clickStatInfo.getClicksPerPosition().entrySet()) {
		String key = entry.getKey ();
		int clicks = entry.getValue ();
		int overall = clickStatInfo.getClicksOverall().get(extractUrlId(key));
		double percent = clickStatInfo.getPercentClicksPerPosition().get(key);
		String color = getColorForPercent(percent, rangeColors);
			    
		statEntries.add(getClickStatInfo(key, clicks, overall, percent, color));
	    }
            info.setStatEntries(statEntries);
        }

        info.setNullColor("#" + getColorForPercent(0, rangeColors));
        return info;
    }

	private int extractUrlId(String urlIdKey) {
		try {
			int	n = urlIdKey.indexOf ('-');
		
			return Integer.parseInt (n == -1 ? urlIdKey : urlIdKey.substring (0, n));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

    private HeatmapStatInfo.Entry getClickStatInfo(String urlId, int clicks, int clicksOverall, double percent, String color) {
        // format percent value to be in a form XX.XX
        BigDecimal bigDecimal = BigDecimal.valueOf(percent);
        double percentFormatted = bigDecimal.setScale(2, RoundingMode.HALF_UP).doubleValue();
        // in links URLs the coded URL id is used that's why we need to code it
        // here also to be able to get statistics information from hidden input
        // for the link url
        return new HeatmapStatInfo.Entry(urlId, clicks + " / " + clicksOverall + " (" + percentFormatted + "%)", color);
    }

    /**
     * Method finds color for percentage value
     *
     * @param percent     percentage value
     * @param rangeColors collection of color values for differnet percentages
     * @return color in a HEX string if rangeColors has color for this percent,
     * DEFAULT_STAT_LINK_COLOR in other case
     */
    private String getColorForPercent(double percent, List<ClickStatColor> rangeColors) {
        if (CollectionUtils.isEmpty(rangeColors)) {
            return DEFAULT_STAT_LABEL_COLOR;
        }

        String color = DEFAULT_STAT_LABEL_COLOR;
        for (ClickStatColor rangeColor : rangeColors) {
            if (percent >= rangeColor.rangeStart() && percent <= rangeColor.rangeEnd()) {
                color = rangeColor.color();
                if (rangeColor.rangeStart() == percent) {
                    break;
                }
            }
        }
        return color;
    }

}
