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
from	typing import Any, Optional
from	typing import Dict, List
from	typing import cast
from	.exceptions import error
from	.definitions import syscfg
from	.dbconfig import DBConfig
from	.dbcore import Core
from	._db import Drivers
#
__all__ = ['DBDriver']
#
class DBDriver:
	"""Keeps track of available database driver

in conjunction with DBConfig this class can select a database driver
and create an instance for it."""
	__slots__: List[str] = []
	dbcfg: Optional[DBConfig] = None
	@classmethod
	def request (cls, provided_id: Optional[str] = None) -> Core:
		"""Creates a new database driver instance

according to ``id'' the matching driver and the access parameter are
selected from the database configuration."""
		id = provided_id if provided_id is not None else syscfg.get ('dbid', 'emm')
		if cls.dbcfg is None:
			cls.dbcfg = DBConfig ()
		cfg = cls.dbcfg[id]
		dbms = cfg ('dbms')
		if dbms is not None:
			dbms = dbms.lower ()
		return cls.instance (dbms, cfg)
	
	@classmethod
	def driver (cls, dbms: str, **kwargs: str) -> Core:
		cfg = DBConfig.DBRecord (dbms)
		cfg.update (kwargs)
		return cls.instance (dbms, cfg)
	
	@classmethod
	def instance (cls, dbms: Optional[str], cfg: DBConfig.DBRecord) -> Core:
		for driver in (_v.value for _v in Drivers.__members__.values ()):
			if driver.name == dbms or (driver.aliases and dbms in driver.aliases):
				creator = driver
				break
		else:
			raise error (f'Missing/unknwon dbms "{dbms}" found')
		rc = cast (Core, creator.new (cfg))
		rc.setup (cfg)
		return rc
	
	@classmethod
	def inject (cls, id: str, param: Dict[str, Any]) -> None:
		"""adds a new configuration into the database configuration"""
		if cls.dbcfg is None:
			cls.dbcfg = DBConfig ()
		cls.dbcfg.inject (id, param)
	
	@classmethod
	def remove (cls, id: str) -> None:
		"""removes a configuration from the database configuration"""
		if cls.dbcfg is None:
			cls.dbcfg = DBConfig ()
		cls.dbcfg.remove (id)
