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
#
import	os, stat, errno, logging
from	typing import Any, Callable, Optional, Union
from	typing import Dict, List, NamedTuple, Set
from	.definitions import base
from	.exceptions import error
from	.id import IDs
from	.io import create_path
from	.log import log
#
__all__ = ['Sanity', 'Report', 'File', 'Symlink']
#
logger = logging.getLogger (__name__)
#
class Report (NamedTuple):
	info: List[str]
	error: List[str]
	warning: List[str]

class File (NamedTuple):
	name: str
	content: Optional[str] = None
	optional: Optional[bool] = None
	gid: Union[None, int, str] = None
	uid: Union[None, int, str] = None
	isdir: Optional[bool] = None
	isfile: Optional[bool] = None
	issuid: Optional[bool] = None
	issgid: Optional[bool] = None
	islink: Optional[bool] = None
	mode: Optional[int] = None
	mask: Optional[int] = None

class Symlink (NamedTuple):
	path: str
	target: str

class Sanity (IDs):
	"""Framewaork for sanity checks

This is the base to perform sanity checks. This class should be
subclassed and extended to perform custom checks. If the included
checks are sufficient, this class can be used directly."""
	__slots__: List[str] = []
	def preproc (self, r: Report, area: str, *args: Any) -> None:
		pass
	def postproc (self, r: Report, area: str, *args: Any) -> None:
		pass
	def __init__ (self,
		directories: Optional[List[str]] = None,
		files: Optional[List[File]] = None,
		symlinks: Optional[List[Symlink]] = None,
		executables: Optional[List[str]] = None,
		runas: Union[None, int, str] = None,
		startas: Union[None, int, str] = None,
		umask: Optional[int] = None,
		modules: Optional[List[str]] = None,
		checks: Optional[List[Callable[[Report], None]]] = None
	) -> None:
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
instance of Report (info = [], error = [], warning = []) and should be
filled by the check according to its results. While both are printed,
only ``errors'' leading to an exit code not equal 0. """
		super ().__init__ ()
		r = Report ([], [], [])
		if startas is not None:
			self.preproc (r, 'startas', startas)
			pw = self.get_user ()
			self.postproc (r, 'startas', startas, pw)
			if pw is None or pw.pw_name != startas:
				if pw is None:
					myself = '#%d' % os.getuid ()
				else:
					myself = pw.pw_name
				raise error ('startas: start as user "%s", not "%s"' % (startas, myself))
		if runas is not None:
			self.preproc (r, 'runas', runas)
			pw = self.get_user (runas)
			self.postproc (r, 'runas', runas, pw)
			if pw is None:
				raise error ('runas: failed to find user for %r' % runas)
			try:
				if os.getgid () != pw.pw_gid:
					os.setgid (pw.pw_gid)
				if os.getuid () != pw.pw_uid:
					os.setuid (pw.pw_uid)
				os.environ['HOME'] = pw.pw_dir
				os.environ['USER'] = pw.pw_name
				os.chdir (pw.pw_dir)
			except OSError as e:
				raise error ('runas: failed to setup user %r: %r' % (runas, e.args))
		if umask is not None:
			try:
				self.preproc (r, 'umask', umask)
				omask = os.umask (umask)
				self.postproc (r, 'umask', umask, omask)
			except OSError as e:
				raise error ('umask: failed to set umask to %03o: %r' % (umask, e.args))
		if directories is not None:
			for d in directories:
				path = os.path.join (base, d)
				try:
					self.preproc (r, 'directory', path, d)
					create_path (path)
					self.postproc (r, 'directory', path, d)
				except error as e:
					r.error.append ('Directory: Failed to access/create %s: %s' % (path, e))
		if files is not None:
			for f in files:
				name = f.name if f.name.startswith (os.path.sep) else os.path.join (base, f.name)
				self.preproc (r, 'file', f)
				if f.content is not None and not os.path.isfile (name):
					try:
						with open (name, 'w') as fd:
							fd.write (f.content)
						if f.mode is not None:
							os.chmod (name, f.mode)
					except IOError as e:
						r.error.append ('File: %s failed to create missing file: %s' % (name, e))
					except OSError as e:
						r.error.append ('File: %s failed to modify newly created file: %s' % (name, e))
				self.postproc (r, 'file', f)
				try:
					st = os.stat (name)
					if f.gid is not None:
						gr = self.get_group (f.gid)
						if gr is not None:
							if gr.gr_gid != st.st_gid:
								r.error.append ('File: %s expected to have group id %d (%r), but has %d' % (name, gr.gr_gid, f.gid, st.st_gid))
						else:
							r.error.append ('File: no group "%s" found for %s' % (f.gid, name))
					#
					if f.uid is not None:
						ui = self.get_user (f.uid)
						if ui is not None:
							if ui.pw_uid != st.st_uid:
								r.error.append ('File: %s expected to have user id %d (%r) but has %d' % (name, ui.pw_uid, f.uid, st.st_uid))
						else:
							r.error.append ('File: no user "%s" found for %s' % (f.uid, name))
					#
					lst = os.lstat (name)
					for (flag, value, what) in [
						(f.isdir, stat.S_ISDIR (st.st_mode), 'directory'),
						(f.isfile, stat.S_ISREG (st.st_mode), 'regular file'),
						(f.issuid, (stat.S_ISUID & st.st_mode) == stat.S_ISUID, 'set uid'),
						(f.issgid, (stat.S_ISGID & st.st_mode) == stat.S_ISGID, 'set gid'),
						(f.islink, stat.S_ISLNK (lst.st_mode), 'symbolic link')
					]:
						if flag is not None and flag != value:
							r.error.append ('File: %s is %sa %s, but expected to be %sa %s' % (name, '' if value else 'NOT ', what, '' if flag else 'NOT ', what))
					#
					if f.mode is not None:
						if stat.S_IMODE (st.st_mode) != f.mode:
							r.error.append ('File: %s has mode %o but expected %o' % (name, stat.S_IMODE (st.st_mode), f.mode))
					#
					if f.mask is not None:
						if stat.S_IMODE (st.st_mode) & f.mask != f.mask:
							r.error.append ('File: %s does not match file mask %o (%o results in %o)' % (name, f.mask, stat.S_IMODE (st.st_mode), stat.S_IMODE (st.st_mode) & f.mask))
				except OSError as e:
					if e.args[0] == errno.ENOENT:
						if f.optional is None or not f.optional:
							r.error.append ('File: %s does not exist' % name)
					else:
						r.error.append ('File: %s raises unexpected error: %s' % (name, e))
		if symlinks is not None:
			for symlink in symlinks:
				need_create = False
				try:
					self.preproc (r, 'symlink', 'stat', symlink)
					st = os.lstat (symlink.path)
					self.postproc (r, 'symlink', 'stat', symlink, st)
					if not stat.S_ISLNK (st.st_mode):
						r.error.append ('Symlink: %s is existing, but is no symlink' % symlink.path)
					else:
						old = os.readlink (symlink.path)
						if old != symlink.target:
							os.unlink (symlink.path)
							need_create = True
				except OSError as e:
					if e.args[0] == errno.ENOENT:
						need_create = True
					else:
						r.error.append ('Symlink: %s failed to determinate current state: %s' % (symlink.path, str (e)))
				if need_create:
					try:
						self.preproc (r, 'symlink', 'create', symlink)
						create_path (os.path.dirname (symlink.path))
						os.symlink (symlink.target, symlink.path)
						self.postproc (r, 'symlink', 'create', symlink)
					except error as e:
						r.error.append ('Symlink: %s failed to create path to file: %s' % (symlink.path, e))
					except OSError as e:
						r.error.append ('Symlink: %s failed to create symlink: %s' % (symlink.path, e))
		if executables is not None:
			try:
				path = os.environ['PATH']
				pmap: Dict[str, str] = {}
				seen: Set[str] = set ()
				for directory in ((_d if _d else '.') for _d in path.split (':')):
					if directory not in seen:
						seen.add (directory)
						if os.path.isdir (directory):
							try:
								for filename in os.listdir (directory):
									if filename not in pmap:
										pmap[filename] = os.path.join (directory, filename)
							except OSError as e:
								r.error.append ('Exec: Failed to read directory %s: %s' % (directory, e))
				for executable in executables:
					try:
						path = executable if executable.startswith (os.path.sep) else pmap[executable]
						if not os.access (path, os.X_OK):
							r.error.append ('Exec: %s is not executable' % executable)
					except KeyError:
						r.error.append ('Exec: %s not found in path' % executable)
			except KeyError:
				r.error.append ('Exec: No $PATH variable found')
		if modules is not None:
			for module in modules:
				try:
					self.preproc (r, 'module', module)
					__import__ (module)
					self.postproc (r, 'module', module)
				except ImportError as e:
					r.error.append ('Module: %s not importable: %r' % (module, e.args))
		if checks is not None:
			for check in checks:
				try:
					check (r)
				except Exception as e:
					r.error.append ('Check: %r failed: %r' % (check, e.args))
		#
		with log ('sanity'):
			if r.info:
				print ('Informative messages during sanity check:')
				for line in r.info:
					print (line)
					logger.info (line)
			if r.warning:
				print ('*** Warning report for sanity check ***')
				for line in r.warning:
					print (line)
					logger.warning (line)
				if not r.error:
					print ('*** will continue execution ***')
			if r.error:
				print ("*** Error report for sanity check ***")
				for line in r.error:
					print (line)
					logger.error (line)
				raise error ('Failed in sanity check')
