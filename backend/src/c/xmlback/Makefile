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

CFLAGS	= '-DEMM_VERSION="OpenEMM 20.10.000.126"' -I. -I../lib -I/usr/include/libxml2
LDFLAGS	= -L. -L../lib
LIBS	= -lagn -lslang -llua -lssl -lcrypto -lxml2 -lz -ldl -lm
SRCS	= entity.c xmlback.c parse.c create.c replace.c modify.c protect.c convert.c append.c \
	  blockmail.c block.c tagpos.c tracker.c mailtypedefinition.c links.c blockspec.c fix.c \
	  postfix.c dcache.c receiver.c tag.c dyn.c media.c columns.c url.c link_resolve.c field.c dataset.c encrypt.c mailtrack.c \
	  codec.c eval.c counter.c rblock.c misc.c sqllike.c data.c none.c \
	  generate.c count.c preview.c head.c \
	  strmap.c uid.c alua.c tfunc.c tflua.c \
	  grammar/parse.c grammar/transform.c
OBJS	= $(SRCS:%.c=%.o)
LOBJS	= luatc.o $(shell echo $(OBJS) | tr ' ' '\n' | egrep -v 'tflua.o|xmlback.o')

all:	xmlback luatc

xmlback:	libslang.a liblua.a libssl.a libcrypto.a $(OBJS) ../lib/libagn.a
	$(CC) $(LDFLAGS) -o $@ $(OBJS) $(LIBS)
	strip -s $@

luatc:		libslang.a liblua.a libssl.a libcrypto.a $(LOBJS) ../lib/libagn.a
	$(CC) $(LDFLAGS) -o $@ $(LOBJS) $(LIBS)
	strip -s $@

xmlback.o:	xmlback.c Makefile

../lib/libagn.a:
	$(MAKE) -C ../lib `basename $@`

libslang.a:	slang-1.4.9.tar.gz slang-1.4.9.patch
	tar xaf slang-1.4.9.tar.gz && ( cd slang-1.4.9; patch -p1 < ../slang-1.4.9.patch; ./configure; make; mv src/objs/libslang.a src/slang.h ..; cd ..; rm -rf slang-1.4.9 )
liblua.a:	lua-5.3.5.tar.gz
	tar xaf lua-5.3.5.tar.gz && ( cd lua-5.3.5; make linux; mv src/liblua.a src/lauxlib.h src/lua.h src/luaconf.h src/lualib.h ..; cd ..; rm -rf lua-5.3.5 )
libssl.a libcrypto.a:	openssl-1.0.1j.tar.gz
	tar xzf openssl-1.0.1j.tar.gz && ( cd openssl-1.0.1j; ./config no-shared --prefix=`dirname \`pwd\`` --openssldir=/home/openemm//etc/openssl; make; mv libcrypto.a libssl.a ..; rm -rf ../openssl; mkdir ../openssl; cp include/openssl/* ../openssl; cd ..; rm -rf rm -rf openssl-1.0.1j )

grammar/parse.c:	grammar/parse.y grammar/lemon
	./grammar/lemon -c -s $<

grammar/lemon:		grammar/lemon.c
	$(CC) $< -o $@

clean:
	rm -f $(OBJS) grammar/parse.c grammar/parse.h grammar/parse.out grammar/lemon xmlback luatc luatc.o
	rm -f slang.h libslang.a lauxlib.h lua.h luaconf.h lualib.h liblua.a libssl.a libcrypto.a
	rm -rf openssl
