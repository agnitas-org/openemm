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
import	os, pwd, grp
from	typing import Any, Callable, Union
from	typing import List
from	typing import cast
from	.ignore import Ignore
#
__all__ = ['IDs']
#
class IDs:
	"""support class to handle UID/GID

This is a general class to lookup user and group and find related IDs
on the local server. The get_user() and get_group() method accepts
either a string or a numeric value and tries to interpret these as
name or id. If ``relaxed'' is True then, in case the name is of type
str, it will try to interpret this as the id as well, if the name
cannot be found."""
	__slots__: List[str] = []
	def __get (self,
		what: Union[None, int, str],
		lookup_name: Callable[[str], Any],
		lookup_id: Callable[[int], Any],
		lookup_default: Callable[[], int],
		relaxed: bool
	) -> Any:
		rc = None
		if what is None:
			with Ignore (KeyError):
				rc = lookup_id (lookup_default ())
		elif type (what) is str:
			try:
				rc = lookup_name (cast (str, what))
			except KeyError:
				if relaxed:
					with Ignore (KeyError, ValueError, TypeError):
						rc = lookup_id (int (what))
		elif type (what) in (int, float):
			with Ignore (KeyError):
				rc = lookup_id (int (what))
		return rc
	
	def get_user (self, user: Union[None, int, str] = None, relaxed: bool = True) -> Any:
		"""Tries to find ``user'' and returns pwd.struct_passwd"""
		return self.__get (user, pwd.getpwnam, pwd.getpwuid, os.getuid, relaxed)
	
	def get_group (self, group: Union[None, int, str] = None, relaxed: bool = True) -> Any:
		"""Tries to find ``group'' and returns grp.struct_group"""
		return self.__get (group, grp.getgrnam, grp.getgrgid, os.getgid, relaxed)

