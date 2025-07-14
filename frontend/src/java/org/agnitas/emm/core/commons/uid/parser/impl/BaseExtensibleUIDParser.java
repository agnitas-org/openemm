/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.uid.parser.impl;

import java.util.Objects;

import org.agnitas.emm.core.commons.uid.builder.ExtensibleUIDStringBuilder;
import org.agnitas.emm.core.commons.uid.builder.impl.exception.RequiredInformationMissingException;
import org.agnitas.emm.core.commons.uid.builder.impl.exception.UIDStringBuilderException;
import org.agnitas.emm.core.commons.uid.parser.ExtensibleUIDParser;
import org.agnitas.emm.core.commons.uid.parser.exception.DeprecatedUIDVersionException;
import org.agnitas.emm.core.commons.uid.parser.exception.InvalidUIDException;
import org.agnitas.emm.core.commons.uid.parser.exception.SignatureNotMatchParseException;
import org.agnitas.emm.core.commons.uid.parser.exception.UIDParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.commons.uid.beans.CompanyUidData;
import com.agnitas.emm.core.commons.uid.daocache.impl.CompanyUidDataDaoCache;

/**
 * Base class for all UID parsers.
 */
public abstract class BaseExtensibleUIDParser implements ExtensibleUIDParser {
    
    /** Caching DAO for accessing company data. */
	protected CompanyUidDataDaoCache companyUidDataDaoCache;
    
    /** Corresponding UID string builder. */
    protected ExtensibleUIDStringBuilder stringBuilder;

    /** Minimum number of required UID parts. */
    private int minPartsCount;
    
    /** Maximum number of UID parts. */
    private int maxPartsCount;
    
    /** Length of signature in bytes. */
    private int signatureLength;

    /** Logger. */
    private Logger logger;

    /**
     * Creates a new instance.
     * 
     * @param logger Logger from sub class
     * @param minPartsCount minimum number of UID parts
     * @param maxPartsCount maximum number of UID parts
     * @param signatureLength length of signature in bytes
     */
    protected BaseExtensibleUIDParser(Logger logger, int minPartsCount, int maxPartsCount, int signatureLength) {
        this.logger = logger;
        this.minPartsCount = minPartsCount;
        this.maxPartsCount = maxPartsCount;
        this.signatureLength = signatureLength;
    }

    @Override
    public ExtensibleUID parse(final String uidString) throws UIDParseException, InvalidUIDException, DeprecatedUIDVersionException {
        if (StringUtils.isEmpty(uidString)) {
            return null;
        }

        // Check the base format (number of parts, length of signature, ...)
        if (!isSupportedUidFormat(uidString)) {
            throw new InvalidUIDException("Invalid number of uid parts or invalid length of signature.", uidString);
        }

        // Split the UID into parts
        final String[] parts = splitUIDString(uidString);
        final ExtensibleUID uid = parse(uidString, parts);

        checkSignature(uidString, uid, parts);
        checkUIDVersion(uidString, uid);

        return uid;
    }

    /**
     * Parses UID from given UID string.
     * 
     * @param uidString full UID string
     * @param parts splitted UID string
     * 
     * @return parsed UID
     * 
     * @throws UIDParseException on errors parsing UID
     */
    protected abstract ExtensibleUID parse(final String uidString, final String[] parts) throws UIDParseException;

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

    /**
     * Checks if UID version is supported by company.
     * 
     * @param uidString UID string (used for error message only)
     * @param uid parsed UID
     * 
     * @throws DeprecatedUIDVersionException if UID version is not supported
     */
    private void checkUIDVersion(String uidString, ExtensibleUID uid) throws DeprecatedUIDVersionException {
        final CompanyUidData companyUidData = this.companyUidDataDaoCache.getItem(uid.getCompanyID());
        Number minimumSupportedVersion = companyUidData.getMinimumSupportedUIDVersion();
        if (getHandledUidVersion().isOlderThan(minimumSupportedVersion)) {
            String descriptionTemplate = "Version validation Error. Deprecated UID version. minimumSupportedVersion: %d, actualVersion: %s, encodedUid: %s";
            String message = String.format(descriptionTemplate, minimumSupportedVersion, getHandledUidVersion().getVersionCode(), uidString);
            logger.warn(message);
            throw new DeprecatedUIDVersionException(message, uid);
        }
    }

    /**
     * Checks signature of UID.
     * 
     * @param uidString UID string (used for error message only)
     * @param uid parsed UID
     * @param parts parts of the UID string
     * 
     * @throws InvalidUIDException on mismatching UID signature
     * @throws UIDParseException on errors parsing UID
     */
    private void checkSignature(String uidString, ExtensibleUID uid, String[] parts) throws InvalidUIDException, UIDParseException { // TODO Replace InvalidUIDException by SignatureNotMatchParseExcepitn
        try {
            String expectedSignature = getExpectedSignature(uid);
            String actualSignature = getActualSignature(parts);
            if (!actualSignature.equals(expectedSignature)) {
                String descriptionTemplate = "Signature validation error. Signature doesn't match. Expected signature: %s, actual signature: %s, version: %d, uid: %s";
                String message = String.format(descriptionTemplate, expectedSignature, actualSignature, getHandledUidVersion().getVersionCode(), uidString);
                if(logger.isInfoEnabled()) {
                	logger.info(message);
                }
                
                throw new SignatureNotMatchParseException(message);
            }
        } catch (UIDStringBuilderException | RequiredInformationMissingException e) {
            String messageTemplate = "Signature validation error. Cannot create string from decoded uid. uid: %s, decoded UID: %s";
            String errorMessage = String.format(messageTemplate, uidString, uid);
            logger.error(String.format(errorMessage, uidString, uid), e);
            throw new UIDParseException(errorMessage, e);
        }
    }

    /**
     * Determine the actual signature for the given parts of UID string.
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
    protected abstract String getExpectedSignature(final ExtensibleUID uid) throws UIDStringBuilderException, RequiredInformationMissingException;

    @Override
    public boolean isSupportedUidFormat(String uidString) {
        return StringUtils.isNotBlank(uidString) && isValidBaseFormat(splitUIDString(uidString));
    }

    /**
     * Check base form of UID. (Number of parts, signature length, ...)
     * 
     * @param parts parts uid UID string
     * 
     * @return <code>true</code> if base form of UID is valid
     */
    protected boolean isValidBaseFormat(final String[] parts) {
        return (parts.length == minPartsCount || parts.length == maxPartsCount) && parts[parts.length - 1].length() == signatureLength;
    }

    /**
     * Set CompanyUidDataDaoCache.
     *
     * @param companyDaoCache CompanyUidDataDaoCache
     */
    public void setCompanyUidDataDaoCache(final CompanyUidDataDaoCache companyUidDataDaoCache) {
        this.companyUidDataDaoCache = Objects.requireNonNull(companyUidDataDaoCache, "Company cache cannot be null");
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
    public final void setStringBuilder(final ExtensibleUIDStringBuilder stringBuilder) {
        this.stringBuilder = Objects.requireNonNull(stringBuilder, "String builder cannot be null");
    }
}
