/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans;

import com.agnitas.beans.IntEnum;

public enum WorkflowReactionType implements IntEnum {
    OPENED(1, "icon-eye", "opened"),
    NOT_OPENED(2, "icon-eye-slash", "not_opened"),
    CLICKED(3, "icon-paper-plane", "clicked"),
    NOT_CLICKED(4, "icon-times", "not_clicked"),
    BOUGHT(5, "icon-shopping-cart", "bought"),
    NOT_BOUGHT(6, "icon-minus", "not_bought"),
    DOWNLOAD(7, "icon-download", "download"),
    CHANGE_OF_PROFILE(8, "icon-exchange", "change_of_profile"),
    WAITING_FOR_CONFIRM(9, "icon-history", "waiting_for_confirm"),
    OPT_IN(10, "icon-sign-in", "opt_in"),
    OPT_OUT(11, "icon-sign-out", "opt_out"),
    CLICKED_LINK(12, "", "clicled_on_link"),
    OPENED_AND_CLICKED(13, "", "opened_and_clicked"),
    OPENED_OR_CLICKED(14, "", "opened_or_clicked"),
    CONFIRMED_OPT_IN(15, "", "confirmed_opt_in");

    private final int id;

    private final String iconClass;

    public String getIconClass() {
        return iconClass;
    }

    public String getName() {
        return name;
    }

    private final String name;

    WorkflowReactionType(int id, String iconClass, String name) {
        this.id = id;
        this.iconClass = iconClass;
        this.name = name;
    }

    public static WorkflowReactionType fromId(int id) {
        return IntEnum.fromId(WorkflowReactionType.class, id);
    }

    public static WorkflowReactionType fromId(int id, boolean safe) {
        return IntEnum.fromId(WorkflowReactionType.class, id, safe);
    }

    @Override
    public int getId() {
        return id;
    }
    
    public static WorkflowReactionType fromName(String name) {
        try {
            return WorkflowReactionType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
