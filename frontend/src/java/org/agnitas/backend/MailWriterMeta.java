/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.agnitas.backend.dao.MailkeyDAO;
import org.agnitas.util.Bit;
import org.agnitas.util.Log;
import org.agnitas.util.Str;
import org.agnitas.util.Systemconfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jakarta.mail.internet.MimeUtility;

/**
 * Implements writing of mailing information to
 * a XML file
 */
public class MailWriterMeta extends MailWriter {
	/**
	 * Write a log entry to the database after that number of mails
	 */
	private int logSize;
	/**
	 * Reference to available tagnames
	 */
	private Map<String, EMMTag> tagNames;
	/**
	 * Base pathname without extension to write to
	 */
	private String fname;
	/**
	 * The pathname for the real XML file
	 */
	private String pathname;
	/**
	 * Output stream
	 */
	private OutputStream out;
	/**
	 * Output buffer
	 */
	protected XMLWriter writer;
	/**
	 * Counter to give each block written an unique ID
	 */
	private int blockID;
	/**
	 * if we should keep admin/test mails for debug purpose
	 */
	private boolean keepATmails;
	private boolean keepPreviewMails;
	private List<Reference> multi;
	private List <MailkeyDAO.Mailkey> mailkeys;

	/**
	 * Constructor
	 *
	 * @param data      Reference to configuration
	 * @param allBlocks all content blocks
	 * @param nTagNames all tag definitions
	 */
	public MailWriterMeta(Data data, BlockCollection allBlocks, Map<String, EMMTag> nTagNames) throws Exception {
		super(data, allBlocks);
		logSize = 400;
		tagNames = nTagNames;
		fname = null;
		pathname = null;
		out = null;
		writer = null;
		if (data.maildropStatus.isAdminMailing () ||
		    data.maildropStatus.isTestMailing () ||
		    data.maildropStatus.isCampaignMailing () ||
		    data.maildropStatus.isVerificationMailing () ||
		    data.maildropStatus.isPreviewMailing ()) {
			blockSize = 0;
		} else {
			blockSize = data.mailing.blockSize();
		}
		blockID = 1;
		multi = null;
		mailkeys = null;
		setup();

		keepATmails = Str.atob(data.company.info("keep-xml-files"), false);
		keepPreviewMails = Str.atob (data.company.info ("keep-preview-files"), false);
	}

	/**
	 * Cleanup
	 */
	@Override
	public void done() throws Exception {
		super.done();
		if (data.maildropStatus.isAdminMailing() || data.maildropStatus.isTestMailing()) {
			if (pathname != null) {
				if (!keepATmails) {
					data.markToRemove(pathname);
				}

				String gen = generateOutputOptions();

				startXMLBack(null, gen, pathname);
				if (!keepATmails) {
					if ((new File(pathname)).delete()) {
						data.unmarkToRemove(pathname);
					}
				}
			}
		} else if (data.maildropStatus.isPreviewMailing()) {
			if (pathname != null) {
				File output = File.createTempFile("preview", ".xml");
				String path = output.getAbsolutePath();
				String opts = previewOutputOptions(path);
				String error = null;

				if (! keepPreviewMails) {
					data.markToRemove(pathname);
				}
				data.markToRemove(path);
				try {
					List<String> options = new ArrayList<>();

					previewOptions(options);
					startXMLBack(options, opts, pathname);
				} catch (Exception e) {
					error = e.toString();
				}
				if (data.previewOutput != null) {
					if (output.exists() && (output.length() > 0)) {
						try {
							DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
							DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
							Document doc = docBuilder.parse(output);
							Element root = doc.getDocumentElement();
							NodeList nlist = root.getElementsByTagName("content");
							int ncount = nlist.getLength();

							for (int n = 0; n < ncount; ++n) {
								Node node = nlist.item(n);
								NamedNodeMap attr = node.getAttributes();
								Node name = attr.getNamedItem("name");

								if (name != null) {
									Node text = node.getFirstChild();

									data.previewOutput.addContent(name.getNodeValue(), (text == null ? "" : text.getNodeValue()));
								}
							}
						} catch (Exception e) {
							if (error != null) {
								error += "\n" + e.toString();
							} else {
								error = e.toString();
							}
						}
					}
					if (error != null) {
						data.previewOutput.setError(error);
					}
				}
				if ((new File(path)).delete()) {
					data.unmarkToRemove(path);
				}
				if (! keepPreviewMails) {
					if ((new File(pathname)).delete()) {
						data.unmarkToRemove(pathname);
					}
				}
			}
		} else if (fname != null) {
			try (FileOutputStream temp = new FileOutputStream(fname + ".final")) {
				String msg = getFinalMessage();
				try {
					temp.write(msg.getBytes(StandardCharsets.UTF_8));
				} catch (UnsupportedEncodingException e) {
					temp.write(msg.getBytes());
				}
			} catch (FileNotFoundException e) {
				throw new IOException("Unable to write final stamp file " + fname + ".final: " + e.toString(), e);
			}
		}
	}

	/**
	 * Start writing a new block
	 */
	@Override
	public void startBlock() throws Exception {
		super.startBlock();
		fname = Str.makePath(data.targetPath(), filenamePattern);
		if (data.maildropStatus.isAdminMailing() || data.maildropStatus.isTestMailing()) {
			pathname = fname + ".xml";
			out = new FileOutputStream(pathname);
		} else {
			pathname = fname + ".xml.gz";
			out = new GZIPOutputStream(new FileOutputStream(pathname));
		}
		writer = new XMLWriter(out);
		writer.start();
		writer.opennode("blockmail");
		writer.opennode("description");
		emitDescription();
		writer.close("description");
		writer.empty();
		writer.opennode("general");
		generalHeaders();
		generalURLs();
		secrets();
		writer.single("total_subscribers", data.totalSubscribers);
		if (data.company.mailtracking()) {
			writer.single("mailtracking", data.company.mailtrackingExtended() ? "extended" : "standard");
		}
		writer.close("general");
		writer.empty();
		writer.opennode("mailcreation");
		writer.single("blocknr", blockCount);
		writer.single("innerboundary", innerBoundary);
		writer.single("outerboundary", outerBoundary);
		writer.single("attachboundary", attachBoundary);
		writer.close("mailcreation");
		writer.empty();
		writer.cflush();

		writer.open(data.media().size() == 0, "mediatypes", "count", data.media().size());
		if (data.media().size() > 0) {
			for (Media tmp : data.media()) {
				List<String> vars = tmp.getParameterVariables();
				boolean hasVars = vars.size() > 0;

				writer.open(!hasVars, "media", "type", tmp.typeName(), "priority", tmp.priorityName(), "status", tmp.statusName());
				if (vars != null && vars.size() > 0) {
					for (int m = 0; m < vars.size(); ++m) {
						String name = vars.get(m);
						String value = tmp.findParameterValue(name);

						writer.opennode("variable", "name", name);
						writer.single("value", value);
						writer.close("variable");
					}
					writer.close("media");
				}
			}
			writer.close("mediatypes");
		}
		writer.empty();
		writer.cflush();
		tracker();

		writer.opennode("blocks", "count", allBlocks.getTotalNumberOfBlocks());
		for (int n = 0; n < allBlocks.getTotalNumberOfBlocks(); ++n) {
			BlockData b = allBlocks.getBlock(n);

			emitBlock(b, b.type == BlockData.HEADER ? 1 : 0, n);
		}
		writer.close("blocks");
		writer.empty();

		writer.opennode("types", "count", 3);
		for (int n = 0; n < 3; ++n) {
			writer.opennode("type", "id", n);

			List<BlockData> use = new ArrayList<>();
			int used, part, text;

			part = -1;
			text = -1;
			for (int blocknr = 0; blocknr < allBlocks.blockCount(); ++blocknr) {
				BlockData b = allBlocks.getBlock(blocknr);
				boolean doit = b.isEmailHeader() || b.isEmailAttachment();

				if (!doit) {
					switch (n) {
						case 0:
							doit = b.isEmailPlaintext();
							break;
						case 1:
							doit = b.isEmailText();
							break;
						case 2:
							doit = true;
							break;
						default:
							break;
					}
				}
				if (doit) {
					if ((part == -1) && b.isEmailAttachment()) {
						part = use.size() - 1;
					}
					if (b.isEmailText()) {
						text = use.size();
					}
					use.add(b);
				}
			}
			used = use.size();
			if (part == -1) {
				part = used;
			}
			if (text == -1) {
				text = used;
			}
			for (int m = 0; m < used; ++m) {
				BlockData b = use.get(m);
				XMLWriter.Creator c = writer.create("blockspec", "nr", b.id);

				if (b.isEmailPlaintext() && (data.lineLength > 0)) {
					c.add("linelength", data.lineLength);
				} else if (b.isEmailHTML()) {
					if (data.onepixlog != Data.OPL_NONE) {
						String opl;

						switch (data.onepixlog) {
							default:
								opl = null;
								break;
							case Data.OPL_TOP:
								opl = "top";
								break;
							case Data.OPL_BOTTOM:
								opl = "bottom";
								break;
						}
						c.add("onepixlog", opl);
					}
					if (data.requiresClearance && data.maildropStatus.isTestMailing ()) {
						c.add ("clearance", "true");
					}
				}
				writer.opennode(c);
				if (b.comptype == 0) {
					if (b.type == BlockData.HEADER) {
						writer.opennode ("postfix", "output", 0);
						writer.opennode ("fixdata", "valid", "simple");
						if (n == 0) {		// simple text mail
							writer.data ("HContent-Type: text/plain; charset=\"" + data.mailing.charset () + "\"\n" + 
								     "HContent-Transfer-Encoding: " + data.mailing.encoding () + "\n");
						} else if (n == 1) {	// online HTML
							writer.data ("HContent-Type: multipart/alternative;\n" + 
								     "\tboundary=\"" + outerBoundary + "\"\n");
						} else {		// offline HTML
							writer.data ("HContent-Type: multipart/related;\n" + 
								     "\tboundary=\"" + outerBoundary + "\"\n");
						}
						writer.data (".\n");
						writer.close ("fixdata");
						writer.opennode ("fixdata", "valid", "attach");
						writer.data ("HContent-Type: multipart/mixed; boundary=\"" + attachBoundary +"\"\n" +
							     ".\n");
						writer.close ("fixdata");
						writer.close ("postfix");
					} else if (b.type == BlockData.TEXT) {
						writer.opennode ("prefix");
						if (n > 0) {
							writer.opennode ("fixdata", "valid", "simple");
							writer.data ("This is a multi-part message in MIME format.\n\n" +
								     "--" + outerBoundary + "\n");
							if (n == 2) {
								writer.data ("Content-Type: multipart/alternative;\n" +
									     "\tboundary=\"" + innerBoundary + "\"\n\n" +
									     "--" + innerBoundary + "\n");
							}
							writer.data ("Content-Type: text/plain; charset=\"" + data.mailing.charset () + "\"\n" +
								     "Content-Transfer-Encoding: " + data.mailing.encoding () + "\n\n");
							writer.close ("fixdata");
						}
						writer.opennode ("fixdata", "valid", "attach");
						writer.data ("--" + attachBoundary + "\n");
						if (n == 0) {
							writer.data ("Content-Type: text/plain; charset=\"" +  data.mailing.charset () + "\"\n" +
								     "Content-Transfer-Encoding: " + data.mailing.encoding () + "\n\n");
						} else if (n == 1) {
							writer.data ("Content-Type: multipart/alternative;\n" +
								     "\tboundary=\"" + outerBoundary + "\"\n\n");
						} else {
							writer.data ("Content-Type: multipart/related;\n" +
								     "\tboundary=\"" + outerBoundary + "\"\n\n");
						}
						if (n > 0) {
							writer.data ("--" + outerBoundary + "\n");
							if (n == 2) {
								writer.data ("Content-Type: multipart/alternative;\n" +
									     "\tboundary=\"" + innerBoundary + "\"\n\n" +
									     "--" + innerBoundary + "\n");
							}
							writer.data ("Content-Type: text/plain; charset=\"" + data.mailing.charset () + "\"\n" +
								     "Content-Transfer-Encoding: " + data.mailing.encoding () + "\n\n");
						}
						writer.close ("fixdata");
						writer.close ("prefix");
						if (n == 2) {
							writer.opennode ("postfix", "output", text, "pid", "inner");
							writer.opennode ("fixdata", "valid", "all");
							writer.data ("--" + innerBoundary + "--\n\n");
							writer.close ("fixdata");
							writer.close ("postfix");
						}
						if (n > 0) {
							writer.opennode ("postfix", "output", part, "pid", "outer");
							writer.opennode ("fixdata", "valid", "all");
							writer.data ("--" + outerBoundary + "--\n\n");
							writer.close ("fixdata");
							writer.close ("postfix");
						}
						writer.opennode ("postfix", "output", allBlocks.getTotalNumberOfBlocks (), "pid", "attach");
						writer.opennode ("fixdata", "valid","attach");
						writer.data ("--" + attachBoundary + "--\n\n");
						writer.close ("fixdata");
						writer.close ("postfix");
					} else if (b.type == BlockData.HTML) {
						writer.opennode("prefix");
						writer.opennode("fixdata", "valid", "all");
						if (n == 1) {
							writer.data("--" + outerBoundary + "\n");
						} else {
							writer.data("--" + innerBoundary + "\n");
						}
						writer.data ("Content-Type: " + b.getContentMime () + "; charset=\"" + data.mailing.charset () + "\"\n" +
							     "Content-Transfer-Encoding: " + getTransferEncoding (b) + "\n\n");
						writer.close ("fixdata");
						writer.close ("prefix");
					}
				} else {        // offline + attachments
					writer.opennode("prefix");
					if ((b.type == BlockData.ATTACHMENT_TEXT) || (b.type == BlockData.ATTACHMENT_BINARY)) {
						writer.opennode ("fixdata", "valid", "attach");
						writer.data ("--" + attachBoundary + "\n" +
							     "Content-Type: " + b.getContentMime () + "\n" +
							     "Content-Disposition: attachment; filename=\"" + encode (b.getContentFilename ()) + "\"\n" +
							     "Content-Transfer-Encoding: " + getTransferEncoding (b) + "\n\n");
						writer.close ("fixdata");
					} else {
						writer.opennode ("fixdata", "valid", "all");
						writer.data ("--" + outerBoundary + "\n" +
							     "Content-Type: " + b.getContentMime () + "\n" +
							     "Content-Transfer-Encoding: " + getTransferEncoding (b) + "\n" +
							     "Content-Location: " + encode (b.getContentFilename ()) + "\n\n");
						writer.close ("fixdata");
					}
					writer.close ("prefix");
					if ((b.type == BlockData.ATTACHMENT_TEXT) || (b.type == BlockData.ATTACHMENT_BINARY)) {
						writer.opennode ("postfix", "output", allBlocks.getTotalNumberOfBlocks (), "pid", "attach");
						writer.opennode ("fixdata", "valid", "attach");
						writer.data ("--" + attachBoundary + "--\n\n");
						writer.close ("fixdata");
						writer.close ("postfix");
					} else {
						writer.opennode ("postfix", "output", part, "pid", "outer");
						writer.opennode ("fixdata", "valid", "all");
						writer.data ("--" + outerBoundary + "--\n\n");
						writer.close ("fixdata");
						writer.close ("postfix");
					}
				}
				writer.close(c.name);
			}
			writer.close("type");
		}
		writer.close("types");
		writer.empty();
		writer.cflush();

		layout();

		boolean found;

		found = false;
		for (EMMTag tag : tagNames.values()) {
			if (!found) {
				writer.opennode("taglist", "count", tagNames.size());
				found = true;
			}
			writer.openclose("tag", "name", tag.mTagFullname, "hash", tag.mTagFullname.hashCode());
		}
		if (found) {
			writer.close("taglist");
		} else {
			writer.comment("no taglist");
		}
		writer.empty();
		writer.cflush();

		found = false;
		for (EMMTag tag : tagNames.values()) {
			String ttype = tag.getType();
			String value;

			switch (tag.tagType) {
				case EMMTag.TAG_INTERNAL:
					if (tag.globalValue) {
						if (!found) {
							writer.opennode("global_tags");
							found = true;
						}
						XMLWriter.Creator c = writer.create("tag", "name", tag.mTagFullname, "hash", tag.mTagFullname.hashCode());

						c.add("type", ttype);
						if ((value = tag.makeInternalValue(data, null)) != null) {
							writer.opennode(c);
							writer.data(value);
							writer.close(c.name);
						} else {
							writer.openclose(c);
						}
					}
					break;
				default:
					break;
			}
		}
		if (found) {
			writer.close("global_tags");
		} else {
			writer.comment("no global_tags");
		}
		writer.empty();
		writer.cflush();

		dynamics();
		writer.empty();
		writer.cflush();

		urls();
		writer.empty();
		writer.cflush();
		
		virtuals();
		writer.empty();
		writer.cflush();
		
		writer.opennode("receivers");
	}

	protected void generalHeaders() {
		writer.single("domain", data.mailing.domain());
		writer.single("subject", data.mailing.subject());
		writer.single("from_email", data.mailing.getFromEmailFull());
	}

	/**
	 * Finalize a block
	 */
	@Override
	public void endBlock() throws Exception {
		super.endBlock();
		if (out != null) {
			writer.close("receivers");
			writer.close("blockmail");
			writer.flush();
			writer.end();
			writer = null;
			out.close();
			out = null;

			if (data.xmlValidate()) {
				data.logging(Log.INFO, "writer/meta", "Validating XML output");
				startXMLBack(null, "none", pathname);
				data.logging(Log.INFO, "writer/meta", "Validation done");
			} else {
				data.logging(Log.INFO, "writer/meta", "Skip validation of XML document");
			}

			if (!(data.maildropStatus.isAdminMailing() || data.maildropStatus.isTestMailing() || data.maildropStatus.isPreviewMailing())) {
				try (FileOutputStream temp = new FileOutputStream(fname + ".stamp")) {
					String msg = getStampMessage();
					try {
						temp.write(msg.getBytes(StandardCharsets.UTF_8));
					} catch (UnsupportedEncodingException e) {
						temp.write(msg.getBytes());
					}
				} catch (FileNotFoundException e) {
					throw new IOException("Unable to write stamp file " + fname + ".stamp: " + e.toString());
				}
			}
		}
	}

	@Override
	public void writeMailDone() throws Exception {
		if (pending) {
			writer.close("receiver");
			writer.cflush();
			if ((data.mailing.maxBytesPerOutputFile() > 0) && (writer.outputSize() > data.mailing.maxBytesPerOutputFile()) && (data.maildropStatus.isWorldMailing() || data.maildropStatus.isRuleMailing() || data.maildropStatus.isOnDemandMailing())) {
				data.forceNewBlock = true;
				data.logging(Log.VERBOSE, "write/meta", "Force new block due to current file size of " + writer.outputSize() + " exceeding limit of " + data.mailing.maxBytesPerOutputFile());
			}
		}
		super.writeMailDone();
	}

	/**
	 * Write a single receiver record
	 *
	 * @param cinfo             Information about the customer
	 * @param mcount            if more than one mail is written for this receiver
	 * @param mailtype          the mailtype for this receiver
	 * @param icustomer_id      the customer ID
	 * @param tagNamesParameter the available tags
	 */
	@Override
	public void writeMail(Custinfo cinfo,
			      int mcount, int mailtype, long icustomer_id,
			      String mediatypes, String userStatuses, Map<String,
			      EMMTag> tagNamesParameter) throws Exception {
		super.writeMail(cinfo, mcount, mailtype, icustomer_id, mediatypes, userStatuses, tagNamesParameter);
		if ((mailCount % 100) == 0) {
			data.logging(Log.VERBOSE, "writer/meta", "Currently at " + mailCount + " mails (in block " + blockCount + ": " + inBlockCount + ", records: " + inRecordCount + ") ");
		}
		if ((backendLog != null) && (logSize > 0) && ((mailCount % logSize) == 0)) {
			try {
				backendLog.update(data.dbase, mailCount, data.totalReceivers);
			} catch (Exception e) {
				data.logging(Log.WARNING, "writer/meta", "Failed to write current state to database, assume next try to success: " + e.toString());
			}
		}

		XMLWriter.Creator c = writer.create("receiver", "customer_id", icustomer_id, "user_type", cinfo.getUserType());

		getMediaInformation(cinfo, c);
		c.add("mailtype", mailtype);
		c.add("mediatypes", mediatypes);
		c.add("user_status", userStatuses);

		if (icustomer_id > 0) {
			//
			// Add Bcc only for non sample receviers
			List<String> bcc = data.bcc();

			if ((bcc != null) && (bcc.size() > 0)) {
				c.add("bcc", bcc.stream().map((s) -> Str.punycodeEMail(s.trim())).filter((s) -> s.length() > 0).reduce((s, e) -> s + "," + e).orElse(null));
			}
		}

		writer.opennode(c);
		if (icustomer_id != 0) {
			++totalMails;
		}
	}

	@Override
	public void writeContent(Custinfo cinfo, long icustomer_id, Map<String, EMMTag> tagNamesParameter, Column[] rmap) throws Exception {
		super.writeContent(cinfo, icustomer_id, tagNamesParameter, rmap);

		next(rmap);
		writer.opennode("tags");

		for (EMMTag tag : tagNamesParameter.values()) {
			String value;

			switch (tag.tagType) {
				case EMMTag.TAG_DBASE:
					if (!tag.globalValue) {
						value = tag.getTagValue();
					} else {
						value = null;
					}
					break;
				case EMMTag.TAG_INTERNAL:
					if (!tag.globalValue) {
						value = tag.makeInternalValue(data, cinfo);
					} else {
						value = null;
					}
					break;
				default:
					throw new Exception("Invalid tag type: " + tag.toString());
			}
			if (value != null) {
				writer.opennode("tag", "name", tag.mTagFullname, "hash", tag.mTagFullname.hashCode());
				writer.data(value);
				writer.close("tag");
			}
		}
		writer.close("tags");

		if (data.lusecount > 0) {
			for (int n = 0; n < data.lcount; ++n) {
				if (data.columnUse(n)) {
					if (data.columnIsNull(n)) {
						writer.opennode("data", "null", true);
					} else {
						writer.opennode("data");
					}
					writer.data(data.columnGetStr(n));
					writer.close("data");
				}
			}
		}
		String targetGroupValue = cinfo.getTargetGroupValue();
		if (targetGroupValue != null) {
			writer.single("target_group", targetGroupValue);
		}
	}

	private void setup() {
		if (data.references != null) {
			boolean hasMulti = false;

			for (Reference r : data.references.values()) {
				if (r.isMulti()) {
					hasMulti = true;
					break;
				}
			}
			if (hasMulti) {
				int[] indexes = new int[data.lcount];
				int pos = 0;

				for (Reference r : data.references.values()) {
					if (multi == null) {
						multi = new ArrayList<>();
					}
					multi.add(r);
					for (int n = 0, inuse = 0; n < data.lcount; ++n) {
						Column c = data.columnByIndex(n);

						if (c.getInuse()) {
							if ((c.getRef() != null) && r.name().equals(c.getRef())) {
								indexes[pos++] = inuse;
							}
							++inuse;
						}
					}
				}
			}
		}
	}

	private List<String> escape(List<String> input) {
		List<String> rc = new ArrayList<>(input.size());

		for (int n = 0; n < input.size(); ++n) {
			rc.add(input.get(n).replace("\\", "\\\\"));
		}
		return rc;
	}

	private String encode(String s) {
		try {
			return MimeUtility.encodeText(s, "UTF-8", "Q");
		} catch (UnsupportedEncodingException e) {
			return s;
		}
	}

	/**
	 * Start the mail generating backend
	 *
	 * @param output   detailed output description
	 * @param filename pathname to the XML file
	 */
	private void startXMLBack(List<String> options, String output, String filename) throws Exception {
		List<String> command = new ArrayList<>();
		String cmd;
		File efile;
		int rc;

		try {
			efile = File.createTempFile("error", null);
		} catch (Exception e) {
			String javaTemp = System.getProperty("java.io.tmpdir");
			data.logging(Log.ERROR, "write/meta", "Failed to create temp.file due to: " + e.toString() + " (missing temp.directory '" + javaTemp + "'?)", e);
			throw e;
		}

		command.add(data.xmlBack());
		if (options != null) {
			for (int n = 0; n < options.size(); ++n) {
				command.add(options.get(n));
			}
		}
		command.add("-q");
		command.add("-l");
		command.add("-E" + efile.getAbsolutePath());
		command.add("-o" + output);
		command.add(filename);
		cmd = command.toString();
		data.markToRemove(efile);
		try {
			data.logging(Log.DEBUG, "write/meta", "Try to execute " + cmd);

			ProcessBuilder bp = new ProcessBuilder(escape(command));
			Process proc = bp.start();

			try (OutputStream otemp = proc.getOutputStream()) {
				otemp.flush();
			} catch (IOException e) {
				data.logging(Log.VERBOSE, "write/meta", "Failed to close stdin for " + cmd + ": " + e.toString());
			}
			rc = proc.waitFor();
			try (InputStream itemp = proc.getInputStream()) {
				itemp.markSupported();
				// do nothing
			} catch (IOException e) {
				data.logging(Log.VERBOSE, "write/meta", "Failed to close stdout for " + cmd + ": " + e.toString());
			}

			try (InputStream itemp = proc.getErrorStream()) {
				itemp.markSupported();
				// do nothing
			} catch (IOException e) {
				data.logging(Log.VERBOSE, "write/meta", "Failed to close stderr for " + cmd + ": " + e.toString());
			}

			String msg = null;

			try (FileInputStream err = new FileInputStream(efile)) {
				int size = err.available();
				if (size > 0) {
					int use = size > 4096 ? 4096 : size;
					byte[] buf = new byte[use];

					err.read(buf);
					msg = new String(buf);
				}
			} catch (IOException e) {
				if (!(e instanceof FileNotFoundException)) {
					data.logging(Log.ERROR, "writer/meta", "Failed to read error output: " + e.toString(), e);
				}
				msg = null;
			}
			if ((rc != 0) || (msg != null)) {
				data.logging(rc == 0 ? Log.INFO : Log.ERROR, "writer/meta", "command " + cmd + " returns " + rc + (msg != null ? ":\n" + msg : ""));
				if (rc != 0) {
					throw new Exception("command returns " + rc);
				}
			}
		} catch (Exception e) {
			data.logging(Log.ERROR, "writer/meta", "command " + cmd + " failed (Missing binary? Wrong permissions?): " + e.toString(), e);
			throw new Exception("Execution of " + cmd + " failed: " + e.toString(), e);
		} finally {
			if (efile.delete()) {
				data.unmarkToRemove(efile);
			}
		}
	}

	private void addGenerateMediaOptions(List<String> opts, String mta) {
		opts.add("media=email");
		if ((mta != null) && "spool".equals (mta)) {
			opts.add("path=" + data.mailing.outputDirectoryForCompany ("mailer"));
		} else {
			if ((mta != null) && (! "postfix".equals (mta))) {
				data.logging(Log.WARNING, "writer/meta", "Unsupported MTA \"" + mta + "\", will use default behaviour using sendmail like calling interface");
			}
			opts.add("inject=/usr/sbin/sendmail -NNEVER -f %(sender) -- %(recipient)");
		}
		for (Media m : data.media()) {
			if ((m.type != Media.TYPE_EMAIL) && Bit.isset(data.availableMedias, m.type)) {
				opts.add("media=" + m.typeName());
				String path = null;
				switch (m.type) {
					case Media.TYPE_FAX:
						path = data.mailing.outputDirectoryForCompany("fax");
						break;
					case Media.TYPE_PRINT:
						path = data.mailing.outputDirectoryForCompany("print");
						break;
					case Media.TYPE_SMS:
						path = data.mailing.outputDirectoryForCompany("sms");
						break;
					default:
						break;
				}
				if (path != null) {
					opts.add("path=" + path);
				}
			}
		}
	}

	/**
	 * Create xmlback generation string
	 *
	 * @return the newly formed string
	 */
	private String generateOutputOptions() {
		List<String> opts = new ArrayList<>();
		String mta = data.mailing.messageTransferAgent();

		opts.add("temporary=true");
		opts.add("account-logfile=" + data.mailing.accountLogfile());
		opts.add("bounce-logfile=" + data.mailing.bounceLogfile());
		opts.add("mailtrack-logfile=" + data.mailing.mailtrackLogfile());
		addGenerateMediaOptions(opts, mta);
		return "generate:" + String.join(";", opts);
	}

	private String previewOutputOptions(String output) {
		return "preview:path=" + output;
	}

	private void previewOptions(List<String> options) {
		options.add("-r");
		if (data.previewAnon) {
			options.add("-a");
			if (data.previewAnonPreserveLinks) {
				options.add ("-A");
			}
		}
		if (data.previewSelector != null) {
			options.add("-s" + data.previewSelector);
		}
		if (!data.previewCachable) {
			options.add("-unc");
		}
		if (data.previewConvertEntities) {
			options.add("-e");
		}
		if (data.previewEcsUIDs) {
			options.add("-g");
		}
		if ((data.previewTargetIDs != null) && (data.previewTargetIDs.length > 0)) {
			for (long targetID : data.previewTargetIDs) {
				options.add("-t" + targetID);
			}
		}
	}

	private String getStampMessage () {
		return	"Licence-ID: " + data.licenceID () + "\n" +
			"Company-ID: " + data.company.id () + "\n" +
			"Company-Name: " + data.company.name () + "\n" +
			"Mailinglist-ID: " + data.mailinglist.id () + "\n" +
			"Mailinglist-Name: " + data.mailinglist.name () + "\n" +
			"Mailing-ID: " + data.mailing.id () + "\n" +
			"Mailing-Name: " + data.mailing.name () + "\n" +
			"Subscriber-Count: " + data.totalSubscribers + "\n" +
			"Receiver-Count: " + data.totalReceivers + "\n" +
			"Count: " + inBlockCount + "\n" +
			"Origin: " + Systemconfig.fqdn + "\n";
	}

	private String getFinalMessage() {
		return data.company.id() + "-" + data.mailing.id() + "-" + blockCount + "\t" + "Start: " + startExecutionTime + "\tEnd: " + endExecutionTime + "\n";
	}

	/**
	 * Get encoding for block
	 *
	 * @param b the block to examine
	 * @return the encoding for this block
	 */
	private String getEncoding(BlockData b) {
		String encode;

		if (b.isText) {
			if (b.isPDF) {
				encode = "base64";
			} else if (b.media == Media.TYPE_EMAIL) {
				encode = data.mailing.encoding();
			} else {
				encode = "none";
			}
		} else if (b.isFont) {
			encode = "none";
		} else {
			encode = "base64";
		}
		return encode;
	}

	/**
	 * Write entry part for a single block
	 *
	 * @param b      the block to write
	 * @param encode the encoding for this block
	 */
	private void emitBlockEntry(BlockData b, XMLWriter.Creator c, String encode) {
		String flag;

		if (b.mime != null) {
			c.add("mimetype", b.getContentMime());
		}
		c.add("charset", data.mailing.charset(), "encode", encode);
		c.add("cid", b.getContentFilename());
		if (b.isParseable) {
			flag = "is_parsable";
		} else if (b.isText) {
			flag = "is_text";
		} else {
			flag = "is_binary";
		}
		c.add(flag, true);
		c.addIfTrue("is_attachment", b.isAttachment);
		if (b.media != Media.TYPE_UNRELATED) {
			c.add("media", b.mediaType());
		}
		if (b.targetID > 0) {
			c.add("target_id", b.targetID);
			c.add("condition", b.condition);
		}
		c.addIfTrue("is_precoded", ((b.type == BlockData.RELATED_BINARY) || (b.type == BlockData.ATTACHMENT_BINARY)) && b.isParseable);
		c.addIfTrue("is_pdf", b.isPDF);
		c.addIfTrue("is_font", b.isFont);
	}

	/**
	 * Write content part for a single block
	 *
	 * @param b the block to write
	 */
	private void emitBlockContent(BlockData b) {
		writer.opennode("content");
		if ((b.content != null) && (b.content.length () > 0)) {
			writer.data (b.content);
		} else if (b.binary != null) {
			writer.data (b.binary);
		} else {
			writer.data ("");
		}
		writer.close("content");
		if ((b.binary != null) && (b.binary.length > 3)) {
			if (b.isPDF) {
				writer.opennode("background");
				writer.data(b.binary);
				writer.close("background");
			} else if (b.isFont) {
				writer.opennode("fontdata");
				writer.data(b.binary);
				writer.close("fontdata");
			}
		}
	}

	/**
	 * Write blocks tag information
	 * param b the block to write
	 */
	private void emitBlockTags(BlockData b, int isHeader) throws IOException {
		if (b.isParseable && (b.tagPositions != null)) {
			List<TagPos> p = b.tagPositions;
			int count = p.size();

			for (int m = 0; m < count; ++m) {
				TagPos tp = p.get(m);
				int type;
				XMLWriter.Creator c = writer.create("tagposition", "name", tp.getTagname(), "hash", tp.getTagname().hashCode());

				type = 0;
				if (tp.isDynamic()) {
					type |= 0x1;
				}
				if (tp.isDynamicValue()) {
					type |= 0x2;
				}
				if (type != 0) {
					c.add("type", type);
				}
				if (tp.getContent() != null) {
					writer.opennode(c);
					emitBlock(tp.getContent(), (isHeader != 0 ? isHeader + 1 : 0), 0);
					writer.close(c.name);
				} else {
					writer.openclose(c);
				}
			}
		}
	}

	/**
	 * Write a single block
	 *
	 * @param b        the block to write
	 * @param isHeader if this is the header block
	 * @param index    the unique block number
	 */
	private void emitBlock(BlockData b, int isHeader, int index) throws IOException {
		String encode;

		if (isHeader != 0) {
			encode = "header";
		} else {
			encode = getEncoding(b);
		}
		XMLWriter.Creator c = writer.create("block", "id", blockID++, "nr", index);

		emitBlockEntry(b, c, encode);
		writer.opennode(c);
		emitBlockContent(b);
		emitBlockTags(b, isHeader);
		writer.close(c.name);
		writer.cflush();
	}

	/**
	 * Write description entity
	 */
	private void emitDescription() {
		writer.openclose("licence", "id", data.licenceID());
		XMLWriter.Creator c = writer.create ("company", "id", data.company.id (), "name", data.company.name ());
		String token = data.company.token ();
		
		if (token != null) {
			c.add ("token", token);
		}
		if (data.company.allowUnnormalizedEmails ()) {
			c.add ("allow_unnormalized_emails", "true");
		}
		if (data.company.infoAvailable ()) {
			writer.opennode (c);
			for (String name : data.company.infoKeys()) {
				String value = data.company.infoValue(name);

				writer.single("info", value, "name", name);
			}
			writer.close("company");
		} else {
			writer.openclose (c);
		}
		mailingInfo();
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date sendDate = data.genericSendDate();

		writer.openclose("send", "date", fmt.format(sendDate), "epoch", (sendDate.getTime () / 1000));

		try {
			if (mailkeys == null) {
				mailkeys = (new MailkeyDAO (
							    data.dbase, data.company.id (),
							    Str.atob (data.company.info ("dkim-extended", data.mailing.id ()), false) && (data.dkimAvailable != null) && (data.dkimAvailable.size () > 0) ? data.dkimAvailable : null
				)).mailkeys ();
			}
			if ((mailkeys != null) && (mailkeys.size () > 0)) {
				writer.opennode ("mailkeys");
				for (int state = 0; state < 2; ++state) {
					boolean	local = state == 0;

					for (MailkeyDAO.Mailkey mailkey : mailkeys) {
						if (mailkey.local () == local) {
							XMLWriter.Creator	cr = writer.create ("key", "id", mailkey.id (), "method", mailkey.method ());
						
							for (Entry <String, String> entry : mailkey.parameter ().entrySet ()) {
								cr.add (entry.getKey (), entry.getValue ());
							}
							writer.single (cr, mailkey.key ());    
						}
					}
				}
				writer.close ("mailkeys");
			}
		} catch (SQLException e) {
			data.logging (Log.ERROR, "writer/meta", "Failed to retrieve keys: " + e.toString ());
		}
	}

	protected void mailingInfo() {
		writer.openclose("mailinglist", "id", data.mailinglist.id(), "name", data.mailinglist.name ());
		writer.open(data.mailingInfo == null, "mailing", "id", data.mailing.id(), "name", data.mailing.name());
		if (data.mailingInfo != null) {
			for (String name : data.mailingInfo.keySet()) {
				String value = data.mailingInfo.get(name);

				writer.single("info", value, "name", name);
			}
			writer.close("mailing");
		}
		if (data.mailing.description () != null) {
			writer.single ("mailing-description", data.mailing.description ());
		}
		writer.openclose("maildrop", "status_id", data.maildropStatus.id());
		writer.openclose("status", "field", data.maildropStatus.statusField());
	}

	/**
	 * Get transfer encoding
	 *
	 * @param b the block to emit
	 * @return the encoding
	 */
	private String getTransferEncoding(BlockData b) {
		return b.isText && (!b.isPDF) ? data.mailing.encoding() : "base64";
	}

	private void getDynamicInfo(DynName dn, XMLWriter.Creator c) {
		c.add("interest", dn.interest);
		c.addIfTrue("disable_link_extension", dn.disableLinkExtension);
	}

	private void generalURLs() {
		writer.single("auto_url", data.autoURL);
		writer.single("onepixel_url", data.onePixelURL);
		if (data.honeyPotURL != null) {
			writer.single("honeypot_url", data.honeyPotURL);
		}
		writer.single("anon_url", data.anonURL);
	}

	private void secrets() {
		if (data.company.secretKey() != null) {
			writer.single("uid_version", data.company.uidVersion());
			writer.single("secret_key", data.company.secretKey());
			writer.single("secret_timestamp", data.company.secretTimestamp());
		}
	}

	private void tracker() throws IOException {
		if (data.tracker != null) {
			writer.opennode("trackers");
			for (Tracker track : data.tracker) {
				writer.single("tracker", track.getCode(), "name", track.getName());
			}
			writer.close("trackers");
			writer.empty();
			writer.cflush();
		}
	}

	@SuppressWarnings("unused")
	private void layout() throws IOException {
		if (data.lusecount > 0) {
			writer.opennode("layout", "count", data.lusecount);
			for (int n = 0; n < data.lcount; ++n) {
				Column c = data.columnByIndex(n);

				if (c.getInuse()) {
					XMLWriter.Creator cr = writer.create("element", "name", c.getName());

					cr.add("ref", c.getRef());
					cr.add("type", c.typeStr());
					writer.openclose(cr);
				}
			}
			writer.close("layout");
		} else {
			writer.comment("no layout");
		}
		if (multi != null) {
			writer.opennode("references", "count", multi.size(), "id_column", Reference.multiID);
			for (Reference r : multi) {
				String columns = null;

				for (int n = 0, inuse = 0; n < data.lcount; ++n) {
					Column c = data.columnByIndex(n);

					if (c.getInuse()) {
						if ((c.getRef() != null) && r.name().equals(c.getRef())) {
							if (columns == null) {
								columns = c.getName();
							} else {
								columns += "," + c.getName();
							}
						}
						++inuse;
					}
				}
				if (columns != null) {
					writer.single("reference", columns, "name", r.name(), "table", r.table());
				} else {
					writer.openclose("reference", "name", r.name(), "table", r.table());
				}
			}
			writer.close("references");
		}
		String targetGroups = data.targetExpression.resolveByDatabase().stream().map(t -> Long.toString(t.getID())).reduce((s, e) -> s + "," + e).orElse(null);

		if (targetGroups != null) {
			writer.single("target_groups", targetGroups);
		} else {
			writer.comment("no target_groups");
		}
		writer.cflush();
	}

	private void dynamics() throws IOException {
		int nameCount = allBlocks.getNumberOfDynamicNames();

		if (nameCount > 0) {
			writer.opennode("dynamics", "count", nameCount);
			for (DynName dtmp : allBlocks.getDynamicNameEntries()) {
				XMLWriter.Creator c = writer.create("dynamic", "id", dtmp.id, "name", dtmp.name);

				getDynamicInfo(dtmp, c);
				writer.opennode(c);
				for (int n = 0; n < dtmp.clen; ++n) {
					DynCont cont = dtmp.content.elementAt(n);

					if (cont.targetID != DynCont.MATCH_NEVER) {
						XMLWriter.Creator cr = writer.create("dyncont", "id", cont.id, "order", cont.order);

						if ((cont.targetID != DynCont.MATCH_ALWAYS) && (cont.targetID > 0)) {
							cr.add("target_id", cont.targetID);
							cr.add("condition", cont.condition);
						}
						writer.opennode(cr);
						if (cont.text != null) {
							emitBlock(cont.text, 0, 0);
						}
						if (cont.html != null) {
							emitBlock(cont.html, 0, 1);
						}
						writer.close(cr.name);
					}
				}
				writer.close(c.name);
			}
			writer.close("dynamics");
		} else {
			writer.comment("no dynamics");
		}
	}

	private void urls() throws IOException {
		if ((data.URLlist != null) && (data.URLlist.size () > 0)) {
			writer.opennode("urls", "count", data.URLlist.size ());
			for (int n = 0; n < data.URLlist.size (); ++n) {
				URL url = data.URLlist.get(n);
				XMLWriter.Creator cr = writer.create("url", "id", url.getId(), "destination", url.getUrl(), "usage", url.getUsage());

				cr.addIfTrue("admin_link", url.getAdminLink());
				cr.add("original_url", url.getOriginalURL());
				cr.addIfTrue("static_value", url.getStaticValue());
				cr.add("columns", url.getStaticValueColumns());
				writer.openclose(cr);
			}
			writer.close("urls");
		} else {
			writer.comment("no urls");
		}
		if (data.extendURL != null) {
			boolean first = true;

			for (long urlID : data.extendURL.getURLIDs()) {
				for (URLExtension.URLEntry entry : data.extendURL.getParameter(urlID)) {
					if (first) {
						writer.opennode("url-extension", "count", data.extendURL.count());
						first = false;
					}
					XMLWriter.Creator cr = writer.create("entry", "id", urlID, "key", entry.getKey(), "value", entry.getValue());

					cr.add("columns", entry.getStaticValueColumns(data));
					writer.openclose(cr);
				}
			}
			if (!first) {
				writer.close("url-extension");
			} else {
				writer.comment("no url-extension");
			}
		}
		writer.cflush();
	}

	private void virtuals () throws IOException {
		Map <String, String>	vMap = data.virtualMap ();
		
		if ((vMap != null) && (vMap.size () > 0)) {
			writer.opennode ("virtuals", "count", vMap.size ());
			for (Entry <String, String> entry : vMap.entrySet ()) {
				writer.opennode ("virtual", "name", entry.getKey ());
				writer.data (entry.getValue ());
				writer.close ("virtual");
			}
			writer.close ("virtuals");
		} else {
			writer.comment ("no virtuals");
		}
		writer.cflush ();
	}

	/**
	 * Create string for media informations
	 *
	 * @param cinfo information about this customer
	 * @return the media string
	 */
	protected void getMediaInformation(Custinfo cinfo, XMLWriter.Creator c) {
		for (Media m : data.media()) {
			c.add("to_" + m.typeName(), cinfo.getMediaFieldContent(m));
		}
		c.addIfTrue("tracking_veto", cinfo.getTrackingVeto());
	}

	private void next(Column[] rmap) {
		if (multi != null) {
			XMLWriter.Creator c = writer.create("record");

			for (Reference r : multi) {
				int index = r.getIdIndex();

				if ((index != -1) && (!rmap[index].isnull())) {
					c.add(r.name(), rmap[index].get());
				}
			}
			writer.openclose(c);
		} else {
			writer.openclose("record");
			++inRecordCount;
		}
	}
}
