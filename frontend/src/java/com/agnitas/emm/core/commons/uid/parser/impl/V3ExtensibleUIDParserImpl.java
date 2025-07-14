/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.uid.parser.impl;

import java.util.Objects;

import org.agnitas.emm.core.commons.uid.builder.impl.exception.RequiredInformationMissingException;
import org.agnitas.emm.core.commons.uid.builder.impl.exception.UIDStringBuilderException;
import org.agnitas.emm.core.commons.uid.parser.exception.MailingNotFoundParseException;
import org.agnitas.emm.core.commons.uid.parser.exception.UIDParseException;
import org.agnitas.emm.core.commons.uid.parser.impl.BaseExtensibleUIDParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.agnitas.beans.RdirMailingData;
import com.agnitas.emm.core.commons.encoder.UIDBase64;
import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.commons.uid.ExtensibleUidVersion;
import com.agnitas.emm.core.commons.uid.UIDFactory;
import com.agnitas.emm.core.commons.uid.daocache.impl.RdirMailingDataDaoCache;

/**
 * Implementation of UID parser for UID version 2.
 *
 * Supported format of UID is:
 * <pre>
 * [ &lt;prefix> &quot;.&quot; ] &lt;extension> &quot;.&quot; &lt;license-ID> &quot;.&quot; &lt;mailing-ID> &quot;.&quot; &lt;customer-ID> &quot;.&quot; &lt;url-ID> &quot;.&quot; &lt;signature>
 * </pre>
 *
 * <ul>
 *  <li><i>prefix</i> is optional. It is used for control values (like &quot;nc&quot; to disable caching)</li>
 *  <li><i>uid-version</i> is used to improve performance when parsing UID</li>
 *  <li><i>license-ID</i> is the ID of the EMM license</li>
 *  <li><i>mailing-ID</i> is the ID of that mailing, that contains the UID</li>
 *  <li><i>customer-ID</i> is the ID of the customer for that this UID is issued</li>
 *  <li><i>url-ID</i> if UID is part of an trackable link, this is the ID of that link</li>
 *  <li><i>signature</i> is a security value to make engineering some valid UID more difficult</li>
 * </ul>
 */
public class V3ExtensibleUIDParserImpl extends BaseExtensibleUIDParser {

	/** The logger. */
    private static final Logger logger = LogManager.getLogger(V3ExtensibleUIDParserImpl.class);

    private static final int PREFIX_GROUP = 0;
    private static final int LICENSE_ID_GROUP = 2;
    private static final int MAILING_ID_GROUP = 3;
    private static final int CUSTOMER_ID_GROUP = 4;
    private static final int URL_ID_GROUP = 5;
    private static final int BITFIELD_GROUP = 6;
    private static final int SIGNATURE_GROUP = 7;

    private static final int MIN_GROUPS_LENGTH = 7;
    private static final int MAX_GROUPS_LENGTH = 8;
    private static final int SIGNATURE_LENGTH = 86;

    private final UIDBase64 base64Encoder;
    private RdirMailingDataDaoCache mailingDataDaoCache;

    public V3ExtensibleUIDParserImpl() {
        super(logger, MIN_GROUPS_LENGTH, MAX_GROUPS_LENGTH, SIGNATURE_LENGTH);
        base64Encoder = new UIDBase64();
    }

    @Override
    protected ExtensibleUID parse(String uidString, String[] parts) throws UIDParseException {
        final String[] correctedParts = correctUIDParts(parts);
        RdirMailingData mailingData = getMailingData(uidString, correctedParts);

        return UIDFactory.from(
                correctedParts[PREFIX_GROUP],
                (int) base64Encoder.decodeLong(correctedParts[LICENSE_ID_GROUP]),
                mailingData.getCompanyID(),
                (int) (base64Encoder.decodeLong(correctedParts[CUSTOMER_ID_GROUP])),
                (int) (base64Encoder.decodeLong(correctedParts[MAILING_ID_GROUP])),
                (int) (base64Encoder.decodeLong(correctedParts[URL_ID_GROUP])),
                base64Encoder.decodeLong(correctedParts[BITFIELD_GROUP]));
    }

    private RdirMailingData getMailingData(final String uidString, String[] parts) throws UIDParseException {
        final int mailingID = (int) base64Encoder.decodeLong(parts[MAILING_ID_GROUP]);
        final RdirMailingData mailingData = this.mailingDataDaoCache.getItem(mailingID);

        if (Objects.isNull(mailingData)) {
            String errorMessage = String.format("Error validating UID. No such mailing. mailingId: %d uid: %s", mailingID, uidString);
            
            if(logger.isInfoEnabled()) {
            	logger.info(errorMessage);
            }
            
            throw new MailingNotFoundParseException(errorMessage);
        }

        return mailingData;
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

    public final void setRdirMailingDataDaoCache(final RdirMailingDataDaoCache mailingDataDaoCache) {
        this.mailingDataDaoCache = Objects.requireNonNull(mailingDataDaoCache, "Mailing cache cannot be null");
    }

	@Override
	public final ExtensibleUidVersion getHandledUidVersion() {
		return ExtensibleUidVersion.UID_WITH_BITFIELD_USING_SHA512;
	}
    
    
}
