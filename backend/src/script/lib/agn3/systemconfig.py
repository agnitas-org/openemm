####################################################################################################################################################################################################################################################################
#                                                                                                                                                                                                                                                                  #
#                                                                                                                                                                                                                                                                  #
#        Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   #
#                                                                                                                                                                                                                                                                  #
#        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    #
#        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           #
#        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            #
#                                                                                                                                                                                                                                                                  #
####################################################################################################################################################################################################################################################################
#
from	__future__ import annotations
import	os, json, platform, pwd
from	typing import Callable, Final, Optional, TypeVar, Union
from	typing import Dict, KeysView, List, Set, Tuple
from	typing import overload
from	.ignore import Ignore
from	.stream import Stream
from	.tools import atob, listsplit
#
__all__ = ['Systemconfig']
#
R = TypeVar ('R')
T = TypeVar ('T')
#
def _determinate_essentials () -> Tuple[str, str, str, str]:
	fqdn = platform.node ().lower ()
	host = fqdn.split ('.', 1)[0]
	try:
		pw = pwd.getpwuid (os.getuid ())
		user = pw.pw_name
		home = pw.pw_dir
	except KeyError:
		user = os.environ.get ('USER', '#{uid}'.format (uid = os.getuid ()))
		home = os.environ.get ('HOME', '.')
	return (fqdn, host, user, home)

class Systemconfig:
	"""Handling system specific configuration

generic class to read system information from external configuration
file to reflect the individual values for this installation. This
class can be used simular to a dictionary.

The content of the file has line based <key> <value> pairs, separated
by an equal sign. If you need a longer value, you can use the opening
curly bracket as the start of the value and then add the value in the
next lines. Close this block by using a closing curly bracket.
Multiline values are folded to a single line value.

>>> s = Systemconfig ()
>>> s.cfg = {'test1': 'global', f'test1-{s._user}': 'global-user', f'test2[{s._user}@]': 'local-user', f'test3[{s._fqdn}]': 'local-fqdn'}
>>> s['test1']
'global'
>>> s.user_get ('test1')
'global-user'
>>> s['test2']
'local-user'
>>> s['test3']
'local-fqdn'
>>> 'test1' in s
True
>>> 'test2' in s
True
>>> 'test3' in s
True
>>> 'test4' in s
False
"""
	__slots__ = ['path', 'last_modified', 'content', 'cfg']
	_default_path: Final[str] = os.environ.get ('SYSTEM_CONFIG_PATH', '/opt/agnitas.com/etc/system.cfg')
	_default_legacy_path: Final[str] = os.path.join (os.path.dirname (_default_path), 'licence.cfg')
	(_fqdn, _host, _user, _home) = _determinate_essentials ()
	class Selection:
		"""select an option from a hostname expression

the precedence of the hostname expressions are:
- <user>@<fqdn>
- <user>@<host>
- <user>@
- <fqdn>
- <host>
- None

>>> (fqdn, host, user, _) = _determinate_essentials ()
>>> s = Systemconfig ().selection ()
>>> cfg = {}
>>> s.pick (cfg)
Traceback (most recent call last):
...
KeyError
>>> cfg[None] = 'default'
>>> s.pick (cfg)
'default'
>>> cfg[f'{host}'] = 'host'
>>> s.pick (cfg)
'host'
>>> cfg[f'{fqdn}'] = 'fqdn'
>>> s.pick (cfg)
'fqdn'
>>> cfg[f'{user}@'] = 'user'
>>> s.pick (cfg)
'user'
>>> cfg[f'{user}@{host}'] = 'user@host'
>>> s.pick (cfg)
'user@host'
>>> cfg[f'{user}@{fqdn}'] = 'user@fqdn'
>>> s.pick (cfg)
'user@fqdn'
>>> None in s
True
>>> host in s
True
>>> fqdn in s
True
>>> f'{user}@' in s
True
>>> f'{user}@{host}' in s
True
>>> f'{user}@{fqdn}' in s
True
>>> f'{user}-{fqdn}' in s
False
>>> '' in s
False
>>> set () in s
False
>>> {None} in s
True
>>> {f'{user}@'} in s
True
>>> {None, host, fqdn, f'{user}@', f'{user}@{host}', f'{user}@{fqdn}'} in s
True
>>> {None, host, fqdn, f'{user}@', f'{user}@{host}', f'{user}@{fqdn}', ''} in s
True
"""
		__slots__ = ['selections', 'selections_set', '_user', '_fqdn', '_host']
		def __init__ (self, user: str, fqdn: str, host: str) -> None:
			self.selections = [
				f'{user}@{fqdn}',
				f'{user}@{host}',
				f'{user}@',
				fqdn,
				host,
				None
			]
			self.selections_set = set (self.selections)
			self._user = user
			self._fqdn = fqdn
			self._host = host

		@property
		def user (self) -> str:
			return self._user
		
		@property
		def fqdn (self) -> str:
			return self._fqdn
	
		@property
		def host (self) -> str:
			return self._host

		def pick (self, collection: Dict[Optional[str], T]) -> T:
			for selection in self.selections:
				with Ignore (KeyError):
					return collection[selection]
			raise KeyError ()
	
		def pick_pattern (self, collection: Dict[str, T], key: str) -> T:
			for selection in self.selections:
				with Ignore (KeyError):
					return collection[f'{key}[{selection}]' if selection is not None else key]
			raise KeyError ()
	
		def __contains__ (self, hostname: Union[None, str, Set[Optional[str]]]) -> bool:
			if isinstance (hostname, set):
				return bool (self.selections_set.intersection (hostname))
			return hostname in self.selections_set
	
	_selection = Selection (_user, _fqdn, _host)
	__sentinel: Final[object] = object ()
	def __init__ (self, path: Optional[str] = None) -> None:
		"""path to configuration file or None to use default

Setup the Systemconfig object and read the content of the system confg
file, if it is available. """
		self.path = None
		self.last_modified = 0.0
		self.content = os.environ.get ('SYSTEM_CONFIG')
		self.cfg: Dict[str, str] = {}
		if self.content is None:
			if path is None:
				path = self._default_path
				if not os.path.isfile (path) and os.path.isfile (self._default_legacy_path):
					path = self._default_legacy_path
			self.path = path
			self._check ()
		else:
			self._parse ()
			
	def _check (self) -> None:
		if self.path is not None:
			try:
				st = os.stat (self.path)
				if st.st_mtime != self.last_modified:
					self.last_modified = st.st_mtime
					with open (self.path) as fd:
						self.content = fd.read ()
					self._parse ()
			except (OSError, IOError):
				pass
	
	def _parse (self) -> None:
		self.cfg.clear ()
		if self.content is not None:
			try:
				self.cfg = json.loads (self.content)
				if type (self.cfg) != dict:
					raise ValueError ('expect json object, not {typ}'.format (typ = type (self.cfg)))
			except ValueError:
				cont = None
				cur: Optional[List[str]] = None
				for line in (_l.strip () for _l in self.content.split ('\n')):
					if cont is not None:
						if line == '}':
							self.cfg[cont] = '\n'.join (cur)
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
								self.cfg[var] = val
						except ValueError:
							pass

	def __str__ (self) -> str:
		self._check ()
		return '{name}:\n\t{content}'.format (
			name = self.__class__.__name__,
			content = Stream (self.cfg.items ())
				.switch (lambda kv: kv[1] is None, lambda kv: str (kv[0]), lambda kv: '{var}={val!r}'.format (var = kv[0], val = kv[1]))
				.sorted ()
				.join ('\n\t')
		)
	
	def __getitem__ (self, var: str) -> str:
		"""Get configuration value"""
		self._check ()
		return self._selection.pick_pattern (self.cfg, var)

	def __contains__ (self, var: str) -> bool:
		"""Checks for existance of configuration variable"""
		self._check ()
		try:
			self._selection.pick_pattern (self.cfg, var)
		except KeyError:
			return False
		else:
			return True
		return var in self.cfg

	def keys (self) -> KeysView[str]:
		"""Returns all available configuration variables"""
		self._check ()
		return self.cfg.keys ()

	@overload
	def get (self, var: str, default: None = ...) -> Optional[str]: ...
	@overload
	def get (self, var: str, default: str) -> str: ...
	def get (self, var: str, default: Optional[str] = None) -> Optional[str]:
		"""Get configuration value with default fallback"""
		try:
			return self[var]
		except KeyError:
			return default

	@overload
	def iget (self, var: str, default: None = ...) -> Optional[int]: ...
	@overload
	def iget (self, var: str, default: int) -> int: ...
	def iget (self, var: str, default: Optional[int] = None) -> Optional[int]:
		"""Get configuration value as integer with default fallback"""
		try:
			return int (self[var])
		except KeyError:
			return default

	@overload
	def fget (self, var: str, default: None = ...) -> Optional[float]: ...
	@overload
	def fget (self, var: str, default: float) -> float: ...
	def fget (self, var: str, default: Optional[float] = None) -> Optional[float]:
		"""Get configuration value as float with default fallback"""
		try:
			return float (self[var])
		except KeyError:
			return default

	@overload
	def bget (self, var: str, default: None = ...) -> Optional[bool]: ...
	@overload
	def bget (self, var: str, default: bool) -> bool: ...
	def bget (self, var: str, default: Optional[bool] = None) -> Optional[bool]:
		"""Get configuration value as boolean with default fallback"""
		try:
			return atob (self[var])
		except KeyError:
			return default

	@overload
	def lget (self, var: str, default: None = ...) -> Optional[List[str]]: ...
	@overload
	def lget (self, var: str, default: List[str]) -> List[str]: ...
	def lget (self, var: str, default: Optional[List[str]] = None) -> Optional[List[str]]:
		"""Get configuration value as list with default fallback"""
		try:
			return list (listsplit (self[var]))
		except KeyError:
			return default

	@overload
	def __user (self, var: str, default: None, retriever: Callable[..., R]) -> Optional[R]: ...
	@overload
	def __user (self, var: str, default: R, retriever: Callable[..., R]) -> R: ...
	def __user (self, var: str, default: Optional[R], retriever: Callable[..., R]) -> Optional[R]:
		rc = retriever (f'{var}-{Systemconfig._user}', Systemconfig.__sentinel) if Systemconfig._user is not None else Systemconfig.__sentinel
		return rc if rc is not Systemconfig.__sentinel else retriever (var, default)

	@overload
	def user_get (self, var: str, default: None = ...) -> Optional[str]: ...
	@overload
	def user_get (self, var: str, default: str) -> str: ...
	def user_get (self, var: str, default: Optional[str] = None) -> Optional[str]:
		"""Get configuration, first try with user append with default fallback"""
		return self.__user (var, default, self.get)
	@overload
	def user_iget (self, var: str, default: None = ...) -> Optional[int]: ...
	@overload
	def user_iget (self, var: str, default: int) -> int: ...
	def user_iget (self, var: str, default: Optional[int] = None) -> Optional[int]:
		return self.__user (var, default, self.iget)
	@overload
	def user_fget (self, var: str, default: None = ...) -> Optional[float]: ...
	@overload
	def user_fget (self, var: str, default: float) -> float: ...
	def user_fget (self, var: str, default: Optional[float] = None) -> Optional[float]:
		return self.__user (var, default, self.fget)
	@overload
	def user_bget (self, var: str, default: None = ...) -> Optional[bool]: ...
	@overload
	def user_bget (self, var: str, default: bool) -> bool: ...
	def user_bget (self, var: str, default: Optional[bool] = None) -> Optional[bool]:
		return self.__user (var, default, self.bget)
	@overload
	def user_lget (self, var: str, default: None = ...) -> Optional[List[str]]: ...
	@overload
	def user_lget (self, var: str, default: List[str]) -> List[str]: ...
	def user_lget (self, var: str, default: Optional[List[str]] = None) -> Optional[List[str]]:
		return self.__user (var, default, self.lget)
	
	def dump (self) -> None:
		"""Display current configuration content"""
		self._check ()
		for (var, val) in self.cfg.items ():
			print (f'{var}={val}')

	def sendmail (self, recipients: Union[str, List[str]]) -> List[str]:
		"""Returns sendmail command based on configuration"""
		cmd = ['/usr/sbin/sendmail']
		if not self.bget ('enable-sendmail-dsn', False):
			cmd.append ('-NNEVER')
		cmd.append ('--')
		if isinstance (recipients, str):
			cmd.append (recipients)
		else:
			cmd += recipients
		return cmd

	def selection (self, user: str = _user, fqdn: str = _fqdn, host: str = _host) -> Systemconfig.Selection:
		return Systemconfig.Selection (user, fqdn, host)
