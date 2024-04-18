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
import	os, re
from	datetime import datetime
from	typing import Final, Optional
from	typing import Match, Tuple
from	typing import cast
from	..definitions import base, fqdn, user, version
from	..exceptions import error
from	..ignore import Ignore
from	..parser import ParseTimestamp, Field, Lineparser
from	..stream import Stream
#
__all__ = ['spec', 'require']
#
class Spec:
	__slots__ = ['spec']
	build_spec_path: Final[str] = os.path.join (base, 'scripts', 'build.spec')
	timestamp_parser = ParseTimestamp ()
	parser = Lineparser (
		lambda a: a.split (';', 3),
		'version',
		Field ('timestamp', lambda t: Spec.timestamp_parser (t)),
		'host',
		'user'
	)
	def __init__ (self, build_spec_path: Optional[str] = None) -> None:
		self.spec = Spec.parser.target_class (version, datetime.now (), fqdn, user)
		path = build_spec_path if build_spec_path is not None else Spec.build_spec_path
		if os.path.isfile (path):
			with open (path, 'r', errors = 'backslashreplace') as fd:
				build_spec = fd.read ().strip ()
				with Ignore (error):
					self.spec = Spec.parser (build_spec)

spec = Spec ().spec

def require (version: str) -> None:
	reduce_to_num_pattern = re.compile ('[0-9]+')
	def reduce_to_num (v: str) -> Tuple[int, ...]:
		return (Stream (v.split ('.'))
			.map (lambda e: reduce_to_num_pattern.search (e))
			.filter (lambda m: m is not None)
			.map (lambda m: int (cast (Match[str], m).group ()))
			.tuple ()
		)
	required_version = reduce_to_num (version)
	current_version = reduce_to_num (spec.version)
	if required_version > current_version:
		raise error (f'required version {version} is not satisfied by available version {spec.version}')

