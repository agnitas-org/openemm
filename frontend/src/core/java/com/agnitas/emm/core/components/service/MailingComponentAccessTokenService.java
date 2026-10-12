/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.components.service;

import com.agnitas.util.DataEncryptor;
import com.agnitas.util.SaltHmacSigner;
import org.springframework.stereotype.Service;

@Service
public class MailingComponentAccessTokenService {

    private final SaltHmacSigner signer;

    public MailingComponentAccessTokenService(DataEncryptor dataEncryptor) {
        this.signer = new SaltHmacSigner(dataEncryptor);
    }

    public String sign(int componentId, int companyId) {
        return signer.sign(payload(componentId, companyId));
    }

    public boolean verify(int componentId, int companyId, String token) {
        return signer.verify(payload(componentId, companyId), token);
    }

    private String payload(int componentId, int companyId) {
        return componentId + ":" + companyId;
    }

}
