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
import	re
from	typing import Optional, Union
from	typing import List, Sequence
from	agn3.ignore import Ignore
from	agn3.stream import Stream
#
__all__ = ['PubID']
#
class PubID:
	"""Handled public id agnPUBID

this handles the public id agnPUBID which is used for the anon
preview."""
	__slots__ = ['mailing_id', 'customer_id', 'source', 'selector']
	scramble = 'w5KMCHOXE_PTuLcfF6D1ZI3BydeplQaztVAnUj0bqos7k49YgWhxiS-RrGJm8N2v'
	source_invalid = re.compile ('[^0-9a-zA-Z_-]')

	def __init__ (self) -> None:
		self.mailing_id = 0
		self.customer_id = 0
		self.source: Optional[str] = None
		self.selector: Optional[str] = None
	
	def __str__ (self) -> str:
		return '{name} <{parameter}>'.format (
			name = self.__class__.__name__,
			parameter = (Stream ([
					('mailing', self.mailing_id),
					('customer', self.customer_id),
					('source', self.source),
					('selector', self.selector)
				])
				.filter (lambda kv: kv[1] is not None)
				.map (lambda kv: '{key}={value!r}'.format (key = kv[0], value = kv[1]))
				.join (', ')
			)
		)

	def __source (self, source: Optional[str]) -> Optional[str]:
		if source is not None:
			if len (source) > 20:
				source = source[:20]
			source = self.source_invalid.subn ('_', source)[0]
		return source

	def __checksum (self, s: Sequence[str]) -> str:
		cs = 12
		for ch in s:
			cs += ord (ch)
		return self.scramble[cs & 0x3f]

	def __encode (self, source: Union[str, bytes]) -> str:
		s: bytes = source.encode ('UTF-8') if isinstance (source, str) else source
		slen = len (s)
		temp: List[str] = []
		n = 0
		while n < slen:
			chk = s[n:n + 3]
			d = chk[0] << 16
			if len (chk) > 1:
				d |= chk[1] << 8
				if len (chk) > 2:
					d |= chk[2]
			n += 3
			temp.append (self.scramble[(d >> 18) & 0x3f])
			temp.append (self.scramble[(d >> 12) & 0x3f])
			temp.append (self.scramble[(d >> 6) & 0x3f])
			temp.append (self.scramble[d & 0x3f])
		temp.insert (5, self.__checksum (temp))
		return ''.join (temp)

	def __decode (self, s: str) -> Optional[str]:
		rc: Optional[str] = None
		slen = len (s)
		if slen > 5 and (slen - 1) & 3 == 0:
			check = s[5]
			s = s[:5] + s[6:]
			if check == self.__checksum (s):
				slen -= 1
				collect = []
				ok = True
				n = 0
				while n < slen and ok:
					v = [self.scramble.find (s[_c]) for _c in range (n, n + 4)]
					n += 4
					if -1 in v:
						ok = False
					else:
						d = (v[0] << 18) | (v[1] << 12) | (v[2] << 6) | v[3]
						collect.append (chr ((d >> 16) & 0xff))
						collect.append (chr ((d >> 8) & 0xff))
						collect.append (chr (d & 0xff))
				if ok:
					while collect and collect[-1] == '\0':
						collect = collect[:-1]
					rc = ''.join (collect)
		return rc

	def create (self, mailing_id: Optional[int] = None, customer_id: Optional[int] = None, source: Optional[str] = None, selector: Optional[str] = None) -> str:
		"""Creates a new agnPUBID

creates a new public id which can be used for the anon preview. Either
set the variables here or set them by assigning the corrosponding
instance variables."""
		if mailing_id is None:
			mailing_id = self.mailing_id
		if customer_id is None:
			customer_id = self.customer_id
		if source is None:
			source = self.source
			if source is None:
				source = ''
		if selector is None:
			selector = self.selector
			if not selector:
				selector = None
		src = '{mailing_id};{customer_id};{source}'.format (
			mailing_id = mailing_id,
			customer_id = customer_id,
			source = self.__source (source)
		)
		if selector is not None:
			src += f';{selector}'
		return self.__encode (src)

	def parse (self, pid: str) -> bool:
		"""Parses an agnPUBID
		
if parsing had been successful, the instance variable containing the
parsed content and the method returns True, otherwise the content of
the instance variables is undefinied and the method returns False."""
		rc = False
		dst = self.__decode (pid)
		if dst is not None:
			parts = dst.split (';', 3)
			if len (parts) in (3, 4):
				with Ignore (ValueError):
					mailing_id = int (parts[0])
					customer_id = int (parts[1])
					source = parts[2]
					if len (parts) > 3:
						selector: Optional[str] = parts[3]
					else:
						selector = None
					if mailing_id > 0 and customer_id > 0:
						self.mailing_id = mailing_id
						self.customer_id = customer_id
						if source:
							self.source = source
						else:
							self.source = None
						self.selector = selector
						rc = True
		return rc

