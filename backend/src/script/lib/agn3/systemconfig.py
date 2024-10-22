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
import	os, json, platform, pwd, re, base64
from	types import TracebackType
from	typing import Any, Callable, Final, Optional, TypeVar, Union
from	typing import Dict, ItemsView, KeysView, List, Set, ValuesView, Tuple, Type
from	typing import overload
from	.exceptions import error
from	.ignore import Ignore
from	.parser import unit
from	.sentinel import sentinel
from	.stream import Stream
from	.template import Placeholder
from	.tools import atob, listsplit
#
__all__ = ['Systemconfig']
#
_R = TypeVar ('_R')
_T = TypeVar ('_T')
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
>>> s._cfg = {'test1': 'global', f'test1-{s._user}': 'global-user', f'test2[{s._user}@]': 'local-user', f'test3[{s._fqdn}]': 'local-fqdn'}
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
	__slots__ = ['_path', '_custom_path', '_last_modified', '_content', '_content_environ', '_cfg', '_services']
	_config_environ: Final[str] = 'SYSTEM_CONFIG'
	_config_environ_path: Final[str] = 'SYSTEM_CONFIG_PATH'
	_default_path: Final[str] = '/opt/agnitas.com/etc/system.cfg'
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
		ignore_keyerror = Ignore (KeyError)
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

		def __enter__ (self) -> Systemconfig.Selection:
			return self

		def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
			return None

		def pick (self, collection: Dict[Optional[str], _T]) -> _T:
			for selection in self.selections:
				with self.ignore_keyerror:
					return collection[selection]
			raise KeyError ()
	
		def pick_pattern (self, collection: Dict[str, _T], key: str) -> _T:
			for selection in self.selections:
				with self.ignore_keyerror:
					return collection[f'{key}[{selection}]' if selection is not None else key]
			raise KeyError ()
		
		def select (self, collection: Dict[str, _T], key: str) -> Dict[Optional[str], _T]:
			rc: Dict[Optional[str], _T] = {}
			for selection in self.selections:
				with self.ignore_keyerror:
					rc[selection] = collection[f'{key}[{selection}]' if selection is not None else key]
			return rc
	
		def __contains__ (self, hostname: Union[None, str, Set[Optional[str]]]) -> bool:
			if isinstance (hostname, set):
				return bool (self.selections_set.intersection (hostname))
			return hostname in self.selections_set
	
	_selection = Selection (_user, _fqdn, _host)
	def __init__ (self) -> None:
		"""path to configuration file or None to use default

Setup the Systemconfig object and read the content of the system confg
file, if it is available. """
		self._path: Optional[str] = None
		self._custom_path = os.environ.get (self._config_environ_path)
		self._last_modified = 0.0
		self._content_environ = os.environ.get (self._config_environ)
		self._cfg: Dict[str, str] = {}
		self._services: Set[str] = set ()
		if self._content_environ is None:
			self._check ()
		else:
			self._content = self._content_environ
			self._parse ()
			
	def _check (self) -> None:
		if self._content_environ is None:
			if self._path is None or not os.path.isfile (self._path):
				if self._custom_path is not None:
					if os.path.isfile (self._custom_path):
						self._path = self._custom_path
					else:
						self._path = None
				else:
					self._path = os.path.join (self._home, 'etc', 'system.cfg')
					if not os.path.isfile (self._path):
						self._path = self._default_path
						if not os.path.isfile (self._path):
							if os.path.isfile (self._default_legacy_path):
								self._path = self._default_legacy_path
							else:
								self._path = None
				self._last_modified = 0
			if self._path is not None:
				try:
					st = os.stat (self._path)
					if st.st_mtime != self._last_modified:
						self._last_modified = st.st_mtime
						with open (self._path) as fd:
							self._content = fd.read ()
						self._parse ()
				except (OSError, IOError):
					self._clear ()
			else:
				self._clear ()

	pattern_selective = re.compile ('^([^[]+)\\[([^]]+)\\]$')
	def _parse (self) -> None:
		self._clear ()
		if self._content is not None:
			try:
				self._cfg = json.loads (self._content)
				if not isinstance (self._cfg, dict):
					raise ValueError ('expect json object, not {typ}'.format (typ = type (self._cfg)))
				mismatch: List[str] = []
				for (key, value) in self._cfg.items ():
					if not isinstance (key, str):
						mismatch.append ('{key}: {typ} ({value!r})'.format (
							key = key,
							typ = type (value),
							value = value
						))
				if mismatch:
					raise TypeError ('syscfg: type mismatch, expect str for all values:\n{mismatch}\nfor content:\n{content}'.format (
						mismatch = Stream (mismatch).join ('\n\t'),
						content = self._content
					))
			except ValueError:
				cont: Optional[str] = None
				cur: List[str] = []
				for line in self._content.split ('\n'):
					if line.endswith ('\r'):
						line = line.rstrip ('\r')
					if cont is not None:
						if line.rstrip () == '}':
							self._cfg[cont] = '\n'.join (cur)
							cont = None
						else:
							cur.append (line)
					elif line and not line.startswith ('#'):
						with Ignore (ValueError):
							(var, val) = [_v.strip () for _v in line.split ('=', 1)]
							if val == '{':
								cont = var
								cur.clear ()
							else:
								self._cfg[var] = val
			update: Dict[str, str] = {}
			for (option, value) in self._cfg.items ():
				mtch = self.pattern_selective.match (option)
				if mtch is not None:
					(base_option, indexlist) = mtch.groups ()
					indexes = list (listsplit (indexlist))
					if len (indexes) > 1:
						for index in indexes:
							simple_option = f'{base_option}[{index}]'
							if simple_option not in self._cfg:
								update[simple_option] = value
			self._cfg.update (update)

	def _clear (self) -> None:
		self._cfg.clear ()
		self._services.clear ()

	def __str__ (self) -> str:
		self._check ()
		return '{name}:\n\t{content}'.format (
			name = self.__class__.__name__,
			content = Stream (self._cfg.items ())
				.switch (lambda kv: kv[1] is None, lambda kv: str (kv[0]), lambda kv: '{var}={val!r}'.format (var = kv[0], val = kv[1]))
				.sorted ()
				.join ('\n\t')
		)
	
	def __getitem__ (self, var: str) -> str:
		"""Get configuration value"""
		self._check ()
		return self._selection.pick_pattern (self._cfg, var)

	def __contains__ (self, var: str) -> bool:
		"""Checks for existance of configuration variable"""
		self._check ()
		try:
			self._selection.pick_pattern (self._cfg, var)
		except KeyError:
			return False
		else:
			return True

	def keys (self) -> KeysView[str]:
		"""Returns all available configuration variables"""
		self._check ()
		return self._cfg.keys ()
	
	def values (self) -> ValuesView[str]:
		self._check ()
		return self._cfg.values ()

	def items (self) -> ItemsView[str, str]:
		self._check ()
		return self._cfg.items ()
	
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
	
	def eget (self, var: str, default: Union[int, str] = 0) -> int:
		"""Get configuration value as int parsed from unit with fallback"""
		default_value = unit.parse (default, 0) if isinstance (default, str) else default
		try:
			return unit.parse (self[var], default_value)
		except KeyError:
			return default_value

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
	def __user (self, var: str, default: None, retriever: Callable[..., _R]) -> Optional[_R]: ...
	@overload
	def __user (self, var: str, default: _R, retriever: Callable[..., _R]) -> _R: ...
	def __user (self, var: str, default: Optional[_R], retriever: Callable[..., _R]) -> Optional[_R]:
		rc = retriever (f'{var}-{Systemconfig._user}', sentinel) if Systemconfig._user is not None else sentinel
		return rc if rc is not sentinel else retriever (var, default)

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
	
	def user_eget (self, var: str, default: Union[int, str] = 0) -> int:
		return self.__user (var, unit (default, 0), self.eget)

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

	def has (self, var: str, default: Optional[bool] = None) -> bool:
		try:
			return atob (self[var])
		except KeyError:
			has_var = f'has-{var}'
			if default is not None:
				return self.bget (f'has-{var}', default)
			return atob (self[has_var])
	
	def user_has (self, var: str, default: Optional[bool] = None) -> bool:
		if (rc := self.user_bget (var)) is not None:
			return rc
		return self.has (var, default = default)

	__ph = Placeholder (lazy = True, extended = True)
	def as_config (self,
		value: str,
		*,
		namespace: Optional[Dict[str, Any]] = None,
		path_namespace: Optional[Dict[str, str]] = None,
		path_base: str = '.',
		optional: bool = False
	) -> Dict[str, Any]:
		rc: Dict[str, Any] = {}
		if value.startswith ('@'):
			path = value[1:].strip ()
			if path_namespace:
				path = Systemconfig.__ph (path, path_namespace)
			path = os.path.expanduser (path)
			if not os.path.isabs (path) and path_base:
				path = os.path.abspath (os.path.join (path_base, path))
			else:
				path = os.path.normpath (path)
			try:
				with open (path) as fd:
					value = '\n'.join ([_l for _l in fd.readlines () if not _l.lstrip ().startswith ('#')])
			except IOError:
				if optional:
					return rc
				raise
		with Ignore ():
			value = base64.b64decode (value, validate = True).decode ('UTF-8')
		try:
			config: Any = eval (value, namespace if namespace is not None else {})
		except Exception as e:
			for state in 0, 1:
				with Ignore ():
					if state == 0:
						if not namespace:
							continue
						config = json.loads (Systemconfig.__ph (value, namespace))
					else:
						config = json.loads (value)
					break
			else:
				raise error (f'"{value}": not a valid expression: {e}')
		if isinstance (config, dict):
			for (key, option) in config.items ():
				if isinstance (key, str):
					rc[key] = option
		return rc

	def dump (self) -> None:
		"""Display current configuration content"""
		self._check ()
		for (var, val) in self._cfg.items ():
			print (f'{var}={val}')

	_sendmail_options = [
		os.path.join (_home, 'lbin', 'sendmail'),
		'/usr/sbin/sendmail'
	] + [_f for _f in [os.path.join (_p, 'sendmail') for _p in os.environ.get ('PATH', '').split (':') if _p] if os.access (_f, os.X_OK)]
	_sendmail_cli = Stream (_sendmail_options).filter (lambda p: os.access (p, os.X_OK)).limit (1).first (no = _sendmail_options[1])
	def sendmail (self, recipients: Union[None, str, List[str]] = None, *, sender: Optional[str] = None) -> List[str]:
		"""Returns sendmail command based on configuration"""
		cmd = [self.user_get ('sendmail-cli', default = Systemconfig._sendmail_cli)]
		if not self.bget ('enable-sendmail-dsn', False):
			cmd.append ('-NNEVER')
		if sender is not None:
			cmd.append ('-f')
			cmd.append (sender)
		if recipients is not None:
			cmd.append ('--')
			if isinstance (recipients, str):
				cmd.append (recipients)
			else:
				cmd += recipients
		return cmd

	def selection (self, user: str = _user, fqdn: str = _fqdn, host: str = _host) -> Systemconfig.Selection:
		return Systemconfig.Selection (user, fqdn, host)

	def service (self, *service: str) -> bool:
		return bool (self.services.intersection (service))
	

	@property
	def services (self) -> Set[str]:
		self._check ()
		if not self._services:
			self._services.update (['openemm'])
		return self._services

