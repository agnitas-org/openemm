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
import	os, argparse, logging, re, shutil
from	typing import Final
from	typing import NamedTuple
from	agn3.definitions import base, licence
from	agn3.emm.scripttag import ScriptTag
from	agn3.log import log
from	agn3.runtime import CLI
from	agn3.tools import call, listsplit
#
logger = logging.getLogger (__name__)
#
class TagInstall (CLI):
	__slots__ = ['dryrun', 'directory', 'keep', 'parameter']
	default_directory: Final[str] = os.path.join (base, 'scripts', 'once', 'tags')
	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		parser.add_argument (
			'-n', '--dryrun', action = 'store_true',
			help = 'dry run mode'
		)
		parser.add_argument (
			'-d', '--directory', action = 'store', default = self.default_directory,
			help = f'directory to scan for tagfiles (default "{self.default_directory}"'
		)
		parser.add_argument (
			'-k', '--keep', action = 'store_true',
			help = 'keep (do not remove) success processed files'
		)
		parser.add_argument (
			'parameter', nargs = '*',
			help = 'limit files to listed filenames'
		)
		
	def use_arguments (self, args: argparse.Namespace) -> None:
		self.dryrun: bool = args.dryrun
		self.directory: str = args.directory
		self.keep: bool = args.keep
		self.parameter: list[str] = args.parameter
	
	def executor (self) -> bool:
		if not os.path.isdir (self.directory):
			logger.warning (f'{self.directory}: not a directory')
			return True
		#
		remove_directory = True
		for filename in os.listdir (self.directory):
			if self.parameter and filename not in self.parameter:
				remove_directory = False
				continue
			#
			with log (filename):
				path = os.path.join (self.directory, filename)
				if self._install (filename, path):
					logger.info ('successful installed')
					if not self.keep:
						if self.dryrun:
							print (f'{path}: would be removed after successful install')
						else:
							try:
								os.unlink (path)
								logger.info ('file successful removed')
							except OSError as e:
								logger.error (f'failed to remove successful installed file: {e}')
								remove_directory = False
				else:
					logger.error (f'{filename}: failed to install')
					remove_directory = False
		if remove_directory:
			if self.dryrun:
				print (f'{self.directory}: would be removed as no failures had been encountered')
			else:
				try:
					os.rmdir (self.directory)
				except OSError as e:
					logger.error (f'{self.directory}: failed to remove: {e}')
		return True
	
	_install_pattern = re.compile ('^--\\s*(install|owner|description):(.*);$')
	def _install (self, filename: str, path: str) -> bool:
		class Owner (NamedTuple):
			licence_id: int
			company_id: int
			@classmethod
			def parse (cls, expression: str) -> list[Owner]:
				rc: list[Owner] = []
				for element in listsplit (expression):
					(licence_expression, company_list) = element.split (':', 1)
					licence_id = int (licence_expression)
					for company_id in company_list.split (';'):
						rc.append (Owner (
							licence_id = licence_id,
							company_id = int (company_id)
						))
				return rc
		#
		(basename, extension) = os.path.splitext (filename)
		tagnames: list[str] = []
		owners: list[Owner] = []
		description = 'created by tag-install'
		with open (path) as fd:
			script = fd.read ()
		for line in script.split ('\n'):
			if (mtch := self._install_pattern.match (line)) is not None:
				match mtch.groups ():
					case ('install', names):
						for name in (f'agn{_n.upper ()}:{_n}' for _n in listsplit (names)):
							if name not in tagnames:
								tagnames.append (name)
					case ('owner', owner_list):
						owners += Owner.parse (owner_list)
					case ('description', description):
						pass
		if not tagnames:
			tagnames.append ('agn' + basename.upper ())
		if not owners:
			owners.append (Owner (licence_id = licence, company_id = 0))
		lang = extension.lstrip ('.')
		if self.dryrun:
			self._validate (lang, path)
		rc = True
		script_tag = ScriptTag (path)
		for owner in owners:
			if owner.licence_id != licence:
				continue
			#
			if not script_tag.install (
				tags = tagnames,
				dryrun = self.dryrun,
				company_id = owner.company_id,
				description = description
			):
				logger.error (f'failed to install for owner {owner}')
				rc = False
			else:
				logger.info (f'successful installed for owner {owner}')
		return rc
	
	_luatc = shutil.which ('luatc')
	def _validate (self, lang: str, path: str) -> None:
		match lang:
			case 'lua':
				if self._luatc and os.access (self._luatc, os.X_OK):
					print (f'Validating {path} using {self._luatc}:')
					n = call (['luatc', path])
					print ('Validation {result}'.format (
						result = 'succeeded' if n == 0 else f'failed with exist code {n}'
					))

if __name__ == '__main__':
	TagInstall.main ()
