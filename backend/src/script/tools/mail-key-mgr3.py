#!/usr/bin/env python3
####################################################################################################################################################################################################################################################################
#                                                                                                                                                                                                                                                                  #
#                                                                                                                                                                                                                                                                  #
#        Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                              #
#                                                                                                                                                                                                                                                                  #
#        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    #
#        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           #
#        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.                                                                                                       #
#                                                                                                                                                                                                                                                                  #
####################################################################################################################################################################################################################################################################
#
from	__future__ import annotations
import	argparse
import	sys, os, time, re
import	DNS
from	datetime import datetime
from	getpass import getpass
from	subprocess import Popen, PIPE
from	typing import Any, Optional
from	typing import Dict, List, NamedTuple, Tuple
from	agn3.db import DB
from	agn3.exceptions import error
from	agn3.ignore import Ignore
from	agn3.parameter import MailingParameter
from	agn3.parser import parse_timestamp
from	agn3.runtime import CLI
from	agn3.stream import Stream
from	agn3.tools import silent_call, Config
#
class RC (NamedTuple):
	output: str
	error: str
	ec: int
	
def pread (command: list[str], silent: bool = False, input: None | str = None) -> RC:
	pp = Popen (command, stdin = PIPE, stdout = PIPE, stderr = PIPE, text = True, errors = 'backslashreplace')
	(out, err) = pp.communicate (input)
	if not silent and err:
		print (err, file = sys.stderr, flush = True)
	return RC (output = out, error = err, ec = pp.returncode)
	
class Mgr:
	__slots__ = ['method', 'company_id', 'verbose', 'options', 'valid', 'parameter']
	class Valid (NamedTuple):
		start: Optional[datetime]
		end: Optional[datetime]
	def __init__ (self, method: str, company_id: int, verbose: bool, options: List[str], valid: Optional[str], parameter: List[str]) -> None:
		self.method = method
		self.company_id = company_id
		self.verbose = verbose
		self.options = self._parse_options (options)
		self.valid = self._parse_valid (valid)
		self.parameter = parameter
	
	def _parse_options (self, options: List[str]) -> Config:
		return Config (**Stream (options)
			.map (lambda o: tuple (_o.strip () for _o in o.split ('=', 1)))
			.filter (lambda kv: len (kv) == 2)
			.dict ()
		)
		
	def _parse_valid (self, vstr: Optional[str]) -> Mgr.Valid:
		start: Optional[datetime] = None
		end: Optional[datetime] = None
		if vstr:
			parts = vstr.split ('..', 1)
			if len (parts) != 2:
				parts = vstr.split ('-')
			if len (parts) == 2:
				start = parse_timestamp (parts[0].strip ())
				end = parse_timestamp (parts[1].strip ())
				if start and end and start > end:
					start, end = end, start
		return Mgr.Valid (start = start, end = end)

	def list (self) -> bool:
		with DB () as db:
			for row in db.stream (
				'SELECT mail_key_id, company_id, method, parameter, valid_start, valid_end '
				'FROM mail_key_tbl '
			).filter (lambda r: self.company_id == 0 or self.company_id == r.company_id).sorted (lambda r: r.mail_key_id):
				line = f'{row.mail_key_id:4d} (Company {row.company_id:d}) for "{row.method}" using "{row.parameter}"'
				if row.valid_start or row.valid_end:
					line += ' [{start} .. {end}]'.format (
						start = row.valid_start if row.valid_start is not None else 'oo',
						end = row.valid_end if row.valid_end is not None else 'oo'
					)
				print (line)
		return True
				
	def store (self, key: str, **kwargs: str) -> bool:
		rc = True
		parameter = MailingParameter ()
		for (option, value) in kwargs.items ():
			parameter[option] = value
		parstr = parameter.dumps ()
		with DB () as db:
			found = False
			for row in db.queryc (
				'SELECT mail_key_id, mail_key, valid_start, valid_end '
				'FROM mail_key_tbl '
				'WHERE company_id = :company_id AND method = :method AND parameter = :parameter',
				{
					'company_id': self.company_id,
					'method': self.method,
					'parameter': parstr
				}
			):
				found = True
				if row.mail_key != key or row.valid_start != self.valid.start or row.valid_end != self.valid.end:
					if db.update (
						'UPDATE mail_key_tbl '
						'SET mail_key = :mail_key, valid_start = :valid_start, valid_end = :valid_end, change_date = CURRENT_TIMESTAMP '
						'WHERE mail_key_id = :mail_key_id',
						{
							'mail_key': key,
							'valid_start': self.valid.start,
							'valid_end': self.valid.end,
							'mail_key_id': row.mail_key_id
						}
					) != 1:
						print (f'** failed to update record with mail_key_id {row.mail_key_id}')
						rc = False
					else:
						print (f'Updated record with mail_key_id {row.mail_key_id}')
				else:
					print (f'Record already existing and up-to-date, no updated for mail_key_id {row.mail_key_id}')
			if not found:
				if db.update (
					db.qselect (
						oracle = (
							'INSERT INTO mail_key_tbl '
							'       (mail_key_id, company_id, method, parameter, mail_key, creation_date, change_date, valid_start, valid_end) '
							'VALUES '
							'       (mail_key_tbl_seq.nextval, :company_id, :method, :parameter, :mail_key, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :valid_start, :valid_end)'
						), mariadb = (
							'INSERT INTO mail_key_tbl '
							'       (company_id, method, parameter, mail_key, creation_date, change_date, valid_start, valid_end) '
							'VALUES '
							'       (:company_id, :method, :parameter, :mail_key, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :valid_start, :valid_end)'
						), postgres = (
							'INSERT INTO mail_key_tbl '
							'       (mail_key_id, company_id, method, parameter, mail_key, creation_date, change_date, valid_start, valid_end) '
							'VALUES '
							'       (nextval(\'mail_key_tbl_mail_key_id_seq\'), :company_id, :method, :parameter, :mail_key, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :valid_start, :valid_end)'
						)
					), {
						'company_id': self.company_id,
						'method': self.method,
						'parameter': parstr,
						'mail_key': key,
						'valid_start': self.valid.start,
						'valid_end': self.valid.end
					}
				) != 1:
					print ('** failed to create new record')
					rc = False
				else:
					print ('Created new record')
			db.sync (rc)
		return rc

	def execute (self) -> bool:
		if not self.parameter:
			raise error (f'{self.method}: missing command')
		return self.command (self.parameter.pop (0).strip ().lower ())

	def command (self, command: str) -> bool:
		raise NotImplementedError ()

class DKIM (Mgr):
	__slots__: List[str] = []
	def command (self, command: str) -> bool:
		match command:
			case 'list':
				return self.list ()
			case 'new':
				return self._new_key ()
			case 'show':
				return self._show_key ()
			case 'validate':
				return self._validate_key ()
			case 'store':
				return self._store_key ()
		raise error (f'{self.method}: unknown command "{command}", these are known: new, show, validate, store')
	
	def _new_key (self) -> bool:
		if len (self.parameter) < 2 or len (self.parameter) > 3:
			raise error ('dkim.new: expect two filenames: the path to the private and public key to be written to, optional a third parameter, the length of the key')
		private_path = self.parameter[0]
		public_path = self.parameter[1]
		if len (self.parameter) > 2:
			length = int (self.parameter[2])
		else:
			length = 2048
		self._create (private_path, public_path, length)
		return True
	
	def _show_key (self) -> bool:
		for filename in self.parameter:
			if len (self.parameter) != 1:
				print (f'Display content of {filename}:')
			self._display (filename, True)
			if len (self.parameter) != 1:
				print ('')
		return True
	
	def _validate_key (self) -> bool:
		domain, selector, key = self._dkim_parameter ('validate')
		if not self._validate (domain, selector, key):
			raise error (f'dkim.validate: failed to validate key "{self.parameter[2]}" for domain {domain} using selector {selector}')
		return True

	def _store_key (self) -> bool:
		domain, selector, key = self._dkim_parameter ('store')
		return self.store (key, domain = domain, selector = selector)
		
	def _dkim_parameter (self, area: str) -> Tuple[str, str, str]:
		if len (self.parameter) != 3:
			raise error (f'dkim.{area}: expect three parameter: <domain> <selector> <private-key-file>')
		domain = self.parameter[0]
		selector = self.parameter[1]
		with open (self.parameter[2]) as fd:
			key = fd.read ()
		return domain, selector, key

	def _create (self, private_file: str, public_file: str, length: int) -> None:
		n = silent_call (['openssl', 'genrsa', '-out', private_file, str (length)])
		if n:
			raise error ('Failed to create private key %s, openssl returns %d' % (private_file, n))
		n = silent_call (['openssl', 'rsa', '-in', private_file, '-out', public_file, '-pubout', '-outform', 'PEM'])
		if n:
			raise error ('Failed to extract public key %s, openssl returns %d' % (public_file, n))
		if self.verbose:
			if not self._display (public_file, False):
				raise error ('Failed to parse newly created public key %s, aborted' % public_file)
	
	def _display (self, public_file: str, pure: bool) -> bool:
		with open (public_file, 'r') as fd:
			data = fd.read ()
		state = 0
		key = ''
		for line in [_l.strip () for _l in data.split ('\n')]:
			if state == 0 and line == '-----BEGIN PUBLIC KEY-----':
				state = 1
			elif state == 1:
				if line == '-----END PUBLIC KEY-----':
					state = 2
				else:
					key += line
		if state == 2:
			if pure:
				print (key)
			else:
				print (f'<selector>._domainkey.<domain> TXT\t"v=DKIM1; p={key}"')
			return True
		return False

	def _validate (self, domain: str, selector: str, key: str) -> bool:
		rc = False
		dns_options: Dict[str, Any] = {}
		if self.options.bget ('dns-tcp', False):
			dns_options['protocol'] = 'TCP'
		if (server := self.options.get ('dns-server')) is not None and server:
			dns_options['server'] = server
		try:
			qtype = 'TXT'
			dkim_domain = f'{selector}._domainkey.{domain}'
			answer = DNS.Request (**dns_options).req (name = dkim_domain, qtype = qtype)
			text = (Stream (answer.answers)
				.filter (lambda a: bool (a['typename'] == qtype))
				.map_to (List[bytes], lambda a: a['data'])
				.chain (bytes)
				.map (lambda t: t.decode ('UTF-8', errors = 'backslashreplace'))
				.join ()
			)
		except Exception as e:
			print (f'*** Failed to query dkim domain "{dkim_domain}": {e}')
		else:
			if text:
				record = (Stream (text.split (';'))
					.map (lambda e: e.strip ().split ('=', 1))
					.filter (lambda kv: len (kv) == 2 and bool (kv[0]) and bool (kv[1]))
					.dict ()
				)
				if record.get ('v') != 'DKIM1':
					print (f'*** No (valid) DKIM1 entry for "{dkim_domain}" found: {text}')
				else:
					try:
						rc = self._sign (key, record['p'])
					except KeyError:
						print (f'*** No KEY in DKIM entry "{dkim_domain}" found:\n{text}')
					except error as e:
						print (f'*** Failed to sign signature for "{dkim_domain}": {e}')
			else:
				print (f'*** No {qtype} record for {dkim_domain} found')
		print ('{dkim_domain} validation {status}'.format (
			dkim_domain = dkim_domain,
			status = 'successful' if rc else 'failed'
		))
		return rc
	
	def _sign (self, private_key: str, public_key: str) -> bool:
		rc = False
		pattern = '/var/tmp/mail-key-mgr-%%s-%d.%f' % (os.getpid (), time.time ())
		sign_file = pattern % 'sign'
		private_file = pattern % 'priv'
		public_file = pattern % 'pub'
		data_file = pattern % 'data'
		try:
			try:
				with open ('/dev/urandom', 'rb') as fdb:
					data = fdb.read (65536)
				with open (data_file, 'wb') as fdb:
					fdb.write (data)
				with open (private_file, 'w') as fdt:
					fdt.write (private_key)
				with open (public_file, 'w') as fdt:
					fdt.write ('-----BEGIN PUBLIC KEY-----\n')
					klen = len (public_key)
					n = 0
					while n < klen:
						fdt.write (public_key[n:n+70] + '\n')
						n += 70
					fdt.write ('-----END PUBLIC KEY-----\n')
			except IOError as e:
				raise error (f'Failed to setup files: {e}')
			cipher = self._find_cipher ()
			n = silent_call (['openssl', 'dgst', cipher, '-sign', private_file, '-out', sign_file, data_file])
			if n:
				raise error ('Failed to sign data, openssl returns with %d' % n)
			n = silent_call (['openssl', 'dgst', cipher, '-verify', public_file, '-signature', sign_file, data_file])
			if n:
				raise error ('Failed to verify data, openssl returns with %d' % n)
			rc = True
		finally:
			for fname in [sign_file, private_file, public_file, data_file]:
				if os.path.isfile (fname):
					try:
						os.unlink (fname)
					except OSError as e:
						print (f'*** Cleanup, failed to remove "{fname}": {e}')
		return rc
	
	def _find_cipher (self) -> str:
		cipher = '-sha512'
		pp = Popen (['openssl', 'dgst', '-list'], stdin = PIPE, stdout = PIPE, stderr = PIPE, text = True, errors = 'backslashreplace')
		(out, err) = pp.communicate (None)
		if pp.returncode == 0:
			cipher = Stream (out.split ()).regexp ('^-sha[0-9]+$').sorted ().last (no = cipher)
		return cipher

class SMIME (Mgr):
	__slots__: List[str] = []
	def command (self, command: str) -> bool:
		match command:
			case 'list':
				return self.list ()
			case 'store':
				return self._store_key ()
		raise error (f'{self.method}: unknown command "{command}", these are known: list, store')

	def _store_key (self) -> bool:
		if len (self.parameter) != 2:
			raise error ('smime.store: expected one parameter: <email> <keyfile>')
		email = self.parameter[0]
		key = self._read_key (self.parameter[1])
		return self.store (key, email = email)
	
	def _read_key (self, filename: str) -> str:
		if not os.path.isfile (filename):
			raise error (f'smime.store: missing file "{filename}"')
		#
		def read_file (path: str) -> None | str:
			with Ignore (OSError, UnicodeDecodeError):
				with open (path) as fd:
					return fd.read ()
			return None
		#
		output = '{directory}/.{filename}.temp'.format (
			directory = os.path.abspath (os.path.dirname (filename)),
			filename = os.path.basename (filename)
		)
		rc = pread (['openssl', 'version'])
		if rc.ec != 0:
			raise error (f'problem detecting version for openssl binary:\n{rc.error}')
		if (mtch := re.match ('^OpenSSL ([0-9]+)\\.', rc.output)) is not None:
			major = int (mtch.group (1))
		else:
			raise error (f'problem detecting openssl version from "{rc.output}"')
		command = ['openssl', 'pkcs12', '-in', filename, '-out', output]
		if major == 3:
			command += ['-noenc', '-legacy']
		else:
			command += ['-nodes']
		command += ['-password']
		result: None | str = None
		try:
			if filename.lower ().endswith ('.p12') or (result := read_file (filename)) is None:
				n = silent_call (command + ['pass:'])
				if n != 0:
					password: None | str = None
					while password is None:
						password = getpass (f'Please enter password for "{filename}": ')
						if not password:
							print ('** empty password not accepted')
							password = None
						else:
							n = silent_call (command + [f'pass:{password}'])
							if n != 0:
								print ('** password error, please retry')
								password = None
				if os.path.isfile (output):
					with open (output) as fd:
						result = fd.read ()
		finally:
			if os.path.isfile (output):
				os.unlink (output)
		if result is not None:
			return result
		raise error (f'{filename}: failed to read key')

class Mailkey (CLI):
	__slots__ = ['company_id', 'verbose', 'method', 'options', 'valid', 'parameter']
	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		parser.add_argument (
			'-c', '--company-id', action = 'store', type = int, dest = 'company_id', default = 0,
			help = 'the company id'
		)
		parser.add_argument (
			'-v', '--verbose', action = 'store_true',
			help = 'be more verbose'
		)
		parser.add_argument (
			'-m', '--method', action = 'store',
			help = 'select method for key management'
		)
		parser.add_argument (
			'-o', '--option', action = 'append', default = [],
			help = 'set method specific option'
		)
		parser.add_argument (
			'--valid', action = 'store',
			help = 'set valid period for this key'
		)
		parser.add_argument (
			'parameter', nargs = '*',
			help = 'method specific parameters'
		)

	def use_arguments (self, args: argparse.Namespace) -> None:
		self.company_id: int = args.company_id
		self.verbose: bool = args.verbose
		self.method: Optional[str] = args.method
		self.options: List[str] = args.option
		self.valid: Optional[str] = args.valid
		self.parameter: List[str] = args.parameter
	
	def executor (self) -> bool:
		if self.method is None:
			raise error ('missing method')
		try:
			mgr: Mgr = ({
				'dkim': DKIM,
				'smime': SMIME
			}[self.method]) (self.method, self.company_id, self.verbose, self.options, self.valid, self.parameter)
		except KeyError:
			raise error (f'{self.method}: unknown method')
		else:
			try:
				rc = mgr.execute ()
			except error as e:
				print (f'** {e}', file = sys.stderr, flush = True)
				rc = False
		return rc
#
if __name__ == '__main__':
	Mailkey.main ()
