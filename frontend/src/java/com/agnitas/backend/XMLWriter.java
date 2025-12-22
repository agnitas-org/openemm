/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to write XML documents which keeps track of open
 * nodes and can encode data
 */
public class XMLWriter {
	static class Entity {
		String name;
		Entity parent;

		protected Entity(String nName, Entity nParent) {
			name = nName;
			parent = nParent;
		}
	}

	static public class Creator {
		String name;
		ArrayList<String> vars;
		ArrayList<String> vals;
		private int count;

		protected Creator(String nName) {
			name = nName;
			vars = new ArrayList<>();
			vals = new ArrayList<>();
			count = 0;
		}

		public String name() {
			return name;
		}

		public void add(String var, Object val) {
			if (val != null) {
				vars.add(var);
				vals.add(val.toString());
				++count;
			}
		}

		public void addIfTrue(String var, boolean val) {
			if (val) {
				add(var, val);
			}
		}

		public void add(Object... param) {
			int plen = param.length;

			for (int n = 0; n < plen; n += 2) {
				add(param[n].toString(), param[n + 1]);
			}
		}

		public String[] getVariables() {
			return vars.toArray(new String[count]);
		}

		public String[] getValues() {
			return vals.toArray(new String[count]);
		}
	}

	private StringBuffer buf;
	private OutputStream out;
	private int conditionalFlush;
	private int outputBlockSize;
	private Entity entity;
	private int depth;
	private boolean indentNext;
	private long outputSize;
	private IOException error;

	/**
	 * Constructor
	 *
	 * @param destination the stream the output xml is written to
	 */
	public XMLWriter(OutputStream destination) {
		buf = new StringBuffer();
		out = destination;
		conditionalFlush = 1024 * 1024;
		outputBlockSize = 128 * 1024;
		entity = null;
		depth = 0;
		indentNext = true;
		outputSize = 0L;
		error = null;
	}

	/**
	 * set the amount in bytes after which the output stream
	 * is flushed when the conditional flush is called,
	 * typically after each logical block
	 *
	 * @param bytes the minimum amount of bytes to be collected before flushing the stream
	 */
	public void conditionalFlush(int bytes) {
		conditionalFlush = bytes;
	}
	public int conditionalFlush() {
		return conditionalFlush;
	}

	/**
	 * set the block size to be used to write to the output
	 * stream during a flush
	 *
	 * @param bytes the size of the output block size
	 */
	public void outputBlockSize(int bytes) {
		outputBlockSize = bytes;
	}
	public int outputBlockSize() {
		return outputBlockSize;
	}

	/**
	 * returns the aprox. absolute number of bytes written
	 * and waiting to write to the output stream
	 *
	 * @return the number of bytes outputed
	 */
	public long outputSize() {
		return outputSize + buf.length();
	}

	/**
	 * Start a document, should only be called once
	 * as there is no check if this had been called
	 * more than once
	 */
	public void start() {
		truncate ();
		append ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	}
	
	/**
	 * End a document, closes all open nodes and
	 * forces to write out all buffered data
	 *
	 * @throws IOException
	 */
	public void end() throws IOException {
		while (entity != null) {
			close();
		}
		append ("\n");
		flush (0);
	}

	/**
	 * Open a new node
	 *
	 * @param simple if this is true, this is a simple node, it is closed directly
	 * @param name   the name of the node
	 * @param vars   the property names of the node
	 * @param vals   the property values of the node, must be of the same length as vars
	 */
	public void vopen(boolean simple, String name, String[] vars, String[] vals) {
		indent();
		append ("<", name);
		if (vars != null) {
			for (int n = 0; n < vars.length; ++n) {
				String val = (vals != null) && (n < vals.length) ? vals[n] : "";

				append (" ", vars[n], "=\"");
				escape (val);
				append ("\"");
			}
		}
		if (simple) {
			append ("/>");
		} else {
			append (">");
			entity = new Entity(name, entity);
			++depth;
		}
	}

	/**
	 * Open a new node
	 *
	 * @param simple if this is true, this is a simple node, it is closed directly
	 * @param name   the name of the node
	 * @param param  a list of variable/value pair for the properties of node
	 */
	public void open(boolean simple, String name, Object... param) {
		int plen = param.length;
		String[] vars = new String[plen >> 1];
		String[] vals = new String[plen >> 1];

		for (int n = 0; n < plen; n += 2) {
			vars[n >> 1] = param[n].toString();
			vals[n >> 1] = param[n + 1] != null ? param[n + 1].toString() : "";
		}
		vopen(simple, name, vars, vals);
	}

	/**
	 * Open a new node without closing it directly
	 *
	 * @param name  the name of the node
	 * @param param a list of variable/value pair for the properties of node
	 */
	public void opennode(String name, Object... param) {
		open(false, name, param);
	}

	/**
	 * Open a new node and close it directly
	 *
	 * @param name  the name of the node
	 * @param param a list of variable/value pair for the properties of node
	 */
	public void openclose(String name, Object... param) {
		open(true, name, param);
	}

	/**
	 * Create a new XMLWriter.Creator instance for
	 * dynamically building up a property list
	 *
	 * @param name  the name of the node which will be created with this instance
	 * @param param a list of variable/value pair for the properties of node
	 * @return a new instance of a Creator
	 */
	public Creator create(String name, Object... param) {
		Creator c = new Creator(name);

		c.add(param);
		return c;
	}
	
	/**
	 * Open a new node using a creator
	 *
	 * @param simple if this is true, this is a simple node, it is closed directly
	 * @param c      the creator to be used to build the node from
	 */
	public void open (boolean simple, Creator c) {
		vopen (simple, c.name, c.getVariables (), c.getValues ());
	}

	/**
	 * Open a new node using a creator without closing it directly
	 *
	 * @param c the creator to be used to build the node from
	 */
	public void opennode(Creator c) {
		open (false, c);
	}

	/**
	 * Open a new node using a creator and close it directly
	 *
	 * @param c the creator to be used to build the node from
	 */
	public void openclose(Creator c) {
		open (true, c);
	}

	/**
	 * Close an open node with the given name. If there are other
	 * open nodes, that had been opened later, these are closed
	 * as well
	 *
	 * @param name the node to close or, if it is null, the last opened node
	 */
	public void close(String name) {
		boolean match = false;

		while ((!match) && (entity != null)) {
			match = (name == null) || entity.name.equals(name);
			--depth;
			indent();
			append ("</", entity.name, ">");
			entity = entity.parent;
		}
	}

	/**
	 * Close the last opened node
	 */
	public void close() {
		close(null);
	}

	/**
	 * Add plain string to output
	 *
	 * @param s the string to add to the output
	 */
	public void data(String s) {
		escape(s);
		indentNext = false;
	}

	/**
	 * Add plain data to the output in base64 coding
	 *
	 * @param b the data to add to the output
	 */
	public void data(byte[] b) {
		encode(b);
		indentNext = false;
	}

	/**
	 * Open a new node, add the content and close it,
	 * This is a shorthand function for open/data/close
	 *
	 * @param name    the name of the node
	 * @param content the content for the node
	 * @param param   key/value pairs as the property of the node
	 */
	public void single(String name, String content, Object... param) {
		if ((content != null) && (content.length() > 0)) {
			opennode(name, param);
			data(content);
			close(name);
		} else {
			openclose(name, param);
		}
	}

	/**
	 * Open a new node, add the content in base64 encoding
	 * and close it,
	 * This is a shorthand function for open/data/close for
	 * binary data.
	 *
	 * @param name    the name of the node
	 * @param content the content for the node
	 * @param param   key/value pairs as the property of the node
	 */
	public void single(String name, byte[] content, Object... param) {
		if ((content != null) && (content.length > 0)) {
			opennode(name, param);
			data(content);
			close(name);
		} else {
			openclose(name, param);
		}
	}

	/**
	 * Open a new node, add the content converted to string
	 * and close it,
	 * This is a shorthand function for open/data/close for
	 * binary data.
	 *
	 * @param name    the name of the node
	 * @param content the content for the node
	 * @param param   key/value pairs as the property of the node
	 */
	public void single(String name, Object content, Object... param) {
		single(name, content.toString(), param);
	}

	/**
	 * Open a new node using a creator, add the content and close it,
	 * This is a shorthand function for open/data/close
	 *
	 * @param name    the name of the node
	 * @param content the content for the node
	 * @param param   key/value pairs as the property of the node
	 */
	public void single(Creator c, String content) {
		if ((content != null) && (content.length() > 0)) {
			opennode(c);
			data(content);
			close(c.name);
		} else {
			openclose(c);
		}
	}

	/**
	 * Open a new node using a creator, add the content in base64 encoding
	 * and close it,
	 * This is a shorthand function for open/data/close for
	 * binary data.
	 *
	 * @param name    the name of the node
	 * @param content the content for the node
	 * @param param   key/value pairs as the property of the node
	 */
	public void single(Creator c, byte[] content) {
		if ((content != null) && (content.length > 0)) {
			opennode(c);
			data(content);
			close(c.name);
		} else {
			openclose(c);
		}
	}

	/**
	 * Open a new node using a creator, add the content converted to string
	 * and close it,
	 * This is a shorthand function for open/data/close for
	 * binary data.
	 *
	 * @param name    the name of the node
	 * @param content the content for the node
	 * @param param   key/value pairs as the property of the node
	 */
	public void single(Creator c, Object content) {
		single(c, content.toString());
	}

	/**
	 * Add a comment to the output stream
	 *
	 * @param s the comment
	 */
	public void comment(String s) {
		indent();
		append ("<!-- ");
		escape(s);
		append (" -->");
	}

	/**
	 * Add an empty line to the output
	 */
	public void empty() {
		append ("\n");
		indentNext = true;
	}

	private void indent() {
		if (indentNext) {
			append ("\n");
			for (int n = 0; n < depth; ++n) {
				append ("  ");
			}
		} else {
			indentNext = true;
		}
	}

	private static Pattern escaper = Pattern.compile("[<>&'\"\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008\u000b\u000c\u000e\u000f\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u001b\u001c\u001d\u001e\u001f]");
	private void escape(String s) {
		if (s != null) {
			Matcher m = escaper.matcher(s);
			int last;

			last = 0;
			while (m.find()) {
				int pos = m.start();

				if (pos > last) {
					append (s, last, pos);
				}
				switch (s.charAt(pos)) {
					case '<':
						append ("&lt;");
						break;
					case '>':
						append ("&gt;");
						break;
					case '&':
						append ("&amp;");
						break;
					case '\'':
						append ("&apos;");
						break;
					case '"':
						append ("&quot;");
						break;
					default:
						break;
				}
				last = pos + 1;
			}
			if (last == 0) {
				append (s);
			} else if (last < s.length()) {
				append (s, last);
			}
		}
	}

	private static final String code = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	private void encode(byte[] cont) {
		int len;
		int limit;
		int count;
		int i0, i1, i2;
		char[] chunk = new char[4];

		len = cont != null ? cont.length : 0;
		limit = ((len + 2) / 3) * 3;
		count = 0;
		for (int n = 0; n < limit; n += 3) {
			if (count == 0) {
				append ("\n");
			}
			if (cont == null) {
				throw new RuntimeException("Unexpected empty cont");
			}
			i0 = cont[n] & 0xff;
			if (n + 1 < len) {
				i1 = cont[n + 1] & 0xff;
				if (n + 2 < len) {
					i2 = cont[n + 2] & 0xff;
				} else {
					i2 = 0;
				}
			} else {
				i1 = i2 = 0;
			}
			chunk[0] = code.charAt (i0 >>> 2);
			chunk[1] = code.charAt (((i0 & 0x3) << 4) | (i1 >>> 4));
			if (n + 1 < len) {
				chunk[2] = code.charAt (((i1 & 0xf) << 2) | (i2 >>> 6));
				if (n + 2 < len) {
					chunk[3] = code.charAt (i2 & 0x3f);
				} else {
					chunk[3] = '=';
				}
			} else {
				chunk[2] = chunk[3] = '=';
			}
			append (String.valueOf (chunk));
			count += 4;
			if (count >= 76) {
				count = 0;
			}
		}
		append ("\n");
	}
	
	/**
	 * Write the currently buffered output to the output
	 * stream and encode it to the target character set
	 * (which is currently hardcoded UTF-8), if the limit
	 * (minSize) is reached.
	 *
	 * @param minSize the minimum size of the buffered output to continue the flush
	 * @throws IOException
	 */
	private void flush(int minSize) throws IOException {
		if ((error == null) && (buf.length () > minSize)) {
			try {
				for (int written = 0; written < buf.length(); ) {
					int chunk = buf.length() - written;

					if (chunk > outputBlockSize) {
						chunk = outputBlockSize;
					}
					out.write(buf.substring(written, written + chunk).getBytes(StandardCharsets.UTF_8));
					out.flush();
					written += chunk;
					outputSize += chunk;
				}
			} catch (IOException e) {
				error = e;
			}
			truncate ();
		}
		if (error != null) {
			throw error;
		}
	}

	private void truncate () {
		buf.setLength (0);
	}

	private void append (String ... chunks) {
		if (error == null) {
			for (int n = 0; n < chunks.length; ++n) {
				if (conditionalFlush > 0) {
					try {
						flush (conditionalFlush);
					} catch (IOException e) {
						break;
					}
				}
				buf.append (chunks[n]);
			}
		}
	}

	private void append (String chunk, int start, int end) {
		append (chunk.substring (start, end));
	}
	
	private void append (String chunk, int start) {
		append (chunk.substring (start));
	}
}
