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
import	argparse, logging, os, time, socket, re
from	datetime import datetime
from	dataclasses import dataclass
from	typing import Optional
from	typing import Dict, List, NamedTuple, Set
from	agn3.db import DB
from	agn3.definitions import base, fqdn, syscfg
from	agn3.email import EMail
from	agn3.exceptions import error
from	agn3.ignore import Ignore
from	agn3.io import create_path, which
from	agn3.mta import MTA
from	agn3.parser import Unit
from	agn3.runtime import Runtime
from	agn3.tools import listsplit, silent_call
#
logger = logging.getLogger (__name__)
#
class Autoresponder:
	directory = os.path.join (base, 'var', 'lib')
	def __init__ (self,
		rid: int,
		timestamp: datetime,
		ar_mailing_id: Optional[int],
		ar_security_token: Optional[str]
	) -> None:
		self.rid = rid
		self.timestamp = timestamp
		self.ar_mailing_id = ar_mailing_id
		self.ar_security_token = ar_security_token
		self.fname = os.path.join (self.directory, 'ar_%s.mail' % rid)
		self.limit = os.path.join (self.directory, 'ar_%s.limit' % rid)

	def write_file (self) -> None:
		if self.ar_mailing_id and os.path.isfile (self.fname):
			try:
				os.unlink (self.fname)
			except OSError as e:
				logger.error ('Unable to remove file %s: %s' % (self.fname, e))
	
	def remove_file (self) -> None:
		for fname in self.fname, self.limit:
			if os.path.isfile (fname):
				try:
					os.unlink (fname)
				except OSError as e:
					logger.error ('Unable to remove file %s: %s' % (fname, e))
#
class BavUpdate (Runtime): 
	unit = Unit ()
	config_filename = os.path.join (base, 'var', 'lib', 'bav.conf')
	local_filename = os.path.join (base, 'var', 'lib', 'bav.conf-local')
	rule_directory = os.path.join (base, 'var', 'lib')
	rule_file = os.path.join (base, 'lib', 'bav.rule')
	rule_pattern = re.compile ('bav_([0-9]+).rule')
	rule_format = os.path.join (rule_directory, 'bav_%d.rule')
	control_sendmail = os.path.join (base, 'bin', 'smctrl')
	restart_sendmail = os.path.join (base, 'var', 'run', 'sendmail-control.sh')
	sendmail_base = '/etc/mail'
	def supports (self, option: str) -> bool:
		return option not in ('dryrun', 'parameter')
	
	def setup (self) -> None:
		EMail.force_encoding ('UTF-8', 'quoted-printable')
		self.delay = self.unit.parse ('3m')
		self.fqdn = socket.getfqdn ().lower ()
		if not self.fqdn:
			self.fqdn = fqdn
		self.fixdomain = syscfg.get_str ('filter-name', 'localhost')
		self.mta = MTA ()
		self.domains: List[str] = []
		self.mtdom: Dict[str, int] = {}
		self.prefix = 'aml_'
		self.last = ''
		self.autoresponder: List[Autoresponder] = []
		self.read_mailertable ()
		try:
			files = os.listdir (Autoresponder.directory)
			for fname in files:
				if len (fname) > 8 and fname[:3] == 'ar_' and fname[-5:] == '.mail':
					with Ignore (ValueError, OSError):
						rid = int (fname[3:-5])
						st = os.stat (os.path.join (Autoresponder.directory, fname))
						self.autoresponder.append (Autoresponder (rid, datetime.fromtimestamp (st.st_ctime), None, None))
		except OSError as e:
			logger.error ('Unable to read directory %s %s' % (Autoresponder.directory, e))

	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		parser.add_argument ('-D', '--delay', action = 'store', type = self.unit.parse, default = self.delay, help = 'delay between each reread of database content')
	
	def use_arguments (self, args: argparse.Namespace) -> None:
		self.delay = args.delay

	def executor (self) -> bool:
		while self.running:
			self.update ()
			n = self.delay
			while n > 0 and self.running:
				time.sleep (1)
				n -= 1
		return True
	
	def file_reader (self, fname: str) -> List[str]:
		with open (fname, errors = 'backslashreplace') as fd:
			return [line.rstrip ('\r\n') for line in fd if not line[0] in '\n#']

	class Domain (NamedTuple):
		name: str
		value: str
	@dataclass
	class Filecontent:
		path: Optional[str]
		content: List[BavUpdate.Domain]
		modified: bool
		hash: Optional[str]
	@dataclass
	class RID:
		rid: int
		domain: str
	def read_mailertable (self, new_domains: Optional[Dict[str, BavUpdate.RID]] = None) -> None:
		self.domains.clear ()
		self.mtdom.clear ()
		if self.mta.mta == 'postfix':
			def find (key: str, default_value: str) -> BavUpdate.Filecontent:
				rc = BavUpdate.Filecontent (path = None, content = [], modified = False, hash = None)
				with Ignore (KeyError):
					for element in self.mta.getlist (key):
						hash: Optional[str]
						path: str
						try:
							(hash, path) = element.split (':', 1)
						except ValueError:
							(hash, path) = (None, element)
						if path.startswith (base):
							if rc.path is None:
								rc.path = path
								rc.hash = hash
							if not os.path.isfile (path):
								create_path (os.path.dirname (path))
								open (path, 'w').close ()
								if hash is not None:
									self.mta.postfix_make (path)
					if rc.path is not None:
						try:
							with open (rc.path) as fd:
								for line in (_l.strip () for _l in fd):
									try:
										(var, val) = [_v.strip () for _v in line.split (None, 1)]
									except ValueError:
										var = line
										val = default_value
										rc.modified = True
									if var not in [_c.name for _c in rc.content]:
										rc.content.append (BavUpdate.Domain (name = var, value = val))
									else:
										rc.modified = True
							logger.debug ('Read %d lines from %s' % (len (rc.content), rc.path))
						except OSError as e:
							logger.error ('Failed to read %s: %s' % (rc.path, e))
					else:
						logger.warning ('No path for postfix parameter %s found' % key)
				return rc
			def save (ct: BavUpdate.Filecontent) -> None:
				if ct.path is not None and (ct.modified or not os.path.isfile (ct.path)):
					try:
						with open (ct.path, 'w') as fd:
							if ct.content:
								fd.write ('\n'.join (['%s\t%s' % _c for _c in ct.content]) + '\n')
						logger.info ('Written %d lines to %s' % (len (ct.content), ct.path))
						if ct.hash is not None:
							self.mta.postfix_make (ct.path)
					except OSError as e:
						logger.error ('Failed to save %s: %s' % (ct.path, e))
			#
			relay_default_value = 'dummy'
			relays = find ('relay_domains', relay_default_value)
			for d in relays.content:
				self.mtdom[d.name] = 0
			#
			def add_relay_domain (domain_to_add: str) -> None:
				if domain_to_add and domain_to_add not in self.mtdom:
					relays.content.append (BavUpdate.Domain (name = domain_to_add, value = relay_default_value))
					relays.modified = True
					self.mtdom[domain_to_add] = 0
			#
			if new_domains:
				for domain in new_domains:
					add_relay_domain (domain)
			transport_default_value = 'mailloop:'
			transports = find ('transport_maps', transport_default_value)
			with DB () as db:
				for row in db.query ('SELECT mailloop_domain FROM company_tbl WHERE mailloop_domain IS NOT NULL AND status = :status', {'status': 'active'}):
					if row.mailloop_domain:
						add_relay_domain (row.mailloop_domain.strip ().lower ())
			transport_domains = set ([_c[0] for _c in transports.content])
			for d in relays.content[:]:
				if d.name not in transport_domains:
					transports.content.append (BavUpdate.Domain (name = d.name, value = transport_default_value))
					transports.modified = True
				self.mtdom[d.name] += 1
			save (relays)
			save (transports)
			if relays.modified or transports.modified:
				cmd = which ('smctrl')
				if cmd is not None:
					n = silent_call (cmd, 'service', 'reload')
					if n == 0:
						logger.info ('Reloaded')
					else:
						logger.error ('Reloading failed: %d' % n)
			self.domains = [_c.name for _c in relays.content]
		else:
			try:
				for line in self.file_reader (os.path.join (self.sendmail_base, 'mailertable')):
					parts = line.split ()
					if len (parts) > 1 and not parts[0].startswith ('.') and parts[1].startswith ('procmail:'):
						self.domains.append (parts[0])
						self.mtdom[parts[0]] = 0
			except IOError as e:
				logger.error ('Unable to read mailertable %s' % e)
			try:
				for line in self.file_reader (os.path.join (self.sendmail_base, 'relay-domains')):
					if line in self.mtdom:
						self.mtdom[line] += 1
					else:
						logger.debug ('We relay domain "%s" without catching it in mailertable' % line)
				for key in self.mtdom.keys ():
					if self.mtdom[key] == 0:
						logger.debug ('We define domain "%s" in mailertable, but do not relay it' % key)
			except IOError as e:
				logger.error ('Unable to read relay-domains %s' % e)
	
	def read_mail_files (self) -> List[str]:
		rc: List[str] = []
		if self.fqdn:
			rc.append ('@%s\taccept:rid=local' % self.fqdn)
		if self.mta.mta == 'sendmail':
			try:
				for line in self.file_reader (os.path.join (self.sendmail_base, 'local-host-names')):
					rc.append ('@%s\taccept:rid=local' % line)
			except IOError as e:
				logger.error (f'Unable to read local-host-names: {e}')
			try:
				for line in self.file_reader (os.path.join (self.sendmail_base, 'virtusertable')):
					parts = line.split ()
					if len (parts) == 2:
						rc.append ('%s\taccept:rid=virt,fwd=%s' % (parts[0], parts[1]))
			except IOError as e:
				logger.error (f'Unable to read virtusertable: {e}')
		return rc

	def update_rules (self, rules: Dict[int, Dict[str, List[str]]]) -> None:
		inuse: Set[int] = set ()
		global_rule = None
		for (rid, rule) in rules.items ():
			if global_rule is None:
				try:
					with open (self.rule_file) as fd:
						global_rule = fd.read ()
					if not global_rule.endswith ('\n'):
						global_rule += '\n'
				except IOError as e:
					logger.error ('Failed to open "%s" for reading: %s' % (self.rule_file, e))
			inuse.add (rid)
			fname = self.rule_format % rid
			try:
				with open (fname, 'w') as fd:
					if global_rule:
						fd.write (global_rule)
					for sect in sorted (rule):
						fd.write ('[%s]\n' % sect)
						for line in rule[sect]:
							fd.write ('%s\n' % line)
			except IOError as e:
				logger.error ('Failed to open "%s" for writing: %s' % (fname, e))
		todel: List[str] = []
		try:
			for fname in os.listdir (self.rule_directory):
				m = self.rule_pattern.match (fname)
				if m is not None:
					rid = int (m.group (1))
					if rid not in inuse:
						todel.append (fname)
			for fname in todel:
				path = os.path.join (self.rule_directory, fname)
				try:
					os.unlink (path)
				except OSError as e:
					logger.error ('Failed to remove "%s": %s' % (fname, e))
		except OSError as e:
			logger.error ('Failed to access rule_directory "%s": %s' % (self.rule_directory, e))

	class Forward (NamedTuple):
		rid: int
		address: str
	def read_database (self, auto: List[Autoresponder]) -> List[str]:
		rc: List[str] = []
		with DB () as db:
			company_list: List[int] = []
			new_domains: Dict[str, BavUpdate.RID] = {}
			forwards: List[BavUpdate.Forward] = []
			seen_domains: Set[str] = set ()
			accepted_forwards: Set[str] = set ()
			ctab: Dict[int, str] = {}
			#
			rc.append ('fbl@%s\taccept:rid=unsubscribe' % self.fixdomain)
			for domain in self.domains:
				if domain not in seen_domains:
					rc.append ('fbl@%s\talias:fbl@%s' % (domain, self.fixdomain))
					seen_domains.add (domain)
			if self.fixdomain not in seen_domains:
				new_domains[self.fixdomain] = BavUpdate.RID (rid = 0, domain = self.fixdomain)
			seen_domains.add (self.fixdomain)
			#
			missing = []
			for row in db.query ('SELECT company_id, mailloop_domain FROM company_tbl WHERE status = :status', {'status': 'active'}):
				if row.mailloop_domain:
					ctab[row.company_id] = row.mailloop_domain
					if row.mailloop_domain not in seen_domains:
						rc.append ('fbl@%s\talias:fbl@%s' % (row.mailloop_domain, self.fixdomain))
						if row.mailloop_domain not in self.mtdom and row.mailloop_domain.lower () != self.fqdn:
							new_domains[row.mailloop_domain] = BavUpdate.RID (rid = 0, domain = row.mailloop_domain)
						seen_domains.add (row.mailloop_domain)
				else:
					missing.append (row.company_id)
				company_list.append (row.company_id)
			if missing:
				missing.sort ()
				logger.debug ('Missing mailloop_domain for %s' % ', '.join ([str (m) for m in missing]))
			#
			seen_rids: Set[int] = set ()
			for row in db.query (
				'SELECT rid, shortname, company_id, filter_address, '
				'       forward_enable, forward, ar_enable, '
				'       subscribe_enable, mailinglist_id, form_id, timestamp, '
				'       spam_email, spam_required, spam_forward, '
				'       autoresponder_mailing_id, security_token '
				'FROM mailloop_tbl'
			):
				if row.company_id not in company_list or row.rid is None:
					continue
				seen_rids.add (row.rid)
				domains: List[str] = [self.fixdomain]
				aliases: List[str] = []
				if row.filter_address is not None:
					for alias in listsplit (row.filter_address):
						if not alias.startswith (self.prefix):
							with Ignore (ValueError):
								domain_part = alias.split ('@', 1)[-1]
								if domain_part not in domains:
									domains.append (domain_part)
									if domain_part not in self.mtdom and domain_part not in new_domains:
										new_domains[domain_part] = BavUpdate.RID (rid = row.rid, domain = domain_part)
								aliases.append (alias)
				#
				ar_enable = False
				if row.ar_enable and row.autoresponder_mailing_id:
					if not row.security_token:
						logger.error ('%s: Autoresponder has mailing id, but no security token' % row.rid)
					else:
						auto.append (
							Autoresponder (
								row.rid,
								row.timestamp if row.timestamp is not None else datetime.now (),
								row.autoresponder_mailing_id,
								row.security_token
							)
						)
						ar_enable = True
				#
				try:
					cdomain = ctab[row.company_id]
					if cdomain not in domains:
						if cdomain in self.domains:
							domains.append (cdomain)
						else:
							logger.debug ('Company\'s domain "%s" not found in mailertable' % cdomain)
				except KeyError:
					logger.debug ('No domain for company found, further processing')
				extra = ['rid=%s' % row.rid]
				if row.company_id:
					extra.append ('cid=%d' % row.company_id)
				if row.forward_enable and row.forward:
					extra.append ('fwd=%s' % row.forward)
					forwards.append (BavUpdate.Forward (rid = row.rid, address = row.forward))
				if row.spam_email:
					extra.append ('spam_email=%s' % row.spam_email)
				if row.spam_forward:
					extra.append ('spam_fwd=%d' % row.spam_forward)
				if row.spam_required:
					extra.append ('spam_req=%d' % row.spam_required)
				if ar_enable:
					extra.append ('ar=%s' % row.rid)
					if row.autoresponder_mailing_id:
						extra.append ('armid=%d' % row.autoresponder_mailing_id)
				if row.subscribe_enable and row.mailinglist_id and row.form_id:
					extra.append ('sub=%d:%d' % (row.mailinglist_id, row.form_id))
				for domain in domains:
					line = '%s%s@%s\taccept:%s' % (self.prefix, row.rid, domain, ','.join (extra))
					logger.debug (f'Add line: {line}')
					rc.append (line)
				if aliases and domains:
					for alias in aliases:
						rc.append ('%s\talias:%s%s@%s' % (alias, self.prefix, row.rid, domains[0]))
						accepted_forwards.add (alias)
			#
			if seen_rids:
				rules: Dict[int, Dict[str, List[str]]] = {}
				for row in db.query ('SELECT rid, section, pattern FROM mailloop_rule_tbl'):
					if row.rid in seen_rids:
						try:
							rule = rules[row.rid]
						except KeyError:
							rule = rules[row.rid] = {}
						try:
							sect = rule[row.section]
						except KeyError:
							sect = rule[row.section] = []
						sect.append (row.pattern)
				self.update_rules (rules)
			#
			for forward in forwards:
				with Ignore (ValueError):
					fdomain = (forward.address.split ('@', 1)[-1]).lower ()
					for domain in self.mtdom:
						if domain == fdomain and forward.address not in accepted_forwards:
							logger.warning ('%s: using address "%s" with local handled domain "%s"' % (forward.rid, forward.address, domain))
					refuse = []
					for (domain, new_domain) in ((_d, _n) for (_d, _n) in new_domains.items () if _d == fdomain):
						logger.warning ('%s: try to add new domain for already existing forward address "%s" in %s, refused' % (new_domain.rid, forward.address, forward.rid))
						refuse.append (domain)
					for domain in refuse:
						del new_domains[domain]
			#
			if new_domains:
				if self.mta.mta == 'sendmail':
					cmd = [self.control_sendmail, 'add']
					for domain in new_domains:
						cmd.append (domain)
					logger.info (f'Found new domains, add them using {cmd}')
					silent_call (*cmd)
					logger.info ('Restarting sendmail due to domain update')
					silent_call (self.restart_sendmail)
				self.read_mailertable (new_domains)
		return rc
	
	def read_local_files (self) -> List[str]:
		try:
			return self.file_reader (self.local_filename)
		except IOError as e:
			logger.debug ('Unable to read local file %s %s' % (self.local_filename, e))
			return []
	
	def update_autoresponder (self, auto: List[Autoresponder]) -> None:
		newlist: List[Autoresponder] = []
		for new in auto:
			found = None
			for old in self.autoresponder:
				if new.rid == old.rid:
					found = old
					break
			if not found or new.timestamp > found.timestamp:
				new.write_file ()
				newlist.append (new)
			else:
				newlist.append (found)
		for old in self.autoresponder:
			for new in newlist:
				if old.rid == new.rid:
					break
			else:
				old.remove_file ()
		self.autoresponder = newlist
	
	def rename_file (self, old_file: str, new_file: str) ->None:
		try:
			os.rename (old_file, new_file)
		except OSError as e:
			logger.error ('Unable to rename %s to %s %s' % (old_file, new_file, e))
			try:
				os.unlink (old_file)
			except OSError as e:
				logger.warning ('Failed to remove temp. file %s %s' % (old_file, e))
			raise error ('rename_file')

	def update_config_file (self, new: str) -> None:
		if new != self.last:
			temp = '%s.%d' % (self.config_filename, os.getpid ())
			try:
				with open (temp, 'w') as fd:
					fd.write (new)
				self.rename_file (temp, self.config_filename)
				self.last = new
			except IOError as e:
				logger.error ('Unable to write %s %s' % (temp, e))
				raise error ('update_config_file.open', e)

	def update (self) -> None:
		try:
			auto: List[Autoresponder] = []
			new = self.read_mail_files ()
			new += self.read_database (auto)
			new += self.read_local_files ()
			self.update_autoresponder (auto)
			self.update_config_file ('\n'.join (new) + '\n')
		except error as e:
			logger.exception ('Update failed: %s' % e)

if __name__ == '__main__':
	BavUpdate.main ()
