####################################################################################################################################################################################################################################################################
#                                                                                                                                                                                                                                                                  #
#                                                                                                                                                                                                                                                                  #
#        Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   #
#                                                                                                                                                                                                                                                                  #
#        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    #
#        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           #
#        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            #
#                                                                                                                                                                                                                                                                  #
####################################################################################################################################################################################################################################################################
#	-*- mode: python; mode: fold -*-
#
"""Standard library for AGNITAS python runtime

These are the base global variables, functions and classes used
throughout the python based programs in the AGNITAS system. For more
enhanced and higher level functions and class look at the modules:

	- eagn (enhanced library)
	- xagn (XML related library)
	- dagent (application related library)
	- aps (general plugin framewortk)

These variables are available for direct access from other modules.
The type of allowed access (not enforced by the program) is written as
read-only (ro) or read-write (rw):

	- (ro) _syscfg: an instance if class Systemconfig to hold the system specific configuration
	- (ro) licence: the licence ID of the running instance
	- (ro) system: the system type the script is running on
	- (ro) fqdn: the full qualified domain name of current system
	- (ro) host: the bare hostname without domain
	- (ro) base: the base directory, $HOME as default
	- (ro) user: the user name who invoked the program by uid
	- (ro) LV_NONE, LV_FATAL, LV_REPORT, LV_ERROR, LV_WARNING, LV_NOTICE, LV_INFO, LV_VERBOSE, LV_DEBUG: available log levels
	- (rw) loglevel: the global logging level
	- (ro) logtable: name/value dict for loglevel (both directions)
	- (rw) loghost: the hostname used as part of the logfile
	- (rw) logname: the name for this service as part of the logfile (by default it is set based on sys.argv[0], if possible)
	- (rw) logpath: the path to wirte log files to
	- (rw) outlevel: the global log level for writing output to a separate stream
	- (rw) outstream: the stream to write additional to the file based logging
	- (ro) backlog: instance of Backlog, if backlogging is enabled
	- (ro) loglast: last time an entry had been written to the logifile (used by function ``mark'')
	- (ro) lockname: name to be used as base for locking
	- (rw) lockpath: path to directory where lock files are written to
	- (ro) CSVDialetcs: a list of all definied dialects for CSV handling
	- (ro) CSVDefault: the name of the default CSV dialect
"""
#
# Imports, Constants and global Variables
#{{{
import	sys, os, errno, stat, signal, multiprocessing
import	time, re, socket, hashlib, json
import	platform, traceback, pwd, functools, keyword, logging
import	smtplib, urllib, httplib, xmlrpclib
import	base64, datetime, csv, codecs, subprocess, collections, itertools
import	Queue
import	gzip
try:
	import	bz2
except ImportError:
	bz2 = None
#
_changelog = [

	('1.0.0', '2019-01-01', 'Base library', 'openemm@agnitas.de')
]
__version__ = _changelog[-1][0]
__date__ = _changelog[-1][1]
__author__ = ', '.join (sorted (list (set ([_c[3] for _c in _changelog]))))
#
class Systemconfig (object):
	"""Handling system specific configuration

generic class to read system information from external configuration
file to reflect the individual values for this installation. This
class can be used simular to a dictionary.

The content of the file has line based <key> <value> pairs, separated
by an equal sign. If you need a longer value, you can use the opening
curly bracket as the start of the value and then add the value in the
next lines. Close this block by using a closing curly bracket.
Multiline values are folded to a single line value.
"""
	__slots__ = ['path', 'content', 'cfg']
	_defaultPath = os.environ.get ('SYSTEM_CONFIG_PATH', '/home/openemm/etc/system.cfg')
	def __init__ (self, path = None):
		"""path to configuration file or None to use default

Setup the Systemconfig object and read the content of the system confg
file, if it is available. """
		if path is None:
			path = self._defaultPath
		self.path = path
		self.content = os.environ.get ('SYSTEM_CONFIG')
		self.cfg = {}
		if self.content is None and self.path and os.path.isfile (self.path):
			with open (self.path) as fd:
				self.content = fd.read ()
		self.parse ()
	
	def __str__ (self):
		return '%s:\n\t%s' % (self.__class__.__name__, Stream (self.cfg.iteritems ())
			.switch (lambda kv: kv[1] is None, lambda kv: str (kv[0]), lambda kv: '%s=%r' % kv)
			.sorted ()
			.join ('\n\t')
		)
	
	def parse (self):
		self.cfg = {}
		if self.content is not None:
			try:
				self.cfg = json.loads (self.content)
				if type (self.cfg) != dict:
					raise ValueError ('expect json object, not %s' % type (self.cfg))
			except ValueError:
				cont = None
				cur = None
				for line in (_l.strip () for _l in self.content.split ('\n')):
					if cont is not None:
						if line == '}':
							self[cont] = ' '.join (cur)
							cont = None
						elif line:
							cur.append (line)
					elif line and not line.startswith ('#'):
						try:
							(var, val) = [_v.strip () for _v in line.split ('=', 1)]
							if val == '{':
								cont = var
								cur = []
							else:
								self[var] = val
						except ValueError:
							pass

	def __setitem__ (self, var, val):
		"""Set configuration value"""
		self.cfg[var] = val

	def __getitem__ (self, var):
		"""Get configuration value"""
		return self.cfg[var]

	def __delitem__ (self, var):
		"""Remove configuration value"""
		del self.cfg[var]

	def __contains__ (self, var):
		"""Checks for existance of configuration variable"""
		return var in self.cfg

	def keys (self):
		"""Returns all available configuration variables"""
		return self.cfg.keys ()

	def get (self, var, dflt = None):
		"""Get configuration value with default fallback"""
		try:
			return self[var]
		except KeyError:
			return dflt

	def iget (self, var, dflt = None):
		"""Get configuration value as integer with default fallback"""
		try:
			return int (self[var])
		except KeyError:
			return dflt

	def fget (self, var, dflt = None):
		"""Get configuration value as float with default fallback"""
		try:
			return float (self[var])
		except KeyError:
			return dflt

	def bget (self, var, dflt = None):
		"""Get configuration value as boolean with default fallback"""
		try:
			return atob (self[var])
		except KeyError:
			return dflt

	def lget (self, var, sep = ',', dflt = None):
		"""Get configuration value as list with default fallback"""
		try:
			return [_v.strip () for _v in self[var].split (sep) if _v.strip ()]
		except KeyError:
			return dflt

	def dump (self):
		"""Display current configuration content"""
		for (var, val) in self.cfg.items ():
			print ('%s=%s' % (var, val))

	def sendmail (self, recipients):
		"""Returns sendmail command based on configuration"""
		cmd = ['/usr/sbin/sendmail']
		if not self.bget ('enable-sendmail-dsn', False):
			cmd.append ('-NNEVER')
		cmd.append ('--')
		if type (recipients) in (str, unicode):
			cmd.append (recipients)
		else:
			cmd += recipients
		return cmd
_syscfg = Systemconfig ()
licence = _syscfg.iget ('licence', 0)
#
system = platform.system ().lower ()
fqdn = platform.node ()
host = fqdn.split ('.', 1)[0]
#
base = os.environ.get ('HOME', '.')
_scripts = os.path.sep.join ([base, 'scripts'])
if not _scripts in sys.path:
	sys.path.insert (0, _scripts)
del _scripts
try:
	user = pwd.getpwuid (os.getuid ()).pw_name
except KeyError:
	user = os.environ.get ('USER', '#%d' % os.getuid ())
#}}}
#
# Support routines
#
#{{{
class mutable (object):
	"""General empty class as placeholder for temp. structured data

This class can be used if there is no reason to create an own class
but to keep some data in a structured way. You can access the fields
either using a member reference or like a dict object, e.g.

temp = agn.mutable ()
temp.a = 1
print temp.a

temp['a'] = 1
print temp['a']

should be equal."""
	def __init__ (self, **kws):
		self (**kws)

	def __iter__ (self):
		return iter (self.__dict__)

	def __getitem__ (self, var):
		return self.__dict__[var]

	def __setitem__ (self, var, val):
		self.__dict__[var] = val

	def __call__ (self, **kws):
		for (var, val) in kws.items ():
			if not var.startswith ('_'):
				self.__dict__[var] = val

	def __len__ (self):
		return len (self.__dict__)

	def __contains__ (self, var):
		return var in self.__dict__

	def __str__ (self):
		return '[%s]' % ', '.join (['%s=%r' % (_n, self.__dict__[_n]) for _n in self.__dict__])
	
	def __repr__ (self):
		return '%s (**%r)' % (self.__class__.__name__, self.__dict__)
struct = mutable

def namedmutable (name, fields, keyword_arguments_only = True):
	"""General placeholder class with fixed named elements simular to collections.namedtuple

This class is used, when a data class is needed and the elements
should be mutable (otherwise collections.namedtuple is the proper
choice). The ``name'' represents the name of the generated class,
``fields'' is a list of fields that are allowed by the class. If an
element of the ``fields'' is a tuple of exactly two elements, then the
first one is used as the field name and second as its default value.

If ``keyword_arguments_only'' is set to False, then a created class's
init method can also accept positional arguments beside keyword
arguments.

The class ensures, that all fields are available and initialized with
the default value on instanciation, if default values are used."""
	if len (filter (lambda f: type (f) is tuple, fields)) > 0:
		(fields, defaults) = (Stream (fields)
			.map (lambda f: f if type (f) is tuple else (f, None))
			.collect (
				supplier = ([], []), 
				accumulator = Stream.multi (lambda s, e: s[0].append (e[0]), lambda s, e: s[1].append (e[1]))
			)
		)
		ns_defaults = dict (zip (fields, defaults))
	else:
		ns_defaults = None
	def ns_init (self, *args, **kws):
		if keyword_arguments_only and args:
			raise ValueError ('only keyword arguments allowed, no positional arguments')
		if ns_defaults is not None:
			for (key, value) in ns_defaults.iteritems ():
				setattr (self, key, value () if callable (value) else value)
		for (key, value) in zip (self.__slots__, args):
			setattr (self, key, value)
		for (key, value) in kws.iteritems ():
			setattr (self, key, value)
	def ns_str (self):
		return '%s (%s)' % (self.__class__.__name__, ', '.join ('%s = %r' % (_f, getattr (self, _f)) for _f in self.__slots__ if hasattr (self, _f)))
	namespace = {
		'__slots__': fields,
		'__init__': ns_init,
		'__str__': ns_str,
		'__repr__': ns_str,
	}
	if ns_defaults is not None:
		namespace['__delattr__'] = lambda self, option: setattr (self, option, ns_defaults[option])
	return type (
		name, 
		(object, ),
		namespace
	)

class NamedMutable (object):
	"""Metaclass to be subclassed

This class is designed to be subclassed to create a data class with
default values in the spirit of agn.namedmutable, i.e.:

class Line (agn.NamedMutable):
	no = 0
	content = None

line = Line (no = 1, content = 'sample line')
"""
	def __new__ (cls, *args, **kws):
		return namedmutable (cls.__name__, list (_kv for _kv in cls.__dict__.iteritems () if _kv[0] not in NamedMutable.__dict__)) (*args, **kws)

class error (Exception):
	"""This is a general exception thrown by this module."""
	@property
	def msg (self):
		logexc (LV_WARNING, 'agn.error', 'access deprecated property msg')
		return str (self)

class Ignore (object):
	"""Context to ignore selected exceptions as a replacement for

try:
	...
except ...:
	pass

If no exception are specified, all exceptions are ignored and loglevel
is set to WARNING. This is general a bad idea and should be used with
care."""
	def __init__ (self, *exceptions):
		self.exceptions = set (exceptions)
		self.onexception = None
		self.loglevel = None if self.exceptions else LV_WARNING
		self.logident = 'ignored'
		self.logmessage = None
		
	def __enter__ (self):
		return self
		
	def __exit__ (self, exc_type, exc_value, traceback):
		if exc_type is not None:
			try:
				if self.loglevel is not None:
					logexc (self.loglevel, self.logident, self.logmessage)
				if not self.exceptions or exc_type in self.exceptions:
					if callable (self.onexception):
						self.onexception (exc_type, exc_value, traceback)
					return True
			except Exception as e:
				logexc (LV_FATAL, 'ignore', 'Failed during processing: %s' % e)
		return False

class Experimental (Ignore):
	"""This is a derivated class from Ignore to provide a context
manager for experimental code. This could be used, if new code is
added to an already productive program to avoid abortion of the
program due to bugs in the new code. This should NOT be used, if
failure of the new code harms the result of the program.

Instead of passing excpetions, use a speaking name as an argument to
create an instance."""
	last_used = collections.defaultdict (int)
	def __init__ (self, name = None):
		super (Experimental, self).__init__ ()
		if name is None:
			name = logname
		today = datetime.datetime.now ().toordinal ()
		if self.last_used[name] != today:
			self.last_used[name] = today
			log (LV_REPORT, 'experimental', '%s: reminder for experimental code in production' % name)
		self.loglevel = LV_REPORT
		self.logident = 'experimental'
		self.logmessage = '%s: this code is marked as experimental' % name

class Deprecated (object):
	"""Annotation to mark a function as deprecated

This is used to mark a function or method as deprecated. Use this
decorator always with brackets. An optional argument is the name the
class as we did not found a way to reliably automatically determinate
the name.

The class has the class variable "silent" which, if set to True, will
supress any output (as it is suppressed, if sys.stderr is not a tty).
"""
	_seen = set ()
	silent = False
	def __init__ (self, *args, **kws):
		self.args = args
		self.kws = kws
	
	def __call__ (self, function):
		def wrapper (*args, **kws):
			if function not in self._seen:
				try:
					name = function.__name__
				except AttributeError:
					name = str (function)
				try:
					path = [self.kws['path']]
				except KeyError:
					try:
						path = [function.__module__]
					except AttributeError:
						path = []
					if len (self.args) > 0:
						try:
							path.append (self.args[0].__name__)
						except AttributeError:
							path.append (self.args[0])
					elif len (args) > 0 and hasattr (args[0], name) and callable (getattr (args[0], name)):
						with Ignore (AttributeError):
							path.append (args[0].__class__.__name__)
				if path:
					name = '%s.%s' % ('.'.join (path), self.kws.get ('name', name))
				if not self.silent and sys.stderr.isatty ():
					sys.stderr.write ('WARNING: "%s" is marked as deprecated\n' % name)
				else:
					log (LV_WARNING, 'deprecated', '"%s" is marked as deprecated' % name)
				self._seen.add (function)
			return function (*args, **kws)
		return wrapper

def __require (checkversion, srcversion, modulename):
	"""__require (checkversion, srcversion, modulename):

Check for version mismatch."""
	for (c, v) in ((int (_c), int (_v)) for (_c, _v) in zip (checkversion.split ('.'), srcversion.split ('.'))):
		if c > v:
			raise error ('%s: Version too low, require at least %s, found %s' % (modulename, checkversion, srcversion))
		elif c < v:
			break
	if checkversion.split ('.')[0] != srcversion.split ('.')[0]:
		raise error ('%s: Major version mismatch, %s is required, %s is available' % (modulename, checkversion.split ('.')[0], srcversion.split ('.')[0]))

def require (checkversion, checklicence = None):
	"""Checks if current version satisfies the requirements

The given version must be of the same major number as the version of
this library and the minor.patchlevel must be at least as high as
requested.
"""
	__require (checkversion, __version__, 'agn')
	if not checklicence is None and checklicence != licence:
		raise error ('Licence mismatch, require %d, but having %d' % (checklicence, licence))

def chop (s):
	"""removes trailing carrige returns and newlines"""
	return s.rstrip ('\r\n')

def atoi (s, ibase = 10, dflt = 0):
	"""Lazy parses a value into an integer

parses input parameter as numeric value, use default if it is not
parsable.
"""
	if type (s) in (int, long):
		return s
	elif type (s) is float:
		return int (s)
	try:
		return int (s, ibase)
	except (ValueError, TypeError):
		return dflt

def atob (s):
	"""Interprets a value as a boolean"""
	if type (s) is bool:
		return s
	elif type (s) in (int, long):
		return s != 0
	elif type (s) is float:
		return s != 0.0
	elif type (s) in (str, unicode) and s and s[0] in [ '1', 'T', 't', 'Y', 'y', '+' ]:
		return True
	return False

def numfmt (n, separator = '.'):
	"""Format an integer value to be more readable"""
	return '{:,d}'.format (int (n)).replace (',', separator)

def sizefmt (n, flip = 10):
	"""Converts the number n to a readable form of memory sizes.

Use flip to avoid a too rough rounding of the value, e.g.:
sizefmt (8999) --> '8999 Byte'
sizefmt (8999, 1) --> '8.79 kByte'
"""
	sizetab = [
		(1024 * 1024 * 1024 * 1024, 'TByte'),
		(1024 * 1024 * 1024, 'GByte'),
		(1024 * 1024, 'MByte'),
		(1024, 'kByte')
	]
	for (size, unit) in sizetab:
		if size * flip <= n:
			return '%.2f %s' % (float (n) / size, unit)
	return '%d Byte' % n

def validate (s, pattern, *funcs, **kw):
	"""Validates an input string

The input string s is matched against the regular expression "pattern"
(where the pattern must cover the whole string, a missing leading ^
and a missing trailing $ will be appended.) If the pattern match had
been successful then an optional list of functions will be consulted
for further validation. Each function will be called using a matching
group from the regular expression. If there had been no group, then
the whole match will be used. Each function must return "True" on
success or "False" on failure. Each funtion may either be the pure
function or a tuple containing the function and the reason as string.
This reason string is used, if the validation failed.
"""
	if not pattern.startswith ('^'):
		pattern = '^' + pattern
	if not pattern.endswith ('$') or pattern.endswith ('\\$'):
		pattern += '$'
	reflags = kw.get ('flags', 0)
	try:
		pat = re.compile (pattern, reflags)
	except Exception as e:
		raise error ('Failed to compile regular expression "%s": %s' % (pattern, e.args[0]))
	mtch = pat.match (s)
	if mtch is None:
		raise error (kw.get ('reason', 'No match'))
	if len (funcs) > 0:
		flen = len (funcs)
		n = 0
		report = []
		grps = mtch.groups ()
		if not grps:
			grps = [mtch.group ()]
		for elem in grps:
			if n < flen:
				if type (funcs[n]) in (list, tuple):
					(func, reason) = funcs[n]
				else:
					func = funcs[n]
					reason = '%r' % func
				if not func (elem):
					report.append ('Failed in group #%d: %s' % (n + 1, reason))
			n += 1
		if report:
			raise error ('Validation failed: %s' % ', '.join (report))

def filecount (directory, pattern):
	"""Count number of files in a directory matching a regular expression"""
	pat = re.compile (pattern)
	return sum (1 for _fname in os.listdir (directory) if pat.search (_fname) is not None)

def relink (source, target, pattern = None):
	"""Updateds symbolic links in target from source, optional only these files matching pattern"""
	make_real = lambda p: os.path.realpath (p) if not os.path.isdir (p) and os.path.islink (p) else p
	real_source = make_real (source)
	real_target = make_real (target)
	if not os.path.isdir (real_source):
		raise error ('%s: source not a directory' % source)
	if not os.path.isdir (real_target):
		raise error ('%s: target not a directory' % target)
	if pattern is None:
		match = lambda f: True
	else:
		match = lambda f: Stream.of (pattern).filter (lambda p: p.match (f)).limit (1).count () > 0
	Filepath = collections.namedtuple ('Filepath', ['filename', 'source_path', 'target_path'])
	installed = [_f for _f in os.listdir (real_target) if os.path.islink (os.path.join (real_target, _f)) and match (_f)]
	available = [_f for _f in os.listdir (real_source) if match (_f)]
	for fp in (Stream (available)
		.map (lambda f: Filepath (f, os.path.join (real_source, f), os.path.join (real_target, f)))
		.filter (lambda fp: fp.filename not in installed or (os.path.islink (fp.target_path) and os.readlink (fp.target_path) != fp.source_path))
	):
		if fp.filename in installed:
			os.unlink (fp.target_path)
		os.symlink (fp.source_path, fp.target_path)
	#
	def resolvable (path):
		seen = set ()
		while os.path.islink (path) and path not in seen:
			seen.add (path)
			new_path = os.readlink (path)
			if not os.path.isabs (new_path):
				new_path = os.path.join (os.path.dirname (path), new_path)
			path = os.path.realpath (new_path)
		return os.access (path, os.F_OK)
	#
	(Stream (installed)
		.map (lambda f: os.path.join (real_target, f))
		.filter (lambda p: os.path.islink (p) and not resolvable (p))
		.each (lambda p: os.unlink (p))
	)

def which (program, *args):
	"""Finds the path to an executable

``args'' may contain more directories to search for if the programn
can be expected in a known directory which is not part of $PATH.
"""
	return (Stream (os.environ.get ('PATH', '').split (':') + list (args))
		.map (lambda p: os.path.join (p if p else os.path.curdir, program))
		.filter (lambda p: os.access (p, os.X_OK))
		.first (no = None)
	)

def mkpath (*parts, **opts):
	"""Create a pathname from elements

Optional keyword is absolute (boolean) to make the path absolute, i.e.
with leading slash.
"""
	absolute = opts.get ('absolute', False)
	rc = os.path.sep.join (str (_p) for _p in parts)
	if absolute and not rc.startswith (os.path.sep):
		rc = os.path.sep + rc
	return os.path.normpath (rc)

def fingerprint (fname):
	"""calculates a MD5 hashvalue (a fingerprint) of a given file."""
	fp = hashlib.md5 ()
	with open (fname, 'rb') as fd:
		for chunk in iter (functools.partial (fd.read, 65536), ''):
			fp.update (chunk)
	return fp.hexdigest ()

def toutf8 (s, charset = 'ISO-8859-1'):
	"""converts an input string to UTF-8"""
	if type (s) is str:
		if charset is None:
			s = unicode (s)
		else:
			s = unicode (s, charset)
	return s.encode ('UTF-8')

def fromutf8 (s):
	"""interprets an input string as UTF-8"""
	return unicode (s, 'UTF-8') if type (s) is not unicode else s

def splitutf8 (s, length):
	"""split UTF-8 string

splits a string to an array of strings with at most length bytes while
ensuring keeping these substrings valid UTF-8 strings.
"""
	utf8start = lambda a: ord (a) & 0xc0 == 0xc0
	utf8cont = lambda a: ord (a) & 0xc0 == 0x80
	rc = []
	if type (s) is unicode:
		s = toutf8 (s)
	while s:
		clen = len (s)
		if clen > length:
			clen = length
			if utf8cont (s[clen]):
				while clen > 0 and not utf8start (s[clen]):
					clen -= 1
		if clen == 0:
			raise error ('reduced block to zero bytes, aborting')
		rc.append (s[:clen])
		s = s[clen:]
	return rc

def msgn (s):
	"""outputs a string to stdout"""
	sys.stdout.write (s)
	sys.stdout.flush ()
def msgcnt (cnt):
	"""outputs a counter to stdout

this moves the cursor back to the previous position so you can update
the output by multiple calling this function. To finalize the output
you should call msgfcnt.
"""
	msgn ('%8d\b\b\b\b\b\b\b\b' % cnt)
def msgfcnt (cnt):
	"""outputs a counter to stdout (finalize)

finalize the output of counter
"""
	msgn ('%8d' % cnt)
def msg (s):
	"""outputs a string to stdout adding a trailing newline"""
	msgn (s + '\n')
def err (s):
	"""outputs a string to stderr adding a trailing newline"""
	sys.stderr.write (s + '\n')
	sys.stderr.flush ()

def transformSQLwildcard (s):
	"""transforms a SQL wildcard expression to a regular expression"""
	r = ''
	needFinal = True
	for ch in s:
		needFinal = True
		if ch in '$^*?()+[{]}|\\.':
			r += '\\%s' % ch
		elif ch == '%':
			r += '.*'
			needFinal = False
		elif ch == '_':
			r += '.'
		else:
			r += ch
	if needFinal:
		r += '$'
	return r
def compileSQLwildcard (s, reFlags = 0):
	"""compiles a SQL wildcard expression as a regular expression"""
	return re.compile (transformSQLwildcard (s), reFlags)

class Stream (object):
	"""Stream implementation as inspired by Java 1.8

Original based on pystreams but as this project seems to be abandoned
a subset of these methods are implemented here by giving up parallel
execution at all."""
	__slots__ = ['iterator']
	__sentinel = object ()
	@classmethod
	def of (cls, *args):
		"""Creates a stream from an iterable object or a list of items"""
		if len (args) == 1:
			if args[0] is None:
				return cls (())
			with Ignore (TypeError):
				return cls (args[0])
		elif len (args) > 1 and callable (args[0]):
			if len (args) > 2:
				method = functools.partial (args[0], *args[1:-1])
			else:
				method = args[0]
			return cls (iter (method, args[-1]))
		return cls (args)
	
	@classmethod
	def defer (cls, obj, defer = None):
		"""Create a stream from an iterable ``obj'' and defer cleanup to the end"""
		def provider (obj, defer):
			try:
				for elem in obj:
					yield elem
			finally:
				if defer is not None:
					defer (obj)
				else:
					del obj
		return cls.of (provider (obj, defer))

	@classmethod
	def empty (cls):
		"""Creates an empty stream"""
		return cls.of ()
	
	@classmethod
	def concat (cls, *args):
		"""Create a new stream by concaternate all streams from ``args''"""
		return cls (args).chain ()
	
	@classmethod
	def merge (cls, *args):
		"""Like concat, but use items which are not iterable as literal to the target stream"""
		return cls ((_a if isinstance (_a, (collections.Iterable, collections.Iterator)) else [_a] for _a in args)).chain ()
			
	@classmethod
	def range (cls, *args, **kws):
		"""Creaste a new stream using xrange (``args'' and ``kws'' are pased to xrange)"""
		return cls (xrange (*args, **kws))
	
	@staticmethod
	def ifelse (value, predicate = None, mapper = None, alternator = None):
		"""if/then/else like for single values in the spirit of streams"""
		if predicate (value) if predicate is not None else bool (value):
			if mapper is None:
				return value
			elif callable (mapper):
				return mapper (value)
			return mapper
		if callable (alternator):
			return alternator (value)
		return alternator
	
	@staticmethod
	def loop (supplier, predicate, executor, finalizer = None, repeat_until = False):
		"""while/do or repeat/until like loop in the spirit of streams"""
		value = supplier () if callable (supplier) else supplier
		while repeat_until or predicate (value):
			repeat_until = False
			value = executor (value)
		return finalizer (value) if finalizer is not None else value
	
	@staticmethod
	def reloop (pattern, s, flags = 0, position = 1, finalizer = None):
		"""loop to find last occurance of nested regex"""
		expression = re.compile (pattern, flags) if not hasattr (pattern, 'search') else pattern
		while s:
			m = expression.search (s)
			if m is None:
				break
			ns = m.group (position)
			if ns == s:
				break
			s = ns
		return s
	
	@classmethod
	def multi (cls, *methods):
		"""execute multi methods and return the result of the last one, work around for lambda limitations"""
		def multier (*args, **kws):
			return cls (methods).map (lambda m: m (*args, **kws)).last (no = None)
		return multier
	
	@staticmethod
	def multichain (*methods):
		"""execute multi methods and feeding the previous
return value as first argument to next method (initial this is None),
returning the final value"""
		def multichainer (*args, **kws):
			value = None
			for method in methods:
				value = method (value, *args, **kws)
			return value
		return multichainer
	
	def __init__ (self, iterator):
		self.iterator = iter (iterator)

	def __str__ (self):
		return '%s <%s>' % (self.__class__.__name__, self.iterator)
	
	def __repr__ (self):
		return '%s (%r)' % (self.__class__.__name__, self.iterator)

	def __len__ (self):
		return self.count ()
		
	def __iter__ (self):
		return self.iterator
		
	def __reversed__ (self):
		try:
			return reversed (self.iterator)
		except TypeError:
			return reversed (list (self.iterator))

	def __enter__ (self):
		return self

	def __exit__ (self, exc_type, exc_value, traceback):
		return False
	
	def __contains__ (self, o):
		return sum ((1 for _o in self.iterator if _o == o)) > 0

	def new (self, iterator):
		"""Create a new stream using ``iterator''"""
		return self.__class__ (iterator)
	#
	# Intermediates
	#
	def filter (self, predicate):
		"""Create a new stream for each element ``predicate'' returns True"""
		return self.new (itertools.ifilter (predicate, self.iterator))
		
	def exclude (self, predicate):
		"""Create a new stream excluding each element ``prdicate'' returns True"""
		return self.new (itertools.ifilterfalse (predicate, self.iterator))
		
	def regexp (self, pattern, flags = 0, key = None, predicate = None):
		"""Create a new stream for each element matching
regular expression ``pattern''. ``flags'' is passed to re.compile. If
``predicate'' is not None, this must be a callable which accepts three
arguments, the compiled regular expression, the regular expression
matching object and the element itself."""
		expression = re.compile (pattern, flags) if not hasattr (pattern, 'match') else pattern
		if predicate is None:
			if key is None:
				return self.filter (lambda v: expression.match (v) is not None)
			return self.filter (lambda v: expression.match (key (v)) is not None)
		#
		if key is None:
			def regexper ():
				for elem in self.iterator:
					m = expression.match (elem)
					if m is not None:
						yield predicate (expression, m, elem)
		else:
			def regexper ():
				for elem in self.iterator:
					m = expression.match (key (elem))
					if m is not None:
						yield predicate (expression, m, elem)
		return self.new (regexper ())
	
	def map (self, predicate):
		"""Create a new stream for each element mapped with ``predicate''"""
		return self.new ((predicate (_v) for _v in self.iterator))
	
	def distinct (self, key = None):
		"""Create a new stream eleminating duplicate elements. If ``key'' is not None, it is used to build the key for checking identical elements"""
		seen = set ()
		if key is None:
			return self.filter (lambda v: not (v in seen or seen.add (v)))
		#
		def distincter (v):
			kv = key (v)
			return not (kv in seen or seen.add (kv))
		return self.filter (distincter)
		
	def sorted (self, key = None, reverse = False):
		"""Create a new stream with sorted elements ``key'' and ``reverse'' are passed to sorted()"""
		return self.new (sorted (self.iterator, key = key, reverse = reverse))
		
	def reversed (self):
		"""Create a new stream in reverse order"""
		return self.new (reversed (self))
		
	def peek (self, predicate = None):
		"""Create a new stream while executing ``predicate'' for each element

If predicate is None or a string, then each object is printed to
stderr, if predicate is a string, the output is prefixed by this
string, otherwise with a generic 'Peek'"""
		if predicate is None or type (predicate) in (str, unicode):
			format = '%s: %%r\n' % (predicate if predicate is not None else 'Peek', )
			predicate = lambda v: sys.stderr.write (format % (v, ))
		return self.filter (lambda v: predicate (v) or True)

	class Progress (object):
		def tick (self, elem):
			pass
		def final (self, count):
			pass
	
	def __progress (self, p, checkpoint):
		if type (p) in (str, unicode):
			class Progress (self.Progress):
				def __init__ (self, name, checkpoint = None):
					self.name = name
					self.checkpoint = checkpoint if checkpoint is not None and checkpoint > 0 else 100
					self.count = 0
					self.shown = -1
				def show (self):
					if self.count != self.shown:
						sys.stderr.write ('%s: Now at #%s\n' % (self.name, numfmt (self.count)))
						self.shown = self.count
				def tick (self, elem):
					self.count += 1
					if self.count % self.checkpoint == 0:
						self.show ()
				def final (self, count):
					self.show ()
			p = Progress (p)
		return p

	def progress (self, p, checkpoint = None):
		"""Create a new stream which copies the stream calling
the instance of ``p'' (an instance of agn.Stream.Progress or a string)
on each iteration. If ``p'' is a string, then ``checkpoint'' is an
optional integer value which specifies in which intervals the a
progression messsages is emitted'"""
		p = self.__progress (p, checkpoint)
		def progressor ():
			count = 0
			for elem in self.iterator:
				count += 1
				if p:
					p.tick (elem)
				yield elem
			if p:
				p.final (count)
		return self.new (progressor ())

	def __functions (self, *args):
		conditions = []
		while len (args) > 1:
			conditions.append (args[:2])
			args = args[2:]
		return (conditions, args[0] if args else None)
		
	def switch (self, *args):
		"""Create a new stream for using mulitple condition/mapping pairs.

If an odd number of arguments are passed, the last one is considered
as default mapping. Each pair is evaluated, if the condition returns a
true value, the mapping is executed and the return value of the
mapping is added to the new stream. No further mapping is applied. If
a default mapping is used and no other condition/mapping pair has
matched, this mapping is called for the element, otherwise the element
is added unmapped to the new stream."""
		(conditions, default) = self.__functions (*args)
		def switcher (elem):
			for (predicate, callback) in conditions:
				if predicate (elem):
					return callback (elem)
			return default (elem) if default else elem
		return self.new ((switcher (_e) for _e in self.iterator))
	
	def snap (self, target):
		"""Create a new stream saving each element in ``target'' (which must provide an append method)"""
		return self.peek (lambda v: target.append (v))
		
	def dropwhile (self, predicate):
		"""Create a new stream ignore all elements where ``predicate'' returns False up to first match"""
		return self.new (itertools.dropwhile (predicate, self.iterator))
	
	def takewhile (self, predicate):
		"""Create a new stream as long as ``predicate'' returns the first time False"""
		return self.new (itertools.takewhile (predicate, self.iterator))
	
	def limit (self, size):
		"""Create a new stream with a maximum of ``size'' elements"""
		return self.new (itertools.islice (self.iterator, 0, size))
		
	def skip (self, size):
		"""Create a new stream where the first ``size'' elements are skipped"""
		return self.new (itertools.islice (self.iterator, size, None))
	
	def remain (self, size):
		"""Create a new stream which contains the remaining ``size'' elements"""
		return self.new (collections.deque (self.iterator, maxlen = size))
	
	def slice (self, *args):
		"""Create a new stream selecting slice(*``args'')"""
		return self.new (itertools.islice (self.iterator, *args))
	
	def chain (self):
		"""Create a new stream flatten the elements of the stream."""
		return self.new (itertools.chain.from_iterable (self.iterator))
	#
	# Terminals
	#
	def dismiss (self):
		"""Dismiss all elements to terminate the stream"""
		collections.deque (self.iterator, maxlen = 0)

	def reduce (self, predicate, identity = __sentinel):
		"""Reduce the stream by applying ``predicate''. If ``identity'' is available, use this as the initial value"""
		if identity is self.__sentinel:
			return functools.reduce (predicate, self.iterator)
		return functools.reduce (predicate, self.iterator, identity)

	def __checkNo (self, no, where):
		if no is self.__sentinel:
			raise ValueError ('no value available for Stream.%s: empty result set' % where)
		return no
		
	def __position (self, finisher, no, position, where):
		collect = collections.defaultdict (int)
		for elem in self.iterator:
			collect[elem] += 1
		if collect:
			value = (sorted (collect.items (), key = lambda a: a[1])[position (len (collect))])[0]
			return value if finisher is None else finisher (value)
		return self.__checkNo (no, where)

	def first (self, finisher = None, no = __sentinel):
		"""Returns the first element, ``no'' if stream is empty. ``finisher'', if not None, is called on a found element"""
		try:
			rc = next (self.iterator)
			collections.deque (self.iterator, maxlen = 0)
			return rc if finisher is None else finisher (rc)
		except StopIteration:
			return self.__checkNo (no, 'first')
	
	def last (self, finisher = None, no = __sentinel):
		"""Returns the last element, ``no'' if stream is empty. ``finisher'', if not None, is called on a found element"""
		rc = collections.deque (self.iterator, maxlen = 1)
		if len (rc):
			return rc[0] if finisher is None else finisher (rc[0])
		return self.__checkNo (no, 'last')

	def most (self, finisher = None, no = __sentinel):
		"""Returns the element with the most often occurance, ``no'' if stream is empty. ``finisher'', if not None, is called on a found element"""
		return self.__position (finisher, no, lambda c: -1, 'most')
	
	def least (self, finisher = None, no = __sentinel):
		"""Returns the element with the least often occurance, ``no'' if stream is empty. ``finisher'', if not None, is called on a found element"""
		return self.__position (finisher, no, lambda c: 0, 'least')
	
	def sum (self):
		"""Returns the sum of the stream"""
		return sum (self.iterator)
	
	def min (self, no = __sentinel):
		"""Returns the minimum value of the stream"""
		try:
			return min (self.iterator)
		except ValueError:
			return self.__checkNo (no, 'min')

	def max (self, no = __sentinel):
		"""Returns the maximum value of the stream"""
		try:
			return max (self.iterator)
		except ValueError:
			return self.__checkNo (no, 'max')
	
	def count (self, *args):
		"""Without arguments returns the number of elements or return all elements which are part of ``args''"""
		if len (args) == 0:
			try:
				return len (self.iterator)
			except TypeError:
				return sum ((1 for _ in self.iterator))
		return sum ((1 for _v in self.iterator if _v in args))
		
	def any (self, predicate = bool):
		"""Return True if at least one element matches ``predicate''"""
		return sum ((1 for _v in self.iterator if predicate (_v))) > 0
		
	def all (self, predicate = bool):
		"""Return True if all element match ``predicate''"""
		class Counter (object):
			def __init__ (self):
				self.n = 0
		counter = Counter ()
		def predicate_and_count (v):
			counter.n += 1
			return predicate (v)
		matches = sum ((1 for _v in self.iterator if predicate_and_count (_v)))
		return matches == counter.n
	
	def each (self, predicate):
		"""Calls ``predicate'' on each element of the stream like java forEach()"""
		collections.deque (itertools.ifilter (predicate, self.iterator), maxlen = 0)
	
	def dispatch (self, *args, **kws):
		"""``args'' is a list of filter/handler functions

If the number of arguments is odd, then the last method is called if
no other pair had matched. For each pair the the filter method is
called and if it returns a boolean value, the handler is invoked and
the dispatching for this element ends. If the keyword argument
exclusive is False (the default), then each method where the filter
matches (and also the default method, if available) are called for
each element. """
		(conditions, default) = self.__functions (*args)
		exclusive = kws.get ('exclusive', False)
		def dispatcher (elem):
			for (predicate, callback) in conditions:
				if predicate (elem):
					callback (elem)
					if exclusive:
						return False
			if default:
				default (elem)
			return False
		collections.deque (itertools.ifilter (dispatcher, self.iterator), maxlen = 0)
	
	def list (self):
		"""Returns the stream as a list like java asList()"""
		return list (self.iterator)

	def tuple (self):
		"""Returns the stream as a tuple"""
		return tuple (self.iterator)

	def set (self):
		"""Returns the stream as set"""
		return set (self.iterator)
	
	def dict (self):
		"""Returns the stream as a dictionary"""
		return dict (self.iterator)
	
	def deque (self):
		"""Return the stream as collections.deque"""
		return collections.deque (self.iterator)
	
	def group (self, predicate = None, finisher = None):
		"""Returns a dict of grouped elements as separated by ``predicate'', optional modify the final dict by ``finisher''."""
		rc = collections.defaultdict (list)
		if predicate is None:
			for (key, value) in self.iterator:
				rc[key].append (value)
		else:
			for elem in self.iterator:
				(key, value) = predicate (elem)
				rc[key].append (value)
		return rc if finisher is None else finisher (rc)

	def join (self, separator, finisher = None):
		"""Returns a string joining all elements of stream with separator, optional apply ``finisher'' on result, if not None"""
		rc = separator.join ((str (_s) for _s in self.iterator))
		return rc if finisher is None else finisher (rc)
	
	class Collector (object):
		def supplier (self):
			return self
		
		def accumulator (self, supplier, element):
			pass
		
		def finisher (self, supplier, count):
			return supplier
			
	def collect (self, supplier, accumulator = None, finisher = None, progress = None, checkpoint = None):
		"""A generic terminal like the java collect().
``supplier'' is called for the initial element (or, if not callable,
as the inital element), ``accumulator'' is called with the supplied
value and each element of the stream and the optional ``finisher'' is
called with the supplied value and the number of elements processed to
create the final result

Optional ``supplier'' may be an instance of a subclass of
``Stream.Collector'' and its methods supplier, accumulator and
finisher are used instead. This is useful if you need some context
during collecting.

Optional ``progress'' is used to display the progression of the
collection where ``checkpoint'' definies the interval of showing the
progression. See method ``progress'' for further details."""
		if isinstance (supplier, self.Collector) and accumulator is None and finisher is None:
			accumulator = supplier.accumulator
			finisher = supplier.finisher
			supplier = supplier.supplier
		elif accumulator is None:
			raise TypeError ('missing accumulator')
		s = supplier () if callable (supplier) else supplier
		progress = self.__progress (progress, checkpoint)
		counter = 0
		for elem in self.iterator:
			accumulator (s, elem)
			counter += 1
			if progress:
				progress.tick (elem)
		if progress:
			progress.final (counter)
		return s if finisher is None else finisher (s, counter)

class Range (object):
	"""Create an interpreter for a positive numierc range expression

This class takes a generic expression, which is a comma separated list
of single expressions. Each single expression may be prefixed by an
exclamation mark to exclude these values. 
"""
	__slots__ = ['slices', 'low', 'high', 'default', 'onlyInverse']
	__splitter = re.compile (' *, *')
	class Slice (object):
		"""Stores a single expression for Range"""
		__slots__ = ['inverse', 'start', 'end']
		def __init__ (self, inverse, start, end):
			self.inverse = inverse
			self.start = start
			self.end = end
		
		def __contains__ (self, val):
			if (self.start is None or self.start <= val) and (self.end is None or val <= self.end):
				return True
			return False
		
		def __str__ (self):
			if self.start is not None and self.end is not None and self.start == self.end:
				s = str (self.start)
			else:
				infinite = lambda sign, value: value if value is not None else '%soo' % sign
				s = '%s-%s' % (infinite ('-', self.start), infinite ('', self.end))
			return '%s%s' % ('!' if self.inverse else '', s)
	
	def __init__ (self, expr, low = None, high = None, default = False):
		"""setup interpreter for range

the expression is a string represeantion of the desired range to
cover. Optional low and high values are used for open ranges (i.e. the
expression starts or ends with a dash). The default value is used, if
no single expression matched.
"""
		self.slices = []
		self.low = low
		self.high = high
		self.default = default
		self.onlyInverse = None
		if expr:
			for e in [_e for _e in self.__splitter.split (expr) if _e]:
				if e.startswith ('!'):
					inverse = True
					e = e[1:].lstrip ()
					if self.onlyInverse is None:
						self.onlyInverse = True
				else:
					inverse = False
					self.onlyInverse = False
				parts = [_e.strip () for _e in e.split ('-', 1)]
				try:
					if len (parts) == 2:
						start = int (parts[0]) if parts[0] else low
						end = int (parts[1]) if parts[1] else high
						if start is not None and end is not None and start > end:
							raise error ('%s: start value (%d) higher than end value (%d) in %s' % (expr, start, end, e))
					elif e == '*':
						start = low
						end = high
					else:
						start = int (e)
						end = start
					slice = self.Slice (inverse, start, end)
					self.slices.append (slice)
				except ValueError:
					raise error ('%s: invalid expression at: %s' % (expr, e))
			if self.onlyInverse and low is not None and high is not None:
				self.slices.insert (0, self.Slice (False, low, high))

	def __contains__ (self, val):
		rc = self.default or self.onlyInverse
		for slice in self.slices:
			if val in slice:
				rc = not slice.inverse
		return rc
	
	def __len__ (self):
		return len (self.slices)

	def __repr__ (self):
		return '%s (%s)' % (self.__class__.name, str (self))
		
	def __str__ (self):
		return (Stream.of (self.slices)
			.map (lambda s: str (s))
			.join (', ')
		)
	
	def mkset (self, low = None, high = None):
		"""Creates a set out of the parsed expression"""
		if low is None:
			low = self.low
			if low is None:
				raise error ('no low limit passed or set')
		if high is None:
			high = self.high
			if high is None:
				raise error ('no high limit passed or set')
		if self.onlyInverse and (self.low is None or self.high is None):
			extra = [self.Slice (False, low, high)]
		else:
			extra = []
		rc = set ()
		if self.default:
			rc.update (range (low, high + 1))
		for slice in extra + self.slices:
			start = slice.start if slice.start is not None else low
			end = slice.end if slice.end is not None else high
			if not slice.inverse:
				rc.update (range (start, end + 1))
			else:
				rc = rc.difference (range (start, end + 1))
		return rc

class Parameter (object):
	"""Parsing comma separated list of key value pairs

This class offers a parser for key-value pairs in a comma separted
list with different methods of persistance.
"""
	Codec = collections.namedtuple ('Codec', ['decode', 'encode', 'ctx'])
	skipPattern = re.compile (',[ \t]*')
	decodePattern = re.compile ('([a-z0-9_-]+)[ \t]*=[ \t]*"([^"]*)"', re.IGNORECASE | re.MULTILINE)
	def __decode (self, ctx, s, target):
		while s:
			mtch = self.skipPattern.match (s)
			if mtch is not None:
				s = s[mtch.end ():]
			mtch = self.decodePattern.match (s)
			if mtch is not None:
				(var, val) = mtch.groups ()
				target[var] = val
				s = s[mtch.end ():]
			else:
				break

	def __encode (self, ctx, source):
		for value in source.values ():
			if '"' in value:
				raise ValueError ('Unable to enocde %s due to "')
		return ', '.join (['%s="%s"' % (str (_d[0]), str (_d[1])) for _d in source.items ()])

	__valueDecodePattern = re.compile ('%([0-9A-Fa-f]{2})')
	__valueDecodeMap = {
		'0': 0, '1': 1, '2': 2, '3': 3, '4': 4,
		'5': 5, '6': 6, '7': 7, '8': 8, '9': 9,
		'a': 10, 'A': 10,
		'b': 11, 'B': 11,
		'c': 12, 'C': 12,
		'd': 13, 'D': 13,
		'e': 14, 'E': 14,
		'f': 15, 'F': 15
	}
	def __tokenDecode (self, m):
		token = m.group (1)
		return chr (self.__valueDecodeMap[token[0]] << 4 | self.__valueDecodeMap[token[1]])
	def __valueDecode (self, value):
		return self.__valueDecodePattern.sub (self.__tokenDecode, value)
	def __lineDecode (self, ctx, s, target):
		for elem in s.split ():
			try:
				(var, val) = elem.split ('=', 1)
				target[var] = self.__valueDecode (val)
			except ValueError:
				raise ValueError ('invalid element: %s' % elem)

	__valueEncodePattern = re.compile ('[^][!#$&()*+,./0123456789:;<=>@A-Z_a-z{|}~-]')
	__valueEncodeMap = {
		0: '0', 1: '1', 2: '2', 3: '3', 4: '4',
		5: '5', 6: '6', 7: '7', 8: '8', 9: '9',
		10: 'a', 11: 'b', 12: 'c',
		13: 'd', 14: 'e', 15: 'f'
	}
	def __tokenEncode (self, m):
		ch = ord (m.group ())
		return '%%%s%s' % (self.__valueEncodeMap[ch >> 4], self.__valueEncodeMap[ch & 0xf])
	def __valueEncode (self, value):
		if type (value) is unicode:
			value = value.encode ('UTF-8')
		elif value is None:
			value = ''
		elif type (value) != str:
			value = str (value)
		return self.__valueEncodePattern.sub (self.__tokenEncode, value)
	def __lineEncode (self, ctx, source):
		rc = []
		for (key, value) in source.items ():
			rc.append ('%s=%s' % (key, self.__valueEncode (value)))
		return ' '.join (rc)

	def __init__ (self, s = None):
		"""String to be parsed"""
		self.methods = {}
		self.addMethod (None, self.__decode, self.__encode)
		self.addMethod ('line', self.__lineDecode, self.__lineEncode)

		def jsonDecode (ctx, s, target):
			d = ctx['decoder'].decode (s)
			if type (d) != dict:
				raise ValueError ('JSON: input %r did not lead into a dictionary' % s)
			for (var, val) in d.items ():
				target[var] = val
		def jsonEncode (ctx, source):
			return ctx['encoder'].encode (source)
		ctx = self.addMethod ('json', jsonDecode, jsonEncode)
		ctx['decoder'] = json.JSONDecoder ()
		ctx['encoder'] = json.JSONEncoder ()
		self.data = {}
		if s is not None:
			self.loads (s)
	
	def __str__ (self):
		return '%s <%s>' % (self.__class__.__name__, Stream (self.data.iteritems ())
			.map (lambda kv: '%s=%r' % kv)
			.join (', ')
		)

	def __getitem__ (self, var):
		return self.data[var]

	def __setitem__ (self, var, val):
		self.data[var] = val

	def __contains__ (self, var):
		return var in self.data

	def __iter__ (self):
		return iter (self.data)

	def __call__ (self, var, dflt = None):
		"""Alias for Parameter.get"""
		return self.get (var, dflt)
	
	def set (self, newdata):
		"""Set whole content"""
		self.data = newdata

	def get (self, var, dflt = None):
		"""Get value as string"""
		try:
			return self.data[var]
		except KeyError:
			return dflt

	def iget (self, var, dflt = 0):
		"""Get value as integer"""
		try:
			return int (self.data[var])
		except (KeyError, ValueError, TypeError):
			return dflt

	def fget (self, var, dflt = 0.0):
		"""Get value as float"""
		try:
			return float (self.data[var])
		except (KeyError, ValueError, TypeError):
			return dflt

	def bget (self, var, dflt = False):
		"""Get value as boolean"""
		try:
			return atob (self.data[var])
		except KeyError:
			return dflt

	def addMethod (self, method, decoder, encoder):
		"""Add persistance coding

Add a new method for encoding/decoding data for persistance. If this
method already exists, it is overwritten wirh the new codec."""
		m = self.Codec (decoder, encoder, {})
		self.methods[method] = m
		return m.ctx

	def hasMethod (self, method):
		"""Check if a named method exists"""
		return method in self.methods

	def loads (self, s, method = None):
		"""Parse a persistent data chunk

parses the chunk passed in s according to the ``method''. If the
method is missing, the default method is used """
		self.data = {}
		if method not in self.methods:
			raise LookupError ('Unknown decode method: %s' % method)
		m = self.methods[method]
		m.decode (m.ctx, s, self.data)

	def dumps (self, method = None):
		"""Build a persistant representation of current state"""
		if method not in self.methods:
			raise LookupError ('Unknown encode method: %s' % method)
		m = self.methods[method]
		return m.encode (m.ctx, self.data)
	
	def clear (self):
		"""Clear out all data"""
		self.data = {}


class MailingParameter (Parameter):
	"""Derivated class to handle format as found in mailing_mt_tbl"""
	import	string
	
	startOfName = string.lowercase
	partOfName = string.lowercase + string.digits
	def __mpDecode (self, ctx, s, target):
		pos = 0
		state = 0
		while pos < len (s):
			ch = s[pos]
			if state == 0:
				if ch in self.startOfName:
					start = pos
					state = 1
			elif state == 1:
				if ch == '=':
					variable = s[start:pos]
					state = 2
				elif ch not in self.partOfName:
					state = 0
			elif state == 2:
				if ch == '"':
					state = 3
					value = ''
				else:
					state = 0
			elif state == 3:
				if ch == '"':
					target[variable] = value
					state = 0
				elif ch == '\\':
					state = 4
				else:
					value += ch
			elif state == 4:
				value += ch
				state = 3
			pos += 1

	__mpPattern = re.compile ('["\\\\]')
	def __mpEncode (self, ctx, source):
		out = []
		def escape (s):
			return self.__mpPattern.subn (lambda a: '\\%s' % a.group (0), s)[0]
		for (variable, value) in source.items ():
			out.append ('%s="%s"' % (variable, escape (value)))
		return ', '.join (out)

	def __init__ (self, s = None):
		super (MailingParameter, self).__init__ ()
		self.addMethod (None, self.__mpDecode, self.__mpEncode)
		if s is not None:
			self.loads (s)

class MediaType (object):
	"""Names of known mediatypes

These are a list of known mediatypes as symbolic names and also
available as string representation."""

	EMAIL = 0
	types = { 'E-Mail': EMAIL }
	rtypes = dict ([(_t[1], _t[0]) for _t in types.items ()])

class UserStatus (object):
	"""Names of known user statuses

These are a list of known unser statuses as symbolic names and also
available as string representation."""

	UNSET = 0
	ACTIVE = 1
	BOUNCE = 2
	ADMOUT = 3
	OPTOUT = 4
	WAITCONFIRM = 5
	BLACKLIST = 6
	SUSPEND = 7
	stati = {
		'unset': UNSET,
		'active': ACTIVE,
		'bounce': BOUNCE,
		'admout': ADMOUT,
		'optout': OPTOUT,
		'waitconfirm': WAITCONFIRM,
		'blacklist': BLACKLIST,
		'suspend': SUSPEND
	}
	rstati = dict ([(_s[1], _s[0]) for _s in stati.items ()])

	def findStatus (self, st, dflt = None):
		"""Find numeric value for a string representation

tries to find the numeric value for a given string. If not found it
tries to interpret the input as numeric value. In case of failure, the
default dflt is returned."""
		rc = None
		if type (st) in (str, unicode):
			try:
				rc = self.stati[st.lower ()]
			except KeyError:
				rc = None
		if rc is None:
			try:
				rc = int (st)
			except ValueError:
				rc = None
		if rc is None:
			rc = dflt
		return rc

	def findStatusName (self, stid):
		"""Finds the name for a numeric representation

If there is no name assigned to this status, None is returned."""
		try:
			rc = self.rstati[stid]
		except KeyError:
			rc = None
		return rc
#}}}
#
# 1.) Logging
#
#{{{
class Backlog (object):
	"""Stores log information without saving it
	
This class is used to collect a limited number of log informations
without writing it to a file. If then an error occurs these collected
informations can be written to a file so it can be easier to track
error without creating huge log files on every incarnation.
"""
	def __init__ (self, maxcount, level):
		"""Maximum number of entries to collect, highest level to store messages

If maxcount is None or 0, then an unlimited number of logfile entries
are collected.
"""
		self.maxcount = maxcount
		self.level = level
		self.backlog = []
		self.count = 0
		self.isSuspended = False
		self.asave = None

	def add (self, s):
		"""Adds a log line to the collections"""
		if not self.isSuspended and self.maxcount:
			if self.maxcount > 0 and self.count >= self.maxcount:
				self.backlog.pop (0)
			else:
				self.count += 1
			self.backlog.append (s)

	def suspend (self):
		"""Suspend collecting of logfile entries"""
		self.isSuspended = True

	def resume (self):
		"""Resume collectiong of logfile entries"""
		self.isSuspended = False

	def restart (self):
		"""Restart logfile collecting

Clears the existing log and resumes a possible suspended
collecting."""
		self.backlog = []
		self.count = 0
		self.isSuspended = False

	def save (self):
		"""If entries are collected, write them to the logfile"""
		if self.count > 0:
			self.backlog.insert (0, '-------------------- BEGIN BACKLOG --------------------\n')
			self.backlog.append ('--------------------  END BACKLOG  --------------------\n')
			logappend (self.backlog)
			self.backlog = []
			self.count = 0

	def autosave (self, level):
		"""If level is marked as auto save, save the content to logfile"""
		if not self.asave is None and level in self.asave:
			return True
		return False

	def addLevelForAutosave (self, level):
		"""Add a log level for auto saving backlog"""
		if self.asave is None:
			self.asave = [level]
		elif not level in self.asave:
			self.asave.append (level)

	def removeLevelForAutosave (self, level):
		"""Remove a log level from auto saving backlog"""
		if not self.asave is None and level in self.asave:
			self.asave.remove (level)
			if not self.asave:
				self.asave = None

	def clearLevelForAutosave (self):
		"""Clear all log levels for auto saving backlog"""
		self.asave = None

	def setLevelForAutosave (self, levels):
		"""Set levels for auto saving backlog"""
		if levels:
			self.asave = levels
		else:
			self.asave = None

LV_NONE = 0
LV_FATAL = 1
LV_REPORT = 2
LV_ERROR = 3
LV_WARNING = 4
LV_NOTICE = 5
LV_INFO = 6
LV_VERBOSE = 7
LV_DEBUG = 8
loglevel = LV_WARNING
logtable = {
	'FATAL':	LV_FATAL,
	'REPORT':	LV_REPORT,
	'ERROR':	LV_ERROR,
	'WARNING':	LV_WARNING,
	'NOTICE':	LV_NOTICE,
	'INFO':		LV_INFO,
	'VERBOSE':	LV_VERBOSE,
	'DEBUG':	LV_DEBUG
}
logtable.update (dict ([(_e[1], _e[0]) for _e in logtable.items ()]))
loghost = host
logname = None
logpath = None
outlevel = LV_FATAL
outstream = None
backlog = None
logpath = os.environ.get ('LOG_HOME', mkpath (base, 'var', 'log'))
if len (sys.argv) > 0:
	logname = os.path.basename (sys.argv[0])
	(_basename, _extension) = os.path.splitext (logname)
	if _extension.lower ().startswith ('.py'):
		logname = _basename
if not logname:
	logname = 'unset'
loglast = 0
#
def loglevelName (lvl):
	"""Returns a string representation for a log level"""
	return logtable.get (lvl, str (lvl))

def loglevelValue (lvlname):
	"""Tries to find a log level for a string representation"""
	name = lvlname.upper ().strip ()
	try:
		return logtable[name]
	except KeyError:
		for k in [_k for _k in logtable.keys () if type (_k) is str]:
			if k.startswith (name):
				return logtable[k]
	raise error ('Unknown log level name "%s"' % lvlname)

def logfilename (name = None, epoch = None, ts = None):
	"""Build a logfile in the defined conventions

a logfilename is created from the global logpath as the target
directory and is created using a timestamp
(YYYYMMDD-<hostname>-<name>.log

as timestamp the current time is used, but may be either set using an
own version in ts or by setting the epoch (seconds since 1.1.1970)
"""
	global	logname, logpath, loghost

	if name is None:
		name = logname
	if ts is None:
		if epoch is None:
			epoch = time.time ()
		now = time.localtime (epoch)
		ts = datetime.datetime (now[0], now[1], now[2])
	return mkpath (logpath, '%04d%02d%02d-%s-%s.log' % (ts.year, ts.month, ts.day, loghost, name))

def logdataname (name, epoch = None, ts = None):
	"""Build a logfile for storing data.

see ``logfilename'', in this case the name is required and no host
name is part of the final filename"""
	global	logpath

	if ts is None:
		if epoch is None:
			epoch = time.time ()
		now = time.localtime (epoch)
		ts = datetime.datetime (now[0], now[1], now[2])
	return mkpath (logpath, '%04d%02d%02d-%s.log' % (ts.year, ts.month, ts.day, name))

def logappend (s):
	"""Writes data to a logfile

s may either be a string or a list or tuple containing strings. If it
is neither then it tries to write a string representation of the
passed object."""
	global	loglast

	fname = logfilename ()
	try:
		fd = open (fname, 'a')
		if type (s) in (str, unicode):
			fd.write (s)
		elif type (s) in (list, tuple):
			for l in s:
				fd.write (l)
		else:
			fd.write (str (s) + '\n')
		fd.close ()
		loglast = int (time.time ())
	except Exception as e:
		err ('LOGFILE write failed[%r, %r, %s]: %r' % (type (e), e.args, fname, s))

def log (lvl, ident, s):
	"""add an entry to the logfile
	
main logfile writing method. Dependig on set global loglevel and
output methods, write logfile to file or defined output stream and
store, if configured, logentry to backlog storage.
"""
	global	loglevel, logname, backlog

	if not backlog is None and backlog.autosave (lvl):
		backlog.save ()
		backlogIgnore = True
	else:
		backlogIgnore = False
	if lvl <= loglevel or (lvl <= outlevel and not outstream is None) or (not backlog is None and lvl <= backlog.level):
		if not ident:
			ident = logname
		now = time.localtime (time.time ())
		lstr = '[%02d.%02d.%04d  %02d:%02d:%02d] %d %s/%s: %s\n' % (now[2], now[1], now[0], now[3], now[4], now[5], os.getpid (), loglevelName (lvl), ident, s)
		if lvl <= loglevel:
			logappend (lstr)
		else:
			backlogIgnore = False
		if lvl <= outlevel and not outstream is None:
			outstream.write (lstr)
			outstream.flush ()
		if not backlogIgnore and not backlog is None and lvl <= backlog.level:
			backlog.add (lstr)

def logexc (lvl, ident, s = None):
	"""logs an exception with optional mesage to logfile"""
	exc = sys.exc_info ()
	if not s is None:
		log (lvl, ident, s)
	if not None in exc:
		(typ, value, tb) = exc
		for l in [_l for _l in ('\n'.join (traceback.format_exception (typ, value, tb))).split ('\n') if _l]:
			log (lvl, ident, l)
		del tb

class Logcounter (object):
	"""Simplifies logging of progress"""
	def __init__ (self, level, ident, s = None, format = None, logat = 1000, target = 0):
		"""creates a logging counter

using level as the loglevel, ident for the identity, format for the
output, logat for the number to write a logfile entry and target, if
the final number is known. ``format'' is used as a template for the
class ``agn.Template'', and these variables are accessable:
	- $ident, $logat, $target: the values from the constructor
	- $count: the current number
	- $ftarget, $fcount: formated version of $target and $count
	- $last: last value displayed
"""
		self.level = level
		self.ident = ident
		if format is None:
			if target > 0:
				self.format = '#$fcount of $ftarget'
			else:
				self.format = '#$fcount'
		else:
			self.format = format
		self.logat = logat
		self.target = target
		self.count = 0
		self.last = None
		self.tmpl = Template (self.format)
		self.__log (s)

	def __ns (self):
		return {
			'ident': self.ident,
			'logat': self.logat,
			'target': self.target,
			'ftarget': numfmt (self.target),
			'count': self.count,
			'fcount': numfmt (self.count),
			'last': self.last
		}

	def __log (self, s):
		if s is not None:
			try:
				out = Template (s).fill (self.__ns ())
			except Exception:
				out = s
			log (self.level, self.ident, out)

	def __show (self):
		if self.count is not self.last:
			try:
				out = self.tmpl.fill (self.__ns ())
			except Exception:
				out = self.format
			log (self.level, self.ident, out)
			self.last = self.count

	def __call__ (self, incr = 1):
		"""Increases the counter, default with 1"""
		ocount = self.count
		self.count += incr
		if ocount // self.logat != self.count // self.logat:
			self.__show ()

	def reset (self, level = None, ident = None, s = None, format = None, logat = None, target = None):
		"""reset configuration to reuse an instance of the class"""
		if level is not None:
			self.level = level
		if ident is not None:
			self.ident = ident
		if logat is not None:
			self.logat = logat
		if format is not None:
			self.format = format
			self.tmpl = Template (self.format)
		if target is not None:
			self.target = target
		self.count = 0
		self.last = None
		self.__log (s)

	def done (self, s = None):
		"""final method should be called exactly one time at the end of counting"""
		self.__show ()
		self.__log (s)

def mark (lvl, ident, dur = 60):
	"""write a periodic mark to the logfile
	
write a mark to logfile, if there had been no change in the last given
minutes.
"""
	global	loglast

	now = int (time.time ())
	if loglast + dur * 60 < now:
		log (lvl, ident, '-- MARK --')

def backlogEnable (maxcount = 100, level = LV_DEBUG):
	"""enable global backlog storing"""
	global	backlog

	if maxcount == 0:
		backlog = None
	else:
		backlog = Backlog (maxcount, level)

def backlogDisable ():
	"""disable global backlog storing"""
	global	backlog

	backlog = None

def backlogRestart ():
	"""reset global backlog storing"""
	global	backlog

	if not backlog is None:
		backlog.restart ()

def backlogSave ():
	"""save current backlog"""
	global	backlog

	if not backlog is None:
		backlog.save ()

def backlogSuspend ():
	"""suspend collecting backlog informtion"""
	global	backlog

	if not backlog is None:
		backlog.suspend ()

def backlogResume ():
	"""resume collecting backlog information"""
	global	backlog

	if not backlog is None:
		backlog.resume ()

_emergencyFD = None
def emergencyEnable ():
	"""enable emergency log

in case there are no more free file handles, pre open an emergency
logfile for writing critical informations"""

	global	_emergencyFD
	
	if _emergencyFD is None:
		try:
			_emergencyFD = open (mkpath (base, 'var', 'run', '%s.emergency' % logname), 'a')
		except IOError:
			_emergencyFD = None
	return _emergencyFD is not None

def emergencyLog (msg):
	"""write an logfile entry to the emergency log, of open"""
	global	_emergencyFD

	if _emergencyFD is not None:
		now = time.localtime ()
		_emergencyFD.write ('[%04d-%02d-%02d  %02d:%02d:%02d] %s\n' % (now.tm_year, now.tm_mon, now.tm_mday, now.tm_hour, now.tm_min, now.tm_sec, msg))
		_emergencyFD.flush ()

def _logExcept (typ, value, tb):
	ep = traceback.format_exception (typ, value, tb)
	rc = 'CAUGHT EXCEPTION:\n'
	for p in ep:
		rc += p
	try:
		backlogSave ()
		log (LV_FATAL, 'except', rc)
		err (rc)
	except Exception as e:
		emergencyLog ('%s\n%s' % (str (e), rc))
		raise
sys.excepthook = _logExcept

class LogfileParser (object):
	"""Parses logfile in the AGNITAS standard format"""
	pattern = re.compile ('^\\[([0-9]{2})\\.([0-9]{2})\\.([0-9]{4})  ([0-9]{2}):([0-9]{2}):([0-9]{2})\\] ([0-9]+) ([A-Z]+)(/([^ :]+))?: (.*)$')
	Line = collections.namedtuple ('Line', ['timestamp', 'epoch', 'pid', 'level', 'level_name', 'area', 'msg'])
	def parseLine (self, line):
		"""parses a single line of a log file"""
		m = self.pattern.match (line.strip ())
		if m is None:
			return None
		g = m.groups ()
		timestamp = datetime.datetime (int (g[2]), int (g[1]), int (g[0]), int (g[3]), int (g[4]), int (g[5]))
		level = g[7]
		return self.Line (
			timestamp,
			time.mktime ((timestamp.year, timestamp.month, timestamp.day, timestamp.hour, timestamp.minute, timestamp.second, -1, -1, -1)),
			int (g[6]),
			level,
			loglevelName (level),
			g[9],
			g[10]
		)

def _logTraceback (sig, stack):
	rc = 'Traceback request:\n'
	for line in traceback.format_stack (stack):
		rc += line
	log (LV_REPORT, 'traceback', rc)
signal.signal (signal.SIGUSR2, _logTraceback)

class _RootHandler (logging.Handler):
	__levelMapper = {
		logging.CRITICAL:	LV_FATAL,
		logging.ERROR:		LV_ERROR,
		logging.WARNING:	LV_WARNING,
		logging.INFO:		LV_INFO,
		logging.DEBUG:		LV_DEBUG
	}
	__levels = sorted (__levelMapper.keys ())
			
	def emit (self, record):
		try:
			level = self.__levelMapper[record.levelno]
		except KeyError:
			use = logging.CRITICAL
			for n in self.__levels:
				if n < record.levelno:
					use = n
			level = self.__levelMapper[use]
		log (level, record.name, record.getMessage ())

_rootLogger = logging.getLogger ()
_rootLogger.setLevel (logging.NOTSET)
_rootHandler = _RootHandler ()
_rootLogger.addHandler (_rootHandler)
	
def log_filter (predicate):
	class Filter (logging.Filter):
		def filter (self, record):
			return predicate (record)
	_rootHandler.addFilter (Filter ())
#}}}
#
# 2.) Locking
#
#{{{
lockname = None
lockpath = os.environ.get ('LOCK_HOME', mkpath (base, 'var', 'lock'))

def _mklockpath (pgmname):
	global	lockpath

	return mkpath (lockpath, '%s.lock' % pgmname)

def lock (isFatal = True, id = None):
	"""create a lock

if isFatal is True, then a failure in gaining a lock is considered as
a fatail error and an exception is raised otherwise the function just
returns False. If id is None then the logname for the current running
program is used."""
	global	lockname, logname

	if lockname:
		return lockname
	if id is None:
		id = logname
	name = _mklockpath (id)
	s = '%10d\n' % (os.getpid ())
	report = 'Try locking using file "' + name + '"\n'
	n = 0
	while n < 2:
		n += 1
		try:
			if not lockname:
				fd = os.open (name, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o444)
				os.write (fd, s)
				os.close (fd)
				lockname = name
				report += 'Lock aquired\n'
		except OSError as e:
			if e.errno == errno.EEXIST:
				report += 'File exists, try to read it\n'
				try:
					fd = os.open (name, os.O_RDONLY)
					inp = os.read (fd, 32)
					os.close (fd)
					inp = (inp.split ('\n')[0]).rstrip ('\r\n')
					try:
						pid = int (inp)
					except ValueError:
						pid = -1
					if pid > 0:
						report += 'Locked by process %d, look if it is still running\n' % (pid)
						try:
							os.kill (pid, 0)
							report += 'Process is still running\n'
							n += 1
						except OSError as e:
							if e.errno == errno.ESRCH:
								report += 'Remove stale lockfile\n'
								try:
									os.unlink (name)
								except OSError as e:
									report += 'Unable to remove lockfile: ' + e.strerror + '\n'
							elif e.errno == errno.EPERM:
								report += 'Process is running and we cannot access it\n'
							else:
								report += 'Unable to check: ' + e.strerror + '\n'
					else:
						try:
							st = os.stat (name)
							if st.st_size == 0:
								report += 'Empty lock file, assuming due to crash or disk full\n'
								os.unlink (name)
						except OSError as e:
							report += 'Failed to check for or remove empty lock file: %s\n' % e.strerror
				except OSError as e:
					report += 'Unable to read file: ' + e.strerror + '\n'
			else:
				report += 'Unable to create file: ' + e.strerror + '\n'
	if not lockname and isFatal:
		raise error (report)
	return lockname

def lockwait (id = None, timeout = None):
	"""create a lock and wait until it is free

This uses ``lock'' to create a lock. If it fails, it will retry to
lock until either ``timeout'' (in seconds) is expired or forever (if
``timeout''is None). It returns the filename for the lockfile on
success or None on failure."""
	global	lockname
	
	while lockname is None:
		with Ignore (error):
			lock (id = id)
		if lockname is None:
			if timeout is not None:
				if timeout <= 0:
					break
				timeout -= 10
			time.sleep (10)
	return lockname
				
def unlock ():
	"""Releases an acquired lock"""
	global	lockname

	if lockname:
		try:
			os.unlink (lockname)
			lockname = None
		except OSError as e:
			if e.errno != errno.ENOENT:
				raise error ('Unable to remove lock: ' + e.strerror + '\n')

def signallock (program, signr = signal.SIGTERM):
	"""Send a signal to a process holding a lock"""
	rc = False
	report = ''
	fname = _mklockpath (program)
	try:
		with  open (fname, 'r') as fd:
			pline = fd.readline ()
			try:
				pid = int (pline.strip ())
				if pid > 0:
					try:
						os.kill (pid, signr)
						rc = True
						report = None
					except OSError as e:
						if e.errno == errno.ESRCH:
							report += 'Process %d does not exist\n' % pid
							try:
								os.unlink (fname)
							except OSError as e:
								report += 'Unable to remove stale lockfile %s %r\n' % (fname, e.args)
						elif e.errno == errno.EPERM:
							report += 'No permission to signal process %d\n' % pid
						else:
							report += 'Failed to signal process %d %r' % (pid, e.args)
				else:
					report += 'PIDFile contains invalid PID: %d\n' % pid
			except ValueError:
				report += 'Content of PIDfile is not valid: "%s"\n' % pline.rstrip ('\r\n')
	except IOError as e:
		if e.args[0] == errno.ENOENT:
			report += 'Lockfile %s does not exist\n' % fname
		else:
			report += 'Lockfile %s cannot be opened: %r\n' % (fname, e.args)
	return (rc, report)
#}}}
#
# 3.) file I/O
#
#{{{
def createPath (path, mode = 0o777):
	"""create a path and all missing elements"""
	if not os.path.isdir (path):
		try:
			os.mkdir (path, mode)
		except OSError as e:
			if e.args[0] != errno.EEXIST:
				if e.args[0] != errno.ENOENT:
					raise error ('Failed to create %s: %s' % (path, e.args[1]))
				elem = path.split (os.path.sep)
				target = ''
				for e in elem:
					target += e
					if target and not os.path.isdir (target):
						try:
							os.mkdir (target, mode)
						except OSError as e:
							raise error ('Failed to create %s at %s: %s' % (path, target, e.args[1]))
					target += os.path.sep

_archtab = {}
def mkArchiveDirectory (path, mode = 0o777):
	"""create an archive directory

if missing, creates a path to a directory with the given path as base
directory followed a directory based on the current day (YYYYMMDD) to
be used to archive files on a daily base."""
	global	_archtab

	tt = time.localtime (time.time ())
	ts = '%04d%02d%02d' % (tt[0], tt[1], tt[2])
	arch = mkpath (path, ts)
	if not arch in _archtab:
		try:
			st = os.stat (arch)
			if not stat.S_ISDIR (st[stat.ST_MODE]):
				raise error ('%s is not a directory' % arch)
		except OSError as e:
			if e.args[0] != errno.ENOENT:
				raise error ('Unable to stat %s: %s' % (arch, e.args[1]))
			try:
				os.mkdir (arch, mode)
			except OSError as e:
				raise error ('Unable to create %s: %s' % (arch, e.args[1]))
		_archtab[arch] = True
	return arch

class Filepos (object):
	"""read a file and keep track of position
	
this class can be used to read a file which is managed by another
process. So it stores the last position in a file and also detects if
a file is new (e.g. due to a log rotate) and starts reading from the
beginning of the new file.
"""
	_openfiles = []
	def __stat (self, stat_file):
		try:
			return os.stat (self.fname) if stat_file else os.fstat (self.fd.fileno ())
		except (OSError, IOError) as e:
			self.log (LV_WARNING, 'Failed to stat file %s: %s' % (self.fname if stat_file else ('open file #%d' % self.fd.fileno ()), e))
		return None

	def __open (self):
		try:
			pos = 0
			if os.access (self.info, os.F_OK):
				try:
					with open (self.info, 'r') as fd:
						line = fd.readline ().rstrip ('\r\n')
						parts = line.split (':')
						if len (parts) == 3:
							(self.inode, self.ctime, pos) = (int (_p) for _p in parts)
							self.log (LV_DEBUG, 'Read file information from %s: inode=%d, ctime=%d, pos=%d' % (self.info, self.inode, self.ctime, pos))
						else:
							fd.seek (0)
							self.log (LV_ERROR, 'Corrupted info file %s, try to remove it, content of file is: %s' % (self.info, fd.read (4096)))
							with Ignore (OSError):
								os.unlink (self.info)
				except (IOError, ValueError) as e:
					self.log (LV_ERROR, 'Failed to access %s: %s' % (self.info, e))
					raise error ('Unable to read info file %s: %s' % (self.info, e))
			try:
				self.fd = open (self.fname, 'r')
				st = self.__stat (False)
				if st:
					ninode = st.st_ino
					nctime = int (st.st_ctime)
					if ninode == self.inode:
						if st.st_size >= pos:
							if pos > 0:
								self.fd.seek (pos)
								self.log (LV_DEBUG, 'Seek to last position %d of %d of current size' % (pos, st.st_size))
						else:
							self.log (LV_ERROR, 'Old position %d is larger than current file size %d, file had been truncated?' % (pos, st.st_size))
					else:
						self.log (LV_INFO, 'File inode has changed from %d to %d, start reading from beginning' % (self.inode, ninode))
					self.inode = ninode
					self.ctime = nctime
				else:
					raise error ('Failed to stat %s' % self.fname)
			except IOError as e:
				self.log (LV_WARNING, 'Failed to access %s: %s' % (self.fname, e))
				raise error ('Unable to open %s: %s' % (self.fname, e))
		except Exception:
			if self.fd:
				self.fd.close ()
				self.fd = None
			raise
		#
		if self not in self._openfiles:
			self._openfiles.append (self)

	def __init__ (self, fname, info, checkpoint = 1000):
		"""Opens a file and stores every ``checkpoint'' lines the current position in info"""
		self.fname = fname
		self.info = info
		self.checkpoint = checkpoint
		self.fd = None
		self.inode = -1
		self.ctime = 0
		self.count = 0
		self.__open ()
	
	def __iter__ (self):
		for line in iter (lambda: self.readline (), None):
			yield line
		self.save ()

	def save (self):
		"""Save current position for recovery"""
		for state in 0, 1:
			try:
				with open (self.info, 'w') as fd:
					fd.write ('%d:%d:%d\n' % (self.inode, self.ctime, self.fd.tell ()))
				break
			except IOError as e:
				self.log (LV_ERROR, 'Failed to write %s: %s' % (self.info, e))
				if state == 0:
					with Ignore (OSError):
						os.unlink (self.info)
				else:
					raise
		self.count = 0

	def close (self):
		"""closes the file"""
		if self.fd:
			self.save ()
			self.fd.close ()
			self.fd = None
		with Ignore (ValueError):
			self._openfiles.remove (self)

	def __is_same_file (self):
		st = self.__stat (True)
		return st and st.st_ino == self.inode and int (st.st_ctime) == self.ctime and st.st_size > self.fd.tell ()

	def __readline (self):
		line = self.fd.readline ()
		if line != '':
			self.count += 1
			if self.count >= self.checkpoint:
				self.save ()
			return line.rstrip ('\r\n')
		else:
			return None

	def readline (self):
		"""reads a line from the file

returns the line read with trailing line termination removed or None
if no more lines are available."""
		line = self.__readline ()
		if line is None and self.__is_same_file ():
			self.close ()
			self.__open ()
			line = self.__readline ()
		return line
	
	def log (self, level, message):
		pass
#
def die (lvl = LV_FATAL, ident = None, s = None):
	"""Terminates process and releases global resources"""
	if s:
		err (s)
		log (lvl, ident, s)
	for st in Filepos._openfiles[:]:
		st.close ()
	unlock ()
	sys.exit (1)
rip = die
#}}}
#
# 4.) Parallel process wrapper
#
#{{{
class Parallel (object):
	"""Simple parallel framework using multiprocessing"""
	class Process (multiprocessing.Process):
		def __init__ (self, method, args, name):
			multiprocessing.Process.__init__ (self, name = name)
			self.method = method
			self.args = args
			self.logname = name
			self.resultq = multiprocessing.Queue ()
			self.value = None

		def run (self):
			if self.logname is not None:
				global	logname

				logname = '%s-%s' % (logname, self.logname.lower ().replace ('/', '_'))
				self.logname = None

			if self.args is None:
				rc = self.method ()
			else:
				rc = self.method (*self.args)
			self.resultq.put (rc)

		def result (self):
			if self.value is None:
				if not self.resultq.empty ():
					self.value = self.resultq.get ()
			return self.value

	def __init__ (self):
		self.active = set ()

	def fork (self, method, args = None, name = None):
		"""Setup and start a subprocess to call a method"""
		p = self.Process (method, args, name)
		self.active.add (p)
		p.start ()
		return p

	def living (self):
		"""Return number of active subprocesses"""
		return (len ([_p for _p in self.active if _p.is_alive ()]), len (self.active))

	def wait (self, name = None, timeout = None, count = None):
		"""Wait for one or all (if name is None) active processes"""
		done = set ()
		rc = {}
		for p in self.active:
			if name is None or p.name == name:
				p.join (timeout)
				if timeout is None or not p.is_alive ():
					rc[p.name] = (p.exitcode, p.result ())
					done.add (p)
					if count is not None:
						count -= 1
						if count == 0:
							break
		self.active = self.active.difference (done)
		return rc

	def ready (self):
		"""Check if at least one process has finished and return its value"""
		for p in self.active:
			if not p.is_alive ():
				rc = self.wait (name = p.name, count = 1)
				if rc:
					return rc.values ()[0]
		return None

	def term (self, name = None):
		"""Terminate one or all (if name is None) active processes"""
		for p in self.active:
			if name is None or p.name == name:
				p.terminate ()
		return self.wait (name = name)
#}}}
#
# 5.) mailing/httpclient
#
#{{{
def mailsend (relay, sender, receivers, headers, body,
	myself = fqdn
):
	"""Send mail via SMTP
	
``relay'' is used as the target SMTP server, ``sender'' must be a
valid e-mail address, ``receivers'' can either be a list or tuple of
valid e-mail addresses or a string as a valid e-mail address.
``headers'' are optional mail headers and ``body'' is the content of
the mail. ``myself'' is using during SMTP greeting as my local name."""
	codetype = lambda code: code / 100
	rc = False
	if not relay:
		return (rc, 'Missing relay\n')
	if not sender:
		return (rc, 'Missing sender\n')
	if type (receivers) in (str, unicode):
		receivers = [receivers]
	if len (receivers) == 0:
		return (rc, 'Missing receivers\n')
	if not body:
		return (rc, 'Empty body\n')
	report = ''
	try:
		s = smtplib.SMTP (relay)
		(code, detail) = s.helo (myself)
		if codetype (code) != 2:
			raise smtplib.SMTPResponseException (code, 'HELO ' + myself + ': ' + detail)
		else:
			report += 'HELO %s sent\n%d %s recvd\n' % (myself, code, detail)
		(code, detail) = s.mail (sender)
		if codetype (code) != 2:
			raise smtplib.SMTPResponseException (code, 'MAIL FROM:<' + sender + '>: ' + detail)
		else:
			report += 'MAIL FROM:<%s> sent\n%d %s recvd\n' % (sender, code, detail)
		for r in receivers:
			(code, detail) = s.rcpt (r)
			if codetype (code) != 2:
				raise smtplib.SMTPResponseException (code, 'RCPT TO:<' + r + '>: ' + detail)
			else:
				report += 'RCPT TO:<%s> sent\n%d %s recvd\n' % (r, code, detail)
		mail = ''
		hsend = False
		hrecv = False
		if headers:
			for h in headers:
				if len (h) > 0 and h[-1] != '\n':
					h += '\n'
				if not hsend and len (h) > 5 and h[:5].lower () == 'from:':
					hsend = True
				elif not hrecv and len (h) > 3 and h[:3].lower () == 'to:':
					hrecv = True
				mail = mail + h
		if not hsend:
			mail += 'From: ' + sender + '\n'
		if not hrecv:
			recvs = ''
			for r in receivers:
				if recvs:
					recvs += ', '
				recvs += r
			mail += 'To: ' + recvs + '\n'
		mail += '\n' + body
		(code, detail) = s.data (mail)
		if codetype (code) != 2:
			raise smtplib.SMTPResponseException (code, 'DATA: ' + detail)
		else:
			report += 'DATA sent\n%d %s recvd\n' % (code, detail)
		s.quit ()
		report += 'QUIT sent\n'
		rc = True
	except smtplib.SMTPConnectError as e:
		report += 'Unable to connect to %s, got %d %s response\n' % (relay, e.smtp_code, e.smtp_error)
	except smtplib.SMTPServerDisconnected:
		report += 'Server connection lost\n'
	except smtplib.SMTPResponseException as e:
		report += 'Invalid response: %d %s\n' % (e.smtp_code, e.smtp_error)
	except socket.error as e:
		report += 'General socket error: %r\n' % (e.args, )
	except Exception as e:
		report += 'General problems during mail sending: %r, %r\n' % (type (e), e.args)
	return (rc, report)

def httpget (hostname, port, query):
	"""Minimal http client

this can be used to send a GET request via http and expect an answer
with a flag, followed by a colon and some data. If the flag is a plus
sign, then the request had been successful, otherwise it is considered
as failed."""
	success = False
	data = ''
	try:
		conn = httplib.HTTPConnection (hostname, port, True)
		conn.connect ()
		conn.request ('GET', query)
		answ = conn.getresponse ()
		data = answ.read ()
		n = data.find (':')
		if n != -1:
			st = data[:n]
			if st[0] == '+':
				success = True
			data = data[n + 1:].strip ()
		else:
			data = 'Invalid response: ' + data.strip ()
	except Exception as e:
		data = 'Caught exception %r: %r' % (type (e), e.args)
	return (success, data)

URLEncode = urllib.quote
def URLEncodeHash (parm):
	"""Encode a dict into an URL query string"""
	return '&'.join (['%s=%s' % (_p, URLEncode (parm[_p])) for _p in parm.keys ()])
#}}}
#
# 6.) system interaction
#
#{{{
def call (*args, **kwargs):
	"""Replacement for signal safe subprocess.call

while subprocess.call returns from wait, if a signal is receivied,
this version keep waiting."""
	rc = None
	pp = subprocess.Popen (*args, **kwargs)
	while rc is None:
		try:
			rc = pp.wait ()
		except OSError as e:
			if e.args[0] == errno.ECHILD:
				rc = -1
	return rc

def fileAccess (path):
	"""Check if a process access a file

returns two lists, the first is a list of process-ids which accessing
the path, the second is a list of failures (e.g. due to missing
permissions) while trying to determinate the access to the file."""
	if system != 'linux':
		raise error ('lsof only supported on linux')
	try:
		st = os.stat (path)
	except OSError as e:
		raise error ('failed to stat "%s": %r' % (path, e.args))
	device = st[stat.ST_DEV]
	inode = st[stat.ST_INO]
	rc = []
	fail = []
	seen = {}
	isnum = re.compile ('^[0-9]+$')
	for pid in [_p for _p in os.listdir ('/proc') if not isnum.match (_p) is None]:
		bpath = '/proc/%s' % pid
		checks = ['%s/%s' % (bpath, _c) for _c in ('cwd', 'exe', 'root')]
		try:
			fdp = '%s/fd' % bpath
			for fds in os.listdir (fdp):
				checks.append ('%s/%s' % (fdp, fds))
		except OSError as e:
			fail.append ([e.args[0], '%s/fd: %s' % (bpath, e.args[1])])
		try:
			fd = open ('%s/maps' % bpath, 'r')
			for line in fd:
				parts = line.split ()
				if len (parts) == 6 and parts[5].startswith ('/'):
					checks.append (parts[5].strip ())
			fd.close ()
		except IOError as e:
			fail.append ([e.args[0], '%s/maps: %s' % (bpath, e.args[1])])
		for check in checks:
			try:
				if seen[check]:
					rc.append (pid)
			except KeyError:
				seen[check] = False
				cpath = check
				try:
					fpath = None
					count = 0
					while fpath is None and count < 128 and cpath.startswith ('/'):
						count += 1
						st = os.lstat (cpath)
						if stat.S_ISLNK (st[stat.ST_MODE]):
							cpath = os.readlink (cpath)
						else:
							fpath = cpath
					if not fpath is None and st[stat.ST_DEV] == device and st[stat.ST_INO] == inode:
						rc.append (pid)
						seen[check] = True
				except OSError as e:
					fail.append ([e.args[0], '%s: %s' % (cpath, e.args[1])])
	return (rc, fail)

def fileExistance (fname, content, mode, report = None):
	"""Writes content to file if changed

tries to read the original content of the file and only writes the new
content to the file, if does not exist or the content has changed."""
	rc = False
	try:
		fd = open (fname, 'rb')
		ocontent = fd.read ()
		fd.close ()
	except IOError as e:
		ocontent = None
		if report is not None:
			report.append ('Failed to read original file: %r' % (e.args, ))
	try:
		if ocontent is None or ocontent != content:
			fd = open (fname, 'wb')
			fd.write (content)
			fd.close ()
		st = os.stat (fname)
		if stat.S_IMODE (st.st_mode) != mode:
			os.chmod (fname, mode)
		rc = True
	except (IOError, OSError) as e:
		report.append ('Failed to update file: %r' % (e.args, ))
	return rc

def copen (path, mode = 'r'):
	"""Opens a file according to its extension

this opens a file dependig of the extension of the filename (and if a
the required module is available). Fall back to standard open() if no
match is found."""
	if path.endswith ('.gz'):
		return gzip.open (path, mode)
	elif path.endswith ('.bz2') and bz2 is not None:
		return bz2.BZ2File (path, mode)
	return open (path, mode)

def cstreamopen (path):
	"""Openes a file for reading using copen and return a stream to process the file."""
	return Stream.defer (copen (path), lambda o: o.close ())

def gopen (path, mode):
	"""Tries to open a compressed version of a file, if available

looks up, if there is a compressed version of the file available and
opens this, otherwiese falls back to the standard open()."""
	for (ext, module, method) in ('.gz', gzip, lambda: gzip.open), ('.bz2', bz2, lambda: bz2.BZ2File):
		if module is not None:
			npath = '%s%s' % (path, ext)
			if os.path.isfile (npath):
				return method () (npath, mode)
	return open (path, mode)
#}}}
#
# 7.) Validate UIDs
#
#{{{
class UID (object):
	"""Handles V2++ agnUID (current version)

this handles the current version 3 of the agnUID. It is simular to
UID, but has some more requirements and some changes. To create a new
UID, you must set these instance variables, on parsing they are filled
by the parser:
	- companyID: the companyID for the UID
	- mailingID: the assigned mailingID for the UID
	- customerID: the customer to whom the UID belongs
	- URLDID: (optional) the assigned ID of the URL (see rdir_url_tbl)

In both cases you must set the timestamp (from
mailing_tbl.creation_date) using the method:
	- setTimestamp()
"""
	__slots__ = ['timestamp', 'timestamps', 'secret', 'version', 'licenceID', 'companyID', 'mailingID', 'customerID', 'URLID', 'bitOption', 'prefix']
	versions = (0, 2, 3)
	defaultVersion = versions[0]
	symbols = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_'
	def __init__ (self):
		self.timestamp = 0
		self.timestamps = [0, 0]
		self.secret = None
		self.version = self.defaultVersion
		self.licenceID = licence
		self.companyID = 0
		self.mailingID = 0
		self.customerID = 0
		self.URLID = 0
		self.bitOption = 0
		self.prefix = None
	
	def __str__ (self):
		return '%s <%s>' % (
			self.__class__.__name__,
			Stream.of (
				('version', self.version),
				('licence', self.licenceID),
				('company', self.companyID),
				('mailing', self.mailingID),
				('customer', self.customerID),
				('urlid', self.URLID),
				('bitOption', Stream.loop (
					(self.bitOption, ''),
					lambda bv: bool (bv[0]),
					lambda bv: (bv[0] >> 1, ('1' if bv[0] & 0x1 else '0') + bv[1]),
					finalizer = lambda bv: bv[1] if bv[1] else None
				)),
				('prefix', self.prefix)
			)
			.filter (lambda kv: kv[1] is not None)
			.map (lambda kv: '%s=%r' % kv)
			.join (', ')
		)

	def __iencode (self, n):
		if n <= 0:
			rc = self.symbols[0]
		else:
			rc = ''
			while n > 0:
				rc = self.symbols[n & 0x3f] + rc
				n >>= 6
		return rc

	def __idecode (self, s):
		n = 0
		for ch in s:
			n <<= 6
			v = self.symbols.find (ch)
			if v == -1:
				raise error ('Illegal charater in numeric value')
			n += v
		return n

	invalidReplacement = toutf8 (u'\ufffd')
	def __normalizeSecret (self):
		if not self.secret:
			return ''
		#
		# Wild hack to interpret invalid character in secret key
		# as handled in oracle/jdbc/java
		self.secret
		try:
			fromutf8 (self.secret)
			rc = self.secret
		except UnicodeDecodeError:
			builder = []
			skip = 0
			for c in self.secret:
				if skip > 0:
					skip -= 1
				else:
					ch = ord (c)
					if ch < 128:
						builder.append (c)
					else:
						if ch < 240:
							if ch >= 224:
								skip = 2
							elif ch >= 192:
								skip = 1
						builder.append (self.invalidReplacement)
			rc = ''.join (builder)
		return rc

	def __checkVersion (self, version = None):
		if version is None:
			version = self.version
		if version not in self.versions:
			raise error ('uid: unsupported version %r detected, known versions are: %s' % (version, ', '.join ([str (_v) for _v in self.versions])))

	def __makeSignature (self):
		sig = []
		if self.prefix:
			sig.append (self.prefix)
		sig.append (self.version)
		sig.append (self.licenceID)
		sig.append (self.mailingID)
		sig.append (self.customerID)
		sig.append (self.URLID)
		if self.version == 3:
			sig.append (self.bitOption)
		sig.append (self.__normalizeSecret ())
		if self.version == 0:
			dig = hashlib.md5 ()
		elif self.version in (2, 3):
			dig = hashlib.sha512 ()
		dig.update (Stream (sig).join ('.'))
		return base64.urlsafe_b64encode (dig.digest ()).replace ('=', '')

	def __date2timestamp (self, year, month, day, hour, minute, second, dst = -1):
		return int (time.mktime ((year, month, day, hour, minute, second, -1, -1, dst))) * 1000

	def setTimestamp (self, timestamp):
		"""Set timestamp for mailing

this is the creation date of the assigned mailing, either as type
datetime.datetime or time.struct_time."""
		if type (timestamp) == datetime.datetime:
			timestamp = self.__date2timestamp (timestamp.year, timestamp.month, timestamp.day, timestamp.hour, timestamp.minute, timestamp.second)
		elif type (timestamp) == time.struct_time:
			timestamp = self.__date2timestamp (timestamp.tm_year, timestamp.tm_mon, timestamp.tm_mday, timestamp.tm_hour, timestamp.tm_min, timestamp.tm_sec, timestamp.tm_isdst)
		self.timestamp = timestamp
		self.timestamps = [self.timestamp & 0xffff, (self.timestamp * 37) & 0xffff]

	def createUID (self):
		"""Creates a new UID

returns a newly created UID from the set instance variables."""
		self.__checkVersion ()
		uid = []
		if self.prefix:
			uid.append (self.prefix)
		uid.append (self.__iencode (self.version))
		uid.append (self.__iencode (self.licenceID))
		uid.append (self.__iencode (self.mailingID))
		if self.version in (0, 2):
			uid.append (self.__iencode (self.customerID ^ self.timestamps[0]))
			uid.append (self.__iencode (self.URLID ^ self.timestamps[1] ^ self.companyID))
		elif self.version == 3:
			uid.append (self.__iencode (self.customerID))
			uid.append (self.__iencode (self.URLID))
			uid.append (self.__iencode (self.bitOption))
		uid.append (self.__makeSignature ())
		return '.'.join (uid)

	def parseUID (self, uid, callback, doValidate = True):
		"""Parses a UID

parses a UID and fill the instance variables from the UID. As you do
normally not know the mailingID at this stage, you can not call
setTimestamp() before as required. So you have to pass a callable in
callback which is called with this class instance as its parameter. So
you can retrieve the mailingID by using the attribute "mailingID" from
this variable and provide the timestamp information.

If doValidate is True, validation of the signature is performed and an
exception is thrown, if it is not valid."""
		elem = uid.split ('.')
		if not elem:
			raise error ('Empty UID')
		if len (elem) == 6:
			hasPrefix = False
		elif len (elem) == 7:
			try:
				temp = self.__idecode (elem[0])
				self.__checkVersion (temp)
				if temp in (0, 2):
					hasPrefix = True
				else:
					hasPrefix = False
			except error:
				hasPrefix = True
		elif len (elem) == 8:
			hasPrefix = True
		else:
			raise error ('Invalid formated UID')
		if hasPrefix:
			self.prefix = elem[0]
			elem = elem[1:]
		else:
			self.prefix = None
		#
		version = self.__idecode (elem[0])
		self.__checkVersion (version)
		#
		self.version = version
		self.licenceID = self.__idecode (elem[1])
		self.mailingID = self.__idecode (elem[2])
		if callback is not None and not callback (self):
			raise error ('Failed to find secrets for mailingID %d' % self.mailingID)
		if self.version in (0, 2):
			self.customerID = self.__idecode (elem[3]) ^ self.timestamps[0]
			self.URLID = self.__idecode (elem[4]) ^ self.timestamps[1] ^ self.companyID
			self.bitOption = 0
		elif self.version == 3:
			self.customerID = self.__idecode (elem[3])
			self.URLID = self.__idecode (elem[4])
			self.bitOption = self.__idecode (elem[5])
		if doValidate and elem[-1] != self.__makeSignature ():
			raise error ('Signature mismatch')
XUID = UID

class PubID (object):
	"""Handled public id agnPUBID

this handles the public id agnPUBID which is used for the anon
preview."""
	scramble = 'w5KMCHOXE_PTuLcfF6D1ZI3BydeplQaztVAnUj0bqos7k49YgWhxiS-RrGJm8N2v'
	sourceInvalid = re.compile ('[^0-9a-zA-Z_-]')

	def __init__ (self):
		self.mailingID = 0
		self.customerID = 0
		self.source = None
		self.selector = None
	
	def __str__ (self):
		return '%s <%s>' % (
			self.__class__.__name__,
			Stream.of (
				('mailing', self.mailingID),
				('customer', self.customerID),
				('source', self.source),
				('selector', self.selector))
			.filter (lambda kv: kv[1] is not None)
			.map (lambda kv: '%s=%r' % kv)
			.join (', ')
		)

	def __source (self, source):
		if not source is None:
			if len (source) > 20:
				source = source[:20]
			source = self.sourceInvalid.subn ('_', source)[0]
		return source

	def __checksum (self, s):
		cs = 12
		for ch in s:
			cs += ord (ch)
		return self.scramble[cs & 0x3f]

	def __encode (self, s):
		if type (s) is unicode:
			s = toutf8 (s)
		slen = len (s)
		temp = []
		n = 0
		while n < slen:
			chk = s[n:n + 3]
			d = ord (chk[0]) << 16
			if len (chk) > 1:
				d |= ord (chk[1]) << 8
				if len (chk) > 2:
					d |= ord (chk[2])
			n += 3
			temp.append (self.scramble[(d >> 18) & 0x3f])
			temp.append (self.scramble[(d >> 12) & 0x3f])
			temp.append (self.scramble[(d >> 6) & 0x3f])
			temp.append (self.scramble[d & 0x3f])
		temp.insert (5, self.__checksum (temp))
		return ''.join (temp)

	def __decode (self, s):
		rc = None
		slen = len (s)
		if slen > 5 and (slen - 1) & 3 == 0:
			check = s[5]
			s = s[:5] + s[6:]
			if check == self.__checksum (s):
				slen -= 1
				collect = []
				ok = True
				n = 0
				while n < slen and ok:
					v = [self.scramble.find (s[_c]) for _c in range (n, n + 4)]
					n += 4
					if -1 in v:
						ok = False
					else:
						d = (v[0] << 18) | (v[1] << 12) | (v[2] << 6) | v[3]
						collect.append (chr ((d >> 16) & 0xff))
						collect.append (chr ((d >> 8) & 0xff))
						collect.append (chr (d & 0xff))
				if ok:
					while collect and collect[-1] == '\0':
						collect = collect[:-1]
					rc = ''.join (collect)
		return rc

	def createID (self, mailingID = None, customerID = None, source = None, selector = None):
		"""Creates a new agnPUBID

creates a new public id which can be used for the anon preview. Either
set the variables here or set them by assigning the corrosponding
instance variables."""
		if mailingID is None:
			mailingID = self.mailingID
		if customerID is None:
			customerID = self.customerID
		if source is None:
			source = self.source
			if source is None:
				source = ''
		if selector is None:
			selector = self.selector
			if not selector:
				selector = None
		src = '%d;%d;%s' % (mailingID, customerID, self.__source (source))
		if selector is not None:
			src += ';%s' % selector
		return self.__encode (src)

	def parseID (self, pid):
		"""Parses an agnPUBID
		
if parsing had been successful, the instance variable containing the
parsed content and the method returns True, otherwise the content of
the instance variables is undefinied and the method returns False."""
		rc = False
		dst = self.__decode (pid)
		if dst is not None:
			parts = dst.split (';', 3)
			if len (parts) in (3, 4):
				with Ignore (ValueError):
					mailingID = int (parts[0])
					customerID = int (parts[1])
					source = parts[2]
					if len (parts) > 3:
						selector = parts[3]
					else:
						selector = None
					if mailingID > 0 and customerID > 0:
						self.mailingID = mailingID
						self.customerID = customerID
						if source:
							self.source = source
						else:
							self.source = None
						self.selector = selector
						rc = True
		return rc
#}}}
#
# 8.) Mail transport / generation
#
#{{{
class METAFile (object):
	"""Handles XML files from mail generation

this class can help to interpret the filenames of files created by the
merger which contains serveral informations."""
	splitter = re.compile ('[^0-9]+')
	def __init__ (self, path):
		self.setPath (path)

	def __makeTimestamp (self, epoch):
		tt = time.localtime (epoch)
		return '%04d%02d%02d%02d%02d%02d' % (tt[0], tt[1], tt[2], tt[3], tt[4], tt[5])

	def __parseTimestamp (self, ts):
		if ts[0] == 'D' and len (ts) == 15:
			rc = ts[1:]
		else:
			try:
				rc = self.__makeTimestamp (int (ts))
			except ValueError:
				rc = None
		return rc

	def __error (self, s):
		if self.error is None:
			self.error = [s]
		else:
			self.error.append (s)
		self.valid = False

	def isReady (self, epoch = None):
		"""Checks if file is ready for sending

according to the coded timestamp of the filename, this method checks,
if the file is ready for sending."""
		if epoch is None:
			ts = self.__makeTimestamp (time.time ())
		elif type (epoch) in (str, unicode):
			ts = epoch
		elif type (epoch) in (int, long):
			ts = self.__makeTimestamp (epoch)
		else:
			raise TypeError ('Expecting either None, string or numeric, got %r' % type (epoch))
		return self.valid and self.timestamp <= ts

	def getError (self):
		"""Returns errors

if there are errors during parsing, this returns a string with a list
of all errors or the string "no error", if there had been no error.
Primary used for logging."""
		if self.error is None:
			return 'no error'
		return ', '.join (self.error)

	def setPath (self, path):
		"""Sets the path to the XML file

this sets the path to the XML file and parses the coded content of the
filename."""
		self.valid = False
		self.error = None
		self.path = path
		self.directory = None
		self.filename = None
		self.extension = None
		self.basename = None
		self.licence = None
		self.company = None
		self.timestamp = None
		self.mailid = None
		self.mailing = None
		self.blocknr = None
		self.blockid = None
		self.single = None
		if path is not None:
			self.directory = os.path.dirname (self.path)
			self.filename = os.path.basename (self.path)
			n = self.filename.find ('.')
			if n != -1:
				self.extension = self.filename[n + 1:]
				self.basename = self.filename[:n]
			else:
				self.basename = self.filename
			parts = self.basename.split ('=')
			if len (parts) != 6:
				self.__error ('Invalid format of input file')
			else:
				self.valid = True
				n = parts[0].find ('-')
				if n != -1:
					try:
						self.licence = int (parts[0][n + 1:])
					except ValueError:
						self.licence = None
						self.__error ('Unparsable licence ID in "%s" found' % parts[0])
				else:
					self.licence = licence
				cinfo = parts[2].split ('-')
				try:
					self.company = int (cinfo[0])
				except ValueError:
					self.company = None
					self.__error ('Unparseable company ID in "%s" found' % parts[2])
				self.timestamp = self.__parseTimestamp (parts[1])
				if self.timestamp is None:
					self.__error ('Unparseable timestamp in "%s" found' % parts[1])
				self.mailid = parts[3]
				mparts = [_m for _m in self.splitter.split (self.mailid) if _m]
				if len (mparts) == 0:
					self.__error ('Unparseable mailing ID in "%s" found' % parts[3])
				else:
					try:
						self.mailing = int (mparts[-1])
					except ValueError:
						self.__error ('Unparseable mailing ID in mailid "%s" found' % self.mailid)
				try:
					self.blocknr = int (parts[4])
					self.blockid = '%d' % self.blocknr
					self.single = False
				except ValueError:
					self.blocknr = 0
					self.blockid = parts[4]
					self.single = True

class MTA (object):
	"""Handles different MTAs

This class is used to handle different MTAs on a central base. It also
supports calling xmlback to generate the final mail depending on the
used MTA."""
	def __init__ (self, xmlback = None):
		"""``xmlback'' is an alternate path to the executable to call"""
		self.xmlback = xmlback if xmlback is not None else mkpath (base, 'bin', 'xmlback')
		self.mta = os.environ.get ('MTA', 'sendmail')
		self.dsnopt = os.environ.get ('SENDMAIL_DSN_OPT', '-NNEVER')
		self.conf = {}
		if self.mta == 'postfix':
			cmd = self.postfixCommand ('postconf')
			if cmd:
				pp = subprocess.Popen ([cmd], stdout = subprocess.PIPE, stderr = subprocess.PIPE, stdin = subprocess.PIPE)
				(out, err) = pp.communicate ()
				if pp.returncode != 0:
					self.log (LV_WARNING, 'Command %s returnd %d' % (cmd, pp.returncode))
				if out:
					for line in (_l.strip () for _l in out.split ('\n')):
						if line:
							try:
								(var, val) = [_v.strip () for _v in line.split ('=', 1)]
								self.conf[var] = val
							except ValueError:
								self.log (LV_WARNING, 'Unparsable line: "%s"' % line)
			else:
				self.log (LV_WARNING, 'No command to determinate configuration found')

	def postfixCommand (self, cmd):
		"""return path to ``cmd'' for a typical postifx installation"""
		return which (cmd, '/usr/sbin', '/sbin', '/etc')
	
	def postfixMake (self, filename):
		"""creates a postfix hash file for ``filename''"""
		cmd = self.postfixCommand ('postmap')
		if cmd:
			n = call ([cmd, filename])
			if n == 0:
				self.log (LV_INFO, '%s written using %s' % (filename, cmd))
			else:
				self.log (LV_ERROR, '%s not written using %s: %d' % (filename, cmd, n))
		else:
			self.log (LV_ERROR, '%s not written due to missing postmap command' % filename)

	def log (self, level, msg):
		"""write log entry with current MTA name as part of the ident"""
		log (level, 'mta/%s' % self.mta, msg)
	
	def __getitem__ (self, key):
		return self.conf[key]

	__pattern = re.compile (',?[ \t\r\n]+')
	def getlist (self, key):
		"""returns the value for ``key'' as list"""
		return self.__pattern.split (self[key])
	
	def __call__ (self, path, **kws):
		"""``path'' is the file to process

kws may contain other parameter required or optional used by specific
instances of mail creation"""
		generate = [
			'account-logfile=%s/log/account.log' % base,
			'bounce-logfile=%s/log/extbounce.log' % base
		]
		if self.mta == 'postfix':
			generate += [
				'messageid-logfile=%s/log/messageid.log' % base
			]
		generate += [
			'media=email',
			'log-mfrom=%s/var/run/envelope.db' % base
		]
		if self.mta == 'postfix':
			generate += [
				'inject=/usr/sbin/sendmail %s -f %%(sender) -- %%(recipient)' % self.dsnopt
			]
		else:
			generate += [
				'path=%s' % kws['targetDirectory'],
			]
		cmd = [
			self.xmlback,
			'-l',
			'-o', 'generate:%s' % ';'.join (generate),
			'-L', 'info',
			path
		]
		log (LV_DEBUG, 'xmlback', '%s starting' % ' '.join (cmd))
		pp = subprocess.Popen (cmd, stdin = subprocess.PIPE, stdout = subprocess.PIPE, stderr = subprocess.PIPE)
		(out, err) = pp.communicate (None)
		n = pp.returncode
		log (LV_DEBUG, 'xmlback', '%s returns %d' % (' '.join (cmd), n))
		if n != 0:
			log (LV_ERROR, 'xmlback', 'Failed to unpack %s (%d)' % (path, n))
			for (name, content) in [('Output', out), ('Error', err)]:
				if content:
					log (LV_ERROR, 'xmlback', '%s:\n%s' % (name, content))
			return False
		log (LV_INFO, 'xmlback', 'Unpacked %s' % path)
		return True
#}}}
#
# 9.) General database interface
#
#{{{
class DBResultType:
	"""Constants for query result type
	
this class defines some constants which specifies the return type of a
database query row:
	- Array: this is the standard return type, each value can be accessed by its index starting by 0
	- Named: a named array where the result can be access by its column names
	- List: this is like Array, but each element is not just the value, but a tuple of (column, value)
	- Mutable: this packs the row into an agn.mutable, so each column can be access as an attribute
	- Hash: returns the row in a Map
	- Result: returns the row as agn.DBResult which can be accessed as it would be an Array, Mutable or Hash
"""
	Array = 0
	Named = 1
	List = 2
	Mutable = 3
	Hash = 4
	Result = 5
	Struct = Mutable

class DBResult (object):
	"""Result for a DBResultType.Result

this represents a row returned by a query when result type
DBResultType.Result is selected. One can access each column either by
its index, by its name as an attribute or key, e.g. the column id of
one row of the query

SELECT id FROM some_table

can be accessed by either one of this expressions (assuming the row is
stored in variable namend ``row''):
	- row[0]
	- row.id
	- rwo['id']
	- row.getItems ()[0][1]
"""

	def __init__ (self, col, row):
		"""Setup the result with the column names and the values for this row"""
		self._Col = col
		self._Row = row

	def __len__ (self):
		return len (self._Row)

	def __getitem__ (self, what):
		if type (what) in (int, long):
			return self._Row[what]
		return self.__dict__[what]

	def __contains__ (self, what):
		return what in self._Col

	def __call__ (self, what, dflt = None):
		try:
			return self[what]
		except (KeyError, IndexError):
			return dflt

	def __str__ (self):
		return '[%s]' % ', '.join (['%s=%r' % (_c, _r) for (_c, _r) in zip (self._Col, self._Row)])
	__repr__ = __str__

	def getColumns (self):
		"""returns all column names"""
		return self._Col

	def getValues (self):
		"""returns all values"""
		return self._Row

	def getItems (self):
		"""returns a list of tuple of type (column, value)"""
		return list (zip (self._Col, self._Row))

	def NVL (self, what, onNull):
		"""Helper function to simulate the DB nvl(..) function

if the value ``what'' is not NULL (in python this means None), then
the value itself is returned, otherwise ``onNull'' is returned, e.g.
to enforce a numeric value:
	NVL (value, 0)
will return 0 if the value is None."""
		rc = self[what]
		if rc is None:
			rc = onNull
		return rc

class DBCore (object):
	"""Meta class for database driver

this is the base class for all database specific drivers and should
inherit this class. """
	DBTYPE = lambda n: -abs(n) - 1
	STRING = 0
	BINARY = 1
	NUMBER = 2
	TIMESTAMP = 3
	ROWID = 4
	def __init__ (self, dbms, driver, cursorClass):
		"""``dbms'' is the symbolic name, ``driver'' the real driver class and ``cursorClass'' based on DBCursor

for verbose logging you can assing the instance variable ``log'' a
callable which is called in several stages with one argument, a
logging information. This is useful for debugging the database level
but can produce lots of output in prdocutive use."""
		self.dbms = dbms
		self.driver = driver
		self.cursorClass = cursorClass
		self.db = None
		self.lasterr = None
		self.log = None
		self.resultType = DBResultType.Named
		self.cursors = []
		self.types = {None: self.STRING}
		for (id, names) in [
			(self.BINARY, ('BINARY', 'BLOB', 'LOB')),
			(self.NUMBER, ('NUMBER', )),
			(self.TIMESTAMP, ('TIMESTAMP', 'DATETIME', 'DATE', 'TIME')),
			(self.ROWID, ('ROWID', ))
		]:
			for name in names:
				if name in driver.__dict__:
					self.types[driver.__dict__[name]] = id
					with Ignore (TypeError):
						for t in driver.__dict__[name]:
							self.types[t] = id

	def __enter__ (self):
		return self

	def __exit__ (self, exc_type, exc_value, traceback):
		if exc_type is None:
			self.commit ()
		else:
			self.rollback ()
		self.close ()
		return False
	
	def setup (self):
		"""hook to add driver specific setup code"""
		pass
	
	def typ (self, t):
		"""returns a normalized type of the database specific value type"""
		try:
			return self.types[t]
		except KeyError:
			return self.types[None]

	def error (self, errmsg):
		"""is called in error condition

this will store the message in lasterr and retrieved using the method
``lastError'' and closes the database connection"""
		self.lasterr = errmsg
		self.close ()

	def reprLastError (self):
		"""returns string representation of last occured error"""
		return str (self.lasterr)

	def lastError (self):
		"""return string representation, if an error has occured, otherwise "success"."""
		if self.lasterr is not None:
			return self.reprLastError ()
		return 'success'

	def sync (self, commit = True):
		"""flushes the current transaction

if ``commit'' is True, then the last changes are written back to the
database, otherwise they are discarded (rollback). On databases
without transaction support this may be a NO-OP."""
		if self.db is not None:
			if commit:
				self.db.commit ()
			else:
				self.db.rollback ()

	def commit (self):
		"""shortcat for self.sync (True)"""
		self.sync (True)

	def rollback (self):
		"""shortcat for self.sync (False)"""
		self.sync (False)

	def close (self):
		"""closes database and all created cursors"""
		if self.db is not None:
			for c in self.cursors[:]:
				if self.log is not None and c.id is not None:
					self.log ('Closing pending cursor %s' % c.id)
				with Ignore (self.driver.Error) as ig:
					ig.loglevel = LV_WARNING
					c.close ()
			try:
				self.db.close ()
			except self.driver.Error as e:
				self.lasterr = e
			self.db = None
		self.cursors = []

	def open (self):
		"""opens the database and returns True on success, False otherwise"""
		self.close ()
		try:
			self.connect ()
		except self.driver.Error as e:
			self.error (e)
		return self.isOpen ()

	def isOpen (self):
		"""returns True, if database is open, else False"""
		return self.db is not None

	def getCursor (self):
		"""create a new cursor

this method creates a new cursor. This method should NOT be used by an
application as the cursor will not be kept track of and cannnot be
automatically closed.

Use the method ``cursor'' instead."""
		if self.isOpen () or self.open ():
			try:
				curs = self.db.cursor ()
				if curs is not None:
					with Ignore (AttributeError):
						if curs.arraysize < 100:
							curs.arraysize = 100
			except self.driver.Error as err:
				curs = None
				self.error (err)
		else:
			curs = None
		return curs

	def cursor (self, autocommit = False, id = None):
		"""request a new cursor

if ``autocommit'' is on, every change to the database is directly
followed by a commit. ``id'' can be set to some readable values and is
used during logging."""
		if self.isOpen () or self.open ():
			c = self.cursorClass (self, autocommit)
			if c is not None:
				c.setID (id)
				if self.resultType is not None:
					c.defaultResultType (self.resultType)
				self.cursors.append (c)
		else:
			c = None
		return c

	def release (self, cursor):
		"""releases and closes a cursor

this is internally used by DBCursor to ensure that the cursor is
closed and removed from the internal tracking."""
		if cursor in self.cursors:
			self.cursors.remove (cursor)
			if cursor.curs:
				cursor.curs.close ()
				cursor.curs = None
		elif self.log is not None and cursor.id is not None:
			self.log ('Try to release a not managed cursor %s' % cursor.id)

	def query (self, req):
		"""query the database

this should only be used if you just want to query a single value. It
is costly as it always creates a cursor and closes it again. For a
more intense database access, create a cursor on your own and use the
cursor instead."""
		c = self.cursor ()
		if c is None:
			raise error ('Unable to get database cursor: ' + self.lastError ())
		rc = None
		try:
			rc = [r for r in c.query (req)]
		finally:
			c.close ()
		return rc

	def execute (self, req):
		"""execute a statement"""
		c = self.cursor ()
		if c is None:
			raise error ('Unable to get database cursor: ' + self.lastError ())
		rc = None
		try:
			rc = c.execute (req)
		finally:
			c.close ()
		return rc
	
	def update (self, *args, **kws):
		"""execute an update statement"""
		return self.execute (*args, **kws)
	
	def stream (self, *args, **kws):
		"""creates a stream using a dedicated cursor and using ``*args'' and ``**kws'' for DBCursor.query()"""
		cursor = self.cursor ()
		return Stream.defer (cursor.query (*args, **kws), lambda o: cursor.close ())

	def connect (self):
		"""Establish a connection to the database"""
		raise error ('Subclass responsible to implement method connect')

class DBCache (object):
	"""Cache a query in memory

this class is used to read the whole result of a query and store it in
memory using the DBcursor.queryc method."""
	def __init__ (self, data):
		self.data = data
		self.count = len (data)

	def __iter__ (self):
		return iter (self.data)

class DBCursor (object):
	"""Metaclass for database cursor

this is the base class for a database specific cursor and should
inherit this class. This class should not be instantiated by an
application, use ``DBCore.cursor()'' instead."""
	def __init__ (self, db, autocommit, needReformat):
		"""``db'' is the database specific subclass of DBCore.

If ``autocommit'' is set, then every update is followed by a commit to
write the content directly to the database. ``needReformat'' should be
set to True, if the database does not support named placeholder."""
		self.db = db
		self.autocommit = autocommit
		self.needReformat = needReformat
		self.id = None
		self.curs = None
		self.desc = False
		self.defaultRType = None
		self.querypost = None
		self.rowspec = None
		self.rowtype = None
		self.cacheReformat = {}
		self.cacheVariables = {}
		self.log = db.log
		self.qreplace = {}

	def __enter__ (self):
		return self

	def __exit__ (self, exc_type, exc_value, traceback):
		self.close ()
		return False

	def setID (self, id):
		"""Set internal id for logging"""
		self.id = id

	def rselect (self, s, **kws):
		"""Replaces database specific keywords using a python %(keyword)s construct."""
		if kws:
			rplc = self.qreplace.copy ()
			for (var, val) in kws.items ():
				rplc[var] = val
			return s % rplc
		return s % self.qreplace

	qmapper = {
		'MySQLdb':		'mysql',
		'mysql.connector':	'mysql',
		'cx_Oracle':		'oracle',
		'sqlite3':		'sqlite',
	}
	def qselect (self, **args):
		"""Selects a database sepcific query variant

it is not always possible to write database neutral code. So if you
have to use a database specific query, you can use this method to
automatically select the query required for the currently used
database. The known databases are detected by the used driver and are
normalized to these names:
	- mysql: for MySQL or MariaDB databases
	- oracle: for Oracle RDBMs
	- sqlite: for using a SQLite database

e.g. you want to create a new record, but you use a sequence in oracle
and an autoincrement in mysql:
	query = cursor.qselect (
		oracle = 'INSERT INTO some_table (id, content) VALUES (some_sequence.nextval, :content)',
		mysql = 'INSERT INTO some_table (content) VALUES (:content)'
	)
And to get the query to select the newly created ID:
	query = cursor.qselect (
		oracle = 'SELECT some_sequence.currval FROM dual',
		mysql = 'SELECT last_insert_id()'
	)
"""
		try:
			return args[self.db.driver.__name__]
		except KeyError:
			return args[self.qmapper[self.db.driver.__name__]]

	def lastError (self):
		"""returns the last occured error string"""
		if self.db is not None:
			return self.db.lastError ()
		return 'no database interface active'

	def error (self, errmsg):
		"""sets a new error condititon"""
		if self.db is not None:
			self.db.lasterr = errmsg
		self.close ()

	def executor (self, *args, **kws):
		"""internally used to intercept a database access before it is passed to the database"""
		return self.curs.execute (*args, **kws)
		
	def close (self):
		"""closes the current cursor"""
		try:
			self.db.release (self)
			if self.log: self.log ('Cursor closed')
		except self.db.driver.Error as e:
			self.db.lasterr = e
			if self.log: self.log ('Cursor closing failed: %s' % self.lastError ())
		self.curs = None
		self.desc = False

	def open (self):
		"""opens the cursor used by DBCore.cursor()"""
		self.close ()
		if self.db is not None:
			try:
				self.curs = self.db.getCursor ()
				if self.curs is not None:
					if self.log: self.log ('Cursor opened')
				else:
					if self.log: self.log ('Cursor open failed')
			except self.db.driver.Error as e:
				self.error (e)
				if self.log: self.log ('Cursor opening failed: %s' % self.lastError ())
		else:
			if self.log: self.log ('Cursor opeing failed: no database available')
		return self.curs is not None

	__fieldClass = collections.namedtuple ('Field', ['name', 'typ', 'displaySize', 'size', 'precision', 'scale', 'nullOk'])
	def description (self, normalize = False):
		"""returns the layout description of last query call

if ``normalize'' is True then a normalized version is returned to be
portable across different databases."""
		if self.desc:
			if normalize:
				return [self.__fieldClass (_d[0].lower (), self.db.typ (_d[1]), _d[2], _d[3], _d[4], _d[5], _d[6]) for _d in self.curs.description]
			return self.curs.description
		return None

	variablePattern = re.compile ('\'[^\']*\'|:[a-z0-9_]+', re.IGNORECASE)
	def variablesInQuery (self, query):
		"""Internally used method to find all variables in a query"""
		try:
			varlist = self.cacheVariables[query]
		except KeyError:
			varlist = (Stream (self.variablePattern.findall (query))
				.filter (lambda p: p.startswith (':'))
				.map (lambda p: p[1:])
				.set ()
			)
			self.cacheVariables[query] = varlist
		return varlist
		
	def reformat (self, req, parm):
		"""internally used method to reformat a query with named placeholder for databases not supporting them."""
		try:
			(nreq, varlist) = self.cacheReformat[req]
		except KeyError:
			def reformater (query, varlist):
				for (plain, match) in itertools.izip_longest (self.variablePattern.split (req), self.variablePattern.findall (req)):
					yield plain.replace ('%', '%%')
					if match is not None:
						if match.startswith (':'):
							varlist.append (match[1:])
							yield '%s'
						else:
							yield match.replace ('%', '%%')
			varlist = []
			nreq = ''.join (reformater (req, varlist))
			self.cacheReformat[req] = (nreq, varlist)
		#
		return (nreq, [parm[_k] for _k in varlist])

	def cleanup (self, req, parm):
		"""internally used method to cleanup query parameter that are not used in the query itself."""
		return dict ((_k, _v) for (_k, _v) in parm.iteritems () if _k in self.variablesInQuery (req))

	def qvalidate (self, req, parm):
		"""validates a query against its query parameter

this is a useful tool for a complex query and mismatched query
parameter. This returns a tuple with three elements, the first is a
bool which is True if the query and its parameter are looking okay (in
this case the remaining two elements are empty lists) or False if not.
In this case the second element of the tuple contains the parameter
which are used in the query but are not found in the query parameter,
the third element contains the keys of the query parameter which are
not used in the query."""
		var = self.variablesInQuery (req)
		missing = [_v for _v in var if _v not in parm]
		keys = [_k for _k in parm if _k not in var]
		return (not missing and not keys, missing, keys)
	
	def __valid (self):
		if self.curs is None:
			if not self.open ():
				raise error ('Unable to setup cursor: ' + self.lastError ())

	def __rowsetup (self, data):
		if self.rowspec is None:
			d = self.description ()
			if d is None:
				self.rowspec = ['_%d' % (_n + 1) for _n in range (len (data))]
			else:
				self.rowspec = [_d[0].lower () for _d in d]

	__validName = re.compile ('^[a-z][a-z0-9_]*$', re.IGNORECASE)
	def __rownamed (self, data):
		self.__rowsetup (data)
		if self.rowtype is None:
			seen = set ()
			def norm (n, c):
				if self.__validName.match (c) is None or keyword.iskeyword (c) or c in seen:
					c = 'Column%d' % n
				seen.add (c)
				return c
			try:
				self.rowtype = collections.namedtuple ('row', [norm (_n, _c) for (_n, _c) in enumerate (self.rowspec, start = 1)])
			except:
				self.rowtype = collections.namedtuple ('row', ['Column%d' % (_n, ) for _n in range (len (self.rowspec, start = 1))])
		return self.rowtype (*data)
		
	def __rowlist (self, data):
		self.__rowsetup (data)
		return zip (self.rowspec, data)

	def __rowfill (self, hash, data):
		for (var, val) in self.__rowlist (data):
			hash[var] = val

	def __rowmutable (self, data):
		rc = mutable ()
		self.__rowfill (rc.__dict__, data)
		return rc

	def __rowhash (self, data):
		rc = {}
		self.__rowfill (rc, data)
		return rc

	def __rowresult (self, data):
		self.__rowsetup (data)
		rc = DBResult (self.rowspec, data)
		self.__rowfill (rc.__dict__, data)
		return rc

	def __iter__ (self):
		return self

	def next (self):
		try:
			data = self.curs.fetchone ()
		except self.db.driver.Error as e:
			self.error (e)
			raise error ('query next failed: ' + self.lastError ())
		if data is None:
			raise StopIteration ()
		if self.querypost:
			return self.querypost (data)
		return data

	def setOutputSize (self, *args):
		"""oracle specific method to handle LOBs in queries"""
		if self.db.dbms == 'oracle':
			self.__valid ()
			self.curs.setoutputsize (*args)

	def setInputSizes (self, **args):
		"""oracle specific method to handle LOBs in inserts/updates"""
		if self.db.dbms == 'oracle':
			self.__valid ()
			self.curs.setinputsizes (**args)

	def defaultResultType (self, rtype):
		"""set the default return type as one of DBResultType. Returns the previous value."""
		old = self.defaultRType
		self.defaultRType = rtype
		return old

	__ph = re.compile (':([a-z0-9_]+)', re.IGNORECASE)
	def __parmformat (self, req, parm):
		try:
			return Stream.of (self.__ph.findall (req)).map (lambda ph: '%s=%r' % (ph, parm[ph])).join (', ')
		except Exception as e:
			return '%r (%s)' % (parm, e)
	
	def query (self, req, parm = None, cleanup = False, rtype = None):
		"""Query the database

``req'' is the query itself, ``parm'' are the optional query parameter
as a dict, if the query is using a prepared statement. ``cleanup''
should be set to True, if the query parameter may contain more keys
than are used in the query. ``rtype'' is the result type s found in
DBResultType. If not set, the cursor default result type is used. If
this is not set, it is DBResulType.Array by default.

This method return an iterable realizied by itself."""
		self.__valid ()
		if rtype is None:
			rtype = self.defaultRType
		self.rowspec = None
		self.rowtype = None
		if rtype == DBResultType.Named:
			self.querypost = self.__rownamed
		elif rtype == DBResultType.List:
			self.querypost = self.__rowlist
		elif rtype == DBResultType.Mutable:
			self.querypost = self.__rowmutable
		elif rtype == DBResultType.Hash:
			self.querypost = self.__rowhash
		elif rtype == DBResultType.Result:
			self.querypost = self.__rowresult
		else:
			self.querypost = None
		try:
			if parm is None:
				if self.log: self.log ('Query: %s' % req)
				self.executor (req)
			else:
				if self.needReformat:
					(req, parm) = self.reformat (req, parm)
				elif cleanup:
					parm = self.cleanup (req, parm)
				if self.log: self.log ('Query: %s [%s]' % (req, self.__parmformat (req, parm)))
				self.executor (req, parm)
			if self.log: self.log ('Query started')
		except self.db.driver.Error as e:
			self.error (e)
			if self.log:
				if parm is None:
					self.log ('Query %s failed: %s' % (req, self.lastError ()))
				else:
					self.log ('Query %s using %r failed: %s' % (req, parm, self.lastError ()))
			raise error ('query start failed: ' + self.lastError ())
		self.desc = True
		return self

	def queryc (self, req, parm = None, cleanup = False, rtype = None):
		"""See query, but returns a cached version of the query. Use with care on large result sets!"""
		if self.query (req, parm, cleanup, rtype) == self:
			try:
				data = self.curs.fetchall ()
				if self.querypost:
					data = [self.querypost (_d) for _d in data]
				return DBCache (data)
			except self.db.driver.Error as e:
				self.error (e)
				if self.log:
					if parm is None:
						self.log ('Queryc %s fetch failed: %s' % (req, self.lastError ()))
					else:
						self.log ('Queryc %s using %r fetch failed: %s' % (req, parm, self.lastError ()))
				raise error ('query all failed: ' + self.lastError ())
		if self.log:
			if parm is None:
				self.log ('Queryc %s failed: %s' % (req, self.lastError ()))
			else:
				self.log ('Queryc %s using %r failed: %s' % (req, parm, self.lastError ()))
		raise error ('unable to setup query: ' + self.lastError ())

	def querys (self, req, parm = None, cleanup = False, rtype = None):
		"""See query, but returns only one (the first) row or None, if no row is found at all."""
		rc = None
		for rec in self.query (req, parm, cleanup, rtype):
			rc = rec
			if self.db.dbms in ('mysql', 'mariadb'):
				with Ignore ():
					while self.curs.fetchmany ():
						pass
			break
		return rc

	def queryp (self, req, **kws):
		"""See query, query parameter are passed keywords"""
		return self.query (req, kws)
	def querypc (self, req, **kws):
		"""See queryc, query parameter are passed keywords"""
		return self.queryc (req, kws)
	def queryps (self, req, **kws):
		"""See querys, query parameter are passed keywords"""
		return self.querys (req, kws)

	def sync (self, commit = True):
		"""Ends a transaction by either writing the changes to the database (``commit'' is True) or by rollbacking the changes."""
		rc = False
		if self.db is not None:
			if self.db.db is not None:
				try:
					if self.log:
						if commit:
							self.log ('Sync: commit')
						else:
							self.log ('Sync: rollback')
					self.db.sync (commit)
					if self.log:
						if commit:
							self.log ('Sync done commiting')
						else:
							self.log ('Sync done rollbacking')
					rc = True
				except self.db.driver.Error as e:
					self.error (e)
					if self.log:
						if commit:
							self.log ('Sync failed commiting')
						else:
							self.log ('Sync failed rollbacking')
			else:
				if self.log: self.log ('Sync failed: database not open')
		else:
			if self.log: self.log ('Sync failed: database not available')
		return rc

	def execute (self, req, parm = None, commit = False, cleanup = False):
		"""Performs non query database calls. For parameter see query. Returns number of changed rows, if applicated."""
		self.__valid ()
		try:
			if parm is None:
				if self.log: self.log ('Execute: %s' % req)
				self.executor (req)
			else:
				if self.needReformat:
					(req, parm) = self.reformat (req, parm)
				elif cleanup:
					parm = self.cleanup (req, parm)
				if self.log: self.log ('Execute: %s [%s]' % (req, self.__parmformat (req, parm)))
				self.executor (req, parm)
			if self.log: self.log ('Execute affected %d rows' % self.curs.rowcount)
		except self.db.driver.Error as e:
			self.error (e)
			if self.log:
				if parm is None:
					self.log ('Execute %s failed: %s' % (req, self.lastError ()))
				else:
					self.log ('Execute %s using %r failed: %s' % (req, parm, self.lastError ()))
			raise error ('execute failed: ' + self.lastError ())
		rows = self.curs.rowcount
		if rows > 0 and (commit or self.autocommit):
			if not self.sync ():
				if self.log:
					if parm is None:
						self.log ('Commit after execute failed for %s: %s' % (req, self.lastError ()))
					else:
						self.log ('Commit after execute failed for %s using %r: %s' % (req, parm, self.lastError ()))
				raise error ('commit failed: ' + self.lastError ())
		self.desc = False
		return rows
	
	def update (self, *args, **kws):
		"""See execute"""
		return self.execute (*args, **kws)

	def executep (self, req, **kws):
		"""See execute, parameter are passed keywords"""
		return self.execute (req, kws)
	def updatep (self, *args, **kws):
		"""See update, parameter are passed keywords"""
		return self.executep (*args, **kws)

	def stream (self, *args, **kws):
		"""creates a stream using this cursor and using ``*args'' and ``**kws'' for DBCursor.query()"""
		return Stream (self.query (*args, **kws))

try:
	try:
		import	MySQLdb
	except ImportError:
		import	mysql.connector as MySQLdb

	class DBCursorMySQL (DBCursor):
		"""MySQL sepcific DBCursor implementation"""
		def __init__ (self, db, autocommit):
			DBCursor.__init__ (self, db, autocommit, True)
			self.qreplace['sysdate'] = 'current_timestamp'

	class DBMySQL (DBCore):
		"""MySQL specific DBCore implementation"""
		def __init__ (self, host = None, user = None, passwd = None, name = None):
			DBCore.__init__ (self, 'mysql', MySQLdb, DBCursorMySQL)
			self.host = host
			self.user = user
			self.passwd = passwd
			self.name = name

		def reprLastError (self):
			return 'MySQL-%d: %s' % (self.lasterr.args[0], self.lasterr.args[1].strip ())

		def connect (self):
			try:
				(host, port) = self.host.split (':', 1)
				port = int (port)
			except ValueError:
				host = self.host
				port = None
			if self.driver.__name__ == 'mysql.connector':
				config = {
					'host': host,
					'user': self.user,
					'password': self.passwd,
					'database': self.name,
					'charset': 'utf8',
					'use_unicode': True
				}
				if port is not None:
					config['port'] = port
				self.db = self.driver.connect (**config)
			else:
				utf8 = {
					'charset': 'utf8',
					'use_unicode': True
				}
				if port is not None:
					self.db = self.driver.connect (host, self.user, self.passwd, self.name, port, **utf8)
				else:
					self.db = self.driver.connect (self.host, self.user, self.passwd, self.name, **utf8)
except ImportError:
	DBMySQL = None

try:
	os.environ['NLS_LANG'] = 'american_america.UTF8'
	import	cx_Oracle

	class DBCursorOracle (DBCursor):
		"""Oracle RDBM specific DBCursor implementation"""
		def __init__ (self, db, autocommit):
			DBCursor.__init__ (self, db, autocommit, False)
			self.qreplace['sysdate'] = 'sysdate'
			self.tries = 3
		
		def executor (self, *args, **kws):
			for state in range (self.tries):
				if state:
					time.sleep (state)
				try:
					return super (DBCursorOracle, self).executor (*args, **kws)
				except cx_Oracle.DatabaseError as e:
					if self.log: self.log ('Got oracle error: %s' % str (e).strip ())
					try:
						#
						# ORA-00040: active time limit exceeded - call aborted
						# ORA-00050: operating system error occurred while obtaining an enqueue
						# ORA-00051: timeout occurred while waiting for a resource
						# ORA-00060: deadlock detected while waiting for resource
						if e.message.code not in (40, 50, 51, 60):
							raise
					except AttributeError:
						raise e
			raise

		def execute (self, req, parm = None, commit = False, cleanup = False, adapt = False):
			if parm is not None and adapt:
				amap = {}
				if cleanup:
					parm = self.cleanup (req, parm)
					cleanup = False
				for (var, val) in parm.items ():
					if (type (val) is str and len (val) >= 4000) or (type (val) is unicode and len (val.encode ('UTF-8')) >= 4000):
						amap[var] = cx_Oracle.CLOB
				if amap:
					self.curs.setinputsizes (**amap)
			return super (DBCursorOracle, self).execute (req, parm, commit, cleanup)

	class DBOracle (DBCore):
		"""Oracle RDBM specific DBCore implementation"""
		def __init__ (self, user = None, passwd = None, sid = None):
			DBCore.__init__ (self, 'oracle', cx_Oracle, DBCursorOracle)
			self.user = user
			self.passwd = passwd
			self.sid = sid

		def reprLastError (self):
			message = self.lasterr.args[0]
			if type (message) in (str, unicode):
				return message.strip ()
			return message.message.strip ()

		def connect (self):
			self.db = self.driver.connect (self.user, self.passwd, self.sid)
			with Ignore (AttributeError):
				if self.db.stmtcachesize < 20:
					self.db.stmtcachesize = 20
			with Ignore (AttributeError):
				if self.db.autocommit:
					self.db.autocommit = 0

except ImportError:
	DBOracle = None

try:
	import	sqlite3
	try:
		import	cPickle as pickle
	except ImportError:
		import	pickle

	class DBCursorSQLite3 (DBCursor):
		"""SQLite specific DBCursor implementation

these database specific methods are available here:
	- mode: set the operation mode:
	- validate: performs an integrity check of the database
"""
		def __init__ (self, db, autocommit):
			DBCursor.__init__ (self, db, autocommit, False)
			self.qreplace['sysdate'] = 'current_timestamp'

		modes = {
			'fast':	[
				'PRAGMA cache_size = 65536',
				'PRAGMA automatic_index = ON',
				'PRAGMA journal_mode = OFF',
				'PRAGMA synchronous = OFF',
				'PRAGMA secure_delete = OFF',
			],
			'exclusive': [
				'PRAGMA locking_mode = EXCLUSIVE'
			]
		}
		def mode (self, m):
			"""set operation mode for the database
			
currently these two modes are implemented, which can also be used in conjunction:
	- "fast" for fast, but insecure database access
	- "exclusive" for exclusive locking
"""
			try:
				for op in self.modes[m]:
					self.execute (op)
			except KeyError:
				raise error ('invalid mode: %s (expecting one of %s)' % (m, ', '.join (sorted (self.modes.keys ()))))

		def validate (self, collect = None, quick = False, amount = None):
			"""performs an integrity check of the database, returns True on okay, otherwise False

``collect'' can be a list where the results of the integrity check are
stored. ``quick'' can be set to True to just perform a quick check of
the database. ``amount'' can be numeric value to restrict the check."""
			rc = False
			if quick:
				method = 'quick_check'
			else:
				method = 'integrity_check'
			if amount is not None and amount >= 0:
				method += '(%d)' % amount
			count = 0
			for r in self.queryc ('PRAGMA %s' % method, rtype = DBResultType.Array):
				count += 1
				if r[0]:
					if count == 1 and r[0] == 'ok':
						rc = True
					else:
						rc = False
					if collect is not None:
						collect.append (r[0])
			return rc
		
	class DBSQLite3 (DBCore):
		"""SQLite specific DBCore implementation"""
		class Binary (object):
			name = 'binary'
			datatype = DBCore.BINARY
			def __init__ (self, value):
				self.value = value
		
			@classmethod
			def dump (cls, o):
				if o.value is None:
					return None
				return base64.encodestring (o.value)
		
			@classmethod
			def load (cls, s):
				if s is None:
					return cls (None)
				try:
					return cls (base64.decodestring (s))
				except Exception:
					return cls (s)

		def __init__ (self, filename, layout = None, extendedTypes = False, extendedRows = False, textType = None, extendedFunctions = False):
			DBCore.__init__ (self, 'sqlite', sqlite3, DBCursorSQLite3)
			self.filename = filename
			self.layout = layout
			self.extendedTypes = extendedTypes
			self.extendedRows = extendedRows
			if textType:
				self.textType = textType.lower ()
			else:
				self.textType = None
			self.functions = []
			if extendedTypes:
				self.registerClass (self.Binary)
			if extendedFunctions:
				def __nvl (value, default):
					if value is None:
						return default
					return value
				self.createFunction ('nvl', 2, __nvl)
				#
				def __decode (*args):
					alen = len (args)
					n = 0
					while n + 2 < alen:
						if args[n] == args[n + 1]:
							return args[n + 2]
						n += 3
					if n + 1 == alen:
						return args[n]
					return None
				self.createFunction ('decode', -1, __decode)

		def registerType (self, name, registerClass, adapt, convert, datatype):
			"""registers a new type to be interpreted by this driver

``name'' is the name of the type to register, ``registerClass'' is the
class to represent the data of this type, ``adapt'' is a callable
which transforms the class to a string based representation,
``convert'' is for parsing a string into the class and ``datatype'' is
used to map this new type to a normalized data type. """
			if not self.extendedTypes:
				raise error ('extended types disabled, %s not registered' % name)
			sqlite3.register_adapter (registerClass, adapt)
			sqlite3.register_converter (name, convert)
			if datatype is not None:
				self.types[name] = datatype
		
		def registerClass (self, registerClass):
			"""registers a new type and use the class attributes for configuration

the class must provide these attributes to be used (see registerType for the meaing of them):
	- name
	- dump (for adapt)
	- load (for convert)
	- datatype
"""
			self.registerType (registerClass.name, registerClass, registerClass.dump, registerClass.load, registerClass.datatype)

		def register (self, name, registerClass, datatype = None):
			"""registers a new type and use python pickler for converting from/to string representation"""
			self.registerType (name, registerClass, lambda o: pickle.dumps (o), lambda s: pickle.loads (s), datatype)
		
		def __function (self, name, args):
			self.functions.append ((name, args))
			if self.db is not None:
				name (*args)
		def __createFunction (self, name, noOfParam, method):
			self.db.create_function (name, noOfParam, method)
		def createFunction (self, name, noOfParam, method):
			self.__function (self.__createFunction, (name, noOfParam, method))
		def __createCollation (self, name, method):
			self.db.create_collation (name, method)
		def createCollation (self, name, method):
			self.__function (self.__createCollation, (name, method))
		def __createAggregate (self, name, noOfParam, cls):
			self.db.create_aggregate (name, noOfParam, cls)
		def createAggregate (self, name, noOfParam, cls):
			self.__function (self.__createAggregate, (name, noOfParam, cls))

		textFactories = {
			'str':		str,
			'utf-8':	lambda s: unicode (s, 'UTF-8', 'replace') if type (s) is not unicode else s,
			'unicode':	sqlite3.OptimizedUnicode
		}
		def connect (self):
			isNew = not os.path.isfile (self.filename)
			if self.extendedTypes:
				self.db = self.driver.connect (self.filename, detect_types = sqlite3.PARSE_DECLTYPES | sqlite3.PARSE_COLNAMES)
			else:
				self.db = self.driver.connect (self.filename)
			if self.extendedRows:
				self.db.row_factory = sqlite3.Row
			if self.textType is not None and self.textType in self.textFactories:
				self.db.text_factory = self.textFactories[self.textType]
			if self.db is not None:
				for (name, args) in self.functions:
					name (*args)
				if isNew and self.layout:
					c = self.cursor ()
					c.mode ('fast')
					if type (self.layout) in (str, unicode):
						layout = [self.layout]
					else:
						layout = self.layout
					for stmt in layout:
						if type (stmt) in (tuple, list) and len (stmt) == 2:
							c.execute (stmt[0], stmt[1])
						else:
							c.execute (stmt)
					c.close ()
	SDBase = DBSQLite3
except ImportError:
	DBSQLite3 = None

class DBConfig (object):
	"""Database configuration handler

reads and stores configuration from a configuration file"""
	configPath = '/home/openemm/etc/dbcfg'
	def __init__ (self, path = None):
		if path is None:
			path = self.configPath
		self.path = path
		self.data = None
		self.keys = None

	class DBRecord (object):
		"""A Record for one database configuration line"""
		def __init__ (self, dbid, param):
			self.dbid = dbid
			self.data = {}
			if param:
				for elem in [_p.strip () for _p in param.split (',')]:
					elem = elem.strip ()
					parts = [_e.strip () for _e in elem.split ('=', 1)]
					if len (parts) == 2:
						self.data[parts[0]] = parts[1]

		def __getitem__ (self, id):
			return self.data[id]
		
		def __setitem__ (self, id, value):
			self.data[id] = value

		def __contains__ (self, id):
			return id in self.data

		def __call__ (self, id, dflt = None):
			try:
				return self[id]
			except KeyError:
				return dflt
		
		def copy (self):
			rc = self.__class__ (self.dbid, None)
			rc.data = self.data.copy ()
			return rc

	parseLine = re.compile ('([a-z0-9._+-]+):[ \t]*(.*)$', re.IGNORECASE)
	def __read (self):
		self.data = {}
		if not os.path.isfile (self.path):
			dbcfg = _syscfg.get ('dbcfg')
			if dbcfg is not None:
				id = DBDriver.dbid
				self.data[id] = self.DBRecord (id, dbcfg)
				return
		#
		fd = open (self.path, 'r')
		for line in fd:
			line = line.strip ()
			if not line or line.startswith ('#'):
				continue
			mtch = self.parseLine.match (line)
			if mtch is None:
				continue
			(id, param) = mtch.groups ()
			self.data[id] = self.DBRecord (id, param)

	def __setup (self):
		if self.data is None:
			self.__read ()

	def __getitem__ (self, id):
		self.__setup ()
		return self.data[id]

	def __iter__ (self):
		self.__setup ()
		return iter (self.data)

	def inject (self, id, param):
		"""adds a configuration which is not part of the configuration file

with this method it is possible to add a configuration for a database
which is not part of the configuration file."""
		self.__setup ()
		rec = self.DBRecord (id, None)
		rec.data = param
		self.data[id] = rec
	
	def remove (self, id):
		"""remove a configuration entry"""
		self.__setup ()
		if id in self.data:
			del self.data[id]

class DBDriver (object):
	"""Keeps track of available database driver

in conjunction with DBConfig this class can select a database driver
and create an instance for it."""
	dbid = _syscfg.get ('dbid', 'openemm')
	dbcfg = None
	Driver = collections.namedtuple ('Driver', ['driver', 'new'])
	dbDrivers = {
		'oracle':	Driver (DBOracle, lambda cfg: DBOracle (cfg ('user'), cfg ('password'), cfg ('sid'))),
		'mysql':	Driver (DBMySQL, lambda cfg: DBMySQL (cfg ('host'), cfg ('user'), cfg ('password'), cfg ('name'))),
		'mariadb':	Driver (DBMySQL, lambda cfg: DBMySQL (cfg ('host'), cfg ('user'), cfg ('password'), cfg ('name'))),
		'sqlite':	Driver (DBSQLite3, lambda cfg: DBSQLite3 (cfg ('filename'), extendedTypes = atob (cfg ('extended-types', False)), extendedRows = atob (cfg ('extended-rows', False)), textType = cfg ('text-type'), extendedFunctions = atob (cfg ('extended-functions', False))))
	}
	dbDriverAliases = {
		'sqlite3':	'sqlite'
	}
	@classmethod
	def request (cls, id):
		"""Creates a new database driver instance

according to ``id'' the matching driver and the access parameter are
selected from the database configuration."""
		if id is None:
			id = cls.dbid
		if cls.dbcfg is None:
			cls.dbcfg = DBConfig ()
		cfg = cls.dbcfg[id]
		dbms = cfg ('dbms')
		try:
			if dbms in cls.dbDriverAliases:
				creator = cls.dbDrivers[cls.dbDriverAliases[dbms]]
			else:
				creator = cls.dbDrivers[dbms]
		except KeyError:
			raise error ('Missing/unknwon dbms "%s" found' % dbms)
		if creator.driver is None:
			raise error ('no driver for "%s" available' % dbms)
		rc = creator.new (cfg)
		rc.setup ()
		return rc
	
	@classmethod
	def addDriver (cls, driverName, driverClass, driverNew):
		"""adds a new driver to list of known driver"""
		cls.dbDrivers[driverName] = cls.Driver (driverClass, driverNew)
	
	@classmethod
	def removeDriver (cls, driverName):
		"""removes a driver from the list of known driver"""
		if driverName in cls.dbDrivers:
			del cls.dbDrivers[driverName]

	@classmethod
	def inject (cls, id, param):
		"""adds a new configuration into the database configuration"""
		if cls.dbcfg is None:
			cls.dbcfg = DBConfig ()
		cls.dbcfg.inject (id, param)
	
	@classmethod
	def remove (cls, id):
		"""removes a configuration from the database configuration"""
		if cls.dbcfg is None:
			cls.dbcfg = DBConfig ()
		cls.dbcfg.remove (id)

def DBaseID (dbid = None):
	"""Selects database driver according to dbid

this is a convient function to select the proper database driver for
the configuration for ``dbid''. If ``dbid'' is None, then the default
id for this installation is used.

This function should be used in general to select a database driver,
unless you have some reasons to access the database driver
directly."""
	try:
		return DBDriver.request (dbid)
	except (IOError, KeyError) as e:
		log (LV_WARNING, 'dbid', 'Failed to find driver for %s (%s)' % (str (dbid), str (e)))
	return None

if DBSQLite3 is not None:
	class DBSimulate (object):
		"""Offers a database simulation

this class is the base for using a local sqlite3 database for
simulation, if you have not access to the target database or you need
a scratch database during development process without risking damage
of the productive database during the early stage of development. See
sim.DBSimulate for a more complete implementation."""
		dbms = 'dbsim'
		class Cursor (DBCursorSQLite3):
			def __init__ (self, *args, **kws):
				super (DBSimulate.Cursor, self).__init__ (*args, **kws)
				self.ref = None

			def intercept (self, id, args, kws):
				if self.ref is not None:
					args = self.ref.intercept (id, args, kws)
					if args is not None and type (args) != tuple:
						try:
							args = tuple (args)
						except TypeError:
							args = (args, )
				return args

			def query (self, *args, **kws):
				args = self.intercept ('query', args, kws)
				if args is None:
					return None
				return super (DBSimulate.Cursor, self).query (*args, **kws)

			def execute (self, *args, **kws):
				args = self.intercept ('execute', args, kws)
				if args is None:
					return None
				return super (DBSimulate.Cursor, self).execute (*args, **kws)
				
		class Driver (DBSQLite3):
			def __init__ (self, *args, **kws):
				super (DBSimulate.Driver, self).__init__ (*args, **kws)
				self.dbms = DBSimulate.dbms
				self.cursorClass = DBSimulate.Cursor
				self.ref = None
			
			def setup (self):
				if self.ref is not None:
					self.ref.setup (self)
			
			def cursor (self, *args, **kws):
				c = super (DBSimulate.Driver, self).cursor (*args, **kws)
				if c is not None:
					c.ref = self.ref
				return c
				
		def __init__ (self, filename):
			"""``filename'' is the filename to store the simulation database to"""
			self.filename = filename
			self.dbid = 'simulate:%s' % filename
			self.save = None

		def __enter__ (self):
			self.enable ()
			
		def __exit__ (self, exc_type, exc_value, traceback):
			self.disable ()
			return False

		def __new (self, cfg):
			rc = self.Driver (
				cfg ('filename'),
				layout = cfg ('layout'),
				extendedTypes = cfg ('extendedTypes', True),
				extendedRows = cfg ('extendedRows', False),
				textType = cfg ('textType'),
				extendedFunctions = cfg ('extendedFunctions', True)
			)
			if rc is not None:
				rc.ref = self
			return rc

		State = collections.namedtuple ('State', ['dbid'])
		def enable (self, **kws):
			"""enables simulation

this enables the use of the simulation database and sets the default
database ID to the simlated database."""
			if self.save is None:
				self.save = self.State (DBDriver.dbid)
				cfg = kws.copy ()
				cfg['dbms'] = self.dbms
				cfg['filename'] = self.filename
				for (key, value) in ('extendedTypes', True), ('extendedFunctions', True):
					if key not in cfg:
						cfg[key] = value
				DBDriver.inject (self.dbid, cfg)
				DBDriver.dbid = self.dbid
				DBDriver.addDriver (self.dbms, self.Driver, self.__new)
				self.log (LV_INFO, 'simulation enabled')
			else:
				self.log (LV_INFO, 'simulation is already enabled')
	
		def disable (self):
			"""disbale simulation

this rollbacks an enabled simulated and resets the database ID to its
original value."""
			if self.save is not None:
				DBDriver.removeDriver (self.dbms)
				DBDriver.remove (self.dbid)
				DBDriver.dbid = self.save.dbid
				self.save = None
				self.log (LV_INFO, 'simulation disabled')
			else:
				self.log (LV_INFO, 'simulation is already disabled')

		__reserved = set ("""ABORT ACTION ADD AFTER ALL ALTER ANALYZE AND AS ASC ATTACH AUTOINCREMENT
		BEFORE BEGIN BETWEEN BY CASCADE CASE CAST CHECK COLLATE COLUMN COMMIT
		CONFLICT CONSTRAINT CREATE CROSS CURRENT_DATE CURRENT_TIME
		CURRENT_TIMESTAMP DATABASE DEFAULT DEFERRABLE DEFERRED DELETE DESC DETACH
		DISTINCT DROP EACH ELSE END ESCAPE EXCEPT EXCLUSIVE EXISTS EXPLAIN FAIL FOR
		FOREIGN FROM FULL GLOB GROUP HAVING IF IGNORE IMMEDIATE IN INDEX INDEXED
		INITIALLY INNER INSERT INSTEAD INTERSECT INTO IS ISNULL JOIN KEY LEFT LIKE
		LIMIT MATCH NATURAL NO NOT NOTNULL NULL OF OFFSET ON OR ORDER OUTER PLAN
		PRAGMA PRIMARY QUERY RAISE RECURSIVE REFERENCES REGEXP REINDEX RELEASE
		RENAME REPLACE RESTRICT RIGHT ROLLBACK ROW SAVEPOINT SELECT SET TABLE TEMP
		TEMPORARY THEN TO TRANSACTION TRIGGER UNION UNIQUE UPDATE USING VACUUM
		VALUES VIEW VIRTUAL WHEN WHERE WITH WITHOUT""".split ())
	
		Rule = collections.namedtuple ('Rule', ['table', 'clause'])
		def fill (self, dbid, rules):
			"""fill a simulated database with real data

this is used to fill a simulation database from a real productive
database to provide required test data. The simulation must not be
enabled at this stage. ``rules'' is a list of rules where each rule
must have these attributes (``DBSimulate.Rule'' can be used to create
a rule):
	- rule.table (required): the name of the table to be used for source and target
	- rule.clause (optional): a where clause to reduce the amount of data copied (otherwise the whole content is retrieved!)
"""
			if self.save:
				raise error ('cannot fill database while simulation is enabled')
			src = DBaseID (dbid)
			try:
				if not src.open ():
					raise error ('cannot open source database: %s' % src.lastError ())
				scursor = src.cursor ()
				dest = DBSQLite3 (self.filename, extendedTypes = True, extendedFunctions = True)
				try:
					if not dest.open ():
						raise error ('cannot open destination database: %s' % dest.lastError ())
					self.log (LV_INFO, 'databases for filling setup completed')
					dcursor = dest.cursor ()
					dcursor.mode ('fast')
					def reserved (name):
						if name.upper () in self.__reserved:
							return '`%s`' % name
						return name
					types = {}
					for (generic, names) in [
						(src.driver.NUMBER, ()),
						(src.driver.TIMESTAMP, ('DATETIME', 'DATE', 'TIME')),
						(src.driver.BINARY, ('BLOB', 'LOB'))
					]:
						types[generic] = set ([generic])
						for name in names:
							if name in src.driver.__dict__:
								types[generic].add (src.driver.__dict__[name])
					def mapper (field):
						if field.typ == src.NUMBER:
							if field.size and field.precision:
								return 'real'
							return 'integer'
						elif field.typ == src.TIMESTAMP:
							return 'timestamp'
						elif field.typ == src.BINARY:
							return 'binary'
						return 'text'
					def convert (typ, value):
						if value is not None:
							if typ == 'text':
								if type (value) is not unicode:
									value = str (value)
									try:
										return unicode (value, 'UTF-8')
									except UnicodeDecodeError:
										for fallback in ['ISO-8859-15', ]:
											with Ignore (UnicodeDecodeError):
												return unicode (value, fallback)
										return unicode (value, 'UTF-8', 'replace')
							elif typ == 'binary':
								return dest.Binary (str (value))
						return value
					for rule in rules:
						table = rule.table.lower ()
						self.log (LV_INFO, 'fill %s' % table)
						rq = dcursor.querys ('SELECT count(*) FROM sqlite_master WHERE lower(name) = :name AND type = :type', {'name': table, 'type': 'table'})
						exists = rq is not None and rq[0] == 1
						query = 'SELECT * FROM %s' % table
						scursor.querys ('%s WHERE 1 = 0' % query)
						layout = scursor.description (True)
						if layout is not None:
							fields = []
							for field in layout:
								fields.append ((field.name, mapper (field)))
							if exists:
								stmt = 'DROP TABLE %s' % table
								dcursor.execute (stmt)
							stmt = 'CREATE TABLE %s (\n%s\n)' % (table, ',\n'.join (['%s %s' % (reserved (_f[0]), _f[1]) for _f in fields]))
							dcursor.execute (stmt)
							with Ignore (AttributeError):
								clause = rule.clause
								if clause:
									query += ' WHERE %s' % clause
							insert = 'INSERT INTO %s VALUES (%s)' % (table, ', '.join ([':v%d' % _n for _n in range (len (fields))]))
							for row in scursor.query (query):
								data = {}
								for (nr, field, value) in zip (range (len (fields)), fields, row):
									data['v%d' % nr] = convert (field[1], value)
								dcursor.execute (insert, data)
							dcursor.sync ()
						self.log (LV_INFO, 'fill %s done' % table)
					dcursor.close ()
				finally:
					dest.close ()
				scursor.close ()
			finally:
				src.close ()
		#
		# these can be overwritten by subclass
		def log (self, level, msg):
			"""log a message

a simple implementation of this class could be:
	agn.log ('sim', level, msg)"""
			pass
		def setup (self, db):
			"""setup the simlation database

``db'' ist the database driver for the simulation database. Here you
can setup the database to reflect your requirements."""
			pass
		def intercept (self, id, args, kws):
			"""intercepts a database query

where ``id'' is either "query" or "execute" depending of the called
method. ``args'' and ``kws'' are the unmodified arguments and keyword
arguments for this call. You can modifiy them and ensure that args is
used as the return value.

With this method you can interpret SQL statements that the underlying
sqlite3 database does not understand to allow handling of other
database specific features (e.g. oracle sequences.)"""
			return args

class Datasource (object):
	"""Get or create a datasource id by Name

This class fetches an existing or creates a new datasource_id by
name."""
	def __init__ (self):
		self.cache = {}

	def getID (self, name, companyID, sourceGroup, db = None):
		"""get an existing ID or creates a new one

Retrieves the datasource-id for a given ``name'' for the company
``companyID''. If it does not exists, a new one is created using the
``sourceGroup'' (either numeric or textual representation). The
optional ``db'' parameter is an open database driver, if this is None,
a default database driver is created for database access."""
		key = (name, companyID)
		try:
			rc = self.cache[key]
		except KeyError:
			rc = None
			if db is None:
				db = DBaseID ()
				dbOpened = True
			else:
				dbOpened = False
			if not db is None:
				curs = db.cursor ()
				if not curs is None:
					for state in [0, 1]:
						for rec in curs.query ('SELECT datasource_id FROM datasource_description_tbl WHERE company_id = %d AND description = :description' % companyID, {'description': name}):
							rc = int (rec[0])
						if rc is None and state == 0:
							for sgField in 'sourcegroup_type', 'description':
								if type (sourceGroup) in (int, long):
									break
								rq = curs.querys ('SELECT sourcegroup_id FROM sourcegroup_tbl WHERE %s = :source' % sgField, {'source': sourceGroup})
								if rq is not None and rq[0] is not None:
									sourceGroup = int (rq[0])
							if type (sourceGroup) not in (int, long):
								raise error ('Invalid sourceGroup: %r' % (sourceGroup, ))
							rq = curs.querys ('SELECT sourcegroup_type, description FROM sourcegroup_tbl WHERE sourcegroup_id = :source', {'source': sourceGroup})
							if rq is None:
								raise error ('Unknown sourceGroup: %d' % sourceGroup)
							query = curs.qselect (oracle = \
								'INSERT INTO datasource_description_tbl (datasource_id, description, company_id, sourcegroup_id, timestamp) VALUES ' + \
								'(datasource_description_tbl_seq.nextval, :description, %d, %d, sysdate)' % (companyID, sourceGroup), \
								mysql = \
								'INSERT INTO datasource_description_tbl (description, company_id, sourcegroup_id, timestamp) VALUES ' + \
								'(:description, %d, %d, current_timestamp)' % (companyID, sourceGroup), \
								sqlite = \
								'INSERT INTO datasource_description_tbl (description, company_id, sourcegroup_id, timestamp) VALUES ' + \
								'(:description, %d, %d, current_timestamp)' % (companyID, sourceGroup))
							curs.execute (query, {'description': name}, commit = True)
							log (LV_INFO, 'datasource', 'Created new datasource id companyID %d with %s for %s (%s)' % (companyID, name, rq[1], rq[0]))
					curs.close ()
				else:
					log (LV_ERROR, 'datasource', 'Failed to get database cursor: %s' % db.lastError ())
				if dbOpened:
					db.close ()
			else:
				log (LV_ERROR, 'datasource', 'Failed to setup database interface')
			if not rc is None:
				self.cache[key] = rc
				log (LV_INFO, 'datasource', 'Found datasource %d for companyID %d with %s' % (rc, companyID, name))
			else:
				log (LV_ERROR, 'datasource', 'Did not found datasource for companyID %d with %s' % (companyID, name))
		return rc

class ParseTimestamp (object):
	"""parses common used timestamps

the instance is called directly, i.e.:
	ParseTimestamp () ('2017-02-16 11:59:30')
"""
	__months = {
		'jan':  1, 'feb':  2, 'mar':  3, 'apr':  4, 'may':  5, 'jun':  6,
		'jul':  7, 'aug':  8, 'sep':  9, 'oct': 10, 'nov': 11, 'dec': 12
	}
	__parser = [
		(re.compile ('([0-9]{4})-([0-9]{2})-([0-9]{2})([ :T]([0-9]{2}):([0-9]{2}):([0-9]{2}))?'), (0, 1, 2)),
		(re.compile (' *([0-9]{1,2})\\.([0-9]{1,2})\\.([0-9]{4})( +([0-9]{2}):([0-9]{2}):([0-9]{2}))?'), (2, 1, 0)),
		(re.compile ('(%s) +([0-9]+) ([0-9]{2}):([0-9]{2}):([0-9]{2})' % '|'.join (__months.keys ()), re.IGNORECASE), lambda s, *a: s.__parseSendmailLogdate (*a))
	]

	def __parseSendmailLogdate (self, monthName, day, hour, min, sec):
		month = self.__months[monthName.lower ()]
		now = datetime.datetime.now ()
		year = now.year
		if now.month < month:
			year -= 1
		return datetime.datetime (year, month, int (day), int (hour), int (min), int (sec))
		
	def __call__ (self, s, dflt = None, *args):
		"""parses a string to a datetime.datetime representation

``s'' may either be a supported time pattern or one of the static
strings "now" (for the current timestamp) or "epoch" (for the start of
the epoch, the 1.1.1970).

If the parsing failed and ``dflt'' is a callable method, then this is
called with ``*args'' as its argument to create the return value.
Otherwise None is returned."""
		if s is not None:
			ty = type (s)
			if ty in (str, unicode):
				for (pattern, seq) in self.__parser:
					m = pattern.match (s)
					if m is not None:
						g = m.groups ()
						if callable (seq):
							try:
								ts = seq (self, *g)
								if ts is not None:
									return ts
							except Exception as e:
								log (LV_ERROR, 'parsetimestamp', 'Failed to parse %s: %s' % (s, str (e)))
						else:
							if g[3] is None:
								return datetime.datetime (int (g[seq[0]]), int (g[seq[1]]), int (g[seq[2]]))
							else:
								return datetime.datetime (int (g[seq[0]]), int (g[seq[1]]), int (g[seq[2]]), int (g[4]), int (g[5]), int (g[6]))
						break
				if s == 'now':
					now = time.localtime ()
					return datetime.datetime (now.tm_year, now.tm_mon, now.tm_mday, now.tm_hour, now.tm_min, now.tm_sec)
				elif s == 'epoch':
					return datetime.datetime (1970, 1, 1)
			elif ty in (int, long, float):
				tm = time.localtime (s)
				return datetime.datetime (tm.tm_year, tm.tm_mon, tm.tm_mday, tm.tm_hour, tm.tm_min, tm.tm_sec)
			elif ty is time.struct_time:
				return datetime.datetime (s.tm_year, s.tm_mon, s.tm_mday, s.tm_hour, s.tm_min, s.tm_sec)
			elif ty is datetime.datetime:
				return s
		if callable (dflt):
			return dflt (*args)
		return dflt

	def dump (self, d):
		"""returns ``d'' (datetime.datetime) as a parsable string representation"""
		return '%04d-%02d-%02d %02d:%02d:%02d' % (d.year, d.month, d.day, d.hour, d.minute, d.second)

class Period (ParseTimestamp):
	"""Represents a period of time"""
	def __init__ (self, start, end):
		ParseTimestamp.__init__ (self)
		def fail (what, s):
			raise error ('failed to parse %s: %r' % (what, s))
		self.start = self (start, fail, 'start', start)
		self.end = self (end, fail, 'end', end)
	
	def __str__ (self):
		return '%s .. %s' % (str (self.start), str (self.end))
	
	def __repr__ (self):
		return '<class Period (%r, %r)>' % (self.start, self.end)

class Timestamp (object):
	"""Persistance timestamp handling

for incremental processes it is important to keep track of last
timestamp it ran. To keep track of this timestamp this class manages
the persistance and offers some handy methods."""
	Interval = collections.namedtuple ('Interval', ['start', 'end'])
	Cascade = collections.namedtuple ('Cascade', ['clause', 'cascade'])
	def __init__ (self, name, initialTimestamp = None, description = None):
		self.name = name
		self.initialTimestamp = initialTimestamp
		self.description = description
		self.db = None
		self.mydb = False
		self.cursor = None
		self.parm = {'name': self.name}
		self.lowmark = None
		self.highmark = None
		self.parseTimestamp = ParseTimestamp ()

	def __cleanup (self):
		if not self.cursor is None:
			self.cursor.close ()
			self.cursor = None
		if self.mydb and not self.db is None:
			self.db.close ()
			self.db = None

	def __setup (self, db):
		if db is None:
			self.db = DBaseID ()
			self.mydb = True
		else:
			self.db = db
			self.mydb = False
		if self.db is None:
			raise error ('Failed to setup database')
		self.cursor = self.db.cursor ()
		if self.cursor is None:
			self.__cleanup ()
			raise error ('Failed to get database cursor')
		count = self.cursor.querys ('SELECT count(*) FROM timestamp_tbl WHERE name = :name', self.parm)
		if count is None or not count[0]:
			rc = self.cursor.querys ('SELECT max(timestamp_id) + 1 FROM timestamp_tbl')
			if not rc is None and not rc[0] is None:
				tid = rc[0]
			else:
				tid = 1
			ts = self.parseTimestamp (self.initialTimestamp)
			if ts is None:
				ts = datetime.datetime (1980, 1, 1)
			if self.cursor.execute ('INSERT INTO timestamp_tbl (timestamp_id, name, description, cur) VALUES (:tid, :name, :descr, :ts)', {'tid': tid, 'name': self.name, 'descr': self.description, 'ts': ts}) != 1:
				self.__cleanup ()
				raise error ('Failed to create new entry in timestamp table')
		elif count[0] != 1:
			raise error ('Expect one entry with name "%s", but found %d' % (self.name, count[0]))

	def done (self, commit = True):
		"""finalize timestamp

if ``commit'' is True, write new timestamp to database, otherwise
leave it unchanged."""
		rc = False
		if not self.cursor is None:
			if commit:
				if self.cursor.execute ('UPDATE timestamp_tbl SET prev = cur WHERE name = :name', self.parm) == 1 and self.cursor.execute ('UPDATE timestamp_tbl SET cur = temp WHERE name = :name', self.parm) == 1:
					self.cursor.sync ()
					rc = True
			else:
				self.cursor.sync (False)
				rc = True
		self.__cleanup ()
		if not rc:
			raise error ('Failed to finalize timestamp entry')

	def setup (self, db = None, timestamp = None):
		"""setup timestamp

either reads an existing timestamp from the database or uses
``timestamp'' as the current value. If ``timestamp'' is None, then the
current timestamp is used. ``db'' is an optional parameter to use an
existing database driver, otherwise an own driver is created fro the
default database id."""
		self.__setup (db)
		timestamp = self.parseTimestamp (timestamp)
		if timestamp is None:
			timestamp = datetime.datetime.now ()
		parm = self.parm.copy ()
		parm['ts'] = timestamp
		if self.cursor.execute ('UPDATE timestamp_tbl SET temp = :ts WHERE name = :name', parm) != 1:
			raise error ('Failed to setup timestamp for current time')
		rc = self.cursor.querys ('SELECT cur, temp FROM timestamp_tbl WHERE name = :name', self.parm)
		if not rc is None:
			(self.lowmark, self.highmark) = rc
		self.cursor.sync ()

	def __prepName (self, parm):
		if parm is None:
			name = '\'%s\'' % self.name.replace ('\'', '\'\'')
		else:
			name = ':timestampName'
			parm[name[1:]] = self.name
		return name

	def makeSelectLowerMark (self, parm = None):
		"""creates a SQL statement to read the low mark"""
		return 'SELECT cur FROM timestamp_tbl WHERE name = %s' % self.__prepName (parm)

	def makeSelectUpperMark (self, parm = None):
		"""creates a SQL statement to read the high mark"""
		return 'SELECT temp FROM timestamp_tbl WHERE name = %s' % self.__prepName (parm)

	def makeBetweenClause (self, column, parm = None):
		"""creates a SQL clause to check if a column is in current delta

if ``parm'' is not None it must be of type dict. In this case the name
of the timestamp is added to the dictionary and the value is handled
for a prepared statement, otherwise the name is directly written to
the clause."""
		name = self.__prepName (parm)
		return '(%(column)s >= (SELECT cur FROM timestamp_tbl WHERE name = %(name)s) AND %(column)s < (SELECT temp FROM timestamp_tbl WHERE name = %(name)s))' % \
			{'column': column, 'name': name}

	def makeIntervalClause (self, column):
		"""create a SQL clause for a delta (using retrieved data)"""
		return '(%(column)s >= :timestampStart AND %(column)s < :timestampEnd)' % {'column': column}

	def makeInterval (self):
		"""create an interval from low to high"""
		start = self.lowmark
		end = self.highmark
		if start is None:
			start = self.parseTimestamp ('epoch')
		if end is None:
			end = self.parseTimestamp ('now')
		return self.Interval (start, end)

	def makeCascadingInterval (self, days = 1):
		"""creates a list of intervals to cover as a whole the interval from low to high"""
		rc = None
		interval = self.makeInterval ()
		if interval is not None:
			if days < 1:
				days = 1
			cur = interval.start
			while cur < interval.end:
				step = cur.fromordinal (cur.toordinal () + days)
				if step > interval.end:
					step = interval.end
				if rc is None:
					rc = []
				rc.append (self.Interval (cur, step))
				cur = step
		return rc

	def makeCascadingQueries (self, column, param = None, days = 1):
		"""creates a SQL clause and a list of intervals to cover from low to high

this is used if you expected a lot of data to be read or a large
interval to be covered. In this case it returns an instance of class
agn.Timestamp.Cascade with two attributes:

	- clause: the clause to be used in a prepared SQL statement
	- cascade: a list of dictionaries containing each time slice

The optional ``param'' (a dict) is used as the base for each created
dictionary in cascade so you can directly use this for each
incarnation of the query."""

		rc = None
		cc = self.makeCascadingInterval (days)
		if cc is not None:
			rc = self.Cascade (self.makeIntervalClause (column), [])
			for c in cc:
				if param is not None:
					p = param.copy ()
				else:
					p = {}
				p['timestampStart'] = c.start
				p['timestampEnd'] = c.end
				rc.cascade.append (p)
		return rc
#}}}
#
# 10.) Simple templating
#
#{{{
class MessageCatalog:
	"""Message Catalog for templating

This class is primary designed to be integrated in the templating system,
but can also be used stand alone. You instanciate the class with a file
name of the message file which contains of a default section (starting
from top or introduced by a section "[*]". For each supported language
you add a section with the language token, e.g. "[de]" for german and
a list of tokens with the translation. A message catalog file may look
like this:
#	comments start as usual with a hash sign
#	this is the default section
yes: Yes
no: No
#
#	this is the german version
[de]
yes: Ja
no: Nein

You may extend an entry over the current line with a trailing backslash.

If you pass a message catalog to the templating system, you can refer
to the catalog by either using ${_['token']} to just translate one token
or using ${_ ('In your mother language YES means %(yes)')}. There are
also shortcut versions for ${_['xxx']) can be written as _[xxx] and
${_ ('xxx')} can be written as _{xxx}.

In a stand alone variante, this looks like this:
>>> m = MessageCatalog ('/some/file/name')
>>> m.setLang ('de')
>>> print m['yes']
Ja
>>> print m ('yes')
yes
>>> print m ('yes in your language is %(yes)')
yes in your language is Ja
>>> print m['unset']
*unset*
>>> m.setFill (None)
>>> print m['unset']
unset

As you can see in the last example an unknown token is expanded to itself
surrounded by a fill string, if set (to easyly catch missing tokens). If
you unset the fill string, the token itself is used with no further
processing.
"""
	messageParse = re.compile ('%\\(([^)]+)\\)')
	commentParse = re.compile ('^[ \t]*#')
	def __init__ (self, fname, lang = None, fill = '*'):
		"""``fname'' is the path to the message file, ``lang''
the language to use, ``fill'' used to mark unknown tokens"""
		self.messages = {None: {}}
		self.lang = None
		self.fill = fill
		if not fname is None:
			cur = self.messages[None]
			fd = open (fname, 'r')
			for line in [_l.strip () for _l in fd.read ().replace ('\\\n', '').split ('\n') if _l and self.commentParse.match (_l) is None]:
				if len (line) > 2 and line.startswith ('[') and line.endswith (']'):
					lang = line[1:-1]
					if lang == '*':
						lang = None
					if not lang in self.messages:
						self.messages[lang] = {}
					cur = self.messages[lang]
				else:
					parts = line.split (':', 1)
					if len (parts) == 2:
						(token, msg) = [_p.strip () for _p in parts]
						if len (msg) >= 2 and msg[0] in '\'"' and msg[-1] == msg[0]:
							msg = msg[1:-1]
						cur[token] = msg
			fd.close ()

	def __setitem__ (self, token, s):
		try:
			self.messages[self.lang][token] = s
		except KeyError:
			self.messages[self.lang] = {token: s}

	def __getitem__ (self, token):
		try:
			msg = self.messages[self.lang][token]
		except KeyError:
			if not self.lang is None:
				try:
					msg = self.messages[None][token]
				except KeyError:
					msg = None
			else:
				msg = None
		if msg is None:
			if self.fill is None:
				msg = token
			else:
				msg = '%s%s%s' % (self.fill, token, self.fill)
		return msg

	def __call__ (self, s):
		return self.messageParse.subn (lambda m: self[m.groups ()[0]], s)[0]

	def setLang (self, lang):
		"""set the language to use"""
		self.lang = lang

	def setFill (self, fill):
		"""set the fill string to mark unknown tokens"""
		self.fill = fill

class Template:
	"""Simple general templating

This class offers a simple templating system. One instance the class
using the template in string from. The syntax is inspirated by velocity,
but differs in serveral ways (and is even simpler). A template can start
with an optional code block surrounded by the tags '#code' and '#end'
followed by the content of the template. Access to variables and
expressions are realized by $... where ... is either a simple varibale
(e.g. $var) or something more complex, then the value must be
surrounded by curly brackets (e.g. ${var.strip ()}). To get a literal
'$'sign, just type it twice, so '$$' in the template leads into '$'
in the output. A trailing backslash removes the following newline to
join lines.

Handling of message catalog is either done by calling ${_['...']} and
${_('...')} or by using the shortcut _[this is the origin] or
_{%(message): %(error)}. As this is a simple parser the brackets
must not part of the string in the shortcut, in this case use the
full call.

Control constructs must start in a separate line, leading whitespaces
ignoring, with a hash '#' sign. These constructs are supported and
are mostly transformed directly into a python construct:

## ...                      this introduces a comment up to end of line
#property(expr)             this sets a property of the template
#pragma(expr)               alias for property
#include(expr)              inclusion of file, subclass must realize this
#if(pyexpr)             --> if pyexpr:
#elif(pyexpr)           --> elif pyexpr:
#else                   --> else
#do(pycmd)              --> pycmd
#pass                   --> pass [same as #do(pass)]
#break			--> break [..]
#continue		--> continue [..]
#for(pyexpr)            --> for pyexpr:
#while(pyexpr)          --> while pyexpr:
#try                    --> try:
#except(pyexpr)         --> except pyexpr:
#finally                --> finally
#with(pyexpr)           --> with pyexpr:
#end                        ends an indention level
#stop                       ends processing of input template

To fill the template you call the method fill(self, namespace, lang = None)
where 'namespace' is a dictonary with names accessable by the template.
Beside, 'lang' could be set to a two letter string to post select language
specific lines from the text. These lines must start with a two letter
language ID followed by a colon, e.g.:

en:This is an example.
de:Dies ist ein Beispiel.

Depending on 'lang' only one (or none of these lines) are outputed. If lang
is not set, these lines are put (including the lang ID) both in the output.
If 'lang' is set, it is also copied to the namespace, so you can write the
above lines using the template language:

#if(lang=='en')
This is an example.
#elif(lang=='de')
Dies ist ein Beispiel.
#end

And for failsafe case, if lang is not set:

#try
 #if(lang=='en')
This is an example.
 #elif(lang=='de')
Dies ist ein Beispiel.
 #end
#except(NameError)
 #pass
#end
"""
	codeStart = re.compile ('^[ \t]*#code[^\n]*\n', re.IGNORECASE)
	codeEnd = re.compile ('(^|\n)[ \t]*#end[^\n]*(\n|$)', re.IGNORECASE | re.MULTILINE)
	token = re.compile ('((^|\n)[ \t]*#(#|property|pragma|include|if|elif|else|do|pass|break|continue|for|while|try|except|finally|with|end|stop)|\\$(\\$|[0-9a-z_]+(\\.[0-9a-z_]+)*|\\{[^}]*\\})|_(\\[[^]]+\\]|{[^}]+}))', re.IGNORECASE | re.MULTILINE)
	rplc = re.compile ('\\\\|"|\'|\n|\r|\t|\f|\v', re.MULTILINE)
	rplcMap = {'\n': '\\n', '\r': '\\r', '\t': '\\t', '\f': '\\f', '\v': '\\v'}
	langID = re.compile ('^([ \t]*)([a-z][a-z]):', re.IGNORECASE)
	emptyCatalog = MessageCatalog (None, fill = None)
	def __init__ (self, content, precode = None, postcode = None):
		"""``content'' is the template itself, ``precode'' and
``postcode'' will be copied literally to the generated code before
resp. after the auto generated code"""
		self.content = content
		self.precode = precode
		self.postcode = postcode
		self.compiled = None
		self.properties = {}
		self.namespace = None
		self.code = None
		self.indent = None
		self.empty = None
		self.compileErrors = None

	def __getitem__ (self, var):
		return self.namespace.get (var, '') if self.namespaces is not None else None

	def __setProperty (self, expr):
		try:
			(var, val) = [_e.strip () for _e in expr.split ('=', 1)]
			if len (val) >= 2 and val[0] in '"\'' and val[-1] == val[0]:
				quote = val[0]
				self.properties[var] = val[1:-1].replace ('\\%s' % quote, quote).replace ('\\\\', '\\')
			elif val.lower () in ('true', 'on', 'yes'):
				self.properties[var] = True
			elif val.lower () in ('false', 'off', 'no'):
				self.properties[var] = False
			else:
				try:
					self.properties[var] = int (val)
				except ValueError:
					self.properties[var] = val
		except ValueError:
			var = expr.strip ()
			if var:
				self.properties[var] = True

	def __indent (self):
		if self.indent:
			self.code += ' ' * self.indent

	def __code (self, code):
		self.__indent ()
		self.code += '%s\n' % code
		if code:
			if code[-1] == ':':
				self.empty = True
			else:
				self.empty = False

	def __deindent (self):
		if self.empty:
			self.__code ('pass')
		self.indent -= 1

	def __compileError (self, start, errtext):
		if not self.compileErrors:
			self.compileErrors = ''
		self.compileErrors += '** %s: %s ...\n\n\n' % (errtext, self.content[start:start + 60])

	def __replacer (self, mtch):
		rc = []
		for ch in mtch.group (0):
			try:
				rc.append (self.rplcMap[ch])
			except KeyError:
				rc.append ('\\x%02x' % ord (ch))
		return ''.join (rc)

	def __escaper (self, s):
		return s.replace ('\'', '\\\'')

	def __compileString (self, s):
		self.__code ('__result.append (\'%s\')' % re.sub (self.rplc, self.__replacer, s))

	def __compileExpr (self, s):
		self.__code ('__result.append (str (%s))' % s)

	def __compileCode (self, token, arg):
		if not token is None:
			if arg:
				self.__code ('%s %s:' % (token, arg))
			else:
				self.__code ('%s:' % token)
		elif arg:
			self.__code (arg)

	def __compileContent (self):
		self.code = ''
		if self.precode:
			self.code += self.precode
			if self.code[-1] != '\n':
				self.code += '\n'
		pos = 0
		clen = len (self.content)
		mtch = self.codeStart.search (self.content)
		if not mtch is None:
			start = mtch.end ()
			mtch = self.codeEnd.search (self.content, start)
			if not mtch is None:
				(end, pos) = mtch.span ()
				self.code += self.content[start:end] + '\n'
			else:
				self.__compileError (0, 'Unfinished code segment')
		self.indent = 0
		self.empty = False
		self.code += '__result = []\n'
		while pos < clen:
			mtch = self.token.search (self.content, pos)
			if mtch is None:
				start = clen
				end = clen
			else:
				(start, end) = mtch.span ()
				groups = mtch.groups ()
				if groups[1]:
					start += len (groups[1])
			if start > pos:
				self.__compileString (self.content[pos:start])
			pos = end
			if not mtch is None:
				tstart = start
				if not groups[2] is None:
					token = groups[2]
					arg = ''
					if token != '#':
						if pos < clen and self.content[pos] == '(':
							pos += 1
							level = 1
							quote = None
							escape = False
							start = pos
							end = -1
							while pos < clen and level > 0:
								ch = self.content[pos]
								if escape:
									escape = False
								elif ch == '\\':
									escape = True
								elif not quote is None:
									if ch == quote:
										quote = None
								elif ch in '\'"':
									quote = ch
								elif ch == '(':
									level += 1
								elif ch == ')':
									level -= 1
									if level == 0:
										end = pos
								pos += 1
							if start < end:
								arg = self.content[start:end]
							else:
								self.__compileError (tstart, 'Unfinished statement')
						if pos < clen and self.content[pos] == '\n':
							pos += 1
					if token == '#':
						while pos < clen and self.content[pos] != '\n':
							pos += 1
						if pos < clen:
							pos += 1
					elif token in ('property', 'pragma'):
						self.__setProperty (arg)
					elif token in ('include', ):
						try:
							included = self.include (arg)
							if included:
								self.content = self.content[:pos] + included + self.content[pos:]
								clen += len (included)
						except error as e:
							self.__compileError (tstart, 'Failed to include "%s": %s' % (arg, e))
					elif token in ('if', 'else', 'elif', 'for', 'while', 'try', 'except', 'finally', 'with'):
						if token in ('else', 'elif', 'except', 'finally'):
							if self.indent > 0:
								self.__deindent ()
							else:
								self.__compileError (tstart, 'Too many closeing blocks')
						if (arg and token in ('if', 'elif', 'for', 'while', 'except', 'with')) or (not arg and token in ('else', 'try', 'finally')):
							self.__compileCode (token, arg)
						elif arg:
							self.__compileError (tstart, 'Extra arguments for #%s detected' % token)
						else:
							self.__compileError (tstart, 'Missing statement for #%s' % token)
						self.indent += 1
					elif token in ('pass', 'break', 'continue'):
						if arg:
							self.__compileError (tstart, 'Extra arguments for #%s detected' % token)
						else:
							self.__compileCode (None, token)
					elif token in ('do', ):
						if arg:
							self.__compileCode (None, arg)
						else:
							self.__compileError (tstart, 'Missing code for #%s' % token)
					elif token in ('end', ):
						if arg:
							self.__compileError (tstart, 'Extra arguments for #end detected')
						if self.indent > 0:
							self.__deindent ()
						else:
							self.__compileError (tstart, 'Too many closing blocks')
					elif token in ('stop', ):
						pos = clen
				elif not groups[3] is None:
					expr = groups[3]
					if expr == '$':
						self.__compileString ('$')
					else:
						if len (expr) >= 2 and expr[0] == '{' and expr[-1] == '}':
							expr = expr[1:-1]
						self.__compileExpr (expr)
				elif not groups[5] is None:
					expr = groups[5]
					if expr[0] == '[':
						self.__compileExpr ('_[\'%s\']' % self.__escaper (expr[1:-1]))
					elif expr[0] == '{':
						self.__compileExpr ('_ (\'%s\')' % self.__escaper (expr[1:-1]))
				elif not groups[0] is None:
					self.__compileString (groups[0])
		if self.indent > 0:
			self.__compileError (0, 'Missing %d closing #end statement(s)' % self.indent)
		if self.compileErrors is None:
			if self.postcode:
				if self.code and self.code[-1] != '\n':
					self.code += '\n'
				self.code += self.postcode
			self.compiled = compile (self.code, '<template>', 'exec')

	def include (self, arg):
		"""method to overwrite to implement the #include statement"""
		raise error ('Subclass responsible for implementing "include (%r)"' % arg)

	def property (self, var, dflt = None):
		"""returns a property from the template if found, else ``dflt''"""
		try:
			return self.properties[var]
		except KeyError:
			return dflt

	def compile (self):
		"""compiles the template (generates internal code from template)"""
		if self.compiled is None:
			try:
				self.__compileContent ()
				if self.compiled is None:
					raise error ('Compilation failed: %s' % self.compileErrors)
			except Exception as e:
				raise error ('Failed to compile [%r] %r:\n%s\n' % (type (e), e.args, self.code))

	def fill (self, namespace, lang = None, mc = None):
		"""uses the template to fill it using the parameter
from ``namespace'' for language ``lang'' by using the message catalog
``mc''"""
		if self.compiled is None:
			self.compile ()
		if namespace is None:
			self.namespace = {}
		else:
			self.namespace = namespace.copy ()
		if not lang is None:
			self.namespace['lang'] = lang
		self.namespace['property'] = self.properties
		if mc is None:
			mc = self.emptyCatalog
		mc.setLang (lang)
		self.namespace['_'] = mc
		try:
			exec self.compiled in self.namespace
		except Exception as e:
			raise error ('Execution failed [%s]: %s' % (e.__class__.__name__, str (e)))
		result = ''.join (self.namespace['__result'])
		if not lang is None:
			nresult = []
			for line in result.split ('\n'):
				mtch = self.langID.search (line)
				if mtch is None:
					nresult.append (line)
				else:
					(pre, lid) = mtch.groups ()
					if lid.lower () == lang:
						nresult.append (pre + line[mtch.end ():])
			result = '\n'.join (nresult)
		result = result.replace ('\\\n', '')
		self.namespace['result'] = result
		return result
#}}}
#
# 11.) SSH Wrapper/command issuer
#
#{{{
class Remote:
	"""Access remoote resources using ssh (deprecated, use eagn.SFTP if possible)

this class provides some methods to access remote resources via the
ssh protocol using external commands. It requires a small wrapper
command ``wrap'' that implements a hard timeout for the called command
to avoid endless running commands. To use the sftp procotol instead of
scp for file access, set the insance ``sftp.use'' to True, e.g:

rem = Remote ('user@host')
rem.sftp.use = True
"""
	SFTPControl = namedmutable ('SFTPControl', ['use', 'command', 'tempfile'])
	def __init__ (self, addr):
		self.addr = addr
		self.wrap = 'wrap'
		self.ssh = 'ssh'
		self.scp = 'scp'
		self.sftp = self.SFTPControl (
			use = False,
			command = 'sftp',
			tempfile = mkpath (base, 'var', 'tmp', 'sftp.%d.batch' % os.getpid ())
		)
		self.timeout = 30

	def __wraprc (self, pgm):
		os.environ['WRAPRC'] = 'timeout=%s,program=%s,0+,2-' % (self.timeout, pgm)

	def __ssh (self, cmd):
		self.__wraprc (self.ssh)
		n = os.system ('%s "%s" %s' % (self.wrap, self.addr, cmd))
		if n:
			log (LV_ERROR, '__ssh', 'Failed to execute "%s" for "%s" (%d)' % (cmd, self.addr, n))
			return False
		return True

	def __sshread (self, cmd):
		self.__wraprc (self.ssh)
		pp = os.popen ('%s "%s" %s' % (self.wrap, self.addr, cmd), 'r')
		data = pp.read ()
		n = pp.close ()
		if n:
			log (LV_ERROR, '__sshread', 'Failed to execute "%s" for "%s" (%d)' % (cmd, self.addr, n))
			data = None
		return data

	def __sshwrite (self, cmd, data):
		self.__wraprc (self.ssh)
		pp = os.popen ('%s "%s" %s' % (self.wrap, self.addr, cmd), 'w')
		pp.write (data)
		n = pp.close ()
		if n:
			log (LV_ERROR, '__sshwrite', 'Failed to execute "%s" for "%s" (%d)' % (cmd, self.addr, n))
			return False
		return True

	def __scp (self, src, dest, toremote):
		self.__wraprc (self.scp)
		cmd = '%s -qp' % self.wrap
		if type (src) in (str, unicode):
			src = [ src ]
		else:
			cmd += ' -d'
		for s in src:
			if toremote:
				cmd += ' "%s"' % s
			else:
				cmd += ' "%s:%s"' % (self.addr, s)
		if toremote:
			cmd += ' "%s:%s"' % (self.addr, dest)
		else:
			cmd += ' "%s"' % dest

		n = os.system (cmd)
		if not n:
			return True
		return False

	def __sftp (self, cmds):
		fd = open (self.sftp.tempfile, 'w')
		for cmd in cmds + ['quit']:
			fd.write ('%s\n' % cmd)
		fd.close ()
		pp = subprocess.Popen ([self.sftp.command, '-b', self.sftp.tempfile, self.addr], stdin = subprocess.PIPE, stdout = subprocess.PIPE, stderr = subprocess.PIPE)
		(pout, perr) = pp.communicate (None)
		os.unlink (self.sftp.tempfile)
		if pp.returncode:
			log (LV_ERROR, '__sftp', 'Failed to execute %r for "%s": (%d)' % (cmds, self.addr, pp.returncode))
			if perr:
				log (LV_ERROR, '__sftp', 'Stderr:\n%s' % perr)
			if pout:
				log (LV_ERROR, '__sftp', 'Stdout:\n%s' % pout)
			pout = None
		else:
			pout = '\n'.join ([_p for _p in pout.split ('\n') if not _p.startswith ('sftp> ')])
		return (pp.returncode == 0, pout)

	def listdir (self, directory = None, mask = None):
		"""``directory'' is the directory of the remote system
to get the file list from, ``mask'' is a regular expression to filter
the result list"""
		rc = None
		cmd = 'ls -1a'
		if directory is not None:
			cmd += ' "%s"' % directory
		if self.sftp.use:
			(st, data) = self.__sftp ([cmd])
		else:
			data = self.__sshread (cmd)
		if data and len (data) > 0:
			rc = []
			for fname in [os.path.basename (_f) for _f in data.split ('\n')]:
				if fname not in ['', '.', '..']:
					rc.append (fname)
			rc.sort ()
			if mask:
				reg = re.compile (mask)
				temp = []
				for fname in rc:
					if reg.match (fname):
						temp.append (fname)
				rc = temp
		return rc

	def getcmd (self, cmd):
		"""``cmd'' is executed on the remote system and the
stdout of the command is returned"""
		if self.sftp.use:
			raise error ('getcmd: not supported using sftp')
		return self.__sshread (cmd)

	def putcmd (self, cmd, data):
		"""``cmd'' is execute on the remote system and
``data'' is fed to the command as stdin"""
		if self.sftp.use:
			raise error ('putcmd: not supported using sftp')
		return self.__sshwrite (cmd, data)

	def putfile (self, local, remote):
		"""copies local file ``local'' to remote file ``remote''"""
		if self.sftp.use:
			(st, out) = self.__sftp (['put -P "%s" "%s"' % (local, remote)])
			return st
		else:
			return self.__scp (local, remote, True)

	def getfile (self, remote, local):
		"""copies remote file ``remote'' to local file ``local''"""
		if self.sftp.use:
			(st, out) = self.__sftp (['get -P "%s" "%s"' % (remote, local)])
			return st
		else:
			return self.__scp (remote, local, False)

	def removefile (self, remote):
		"""remove remote file ``remote''"""
		if self.sftp.use:
			(st, out) = self.__sftp (['rm "%s"' % remote])
			return st
		else:
			return self.__ssh ('rm "%s"' % remote)

	def do (self, cmd):
		"""execute ``cmd'' on remote side either as SFTP
command (if sftp.use is True) or via ssh"""
		if self.sftp.use:
			(st, out) = self.__sftp ([cmd])
			return st
		else:
			return self.__ssh (cmd)
#}}}
#
# 12.) Simple DNS class
#
#{{{
try:
	import	DNS

	class IPv6 (object):
		@classmethod
		def bin2hex (cls, a):
			"""Convert a binary IPv6 address to its hex representation"""
			if len (a) != 16:
				raise ValueError ('input must be exactly 16 bytes')
			return (Stream.range (0, 16, 2)
				.map (lambda n: '%04x' % (ord (a[n]) << 8 | ord (a[n + 1]), ))
				.join (':')
			)
	
		@classmethod
		def hex2bin (cls, h):
			"""Convert an IPv6 address to hex representation to binary"""
			expand = None
			chunks = []
			for elem in h.split (':'):
				if elem == '':
					if expand is None:
						expand = len (chunks)
					elif expand != len (chunks):
						raise ValueError ('only one expansion allowed')
				else:
					value = int (elem, 16)
					if value > 0xffff:
						raise ValueError ('%s: too large' % elem)
					chunks.append (value)
			if len (chunks) < 8:
				if expand is None:
					raise ValueError ('%s: too short (expect 8 chunks, found just %d' % len (chunks))
				while len (chunks) < 8:
					chunks.insert (expand, 0)
			return (Stream (chunks)
				.map (lambda c: (c >> 8, c & 0xff))
				.chain ()
				.map (lambda c: chr (c))
				.join ('')
			)
	
		@classmethod
		def norm (cls, h):
			"""Normalize a hex representation of an IPv6 address"""
			return cls.bin2hex (cls.hex2bin (h))

	class sDNS (object):
		"""simple DNS queries

this calss provides a simple interface for DNS queries."""
		initialized = False

		def __init__ (self):
			if not self.initialized:
				DNS.DiscoverNameServers ()
				sDNS.initialized = True
			self.req = DNS.Request ()

		def __resolve (self, name, qtype, seen):
			rc = []
			seen.add (name)
			ans = self.req.req (name = name, qtype = qtype)
			if ans:
				for elem in ans.answers:
					try:
						if name == elem['name']:
							typename = elem['typename']
							data = elem['data']
							if qtype == typename:
								rc.append (data)
							elif typename == 'CNAME' and data not in seen:
								rc += self.__resolve (data, qtype, seen)
					except KeyError as e:
						log (LV_DEBUG, 'dns', 'Missing element in %r: %s' % (elem, e))
			return rc
			
		def __req (self, name, qtype, func):
			try:
				return func (self.__resolve (name, qtype, set ()))
			except DNS.Error as e:
				log (LV_DEBUG, 'dns', 'Failed to query %s for %s: %s' % (name, qtype, e))
				return None

		def __a (self, ans):
			return ans
		def getAddress (self, name):
			"""get IP4 address(es) for a hostname"""
			return self.__req (name, 'A', self.__a)

		def __aaaa (self, ans):
			try:
				return [IPv6.bin2hex (_a) for _a in ans]
			except ValueError as e:
				log (LV_DEBUG, 'dns', 'Failed to parse IPv6 address %r: %s' % (ans, e))
				return []

		def getAAAAdress (self, name):
			"""get IP6 address(es) for a hostname"""
			return self.__req (name, 'AAAA', self.__aaaa)
		
		def getAllAddresses (self, name):
			"""get IP4 and IP6 address(es) for hostname"""
			return Stream.merge (self.getAddress (name), self.getAAAAdress (name)).filter (lambda a: a is not None).list ()

		def __ptr (self, ans):
			return ans
		def getHostname (self, ipaddr):
			"""get hostname for IP address"""
			if ipaddr.count ('.') > 0:
				parts = ipaddr.split ('.')
				parts.reverse ()
				name = '%s.in-addr.arpa' % '.'.join (parts)
			elif ipaddr.count (':') > 0:
				name = (Stream (IPv6.norm (ipaddr))
					.filter (lambda c: c != ':')
					.reversed ()
					.join ('.', lambda n: '%s.ip6.arpa' % n)
				)
			else:
				raise ValueError ('invalid ipaddr: %s' % ipaddr)
			return self.__req (name, 'PTR', self.__ptr)

		def __mx (self, ans):
			return [_a[1] for _a in ans]
		def getMailexchanger (self, name):
			"""get mailexchange for hostname"""
			return self.__req (name, 'MX', self.__mx)

		def __txt (self, ans):
			rc = []
			for a in ans:
				rc += a
			return rc
		def getText (self, name):
			"""get text record for domainname"""
			return self.__req (name, 'TXT', self.__txt)
except ImportError:
	sDNS = None
#}}}
#
# 13.) Wrapper for Statd
#
#{{{
def statdProxy (hostname, user = None):
	"""provides a xml-rpc proxy to access the statd daemon

``hostname'': is the host to connect to
``port'': is the optional port to connect to (default 8300)"""
	if user is not None:
		port = _syscfg.get ('statd-port-%s' % user)
	else:
		port = None
	if port is None:
		port = _syscfg.get ('statd-port', '8300')
	parts = hostname.split (':', 2)
	if len (parts) == 2:
		(hostname, port) = parts
	return xmlrpclib.ServerProxy ('http://%s:%s' % (hostname, port), allow_none = True)
#}}}
#
# 14.) CSV Simplifier
#
#{{{
class _CSVBase (csv.Dialect):
	doublequote = True
	escapechar = '\\'
	lineterminator = '\r\n'
	quotechar = '"'
	quoting = csv.QUOTE_NONE
	skipinitialspace = True
class _CSVSemicolon (_CSVBase):
	delimiter = ';'
class _CSVSemicolon1 (_CSVSemicolon):
	quoting = csv.QUOTE_MINIMAL
class _CSVSemicolon2 (_CSVSemicolon):
	quoting = csv.QUOTE_ALL
class _CSVComma (_CSVBase):
	delimiter = ','
class _CSVComma1 (_CSVComma):
	quoting = csv.QUOTE_MINIMAL
class _CSVComma2 (_CSVComma):
	quoting = csv.QUOTE_ALL
class _CSVTAB (_CSVBase):
	delimiter = '\t'
class _CSVTAB1 (_CSVTAB):
	quoting = csv.QUOTE_MINIMAL
class _CSVTAB2 (_CSVTAB):
	quoting = csv.QUOTE_ALL
class _CSVBar (_CSVBase):
	delimiter = '|'
class _CSVBar1 (_CSVBar):
	quoting = csv.QUOTE_MINIMAL
class _CSVBar2 (_CSVBar):
	quoting = csv.QUOTE_ALL
class _CSVSpace (_CSVBase):
	delimiter = ' '
class _CSVSpace1 (_CSVSpace):
	quoting = csv.QUOTE_MINIMAL
class _CSVSpace2 (_CSVSpace):
	quoting = csv.QUOTE_ALL
class _CSVAuto (_CSVBase):
	pass
_csvregister = [
	('agn-default', _CSVSemicolon1),
	('agn-semicolon-none', _CSVSemicolon),
	('agn-semicolon-minimal', _CSVSemicolon1),
	('agn-semicolon-full', _CSVSemicolon2),
	('agn-comma-none', _CSVComma),
	('agn-comma-minimal', _CSVComma1),
	('agn-comma-full', _CSVComma2),
	('agn-tab-none', _CSVTAB),
	('agn-tab-minimal', _CSVTAB1),
	('agn-tab-full', _CSVTAB2),
	('agn-bar-none', _CSVBar),
	('agn-bar-minimal', _CSVBar1),
	('agn-bar-full', _CSVBar2),
	('agn-space-none', _CSVSpace),
	('agn-space-minimal', _CSVSpace1),
	('agn-space-full', _CSVSpace2)
]
CSVDialects = []
for( _csvname, _csvclass) in _csvregister:
	csv.register_dialect (_csvname, _csvclass)
	CSVDialects.append (_csvname)
CSVDefault = CSVDialects[0]

class CSVIO (object):
	"""Base class for all CSV wrapper

in general you will not instance this class directly but some other
class (CSVReader, CSVWriter) which inherits this class.

The module ``agn'' defines some often used dialects which can be used
in constructors for the various derivated classes. These all are
starting with "agn-" following a (hopefully) descriptive name:
	- agn-default
	- agn-semicolon-none
	- agn-semicolon-minimal
	- agn-semicolon-full
	- agn-comma-none
	- agn-comma-minimal
	- agn-comma-full
	- agn-tab-none
	- agn-tab-minimal
	- agn-tab-full
	- agn-bar-none
	- agn-bar-minimal
	- agn-bar-full
	- agn-space-none
	- agn-space-minimal
	- agn-space-full

There is als an ``agn.CSVDefault'' which is "agn-semicolon-minimal" as
a default (and has the alias name "agn-default"). The other names the
middle part describes the separator for this dialct and tha last part
the handling of text marks (where ``none'' means no marks at all,
``minimal'' use textmark if required and ``full' use always
textmarks). Textmark is the double quote sign. But you can always
create new dialects either using one of these as a base or starting
with an empty one as supported by the standard "csv" module."""
	BOM = collections.namedtuple ('BOM', ['name', 'bom'])
	boms = [BOM (_b, codecs.__dict__[_b]) for _b in dir (codecs) if _b.startswith ('BOM') and type (codecs.__dict__[_b]) is str]
	maxBomLength = max ([len (_b.bom) for _b in boms])
	bom2charset = {
		codecs.BOM_UTF16_BE:	'UTF-16-BE',
		codecs.BOM_UTF16_LE:	'UTF-16-LE',
		codecs.BOM_UTF32_BE:	'UTF-32-BE',
		codecs.BOM_UTF32_LE:	'UTF-32-LE',
		codecs.BOM_UTF8:	'UTF-8'
	}
	charset2bom = dict ([(_i[1], _i[0]) for _i in bom2charset.items ()])
	def __init__ (self):
		self.fd = None
		self.foreign = False
		self.bom = None
		self.charset = None
		self.empty = False

	def __del__ (self):
		self.done ()

	def __enter__ (self):
		return self

	def __exit__ (self, exc_type, exc_value, traceback):
		self.close ()
		return False

	def open (self, stream, mode, bomCharset = None):
		"""opens a stream for reading or writiing

``stream'' may either be a file like object or a string in the later
case it is used as a filename.

In reading mode it tries to find a BOM (byte order mark) and skip this
for the reading of the content, but set the atribute ``charset'' to
the corrosponding name of the found BOM.

In writing mode and if ``bomCharset'' is not None and a BOM is found
for this name, the BOM is written to the beginning of a file (in
append mode it is only written, if the file had zero length on open)."""
		if type (stream) in (str, unicode):
			self.fd = copen (stream, mode)
			self.foreign = False
		else:
			self.fd = stream
			self.foreign = True
		self.bom = None
		self.charset = None
		if mode is None or 'r' in mode:
			with Ignore (AttributeError):
				self.fd.seek	# just test if method "seek" is available
				pos = self.fd.tell ()
				start = self.fd.read (self.maxBomLength)
				for bom in self.boms:
					if start.startswith (bom.bom):
						pos += len (bom.bom)
						self.bom = bom.name
						self.charset = self.bom2charset.get (bom.bom)
						break
				self.fd.seek (pos)
		elif 'w' in mode or 'a' in mode:
			bom = None
			if bomCharset is not None:
				try:
					self.charset = bomCharset.upper ()
					bom = self.charset2bom[self.charset]
					for b in self.boms:
						if b == bom.bom:
							self.bom = bom.name
							break
				except (AttributeError, KeyError) as e:
					log (LV_DEBUG, 'csvio', 'No BOM for %s found: %s' % (bomCharset, e))
			if self.fd.tell () == 0:
				if bom is not None:
					self.fd.write (bom)
				self.empty = True

	def done (self):
		"""cleanup resources"""
		if not self.fd is None:
			if not self.foreign:
				self.fd.close ()
			else:
				with Ignore (AttributeError):
					if not self.fd.closed:
						self.fd.flush ()
			self.fd = None

	def close (self):
		"""closes an open file descriptor"""
		self.done ()

class _CSVWriter (CSVIO):
	def __init__ (self, stream, mode, bomCharset, header):
		CSVIO.__init__ (self)
		self.writer = None
		self.open (stream, mode if mode is not None else 'w', bomCharset)
		self.header = header

	def write (self, row):
		if self.empty:
			if self.header is not None:
				self.writer.writerow (self.header)
			self.empty = False
		self.writer.writerow (row)

class CSVWriter (_CSVWriter):
	"""Wrapper to write a CSV file"""
	def __init__ (self, stream, dialect, mode = None, bomCharset = None, header = None):
		"""for meaning of ``stream'' and ``bomCharset''see
CSVIO.open (), for availble ``dialect'' values see CSVIO.__doc__"""
		_CSVWriter.__init__ (self, stream, mode, bomCharset, header)
		self.writer = csv.writer (self.fd, dialect = dialect)

class CSVDictWriter (_CSVWriter):
	"""Wrapper to write a CSV file from a dict"""
	def __init__ (self, stream, fieldList, dialect, mode = None, bomCharset = None, header = None, relaxed = False):
		"""for meaning of ``stream'' and ``bomCharset''see
CSVIO.open (), for availble ``dialect'' values see CSVIO.__doc__,
``fieldList'' is an array for the keys of the dictonary and if
``relaxed'' is True, errors are ignored otherwise raised (as
implemented by the csv.DictWriter module)"""
		_CSVWriter.__init__ (self, stream, mode, bomCharset, header)
		if relaxed:
			extrasaction = 'ignore'
		else:
			extrasaction = 'raise'
		self.writer = csv.DictWriter (self.fd, fieldList, dialect = dialect, extrasaction = extrasaction)

class _CSVReader (CSVIO):
	def __init__ (self, stream):
		CSVIO.__init__ (self)
		self.reader = None
		self.open (stream, 'rU')

	def __iter__ (self):
		return iter (self.reader)

	def read (self):
		return next (self.reader, None)
	
	def stream (self):
		return Stream.defer (self.reader, lambda o: self.close ())

class CSVReader (_CSVReader):
	"""Wrapper to read a CSV file"""
	def __init__ (self, stream, dialect):
		"""for meaning of ``stream'' and ``bomCharset''see
CSVIO.open (), for availble ``dialect'' values see CSVIO.__doc__"""
		_CSVReader.__init__ (self, stream)
		self.reader = csv.reader (self.fd, dialect = dialect)

class CSVDictReader (_CSVReader):
	"""Wrapper to read a CSV file as a dict"""
	def __init__ (self, stream, fieldList, dialect, restKey = None, restValue = None):
		"""for meaning of ``stream'' and ``bomCharset''see
CSVIO.open (), for availble ``dialect'' values see CSVIO.__doc__,
``fieldList'', ``restKey'' and ``restValue'' are passed to
csv.DictReader to fill the dictionary."""
		_CSVReader.__init__ (self, stream)
		self.reader = csv.DictReader (self.fd, fieldList, dialect = dialect, restkey = restKey, restval = restValue)

class CSVAutoDictReader (_CSVReader):
	"""Wrapper to a read a CSV file as dict, determinating the field list form the first header line of the CSV"""
	def __init__ (self, stream, dialect):
		"""for meaning of ``stream'' and ``bomCharset''see
CSVIO.open (), for availble ``dialect'' values see CSVIO.__doc__"""
		super (CSVAutoDictReader, self).__init__ (stream)
		header = next (csv.reader (self.fd, dialect = dialect))
		self.reader = csv.DictReader (self.fd, header, dialect = dialect)

class CSVAuto (object):
	"""CSV reading class which tries to guess the dialect of the input file"""
	validHeader = re.compile ('[0-9a-z_][0-9a-z_-]*', re.IGNORECASE)
	Guess = namedmutable (
		'Guess', 
		[
			('delimiter', None),
			('doublequote', False),
			('escapechar', None),
			('lineterminator', '\n'),
			('quotechar', None),
			('quoting', csv.QUOTE_NONE),
			('skipinitialspace', False)
		]
	)
	def __init__ (self, fname, dialect = 'agn-auto', linecount = 10):
		self.fname = fname
		self.dialect = dialect
		self.linecount = linecount
		self.header = None
		self.headerLine = None

	def __enter__ (self):
		self.setup ()
		return self

	def __exit__ (self, exc_type, exc_value, traceback):
		self.done ()
		return False

	def __analyse (self):
		err = None
		fd = open (self.fname, 'r')
		head = fd.readline ()
		datas = []
		if head == '':
			err = 'Empty input file "%s"' % self.fname
		else:
			n = 0
			while n < self.linecount:
				data = fd.readline ()
				if data == '':
					break
				datas.append (data)
		fd.close ()
		if err:
			raise error (err)
		temp = self.Guess ()
		if len (head) > 1 and head[-2] == '\r':
			temp.lineterminator = '\r\n'
			head = head[:-2]
		else:
			head = head[:-1]
		if head[0] == '"' or head[0] == '\'':
			temp.quotechar = head[0]
			temp.quoting = csv.QUOTE_ALL
			n = 0
			while True:
				n = head.find (temp.quotechar, n + 1)
				if n != -1 and n + 1 < len (head):
					n += 1
					if head[n] == temp.quotechar:
						temp.doublequote = True
					else:
						temp.delimiter = head[n]
						if n + 1 < len (head) and (head[n + 1] == ' ' or head[n + 1] == '\t'):
							temp.skipinitialspace = True
						break
				else:
					break
		if temp.delimiter is None:
			use = [None, 0]
			most = [None, 0]
			for d in ',;|\t!@#$%^&*?':
				cnt1 = head.count (d)
				cnt2 = cnt1
				for data in datas:
					c = data.count (d)
					if c < cnt2:
						cnt2 = c
				if cnt1 > use[1] and cnt1 == cnt2:
					use = [d, cnt1]
				if cnt1 > most[1]:
					most = [d, cnt1]
			if use[0] is None:
				if most[0] is None:
					raise error ('No delimiter found')
				use = most
			temp.delimiter = use[0]
		if not temp.doublequote:
			for data in datas:
				if '""' in data or '\'\'' in data:
					temp.doublequote = True
					break
		for data in datas:
			if '\\' in data:
				temp.escapechar = '\\'
				break
		return temp

	def setup (self):
		"""Starts the auto detection"""
		a = self.__analyse ()
		_CSVAuto.delimiter = a.delimiter
		_CSVAuto.doublequote = a.doublequote
		_CSVAuto.escapechar = a.escapechar
		_CSVAuto.lineterminator = a.lineterminator
		_CSVAuto.quotechar = a.quotechar
		_CSVAuto.quoting = a.quoting
		_CSVAuto.skipinitialspace = a.skipinitialspace
		csv.register_dialect (self.dialect, _CSVAuto)
		rd = CSVReader (self.fname, dialect = self.dialect)
		head = rd.read ()
		rd.close ()
		self.headerLine = True
		for h in head:
			if self.validHeader.match (h) is None:
				self.headerLine = False
				break
		if self.headerLine:
			self.header = head
		else:
			self.header = None

	def done (self):
		"""Ends/cleanup the process"""
		csv.unregister_dialect (self.dialect)
		self.header = None
		self.headerLine = None

	def reader (self):
		"""Provides a reader a file in the guessed dialect"""
		if self.headerLine is None:
			self.setup ()
			temp = True
		else:
			temp = False
		try:
			rd = CSVReader (self.fname, dialect = self.dialect)
			if self.headerLine:
				rd.read ()
		finally:
			if temp:
				self.done ()
		return rd

	def writer (self, fname = None, force = False):
		"""Provides a writer based on the dialect of the original input file

``fname'' is the file to be written to (if None, the input file name
is used) and ``force'' must be set to True to overwrite an existing
file."""
		if fname is None or fname == self.fname:
			if fname is None:
				fname = self.fname
			if not force:
				raise error ('Will not overwrite source file w/o being forced to')
		if self.headerLine is None:
			self.setup ()
			temp = True
		else:
			temp = False
		try:
			wr = CSVWriter (fname, dialect = self.dialect, header = self.header if self.headerLine else None)
		finally:
			if temp:
				self.done ()
		return wr

#}}}
#
# 15.) Caching
#
#{{{
class Cache:
	"""Generic caching

this class provides a generic caching implementation with limitiation
of stored entries (LRU cache) and optional time based expiration of
entries."""
	toPattern = re.compile ('([0-9]+)([A-za-z])')
	toUnits = {
		's':	1,
		'm':	60,
		'h':	60 * 60,
		'd':	24 * 60 * 60,
		'w':	7 * 24 * 60 * 60
	}
	def __parse (self, to):
		if type (to) in (str, unicode):
			rc = 0
			for amount, unit in [(int (_c[0]), _c[1]) for _c in self.toPattern.findall (to)]:
				with Ignore (KeyError):
					rc = amount * self.toUnits[unit]
		else:
			rc = to
		return rc

	class Entry (object):
		"""Represents a single caching entry"""
		def __init__ (self, value, active):
			if active:
				self.created = time.time ()
			self.value = value

		def valid (self, now, timeout):
			"""if the entry is still valid"""
			return self.created + timeout >= now

	def __init__ (self, limit = 0, timeout = None):
		"""``limit'' is the maximum number of elements of the
cache (use 0 for no limits) and ``timeout'' is the timeout for entries
to be valid. ``timeout'' can either be specified as int in seconds or
as a str using modfiers "s" for seconds, "m" for minutes, "h" for
hours, "d" for days and "w" for weeks, e.g.:
	30m: means 30 minutes 
	2h30m: means 2 hours and 30 minutes
	2h 30m: dito, spaces are ignored and can be inserted for better readability"""
		self.limit = limit
		self.timeout = self.__parse (timeout)
		self.active = self.timeout is not None
		self.count = 0
		self.cache = {}
		self.cacheline = collections.deque ()

	def __getitem__ (self, key):
		e = self.cache[key]
		if self.active and not e.valid (time.time (), self.timeout):
			self.remove (key)
			raise KeyError ('%r: expired' % (key, ))
		self.cacheline.remove (key)
		self.cacheline.append (key)
		return e.value

	def __setitem__ (self, key, value):
		if key in self.cache:
			self.cacheline.remove (key)
		else:
			if self.limit and self.count >= self.limit:
				drop = self.cacheline.popleft ()
				del self.cache[drop]
			else:
				self.count += 1
		self.cache[key] = self.Entry (value, self.active)
		self.cacheline.append (key)

	def __delitem__ (self, key):
		del self.cache[key]
		self.cacheline.remove (key)
		self.count -= 1

	def __len__ (self):
		return len (self.cache)

	def __contains__ (self, key):
		return key in self.cache

	def reset (self):
		"""clears the cache"""
		self.count = 0
		self.cache = {}
		self.cacheline = collections.deque ()

	def remove (self, key):
		"""remove ``key'' from cache, if ``key'' is in cache"""
		if key in self.cache:
			del self[key]

	def expire (self, blocksize = 1000):
		"""expires outdated cache entries, work in ``blocksize'' chunks"""
		if self.active:
			now = time.time ()
			while True:
				toRemove = []
				for key, e in self.cache.items ():
					if not e.valid (now, self.timeout):
						toRemove.append (key)
						if blocksize and len (toRemove) == blocksize:
							break
				if not toRemove:
					break
				for key in toRemove:
					del self[key]
		if self.limit:
			while self.count > self.limit:
				del self[self.cacheline[0]]
#}}}
#
# 16.) Family
#
#{{{
class Family (object):
	"""Control multiple child processes

this class provides a framework to start and control multiple child
processes, optional limit the started of child processes to a fixed
number.

To use this class one must subclass Family.Child and add an
implementation of the method ``start'' as the entry point for the
child process. The child process itself can check if it should
continue processing by calling its method ``active()''.

For each child process this class must be instanced and pased to the
class using Family.add(). After adding in general Family.start() is
called. This forks new processes for each child.

Using Family.wait() will wait (optional with a timeout) for
termination of the child processes. If the number of subprocesses is
limited, then this method will also start remaining children, when one
active child exits."""
	class Child (object):
		"""Meta class for child process"""
		NEW = 0
		SCHEDULE = 1
		RUNNING = 2
		DEAD = 3
		_incarnation = 0
		def __init__ (self, name = None):
			"""``name'' is an optional identifier for this child"""
			self._name = name
			if self._name is None:
				self.__class__._incarnation += 1
				self._name = 'Child-%d' % self.__class__._incarnation
			self._pid = -1
			self._status = -1
			self._state = self.NEW
			self._running = True

		def __catch (self, sig, stack):
			self._running = False

		def setupHandler (self):
			"""setup the signal handler for process"""
			signal.signal (signal.SIGTERM, self.__catch)
			signal.signal (signal.SIGINT, signal.SIG_IGN)
			signal.signal (signal.SIGHUP, signal.SIG_IGN)
			signal.signal (signal.SIGPIPE, signal.SIG_IGN)
			signal.signal (signal.SIGCHLD, signal.SIG_DFL)

		def exited (self):
			"""returns the exit status, if child exited, else -1"""
			if os.WIFEXITED (self._status):
				return os.WEXITSTATUS (self._status)
			return -1

		def terminated (self):
			"""returns the termination signal, if child was killed, else -1"""
			if os.WIFSIGNALED (self._status):
				return os.WTERMSIG (self._status)
			return -1

		def reset (self):
			"""reset the state of the child"""
			self._pid = -1
			self._status = -1
			self._state = self.NEW
			self._running = True

		def active (self):
			"""checks if the child is started (for the parent) or if it is still active (for the child itself)"""
			if self._pid > 0:
				return True
			elif self._pid == 0:
				return self._running
			return False

		def start (self):
			"""Entry point for the subclass for the child process"""
			raise error ('Subclass responsible for implementing "start"')

	def __init__ (self, limit = None):
		"""if ``limit'' larger than 0, this limits the number
of running child processes at the same time."""
		signal.signal (signal.SIGCHLD, self.__catch)
		self.limit = limit
		self.children = []
		self.running = 0

	def __catch (self, sig, stack):
		pass

	def __iter__ (self):
		return iter (self.children)

	def reset (self):
		"""Resets the monitored children, running children stay active!"""
		if self.children:
			self.debug ('Reset: %s' % ', '.join ([_c._name for _c in self.children]))
		else:
			self.debug ('Reset with no children')
		self.children = []

	def add (self, child):
		"""Add a new child or reschedule a terminated child"""
		if child in self.children:
			if not child.active ():
				self.debug ('Reactived %s' % child._name)
				child.reset ()
			else:
				self.debug ('Add already active %s' % child._name)
		else:
			self.debug ('Add %s' % child._name)
			child.reset ()
			self.children.append (child)

	def __spawn (self, child):
		child._state = child.RUNNING
		self.debug ('Starting %s' % child._name)
		child._status = 0
		child._pid = os.fork ()
		if child._pid == 0:
			child.setupHandler ()
			try:
				ec = child.start ()
				self.debug ('%s: Exiting with %r' % (child._name, ec))
				if type (ec) is bool:
					ec = 0 if ec else 1
				elif type (ec) not in (int, long):
					ec = 0
			except Exception as e:
				self.debug ('%s: Exiting due to exception: %s' % (child._name, str (e)))
				logexc (LV_ERROR, child._name)
				ec = 127
			os._exit (ec)
		self.running += 1

	def __join (self, child, status = -1):
		child._pid = -1
		child._status = status
		child._state = child.DEAD
		self.running -= 1

	def start (self):
		"""Start ready to run children until reaching the limit, if set"""
		for child in self.children:
			if child._state == child.NEW:
				child._state = child.SCHEDULE
				self.debug ('%s: scheduled' % child._name)
			if child._state == child.SCHEDULE and (self.limit is None or self.running < self.limit):
				self.__spawn (child)

	def __kill (self, sig):
		for child in self.children:
			if child._state == child.RUNNING:
				try:
					os.kill (child._pid, sig)
					self.debug ('%s: signalled with %d' % (child._name, sig))
				except OSError as e:
					self.debug ('%s: failed signalling with %d: %s' % (child._name, sig, str (e)))

	def stop (self):
		"""Send SIGTERM to all active children"""
		self.__kill (signal.SIGTERM)

	def kill (self):
		"""Send SIGKILL to all active children"""
		self.__kill (signal.SIGKILL)

	def wait (self, timeout = None):
		"""Wait for termination of children, returning the
number of remaining active children. ``timeout'' is a timeout in
seconds to wait for children to terminate."""
		active = [_c for _c in self.children if _c._state in (_c.SCHEDULE, _c.RUNNING)]
		schedule = [_c for _c in self.children if _c._state == _c.SCHEDULE]
		if timeout > 0:
			start = time.time ()
		while active:
			while schedule and (self.limit is None or self.running < self.limit):
				self.__spawn (schedule.pop (0))
			if timeout >= 0:
				flag = os.WNOHANG
			else:
				flag = 0
			try:
				(pid, status) = os.waitpid (-1, flag)
				if pid > 0:
					for child in active:
						if child._pid == pid:
							self.__join (child, status)
							active.remove (child)
							self.debug ('%s: exited with %d' % (child._name, status))
							break
			except OSError as e:
				self.debug ('Wait aborted with %s' % str (e))
				if e.args[0] == errno.ECHILD:
					for child in active:
						if child._state == child.RUNNING:
							self.__join (child, 126)
							active.remove (child)
			if timeout == 0:
				break
			elif timeout > 0 and active:
				if start + timeout < time.time ():
					break
				time.sleep (1)
		return len (active)

	def active (self):
		"""return the number of active children"""
		return self.wait (0)

	def cancel (self):
		"""prevend scheduled children from being started"""
		for child in self.children:
			if child._state == child.SCHEDULE:
				self.__join (child, 125)

	def abort (self):
		"""cancel not started children and stop running children"""
		self.cancel ()
		self.stop ()
		if self.wait (2):
			self.kill ()
			self.wait (0)

	def clean (self):
		"""clean monitored list from inactive children"""
		cleaned = []
		for child in [_c for _c in self.children if _c._pid == -1]:
			cleaned.append (child)
			self.children.remove (child)
			self.debug ('Removed %s' % child._name)
		return cleaned

	def debug (self, m):
		"""Can be overwritten to log the activty of Family"""
		pass

class Processqueue (object):
	"""Simple processor to start subtasks in parallel with a
limited number of subprocess or optional without forking any
subprocess by sequential execution of each task (mainly designed to be
used for debugging to avoid fiddling with subprocesses).

Instanciate the class with the number of maximal in parallel running
processes or without any argument (or None as argument) to use the
non-forking sequential way."""
	Process = collections.namedtuple ('Process', ['name', 'method', 'queue'])
	def __init__ (self, parallel = None):
		self.parallel = parallel
		self.processes = []
	
	def is_parallel (self):
		return self.parallel and self.parallel > 0
		
	def add (self, name, method):
		"""Ad a task with ``name'' and ``method'' to invoke."""
		self.processes.append (self.Process (name, method, multiprocessing.JoinableQueue (1) if self.is_parallel () else None))
	
	def execute (self):
		"""Execute the processqueue and return a dict with a
key as the name for each task and a boolean flag, if the task has
ended successful or not. No further information about the reason is
stored,"""
		rc = Stream.of (self.processes).map (lambda p: (p.name, False)).dict ()
		if self.parallel and self.parallel > 0:
			class Child (Family.Child):
				def __init__ (self, process):
					super (Child, self).__init__ (name = str (process.name))
					self.process = process
				
				def start (self):
					try:
						rc = self.process.method ()
						return True
					except Exception as e:
						logexc (LV_ERROR, self.process.name)
						rc = e
						return False
					finally:
						self.process.queue.put (rc)
						self.process.queue.task_done ()

			family = Family (limit = self.parallel)
			(Stream.of (self.processes)
				.each (lambda p: family.add (Child (p)))
			)
			family.start ()
			while family.wait ():
				pass
			for child in family:
				try:
					rc[child.process.name] = child.process.queue.get (block = False)
				except Queue.Empty:
					rc[child.process.name] = (child.exited () == 0)
				child.process.queue.join ()
		else:
			for process in self.processes:
				try:
					ec = process.method ()
				except Exception as e:
					logexc (LV_ERROR, process.name)
					ec = e
				rc[process.name] = ec
		return rc
#}}}
#
# 17.) Profile
#
#{{{
class Profile (object):
	"""Application based profiling

to profile an application on definied checkpoints a general profiling
tool may not be able to solve this problem. This class provides a
simple interface to surrount blocks of code with checkpoints and print
a summary at the end. This is for developing enviroments only.

As a shortcut you can use the instance as a callable:
	p = Profile ()
	p (name)		# == p.start (name)
	p ()			# == p.stop ()
"""
	Block = namedmutable ('Block', ['name', 'spent', 'count', 'start'])
	def __init__ (self, showOnExit = True):
		"""``showOnExit'' is True, then the result is displayed when the destructor is called"""
		self.showOnExit = showOnExit
		self.collect = {}
		self.cur = None

	def stop (self):
		"""stop meassurement for the current block"""
		ts = time.time ()
		if self.cur:
			self.cur.spent += ts - self.cur.start
			self.cur.count += 1
			self.cur = None

	def start (self, name):
		"""start a new meassurement block"""
		self.stop ()
		try:
			self.cur = self.collect[name]
		except KeyError:
			self.cur = self.Block (name = name, spent = 0, count = 0, start = None)
			self.collect[name] = self.cur
		self.cur.start = time.time ()

	def show (self, stream = sys.stderr):
		"""shows the result, sending output to ``stream''"""
		out = []
		for entry in self.collect.values ():
			if entry.count > 0:
				entry.call = entry.spent / entry.count
				out.append (entry)
		stream.write ('Profiling output\n')
		for state in 0, 1, 2:
			if state == 0:
				out.sort (key = lambda a: a.spent, reverse = True)
				stream.write ('Output sorted for most overall time spent\n')
			elif state == 1:
				out.sort (key = lambda a: a.call, reverse = True)
				stream.write ('Output sorted for most time spent per call\n')
			elif state == 2:
				out.sort (key = lambda a: a.count, reverse = True)
				stream.write ('Output sorted for most often called checkpoints\n')
			stream.write ('Checkpoint               Calls     Spent     Spent/call\n')
			for entry in out:
				stream.write ('%-20.20s  %8s  %8.3f       %8.3f\n' % (entry.name, numfmt (entry.count), entry.spent, entry.call))
			stream.write ('\n')

	def __del__ (self):
		self.stop ()
		if self.showOnExit:
			self.show ()

	def __call__ (self, name = None):
		if name is None:
			self.stop ()
		else:
			self.start (name)
#}}}
