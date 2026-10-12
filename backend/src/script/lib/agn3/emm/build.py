####################################################################################################################################################################################################################################################################
#                                                                                                                                                                                                                                                                  #
#                                                                                                                                                                                                                                                                  #
#        Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                              #
#                                                                                                                                                                                                                                                                  #
#        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    #
#        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           #
#        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.                                                                                                       #
#                                                                                                                                                                                                                                                                  #
####################################################################################################################################################################################################################################################################
#
from	__future__ import annotations
import	os, re
from	dataclasses import dataclass, field
from	datetime import datetime
from	functools import total_ordering
from	typing import ClassVar
from	..definitions import base, fqdn, user, version
from	..parser import parse_timestamp
#
__all__ = ['spec']
#
@total_ordering
class Version:
	__slots__ = ['_major', '_minor', '_patchlevel', '_hotfix', '_strict', '_lts']
	_pattern_version = re.compile ('^([0-9]+(\\.[0-9]+){2,}).*')
	def __init__ (self,
		version_expression: None | str = None,
		*,
		major: int = 0,
		minor: int = 0,
		patchlevel: int = 0,
		hotfix: int = 0,
		strict: bool = False
	) -> None:
		self._major = major
		self._minor = minor
		self._patchlevel = patchlevel
		self._hotfix = hotfix
		self._strict = strict
		self._lts = False
		if version_expression is not None:
			if (mtch := self._pattern_version.match (version_expression)) is None:
				raise ValueError (f'"{version_expression}": unparsable')
			elements = tuple (int (_v) for _v in mtch.group (1).split ('.'))
			if len (elements) < 3 or len (elements) > 4:
				raise ValueError (f'"{version_expression}": expect three or four numbers')
			self.major = elements[0]
			self.minor = elements[1]
			self.patchlevel = elements[2]
			if len (elements) == 4:
				self.hotfix = elements[3]
		self._validate ()
	
	def __str__ (self) -> str:
		parts = [f'{self._major:02d}.{self._minor:03d}.{self._patchlevel:03d}']
		if self._hotfix:
			parts.append (f'.{self._hotfix:03d}')
		if self._lts:
			parts.append (' (LTS)')
		return ''.join (parts)

	def __repr__ (self) -> str:
		return f'{self.__class__.__name__} (major = {self._major}, minor = {self._minor}, patchlevel = {self._patchlevel}, hotfix = {self._hotfix})'
	
	def __eq__ (self, other: object) -> bool:
		if isinstance (other, Version):
			return (self._major, self._minor, self._patchlevel, self._hotfix) == (other._major, other._minor, other._patchlevel, other._hotfix)
		raise NotImplementedError ()

	def __lt__ (self, other: object) -> bool:
		if isinstance (other, Version):
			return (self._major, self._minor, self._patchlevel, self._hotfix) < (other._major, other._minor, other._patchlevel, other._hotfix)
		raise NotImplementedError ()
	
	def _validate (self, major: None | int = None, minor: None | int = None, patchlevel: None | int = None, hotfix: None | int = None) -> None:
		if self._strict:
			if major is not None and (major < 20 or major >= 34):
				raise ValueError (f'major: invalid number {major}')
			if minor is not None and minor not in (1, 4, 5, 7, 10, 11):
				raise ValueError (f'minor: invalid number {minor}')
			if patchlevel is not None and (patchlevel < 0 or patchlevel >= 1000):
				raise ValueError (f'patchlevel: out of range {patchlevel}')
			if hotfix is not None and (hotfix < 0 or hotfix >= 1000):
				raise ValueError (f'hotfix: out of range {hotfix}')
		if major is not None:
			self._major = major
		if minor is not None:
			self._minor = minor
		if patchlevel is not None:
			self._patchlevel = patchlevel
		if hotfix is not None:
			self._hotfix = hotfix
		self._lts = self.major >= 20 and self.major < 34 and self.minor in (5, 11) and self.patchlevel >= 0 and self.patchlevel < 1000 and self.hotfix >= 0 and self.hotfix < 1000
	
	def _get_major (self) -> int:
		return self._major
	def _set_major (self, value: int) -> None:
		self._validate (major = value)
		self._major = value
	def _del_major (self) -> None:
		self.major = 0
	major = property (_get_major, _set_major, _del_major)
	def _get_minor (self) -> int:
		return self._minor
	def _set_minor (self, value: int) -> None:
		self._validate (minor = value)
		self._minor = value
	def _del_minor (self) -> None:
		self.minor = 0
	minor = property (_get_minor, _set_minor, _del_minor)
	def _get_patchlevel (self) -> int:
		return self._patchlevel
	def _set_patchlevel (self, value: int) -> None:
		self._validate (patchlevel = value)
		self._patchlevel = value
	def _del_patchlevel (self) -> None:
		self.patchlevel = 0
	patchlevel = property (_get_patchlevel, _set_patchlevel, _del_patchlevel)
	def _get_hotfix (self) -> int:
		return self._hotfix
	def _set_hotfix (self, value: int) -> None:
		self._validate (hotfix = value)
		self._hotfix = value
	def _del_hotfix (self) -> None:
		self.hotfix = 0
	hotfix = property (_get_hotfix, _set_hotfix, _del_hotfix)		
		
		
@dataclass
class Spec:
	version: str = version
	timestamp: datetime = field (default_factory = datetime.now)
	host: str = fqdn
	user: str = user
	typ: str = 'classic'
	build_spec_env: ClassVar[str] = 'BUILD_SPEC'
	build_spec_path_env: ClassVar[str] = 'BUILD_SPEC_PATH'
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
	def build (cls) -> Spec:
		if (spec := os.environ.get (cls.build_spec_env)) is not None:
			return cls.parse (spec)
		for path in [os.environ.get (cls.build_spec_path_env), cls.build_spec_path]:
			if path is not None and os.path.isfile (path):
				with open (path, errors = 'backslashreplace') as fd:
					return cls.parse (fd.readline ())
		return cls ()
	
	def as_version (self) -> Version:
		return Version (self.version)


spec = Spec.build ()
