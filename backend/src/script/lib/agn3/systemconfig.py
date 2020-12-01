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
from	typing import Any, Callable, Final, Optional, Union
from	typing import Dict, KeysView, List
from	typing import cast
from	.exceptions import error
from	.stream import Stream
from	.tools import atob
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
	
	def get (self, var: str, default: Optional[str] = None) -> Optional[str]:
		"""Get configuration value with default fallback"""
		try:
			return self[var]
		except KeyError:
			return default

	def iget (self, var: str, default: Optional[int] = None) -> Optional[int]:
		"""Get configuration value as integer with default fallback"""
		try:
			return int (self[var])
		except KeyError:
			return default

	def fget (self, var: str, default: Optional[float] = None) -> Optional[float]:
		"""Get configuration value as float with default fallback"""
		try:
			return float (self[var])
		except KeyError:
			return default

	def bget (self, var: str, default: Optional[bool] = None) -> Optional[bool]:
		"""Get configuration value as boolean with default fallback"""
		try:
			return atob (self[var])
		except KeyError:
			return default

	def lget (self, var: str, sep: str = ',', default: Optional[List[str]] = None) -> Optional[List[str]]:
		"""Get configuration value as list with default fallback"""
		try:
			return [_v.strip () for _v in self[var].split (sep) if _v.strip ()]
		except KeyError:
			return default
	
	def get_str (self, var: str, default: str) -> str:
		return cast (str, self.get (var, default))
	def get_int (self, var: str, default: int) -> int:
		return cast (int, self.iget (var, default))
	def get_float (self, var: str, default: float) -> float:
		return cast (float, self.fget (var, default))
	def get_bool (self, var: str, default: bool) -> bool:
		return cast (bool, self.bget (var, default))
	def get_list (self, var: str, sep: str, default: List[str]) -> List[str]:
		return cast (List[str], self.lget (var, sep, default))

	def __user (self, var: str, default: Any, retriever: Callable[..., Any], **kwargs: Any) -> Any:
		if self.user is None:
			raise error ('"user" not set in Systemconfig instance')
		rc = retriever (f'{var}-{self.user}', Systemconfig.__sentinel, **kwargs)
		return rc if rc is not Systemconfig.__sentinel else retriever (var, default, **kwargs)
		
	def user_get (self, var: str, default: Optional[str] = None) -> Optional[str]:
		"""Get configuration, first try with user append with default fallback"""
		return cast (Optional[str], self.__user (var, default, self.get))
	def user_iget (self, var: str, default: Optional[int] = None) -> Optional[int]:
		return cast (Optional[int], self.__user (var, default, self.iget))
	def user_fget (self, var: str, default: Optional[float] = None) -> Optional[float]:
		return cast (Optional[float], self.__user (var, default, self.fget))
	def user_bget (self, var: str, default: Optional[bool] = None) -> Optional[bool]:
		return cast (Optional[bool], self.__user (var, default, self.bget))
	def user_lget (self, var: str, sep: str = ',', default: Optional[List[str]] = None) -> Optional[List[str]]:
		return cast (Optional[List[str]], self.__user (var, default, self.lget, sep = sep))
	
	def user_get_str (self, var: str, default: str) -> str:
		return cast (str, self.user_get (var, default))
	def user_get_int (self, var: str, default: int) -> int:
		return cast (int, self.user_iget (var, default))
	def user_get_float (self, var: str, default: float) -> float:
		return cast (float, self.user_fget (var, default))
	def user_get_bool (self, var: str, default: bool) -> bool:
		return cast (bool, self.user_bget (var, default))
	def user_get_list (self, var: str, sep: str, default: List[str]) -> List[str]:
		return cast (List[str], self.user_lget (var, sep, default))

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
