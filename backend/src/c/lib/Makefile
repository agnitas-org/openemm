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
CFLAGS	= -I. -Iinclude
LDFLAGS	= -L. -Llib
LIBS	:= -lagn -lparson -llua -lexpat -lssl -lcrypto
SRCS	= buffer.c var.c log.c node.c map.c csig.c \
	  tzdiff.c atob.c str.c net.c hash.c path.c \
	  set.c skip.c lock.c xml.c daemon.c call.c cache.c \
	  cvt.c unhex.c which.c timeout.c url.c systemconfig.c build.c \
	  fsdb.c alua.c \
	  parson.c
OBJS	= $(SRCS:%.c=%.o)
LIB	= libagn.a

all:	parson.h libs $(LIB)

clean:
	rm -f $(LIB) $(OBJS) parson.c parson.h
	rm -rf lib include
	
$(LIB):	$(OBJS)
	rm -f $@
	ar rc $@ $(OBJS)

parson.c parson.h:	parson-0.0.0.tar.gz
	tar xaf $< && ( cd parson-0.0.0; mv parson.c parson.h ..; cd ..; rm -rf parson-0.0.0 )
	touch -c parson.c parson.h

libs:	lib include lib/libslang.a lib/liblua.a lib/libssl.a lib/libcrypto.a lib/libz.a lib/libxml2.a lib/libbsd.a lib/libopendkim.a lib/libmpack.a lib/libmilter.a

lib include:
	[ -d $@ ] || mkdir $@
lib/libslang.a:	slang-1.4.9.tar.gz slang-1.4.9.patch
	tar xaf slang-1.4.9.tar.gz && ( cd slang-1.4.9; patch -p1 < ../slang-1.4.9.patch; ./configure; make; mv src/objs/libslang.a ../lib; mv src/slang.h ../include; cd ..; rm -rf slang-1.4.9 )
lib/liblua.a:	lua-5.4.7.tar.gz
	tar xaf lua-5.4.7.tar.gz && ( cd lua-5.4.7; make linux; mv src/liblua.a ../lib; mv src/lauxlib.h src/lua.h src/luaconf.h src/lualib.h ../include; cd ..; rm -rf lua-5.4.7 )
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
lib/libmpack.a:	mpack-amalgamation-1.1.1.tar.gz
	tar xzf mpack-amalgamation-1.1.1.tar.gz && ( cd mpack-amalgamation-1.1.1; gcc -O3 -Isrc/mpack -c -o mpack.o src/mpack/mpack.c; ar rc ../lib/libmpack.a mpack.o; mv src/mpack/mpack.h ../include; cd ..; rm -rf mpack-amalgamation-1.1.1 )
lib/libmilter.a:	sendmail-8.17.2.tar.gz
	tar xaf $< && ( cd sendmail-8.17.2/libmilter; echo QVBQRU5EREVGKGBjb25mRU5WREVGJyxgLURORVRJTkVUNicpCg== | base64 -d > env; sh Build -c -fenv; cd ..; cp -r obj.*/libmilter/libmilter.a ../lib; cp -r include/libmilter ../include; cd ..; rm -rf sendmail-8.17.2 )

