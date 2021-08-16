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
import	os, re
import	argparse, logging
from	typing import Optional
from	typing import List
from	agn3.db import DB
from	agn3.runtime import CLI
from	agn3.stream import Stream
from	agn3.tools import listsplit
#
logger = logging.getLogger (__name__)
#
class ScriptTag (CLI):
	__slots__ = [
		'dryrun', 'quiet',
		'language', 'company_id', 'description',
		'tags', 'only_tags', 'filename'
	]
	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		parser.add_argument (
			'-n', '--dryrun', action = 'store_true',
			help = 'do not execute any database modifiaction'
		)
		parser.add_argument (
			'-q', '--quiet', action = 'store_true',
			help = 'quiet mode, do not output any information'
		)
		parser.add_argument (
			'-l', '--language', action = 'store',
			help = 'set this as the language of the script (default: determinate from filename extension)'
		)
		parser.add_argument (
			'-c', '--company-id', action = 'store', type = int, default = 0, dest = 'company_id',
			help = 'create the tag for this company-id (default: 0)'
		)
		parser.add_argument (
			'-d', '--description', action = 'store',
			help = 'explaination for the code'
		)
		parser.add_argument (
			'-t', '--tags', action = 'append', default = [],
			help = (
				'list of agnTags to create or update in tag_tbl it is possible to use a '
				'different function name by separating it with a colon, e.g.: '
				',,angTAG`` will call the function ,,tag``, but ,,agnTAG:func`` will '
				'call the function ,,func`` an optional description may be appended '
				'using another colon as separator'
			)
		)
		parser.add_argument (
			'-T', '--only-tags', action = 'store_true', dest = 'only_tags',
			help = 'only create/update tag(s), do not process the function code'
		)
		parser.add_argument (
			'filename', nargs = 1,
			help = 'the filename containing the code for this scripted tag'
		)
	
	def use_arguments (self, args: argparse.Namespace) -> None:
		self.dryrun = args.dryrun
		self.quiet = args.quiet
		self.language = args.language
		self.company_id = args.company_id
		self.description = args.description
		self.tags = Stream (args.tags).map (lambda t: listsplit (t)).chain ().list ()
		self.only_tags = args.only_tags
		self.filename = args.filename[0]
		
	def executor (self) -> bool:
		rc = True
		(name, extension) = os.path.splitext (os.path.basename (self.filename))
		if not self.language:
			if extension.startswith ('.'):
				extension = extension[1:]
			self.language = extension.lower ()
		with DB () as db, db.request () as cursor:
			if not self.only_tags:
				with open (self.filename) as fd:
					code = self.cleanup_code (fd.read ())
				rq = cursor.querys (
					'SELECT tag_function_id, lang, description, code '
					'FROM tag_function_tbl '
					'WHERE name = :name AND company_id = :company_id',
					{'name': name, 'company_id': self.company_id}
				)
				if rq is None:
					data = {
						'name': name,
						'company_id': self.company_id,
						'lang': self.language,
						'code': code,
						'tdesc': self.description
					}
					query = cursor.qselect (
						oracle = (
							'INSERT INTO tag_function_tbl '
							'       (tag_function_id, company_id, creation_date, timestamp, name, lang, description, code) '
							'VALUES '
							'       (tag_function_tbl_seq.nextval, :company_id, current_timestamp, current_timestamp, :name, :lang, :tdesc, :code)'
						), mysql = (
							'INSERT INTO tag_function_tbl '
							'       (company_id, creation_date, timestamp, name, lang, description, code) '
							'VALUES '
							'       (:company_id, current_timestamp, current_timestamp, :name, :lang, :tdesc, :code)'
						)
					)
					if self.dryrun:
						print (f'{name}: Would execute {query} using {data}')
					else:
						if db.dbms == 'oracle' and db.db is not None:
							cursor.set_input_sizes (code = db.db.driver.CLOB)
						rows = cursor.update (query, data)
						if rows == 1:
							if not self.quiet:
								print (f'{name}: code inserted setting language to "{self.language}" for company {self.company_id}')
						else:
							print ('{name}: FAILED to insert code into database: {error}'.format (
								name = name,
								error = db.last_error ()
							))
							rc = False
				elif rq.lang == self.language and str (rq.code) == code and (not self.description or self.description == rq.description):
					if not self.quiet:
						print (f'{name}: no change')
				else:
					data = {
						'fid': rq.tag_function_id, 
						'lang': self.language,
						'code': code
					}
					if self.description:
						data['tdesc'] = self.description
						extra = ', description = :tdesc'
					else:
						extra = ''
					query = (
						'UPDATE tag_function_tbl '
						f'SET code = :code, lang = :lang, timestamp = current_timestamp{extra} '
						'WHERE tag_function_id = :fid'
					)
					if self.dryrun:
						print (f'{name}: Would execute {query} using {data}')
					else:
						if db.dbms == 'oracle' and db.db is not None:
							cursor.set_input_sizes (code = db.db.driver.CLOB)
						rows = cursor.update (query, data)
						if rows == 1:
							if not self.quiet:
								print (f'{name}: code updated using language "{self.language}"')
						else:
							print ('{name}: FAILED to update code: {error}'.format (
								name = name,
								error = db.last_error ()
							))
							rc = False
			#
			for tag in self.tags:
				parts = tag.split (':', 2)
				cdesc = name
				tdesc: Optional[str] = None
				if len (parts) > 1:
					tag = parts[0]
					if parts[1]:
						cdesc = '%s:%s' % (name, parts[1])
					if len (parts) == 3 and parts[2]:
						tdesc = parts[2]
				data = {'cdesc': cdesc}
				rq = cursor.querys (
					'SELECT tag_id, type, selectvalue, description '
					'FROM tag_tbl '
					'WHERE tagname = :tname AND company_id = :company_id',
					{'tname': tag, 'company_id': self.company_id}
				)
				if rq is None:
					data['type'] = 'FUNCTION'
					data['tname'] = tag
					data['company_id'] = self.company_id
					if tdesc is None:
						tdesc = 'Created by script-tag'
					data['tdesc'] = tdesc
					query = cursor.qselect (
						oracle = (
							'INSERT INTO tag_tbl '
							'       (tag_id, tagname, selectvalue, type, company_id, description, timestamp) '
							'VALUES '
							'       (tag_tbl_seq.nextval, :tname, :cdesc, :type, :company_id, :tdesc, current_timestamp)'
						), mysql = (
							'INSERT INTO tag_tbl '
							'       (tagname, selectvalue, type, company_id, description, change_date) '
							'VALUES '
							'       (:tname, :cdesc, :type, :company_id, :tdesc, current_timestamp)'
						)
					)
					if self.dryrun:
						print (f'Tag {tag}: Would execute {query} using {data}')
					else:
						rows = cursor.update (query, data)
						if rows == 1:
							if not self.quiet:
								print (f'Tag {tag}: inserted into database')
						else:
							print ('Tag {tag}: FAILED to insert into database: {error}'.format (
								tag = tag,
								error = db.last_error ()
							))
							rc = False
				elif rq.selectvalue == cdesc and (not tdesc or rq.description == tdesc):
					if not self.quiet:
						print (f'Tag {tag}: no change')
				else:
					data['tid'] = rq.tag_id
					if tdesc:
						data['tdesc'] = tdesc
						extra = ', description = :tdesc'
					else:
						extra = ''
					if rq.type != 'FUNCTION':
						if not self.quiet:
							print (f'Tag {tag}: modify type from {rq.type}')
						data['type'] = 'FUNCTION'
						extra += ', type = :type'
					query = cursor.qselect (
						oracle = (
							'UPDATE tag_tbl '
							f'SET selectvalue = :cdesc, timestamp = current_timestamp{extra} '
							'WHERE tag_id = :tid'
						), mysql = (
							'UPDATE tag_tbl '
							f'SET selectvalue = :cdesc, change_date = current_timestamp{extra} '
							'WHERE tag_id = :tid'
						)
					)
					if self.dryrun:
						print (f'Tag {tag}: Would execute (query) using {data}')
					else:
						rows = cursor.update (query, data)
						if rows == 1:
							if not self.quiet:
								print (f'Tag {tag}: updated')
						else:
							print ('Tag {tag}: FAILED to update: {error}'.format (
								tag = tag,
								error = db.last_error ()
							))
							rc = False
			cursor.sync (not self.dryrun and rc)
		return rc
	
	def cleanup_code (self, code: str) -> str:
		if code.startswith ('#!'):
			code = code.split ('\n', 1)[-1]
		pattern_condition = re.compile ('#<.*>#$')
		pattern_unittest = re.compile ('%%$')
		output: List[str] = []
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
#
if __name__ == '__main__':
	ScriptTag.main ()
