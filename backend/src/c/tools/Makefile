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

CFLAGS	= -I../lib
LDFLAGS	= -L../lib
LIBS	= -lagn

SRCS1	= qctrl.c qlib.c qmove.c qstat.c qflush.c
OBJS1	= $(SRCS1:%.c=%.o)
SRCS2	= smctrl.c
OBJS2	= $(SRCS2:%.c=%.o)
SRCS3	= cquery.c
OBJS3	= $(SRCS3:%.c=%.o)
SRCS4	= pathstrip.c
OBJS4	= $(SRCS4:%.c=%.o)

all:	qctrl smctrl config-query pathstrip

qctrl:	$(OBJS1) ../lib/libagn.a
	$(CC) $(LDFLAGS) -o $@ $(OBJS1) $(LIBS)

smctrl:	$(OBJS2) ../lib/libagn.a
	$(CC) $(LDFLAGS) -o $@ $(OBJS2) $(LIBS) -lutil

config-query:	$(OBJS3) ../lib/libagn.a
	$(CC) $(LDFLAGS) -o $@ $(OBJS3) $(LIBS)

pathstrip:	$(OBJS4)
	$(CC) $(LDFLAGS) -o $@ $(OBJS4)

../lib/libagn.a:
	@$(MAKE) -C ../lib `basename $@`

clean:
	rm -f $(OBJS1) $(OBJS2) $(OBJS3) $(OBJS4) qctrl smctrl config-query pathstrip
