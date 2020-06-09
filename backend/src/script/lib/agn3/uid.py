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
from	__future__ import annotations
import	time, hashlib, base64, re, logging
from	datetime import datetime
from	types import TracebackType
from	typing import Optional, Union
from	typing import Dict, List, NamedTuple, Sequence, Tuple, Type
from	typing import cast
from	.db import DB
from	.definitions import licence
from	.emm.companyconfig import CompanyConfig
from	.exceptions import error
from	.ignore import Ignore
from	.stream import Stream
#
__all__ = ['UID', 'CachingUID', 'PubID']
#
logger = logging.getLogger (__name__)
#
class UID:
	"""Handles V2++ agnUID (current version)

this handles the current version 3 of the agnUID. It is simular to
UID, but has some more requirements and some changes. To create a new
UID, you must set these instance variables, on parsing they are filled
by the parser:
	- company_id: the company_id for the UID
	- mailing_id: the assigned mailing_id for the UID
	- customer_id: the customer to whom the UID belongs
	- url_id: (optional) the assigned ID of the URL (see rdir_url_tbl)

In both cases you must set the timestamp (from
mailing_tbl.creation_date) using the method:
	- setTimestamp()
"""
	__slots__ = ['timestamp', 'timestamps', 'secret', 'version', 'licence_id', 'company_id', 'mailing_id', 'customer_id', 'url_id', 'bit_option', 'prefix']
	versions = (2, 3)
	default_version = versions[-1]
	symbols = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_'
	def __init__ (self) -> None:
		self.timestamp = 0
		self.timestamps = [0, 0]
		self.secret: Optional[str] = None
		self.version = self.default_version
		self.licence_id = licence
		self.company_id = 0
		self.mailing_id = 0
		self.customer_id = 0
		self.url_id = 0
		self.bit_option = 0
		self.prefix: Optional[str] = None
	
	def __str__ (self) -> str:
		return '%s <%s>' % (
			self.__class__.__name__,
			Stream.of (
				('version', self.version),
				('licence', self.licence_id),
				('company', self.company_id),
				('mailing', self.mailing_id),
				('customer', self.customer_id),
				('urlid', self.url_id),
				('bit_option', Stream.loop (
					(self.bit_option, ''),
					lambda bv: bool (bv[0]),
					lambda bv: (bv[0] >> 1, ('1' if bv[0] & 0x1 else '0') + bv[1]),
					finalizer = lambda bv: bv[1] if bv[1] else None
				)),
				('prefix', self.prefix)
			)
			.filter (lambda kv: kv[1] is not None)
			.map (lambda kv: '%s=%r' % kv)
			.join (', ')
		)

	def __enter__ (self) -> UID:
		self.open ()
		return self

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		self.close ()
		return None
	
	def __iencode (self, n: Optional[int]) -> str:
		rc = ''
		if n is not None:
			if n <= 0:
				rc = self.symbols[0]
			else:
				while n > 0:
					rc = self.symbols[n & 0x3f] + rc
					n >>= 6
		return rc

	def __idecode (self, s: str) -> int:
		n = 0
		for ch in s:
			n <<= 6
			v = self.symbols.find (ch)
			if v == -1:
				raise error ('Illegal charater in numeric value')
			n += v
		return n

	def __check_version (self, version: int = -1) -> None:
		if version == -1:
			version = self.version
		if version not in self.versions:
			raise error ('uid: unsupported version %r detected, known versions are: %s' % (version, ', '.join ([str (_v) for _v in self.versions])))

	def __make_signature (self) -> str:
		sig: List[Union[None, int, str]] = []
		if self.prefix:
			sig.append (self.prefix)
		sig.append (self.version)
		sig.append (self.licence_id)
		sig.append (self.mailing_id)
		sig.append (self.customer_id)
		sig.append (self.url_id)
		if self.version == 3:
			sig.append (self.bit_option)
		sig.append (self.secret)
		dig = hashlib.sha512 ()
		dig.update (Stream (sig).join ('.').encode ('UTF-8'))
		return base64.urlsafe_b64encode (dig.digest ()).decode ('us-ascii').replace ('=', '')

	def __date2timestamp (self, year: int, month: int, day: int, hour: int, minute: int, second: int, dst: int = -1) -> int:
		return int (time.mktime ((year, month, day, hour, minute, second, -1, -1, dst))) * 1000

	def set_timestamp (self, timestamp: Union[int, datetime, time.struct_time]) -> None:
		"""Set timestamp for mailing

this is the creation date of the assigned mailing, either as type
datetime or time.struct_time."""
		if type (timestamp) is datetime:
			ts_datetime = cast (datetime, timestamp)
			timestamp = self.__date2timestamp (ts_datetime.year, ts_datetime.month, ts_datetime.day, ts_datetime.hour, ts_datetime.minute, ts_datetime.second)
		elif type (timestamp) is time.struct_time:
			ts_stime = cast (time.struct_time, timestamp)
			timestamp = self.__date2timestamp (ts_stime.tm_year, ts_stime.tm_mon, ts_stime.tm_mday, ts_stime.tm_hour, ts_stime.tm_min, ts_stime.tm_sec, ts_stime.tm_isdst)
		self.timestamp = cast (int, timestamp)
		self.timestamps = [self.timestamp & 0xffff, (self.timestamp * 37) & 0xffff]

	def open (self) -> bool:
		return True
	
	def close (self) -> None:
		pass

	def create (self) -> str:
		"""Creates a new UID

returns a newly created UID from the set instance variables."""
		self.__check_version ()
		uid: List[str] = []
		if self.prefix:
			uid.append (self.prefix)
		uid.append (self.__iencode (self.version))
		uid.append (self.__iencode (self.licence_id))
		uid.append (self.__iencode (self.mailing_id))
		if self.version == 2:
			uid.append (self.__iencode (self.customer_id ^ self.timestamps[0]))
			uid.append (self.__iencode (self.url_id ^ self.timestamps[1] ^ self.company_id))
		elif self.version == 3:
			uid.append (self.__iencode (self.customer_id))
			uid.append (self.__iencode (self.url_id))
			uid.append (self.__iencode (self.bit_option))
		uid.append (self.__make_signature ())
		return '.'.join (uid)

	def parse (self, uid: str, validate: bool = True) -> None:
		"""Parses a UID

parses a UID and fill the instance variables from the UID. As you do
normally not know the mailing_id at this stage, you can not call
set_timestamp() before as required.

If validate is True, validation of the signature is performed and an
exception is thrown, if it is not valid."""
		elem = uid.split ('.')
		if not elem:
			raise error ('Empty UID')
		if len (elem) == 6:
			hasPrefix = False
		elif len (elem) == 7:
			try:
				temp = self.__idecode (elem[0])
				self.__check_version (temp)
				if temp in (0, 2):
					hasPrefix = True
				else:
					hasPrefix = False
			except error:
				hasPrefix = True
		elif len (elem) == 8:
			hasPrefix = True
		else:
			raise error ('Invalid formated UID')
		if hasPrefix:
			self.prefix = elem[0]
			elem = elem[1:]
		else:
			self.prefix = None
		#
		version = self.__idecode (elem[0])
		self.__check_version (version)
		#
		self.version = version
		self.licence_id = self.__idecode (elem[1])
		self.mailing_id = self.__idecode (elem[2])
		if not self.fill ():
			raise error ('Failed to find secrets for mailing_id %d' % self.mailing_id)
		if self.version == 2:
			self.customer_id = self.__idecode (elem[3]) ^ self.timestamps[0]
			self.url_id = self.__idecode (elem[4]) ^ self.timestamps[1] ^ self.company_id
			self.bit_option = 0
		elif self.version == 3:
			self.customer_id = self.__idecode (elem[3])
			self.url_id = self.__idecode (elem[4])
			self.bit_option = self.__idecode (elem[5])
		if validate and elem[-1] != self.__make_signature ():
			raise error ('Signature mismatch')

	def fill (self) -> bool:
		"""Can be overwritten to fill missing parameter for parsing
		
You have to implement this method which is called, so you can retrieve
the mailing_id by using the attribute "mailing_id" from this variable
and provide the timestamp information. Returns True on success or
False on failure."""
		return True

class CachingUID (UID):
	"""Convinient UID handling

compared to the UID handles this class the retrival of database
values by itself."""
	__slots__ = ['dbid', 'db', 'cursor', 'used_id', 'make_version', 'min_version']
	licences: Dict[Optional[str], int] = {}
	companies: Dict[Tuple[Optional[str], int], Optional[CachingUID.Company]] = {}
	mailings: Dict[Tuple[Optional[str], int], Optional[CachingUID.Mailing]] = {}
	TRACKING_VETO = 0
	DISABLE_LINK_EXTENSION = 1
	class Company (NamedTuple):
		secret: str
		make_version: int
		min_version: int
	class Mailing (NamedTuple):
		company_id: int
		timestamp: datetime
	def __init__ (self, dbid: Optional[str] = None) -> None:
		super ().__init__ ()
		self.dbid = dbid
		self.db: Optional[DB] = None
		self.used_id: Optional[str] = None
		self.make_version: Optional[int] = None
		self.min_version: Optional[int] = None
	
	def __is_open_db (self) -> bool:
		if self.db is not None and self.dbid != self.used_id:
			self.__close_db ()
		return self.db is not None and self.db.isopen ()
	
	def __open_db (self) -> bool:
		self.db = DB (self.dbid)
		if self.db.isopen ():
			self.used_id = self.dbid
		else:
			self.db.close ()
			self.db = None
		return self.__is_open_db ()
	
	def __close_db (self) -> None:
		if self.db is not None:
			self.db.close ()
		self.db = None
	
	def __retrieve_licence (self) -> None:
		if self.dbid not in self.licences:
			licence = 0
			if self.__is_open_db () or self.__open_db ():
				cc = CompanyConfig (self.db)
				with Ignore (KeyError):
					licence_str = cc.get_config (class_name = 'system', name = 'licence')
					try:
						licence = int (licence_str)
					except ValueError:
						logger.error ('Invalid licence %r in database found for %r' % (licence_str, self.dbid))
			self.licences[self.dbid] = licence
	
	def __retrieve_mailing (self) -> None:
		if self.mailing_id:
			key = (self.dbid, self.mailing_id)
			try:
				mailing: Optional[CachingUID.Mailing] = self.mailings[key]
			except KeyError:
				mailing = None
				if self.__is_open_db () or self.__open_db ():
					for r in cast (DB, self.db).query ('SELECT company_id, creation_date FROM mailing_tbl WHERE mailing_id = :mailing_id', {'mailing_id': self.mailing_id}):
						mailing = CachingUID.Mailing (
							company_id = r.company_id,
							timestamp = r.creation_date
						)
				self.mailings[key] = mailing
			if mailing is not None:
				self.company_id = mailing.company_id
				self.set_timestamp (mailing.timestamp)
			else:
				self.company_id = 0
				self.set_timestamp (0)
		
	def __retrieve_company (self) -> None:
		if self.company_id:
			key = (self.dbid, self.company_id)
			try:
				company: Optional[CachingUID.Company] = self.companies[key]
			except KeyError:
				company = None
				if self.__is_open_db () or self.__open_db ():
					for r in cast (DB, self.db).query ('SELECT secret_key, enabled_uid_version, uid_version FROM company_tbl WHERE company_id = :company_id', {'company_id': self.company_id}):
						company = CachingUID.Company (
							secret = r.secret_key,
							make_version = r.enabled_uid_version,
							min_version = r.uid_version
						)
				self.companies[key] = company
			if company is not None:
				self.secret = company.secret
				self.make_version = company.make_version
				self.min_version = company.min_version
			else:
				self.secret = None
				self.make_version = None
				self.min_version = None
	
	def open (self) -> bool:
		"""Open the database connection"""
		self.__close_db ()
		return self.__open_db ()
	
	def close (self) -> None:
		"""Finalize usage of class and release resources"""
		self.__close_db ()

	def create (self) -> str:
		"""Create a new UID using the previously set instance variables"""
		isopen = self.__is_open_db ()
		self.__retrieve_mailing ()
		self.__retrieve_company ()
		if self.make_version is not None:
			old_version = self.version
			self.version = self.make_version
			if self.version not in self.versions:
				self.version = (Stream.of (self.versions if self.min_version is not None else None)
					.filter (lambda v: self.min_version is None or v >= self.min_version)
					.first (no = self.default_version)
				)
		fail = None
		try:
			rc = super ().create ()
		except error as e:
			fail = e
		finally:
			if self.make_version is not None:
				self.version = old_version
		if not isopen:
			self.__close_db ()
		if fail is not None:
			raise fail
		return rc
	
	def parse (self, uid: str, validate: bool = True) -> None:
		"""Parse a uid and fill the instance variables with the values"""
		isopen = self.__is_open_db ()
		try:
			super ().parse (uid, validate)
		finally:
			if not isopen:
				self.__close_db ()
		if validate and self.min_version is not None and self.version < self.min_version:
			raise error ('version %d too old, allow only version %d and up for company %d' % (self.version, self.min_version, self.company_id))

	def __bit_is_set (self, bit: int) -> bool:
		return bool (self.bit_option & (1 << bit))
	def __bit_set (self, bit: int, value: bool) -> None:
		if value:
			self.bit_option |= (1 << bit)
		else:
			self.bit_option &= ~(1 << bit)
	def _tracking_veto_get (self) -> bool:
		return self.__bit_is_set (self.TRACKING_VETO)
	def _tracking_veto_set (self, value: bool) -> None:
		self.__bit_set (self.TRACKING_VETO, value)
	def _tracking_veto_del (self) -> None:
		self.__bit_set (self.TRACKING_VETO, False)
	tracking_veto = property (_tracking_veto_get, _tracking_veto_set, _tracking_veto_del)

	def _disable_link_extension_get (self) -> bool:
		return self.__bit_is_set (self.DISABLE_LINK_EXTENSION)
	def _disable_link_extension_set (self, value: bool) -> None:
		self.__bit_set (self.DISABLE_LINK_EXTENSION, value)
	def _disable_link_extension_del (self) -> None:
		self.__bit_set (self.DISABLE_LINK_EXTENSION, False)
	disable_link_extension = property (_disable_link_extension_get, _disable_link_extension_set, _disable_link_extension_del)
	
	def fill (self) -> bool:
		self.__retrieve_licence ()
		if self.licence_id != self.licences[self.dbid]:
			raise error ('Unable to fill foreign ID (%r vs. %r)' % (self.licence_id, self.licences[self.dbid]))
		self.__retrieve_mailing ()
		self.__retrieve_company ()
		if self.company_id and self.timestamp and self.secret is not None:
			return True
		return False

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
		return '%s <%s>' % (
			self.__class__.__name__,
			Stream.of (
				('mailing', self.mailing_id),
				('customer', self.customer_id),
				('source', self.source),
				('selector', self.selector))
			.filter (lambda kv: kv[1] is not None)
			.map (lambda kv: '%s=%r' % kv)
			.join (', ')
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
		s: bytes = cast (str, source).encode ('UTF-8') if type (source) is str else cast (bytes, source)
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
		src = '%d;%d;%s' % (mailing_id, customer_id, self.__source (source))
		if selector is not None:
			src += ';%s' % selector
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

