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
import	logging, hashlib, base64, pickle
import	msgpack
from	collections import deque
from	dataclasses import dataclass, field, replace
from	datetime import datetime
from	types import TracebackType
from	typing import Any, ClassVar, Final, Optional, Union
from	typing import Deque, Dict, List, NamedTuple, Set, Tuple, Type
from	.db import DB
from	.dbconfig import DBConfig
from	.definitions import licence
from	.emm.config import EMMConfig
from	.exceptions import error
from	.ignore import Ignore
from	.log import log_limit
from	.stream import Stream
#
__all__ = ['UID', 'UIDHandler']
#
logger = logging.getLogger (__name__)
#
@dataclass
class UID:
	version: Optional[int] = None
	licence_id: int = 0
	company_id: int = 0
	mailing_id: int = 0
	customer_id: int = 0
	senddate: None | datetime = None
	status_field: None | str = None
	url_id: int = 0
	position: int = 0
	bit_option: int = 0
	prefix: Optional[str] = None
	ctx: Dict[str, Any] = field (default_factory = dict)
	TRACKING_VETO: ClassVar[int] = 0
	DISABLE_LINK_EXTENSION: ClassVar[int] = 1

	def __hash__ (self) -> int:
		try:
			hashable_context: Union[bytes, str] = pickle.dumps (self.ctx)
		except:
			hashable_context = str (self.ctx)
		return hash ((self.version, self.licence_id, self.company_id, self.mailing_id, self.customer_id, self.url_id, self.position, self.bit_option, self.prefix, hashable_context))
	def __getitem__ (self, option: str) -> Any:
		return self.ctx[option]
	def __setitem__ (self, option: str, value: Any) -> None:
		self.ctx[option] = value
	def __delitem__ (self, option: str) -> None:
		del self.ctx[option]

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

	@property
	def context (self) -> bytes:
		def set (key: str, value: Union[None, int, str]) -> None:
			if value is not None and bool (value):
				self.ctx[key] = value
			elif key in self.ctx:
				del self.ctx[key]
		set ('_l', self.licence_id)
		set ('_c', self.company_id)
		set ('_m', self.mailing_id)
		set ('_r', self.customer_id)
		if self.senddate is not None:
			set ('_s', int (self.senddate.timestamp ()))
		set ('_f', self.status_field)
		set ('_u', self.url_id)
		if self.url_id and self.position > 1:
			set ('_x', self.position)
		set ('_o', self.bit_option)
		return msgpack.dumps (Stream (self.ctx.items ()).sorted (key = lambda kv: kv[0]).dict ())
	
	def parse (self, content: bytes) -> None:
		try:
			self.ctx = msgpack.loads (content)
		except Exception as e:
			raise error (f'failed to unpack {content!r}: {e}')
		else:
			self.licence_id = self.ctx.get ('_l', self.licence_id)
			self.company_id = self.ctx.get ('_c', self.company_id)
			self.mailing_id = self.ctx.get ('_m', self.mailing_id)
			self.customer_id = self.ctx.get ('_r', self.customer_id)
			if (senddate := self.ctx.get ('_s')) is not None:
				self.senddate = datetime.fromtimestamp (senddate)
			self.status_field = self.ctx.get ('_f', self.status_field)
			self.url_id = self.ctx.get ('_u', self.url_id)
			self.position = self.ctx.get ('_x', self.position)
			self.bit_option = self.ctx.get ('_o', self.bit_option)
	
	@staticmethod
	def encode (content: bytes) -> str:
		try:
			return base64.urlsafe_b64encode (content).rstrip (b'=').decode ('us-ascii')
		except Exception as e:
			raise error (f'failed to encode {content!r}: {e}')
	@staticmethod
	def decode (content: str) -> bytes:
		try:
			return base64.urlsafe_b64decode (content + '=' * (len (content) % 4))
		except Exception as e:
			raise error (f'failed to decode {content}: {e}')
	
class UIDCache:
	__slots__ = ['instances']
	class Company (NamedTuple):
		company_id: int
		secret_key: str
		enabled_uid_version: int
		minimal_uid_version: int
		status: str
		
	class Mailing (NamedTuple):
		mailing_id: int
		company_id: int
		creation_date: datetime

	class Instance:
		__slots__ = ['licence_id', 'db', 'companies', 'mailings']
		def __init__ (self, licence_id: int, db: DB) -> None:
			self.licence_id = licence_id
			self.db = db
			self.companies: Dict[int, Optional[UIDCache.Company]] = {}
			self.mailings: Dict[int, Optional[UIDCache.Mailing]] = {}
			
		def close (self) -> None:
			self.db.close ()
			
		def find_company (self, company_id: int) -> UIDCache.Company:
			try:
				company = self.companies[company_id]
			except KeyError:
				company = None
				if self.db.isopen ():
					rq = self.db.querys (
						'SELECT secret_key, enabled_uid_version, uid_version, status '
						'FROM company_tbl '
						'WHERE company_id = :company_id',
						{
							'company_id': company_id
						}
					)
					if rq is not None:
						company = UIDCache.Company (
							company_id = company_id,
							secret_key = rq.secret_key,
							enabled_uid_version = rq.enabled_uid_version,
							minimal_uid_version = rq.uid_version,
							status = rq.status
						)
				self.companies[company_id] = company
			if company is not None and company.status == 'active':
				return company
			raise error ('{licence_id}: company {company_id} not {reason}'.format (
				licence_id = self.licence_id,
				company_id = company_id,
				reason = 'found' if company is None else f'active, but {company.status}'
			))
			
		def find_mailing (self, mailing_id: int) -> UIDCache.Mailing:
			try:
				mailing = self.mailings[mailing_id]
			except KeyError:
				mailing = None
				if self.db.isopen ():
					rq = self.db.querys (
						'SELECT company_id, creation_date '
						'FROM mailing_tbl '
						'WHERE mailing_id = :mailing_id',
						{
							'mailing_id': mailing_id
						}
					)
					if rq is not None:
						mailing = UIDCache.Mailing (
							mailing_id = mailing_id,
							company_id = rq.company_id,
							creation_date = rq.creation_date
						)
				self.mailings[mailing_id] = mailing
			if mailing is not None:
				return mailing
			raise error (f'{self.licence_id}: mailing {mailing_id} not found')

	def __init__ (self, handle_only_own_instance: bool) -> None:
		self.instances: Dict[int, UIDCache.Instance] = {}
		if handle_only_own_instance:
			self.instances[licence] = UIDCache.Instance (licence, DB ())
		else:
			seen: Set[str] = set ()
			for dbid in DBConfig ():
				if dbid not in seen:
					db = DB (dbid = dbid)
					try:
						if db.open ():
							licence_id = int (EMMConfig (db = db, class_names = ['system']).get ('system', 'licence'))
							if licence_id < 0:
								raise error (f'invalid licence_id {licence_id} found')
							self.instances[licence_id] = UIDCache.Instance (licence_id, db)
							seen.add (dbid)
							log_limit (logger.info, f'{dbid}: using for licence {licence_id}')
					except (error, KeyError, ValueError) as e:
						log_limit (logger.warning, f'{dbid}: failed to open database: {e}', duration = '1h')
						db.close ()
		
	def __del__ (self) -> None:
		self.done ()
			
	def done (self) -> None:
		for instance in self.instances.values ():
			instance.close ()
		
	def find (self, uid: UID) -> Tuple[UIDCache.Company, UIDCache.Mailing]:
		if uid.licence_id < 0:
			raise error (f'invalid licence_id {uid.licence_id}')
		try:
			instance = self.instances[uid.licence_id]
		except KeyError:
			raise error (f'licence_id {uid.licence_id} not known')
		if uid.mailing_id <= 0:
			raise error (f'invalid mailing_id {uid.mailing_id}')
		mailing = instance.find_mailing (uid.mailing_id)
		if uid.company_id == 0:
			uid.company_id = mailing.company_id
		elif uid.company_id != mailing.company_id:
			raise error (f'company mismatch for mailing {uid.mailing_id}: expect {uid.company_id} but found {mailing.company_id}')
		company = instance.find_company (uid.company_id)
		return (company, mailing)

class UIDHandler:
	__slots__ = ['cache']
	available_versions: Final[Tuple[int, ...]] = (2, 3, 4, 5)
	default_version: Final[int] = available_versions[-1]
	symbols: Final[str] = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_'
	class Credential (NamedTuple):
		timestamp: int
		timestamps: Tuple[int, int]
		secret: str
		enabled_uid_version: int
		minimal_uid_version: Optional[int]

	def __init__ (self, enable_cache: bool = False, handle_only_own_instance: bool = True) -> None:
		self.cache = UIDCache (handle_only_own_instance) if enable_cache else None

	def __del__ (self) -> None:
		self.done ()
	
	def __enter__ (self) -> UIDHandler:
		return self

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		self.done ()
		return None

	def __iencode (self, n: int) -> str:
		if n == 0:
			return UIDHandler.symbols[0]
		collect: Deque[str] = deque ()
		while n > 0:
			collect.appendleft (UIDHandler.symbols[n & 0x3f])
			n >>= 6
		return ''.join (collect)

	def __idecode (self, s: str) -> int:
		result = 0
		for ch in s:
			result <<= 6
			value = UIDHandler.symbols.find (ch)
			if value == -1:
				raise error (f'Illegal charater "{ch}" in numeric value')
			result += value
		return result

	def __make_signature (self, version: int, uid: UID, content: Optional[bytes], credential: UIDHandler.Credential) -> str:
		sig: List[Union[int, str, bytes]] = []
		if version == 2:
			if uid.prefix:
				sig.append (uid.prefix)
			sig += [version, uid.licence_id, uid.mailing_id, uid.customer_id, uid.url_id, credential.secret]
		elif version == 3:
			if uid.prefix:
				sig.append (uid.prefix)
			sig += [version, uid.licence_id, uid.mailing_id, uid.customer_id, uid.url_id, uid.bit_option, credential.secret]
		elif version == 4:
			if uid.prefix:
				sig.append (uid.prefix)
			sig += [version, uid.licence_id, uid.company_id, uid.mailing_id, uid.customer_id, uid.url_id, uid.bit_option, credential.secret]
		elif version == 5:
			if uid.prefix:
				sig.append (uid.prefix)
			sig += [version, content if content is not None else uid.context, credential.secret]
		dig = hashlib.sha512 ()
		dig.update (b'.'.join (Stream (sig).map (lambda e: e if isinstance (e, bytes) else str (e).encode ('UTF-8'))))
		return UID.encode (dig.digest ())

	def __select_version (self, version: Optional[int] = None) -> int:
		if version is None or version == 0:
			return UIDHandler.default_version
		if version in UIDHandler.available_versions:
			return version
		raise error ('uid: unsupported version {version} detected, known versions are: {versions}'.format (
			version = version,
			versions = ', '.join ([str (_v) for _v in UIDHandler.available_versions])
		))
	
	def done (self) -> None:
		if self.cache is not None:
			self.cache.done ()
			self.cache = None

	def create (self, uid: UID, version: Optional[int] = None) -> str:
		"""Creates a new UID

returns a newly created UID from the set instance variables."""
		if uid.licence_id == 0 and licence != 0:
			uid = replace (uid, licence_id = licence)
		credential = self.retrieve_credential (uid)
		uid_version = self.__select_version (version if version is not None else credential.enabled_uid_version)
		parts: List[str]
		content: Optional[bytes] = None
		if uid_version == 2:
			parts = (
				([uid.prefix] if uid.prefix else []) +
				[self.__iencode (_v) for _v in (
					uid_version,
					uid.licence_id,
					uid.mailing_id,
					uid.customer_id ^ credential.timestamps[0],
					uid.url_id ^ credential.timestamps[1] ^ uid.company_id
				)]
			)
		elif uid_version == 3:
			parts = (
				([uid.prefix] if uid.prefix else []) +
				[self.__iencode (_v) for _v in (
					uid_version,
					uid.licence_id,
					uid.mailing_id,
					uid.customer_id,
					uid.url_id,
					uid.bit_option
				)]
			)
		elif uid_version == 4:
			parts = (
				([uid.prefix] if uid.prefix else []) +
				[self.__iencode (_v) for _v in (
					uid_version,
					uid.licence_id,
					uid.company_id,
					uid.mailing_id,
					uid.customer_id,
					uid.url_id,
					uid.bit_option
				)]
			)
		elif uid_version == 5:
			content = uid.context
			parts = ([uid.prefix] if uid.prefix else []) + [
				self.__iencode (uid_version),
				UID.encode (content)
			]
		parts.append (self.__make_signature (uid_version, uid, content, credential))
		return '.'.join (parts)

	def parse (self, uid_str: str, validate: bool = True) -> UID:
		"""Parses a UID

parses a UID and fill the instance variables from the UID. As you do
normally not know the mailing_id at this stage, you can not call
set_timestamp() before as required.

If validate is True, validation of the signature is performed and an
exception is thrown, if it is not valid."""
		uid = UID ()
		elem = uid_str.split ('.')
		if len (elem) == 9:
			has_prefix = True
		elif len (elem) in (7, 8):
			has_prefix = False
			if len (elem) == 7:
				version_with_prefix = 2
				version_without_prefix = 3
			else:
				version_with_prefix = 3
				version_without_prefix = 4
			with Ignore (error):
				if self.__select_version (self.__idecode (elem[1])) == version_with_prefix:
					has_prefix = True
					with Ignore (error):
						if self.__select_version (self.__idecode (elem[0])) == version_without_prefix:
							has_prefix = False
		elif len (elem) == 6:
			has_prefix = False
		elif len (elem) == 4:
			has_prefix = True
		elif len (elem) == 3:
			has_prefix = False
		else:
			raise error (f'invalid uid {uid_str}')
		if has_prefix:
			uid.prefix = elem.pop (0)
		#
		uid.version = self.__select_version (self.__idecode (elem[0]))
		content: Optional[bytes] = None
		try:
			if uid.version == 2:
				uid.licence_id = self.__idecode (elem[1])
				uid.mailing_id = self.__idecode (elem[2])
				uid.customer_id = self.__idecode (elem[3])
				uid.url_id = self.__idecode (elem[4])
				uid.bit_option = 0
			elif uid.version == 3:
				uid.licence_id = self.__idecode (elem[1])
				uid.mailing_id = self.__idecode (elem[2])
				uid.customer_id = self.__idecode (elem[3])
				uid.url_id = self.__idecode (elem[4])
				uid.bit_option = self.__idecode (elem[5])
			elif uid.version == 4:
				uid.licence_id = self.__idecode (elem[1])
				uid.company_id = self.__idecode (elem[2])
				uid.mailing_id = self.__idecode (elem[3])
				uid.customer_id = self.__idecode (elem[4])
				uid.url_id = self.__idecode (elem[5])
				uid.bit_option = self.__idecode (elem[6])
			elif uid.version == 5:
				content = UID.decode (elem[1])
				uid.parse (content)
		except IndexError:
			raise error (f'invalid uid {uid_str} for version {uid.version}')
		#
		try:
			credential: Optional[UIDHandler.Credential] = self.retrieve_credential (uid)
		except error as e:
			if validate:
				raise error (f'{uid}: missing credentials (required for validation): {e}')
			credential = None
		#
		if uid.version == 2:
			if credential is not None:
				uid.customer_id = uid.customer_id ^ credential.timestamps[0]
				uid.url_id = uid.url_id  ^ credential.timestamps[1] ^ uid.company_id
		#
		if validate:
			if credential is None:
				raise error (f'{uid}: missing credentials')
			if credential.minimal_uid_version is not None and credential.minimal_uid_version > uid.version:
				raise error (f'{uid}: outdated version')
			if elem[-1] != self.__make_signature (uid.version, uid, content, credential):
				raise error (f'{uid}: invalid signature')
		return uid
	
	def new_credential (self,
		timestamp: Union[int, float],
		secret: str,
		enabled_uid_version: int = default_version,
		minimal_uid_version: Optional[int] = None
	) -> UIDHandler.Credential:
		miliseconds = int (timestamp) * 1000
		return UIDHandler.Credential (
			timestamp = miliseconds,
			timestamps = (miliseconds & 0xffff, (miliseconds * 37) & 0xffff),
			secret = secret,
			enabled_uid_version = enabled_uid_version,
			minimal_uid_version = minimal_uid_version
		)

	def retrieve_credential (self, uid: UID) -> UIDHandler.Credential:
		if self.cache is not None:
			try:
				(company, mailing) = self.cache.find (uid)
				return self.new_credential (mailing.creation_date.timestamp (), company.secret_key, company.enabled_uid_version, company.minimal_uid_version)
			except error as e:
				log_limit (logger.warning, f'{uid}: failed to retrieve credentials: {e}')
		raise error (f'missing credentials for {uid}')
