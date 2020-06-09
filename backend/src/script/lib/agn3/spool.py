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
from	__future__ import annotations
import	os, logging, errno, time, fcntl
from	functools import partial
from	typing import Optional
from	typing import Generator, List, Set
from	.definitions import base
from	.exceptions import error
from	.io import create_path
from	.pattern import isnum
from	.tools import atoi
#
__all__ = ['Mailspool']
#
logger = logging.getLogger (__name__)
#
class Mailspool:
	"""Handling mail spool directories

This can be used by a process which handles incoming mails asynchron.
A common way to use this class is:

ms = agn3.spool.Mailspool ('path/to/spool')
ms.update_procmailrc ()
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
	__slots__ = ['spooldir', 'incoming', 'workspace', 'store', 'quarantine', 'worksize', 'mode', 'scan', 'store_size', 'workspaces', 'path_checked']
	class Workspace:
		"""A temporary workspace

Whenever a mail batch should be processed a temporary workspace is
created and all relevant information is stored in an instance of this
class. One should use this class as an iterator which returns the full
path to the next mail file to process."""
		__slots__ = ['path', 'ref', 'files', 'cur']
		def __init__ (self, path: str, ref: Mailspool) -> None:
			self.path = path
			self.ref = ref
			self.files = sorted (os.listdir (self.path), key = atoi)
			self.cur: Optional[str] = None
		
		def __iter__ (self) -> Generator[str, None, None]:
			while self.files:
				self.cur = os.path.join (self.path, self.files.pop (0))
				yield self.cur

		def __timestamp (self, daybased: bool) -> str:
			now = time.localtime ()
			if daybased:
				return '%04d%02d%02d' % (now.tm_year, now.tm_mon, now.tm_mday)
			else:
				return '%02d%02d%02d' % (now.tm_hour, now.tm_min, now.tm_sec)
		
		def done (self, force: bool = False) -> None:
			"""Cleanup and remove the workspace"""
			files = os.listdir (self.path)
			if not files or force:
				for fname in files:
					path = os.path.join (self.path, fname)
					try:
						os.unlink (path)
						logger.debug (f'{path}: removed')
					except OSError as e:
						logger.exception (f'{path}: failed to remove: {e}')
				try:
					os.rmdir (self.path)
					logger.debug (f'{self.path}: removed workspace')
				except OSError as e:
					logger.exception (f'{self.path}: failed to remove workspace: {e}')
		
		def abort (self) -> None:
			"""Abort processing of a workspace and move it to quarantine"""
			basedir = os.path.join (self.ref.quarantine, os.path.basename (self.path))
			destdir = basedir
			n = 0
			while os.path.isdir (destdir) and n < 65536:
				n += 1
				destdir = f'{basedir}.{n:06d}'
			try:
				os.rename (self.path, destdir)
				logger.info (f'{self.path}: aborted workspace and moved to {destdir}')
			except OSError as e:
				logger.exception (f'{self.path}: failed to remove workspace ({e}), try to remove it')
				self.done (True)

		def fail (self) -> None:
			"""Mark processing of a file as failed and move it to quarantine"""
			if self.cur is not None:
				destdir = os.path.join (self.ref.quarantine, self.__timestamp (True))
				if self.ref.check_path (destdir):
					bname = '%s-%s' % (self.__timestamp (False), os.path.basename (self.cur))
					dest = os.path.join (destdir, bname)
					n = 0
					while os.path.isfile (dest) and n < 128:
						n += 1
						dest = os.path.join (destdir, '%s-%d' % (bname, n))
					try:
						os.rename (self.cur, dest)
						logger.debug (f'{self.cur}: moved to {dest}')
					except OSError as e:
						logger.exception (f'{self.cur}: failed to move to {dest}: {e}')
						try:
							os.unlink (self.cur)
							logger.debug (f'{self.cur}: removed')
						except OSError as e:
							logger.exception (f'{self.cur}: even failed to remove: {e}')
			else:
				logger.debug ('no current file selected')
				
		def success (self, sender: Optional[str] = None) -> None:
			"""Mark processing of a file as successful and move it to store"""
			if self.cur is not None:
				dest = os.path.join (self.ref.store, '%s-mbox' % self.__timestamp (True))
				try:
					with open (self.cur, 'rb') as fdi, open (dest, 'ab') as fdo:
						if self.ref.store_size and self.ref.store_size < 65536:
							bufsize = self.ref.store_size
						else:
							bufsize = 65536
						fcntl.lockf (fdo, fcntl.LOCK_EX)
						if sender is not None:
							fdo.write (('From %s  %s\n' % (sender, time.ctime ())).encode ('UTF-8'))
						copied = 0
						for buf in iter (partial (fdi.read, bufsize), b''):
							fdo.write (buf)
							copied += len (buf)
							if self.ref.store_size and copied >= self.ref.store_size:
								fdo.write (f'\n\n--- Message truncated after {copied:,d} bytes\n\n'.encode ('UTF-8'))
								break
						fcntl.lockf (fdo, fcntl.LOCK_UN)
						try:
							os.unlink (self.cur)
							logger.debug (f'{self.cur}: removed')
						except OSError as e:
							logger.exception (f'{self.cur}: failed ro remove: {e}')
				except IOError as e:
					logger.exception (f'{self.cur}: failed to open: {e}')
					self.fail ()
			else:
				logger.debug ('no current file selected')
			
	def __init__ (self,
		spooldir: str,
		incoming: Optional[str] = None,
		workspace: Optional[str] = None,
		store: Optional[str] = None,
		quarantine: Optional[str] = None,
		worksize: Optional[int] = None,
		mode: Optional[int] = None,
		scan: bool = True,
		store_size: Optional[int] = None
	):
		"""Use ``spooldir'' as the default base path for the
mail spool base directory. You can set ``incoming'', ``workspace'',
``store'' and ``quarantine'' to custom values or let the class create
them below the ``spooldir'' (recommended). ``worksize'' is the maximum
number of mail files per created workspace. ``mode'' is the file mode
when creating the workspace. If ``scan'' is True, then already
existing workspace directories are promopted for further processing,
otherwise they are ignored. ``store_size'' can be set to a maximum
number of bytes when storing a successful mail. This can be used to
limit hard drive usage in case of high volume mail traffic."""
		self.spooldir = spooldir
		self.incoming = incoming if incoming is not None else os.path.join (self.spooldir, 'incoming')
		self.workspace = workspace if workspace is not None else os.path.join (self.spooldir, 'workspace')
		self.store = store if store is not None else os.path.join (self.spooldir, 'store')
		self.quarantine = quarantine if quarantine is not None else os.path.join (self.spooldir, 'quarantine')
		self.worksize = worksize if worksize is not None else 1000
		self.mode = mode if mode is not None else 0o700
		self.scan = scan
		self.store_size = store_size
		self.workspaces: List[str] = []
		self.path_checked: Set[str] = set ()
		for path in [self.spooldir, self.incoming, self.workspace, self.store, self.quarantine]:
			self.check_path (path)
	
	def __iter__ (self) -> Generator[Mailspool.Workspace, None, None]:
		if self.scan:
			self.scan_workspaces ()
		self.create_workspaces ()
		#
		while self.workspaces:
			ws = self.Workspace (self.workspaces.pop (0), self)
			if ws.files:
				logger.debug (f'{ws.path}: provided')
				yield ws
			else:
				logger.debug (f'{ws.path}: skipped due to empty workspace')
			ws.done ()
	
	def check_path (self, path: str) -> bool:
		"""Check and create missing ``path''"""
		if path and not path in self.path_checked:
			try:
				if not os.path.isdir (path):
					create_path (path, self.mode)
					logger.info (f'{path}: created missing directory')
				self.path_checked.add (path)
			except error as e:
				logger.exception (f'{path}: failed to create missing directory: {e}')
				return False
		return True
	
	def scan_workspaces (self) -> None:
		"""Scan for existing workspaces"""
		self.workspaces.clear ()
		for fname in os.listdir (self.workspace):
			path = os.path.join (self.workspace, fname)
			if os.path.isdir (path):
				self.workspaces.append (path)
				logger.debug (f'{path}: found workspace')
		if self.workspaces:
			logger.info ('found {count} available workspaces'.format (count = len (self.workspaces)))
			self.workspaces.sort ()
	
	def create_workspaces (self) -> None:
		"""Create new worksapces from incoming files"""
		cur: Optional[str] = None
		count = 0
		idx = 0
		lastts = ''
		for fname in sorted ((_f for _f in os.listdir (self.incoming) if isnum (_f)), key = lambda f: int (f)):
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
				cur = os.path.join (self.workspace, '%s%03d' % (ts, idx))
				if os.path.isdir (cur):
					cur = None
				else:
					create_path (cur)
					self.workspaces.append (cur)
					logger.info (f'{cur}: created new workspace')
			src = os.path.join (self.incoming, fname)
			dst = os.path.join (cur, fname)
			try:
				os.rename (src, dst)
				count += 1
				logger.debug (f'{cur}: added {fname} from {self.incoming}')
			except OSError as e:
				logger.exception (f'{cur}: failed to move {fname} from {self.incoming} to {dst}: {e}')
			if self.worksize and count >= self.worksize:
				cur = None
				count = 0
	#
	# Tools not directly required for spool handling
	#
	def update_procmailrc (self,
		content: Optional[str] = None,
		procmailrc: str = os.path.join (base, '.procmailrc')
	) -> None:
		"""Create a $HOME/.procmailrc to redirect the mails to
the incoming spool directory. If ``content'' is None, a suitable
content is created, otherwise it must be provided. ``procmailrc'' is
the path to the target file."""
		if content is None:
			content = '# created by agn3.spool.Mailspool\n:0:\n%s/.\n' % self.incoming
		try:
			with open (procmailrc, 'r') as fd:
				ocontent = fd.read ()
		except IOError as e:
			if e.args[0] != errno.ENOENT:
				logger.warning ('Failed to read "%s": %s' % (procmailrc, e))
			ocontent = ''
		if ocontent != content:
			try:
				with open (procmailrc, 'w') as fd:
					fd.write (content)
				os.chmod (procmailrc, 0o600)
				logger.info ('Created new/Updated procmailrc file "%s".' % procmailrc)
			except (IOError, OSError) as e:
				logger.exception ('Failed to install procmailrc file "%s": %s' % (procmailrc, e))
	
