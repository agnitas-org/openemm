/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipientsreport.dto;

import org.springframework.http.MediaType;

import com.agnitas.emm.core.recipientsreport.bean.RecipientsReport;

public class DownloadRecipientReport {
    
    private String filename;
    
    private byte[] content;
    
    private MediaType mediaType;
    
    private boolean suplemental;
    
    private RecipientsReport.EntityType type;
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public byte[] getContent() {
        return content;
    }
    
    public void setContent(byte[] content) {
        this.content = content;
    }
    
    public MediaType getMediaType() {
        return mediaType;
    }
    
    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }
    
    public boolean isSuplemental() {
        return suplemental;
    }
    
    public void setSuplemental(boolean suplemental) {
        this.suplemental = suplemental;
    }

    public RecipientsReport.EntityType getType() {
        return type;
    }

    public void setType(RecipientsReport.EntityType type) {
        this.type = type;
    }
}
