/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Stream;

/**
 * This class provides a common logging interface with separate
 * logging levels. Logfiles are typically written under the
 * home directory of the user in var/log
 */
public class Log {
	/** non ignorable messages has value < 0 */
	/** trigger action in external process */
	final public static int		TRIGGER = -1;
	/** global error which may harm things beyond the application  */
	final public static int		GLOBAL = 0;
	/** fatal error which requires manual correction and termiantes the application */
	final public static int		FATAL = 1;
	/** error which can require manual correction */
	final public static int		ERROR = 2;
	/** warning which can be a hint for some problems */
	final public static int		WARNING = 3;
	/** more important runtime information */
	final public static int		NOTICE = 4;
	/** some general runtime information */
	final public static int		INFO = 5;
	/** more verbose information */
	final public static int		VERBOSE = 6;
	/** just debug output, in normal operation mostly useless */
	final public static int		DEBUG = 7;
	/** textual represenation of loglevels */
	final static String[]		DESC = {
		"GLOBAL",
		"FATAL",
		"ERROR",
		"WARNING",
		"NOTICE",
		"INFO",
		"VERBOSE",
		"DEBUG"
	};
	/** the level up to we will write to the logfile */
	private int			level;
	/** a unique number for this thread */
	private String			unique;
	/** optional output print */
	private PrintStream		printer;
	/** to provide some hirarchical log IDs */
	private Stack <String>		idc;
	/** the base path to the log file directory */
	private String			path;
	/** the part of the logfile after the current date */
	private String			append;
	/** format of date for logfilename */
	private SimpleDateFormat	fmt_fname;
	/** format of date to be written to logfile */
	private SimpleDateFormat	fmt_msg;

	/**
	 * Find the numeric representation of a textual loglevel
	 *
	 * @param desc the textual loglevel
	 * @return     its numeric value
	 * @throws NumberFormatException if desc is neither a known loglevel nor a numeric representation
	 */
	static public int matchLevel (String desc) throws NumberFormatException {
		for (int n = 0; n < DESC.length; ++n) {
			if (DESC[n].equalsIgnoreCase (desc)) {
				return n;
			}
		}
		return Integer.parseInt (desc);
	}

	/**
	 * Make a textual description of the given loglevel
	 *
	 * @param loglvl the numeric loglevel
	 * @return its string version
	 */
	static public String levelDescription (int loglvl) {
		if ((loglvl >= 0) && (loglvl < DESC.length)) {
			return DESC[loglvl];
		}
		return "(" + loglvl + ")";
	}

	/**
	 * For pretty printing, this returns an empty string on 1,
	 * otherwise "s"
	 *
	 * @param nr the number to check
	 * @return the extension based on the number
	 */
	static public String exts (long nr) {
		return (nr == 1) ? "" : "s";
	}

	/**
	 * Wrapper for integer input to exts
	 *
	 * @param nr the number to check
	 * @return the extension based on the number
	 */
	static public String exts (int nr) {
		return exts ((long) nr);
	}

	/**
	 * Like exts, but for words like entry vs. entries
	 *
	 * @param nr the number to check
	 * @return the extension based on the number
	 */
	static public String exty (long nr) {
		return (nr == 1) ? "y" : "ies";
	}

	/**
	 * Wrapper for integer input to exty
	 *
	 * @param nr the number to check
	 * @return the extension based on the number
	 */
	static public String exty (int nr) {
		return exty ((long) nr);
	}

	/**
	 * Constructor for class
	 *
	 * @param program the name of the application
	 * @param level the maximum log level to report
	 * @param uniqueValue a unique number to unify parallel logging
	 */
	public Log (String program, int level, long uniqueValue) {
		this.level = level;
		unique = uniqueValue != 0 ? "/ID:" + Long.toHexString (uniqueValue) : null;
		printer = Str.atob (System.getProperty ("log.print", "false")) ? System.out : null;

		String	separator = System.getProperty ("file.separator");
		String	hostname;
		int idx;

		idc = new Stack<>();
		path = System.getProperty ("log.home", Str.makePath ("$home", "var", "log"));
		try {
			InetAddress addr = InetAddress.getLocalHost ();

			try {
				hostname = addr.getHostName ();
				if ((idx = hostname.indexOf ('.')) != -1) {
					hostname = hostname.substring (0, idx);
				}
			} catch (SecurityException e) {
				hostname = addr.getHostAddress ();
			}
		} catch (UnknownHostException e) {
			hostname = "unknown";
		}

		if ((idx = program.lastIndexOf (separator)) != -1) {
			program = program.substring (idx + 1);
		}
		append = hostname + "-" + program;
		fmt_fname = new SimpleDateFormat ("yyyyMMdd");
		fmt_msg = new SimpleDateFormat ("[dd.MM.yyyy  HH:mm:ss] ");
	}

	/**
	 * returns the current loglevel
	 *
	 * @return log level
	 */
	public int level () {
		return level;
	}

	/**
	 * sets the current loglevel
	 *
	 * @param nlevel new log level
	 */
	public void level (int nlevel) {
		level = nlevel;
	}

	/**
	 * returns the textual representation of the
	 * current loglevel
	 *
	 * @return current loglevel as string
	 */
	public String levelDescription () {
		return levelDescription (level);
	}

	/**
	 * sets the loglevel using its textual representation
	 * @param desc the loglevel as string
	 */
	public void levelDescription (String desc) throws NumberFormatException {
		level = matchLevel (desc);
	}

	/** sets the optional output stream
	 *
	 * @param nprinter new stream
	 */
	public void setPrinter (PrintStream nprinter) {
		printer = nprinter;
	}

	/**
	 * Pushes a new id, so a chain of IDs is put into any
	 * logfile entry
	 *
	 * @param nid the new id
	 * @param separator howto separate the the IDs
	 */
	public void pushID (String nid, String separator) {
		if ((separator != null) && (! idc.empty ())) {
			idc.push (idc.peek () + separator + nid);
		} else {
			idc.push (nid);
		}
	}

	/**
	 * Pushes a new id without separator
	 *
	 * @param nid the new id
	 */
	public void pushID (String nid) {
		pushID (nid, null);
	}

	/**
	 * Removes to top element of the ID stack
	 *
	 * @return the top ID on the stack or null, if stack is empty
	 */
	public String popID () {
		try {
			return idc.pop ();
		} catch (EmptyStackException e) {
			// Nothing to do here?
		}
		return null;
	}

	/**
	 * Clear all stacked IDs
	 */
	public void clrID () {
		idc.clear ();
	}

	/**
	 * Set ID after removing all existing IDs
	 *
	 * @param mid the ID to set
	 */
	public void setID (String mid) {
		idc.clear ();
		idc.push (mid);
	}

	/**
	 * check if the given level should be logged
	 *
	 * @param loglvl the level to check
	 * @return true, if logging is enabled for this level
	 */
	public boolean islog (int loglvl) {
		return loglvl <= level;
	}

	private String mkfname (String postfix) {
		return Str.makePath (path, fmt_fname.format (new Date ()) + "-" + postfix + ".log");
	}

	/**
	 * writes an entry to the logfile
	 *
	 * @param loglvl the level of this message
	 * @param mid the ID of this message
	 * @param msg the message itself
	 * @param th an optional throwable to print the stack trace
	 */
	public void out (int loglvl, String mid, String msg, Throwable th) {
		if (loglvl <= level) {
			Date			now = new Date ();
			String			fname = mkfname (append);
			String			marker = fmt_msg.format (now) + levelDescription (loglvl) + (unique != null ? unique : "") + (mid != null ? "/" + mid : "");
			List <String>		output = new ArrayList <> ();
			
			Stream.of (msg.split ("\n")).forEach (s -> output.add (s));
			if (th != null) {
				Set <Throwable>	seen = new HashSet<>();
				
				while (th != null) {
					StackTraceElement[] elements = th.getStackTrace ();

					if (elements != null) {
						output.add ("Stacktrace for " + th.toString () + ":");
						for (StackTraceElement element : elements) {
							output.add ("\tat " + element.toString ());
						}
					}
					seen.add (th);
					th = th.getCause ();
					if ((th != null) && seen.contains (th)) {
						output.add ("\t... recursive stack trace detected, aborting");
					}
				}
			}
			try {
				try (FileOutputStream file = new FileOutputStream (fname, true)) {
					file.write ((output
						     .stream ()
						     .map (s -> marker + ": " + s)
						     .reduce ((s, e) -> s + "\n" + e).orElse ("") + "\n")
						    .getBytes ());
					if (printer != null) {
						printer.println (msg);
					}
				}
			} catch (Exception e) {
				System.out.println ("FAILED TO LOG " + e.toString () + ": " + msg);
			}
		}
	}
	/**
	 * writes an entry to the logfile
	 *
	 * @param loglvl the level of this message
	 * @param mid the ID of this message
	 * @param msg the message itself
	 */
	public void out (int loglvl, String mid, String msg) {
		out (loglvl, mid, msg, null);
	}
	/**
	 * writes an entry to the logfile using the stacked ID
	 *
	 * @param loglvl the level of this message
	 * @param msg the mesage itself
	 */
	public void out (int loglvl, String msg) {
		String	mid;

		try {
			mid = idc.peek ();
		} catch (EmptyStackException e) {
			mid = null;
		}
		out (loglvl, mid, msg);
	}

	/**
	 * writes a line to a generic log/data file
	 *
	 * @param name the name of the destination file
	 * @param msg the message itself
	 */
	public void out (String name, String msg) {
		String			fname = mkfname (name);

		try (FileOutputStream file =new FileOutputStream (fname, true)) {
			file.write ((msg + "\n").getBytes ());
			if (printer != null) {
				printer.println ("[" + name + "]: " + msg);
			}
		} catch (FileNotFoundException e) {
			out (ERROR, name, fname + " not found (" + e.toString () + "): " + msg);
		} catch (IOException e) {
			out (ERROR, name, fname + " io failed (" + e.toString () + "): " + msg);
		}
	}
}
