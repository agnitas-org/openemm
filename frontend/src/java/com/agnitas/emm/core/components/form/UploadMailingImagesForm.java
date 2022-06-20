/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.form;

import java.util.Map;

import org.agnitas.web.forms.PaginationForm;

import com.agnitas.emm.core.components.dto.UploadMailingImageDto;

public class UploadMailingImagesForm extends PaginationForm {

    private Map<Integer, UploadMailingImageDto> images;

    public Map<Integer, UploadMailingImageDto> getImages() {
        return images;
    }

    public void setImages(Map<Integer, UploadMailingImageDto> images) {
        this.images = images;
    }
}
