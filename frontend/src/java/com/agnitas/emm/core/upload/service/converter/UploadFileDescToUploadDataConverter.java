/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.upload.service.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.upload.bean.UploadData;
import com.agnitas.emm.core.upload.service.dto.UploadFileDescription;

@Component
public class UploadFileDescToUploadDataConverter implements Converter<UploadFileDescription,UploadData> {

    @Override
    public UploadData convert(UploadFileDescription source) {
        UploadData data = new UploadData();
        data.setAdminID(source.getOwner());
        data.setContactFirstname(source.getFirstName());
        data.setContactName(source.getLastName());
        data.setContactPhone(source.getPhone());
        data.setDescription(source.getDescription());
        data.setContactMail(source.getEmail());
        data.setSendtoMail(source.getNotifyEmail());
        data.setLocale(source.getLocale());
        data.setUploadID(source.getUploadId());
        data.setCompanyID(source.getCompanyId());
        data.setFilename(source.getFileName());
        data.setOwners(source.getOwners());
        data.setFromAdminID(source.getFromUser());

        return data;
    }
}
