/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import java.util.Vector;

/**
 * Hold the data for one block
 */
public class BlockData implements Comparable <BlockData> {
	/** Possible block types */
	public static final int	HEADER = 0;
	public static final int	TEXT = 1;
	public static final int	HTML = 2;
	public static final int	RELATED_TEXT = 3;
	public static final int	RELATED_BINARY = 4;
	public static final int	ATTACHMENT_TEXT = 5;
	public static final int	ATTACHMENT_BINARY = 6;
	public final static int	FONT = 7;
	public final static int	PDF = 8;
	public final static int	MMS = 9;
	public final static int	SMS = 10;
	public final static int WHATSAPP = 11;

	public final static int	SIG_NONE = 0;
	public final static int	SIG_EXTERN = 1;
	public final static int	SIG_INTERN = 2;

	/** The index in the array in BlockCollection */
	public int	id;
	/** The content from the database */
	public String	content;
	/** the related binary part for this block */
	public byte[]	binary;
	/** the content ID */
	public String	cid;
	/** the content ID to emit, if not NULL, else use cid */
	public String	cidEmit;
	/** Type of the block */
	public int	type;
	/** Media of the block (just EMail atm) */
	public int	media = -1;
	/** Component type */
	public int	comptype;
	/** optional URL_ID (for image links) */
	public long	urlID;
	/** optional assigned condition */
	public int	targetID;
	/** MIME type for block */
	public String	mime;
	/** Emit string for mime, if not null */
	public String	mimeEmit;
	/** if this block is parsable */
	public boolean	isParseable;
	/** if this is a textual block */
	public boolean	isText;
	/** if this is an image */
	public boolean	isImage;
	/** if this should be handled as attachment */
	public boolean	isAttachment;
	/** if this is a personalized PDF */
	public boolean	isPDF;
	/** if this is a font for the personalized PDF */
	public boolean	isFont;
	/** if this is a signature for the personalized PDF */
	public boolean	isSignature;
	/** if this is a signrature, the type if this signature */
	public int	signatureType;
	/** if the content is already base64 precoded */
	public boolean	isPrecoded;
	/** The condition from dyn target table */
	public  String	condition;
	/** Index for internal tag parseing */
	private int current_pos=0;
	/** Store positions of found tags: start/end values */
	public Vector <TagPos>
			tagPositions;
	/** Store tag names on recrusive calls */
	private Vector <String>
			tagNames;

	public void setID (int nID) {
		id = nID;
	}
	public void setContent (String nContent) {
		content = nContent;
	}
	public String cid () {
		return cid;
	}
	public void setCidEmit (String nCidEmit) {
		cidEmit = nCidEmit;
	}
	public int type () {
		return type;
	}
	public int comptype () {
		return comptype;
	}
	public long urlID () {
		return urlID;
	}
	public String mime () {
		return mime;
	}
	public boolean mimeStartsWith (String prefix) {
		return mime != null && mime.startsWith (prefix);
	}
	public void setMimeEmit (String nMimeEmit) {
		mimeEmit = nMimeEmit;
	}
	public boolean isParseable () {
		return isParseable;
	}
	
	/** Constructor for this class
	 */
	public BlockData() {
		id = -1;
		content = null;
		binary = null;
		cid = null;
		cidEmit = null;
		type = -1;
		comptype = -1;
		urlID = 0;
		targetID = 0;
		mime = null;
		mimeEmit = null;
		isParseable = false;
		isText = false;
		isImage = false;
		isAttachment = false;
		isPDF = false;
		isFont = false;
		isSignature = false;
		signatureType = SIG_NONE;
		isPrecoded = false;
		condition = null;
		current_pos = 0;
		tagPositions = new Vector<>();
		tagNames = new Vector<>();
	}
	/** Constructor with most variables set
	 */
	public BlockData(String content, byte[] binary, String cid,
			 int type, int comptype, long urlID, String mime,
			 boolean isParseable, boolean isText, boolean isImage) {
		this ();
		this.content = content;
		this.binary = binary;
		this.cid = cid;
		this.type = type;
		this.comptype = comptype;
		this.urlID = urlID;
		this.mime = mime;
		this.isParseable = isParseable;
		this.isText = isText;
		this.isImage = isImage;
	}
	public BlockData(String content, byte[] binary, String cid, 
			 int type, int comptype, long urlID, String mime,
			 boolean isParseable, boolean isText, boolean isImage,
			 boolean isPDF, boolean isFont, boolean isSignature, int signatureType,
			 boolean isPrecoded) {
		this (content, binary, cid, type, comptype, urlID, mime, isParseable, isText, isImage);
		this.isPDF = isPDF;
		this.isFont = isFont;
		this.isSignature = isSignature;
		this.signatureType = signatureType;
		this.isPrecoded = isPrecoded;
	}
	
	@Override
	public String toString () {
		return "org.agnitas.backend.BlockData (id=" + id + ",contentID=" + cid + ",comptype=" + comptype +
			",urlID=" + urlID + ",targetID=" + targetID + ",mime=" + mime +
			",parsable?" + isParseable + ",text?" + isText + ",image?" + isImage + ",attachment?" + isAttachment +
			",pdf?" + isPDF + ",font?" + isFont + ",signature?" + isSignature + ",precoded?" + isPrecoded +
			",condition=" + condition + ")";
	}

	/**
	 * To make this class sortable
	 * @param other the block to compare us to
	 * @return the sort relation
	 */
	@Override
	public int compareTo (BlockData other) {
		int myType = norm (comptype);
		int otherType = norm (other.comptype);

		if (myType != otherType) {
			return myType - otherType;
		}
		return type - other.type;
	}

	/** Returns the next tagname found in the Block
	 *	The current position is stored in the private var current_pos
	 *	@return String with Name of the found tag
	 */
	public String getNextTag() throws Exception {
		// first return names from our precollection
		if (tagNames.size () > 0) {
			return tagNames.remove (0);
		}
		if (content == null) {
			return null;
		}

		// if this is the first time, cleanup all tags
		if (current_pos == 0) {
			cleanupTags();
		}

		int end_position;
		int new_position = getNextOpeningTag();
		if (new_position == -1) {
			return null;
		}
		end_position = endOfTag (current_pos);

		if (end_position == content.length ()) {
			throw new Exception ("Syntax error in tag name " + content.substring (current_pos));
		}

		String tagname = content.substring(current_pos, end_position + 1);
		current_pos += tagname.length();

		// store start and name
		TagPos tagpos = createTagPos(new_position, end_position, tagname);

		if (tagpos.isDynamic () && (! tagpos.isSimpleTag ())) {
			int depth = 1;
			int content_start = current_pos;
			int content_position = current_pos;

			do {
				int depth_position = content.indexOf ("[" + tagpos.getTagid () + " ", current_pos);

				content_position = content.indexOf ("[/" + tagpos.getTagid () + " ", current_pos);
				if ((depth_position != -1) && (depth_position < content_position)) {
					int depth_end = endOfTag (depth_position);

					if (depth_end != -1) {
						if (content.charAt (depth_end - 1) != '/')
							++depth;
						current_pos = depth_end + 1;
					}
				} else if (content_position == -1) {
					depth = 0;
					content_position = content.length ();
					current_pos = content_position;
				} else {
					--depth;
					current_pos = content_position + tagpos.getTagid ().length () + 3;
				}
			}	while (depth > 0);

			BlockData	cont = createSubBlock (content.substring (content_start, content_position));
			String		name;

			while ((name = cont.getNextTag()) != null) {
				tagNames.addElement (name);
			}
			tagpos.setContent (cont);
			if (((content_position = endOfTag (content_position)) != -1) && (content_position < content.length ())) {
				content_position++;
			} else {
				content_position = content.length ();
			}
			content = content.substring (0, content_start) + content.substring (content_position);
			current_pos = content_start;
		}
		tagPositions.add(tagpos);
		return tagname;
	}

	/** returns the CID as filename
	 * @return the string used for filenames
	 */
	public String getContentFilename () {
		String	c = cidEmit != null ? cidEmit : cid;
		
		if (isSignature) {
			return c + ".signature";
		}
		return c;
	}

	/** returns MIME type
	 * @return the string used for mime
	 */
	public String getContentMime () {
		return mimeEmit != null ? mimeEmit : mime;
	}

	/** returns the size of the content
	 * @return the content length
	 */
	public int length () {
		return content != null ? content.length () : 0;
	}

	/** returns the name of the media type
	 * @return the media type
	 */
	public String mediaType () {
		return Media.typeName (media);
	}

	public boolean isEmailHeader () {
		return (comptype == 0) && (type == HEADER);
	}
	public boolean isEmailPlaintext () {
		return (comptype == 0) && (type == TEXT);
	}
	public boolean isEmailHTML () {
		return ((comptype == 0) && (type == HTML));
	}
	public boolean isEmailText () {
		return isEmailHeader () || isEmailPlaintext () || isEmailHTML ();
	}
	public boolean isEmailAttachment () {
		return (type == ATTACHMENT_TEXT) || (type == ATTACHMENT_BINARY);
	}

	/** Create a new sub block
	 *
	 * @param nContent the new content for this subblock
	 * @return the new block
	 */
	private BlockData createSubBlock (String nContent) {
		BlockData	bd = new BlockData ();
		
		bd.content = nContent;
		bd.type = type;
		bd.comptype = comptype;
		bd.mime = mime;
		bd.isParseable = isParseable;
		bd.isText = isText;
		bd.isImage = isImage;
		bd.isPDF = isPDF;
		bd.isFont = isFont;
		bd.isSignature = isSignature;
		bd.isPrecoded = isPrecoded;
		return bd;
	}
	
	/**
	 * check for end of found tag
	 * @param start the start position of the found tag
	 * @return the end position
	 */
	private int endOfTag (int start) {
		int end = content.indexOf ("]", start);

		if (end == -1) {
			end = content.length ();
		}
		return end;
	}

	private TagPos createTagPos(int begin, int end, String tagname) {
		return new TagPos(begin, end, tagname);
	}

	private boolean matchOpeningTagName(int begin) {
		return content.startsWith("agn", begin) || content.startsWith("gridPH", begin);
	}

	private int getNextOpeningTag() {
		while ((current_pos = content.indexOf('[', current_pos)) != -1) {
			if (matchOpeningTagName(current_pos + 1)) {
				return current_pos;
			}
			current_pos++;
		}

		current_pos = 0;
		return -1;
	}

	private void cleanupTags() {
		int start, end = 0;

		while ((start = content.indexOf ("[", end)) != -1) {
			int cBegin = start + 1;

			if (content.startsWith("/", cBegin)) {
				cBegin++;
			}
			end = endOfTag (start);

			if (matchOpeningTagName(cBegin)) {
				for (int n = start; n < end; ++n) {
					char ch = content.charAt (n);
					int count = 0;

					while ((n + count + 1 < end) && Character.isWhitespace(ch)) {
						count++;
						ch = content.charAt (n + count);
					}
					if (count > 0) {
						content = content.substring (0, n) + ' ' + content.substring (n + count);
						end -= count - 1;
					}
				}
			}
		}
	}

	private int norm (int cType) {
		return cType == 5 ? 1 : cType;
	}
}
