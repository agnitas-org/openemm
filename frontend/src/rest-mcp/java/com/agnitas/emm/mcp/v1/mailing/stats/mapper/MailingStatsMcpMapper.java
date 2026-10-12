/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.stats.mapper;

import com.agnitas.emm.core.birtstatistics.mailing.MailingSummaryStatsService;
import com.agnitas.emm.mcp.v1.mailing.stats.dto.MailingStatsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MailingStatsMcpMapper {

    @Mapping(source = "openers.measured", target = "openersMeasured")
    @Mapping(source = "openers.invisible", target = "clickersWithoutOpening")
    @Mapping(source = "openingsGross", target = "openings")
    @Mapping(source = "anonymousOpenings", target = "openingsAnonymous")
    @Mapping(source = "openers.total", target = "openersExtrapolated")
    @Mapping(source = "openers.byDevice", target = "openersByDeviceClass")
    @Mapping(source = "clickers.total", target = "clickers")
    @Mapping(source = "anonymousClicks", target = "clicksAnonymous")
    @Mapping(source = "clickers.byDevice", target = "clickersByDeviceClass")
    @Mapping(source = "optOuts", target = "unsubscribers")
    MailingStatsResponse toResponse(MailingSummaryStatsService.Stats stats);

    @Mapping(source = "pc", target = "desktop")
    @Mapping(source = "multi", target = "multiple")
    MailingStatsResponse.ByDeviceClass toByDeviceClass(MailingSummaryStatsService.ByDevice byDevice);

}
