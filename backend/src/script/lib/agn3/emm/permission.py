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
from	typing import Optional
from	typing import Dict, Set
from	..db import DB
#
class Permissions:
	"""Manage permission based on permission tables

This class reads the permission for a company and provides an
information, if a permission for this company is set (i.e. allowed) or
not."""
	__slots__ = ['permissions']
	def __init__ (self, company_id: int, db: Optional[DB] = None) -> None:
		self.permissions: Dict[str, Set[str]] = {
			'group':	set (),
			'admin':	set (),
			'company':	set ()
		}
		mydb = db if db is not None else DB ()
		if mydb.isopen ():
			with mydb.request () as cursor:
				data = {'company_id': company_id}
				for (key, target) in self.permissions.items ():
					if key == 'group':
						query = (
							'SELECT permission_name FROM admin_group_permission_tbl '
							'WHERE admin_group_id IN (SELECT admin_group_id FROM admin_group_tbl WHERE company_id IN (0, :company_id) AND deleted = 0)'
						)
					elif key == 'admin':
						query = (
							'SELECT permission_name FROM admin_permission_tbl '
							'WHERE admin_id IN (SELECT admin_id FROM admin_tbl WHERE company_id IN (0, :company_id))'
						)
					elif key == 'company':
						query = (
							'SELECT permission_name FROM company_permission_tbl '
							'WHERE company_id IN (0, :company_id)'
						)
					for row in cursor.query (query, data):
						if row.permission_name is not None:
							target.add (row.permission_name)
		if db is None:
			mydb.close ()
	
	def __contains__ (self, key: str) -> bool:
		return sum (1 for _ in filter (lambda a: key in a, self.permissions.values ())) > 0
