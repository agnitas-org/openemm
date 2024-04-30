/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.converter;

import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.LinkProperty;
import com.agnitas.emm.core.trackablelinks.dto.ExtensionProperty;
import com.agnitas.emm.core.trackablelinks.form.TrackableLinkForm;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.util.LinkUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ComTrackableLinkToTrackableLinkFormConverter implements Converter<ComTrackableLink, TrackableLinkForm> {

    private final ExtendedConversionService conversionService;

    public ComTrackableLinkToTrackableLinkFormConverter(ExtendedConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public TrackableLinkForm convert(ComTrackableLink link) {
        TrackableLinkForm form = new TrackableLinkForm();
        form.setId(link.getId());
        form.setUrl(link.getFullUrl());
        form.setUsage(link.getUsage());
        form.setAdmin(link.isAdminLink());
        form.setAction(link.getActionID());
        form.setShortname(link.getShortname());
        form.setStaticLink(link.isStaticValue());
        form.setDeepTracking(link.getDeepTracking());
        form.setCreateSubstituteForAgnDynMulti(link.isCreateSubstituteLinkForAgnDynMulti());
        form.setExtensions(conversionService.convert(link.getProperties().stream()
                .filter(l -> LinkUtils.isExtension(l.getPropertyType()))
                .collect(Collectors.toList()), LinkProperty.class, ExtensionProperty.class));
        return form;
    }
}
