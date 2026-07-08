#!/usr/bin/env python3
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
import	argparse, logging
from	agn3.emm.scripttag import ScriptTag
from	agn3.runtime import CLI
from	agn3.stream import Stream
from	agn3.tools import listsplit
#
logger = logging.getLogger (__name__)
#
class Main (CLI):
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
		self.tags = Stream (args.tags).map (listsplit).chain (str).list ()
		self.only_tags = args.only_tags
		self.filename = args.filename[0]
		
	def executor (self) -> bool:
		return ScriptTag (self.filename).install (
			tags = self.tags,
			dryrun = self.dryrun,
			company_id = self.company_id,
			only_tags = self.only_tags,
			description = self.description
		)
#
if __name__ == '__main__':
	Main.main ()
