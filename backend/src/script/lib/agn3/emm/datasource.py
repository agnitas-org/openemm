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
import	logging
from	typing import Optional, Union
from	typing import Dict, Tuple
from	..db import DB
from	..exceptions import error
#
__all__ = ['Datasource']
#
logger = logging.getLogger (__name__)
#
class Datasource:
	"""Get or create a datasource id by Name

This class fetches an existing or creates a new datasource_id by
name."""
	__slots__ = ['cache']
	def __init__ (self) -> None:
		self.cache: Dict[Tuple[str, int], Optional[int]] = {}

	def get_id (self, name: str, company_id: int, source_group: Union[int, str], db: Optional[DB] = None) -> Optional[int]:
		"""get an existing ID or creates a new one

Retrieves the datasource-id for a given ``name'' for the company
``company_id''. If it does not exists, a new one is created using the
``source_group'' (either numeric or textual representation). The
optional ``db'' parameter is an open database driver, if this is None,
a default database driver is created for database access."""
		key = (name, company_id)
		try:
			rc = self.cache[key]
		except KeyError:
			rc = None
			usedb = db if db is not None else DB ()
			if usedb.isopen ():
				for state in [0, 1]:
					for row in usedb.query (
						'SELECT datasource_id '
						'FROM datasource_description_tbl '
						'WHERE company_id = :company_id AND description = :description',
						{
							'company_id': company_id,
							'description': name
						}
					):
						rc = int (row.datasource_id)
					if rc is None and state == 0:
						for sourcegroup_field in 'sourcegroup_type', 'description':
							if type (source_group) is int:
								break
							rq = usedb.querys (
								'SELECT sourcegroup_id '
								'FROM sourcegroup_tbl '
								f'WHERE {sourcegroup_field} = :source',
								{'source': source_group}
							)
							if rq is not None and rq.sourcegroup_id is not None:
								source_group = int (rq.sourcegroup_id)
						if type (source_group) is not int:
							raise error (f'Invalid source_group: {source_group}')
						#
						rq = usedb.querys (
							'SELECT sourcegroup_type, description '
							'FROM sourcegroup_tbl '
							'WHERE sourcegroup_id = :source',
							{'source': source_group}
						)
						if rq is None:
							raise error (f'Unknown source_group: {source_group}')
						query = usedb.qselect (
							oracle = (
								'INSERT INTO datasource_description_tbl ('
								'            datasource_id, description, company_id, sourcegroup_id, timestamp'
								') VALUES ('
								'            datasource_description_tbl_seq.nextval, :description, :company_id, :source_group, sysdate'
								')'
							), mysql = (
								'INSERT INTO datasource_description_tbl ('
								'            description, company_id, sourcegroup_id, timestamp'
								') VALUES ('
								'            :description, :company_id, :source_group, CURRENT_TIMESTAMP'
								')'
							)
						)
						usedb.update (
							query,
							{
								'description': name,
								'company_id': company_id,
								'source_group': source_group
							},
							commit = True
						)
						logger.info (f'Created new datasource id companyID {company_id} with {name} for {rq.description} ({rq.sourcegroup_type})')
			else:
				logger.error ('Failed to open database: {error}'.format (error = usedb.last_error ()))
			if db is None:
				usedb.close ()
			self.cache[key] = rc
			if rc is not None:
				logger.info (f'Found datasource {rc} for companyID {company_id} with {name}')
			else:
				logger.info (f'Did not found datasource for companyID {company_id} with {name}')
		return rc
