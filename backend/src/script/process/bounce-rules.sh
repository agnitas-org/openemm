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
import	argparse, re
from	typing import Optional
from	typing import Dict, List, Tuple
from	agn3.db import DB, Row
from	agn3.emm.companyconfig import CompanyConfig
from	agn3.exceptions import error
from	agn3.ignore import Ignore
from	agn3.parameter import Parameter
from	agn3.parser import Line, Field, Lineparser
from	agn3.runtime import CLI
from	softbounce3 import Softbounce
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
			parser = Lineparser (
				lambda l: l.split (';', 4),
				'name',
				'description',
				Field ('dsn', int),
				Field ('detail', int),
				Field ('pattern', lambda v: v if v else None)
			)
			pattern_parameter = re.compile ('^## *PARAMETER: *(.*)$')
			pattern_start = re.compile ('^## *RULES$')
			start_found = False
			parameter: Optional[Parameter] = None
			rules: List[Line] = []
			for (lineno, line) in enumerate ((_l.strip () for _l in fd), start = 1):
				if not start_found:
					match = pattern_parameter.match (line)
					if match is not None:
						parameter = Parameter (match.group (1))
					else:
						start_found = pattern_start.match (line) is not None
				elif line and not line.startswith ('#'):
					try:
						rules.append (parser (line))
					except error as e:
						print (f'Failed to parse {self.caller[0]}:{lineno}:{line} due to: {e}')
						ok = False
			if ok:
				with DB () as db:
					if parameter is not None:
						orig = Parameter ()
						ccfg = CompanyConfig ()
						description = 'written'
						with Ignore (KeyError):
							orig.loads (ccfg.get_company_info (Softbounce.conversion_name))
							description = 'updated'
						description += ' by EMM backend update release'
						target = Parameter ()
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
						if orig != target:
							if self.dryrun:
								print (f'Would update parameter from {orig} to {target} using {parameter}')
							else:
								if not ccfg.write_company_info (
									0, Softbounce.conversion_name, target.dumps (),
									description = description
								):
									print ('Failed writing changed parameter to database')
									ok = False
						elif self.dryrun:
							print ('Parameter not changed')
					#
					current: Dict[Tuple[int, int, Optional[str]], Row] = {}
					rulekey: Tuple[int, int, Optional[str]]
					for row in db.query (
						'SELECT rule_id, dsn, detail, pattern, shortname, description '
						'FROM bounce_translate_tbl '
						'WHERE company_id = 0 AND active = 1'
					):
						rulekey = (row.dsn, row.detail, row.pattern)
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
							'       (bounce_translate_tbl_seq.nextval, 0, :dsn, :detail, :pattern, 1, :shortname, :description, current_timestamp, current_timestamp)'
						), mysql = (
							'INSERT INTO bounce_translate_tbl '
							'       (company_id, dsn, detail, pattern, active, shortname, description, creation_date, change_date) '
							'VALUES '
							'       (0, :dsn, :detail, :pattern, 1, :shortname, :description, current_timestamp, current_timestamp)'
						)
					)
					query_disable = 'UPDATE bounce_translate_tbl SET active = 0, change_date = current_timestamp WHERE rule_id = :rule_id'
					query_update_name = 'UPDATE bounce_translate_tbl SET shortname = :shortname, change_date = current_timestamp WHERE rule_id = :rule_id'
					query_update_desc = 'UPDATE bounce_translate_tbl SET description = :description, change_date = current_timestamp WHERE rule_id = :rule_id'
					for rule in rules:
						rulekey = (rule.dsn, rule.detail, rule.pattern)
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
							if self.dryrun:
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
					if failed:
						ok = False
					if self.dryrun or new or disabled or updated or failed:
						print (f'Added {new} rules, disable {disabled} rules, update {updated} rules where {failed} failed and found {exists} already existing rules')
					db.sync (bool (not self.dryrun and (new or disabled or updated) and ok))
		return ok

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
#
#
#
## PARAMETER: convert-bounce-count="40", convert-bounce-duration="30", last-click="30", last-open="30", max-age-create="180", max-age-change="60", fade-out="14"
# 
## RULES
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
EMM-7409;;500;511;stat="5.1.0 Address rejected.", relay=".man.eu."
EMM-7389;;530;511;stat="No such user"
EMM-7379 GMX;;500;511;stat="mailbox unavailable", relay=".gmx.net."
EMM-7379 WEB;;500;511;stat="mailbox unavailable", relay=".web.de."
EMM-7379 KDN;;500;511;stat="mailbox unavailable", relay=".kundenserver.de."
EMM-7378;;500;511;stat="invalid mailbox", relay=".mail.ru."
EMM-7492;;520;511;stat="no such mailbox", relay=".rzone.de."
EMM-7491;;550;511;stat="no such recipient"
EMM-7490;;520;511;stat="mailbox unavailable", relay=".rmx.de."
EMM-7593;;500;511;stat="Not a valid recipient", relay=".yahoo"
EMM-7594;;500;511;stat="mailbox not found|unknown adress|unrouteable adress"
EMM-7489;;550;511;stat="mailbox unavailable", relay=".protection.outlook.com."
EMM-7489;;550;511;stat="mailbox unavailable", relay=".retarus.com."
EMM-7743;;541;511;stat="Recipient address rejected", relay=".protection.outlook.com."
EMM-7691;;500;511;stat="No such user"
#
# outdated rules
#
# is covered by 50x
-;;500;400;
# is covered by EMM-7594
-EMM-7315;;500;511;stat="mailbox not found", relay=".aon.at."
