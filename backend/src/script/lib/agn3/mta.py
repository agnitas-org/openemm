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
import	os, subprocess, logging
from	typing import Optional
from	typing import Dict, Iterator, List, Tuple
from	.definitions import base, syscfg
from	.exceptions import error
from	.ignore import Ignore
from	.io import which, create_path
from	.log import log
from	.stream import Stream
from	.tools import call, listsplit
#
__all__ = [
	'MTA'
]
#
logger = logging.getLogger (__name__)
#
class MTA:
	"""Handles different MTAs

This class is used to handle different MTAs on a central base. It also
supports calling xmlback to generate the final mail depending on the
used MTA."""
	__slots__ = ['xmlback', 'service', 'mta', 'conf']
	def __init__ (self, xmlback: Optional[str] = None, service: Optional[str] = None) -> None:
		"""``xmlback'' is an alternate path to the executable to call"""
		self.xmlback = xmlback if xmlback is not None else os.path.join (base, 'bin', 'xmlback')
		self.service = service
		self.mta = 'postfix'
		self.conf: Dict[str, str] = {}
		if self.mta == 'postfix':
			cmd = self.postfix_command ('postconf')
			if cmd:
				pp = subprocess.Popen ([cmd], stdout = subprocess.PIPE, stderr = subprocess.PIPE, stdin = subprocess.PIPE, text = True, errors = 'backslashreplace')
				(out, err) = pp.communicate ()
				if pp.returncode != 0:
					logger.warning (f'Command {cmd} returnd {pp.returncode}')
				if out:
					for line in (_l.strip () for _l in out.split ('\n')):
						if line:
							try:
								(var, val) = [_v.strip () for _v in line.split ('=', 1)]
								self.conf[var] = val
							except ValueError:
								logger.exception (f'Unparsable line: "{line}"')
			else:
				logger.warning ('No command to determinate configuration found')

	def postfix_command (self, cmd: str) -> Optional[str]:
		"""return path to ``cmd'' for a typical postifx installation"""
		return which (cmd, '/usr/sbin', '/sbin', '/etc')
	
	def postfix_make (self, hash: Optional[str], filename: str) -> None:
		"""creates a postfix hash file for ``filename''"""
		cmd = self.postfix_command ('postmap')
		filespec = f'{hash}:{filename}' if hash else filename
		if cmd is not None:
			n = call ([cmd, filespec])
			if n == 0:
				logger.info (f'{filespec} written using {cmd}')
			else:
				logger.error (f'{filespec} not written using {cmd}: {n}')
		else:
			logger.error (f'{filespec} not written due to missing postmap command')
	
	class File:
		__slots__ = ['mta', 'name', 'path', 'map', 'map_path', 'content', 'normalized']
		extension_mapping = {
			'btree':	'db',
			'cdb':		'cdb',
			'dbm':		'dir',		# also "pag" is created, but we just use one file to check for changes atm
			'hash':		'db',
			'sdbm':		'dir',		# like "dbm" the "pag" file is not handled here
		}
		def __init__ (self, mta: MTA, name: str, path: str, map: Optional[str] = None) -> None:
			self.name = name
			self.mta = mta
			self.path = path
			self.map = map
			self.map_path: Optional[str] = None
			if not os.path.isfile (self.path):
				create_path (os.path.dirname (self.path))
				with open (self.path, 'w'):
					pass
			if self.map is not None:
				self.map_path = '{path}.{extension}'.format (
					path = self.path,
					extension = self.extension_mapping.get (self.map, self.map)
				)
				try:
					map_stat = os.stat (self.map_path)
					file_stat = os.stat (self.path)
					if map_stat.st_mtime <= file_stat.st_mtime:
						raise error (f'{self.path}: outdated map file {self.map_path}')
				except:
					self.mta.postfix_make (self.map, self.path)
			with open (self.path) as fd:
				self.content = fd.read ()
			self.normalized = self._normalize (self.content)

		def __iter__ (self) -> Iterator[str]:
			for line in self.content.split ('\n'):
				if line:
					yield line

		def write (self, content: str) -> None:
			normalized = self._normalize (content)
			if self.normalized != normalized:
				if content and not content.endswith ('\n'):
					content += '\n'
				with open (self.path, 'w') as fd:
					fd.write (content)
				self.content = content
				self.normalized = normalized
				if self.map is not None:
					self.mta.postfix_make (self.map, self.path)
		
		def _normalize (self, content: str) -> str:
			return Stream (content.split ('\n')).filter (lambda l: bool (l) and not l.startswith ('#')).sorted ().join ('\0')
					
	def postfix_local_file (self, key: str) -> Optional[MTA.File]:
		with Ignore (KeyError):
			fallback: Optional[Tuple[str, Optional[str]]] = None
			for element in self.getlist (key):
				map: Optional[str]
				path: str
				try:
					(map, path) = element.split (':', 1)
				except ValueError:
					(map, path) = (None, element)
				if path.startswith (base + os.path.sep):
					if self.service is None and path.endswith (f'-{self.service}'):
						return MTA.File (self, key, path, map)
					if fallback is None:
						fallback = (path, map)
			if fallback is not None:
				return MTA.File (self, key, fallback[0], fallback[1])
		return None

	def __getitem__ (self, key: str) -> str:
		return self.conf[key]

	def getlist (self, key: str) -> List[str]:
		"""returns the value for ``key'' as list"""
		return list (listsplit (self[key]))

	def make (self, path: str, **kwargs: str) -> List[str]:
		generate = [
			f'account-logfile={base}/log/account.log',
			f'bounce-logfile={base}/log/extbounce.log',
			f'mailtrack-logfile={base}/log/mailtrack.log',
			f'messageid-logfile={base}/var/run/messageid.log',
			'media=email',
			'inject={sendmail} -f %(sender) -- %(recipient)'.format (
				sendmail = ' '.join (syscfg.sendmail ())
			)
		]
		return [
			self.xmlback,
			'-l',
			'-o', 'generate:{generate}'.format (generate = ';'.join (generate)),
			'-L', log.get_loglevel (default = 'info'),
			path
		]
		
	def __call__ (self, path: str, **kwargs: str) -> bool:
		"""``path'' is the file to process

kwargs may contain other parameter required or optional used by specific
instances of mail creation"""
		cmd = self.make (path, **kwargs)
		logger.debug (f'{cmd} starting')
		pp = subprocess.Popen (cmd, stdin = subprocess.PIPE, stdout = subprocess.PIPE, stderr = subprocess.PIPE, text = True, errors = 'backslashreplace')
		(out, err) = pp.communicate (None)
		n = pp.returncode
		logger.debug (f'{cmd} returns {n}')
		if n != 0:
			logger.error (f'Failed to unpack {path} ({n})')
			for (name, content) in [('Output', out), ('Error', err)]:
				if content:
					logger.error (f'{name}:\n{content}')
			return False
		logger.info (f'Unpacked {path}')
		return True
