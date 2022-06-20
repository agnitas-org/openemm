/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

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

import org.agnitas.ecs.backend.beans.ClickStatColor;
import org.agnitas.ecs.backend.beans.ClickStatInfo;
import org.agnitas.ecs.backend.dao.EmbeddedClickStatDao;
import org.agnitas.ecs.backend.service.EmbeddedClickStatService;
import org.agnitas.ecs.backend.service.UrlMaker;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.preview.PreviewFactory;
import org.agnitas.preview.PreviewImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.emm.ecs.web.HeatmapStatInfo;

/**
 * Implementation of {@link EmbeddedClickStatService} interface
 */
public class ComEmbeddedClickStatServiceImpl implements EmbeddedClickStatService {

	/** The logger. */
    private static final transient Logger logger = LogManager.getLogger(ComEmbeddedClickStatServiceImpl.class);

    private EmbeddedClickStatDao ecsDao;
    private PreviewFactory previewFactory;
    private ConfigService configService;

    @Override
    public String getMailingContent(int mailingId, int recipientId) {
        PreviewImpl preview = (PreviewImpl) previewFactory.createPreview();
        String proxy = configService.getValue(ConfigValue.HeatmapProxy);
        String output = preview.makePreviewForHeatmap(mailingId, recipientId, proxy);
        preview.done();
        return StringUtils.defaultString(output);
    }

    @Override
    public HeatmapStatInfo getStatsInfo(int viewMode, int mailingId, int companyId, int deviceType) throws Exception {
        try {
            HeatmapStatInfo info = new HeatmapStatInfo();
            // get click statistics and color values for stat-labels
            List<ClickStatColor> rangeColors = ecsDao.getClickStatColors(companyId);
            ClickStatInfo clickStatInfo = ecsDao.getClickStatInfo(companyId, mailingId, viewMode, deviceType);
            // create hidden elements containing clicks stats - to be used by javascript to
            // create clicks stat labels above the links
            if (clickStatInfo != null && MapUtils.isNotEmpty(clickStatInfo.getClicks())) {
                UrlMaker urlMaker;
                try {
                    urlMaker = new UrlMaker();
                } catch (Exception e) {
                    logger.error("Error during UID creation for heatmap ", e);
                    return null;
                }
                List<HeatmapStatInfo.Entry> statEntries = new ArrayList<>();
                for (Map.Entry<Integer, Integer> entry : clickStatInfo.getClicks().entrySet()) {
                    int clicks = entry.getValue();
                    double percent = clickStatInfo.getPercentClicks().get(entry.getKey());
                    String color = getColorForPercent(percent, rangeColors);
                    statEntries.add(getClickStatInfo(entry.getKey(), clicks, clickStatInfo.getClicksOverall().get(entry.getKey()), percent, color, urlMaker));
                }
                info.setStatEntries(statEntries);
            }

            info.setNullColor("#" + getColorForPercent(0, rangeColors));
            return info;
        } catch (Exception e) {
            logger.error("Cannot addStatsInfo: " + e.getMessage(), e);
            throw e;
        }
    }

    private HeatmapStatInfo.Entry getClickStatInfo(int urlId, int clicks, int clicksOverall, double percent, String color, UrlMaker urlMaker) {
        // format percent value to be in a form XX.XX
        BigDecimal bigDecimal = BigDecimal.valueOf(percent);
        double percentFormatted = bigDecimal.setScale(2, RoundingMode.HALF_UP).doubleValue();
        // in links URLs the coded URL id is used that's why we need to code it
        // here also to be able to get statistics information from hidden input
        // for the link url
        String codedUrlId = null;
        try {
            if (urlMaker != null) {
                urlMaker.setURLID(urlId);
                codedUrlId = urlMaker.makeUID();
            }
        } catch (Exception e) {
            logger.error("Error during UID creation for heatmap ", e);
        }

        return new HeatmapStatInfo.Entry(codedUrlId, clicks + " / " + clicksOverall + " (" + percentFormatted + "%)", color);
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
            if (percent >= rangeColor.getRangeStart() && percent <= rangeColor.getRangeEnd()) {
                color = rangeColor.getColor();
                if (rangeColor.getRangeStart() == percent) {
                    break;
                }
            }
        }
        return color;
    }

    public void setEmbeddedClickStatDao(EmbeddedClickStatDao ecsDao) {
        this.ecsDao = ecsDao;
    }

    public void setPreviewFactory(PreviewFactory previewFactory) {
        this.previewFactory = previewFactory;
    }

    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

}
