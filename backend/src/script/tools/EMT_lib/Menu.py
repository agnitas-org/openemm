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
import sys
import os

from EMT_lib.Environment import Environment
from EMT_lib import Colors
from EMT_lib import DbConnector
from EMT_lib import EMTUtilities
from EMT_lib.License import License

class Menu:
	def __init__(self, title, availabilityCheckFunction = None):
		self.title = title
		self.parentMenu = None
		self.subMenus = []
		self.currentMenuIndex = -1
		self.availabilityCheckFunction = availabilityCheckFunction
		self.action = None
		self.actionParameters = None

	def __str__(self):
		return self.title + " Menu"

	def setParentMenu(self, parentMenu):
		self.parentMenu = parentMenu
		return self

	def addSubMenu(self, subMenu):
		subMenu.setParentMenu(self)
		self.subMenus.append(subMenu)
		return subMenu

	def setAction(self, action):
		self.action = action
		return self

	def show(self, startMenu = None):
		if Environment.overrideNextMenu is not None:
			startMenu = Environment.overrideNextMenu
		while startMenu is not None:
			intermediateStartMenu = startMenu
			while intermediateStartMenu is not None and intermediateStartMenu != self:
				if intermediateStartMenu in self.subMenus:
					break
				else:
					intermediateStartMenu = intermediateStartMenu.parentMenu
			if intermediateStartMenu is not None and intermediateStartMenu != self:
				Environment.overrideNextMenu = None
				intermediateStartMenu.show(startMenu)
				startMenu = None
				if Environment.overrideNextMenu is not None:
					startMenu = Environment.overrideNextMenu
			else:
				break

		while True:
			EMTUtilities.clearTerminalScreen()

			EMTUtilities.printTextInBox(Environment.toolName + " v" + Environment.toolVersion, "=")

			if EMTUtilities.isDebugMode():
				print("Debug mode: On")
			if EMTUtilities.hasRootPermissions():
				print("Root mode: On")
			if Environment.updateChannel is not None:
				print("Update Channel: " + Environment.updateChannel)
			print("Hostname: " + Environment.hostname)

			harddriveProperties = os.statvfs("/tmp")
			hddSize = harddriveProperties.f_frsize * harddriveProperties.f_blocks
			hddFreeSize = harddriveProperties.f_frsize * harddriveProperties.f_bfree
			hddFreePercentage = (hddFreeSize / hddSize) * 100
			if hddFreePercentage < 10:
				color = Colors.RED
			elif hddFreePercentage < 20:
				color = Colors.YELLOW
			else:
				color = Colors.GREEN

			hddFreePercentageString = color + "{:.1f} %".format(hddFreePercentage) + Colors.DEFAULT
			print("Free diskspace: " + hddFreePercentageString + " (of " + "{:.2f}".format(hddSize / 1024 / 1024 / 1024) + " GiB)")

			if Environment.isOpenEmmServer:
				if License.getLicenseName() is not None:
					print("OpenEMM License: " + License.getLicenseName() + " (ID: " + License.getLicenseID() + ")")
				print("OpenEMM Runtime Version: " + Environment.runtimeVersion)
				print("OpenEMM Version: " + Environment.frontendVersion)
				if Environment.manualVersion is not None and Environment.manualVersion != "Unknown":
					print("OpenEMM Manual Version: " + Environment.manualVersion)
			else:
				if License.getLicenseName() is not None:
					print("EMM License: " + License.getLicenseName() + " (ID: " + License.getLicenseID() + ")")
				if Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer:
					print("EMM Runtime Version: " + Environment.runtimeVersion)
				if Environment.isEmmFrontendServer:
					print("EMM Frontend Version: " + Environment.frontendVersion)
				if Environment.isEmmStatisticsServer:
					print("EMM Statistics Version: " + Environment.statisticsVersion)
				if Environment.isEmmWebservicesServer:
					print("EMM Webservices Version: " + Environment.webservicesVersion)
				if Environment.isEmmConsoleRdirServer:
					print("EMM Console-Rdir Version: " + Environment.consoleRdirVersion)
				if Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer:
					print("EMM Backend Version: " + Environment.emmBackendVersion)
				if Environment.isEmmRdirServer:
					if Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer:
						print("EMM Rdir Runtime Version: " + Environment.rdirRuntimeVersion)
					else:
						print("EMM Runtime Version: " + Environment.rdirRuntimeVersion)
					print("EMM Rdir Version: " + Environment.rdirVersion)
					if Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer:
						print("EMM Rdir Backend Version: " + Environment.rdirBackendVersion)
					else:
						print("EMM Backend Version: " + Environment.rdirBackendVersion)
				if Environment.startupBackendVersion is not None:
					print("EMM Startup Backend Version: " + Environment.startupBackendVersion)
				if Environment.isEmmMergerServer:
					print("EMM Merger Backend Version: " + Environment.mergerBackendVersion)
				if Environment.isEmmMailerServer:
					print("EMM Mailer Backend Version: " + Environment.mailerBackendVersion)
				if Environment.isEmmMailloopServer:
					print("EMM Mailloop Backend Version: " + Environment.mailloopBackendVersion)

			print("System-Url: " + Environment.getSystemUrl())

			if DbConnector.getDbClientPath() is None:
				print()
				if DbConnector.emmDbVendor is None:
					print(Colors.YELLOW + "Database is not available, therefore some menu items are not available" + Colors.DEFAULT)
				else:
					print(Colors.RED + "Database client (" + DbConnector.emmDbVendor + ") is missing, therefore some menu items are not available" + Colors.DEFAULT)

			print()

			if len(Environment.messages) > 0:
				for message in Environment.messages:
					print(Colors.BLUE + message + Colors.DEFAULT)
				print()
			if len(Environment.warnings) > 0:
				for warning in Environment.warnings:
					print(Colors.YELLOW + warning + Colors.DEFAULT)
				print()
			if len(Environment.errors) > 0:
				for error in Environment.errors:
					print(Colors.RED + error + Colors.DEFAULT)
				print()

			if Environment.unsavedDbcfgChanges is not None:
				print(Colors.YELLOW + "You made dbcfg changes, that need to be stored to become active." + Colors.DEFAULT + "\n")
			if Environment.unsavedConfigChanges is not None:
				print(Colors.YELLOW + "You made configuration changes, that need to be stored to become active." + Colors.DEFAULT + "\n")
			if Environment.unsavedSystemCfgChanges is not None:
				print(Colors.YELLOW + "You made system.cfg changes, that need to be stored to become active." + Colors.DEFAULT + "\n")
			if Environment.unsavedClientChanges is not None:
				print(Colors.YELLOW + "You made changes to the client data, that need to be stored to become active." + Colors.DEFAULT + "\n")
			if Environment.rebootNeeded:
				print(Colors.YELLOW + "Current configuration changes or updates need the " + Environment.applicationName + " system to be restarted." + Colors.DEFAULT + "\n")
			if Environment.otherSystemsNeedConfig and Environment.multiServerSystem:
				print(Colors.YELLOW + "Please configure other systems accordingly (e.g. RDIR)." + Colors.DEFAULT + "\n")

			Environment.messages = []
			Environment.warnings = []
			Environment.errors = []

			print(Colors.WHITE + "Current menu: " + self.title + Colors.DEFAULT + "\n")

			if self.action is not None:
				self.actionParameters = self.action(self.actionParameters)
				if self.actionParameters is None:
					return
			else:
				if self.parentMenu is None:
					defaultMenuTitle = "Quit"
				else:
					defaultMenuTitle = "Back to " + self.parentMenu.title
				print("Please choose (Blank => " + defaultMenuTitle + "):")
				subMenuIndex = 0
				availableSubMenus = []
				for subMenu in self.subMenus:
					if subMenu.availabilityCheckFunction is None or subMenu.availabilityCheckFunction():
						subMenuIndex = subMenuIndex + 1
						if subMenuIndex < 10:
							itemNumberFiller = " "
						else:
							itemNumberFiller = ""
						print(" " + itemNumberFiller + str(subMenuIndex) + ". " + subMenu.title)
						availableSubMenus.append(subMenu)
					else:
						print("   (" + subMenu.title + ") " + Colors.YELLOW + "not available" + Colors.DEFAULT)
				print("  0. " + defaultMenuTitle)

				choice = input(" > ")
				choice = choice.strip()

				if choice == "":
					return
				elif choice.lower() == "q" or choice.lower() == "quit" or choice.lower() == "exit":
					EMTUtilities.clearTerminalScreen()
					sys.exit(0)
				else:
					try:
						choice = int(choice)
					except:
						Environment.errors.append("Invalid input: " + choice)
						continue

					if choice == 0:
						return
					elif 1 <= choice and choice <= len(availableSubMenus):
						availableSubMenus[choice - 1].show()
					else:
						Environment.errors.append("Invalid input: " + str(choice))
