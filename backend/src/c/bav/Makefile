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

CFLAGS	= -I. -I../lib -pthread
LDFLAGS	= -L. -L../lib -pthread
LIBS	= -lagn
SRCS1	= bav.c cfg.c
OBJS1	= $(SRCS1:%.c=%.o)
#
all:	bav

bav:	libmilter.a $(OBJS1) ../lib/libagn.a
	$(CC) $(LDFLAGS) $(OBJS1) -o $@ $(LIBS) -lmilter

../lib/libagn.a:
	$(MAKE) -C ../lib `basename $@`

libmilter.a:	sendmail-8.13.8.tar.gz
	tar xaf $< && ( cd sendmail-8.13.8/libmilter; sh Build; cd ..; cp -r obj.*/libmilter/libmilter.a include/libmilter ..; cd ..; rm -rf sendmail-8.13.8 )

clean:
	rm -rf $(OBJS1) bav libmilter.a libmilter
