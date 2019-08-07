/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.uid.parser.impl;

import java.util.Objects;

import org.agnitas.beans.Company;
import org.agnitas.emm.core.commons.daocache.CompanyDaoCache;
import org.agnitas.emm.core.commons.uid.builder.ExtensibleUIDStringBuilder;
import org.agnitas.emm.core.commons.uid.builder.impl.exception.RequiredInformationMissingException;
import org.agnitas.emm.core.commons.uid.builder.impl.exception.UIDStringBuilderException;
import org.agnitas.emm.core.commons.uid.parser.ExtensibleUIDParser;
import org.agnitas.emm.core.commons.uid.parser.exception.DeprecatedUIDVersionException;
import org.agnitas.emm.core.commons.uid.parser.exception.InvalidUIDException;
import org.agnitas.emm.core.commons.uid.parser.exception.SignatureNotMatchParseException;
import org.agnitas.emm.core.commons.uid.parser.exception.UIDParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.commons.uid.ExtensibleUidVersion;

public abstract class BaseExtensibleUIDParser implements ExtensibleUIDParser {

    protected ExtensibleUidVersion handledUidVersion;
    protected CompanyDaoCache companyDaoCache;
    protected ExtensibleUIDStringBuilder stringBuilder;

    private int minPartsCount;
    private int maxPartsCount;
    private int signatureLength;

    private Logger logger;

    protected BaseExtensibleUIDParser(Logger logger, int minPartsCount, int maxPartsCount, int signatureLength) {
        this.logger = logger;
        this.minPartsCount = minPartsCount;
        this.maxPartsCount = maxPartsCount;
        this.signatureLength = signatureLength;
    }

    public ComExtensibleUID parse(final String uidString) throws UIDParseException, InvalidUIDException, DeprecatedUIDVersionException {
        if (StringUtils.isEmpty(uidString)) {
            return null;
        }

        // Check the base format (number of parts, length of signature, ...)
        if (!isSupportedUidFormat(uidString)) {
            throw new InvalidUIDException("Invalid number of uid parts or invalid length of signature.", uidString);
        }

        // Split the UID into parts
        final String[] parts = splitUIDString(uidString);
        final ComExtensibleUID uid = parse(uidString, parts);

        checkSignature(uidString, uid, parts);
        checkUIDVersion(uidString, uid);

        return uid;
    }

    protected abstract ComExtensibleUID parse(final String uidString, final String[] parts) throws UIDParseException;

    /**
     * Splits the UID string.
     *
     * @param str UID string
     * @return String array containing the elements of the UID string
     */
    protected String[] splitUIDString(final String str) {
        return str.split("\\.");
    }

    /**
     * May shift one position of the uid array to the right if it necessary.
     * It may be necessary if UID doesn't contain prefix.
     *
     * @param parts - current uid parts.
     * @return shifted uid parts
     */
    protected String[] correctUIDParts(final String[] parts) {
        int start = 0;
        int size = parts.length;

        if (parts.length < maxPartsCount) {
            start = 1;
            size++;
        }

        String[] result = new String[size];
        System.arraycopy(parts, 0, result, start, parts.length);
        return result;
    }

    private void checkUIDVersion(String uidString, ComExtensibleUID uid) throws DeprecatedUIDVersionException {
        final Company company = this.companyDaoCache.getItem(uid.getCompanyID());
        Number minimumSupportedVersion = company.getMinimumSupportedUIDVersion();
        if (handledUidVersion.isOlderThan(minimumSupportedVersion)) {
            String descriptionTemplate = "Version validation Error. Deprecated UID version. minimumSupportedVersion: %d, actualVersion: %s, encodedUid: %s";
            String message = String.format(descriptionTemplate, minimumSupportedVersion, handledUidVersion.getVersionCode(), uidString);
            logger.error(message);
            throw new DeprecatedUIDVersionException(message, uid);
        }
    }

    private void checkSignature(String uidString, ComExtensibleUID uid, String[] parts) throws InvalidUIDException, UIDParseException {
        try {
            String expectedSignature = getExpectedSignature(uid);
            String actualSignature = getActualSignature(parts);
            if (!actualSignature.equals(expectedSignature)) {
                String descriptionTemplate = "Signature validation error. Signature doesn't match. expectedSignature: %s, actualSignature: %s, version: %d, uid: %s";
                String message = String.format(descriptionTemplate, expectedSignature, actualSignature, handledUidVersion.getVersionCode(), uidString);
                logger.error(message);
                throw new SignatureNotMatchParseException(message);
            }
        } catch (UIDStringBuilderException | RequiredInformationMissingException e) {
            String messageTemplate = "Signature validation error. Cannot create string from decoded uid. uid: %s, decodedUid: %s";
            String errorMessage = String.format(messageTemplate, uidString, uid);
            logger.error(String.format(errorMessage, uidString, uid), e);
            throw new UIDParseException(errorMessage, e);
        }
    }

    /**
     * Determine the actual signature for the given parts of uid string.
     *
     * @param parts UID.
     * @return actual signature.
     */
    protected abstract String getActualSignature(String[] parts);


    /**
     * Determine the expected signature for the given ExtensibleUID.
     *
     * @param uid UID
     * @return expected signature
     * @throws UIDStringBuilderException           on errors computing the expected signature
     * @throws RequiredInformationMissingException in required information is not set in UID
     */
    protected abstract String getExpectedSignature(final ComExtensibleUID uid) throws UIDStringBuilderException, RequiredInformationMissingException;

    @Override
    public boolean isSupportedUidFormat(String uidString) {
        return StringUtils.isNotBlank(uidString) && isValidBaseFormat(splitUIDString(uidString));
    }

    protected boolean isValidBaseFormat(final String[] parts) {
        return (parts.length == minPartsCount || parts.length == maxPartsCount) && parts[parts.length - 1].length() == signatureLength;
    }

    public int getHandledUidVersion() {
        return handledUidVersion.getVersionCode();
    }

    /**
     * Set CompanyDaoCache.
     *
     * @param companyDaoCache CompanyDaoCache
     */
    @Required
    public void setCompanyDaoCache(final CompanyDaoCache companyDaoCache) {
        this.companyDaoCache = Objects.requireNonNull(companyDaoCache, "Company cache cannot be null");
    }

    @Required
    public void setHandledUidVersion(ExtensibleUidVersion handledUidVersion) {
        this.handledUidVersion = Objects.requireNonNull(handledUidVersion, "handled uid version cannot be null");
    }

    /**
     * In this case ExtensibleUIDStringBuilder used for validation of signature.
     * Decoded uid object encodes again and their signatures compares between each other.
     * ExtensibleUIDStringBuilder used to validate the signature.
     *
     * Ensure that the implementation of the ExtensibleUIDStringBuilder and this
     * implementation of the parser fit together, otherwise validating a valid signature will fail!
     *
     * @param stringBuilder - string builder for uid object. converts the uid object to corresponding string.
     */
    @Required
    public final void setStringBuilder(final ExtensibleUIDStringBuilder stringBuilder) {
        this.stringBuilder = Objects.requireNonNull(stringBuilder, "String builder cannot be null");
    }
}
