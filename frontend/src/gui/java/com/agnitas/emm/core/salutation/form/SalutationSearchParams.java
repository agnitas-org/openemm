/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.salutation.form;

import com.agnitas.web.forms.FormSearchParams;

public class SalutationSearchParams extends SalutationOverviewFilter implements FormSearchParams<SalutationOverviewFilter> {

    @Override
    public void storeParams(SalutationOverviewFilter form) {
        this.setSalutationId(form.getSalutationId());
        this.setName(form.getName());
        this.setGender0(form.getGender0());
        this.setGender1(form.getGender1());
        this.setGender2(form.getGender2());
        this.setGender4(form.getGender4());
        this.setGender5(form.getGender5());
    }

    @Override
    public void restoreParams(SalutationOverviewFilter form) {
        form.setSalutationId(this.getSalutationId());
        form.setName(this.getName());
        form.setGender0(this.getGender0());
        form.setGender1(this.getGender1());
        form.setGender2(this.getGender2());
        form.setGender4(this.getGender4());
        form.setGender5(this.getGender5());
    }

}
