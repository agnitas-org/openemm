####################################################################################################################################################################################################################################################################
#                                                                                                                                                                                                                                                                  #
#                                                                                                                                                                                                                                                                  #
#        Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   #
#                                                                                                                                                                                                                                                                  #
#        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    #
#        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           #
#        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            #
#                                                                                                                                                                                                                                                                  #
####################################################################################################################################################################################################################################################################
#
from	__future__ import annotations
import	os, errno, logging, time
from	types import TracebackType
from	typing import Optional
from	typing import Type
from	.definitions import base, program
from	.exceptions import LockError, error
from	.ignore import Ignore
from	.io import create_path
from	.log import log_limit
#
__all__ = ['Lock']
#
logger = logging.getLogger (__name__)
#
class Lock:
	__slots__ = ['id', 'lazy', 'silent', 'block', 'lockpath', 'lockpid', 'is_locked']
	lock_directory = os.environ.get ('LOCK_HOME', os.path.join (base, 'var', 'lock'))
	def __init__ (self, id: Optional[str] = None, lazy: bool = False, silent: bool = False, block: bool = False) -> None:
		self.id = id if id is not None else program
		self.lazy = lazy
		self.silent = silent
		self.block = block
		self.lockpath = os.path.join (self.lock_directory, f'{self.id}.lock')
		self.lockpid: Optional[int] = None
		self.is_locked = False
	
	def __enter__ (self) -> Optional[Lock]:
		self.lock ()
		return self if self.is_locked else None

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		self.unlock ()
		return None
			
	def lock (self) -> bool:
		"""create a lock

if isFatal is True, then a failure in gaining a lock is considered as
a fatail error and an exception is raised otherwise the function just
returns False. If id is None then the logname for the current running
program is used."""
		if self.is_locked:
			return True
		#
		lockpid = os.getpid ()
		content = f'{lockpid:10d}\n'
		retry = True
		while retry and not self.is_locked:
			retry = self.block
			for state in 0, 1:
				try:
					fd = os.open (self.lockpath, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o444)
					os.write (fd, content.encode ('UTF-8'))
					os.close (fd)
					self.lockpid = lockpid
					self.is_locked = True
					if not self.silent:
						logger.info (f'{self.id}: lock created')
					break
				except OSError as e:
					if state == 0:
						if e.errno == errno.EEXIST:
							try:
								fd = os.open (self.lockpath, os.O_RDONLY)
								line = str (os.read (fd, 32), 'UTF-8').split ()[0]
								os.close (fd)
								pid = int (line)
								if pid > 0:
									try:
										os.kill (pid, 0)
									except OSError as e2:
										if e2.errno == errno.ESRCH:
											try:
												os.unlink (self.lockpath)
												logger.info (f'{self.id}: stale lockfile removed')
											except OSError as e3:
												logger.warning (f'{self.id}: failed to remove stale lockfile {self.lockpath}: {e3}')
										else:
											break
							except (IndexError, ValueError):
								with Ignore (OSError):
									st = os.stat (self.lockpath)
									if st.st_size == 0:
										os.unlink (self.lockpath)
										logger.info (f'{self.id}: removed corrupted (empty) lockfile')
						elif e.errno == errno.ENOENT:
							lockdirectory = os.path.abspath (os.path.dirname (self.lockpath))
							try:
								if create_path (lockdirectory):
									logger.info (f'{self.id}: created missing lock directory {lockdirectory}')
							except error as e:
								logger.warning (f'{self.id}: failed to create missing lock directory {lockdirectory}: {e}')
			if not self.is_locked:
				if retry:
					log_limit (logger.info, f'{self.id}: lock in use', duration = '30m')
					time.sleep (1)
				elif not self.lazy:
					raise LockError (f'{self.lockpath}: lock exists')
		return self.is_locked

	def unlock (self) -> None:
		"""Releases an acquired lock"""
		if self.is_locked:
			if self.lockpid is not None and self.lockpid == os.getpid ():
				try:
					os.unlink (self.lockpath)
					if not self.silent:
						logger.info (f'{self.id}: removed lockfile')
				except OSError as e:
					if e.errno != errno.ENOENT:
						logger.warning (f'{self.id}: failed to remove lockfile {self.lockpath}: {e}')
			self.is_locked = False
