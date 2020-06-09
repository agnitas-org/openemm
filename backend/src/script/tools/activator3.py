#!/usr/bin/env python3
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
import	logging, argparse
import	os, time
from	types import TracebackType
from	typing import Optional, Final
from	typing import List, Type
from	agn3.dblite import DBLite, Layout
from	agn3.definitions import base
from	agn3.exceptions import error
from	agn3.runtime import CLI
#
logger = logging.getLogger (__name__)
#
class Activator:
	__slots__ = ['db']
	db_path: Final[str] = os.path.join (base, 'var', 'run', 'activator.db')
	db_layout: Final[List[Layout]] = [Layout (
"""CREATE TABLE Service (
	name	text,
	active	integer,
	created	integer,
	changed	integer
)""", None)
	]
	def __init__ (self) -> None:
		self.db = DBLite (Activator.db_path, Activator.db_layout)
	
	def __enter__ (self) -> Activator:
		if not self.db.isopen ():
			raise error (f'{Activator.db_path}: failed to open database')
		return self

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		self.close ()
		return None

	def close (self) -> None:
		self.db.close ()

	def show (self, services: Optional[List[str]]) -> bool:
		def show_active (a: int) -> str:
			return 'active  ' if a > 0 else 'inactive'
		def show_date (d: int) -> str:
			ts = time.localtime (d)
			return '%2d.%02d.%04d %02d:%02d:%02d' % (ts.tm_mday, ts.tm_mon, ts.tm_year, ts.tm_hour, ts.tm_min, ts.tm_sec)
					
		first = True
		seen = set ()
		for (name, active, created, changed) in self.db.query ('SELECT name, active, created, changed FROM Service ORDER BY name'):
			if not services or name in services:
				if first:
					print ('Service              Status     created               changed')
					first = False
				print ('%-20s %s   %s   %s' % (name, show_active (active), show_date (created), show_date (changed)))
				seen.add (name)
		if not first:
			print ('')
		if services:
			for service in [_s for _s in services if _s not in seen]:
				print ('** service "%s" is not configured, it is active, by default' % service)
		return True
	
	def check (self, services: Optional[List[str]]) -> bool:
		if services is not None:
			for service in services:
				rq = self.db.querys ('SELECT active FROM Service WHERE name = :name', {'name': service})
				if rq is None:
					logger.info ('Create missing entry for %s' % service)
					self.__create (service, 1)
				elif rq.active is None or rq.active == 0:
					logger.debug ('%s: is inactive: %r' % (service, rq.active))
					return False
				else:
					logger.debug ('%s: is active: %r' % (service, rq.active))
		return True
		
	def activate (self, services: Optional[List[str]]) -> bool:
		return self.__modify (services, 1)
	
	def deactivate (self, services: Optional[List[str]]) -> bool:
		return self.__modify (services, 0)
	
	def remove (self, services: Optional[List[str]]) -> bool:
		if services is not None:
			rc = True
			for service in services:
				if not self.__remove (service):
					rc = False
			self.show (services)
			return rc
		return False
		
	def __create (self, service: str, status: int) -> bool:
		now = int (time.time ())
		cnt = self.db.update ('INSERT INTO Service (name, active, created, changed) VALUES (:name, :active, :now, :now)', {'name': service, 'active': status, 'now': now})
		return cnt == 1
	
	def __modify (self, services: Optional[List[str]], status: int) -> bool:
		if services is not None:
			rc = True
			for service in services:
				if not self.__change (service, status):
					rc = False
			self.show (services)
			(logger.info if rc else logger.error) ('Services %s %s%sactivated' % (', '.join (services), '' if rc else 'NOT ', 'de' if status == 0 else ''))
			return rc
		return False

	def __change (self, service: str, status: int) -> bool:
		now = int (time.time ())
		cnt = self.db.update ('UPDATE Service SET active = :active, changed = :now WHERE name = :name AND active != :active', {'name': service, 'active': status, 'now': now})
		if cnt == 0:
			rq = self.db.querys ('SELECT count(*) FROM Service WHERE name = :name AND active = :active', {'name': service, 'active': status})
			if rq is None or rq[0] == 0:
				return self.__create (service, status)
			return True
		return cnt == 1
	
	def __remove (self, service: str) -> bool:
		cnt = self.db.update ('DELETE FROM Service WHERE name = :name', {'name': service})
		return cnt == 1

class Main (CLI):
	__slots__ = ['activator', 'action', 'services']
	def setup (self) -> None:
		self.activator = Activator ()

	def cleanup (self, success: bool) -> None:
		self.activator.close ()

	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		parser.add_argument (
			'-s', '--show',
			action = 'store_true',
			help = 'show current status for selected or all services'
		)
		parser.add_argument (
			'-a', '-e', '--activate', '--enable',
			action = 'store_true',
			help = 'activate/enable services'
		)
		parser.add_argument (
			'-d', '--deactivate', '--disable',
			action = 'store_true',
			help = 'deactivate/disable services'
		)
		parser.add_argument (
			'-r', '--remove',
			action = 'store_true',
			help = 'remove services'
		)
		parser.add_argument (
			'services', nargs = '*',
			help = 'service(s) to manage'
		)

	def use_arguments (self, args: argparse.Namespace) -> None:
		self.action = self.activator.check
		if args.show:
			self.action = self.activator.show
		elif args.activate:
			self.action = self.activator.activate
		elif args.deactivate:
			self.action = self.activator.deactivate
		elif args.remove:
			self.action = self.activator.remove
		self.services = args.services

	def executor (self) -> bool:
		with self.activator:
			return self.action (self.services)
#
if __name__ == '__main__':
	Main.main ()
