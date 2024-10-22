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
from	dataclasses import dataclass, field
from	datetime import datetime
from	typing import ClassVar, Optional
from	typing import Match, Tuple
from	typing import cast
from	..definitions import base, fqdn, user, version
from	..exceptions import error
from	..parser import parse_timestamp
from	..stream import Stream
#
__all__ = ['spec', 'require']
#
@dataclass
class Spec:
	version: str = version
	timestamp: datetime = field (default_factory = datetime.now)
	host: str = fqdn
	user: str = user
	typ: str = 'classic'
	build_spec_path: ClassVar[str] = os.path.join (base, 'scripts', 'build.spec')

	@classmethod
	def parse (cls, build_spec: str) -> Spec:
		spec = cls ()
		if build_spec:
			for (index, value) in enumerate (build_spec.strip ().split (';')):
				if index == 0:
					spec.version = value
				elif index == 1:
					spec.timestamp = parse_timestamp (value, spec.timestamp)
				elif index == 2:
					spec.host = value
				elif index == 3:
					spec.user = value
				elif index == 4:
					spec.typ = value
		return spec
		
	@classmethod
	def build (cls, build_spec_path: Optional[str] = None) -> Spec:
		path = build_spec_path if build_spec_path is not None else cls.build_spec_path
		if os.path.isfile (path):
			with open (path, errors = 'backslashreplace') as fd:
				return cls.parse (fd.readline ())
		return cls ()

spec = Spec.build ()

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

