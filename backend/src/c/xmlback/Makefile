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

CFLAGS	= '-DEMM_VERSION="OpenEMM 21.04.000.118"' -I. -I../lib -Iinclude
LDFLAGS	= -L. -L../lib -Llib
LIBS	= -lagn -lslang -lopendkim -lbsd -lresolv -llua -lssl -lcrypto -lxml2 -lz -lm
SRCS	= entity.c xmlback.c parse.c create.c replace.c modify.c protect.c convert.c append.c \
	  blockmail.c block.c tagpos.c tracker.c mailtypedefinition.c links.c blockspec.c fix.c \
	  postfix.c dcache.c receiver.c tag.c dyn.c media.c columns.c url.c link_resolve.c field.c dataset.c encrypt.c mailtrack.c \
	  codec.c eval.c counter.c rblock.c misc.c sqllike.c data.c none.c \
	  generate.c count.c preview.c head.c \
	  strmap.c dkim.c uid.c alua.c tfunc.c tflua.c \
	  grammar/parse.c grammar/transform.c
OBJS	= $(SRCS:%.c=%.o)
LOBJS	= luatc.o $(shell echo $(OBJS) | tr ' ' '\n' | egrep -v 'tflua.o|xmlback.o')

all:	xmlback luatc

xmlback:	libs $(OBJS) ../lib/libagn.a
	$(CC) $(LDFLAGS) -o $@ $(OBJS) $(LIBS)

luatc:		libs $(LOBJS) ../lib/libagn.a
	$(CC) $(LDFLAGS) -o $@ $(LOBJS) $(LIBS)

xmlback.o:	xmlback.c Makefile

../lib/libagn.a:
	$(MAKE) -C ../lib `basename $@`

libs:	lib include lib/libslang.a lib/liblua.a lib/libssl.a lib/libcrypto.a lib/libz.a lib/libxml2.a lib/libbsd.a lib/libopendkim.a

lib include:
	[ -d $@ ] || mkdir $@
lib/libslang.a:	slang-1.4.9.tar.gz slang-1.4.9.patch
	tar xaf slang-1.4.9.tar.gz && ( cd slang-1.4.9; patch -p1 < ../slang-1.4.9.patch; ./configure; make; mv src/objs/libslang.a ../lib; mv src/slang.h ../include; cd ..; rm -rf slang-1.4.9 )
lib/liblua.a:	lua-5.3.5.tar.gz
	tar xaf lua-5.3.5.tar.gz && ( cd lua-5.3.5; make linux; mv src/liblua.a ../lib; mv src/lauxlib.h src/lua.h src/luaconf.h src/lualib.h ../include; cd ..; rm -rf lua-5.3.5 )
lib/libssl.a lib/libcrypto.a:	openssl-1.0.1j.tar.gz
	tar xzf openssl-1.0.1j.tar.gz && ( cd openssl-1.0.1j; ./config no-shared no-dso --prefix=`dirname \`pwd\`` --openssldir=/home/openemm//etc/openssl; make; mv libcrypto.a libssl.a ../lib; rm -rf ../include/openssl; mkdir ../include/openssl; cp include/openssl/*.h ../include/openssl; cd ..; rm -rf rm -rf openssl-1.0.1j )
lib/libz.a:	zlib-1.2.3.tar.gz
	tar xzf zlib-1.2.3.tar.gz && ( cd zlib-1.2.3; ./configure --prefix=`dirname \`pwd\``; make; mv libz.a ../lib; mv zlib.h zconf.h ../include; cd ..; rm -rf zlib-1.2.3 )
lib/libxml2.a:	libxml2-2.7.6.tar.gz lib/libz.a
	tar xzf libxml2-2.7.6.tar.gz && ( cd libxml2-2.7.6; ./configure --prefix=`dirname \`pwd\`` --disable-shared --without-threads --without-ftp --without-html --without-http --with-zlib=`dirname \`pwd\``; make; mv .libs/libxml2.a ../lib; rm -rf ../include/libxml; mv  include/libxml ../include; cd ..; rm -rf libxml2-2.7.6 )
lib/libbsd.a:	libbsd-0.6.0.tar.gz
	tar xzf libbsd-0.6.0.tar.gz && ( cd libbsd-0.6.0; ./configure --prefix=`dirname \`pwd\`` --disable-shared; make; mv src/.libs/libbsd.a ../lib; rm -rf ../include/bsd; mv include/bsd ../include; cd ..; rm -rf libbsd-0.6.0 )
lib/libopendkim.a:	opendkim-2.10.3.tar.gz lib/libbsd.a
	tar xzf opendkim-2.10.3.tar.gz && ( cd opendkim-2.10.3; patch -p1 < ../opendkim-2.10.3.patch; env "CFLAGS=-I`dirname \`pwd\``/include" "LDFLAGS=-L`dirname \`pwd\``/lib" ./configure "--prefix=`dirname \`pwd\``" --disable-shared --disable-filter --without-milter; make; mv libopendkim/.libs/libopendkim.a ../lib; mkdir -p ../include/opendkim; mv libopendkim/dkim.h ../include/opendkim; cd ..; rm -rf opendkim-2.10.3 )

grammar/parse.c:	grammar/parse.y grammar/lemon
	./grammar/lemon -c -s $<

grammar/lemon:		grammar/lemon.c
	$(CC) $< -o $@

clean:
	rm -f $(OBJS) grammar/parse.c grammar/parse.h grammar/parse.out grammar/lemon xmlback luatc luatc.o
	rm -rf lib include
