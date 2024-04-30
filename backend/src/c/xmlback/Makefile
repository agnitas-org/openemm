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
CFLAGS	= -I. -I../lib -I../lib/include
LDFLAGS	= -L. -L../lib -L../lib/lib
LIBS	= -lagn -lslang -lopendkim -lbsd -lresolv -llua -lmpack -lssl -lcrypto -lxml2 -lz -lm -ldl
SRCS	= entity.c xmlback.c parse.c create.c replace.c modify.c protect.c html.c convert.c append.c \
	  blockmail.c block.c tagpos.c tracker.c mailtypedefinition.c links.c blockspec.c fix.c \
	  postfix.c dcache.c receiver.c tag.c dyn.c media.c columns.c url.c link_resolve.c field.c dataset.c encrypt.c mailtrack.c \
	  codec.c eval.c counter.c rblock.c misc.c sqllike.c none.c \
	  generate.c count.c preview.c head.c \
	  strmap.c dkim.c spf.c uid.c tfunc.c tflua.c \
	  grammar/parse.c grammar/transform.c
OBJS	= $(SRCS:%.c=%.o)
LOBJS	= luatc.o $(shell echo $(OBJS) | tr ' ' '\n' | egrep -v 'tflua.o|xmlback.o')

all:	xmlback luatc

xmlback:	$(OBJS) ../lib/libagn.a
	$(CC) $(LDFLAGS) -o $@ $(OBJS) $(LIBS)

luatc:		$(LOBJS) ../lib/libagn.a
	$(CC) $(LDFLAGS) -o $@ $(LOBJS) $(LIBS)

xmlback.o:	xmlback.c Makefile

../lib/libagn.a:
	$(MAKE) -C ../lib `basename $@`

grammar/parse.c:	grammar/parse.y grammar/lemon
	./grammar/lemon -c -s $<

grammar/lemon:		grammar/lemon.c
	$(CC) $< -o $@

clean:
	rm -f $(OBJS) grammar/parse.c grammar/parse.h grammar/parse.out grammar/lemon xmlback luatc luatc.o
