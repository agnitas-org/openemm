/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.preview.dto;

import com.agnitas.emm.core.mailing.web.MailingPreviewHelper;
import com.agnitas.preview.ModeType;
import com.agnitas.preview.Preview;

public class MailingPreviewSettings {

    private int format = MailingPreviewHelper.INPUT_TYPE_HTML;
    private int size = Preview.Size.DESKTOP.getValue();
    private int targetId;
    private boolean noImages;
    private String customerEmail;
    private ModeType modeType = ModeType.RECIPIENT;

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public ModeType getModeType() {
        return modeType;
    }

    public void setModeType(ModeType modeType) {
        this.modeType = modeType;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public boolean isNoImages() {
        return noImages;
    }

    public void setNoImages(boolean noImages) {
        this.noImages = noImages;
    }
}
