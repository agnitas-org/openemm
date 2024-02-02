#!/bin/sh
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
. $HOME/scripts/config.sh
#
script="$BASE/var/tmp/bounce-rules3.py"
cat << __EOF__ > $script
#!/usr/bin/env python3
#
import	argparse, re, json, difflib
from	typing import Literal, Optional
from	typing import Dict, List, Set
from	agn3.db import DB
from	agn3.emm.bounce import Bounce
from	agn3.emm.build import spec
from	agn3.emm.config import EMMCompany
from	agn3.exceptions import error
from	agn3.ignore import Ignore
from	agn3.parameter import Parameter
from	agn3.parser import Line, Field, Lineparser
from	agn3.runtime import CLI
#
class Main (CLI):
	__slots__ = ['dryrun', 'caller']
	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		parser.add_argument ('-n', '--dryrun', action = 'store_true')
		parser.add_argument ('caller', nargs = 1)
	
	def use_arguments (self, args: argparse.Namespace) -> None:
		self.dryrun = args.dryrun
		self.caller = args.caller

	def executor (self) -> bool:
		ok = True
		with open (self.caller[0]) as fd:
			bounce_parser = Lineparser (
				lambda l: l.split (';', 4),
				'name',
				Field ('description', lambda v: v if v else None),
				Field ('dsn', int),
				Field ('detail', int),
				Field ('pattern', lambda v: v if v else None)
			)
			bav_parser = Lineparser (
				lambda l: l.split (';', 2),
				'name',
				'section',
				'pattern'
			)
			ote_parser = Lineparser (
				lambda l: l.split (';', 2),
				'name',
				'domains',
				'relays'
			)
			#
			pattern_reset = re.compile ('^## *RESET$')
			pattern_parameter = re.compile ('^## *PARAMETER: *(.*)$')
			pattern_bounce_rules = re.compile ('^## *BOUNCE-RULES$')
			pattern_bav_rules = re.compile ('^## *BAV-RULES$')
			pattern_ote = re.compile ('^## *OTE$')
			section: Literal[
				None,
				'bounce',
				'bav',
				'ote'
			] = None
			parameter: Optional[Parameter] = None
			bounce_rules: List[Line] = []
			bav_rules: List[Line] = []
			ote_rules: List[Line] = []
			for (lineno, line) in enumerate ((_l.strip () for _l in fd), start = 1):
				if (match := pattern_parameter.match (line)) is not None:
					parameter = Parameter (match.group (1))
					continue
				if pattern_reset.match (line) is not None:
					section = None
					continue
				if pattern_bounce_rules.match (line) is not None:
					section = 'bounce'
					continue
				if pattern_bav_rules.match (line) is not None:
					section = 'bav'
					continue
				if pattern_ote.match (line) is not None:
					section = 'ote'
					continue
				if section is None or not line or line.startswith ('#'):
					continue
				#
				try:
					if section == 'bounce':
						bounce_rules.append (bounce_parser (line))
					elif section == 'bav':
						bav_rules.append (bav_parser (line))
					elif section == 'ote':
						ote_rules.append (ote_parser (line))
				except error as e:
					print (f'Failed to parse {self.caller[0]}:{lineno}:{line} due to: {e}')
					ok = False
			if ok:
				with DB () as db:
					if parameter is not None and not self.update_bounce_configuration (db, parameter):
						ok = False
					if not self.update_bounce_rules (db, bounce_rules):
						ok = False
					if not self.update_bav_rules (db, bav_rules):
						ok = False
					if not self.update_ote_rules (db, ote_rules):
						ok = False
					db.sync (not self.dryrun and ok)
		return ok

	def update_bounce_configuration (self, db: DB, parameter: Parameter) -> bool:
		rc = True
		orig = Parameter ()
		emmcompany = EMMCompany (db = db, keys = [Bounce.name_company_info_conversion])
		description = 'written'
		with Ignore (KeyError):
			orig.loads (emmcompany.get (Bounce.name_company_info_conversion))
			description = 'updated'
		description += f' by EMM backend update release {spec.version}'
		target = Parameter ()
		to_remove: Set[str] = set ()
		for (key, value) in orig.items ():
			with Ignore (KeyError):
				new_value = parameter[key]
				if new_value != '-':
					target[key] = value
				elif self.dryrun:
					print (f'Parameter: removed "{key}" from parameter set')
		for (key, value) in parameter.items ():
			if value != '-':
				target[key] = value
				try:
					old_value = orig[key]
					if self.dryrun and old_value != value:
						print (f'Parameter: updated "{key}" from "{old_value}" to "{value}"')
				except KeyError:
					if self.dryrun:
						print (f'Parameter: added "{key}" with "{value}"')
			else:
				to_remove.add (key)
		if orig != target:
			if self.dryrun:
				print (f'Would update parameter from {orig} to {target} using {parameter}')
			else:
				if not emmcompany.write (
					0, Bounce.name_company_info_conversion, target.dumps (),
					description = description
				):
					print ('Failed writing changed parameter to database')
					rc = False
		elif self.dryrun:
			print ('Parameter not changed')
		if db.exists (Bounce.bounce_config_table):
			with Bounce (db = db) as bounce:
				original = bounce.get_config (0, 0, Bounce.name_conversion)
				obj = original.copy ()
				for (key, value) in target.items ():
					obj[key] = int (value)
				for key in to_remove:
					if key in obj:
						del obj[key]
				if original != obj:
					if self.dryrun:
						print (f'Would update {Bounce.bounce_config_table} from {original} to {obj}')
					else:
						bounce.set_config (0, 0, Bounce.name_conversion, obj)
				elif self.dryrun:
					print ('Configuration not changed')
		return rc

	def update_bounce_rules (self, db: DB, rules: List[Line]) -> bool:
		valid = True
		names: Dict[str, Line] = {}
		for rule in rules:
			rule_formated = f'{rule.name};{rule.description};{rule.dsn};{rule.detail};{rule.pattern}'
			if not rule.name:
				print (f'{rule_formated}: missing name')
				valid = False
			elif rule.name in names:
				print (f'{rule_formated}: duplicate name "{rule.name}"')
				valid = False
			else:
				names[rule.name] = rule
		if not valid:
			return False
		#
		new = 0
		disabled = 0
		updated = 0
		existing: Set[str] = set ()
		for row in db.queryc (
			'SELECT rule_id, dsn, detail, pattern, shortname, description '
			'FROM bounce_translate_tbl '
			'WHERE company_id = 0 AND active = 1'
		):
			existing.add (row.shortname)
			try:
				rule = names[row.shortname]
				if (rule.description, rule.dsn, rule.detail, rule.pattern) != (row.description, row.dsn, row.detail, row.pattern):
					if self.dryrun:
						print (f'Would update {row} using {rule}')
					else:
						db.update (
							'UPDATE bounce_translate_tbl '
							'SET dsn = :dsn, detail = :detail, pattern = :pattern, description = :description, change_date = CURRENT_TIMESTAMP '
							'WHERE rule_id = :rule_id',
							{
								'dsn': rule.dsn,
								'detail': rule.detail,
								'pattern': rule.pattern,
								'description': rule.description,
								'rule_id': row.rule_id
							}
						)
					updated += 1
			except KeyError:
				if self.dryrun:
					print (f'Would disable {row} to mark as inactive')
				else:
					db.update (
						'UPDATE bounce_translate_tbl '
						'SET active = 0, change_date = CURRENT_TIMESTAMP '
						'WHERE rule_id = :rule_id',
						{
							'rule_id': row.rule_id
						}
					)
				disabled += 1
		for rule in (_r for _r in names.values () if _r.name not in existing):
			if self.dryrun:
				print (f'Would insert new rule {rule}')
			else:
				db.update (
					db.qselect (
						oracle = (
							'INSERT INTO bounce_translate_tbl '
							'       (rule_id, company_id, dsn, detail, pattern, active, shortname, description, creation_date, change_date) '
							'VALUES '
							'       (bounce_translate_tbl_seq.nextval, 0, :dsn, :detail, :pattern, 1, :shortname, :description, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)'
						), mysql = (
							'INSERT INTO bounce_translate_tbl '
							'       (company_id, dsn, detail, pattern, active, shortname, description, creation_date, change_date) '
							'VALUES '
							'       (0, :dsn, :detail, :pattern, 1, :shortname, :description, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)'
						)
					),
					{
						'shortname': rule.name,
						'dsn': rule.dsn,
						'detail': rule.detail,
						'pattern': rule.pattern,
						'description': rule.description
					}
				)
			new += 1
		if self.dryrun or new or disabled or updated:
			print (f'Bounce rules: added {new} rules, disable {disabled} rules, update {updated} rules')
		return True

	def invalid_bounce_rule_pattern (self, pattern: str) -> bool:
		try:
			Parameter (pattern)
		except ValueError:
			return True
		else:
			return False
	
	def update_ote_rules (self, db: DB, rules: List[Line]) -> bool:
		rc = True
		if db.exists (Bounce.ote_table):
			def mkdesc (name: str) -> str:
				return f'predefined entry: {name}'
			def norm (value: str) -> str:
				return value.strip () if value else ''
			source: Dict[str, Line] = {}
			for rule in rules:
				if not rule.name:
					print ('Empty rule name not allowed')
					rc = False
				elif rule.name in source:
					print (f'Duplicate entry from source: "{rule.name}" found')
					rc = False
				else:
					source[mkdesc (rule.name)] = rule
			if not rc:
				return False
			#
			updated = 0
			removed = 0
			added = 0
			for row in db.streamc (
				'SELECT description, domains, relays '
				f'FROM {Bounce.ote_table} '
				'WHERE company_id = 0'
			).filter (lambda r: bool (r.description) and r.description in source):
				rule = source.pop (row.description)
				domains = norm (row.domains)
				relays = norm (row.relays)
				if domains != rule.domains or relays != rule.relays:
					if rule.domains or rule.relays:
						if self.dryrun:
							print (f'Would update "{row.description}" with: {rule}')
							updated += 1
						else:
							updated += db.update (
								f'UPDATE {Bounce.ote_table} '
								'SET domains = :domains, relays = :relays, change_date = CURRENT_TIMESTAMP '
								'WHERE description = :description AND company_id = 0',
								{
									'domains': rule.domains if rule.domains else None,
									'relays': rule.relays if rule.relays else None,
									'description': row.description
								}
							)
					else:
						if self.dryrun:
							print (f'Would remove "{row.description}"')
							removed += 1
						else:
							removed += db.update (
								f'DELETE FROM {Bounce.ote_table} '
								'WHERE description = :description AND company_id = 0',
								{
									'description': row.description
								}
							)
			for (description, rule) in source.items ():
				if rule.domains or rule.relays:
					if self.dryrun:
						print (f'Would add "{description}" with: {rule}')
						added += 1
					else:
						added += db.update (
							f'INSERT INTO {Bounce.ote_table} '
							'        (company_id, domains, relays, active, description, creation_date, change_date) '
							'VALUES '
							'        (0, :domains, :relays, 1, :description, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)',
							{
								'domains': rule.domains if rule.domains else None,
								'relays': rule.relays if rule.relays else None,
								'description': description
							}
						)
			if self.dryrun or updated or removed or added:
				print (f'OTE: added {added} rules, removed {removed} rules, update {updated} rules')
		return rc
				
	def update_bav_rules (self, db: DB, rules: List[Line]) -> bool:
		rc = True
		if db.exists (Bounce.bounce_rule_table):
			with Bounce (db = db) as bounce:
				original = json.dumps (bounce.get_rule (0, 0), indent = 2, sort_keys = True)
				record: Dict[str, List[str]] = {}
				for rule in rules:
					try:
						record[rule.section].append (rule.pattern)
					except KeyError:
						record[rule.section] = [rule.pattern]
				updated = json.dumps (record, indent = 2, sort_keys = True)
				if original != updated:
					if self.dryrun:
						diff = '\n'.join (
							difflib.unified_diff (
								original.split ('\n'),
								updated.split ('\n'),
								fromfile = 'original',
								tofile = 'updated'
							)
						)
						print (f'Would update bav rules records with these changes:\n{diff}')
					else:
						bounce.set_rule (0, 0, record)
				elif self.dryrun:
					print ('No changes in bav rules.')
		return rc
		
if __name__ == '__main__':
	Main.main ()
__EOF__
chmod 755 $script
$script "$0"
rc=$?
rm -f $script
exit $rc
#STOP
#
## PARAMETER: convert-bounce-count="10", convert-bounce-duration="30", last-click="30", last-open="30", max-age-create="-", max-age-change="-", fade-out="14", expire="1100", threshold="5"
## BOUNCE-RULES
50x;;50;400;
51x;;51;410;
52x;;52;420;
53x;;53;400;
54x;;54;400;
55x;;55;400;
56x;;56;400;
57x;;57;400;
58x;;58;400;
59x;;59;400;
EMM-7115;;443;410;stat="Host or domain name not found"
EMM-7316 GMX;;500;420;stat="exceeded storage allocation", relay=".gmx.net."
EMM-7316 WEB;;500;420;stat="exceeded storage allocation", relay=".web.de."
EMM-7316 KDN;;500;420;stat="exceeded storage allocation", relay=".kundenserver.de."
EMM-7312;;500;511;relay=".yahoo", stat="This user doesn't have a yahoo.* account"
EMM-7310;;500;511;stat="mailbox is disabled", relay=".yahoo"
EMM-7311;;500;511;stat="Your IP will be reported for abuse - better watch out next time."
restore default behviour for 511;;511;511;
restore default behviour for 512;;512;512;
restore default behviour for 513;;513;511;
restore default behviour for 516;;516;511;
restore default behviour for 521;;521;511;
restore default behviour for 531;;531;510;
restore default behviour for 550;;550;511;stat="unknown recipient"
restore default behviour for 571;;571;410;
restore default behviour for 572;;572;511;
EMM-7389;;530;511;stat="No such user"
EMM-7378;;500;511;stat="invalid mailbox", relay=".mail.ru."
EMM-7492;;520;511;stat="no such mailbox", relay=".rzone.de."
EMM-7491;;550;511;stat="no such recipient"
EMM-7593;;500;511;stat="Not a valid recipient", relay=".yahoo"
EMM-7691;;500;511;stat="No such user"
EMM-7743;;541;511;stat="Recipient address rejected", relay=".protection.outlook.com.|.eo.outlook.com.|.protection.office365.us."
EMM-7816 417;;417;0;
EMM-7816 418;;418;0;
EMM-7816 517;;517;0;
EMM-7816 518;;518;0;
EMM-7990;;500;511;stat="no mailbox here"
EMM-7991;;5;511;relay="void.blackhole.mx."
EMM-7901;;500;511;stat="user unknown|unknown user"
EMM-7594;;500;511;stat="mailbox not found|unknown add?ress|unrouteable add?ress"
EMM-7379;;500;511;stat="mailbox unavailable", relay=".rmx.de.|.protection.outlook.com.|.retarus.com."
EMM-7409;;500;511;stat="5.1.0 Address rejected."
EMM-8045 510;;510;511;stat="user unknown|unknown user"
EMM-8045 530;;530;511;stat="user unknown|unknown user"
EMM-8046;;500;511;stat="Es gibt keine Person mit diesem Namen unter dieser Adresse", relay=".sp-server.net."
EMM-8044;;530;511;stat="user does not exist"
EMM-8028;;571;511;stat="user does not exist"
EMM-8043;;546;512;stat="mail for .* loops back to myself"
EMM-8027;;500;511;stat="no such recipient here|recipient unknown|invalid recipient|no valid recipient"
EMM-8030;;500;511;stat="Mailaddress is administratively disabled"
EMM-8029;;500;511;stat="mailbox for .* does not exist"
EMM-8135;;474;513;stat="TLS is required, but was not offered"
EMM-8135-4;;4;513;port="465"
EMM-8135-5;;5;513;port="465"
EMM-8617;;500;511;stat="No such local user", relay=".firemail.de."
EMM-8629;;550;511;stat="recipient rejected"
EMM-8634;;571;511;stat="user unknown|no such user", relay=".vol.at.|.yandex.ru.|.antispameurope.com.|.uni-wuerzburg.de.|.bund.de."
EMM-8628;;500;511;stat="user suspended"
EMM-8637;;500;511;stat="address unknown"
EMM-8636;;500;511;stat="user not found"
EMM-8638;;500;511;stat="unkown user", relay=".sil.at.|.silverserver.at."
EMM-7490 520;;520;511;stat="mailbox.*unavailable"
EMM-7490 550;;550;511;stat="mailbox.*unavailable"
EMM-8635;;571;511;stat="User email address is marked as invalid", relay=".ppe-hosted.com."
EMM-8618;;500;511;stat="max message size exceeded", relay="mx.tim.it."
EMM-8826;;500;511;stat="mailbox unavailable \(in reply to RCPT TO command\)"
EMM-8802;;571;511;stat="mailbox unavailable", relay=".bund.de."
EMM-8801;;571;511;stat="user [^ ]+ does not exist", relay=".hostedemail.com."
EMM-8800;;500;511;stat="address invalid"
EMM-8725;;500;512;stat="this domain is not hosted here"
EMM-8723;;500;511;stat="account is disabled"
EMM-8721;;510;512;stat="domain [^ ]+ does not accept mail"
EMM-8713;;500;511;stat="recipient does not exist"
EMM-8709;;500;511;stat="unknown recipient"
EMM-8710;;500;511;stat="address not present"
EMM-8693 500;;500;511;stat="recipient not found"
EMM-8693 571;;571;511;stat="recipient not found", relay=".uni-bielefeld.de.|.tutanota.de.|.cleanmail.ch.|.uni-kl.de."
EMM-8708;;500;511;stat="envelope blocked", relay=".mimecast.com."
EMM-8870;;500;511;stat="Address not present in directory"
EMM-8724;;500;511;stat="can't verify recipient", relay=".koeln.de.|.tele2.de|.hamburg.de.|.inter.net.|.berlin.de.'"
EMM-9020;;500;511;stat="Domain not in use", relay=".tuwien.ac.at."
EMM-10052;;500;511;stat="denied by SecuMail valid-address-filter"
##
## BAV-RULES
##
#
#	Detect non daemon mail, which is still generated automatically
;systemmail;^Subject: .*(DELIVER|RETURNED MAIL).*
;systemmail;^Return-Path:.*<>
;systemmail;^Precedence: bulk
;systemmail;^Return-Path:.*<(MAILER-DA?EMON.*|(mailgun|mailout)@(.*\.)?(agnitas|mailemm)\.de)>
;systemmail;^Subject:.*unsubscribe:[A-Za-z0-9]+(\.[A-Za-z0-9]+)+
;systemmail;^X-Env-Sender: (mailgun|mailout)@(.*\.)?(agnitas|mailemm)\.de
#
#
#	Every mail that matches this filter won't be forwarded and will be
#	stored locally in a file beginning with {mark}. Forward mail used
#	`sent' as mark.
;filter;{auto}^(From|Sender): .*(AUTO.*(RESPOND|ANTWORT|REPLY)|(ZUSTELL|EINGANGS)BEST(AE|Ä|=E4)TIGUNG|KEINEANTWORTADRESSE).*
;filter;{ooo}^Subject: .*(OUT OF.*OFFICE|AUTO.*RESPOND|AUTO.*REPLY|ABWESENHEIT|URLAUBSVERTRETUNG|ABWESEND).*
;filter;{ooo}^Subject: .*(AUTOMATISCH|VACATION|ZUSTELLBENACH|IST.*AU(SS|ß|=DF)ER.*HAUS|IST NICHT IM HAUS|IST IM URLAUB|(AUTO|ACTION).*NOTIFICATION|EINGANGSBEST(AE|Ä|=E4)TIGUNG|IS OUT OF E-MAILACCESS).*
#
#
#	Hardbounces
;hard;unknown user|user unknown|user not found
;hard;^There is no such user\.
;hard;^        Recipient.s Mailbox unavailable
;hard;^Receiver not found:
;hard;^Action: failed
;hard;^Sorry, no mailbox here by that name\.
;hard;^    The recipient name is not recognized
;hard;^Invalid receiver address:
;hard;^did not reach the following recipient\(s\):
;hard;^Ihre Mail ist leider nicht zustellbar\.
;hard;^Sorry. Your message could not be delivered to:
;hard;^No such user\.
;hard;^   user .* not known at this site\.
;hard;^    unknown local-part .* in domain .*
;hard;^.* sorry, no such mailbox here
;hard;^User  not listed in public
;hard;The user.s email name is not found\.
;hard;: unknown recipient:
;hard;^User not known
;hard;^Diese Adresse ist nicht mehr verfuegbar\.$
;hard;^The following destination addresses were unknown
;hard;Received <<< 550 Invalid recipient <.*>
;hard;Unknown recipient address
;hard;address: <.*> ... failed
;hard;<<< 550 <.*> ... failed
;hard;550 Invalid recipient <.*>
;hard;550 Unknown local part .* in <.*>
;hard;553 Invalid recipient address
;hard;550 No such recipient
;hard;User name is unknown
;hard;no vaild recipients were found for this message
;hard;This user doesn.t have a yahoo.de account
Sourceforge Bugreport #2620217;hard;^    Unrouteable address
#
#
#	Softbounces
;soft;^Mailbox size exceeded - Mailbox voll
;soft;^This message is looping:
;soft;^.* User.?s Disk Quota exceeded
;soft;^.* \.\.\. 550 Mailbox quota exceeded / Mailbox voll\.
;soft;^since the mailbox size of the recipient was exceeded!
;soft;Message could not be delivered for [0-9][0-9] hours - returned\.
;soft;\.\.\. Benutzer hat das Speichervolumen ueberschritten
;soft;.* The intended recipient.s mailbox is full\.
;soft;^Sorry, i couldn.t find any host by that name
;soft;^<.*>: message rejected by recipient, comment: .Quota not
;soft;The following mail address is unreachable: <.*>
;soft;mailbox is full \(MTA-imposed quote exceeded while writing to file .*\):
;soft;Returned mail: Mailbox full
;soft;output: .*: Over quota
;soft;the recipient mailbox is full
;soft;552 RCPT TO:<.*> Mailbox disk quota
;soft;Quota exceeded. The recipients mailbox is full.
;soft;^User mailbox exceeds allowed size: .*
## RESET
