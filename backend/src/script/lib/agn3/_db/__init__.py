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
import	enum
from	itertools import chain
from	.types import Driver
from	. import mysql, mariadb, oracle, sqlite
#
__all__ = ['Drivers']
#
Drivers = getattr (enum, 'Enum') ('Drivers', ((_v.name, _v) for (_n, _v) in chain (*(_m.__dict__.items () for _m in (mysql, mariadb, oracle, sqlite))) if type (_v) is Driver))
#
# XXX: the getattr(...) is a workaround instead of calling enum.Enum direct to bypass mypy check which results in a false positive
