#!/bin/sh
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
. $HOME/scripts/config.sh
#
if py3available ; then
	script=/var/tmp/bounce-rules3.py
	cat << __EOF__ > $script
#!/usr/bin/env python3
#
import	argparse, re, json, difflib
from	typing import Any, Callable, Literal, Optional
from	typing import Dict, List, Set, Tuple
from	agn3.db import DB, Row
from	agn3.emm.bounce import Bounce
from	agn3.emm.build import spec
from	agn3.emm.companyconfig import CompanyConfig
from	agn3.exceptions import error
from	agn3.ignore import Ignore
from	agn3.parameter import Parameter
from	agn3.parser import Line, Field, Lineparser
from	agn3.runtime import CLI
from	agn3.stream import Stream
from	update3 import UpdateBounce
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
				'description',
				Field ('dsn', int),
				Field ('detail', int),
				Field ('pattern', lambda v: v if v else None)
			)
			bounce_keymaker = lambda l: (l.dsn, l.detail, l.pattern)
			bav_parser = Lineparser (
				lambda l: l.split (';', 2),
				'name',
				'section',
				'pattern'
			)
			bav_keymaker = lambda l: (l.section, l.pattern)
			#
			pattern_parameter = re.compile ('^## *PARAMETER: *(.*)$')
			pattern_bounce_rules = re.compile ('^## *BOUNCE-RULES$')
			pattern_bav_rules = re.compile ('^## *BAV-RULES$')
			section: Literal[
				None,
				'bounce',
				'bav'
			] = None
			parameter: Optional[Parameter] = None
			bounce_rules: List[Line] = []
			bav_rules: List[Line] = []
			for (lineno, line) in enumerate ((_l.strip () for _l in fd), start = 1):
				if (match := pattern_parameter.match (line)) is not None:
					parameter = Parameter (match.group (1))
					continue
				if pattern_bounce_rules.match (line) is not None:
					section = 'bounce'
					continue
				if pattern_bav_rules.match (line) is not None:
					section = 'bav'
					continue
				if section is None or not line or line.startswith ('#'):
					continue
				#
				try:
					if section == 'bounce':
						bounce_rules.append (bounce_parser (line))
					elif section == 'bav':
						bav_rules.append (bav_parser (line))
				except error as e:
					print (f'Failed to parse {self.caller[0]}:{lineno}:{line} due to: {e}')
					ok = False
			if ok:
				with DB () as db:
					if parameter is not None and not self.update_bounce_configuration (db, parameter):
						ok = False
					if not self.update_bounce_rules (db, self.sort_list (bounce_rules, bounce_keymaker), bounce_parser, bounce_keymaker):
						ok = False
					if not self.update_bav_rules (db, self.sort_list (bav_rules, bav_keymaker), bav_parser, bav_keymaker):
						ok = False
					db.sync (not self.dryrun and ok)
		return ok

	def update_bounce_configuration (self, db: DB, parameter: Parameter) -> bool:
		rc = True
		orig = Parameter ()
		ccfg = CompanyConfig ()
		description = 'written'
		with Ignore (KeyError):
			orig.loads (ccfg.get_company_info (Bounce.name_company_info_conversion))
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
				if not ccfg.write_company_info (
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

	def sort_list (self, rules: List[Line], keymaker: Callable[[Line], Any]) -> List[Line]:
		delete_rules: List[Line] = Stream (rules).filter (lambda r: bool (r.name.startswith ('-'))).list ()
		if delete_rules:
			new_rules: List[Line] = []
			for rule in rules:
				if not rule.name.startswith ('-'):
					rulekey = keymaker (rule)
					for to_delete in delete_rules:
						if rulekey == keymaker (to_delete):
							if self.dryrun:
								print (f'Drop rule {rule} due to deletion rule {to_delete}')
							break
					else:
						new_rules.append (rule)
			return delete_rules + new_rules
		return rules

	def update_bounce_rules (self, db: DB, rules: List[Line], parser: Lineparser, keymaker: Callable[[Line], Any]) -> bool:
		current: Dict[Tuple[int, int, Optional[str]], Row] = {}
		for row in db.query (
			'SELECT rule_id, dsn, detail, pattern, shortname, description '
			'FROM bounce_translate_tbl '
			'WHERE company_id = 0 AND active = 1'
		):
			rulekey = keymaker (parser.make (dsn = row.dsn, detail = row.detail, pattern = row.pattern if row.pattern else None))
			if rulekey in current:
				if self.dryrun:
					print ('{row}: already found entry with same {rulekey}: {entry}'.format (
						row = row,
						rulekey = rulekey,
						entry = current[rulekey]
					))
				else:
					raise error (f'{row}: duplicate record found')
			current[rulekey] = row
		new = 0
		disabled = 0
		updated = 0
		exists = 0
		failed = 0
		query_insert = db.qselect (
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
		)
		query_disable = 'UPDATE bounce_translate_tbl SET active = 0, change_date = CURRENT_TIMESTAMP WHERE rule_id = :rule_id'
		query_update_name = 'UPDATE bounce_translate_tbl SET shortname = :shortname, change_date = CURRENT_TIMESTAMP WHERE rule_id = :rule_id'
		query_update_desc = 'UPDATE bounce_translate_tbl SET description = :description, change_date = CURRENT_TIMESTAMP WHERE rule_id = :rule_id'
		for rule in rules:
			rulekey = keymaker (rule)
			if rule.name.startswith ('-'):
				if rulekey in current:
					row = current[rulekey]
					disabled += 1
					if self.dryrun:
						print (f'Would disable outdated rule {rule} {row}')
					else:
						if db.update (query_disable, {'rule_id': row.rule_id}) != 1:
							print (f'Failed to disable outdated rule {rule} using {row.rule_id}')
							failed += 1
			elif rulekey in current:
				row = current[rulekey]
				exists += 1
				if self.dryrun:
					print (f'Rule {rule} already exists in database')
				if (not row.shortname and rule.name) or (not row.description and rule.description):
					updated += 1
				if not row.shortname and rule.name:
					if self.dryrun:
						print (f'Rule {rule} would set name to {rule.name}')
					else:
						if db.update (query_update_name, {'rule_id': row.rule_id, 'shortname': rule.name}) != 1:
							print (f'Failed to set name for rule {rule} using {row.rule_id}')
							failed += 1
				if not row.description and rule.description:
					if self.dryrun:
						print (f'Rule{rule} would set description to {rule.description}')
					else:
						if db.update (query_update_desc, {'rule_id': row.rule_id, 'description': rule.description}) != 1:
							print (f'Failed to set description for rule {rule} using {row.rule_id}')
							failed += 1
			else:
				new += 1
				if rule.pattern and not UpdateBounce.Translate.Pattern (rule.pattern, False).valid:
					print (f'Failed to validate pattern for {rule}, mark as failed')
					failed += 1
				elif self.dryrun:
					print (f'Would insert new rule {rule}')
				else:
					if db.update (query_insert, {
						'dsn': rule.dsn,
						'detail': rule.detail,
						'pattern': rule.pattern,
						'shortname': rule.name,
						'description': rule.description
					}) != 1:
						print (f'Failed to insert new rule {rule}')
						failed += 1
		if self.dryrun or new or disabled or updated or failed:
			print (f'Added {new} rules, disable {disabled} rules, update {updated} rules where {failed} failed and found {exists} already existing active rules')
		return failed == 0

	def update_bav_rules (self, db: DB, rules: List[Line], parser: Lineparser, keymaker: Callable[[Line], Any]) -> bool:
		rc = True
		if db.exists (Bounce.bounce_rule_table):
			with Bounce (db = db) as bounce:
				record = bounce.get_rule (0, 0)
				original = json.dumps (record, indent = 2)
				for rule in rules:
					if rule.name.startswith ('-'):
						if rule.section in record:
							while rule.pattern in record[rule.section]:
								record[rule.section].remove (rule.pattern)
							if not record[rule.section]:
								del record[rule.section]
					else:
						if rule.section not in record or rule.pattern not in record[rule.section]:
							try:
								record[rule.section].append (rule.pattern)
							except KeyError:
								record[rule.section] = [rule.pattern]
				updated = json.dumps (record, indent = 2)
				if original != updated:
					if self.dryrun:
						diff = '\n'.join (
							difflib.context_diff (
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
else
	rc=0
fi
exit $rc
#STOP
#
#
## PARAMETER: convert-bounce-count="35", convert-bounce-duration="30", last-click="30", last-open="30", max-age-create="-", max-age-change="-", fade-out="14", expire="1100", threshold="5"
# 
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
;;511;511;
;;512;512;
;;513;511;
;;516;511;
;;517;511;
;;518;512;
;;521;511;
;;531;510;
;;550;511;stat="unknown recipient"
;;571;410;
;;572;511;
EMM-7389;;530;511;stat="No such user"
EMM-7378;;500;511;stat="invalid mailbox", relay=".mail.ru."
EMM-7492;;520;511;stat="no such mailbox", relay=".rzone.de."
EMM-7491;;550;511;stat="no such recipient"
EMM-7593;;500;511;stat="Not a valid recipient", relay=".yahoo"
EMM-7743;;541;511;stat="Recipient address rejected", relay=".protection.outlook.com."
EMM-7691;;500;511;stat="No such user"
EMM-7816;;417;0;
EMM-7816;;418;0;
EMM-7816;;517;0;
EMM-7816;;518;0;
EMM-7990;;500;511;stat="no mailbox here"
EMM-7991;;5;511;relay="void.blackhole.mx."
EMM-7901;;500;511;stat="user unknown|unknown user"
EMM-7594;;500;511;stat="mailbox not found|unknown add?ress|unrouteable add?ress"
EMM-7379;;500;511;stat="mailbox unavailable", relay=".rmx.de.|.protection.outlook.com.|.retarus.com."
EMM-7409;;500;511;stat="5.1.0 Address rejected."
EMM-8045;;510;511;stat="user unknown|unknown user"
EMM-8045;;530;511;stat="user unknown|unknown user"
EMM-8046;;500;511;stat="Es gibt keine Person mit diesem Namen unter dieser Adresse", relay=".sp-server.net."
EMM-8044;;530;511;stat="user does not exist"
EMM-8028;;571;511;stat="user does not exist"
EMM-8043;;546;512;stat="mail for .* loops back to myself"
EMM-8027;;500;511;stat="no such recipient here|recipient unknown|invalid recipient|no valid recipient"
EMM-8030;;500;511;stat="Mailaddress is administratively disabled"
EMM-8029;;500;511;stat="mailbox for .* does not exist"
EMM-8135;;474;513;stat="TLS is required, but was not offered"
EMM-8617;;500;511;stat="No such local user", relay=".firemail.de."
EMM-8629;;550;511;stat="recipient rejected"
EMM-8634;;571;511;stat="user unknown|no such user", relay=".vol.at.|.yandex.ru."
EMM-8628;;500;511;stat="user suspended"
EMM-8637;;500;511;stat="address unknown"
EMM-8636;;500;511;stat="user not found"
EMM-8638;;500;511;stat="unkown user", relay=".sil.at.|.silverserver.at."
EMM-7490;;520;511;stat="mailbox.*unavailable"
EMM-7490;;550;511;stat="mailbox.*unavailable"
EMM-8635;;571;511;stat="User email address is marked as invalid", relay=".ppe-hosted.com."
EMM-8618;;500;511;stat="max message size exceeded", relay="mx.tim.it."
EMM-7743;;541;511;stat="Recipient address rejected", relay=".protection.outlook.com.|.eo.outlook.com.|.protection.office365.us."
#
# outdated rules
#
# is covered by 50x
-;;500;400;
# is covered by EMM-7594
-EMM-7315;;500;511;stat="mailbox not found", relay=".aon.at."
# is covered by EMM-7816
-;;517;511;
-;;518;512;
# updated rules by new definitions
-EMM-7901;;500;511;stat="user unknown"
-EMM-7594;;500;511;stat="mailbox not found|unknown adress|unrouteable adress"
-EMM-7379 GMX;;500;511;stat="mailbox unavailable", relay=".gmx.net."
-EMM-7379 WEB;;500;511;stat="mailbox unavailable", relay=".web.de."
-EMM-7379 KDN;;500;511;stat="mailbox unavailable", relay=".kundenserver.de."
-EMM-7490;;520;511;stat="mailbox unavailable", relay=".rmx.de."
-EMM-7489;;550;511;stat="mailbox unavailable", relay=".protection.outlook.com."
-EMM-7489;;550;511;stat="mailbox unavailable", relay=".retarus.com."
-EMM-7409;;500;511;stat="5.1.0 Address rejected.", relay=".man.eu."
-EMM-7490;;520;511;stat="mailbox unavailable"
-EMM-7490;;550;511;stat="mailbox unavailable"
-EMM-8618;;500;511;stat="max message size exceeded", relay=".alice.it."
-EMM-7743;;541;511;stat="Recipient address rejected", relay=".protection.outlook.com."
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
