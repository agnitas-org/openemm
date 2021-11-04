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
import	os, json
from	typing import Callable, Final, Optional, TypeVar, Union
from	typing import Dict, KeysView, List
from	typing import overload
from	.exceptions import error
from	.stream import Stream
from	.tools import atob, listsplit
#
__all__ = ['Systemconfig']
#
R = TypeVar ('R')
#
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
"""
	__slots__ = ['path', 'content', 'cfg', 'user']
	_defaultPath: Final[str] = os.environ.get ('SYSTEM_CONFIG_PATH', '/opt/agnitas.com/etc/system.cfg')
	_defaultLegacyPath: Final[str] = os.path.join (os.path.dirname (_defaultPath), 'licence.cfg')
	__sentinel: Final[object] = object ()
	def __init__ (self, path: Optional[str] = None) -> None:
		"""path to configuration file or None to use default

Setup the Systemconfig object and read the content of the system confg
file, if it is available. """
		if path is None:
			path = self._defaultPath
			if not os.path.isfile (path) and os.path.isfile (self._defaultLegacyPath):
				path = self._defaultLegacyPath
		self.path = path
		self.content = os.environ.get ('SYSTEM_CONFIG')
		self.cfg: Dict[str, str] = {}
		if self.content is None and self.path and os.path.isfile (self.path):
			with open (self.path) as fd:
				self.content = fd.read ()
		self.user: Optional[str] = None
		self.parse ()
	
	def __str__ (self) -> str:
		return '{name}:\n\t{content}'.format (
			name = self.__class__.__name__,
			content = Stream (self.cfg.items ())
				.switch (lambda kv: kv[1] is None, lambda kv: str (kv[0]), lambda kv: '{var}={val!r}'.format (var = kv[0], val = kv[1]))
				.sorted ()
				.join ('\n\t')
		)
	
	def parse (self) -> None:
		self.cfg = {}
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
							self[cont] = '\n'.join (cur)
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

	def __setitem__ (self, var: str, val: str) -> None:
		"""Set configuration value"""
		self.cfg[var] = val

	def __getitem__ (self, var: str) -> str:
		"""Get configuration value"""
		return self.cfg[var]

	def __delitem__ (self, var: str) -> None:
		"""Remove configuration value"""
		del self.cfg[var]

	def __contains__ (self, var: str) -> bool:
		"""Checks for existance of configuration variable"""
		return var in self.cfg

	def keys (self) -> KeysView[str]:
		"""Returns all available configuration variables"""
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
		if self.user is None:
			raise error ('"user" not set in Systemconfig instance')
		rc = retriever (f'{var}-{self.user}', Systemconfig.__sentinel)
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
