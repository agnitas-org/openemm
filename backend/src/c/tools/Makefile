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
#	-*- sh -*-
#

CFLAGS	= -I../lib
LDFLAGS	= -L../lib
LIBS	= -lagn

SRCS1	= qctrl.c qlib.c qmove.c qstat.c qflush.c
OBJS1	= $(SRCS1:%.c=%.o)
SRCS2	= smctrl.c
OBJS2	= $(SRCS2:%.c=%.o)
SRCS3	= wrap.c
OBJS3	= $(SRCS3:%.c=%.o)
SRCS4	= cquery.c
OBJS4	= $(SRCS4:%.c=%.o)

all:	qctrl smctrl wrap config-query

qctrl:	$(OBJS1) ../lib/libagn.a
	$(CC) $(LDFLAGS) -o $@ $(OBJS1) $(LIBS)
	strip -s $@

smctrl:	$(OBJS2) ../lib/libagn.a
	$(CC) $(LDFLAGS) -o $@ $(OBJS2) $(LIBS) -lutil
	strip -s $@

wrap:	$(OBJS3) ../lib/libagn.a
	$(CC) $(LDFLAGS) -o $@ $(OBJS3) $(LIBS)
	strip -s $@

config-query:	$(OBJS4) ../lib/libagn.a
	$(CC) $(LDFLAGS) -o $@ $(OBJS4) $(LIBS)
	strip -s $@

../lib/libagn.a:
	@$(MAKE) -C ../lib `basename $@`

clean:
	rm -f $(OBJS1) $(OBJS2) $(OBJS3) $(OBJS4) qctrl smctrl wrap config-query
