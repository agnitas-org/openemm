/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.ecs.backend.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map.Entry;

import org.agnitas.ecs.backend.beans.ClickStatColor;
import org.agnitas.ecs.backend.beans.ClickStatInfo;
import org.agnitas.ecs.backend.dao.EmbeddedClickStatDao;
import org.agnitas.ecs.backend.service.EmbeddedClickStatService;
import org.agnitas.ecs.backend.service.UrlMaker;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.preview.Preview;
import org.agnitas.preview.PreviewFactory;
import org.apache.log4j.Logger;

/**
 * Implementation of {@link EmbeddedClickStatService} interface
 */
public abstract class EmbeddedClickStatServiceImpl implements EmbeddedClickStatService {

    private static final transient Logger logger = Logger.getLogger(EmbeddedClickStatServiceImpl.class);

	private EmbeddedClickStatDao ecsDao;
	protected PreviewFactory previewFactory;

	@Override
	public String getMailingContent(int mailingId, int recipientId) {
		Preview preview =  previewFactory.createPreview();
		String output = preview.makePreviewForHeatmap(mailingId, recipientId);
		String content = output == null ? "" : output;
		preview.done();
		return content;
	}

	@Override
	public String addStatsInfo(String content, int mode, int mailingId, @VelocityCheck int companyId) throws Exception {
		try {
			String finalHtml = content;
			// get click statistics and color values for stat-labels
			List<ClickStatColor> rangeColors = ecsDao.getClickStatColors(companyId);
			ClickStatInfo clickStatInfo = ecsDao.getClickStatInfo(companyId, mailingId, mode);
			// create hidden elements containing clicks stats - to be used by javascript to
			// create clicks stat labels above the links
			if(clickStatInfo != null) {
			    UrlMaker urlMaker = null;
			    try {
			        urlMaker = getURLMaker("mailgun", mailingId, "meta:xml/gz");
			    } catch (Exception e) {
			        logger.error("Error during UID creation for heatmap ", e);
			        return null;
			    }
			    for (Entry<Integer, Integer> entry : clickStatInfo.getClicks().entrySet()) {
			        int clicks = entry.getValue();
			        double percent = clickStatInfo.getPercentClicks().get(entry.getKey());
			        String color = getColorForPercent(percent, rangeColors);
			        String statInfo = createClickStatInfo(entry.getKey(), clicks, clickStatInfo.getClicksOverall().get(entry.getKey()), percent, color, urlMaker);
			        finalHtml = finalHtml + statInfo;
			    }
			}
			finalHtml = finalHtml + createNullColorInfoElement(rangeColors);
			return finalHtml;
		} catch (Exception e) {
			logger.error("Cannot addStatsInfo: " + e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * Method generates hidden field that carries link click statistics information.
	 * Such hidden fields are used for creating click-stat-labels by javascript
	 * of ecs-page (statLabelAdjuster.js)
	 * ("id" carries urld id; "value" carries click number + percent value;
	 * "name" carries color value)
	 *
	 * @param urlId   link url id
	 * @param clicks  number of clicks
	 * @param percent percent value of clicks
	 * @param color   color that will be used for click-stat-label
	 * @return hidden field in a form of String
	 */
	private String createClickStatInfo(int urlId, int clicks, int clicksOverall, double percent, String color, UrlMaker urlMaker) {
		// format percent value to be in a form XX.XX
		BigDecimal bigDecimal = new BigDecimal(percent);
		double percentFormatted = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
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
        // create hidden input element that carries information about link clicking statistics
		return "<input id='info-" + codedUrlId + "' type='hidden' value='" + clicks + " / " + clicksOverall + " (" + percentFormatted + "%)' name='#" + color + "'/>";
	}

	/**
	 * Method constructs hidden input element that carries information
	 * about color for 0% percentage values (used by javascript to set color
	 * for link-labels that have 0 clicks done)
	 *
	 * @param clickStatColors color values for percentage values
	 * @return String containing HTML of hidden input
	 */
	private String createNullColorInfoElement(List<ClickStatColor> clickStatColors) {
		String nullColor = getColorForPercent(0, clickStatColors);
		return "<input id='info-null-color' type='hidden' value='#" + nullColor + "'>";
	}

	/**
	 * Method finds color for percentage value
	 *
	 * @param percent	 percentage value
	 * @param rangeColors collection of color values for differnet percentages
	 * @return color in a HEX string if rangeColors has color for this percent,
	 *         DEFAULT_STAT_LINK_COLOR in other case
	 */
	private String getColorForPercent(double percent, List<ClickStatColor> rangeColors) {
		String color = DEFAULT_STAT_LABEL_COLOR;
		if(rangeColors == null || rangeColors.isEmpty()) {
			return DEFAULT_STAT_LABEL_COLOR;
		}
		for(ClickStatColor rangeColor : rangeColors) {
			if(percent >= rangeColor.getRangeStart() && percent <= rangeColor.getRangeEnd()) {
				color = rangeColor.getColor();
				if(rangeColor.getRangeStart() == percent) {
					break;
				}
			}
		}
		return color;
	}

	@Override
	public void setEmbeddedClickStatDao(EmbeddedClickStatDao ecsDao) {
		this.ecsDao = ecsDao;
	}

    public void setPreviewFactory(PreviewFactory previewFactory) {
		this.previewFactory = previewFactory;
	}
}
