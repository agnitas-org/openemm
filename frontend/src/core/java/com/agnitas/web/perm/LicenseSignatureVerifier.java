/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.web.perm;

import java.security.PublicKey;
import java.util.Objects;

import com.agnitas.dao.SeasonKeyDao;
import com.agnitas.dao.LicenseDao;
import com.agnitas.util.CryptographicUtilities;
import com.agnitas.util.Version;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;


@Component
public class LicenseSignatureVerifier {

    private static final Logger logger = LogManager.getLogger(LicenseSignatureVerifier.class);

    private static final String PUBLIC_LICENSE_KEYSTRING = "-----BEGIN PUBLIC KEY-----\nMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCcdArGIy/hseE9bz53siYnClOQ\nABrRFRVs/zdN8HpweXxpFqa4SUcp9SFIjqgQ5l/FRdEE9EFc865oGZI1H2RK9Jl1\nb7NxFBwu6S4kWFpy+0Xlp+FCLMXVkBDxLB3vv96VR714n2bFh11/UanlfptqMYPQ\nq7gZCmP5Bc06ORaxrQIDAQAB\n-----END PUBLIC KEY-----";
    private static final String MASTER_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA+I3HUOyyvLNaCU/XaUjE\nuxIrTvyDj0CbhZvxBtqKOu2k48eYkDpRpM6rzifABeUDPJf81frrYSUEylsaAvbV\nlRyprz754vHTiBuYOaG/46JqECD76k9hOu/z+x6egQtTgjkyWpep5GeU730I9Jlm\nLtcw/xXMHmFUO0+CyDkhh8s4P3F7+6L2Tc5O+4uK9xfMk8J7tSyeZAoF4kAD0w+h\nsLqUq/SiZuCK6Lyfpa7rvOGvhlzbW2hlMA0PkQy7mZNxF/HaLwKiw0hNUSZIykXJ\nhG3Np/WiqGR18y3AqnvwAVXRAxMhwDayx04EHMjfvY6wh3Fsy38tyRQc3J9l1IVk\nEwIDAQAB\n-----END PUBLIC KEY-----";

    private static final String newLicenseProcessingMinVersion = "26.11";

    private final LicenseDao licenseDao;
    private final SeasonKeyDao seasonKeyDao;

    public LicenseSignatureVerifier(LicenseDao licenseDao, SeasonKeyDao seasonKeyDao) {
        this.licenseDao = licenseDao;
        this.seasonKeyDao = seasonKeyDao;
    }

    public boolean verify(byte[] licenseData, byte[] signature) {
        return CryptographicUtilities.verifyData(licenseData, getPublicKey(PUBLIC_LICENSE_KEYSTRING), signature);
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

    public void verify(String version) throws Exception {
        byte[] licenseSignature = licenseDao.getLicenseSignatureData();
        if (licenseSignature == null) {
            throw new SecurityException("License signature is missing!");
        }

        byte[] licenseData = licenseDao.getLicenseData();

        if (licenseData == null) {
            throw new SecurityException("License data is missing!");
        }
        if (!Objects.equals(version, "Unlimited")) {
            byte [] seasonKey = seasonKeyDao.getSeasonKey(version);
            byte [] seasonSignature = seasonKeyDao.getSeasonSignature(version);

            Version licenseVersion = new Version(version);
            if (licenseVersion.compareTo(new Version(newLicenseProcessingMinVersion)) >= 0){
                if (!CryptographicUtilities.verifySeasonKey(getPublicKey(MASTER_PUBLIC_KEY), seasonKey, seasonSignature)){
                    throw new SecurityException("SeasonKey is invalid");
                }
                if (!CryptographicUtilities.verifyData(licenseData, getPublicKey(new String(seasonKey, java.nio.charset.StandardCharsets.UTF_8)), licenseSignature)) {
                    throw new SecurityException("LicenseSignature is invalid");
                }
            } else {
                verify();
            }

        } else {
            verify();
        }

    }

    private PublicKey getPublicKey(String publicKey) {
        try {
            return CryptographicUtilities.getPublicKeyFromString(publicKey);
        } catch (Exception e) {
            logger.error("Can't get public key from public key string.", e);
            return null;
        }
    }
}
