/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.agnitas.backend.dao.ComponentDAO;
import org.agnitas.util.Bit;
import org.agnitas.util.Const;
import org.agnitas.util.Log;

/**
 * Holds all Blocks of a Mailing
 */
public class BlockCollection {
	private static final int	ATM_UNSET = 0;
	private static final int	ATM_NONE = 0;
	private static final int	ATM_TO = 1;
	private static final int	ATM_SUBJECT = 2;
	private static final String	INFO_KEY = "admin-test-mark";

	/** Reference to configuration */
	private Data		 data = null;
	/** Reference to DAO to access component table */
	private ComponentDAO	componentDao = null;
	/** All blocks in the mailing  */
	private BlockData	blocks[] = null;
	/** Total number of all blocks */
	private int		totalNumber = 0;
	/** All dynamic blocks */
	private DynCollection	dynContent = null;
	/** Collection of all found dynamic names in blocks */
	private List <String>	dynNames = null;
	/** Number of all names in dynNames */
	private int		dynCount = 0;
	/** referenced database fields in conditions */
	private HashSet <String> conditionFields = null;
	/** admin/testmark to generate for admin- and testmailings for Subject and/or From */
	private int		atmark = ATM_UNSET;
	/** number of personalizable PDF documents found for this mailing */
	private int		pdfCount = 0;
	/** number of fonts for generating personalized PDF documents for this mailing */
	private int		fontCount = 0;
	/** if maskEnvelopeFrom is true, then the sending servers address is used for envelope from, not the sender of the mailing */
	private boolean		maskEnvelopeFrom = true;
	/** a map containing all representation to be accessable for replacement during processing */
	private Map <String, String>
				headerReplace = null;

	/**
	 * setup the collection using the global configuration, optional
	 * using a customText as the only block
	 * 
	 * @param nData      the reference to the global configuration
	 * @param customText an optional text to be used instead of the mailing content
	 */
	public void setupBlockCollection (Data nData, String customText) throws Exception {
		data = nData;
		componentDao = new ComponentDAO (data.company.id (), data.mailing.id ());
		maskEnvelopeFrom = StringOps.atob (data.company.info ("mask-envelope-from", data.mailing.id ()), true);
		
		setupAdminTestmailingMarks ();
		setupAllBlocks (customText);
		setupTextblocksFromMailingInfo ();
		setupContentValidator ();
	}

	/**
	 * Parses all blocks returning a hashtable with all found
	 * tags
	 *
	 * @return the hashtable with all tags
	 */
	public Map <String, EMMTag> parseBlocks() throws Exception {
		Map <String, EMMTag>	tagTable = new HashMap<>();

		// go through all blocks
		for (int count = 0; count < this.totalNumber; count++) {
			data.logging (Log.DEBUG, "collect", "Parsing block " + (count + 1) + " of " + totalNumber);

			parseBlock (blocks[count], tagTable);
		}
		if (dynContent != null) {
			for (DynName tmp : dynContent.names ().values ()) {
				String	cname = tmp.getInterest ();

				if (cname != null) {
					conditionFields.add (cname);
				}
				for (int n = 0; n < tmp.clen; ++n) {
					DynCont cont = tmp.content.elementAt (n);

					if (cont.text != null) {
						parseBlock (cont.text, "\"" + tmp.name + "\" (text " + cont.id + ")", tagTable);
					}
					if (cont.html != null) {
						parseBlock (cont.html, "\"" + tmp.name + "\" (html " + cont.id + ")", tagTable);
					}
				}
			}
		}
		setFullURLForImageContentID (tagTable);
		consolidateUsedImages ();
		return tagTable;
	}

	/**
	 * Parse and replace all tags with fixed value
	 *
	 * @param tagTable the collection of all tags
	 */
	public void replaceFixedTags (Map <String, EMMTag> tagTable) {
		for (int n = 0; n < totalNumber; ++n) {
			if (blocks[n].isParseable) {
				parseFixedBlock (blocks[n], tagTable);
			}
		}
		if (dynContent != null) {
			for (DynName tmp : dynContent.names ().values ()) {
				for (int n = 0; n < tmp.clen; ++n) {
					DynCont cont = tmp.content.elementAt (n);

					if (cont.text != null) {
						parseFixedBlock (cont.text, tagTable);
					}
					if (cont.html != null) {
						parseFixedBlock (cont.html, tagTable);
					}
				}
			}
		}
	}

	/**
	 * Create a new image block "on-the-fly" to allow adding images which
	 * are not initital part of the mailing (e.g. during processing of an
	 * agnTag such as agnSWYN
	 * 
	 * @param name    the name of the image
	 * @param content the image as a byte array
	 * @throws        Exception
	 */
	public BlockData newImage (String name, byte[] content) throws Exception {
		int		n;
		String		ext;
		BlockData	bd;
		
		n = name.lastIndexOf ('.');
		if (n != -1) {
			ext = name.substring (n + 1).toLowerCase ();
		} else {
			ext = "gif";
		}
		bd = new BlockData (null, content, name, BlockData.RELATED_BINARY, 5, 0, "image/" + ext, false, false, true, false, false, false);
		bd.cidEmit = data.defaultImageLink (bd.cid, Imagepool.MAILING, true);
		componentDao.add (data.dbase, bd);
		addImage (bd);
		return bd;
	}

	/**
	 * add a dynamic created image block to collection
	 * 
	 * @param bd the block itself to be added
	 */
	private void addImage (BlockData bd) {
		addImages (new BlockData[] { bd });
	}
	private void addImages (List <BlockData> images) {
		int	size = images.size ();
		
		if (size > 0) {
			BlockData[]	newBlocks = new BlockData[size];
			
			for (int n = 0; n < size; ++n) {
				newBlocks[n] = images.get (n);
			}
			addImages (newBlocks);
		}
	}
	private void addImages (BlockData[] images) {
		if ((images != null) && (images.length > 0)) {
			int		size = images.length;
			BlockData[]	newBlocks = new BlockData[totalNumber + size];
			int		n, m;
			
			for (n = 0; n < totalNumber; ++n) {
				BlockData	b = blocks[n];
				
				if ((b.comptype > 1) && (b.comptype != 5)) {
					break;
				}
				newBlocks[n] = b;
			}
			for (m = 0; m < size; ++m) {
				BlockData	b = images[m];
				
				b.id = n + m;
				newBlocks[n + m] = b;
				data.logging (Log.DEBUG, "img", "Added image " + b.cid + ((b.cidEmit != null) && b.cid.equals (b.cidEmit) ? "" : " (" + b.cidEmit + ")"));
			}
			for (; n < totalNumber; ++n) {
				BlockData	b = blocks[n];
				
				b.id = n + size;
				newBlocks[n + size] = b;
			}
			totalNumber += size;
			blocks = newBlocks;
			data.logging (Log.VERBOSE, "img", "Added " + size + " images");
		}
	}

	/**
	 * return number of all blocks (component and dynamic) blocks available
	 * 
	 * @return the total count
	 */
	public int getTotalNumberOfBlocks () {
		return totalNumber;
	}
	
	/**
	 * returns number of available component blocks
	 * 
	 * @return the count of component blocks
	 */
	public int blockCount () {
		return blocks != null ? blocks.length : 0;
	}
	
	/**
	 * returns the block at the given position
	 *
	 * @param pos the index into the block array
	 * @return the block at the requested position
	 */
	public BlockData getBlock (int pos) {
		return blocks[pos];
	}

	/**
	 * get all database fields for the conditions found in each single
	 * block
	 * 
	 * @return a set of all found database fields
	 */
	public Set <String> getConditionFields () {
		return conditionFields;
	}
	
	public int getNumberOfDynamicNames () {
		return dynContent.nameCount ();
	}
	
	public Collection <DynName> getDynamicNameEntries () {
		return dynContent.names ().values ();
	}
	
	
	private void setupAdminTestmailingMarks () {
		String	atmdesc = data.company.info (INFO_KEY, data.mailing.id ());
		if (atmdesc != null) {
			List <String>	v = StringOps.splitString (null, atmdesc);
			
			for (int n = 0; n < v.size (); ++n) {
				String	desc = v.get (n);
				
				if (desc.equals ("none")) {
					atmark = ATM_NONE;
				} else if (desc.equals ("to")) {
					atmark = Bit.set (atmark, ATM_TO);
				} else if (desc.equals ("subject")) {
					atmark = Bit.set (atmark, ATM_SUBJECT);
				}
			}
		}
	}

	private void setupAllBlocks (String customText) throws Exception {
		if (customText == null) {
			totalNumber = 0;
			blocks = null;
			readBlockdata ();
		} else {
			BlockData	block = new BlockData ();

			block.id = 0;
			block.content = customText;
			block.cid = Const.Component.NAME_TEXT;
			block.isParseable = true;
			block.isText = true;
			block.type = BlockData.TEXT;
			block.media = Media.TYPE_EMAIL;
			block.comptype = 0;

			totalNumber = 1;
			blocks = new BlockData[1];
			blocks[0] = block;
		}
		dynContent = new DynCollection (data);
		dynContent.collectParts ();

		dynNames = new ArrayList <> ();
		dynCount = 0;

		conditionFields = new HashSet<>();
	}
	
	private void setupTextblocksFromMailingInfo () {
		if (StringOps.atob (data.company.info ("extended-mailing-info", data.mailing.id ()), false) && (data.mailingInfo != null)) {
			String		prefix = data.company.info ("extended-mailing-info-prefix", data.mailing.id ());
			
			if (prefix != null) {
				if (! prefix.endsWith (":")) {
					prefix += ":";
				}
			} else {
				prefix = "mi:";
			}
			for (String name : data.mailingInfo.keySet ()) {
				if (name.startsWith (prefix)) {
					String	value = data.mailingInfo.get (name);

					dynContent.addExtraContent (name, value);
				}
			}
		}
	}

	private void setupContentValidator () {
		if (! data.maildropStatus.isPreviewMailing ()) {
			String	contentValidators = data.company.info ("content-validators", data.mailing.id ());
			if (contentValidators != null) {
				data.logging (Log.INFO, "blockcollection", "Content check using \"" + contentValidators + "\" enabled");
				Stream.of (contentValidators.split (", *"))
					.filter (s -> s.length () > 0)
					.map ((String v) -> {
						String	validator = data.company.info ("content-validator-" + v);
					
						if (validator == null) {
							throw new RuntimeException (v + ": validator not found");
						}
						return new ContentValidator (data, v, validator);
					})
					.forEach (validator -> {
						if (blocks != null) {
							Stream.of (blocks)
								.filter (b -> b.isText)
								.forEach (b -> validator.validate ("component", b.cid, b.id, b));
						}
						if (dynContent != null) {
							dynContent.names ()
								.forEach ((contentNameID, contentBlock) -> {
									contentBlock.content
										.forEach (content -> validator.validate ("content", contentBlock.name, content.id, content.html));
								});
						}
					});
				data.logging (Log.INFO, "blockcollection", "Content checking succeeded");
			}
		}
	}

	private void setFullURLForImageContentID (Map <String, EMMTag> tagTable) {
		for (int count = 0; count < totalNumber; ++count) {
			BlockData	b = blocks[count];

			switch (b.comptype) {
			case 3:
			case 4:
			case 7:
				b.cidEmit = b.cid == null ? null : parseSubstitution (b.cid);
				b.mimeEmit = b.mime == null ? null : parseSubstitution (b.mime);
				break;
			case 5:
				boolean match = false;

				for (EMMTag tag : tagTable.values ()) {
					if ((tag.tagType == EMMTag.TAG_INTERNAL) && (tag.tagSpec == EMMTag.TI_IMAGE)) {
						String	name = tag.mTagParameters.get ("name");

						if ((name != null) && name.equals (b.cid)) {
							b.cidEmit = tag.getTagValue ();
							match = true;
						}
					} else if ((tag.tagType == EMMTag.TAG_INTERNAL) && (tag.tagSpec == EMMTag.TI_IMGLINK)) {
						String	name = tag.mTagParameters.get ("name");

						if ((name != null) && name.equals (b.cid)) {
							tag.imageLinkReference (data, b.urlID);
							b.cidEmit = tag.ilURL;
							match = true;
						}
					}
				}
				if ((! match) && b.isImage) {
					b.cidEmit = data.defaultImageLink (b.cid, Imagepool.MAILING, false);
				}
				break;
			default:
				break;
			}
		}
	}
		
	private void consolidateUsedImages () throws SQLException {
		BlockData[]		newBlocks;
		int			n, m;
		Set <String>		usedImages = data.getUsedComponentImages ();
		List <BlockData>	mediapool = data.getUsedMediapoolImages ();
		
		//
		// Remove images not referenced in mailing
		newBlocks = new BlockData[totalNumber];
		for (n = 0, m =0; n < totalNumber; ++n) {
			BlockData	b = blocks[n];
			
			if ((! b.isImage) || usedImages.contains (b.cid)) {
				newBlocks[m++] = blocks[n];
			} else {
				data.logging (Log.DEBUG, "img", "Remove not referenced picture " + b.cid);
			}
		}
		if (m < n) {
			data.logging (Log.VERBOSE, "img", "Removed " + (n - m) + " not referenced images");
			totalNumber = m;
			blocks = new BlockData[totalNumber];
			for (n = 0; n < totalNumber; ++n) {
				blocks[n] = newBlocks[n];
			}
		}
		//
		// Add referenced mediapool images
		if ((mediapool != null) && (mediapool.size () > 0)) {
			addImages (mediapool);
		}
	}
	
	private String replace (String key, String dflt) {
		String	rc = data.company.infoSubstituted (INFO_KEY + "-" + key, data.mailing.id ());
		
		return rc != null ? rc : dflt;
	}
	
	private String addTo () {
		if (atmark == ATM_UNSET) {
			if (data.maildropStatus.isAdminMailing ()) {
				return "\"Adminmail\" ";
			} else if (data.maildropStatus.isTestMailing ()) {
				return "\"Testmail\" ";
			}
		} else if (Bit.isset (atmark, ATM_TO)) {
			if (data.maildropStatus.isAdminMailing ()) {
				return "\"" + replace ("to-admin", "Adminmail") + "\" ";
			} else if (data.maildropStatus.isTestMailing ()) {
				return "\"" + replace ("to-test", "Testmail") + "\" ";
			}
		}
		return "";
	}

	private String addSubject () {
		if ((atmark != ATM_UNSET) && Bit.isset (atmark, ATM_SUBJECT)) {
			if (data.maildropStatus.isAdminMailing ()) {
				return replace ("subject-admin", "[ADMIN] ");
			} else if (data.maildropStatus.isTestMailing ()) {
				return replace ("subject-test", "[TEST] ");
			}
		}
		return "";
	}

	private void setupHeaderReplace () {
		if (headerReplace == null) {
			headerReplace = new HashMap <> ();
			for (int state = 0; state < 3; ++state) {
				EMail	email;
				String	key;
				
				switch (state) {
				default:
					email = null;
					key = null;
					break;
				case 0:
					email = data.mailing.fromEmail ();
					key = "email";
					break;
				case 1:
					email = data.mailing.replyTo ();
					key = "reply-to";
					break;
				case 2:
					email = data.mailing.envelopeFrom ();
					key = "envelope";
					break;
				}
				if (email != null) {
					headerReplace.put (key + "-pure", email.pure);
					headerReplace.put (key + "-full", email.full);
					headerReplace.put (key + "-pure-puny", email.pure_puny);
					headerReplace.put (key + "-full-puny", email.full_puny);
				}
			}
		}
	}
	
	private String envelope (String id) {
		String	env;
		
		env = data.mailing.getEnvelopeFrom ();
		if (! maskEnvelopeFrom) {
			return env;
		}
		return "[agnSYSINFO name=\"" + id + "\"" + (env != null ? " default=\"" + env + "\"" : "") + "/]";
	}
	private String envelopeFrom () {
		if (StringOps.atob (data.company.info ("dkim-native-return-path"), false)) {
			return returnPath ();
		}
		return envelope ("MFROM");
	}
	private String returnPath () {
		return envelope ("RPATH");
	}

	private String headFrom () {
		String	frm;

		setupHeaderReplace ();
		if ((frm = data.company.infoSubstituted ("header-from", data.mailing.id (), headerReplace)) != null) {
			return "HFrom: " + frm + data.eol;
		} else {
			return "HFrom: " + data.mailing.getFromEmailForHeader () + data.eol;
		}
	}

	private String headReplyTo () {
		String	rply;
		
		setupHeaderReplace ();
		if ((rply = data.company.infoSubstituted ("header-reply-to", data.mailing.id (), headerReplace)) != null) {
			return (rply.length () > 0 ? "HReply-To: " + rply + data.eol : rply);
		}
		if ((rply = data.mailing.getReplyToForHeader ()) != null) {
			return "HReply-To: " + rply + data.eol;
		}
		return "";
	}

	private String headAdditional () {
		String	env;
		String	method;
		String	rc;
		Map <String, String>
			extra = new HashMap <> ();
		
		env = envelopeFrom ();
		extra.put ("envelope-from", env);
		if ((method = data.company.infoSubstituted ("list-unsubscribe", data.mailing.id (), extra)) == null) {
			String	link = null;
			if (data.rdirDomain != null) {
				String	rdirContextLink = data.company.info ("rdir.UseRdirContextLinks");
				
				if ((rdirContextLink != null) && StringOps.atob (rdirContextLink, false)) {
					link = "<" + data.rdirDomain + "/uq/[agnUID]/uq.html>";
				} else {
					link = "<" + data.rdirDomain + "/uq.html?uid=[agnUID]>";
				}
			}
			method = (link != null ? (link + ", ") : "") + "<mailto:" + env + "?subject=unsubscribe:[agnUID]>";
		} else {
			method = method.trim ();
			if (method.equals ("-")) {
				method = null;
			}
		}
		if ((method == null) || (method.length () == 0)) {
			rc = "";
		} else {
			rc = "HList-Unsubscribe: " + method + data.eol;
			String	listUnsubscribePost = data.company.infoSubstituted ("list-unsubscribe-post", data.mailing.id ());
			
			if (! "-".equals (listUnsubscribePost)) {
				rc += "HList-Unsubscribe-Post: " + (listUnsubscribePost != null ? listUnsubscribePost : "List-Unsubscribe=One-Click") + data.eol;
			}
		}
		
		String[]	more = data.company.infoList ("header-add", data.mailing.id (), extra);
		
		if ((more != null) && (more.length > 0)) {
			for (String h : more) {
				String	prefix = "H";
				
				for (String p : h.split ("\r?\n[ \t]*")) {
					rc += prefix + p + data.eol;
					prefix = "\t";
				}
			}
		}
		return rc;
	}

	/**
	 * Creates the first block holding the header information.
	 *
	 * @return the newly created block
	 */
	private BlockData createBlockZero () {
		BlockData	b = new BlockData ();
		String		head;

		if (data.mailing.fromEmailIsValid () && (data.mailing.subject () != null)) {
			String mfrom = envelopeFrom ();
			String rpath = returnPath ();

			if (mfrom == null) {
				mfrom = "";
			}
			if (rpath == null) {
				rpath = mfrom;
			}
			head =	"T[agnSYSINFO name=\"EPOCH\"]" + data.eol +
				"S<" + mfrom + ">" + data.eol +
				"R<[agnEMAIL code=\"punycode\"]>" + data.eol +
				"H?mP?Return-Path: <" + rpath +">" + data.eol +
				"HReceived: by [agnSYSINFO name=\"FQDN\" default=\"" + data.mailing.domain () + "\"] for <[agnEMAIL]>; [agnSYSINFO name=\"RFCDATE\"]" + data.eol +
				"HMessage-ID: <[agnMESSAGEID]>" + data.eol +
				"HDate: [agnSYSINFO name=\"RFCDATE\"]" + data.eol;
			head += headFrom ();
			head += headReplyTo ();
			head += "HTo: " + addTo () + "<" + "[agnEMAIL code=\"punycode\"]" + ">" + data.eol +
				"HSubject: " + addSubject () + data.mailing.subject () + data.eol;
			head += headAdditional ();
			head += "HX-Mailer: " + data.mailing.makeMailer () + data.eol +
				"HMIME-Version: 1.0" + data.eol;
		} else {
			head = "- unset -" + data.eol;
		}

		b.content = head;
		b.cid = Const.Component.NAME_HEADER;
		b.isParseable = true;
		b.isText = true;
		b.type = BlockData.HEADER;
		b.media = Media.TYPE_EMAIL;
		b.comptype = 0;
		return b;
	}

	private void cleanupBlockCollection(List <BlockData> c) {
		BlockData	header;
		
		header = null;
		for (int n = 0; n < c.size (); ++n) {
			BlockData	bd = c.get (n);
			
			if (bd.type == BlockData.HEADER) {
				if (header == null) {
					header = bd;
				} else {
					if (header.id < bd.id) {
						c.remove (header);
						header = bd;
					} else {
						c.remove (bd);
					}
					--n;
				}
			}
		}
		if ((pdfCount == 0) && (fontCount > 0)) {
			for (int n = 0; (n < c.size ()) && (fontCount > 0); ) {
				BlockData	bd = c.get (n);
				
				if (bd.isFont && c.remove (bd)) {
					--fontCount;
				} else {
					++n;
				}
			}
		}
	}

	/**
	 * Reads the blocks used by this mailing from the database
	 */
	private void readBlockdata () throws Exception {
		int[]			comptypes = null;
		
		if (data.maildropStatus.isPreviewMailing ()) {
			if (data.previewCreateAll) {
				comptypes = new int[] {0, 4, 5, 6, 7};
			} else {
				comptypes = new int[] {0, 4, 5};
			}
		}
		
		List <BlockData>	components = componentDao.retrieve (data.dbase, comptypes);
		List <BlockData>	collect;

		collect = new ArrayList <> ();
		collect.add (createBlockZero ());
		totalNumber = 1;
		for (BlockData block : components) {
			collect.add (block);
			++totalNumber;
			if (block.isPDF) {
				++pdfCount;
			}
			if (block.isFont) {
				++fontCount;
			}
		}
		cleanupBlockCollection(collect);
		totalNumber = collect.size ();
		blocks = collect.toArray (new BlockData[totalNumber]);
		for (int n = 0; n < totalNumber; ++n) {
			BlockData	b = blocks[n];

			data.logging (Log.DEBUG, "collect",
					  "Block " + n + " (" + totalNumber + "): " + b.cid + " [" + b.mime + "]");
		}

		Arrays.sort(blocks);

		for (int n = 0; n < totalNumber; ++n) {
			BlockData	b = blocks[n];

			b.id = n;
			if (b.targetID != 0) {
				Target	tgt = data.targetExpression.getTarget (b.targetID, true);

				if ((tgt != null) && tgt.valid ()) {
					b.condition = tgt.getSQL (false);
				}
			}
			data.logging (Log.DEBUG, "collect",
					  "Block " + n + " (" + totalNumber + "): " + b.cid + " [" + b.mime + "] " + b.targetID + (b.condition != null ? " SQL: " + b.condition : ""));
		}
	}

	/**
	 * Parses a block, collecting all tags in a hashtable
	 *
	 * @param cb       the block to parse
	 * @param tagTable the hashtable to collect tag
	 */
	private void parseBlock (BlockData cb, Map <String, EMMTag> tagTable) throws Exception {
		parseBlock (cb, cb.cid, tagTable);
	}
	private void parseBlock (BlockData cb, String name, Map <String, EMMTag> tagTable) throws Exception {
		if (cb.isParseable) {
			int tag_counter = 0;
			String current_tag;

			// get all tags inside the block
			while( (current_tag = cb.getNextTag() ) != null){
				try{
					// add tag and EMMTag data structure to hashtable
					if (! tagTable.containsKey (current_tag)) {
						EMMTag	ntag = new EMMTag (data, current_tag);
						String	dyName;

						ntag.initialize (data, false);
						if ((ntag.tagType == EMMTag.TAG_INTERNAL) &&
						    (ntag.tagSpec == EMMTag.TI_DYN) &&
						    ((dyName = ntag.mTagParameters.get ("name")) != null)) {
							int n;

							for (n = 0; n < dynCount; ++n) {
								if (dyName.equals (dynNames.get (n))) {
									break;
								}
							}
							if (n == dynCount) {
								dynNames.add (dyName);
								dynCount++;
							}
						}
						tagTable.put(current_tag, ntag);
						data.logging (Log.DEBUG, "collect", "Added Tag: " + current_tag);
					} else {
						data.logging (Log.DEBUG, "collect", "Skip existing Tag: " + current_tag);
					}
				} catch (Exception e) {
					data.logging (Log.ERROR, "collect", "Failed in collecting block " + name, e);
					throw new Exception ("Error while trying to query block " + tag_counter + ": " + e.toString ());
				}
				tag_counter++;
			}
			// check for tagless blocks
			if ( tag_counter == 0 ) {
				cb.isParseable = false; // block contained no tags!
			}
		}
	}

	/**
	 * Substidute parts of a filename using some pattern
	 *
	 * @return the replacement string
	 */
	private String substituteParameter (String mod, String parm, String dflt) {
		if (mod.equals ("TS")) {
			if (parm == null) {
				parm = "yyyy-MM-dd";
			}
			SimpleDateFormat	fmt = new SimpleDateFormat (parm);
			String			what;

			try {
				what = fmt.format (new java.util.Date ());
			} catch (java.lang.IllegalArgumentException e) {
				data.logging (Log.INFO, "subparam", "used invalid format for type Date: " + parm + ": " + e.toString ());
				fmt = new SimpleDateFormat ("yyyy-MM-dd");
				what = fmt.format (new java.util.Date ());
			}
			dflt = what;
		} else if (mod.equals ("COL")) {
			if (parm != null) {
				parm = parm.toLowerCase ();
				dflt = "%(" + parm + ")";
				conditionFields.add (parm);
			}
		}
		return dflt;
	}

	private String parseSubstitution (String src) {
		int		cur = 0;
		int		start, end;
		StringBuffer	res = null;

		while ((start = src.indexOf ("%[", cur)) != -1) {
			end = src.indexOf ("]%", start);
			if (end == -1) {
				break;
			}
			if (res == null) {
				res = new StringBuffer (src.length ());
			}
			res.append (src.substring (cur, start));

			String	cont = src.substring (start + 2, end);
			int parmoffset = cont.indexOf (':');
			String	mod, parm;

			if (parmoffset == -1) {
				mod = cont;
				parm = null;
			} else {
				mod = cont.substring (0, parmoffset);
				++parmoffset;
				while ((parmoffset < cont.length ()) && Character.isWhitespace (cont.charAt (parmoffset))) {
					++parmoffset;
				}
				parm = cont.substring (parmoffset);
			}
			res.append (substituteParameter (mod, parm, src.substring (start, end + 2)));
			cur = end + 2;
		}
		if ((res != null) && (cur < src.length ())) {
			res.append (src.substring (cur));
		}
		return res == null ? null: res.toString ();
	}

	/**
	 * Already parse and replace tags with fixed value
	 *
	 * @param b the block to parse
	 * @param tagTable the tag collection
	 */
	private void parseFixedBlock (BlockData b, Map <String, EMMTag> tagTable) {
		String		cont = b.content != null ? b.content : "";
		int		clen = cont.length ();
		StringBuffer	buf = new StringBuffer (clen + 128);
		List <TagPos>	pos = b.tagPositions;
		int		count = pos.size ();
		int		start = 0;
		int		offset = 0;
		boolean		changed = false;

		for (int m = 0; m < count; ) {
			TagPos	tp = pos.get (m);
			EMMTag	tag = tagTable.get (tp.getTagname ());
			String	value = tag.getTagValue ();

			if (value == null) {
				tag.fixedValue = false;
			}
			if (value != null && tag.fixedValue) {
				offset += value.length () - tag.mTagFullname.length ();
				buf.append (cont.substring (start, tp.getStart ()) + value);
				start = tp.getEnd () + 1;
				pos.remove (m);
				--count;
				changed = true;
			} else {
				if ((tp.getContent () != null) && tp.getContent ().isParseable) {
					parseFixedBlock (tp.getContent (), tagTable);
				}
				tp.relocateBy (offset);
				++m;
			}
		}
		if (changed) {
			if (start < clen) {
				buf.append (cont.substring (start));
			}
			b.content = StringOps.convertOld2New (buf.toString ());
			if (count == 0) {
				b.isParseable = false;
			}
		}
	}
}
