/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.perm;

import java.security.PublicKey;

import com.agnitas.dao.LicenseDao;
import com.agnitas.util.CryptographicUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class LicenseSignatureVerifier {

    private static final Logger logger = LogManager.getLogger(LicenseSignatureVerifier.class);

    private static final String PUBLIC_LICENSE_KEYSTRING = "-----BEGIN PUBLIC KEY-----\nMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCcdArGIy/hseE9bz53siYnClOQ\nABrRFRVs/zdN8HpweXxpFqa4SUcp9SFIjqgQ5l/FRdEE9EFc865oGZI1H2RK9Jl1\nb7NxFBwu6S4kWFpy+0Xlp+FCLMXVkBDxLB3vv96VR714n2bFh11/UanlfptqMYPQ\nq7gZCmP5Bc06ORaxrQIDAQAB\n-----END PUBLIC KEY-----";

    private final LicenseDao licenseDao;

    public LicenseSignatureVerifier(LicenseDao licenseDao) {
        this.licenseDao = licenseDao;
    }

    public boolean verify(byte[] licenseData, byte[] signature) {
        return CryptographicUtilities.verifyData(licenseData, getPublicKey(), signature);
    }

    public void verify() {
        byte[] licenseSignature = licenseDao.getLicenseSignatureData();
        if (licenseSignature == null) {
            throw new SecurityException("License signature is missing!");
        }

        byte[] licenseData = licenseDao.getLicenseData();
        if (licenseData == null) {
            throw new SecurityException("License data is missing!");
        }

        if (!verify(licenseData, licenseSignature)) {
            throw new SecurityException("LicenseSignature is invalid");
        }
    }

    private PublicKey getPublicKey() {
        try {
            return CryptographicUtilities.getPublicKeyFromString(PUBLIC_LICENSE_KEYSTRING);
        } catch (Exception e) {
            logger.error("Can't get public key from public key string.", e);
            return null;
        }
    }

}
