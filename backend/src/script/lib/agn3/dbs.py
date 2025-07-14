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
import	logging
from	types import TracebackType
from	typing import Optional
from	typing import Dict, Iterator, Type
from	.db import Row, DBIgnore, DB
from	.dbconfig import DBConfig
from	.emm.config import EMMConfig
from	.exceptions import error
from	.stream import Stream
#
__all__ = ['Row', 'DBIgnore', 'DB', 'DBs']
#
logger = logging.getLogger (__name__)
#
class DBs:
	"""Access all accessable (and configured in dbcfg) databases available"""
	__slots__ = ['instances', 'by_licence']
	class Instance (DB):
		__slots__ = ['licence_id']
		def setup (self) -> None:
			self.licence_id = int (EMMConfig (db = self, class_names = ['system']).get ('system', 'licence'))
		
	def __init__ (self) -> None:
		self.instances: Dict[str, DBs.Instance] = {}
		self.by_licence: Dict[int, DBs.Instance] = {}
		for dbid in Stream (DBConfig ()).distinct ():
			instance = DBs.Instance (dbid = dbid)
			try:
				if instance.open ():
					instance.setup ()
					self.instances[dbid] = instance
					self.by_licence[instance.licence_id] = instance
				else:
					raise error ('no access: {error}'.format (error = instance.last_error ()))
			except error as e:
				logger.warning (f'{dbid}: failed to open database: {e}')
				instance.close ()

	def __del__ (self) -> None:
		self.close ()
		
	def __iter__ (self) -> Iterator[DBs.Instance]:
		return iter (self.instances.values ())
	
	def __getitem__ (self, licence_id: int) -> DBs.Instance:
		return self.by_licence[licence_id]
	
	def __enter__ (self) -> DBs:
		return self

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		self.close (exc_type is None)
		return None

	def close (self, commit: bool = True) -> None:
		for (dbid, instance) in self.instances.items ():
			try:
				instance.close (commit)
			except error as e:
				logger.debug (f'{dbid}: failed to close database: {e}')
		self.instances.clear ()
		self.by_licence.clear ()

