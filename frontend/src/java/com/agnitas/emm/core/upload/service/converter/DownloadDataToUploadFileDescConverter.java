/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.upload.service.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.upload.bean.DownloadData;
import com.agnitas.emm.core.upload.service.dto.UploadFileDescription;

@Component
public class DownloadDataToUploadFileDescConverter implements Converter<DownloadData,UploadFileDescription> {
    @Override
    public UploadFileDescription convert(DownloadData source) {
        UploadFileDescription fileDescription = new UploadFileDescription();
        fileDescription.setEmail(source.getContactMail());
        fileDescription.setLastName(source.getContact_name());
        fileDescription.setFirstName(source.getContactFirstname());
        fileDescription.setPhone(source.getContactPhone());
        fileDescription.setOwner(source.getAdminID());
        fileDescription.setOwners(source.getOwners());
        fileDescription.setUploadId(source.getUploadID());
        fileDescription.setFileName(source.getFilename());
        return fileDescription;
    }
}
