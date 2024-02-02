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
from	datetime import datetime
from	typing import Any, Optional
from	typing import Dict, List, NamedTuple
from	typing import cast
from	..db import DB
from	..exceptions import error
from	..parser import ParseTimestamp
#
__all__ = ['Timestamp']
#
class Timestamp:
	"""Persistance timestamp handling

for incremental processes it is important to keep track of last
timestamp it ran. To keep track of this timestamp this class manages
the persistance and offers some handy methods."""
	__slots__ = [
		'name', 'initial_timestamp', 'initial_timestamp_from',
		'description', 'db', 'mydb', 'parm',
		'lowmark', 'highmark'
	]
	class Interval (NamedTuple):
		start: datetime
		end: datetime
	class Cascade (NamedTuple):
		clause: str
		cascade: List[Dict[str, Any]]
		
	parse_timestamp = ParseTimestamp ()
	def __init__ (self,
		name: str,
		*,
		initial_timestamp: Any = None,
		initial_timestamp_from: Optional[str] = None,
		description: Optional[str] = None
	) -> None:
		self.name = name
		self.initial_timestamp = initial_timestamp
		self.initial_timestamp_from = initial_timestamp_from
		self.description = description
		self.db: Optional[DB] = None
		self.mydb = False
		self.parm: Dict[str, Any] = {'name': self.name}
		self.lowmark: Optional[datetime] = None
		self.highmark: Optional[datetime] = None

	def __cleanup (self) -> None:
		if self.mydb and self.db:
			self.db.close ()
			self.db = None

	def __setup (self, db: Optional[DB]) -> None:
		if db is None:
			self.db = DB ()
			self.mydb = True
		else:
			self.db = db
			self.mydb = False
		if not self.db.isopen ():
			self.__cleanup ()
			raise error ('Failed to setup database')
		count = self.db.querys ('SELECT count(*) FROM timestamp_tbl WHERE name = :name', self.parm)
		if count is None or not count[0]:
			rc = self.db.querys ('SELECT max(timestamp_id) + 1 FROM timestamp_tbl')
			if rc is not None and rc[0] is not None:
				tid = rc[0]
			else:
				tid = 1
			ts: Optional[datetime] = None
			if self.initial_timestamp_from is not None:
				rq = self.db.querys (
					'SELECT cur '
					'FROM timestamp_tbl '
					'WHERE name = :name',
					{
						'name': self.initial_timestamp_from
					}
				)
				if rq is not None:
					ts = rq.cur
			if ts is None:
				ts = self.parse_timestamp (self.initial_timestamp)
			if ts is None:
				ts = datetime (1980, 1, 1)
			if self.db.update ('INSERT INTO timestamp_tbl (timestamp_id, name, description, cur) VALUES (:tid, :name, :descr, :ts)', {'tid': tid, 'name': self.name, 'descr': self.description, 'ts': ts}) != 1:
				self.__cleanup ()
				raise error ('Failed to create new entry in timestamp table')
		elif count[0] != 1:
			raise error (f'Expect one entry with name "{self.name}", but found {count[0]}')

	def done (self, commit: bool = True) -> None:
		"""finalize timestamp

if ``commit'' is True, write new timestamp to database, otherwise
leave it unchanged."""
		rc = False
		if self.db and self.db.isopen ():
			if commit:
				if (
					self.db.update ('UPDATE timestamp_tbl SET prev = cur WHERE name = :name', self.parm) == 1 and
					self.db.update ('UPDATE timestamp_tbl SET cur = temp WHERE name = :name', self.parm) ==1
				):
					rc = True
			else:
				rc = True
			self.db.sync (commit)
		self.__cleanup ()
		if not rc:
			raise error ('Failed to finalize timestamp entry')

	def setup (self, db: Optional[DB] = None, timestamp: Any = None) -> None:
		"""setup timestamp

either reads an existing timestamp from the database or uses
``timestamp'' as the current value. If ``timestamp'' is None, then the
current timestamp is used. ``db'' is an optional parameter to use an
existing database driver, otherwise an own driver is created fro the
default database id."""
		self.__setup (db)
		timestamp = self.parse_timestamp.parse (timestamp, datetime.now ())
		parm = self.parm.copy ()
		parm['ts'] = timestamp
		if cast (DB, self.db).update ('UPDATE timestamp_tbl SET temp = :ts WHERE name = :name', parm) != 1:
			raise error ('Failed to setup timestamp for current time')
		rc = cast (DB, self.db).querys ('SELECT cur, temp FROM timestamp_tbl WHERE name = :name', self.parm)
		if rc is not None:
			(self.lowmark, self.highmark) = rc
		cast (DB, self.db).sync ()

	def __prepare_name (self, parm: Optional[Dict[str, Any]]) -> str:
		if parm is None:
			name = '\'{name}\''.format (name = self.name.replace ('\'', '\'\''))
		else:
			name = ':timestampName'
			parm[name[1:]] = self.name
		return name

	def make_select_lower_mark (self, parm: Optional[Dict[str, Any]] = None) -> str:
		"""creates a SQL statement to read the low mark"""
		return 'SELECT cur FROM timestamp_tbl WHERE name = {name}'.format (name = self.__prepare_name (parm))

	def make_select_upper_mark (self, parm: Optional[Dict[str, Any]] = None) -> str:
		"""creates a SQL statement to read the high mark"""
		return 'SELECT temp FROM timestamp_tbl WHERE name = {name}'.format (name = self.__prepare_name (parm))

	def make_between_clause (self, column: str, parm: Optional[Dict[str, Any]] = None) -> str:
		"""creates a SQL clause to check if a column is in current delta

if ``parm'' is not None it must be of type dict. In this case the name
of the timestamp is added to the dictionary and the value is handled
for a prepared statement, otherwise the name is directly written to
the clause."""
		name = self.__prepare_name (parm)
		return f'({column} >= (SELECT cur FROM timestamp_tbl WHERE name = {name}) AND {column} < (SELECT temp FROM timestamp_tbl WHERE name = {name}))'

	def make_interval_clause (self, column: str) -> str:
		"""create a SQL clause for a delta (using retrieved data)"""
		return f'({column} >= :timestampStart AND {column} < :timestampEnd)'

	def make_interval (self) -> Timestamp.Interval:
		"""create an interval from low to high"""
		start = self.lowmark if self.lowmark is not None else datetime (1970, 1, 1)
		end = self.highmark if self.highmark is not None else datetime.now ()
		return Timestamp.Interval (start, end)

	def make_cascading_interval (self, days: int = 1) -> List[Timestamp.Interval]:
		"""creates a list of intervals to cover as a whole the interval from low to high"""
		rc: List[Timestamp.Interval] = []
		interval = self.make_interval ()
		if interval is not None:
			if days < 1:
				days = 1
			cur = interval.start
			while cur < interval.end:
				step = cur.fromordinal (cur.toordinal () + days)
				if step > interval.end:
					step = interval.end
				rc.append (Timestamp.Interval (cur, step))
				cur = step
		return rc

	def make_cascading_queries (self, column: str, param: Optional[Dict[str, Any]] = None, days: int = 1) -> Optional[Timestamp.Cascade]:
		"""creates a SQL clause and a list of intervals to cover from low to high

this is used if you expected a lot of data to be read or a large
interval to be covered. In this case it returns an instance of class
agn.Timestamp.Cascade with two attributes:

	- clause: the clause to be used in a prepared SQL statement
	- cascade: a list of dictionaries containing each time slice

The optional ``param'' (a dict) is used as the base for each created
dictionary in cascade so you can directly use this for each
incarnation of the query."""

		rc: Optional[Timestamp.Cascade] = None
		cc = self.make_cascading_interval (days)
		if cc is not None:
			rc = Timestamp.Cascade (self.make_interval_clause (column), [])
			for c in cc:
				if param is not None:
					p = param.copy ()
				else:
					p = {}
				p['timestampStart'] = c.start
				p['timestampEnd'] = c.end
				rc.cascade.append (p)
		return rc
