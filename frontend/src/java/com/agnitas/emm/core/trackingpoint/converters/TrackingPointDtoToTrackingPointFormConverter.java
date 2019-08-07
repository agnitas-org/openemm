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
public class TrackingPointDtoToTrackingPointFormConverter implements Converter<TrackingPointDto, TrackingPointForm> {

    @Override
    public TrackingPointForm convert(TrackingPointDto source) {
        TrackingPointForm trackingPointForm = new TrackingPointForm();

        trackingPointForm.setId(source.getId());
        trackingPointForm.setAction(source.getActionId());
        trackingPointForm.setCurrency(source.getCurrency());
        trackingPointForm.setDescription(source.getDescription());
        trackingPointForm.setFormat(source.getFormat());
        trackingPointForm.setShortName(source.getShortName());
        trackingPointForm.setPageTag(source.getPageTag());
        trackingPointForm.setType(source.getType());

        return trackingPointForm;
    }
}
