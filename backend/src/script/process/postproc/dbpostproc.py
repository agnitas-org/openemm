#!/usr/bin/env python2
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
#	-*- mode: python; mode: fold -*-
#
import	sys
import	agn, eagn
#
def updateDatabase (db):
	rc = True
	return rc

def main ():
	exitCode = 0
	db = eagn.DB ()
	if db.isopen ():
		if not updateDatabase (db):
			exitCode = 1
		db.commit (exitCode == 0)
		db.close ()
	else:
		agn.log (agn.LV_ERROR, 'db', 'Failed to setup database')
		exitCode = 1
	db.done ()
	if exitCode:
		sys.exit (exitCode)

if __name__ == '__main__':
	main ()
