/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.grid.grid.beans;

import java.util.Date;
import java.util.List;

import org.agnitas.emm.core.velocity.VelocityCheck;

public interface ComGridTemplate {
    int ORIENTATION_ALIGN_LEFT = 1;
    int ORIENTATION_ALIGN_CENTER = 2;

    String REPEAT_XY = "xy";
    String REPEAT_X = "x";
    String REPEAT_Y = "y";
    String STRETCH_FULL_SIZE = "z";
    //TODO: remove after successful testing template migration
    String NO_REPEAT = "no";
    
    int getId();
    void setId(int id);

    int getCompanyId();
    void setCompanyId(@VelocityCheck int companyId);

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

    int getWorkCopyOf();
    void setWorkCopyOf(int workCopyOf);

    boolean isWorkCopy();

    int getBackgroundImageId();
    void setBackgroundImageId(int backgroundImageId);

    String getBackgroundRepeat();
    void setBackgroundRepeat(String backgroundRepeat);

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
}
