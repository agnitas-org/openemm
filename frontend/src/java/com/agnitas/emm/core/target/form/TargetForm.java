/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.form;

import org.agnitas.web.forms.PaginationForm;

public class TargetForm extends PaginationForm {

    private boolean showWorldDelivery;
    private boolean showTestAndAdminDelivery;
    private boolean searchNameChecked = true;
    private boolean searchDescriptionChecked = true;
    private String searchQueryText;

    public boolean isShowWorldDelivery() {
        return showWorldDelivery || !showTestAndAdminDelivery;
    }

    public void setShowWorldDelivery(boolean showWorldDelivery) {
        this.showWorldDelivery = showWorldDelivery;
    }

    public boolean isShowTestAndAdminDelivery() {
        return showTestAndAdminDelivery;
    }

    public void setShowTestAndAdminDelivery(boolean showTestAdminDelivery) {
        this.showTestAndAdminDelivery = showTestAdminDelivery;
    }

    public String getSearchQueryText() {
        return searchQueryText;
    }

    public void setSearchQueryText(String searchQueryText) {
        this.searchQueryText = searchQueryText;
    }

    public boolean isSearchNameChecked() {
        return searchNameChecked;
    }

    public void setSearchNameChecked(boolean searchNameChecked) {
        this.searchNameChecked = searchNameChecked;
    }

    public boolean isSearchDescriptionChecked() {
        return searchDescriptionChecked;
    }

    public void setSearchDescriptionChecked(boolean searchDescriptionChecked) {
        this.searchDescriptionChecked = searchDescriptionChecked;
    }

}
