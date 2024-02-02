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
CFLAGS	= -I../lib
LDFLAGS	= -L../lib
LIBS	= -lagn

SRCS1	= cquery.c
OBJS1	= $(SRCS1:%.c=%.o)
SRCS2	= pathstrip.c
OBJS2	= $(SRCS2:%.c=%.o)

all:	config-query pathstrip

config-query:	$(OBJS1) ../lib/libagn.a
	$(CC) $(LDFLAGS) -o $@ $(OBJS1) $(LIBS)

pathstrip:	$(OBJS2)
	$(CC) $(LDFLAGS) -o $@ $(OBJS2)

../lib/libagn.a:
	@$(MAKE) -C ../lib `basename $@`

clean:
	rm -f $(OBJS1) $(OBJS2) config-query pathstrip
