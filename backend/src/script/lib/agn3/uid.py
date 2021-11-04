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
import	logging, hashlib, base64
from	collections import deque
from	dataclasses import dataclass, replace
from	datetime import datetime
from	types import TracebackType
from	typing import ClassVar, Final, Optional, Union
from	typing import Deque, Dict, List, NamedTuple, Set, Tuple, Type
from	.db import DB
from	.dbconfig import DBConfig
from	.definitions import licence
from	.emm.companyconfig import CompanyConfig
from	.exceptions import error
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
	url_id: int = 0
	bit_option: int = 0
	prefix: Optional[str] = None
	TRACKING_VETO: ClassVar[int] = 0
	DISABLE_LINK_EXTENSION: ClassVar[int] = 1

	def __hash__ (self) -> int:
		return hash ((self.version, self.licence_id, self.company_id, self.mailing_id, self.customer_id, self.url_id, self.bit_option, self.prefix))
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
	
class UIDCache:
	__slots__ = ['instances']
	class Company (NamedTuple):
		company_id: int
		secret_key: str
		enabled_uid_version: int
		minimal_uid_version: int
		
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
						'SELECT secret_key, enabled_uid_version, uid_version '
						'FROM company_tbl '
						'WHERE company_id = :company_id AND status = :status',
						{
							'company_id': company_id,
							'status': 'active'
						}
					)
					if rq is not None:
						company = UIDCache.Company (
							company_id = company_id,
							secret_key = rq.secret_key,
							enabled_uid_version = rq.enabled_uid_version,
							minimal_uid_version = rq.uid_version
						)
					self.companies[company_id] = company
			if company is not None:
				return company
			raise error (f'{self.licence_id}: company {company_id} not found or active')
			
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
							ccfg = CompanyConfig (db = db)
							ccfg.read ()
							licence_id = int (ccfg.get_config ('system', 'licence'))
							if licence_id != 0:
								raise error (f'invalid licence_id {licence_id} found')
							self.instances[licence_id] = UIDCache.Instance (licence_id, db)
							seen.add (dbid)
					except (error, KeyError, ValueError) as e:
						logger.debug (f'{dbid}: failed to open database: {e}')
						db.close ()
		
	def __del__ (self) -> None:
		self.done ()
			
	def done (self) -> None:
		for instance in self.instances.values ():
			instance.close ()
		
	def find (self, uid: UID) -> Tuple[UIDCache.Company, UIDCache.Mailing]:
		if uid.licence_id != 0:
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
	__slots__ = ['cache', 'credentials']
	available_versions: Final[Tuple[int, ...]] = (2, 3)
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
		self.credentials: Dict[Tuple[int, int, int], UIDHandler.Credential] = {}

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

	def __make_signature (self, version: int, uid: UID, credential: UIDHandler.Credential) -> str:
		sig: List[Union[int, str]] = []
		if version == 2:
			if uid.prefix:
				sig.append (uid.prefix)
			sig += [version, uid.licence_id, uid.mailing_id, uid.customer_id, uid.url_id, credential.secret]
		elif version == 3:
			if uid.prefix:
				sig.append (uid.prefix)
			sig += [version, uid.licence_id, uid.mailing_id, uid.customer_id, uid.url_id, uid.bit_option, credential.secret]
		dig = hashlib.sha512 ()
		dig.update (Stream (sig).join ('.').encode ('UTF-8'))
		return base64.urlsafe_b64encode (dig.digest ()).decode ('us-ascii').replace ('=', '')

	def __select_version (self, version: Optional[int] = None) -> int:
		if version is None:
			return UIDHandler.default_version
		if version in UIDHandler.available_versions:
			return version
		raise error ('uid: unsupported version {version} detected, known versions are: {versions}'.format (
			version = version,
			versions = ', '.join ([str (_v) for _v in UIDHandler.available_versions])
		))
	
	def __find_credential (self, uid: UID) -> UIDHandler.Credential:
		key = (uid.licence_id, uid.company_id, uid.mailing_id)
		try:
			return self.credentials[key]
		except KeyError:
			self.credentials[key] = credential = self.retrieve_credential (uid)
			return credential

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
		parts.append (self.__make_signature (uid_version, uid, credential))
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
		if not elem:
			raise error ('Empty UID')
		if len (elem) == 6:
			has_prefix = False
		elif len (elem) == 7:
			try:
				has_prefix = self.__select_version (self.__idecode (elem[0])) == 2
			except error as e:
				logger.debug (f'{uid_str}: assume having a prefix as: {e}')
				has_prefix = True
		elif len (elem) == 8:
			has_prefix = True
		else:
			raise error ('Invalid formated UID')
		if has_prefix:
			uid.prefix = elem[0]
			elem = elem[1:]
		#
		uid.version = self.__idecode (elem[0])
		self.__select_version (uid.version)
		#
		uid.licence_id = self.__idecode (elem[1])
		uid.mailing_id = self.__idecode (elem[2])
		try:
			credential: Optional[UIDHandler.Credential] = self.retrieve_credential (uid)
		except error as e:
			if validate:
				raise error (f'{uid}: missing credentials (required for validation): {e}')
			credential = None
		if uid.version == 2:
			if credential is not None:
				uid.customer_id = self.__idecode (elem[3]) ^ credential.timestamps[0]
				uid.url_id = self.__idecode (elem[4]) ^ credential.timestamps[1] ^ uid.company_id
			uid.bit_option = 0
		elif uid.version == 3:
			uid.customer_id = self.__idecode (elem[3])
			uid.url_id = self.__idecode (elem[4])
			uid.bit_option = self.__idecode (elem[5])
		if validate:
			if credential is None:
				raise error (f'{uid}: missing credentials')
			if credential.minimal_uid_version is not None and credential.minimal_uid_version > uid.version:
				raise error (f'{uid}: outdated version')
			if elem[-1] != self.__make_signature (uid.version, uid, credential):
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

	def add_credential (self,
		licence_id: int,
		company_id: int,
		mailing_id: int,
		timestamp: Union[int, float],
		secret: str,
		enabled_uid_version: int = default_version,
		minimal_uid_version: Optional[int] = None
	) -> UIDHandler.Credential:
		self.credentials[(licence_id, company_id, mailing_id)] = credential = self.new_credential (timestamp, secret, enabled_uid_version, minimal_uid_version)
		return credential
