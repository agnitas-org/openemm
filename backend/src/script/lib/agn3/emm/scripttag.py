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
import	os, logging, re
from	..db import DB
from	..stream import Stream
#
__all__ = ['ScriptTag']
#
logger = logging.getLogger (__name__)
#
class ScriptTag:
	__slots__ = ['path', 'name', 'extension', 'language']
	def __init__(self, path: str) -> None:
		self.path = path
		(self.name, self.extension) = os.path.splitext (os.path.basename (path))
		self.language = self.extension.lstrip ('.').lower ()
	
	def install (self,
		tags: list[str],
		*,
		dryrun: bool = False,
		company_id: int = 0,
		only_tags: bool = False,
		description: None | str = None
	) -> bool:
		rc = True
		with DB () as db, db.request () as cursor:
			if not only_tags:
				with open (self.path) as fd:
					code = self._cleanup_code (fd.read ())
				rq = cursor.querys (
					'SELECT tag_function_id, lang, description, code '
					'FROM tag_function_tbl '
					'WHERE name = :name AND company_id = :company_id',
					{
						'name': self.name,
						'company_id': company_id
					}
				)
				if rq is None:
					data = {
						'name': self.name,
						'company_id': company_id,
						'lang': self.language,
						'code': code,
						'description': description
					}
					query = cursor.qselect (
						oracle = (
							'INSERT INTO tag_function_tbl '
							'       (tag_function_id, company_id, creation_date, timestamp, name, lang, description, code) '
							'VALUES '
							'       (tag_function_tbl_seq.nextval, :company_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :name, :lang, :description, :code)'
						), mariadb = (
							'INSERT INTO tag_function_tbl '
							'       (company_id, creation_date, timestamp, name, lang, description, code) '
							'VALUES '
							'       (:company_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :name, :lang, :description, :code)'
						)
					)
					if dryrun:
						logger.info (f'{self.name}: Would execute {query} using {data}')
					else:
						rows = cursor.update (query, data, input_sizes = {'code': 'CLOB'})
						if rows == 1:
							logger.info (f'{self.name}: code inserted setting language to "{self.language}" for company {company_id}')
						else:
							logger.error ('{name}: FAILED to insert code into database: {error}'.format (
								name = self.name,
								error = db.last_error ()
							))
							rc = False
				else:
					columns: list[str] = []
					data = {
						'fid': rq.tag_function_id
					}
					if rq.lang != self.language:
						columns.append ('lang')
						data['lang'] = self.language
					if str (rq.code) != code:
						columns.append ('code')
						data['code'] = code
					if description and not rq.description:
						columns.append ('description')
						data['description'] = description
					if not columns:
						logger.info (f'{self.name}: no change')
					else:
						query = (
							'UPDATE tag_function_tbl '
							'SET {columns} '
							'WHERE tag_function_id = :fid'.format (
								columns = Stream (columns).map (lambda c: f'{c} = :{c}').join (', ')
							)
						)
						if dryrun:
							logger.info (f'{self.name}: Would execute {query} using {data}')
						else:
							rows = cursor.update (query, data, input_sizes = {'code': 'CLOB'})
							if rows == 1:
								logger.info ('{name}: updated using {content}'.format (
									name = self.name,
									content = Stream (data.items ()).map (lambda kv: f'{kv[0]}={kv[1]!r}').join (', ')
								))
							else:
								logger.error ('{name}: FAILED to update code: {error}'.format (
									name = self.name,
									error = db.last_error ()
								))
								rc = False
			#
			for tag in tags:
				parts = tag.split (':', 2)
				selectvalue = self.name
				if len (parts) > 1:
					tag = parts[0]
					if parts[1]:
						selectvalue = '%s:%s' % (self.name, parts[1])
				data = {'selectvalue': selectvalue}
				rq = cursor.querys (
					'SELECT tag_id, type, selectvalue, description '
					'FROM tag_tbl '
					'WHERE tagname = :tname AND company_id = :company_id',
					{
						'tname': tag,
						'company_id': company_id
					}
				)
				if rq is None:
					data['type'] = 'FUNCTION'
					data['tname'] = tag
					data['company_id'] = company_id
					data['description'] = description if description else 'created by script-tag'
					query = cursor.qselect (
						oracle = (
							'INSERT INTO tag_tbl '
							'      (tag_id, tagname, selectvalue, type, company_id, description, change_date) '
							'VALUES '
							'       (tag_tbl_seq.nextval, :tname, :selectvalue, :type, :company_id, :description, CURRENT_TIMESTAMP)'
						), mariadb = (
							'INSERT INTO tag_tbl '
							'      (tagname, selectvalue, type, company_id, description, change_date) '
							'VALUES '
							'       (:tname, :selectvalue, :type, :company_id, :description, CURRENT_TIMESTAMP)'
						)
					)
					if dryrun:
						logger.info (f'Tag {tag}: Would execute {query} using {data}')
					else:
						rows = cursor.update (query, data)
						if rows == 1:
							logger.info (f'Tag {tag}: inserted into database')
						else:
							logger.error ('Tag {tag}: FAILED to insert into database: {error}'.format (
								tag = tag,
								error = db.last_error ()
							))
							rc = False
				else:
					columns = []
					data = {
						'tid': rq.tag_id
					}
					if rq.selectvalue != selectvalue:
						columns.append ('selectvalue')
						data['selectvalue'] = selectvalue
					if description and not rq.description:
						columns.append ('description')
						data['description'] = description
					if rq.type != 'FUNCTION':
						columns.append ('type')
						data['type'] = 'FUNCTION'
						logger.info (f'{tag}: modify type from {rq.type} to FUNCTION')
					if not columns:
						logger.info (f'Tag {tag}: no change')
					else:
						query = (
							'UPDATE tag_tbl '
							'SET {columns} '
							'WHERE tag_id = :tid'.format (
								columns = Stream (columns).map (lambda c: f'{c} = :{c}').join (', ')
							)
						)
						if dryrun:
							logger.info (f'Tag {tag}: Would execute {query} using {data}')
						else:
							rows = cursor.update (query, data)
							if rows == 1:
								logger.info (f'Tag {tag}: updated')
							else:
								logger.error ('Tag {tag}: FAILED to update: {error}'.format (
									tag = tag,
									error = db.last_error ()
								))
								rc = False
			cursor.sync (not dryrun and rc)
		return rc
	
	def _cleanup_code (self, code: str) -> str:
		if code.startswith ('#!'):
			code = code.split ('\n', 1)[-1]
		pattern_condition = re.compile ('#<.*>#$')
		pattern_unittest = re.compile ('%%$')
		output: list[str] = []
		state = 0
		for line in code.split ('\n'):
			match = pattern_condition.search (line)
			if match is not None:
				state = 0
			elif state == 0:
				match = pattern_unittest.search (line)
				if match is not None:
					state = 1
			if state == 0:
				output.append (line)
		return '\n'.join (output)

