/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailingcontent.form;

public class FrameContentForm {
    
    private String textTemplate;
    private String htmlTemplate;
    private String smsTemplate;
    private boolean useDynamicTemplate;

    public String getTextTemplate() {
        return textTemplate;
    }

    public void setTextTemplate(String textTemplate) {
        this.textTemplate = textTemplate;
    }

    public String getHtmlTemplate() {
        return htmlTemplate;
    }

    public void setHtmlTemplate(String htmlTemplate) {
        this.htmlTemplate = htmlTemplate;
    }

    public String getSmsTemplate() {
        return smsTemplate;
    }

    public void setSmsTemplate(String smsTemplate) {
        this.smsTemplate = smsTemplate;
    }

    public boolean isUseDynamicTemplate() {
        return useDynamicTemplate;
    }

    public void setUseDynamicTemplate(boolean useDynamicTemplate) {
        this.useDynamicTemplate = useDynamicTemplate;
    }
}
