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
import	os, subprocess, logging
from	typing import Optional
from	typing import Dict, List
from	.definitions import base
from	.io import which
from	.tools import call, listsplit
#
__all__ = ['MTA']
#
logger = logging.getLogger (__name__)
#
class MTA:
	"""Handles different MTAs

This class is used to handle different MTAs on a central base. It also
supports calling xmlback to generate the final mail depending on the
used MTA."""
	__slots__ = ['xmlback', 'mta', 'dsnopt', 'conf']
	def __init__ (self, xmlback: Optional[str] = None) -> None:
		"""``xmlback'' is an alternate path to the executable to call"""
		self.xmlback = xmlback if xmlback is not None else os.path.join (base, 'bin', 'xmlback')
		self.mta = os.environ.get ('MTA', 'sendmail')
		self.dsnopt = os.environ.get ('SENDMAIL_DSN_OPT', '-NNEVER')
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
	
	def postfix_make (self, filename: str) -> None:
		"""creates a postfix hash file for ``filename''"""
		cmd = self.postfix_command ('postmap')
		if cmd is not None:
			n = call ([cmd, filename])
			if n == 0:
				logger.info (f'{filename} written using {cmd}')
			else:
				logger.error (f'{filename} not written using {cmd}: {n}')
		else:
			logger.error (f'{filename} not written due to missing postmap command')

	def __getitem__ (self, key: str) -> str:
		return self.conf[key]

	def getlist (self, key: str) -> List[str]:
		"""returns the value for ``key'' as list"""
		return list (listsplit (self[key]))
	
	def __call__ (self, path: str, **kwargs: str) -> bool:
		"""``path'' is the file to process

kwargs may contain other parameter required or optional used by specific
instances of mail creation"""
		generate = [
			f'account-logfile={base}/log/account.log',
			f'bounce-logfile={base}/log/extbounce.log',
			f'mailtrack-logfile={base}/log/mailtrack.log'
		]
		if self.mta == 'postfix':
			generate += [
				f'messageid-logfile={base}/var/run/messageid.log'
			]
		generate += [
			'media=email'
		]
		if self.mta == 'postfix':
			generate += [
				f'inject=/usr/sbin/sendmail {self.dsnopt} -f %(sender) -- %(recipient)'
			]
		else:
			generate += [
				'path={target}'.format (target = kwargs['target_directory'])
			]
			#
			fqu = os.path.join (base, 'bin', 'fqu.sh')
			if os.access (fqu, os.X_OK):
				generate += [
					'queue-flush={count}'.format (count = kwargs.get ('flush_count', '2')),
					f'queue-flush-command={base}/bin/fqu.sh'
				]
		cmd = [
			self.xmlback,
			'-l',
			'-o', 'generate:{generate}'.format (generate = ';'.join (generate)),
			'-L', 'info',
			path
		]
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
