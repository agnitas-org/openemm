/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackingpoint.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.trackingpoint.beans.TrackingPointDto;
import com.agnitas.emm.core.trackingpoint.forms.TrackingPointForm;

@Component
public class TrackingPointFormToTrackingPointDtoConverter implements Converter<TrackingPointForm, TrackingPointDto> {

    @Override
    public TrackingPointDto convert(TrackingPointForm source) {
        TrackingPointDto trackingPoint = new TrackingPointDto();

        trackingPoint.setId(source.getId());
        trackingPoint.setActionId(source.getAction());
        trackingPoint.setPageTag(source.getPageTag());
        trackingPoint.setType(source.getType());
        trackingPoint.setCurrency(source.getCurrency());
        trackingPoint.setDescription(source.getDescription());
        trackingPoint.setFormat(source.getFormat());
        trackingPoint.setShortName(source.getShortName());

        return trackingPoint;
    }
}
