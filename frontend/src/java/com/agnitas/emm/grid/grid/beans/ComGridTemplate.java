/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.grid.grid.beans;

import com.agnitas.beans.TemplateTrackableLink;


import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ComGridTemplate {
    int ORIENTATION_ALIGN_LEFT = 1;
    int ORIENTATION_ALIGN_CENTER = 2;

    int getId();
    void setId(int id);

    int getCompanyId();
    void setCompanyId(int companyId);

    String getName();
    void setName(String name);

    String getCss();
    void setCss(String css);

    String getHiddenCss();
    void setHiddenCss(String css);

    int getGridId();
    void setGridId(int gridId);

    int getMailingId();
    void setMailingId(int mailingId);

    Date getCreationDate();
    void setCreationDate(Date creationDate);

    int getParentTemplateId();
    void setParentTemplateId(int parentTemplateId);

    String getChildTemplateName();
    void setChildTemplateName(String childTemplateName);

    int getOrientation();
    void setOrientation(int orientation);

	List<Boolean> getRowGuttersList();

	String getRowGutters();
	void setRowGutters(String rowGutters);

    boolean isReleased();
    void setReleased(boolean released);

    boolean isCopyOfReleased();
    void setCopyOfReleased(boolean copyOfReleased);

    Date getChangeDate();
    void setChangeDate(Date updateDate);

    String getOuterHtml();
    void setOuterHtml(String outerHtml);

    Map<String, TemplateTrackableLink> getTrackableLinks();
    void setTrackableLinks(Map<String, TemplateTrackableLink> trackableLinks);

    int getOpenActionId();
    void setOpenActionId(int id);

    int getClickActionId();
    void setClickActionId(int id);
}
