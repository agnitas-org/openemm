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

CFLAGS	= -I.
LDFLAGS	= -L.
LIBS	:= -lagn -lparson
SRCS	= buffer.c var.c log.c node.c map.c csig.c \
	  tzdiff.c atob.c str.c net.c hash.c path.c \
	  set.c skip.c lock.c xml.c daemon.c call.c cache.c \
	  unhex.c which.c timeout.c url.c systemconfig.c \
	  fsdb.c \
	  parson.c
OBJS	= $(SRCS:%.c=%.o)
LIB	= libagn.a

all:	parson.h $(LIB)

clean:
	rm -f $(LIB) $(OBJS) parson.c parson.h
	
$(LIB):	$(OBJS)
	rm -f $@
	ar rc $@ $(OBJS)

parson.c parson.h:	parson-0.0.0.tar.gz
	tar xaf $< && ( cd parson-0.0.0; mv parson.c parson.h ..; cd ..; rm -rf parson-0.0.0 )
	touch -c parson.c parson.h
