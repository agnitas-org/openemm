/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.uid.parser.impl;

import java.io.IOException;
import org.agnitas.emm.core.commons.uid.builder.impl.exception.RequiredInformationMissingException;
import org.agnitas.emm.core.commons.uid.builder.impl.exception.UIDStringBuilderException;
import org.agnitas.emm.core.commons.uid.parser.exception.UIDParseException;
import org.agnitas.emm.core.commons.uid.parser.impl.BaseExtensibleUIDParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.MessageFormat;
import org.msgpack.value.ValueType;

import com.agnitas.emm.core.commons.encoder.UIDBase64;
import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.commons.uid.ExtensibleUidVersion;
import com.agnitas.emm.core.commons.uid.UIDFactory;

/**
 * Implementation of UID parser for UID version 5.
 *
 */
public class V5ExtensibleUIDParserImpl extends BaseExtensibleUIDParser {

	/** The logger. */
	private static final Logger logger = LogManager.getLogger(V5ExtensibleUIDParserImpl.class);

	/** Index of prefix in normalized form. */
	private static final int PREFIX_GROUP = 0;

	/** Index of UID version in normalized form. */
	private static final int VERSION_GROUP = 1;

	/** Index of payload in normalized form. */
	private static final int PAYLOAD_GROUP = 2;

	/** Index of signature in normalized form. */
	private static final int SIGNATURE_GROUP = 3;

	/** Minimum number of groups (no optional groups present). */
	private static final int MIN_GROUPS_LENGTH = 3;

	/** Maximuim number of groups (all optional groups present). */
	private static final int MAX_GROUPS_LENGTH = 4;

	/** Length of signature. */
	private static final int SIGNATURE_LENGTH = 86;

	/** Encoder for modified base64 encoding. */
	private final UIDBase64 base64Encoder;

	public V5ExtensibleUIDParserImpl() {
		super(logger, MIN_GROUPS_LENGTH, MAX_GROUPS_LENGTH, SIGNATURE_LENGTH);
		base64Encoder = new UIDBase64();
	}

	@Override
	protected ExtensibleUID parse(String uidString, String[] parts) throws UIDParseException {
		final String[] correctedParts = correctUIDParts(parts);

		final int version = (int) base64Encoder.decodeLong(correctedParts[VERSION_GROUP]);
		if(!ExtensibleUidVersion.V5_AGNOSTIC.isVersionCode(version)) {
			throw new UIDParseException(String.format("UID version %d not supported by this parser", version));
		}
		int	licenceID = 0;
		int	companyID = 0;
		int	customerID = 0;
		int	mailingID = 0;
		int	urlID = 0;
		long	bitfield = 0;
		long	senddate = 0;
		byte[]	payload = base64Encoder.decodeBytes (correctedParts[PAYLOAD_GROUP]);

		if (payload != null && payload.length > 0) {
			try (MessageUnpacker mp = MessagePack.newDefaultUnpacker (payload)) {
				int		state = 0;
				String		name = null;

				while ((state >= 0) && mp.hasNext ()) {
					MessageFormat	format = mp.getNextFormat ();
					ValueType	type = format.getValueType ();
				
					switch (state) {
					default:
						state = -1;
						break;
					case 0:
						if (type.isMapType ()) {
							state = 1;
							mp.unpackMapHeader ();
						} else {
							state = -1;
						}
						break;
					case 1:
						if (type.isStringType ()) {
							state = 2;
							name = mp.unpackString ();
						} else {
							state = -1;
						}
						break;
					case 2:
						state = 1;
						if ((name != null) && type.isIntegerType ()) {
							long	value = 0;
						
							switch (format) {
							case UINT64:
								value = mp.unpackBigInteger ().longValue ();
								break;
							case INT64:
							case UINT32:
								value = mp.unpackLong ();
								break;
							default:
								value = mp.unpackInt ();
								break;
							}
							switch (name) {
							case "_l":	licenceID = (int) value;	break;
							case "_c":	companyID = (int) value;	break;
							case "_m":	mailingID = (int) value;	break;
							case "_r":	customerID = (int) value;	break;
							case "_u":	urlID = (int) value;		break;
							case "_o":	bitfield = value;		break;
							case "_s":	senddate = value;		break;
							default:	/* STFU */			break;
							}
						} else {
							mp.unpackValue ();
						}
						break;
					}
				}
			} catch (IOException e) {
				throw new UIDParseException ("failed parsing payload: " + e.toString ());
			}
		}
		
		ExtensibleUID uid = UIDFactory.from(correctedParts[PREFIX_GROUP], licenceID, companyID, customerID, mailingID, urlID, bitfield);
		if (senddate > 0) {
			uid.setSendDate (senddate);
		}
		return uid;
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
		return ExtensibleUidVersion.V5_AGNOSTIC;
	}

}
