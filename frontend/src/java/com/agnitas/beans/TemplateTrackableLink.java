/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import org.agnitas.beans.BaseTrackableLink;

public interface TemplateTrackableLink extends BaseTrackableLink {

    int getTemplateId();

    void setTemplateId(int templateId);

    String getOriginalUrl();

    void setOriginalUrl(String url);

    String getAltText();

    void setAltText(String altText);

    boolean isAdminLink();

    void setAdminLink(boolean adminLink);

    boolean isDeleted();

    void setDeleted(boolean deleted);

    boolean isExtendByMailingExtensions();

    void setExtendByMailingExtensions(boolean extendByMailingExtensions);

    boolean isMeasureSeparately();

    void setMeasureSeparately(boolean measureSeparately);

    boolean isCreateSubstituteLinkForAgnDynMulti();

    void setCreateSubstituteLinkForAgnDynMulti(boolean createSubstituteForAgnDynMulti);

    boolean isStaticValue();

    void setStaticValue(boolean staticValue);
}
