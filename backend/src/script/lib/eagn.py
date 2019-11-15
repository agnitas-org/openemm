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
"""Extended library for AGNITAS runtime

This extends the standard library found in agn.py and provides some
higher level functionallity.
"""
#
import	sys, os, string, time, re, subprocess, tempfile, socket, collections, Queue
import	stat, errno, pwd, grp, fcntl, signal, select, mmap
import	datetime, base64, shutil, ftplib, urlparse, mimetypes, hashlib
import	platform
try:
	import cPickle as pickle
except ImportError:
	import pickle
try:
	import cStringIO as StringIO
except ImportError:
	import StringIO
import	agn, xagn
try:
	bytes
except NameError:
	bytes = str
#
# Versioning {{{
_changelog = [

	('1.0.0', '2019-01-01', 'Extended library', 'openemm@agnitas.de')
]
__version__ = _changelog[-1][0]
__date__ = _changelog[-1][1]
__author__ = ', '.join (sorted (list (set ([_c[3] for _c in _changelog]))))
def require (checkversion):
	agn.__require (checkversion, __version__, 'eagn')
#}}}
#
# Support {{{
class IDs (object):
	"""support class to handle UID/GID

This is a general class to lookup user and group and find related IDs
on the local server. The getUser() and getGroup() method accepts
either a string or a numeric value and tries to interpret these as
name or id. If ``relaxed'' is True then, in case the name is of type
str, it will try to interpret this as the id as well, if the name
cannot be found."""
	def __get (self, what, lookupName, lookupId, lookupDefault, relaxed):
		rc = None
		if what is None:
			with agn.Ignore (KeyError):
				rc = lookupId (lookupDefault ())
		elif type (what) in (str, unicode):
			try:
				rc = lookupName (what)
			except KeyError:
				if relaxed:
					with agn.Ignore (KeyError, ValueError, TypeError):
						rc = lookupId (int (what))
		elif type (what) in (int, long, float):
			with agn.Ignore (KeyError):
				rc = lookupId (int (what))
		return rc
	
	def getUser (self, user = None, relaxed = True):
		"""Tries to find ``user'' and returns pwd.struct_passwd"""
		return self.__get (user, pwd.getpwnam, pwd.getpwuid, os.getuid, relaxed)
	
	def getGroup (self, group = None, relaxed = True):
		"""Tries to find ``group'' and returns grp.struct_group"""
		return self.__get (group, grp.getgrnam, grp.getgrgid, os.getgid, relaxed)
#}}}
#
# System interaction {{{
class Filesystem:
	"""Filesystem related queries

With this class one can retrieve information about free and used space
and inodes on a filesystem for a specific path."""
	def __init__ (self, path, device = None):
		"""``path'' is a path in the filesystem to examine, ``device'' is currently unused"""
		self.path = path
		self.device = device
		self.stat = os.statvfs (path)
	
	def percentBlockFree (self):
		"""returns the percentage of free blocks"""
		return int ((self.stat.f_bavail * 100) / self.stat.f_blocks)
	
	def percentBlockUsed (self):
		"""returns the percentage of used blocks"""
		return 100 - self.percentBlockFree ()
		
	def percentInodeFree (self):
		"""returns the percentage of free inodes"""
		return int ((self.stat.f_favail * 100) / self.stat.f_files)
		
	def percentInodeUsed (self):
		"""returns the percentage of used inodes"""
		return 100 - self.percentInodeFree ()
		
	def usage (self):
		"""returns a four element tuple containing the percentage of free and used blocks and free and used inodes"""
		return (self.percentBlockFree (), self.percentBlockUsed (), self.percentInodeFree (), self.percentInodeUsed ())

class Process:
	"""Information about one process

when retrieving the process table using class Processtable this
represents a single process"""
	def __init__ (self):
		self.pid = -1
		self.parent = None
		self.sibling = None
		self.child = None

	def relate (self, other):
		"""relate (self, other)
if other is parent (or grandparent), this returns a number
greater than 0, counting the generations, if other is a
child (or even younger), than a number less than 0 is
returned. If the two processes are not related to each other
(they are not related, even if they are siblings), 0 is returned."""
		for (cur, target, modifier) in ((self, other, 1), (other, self, -1)):
			n = 0
			while cur and cur != target:
				cur = cur.parent
				n += modifier
			if cur:
				return n
		return 0
	
class Processtable:
	"""Reads processtable for further processing

This class reads the current running processes and stores them for
further retrivial. This can be used to read the process table once and
perform serveral queries on the result."""
	def __timeparse (self, s):
		part = s.split ('-')
		if len (part) == 2:
			sec = int (part[0]) * 60 * 60 * 24
			sstr = part[1]
		else:
			sec = 0
			sstr = part[0]
		part = sstr.split (':')
		nsec = 0
		for p in part:
			nsec *= 60
			nsec += int (p)
		return sec + nsec

	def __init__ (self):
		self.table = None
		environ = {'COLUMNS': '4096'}
		pp = subprocess.Popen (['ps', '-e', '-o', 'pid,ppid,user,group,etime,time,tty,vsz,comm,args'], stdout = subprocess.PIPE, env = environ)
		(out, err) = pp.communicate (None)
		if pp.returncode == 0:
			self.table = []
			for line in [_o.strip () for _o in out.split ('\n')]:
				elem = line.split ()
				if len (elem) > 7:
					try:
						p = Process ()
						p.pid = int (elem[0])
						p.ppid = int (elem[1])
						p.user = elem[2]
						p.group = elem[3]
						p.etime = self.__timeparse (elem[4])
						p.time = self.__timeparse (elem[5])
						p.tty = elem[6]
						p.size = int (elem[7]) * 1024
						p.comm = elem[8]
						p.cmd = ' '.join (elem[9:])
						self.table.append (p)
					except:
						pass
			self.table.sort (key = lambda a: a.pid)
			self.root = None
			for p in self.table:
				if p.ppid == 0:
					if p.pid == 1:
						self.root = p
				else:
					for pp in self.table:
						if p.ppid == pp.pid:
							p.parent = pp
							if pp.child:
								sib = pp.child
								while sib.sibling:
									sib = sib.sibling
								sib.sibling = p
							else:
								pp.child = p
		else:
			raise agn.error ('Unable to read process table: %r' % err)
	
	def find (self, val):
		"""Returns a process with PID ``val'' (if numeric) or tries to read the PID from the file ``val'' (if an absolute path)"""
		pid = None
		if type (val) in (int, long):
			pid = val
		elif type (val) in (str, unicode):
			if len (val) > 0 and val[0] == '/':
				with agn.Ignore (), open (val, 'r') as fd:
					line = fd.readline ().strip ()
					if line != '':
						pid = int (line)
		if pid == None:
			with agn.Ignore (ValueError):
				pid = int (val)
		if pid == None:
			raise agn.error ('Given paramter "%r" cannot be mapped to a pid' % (val, ))
		match = None
		for p in self.table:
			if p.pid == pid:
				match = p
				break
		if not match:
			raise agn.error ('No process with pid %d (extracted from %r) found' % (pid, val))
		return match
	
	def select (self, user = None, group = None, comm = None, rcmd = None, ropt = 0):
		"""find processes according to the passed parameter

these keyword parameter are used. If they are None, then these are not
used to filter the process table, i.e. with no keyword parameter this
method returns the whole process table. These keyword parameter are
supported:
	- user: name of user the process is owned by
	- group: name of group the process is owned by
	- comm: the command of the process
	- rcmd: a regex to check against the commands name
	- ropt: flags for regex, if ``rcmd'' is used
"""
		if rcmd:
			regcmd = re.compile (rcmd, ropt)
		else:
			regcmd = None
		rc = []
		for t in self.table:
			if user and t.user != user:
				continue
			if group and t.group != group:
				continue
			if comm and t.comm != comm:
				continue
			if regcmd and not regcmd.search (t.cmd):
				continue
			rc.append (t)
		return rc
	
	def tree (self, indent = 2):
		"""Builds a visualisation tree of the processtables, indents child processes by ``indent'' spaces"""
		rc = ''
		up = []
		cur = self.root
		while cur:
			lu = len (up) * indent
			rc += '%*.*s%s\n' % (lu, lu, '', cur.cmd)
			if cur.child:
				up.append (cur.sibling)
				cur = cur.child
			else:
				cur = cur.sibling
			if not cur:
				while len (up) > 0:
					cur = up.pop ()
					if cur:
						break
		return rc

class Processtitle (object):
	"""Modify process name in process table"""
	__slots__ = ['_set', '_get', '_original', '_current', '_template', '_ns', '_stack']
	defaultTemplate = '$original [$title]'
	def __init__ (self, template = None):
		try:
			import	setproctitle
			
			self._set = setproctitle.setproctitle
			self._get = setproctitle.getproctitle
		except ImportError:
			self._set = lambda t: None
			self._get = lambda: None
		self._original = self._get ()
		self._current = self._original
		self._template = None
		self._ns = {
			'original': self._original
		}
		self._stack = []
		#
		self.template = template

	def __del__ (self):
		self ()
		
	def __call__ (self, title = None):
		if title is None:
			new = self._original
		else:
			self._ns['title'] = title
			new = self._template.fill (self._ns)
		if new != self._current:
			self._current = new
			self._set (self._current)
	
	def __getitem__ (self, key):
		return self._ns[key]
	def __setitem__ (self, key, value):
		self._ns[key] = value

	def _getTemplate (self):
		return self._template.content
	def _setTemplate (self, value):
		self._template = agn.Template (value if value is not None else self.defaultTemplate)
	def _delTemplate (self):
		self.template = None
	template = property (_getTemplate, _setTemplate, _delTemplate)

	def push (self, title):
		self._stack.append (self._current)
		self (title)
	
	def pop (self):
		if self._stack:
			new = self._stack.pop (0)
			if new != self._current:
				self._current = new
				self._set (self._current)

class LinuxMem(object):
	"""non cross platfrom way to get free memory on linux"""
	def __init__(self, unit='%'):

		with open('/proc/meminfo', 'r') as mem:
			lines = mem.readlines()

		self._tot = int(lines[0].split()[1])
		self._free = int(lines[1].split()[1])
		self._buff = int(lines[2].split()[1])
		self._cached = int(lines[3].split()[1])
		self._shared = int(lines[20].split()[1])
		self._swapt = int(lines[14].split()[1])
		self._swapf = int(lines[15].split()[1])
		self._swapu = self._swapt - self._swapf
		self.unit = unit
		self._convert = self._percent()

	def _percent(self):
		if self.unit == '%':
			return 1.0/self._tot*100

	@property
	def total(self):
		"""total amount of memory available"""
		return self._convert * self._tot

	@property
	def used(self):
		"""used amount of memory"""
		return self._convert * (self._tot - self._free)

	@property
	def used_real(self):
		"""memory used which is not cache or buffers"""
		return self._convert * (self._tot - self._free - self._buff - self._cached)

	@property
	def user_free(self):
		"""This is the free memory available for the user"""
		return self._convert *(self._free + self._buff + self._cached)

class Crontab (IDs):
	"""Handle crontabs

This helps handling of crontab entries. It tries to determinate
existing entries and tries to update these, add new entries and remove
obsolete entries."""
	header = re.compile ('# (DO NOT EDIT THIS FILE - edit the master and reinstall|\\([^ ]+ installed on|\\(Cron version)')
	def __init__ (self):
		IDs.__init__ (self)
		self.superuser = os.getuid () == 0
	
	def update (self, crontab, user = None, doit = True, runas = None, remove = None):
		"""Update s a crontab entry
		
``crontab'' is a list of crontab lines using proper syntax for
crontabs, i.e. the first five elements are used to determinate the
starting time and the remaining part ist the command to start.
``user'' is the user to update the crontab for, if None then the
current user is used. If ``doit'' is True, then the crontab is
modified, otherwise it is only shown what would have been changed.
``runas'', if not None, is the user who should invoke this method. If
the user mismatches the current user, an exception is raised.
``remove'' is an entry or a list of entries to remove from the
crontab. Unknown entries are normally kept, as they may be added from
outside this process."""
		if runas is not None:
			pw = self.getUser (runas)
			if pw is None:
				raise agn.error ('Should run as user "%s" but this user is not known on this system' % runas)
			uid = os.getuid ()
			if pw.pw_uid != uid:
				cur = self.getUser (uid)
				if cur is None:
					current = '%d' % uid
				else:
					current = '%s (%d)' % (cur.pw_name, cur.pw_uid)
				raise agn.error ('Should run as user "%s" but run as %s' % (runas, current))
		if not user is None and not self.superuser:
			raise agn.error ('Need super user rights to modify user crontab')
		if remove is not None and type (remove) not in (list, tuple):
			remove = [remove]
		command = ['/usr/bin/crontab', '-l']
		if not user is None:
			command.append ('-u%s' % user)
		pp = subprocess.Popen (command, stdin = subprocess.PIPE, stdout = subprocess.PIPE, stderr = subprocess.PIPE)
		(out, err) = pp.communicate ()
		if (pp.returncode != 0 or err) and not err.startswith ('no crontab'):
			raise agn.error ('Failed to read crontab with %d: %s' % (pp.returncode, err))
		lineno = 0
		original = []
		for line in [_o.strip () for _o in out.strip ().split ('\n')]:
			lineno += 1
			if lineno <= 3 and line.startswith ('#') and not self.header.match (line) is None:
				continue
			original.append ([False, line])
		additional = []
		modified = False
		for ctab in crontab:
			if type (ctab) in (tuple, list):
				(pattern, current) = ctab
			else:
				parts = ctab.split (None, 5)
				if len (parts) == 6:
					pattern = parts[-1].split ()[0]
				current = ctab
			missing = True
			for line in original:
				if current == line[1]:
					missing = False
				elif not line[0] and ((pattern and pattern in line[1]) or (remove and filter (lambda r: r in line[1], remove))):
					line[0] = True
					modified = True
			if missing:
				additional.append (current)
				modified = True
		newtab = [_o[1] for _o in original if not _o[0]] + additional
		if doit:
			if modified:
				command = ['/usr/bin/crontab']
				if not user is None:
					command.append ('-u%s' % user)
				fd = tempfile.NamedTemporaryFile ()
				fd.write ('\n'.join (newtab) + '\n')
				fd.flush ()
				command.append (fd.name)
				pp = subprocess.Popen (command, stdout = subprocess.PIPE, stderr = subprocess.PIPE)
				(out, err) = pp.communicate ()
				fd.close ()
				if pp.returncode or err:
					raise agn.error ('Failed to write crontab with %d: %s' % (pp.returncode, err))
		else:
			if modified:
				print ('*** Crontab would be changed ***')
			else:
				print ('*** Crontab is unchanged ***')
			print ('\n'.join (newtab))

class Sanity (IDs):
	"""Framewaork for sanity checks

This is the base to perform sanity checks. This class should be
subclassed and extended to perform custom checks. If the included
checks are sufficient, this class can be used directly."""
	def preproc (self, r, area, *args):
		pass
	def postproc (self, r, area, *args):
		pass
	def __init__ (self, directories = None, files = None, symlinks = None, executables = None, runas = None, startas = None, umask = None, modules = None, checks = None):
		"""setup the check
		
``directories'' is a list of expected directories, if a directory is
missing, it is created.

``files'' is a list of dict which these elements, where only ``name''
is required:
	- name (str): the path to the file
	- content (str): the content the file must have, if not existing
	- optional (bool): if True, then the file may not exists, otherwise a missing file is considered as an error
	- gid (str/int): the owning group of the file
	- uid (str/int): the owning user of the file
	- isdir (bool): if True, the file must be a directory
	- isfile (bool): if True, the file must be a regular file
	- issuid (bool): if True, the file must have the setuid bit set
	- issgid (bool): if True, the file must have the setgid bit set
	- islink (bool): if True, the file must be a symbolic link
	- mode (int): the file access mode must match this mode
	- mask (int): the file access mode must at least satisfy this mode

``symlinks'' is a list of tuple with two elements, the path where the
symlink should be exists and the target where the symlink should point
to. A missing or pointing to a wrong target existing symlink will be
(re)created to point to the desired target.

``executables'' is a list of program names which must be found in any
directory of $PATH and must be executable.

``runas'' is the user who should perform this sanity check. If the
user mismatches it is tried to change the user to this user.

``startas'' is the user who must invoke this check.

``umask'' is the creation mask used by the process for creating
missing filesystem enries.

``modules'' is a list of expected installed python modules.

``checks'' is a list of custom methods to be invoked for further
checks. These checks are called with one parameter ``r'' which is an
agn.mutable (error = [], warning = []) and should be filled by the
check according to its results. While both are printed, only
``errors'' leading to an exit code not equal 0.
"""
		IDs.__init__ (self)
		r = agn.mutable (error = [], warning = [])
		if startas:
			self.preproc (r, 'startas', startas)
			pw = self.getUser ()
			self.postproc (r, 'startas', startas, pw)
			if pw is None or pw.pw_name != startas:
				if pw is None:
					myself = '#%d' % os.getuid ()
				else:
					myself = pw.pw_name
				raise agn.error ('startas: start as user "%s", not "%s"' % (startas, myself))
		if runas:
			self.preproc (r, 'runas', runas)
			pw = self.getUser (runas)
			self.postproc (r, 'runas', runas, pw)
			if pw is None:
				raise agn.error ('runas: failed to find user for %r' % runas)
			try:
				if os.getgid () != pw.pw_gid:
					os.setgid (pw.pw_gid)
				if os.getuid () != pw.pw_uid:
					os.setuid (pw.pw_uid)
				os.environ['HOME'] = pw.pw_dir
				os.environ['USER'] = pw.pw_name
				os.chdir (pw.pw_dir)
			except OSError, e:
				raise agn.error ('runas: failed to setup user %r: %r' % (runas, e.args))
		if not umask is None:
			try:
				self.preproc (r, 'umask', umask)
				omask = os.umask (umask)
				self.postproc (r, 'umask', umask, omask)
			except OSError,e :
				raise agn.error ('umask: failed to set umask to %03o: %r' % (umask, e.args))
		if directories:
			for d in directories:
				path = agn.mkpath (agn.base, d)
				try:
					self.preproc (r, 'directory', path, d)
					agn.createPath (path)
					self.postproc (r, 'directory', path, d)
				except agn.error as e:
					r.error.append ('Directory: Failed to access/create %s: %s' % (path, e))
		if files:
			for f in [_f for _f in files if _f.has_key ('name')]:
				name = f['name']
				if not name.startswith (os.path.sep):
					name = agn.mkpath (agn.base, name)
				self.preproc (r, 'file', name, f)
				if f.has_key ('content') and not os.path.isfile (name):
					try:
						fd = open (name, 'w')
						fd.write (f['content'])
						fd.close ()
						if f.has_key ('mode'):
							os.chmod (name, f['mode'])
					except IOError, e:
						r.error.append ('File: %s failed to create missing file: %r' % (name, e.args))
					except OSError, e:
						r.error.append ('File: %s failed to modify newly created file: %r' % (name, e.args))
				self.postproc (r, 'file', name, f)
				try:
					st = os.stat (name)
					lst = None
					for (var, val) in f.items ():
						if var == 'gid':
							gr = self.getGroup (val)
							if gr is not None:
								if gr.gr_gid != st.st_gid:
									r.error.append ('File: %s expected to have group id %d (%r), but has %d' % (name, gr.gr_gid, val, st.st_gid))
							else:
								r.error.append ('File: no group "%s" found for %s' % (val, name))
						elif var == 'uid':
							ui = self.getUser (val)
							if ui is not None:
								if ui.pw_uid != st.st_uid:
									r.error.append ('File: %s expected to have user id %d (%r) but has %d' % (name, ui.pw_uid, val, st.st_uid))
							else:
								r.error.append ('File: no user "%s" found for %s' % (val, name))
						elif var in ('isdir', 'isfile', 'issuid', 'issgid', 'islink'):
							chk = agn.atob (val)
							if var == 'isdir':
								stc = stat.S_ISDIR (st.st_mode)
							elif var == 'isfile':
								stc = stat.S_ISREG (st.st_mode)
							elif var == 'issuid':
								stc = (stat.S_ISUID & st.st_mode) == stat.S_ISUID
							elif var == 'issgid':
								stc = (stat.S_ISGID & st.st_mode) == stat.S_ISGID
							elif var == 'islink':
								if lst is None:
									lst = os.lstat (name)
								stc = stat.S_ISLNK (lst.st_mode)
							if stc != chk:
								r.error.append ('File: %s %s results to %r, but expected %r (%r)' % (name, var, stc, chk, val))
						elif var == 'mode':
							if stat.S_IMODE (st.st_mode) != val:
								r.error.append ('File: %s has mode %o but expected %o' % (name, stat.S_IMODE (st.st_mode), val))
						elif var == 'mask':
							if stat.S_IMODE (st.st_mode) & val != val:
								r.error.append ('File: %s does not match file mask %o (%o results in %o)' % (name, val, stat.S_IMODE (st.st_mode), stat.S_IMODE (st.st_mode) & val))
						elif not var in ('name', 'content', 'optional'):
							r.error.append ('File: %s unknown check %s' % (name, var))
				except OSError, e:
					if e.args[0] == errno.ENOENT:
						if not f.has_key ('optional') or not f['optional']:
							r.error.append ('File: %s does not exist' % name)
					else:
						r.error.append ('File: %s raises unexpected error: %r' % (name, e.args))
		if symlinks:
			for (path, target) in symlinks:
				needCreate = False
				try:
					self.preproc (r, 'symlink', 'stat', path)
					st = os.lstat (path)
					self.postproc (r, 'symlink', 'stat', path, st)
					if not stat.S_ISLNK (st.st_mode):
						r.error.append ('Symlink: %s is existing, but is no symlink' % path)
					else:
						old = os.readlink (path)
						if old != target:
							os.unlink (path)
							needCreate = True
				except OSError, e:
					if e.args[0] == errno.ENOENT:
						needCreate = True
					else:
						r.error.append ('Symlink: %s failed to determinate current state: %s' % (path, str (e)))
				if needCreate:
					try:
						self.preproc (r, 'symlink', 'create', path, target)
						agn.createPath (os.path.dirname (path))
						os.symlink (target, path)
						self.postproc (r, 'symlink', 'create', path, target)
					except agn.error as e:
						r.error.append ('Symlink: %s failed to create path to file: %s' % (path, e))
					except OSError as e:
						r.error.append ('Symlink: %s failed to create symlink: %s' % (path, e))
		if executables:
			try:
				path = os.environ['PATH']
				pmap = {}
				seen = set ()
				for d in path.split (':'):
					if d == '':
						d = '.'
					if not d in seen:
						if os.path.isdir (d):
							try:
								for f in os.listdir (d):
									if not f in pmap:
										pmap[f] = agn.mkpath (d, f)
							except OSError, e:
								r.error.append ('Exec: Failed to read directory %s: %r' % (d, e.args))
						seen.add (d)
				for e in executables:
					try:
						if e.startswith (os.path.sep):
							path = e
						else:
							path = pmap[e]
						if not os.access (path, os.X_OK):
							r.error.append ('Exec: %s is not executable' % e)
					except KeyError:
						r.error.append ('Exec: %s not found in path' % e)
			except KeyError:
				r.error.append ('Exec: No $PATH variable found')
		if modules:
			for module in modules:
				try:
					self.preproc (r, 'module', module)
					__import__ (module)
					self.postproc (r, 'module', module)
				except ImportError, e:
					r.error.append ('Module: %s not importable: %r' % (module, e.args))
		if checks:
			for check in checks:
				if callable (check):
					try:
						check (r)
					except Exception, e:
						r.error.append ('Check: %r failed: %r' % (check, e.args))
				else:
					r.error.append ('Check: %r is not callable' % (check, ))
		#
		if r.warning:
			print ('*** Warning report for sanity check ***')
			for line in r.warning:
				print (line)
			if not r.error:
				print ('*** will continue execution ***')
		if r.error:
			print ("*** Error report for sanity check ***")
			for line in r.error:
				print (line)
			raise agn.error ('Failed in sanity check')

class Janitor (object):
	"""Framework for housekeeping

This class provides a collection of methods for housekeeping of
application typical log and archive files. The lower level methods can
be used to subclass this class and implmenet some custom cleanup jobs."""
	isnumPattern = re.compile ('^[0-9]+$')
	def __init__ (self, name):
		"""``name'' is the name for this job (informal usage only)"""
		self.name = name
		self.doit = True
		self.timestamp = time.time ()
		self.now = time.localtime (self.timestamp)
		self.today = datetime.datetime (self.now.tm_year, self.now.tm_mon, self.now.tm_mday).toordinal ()

	def log (self, level, message):
		"""Log a ``message'' to ``level'' or print out during dryrun"""
		if self.doit:
			agn.log (level, self.name, message)
		else:
			print ('%s: %s' % (agn.loglevelName (level), message))

	def isnum (self, s):
		"""returns True if s is a valid numeric (>= 0) string representation"""
		return self.isnumPattern.match (s) is not None

	def mktimestamp (self, s):
		"""transforms ``s'' to an ordinal number for that day, otherwise for today"""
		if self.isnum (s) and len (s) == 8:
			ts = datetime.datetime (int (s[:4]), int (s[4:6]), int (s[6:])).toordinal ()
		else:
			ts = self.today
		return ts

	def call (self, cmd):
		"""executes cmd (which is expected as a list or print out command during dryrun"""
		if self.doit:
			try:
				rc = agn.call (cmd)
				if rc != 0:
					self.log (agn.LV_ERROR, 'Command %r returns %d' % (cmd, rc))
			except OSError, e:
				self.log (agn.LV_ERROR, 'Failed to execute %r: %r' % (cmd, e.args))
				rc = -1
		else:
			print ('CALL: %s' % ' '.join (cmd))
			rc = 0
		return rc == 0

	def pack (self, destination, sources, working_directory = None):
		"""Pack ``sources'' (str or list) using tar to
``destination''. Compression is selected depending on extension. If
``working_directory'' is set and is an existing directory, this will
be used as the directory to resolve relative pathnames"""
		if destination.endswith ('.gz'):
			cflag = 'z'
		elif destination.endswith ('.bz2'):
			cflag = 'j'
		elif destination.endswith ('.xz'):
			cflag = 'J'
		else:
			cflag = ''
		if type (sources) in (str, unicode):
			sources = [sources]
		cmd = ['tar']
		if working_directory:
			if not os.path.isdir (working_directory):
				self.log (agn.LV_ERROR, '%s: working directory missing/not a directory for %s' % (working_directory, destination))
				return -1
			cmd += ['-C', working_directory]
		cmd += ['-c%sf' % cflag, destination] + sources
		return self.call (cmd)

	def compress (self, paths, method = 'gzip'):
		"""Compresses a ``paths'' (str or list) using method (gzip by default)"""
		if not paths:
			return True
		#
		if type (paths) in (str, unicode):
			paths = [paths]
		command = None
		if method in ('bzip', 'bzip2', 'bz'):
			command = ['bzip2', '-9']
		elif method == 'xz':
			command = ['xz', '-9']
		if command is None or agn.which (command[0]) is None:
			command = ['gzip', '-9']
		return self.call (command + paths)
	
	def move (self, src, dst):
		"""Move (rename) ``src'' to ``dst''. Requires files to be on the same file system"""
		try:
			if os.path.isdir (dst):
				dst = agn.mkpath (dst, os.path.basename (src))
			if self.doit:
				os.rename (src, dst)
			else:
				print ('MOVE: %s to %s' % (src, dst))
			rc = True
		except OSError, e:
			self.log (agn.LV_ERROR, 'Failed to move %s to %s: %r' % (src, dst, e.args))
			rc = False
		return rc

	def remove (self, path):
		"""Remove a ``path'' (a file or a directory); directories are removed with complete content, if possible!"""
		try:
			if os.path.isdir (path):
				for fname in os.listdir (path):
					self.remove (agn.mkpath (path, fname))
				if self.doit:
					os.rmdir (path)
				else:
					print ('RMDIR: %s' % path)
			else:
				if self.doit:
					os.unlink (path)
				else:
					print ('REMOVE: %s' % path)
			rc = True
		except OSError, e:
			self.log (agn.LV_ERROR, 'Failed to remove %s: %r' % (path, e.args))
			rc = False
		return rc
	
	def rotate (self, path, amount = 4, compress = True):
		"""Rotate ``path'' up to ``amount'' (default 4) instances, compress the rotated file, if ``rotate'' is True"""
		source = amount
		target = None
		while source >= 0:
			if source > 0:
				fnameSource = '%s.%d' % (path, source)
				if compress:
					fnameSource += '.gz'
			else:
				fnameSource = path
			if os.path.isfile (fnameSource):
				if target is None:
					self.log (agn.LV_INFO, 'Removing old %s' % fnameSource)
					self.remove (fnameSource)
				else:
					fnameTarget = '%s.%d' % (path, target)
					if source > 0 and compress:
						fnameTarget += '.gz'
					self.log (agn.LV_INFO, 'Rename %s to %s' % (fnameSource, fnameTarget))
					self.move (fnameSource, fnameTarget)
					if source == 0 and compress:
						self.log (agn.LV_INFO, 'Compress %s' % fnameTarget)
						self.compress (fnameTarget)
			target = source
			source -= 1

	def __countSize (self, seen, path):
		rc = 0L
		if path not in seen:
			seen.add (path)
			if os.path.isdir (path):
				for fname in os.listdir (path):
					npath = os.path.abspath (os.path.join (path, fname))
					if os.path.isdir (npath) or os.path.isfile (npath):
						rc += self.__countSize (seen, npath)
			else:
				st = os.stat (path)
				rc += st.st_size
		return rc

	def freeSpace (self, filesystem, files, multiply = 2.0):
		"""Determinates if a ``filesystem'' has roughly enough room to handle cleanup of ``files'' asuming it will consum ``multiply'' times of space"""
		try:
			st = os.statvfs (filesystem)
			freeSize = st.f_bsize * st.f_bavail
			requiredSize = 0L
			if type (files) not in (list, tuple):
				files = [files]
			seen = set ()
			for fname in files:
				path = os.path.abspath (fname)
				if os.path.isdir (path) or os.path.isfile (path):
					requiredSize += self.__countSize (seen, path)
			if requiredSize * multiply < freeSize:
				rc = (True, 'Currently %s available, %s required' % (agn.sizefmt (freeSize), agn.sizefmt (requiredSize)))
			else:
				rc = (False, 'Not enough space available, %s available, %s required' % (agn.sizefmt (freeSize), agn.sizefmt (requiredSize)))
		except OSError, e:
			self.log (agn.LV_ERROR, 'Failed to stat %s for %r: %r' % (filesystem, files, e.args))
			rc = (False, 'Failed to count required size on %s due to %s' % (filesystem, str (e)))
		return rc
	
	def free (self, filesystem, *args):
		"""Determinate if ``filesystem'' as enough room to handle cleanup of ``*args'' files"""
		(rc, msg) = self.freeSpace (filesystem, args)
		if rc:
			self.log (agn.LV_INFO, msg)
		else:
			self.log (agn.LV_WARNING, msg)
		return rc

	def select (self, path, recr, *callbacks):
		"""Preselect files form ``path'', recrusive if ``recr'' is True and store the selection for each ``*callbacks'' found"""
		selected = [[]] * len (callbacks)
		try:
			subdirs = []
			for fname in os.listdir (path):
				fpath = agn.mkpath (path, fname)
				try:
					st = os.lstat (fpath)
				except OSError:
					st = None
				n = 0
				for callback in callbacks:
					if callback (fpath, fname, st):
						selected[n].append (fpath)
						break
					n += 1
				if recr and st is not None and stat.S_ISDIR (st.st_mode):
					subdirs.append (fpath)
			for subdir in subdirs:
				sselected = self.select (subdir, *callbacks)
				n = 0
				while n < len (selected):
					if sselected[n]:
						selected[n] += sselected[n]
					n += 1
		except OSError, e:
			self.log (agn.LV_WARNING, 'Directory "%s" not scanable: %r' % (path, e.args))
		return selected

	def cleanupTimestampFiles (self, path, compressAfterDays, removeAfterDays):
		"""Cleanup files in ``path'' if they are prefixed by a
valid timestamp by compressing files older than ``compressAfterDays''
days and remove older files than ``removeAfterDays''"""
		toCompress = []
		toRemove = []
		try:
			for fname in os.listdir (path):
				parts = fname.split ('-', 1)
				if len (parts) == 2 and self.isnum (parts[0]):
					ts = self.mktimestamp (parts[0])
					found = False
					for (check, dest, ignoreSuffix) in (removeAfterDays, toRemove, None), (compressAfterDays, toCompress, '.gz'):
						if check is not None:
							for (pattern, offset) in check:
								if (pattern is None or pattern.search (fname) is not None):
									if ts + offset < self.today:
										if ignoreSuffix is None or not fname.endswith (ignoreSuffix):
											dest.append (fname)
										found = True
									break
							if found:
								break
		except OSError, e:
			self.log (agn.LV_ERROR, 'Failed to access log directory "%s": %r' % (path, e.args))
		report = []
		if compressAfterDays is not None:
			report.append ('Found %d files to compress' % len (toCompress))
		if removeAfterDays is not None:
			report.append ('Found %d files to remove' % len (toRemove))
		if report:
			self.log (agn.LV_INFO, ', '.join (report))
		for fname in toRemove:
			self.remove (agn.mkpath (path, fname))
		for fname in toCompress:
			self.compress (agn.mkpath (path, fname))

	def cleanupTimestampDirectories (self, path, packAfterDays, removeAfterDays, compress = None):
		"""Cleanup directories located in ``path'' which are
valid timestamps by packing them after ``packAfterDays'' days and
remove packed files after ``removeAfterDays''. ``compress'' may by set
to a valid value for pack() to use compression for packed files."""
		toPack = []
		toRemove = []
		if compress is not None:
			tarExt = '.tar.%s' % compress
		else:
			tarExt = '.tar'
		for fname in os.listdir (path):
			cpath = agn.mkpath (path, fname)
			if self.isnum (fname) and os.path.isdir (cpath):
				ts = self.mktimestamp (fname)
				if removeAfterDays is not None and ts + removeAfterDays < self.today:
					toRemove.append (cpath)
				elif packAfterDays is not None and ts + packAfterDays < self.today:
					toPack.append (cpath)
			elif fname.endswith (tarExt) and os.path.isfile (cpath):
				ts = self.mktimestamp (fname[:-len (tarExt)])
				if removeAfterDays is not None and ts + removeAfterDays < self.today:
					toRemove.append (cpath)
		for cpath in toRemove:
			self.log (agn.LV_VERBOSE, 'Removing "%s"' % cpath)
			self.remove (cpath)
		for cpath in toPack:
			if self.free (os.path.dirname (cpath), cpath):
				self.log (agn.LV_VERBOSE, 'Packing "%s"' % cpath)
				tar = '%s%s' % (cpath, tarExt)
				if self.pack (tar, os.path.basename (cpath), working_directory = os.path.dirname (cpath)):
					self.log (agn.LV_VERBOSE, 'Removing "%s"' % cpath)
					self.remove (cpath)
			else:
				self.log (agn.LV_WARNING, 'Skip %s, not enough free disk space available' % cpath)
	
	def __cleanupFiletimeFiles (self, path, rules, recrusive):
		toRemove = []
		descend = []
		for fname in os.listdir (path):
			cpath = agn.mkpath (path, fname)
			st = os.stat (cpath)
			if stat.S_ISDIR (st.st_mode):
				if recrusive:
					descend.append (cpath)
			elif stat.S_ISREG (st.st_mode):
				for (check, offset) in rules:
					if ((check is None and not fname.startswith ('.')) or check.search (fname) is not None):
						if st.st_ctime + offset * 24 * 60 * 60 < self.timestamp:
							toRemove.append (cpath)
		for cpath in toRemove:
			self.log (agn.LV_VERBOSE, 'Removing "%s"' % cpath)
			self.remove (cpath)
		for cpath in descend:
			self.log (agn.LV_VERBOSE, 'Descending to "%s"' % cpath)
			self.__cleanupFiletimeFiles (cpath, rules, recrusive)
	def cleanupFiletimeFiles (self, path, rules, recrusive = False):
		"""Cleanup files based on their creation date located
in ``path'' (and below, if ``recrusive'' is True) applying ``rules''.
These is a list of tuple with two members, the first a callback with a
serach() method (e.g. a compiled regular expression pattern) and the
second the age in days to remove files that are older. A shortcut for
remove all files which are older than a given day is by passing an int
instead of list for ``rules''"""
		if type (rules) in (int, long):
			rules = [(None, rules)]
		self.__cleanupFiltimeFiles (path, rules, recrusive)

	def cleanupLogfiles (self, compressAfterDays, removeAfterDays):
		"""Cleanup AGNITAS logfiles, ``compressAfterDays'' days and ``removeAfterDays'' days"""
		self.cleanupTimestampFiles (agn.mkpath (agn.base, 'var', 'log'), compressAfterDays, removeAfterDays)

	def prepare (self, doit):
		"""Setup the cleanup, ``doit'' is True for real action or False for a dryrun"""
		self.doit = doit
		self.log (agn.LV_INFO, 'Janitor starting up')
	
	def execute (self):
		"""Execute the cleanup, must be overwritten by subclass"""
		self.log (agn.LV_INFO, 'No execution available, so do nothing')

	def done (self):
		"""Finalize cleanup"""
		self.log (agn.LV_INFO, 'Janitor going down')

	def run (self, doit):
		"""Main entry point, ``doit'' is True for real action or False for a dryrun"""
		self.prepare (doit)
		self.execute ()
		self.done ()
#
#}}}
#
# Higher database access {{{
class DB (object):
	"""Higher level database abstraction class

This class is used to wrap a database driver and its connections to
one class which provides some convinient methods for dtabase
handling.

You can subclass this class and define a method ``dblog'' (which takes
one argument). This is used for intense database logging and should be
either filtered or used in for debug purpose only."""

	def __init__ (self, dbifc = agn.DBaseID, dbid = None, resultType = None):
		"""``dbifc'' is the factory function using ``dbid'' as database id to create a new driver instance"""
		self.dbifc = dbifc
		self.dbid = dbid
		self.resultType = resultType
		self.db = None
		self.cursor = None
		self._lastError = None
		self._tablespaceCache = {}
		self._scratchTables = []
		self._scratchNo = 0
		self._cache = None
		self._checkpoints = None
		
	def __del__ (self):
		self.close ()
		
	def __enter__ (self):
		if self.isopen ():
			return self
		raise agn.error ('Failed to setup database: %s' % self.lastError ())

	def __exit__ (self, exc_type, exc_value, tb):
		self.close (exc_type is None)
		self.done ()
		return False

	def done (self):
		"""Finalize the usage of this database interface"""
		self.close ()
		
	def close (self, commit = True):
		"""Close the database driver if open, closing (and commiting changes, if ``commit'' is True) all open cursors and checkpoints, remove temp. tables"""
		if self._checkpoints:
			for cp in self._checkpoints.values ():
				cp.done (commit)
			self._checkpoints = None
		if self.cursor:
			while self._scratchTables:
				table = self._scratchTables.pop ()
				self.cursor.execute ('TRUNCATE TABLE %s' % table)
				self.cursor.execute ('DROP TABLE %s' % table)
		if self.db is not None:
			if self.cursor is not None:
				self.cursor.sync (commit)
				self.cursor.close ()
			self.db.close ()
			self._lastError = self.db.lastError ()
		self.db = None
		self.cursor = None
		
	def reset (self):
		"""Resets (rollbacks) changes done"""
		if self.cursor is not None:
			self.cursor.sync (False)
		
	def new (self):
		"""Creates a new database driver instance"""
		if self.dbid is not None:
			db = self.dbifc (self.dbid)
		else:
			db = self.dbifc ()
		with agn.Ignore (AttributeError):
			if callable (self.dblog):
				db.log = self.dblog
		return db
		
	def open (self):
		"""Opens (closes a currently open) database driver and sets up the standard cursor"""
		self.close ()
		self.db = self.new ()
		if self.db is not None:
			if self.resultType is not None:
				self.db.resultType = self.resultType
			self.cursor = self.db.cursor ()
			if self.cursor is None:
				self.close ()
		self._tablespaceCache = {}
		return self.cursor is not None

	def isopen (self):
		"""Checks if database is available and tries to open it, if not"""
		if self.db is None or self.cursor is None:
			self.open ()
		return self.cursor is not None
	
	def stream (self, *args, **kws):
		if self.isopen ():
			return self.db.stream (*args, **kws)
		return None
		
	def request (self):
		"""Requests a new cursor"""
		if self.isopen ():
			return self.db.cursor ()
		return None
		
	def release (self, cursor, commit = True):
		"""Releases a cursor, if ``commit'' is True commits changes"""
		if cursor is not None and cursor is not self.cursor:
			cursor.sync (commit)
			cursor.close ()
		
	def commit (self, commit = True):
		"""If ``commit'' is true, changes are commited, else rollbacked"""
		if self.cursor is not None:
			self.cursor.sync (commit)
		
	def lastError (self):
		"""Returns last database error message"""
		if self.db is not None:
			return self.db.lastError ()
		return self._lastError if self._lastError else 'No active database interface (%s)' % str (self.dbid)

	class Cache (object):
		Memory = agn.mutable (sentinel = 'Memory')
		def __init__ (self, filename):
			self.filename = filename
			if filename is self.Memory:
				self.p = {}
			else:
				self.p = None
			self.e = pickle.dumps
			self.d = lambda v: pickle.loads (str (v))
			if filename is not self.Memory:
				self.p = DDB (self.filename)
			
		def __del__ (self):
			self.done ()
			
		def __setitem__ (self, key, value):
			self.p[self.e (key)] = self.e (value)
			
		def __getitem__ (self, key):
			return self.d (self.p[self.e (key)])
				
		def __delitem__ (self, key):
			del self.p[self.e (key)]
			
		def __contains__ (self, key):
			return self.e (key) in self.p
			
		def keys (self):
			return [self.d (_k) for _k in self.p.keys ()]
			
		def done (self):
			if self.p is not None:
				if type (self.p) is not dict:
					self.p.close ()
				self.p = None
				
	def cacheDisable (self):
		"""Disable caching for database queries"""
		if self._cache:
			self._cache.done ()
			self._cache = None

	def cacheEnable (self, filename = None):
		"""Enable caching for database queries.

Caching only works if the query() method of this class is used. To
access the database, the standard cursor is used. For compaitibility a
queryc() and a querys() method is also available using the cache."""
		if self._cache is not None:
			self._cache.write ()
		self._cache = self.Cache (filename)
		
	def cache (self):
		"""Return the cache instance"""
		return self._cache
		
	def query (self, *args, **kws):
		"""Execute a query try to resolve the query from the cache, if caching is enabled"""
		if self._cache is not None:
			key = (args, kws)
			try:
				rc = self._cache[key]
			except KeyError:
				rc = self.cursor.queryc (*args, **kws)
				self._cache[key] = rc
			return rc
		#
		return self.cursor.query (*args, **kws)

	def queryc (self, *args, **kws):
		"""Execute a cached query try to resolve the query from the cache, if caching is enabled"""
		if self._cache is not None:
			return self.query (*args, **kws)
		#
		return self.cursor.queryc (*args, **kws)
		
	def querys (self, *args, **kws):
		"""Execute a single row query try to resolve the query from the cache, if caching is enabled"""
		if self._cache is not None:
			rc = None
			for rec in self.query (*args, **kws):
				rc = rec
				break
			return rc
		#
		return self.cursor.querys (*args, **kws)
			
	def update (self, *args, **kws):
		"""Update database content, a convinient method to complete the minimal cursor interface"""
		return self.cursor.update (*args, **kws)
	execute = update
		
	sysdate = agn.mutable (id = 'singlton', name = 'sysdate', value = '%(sysdate)s')
	class Row (object):
		def __init__ (self, ref, table, keys, result):
			self.ref = ref
			self.table = table
			self.keys = keys
			self.result = {}
			for (var, val) in result:
				self.result[var] = val
			self.modified = {}
			
		def __getitem__ (self, var):
			try:
				return self.modified[var]
			except KeyError:
				return self.result[var]
			
		def __setitem__ (self, var, val):
			self.modified[var] = val
			
		def update (self, var, val):
			if var not in self.result or self.result[var] != val:
				self[var] = val
			
		def put (self):
			if self.modified:
				rc = False
				query = 'UPDATE %s SET ' % self.table
				data = {}
				sep = ''
				for (var, val) in self.modified.items ():
					query += '%s%s = ' % (sep, var)
					if val is self.ref.sysdate:
						query += self.ref.cursor.rselect (val.value)
					else:
						query += ':v__%s' % var
						data['v__%s' % var] = val
					sep = ', '
				query += ' WHERE %s' % ' AND '.join (['%s = :k__%s' % (_k, _k) for _k in self.keys])
				for (var, val) in self.keys.items ():
					data['k__%s' % var] = val
				if self.ref.cursor.update (query, data) == 1:
					rc = True
			else:
				rc = True
			return rc
		
	class Get (object):
		def __init__ (self, ref, table, keylist, keydata, columns, query):
			self.ref = ref
			self.table = table
			self.keylist = keylist
			self.keydata = keydata
			self.columns = columns
			self.query = query
			self.cnames = None
			self.clobs = None
			self.blobs = None
			self.keypos = None
			self.cursor = None
			
		def __reset (self):
			self.cnames = None
			self.clobs = None
			self.blobs = None
			self.keypos = None
			if self.cursor is not None:
				self.cursor.close ()
				self.cursor = None

		def __iter__ (self):
			self.__reset ()
			self.cursor = self.ref.db.cursor ()
			self.cursor.query (self.query, self.keydata)
			return self
			
		def next (self):
			try:
				data = next (self.cursor)
				if self.cnames is None:
					self.clobs = set ()
					self.blobs = set ()
					desc = self.cursor.description ()
					if desc is not None:
						self.cnames = [_d[0].lower () for _d in desc]
						if self.columns is not None:
							self.cnames = self.cnames[len (self.keylist):]
						driver = self.ref.db.driver
						pos = 0
						for d in desc:
							if d[1] in (driver.CLOB, driver.LONG_STRING):
								self.clobs.add (pos)
							elif d[1] in (driver.BLOB, driver.BINARY, driver.LONG_BINARY):
								self.blobs.add (pos)
							pos += 1
					else:
						self.cnames = self.columns
				if self.keypos is None:
					if self.columns is None:
						self.keypos = []
						for key in self.keylist:
							self.keypos.append (self.cnames.index (key.lower ()))
					else:
						self.keypos = range (len (self.keylist))
				r = []
				n = 0
				for d in data:
					if d is None:
						r.append (d)
					elif n in self.clobs:
						r.append (str (d))
					elif n in self.blobs:
						r.append (bytes (d))
					else:
						r.append (d)
					n += 1
				rkeys = {}
				n = 0
				for key in self.keylist:
					rkeys[self.keylist[n]] = r[self.keypos[n]]
					n += 1
				if self.columns is not None:
					r = r[n:]
				rc = self.ref.Row (self.ref, self.table, rkeys, zip (self.cnames, r))
			except StopIteration:
				self.__reset ()
				raise
			return rc

	def get (self, table, columns, clause, **keys):
		"""A higher level get

this method queries ``table'' for ``columns'' (or all columns if
columns is None or '*') using the optional ``clause''. **keys provides
a selection to create a own clause based on the keys (using AND)."""
		rc = None
		if self.isopen ():
			if type (columns) is tuple:
				columns = list (columns)
			elif type (columns) is not list:
				if columns is None or columns == '*':
					columns = None
				else:
					columns = [columns]
			keylist = keys.keys ()
			if clause is None:
				if keylist:
					clause = ' WHERE %s' % ' AND '.join (['%s = :%s' % (_k, _k) for _k in keylist])
					skeys = keys
				else:
					clause = ''
					skeys = None
			else:
				if clause:
					clause = ' WHERE %s' % clause
				skeys = None
				for key in keys:
					if ':%s' % key in clause:
						if skeys is None:
							skeys = {key: keys[key]}
						else:
							skeys[key] = keys[key]
			if columns is None:
				qcolumns = '*'
			else:
				qcolumns = ', '.join (keylist + columns)
			query = 'SELECT %s FROM %s%s' % (qcolumns, table, clause)
			rc = self.Get (self, table, keylist, skeys, columns, query)
		return rc
		
	def getall (self, table, columns, clause, **keys):
		"""simular to get, but already fetches all rows"""
		getter = self.get (table, columns, clause, **keys)
		if getter is not None:
			rc = []
			for row in getter:
				rc.append (row)
		else:
			rc = None
		return rc
		
	def put (self, rows):
		"""write back changed ``rows''"""
		if type (rows) not in (list, tuple):
			rows = [rows]
		rc = True
		for row in rows:
			if not row.put ():
				rc = False
		return rc
		
	def add (self, table, idColumn, **columns):
		"""adds a new row to ``table'' using ``idColumn'' for unique id, if table has one with **columns as values"""
		rc = None
		if self.isopen ():
			queries = {
				'oracle': 'INSERT INTO %s (' % table,
				'mysql':  'INSERT INTO %s (' % table
			}
			if idColumn is None or idColumn in columns:
				idSet = True
			else:
				queries['oracle'] += '%s' % idColumn
				if columns:
					queries['oracle'] += ', '
				idSet = False
			sep = ''
			for var in columns.keys ():
				for query in queries:
					queries[query] += '%s%s' % (sep, var)
				sep = ', '
			queries['oracle'] += ') VALUES ('
			if not idSet:
				queries['oracle'] += '%s_seq.nextval' % table
				if columns:
					queries['oracle'] += ', '
			queries['mysql'] += ') VALUES ('
			data = {}
			sep = ''
			for (var, val) in columns.items ():
				if val is self.sysdate:
					rvar = self.cursor.rselect (val.value)
				else:
					rvar = ':v__%s' % var
					data['v__%s' % var] = val
				for query in queries:
					queries[query] += '%s%s' % (sep, rvar)
				sep = ', '
			for query in queries:
				queries[query] += ')'
			if self.cursor.execute (self.cursor.qselect (**queries), data) == 1:
				if idSet:
					rc = True
				else:
					queries = {
						'oracle': 'SELECT %s_seq.currval FROM dual' % table,
						'mysql':  'SELECT LAST_INSERT_ID()'
					}
					cnt = self.cursor.querys (self.cursor.qselect (**queries))
					if cnt is not None and cnt[0] is not None:
						newID = cnt[0]
						rec = self.get (table, None, None, **{idColumn: newID})
						if rec and len (rec) == 1:
							rc = rec[0]
		return rc

	def exists (self, tableName, cachable = False):
		"""Check database if ``tableName'' exists as table, if ``cachable'' is True, cache the result for faster future access"""
		if cachable:
			cTableName = tableName.lower ()
			try:
				if cTableName in self.tableCache:
					return self.tableCache[cTableName]
			except AttributeError:
				self.tableCache = {}
		#
		query = self.cursor.qselect (
			oracle = 'SELECT count(*) FROM user_tables WHERE lower(table_name) = lower(:tableName)',
			mysql = 'SELECT count(*) FROM information_schema.tables WHERE lower(table_name) = lower(:tableName) AND table_schema=(SELECT SCHEMA())',
			sqlite = 'SELECT count(*) FROM sqlite_master WHERE lower(name) = lower(:tableName) AND type = \'table\''
		)
		rc = False
		if self.isopen ():
			rq = self.cursor.querys (query, {'tableName': tableName})
			if rq is not None:
				rc = rq[0] > 0
				if cachable:
					self.tableCache[cTableName] = rc
		return rc
		
	def existsIndex (self, tableName, indexName, cachable = False):
		"""Check database if ``indexName' index exists for ``tableName'', if ``cachable'' is True, cache the result for faster future access"""
		if cachable:
			cIndexKey = (tableName.lower (), indexName.lower ())
			try:
				if cIndexKey in self.indexCache:
					return self.indexCache[cIndexKey]
			except AttributeError:
				self.indexCache = {}
		#
		query = self.cursor.qselect (
			oracle = 'SELECT count(*) FROM user_indexes WHERE lower(table_name) = lower(:tableName) AND lower(index_name) = lower(:indexName)',
			mysql = 'SELECT count(*) FROM information_schema.statistics WHERE lower(table_name) = lower(:tableName) AND lower(index_name) = lower(:indexName) AND table_schema=(SELECT SCHEMA())',
			sqlite = 'SELECT count(*) FROM sqlite_master WHERE lower(tbl_name) = lower(:tableName) AND lower(name) = lower(:indexName) AND type = \'index\''
		)
		rc = False
		if self.isopen ():
			rq = self.cursor.querys (query, {'tableName': tableName, 'indexName': indexName})
			if rq is not None:
				rc = rq[0] > 0
				if cachable:
					self.indexCache[cIndexKey] = rc
		return rc
		
	def findTablespace (self, tablespace, *args):
		"""Oracle only: find a proper tablespace by name, use default tablespace, if ``tablespace'' does not exists"""
		if self.isopen ():
			try:
				rc = self._tablespaceCache[tablespace]
			except KeyError:
				rc = None
				if self.db.dbms == 'oracle':
					cursor = self.request ()
					try:
						tablespaces = list (args)
						if tablespace is not None:
							tablespaces.insert (0, tablespace)
						for ts in tablespaces:
							query = 'SELECT tablespace_name FROM user_tablespaces WHERE lower(tablespace_name) = lower(:tablespaceName)'
							data = {'tablespaceName': ts}
							rq = cursor.querys (query, data)
							if rq is not None and rq[0] is not None:
								rc = rq[0]
								break
						if rc is None:
							query = 'SELECT default_tablespace FROM user_users'
							for row in cursor.query (query):
								if row[0] is not None:
									rc = row[0]
									break
					finally:
						self.release (cursor)
				self._tablespaceCache[tablespace] = rc
		return rc
		
	def scratchRequest (self, name = None, layout = None, indexes = None, select = None, tablespace = None, reuse = False):
		"""Request (and create) a scratch table for temporary data

``name'' will become part of the table name of the scratch table, so
keep it short. ``layout'' is the layout of the table as one string
where the columns are comma separated. ``indexes'' can be a string or
a list of strings with columns to create as index on the scratch
table, ``select'' is a select statement to fill the scratch table.
``tablespace'' is the the tablespace where the table should be created
in (Oracle ony) and if ``reuse'' is True, then an existing table is
reused by this process, otherwise it will scan for a non existing
one."""
		if name is None:
			name = agn.logname.upper ()
		table = None
		if self.isopen ():
			while True:
				self._scratchNo += 1
				table = 'TMP_SCRATCH_%s_%d_TBL' % (name, self._scratchNo)
				exists = self.exists (table)
				if exists and reuse:
					self.cursor.execute ('TRUNCATE TABLE %s' % table)
					self.cursor.execute ('DROP TABLE %s' % table)
					exists = self.exists (table)
				if not exists:
					if layout or select:
						query = 'CREATE TABLE %s' % table
						if layout:
							query += ' (%s)' % layout
						if self.db.dbms == 'oracle':
							tablespace = self.findTablespace (tablespace)
							if tablespace:
								query += ' TABLESPACE %s' % tablespace
						if select:
							query += ' AS SELECT %s' % select
						self.cursor.execute (query)
						if indexes:
							if type (indexes) not in (list, tuple):
								indexes = [indexes]
							ino = 0
							for index in indexes:
								ino += 1
								iname = 'TS$%s$%d$%d$IDX' % (name, self._scratchNo, ino)
								query = 'CREATE INDEX %s ON %s (%s)' % (iname, table, index)
								if self.db.dbms == 'oracle' and tablespace:
									query += ' TABLESPACE %s' % tablespace
								self.cursor.execute (query)
					self._scratchTables.append (table)
					break
		return table
		
	def scratchRelease (self, table):
		"""Release (and removes) a scratch table"""
		rc = False
		if self.isopen ():
			while table in self._scratchTables:
				if not rc:
					if self.exists (table):
						self.cursor.execute ('TRUNCATE TABLE %s' % table)
						self.cursor.execute ('DROP TABLE %s' % table)
					rc = True
				self._scratchTables.remove (table)
		return rc

	class Streamer (object):
		def __init__ (self, stream, mode):
			if type (stream) in (str, unicode):
				self.fd = agn.copen (stream, mode)
				self.mine = True
			else:
				self.fd = stream
				self.mine = False
			self.mode = mode
			self.dirty = False
			self.written = 0
			
		def write (self, s):
			self.fd.write (s)
			self.dirty = True
			self.written += len (s)
			
		def read (self, size = None):
			if size is None:
				return self.fd.read ()
			else:
				return self.fd.read (size)
			
		def flush (self):
			if self.dirty:
				with agn.Ignore (AttributeError):
					self.fd.flush ()
				self.dirty = False
			
		def close (self):
			if self.mine:
				self.fd.close ()
			else:
				self.flush ()
		
		def tell (self):
			return self.fd.tell ()
		
		def seek (self, position, whence = 0):
			return self.fd.seek (position, whence)

					
	def dump (self, table, stream, fields = None, clause = None, order = None, param = None, outtable = None, compact = False):
		"""dump a table or a part of a table into a XML file

``table'' is the table to be read, ``stream'' is the output stream to
be written to (either a filename or a file like object). ``fields'' is
the list of columns to select or all if ``fields'' is None. ``clause''
is the optional clause to restrict the amount of the result. ``order''
can be used to output the result in a specific order. ``param'' is a
dict for the query if named parameter are used. ``outtable'' is the
name to be written as table name to the file, if this is None, then
the original table name is used. If ``compact'' is True a more
compact, but less readable file is created."""
		rc = False
		msg = None
		if not self.isopen ():
			msg = 'Failed to setup database'
		elif not self.exists (table):
			msg = 'Table %s does not exists' % table
		else:
			if fields is None:
				flist = '*'
			else:
				flist = ', '.join (fields)
			query = 'SELECT %s FROM %s' % (flist, table)
			if clause:
				query += ' WHERE %s' % clause
			if order:
				query += ' ORDER BY %s' % order
			self.cursor.query (query, param)
			desc = self.cursor.description ()
			if desc:
				layout = []
				for d in desc:
					column = agn.mutable (name = d[0], out = d[0], typ = None, size = d[3], precision = d[4], notNull = d[6] == 0, formater = lambda a: a)
					if d[1] == self.db.driver.STRING:
						column.typ = 's'
					elif d[1] == self.db.driver.FIXED_CHAR:
						column.typ = 'f'
					elif d[1] == self.db.driver.NUMBER:
						column.typ = 'n'
					elif d[1] in (self.db.driver.DATETIME, self.db.driver.TIMESTAMP):
						column.typ = 'd'
						column.formater = lambda a: '%04d-%02d-%02d %02d:%02d:%02d' % (a.year, a.month, a.day, a.hour, a.minute, a.second)
					elif d[1] == self.db.driver.CLOB:
						column.typ = 'c'
						column.formater = lambda a: a.read ()
					elif d[1] == self.db.driver.BLOB:
						column.typ = 'b'
						column.formater = lambda a: base64.encodestring (a.read ())
					else:
						if msg is None:
							msg = ''
						else:
							msg = ', '
						msg += 'Unsupported datatype %r for %s' % (d[1], d[0])
					if column.typ is not None:
						layout.append (column)
				if msg is None:
					fd = self.Streamer (stream, 'w')
					rc = True
					out = xagn.XMLWriter (fd)
					out.start ()
					if outtable is None:
						outtable = table
					out.open ('dump', table = outtable)
					out.open ('layout')
					pos = 0
					aliases = []
					for column in layout:
						pos += 1
						attrs = {'typ': column.typ, 'size': column.size, 'precision': column.precision, 'notNull': column.notNull}
						if compact:
							alias = 'f%d' % pos
							attrs['alias'] = alias
							aliases.append (alias)
							column.out = alias
						out.text ('column', column.name, **attrs)
					out.close ('layout')
					first = True
					for row in self.cursor:
						if first:
							out.open ('data')
							first = False
						out.open ('record')
						for (column, value) in zip (layout, row):
							if value is None:
								out.node (column.out, null = 'true')
							else:
								out.text (column.out, column.formater (value))
						out.close ('record')
					if not first:
						out.close ('data')
					out.close ('dump')
					out.end ()
					fd.close ()
		return (rc, msg)

	class Reader (object):
		def __init__ (self, ref, table, force, callback):
			self.ref = ref
			self.table = table
			self.force = force
			self.callback = callback
			self.skip = False
			self.layout = None
			self.aliases = None
			self.insert = None
			self.isize = None
			self.count = None
			self.record = None
			self.ridx = None
			self.convert = {
				's':	lambda s: s.encode ('UTF-8'),
				'f':	lambda s: s.encode ('UTF-8'),
				'n':	lambda s: float (s) if '.' in s else int (s),
				'd':	lambda s: self.__parseDate (s),
				'c':	lambda s: s.encode ('UTF-8'),
				'b':	lambda s: base64.decodestring (s)
			}
			if self.ref.db.dbms == 'sqlite':
				self.convert['s'] = lambda s: s
				self.convert['f'] = lambda s: s
				self.convert['c'] = lambda s: s
				self.convert['b'] = lambda s: self.ref.db.Binary (base64.decodestring (s))
			
		def dumpEnter (self, path, name, attrs):
			if self.table is None:
				self.table = attrs['table']
				if not self.force and self.ref.exists (self.table):
					raise agn.error ('Target table %s already exists' % self.table)
			if self.force:
				self.skip = self.ref.exists (self.table)
			
		def layoutEnter (self, path, name, attrs):
			self.layout = []
			self.aliases = {}

		def layoutLeave (self, path, name, attrs, content):
			if self.callback:
				self.callback (self.layout, None, None)
			parts = ['CREATE TABLE %s (' % self.table]
			nr = 0
			cols = []
			vals = []
			self.isize = {}
			for l in self.layout:
				nr += 1
				value = 'v%d' % nr
				cols.append (l.name)
				vals.append (':%s' % value)
				s = '\t%s\t' % l.name
				try:
					if self.ref.db.dbms in ('sqlite', 'dbsim'):
						if l.typ in ('s', 'f', 'c'):
							s += 'text'
						elif l.typ == 'n':
							if l.size and l.precision:
								s += 'real'
							else:
								s += 'integer'
						elif l.typ == 'd':
							s += 'timestamp'
						elif l.typ == 'b':
							s += 'binary'
						else:
							raise TypeError ()
					else:
						if l.typ == 's':
							s += 'VARCHAR2(%d)' % l.size
						elif l.typ == 'f':
							s += 'CHAR(%d)' % l.size
						elif l.typ == 'n':
							s += 'NUMBER(%d,%d)' % (l.size, l.precision)
						elif l.typ == 'd':
							s += 'DATE'
						elif l.typ == 'c':
							s += 'CLOB'
							if self.ref.db.dbms == 'oracle':
								self.isize[value] = self.ref.db.driver.CLOB
						elif l.typ == 'b':
							s += 'BLOB'
							if self.ref.db.dbms == 'oracle':
								self.isize[value] = self.ref.db.driver.BLOB
						else:
							raise TypeError ()
				except TypeError:
					raise agn.error ('Column %s has unknown typ %s' % (l.name, l.typ))
				if l.notNull:
					s += ' NOT NULL'
				if l != self.layout[-1]:
					s += ','
				with agn.Ignore (AttributeError):
					self.aliases[l.alias] = l.name
				parts.append (s)
			parts.append (')')
			query = '\n'.join (parts)
			if not self.skip:
				self.ref.cursor.execute (query)
			self.insert = 'INSERT INTO %s (%s) VALUES (%s)' % (self.table, ', '.join (cols), ', '.join (vals))
			if self.isize:
				self.ref.cursor.setInputSizes (**self.isize)
			
		def columnLeave (self, path, name, attrs, content):
			try:
				alias = 'alias'
				col = agn.mutable (name = content, typ = attrs['typ'], size = int (attrs['size']), precision = int (attrs['precision']), notNull = agn.atob (attrs['notNull']))
				if alias in attrs:
					col.alias = attrs[alias]
				self.layout.append (col)
			except (KeyError, ValueError, TypeError), e:
				raise agn.error ('Invalid layout line for "%s": %r (%r)' % (content, attrs, e))

		def dataEnter (self, path, name, attrs):
			self.count = 0
			
		def dataLeave (self, path, name, attrs, content):
			self.ref.commit ()

		__dateParser = re.compile ('([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}):([0-9]{2}):([0-9]{2})')
		def __parseDate (self, s):
			m = self.__dateParser.match (s)
			if m is None:
				raise ValueError ('format mismatch')
			g = [int (_g) for _g in m.groups ()]
			return datetime.datetime (g[0], g[1], g[2], g[3], g[4], g[5])
				
		def recordEnter (self, path, name, attrs):
			self.record = []
			self.ridx = 0

		def recordLeave (self, path, name, attrs, content):
			self.count += 1
			if self.callback:
				self.callback (self.layout, self.record, self.count)
			nr = 0
			data = {}
			for r in self.record:
				nr += 1
				data['v%d' % nr] = r
			cnt = self.ref.cursor.execute (self.insert, data)
			if cnt != 1:
				raise agn.error ('Failed to insert record %r' % (self.record, ))
			if self.count % 1000 == 0:
				self.ref.commit ()
			self.record = None

		def recordCollect (self, path, name, attrs, content):
			l = self.layout[self.ridx]
			if l.name != name and name not in self.aliases and l.name != self.aliases[name]:
				raise agn.error ('Row %d:Sanity check: expecting column %s, got %s' % (self.count, l.name, name))
			try:
				if u'null' in attrs and agn.atob (attrs[u'null']):
					self.record.append (None)
				else:
					if content is None:
						content = ''
					try:
						self.record.append (self.convert[l.typ] (content))
					except KeyError:
						raise agn.error ('Row %d:invalid typ %s for column %s found' % (self.count, l.typ, name))
			except ValueError:
				raise agn.error ('Row %d:invalid value %r for column %s' % (self.count, content, name))
			self.ridx += 1
				
	def load (self, stream, table = None, force = False, callback = None):
		"""Loads a file to database written using DB.dump()

``stream'' is the source, either a filename or a file like object. If
``table'' is not None use this as the target table ane, otherwise use
the name found in the source file. If ``force'' is True, then write
the content even to table which is already existing (and which must
obey the table layout of the XML file).

``callback'' is an optional callbale taking three parameter:
- layout: the layout of the table
- record: the record read
- count: the number of records already read.

``callback'' is called after reading the layout definition from the
file with the second and third parameter set to None and for every row
read."""
		rc = False
		msg = None
			
		if not self.isopen ():
			msg = 'Failed to setup database'
		elif table is not None and not force and self.exists (table):
			msg = 'Target table %s already exists' % table
		else:
			ch = self.Reader (self, table, force, callback if callback and callable (callback) else None)
			rd = xagn.XMLReader ()
			rd.addHandler ('dump', ch.dumpEnter, None)
			rd.addHandler ('dump.layout', ch.layoutEnter, ch.layoutLeave)
			rd.addHandler ('dump.layout.column', None, ch.columnLeave)
			rd.addHandler ('dump.data', ch.dataEnter, ch.dataLeave)
			rd.addHandler ('dump.data.record', ch.recordEnter, ch.recordLeave)
			rd.addHandler ('dump.data.record.', None, ch.recordCollect)
			rc = True
			try:
				if type (stream) in (str, unicode):
					rd.processFile (stream)
				else:
					rd.processStream (stream)
			except agn.error as e:
				rc = False
				msg = str (e)
		return (rc, msg)
		
	def export (self, stream, query, data = None, ignore = None, dialect = None,
		converter = None, checkpoint = None, checkpointLog = None, modifier = None,
		validator = None, limit = None
	):
		"""exports a query to one or more CSV files

``stream'' is either a filename or a callable which creates a filename
(only if ``limit'' is used). ``query'' is the query to be executed
using ``data'' as the named parameter. ``ignore'' is a list of columns
not to output to the file. ``dialect'' is the CSV dialect to use for
the output. ``converter'' is a optional dict to convert input data to
output strings. ``checkpoint'' is a numeric value after which a
checkpoint should be executed which means a commit is performed to
release database resources and the optional method ``checkpointLog''
is called to provide logging which is called with a parameter, the
current number of records written. ``modifier'' is called for each row
to allow further modifications before the data is written to the file.
If ``validator'' is not None it will be called for every row. If this
method returns False, then this row will not be written to the file.
``limit'' is the number of rows per file, this allows to create more
files to limit the size of each file."""
		rc = False
		msg = []
		if not self.isopen ():
			msg.append ('Failed to setup database')
		else:
			cmap = converter.copy () if converter is not None else {}
			driver = self.db.driver
			converterDefaults = [
				(None, lambda h, v: str (v) if v is not None else v),
				((driver.DATETIME, driver.TIMESTAMP), lambda h, v: v.isoformat () if v is not None else v)
			]
			for (tlist, convert) in converterDefaults:
				if type (tlist) not in (list, tuple):
					tlist = [tlist]
				for typ in tlist:
					if typ not in cmap:
						cmap[typ] = convert
			if dialect is None:
				dialect = agn.CSVDefault
			if checkpoint is None:
				checkpoint = 10000
			count = 0
			def handleCheckpoint ():
				self.commit ()
				if checkpointLog is not None and callable (checkpointLog):
					checkpointLog (count)
			if modifier is not None and not callable (modifier):
				raise agn.error ('modifier %r not callable' % (modifier, ))
			if validator is not None and not callable (validator):
				raise agn.error ('validator %r is not callable' % (validator, ))
			fd = None
			wr = None
			try:
				fileno = 0
				header = None
				converter = None
				ign = set ()
				reducer = lambda a: a
				handleCheckpoint ()
				for row in self.cursor.query (query, data):
					count += 1
					if count % checkpoint == 0:
						handleCheckpoint ()
					if header is None:
						desc = self.cursor.description ()
						if ignore:
							if type (ignore) not in (list, tuple):
								ignore = [ignore]
							ignore = [_i.lower () for _i in ignore]
							ign = [_d[0].lower () not in ignore for _d in desc]
							reducer = lambda a: [_f[1] for _f in filter (lambda b: b[0], [_l for _l in zip (ign, a)])]
						header = reducer ([_h[0] for _h in desc])
						converter = []
						for h in reducer (desc):
							try:
								converter.append (cmap[h[1]])
							except KeyError:
								converter.append (cmap[None])
								msg.append ('No converter for type %r for %s found' % (h[1], h[0]))
					if validator and not validator (row):
						continue
					if modifier:
						row = list (row)
						modifier (row)
					if wr is not None and limit is not None and limit > 0 and fd.written > limit:
						wr.close ()
						fd.close ()
						wr = None
						fd = None
					if wr is None:
						if limit is not None:
							fileno += 1
							if callable (stream):
								filename = stream (fileno)
							else:
								filename = stream % fileno
						else:
							filename = stream
						fd = self.Streamer (filename, 'w')
						wr = agn.CSVWriter (fd, dialect)
						wr.write (header)
					wr.write ([_c (_h, _v) for (_c, _h, _v) in zip (converter, header, reducer (row))])
				if count % checkpoint != 0:
					handleCheckpoint ()
				rc = True
			finally:
				if wr is not None:
					wr.close ()
				if fd is not None:
					fd.close ()
		return (rc, ', '.join (msg))

	class Checkpoint (object):
		epoch = datetime.datetime (1970, 1, 1)
		def __init__ (self, db, name, table, cursor, start, end):
			self.db = db
			self.name = name
			self.table = table
			self.cursor = cursor
			self.start = start
			self.end = end
			self.persist = None
			
		def __del__ (self):
			self.done (False)
		
		def __enter__ (self):
			return self
			
		def __exit__ (self, exc_type, exc_value, tb):
			self.done (exc_type is None)

		def setup (self):
			query = 'SELECT checkpoint, persist FROM %s WHERE name = :name' % self.table
			rq = self.cursor.querys (query, {'name': self.name})
			if rq is not None and rq[0] is not None:
				self.start = rq[0]
				self.persist = str (rq[1]) if rq[1] is not None else None
			
		def done (self, commit = True):
			if self.cursor is not None:
				if commit:
					insert = False
					if self.start < self.end:
						query = 'UPDATE %s SET checkpoint = :checkpoint, persist = :persist WHERE name = :name' % self.table
						data = {
							'name': self.name,
							'checkpoint': self.end,
							'persist': self.persist
						}
						if self.db.db.dbms == 'oracle':
							self.cursor.setInputSizes (persist = self.db.db.driver.CLOB)
						if self.cursor.execute (query, data) == 0:
							insert = True
					else:
						data = {
							'name': self.name
						}
						query = 'SELECT count(*) FROM %s WHERE name = :name' % self.table
						rq = self.cursor.querys (query, data)
						if rq[0] == 0:
							data['checkpoint'] = self.start
							data['persist'] = self.persist
							insert = True
					if insert:
						query = 'INSERT INTO %s (name, checkpoint, persist) VALUES (:name, :checkpoint, :persist)' % self.table
						if self.db.db.dbms == 'oracle':
							self.cursor.setInputSizes (persist = self.db.db.driver.CLOB)
						if self.cursor.execute (query, data) == 0:
							raise agn.error ('failed to create new entry for %s in %s' % (self.name, self.table))
					self.cursor.sync (True)
				self.db.release (self.cursor)
				self.cursor = None
				self.db.uncheckpoint (self)
		
	def uncheckpoint (self, cp):
		"""Releases a checkpoint"""
		if self._checkpoints and cp.name in self._checkpoints and self._checkpoints[cp.name] is cp:
			del self._checkpoints[cp.name]

	def checkpoint (self, table, name, start = None, end = None, fresh = False, tablespace = None, endoffset = None):
		"""Requests a checkpoint, create it, if not existing

A checkpoint instance is used to limit a query to create delta results
since last time queried. ``table'' is the table to store the last
checkpoint. If table does not exists, it is created using
``tablespace'', if not None (Oracle only). ``name'' is the name for
this checkpoint, ``start'' is the start timestamp if the checkpoint is
new (1970-01-01 by default), ``end'' is the end for this delta
(default is now). If ``fresh'' is True then the checkpoint behaves as
if it had not existed."""
		if self._checkpoints and name in self._checkpoints:
			return self._checkpoints[name]
		#
		rc = None
		if self.isopen ():
			cursor = self.request ()
			if cursor is not None:
				if not self.exists (table):
					tablespace = self.findTablespace (tablespace)
					if tablespace:
						tablespaceExpr = ' TABLESPACE %s' % tablespace
					else:
						tablespaceExpr = ''
					layout = cursor.qselect (
						oracle = (
							'CREATE TABLE %s (\n'
							'	name		varchar2(100)	PRIMARY KEY NOT NULL,\n'
							'	checkpoint	date		NOT NULL,\n'
							'       persist         clob\n'
							')%s'
							% (table, tablespaceExpr)
						), mysql = (
							'CREATE TABLE %s (\n'
							'	name		varchar(100)	PRIMARY KEY NOT NULL,\n'
							'	checkpoint	timestamp	NOT NULL,\n'
							'       persist         longtext\n'
							')'
							% table
						), sqlite = (
							'CREATE TABLE %s (\n'
							'	name		text		PRIMARY KEY NOT NULL,\n'
							'	checkpoint	timestamp	NOT NULL,\n'
							'       persist         text\n'
							')'
							% table
						)
					)
					cursor.execute (layout)
				else:
					cursor.querys ('SELECT * FROM %s WHERE 1 = 0' % table)
					desc = cursor.description (normalize = True)
					if desc is not None and 'persist' not in [_d[0] for _d in desc]:
						cursor.execute (cursor.qselect (
							oracle = 'ALTER TABLE %s ADD persist clob' % table,
							mysql = 'ALTER TABLE %s ADD persist longtext' % table,
							sqlite = 'ALTER TABLE %s ADD persist text' % table
						))
				if self.exists (table) and name is not None:
					if type (start) in (str, unicode):
						start = agn.ParseTimestamp () (start)
					if start is None:
						start = self.Checkpoint.epoch
					if end is None:
						now = datetime.datetime.now ()
						end = datetime.datetime (now.year, now.month, now.day, now.hour, now.minute, now.second)
						if endoffset is not None:
							if type (endoffset) in (str, unicode):
								offset = Unit ().parse (endoffset, None)
								if offset is None:
									raise agn.error ('%s: invalid offset expression' % endoffset)
							elif type (endoffset) is float:
								offset = int (endoffset)
							elif type (endoffset) in (int, long):
								offset = endoffset
							else:
								raise agn.error ('%r: invalid offset type %s' % (endoffset, type (endoffset)))
							if offset != 0:
								end -= datetime.timedelta (seconds = offset)
					rc = self.Checkpoint (self, name, table, cursor, start, end)
					if not fresh:
						rc.setup ()
					if self._checkpoints is None:
						self._checkpoints = {}
					self._checkpoints[name] = rc
				else:
					self.release (cursor)
		return rc
#}}}
#
# Caching UID handling {{{
class UID (agn.UID):
	"""Convinient UID handling

compared to the agn.UID handles this class the retrival of database
values by itself."""
	__slots__ = ['dbid', 'db', 'cursor', 'usedid', 'makeVersion', 'minVersion']
	licences = {None: agn.licence}
	companies = {}
	mailings = {}
	TRACKING_VETO = 0
	DISABLE_LINK_EXTENSION = 1
	def __init__ (self, dbid = None):
		super (UID, self).__init__ ()
		self.dbid = None
		self.db = None
		self.cursor = None
		self.usedid = None
		self.makeVersion = None
		self.minVersion = None
	
	def __enter__ (self):
		self.open ()
		return self
	
	def __exit__ (self, exc_type, exc_value, tb):
		self.close ()
		return False
	
	def __isopenDB (self):
		if self.cursor is not None and self.dbid != self.usedid:
			self.__closeDB ()
		return self.cursor is not None
	
	def __openDB (self):
		self.db = agn.DBaseID (self.dbid)
		if self.db is not None:
			self.cursor = self.db.cursor ()
			if self.cursor is None:
				self.db.close ()
				self.db = None
			else:
				self.usedid = self.dbid
		return self.__isopenDB ()
	
	def __closeDB (self):
		if self.db is not None:
			if self.cursor is not None:
				self.cursor.close ()
			self.db.close ()
		self.db = None
		self.cursor = None
	
	def __retrieveLicence (self):
		if self.dbid not in self.licences:
			licence = 0
			if self.__isopenDB () or self.__openDB ():
				for r in self.cursor.query (
					'SELECT value FROM config_tbl WHERE class = :class AND name = :name',
					{'class': 'system', 'name': 'licence'}
				):
					try:
						licence = int (r[0])
					except ValueError:
						agn.log (agn.LV_ERROR, 'uid', 'Invalid licence %r in database found for %r' % (licence, self.dbid if self.dbid is not None else agn.DBDriver.dbid))
			self.licences[self.dbid] = licence
	
	def __retrieveMailing (self):
		if self.mailingID:
			key = (self.dbid, self.mailingID)
			try:
				mailing = self.mailings[key]
			except KeyError:
				mailing = None
				if self.__isopenDB () or self.__openDB ():
					for r in self.cursor.query ('SELECT company_id, creation_date FROM mailing_tbl WHERE mailing_id = :mailingID', {'mailingID': self.mailingID}):
						mailing = agn.mutable (companyID = r[0], timestamp = r[1])
				self.mailings[key] = mailing
			if mailing is not None:
				self.companyID = mailing.companyID
				self.setTimestamp (mailing.timestamp)
			else:
				self.companyID = 0
				self.setTimestamp (0)
		
	def __retrieveCompany (self):
		if self.companyID:
			key = (self.dbid, self.companyID)
			try:
				company = self.companies[key]
			except KeyError:
				company = None
				if self.__isopenDB () or self.__openDB ():
					for r in self.cursor.query ('SELECT secret_key, enabled_uid_version, uid_version FROM company_tbl WHERE company_id = :companyID', {'companyID': self.companyID}):
						company = agn.mutable (
							secret = r[0],
							makeVersion = r[1],
							minVersion = r[2]
						)
				self.companies[key] = company
			if company is not None:
				self.secret = company.secret
				self.makeVersion = company.makeVersion
				self.minVersion = company.minVersion
			else:
				self.secret = None
				self.makeVersion = None
				self.minVersion = None
	
	def __fillMissing (self, uid):
		self.prefill ()
		self.__retrieveLicence ()
		if uid.licenceID != self.licences[self.dbid]:
			raise agn.error ('Unable to fill foreign ID (%d vs. %d)' % (uid.licenceID, self.licences[self.dbid]))
		self.__retrieveMailing ()
		self.__retrieveCompany ()
		self.postfill ()
		if self.companyID and self.timestamp and self.secret is not None:
			return True
		return False
	
	def open (self):
		"""Open the database connection"""
		self.__closeDB ()
		return self.__openDB ()
	
	def done (self):
		"""Finalize usage of class and release resources"""
		self.__closeDB ()

	def createUID (self):
		"""Create a new UID using the previously set instance variables"""
		isopen = self.__isopenDB ()
		self.__retrieveMailing ()
		self.__retrieveCompany ()
		if self.makeVersion is not None:
			oldVersion = self.version
			self.version = self.makeVersion
			if self.version not in self.versions:
				self.version = (agn.Stream.of (self.versions if self.minVersion is not None else None)
					.filter (lambda v: v >= self.minVersion)
					.first (no = self.defaultVersion)
				)
		fail = None
		try:
			rc = super (UID, self).createUID ()
		except agn.error as e:
			fail = e
		finally:
			if self.makeVersion is not None:
				self.version = oldVersion
		if not isopen:
			self.__closeDB ()
		if fail is not None:
			raise fail
		return rc
	
	def parseUID (self, uid, dbValidate = True):
		"""Parse a uid and fill the instance variables with the values"""
		isopen = self.__isopenDB ()
		try:
			super (UID, self).parseUID (uid, self.__fillMissing, dbValidate)
		finally:
			if not isopen:
				self.__closeDB ()
		if dbValidate and self.minVersion is not None and self.version < self.minVersion:
			raise agn.error ('version %d too old, allow only version %d and up for company %d' % (self.version, self.minVersion, self.companyID))

	def __bitIsSet (self, bit):
		return self.bitOption & (1 << bit)
	def __bitSet (self, bit, value):
		if value:
			self.bitOption |= (1 << bit)
		else:
			self.bitOption &= ~(1 << bit)
	def _trackingVetoGet (self):
		return self.__bitIsSet (self.TRACKING_VETO)
	def _trackingVetoSet (self, value):
		self.__bitSet (self.TRACKING_VETO, value)
	def _trackingVetoDel (self):
		self.__bitSet (self.TRACKING_VETO, False)
	trackingVeto = property (_trackingVetoGet, _trackingVetoSet, _trackingVetoDel)

	def _disableLinkExtensionGet (self):
		return self.__bitIsSet (self.DISABLE_LINK_EXTENSION)
	def _disableLinkExtensionSet (self, value):
		self.__bitSet (self.DISABLE_LINK_EXTENSION, value)
	def _disableLinkExtensionDel (self):
		self.__bitSet (self.DISABLE_LINK_EXTENSION, False)
	disableLinkExtension = property (_disableLinkExtensionGet, _disableLinkExtensionSet, _disableLinkExtensionDel)
	
	def prefill (self):
		"""Hook called before filling database informations"""
		pass
	def postfill (self):
		"""Hook called after filling database informations"""
		pass
#}}}
#
# Daemonic/Runtime Tools {{{
class Daemonic (object):
	"""Base class for daemon processes

This class provides the basic functionality to allow a process to run
as a daemon (a background process which runs continuously) and low
level process control. In general this (or one of its subclasses)
should be subclassed and extended for the process to implement."""
	@classmethod
	def call (cls, method, *args, **kws):
		"""Call a method in a subprocess, return process status"""
		def wrapper (queue, method, *args, **kws):
			try:
				rc = method (*args, **kws)
			except BaseException as e:
				rc = e
			queue.put (rc)
		queue = agn.multiprocessing.Queue ()
		d = cls ()
		p = d.join (joinPID = d.spawn (wrapper, queue, method, *args, **kws))
		if p.error is not None:
			raise p.error
		if queue.empty ():
			return None
		rc = queue.get ()
		if issubclass (type (rc), BaseException):
			raise rc
		return rc

	def __init__ (self):
		self.running = True
		self.signals = {}

	def done (self):
		agn.Stream.of (self.signals.iteritems ()).each (lambda kv: signal.signal (kv[0], kv[1]))
		
	def setsignal (self, signr, action):
		old_action = signal.signal (signr, action)
		if signr not in self.signals:
			self.signals[signr] = old_action
			
	def signalHandler (self, sig, stack):
		"""Standard signal handler for graceful termination"""
		self.running = False
	
	def setupHandler (self, master):
		"""Setup signal handler, ``master'' should be True, if signal handling is not handled elsewhere, too"""
		self.setsignal (signal.SIGTERM, self.signalHandler)
		if master:
			self.setsignal (signal.SIGINT, self.signalHandler)
		else:
			self.setsignal (signal.SIGINT, signal.SIG_IGN)
		self.setsignal (signal.SIGCHLD, signal.SIG_DFL)
		self.setsignal (signal.SIGHUP, signal.SIG_IGN)
		self.setsignal (signal.SIGPIPE, signal.SIG_IGN)
	
	knownSignals = (agn.Stream.of (signal.__dict__.iteritems ())
		.regexp ('SIG[^_]', key = lambda kv: kv[0])
		.dict ()
	)
	knownHandlers = (agn.Stream.of (signal.__dict__.iteritems ())
		.filter (lambda kv: kv[0].startswith ('SIG_'))
		.dict ()
	)
	def setupCustomHandler (self, **kws):
		(agn.Stream.of (kws.iteritems ())
			.map (lambda kv: (
				kv[0].upper (),
				kv[1].upper () if type (kv[1]) in (str, unicode) else kv[1]
			))
			.map (lambda kv: (
				'SIG%s' % kv[0] if not kv[0].startswith ('SIG') else kv[0],
				'SIG_%s' % kv[1] if type (kv[1]) in (str, unicode) and not kv[1].startswith ('SIG_') else kv[1]
			))
			.each (lambda kv: self.setsignal (
				self.knownSignals[kv[0]],
				self.knownHandlers[kv[1]] if type (kv[1]) in (str, unicode) else kv[1]
			))
		)

	def pushToBackground (self):
		"""Fork a subprocess, returns True for the parent and False for the subprocess"""
		if os.fork () > 0:
			return True
		for fd in 0, 1, 2:
			with agn.Ignore (OSError):
				os.close (fd)
		try:
			devnull = platform.DEV_NULL
		except AttributeError:
			devnull = '/dev/null'
		fd = os.open (devnull, os.O_RDWR)
		if fd == 0:
			if os.dup (fd) == 1:
				os.dup (fd)
		os.setsid ()
		return False
	
	def redirect (self, inputPath, outputPath):
		"""Redirect stdin to ``inputPath'' and stdout and stderr to ``outputPath''"""
		for (path, flags, mode, targets) in [
			(inputPath, os.O_RDONLY, 0, [0]),
			(outputPath, os.O_WRONLY | os.O_APPEND | os.O_CREAT | os.O_SYNC, 0600, [1, 2])
		]:
			if path is not None:
				fd = os.open (path, flags, mode)
				for nfd in targets:
					os.close (nfd)
					cfd = os.dup (fd)
					if cfd != nfd:
						agn.log (agn.LV_ERROR, 'redirect', 'Expect new fd #%d, but got #%d' % (nfd, cfd))
				os.close (fd)

	def spawn (self, method, *args, **kws):
		"""Create a subprocess and call ``method'' using ``args'' as its arguments"""
		pid = os.fork ()
		if pid == 0:
			ec = 1
			self.signals = {}
			try:
				self.setupHandler (False)
				ec = method (*args, **kws)
				if ec is None:
					ec = 0
				elif type (ec) is bool:
					ec = 0 if ec else 1
				elif type (ec) not in (int, long):
					ec = 0
			except Exception as e:
				agn.logexc (agn.LV_ERROR, 'spawn', 'Failed to call %s: %s' % (method, e))
				raise
			finally:
				sys.exit (ec)
		return pid
	
	def execute (self, cmd, failCode = 127):
		"""Executes a command ``cmd'' (a list), replacing the currently running process"""
		if len (cmd) > 0:
			with agn.Ignore (OSError):
				if cmd[0].startswith (os.path.sep):
					os.execv (cmd[0], cmd)
				else:
					os.execvp (cmd[0], cmd)
		os._exit (failCode)

	def join (self, joinPID = -1, block = True):
		"""Waits for ``joinPID'' (if -1, then for any child process), ``block'' is True to wait for a child process to terminate"""
		if block:
			flags = 0
		else:
			flags = os.WNOHANG
		rc = agn.mutable (pid = None, status = None, exitcode = None, signal = None, nochild = False, error = None)
		try:
			(pid, status) = os.waitpid (joinPID, flags)
			rc.pid = pid
			rc.status = status
			if os.WIFEXITED (status):
				rc.exitcode = os.WEXITSTATUS (status)
			elif os.WIFSIGNALED (status):
				rc.signal = os.WTERMSIG (status)
		except OSError as e:
			rc.error = e
			rc.nochild = (rc.error.args[0] == errno.ECHILD)
		return rc

	def term (self, pid, sig = None):
		"""Terminates a child process ``pid'' with signal ``sig'' (if None, SIGTERM is used)"""
		if sig is None:
			sig = signal.SIGTERM
		rc = agn.mutable (killed = False, error = None)
		try:
			os.kill (pid, sig)
			rc.killed = True
		except OSError, e:
			rc.error = e
		return rc

class Watchdog (Daemonic):
	"""Watchdog framework

This class is based on Daemonic to implement a watchdog functionality.
It provides serveral methods which can be overwritten by the subclass.
To start the process one must call the method ``mstart''. The method
``start'' is considered as legacy and should no longer be used. The
watchdog supports monitoring of more than one subprocess.

Every process to run under watchdog control must be an instance of
``Watchdog.Job'' or a subclass of this class."""
	EC_EXIT = 0
	EC_RESTART = 12
	EC_STOP = 13
	class Job (object):
		"""Represents a single subprocess under watchdog control"""
		class Restart (Exception):
			"""Exception to be thrown to force a restart of the process"""
			pass
		def __init__ (self, name, method, args, output = None, heartbeat = None):
			"""Setup the subprocess

``name'' is the name to identify the process, ``method'' is the method
to call with ``args'' as its arguments. The method must either return
True, if it terminates regular and no restart is required, or False if
the method returns with an error, but not restart should take place.
To enforce a restart, the method should raise the Job.Restart
exception. Every other exception will also result in a restart.

If ``output'' is not None, this is a path where stdout and stderr are
redirected to. In this case, stdin is redirected to ``/dev/null''. If
heartbeat is not None, it must be a positive numeric value which
represents seconds in between the child process must signal to be
active calling the method ``beat''. If there is no heartbeat, the
watchdog process will reststart the process assuming it is in some
undefined condition."""
			self.name = name
			self.method = method
			self.args = args
			self.output = output
			self.heartbeat = heartbeat
			self.watchdog = None
			self.pid = None
			self.last = 0
			self.incarnation = 0
			self.hb = None
			self.killedByHeartbeat = False
		
		def __call__ (self):
			"""Entry point for Watchdog to start this job"""
			try:
				if self.output is not None:
					try:
						self.watchdog.redirect ('/dev/null', self.output)
					except Exception as e:
						agn.log (agn.LV_ERROR, 'watchdog/%s' % self.name, 'Failed to establish redirection: %s' % str (e))
				rc = self.method (*self.args)
			except self.Restart:
				sys.exit (Watchdog.EC_RESTART)
			except Exception as e:
				agn.logexc (agn.LV_ERROR, 'watchdog/%s' % self.name, 'Execution failed: %s' % str (e))
				sys.exit (Watchdog.EC_RESTART)
			sys.exit (Watchdog.EC_EXIT if rc else Watchdog.EC_STOP)
		
		def beat (self):
			"""Signal an active subprocess"""
			if self.hb is not None:
				self.hb.set (int (time.time ()))
		
		def beating (self):
			"""Checks for active subprocess (called by the Watchdog)"""
			if self.hb is not None:
				last = self.hb.get ()
				if last is not None:
					now = int (time.time ())
					return last + self.heartbeat > now
			return True
	
	class Heart (object):
		class Beat (object):
			def __init__ (self, mem, pos, size):
				self.mem = mem
				self.pos = pos
				self.size = size
			
			def clear (self):
				self.set (None)
			
			def set (self, value):
				ser = pickle.dumps (value)
				if len (ser) < self.size - 8:
					self.mem[self.pos:self.pos + self.size] = '%8d%s%s' % (len (ser), ser, ' ' * (self.size - len (ser) - 8))
			
			def get (self):
				for retry in 0, 1, 2:
					ser = self.mem[self.pos:self.pos + self.size]
					size = int (ser[:8])
					try:
						return pickle.loads (ser[8:size + 8])
					except Exception as e:
						agn.log (agn.LV_ERROR, 'beat', 'Failed to unpickle input: %s' % str (e))
					time.sleep (0.1)
				return None
				
		def __init__ (self, count):
			self.count = count
			self.chunk = 512
			self.mem = mmap.mmap (-1, self.count * self.chunk)
			self.cur = 0
		
		def done (self):
			self.mem.close ()
		
		def slot (self):
			if self.cur >= self.count:
				raise IndexError ('no more slots left for new heartbeat entry')
			rc = self.Beat (self.mem, self.cur * self.chunk, self.chunk)
			rc.clear ()
			self.cur += 1
			return rc
			
	def mstart (self, jobs, restartDelay = None, terminationDelay = None, maxIncarnations = None):
		"""Starts one or more ``jobs''

The ``jobs'' are (either an instance of Watchdog.Job or a list of
instances of Watchdog.Job. ``restartDelay'' is the amount in seconds
to restrart a subprocess which has terminated unexpected.
``terminationDelay'' is the amount in seconds when a subprocess has
been terminated and is still active to kill it the hard way.
``maxIncarnation'', if not None, is the maximum number of restarts for
a process until the watchdog gives up."""
		unit = Unit ()
		restartDelay = unit.parse (restartDelay, 60)
		terminationDelay = unit.parse (terminationDelay, 10)
		if isinstance (jobs, self.Job):
			jobs = [jobs]
		hb = None
		for job in jobs:
			job.watchdog = self
			if job.heartbeat is not None:
				if hb is None:
					hb = self.Heart (len (jobs))
				job.hb = hb.slot ()
			agn.log (agn.LV_INFO, 'watchdog', 'Added job %s' % job.name)
		done = []
		self.setupHandler (True)
		agn.log (agn.LV_INFO, 'watchdog', 'Startup with %d job%s' % (len (jobs), '' if len (jobs) == 1 else 's'))
		self.startup (jobs)
		nolog = set ()
		while self.running and jobs:
			active = 0
			now = int (time.time ())
			for job in jobs:
				if job.pid is None:
					if job.last + restartDelay < now:
						job.killedByHeartbeat = False
						job.beat ()
						self.spawning (job)
						job.pid = self.spawn (job)
						job.last = now
						job.incarnation += 1
						agn.log (agn.LV_INFO, 'watchdog', 'Launching job %s' % job.name)
						active += 1
						if job in nolog:
							nolog.remove (job)
					else:
						if job not in nolog:
							agn.log (agn.LV_DEBUG, 'watchdog', 'Job %s is not ready, will start in %d seconds' % (job.name, job.last + restartDelay - now))
							nolog.add (job)
				else:
					active += 1
					if not job.beating ():
						agn.log (agn.LV_WARNING, 'watchdog', 'Job %s (PID %d) heart beat failed, force hard kill' % (job.name, job.pid))
						self.term (job.pid, signal.SIGKILL)
						job.killedByHeartbeat = True
			#
			if not self.running: continue
			time.sleep (1)
			if not self.running or not active: continue
			#
			rc = self.join (block = False)
			if rc.pid == 0:
				continue
			#
			if rc.error is not None:
				if rc.nochild:
					agn.log (agn.LV_ERROR, 'watchdog', 'Even with %d active jobs we cannot wait for one' % active)
				elif rc.error.args[0] != errno.EINTR:
					agn.log (agn.LV_WARNING, 'watchdog', 'Waiting for %d jobs is interrupted by %s' % (active, str (rc.error)))
				continue
			#
			found = None
			for job in jobs:
				if job.pid == rc.pid:
					found = job
					break
			if found is None:
				agn.log (agn.LV_INFO, 'watchdog', 'Collected PID %d without matching job' % rc.pid)
				continue
			#
			restart = True
			if rc.exitcode is not None:
				if rc.exitcode in (self.EC_EXIT, self.EC_STOP) or (maxIncarnations and found.incarnation >= maxIncarnations):
					if rc.exitcode == self.EC_EXIT:
						agn.log (agn.LV_INFO, 'watchdog', 'Job %s terminated normally, no restart scheduled' % found.name)
					elif rc.exitcode == self.EC_STOP:
						agn.log (agn.LV_INFO, 'watchdog', 'Job %s terminates with error, but enforces no restart' % found.name)
					else:
						agn.log (agn.LV_INFO, 'watchdog', 'Job %s terminates with error, but reached maximum incarnation counter %d' % (found.name, found.incarnation))
					restart = False
				elif rc.exitcode == self.EC_RESTART:
					agn.log (agn.LV_INFO, 'watchdog', 'Job %s terminated for restart' % found.name)
				else:
					agn.log (agn.LV_INFO, 'watchdog', 'Job %s terminated with exit code %d' % (found.name, rc.exitcode))
			elif rc.signal is not None:
				if rc.signal in (signal.SIGKILL, signal.SIGTERM):
					if rc.signal == signal.SIGKILL and found.killedByHeartbeat:
						agn.log (agn.LV_INFO, 'watchdog', 'Job %s had been killed due to missing heart beat, restart scheduled' % found.name)
					else:
						restart = False
						agn.log (agn.LV_INFO, 'watchdog', 'Job %s died due to signal %d, no restart scheduled assuming forced termination' % (found.name, rc.signal))
				else:
					agn.log (agn.LV_INFO, 'watchdog', 'Job %s died due to signal %d' % (found.name, rc.signal))
			if not restart:
				jobs.remove (found)
				done.append (found)
			self.joining (found, rc)
			found.pid = None
		#
		self.terminating (jobs, done)
		agn.log (agn.LV_INFO, 'watchdog', 'Terminating')
		n = terminationDelay
		while jobs:
			rc = self.join (block = False)
			for job in jobs:
				if job.pid == rc.pid or job.pid is None:
					jobs.remove (job)
					done.append (job)
					self.joining (job, rc)
					break
			#
			if not jobs: continue
			#
			if n in (0, terminationDelay):
				for job in jobs:
					self.term (job.pid, signal.SIGKILL if n == 0 else signal.SIGTERM)
					agn.log (agn.LV_INFO, 'watchdog', 'Signaled job %s to terminate' % job.name)
			n -= 1
			time.sleep (1)
		self.teardown (done)
		if hb is not None:
			hb.done ()
		agn.log (agn.LV_INFO, 'watchdog', 'Teardown')

	@agn.Deprecated ()
	def start (self, *args, **kws):
		def starter (*args, **kws):
			ec = self.run (*args, **kws)
			if ec == self.EC_RESTART:
				raise self.Job.Restart ()
			return ec == self.EC_EXIT
		self.mstart (self.Job ('default', starter, ()), *args, **kws)
	#
	# Methods to override
	#
	# Must implement as entry point for new process
	def run (self):
		"""The entry point for the legacy ``start'' method"""
		raise agn.error ('Subclass must implement run method')
	#
	# called once during startup
	def startup (self, jobs):
		"""Is called after setup, but before any child process is started"""
		pass
	#
	# called once for teardonw
	def teardown (self, done):
		"""Is called after all child processs are terminated, but before the watchdog exits

``done'' is the list of jobs which are known to had terminated."""
		pass
	#
	# called once when ready to terminate
	def terminating (self, jobs, done):
		"""Is called during exit before terminating the children

``jobs'' are a list of still active children and ``done'' is a list of
jobs already terminated."""
		pass
	#
	# called before every starting of a process
	def spawning (self, job):
		"""Is called after ``job'' had been started"""
		pass
	#
	# called after every joining of a process
	def joining (self, job, ec):
		"""Is called after ``job'' has terminated with ``ec'' exit condition"""
		pass

class EWatchdog (Watchdog):
	"""External watchdog

Starts an external program under watchdog control."""
	def __init__ (self, cmd):
		"""``cmd'' is the command (as a list) to be executed"""
		Watchdog.__init__ (self)
		self.cmd = cmd
	
	def run (self):
		self.execute (self.cmd, failCode = self.EC_STOP)

class Daemon (Daemonic):
	"""Daemon process base class

For writing a daemon process you can subclass this class and overwrite
the ``run'' method."""
	def __init__ (self, name):
		"""``name'' is the name of the process"""
		Daemonic.__init__ (self)
		self.name = name
	
	def __checkScript (self, path, script):
		if os.path.isfile (path):
			fd = open (path, 'rb')
			content = fd.read ()
			fd.close ()
			renew = script != content
		else:
			renew = True
		for (cpath, mask, cmode) in (agn.base, 0111, 0711), (os.path.dirname (path), 0555, 0755):
			try:
				st = os.stat (cpath)
				if not stat.S_ISDIR (st.st_mode):
					raise agn.error ('Required path "%s" is no directory' % cpath)
				nmode = st.st_mode | mask
				if nmode != st.st_mode:
					os.chmod (cpath, stat.S_IMODE (nmode))
			except OSError, e:
				if e.args[0] != errno.ENOENT:
					raise
				omask = os.umask (0)
				os.mkdir (cpath, cmode)
				os.umask (omask)
		if renew:
			fd = open (path, 'wb')
			fd.write (script)
			fd.close ()
		st = os.stat (path)
		nmode = st.st_mode | 0555
		if nmode != st.st_mode:
			os.chmod (path, stat.S_IMODE (nmode))
	
	def checkCustomScript (self, script):
		"""Check condition for process

This method starts a custom script to check if the runtime condition
are in a sane condition. If the script does not exists, it is created
using the content of ``script''."""
		path = agn.mkpath (agn.base, 'bin', 'check_custom.sh')
		self.__checkScript (path, script)
		pp = subprocess.Popen ([path], stdin = subprocess.PIPE, stdout = subprocess.PIPE, stderr = subprocess.PIPE)
		(pout, perr) = pp.communicate (None)
		if pp.returncode != 0:
			agn.log (agn.LV_WARNING, self.name, 'Check script returns %d:\nStdout: %s\nStderr: %s' % (pp.returncode, pout, perr))
			return False
		return True
	
	def __starter (self, inBackground, runner, *args):
		if inBackground and self.pushToBackground ():
			return
		agn.lock ()
		agn.log (agn.LV_INFO, self.name, 'Starting up')
		self.setupHandler (True)
		runner (*args)
		agn.log (agn.LV_INFO, self.name, 'Going down')
		agn.unlock ()
	
	def __wait (self, active, block = True):
		got = True
		while got and active:
			got = False
			st = self.join (block = block)
			if st.error is not None:
				if st.nochild:
					agn.log (agn.LV_WARNING, 'wd', 'Unable to wait for any child even if %d are looking active' % len (active))
					for child in list (active.values ()):
						del active[child.pid]
			elif st.pid:
				try:
					child = active[st.pid]
					child.pid = -1
					child.status = st.status
					del active[st.pid]
					got = True
					block = False
					if st.exitcode is not None:
						how = 'exited'
						if st.exitcode > 0:
							how += ' with status %d' % st.exitcode
					elif st.signal is not None:
						how = 'killed by %d' % st.signal
					else:
						how = 'died with status %d' % st.status
					agn.log (agn.LV_INFO, 'wd/%d' % st.pid, 'Child %s %s' % (child.method.__name__, how))
				except KeyError:
					agn.log (agn.LV_WARNING, 'wd/%d' % st.pid, 'Returned not active PID %d' % st.pid)

	def __watchdog (self, children, restartOnError):
		children = [agn.mutable (method = _c, pid = -1, status = 0) for _c in children]
		active = {}
		agn.log (agn.LV_INFO, 'wd', 'Watchdog starting with %d child(ren)' % len (children))
		while self.running:
			for child in children:
				if child.pid == -1:
					if child.status == 0 or restartOnError:
						child.pid = self.spawn (child.method)
						child.status = 0
						active[child.pid] = child
						agn.log (agn.LV_INFO, 'wd/%d' % child.pid, 'Child %s started' % child.method.__name__)
			if not active:
				self.running = False
			else:
				self.__wait (active)
		if active:
			agn.log (agn.LV_INFO, 'wd', 'Watching waiting for %d active child(ren)' % len (active))
			signr = None
			while active:
				for child in list (active.values ()):
					st = self.term (child.pid, signr)
					if st.error is not None and st.error.args[0] == errno.ESRCH:
						agn.log (agn.LV_WARNING, 'wd/%d' % child.pid, 'Unexpected missing child process %s' % child.method.__name__)
						del active[child.pid]
				if active:
					for state in True, False:
						self.__wait (active, False)
						if state and active:
							time.sleep (1)
				signr = signal.SIGKILL
		agn.log (agn.LV_INFO, 'wd', 'Watchdog terminating')
	
	def start (self, inBackground = False):
		"""Start the daemon, push it to the background, if ``inBackground'' is True"""
		self.__starter (inBackground, self.run)
	
	def startWithWatchdog (self, children, inBackground = False, restartOnError = False):
		"""Start the daemon under watchdog control"""
		self.__starter (inBackground, self.__watchdog, children, restartOnError)

	def run (self, *args):
		"""Entry point for implemention"""
		pass

class Worker (Daemon):
	"""Framework for a XML-RPC daemon with worker process

This provides a two process framework where one serves client requests
via XML-RPC )controller) while the other performs the operation as a
worker (executor). Subclass this class and implement the desired
overwritable methods. 

1.) Controller:
- controllerConfig: request a configuration value for a parameter, currently these are available:
	- xmlrpc-server-timeout (float, 5.0): defines the timeout for the XML-RPC server communication
- controllerSetup: is called once at the beginning to inititalize the controller part of the service
	This method returns a generic context which is passed to further methods as first parameter
- controllerTeardown: is called once at the end of the controller
- controllerRegiser: is used to register all XML-RPC methods which should be callable
- controllerStep: is regulary called during server operation

The controller methods can call ``enqueue'' to pass an object to the
worker part, the executor.

2.) Executor:
- executorConfig: request a configuration value for a parameter, currently these are available:
	- ignore-duplicate-requests (bool, False): If identical requests are found in the queue, only one is passed for processing, if this is set to True
	- handle-multiple-requests (bool, False): If False, enqueued objects are passed one by one for processing, otherwise they are passed in chunks
- executorSetup: is called once at the beginning to inititalize the executor part of the service
	This method returns a generic context which is passed to further methods as first parameter
- executorTeardown: is called once at the end of the executor
- executorStep: is regulary called during server operation
- executorRequestPreparse: preparses a received object from the controller, must return the final object
- executorRequestNext: prepare the next object (or objects as a list, if "handle-multiple-requests" is True); must return the value to finally process
- executorRequestHandle: processes the object (or the list of objects) in a subprocess
"""
	nameCtrl = 'ctrl'
	nameExec = 'exec'
	
	def controllerConfig (self, what, default):
		"""returns configuration value for ``what'', using ``default'' if no custom value is available"""
		return default
	def controllerSetup (self):
		"""setup up context to use in further calls"""
		return None
	def controllerTeardown (self, ctx):
		"""cleanup used resources"""
		pass
	def controllerRegister (self, ctx, serv):
		"""register methods to XML-RPC server ``serv''"""
		pass
	def controllerStep (self, ctx):
		"""called periodically"""
		pass
		
	def __controller (self):
		agn.log (agn.LV_DEBUG, 'ctrl', 'Controller starting')
		ctx = self.controllerSetup ()
		timeout = self.controllerConfig ('xmlrpc-server-timeout', 5.0)
		serv = xagn.XMLRPC (self.cfg, timeout = timeout)
		self.controllerRegister (ctx, serv)
		while self.running:
			self.controllerStep (ctx)
			while self.backlog and not self.queue.full ():
				self.queue.put (self.backlog.popleft ())
			with agn.Ignore (select.error):
				serv.server.handle_request ()
		self.controllerTeardown (ctx)
		agn.log (agn.LV_DEBUG, 'ctrl', 'Controller terminating')

	def executorConfig (self, what, default):
		"""returns configuration value for ``what'', using ``default'' if no custom value is available"""
		return default
	def executorSetup (self):
		"""setup up context to use in further calls"""
		return None
	def executorTeardown (self, ctx):
		"""cleanup used resources"""
		pass
	def executorStep (self, ctx):
		"""called periodically"""
		pass
	def executorRequestPreparse (self, ctx, rq):
		"""preparses request ``rq'' after fetching from queue"""
		return rq
	def executorRequestNext (self, ctx, rq):
		"""prepare request(s) ``rq'' before being processed"""
		return rq
	def executorRequestHandle (self, ctx, rq):
		"""process request(s) ``rq''"""
		pass
		
	def __executor (self):
		agn.log (agn.LV_DEBUG, 'exec', 'Executor starting')
		ctx = self.executorSetup ()
		ignoreDups = self.executorConfig ('ignore-duplicate-requests', False)
		multipleRequests = self.executorConfig ('handle-multiple-requests', False)
		pending = []
		child = None
		while self.running:
			self.executorStep (ctx)
			with agn.Ignore (IOError, Queue.Empty, agn.error):
				rq = self.queue.get (timeout = 1.0)
				agn.log (agn.LV_DEBUG, 'exec', 'Received %r while %d requests pending' % (rq, len (pending)))
				rq = self.executorRequestPreparse (ctx, rq)
				if not ignoreDups or rq not in pending:
					pending.append (rq)
				else:
					agn.log (agn.LV_DEBUG, 'exec', 'Ignore request %r as already one is pending' % (rq, ))
			#
			while len (pending) > 0:
				agn.log (agn.LV_DEBUG, 'exec', '%d pending requests' % len (pending))
				if child is not None:
					child.join (0)
					if not child.is_alive ():
						child = None
						agn.log (agn.LV_DEBUG, 'exec', 'Previous instance had finished')
				if child is None:
					if multipleRequests:
						rq = pending
						pending = []
					else:
						rq = pending.pop (0)
					try:
						rq = self.executorRequestNext (ctx, rq)
						child = agn.multiprocessing.Process (target = self.executorRequestHandle, args = (ctx, rq))
						child.start ()
						agn.log (agn.LV_DEBUG, 'exec', 'Started instance process')
					except agn.error, e:
						agn.log (agn.LV_ERROR, 'exec', 'Failed to start child: %s' % str (e))
				else:
					agn.log (agn.LV_DEBUG, 'exec', 'Busy')
					break
		if child is not None:
			child.join ()
		self.executorTeardown (ctx)
		agn.log (agn.LV_DEBUG, 'exec', 'Executor terminating')
	
	def enqueue (self, obj, oob = False):
		"""Put ``obj'' from controller to executor, in front of queue, when ``oob'' is set"""
		if self.queue.full () or self.backlog:
			if oob:
				self.backlog.appendleft (obj)
			else:
				self.backlog.append (obj)
		if not self.queue.full ():
			if self.backlog:
				while self.backlog and not self.queue.full ():
					self.queue.put (self.backlog.popleft ())
			else:
				self.queue.put (obj)

	def run (self, name = None):
		"""Entry point for starting process"""
		if name is None:
			name = agn.logname
		path = agn.mkpath (agn.base, 'scripts', '%s.cfg' % name)
		self.cfg = Config ()
		if os.path.isfile (path):
			self.cfg.read (path)
		self.queue = agn.multiprocessing.Queue ()
		self.backlog = collections.deque ()
		self.ctrl = agn.Parallel ()
		self.ctrl.fork (self.__controller, name = self.nameCtrl)
		self.ctrl.fork (self.__executor, name = self.nameExec)
		while self.running:
			time.sleep (1)
		self.ctrl.wait (timeout = 2.0)
		self.ctrl.term ()
#}}}
#
# EMail handling {{{
import	email, email.Charset
from	email.Message import Message
from	email.Header import Header
from	email.Utils import parseaddr
import	codecs, csv

class EMail (IDs):
	"""Create multipart E-Mails"""
	TO = 0
	CC = 1
	BCC = 2
	correctPattern = re.compile ('\n+[^ \t]', re.MULTILINE)
	namePattern = re.compile ('^([a-z][a-z0-9_-]*):', re.IGNORECASE)
	
	@classmethod
	def forceEncoding (cls, charset, encoding):
		"""Enforce an ``encoding'' for a definied ``charset'' overwriting system default behaviour"""
		try:
			csetname = charset.lower ()
			cset = email.Charset.CHARSETS[csetname]
			cencoding = {
				'quoted-printable': email.Charset.QP,
				'qp': email.Charset.QP,
				'base64': email.Charset.BASE64,
				'b64': email.Charset.BASE64
			}[encoding.lower ()]
			email.Charset.CHARSETS[csetname] = (cencoding, cencoding, cset[2])
		except KeyError, e:
			raise agn.error ('Invalid character set or encoding: %s' % str (e))
	
	class Content (object):
		"""Stores one part of a multipart message"""
		def __init__ (self, content, charset, contentType):
			self.content = content
			self.charset = charset
			self.contentType = contentType
			self.related = []

		def setMessage (self, msg, charset):
			msg.set_type (self.contentType)
			msg.set_payload (self.content, self.charset if self.charset is not None else charset)

	class Attachment (Content):
		"""Stores an attachemnt as part of a multipart message"""
		def __init__ (self, content, charset, contentType, filename):
			EMail.Content.__init__ (self, content, charset, contentType)
			self.filename = filename

		def setMessage (self, msg, charset):
			ct = self.contentType
			if self.filename:
				ct += '; name="%s"' % self.filename
			if self.charset:
				ct += '; charset="%s"' % self.charset
			msg['Content-Type'] = ct
			if self.charset:
#				msg['Content-Transfer-Encoding'] = '8bit'
				content = self.content
			else:
				msg['Content-Transfer-Encoding'] = 'base64'
				content = base64.encodestring (self.content)
			if self.filename:
				msg['Content-Description'] = self.filename
				msg['Content-Location'] = self.filename
				msg['Content-ID'] = '<%s>' % self.filename
			msg.set_payload (content, self.charset)

	def __init__ (self):
		super (EMail, self).__init__ ()
		self.companyID = None
		self.mfrom = None
		self.sender = None
		self.receivers = []
		self.subject = None
		self.headers = []
		self.content = []
		self.charset = None
		self.attachments = []
		try:
			self.host = socket.getfqdn ()
		except Exception:
			self.host = None
		pw = self.getUser ()
		if pw is not None:
			self.user = pw.pw_name
		else:
			self.user = None
		if self.user and self.host:
			self.sender = '%s@%s' % (self.user, self.host)

	def setCompanyID (self, companyID):
		"""Set companyID, used when signing the message"""
		self.companyID = companyID

	def setEnvelope (self, mfrom):
		"""Set the envelope from address"""
		self.mfrom = mfrom
	setMFrom = setEnvelope
	
	def setSender (self, sender):
		"""Set the sender of for the mail"""
		self.sender = sender
	setFrom = setSender
	
	def addReceiver (self, recv):
		"""Add a receiver for the mail"""
		self.receivers.append ((self.TO, recv))
	addTo = addReceiver
	
	def addCc (self, recv):
		"""Add a carbon copy receiver for the mail"""
		self.receivers.append ((self.CC, recv))
	
	def addBcc (self, recv):
		"""Add a blind carbon copy receiver for the mail"""
		self.receivers.append ((self.BCC, recv))
	
	def resetRecipients (self):
		"""Clears all receivers of the mail"""
		self.receivers = []
	
	def setSubject (self, subj):
		"""Set the content of the subject header"""
		self.subject = subj
	
	def addHeader (self, head):
		"""Add a header"""
		self.headers.append (head)
	
	def resetHeader (self):
		"""Clears all definied header"""
		self.headers = []
	
	def addContent (self, content, charset, contentType):
		"""Add ``content'' (str), store it using ``charset'' and mark it of type ``contentType''"""
		content = self.Content (content, charset, contentType)
		self.content.append (content)
		return content
	
	def resetContent (self):
		"""Clearts all parts of the multipart message"""
		self.content = []
		
	def setText (self, text, charset = None):
		"""Add a plain ``text'' variant for the mail using ``charset''"""
		return self.addContent (text, charset, 'text/plain')
	
	def setHTML (self, html, charset = None):
		"""Add a ``html'' variant for the mail using ``charset''"""
		return self.addContent (html, charset, 'text/html')

	def setCharset (self, charset):
		"""Set global ``charset'' to be used for this mail"""
		self.charset = charset

	def __contentType (self, contentType, filename, default):
		if contentType is None and filename is not None:
			contentType = mimetypes.guess_type (filename)[0]
		if contentType is None:
			contentType = default
		return contentType
			
	def addTextAttachment (self, content, charset = None, contentType = None, filename = None, related = None):
		"""Add a textual attachment"""
		if charset is None:
			charset = 'ISO-8859-1'
		contentType = self.__contentType (contentType, filename, 'text/plain')
		at = self.Attachment (content, charset, contentType, filename)
		if related is not None:
			related.related.append (at)
		else:
			self.attachments.append (at)
		return at
	
	def addBinaryAttachment (self, content, contentType = None, filename = None, related = None):
		"""Add a binary attachment"""
		contentType = self.__contentType (contentType, filename, 'application/octet-stream')
		at = self.Attachment (content, None, contentType, filename)
		if related is not None:
			related.related.append (at)
		else:
			self.attachments.append (at)
		return at
	
	def addExcelAttachment (self, content, filename, related = None):
		"""Add an excel sheet binary representation attachement"""
		return self.addBinaryAttachment (content, contentType = 'application/vnd.ms-excel', filename = filename, related = related)
	
	def resetAttachment (self):
		"""Clears all attachments"""
		self.attachments = []
	
	def __cleanupHeader (self, head):
		valid = False
		name = None
		head = head.replace ('\r\n', '\n')
		while len (head) > 0 and head[-1] == '\n':
			head = head[:-1]
		while True:
			mtch = self.correctPattern.search (head)
			if mtch is None:
				break
			(start, end) = mtch.span ()
			head = head[:start] + '\n\t' + head[end - 1:]
		mtch = self.namePattern.match (head)
		if not mtch is None:
			name = (mtch.groups ()[0]).lower ()
			valid = True
		return (valid, name, head)
	
	def __finalizeHeader (self):
		headers = []
		availHeaders = set ()
		for head in self.headers:
			(valid, name, header) = self.__cleanupHeader (head)
			if valid and not name.startswith ('content-') and not name in ('mime-version'):
				headers.append (header)
				availHeaders.add (name)
		if not 'from' in availHeaders and self.sender:
			headers.append ('From: %s' % self.sender)
		for (hid, sid) in [('to', self.TO), ('cc', self.CC)]:
			if not hid in availHeaders:
				recvs = [_r[1] for _r in self.receivers if _r[0] == sid]
				if recvs:
					headers.append ('%s: %s' % (hid.capitalize (), ', '.join (recvs)))
		if not 'subject' in availHeaders and self.subject:
			headers.append ('Subject: %s' % self.subject)
		if self.charset is None:
			charset = 'ISO-8859-15'
		else:
			charset = self.charset
		nheaders = []
		for header in headers:
			(name, value) = header.split (':', 1)
			try:
				codecs.decode (value, 'ascii')
				nheaders.append (header)
			except UnicodeDecodeError:
				try:
					nheaders.append ('%s: %s' % (name, str (Header (value, charset).encode ())))
				except UnicodeDecodeError as e:
					agn.log (agn.LV_WARNING, 'header', 'Failed to encode "%s": %s' % (header, e))
					nheaders.append (header)
		return (nheaders, charset)
	
	def buildMail (self):
		"""Build the multipart mail and return it as a string"""
		(headers, charset) = self.__finalizeHeader ()
		root = Message ()
		for header in headers:
			(name, value) = header.split (':', 1)
			root[name] = value.strip ()
		msgs = []
		parts = []
		if len (self.content) == 1:
			if not self.attachments:
				parts.append ((root, self.content[0]))
			else:
				msg = Message ()
				msgs.append (msg)
				root.attach (msg)
				parts.append ((msg, self.content[0]))
		else:
			if self.content:
				if self.attachments:
					parent = Message ()
					msgs.append (parent)
					root.attach (parent)
				else:
					parent = root
				parent.set_type ('multipart/alternative')
				for content in self.content:
					msg = Message ()
					msgs.append (msg)
					parts.append ((msg, content))
					if content.related:
						base = Message ()
						msgs.append (base)
						base.set_type ('multipart/related')
						base.attach (msg)
						for related in content.related:
							r = Message ()
							msgs.append (r)
							parts.append ((r, related))
							base.attach (r)
						parent.attach (base)
					else:
						parent.attach (msg)
		for (msg, content) in parts:
			content.setMessage (msg, charset)
		if self.attachments:
			root.set_type ('multipart/related')
			for attachment in self.attachments:
				at = Message ()
				msgs.append (at)
				root.attach (at)
				attachment.setMessage (at, None)
		for msg in msgs:
			del msg['MIME-Version']
		rc = root.as_string (False) + '\n'
		return rc
	
	def sendMail (self):
		"""Build and send the mail"""
		(status, returnCode, out, err) = (False, 0, None, None)
		mail = self.buildMail ()
		if self.mfrom is not None:
			mfrom = self.mfrom
		elif self.sender:
			mfrom = parseaddr (self.sender)[1]
		else:
			mfrom = None
		recvs = [parseaddr (_r[1])[1] for _r in self.receivers]
		sendmail = agn.which ('sendmail')
		if not sendmail:
			sendmail = '/usr/sbin/sendmail'
		cmd = [sendmail]
		if mfrom:
			cmd += ['-f', mfrom]
		cmd += ['--'] + recvs
		pp = subprocess.Popen (cmd, stdin = subprocess.PIPE, stdout = subprocess.PIPE, stderr = subprocess.PIPE)
		(out, err) = pp.communicate (mail)
		returnCode = pp.returncode
		if returnCode == 0:
			status = True
		return (status, returnCode, out, err)

class CSVEMail:
	"""Create a mail using a CSV content

An instance of this class must be created passing all relevant
content. These are the ``sender'', a list of ``receivers'', the
``text'' part of the mail, the iterable ``data'' which is used as the
source for the csv content, the ``filename'' of the csv attachment,
the ``charset'' of the csv content and the csv ``dialect'' to be used."""
	def __init__ (self, sender, receivers, subject, text, data, filename = None, charset = None, dialect = 'agn-semicolon-full'):
		mail = EMail ()
		if sender is not None:
			mail.setSender (sender)
		if type (receivers) in (str, unicode):
			receivers = [_r.strip () for _r in receivers.split (',')]
		for r in receivers:
			mail.addReceiver (r)
		if subject is not None:
			mail.setSubject (subject)
		if charset:
			mail.setCharset (charset)
		if text:
			mail.setText (text)
		self.data = ''
		excel = csv.writer (self, dialect = dialect)
		for row in data:
			excel.writerow (row)
		mail.addTextAttachment (self.data, charset, contentType = 'text/csv', filename = filename)
		self.status = mail.sendMail ()
			
	def write (self, s, n = None):
		"""Hook for the csv writing instance"""
		if not n is None and n > len (s):
			n = None
		if not n is None:
			self.data += s[:n]
			rc = n
		else:
			self.data += s
			rc = len (s)
		return rc

class ParseEMail (object):
	"""Parse an EMail to identify recipient

If an EMail contains agnUIDs, this class can be used to parse the
EMail and try to find these IDs and resolve them to the related
customer.

This class can be subclassed and the method parse() can be
overwritten to implement further logic for resolving the customer."""
	def __init__ (self, content, invalids = False):
		"""Parses EMail found in ``content''"""
		self.content = content
		self.invalids = invalids
		self.message = email.message_from_string (self.content)
		self.uids = None
		self.nvuids = None
		self.licence = None
		self.ignore = False
		self.unsubscribe = False
		self.status = []
		self.feedback = []
	
	def parsedUID (self, uid):
		u = UID ()
		u.parseUID (uid)
		u.done ()
		return u
	
	def parsedNonValidUID (self, uid):
		u = agn.UID ()
		u.parseUID (uid, None, False)
		return u
		
	def __addUID (self, uid):
		try:
			self.uids.append (self.parsedUID (uid))
		except agn.error as e1:
			if self.invalids:
				try:
					self.nvuids.append (self.parsedNonValidUID (uid))
				except agn.error as e2:
					agn.log (agn.LV_DEBUG, 'parseEMail', 'Unparsable UID "%s" found: %s/%s' % (uid, e1, e2))
			else:
				agn.log (agn.LV_DEBUG, 'parseEMail', 'Unparsable UID "%s" found: %s' % (uid, e1))
	
	patternLink = re.compile ('https?://[^/]+/[^?]*\\?.*uid=(3D)?([0-9a-z_-]+(\\.[0-9a-z_-]+){5,7})', re.IGNORECASE)
	patternMessageID = re.compile ('<(V[^-]*-)?([0-9]{14}_([0-9]+)(\\.[0-9a-z_-]+){6,7})@[^>]+>', re.IGNORECASE)
	patternSubject = re.compile ('unsubscribe:([0-9a-z_-]+(\\.[0-9a-z_-]+){5,7})', re.IGNORECASE)
	patternListUnsubscribeSeparator = re.compile ('<([^>]*)>')
	patternListUnsubscribe = re.compile ('mailto:[^?]+\\?subject=unsubscribe:([0-9a-z_-]+(\\.[0-9a-z_-]+){5,7})', re.IGNORECASE)
	def __find (self, s, pattern, position, callback = None):
		if s is not None:
			for m in pattern.finditer (s):
				if callable (callback):
					callback (m)
				self.__addUID (m.group (position))
	def __findLink (self, s):
		self.__find (s, self.patternLink, 2)
	def __findMessageID (self, s):
		def checkIgnore (m):
			self.ignore = m.group (1) is not None
		self.__find (s, self.patternMessageID, 2, callback = checkIgnore)
	def __findSubject (self, s):
		def setUnsubscribe (m):
			self.unsubscribe = True
		self.__find (s, self.patternSubject, 1, callback = setUnsubscribe)
	def __findListUnsubscribe (self, s):
		if s is not None:
			for elem in self.patternListUnsubscribeSeparator.findall (s):
				self.__findLink (elem)
				self.__find (elem, self.patternListUnsubscribe, 1)
			
	def __parseHeader (self, payload):
		for header in ['message-id', 'references', 'in-reply-to']:
			self.__findMessageID (payload[header])
		self.__findSubject (payload['subject'])
		self.__findListUnsubscribe (payload['list-unsubscribe'])
	
	def __parsePayload (self, payload, level):
		self.__parseHeader (payload)
		p = payload.get_payload (decode = True)
		if self.parsePayload (payload, p, level):
			if p:
				self.__findLink (p)
			else:
				target = None
				ct = payload.get_content_type ()
				if ct:
					ct = ct.lower ()
					if ct == 'message/feedback-report':
						target = self.feedback
					elif ct == 'message/delivery-status':
						target = self.status
				for p in payload.get_payload ():
					if target is not None:
						target.append (dict (p.items ()))
					elif type (p) in (str, unicode):
						self.__findLink (p)
					else:
						self.__parsePayload (p, level + 1)
		
	def parse (self):
		self.uids = []
		self.nvuids = []
		self.__parsePayload (self.message, 1)
	
	def getOrigins (self):
		"""Returns all found customer, sorted by their validity and frequency"""
		if self.uids is None:
			self.parse ()
		rc = []
		for (valid, uids) in [(False, self.nvuids), (True, self.uids)]:
			m = {}
			for u in uids:
				key = (u.customerID, u.mailingID, u.companyID, u.licenceID)
				try:
					m[key].count += 1
				except KeyError:
					temp = agn.mutable (valid = valid, customerID = u.customerID, mailingID = u.mailingID, companyID = u.companyID, licenceID = u.licenceID, count = 1)
					m[key] = temp
			rc += list (sorted (m.values (), key = lambda a: a.count))
		return rc
	
	def getOrigin (self):
		"""Returns the most probable customer found in the mail"""
		o = self.getOrigins ()
		if o:
			return o[-1]
		return None

	def parsePayload (self, message, content, level):
		"""Hook for custom parsing"""
		return True
	
class EMailValidator (object):
	"""Validate an EMail address

This class allows some basic checks against an email address. This
includes syntactical correctness as well as consulting available
blacklists."""
	special = '][%s\\()<>,::\'"' % ''.join ([chr (_n) for _n in range (32)])
	patternUser = re.compile ('^("[^"]+"|[^%s]+)$' % special, re.IGNORECASE)
	patternDomainPart = re.compile ('[a-z0-9][a-z0-9-]*', re.IGNORECASE)
	class Robinson (object):
		name = 'robinson at'
		def __init__ (self, hash):
			self.hashes = set ((hash[_p:_p + 20] for _p in xrange (0, len (hash), 20)))

		def __contains__ (self, email):
			checks = [email]
			with agn.Ignore (ValueError):
				(user, domain) = email.split ('@', 1)
				checks.append ('@%s' % domain)
			for check in checks:
				hasher = hashlib.new ('SHA1')
				hasher.update (check)
				if hasher.digest () in self.hashes:
					return True
			return False

	def __init__ (self):
		self.badDomains = None
		self.blacklists = None
	
	def __call__ (self, email, companyID = None):
		"""Validate an ``email'' address for ``companyID''"""
		self.valid (email, companyID)

	def setup (self, db = None, companyID = None):
		"""Setup processing ``db'' is an instance of eagn.DB or None for ``companyID''"""
		if self.badDomains is None or self.blacklists is None or (companyID is not None and companyID not in self.blacklists):
			localDB = db is None
			try:
				if db is None:
					db = DB ()
					if not db.isopen ():
						raise agn.error ('Failed to setup database: %s' % db.lastError ())
				if self.badDomains is None:
					self.badDomains = set ()
#
#################################################################
#	Blocked by https://jira.agnitas.de/browse/PROJ-672	#
#################################################################
#
#					table = 'domain_clean_tbl'
#					if db.exists (table):
#						cursor = db.request ()
#						for r in cursor.query ('SELECT bdomain FROM %s' % table):
#							if r[0]:
#								self.badDomains.add (r[0].lower ())
#						db.release (cursor)
				if self.blacklists is None:
					self.blacklists = {None: [], 0: self.__readBlacklist (db, 0)}
				if companyID is not None and companyID not in self.blacklists:
					self.blacklists[companyID] = self.__readBlacklist (db, companyID)
				ccfg = CompanyConfig (db.cursor)
				ccfg.read ()
				if companyID is not None:
					ccfg.setCompanyID (companyID)
				with agn.Ignore (KeyError):
					robinson = ccfg.getCompanyInfo ('blacklist-robinson-at')
					rq = db.cursor.querys ('SELECT valid, block0, block1 FROM %s' % robinson)
					if rq is not None and rq[0] in (0, 1):
						self.blacklists[None].append (self.Robinson (rq[rq[0] + 1].read ()))
			finally:
				if localDB and db is not None:
					db.done ()

	def __readBlacklist (self, db, companyID):
		rc = agn.mutable (plain = set (), wildcards = [])
		table = 'cust_ban_tbl' if companyID == 0 else 'cust%d_ban_tbl' % companyID
		if db.exists (table):
			seen = set ()
			cursor = db.request ()
			for r in cursor.query ('SELECT email FROM %s' % table):
				if r[0]:
					email = r[0].lower ()
					if email not in seen:
						seen.add (email)
						if '%' in email or '_' in email:
							pattern = agn.compileSQLwildcard (email)
							if pattern is not None:
								rc.wildcards.append ((email, pattern))
						else:
							rc.plain.add (email)
			db.release (cursor)
		return rc
	
	def valid (self, email, companyID = None):
		"""Validate an ``email'' address for ``companyID''"""
		self.setup (companyID = companyID)
		if not email:
			raise agn.error ('empty email')
		if not email.strip ():
			raise agn.error ('empty email (just whitespaces)')
		parts = email.split ('@')
		if len (parts) != 2:
			raise agn.error ('expect exactly one "@" sign')
		(user, domain) = parts
		self.validUser (user)
		self.validDomain (domain)
		self.checkBlacklist (email, companyID)
	
	def validUser (self, user):
		"""Validates the local part ``user''"""
		if not user:
			raise agn.error ('empty local part')
		if self.patternUser.match (user) is None:
			raise agn.error ('invalid local part')
	
	def validDomain (self, domain):
		"""Validates the ``domain'' part"""
		if not domain:
			raise agn.error ('emtpy domain')
		parts = domain.split ('.')
		if len (parts) == 1:
			raise agn.error ('missing TLD')
		if parts[-1] == '':
			raise agn.error ('empty TLD')
		if len (parts[-1]) < 2:
			raise agn.error ('too short TLD (minium two character expected)')
		for p in parts:
			if p == '':
				raise agn.error ('domain part is empty')
			if self.patternDomainPart.match (p) is None:
				raise agn.error ('invalid domain part "%s"' % p)
		if self.badDomains and domain.lower () in self.badDomains:
			raise agn.error ('typically mistyped domain detected')

	def checkBlacklist (self, email, companyID = None):
		if not self.blacklists:
			self.setup (companyID = companyID)
		if self.blacklists:
			email = email.lower ()
			companyIDs = [0]
			if companyID is not None and companyID > 0:
				companyIDs.append (companyID)
			for cid in companyIDs:
				if cid in self.blacklists:
					where = 'global' if cid == 0 else 'local'
					blacklist = self.blacklists[cid]
					if email in blacklist.plain:
						raise agn.error ('matches plain in %s blacklist' % where)
					for (wildcard, pattern) in blacklist.wildcards:
						if pattern.match (email) is not None:
							raise agn.error ('matches wildcard %s in %s blacklist' % (wildcard, where))
			for blacklist in self.blacklists[None]:
				if email in blacklist:
					raise agn.error ('matches %s blacklist' % blacklist.name)
#}}}
#
# Minimal plugin framework {{{
#
# for a full featured plugin system use the one found in aps.py
#
class Plugin (object):
	"""Simple plugin

This is a complete simple plugin if the whole feature and workload of the
full fledged plugin system is not required and the plugin code is provided
using strings. The usage is simple:

1.) create an instance of the class passing the code and optional name:

plugin = Plugin ('...')

2.) call a method from the plugin:

plugin.method ()

3.) done!

If the method is not found, a dummy method is called which does nothing
and returns None.

You can initial pass a prefilled namespace to enrich the namespace of the
running plugin.

During runtime, you can add global names into the namespace. To avoid
cluttering the namespace you first have to create a temporary context:
	
plugin ('push')

Then you can simple add or remove global names to/from the plugin:

plugin['someValue'] = 5
del plugin['someValue']
import sys
plugin['sys'] = sys

To remove the provided global names, simple dismiss the temporary context:

plugin ('pop')

Beware: you cannot overwrite existing names in the namespace!

You can subclass this class and implement the method "catchall" which must
return a function which takes a variable number of arguments. "catchall"
itself is only called with the name of the method which is not implemented
itself.
"""
	__slots__ = ['_ns', '_st', '_ca']
	def __init__ (self, code, name = None, ns = None):
		"""Create a plugin using ``code'' for ``name'' using namespace ``ns''"""
		if ns is None:
			self._ns = {}
		else:
			self._ns = ns.copy ()
		self._st = []
		if 'catchall' in self.__class__.__dict__ and callable (self.__class__.__dict__['catchall']):
			self._ca = self.catchall
		else:
			self._ca = lambda name: lambda *args, **kws: None
		if name is None: name = '*unset*'
		compiled = compile (code, name, 'exec')
		exec compiled in self._ns
	
	def __call__ (self, command):
		"""Control interface for plugin class itself"""
		if command == 'push':
			self._st.append (set ())
		elif command == 'pop':
			if self._st:
				for token in self._st.pop ():
					del self._ns[token]

	def __setitem__ (self, option, value):
		"""Set namespace content"""
		if self._st:
			ns = self._st[-1]
			if option not in self._ns or option in ns:
				self._ns[option] = value
				ns.add (option)
	
	def __delitem__ (self, option):
		"""Delete namespace content"""
		if self._st:
			ns = self._st[-1]
			if option in ns:
				del self._ns[option]
				ns.remove (option)

	def __getattr__ (self, name):
		"""Call plugin method"""
		if name in self._ns and callable (self._ns[name]):
			return self._ns[name]
		return self._ca (name)
#}}}
#
# Configuration file handling {{{
class Unit (object):
	"""Parse a unit expression

Class to parse an expression containing common units. Custom unit
convertion values can be added. Examples:
unit = eagn.Unit ()
unit.parse ('3m30') -> 210
unit.parse ('2K 512B') -> 2560"""
	__eparse = re.compile ('([+-]?[0-9]+)([a-z]*)[ \t]*', re.IGNORECASE)
	__eunit = {
		's':	1,				# second
		'm':	60,				# minute
		'h':	60 * 60,			# hour
		'd':	24 * 60 * 60,			# day
		'w':	7 * 24 * 60 * 60,		# week
		
		'B':	1,				# byte
		'K':	1024,				# kByte
		'M':	1024 * 1024,			# MByte
		'G':	1024 * 1024 * 1024,		# GByte
		'T':	1024 * 1024 * 1024 * 1024,	# TByte
	}
	def __init__ (self, **kws):
		"""Further conversion units can be passed as keywords"""
		self.eunit = self.__eunit.copy ()
		for (key, mult) in kws.items ():
			self.eunit[key] = mult
		
	def __getitem__ (self, key):
		"""Returns unit value for ``key''"""
		return self.eunit[key]
	
	def __setitem__ (self, key, mult):
		"""Sets a unit value ``mult'' for unit ``key''"""
		self.eunit[key] = mult
	
	def parse (self, s, dflt = 0):
		"""Parses a unit string"""
		if s is None:
			rc = dflt
		elif type (s) is float:
			rc = int (s)
		elif type (s) in (int, long):
			rc = s
		else:
			rc = None
			scan = self.__eparse.scanner (s)
			try:
				while True:
					m = scan.search ()
					if m is None:
						break
					(i, u) = m.groups ()
					if u:
						mult = self.eunit[u]
					else:
						mult = 1
					val = int (i) * mult
					if rc is None:
						rc = val
					else:
						rc += val
			except ValueError:
				rc = dflt
		return rc

class Config (object):
	"""Configuration file handling

This class provides a complex handling for configuration file in INI
style. A configuration file is typically build out of a global section
and other sections with configuration parameter, e.g.:

param1 = value1
param2 = value2
[section1]
param2 = value3
param4 = value4
[section2]
param5 = value5

NOTE: Section names starting and ending with two underscores are not
allowed to be used by an application, these names are (like in python)
reserverd words for internal usage!

In general a variable is referenced by its section name, followed by a
dot and the parameter name. If the section is omited then the global
section is used. If a section is used and the parameter could not be
found in this section, the global section is consulted for that value,
e.g. related to the sample above:

- param2 -> value2
- section1.param2 -> value3
- section2.param2 -> value2
- section2.param4 -> *not existing*

The application may use different methods to retrieve a value which
allowes an interpretation of the value by this class. See the various
*get methods how they interpret the value. These returns a default
value if the parameter is not existing. One can also access the
parameter using a dict like syntax where a missing parameter results
in a KeyError exception.

A simple substitution logic can optional be enabled so you can
reference other configuration values using $section.parameter or
${section.parameter}. If the configuration files has this content:

location = nowhere
place = There is no place like $location
[home]
location = home place

results to
- place -> There is no place like nowhere
- home.place -> There is no place like home place
"""
	commentPattern = re.compile ('^[ \t]*(#.*)?$')
	sectionPattern = re.compile ('^\\[([^]]+)\\]')
	includePattern = re.compile ('^([^(]+)\\(([^)]*)\\)$')
	commandPattern = re.compile ('^:([a-z]+)([ \t]+(.*))?$')
	dataPattern = re.compile ('^[ \t]*([^ \t=]+)[ \t]*=[ \t]*(.*)$')
	def __init__ (self, fallback = None, overwrite = None, defaultSection = None):
		"""``fallback'' allows to chain multiple instances, so
if a key is not found in this instance, ``fallback'' is consulted for
the value. ``overwrite'' can also be an instance which will be
consulted before the own data of this instance. With
``defaultSection'' one can select a section to be used if a
parametername has no section part."""
		self.sections = None
		self.fallback = fallback
		self.overwrite = overwrite
		self.defaultSection = defaultSection
		self.defaultSectionStack = []
		self.sectionSequence = []
		self.ns = {}
		self.lang = None
		self.mc = None
		self.unit = Unit ()
		self.clear ()
		self.getter = self.__get
	
	def setupNamespace (self, **kws):
		"""Setup the namespace used for tget method. Some
standard values are set for the namespace:
	- now: current date as time.struct_time
	- today: current date as datetime.datetime

This method also copies the enviroment of the process and any passed
keyword argument to the namespace. To further populate you can access
the namespace directly using the ``ns'' attribute."""
		self.ns['now'] = time.localtime ()
		self.ns['today'] = datetime.datetime.today ()
		for (var, val) in os.environ.items ():
			self.ns[var] = val
		for (var, val) in kws.items ():
			self.ns[var] = val
	
	def setupDate (self, offset, name = None, default = None):
		"""Setup an additional datetime.datetime object for
the namespace. ``offset'' is the offset in days for the current day
(int) or as a key for the configuration file (str) where ``default''
is used as the default value if there is no value found in the current
configuration. ``name'' is the name to use for the namespace entry or
None to use the default "date"."""
		if name is None:
			name = 'date'
		if type (offset) in (str, unicode):
			offset = self.iget (offset, default)
		try:
			now = self.ns['today']
		except KeyError:
			now = datetime.datetime.now ()
		if offset:
			then = now.fromordinal (now.toordinal () - offset)
			then = datetime.datetime (then.year, then.month, then.day, now.hour, now.minute, now.second)
		else:
			then = now
		self.ns[name] = then
	
	def enableSubstitution (self):
		"""enable value substitution"""
		self.getter = self.__vget

	def disableSubstitution (self):
		"""disable value substitution"""
		self.getter = self.__get
	
	def __parseVar (self, var):
		parts = var.split ('.', 1)
		if len (parts) == 2:
			sections = [parts[0]]
			var = parts[1]
		else:
			sections = [self.defaultSection] + self.defaultSectionStack
		return (var, sections)
	
	def __get (self, var):
		if not self.overwrite is None:
			with agn.Ignore (KeyError):
				return self.overwrite[var]
		(bvar, sections) = self.__parseVar (var)
		for section in sections:
			with agn.Ignore (KeyError):
				return self.sections[section][bvar]
		if sections[-1] is not None:
			with agn.Ignore (KeyError):
				return self.sections[None][bvar]
		if self.fallback is not None:
			return self.fallback[var]
		raise KeyError (var)

	__pattern = re.compile ('\\$(\\$|[a-z0-9_-]+(\\.[a-z0-9_-]+)?|{([^}]*)})', re.IGNORECASE)
	def __rget (self, var, seen):
		val = self.__get (var)
		cur = 0
		while cur < len (val):
			m = self.__pattern.search (val, cur)
			if m is None:
				break
			#
			(start, end) = m.span ()
			g = m.groups ()
			var = g[2] if g[2] is not None else g[0]
			if var == '$':
				rplc = '$'
			elif var not in seen:
				copy = seen.copy ()
				copy.add (var)
				try:
					rplc = self.__rget (var, copy)
				except KeyError:
					rplc = None
			else:
				rplc = None
			if rplc is not None:
				val = val[:start] + rplc + val[end:]
				cur = start + len (rplc)
			else:
				cur = end
		return val

	def __vget (self, var):
		return self.__rget (var, set ())
		
	def __getitem__ (self, var):
		return self.getter (var)
	
	def __setitem__ (self, var, val):
		(var, sections) = self.__parseVar (var)
		try:
			self.sections[sections[0]][var] = val
		except KeyError:
			self.sections[sections[0]] = {var: val}
			if sections[0] is not None:
				self.sectionSequence.append (sections[0])
	
	def __contains__ (self, var):
		try:
			self[var]
		except KeyError:
			return False
		return True
	
	def push (self, section):
		"""Use ``section'' as the new default section, keep previous for pop()"""
		self.defaultSectionStack.insert (0, self.defaultSection)
		self.defaultSection = section
	
	def pop (self):
		"""Pop the last pushed section and use the previous default section"""
		if not self.defaultSectionStack:
			return None
		rc = self.defaultSection
		self.defaultSection = self.defaultSectionStack.pop (0)
		return rc
	
	def get (self, var, dflt = None):
		"""Retrieve the value for ``var'' as string, use ``dflt'' as default if ``var'' is not found"""
		try:
			val = self[var]
		except KeyError:
			val = dflt
		return val
	
	def iget (self, var, dflt = 0):
		"""Retrieve the value for ``var'' as integer, use ``dflt'' as default if ``var'' is not found"""
		try:
			val = int (self[var])
		except (KeyError, ValueError, TypeError):
			val = dflt
		return val
	
	def eget (self, var, dflt = 0):
		"""Retrieve the value for ``var'' as integer where the value is parsed from a unit string, use ``dflt'' as default if ``var'' is not found"""
		return self.unit.parse (self.get (var), dflt)
	
	def fget (self, var, dflt = 0.0):
		"""Retrieve the value for ``var'' as float, use ``dflt'' as default if ``var'' is not found"""
		try:
			val = float (self[var])
		except (KeyError, ValueError, TypeError):
			val = dflt
		return val
	
	def bget (self, var, dflt = False):
		"""Retrieve the value for ``var'' as boolean, use ``dflt'' as default if ``var'' is not found"""
		try:
			val = agn.atob (self[var])
		except KeyError:
			val = dflt
		return val
	
	def tget (self, var, dflt = None, **kws):
		"""Retrieve the value for ``var'' as str where the value is used as a template which is filled using the current namespace, use ``dflt'' as default if ``var'' is not found"""
		val = self.get (var, dflt)
		if val is not None:
			tmpl = agn.Template (val)
			if kws:
				if self.ns:
					ns = self.ns.copy ()
					for (key, value) in kws.items ():
						ns[key] = value
				else:
					ns = kws
			else:
				ns = self.ns
			val = tmpl.fill (ns, lang = self.lang, mc = self.mc)
		return val

	__dateParser = agn.ParseTimestamp ()
	def __dateparse (self, s):
		rc = self.__dateParser (s)
		if rc is None:
			raise ValueError ('unparsable date exprssion %r' % (s, ))
		return rc
		
	def dget (self, var, dflt = None):
		"""Retrieve the value for ``var'' as datetime.datetime, use ``dflt'' as default if ``var'' is not found"""
		try:
			val = self.__dateparse (self[var])
		except (KeyError, ValueError):
			if type (dflt) in (str, unicode):
				try:
					val = self.__dateparse (dflt)
				except ValueError:
					val = None
			else:
				val = dflt
		return val

	def __listsplit (self, val, listSeparator, modify, modifyDefault, notNull, notEmpty):
		if type (val) is list:
			rc = val
		elif type (val) is tuple:
			rc = list (val)
		else:
			if listSeparator is None: listSeparator = ', *'
			if modify is None: modify = string.strip
			if notNull is None: notNull = False
			if notEmpty is None: notEmpty = False
			if val:
				lsep = re.compile (listSeparator, re.MULTILINE)
				rc = lsep.split (val)
				if rc and notEmpty:
					rc = [_v for _v in rc if _v]
				if rc and modify is not None and callable (modify):
					def modifier (v):
						try:
							return modify (v)
						except:
							return modifyDefault
					rc = [modifier (_v) for _v in rc]
				if rc and notNull:
					rc = [_v for _v in rc if _v is not None]
			else:
				rc = []
		return rc
		
	def lget (self, var, dflt = None, listSeparator = None, modify = None, modifyDefault = None, notNull = None, notEmpty = None):
		"""Retrieve the value for ``var'' as list, use ``dflt'' as default if ``var'' is not found

``listSeparater'' is the regex to separate each element of the list
(default ", *", a comma separated list with option spaces after the
comma), ``modify'' can be a callable which is is called to modify each
element, ``modifyDefault'' is used for an element, if ``modify''
raises an exception. ``notNull'' removes all elements from the which
are None, ``notEmpty'' removes all elements from the list, which bool
value is False."""
		return self.__listsplit (self.get (var, dflt), listSeparator, modify, modifyDefault, notNull, notEmpty)
	
	def mget (self, var, dflt = None, recordSeparator = None, keySeparator = None, keyConvert = None, valueConvert = None):
		"""Retrieve the value for ``var'' as dict, use ``dflt'' as default if ``var'' is not found

``recordSeparator''is the regex to separate each key/value pair
(default newlines), ``keySeparator'' is the regex to separate the key
from its value (default ": *"). ``keyConvert'' and ``valueConvert''
are optional callables which are used to convert the key respective
the value before storing them to the dict."""
		if recordSeparator is None: recordSeparator = '(\n\r?)+'
		if keySeparator is None: keySeparator = ': *'
		if keyConvert is None: keyConvert = lambda k: k.strip ()
		if valueConvert is None: valueConvert = lambda k, v: v
		val = self.get (var, dflt)
		rc = {}
		if val:
			rsep = re.compile (recordSeparator, re.MULTILINE)
			ksep = re.compile (keySeparator, re.MULTILINE)
			for r in rsep.split (val):
				e = ksep.split (r, 1)
				if len (e) == 2:
					value = e[1].strip ()
					if len (value) >= 2 and value[0] in '\'"' and value[0] == value[-1]:
						value = value[1:-1]
					rc[keyConvert (e[0])] = valueConvert (e[0], value)
		return rc
	
	def mlget (self, var, dflt = None, recordSeparator = None, keySeparator = None, keyConvert = None, valueConvert = None,
		listSeparator = None, modify = None, modifyDefault = None, notNull = None, notEmpty = None
	):
		"""Retrieve the value for ``var'' as dict with list as values, use ``dflt'' as default if ``var'' is not found

this is a combination of mget and lget which results in a dict where
the values are parsed as list. For description of the parameter,
consult mget and lget repective."""
		rc = self.mget (var, dflt, recordSeparator, keySeparator, keyConvert, valueConvert)
		for (var, val) in rc.items ():
			rc[var] = self.__listsplit (val, listSeparator, modify, modifyDefault, notNull, notEmpty)
		return rc

	def csvget (self, var, dflt = None, dialect = None):
		"""Retrieve the value for ``var'' as a CSV and parse it into a two dimensional array. Use ``dflt'' if ``var is not found"""
		try:
			buf = StringIO.StringIO (self[var])
			rd = agn.CSVReader (buf, dialect if dialect else agn.CSVDefault)
			rc = list (rd)
			rd.close ()
		except KeyError:
			rc = dflt
		return rc
		
	def __pget (self, code, name, ns, plugin):
		if code is not None:
			if plugin is None:
				plugin = Plugin
			return plugin (code, name, ns)
		return None
	def pget (self, var, dflt = None, name = None, ns = None, plugin = None):
		"""Retrieve the value for ``var'' as a plugin, use ``dflt'' as default if ``var'' is not found

``name'' and ``ns'' are passed to Plugin() if a new copy is created,
otherwise ``plugin'' is used. The application using this method can
then use the returned object to call functions from this plugin."""
		return self.__pget (self.get (var, dflt), name, ns, plugin)
	def tpget (self, var, dflt = None, name = None, ns = None, plugin = None, **kws):
		"""Retrieve the value for ``var'' as a plugin after tempalting, use ``dflt'' as default if ``var'' is not found

like pget(), but uses the tget() instead of get() to retrieve the
plugin code to prefill it using its own namespace."""
		return self.__pget (self.tget (var, dflt, **kws), name, ns, plugin)
	
	def dbget (self, var, dflt = None, dialect = None, output = None, callback = None, null = None, target = None):
		"""Retrieve the value for ``var'' as a database, use ``dflt'' as default if ``var'' is not found

the retrieved value for ``var'' is interpreted as a csv representation
for ``dialect''. The first line must be a header line where the name
is separated by space from its type. These types are currently
supported:
	- integer: int
	- real: float
	- timestamp: datetime.datetime
	- binary: base64 coded binary data
	- anything else: str

The ``output'' can be one of these to define the
returned value:
	- list: a list of each row where each row is list
	- dict: a list of each row where each row is dict
	- mutable: like dict, but as an agn.mutable
	- json: a json object representing the dict version

Internally the data is prepared using a sqlite3 in memory database. If
``callback'' is set to a callable this function is called with three
arguments:
	- rc: the agn.DBSQLite3 instance
	- table: the name of the table where the data is stored
	- fields: the name of the columns in the table

This ``callback'' can be used to modify the data before being
converted to its output format.

If ``null'' is not None and a value is equals to this string, then
NULL is written to the database.

If ``target'' is a pathname then this is used as the sqlite3 database
instead of using an in memory database. """
		rc = None
		table = None
		fields = None
		value = self.get (var, dflt)
		if value is not None:
			cursor = None
			insert = None
			timeParse = agn.ParseTimestamp ()
			def converter (typ):
				if typ == 'integer':
					return lambda a: int (a) if a else None
				elif typ == 'real':
					return lambda a: float (a) if a else None
				elif typ == 'timestamp':
					return lambda a: timeParse (a) if a else None
				elif typ == 'binary':
					return lambda a: agn.DBSqlite3.Binary (base64.decodestring (a)) if a else None
				else:
					return lambda a: unicode (str (a), 'UTF-8') if a is not None else None
			rd = agn.CSVReader (StringIO.StringIO (value.strip ()), agn.CSVDefault if dialect is None else dialect)
			for (lineno, row) in enumerate (rd):
				if rc is None:
					table = var.split ('.')[-1]
					fields = [_r.split ()[0] for _r in row]
					create_table = 'CREATE TABLE %s (%s)' % (table, ', '.join (row))
					convert = [converter (_r.split ()[1]) for _r in row]
					rc = agn.DBSQLite3 (':memory:' if target is None else target, extendedTypes = True, textType = 'unicode', extendedFunctions = True)
					cursor = rc.cursor ()
					cursor.mode ('fast')
					cursor.execute (create_table)
					insert = 'INSERT INTO %s (%s) VALUES (%s)' % (table, ', '.join (fields), ', '.join (['?'] * len (fields)))
				elif len (row) != len (fields):
					raise agn.error ('%s:%d: expect %d values, got %d' % (table, lineno, len (fields), len (row)))
				else:
					if null is not None:
						row = [_r if _r != null else None for _r in row]
					try:
						cursor.execute (insert, [_c (_r) for (_c, _r) in zip (convert, row)])
					except Exception as e:
						raise agn.error ('%s:%d: failed processing row: %s' % (table, lineno, e))
			if cursor is not None:
				cursor.sync ()
				cursor.close ()
			rd.close ()
		if rc is not None and callback is not None and callable (callback):
			callback (rc, table, fields)
		if rc is not None and output is not None:
			query = ('SELECT %s FROM %s ORDER BY rowid' % (', '.join (fields), table))
			cursor = rc.cursor ()
			try:
				rows = []
				for row in cursor.query (query):
					rows.append (row)
			finally:
				cursor.close ()
			rc.close ()
			rc = None
			if output in ('dict', 'mutable', 'struct', 'json'):
				nrc = []
				for row in rows:
					nrc.append (dict (zip (fields, row)))
				if output == 'json':
					rc = json.dumps (nrc)
				elif output == 'mutable' or output == 'struct':
					rc = [agn.mutable (**_r) for _r in nrc]
				else:
					rc = nrc
			elif output == 'list':
				rc = rows
		return rc
	
	def keys (self):
		return iter (agn.Stream.of (self.sections.iteritems ())
			.map (lambda kv: (('%s.%s' % (kv[0], _v)) if kv[0] is not None else _v for _v in kv[1]))
			.chain ()
		)

	__internal = re.compile ('^__(.*)__$')
	def getSections (self):
		"""Retrieves a list of all known sections"""
		return [_k for _k in self.sections.keys () if _k is not None and self.__internal.match (_k) is None]
	
	def getSectionSequence (self):
		"""Retrieves a list of all known sections in the sequence they are created"""
		return [_s for _s in self.sectionSequence if self.__internal.match (_s) is None]
	
	def getSection (self, section):
		"""Get a complete ``section'' as a dict, no conversion of values take place"""
		if section in self.sections:
			rc = self.sections[section].copy ()
		else:
			rc = {}
		if self.overwrite:
			with agn.Ignore (KeyError):
				ov = self.overwrite.getSection (section)
				for (var, val) in ov.items ():
					rc[var] = val
		if self.fallback:
			with agn.Ignore (KeyError):
				fb = self.fallback.getSection (section)
				for (var, val) in fb.items ():
					if var not in rc:
						rc[var] = val
		return rc
	
	def getSectionAsMutable (self, section):
		"""Get a complete ``section'' as an agn.mutable, no conversion of values take place"""
		return agn.mutable (**self.getSection (section))
	getSectionAsStruct = getSectionAsMutable
	
	def getSectionAsConfig (self, section):
		"""Get a complete ``section'' as a new instance of this class, no conversion of values take place"""
		data = self.getSection (section)
		rc = Config (self)
		for (var, val) in data.items ():
			rc.sections[None][var] = val
		rc.ns = self.ns.copy ()
		return rc
	
	def clear (self):
		"""Purge all configurations"""
		self.sections = {None: {}}
		self.sectionSequence = []
	
	def filename (self):
		"""Returns the default filename for the configuration"""
		return agn.mkpath (agn.base, 'scripts', '%s.cfg' % agn.logname)
		
	def write (self, fname):
		"""Write the configuration to ``fname''"""
		fd = open (fname, 'w')
		for section in sorted (self.sections):
			block = self.sections[section]
			if section is None:
				section = '*'
			if block:
				fd.write ('[%s]\n' % section)
				for key in sorted (block):
					value = block[key]
					if '\n' in value:
						if value[-1] == '\n':
							value = value[:-1]
						fd.write ('%s = {\n%s\n}\n' % (key, value))
					else:
						fd.write ('%s = "%s"\n' % (key, value))
		fd.close ()

	def read (self, fname = None, filedesc = None):
		"""Read configuration from ``fname'' or ``filedesc'' (a file like object)"""
		cur = self.sections[None]
		block = None
		if filedesc is not None:
			fd = filedesc
		else:
			if fname is None:
				fname = self.filename ()
			fd = open (fname, 'r')
		fds = []
		splitter = re.compile (', *')
		while not fd is None:
			line = fd.readline ()
			if line == '':
				block = None
				if filedesc is None or fd != filedesc:
					fd.close ()
				try:
					fd = fds.pop (0)
				except IndexError:
					fd = None
			else:
				line = line.rstrip ('\r\n')
				if block is not None:
					if line.startswith ('}') and line.strip () == '}':
						block = None
					else:
						cur[block] += '%s\n' % line
				elif line and self.commentPattern.match (line) is None:
					mtch = self.sectionPattern.match (line)
					if not mtch is None:
						sname = mtch.groups ()[0]
						mtch = self.includePattern.match (sname)
						if mtch is not None:
							(sname, includes) = mtch.groups ()
						else:
							includes = None
						if sname == '*':
							sname = None
						try:
							cur = self.sections[sname]
						except KeyError:
							cur = {}
							self.sections[sname] = cur
							if sname is not None:
								self.sectionSequence.append (sname)
						if includes:
							seen = set ([sname])
							for include in [_i.strip () for _i in splitter.split (includes) if _i.strip ()]:
								if include not in seen:
									seen.add (include)
									if include in self.sections:
										for (var, val) in self.sections[include].items ():
											cur[var] = val
					else:
						mtch = self.commandPattern.match (line)
						if not mtch is None:
							grps = mtch.groups ()
							command = grps[0]
							param = grps[2]
							if command == 'abort':
								fd.close ()
								while len (fds) > 0:
									fds.pop (0).close ()
								fd = None
							elif command == 'close':
								fd.close ()
								if fds:
									fd = fds.pop (0)
								else:
									fd = None
							elif command == 'include':
								nfd = open (param, 'r')
								fds.insert (0, fd)
								fd = nfd
						else:
							mtch = self.dataPattern.match (line)
							if not mtch is None:
								(var, val) = mtch.groups ()
								if val == '{':
									block = var
									cur[var] = ''
								else:
									if len (val) > 1 and val[0] in '\'"' and val[-1] == val[0]:
										val = val[1:-1]
									cur[var] = val
							else:
								raise agn.error ('Unparsable line: %s' % line)

	def writeXML (self, fname):
		"""Write configuration as xml file"""
		fd = open (fname, 'w')
		wr = xagn.XMLWriter (fd)
		wr.startDocument ()
		wr.openNode ('config')
		for section in sorted (self.sections):
			block = self.sections[section]
			if section is None:
				section = "*"
			if block:
				wr.openNode ('section', name = section)
				for key in sorted (block):
					wr.textNode (key, block[key])
				wr.closeNode ('section')
		wr.closeNode ('config')
		wr.endDocument ()
		fd.close ()
	
	def __readSection (self, path, name, attrs):
		try:
			sname = attrs['name']
			if sname == '*':
				sname = None
		except KeyError:
			sname = None
		self._section = sname
	
	def __readEntry (self, path, name, attrs, content):
		try:
			self.sections[self._section][name] = content
		except KeyError:
			self.sections[self._section] = {name: content}
			if self._section is not None:
				self.sectionSequence.append (self._section)
	
	def readXML (self, fname):
		"""Read configuration from xml file"""
		self._section = None
		rd = xagn.XMLReader ()
		rd.addHandler ('config.section', self.__readSection, None)
		rd.addHandler ('config.section.', None, self.__readEntry)
		rd.processFile (fname)

class Permissions (object):
	"""Manage permission based on permission tables

This class reads the permission for a company and provides an
information, if a permission for this company is set (i.e. allowed) or
not."""
	def __init__ (self, companyID, cursor = None):
		self.permissions = {
			'group':	set (),
			'admin':	set (),
			'company':	set ()
		}
		if cursor is None:
			db = agn.DBaseID ()
			cursor = db.cursor ()
		else:
			db = None
		if cursor is not None:
			data = {'companyID': companyID}
			for (key, target) in self.permissions.items ():
				if key == 'group':
					query = (
						'SELECT security_token FROM admin_group_permission_tbl '
						'WHERE admin_group_id IN (SELECT admin_group_id FROM admin_group_tbl WHERE company_id IN (0, :companyID))'
					)
				elif key == 'admin':
					query = (
						'SELECT security_token FROM admin_permission_tbl '
						'WHERE admin_id IN (SELECT admin_id FROM admin_tbl WHERE company_id IN (0, :companyID))'
					)
				else:
					query = None
				if query:
					for row in cursor.query (query, data):
						if row[0] is not None:
							target.add (row[0])
			if db is not None:
				cursor.close ()
		if db is not None:
			db.close ()
	
	def __contains__ (self, key):
		return len (filter (lambda a: key in a, self.permissions.values ())) > 0

class CompanyConfig (object):
	"""Manage configuration from database

With this class one can query the content of the two configuration
tables config_tbl and company_info_tbl from the database. This is a
more convinient way to query these parameter and is also faster when
querying several paramater."""
	def __init__ (self, cursor = None, reread = None):
		"""``cursor'' is an open database cursor to use
(otherwise the database will be opened on demand), ``reread'' can be
either None (do only read the database once and do not reread it
later) or a numeric value as seconds after which the content should be
refreshed from the database.
"""
		self.cursor = cursor
		self.reread = reread
		self.last = None
		self.companyID = None
		self.companyInfo = None
		self.config = None
	
	def __reread (self, now):
		self.companyInfo = {}
		self.config = {}
		if self.cursor is None:
			db = agn.DBaseID ()
			cursor = db.cursor ()
			if cursor is None:
				db.close ()
				db = None
		else:
			db = None
			cursor = self.cursor
		query = (
			'SELECT company_id, cname, cvalue '
			'FROM company_info_tbl'
		)
		for row in cursor.query (query):
			try:
				self.companyInfo[row[0]][row[1]] = row[2]
			except KeyError:
				self.companyInfo[row[0]] = {row[1]: row[2]}
		query = (
			'SELECT class, name, value '
			'FROM config_tbl'
		)
		for row in cursor.query (query):
			try:
				self.config[(row[0], row[1])].append (row[2])
			except KeyError:
				self.config[(row[0], row[1])] = [row[2]]
		if db is not None:
			cursor.close ()
			db.close ()
		self.last = now
	
	def __check (self, force = False):
		now = time.time ()
		if force or self.last is None or (self.reread is not None and self.last + self.reread < now):
			self.__reread (now)

	def read (self):
		"""Force (re)reading the configuration from the database"""
		self.__check (True)

	def setCompanyID (self, companyID = None):
		"""Set the ``companyID'' for queries which are company dependend"""
		self.companyID = companyID
	
	def getConfig (self, className, name, default = None):
		"""retreive an entry from the config_tbl using
``className'' for the configuration class and ``name'' for the name of
the configuration. If ``default'' is not None, this value will be
returned, if no configuration had been found, otherwise a KeyError is
raised.
"""
		self.__check ()
		with agn.Ignore (KeyError):
			return self.config[(className, name)]
		#
		if default is not None:
			return default
		#
		raise KeyError ((className, name))
	
	def scanConfig (self, callback, classNames = None):
		"""Scans the config_tbl and calling ``callback'' for
each available value. ``callback'' must be a callable and is invoked
with three arguments, the configuration class and name and its value.
If ``classNames'' is not None it is used as an iterable or a str to
limit the scan to these configuration classes.
"""
		self.__check ()
		if classNames is not None and not hasattr (classNames, '__iter__'):
			classNames = [classNames]
		for (key, valueList) in self.config.items ():
			if classNames is None or key[0] in classNames:
				for value in valueList:
					callback (key[0], key[1], value)
	
	def collectConfig (self, matcher = None): 

		"""Collects the content of config_tbl to a dict where
each key is a tuple of (configuration class, configuration name). The
optional ``matcher'' can be a callable which is invoked with tow
arguments, the configuration class and name and must return a boolean
value which indicates if this entry should be added to the collection
or not."""
		rc = {}
		def callback (className, name, value):
			if matcher is None or matcher (className, name):
				rc[(className, name)] = value
		self.scanConfig (callback)
		return rc
	
	def getCompanyInfo (self, name, companyID = None, index = None, default = None, convert = None):
		"""Retrieves an entry from the company_info_tbl using
``name'' for the configuration name for ``companyID'', if not None,
else for the instance variable self.companyID (if set). ``index'' is
an optional index for the configuration name and ``default'' will be
used, if no entry had been found, otherwise a KeyError is raised.
``convert'' is an optional callable which will be invoked with the
found value to convert it before returning it."""
		self.__check ()
		if index is not None:
			with agn.Ignore (KeyError):
				return self.getCompanyInfo ('%s[%s]' % (name, str (index)), companyID = companyID, convert = convert)
		#
		if companyID is None:
			companyID = self.companyID
		for cid in [companyID, 0]:
			if cid is not None:
				with agn.Ignore (KeyError):
					value = self.companyInfo[cid][name]
					return convert (value) if convert is not None and callable (convert) else value
		#
		if default is not None:
			return default
		#
		raise KeyError (name)
	
	def scanCompanyInfo (self, callback, companyID = None):
		"""Scan the company_info_tbl and invoke ``callback''
with two arguments, the name and the value for all found configuration
values. if ``companyID'' is not None, then all values for this company
are scanned, otherwise the instance variable self.companyID is used
(if set)."""
		self.__check ()
		available = {}
		if companyID is None:
			companyID = self.companyID
		for cid in [0, companyID]:
			if cid is not None and cid in self.companyInfo:
				for (key, value) in self.companyInfo[cid].items ():
					available[key] = value
		for (key, value) in available.items ():
			callback (key, value)
	
	def collectCompanyInfo (self, matcher = None, companyID = None):
		"""Collects all entries from the company_info_tbl into
a dict for ``companyID'' (if it is not None, else the instance
variable self.companyID is used, if set). ``matcher'' is an optional
callable which is invoked with one argument the name of the
configuration parameter. This entry is added, if this method returns
True, otherwise it is discarded."""
		rc = {}
		def callback (key, value):
			if matcher is None or matcher (key):
				rc[key] = value
		self.scanCompanyInfo (callback, companyID)
		return rc
	
	def convert (self, sectionConfig = None):
		"""Converts the configuration into an instance of
class Config using ``sectionConfig'' to store the content of the
config_tbl. The content of the company_info_tbl is stored in the
default section of the configuration while each company specific value
is stored in the section with the companyID as its name."""
		self.__check ()
		rc = Config ()
		for (skey, value) in self.config.items ():
			key = '.'.join ([_k for _k in skey if _k])
			if key:
				if sectionConfig:
					ckey = '%s.%s' % (sectionConfig, key)
				else:
					ckey = key
				rc[ckey] = ', '.join ([_v if _v is not None else '' for _v in value])
		for (companyID, cfg) in self.companyInfo.items ():
			if companyID:
				rc.push (str (companyID))
			for (key, value) in cfg.items ():
				rc[key] = value
			if companyID:
				rc.pop ()
		return rc
#}}}
#
# Dynamic database {{{
try:
	import	gdbm, json

	class JsonCodec (object):
		"""codec for DDB using json"""
		serializer = [
			agn.mutable (
				name ='__datetime__',
				typ = datetime.datetime,
				encode = lambda obj: [obj.year, obj.month, obj.day, obj.hour, obj.minute, obj.second],
				decode = lambda obj: datetime.datetime (obj[0], obj[1], obj[2], obj[3], obj[4], obj[5])
			)
		]
		class Encoder (json.JSONEncoder):
			def default (self, obj):
				for ser in JsonCodec.serializer:
					if isinstance (obj, ser.typ):
						return {ser.name: ser.encode (obj)}
				return json.JSONEncoder.default (self, obj)
		class Decoder (json.JSONDecoder):
			pass
		def objectHook (self, dct):
			for ser in JsonCodec.serializer:
				if ser.name in dct:
					return ser.decode (dct[ser.name])
			return dct

		def __init__ (self, extended = False, pretty = False):
			if pretty:
				sort_keys = True
				indent = 2
			else:
				sort_keys = False
				indent = None
			if extended:
				self.dec = self.Decoder (object_hook = self.objectHook)
				self.enc = self.Encoder (sort_keys = sort_keys, indent = indent)
			else:
				self.dec = json.JSONDecoder ()
				self.enc = json.JSONEncoder (sort_keys = sort_keys, indent = indent)
			self.decode = self.dec.decode
			self.encode = self.enc.encode
	
	class PickleCodec (object):
		"""codec for DDB using the pickle modules"""
		def __init__ (self):
			self.decode = pickle.loads
			self.encode = pickle.dumps

	class DDB (object):
		"""Dynamic database

This is a persistent database class for key/value storing. Depending
of the used codec it allows to use any python object, with is
peristable using the codec to be used as key or value. Currently
"json" and "pickle" are available codecs, but one can implement a
custom codec by providing a class which provides these methods:
	- encode ()
	- deocde ()

where encode() accepts a python object and returns a string
representation for this codec while decode() takes such a string
representation and returns the object. Add the codec to DDB.codecMap.

By default only strings are supported (and not coded) for the
database, which can be changed by calling the method keymakerCodec().

Beware NOT to change the codec on existing databases!"""
		codecMap = {
			'json': JsonCodec,
			'pickle': PickleCodec
		}
		codecDefault = 'json'
		def __init__ (self, fname, codec = None, options = None):
			"""Use ``fname'' as the filename to store the
data to, ``codec'' the name of the codec (or None to use the default
codec) and ``options'' as keyword parameter to the codec constructor
(or None)"""
			self.fname = fname
			self.db = gdbm.open (self.fname, 'c')
			if options is None:
				options = {}
			if codec is None:
				codec = self.codecDefault
			if type (codec) in (str, unicode):
				self.codec = self.codecMap[codec] (**options)
			else:
				self.codec = codec
			self.keymaker = [None, None]
			self.cur = None
			self.keymakerStandard ()
		
		def close (self):
			"""Close the database"""
			if not self.db is None:
				self.db.close ()
				self.db = None
		
		def keymakerStandard (self):
			"""Switch to string only keys"""
			self.keymaker = [lambda var: str (var), lambda var: var]
		
		def keymakerCodec (self):
			"""Switch to encoded keys"""
			self.keymaker = [lambda var: self.codec.encode (var), lambda var: self.codec.decode (var)]
		
		def __getitem__ (self, var):
			var = self.keymaker[0] (var)
			r = self.codec.decode (self.db[var])
			r[0]['atime'] = int (time.time ())
			try:
				r[0]['get'] += 1
			except KeyError:
				r[0]['get'] = 1
			self.db[var] = self.codec.encode (r)
			return r[1]
		
		def __setitem__ (self, var, val):
			var = self.keymaker[0] (var)
			now = int (time.time ())
			try:
				r = self.codec.decode (self.db[var])
				r[0]['mtime'] = now
				try:
					r[0]['set'] += 1
				except KeyError:
					r[0]['set'] = 1
				r[1] = val
			except KeyError:
				r = [{'ctime': now, 'mtime': now, 'atime': now, 'get': 0, 'set': 1}, val]
			self.db[var] = self.codec.encode (r)
		
		def __delitem__ (self, var):
			var = self.keymaker[0] (var)
			with agn.Ignore (KeyError):
				del self.db[var]
			
		def __len__ (self):
			return len (self.db)
		
		def __enter__ (self):
			return self
		
		def __exit__ (self, exc_type, exc_value, tb):
			self.close ()
			return False
		
		class Iterator (object):
			def __init__ (self, db, decode):
				self.db = db
				self.cur = self.db.firstkey ()
				self.decode = decode
			
			def next (self):
				if self.cur is None:
					raise StopIteration ()
				key = self.cur
				self.cur = self.db.nextkey (self.cur)
				return self.decode (key)

		def __iter__ (self):
			return self.Iterator (self.db, self.keymaker[1])
			
		def __contains__(self, key):
			return self.exists (key)
		
		def __call__ (self, key, **kws):
			try:
				record = self[key]
			except KeyError:
				record = None
			if kws:
				if record is None:
					record = kws
				elif type (record) is not dict:
					raise TypeError ('%s: cannot modify non dictionary data' % key)
				else:
					for (k, v) in kws.items ():
						record[k] = v
				self[key] = record
			return record

		def put (self, var, **kws):
			"""Set ``var'' to dict resultung from passed keywords"""
			self[var] = kws
		
		def get (self, var):
			"""Get value for ``var''"""
			return self[var]
		
		def rawput (self, var, record):
			"""Set ``var'' to raw content including control data"""
			self.db[var] = record
		
		def rawget (self, var):
			"""Get raw content for ``var'' including control data"""
			return self.db[var]
		
		def rawdel (self, var):
			"""Removes entry for ``var''"""
			del self.db[var]
		
		def exists (self, var):
			"""Checks if ``var'' exists in database"""
			return self.db.has_key (self.keymaker[0] (var)) == 1
		
		def stat (self, var):
			"""Returns the control data for ``var''"""
			return self.codec.decode (self.db[self.keymaker[0] (var)])[0]
		
		def sstat (self, var):
			"""Returns the control data for ``var'' as an agn.mutable"""
			return agn.mutable (**self.stat (var))
		
		def keys (self):
			"""Returns all keys from the database"""
			return (self.keymaker[1] (_k) for _k in self.db.keys ())
		
		def sync (self):
			"""Write unwritten output to database"""
			self.db.sync ()
		
		def cleanup (self, minCtime = None, minMtime = None):
			"""Remove entries from the database which creation time before ``minCtime'' or modification time is before ``minMtime''"""
			if not minCtime is None or not minMtime is None:
				toRemove = []
				key = self.db.firstkey ()
				while not key is None:
					ts = self.codec.decode (self.db[key])[0]
					if (not minCtime is None and ts['ctime'] < minCtime) or (not minMtime is None and ts['mtime'] < minMtime):
						toRemove.append (key)
					key = self.db.nextkey (key)
				if toRemove:
					for key in toRemove:
						del self.db[key]
					self.close ()
					def reorg (filename):
						db = gdbm.open (filename, 'w')
						db.reorganize ()
						db.close ()
					Daemonic.call (reorg, self.fname)
					self.db = gdbm.open (self.fname, 'c')
		
		def truncate (self):
			"""Clears the database"""
			self.close ()
			self.db = gdbm.open (self.fname, 'n')

	class FDBBase (object):
		"""Fast persistant database with caching

This is a more simple persistant database compared to DDB, but
implements caching and no support for control information.

This class can be subclassed to implement some hooks to allow using
other types for key and value than strings (as this version does). See
FDBGeneric for a version using pickle for serialization of key and
value."""
		dbMemory = ':memory:'
		dbScratch = ':scratch:'
		def __init__ (self, fname, cachesize, readonly = False):
			"""Use ``fname'' as the file to store the data
(also possible values are FDBBase.dbMemory to keep the database in
memory or FDBBase.dbScratch to create an scratch file which will
vanish when the database is closed. ``cachesize'' is the maximum
number of entries to hold in the cache and ``readonly'' allows using
an existing database for reading only."""
			self.fname = fname
			self.cachesize = cachesize
			self.readonly = readonly
			self.cache = agn.Cache (self.cachesize)
			if self.fname == self.dbMemory:
				self.db = None
			elif self.fname == self.dbScratch:
				ndir = agn.mkpath (agn.base, 'var', 'tmp')
				if os.path.isdir (ndir):
					odir = tempfile.tempdir
					tempfile.tempdir = ndir
				else:
					ndir = None
				(fd, fname) = tempfile.mkstemp ()
				os.close (fd)
				self.db = gdbm.open (fname, 'n')
				os.unlink (fname)
				if ndir is not None:
					tempfile.tempdir = odir
			else:
				if readonly:
					mode = 'r'
				else:
					mode = 'c'
				self.db = gdbm.open (self.fname, mode)
			self.cur = None
			self.setup ()

		def close (self):
			"""Close the database"""
			self.teardown ()
			if self.db is not None:
				self.db.close ()
				self.db = None
		
		def __getitem__ (self, key):
			ckey = self.encodeKey (key)
			try:
				try:
					value = self.cache[ckey]
				except KeyError:
					if self.db is None:
						raise
					value = self.decodeValue (self.db[ckey])
					self.cache[ckey] = value
			except KeyError:
				raise KeyError (key)
			return value
		
		def __permission (self):
			if self.readonly:
				raise IOError ('database is in read only mode')
		
		def __setitem__ (self, key, value):
			self.__permission ()
			ckey = self.encodeKey (key)
			if self.db is not None:
				self.db[ckey] = self.encodeValue (value)
			self.cache[ckey] = value
		
		def __delitem__ (self, key):
			self.__permission ()
			ckey = self.encodeKey (key)
			self.cache.remove (ckey)
			if self.db is not None:
				try:
					del self.db[ckey]
				except KeyError:
					raise KeyError (key)

		def __len__ (self):
			return len (self.db)
		
		def __enter__ (self):
			return self
		
		def __exit__ (self, exc_type, exc_value, tb):
			self.close ()
			return False
		
		class IteratorDB (object):
			def __init__ (self, ref):
				self.ref = ref
				self.db = ref.db
				self.cur = self.db.firstkey ()
			
			def next (self):
				if self.cur is None:
					raise StopIteration ()
				key = self.cur
				self.cur = self.db.nextkey (self.cur)
				return self.ref.decodeKey (key)
		
		def IteratorCache (object):
			def __init__ (self, ref):
				self.ref = ref
				self.cl = ref.cache.cacheline[:]
			
			def next (self):
				if not self.cur:
					raise StopIteration ()
				key = self.cur.pop (0)
				return self.ref.decodeKey (key)
		
		def __iter__ (self):
			if self.db is not None:
				return self.IteratorDB (self)
			else:
				return self.IteratorCache (self)
	
		def __contains__ (self, key):
			if key in self.cache:
				return True
			return self.encodeKey (key) in self.db
		
		def keys (self):
			"""Returns all keys of the database"""
			if self.db is not None:
				return [self.decodeKey (_k) for _k in self.db.keys ()]
			else:
				return [self.decodeKey (_k) for _k in self.cache.cacheline]
		#
		# Methods to override
		#
		def setup (self):
			"""Hook: is called as last method in constructor"""
			pass
		def teardown (self):
			"""Hook: is called as first method in close"""
			pass
		def encodeKey (self, key):
			"""Hook: encode an arbitrary key"""
			return key
		def decodeKey (self, key):
			"""Hook: decode an arbitrary key"""
			return key
		def encodeValue (self, value):
			"""Hook: encode an arbitrary value"""
			return value
		def decodeValue (self, value):
			"""Hook: decode an arbitrary value"""
			return value

	class FDBGeneric (FDBBase):
		"""Fast persistant database which accepts object

For details see FDBBase about usage. This version uses the pickle
module to serialize keys and values for storing so all python objects,
which can be serialized using pickle can be used as key or value."""
		def encodeKey (self, key):
			return pickle.dumps (key, -1)
		def decodeKey (self, key):
			return pickle.loads (key)
		def encodeValue (self, value):
			return pickle.dumps (value, -1)
		def decodeValue (self, value):
			return pickle.loads (value)
except ImportError:
	pass
#}}}
#
# Mailspool handling {{{
class Mailspool:
	"""Handling mail spool directories

This can be used by a process which handles incoming mails asynchron.
A common way to use this class is:

ms = eagn.Mailspool ('path/to/spool')
ms.updateProcmailrc ()
while True:
	for ws in ms:
		try:
			for mail in ws:
				if processMail (mail):
					ws.success ()
				else:
					ws.fail ()
		catch Exception:
			ws.abort ()
		finally:
			ws.done ()
	time.sleep (1)
"""
	class Workspace:
		"""A temporary workspace

Whenever a mail batch should be processed a temporary workspace is
created and all relevant information is stored in an instance of this
class. One should use this class as an iterator which returns the full
path to the next mail file to process."""
		def __init__ (self, ws, ref):
			self.ws = ws
			self.ref = ref
			self.files = os.listdir (self.ws)
			self.cur = None
		
		def __iter__ (self):
			self.cur = None
			return self
		
		def next (self):
			if not self.files:
				raise StopIteration ()
			self.cur = agn.mkpath (self.ws, self.files.pop (0))
			return self.cur

		def __timestamp (self, daybased):
			now = time.localtime ()
			if daybased:
				return '%04d%02d%02d' % (now.tm_year, now.tm_mon, now.tm_mday)
			else:
				return '%02d%02d%02d' % (now.tm_hour, now.tm_min, now.tm_sec)
		
		def done (self, force = False):
			"""Cleanup and remove the workspace"""
			files = os.listdir (self.ws)
			if not files or force:
				for fname in files:
					path = agn.mkpath (self.ws, fname)
					try:
						os.unlink (path)
					except OSError, e:
						agn.log (agn.LV_ERROR, 'ws', 'Failed to remove "%s": %r' % (path, e.args))
				try:
					os.rmdir (self.ws)
				except OSError, e:
					agn.log (agn.LV_ERROR, 'ws', 'Failed to remove workspace "%s": %r' % (self.ws, e.args))
		
		def abort (self):
			"""Abort processing of a workspace and move it to quarantine"""
			basedir = agn.mkpath (self.ref.quarantine, os.path.basename (self.ws))
			destdir = basedir
			n = 0
			while os.path.isdir (destdir):
				n += 1
				if n > 65536:
					break
				destdir = '%s.%06d' % (basedir, n)
			try:
				os.rename (self.ws, destdir)
				agn.log (agn.LV_INFO, 'ws', 'Aborted workspace %s and moved it to %s' % (self.ws, destdir))
			except OSError:
				agn.log (agn.LV_ERROR, 'ws', 'Failed to abort workspace %s, force removing it now' % self.ws)
				self.done (True)

		def fail (self):
			"""Mark processing of a file as failed and move it to quarantine"""
			if self.cur:
				destdir = agn.mkpath (self.ref.quarantine, self.__timestamp (True))
				if self.ref.checkPath (destdir):
					bname = '%s-%s' % (self.__timestamp (False), os.path.basename (self.cur))
					dest = agn.mkpath (destdir, bname)
					n = 0
					while os.path.isfile (dest) and n < 128:
						n += 1
						dest = agn.mkpath (destdir, '%s-%d' % (bname, n))
					try:
						os.rename (self.cur, dest)
					except OSError, e:
						agn.log (agn.LV_ERROR, 'store', 'Failed to move "%s" to "%s": %r' % (self.cur, dest, e.args))
						try:
							os.unlink (self.cur)
						except OSError, e:
							agn.log (agn.LV_ERROR, 'store', 'Even failed to remove stale file "%s": %r' % (self.cur, e.args))
				
		def success (self, sender = None):
			"""Mark processing of a file as successful and move it to store"""
			if self.cur:
				deleteIt = False
				dest = agn.mkpath (self.ref.store, '%s-mbox' % self.__timestamp (True))
				try:
					fdi = open (self.cur, 'r')
					try:
						if self.ref.storeSize and self.ref.storeSize < 65536:
							bufsize = self.ref.storeSize
						else:
							bufsize = 65536
						fdo = open (dest, 'a')
						fcntl.lockf (fdo, fcntl.LOCK_EX)
						if not sender is None:
							fdo.write ('From %s  %s\n' % (sender, time.ctime ()))
						copied = 0
						last = None
						while True:
							buf = fdi.read (bufsize)
							if not buf:
								break
							fdo.write (buf)
							last = buf[-1]
							copied += len (buf)
							if self.ref.storeSize and copied >= self.ref.storeSize:
								if not last is None and last != '\n':
									nl = '\n'
								else:
									nl = ''
								fdo.write ('%s--- Message truncated\n\n' % nl)
								break
						fcntl.lockf (fdo, fcntl.LOCK_UN)
						fdo.close ()
						deleteIt = True
					except IOError, e2:
						agn.log (agn.LV_ERROR, 'store', 'Failed to store "%s" in "%s": %r' % (self.cur, dest, e2.args))
						self.fail ()
					fdi.close ()
				except IOError, e1:
					agn.log (agn.LV_ERROR, 'store', 'Failed to open "%s": %r' % (self.cur, e1.args))
					self.fail ()
				if deleteIt:
					try:
						os.unlink (self.cur)
					except OSError, e:
						agn.log (agn.LV_ERROR, 'store', 'Failed to remove "%s": %r' % (self.cur, e.args))
			
	def __init__ (self, spooldir, incoming = None, workspace = None, store = None, quarantine = None,
		worksize = None, mode = None, scan = True, storeSize = None
	):
		"""Use ``spooldir'' as the default base path for the
mail spool base directory. You can set ``incoming'', ``workspace'',
``store'' and ``quarantine'' to custom values or let the class create
them below the ``spooldir'' (recommended). ``worksize'' is the maximum
number of mail files per created workspace. ``mode'' is the file mode
when creating the workspace. If ``scan'' is True, then already
existing workspace directories are promopted for further processing,
otherwise they are ignored. ``storeSize'' can be set to a maximum
number of bytes when storing a successful mail. This can be used to
limit hard drive usage in case of high volume mail traffic."""
		self.spooldir = spooldir
		if not incoming is None:
			self.incoming = incoming
		else:
			self.incoming = agn.mkpath (self.spooldir, 'incoming')
		if not workspace is None:
			self.workspace = workspace
		else:
			self.workspace = agn.mkpath (self.spooldir, 'workspace')
		if not store is None:
			self.store = store
		else:
			self.store = agn.mkpath (self.spooldir, 'store')
		if not quarantine is None:
			self.quarantine = quarantine
		else:
			self.quarantine = agn.mkpath (self.spooldir, 'quarantine')
		if worksize is None:
			self.worksize = 1000
		else:
			self.worksize = worksize
		if not mode is None:
			self.mode = mode
		else:
			self.mode = 0700
		self.scan = scan
		self.storeSize = storeSize
		self.workspaces = []
		self.pathChecked = set ()
		for path in [self.spooldir, self.incoming, self.workspace, self.store, self.quarantine]:
			self.checkPath (path)
	
	def __iter__ (self):
		if not self.workspaces:
			if self.scan:
				self.scanWorkspaces ()
				if not self.workspaces:
					self.createWorkspaces ()
			else:
				self.createWorkspaces ()
		return self
	
	def next (self):
		while self.workspaces:
			ws = self.Workspace (self.workspaces.pop (0), self)
			if ws.files:
				return ws
			ws.done ()
		raise StopIteration ()
	
	def checkPath (self, path):
		"""Check and create missing ``path''"""
		rc = True
		if path and not path in self.pathChecked:
			try:
				agn.createPath (path, self.mode)
				self.pathChecked.add (path)
			except agn.error, e:
				agn.log (agn.LV_ERROR, 'path', 'Failed to create "%s": %r' % (path, e.args))
				rc = False
		return rc
	
	def scanWorkspaces (self):
		"""Scan for existing workspaces"""
		self.workspaces = []
		for fname in os.listdir (self.workspace):
			path = agn.mkpath (self.workspace, fname)
			if os.path.isdir (path):
				self.workspaces.append (path)
		self.workspaces.sort ()
	
	def createWorkspaces (self):
		"""Create new worksapces from incoming files"""
		inc = os.listdir (self.incoming)
		cur = None
		count = 0
		idx = 0
		lastts = ''
		for fname in inc:
			if fname.startswith ('.'):
				continue

			while cur is None:
				now = time.localtime ()
				ts = '%04d%02d%02d%02d%02d%02d' % (now.tm_year, now.tm_mon, now.tm_mday, now.tm_hour, now.tm_min, now.tm_sec)
				if ts != lastts:
					lastts = ts
					idx = 0
				else:
					idx += 1
					if idx >= 1000:
						idx = 0
						time.sleep (1)
						continue
				cur = agn.mkpath (self.workspace, '%s%03d' % (ts, idx))
				if os.path.isdir (cur):
					cur = None
				else:
					agn.createPath (cur)
					self.workspaces.append (cur)
					agn.log (agn.LV_DEBUG, 'create', 'Created new workspace %s' % cur)
			src = agn.mkpath (self.incoming, fname)
			dst = agn.mkpath (cur, fname)
			try:
				os.rename (src, dst)
				count += 1
			except OSError, e:
				agn.log (agn.LV_ERROR, 'create', 'Failed to move "%s" to "%s": %r' % (src, dst, e.args))
			if self.worksize and count >= self.worksize:
				cur = None
				count = 0
	#
	# Tools not directly required for spool handling
	#
	def updateProcmailrc (self, content = None, procmailrc = agn.mkpath (agn.base, '.procmailrc')):
		"""Create a $HOME/.procmailrc to redirect the mails to
the incoming spool directory. If ``content'' is None, a suitable
content is created, otherwise it must be provided. ``procmailrc'' is
the path to the target file."""
		if content is None:
			content = '# created by eagn.Mailspool\n:0:\n%s/.\n' % self.incoming
		try:
			fd = open (procmailrc, 'r')
			ocontent = fd.read ()
			fd.close ()
		except IOError, e:
			if e.args[0] != errno.ENOENT:
				agn.log (agn.LV_WARNING, 'procmailrc', 'Failed to read "%s": %r' % (procmailrc, e.args))
			ocontent = ''
		if ocontent != content:
			try:
				fd = open (procmailrc, 'w')
				fd.write (content)
				fd.close ()
				os.chmod (procmailrc, 0600)
				agn.log (agn.LV_INFO, 'chk', 'Created new/Updated procmailrc file "%s".' % procmailrc)
			except (IOError, OSError), e:
				agn.log (agn.LV_ERROR, 'chk', 'Failed to install procmailrc file "%s": %r' % (procmailrc, e.args))
	
#}}}
#
# Batch processing {{{
class Batch (object):
	"""Handling of batch files

This class provides a frame work for batch processing using a file"""
	batchUnique = 0
	def __init__ (self, path):
		"""``path'' is the file to use for data exchange"""
		self.path = path
		self.directory = os.path.dirname (path)
		self.filename = os.path.basename (path)
		self.batchUnique += 1
		self.instanceUnique = [self.batchUnique, 0]
	#
	# Producer (simple, ey)
	def write (self, s):
		"""write a line to the file"""
		fd = open (self.path, 'a')
		fd.write ('%s\n' % s)
		fd.close ()
	
	#
	# Consumer (rest of the class)
	class Temp (object): #{{{
		def __init__ (self, orig, path, fail, baseTarget):
			self.orig = orig
			self.path = path
			self.fail = fail
			self.baseTarget = baseTarget
			self.fd = None
			
		def __del__ (self):
			if not self.fail:
				self.remove ()
		
		def remove (self):
			if self.path is not None:
				self.close ()
				try:
					os.unlink (self.path)
					self.path = None
				except OSError, e:
					if e.args[0] != errno.ENOENT:
						agn.log (agn.LV_ERROR, 'batch', 'Failed to remove temp.file "%s": %r' % (self.path, e.args))
						self.fail = True
					else:
						self.path = None
		
		def move (self, destination):
			if self.path is not None:
				self.close ()
				if os.path.isdir (destination):
					dpath = agn.mkpath (destination, self.orig)
					if os.path.isfile (dpath):
						dpath = agn.mkpath (destination, self.path)
				else:
					dpath = destination
					n = 0
					while n < 128 and os.path.isfile (dpath):
						n += 1
						dpath = '%s-%d' % (destination, n)
				try:
					shutil.move (self.path, dpath)
					self.path = None
				except OSError, e:
					agn.log (agn.LV_ERROR, 'batch', 'Failed to move "%s" to "%s": %r' % (self.path, dpath, e.args))
					self.fail = True
		
		def open (self):
			if self.fd is None:
				self.fd = open (self.path, 'r')
			else:
				self.fd.seek (0)
			return self.fd is not None

		def close (self):
			if self.fd is not None:
				self.fd.close ()
				self.fd = None
		
		def read (self, size = None):
			if self.fd is not None:
				if size is None:
					return self.fd.read ()
				return self.fd.read (size)
			return ''
			
		def readline (self):
			if self.fd is not None:
				return self.fd.readline ()
			return ''
		
		def __iter__ (self):
			self.open ()
			return self
		
		def next (self):
			if self.fd is not None:
				rc = self.fd.readline ()
				if rc != '':
					return rc.strip ()
				self.close ()
			raise StopIteration ()
	
		def __save (self, what, s):
			now = time.localtime ()
			target = agn.mkpath (agn.base, 'var', 'log', '%04d%02d%02d-%s.log' % (now.tm_year, now.tm_mon, now.tm_mday, self.baseTarget))
			output = '[%02d.%02d.%04d %02d:%02d:%02d] %s: %s\n' % (now.tm_mday, now.tm_mon, now.tm_year, now.tm_hour, now.tm_min, now.tm_sec, what, s)
			try:
				fd = open (target, 'a')
				fd.write (output)
				fd.close ()
			except IOError, e:
				agn.log (agn.LV_ERROR, 'batch', 'Failed to write to "%s": %r' % (target, e.args))
				self.fail = True
		
		def success (self, s):
			self.__save ('success', s)
		def failure (self, s):
			self.__save ('failure', s)
	#}}}
	def open (self, delay = 10, retry = 10):
		"""opens current batch, ``retry'' time to access the
file with ``delay'' seconds waiting between each try.
		
On success this returns a Batch.Temp instance, which behaves like a
file for reading. This instance provides some helpful methods for
further handling of the processed file:
	- open(): open or rewind the file (must be called at least once)
	- remove(): delete the file
	- move(destination): move the file to destination
	- success(s): writes the string ``s'' to agnitas style data logfile for successful processed lines
	- failure(s): writes the string ``s'' to agnitas style data logfile for failed processed lines
"""
		rc = None
		if os.path.isfile (self.path):
			self.instanceUnique[1] += 1
			temp = agn.mkpath (self.directory, '.%s.%d.%d.%d.%.3f.temp' % (self.filename, os.getpid (), self.instanceUnique[0], self.instanceUnique[1], time.time ()))
			fail = False
			try:
				os.rename (self.path, temp)
			except OSError, e:
				agn.log (agn.LV_ERROR, 'batch', 'Failed to rename "%s" to "%s": %r' % (self.path, temp, e.args))
				fail = True
			if not fail:
				if delay > 0:
					time.sleep (delay)
				if retry > 0:
					while retry > 0 and len (agn.fileAccess (temp)[0]) > 0:
						time.sleep (1)
						retry -= 1
					if retry == 0:
						agn.log (agn.LV_ERROR, 'batch', 'File "%s" still in usage' % temp)
						fail = True
				if not fail:
					rc = self.Temp (self.path, temp, fail, self.filename)
		return rc

class Transaction (object):
	"""handling of transaction/rollback sequence
	
This class will be filled with a list of methods and its coresponding
rollback method to build ab a sequence of methods. During execution
each method will be called in sequence. If one method fails, the
rollback methods assigned to the already executed methods are called."""
	class TransactionStream (object):
		def __init__ (self, rollback = None, failed = False):
			self.rollback = rollback
			self.failed = failed
		
		def __call__ (self, name, *args):
			return self.do (name, *args)
		
		def do (self, name, *args):
			"""args must obey: <method> [<methodArgs>] [<rollback> [<rollbackArgs>]] [<rollbackFailed>]"""
			method = None
			methodArgs = None
			rollback = None
			rollbackArgs = None
			rollbackFailed = False
			state = 0
			for cur in args:
				if state == 0:
					if callable (cur):
						state = 1
						method = cur
						continue
					raise agn.error ('expect callable, not %r (%s)' % (cur, type (cur)))
				#
				if state == 1:
					state = 2
					if type (cur) is tuple:
						methodArgs = cur
						continue
				#
				if state == 2:
					if callable (cur):
						state = 3
						rollback = cur
						continue
					state = 4
				#
				if state == 3:
					state = 4
					if type (cur) is tuple:
						rollbackArgs = cur
						continue
				#
				if state == 4:
					state = 5
					if type (cur) is bool:
						rollbackFailed = cur
						continue
				#
				if state == 5:
					raise agn.error ('expecting no more arguments, but got %r (%s)' % (cur, type (cur)))

			if not self.failed:
				def mkargs (ma):
					if ma is None:
						return ()
					elif type (ma) is list:
						return tuple (ma)
					elif type (ma) is not tuple:
						return (ma, )
					return ma
				def add ():
					if rollback is not None:
						if self.rollback is None:
							self.rollback = []
						self.rollback.append ((name, rollback, mkargs (rollbackArgs)))
				#
				try:
					method (*mkargs (methodArgs))
					add ()
				except Exception as e:
					agn.log (agn.LV_ERROR, name, 'failed to execute: %s' % e)
					self.failed = True
					if rollbackFailed:
						add ()
					if self.rollback is not None:
						while self.rollback:
							(rbName, rbMethod, rbArgs) = self.rollback.pop ()
							try:
								rbMethod (*rbArgs)
							except Exception as e:
								agn.log (agn.LV_ERROR, rbName, 'failed to execute rollback: %s' % e)
						self.rollback = None
			return self.__class__ (self.rollback, self.failed)
		
		@property
		def success (self):
			return not self.failed
	#
	@classmethod
	def stream (cls):
		return cls.TransactionStream ()

	def __init__ (self):
		self.spool = []
		self.back = []
	
	def __mkargs (self, args):
		if args is None:
			return ()
		elif type (args) is list:
			return tuple (args)
		elif type (args) is not tuple:
			return (args, )
		return args

	def log (self, level, what, msg):
		"""log an entry"""
		agn.log (level, what, msg)
	
	def add (self, name, method, methodArgs = None, rollback = None, rollbackArgs = None):
		"""add a method and its rollback method with each arguments"""
		self.spool.append ((name, method, methodArgs, rollback, rollbackArgs))
	
	def execute (self, silent = False, rollbackFailed = False, callback = None):
		"""executes the sequence, on failure and ``silent'' is
False, the exeption causing the error is raised. If ``rollbackFailed''
is True, the rollback method for the method failed will be executed,
too, otherwise only all rollback methods for the successful methods
are executed. If ``callback''is not None, it is invoked with two
arguments, the current name of the method and a state."""
		error = None
		self.reset ()
		for (name, method, methodArgs, rollback, rollbackArgs) in self.spool:
			try:
				if method is not None:
					if callback: callback (name, 'start')
					if rollbackFailed:
						self.back.append ((name, rollback, rollbackArgs))
						method (*self.__mkargs (methodArgs))
					else:
						method (*self.__mkargs (methodArgs))
						self.back.append ((name, rollback, rollbackArgs))
					if callback: callback (name, 'done')
				else:
					self.back.append ((name, rollback, rollbackArgs))
			except Exception, e:
				if callback: callback (name, 'fail', e)
				self.log (agn.LV_ERROR, name, 'Execution failed, transaction aborted due to %s' % str (e))
				error = e
				break
		if error is not None:
			self.rollback (callback)
			if not silent:
				raise error
	
	def rollback (self, callback = None):
		"""If a method fails, this methods unwinds the
rollback stack and executes the rollback methods. If ``callback''is
not None, it is invoked with two arguments, the current name of the
method and a state."""
		while self.back:
			(name, rollback, rollbackArgs) = self.back.pop ()
			if rollback is not None:
				try:
					if callback: callback (name, 'rollback')
					rollback (*self.__mkargs (rollbackArgs))
					if callback: callback (name, 'done')
				except Exception, e:
					if callback: callback (name, 'fail', e)
					self.log (agn.LV_ERROR, name, 'Rollback failed due to %s' % str (e))
	
	def reset (self):
		"""clears the rollback stack"""
		self.back = []

try:
	import	sched
	
	class Schedule (object):
		"""An enhanced scheduler

This class is build around the python sched module but supports
repeating execution of a job. Each job must be encapsuled using the
class Schedule.Job so the housekeeping is working.
"""
		__slots__ = ['_schedule', '_active', '_running', '_jobclass']
		class Job (object):
			"""Single job in Schedule"""
			__slots__ = ['name', 'repeat', 'delay', 'priority', 'method', 'arguments']
			unit = Unit ()
			def __init__ (self, name, repeat, delay, priority, method, arguments):
				"""Create a job with ``name'', if
``repeat'' is True, repeat it with a ``delay'' in seconds between each
execution. ``priority'' controls the sequence of jobs which are ready
at the same time while a lower value means a higher priority.
``method'' is invoked using ``arguments'', if using the class itself,
otherwise the execute() method can be overwritten to implement the
job."""
				self.name = name
				self.repeat = repeat
				if type (delay) in (str, unicode):
					self.delay = self.unit.parse (delay)
				else:
					self.delay = delay
				self.priority = priority
				self.method = method
				self.arguments = arguments
			
			def __call__ (self, schedule):
				schedule.log ('job', '%s started' % self.name)
				start = time.time ()
				rc = self.execute ()
				end = time.time ()
				schedule.log ('job', '%s terminated %s after %.2f seconds' % (self.name, ('successful (%r)' % rc) if type (rc) is bool else ('with error (%r)' % (rc, )), end - start))
				if self.repeat:
					if rc:
						schedule.log ('job', '%s reactivate' % self.name)
						schedule._add (self)
					else:
						schedule.log ('job', '%s not reactivated' % self.name)
				else:
					schedule.log ('job', '%s not reactivated due to single run' % self.name)
			
			def execute (self):
				"""Entry point for execution, must return boolean value, False for error, otherwise True"""
				return self.method (*self.arguments)

		def __init__ (self, jobclass = None):
			"""Create an instance using ``jobclass'' for encapsulation, Schedule.Job is used, if ``jobclass'' is None"""
			self._schedule = sched.scheduler (self._timer, self._delayer)
			self._active = True
			self._running = True
			self._jobclass = jobclass if jobclass is not None else self.Job
		
		def _timer (self):
			return int (time.time ())
		
		def _delayer (self, amount):
			self.log ('delay', 'for %d seconds' % amount)
			while self._running and amount > 0:
				amount -= 1
				self.intercept ()
				if self._running:
					time.sleep (1)
			if amount > 0:
				self.log ('delay', 'terminated with %d seconds left' % amount)
			else:
				self.log ('delay', 'done')
		
		def _offset (self, job, immediately):
			if immediately:
				offset = 0
			elif job.repeat:
				time.tzset ()
				now = self._timer () - time.timezone
				offset = (now // job.delay + 1) * job.delay - now
				while offset <= 0:
					offset += job.delay
			else:
				offset = job.delay
			return offset
			
		def _add (self, job, immediately = False):
			if self._active:
				offset = self._offset (job, immediately)
				self.log ('add', 'job %s with offset in %d seconds with priority %d' % (job.name, offset, job.priority))
				self._schedule.enter (offset, job.priority, job, (self, ))
			else:
				self.log ('add', 'not job %s due inactive scheduler' % job.name)
			return job
			
		def every (self, name, immediately, delay, priority, method, arguments):
			"""Add a repeating job with ``name''. Execute
it ``immediately'', if True, repeat after ``delay'' seconds using
``priority''. Call ``method'' with ``arguments'' for execution."""
			return self._add (self._jobclass (name, True, delay, priority, method, arguments), immediately)
		def once (self, name, delay, priority, method, arguments):
			"""Add a single job with ``name'', start it
after ``delay'' seconds using ``priority''. Call ``method'' with
``arguments''."""
			return self._add (self._jobclass (name, False, delay, priority, method, arguments))
		
		def start (self):
			"""Start the scheduling"""
			self.log ('run', 'start')
			self._active = True
			while not self._schedule.empty ():
				self._schedule.run ()
			self.log ('run', 'end')
		
		def stop (self):
			"""Stop scheduling and cancel all pending jobs"""
			self.log ('stop', 'request')
			self._active = False
			while not self._schedule.empty ():
				self.log ('stop', 'cancel %s' % self._schedule.queue[0].action.name)
				self._schedule.cancel (self._schedule.queue[0])
			self.log ('stop', 'finished')
		
		def term (self):
			"""Terminate scheduling"""
			self.log ('term', 'request')
			self.stop ()
			self._running = False
			self.log ('term', 'finished')

		def log (self, area, message):
			"""Hook: log message"""
			pass
		def intercept (self):
			"Hook: intercept is called during delaying"""
			pass
	
	class Jobqueue (Watchdog):
		"""class Jobqueue (Watchdog):

a scheduler running as a daemon process with watchdog to restart on
errors. To use this class, you should subclass class Schedule and
overwrite the method start() to add jobs to the scheduler, e.g.:

class MySchedule (eagn.Schedule):
	def start (self):
		self.every ('foo', False, '1h', 1, self.foo, ())
		self.every ('bar', False, '6h', 2, self.bar, ())
		self.once ('restart', '1w', 0, self.stop, ()) # this will force the watchdog to restart the subprocess each week
		return super (MySchedule, self).start ()
	
	def foo (self):
		# do something evey hour
		return True
	
	def bar (self):
		# do something every six hour
		return True

Then create an instance of jobqueue and start it:

jq = eagn.Jobqueue (MySchedule ())
jq.start ()
"""
		def __init__ (self, schedule):
			"""Setup using ``schedule'' instance"""
			super (Jobqueue, self).__init__ ()
			self.schedule = schedule
			self.scheduler = None

		def signalHandler (self, sig, stack):
			"""Catch signal for watchdog"""
			super (Jobqueue, self).signalHandler (sig, stack)
			if not self.running:
				if self.scheduler == os.getpid ():
					self.schedule.term ()

		def run (self):
			"""Entry point"""
			self.scheduler = os.getpid ()
			self.schedule.start ()
			if self.schedule._running:
				raise self.Job.Restart ()
			return True
			
		def start (self, *args, **kws):
			"""Start the jobqueue"""
			self.mstart (self.Job ('schedule', self.run, ()), *args, **kws)
			
except ImportError:
	pass
#}}}
#
# Mailing/OnDemandMailing {{{
class Mailing (object):
	"""Start mailing generation

This class wraps the communication to the merger to start generation
of mailings. These ca be regular and on demand mailings. (To control
on demand mailings, eagn.ODMailing provides a more useful alternative
and is based on this class.) The communication uses the trigger.py
process as the endpoint for the merger. It does not talk directly to
the java process."""
	__slots__ = ['merger', 'timeout', 'retries', 'rpcCfg', 'rpc']
	def __init__ (self, merger = None, port = None, timeout = None, retries = None):

		"""``merger'' is the address of the merger (or None to
use the default), ``port'' is the port the merger listens to (or None
to use the default 8080). ``timeout'' is the communication (socket)
timeout in seconds and ``retries'' is the number of attempts to try
starting a mailing if a failure occurs."""
		if merger is None:
			merger = agn._syscfg.get ('merger-address', '127.0.0.1')
		if port is None:
			port = agn._syscfg.iget ('trigger-port', 8080)
		if timeout is None:
			timeout = 30
		if retries is None:
			retries = 3
		self.merger = merger
		self.timeout = timeout
		self.retries = retries
		self.rpcCfg = xagn.XMLRPCConfig (host = merger, port = port)
		self.rpc = xagn.XMLRPCClient (self.rpcCfg)

	def active (self):
		"""Checks if the merger itself is ready and active"""
		def cbActive (rpc, data):
			return rpc.isActive ()
		return self.__rpc (cbActive, retries = 1)
	def fire (self, *args, **kws):
		"""Start a regular mailing, use keyword arguments for options:

- statusID: the maildrop_status_tbl.status_id to use this record for starting the mailing (required)
- mailingID: the mailing_tbl.mailing_id to be started 
- cursor: an open database cursor to retrieve informations from database, if required"""
		return self.__start (False, *args, **kws)
	def demand (self, *args, **kws):
		"""Starts an on demand mailing, use keyword arguments for options:

- statusID: the maildrop_status_tbl.status_id to use this record for starting the mailing (required)
- mailingID: the mailing_tbl.mailing_id to be started 
- cursor: an open database cursor to retrieve informations from database, if required"""
		return self.__start (True, *args, **kws)

	def __start (self, isOnDemandMailing, statusID = None, mailingID = None, cursor = None):
		rc = False
		if statusID is None:
			raise agn.error ('missing statusID for starting mailing')
		if mailingID is None and cursor is not None:
			query = (
				'SELECT mailing_id '
				'FROM maildrop_status_tbl '
				'WHERE status_id = :statusID'
			)
			rq = cursor.querys (query, {'statusID': statusID})
			if rq is None:
				raise agn.error ('no entry for statusID %d found' % statusID)
			mailingID = rq[0]
		if isOnDemandMailing and mailingID is None:
			raise agn.error ('missing mailingID for starting on demand mailing')
		if mailingID is not None:
			mailingName = '#%d' % mailingID
		else:
			mailingName = '#(statusID)%d' % statusID
		if cursor is not None and mailingID is not None:
			query = (
				'SELECT m.shortname, m.company_id, c.shortname '
				'FROM mailing_tbl m INNER JOIN company_tbl c ON c.company_id = m.company_id '
				'WHERE m.mailing_id = :mailingID'
			)
			rq = cursor.querys (query, {'mailingID': mailingID})
			if rq is not None:
				mailingName = '%s (ID %d, statusID %d for %s, ID %d)' % (rq[0], mailingID, statusID, rq[2], rq[1])
		
		genchange = None
		gcQuery = 'SELECT genstatus, genchange FROM maildrop_status_tbl WHERE status_id = :statusID'
		gcData = {'statusID': statusID}
		if cursor is not None:
			rq = cursor.querys (gcQuery, gcData)
			if rq is None:
				agn.log (agn.LV_WARNING, mailingName, 'Failed to retrieve activty status from maildrop_status_tbl for status_id %d' % statusID)
			elif rq[1] is None:
				agn.log (agn.LV_WARNING, mailingName, 'No genchange for status_id %d found' % statusID)
			else:
				genchange = rq[1]
				if rq[0] not in (1, 3):
					agn.log (agn.LV_ERROR, mailingName, 'Unexpected genstatus=%r during startup, aborting' % rq[0])
					return False
				else:
					agn.log (agn.LV_VERBOSE, mailingName, 'Found genstatus=%r and genchange=%s' % (rq[0], genchange))
				time.sleep (1)
		retries = self.retries
		if genchange is None and retries > 1:
			retries = 1
		#
		def cbStart (rpc, data):
			if isOnDemandMailing:
				rc = rpc.startOnDemandMailing (mailingID)
			else:
				rc = rpc.startMailing (statusID)
			if not rc[0]:
				agn.log (agn.LV_ERROR, mailingName, 'Failed to start mailing: %s' % rc[1])
			return rc[0]
		def cbRetry (retries, data):
			rc = False
			time.sleep (2)
			if genchange is not None:
				rq = cursor.querys (gcQuery, gcData)
				if rq is None or rq[1] is None:
					agn.log (agn.LV_ERROR, mailingName, 'No genchange found, no retry takes place')
				elif rq[1] != genchange:
					agn.log (agn.LV_WARNING, mailingName, 'genchange changed from %s to %s, assume successful mail triggering' % (genchange, rq[0]))
					rc = True
				else:
					agn.log (agn.LV_INFO, mailingName, 'genchange not changed, retry starting mailing after a second')
			return (rc, retries)
		rc = self.__rpc (cbStart, retry = cbRetry, retries = retries, id = mailingName)
		#
		if rc and genchange is not None:
			startup = 30
			while startup > 0:
				startup -= 1
				rq = cursor.querys (gcQuery, gcData)
				if rq is None:
					agn.log (agn.LV_ERROR, mailingName, 'Entry for status_id %d vanished' % statusID)
					break
				elif rq[0] == 2:
					agn.log (agn.LV_DEBUG, mailingName, 'Mailing in process')
					break
				elif rq[1] != genchange:
					agn.log (agn.LV_DEBUG, mailingName, 'Mailing already finished')
					break
				elif startup > 0:
					time.sleep (1)
		return rc
		
	def __rpc (self, callback, retry = None, data = None, timeout = None, retries = None, id = None):
		if timeout is None:
			timeout = self.timeout
		if retries is None:
			retries = self.retries
		if type (retries) not in (int, long):
			retries = agn.atoi (retries)
		if retries < 1:
			retries = 1
		if id is None:
			id = 'rpc'
		elif type (id) is unicode:
			id = id.encode ('UTF-8', errors = 'replace')
		elif type (id) is not str:
			id = str (id)
		#
		if timeout > 0:
			otimeout = socket.getdefaulttimeout ()
		rc = False
		while not rc and retries > 0:
			retries -= 1
			try:
				if timeout > 0:
					socket.setdefaulttimeout (timeout)
				rc = callback (self.rpc, data)
				agn.log (agn.LV_INFO if rc else agn.LV_WARNING, id, 'Call to merger %s results in %s' % (self.merger, str (rc).lower ()))
			except xagn.xmlrpclib.Error, e:
				agn.log (agn.LV_ERROR, id, 'Failed to communicate with merger %s: %s' % (self.merger, str (e)))
			except socket.error, e:
				agn.log (agn.LV_ERROR, id, 'Failed to call merger %s: %s' % (self.merger, str (e)))
			finally:
				if timeout > 0:
					socket.setdefaulttimeout (otimeout)
			if not rc and retry is not None and retries > 0:
				(rc, retries) = retry (retries, data)
		return rc

class ODMailing (DB):
	"""Control On Demand Mailings

This class uses eagn.Mailing to start the mailing but offers more
methods to control an on demand mailing. Its basic usage is typically
like this:

mailingID = 1234 # a valid mailing-id for an on demand mailing
odm = eagn.ODMailing (mailingID)
if odm.setup ():
	if odm.starter ():
		if odm.waitfor ():
			pass # everything okay
		else:
			pass # either mailing generation is still active or broken
	else:
		pass # either the generation is active from a previous call or the mailing is not active or broken
else:
	pass # either the mailing is not existing or not active or some resources (like database) is not available
"""
	def __init__ (self, mailingID):
		"""``mailingID'' is the mailing_tbl.mailing_id of the mailing to use"""
		super (ODMailing, self).__init__ ()
		self.mailingID = mailingID
		self.mailingName = None
		self.statusID = None
		self.genchange = None

	numSearch = re.compile ('[0-9]+')
	opMap = {'&': ' AND ', '|': ' OR ', '!': ' NOT '}
	opReplace = re.compile ('&+|\\|+|!+')
	opShrink = re.compile (' {2,}')
	def __getTargetExpression (self, expr):
		targets = [int (_t) for _t in self.numSearch.findall (expr)]
		if not targets:
			agn.log (agn.LV_ERROR, 'od', 'No targets referenced in target expression')
			return None
		tmap = {}
		fail = False
		for r in self.cursor.query ('SELECT target_id, target_sql, deleted FROM dyn_target_tbl WHERE target_id IN (%s)' % ', '.join ([str (_t) for _t in targets])):
			if r.deleted:
				agn.log (agn.LV_ERROR, 'od', 'TargetID %d is marked as deleted' % r.target_id)
				fail = True
			elif not r.target_sql:
				agn.log (agn.LV_ERROR, 'od', 'TargetID %s has empty SQL expression' % r.target_id)
				fail = True
			else:
				tmap[r.target_id] = r.target_sql
		if fail:
			return None
		pos = 0
		rc = []
		parse = lambda _s: self.opShrink.subn (' ', self.opReplace.subn (lambda _v: self.opMap[_v.group ()[0]], _s)[0])[0]
		while pos < len (expr):
			m = self.numSearch.search (expr, pos)
			if m is not None:
				(start, end) = m.span ()
				if start > pos:
					rc.append (parse (expr[pos:start]))
				target = int (m.group ())
				rc.append (tmap[target])
				pos = end
			else:
				rc.append (parse (expr[pos:]))
				pos = len (expr)
		return ''.join (rc)

	def setup (self, validateTargetExpression = None, maxBlocksize = None):
		"""Setup up the on demand mailing and find matching
statusID or create missing entry in maildrop_status_tbl. if
``validateTargetExpression'' is not None, it must be a callable which
is called with one argument, the resolved SQL fragment used as
mailing_tbl.target_expression and must return a boolean value, either
``True'' if the target expression is okay, otherwise ``False''.
``maxBlocksize'' can be set to a numeric value to modify
maildrop_status_tbl.blocksize for finer controlling the mail
generation (e.g. if one expects large mails, one can reduce the
blocksize to avoid too large meta files)."""
		self.statusID = None
		if self.isopen ():
			r = self.cursor.querys ('SELECT shortname, deleted, target_expression, company_id FROM mailing_tbl WHERE mailing_id = :mailingID', {'mailingID': self.mailingID})
			if not r is None:
				self.mailingName = r.shortname
				if r.deleted:
					agn.log (agn.LV_INFO, 'od', 'Mailing %r (%d) is marked as deleted' % (self.mailingName, self.mailingID))
				elif r.target_expression is None:
					agn.log (agn.LV_ERROR, 'od', 'Mailing %r (%d) has no target expression' % (self.mailingName, self.mailingID))
				else:
					targetExpression = self.__getTargetExpression (r.target_expression)
					if targetExpression is None:
						agn.log (agn.LV_ERROR, 'od', 'Assigned target expression "%s" failed to expand' % r.target_expression)
					else:
						companyID = r.company_id
						if validateTargetExpression:
							valid = validateTargetExpression (targetExpression)
							if not valid:
								agn.log (agn.LV_ERROR, 'od', 'Final TargetExpression "%s" can not be validated' % targetExpression)
						else:
							valid = True
						if valid:
							blocksize = None
							for state in 1, 2:
								if self.statusID is None:
									for r in self.cursor.queryc ('SELECT status_id, blocksize FROM maildrop_status_tbl WHERE mailing_id = :mailingID AND status_field = \'D\'', {'mailingID': self.mailingID}):
										agn.log (agn.LV_VERBOSE, 'od', 'Found statusID %r for mailing %r (%d)' % (r.status_id, self.mailingName, self.mailingID))
										if self.statusID is None or r.status_id > self.statusID:
											self.statusID = r.status_id
											blocksize = r.blocksize
								if self.statusID is None and state == 1:
									if maxBlocksize is not None and maxBlocksize > 0:
										blocksize = maxBlocksize
									else:
										blocksize = None
									cnt = self.cursor.execute (self.cursor.qselect (
										oracle = (
											'INSERT INTO maildrop_status_tbl (status_id, company_id, status_field, mailing_id, blocksize, senddate, gendate, genstatus, genchange) '
											'VALUES (maildrop_status_tbl_seq.nextval, :companyID, :statusField, :mailingID, :blocksize, current_timestamp, current_timestamp, 1, current_timestamp)'
										), mysql = (
											'INSERT INTO maildrop_status_tbl (company_id, status_field, mailing_id, blocksize, senddate, gendate, genstatus, genchange) '
											'VALUES (:companyID, :statusField, :mailingID, :blocksize, current_timestamp, current_timestamp, 1, current_timestamp)'
										), sqlite = (
											'INSERT INTO maildrop_status_tbl (company_id, status_field, mailing_id, blocksize, senddate, gendate, genstatus, genchange) '
											'VALUES (:companyID, :statusField, :mailingID, :blocksize, current_timestamp, current_timestamp, 1, current_timestamp)'
										)),
										{'companyID': companyID, 'statusField': 'D', 'mailingID': self.mailingID, 'blocksize': blocksize}
									)
									if cnt == 1:
										agn.log (agn.LV_INFO, 'od', 'Created missing entry in maildrop_status_tbl')
									else:
										agn.log (agn.LV_ERROR, 'od', 'Failed to create new entry in maildrop_status_tbl: %s' % self.cursor.lastError ())
									self.cursor.sync (cnt == 1)
							if self.statusID is not None and maxBlocksize is not None and maxBlocksize > 0 and (blocksize is None or blocksize == 0 or blocksize > maxBlocksize):
								query = 'UPDATE maildrop_status_tbl SET blocksize = :blocksize WHERE status_id = :statusID'
								cnt = self.cursor.execute (query, {'blocksize': maxBlocksize, 'statusID': self.statusID})
								if cnt == 1:
									agn.log (agn.LV_INFO, 'od', 'Set blocksize from %r to %d' % (blocksize, maxBlocksize))
								else:
									agn.log (agn.LV_ERROR, 'od', 'Failed to set blocksize from %r to %d (affected %d rows): %s' % (blocksize, maxBlocksize, cnt, self.cursor.lastError ()))
								self.cursor.sync (cnt == 1)
			self.reset ()
		else:
			agn.log (agn.LV_ERROR, 'od', 'Failed to setup database')
		return self.statusID is not None

	def isBusy (self):
		"""Returns ``True'' if the mailing can not be started, else ``False''"""
		rc = True
		if self.statusID is None and not self.setup ():
			agn.log (agn.LV_WARNING, 'od', 'No valid entry in maildrop_status_tbl for mailingID %d found' % self.mailingID)
		elif not self.isopen ():
			agn.log (agn.LV_WARNING, 'od', 'Failed to setup database interface')
		else:
			r = self.cursor.querys ('SELECT genstatus, genchange FROM maildrop_status_tbl WHERE status_id = :statusID', {'statusID': self.statusID})
			if r is None:
				agn.log (agn.LV_INFO, 'od', 'No entry in maildrop_status_tbl for status_id %d found' % self.statusID)
			else:
				if r.genstatus in (1, 3):
					if self.genchange is not None and self.genchange == r.genchange:
						agn.log (agn.LV_DEBUG, 'od', 'Mailing is busy in startup due to unchanged genchange %s' % (self.genchange, ))
					else:
						self.genchange = None
						rc = False
				else:
					agn.log (agn.LV_INFO, 'od', 'Mailing is busy with genstatus %d since %s' % (r.genstatus, r.genchange))
			self.reset ()
		return rc

	@agn.Deprecated ()
	def start (self, merger = None):
		return self.starter (merger)
	
	def starter (self, merger = None, timeout = 30, retries = 3):
		"""Start the mailing on ``merger'' with ``timeout''
seconds for communcation timeout and ``retries'' attempts on
failure"""
		rc = False
		if not self.isBusy ():
			if not self.isopen ():
				agn.log (agn.LV_WARNING, 'od', 'Activity monitor disabled due to failure to connect to database: %s' % self.lastError ())
			else:
				r = self.cursor.querys ('SELECT genchange FROM maildrop_status_tbl WHERE status_id = :statusID', {'statusID': self.statusID})
				if r is not None and r.genchange is not None:
					self.genchange = r.genchange
					agn.log (agn.LV_DEBUG, 'od', 'Current genchange is %s' % (self.genchange, ))
				else:
					agn.log (agn.LV_WARNING, 'od', 'Failed to get current genchange')
			mailing = Mailing (merger, timeout = timeout, retries = retries)
			rc = mailing.demand (statusID = self.statusID, mailingID = self.mailingID, cursor = self.cursor)
		return rc
	
	def waitfor (self, timeout = 900, keepRunning = None):
		"""Wait for a mail generation to terminate and wait a
maximum of ``timeout'' seconds for termination. If ``keepRunning'' is
a callable, it is called serveral times during waiting for
termination. If this function returns ``False'', the waiting is
interrupted and the function returns. The return value is ``True'' if
the generation had terminated, else ``False''"""
		if self.isBusy ():
			rc = False
			dec = 1
			if keepRunning is not None:
				if callable (keepRunning):
					keepRunningCallback = keepRunning
				else:
					keepRunningCallback = lambda: keepRunning
			else:
				keepRunningCallback = None
			while not rc and timeout > 0 and (keepRunningCallback is None or keepRunningCallback ()):
				agn.log (agn.LV_DEBUG, 'od', 'Mailing %d is still busy, waiting for %2d:%02d min:sec' % (self.mailingID, timeout // 60, timeout % 60))
				time.sleep (dec)
				timeout -= dec
				if dec < 10:
					dec += 1
				rc = not self.isBusy ()
			if rc:
				agn.log (agn.LV_DEBUG, 'od', 'Mailing %d is now ready again' % self.mailingID)
			else:
				agn.log (agn.LV_WARNING, 'od', 'Mailing %d is still busy after reaching timeout' % self.mailingID)
		else:
			if self.statusID is None:
				rc = False
			else:
				rc = True
		return rc
#}}}
#
# (S)FTP communication {{{
class Filetransfer (object):
	"""Abstract class for Filetransfer

This is an abstract class (interface) for subclasses implementing a
real protocol. This class provides a class method which takes an URI
as argument (and keyword arguments passed to the real class found for
the URI). The URI is build of
<protocol>://[<user>[:<password>]@]<server>/[<path>]. Currently the
protocols "ftp", "ftps" and "sftp" (if the paramiko library is
availble) are implemented.

All implementations must implement the methods accessing the remote
system and must support the same arguments. They may also support
further keyword arguments to support protocol specific extenstions."""

	Implementations = {}
	@classmethod
	def getFiletransfer (cls, uri, **kws):
		"""Create an instance for ``uri''"""
		parm = urlparse.urlparse (uri)
		if parm.scheme not in cls.Implementations:
			raise agn.error ('%s: unsupported protocol for %s' % (parm.scheme, uri))
		ft = cls.Implementations[parm.scheme]
		netloc = parm.netloc
		host = None
		path = None
		credpos = netloc.rfind ('@')
		if credpos != -1:
			cred = netloc[:credpos].split (':', 1)
			host = netloc[credpos + 1:]
			kws['user'] = urlparse.unquote (cred[0])
			if len (cred) == 2:
				kws['password'] = urlparse.unquote (cred[1])
		else:
			host = netloc
		if not host:
			raise agn.error ('%s: no host found in URI' % uri)
		cparm = host.split (':', 1)
		if len (cparm) == 2:
			host = cparm[0]
			kws['port'] = int (cparm[1])
		if parm.path:
			path = parm.path
			if path.startswith ('/'):
				path = path[1:]
				if not path:
					path = None
			if path:
				kws['path'] = path
		return ft (host, **kws)
	
	def tobool (self, value):
		"""tries to convert ``value'' to a boolean value"""
		return agn.atob (value) if type (value) in (str, unicode) else value
	__unit = Unit ()
	def tonum (self, value):
		"""tries to convert ``value'' to a numeric (integer) value"""
		return self.__unit.parse (value, value)

	def __init__ (self, hostname, port = None, user = None, password = None, path = None, timeout = None, chunk = None, **kws):
		"""``hostname'' is the target server using ``port'' to
connect to with ``user'' and ``password'' to authenticate and change
to ``path'' after connection. Use ``timeout'' in seconds for
transmission and log (about) every ``chunk'' bytes transmitted."""
		self.hostname = hostname
		self.port = port
		self.user = user
		self.password = password
		self.path = path
		self.timeout = self.tonum (timeout)
		self.chunk = self.tonum (chunk)
		#
		self.last = None
		self.lastlog = None
		self.chunklog = None
		parts = self.hostname.split ('@')
		if len (parts) > 1:
			self.hostname = parts[-1]
			if self.user is None:
				self.user = '@'.join (parts[:-1])
		if type (self.port) in (str, unicode):
			try:
				try:
					self.port = socket.getservbyname (self.port, kws['protocol'])
				except KeyError:
					self.port = socket.getservbyname (self.port)
			except socket.error:
				self.port = int (self.port)
		self.name = self.__class__.__name__.lower ()
		
	def __enter__ (self):
		if self.isopen ():
			return self
		raise agn.error ('failed to open')

	def __exit__ (self, exc_type, exc_value, tb):
		self.close ()
		return False

	def __subclass (self, method):
		raise agn.error ('subclass is responsible to implement %s.%s' % (self.__class__.__name__, method))
	def close (self):
		"""close an open connection"""
		self.__subclass ('close')
	def connect (self):
		"""connect to remote server"""
		self.__subclass ('connect')
	def isopen (self):
		"""check if connection is established"""
		self.__subclass ('isopen')
	def listdir (self, path = None, **kws):
		"""list files in ``path'' (or current directory, if None"""
		self.__subclass ('listdir')

	def log (self, level, msg):
		"""log hook"""
		agn.log (level, self.name, msg)
		
	def logInit (self, chunk):
		"""log transfer for ``chunk'' (or self.chunk, if None) bytes"""
		self.last = 0
		self.lastlog = -1
		if chunk is not None:
			self.chunklog = chunk
		else:
			self.chunklog = self.chunk

	def logNext (self, current):
		"""check if a log entry should be written"""
		if self.last + self.chunklog <= current:
			self.last = (current // self.chunklog) * self.chunklog
			return True
		return False

	def logChunk (self, direction, current, total):
		"""write a log entry"""
		if self.chunklog is not None and self.lastlog != current and (current == total or (self.chunklog > 0 and self.logNext (current))):
			self.log (agn.LV_INFO, '%s: %s of %s bytes transferred' % (direction, agn.numfmt (current), agn.numfmt (total)))
			self.lastlog = current

	def resolve (self, source, target, isdir):
		"""Resolve the ``target'' path for ``source'' file using ``isdir'' to check if remote path is a directory"""
		if target is None:
			target = os.path.basename (source)
		elif isdir (target):
			target = os.path.join (target, os.path.basename (source))
		return target
	
	def retry (self, count, method, args, between = None, onFailure = None, onSuccess = None):
		"""Compatibility wrapper, use retry2 for future usage"""
		return self.retry2 (count, method, args, {}, between, onFailure, onSuccess)
	def retry2 (self, count, method, args, kws, between = None, onFailure = None, onSuccess = None):
		"""Retries ``count'' times to call ``method'' using
``args'' and ``kws'' arguments. Call ``between'' each try and at the
end call either ``onFailure'', if the call failed, otherwise
``onSuccess''"""
		rc = False
		for round in range (count):
			if round > 0:
				if between is not None and callable (between):
					if not between (round, *args, **kws):
						break
			try:
				rc = method (*args, **kws)
			except Exception as e:
				self.log (agn.LV_WARNING, 'Failed in operation during retry: %s' % str (e))
				rc = False
			if rc not in (False, None):
				break
		callback = onSuccess if rc else onFailure
		if callback is not None and callable (callback):
			callback (*args, **kws)
		return rc
		
	def get (self, remote, local = None, chunk = None):
		"""get ``remote'' to ``local'' file, log every ``chunk'' bytes"""
		self.__subclass ('get')
	def put (self, local, remote = None, chunk = None):
		"""put ``local'' to ``remote'' file, log every ``chunk'' bytes"""
		self.__subclass ('put')
	def remove (self, remote):
		"""remove ``remote'' file"""
		self.__subclass ('remove')
	def rename (self, old, new):
		"""rename remote ``old'' to ``new'' file"""
		self.__subclass ('rename')
	def mkdir (self, remote, mode = 0777):
		"""make directory ``remote'' directory using ``mode''"""
		self.__subclass ('mkdir')
	def rmdir (self, remote):
		"""remove ``remote'' directory"""
		self.__subclass ('rmdir')
	def chdir (self, remote):
		"""change directory to ``remote"""
		self.__subclass ('chdir')
	def getcwd (self):
		"""get current working remote directory"""
		self.__subclass ('getcwd')

class FTP (Filetransfer):
	"""class for simple access remote data via FTP"""
	def __init__ (self, *args, **kws):
		"""keyword parameter supported by this class:
- secure (bool, False): switch between FTP and FTPS
- pasv (bool, True): swtich passive mode"""
		Filetransfer.__init__ (self, *args, **kws)
		self.ftp = None
		self.secure = 'secure' in kws and self.tobool (kws['secure'])
		self.pasv = 'pasv' not in kws or self.tobool (kws['pasv'])

	def __debug (self, cmd, result):
		self.log (agn.LV_DEBUG, '%s: %s' % (cmd, result))
		return result

	def __call__ (self):
		if self.isopen ():
			return self.ftp
		raise agn.error ('ftp: failed to open')

	def close (self):
		if self.ftp is not None:
			self.__debug ('quit', self.ftp.quit ())
			self.ftp.close ()
			self.ftp = None
		return True

	def connect (self):
		self.close ()
		if self.secure:
			cls = ftplib.FTP_TLS
		else:
			cls = ftplib.FTP
		self.ftp = cls (self.hostname, self.user, self.password, timeout = self.timeout)
		if not self.user:
			self.__debug ('login', self.ftp.login ())
		if self.secure:
			self.__debug ('prot_p', self.ftp.prot_p ())
		self.ftp.set_pasv (self.pasv)
		if self.path:
			self.__debug ('cwd %s' % self.path, self.ftp.cwd (self.path))
		return self.ftp is not None
		
	def isopen (self):
		if self.ftp is None:
			return self.connect ()
		return True

	def listdir (self, path = None, full = False):
		rc = None
		if self.isopen ():
			if path is None:
				path = '.'
			if full:
				try:
					rc = []
					def collect (line):
						with agn.Ignore (ValueError):
							(param, filename) = line.split ('; ', 1)
							c = {'filename': filename}
							for e in param.split (';'):
								(var, val) = e.split ('=', 1)
								if var in ('sizd', 'size', 'modify'):
									val = int (val)
									if var == 'sizd':
										var = 'size'
									elif var == 'modify':
										var = 'mtime'
								elif var == 'UNIX.mode':
									val = int (val, 8)
									var = 'mode'
								c[var] = val
							with agn.Ignore (KeyError):
								if c['type'] == 'dir':
									c['mode'] |= stat.S_IFDIR
								else:
									c['mode'] |= stat.S_IFREG
							with agn.Ignore (KeyError):
								c['atime'] = c['mtime']
							with agn.Ignore (KeyError):
								if c['type'] in ('dir', 'file'):
									rc.append (agn.mutable (**c))

					self.__debug ('mlsd %s' % path, self.ftp.retrlines ('MLSD %s' % path, callback = collect))
				except ftplib.error_reply, e:
					self.log (agn.LV_INFO, 'Failed to use MLSD, try fallback to NLST: %s' % str (e))
					rc = None
			if rc is None:
				rc = []
				self.__debug ('nlst %s' % path, self.ftp.retrlines ('NLST %s' % path, callback = lambda l: rc.append (l)))
				if full:
					rc = [agn.mutable (filename = _f) for _f in rc]
		return rc
		
	def get (self, remote, local = None, chunk = None):
		if self.isopen ():
			local = self.resolve (remote, local, os.path.isdir)
			size = [0, self.__debug ('size %s' % remote, self.ftp.size (remote))]
			fd = open (local, 'wb')
			def collect (block):
				fd.write (block)
				size[0] += len (block)
				self.logChunk ('get', size[0], size[1])
			self.logInit (chunk)
			self.__debug ('retr %s' % remote, self.ftp.retrbinary ('RETR %s' % remote, callback = collect))
			fd.close ()
			return True
		return False

	def __isdir (self, remote):
		detail = self.listdir (remote, full = True)
		if detail and len (detail) == 1:
			with agn.Ignore (KeyError):
				return detail[0][1]['type'] == 'dir'
		#
		return False
		
	def put (self, local, remote = None, chunk = None):
		if self.isopen ():
			remote = self.resolve (local, remote, self.__sidir)
			size = [0, os.stat (local).st_size]
			fd = open (local, 'rb')
			def collect (block):
				size[0] += len (block)
				self.logChunk ('put', size[0], size[1])
			self.logInit (chunk)
			self.__debug ('stor %s' % remote, self.ftp.storbinary ('STOR %s' % remote, fd, callback = collect))
			fd.close ()
			return True
		return False
		
	def remove (self, remote):
		if self.isopen ():
			self.__debug ('delete %s' % remote, self.ftp.delete (remote))
			return True
		return False
		
	def rename (self, old, new):
		if self.isopen ():
			if self.__isdir (new):
				new = agn.mkpath (new, os.path.basename (old))
			self.__debug ('rename %s %s' % (old, new), self.ftp.rename (old, new))
			return True
		return False
		
	def mkdir (self, remote, mode = 0777):
		if self.isopen ():
			self.__debug ('mkd %s' % remote, self.ftp.mkd (remote, mode))
			return True
		return False
		
	def rmdir (self, remote):
		if self.isopen ():
			self.__debug ('rmd %s' % remote, self.ftp.rmd (remote))
			return True
		return False
		
	def chdir (self, remote):
		if self.isopen ():
			self.__debug ('cwd %s' % remote, self.ftp.cwd (remote))
			return True
		return False
		
	def getcwd (self):
		if self.isopen ():
			return self.__debug ('pwd', self.ftp.pwd ())
		return None

class FTPS (FTP):
	"""Shortcut for eagn.FTP (..., secure = True)"""
	def __init__ (self, *args, **kws):
		FTP.__init__ (self, *args, secure = True, **kws)

Filetransfer.Implementations['ftp'] = FTP
Filetransfer.Implementations['ftps'] = FTPS

try:
	import	paramiko
	
	class SFTP (Filetransfer):
		"""class for simple access remote data via SFTP"""
		def __init__ (self, *args, **kws):
			"""keyword parameter supported by this class:
- allowAgent (bool, False): allow credentials passed by ssh agent
- privateKey (str, None): use this as the private key
- privateKeyFile (str, None): use the content of this file as the private key"""
			Filetransfer.__init__ (self, *args, **kws)
			self.ssh = None
			self.sftp = None
			try:
				self.allowAgent = self.tobool (kws['allowAgent'])
			except KeyError:
				self.allowAgent = False
			try:
				self.privateKey = kws['privateKey']
			except KeyError:
				self.privateKey = None
			try:
				self.privateKeyFile = kws['privateKeyFile']
			except KeyError:
				self.privateKeyFile = None
		
		def __call__ (self):
			if self.isopen ():
				return self.sftp
			raise agn.error ('sftp: failed to open')

		def close (self):
			if self.ssh is not None:
				if self.sftp is not None:
					self.sftp.close ()
				self.ssh.close ()
			self.ssh = None
			self.sftp = None
			return True
		
		def connect (self):
			self.close ()
			self.ssh = paramiko.SSHClient ()
			self.ssh.load_system_host_keys ()
			connectData = {'hostname': self.hostname, 'allow_agent': self.allowAgent}
			if self.port is not None:
				connectData['port'] = self.port
			if self.user is not None:
				connectData['username'] = self.user
			if self.password is not None:
				connectData['password'] = self.password
				connectData['look_for_keys'] = False
			if self.privateKey is not None:
				connectData['pkey'] = self.privateKey
			if self.privateKeyFile is not None:
				connectData['key_filename'] = self.privateKeyFile
			if self.timeout is not None:
				connectData['timeout'] = self.timeout
			try:
				self.ssh.connect (**connectData)
				self.sftp = self.ssh.open_sftp ()
				if self.sftp is not None:
					if self.timeout is not None:
						chan = self.sftp.get_channel ()
						chan.settimeout (self.timeout)
					if self.path:
						self.sftp.chdir (self.path)
				else:
					self.log (agn.LV_ERROR, 'Failed to setup sftp over ssh')
					self.close ()
			except (paramiko.SSHException, socket.error, IOError) as e:
				self.log (agn.LV_ERROR, 'Failed to conenct to remote system: %s' % str (e))
				self.close ()
			except Exception as e:
				self.log (agn.LV_ERROR, 'Error during connecting to remote system: %s' % str (e))
				self.close ()
			return self.sftp is not None
		
		def isopen (self):
			if self.sftp is None:
				return self.connect ()
			return True

		def listdir (self, path = None, full = False):
			rc = None
			if self.isopen ():
				if path is None:
					path = '.'
				try:
					if full:
						rc = self.sftp.listdir_attr (path)
					else:
						rc = self.sftp.listdir (path)
				except IOError as e:
					self.log (agn.LV_ERROR, 'Failed to list remote directory %s: %s' % (path, str (e)))
			return rc
		
		def get (self, remote, local = None, chunk = None):
			if self.isopen ():
				if local is None:
					local = os.path.basename (remote)
				elif os.path.isdir (local):
					local = agn.mkpath (local, os.path.basename (remote))
				self.logInit (chunk)
				try:
					self.sftp.get (remote, local, callback = lambda c, t: self.logChunk ('get', c, t))
					return True
				except IOError as e:
					self.log (agn.LV_ERROR, 'Failed to fetch remote file %s to local file %s: %s' % (remote, local, str (e)))
					if os.path.isfile (local):
						try:
							os.unlink (local)
							self.log (agn.LV_INFO, 'Removed corrupt local file %s' % local)
						except OSError as e:
							self.log (agn.LV_ERROR, 'Failed to remove corrupt local file %s: %s' % (local, str (e)))
			return False

		def __isdir (self, remote):
			try:
				st = self.sftp.stat (remote)
				return stat.S_ISDIR (st.st_mode)
			except IOError as e:
				self.log (agn.LV_DEBUG, 'isdir: failed to stat remote %s: %s' % (remote, str (e)))
				return False

		def __isfile (self, remote):
			try:
				st = self.sftp.stat (remote)
				return stat.S_ISREG (st.st_mode)
			except IOError as e:
				self.log (agn.LV_DEBUG, 'isfile: failed to stat remote %s: %s' % (remote, str (e)))
				return False
		
		def put (self, local, remote = None, chunk = None):
			if self.isopen ():
				if remote is None:
					remote = os.path.basename (local)
				elif self.__isdir (remote):
					remote = agn.mkpath (remote, os.path.basename (local))
				self.logInit (chunk)
				try:
					self.sftp.put (local, remote, callback = lambda c, t: self.logChunk ('put', c, t))
					return True
				except IOError as e:
					self.log (agn.LV_ERROR, 'Failed to store local file %s to remote file %s: %s' % (local, remote, str (e)))
					if self.__isfile (remote):
						try:
							self.sftp.remove (remote)
							self.log (agn.LV_INFO, 'Removed corrupt remote file %s' % remote)
						except IOError as e:
							self.log (agn.LV_ERROR, 'Failed to remove corrupt remote file %s: %s' % (remote, str (e)))
			return False
		
		def remove (self, remote):
			if self.isopen ():
				try:
					self.sftp.remove (remote)
					return True
				except IOError as e:
					self.log (agn.LV_ERROR, 'Failed to remove remote file %s: %s' % (remote, str (e)))
			return False
		
		def rename (self, old, new):
			if self.isopen ():
				if self.__isdir (new):
					new = agn.mkpath (new, os.path.basename (old))
				try:
					self.sftp.rename (old, new)
					return True
				except IOError as e:
					self.log (agn.LV_ERROR, 'Failed to rename remote %s to %s: %s' % (old, new, str (e)))
			return False
		
		def mkdir (self, remote, mode = 0777):
			if self.isopen ():
				try:
					self.sftp.mkdir (remote, mode)
					return True
				except IOError as e:
					self.log (agn.LV_ERROR, 'Failed to create remote directory %s: %s' % (remote, str (e)))
			return False
		
		def rmdir (self, remote):
			if self.isopen ():
				try:
					self.sftp.rmdir (remote)
					return True
				except IOError as e:
					self.log (agn.LV_ERROR, 'Failed to remove remote directory %s: %s' % (remote, str (e)))
			return False
		
		def chdir (self, remote):
			if self.isopen ():
				try:
					self.sftp.chdir (remote)
					return True
				except IOError as e:
					self.log (agn.LV_ERROR, 'Failed to chdir on remote to %s: %s' % (remote, str (e)))
			return False
		
		def getcwd (self):
			if self.isopen ():
				try:
					return self.sftp.getcwd ()
				except IOError as e:
					self.log (agn.LV_ERROR, 'Failed to get current remote working directory: %s' % str (e))
			return None
			
	Filetransfer.Implementations['sftp'] = SFTP
except ImportError:
	SFTP = None

class FiletransferRetry (object):
	"""Wrapper to handle retries on communications"""
	def __init__ (self, ft, count = 3, delay = 0):
		"""``ft'' is an instance of a subclass of Filetransfer
and ``count'' is the amount of retries to execute for each method and
delay the time between two retries in seconds"""
		self.ft = ft
		self.count = count
		self.delay = delay

	def onStart (self, method, *args, **kws):
		pass
	
	def onEnd (self, rc, *args, **kws):
		pass
		
	def between (self, round, *args, **kws):
		if self.delay > 0:
			time.sleep (self.delay)
		return True
	
	def onFailure (self, *args, **kws):
		pass
	
	def onSuccess (self, *args, **kws):
		pass
	
	def __retry (self, method, args, kws):
		return self.ft.retry2 (self.count, method, args, kws, self.between, self.onFailure, self.onSuccess)
	
	def __getattr__ (self, name):
		method = getattr (self.ft, name)
		if callable (method):
			def wrapper (*args, **kws):
				self.onStart (name, *args, **kws)
				rc = self.__retry (method, args, kws)
				self.onEnd (rc, *args, **kws)
				return rc
			return wrapper
		return method

class FiletransferRetryStandard (FiletransferRetry):
	def __init__ (self, *args, **kws):
		super (FiletransferRetryStandard, self).__init__ (*args, **kws)
		self.method = None
		self.cwd = None
		
	def onStart (self, method, *args, **kws):
		super (FiletransferRetryStandard, self).onStart (method, *args, **kws)
		self.method = method
	
	def onEnd (self, rc, *args, **kws):
		if self.method in ('connect', 'chdir'):
			try:
				self.cwd = self.ft.getcwd ()
				if self.cwd is None:
					self.ft.chdir ('.')
					self.cwd = self.ft.getcwd ()
			except Exception:
				self.cwd = None
		self.method = None
		super (FiletransferRetryStandard, self).onEnd (rc, *args, **kws)
	
	def between (self, round, *args, **kws):
		time.sleep (round)
		if round + 1 < self.count and self.cwd is not None and self.method not in ('connect', 'chdir', 'close'):
			try:
				self.ft.close ()
				if self.delay > 0:
					time.sleep (self.delay)
				if self.ft.connect ():
					if self.ft.chdir (self.cwd):
						newCwd = self.ft.getcwd ()
						if newCwd != self.cwd:
							raise agn.error ('failed to chdir after reconnect (expected "%s", found "%s"' % (self.cwd, newCwd))
					else:
						raise agn.error ('failed to chdir to "%s" after reconnect' % self.cwd)
				else:
					raise agn.error ('failed to reconnect')
			except:
				self.cwd = None
				raise
		return super (FiletransferRetryStandard, self).between (round, *args, **kws)

def filetransfer (uri, **kws):
	"""Create an instance of filetransfer depending of URI and use a retry wrapper"""
	retry = agn.mutable (
		count = 3,
		delay = 0,
		cls = FiletransferRetryStandard
	)
	#
	def setCount (newCount):
		retry.count = int (newCount)
	def setDelay (newDelay):
		retry.delay = int (newDelay)
	def setClass (newClass):
		retry.cls = newClass
	#
	forRetry = [
		('retryCount', setCount),
		('retryDelay', setDelay),
		('retryClass', setClass),
	]
	for (key, method) in forRetry:
		if key in kws:
			method (kws[key])
			del kws[key]
	ft = Filetransfer.getFiletransfer (uri, **kws)
	return retry.cls (ft, count = retry.count)
#}}}
#
# Line based protocol framework {{{
#
class Lineprotocol (object):
	"""Framework for line based internet protocols

This can be used to implement line based internet protocols. It also
supports communication over SSL."""
	try:
		import	ssl
	except ImportError:
		ssl = None
	def __init__ (self, host, port, secure, timeout = None, retry = False):
		"""``host'' is the host to connect to using ``port''
(either numeric or its textual representation) using SSL directly
after connection, if ``secure'' is True and using ``timeout'' seconds
during communication and connecting. If ``retry'' is True then a
reconnect is tried, if communication channel is lost."""
		self.host = host
		if type (port) in (int, long):
			self.port = int (port)
		elif type (port) in (str, unicode):
			try:
				self.port = socket.getservbyname (port, 'tcp')
			except:
				raise agn.error ('port "%s" not assigned' % port)
		else:
			raise agn.error ('port is of unsupported type %r' % (type (port), ))
		self.secure = secure
		self.timeout = timeout
		self.retry = retry
		self.otimeout = None
		self.sd = None
		self.isSSL = False
		self.com = []
	
	def __enter__ (self):
		if self.timeout is not None:
			self.otimeout = socket.getdefaulttimeout ()
			socket.setdefaulttimeout (self.timeout)

	def __exit__ (self, exc_type, exc_value, tb):
		if self.timeout is not None:
			socket.setdefaulttimeout (self.otimeout)
		return False
	
	def __call__ (self, id, msg):
		self.com.append ('%s: %s' % (id, msg))
		self.log (id, msg)

	def log (self, id, msg):
		"""Can be overwritten to collect logging information"""
		pass

	def sslOptions (self):
		"""Determinates available SSL versions and select highest version"""
		rc = {}
		if self.ssl is not None:
			rc['ssl_version'] = max ([self.ssl.__dict__[_p] for _p in dir (self.ssl) if _p.startswith ('PROTOCOL_')])
		return rc
		
	def tossl (self):
		"""Switch channel to SSL"""
		if self.ssl is not None and self.isconnected () and not self.isSSL:
			try:
				nsd = self.ssl.wrap_socket (self.sd, **self.sslOptions ())
				self.sd = nsd
				self.isSSL = True
				self ('2SSL', 'enabled')
			except Exception as e:
				self ('2SSL', 'failed due to %s' % str (e))
		return self.isSSL

	def isconnected (self):
		"""Returns True, if connection is already established, else False"""
		return self.sd is not None
			
	def connect (self):
		"""Connects to remote system and returns True, if connection had been established, else False"""
		if not self.isconnected ():
			with self:
				try:
					self ('CONN', 'Try to establish connection to %s:%d' % (self.host, self.port))
					self.sd = socket.socket (socket.AF_INET, socket.SOCK_STREAM, 0)
					self.sd.connect ((self.host, self.port))
					self ('CONN', 'established to %s:%d' % (self.host, self.port))
					if self.secure:
						if not self.tossl ():
							self ('CONN', 'SSL failed')
							self.disconnect ()
						else:
							self ('CONN', 'SSL established')
				except Exception as e:
					self ('CONN', 'failed due to %s to %s:%d' % (str (e), self.host, self.port))
					if self.isconnected ():
						self.disconnect ()
		return self.isconnected ()
	
	def disconnect (self):
		"""Closes an existing connection"""
		if self.isconnected ():
			with self:
				try:
					self.sd.close ()
					self ('DISC', 'disconnected from %s:%d' % (self.host, self.port))
				except Exception as e:
					self ('DISC', 'failed due to %s from %s:%d' % (str (e), self.host, self.port))
			self.sd = None
			self.isSSL = False
	
	def reconnect (self):
		"""Cloeses an exitsing connection and tries to reopen it"""
		self.disconnect ()
		return self.connect ()

	def readline (self, retry = None):
		"""Read a line from remote, if ``retry'' is None, the
global retry value is used as set during creation of the instance,
otherwise the local one is used where ``False'' means no reconnection
is tries on failure and True a reconnection is tried, if connection is
lost"""
		rc = None
		tries = 0 if (retry if retry is not None else self.retry) else 1
		while rc is None and tries < 2:
			tries += 1
			if self.isconnected ():
				with self:
					try:
						ch = None
						s = ''
						while ch != '\n':
							ch = self.sd.recv (1)
							if ch == '':
								raise EOFError
							s += ch
						self ('READ', s.rstrip ())
						rc = s
					except EOFError as e:
						self ('READ', 'EOF')
						self.disconnect ()
					except Exception as e:
						print (e, str (e))
						self ('READ', 'Failed due to %s' % str (e))
			if rc is None and tries < 2:
				self.reconnect ()
		return rc
	
	def writeline (self, s, retry = None):
		"""Writes a line to remote, if ``retry'' is None, the
global retry value is used as set during creation of the instance,
otherwise the local one is used where ``False'' means no reconnection
is tries on failure and True a reconnection is tried, if connection is
lost"""
		rc = False
		tries = 0 if (retry if retry is not None else self.retry) else 1
		if not s.endswith ('\r\n'):
			if s.endswith ('\n') or s.endswith ('\r'):
				s = '%s\r\n' % s[:-1]
			else:
				s += '\r\n'
		while not rc and tries < 2:
			tries += 1
			if self.isconnected ():
				with self:
					try:
						self.sd.send (s)
						self ('SEND', s.rstrip ())
						rc = True
					except Exception as e:
						self ('SEND', 'Failed due to %s' % str (e))
			if not rc and tries < 2:
				self.reconnect ()
		return rc
	
	def communicate (self, chain, retry = None, statusOnly = False):
		"""Communications a list of output and expected answers with remoite.

``chain'' is a list of output and expected answers (which must start
with a '<' sign and can be a single string or a regular expression, if
enclosed in slashes, e.g.:

lp = eagn.Lineprotocol ('some.host.com', 'smtp')
lp.connect ()
lp.communicate (['</2.*/', 'HELO %s' % socket.getfqdn ()', '</2.*/'])
		
If ``retry'' is None, the global retry value is used as set during
creation of the instance, otherwise the local one is used where
``False'' means no reconnection is tries on failure and True a
reconnection is tried, if connection is lost.

If ``statusOnly'' is True, a boolean value is returned, ``True'' if
communication had been completed, ``False'' otherwise. If
``statusOnly'' is False a list of return values for each communication
part is returned."""
		rc = []
		clen = len (chain)
		while chain:
			cur = chain.pop (0)
			if cur.startswith ('<'):
				line = self.readline (retry = retry)
				if line is None:
					break
				line = line.rstrip ()
				pattern = cur[1:]
				if len (pattern) > 2 and pattern.startswith ('/') and pattern.endswith ('/'):
					regex = re.compile (pattern[1:-1])
					rc.append (regex.match (line))
				else:
					rc.append (pattern == line)
			else:
				rc.append (self.writeline (cur[1:] if cur.startswith ('\\') else cur, retry = retry))
			if not rc[-1]:
				break
		return rc if not statusOnly else clen == len (filter (None, rc))
#}}}
#
