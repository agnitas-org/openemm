/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

public interface AbstractTrackableLink extends BaseTrackableLink {

    void setAdminLink(boolean adminLink);

    boolean isAdminLink();

    String getAltText();

    void setAltText(String altText);

    boolean isDeleted();

    void setDeleted(boolean deleted);

    boolean isExtendByMailingExtensions();

    void setExtendByMailingExtensions(boolean value);

    void setOriginalUrl(String url);

    String getOriginalUrl();

    void setStaticValue(final boolean flag);

    boolean isStaticValue();

    void setMeasureSeparately(boolean measureSeparately);

    boolean isMeasureSeparately();

    boolean isCreateSubstituteLinkForAgnDynMulti();

    void setCreateSubstituteLinkForAgnDynMulti(final boolean createSubstituteForAgnDynMulti);
}
