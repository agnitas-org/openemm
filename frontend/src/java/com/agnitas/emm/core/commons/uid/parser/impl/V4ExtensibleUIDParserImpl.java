/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.uid.parser.impl;

import com.agnitas.emm.core.commons.encoder.UIDBase64;
import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.commons.uid.ExtensibleUidVersion;
import com.agnitas.emm.core.commons.uid.UIDFactory;
import com.agnitas.emm.core.commons.uid.builder.impl.exception.RequiredInformationMissingException;
import com.agnitas.emm.core.commons.uid.builder.impl.exception.UIDStringBuilderException;
import com.agnitas.emm.core.commons.uid.parser.exception.UIDParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implementation of UID parser for UID version 4.
 *
 * Supported format of UID is:
 * <pre>
 * [ &lt;prefix> &quot;.&quot; ] &lt;uid-version> &quot;.&quot; &lt;license-ID> &quot;.&quot; &lt;company-ID> &quot;.&quot; &lt;mailing-ID> &quot;.&quot; &lt;customer-ID> &quot;.&quot; &lt;url-ID> &quot;.&quot; &lt;bitfield> &quot;.&quot; &lt;signature>
 * </pre>
 *
 * <ul>
 *  <li><i>prefix</i> is optional. It is used for control values (like &quot;nc&quot; to disable caching)</li>
 *  <li><i>uid-version</i> is used to improve performance when parsing UID</li>
 *  <li><i>license-ID</i> is the ID of the EMM license</li>
 *  <li><i>company-ID</i> is the ID of the company</li>
 *  <li><i>mailing-ID</i> is the ID of that mailing, that contains the UID</li>
 *  <li><i>customer-ID</i> is the ID of the customer for that this UID is issued</li>
 *  <li><i>url-ID</i> if UID is part of an trackable link, this is the ID of that link</li>
 *  <li><i>bitfield</i> field of bit-encoded flags</li>
 *  <li><i>signature</i> is a security value to make engineering some valid UID more difficult</li>
 * </ul>
 */
public class V4ExtensibleUIDParserImpl extends BaseExtensibleUIDParser {

	/** The logger. */
    private static final Logger logger = LogManager.getLogger(V4ExtensibleUIDParserImpl.class);

    /** Index of prefix in normalized form. */
    private static final int PREFIX_GROUP = 0;
    
    /** Index of UID version in normalized form. */
    private static final int VERSION_GROUP = 1;
    
    /** Index of license ID in normalized form. */
    private static final int LICENSE_ID_GROUP = 2;
    
    /** Index of company ID in normalized form. */
    private static final int COMPANY_ID_GROUP = 3;
    
    /** Index of mailing ID in normalized form. */
    private static final int MAILING_ID_GROUP = 4;
    
    /** Index of customer ID in normalized form. */
    private static final int CUSTOMER_ID_GROUP = 5;
    
    /** Index of url ID in normalized form. */
    private static final int URL_ID_GROUP = 6;
    
    /** Index of bit field in normalized form. */
    private static final int BITFIELD_GROUP = 7;
    
    /** Index of signature in normalized form. */
    private static final int SIGNATURE_GROUP = 8;

    /** Minimum number of groups (no optional groups present). */
    private static final int MIN_GROUPS_LENGTH = 8;
    
    /** Maximuim number of groups (all optional groups present). */
    private static final int MAX_GROUPS_LENGTH = 9;
    
    /** Length of signature. */
    private static final int SIGNATURE_LENGTH = 86;

    /** Encoder for modified base64 encoding. */
    private final UIDBase64 base64Encoder;
    
    public V4ExtensibleUIDParserImpl() {
        super(logger, MIN_GROUPS_LENGTH, MAX_GROUPS_LENGTH, SIGNATURE_LENGTH);
        base64Encoder = new UIDBase64();
    }

    @Override
    protected ExtensibleUID parse(String uidString, String[] parts) throws UIDParseException {
        final String[] correctedParts = correctUIDParts(parts);
        
        final int version = (int) base64Encoder.decodeLong(correctedParts[VERSION_GROUP]);
        if(!ExtensibleUidVersion.V4_WITH_COMPANY_ID.isVersionCode(version)) {
        	throw new UIDParseException(String.format("UID version %d not supported by this parser", version));
        }

        return UIDFactory.from(
                correctedParts[PREFIX_GROUP],
                (int) base64Encoder.decodeLong(correctedParts[LICENSE_ID_GROUP]),
                (int) base64Encoder.decodeLong(correctedParts[COMPANY_ID_GROUP]),
                (int) (base64Encoder.decodeLong(correctedParts[CUSTOMER_ID_GROUP])),
                (int) (base64Encoder.decodeLong(correctedParts[MAILING_ID_GROUP])),
                (int) (base64Encoder.decodeLong(correctedParts[URL_ID_GROUP])),
		1,
                base64Encoder.decodeLong(correctedParts[BITFIELD_GROUP]));
    }

    @Override
    protected String getActualSignature(String[] parts) {
        String[] correctedParts = correctUIDParts(parts);
        return correctedParts[SIGNATURE_GROUP];
    }

    @Override
    protected String getExpectedSignature(ExtensibleUID uid) throws UIDStringBuilderException, RequiredInformationMissingException {
        final String uidString = stringBuilder.buildUIDString(uid);
        final String[] uidParts = correctUIDParts(splitUIDString(uidString));
        return uidParts[uidParts.length - 1];
    }

	@Override
	public final ExtensibleUidVersion getHandledUidVersion() {
		return ExtensibleUidVersion.V4_WITH_COMPANY_ID;
	}

}
