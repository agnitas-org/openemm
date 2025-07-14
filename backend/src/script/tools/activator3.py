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
import	logging, argparse
from	agn3.definitions import syscfg
from	agn3.emm.activator import Activator
from	agn3.runtime import CLI
#
logger = logging.getLogger (__name__)
#
class Main (CLI):
	__slots__ = ['activator', 'action', 'system_config', 'services']
	def setup (self) -> None:
		self.activator = Activator ()

	def cleanup (self, success: bool) -> None:
		self.activator.close ()

	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		parser.add_argument (
			'-s', '--show',
			action = 'store_true',
			help = 'show current status for selected or all services'
		)
		parser.add_argument (
			'-a', '-e', '--activate', '--enable',
			action = 'store_true',
			help = 'activate/enable services'
		)
		parser.add_argument (
			'-d', '--deactivate', '--disable',
			action = 'store_true',
			help = 'deactivate/disable services'
		)
		parser.add_argument (
			'-r', '--remove',
			action = 'store_true',
			help = 'remove services'
		)
		parser.add_argument (
			'-S', '--syscfg', '--system-config',
			action = 'store',
			help = 'additional check if the given key is set (true) in the local system configuration'
		)
		parser.add_argument (
			'services', nargs = '*',
			help = 'service(s) to manage'
		)

	def use_arguments (self, args: argparse.Namespace) -> None:
		self.action = self.activator.check
		if args.show:
			self.action = self.activator.show
		elif args.activate:
			self.action = self.activator.activate
		elif args.deactivate:
			self.action = self.activator.deactivate
		elif args.remove:
			self.action = self.activator.remove
		self.system_config = args.syscfg
		self.services = args.services

	def executor (self) -> bool:
		if self.system_config is not None and not syscfg.bget (self.system_config, default = False):
			return False
		with self.activator:
			return self.action (self.services)
#
if __name__ == '__main__':
	Main.main ()
