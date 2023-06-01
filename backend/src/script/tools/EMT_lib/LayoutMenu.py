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
import os

from EMT_lib.Environment import Environment
from EMT_lib import Colors
from EMT_lib import DbConnector

def layoutImagesTableMenuAction(actionParameters):
	layoutImageNames = []
	result = DbConnector.select("SELECT item_name FROM layout_tbl")
	for row in result:
		layoutImageNames.append(row[0])

	print("Images available:")
	if len(layoutImageNames) > 0:
		maxNameLength = len(max(layoutImageNames, key=len))
		for imageName in layoutImageNames:
			if "favicon.ico" == imageName:
				print(f" {imageName}{' '*(maxNameLength - len(imageName))}\t{Colors.GREEN}(the icon often displayed in the browsers tab, besides the title){Colors.DEFAULT}")
			elif "logo.svg" == imageName:
				print(f" {imageName}{' '*(maxNameLength - len(imageName))}\t{Colors.GREEN}(small logo in the webapplication above the menu and login mask){Colors.DEFAULT}")
			elif "logo.png" == imageName:
				print(f" {imageName}{' '*(maxNameLength - len(imageName))}\t{Colors.GREEN}(an alternative to the SVG logo){Colors.DEFAULT}")
			elif "edition_logo.png" == imageName:
				print(f" {imageName}{' '*(maxNameLength - len(imageName))}\t{Colors.GREEN}(logo in login mask){Colors.DEFAULT}")
			elif "report_logo.png" == imageName:
				print(f" {imageName}{' '*(maxNameLength - len(imageName))}\t{Colors.GREEN}(logo used inside pdf reports){Colors.DEFAULT}")
			else:
				print(" " + imageName)

		print()

		print("Please choose layout image to replace (Blank => Back):")

		choice = input(" > ").strip()

		if choice == "":
			return
		elif choice in layoutImageNames:
			imageName = choice
			print("Please enter new image filepath for replacement of '" + imageName + "': ")
			imageFilePath = input(" > ").strip()
			if os.path.isfile(imageFilePath):
				with open(imageFilePath,'rb') as imageFile:
					fileData = imageFile.read()
					DbConnector.update("UPDATE layout_tbl SET data = ? WHERE item_name = ?", fileData, imageName)

				Environment.messages.append("Image '" + imageName + "' replaced")
				Environment.rebootNeeded = True
				return True
			else:
				Environment.errors.append("Invalid image filepath: " + imageFilePath)
				return False
		else:
			Environment.errors.append("Invalid image name: " + choice)
			return False
	else:
		print(" <no layout images available>")
		print()
		print("(Blank => Back):")
		choice = input(" > ").strip()
