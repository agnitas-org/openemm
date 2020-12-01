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
import	re
from	typing import Pattern, Tuple
#
__all__ = ['isnum', 'numkey', 'SQL_wildcard_transform', 'SQL_wildcard_compile']
#
isnum_pattern = re.compile ('^[0-9]+$')
def isnum (s: str) -> bool:
	return isnum_pattern.match (s) is not None

numkey_pattern = re.compile ('[^0-9]+')
def numkey (s: str) -> Tuple[int, ...]:
	rc = tuple (int (_s) for _s in numkey_pattern.split (s) if _s)
	if rc == ():
		return (0, )
	return rc
	
def SQL_wildcard_transform (s: str) -> str:
	"""transforms a SQL wildcard expression to a regular expression"""
	r = ''
	needFinal = True
	for ch in s:
		needFinal = True
		if ch in '$^*?()+[{]}|\\.':
			r += f'\\{ch}'
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

def SQL_wildcard_compile (s: str, reFlags: int = 0) -> Pattern[str]:
	"""compiles a SQL wildcard expression as a regular expression"""
	return re.compile (SQL_wildcard_transform (s), reFlags)
