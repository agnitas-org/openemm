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
from	..ignore import Ignore
#
with Ignore (ImportError):
	try:
		import	psycopg2 as pg
	except:
		raise ImportError ()
	from	typing import Any, Callable
	from	typing import cast
	from	typing_extensions import override
	from	.types import Driver
	from	..dbapi import DBAPI
	from	..dbcli import DBCLI
	from	..dbcore import Cursor, Core
	from	..exceptions import error
	from	..io import which

	class CursorPostgreSQL (Cursor):
		__slots__ = []
		@override
		def __init__ (self, db: Core, autocommit: bool) -> None:
			super ().__init__(db, autocommit)

		mapping = {
			# TIMESTAMPTZ
			1184: lambda v: v.replace (tzinfo = None) if v is not None else None
		}
		mappings = set (mapping.keys ())
		@override
		def rowmapper (self, description: list[tuple[Any, ...]]) -> list[Callable[[Any], Any]]:
			if self.mappings.intersection ({_d[1] for _d in description}):
				return [self.mapping.get (_d[1], lambda v: v) for _d in description]
			return super ().rowmapper (description)

	class PostgreSQL (Core):
		__slots__ = ['host', 'user', 'password', 'database']
		def __init__ (self, host: str, user: str, password: str, database: str) -> None:
			super ().__init__ ('postgres', cast (DBAPI.Vendor, pg), CursorPostgreSQL)
			self.host = host
			self.user = user
			self.password = password
			self.database = database
			
		@override
		def connect (self) -> None:
			port: None | int = None
			try:
				(host, port_str) = self.host.split (':', 1)
				port = int (port_str)
			except ValueError:
				host = self.host
			config: dict[str, bool | int | str] = {
				'host': host,
				'user': self.user,
				'password': self.password,
				'database': self.database
			}
			if port is not None:
				config['port'] = port
			self.db = self.driver.connect (**config)
			cast (DBAPI.DriverPostgres, self.db).autocommit = False
			cast (DBAPI.DriverPostgres, self.db).set_client_encoding ('UTF-8')
			if self.schema is None:
				with self.cursor () as cursor:
					rq = cursor.querys ('SELECT current_schema')
					if rq is not None:
						self.schema = rq.current_schema
				if self.schema is None:
					self.schema = self.user
		
		@override
		def cli_setup (self) -> DBCLI:
			if (psql := which ('psql')) is None:
				raise error ('cli: missing executable')
			#
			try:
				(hostname, port) = self.host.split (':', 1)
			except ValueError:
				(hostname, port) = (self.host, '5432')
			#
			class DBCLIPostgres (DBCLI):
				@override
				def process_file (self, filename: str) -> DBCLI.Result:
					return self.invoke (command = self.command + [f'--file={filename}'])
			#
			return DBCLIPostgres (
				command = [
					psql,
					'--echo-errors',
					'--quiet',
					'--no-readline',
					'--set=ON_ERROR_STOP=on',
					f'--host={hostname}',
					f'--port={port}',
					f'--username={self.user}',
					f'--dbname={self.database}'
				], env = {
					'PGPASSWORD': self.password
				}
			)
			
	postgres = Driver (
		'postgres',
		{'postgresql', 'pg'},
		lambda cfg: PostgreSQL (
			cfg['host'],
			cfg['user'],
			cfg['password'],
			cfg['name']
		)
	)
