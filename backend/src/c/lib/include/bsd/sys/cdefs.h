/*
 * Copyright © 2004-2006, 2009-2011 Guillem Jover <guillem@hadrons.org>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#ifdef LIBBSD_OVERLAY
#include_next <sys/cdefs.h>
#else
#include <sys/cdefs.h>
#endif

#ifndef LIBBSD_SYS_CDEFS_H
#define LIBBSD_SYS_CDEFS_H

/*
 * Some kFreeBSD headers expect those macros to be set for sanity checks.
 */
#ifndef _SYS_CDEFS_H_
#define _SYS_CDEFS_H_
#endif
#ifndef _SYS_CDEFS_H
#define _SYS_CDEFS_H
#endif

#ifdef __GNUC__
#define LIBBSD_GCC_VERSION (__GNUC__ << 8 | __GNUC_MINOR__)
#else
#define LIBBSD_GCC_VERSION 0
#endif

#ifndef __dead2
# if LIBBSD_GCC_VERSION >= 0x0207
#  define __dead2 __attribute__((__noreturn__))
# else
#  define __dead2
# endif
#endif

#ifndef __pure2
# if LIBBSD_GCC_VERSION >= 0x0207
#  define __pure2 __attribute__((__const__))
# else
#  define __pure2
# endif
#endif

#ifndef __packed
# if LIBBSD_GCC_VERSION >= 0x0207
#  define __packed __attribute__((__packed__))
# else
#  define __packed
# endif
#endif

#ifndef __aligned
# if LIBBSD_GCC_VERSION >= 0x0207
#  define __aligned(x) __attribute__((__aligned__(x)))
# else
#  define __aligned(x)
# endif
#endif

/* Linux headers define a struct with a member names __unused.
 * Debian bugs: #522773 (linux), #522774 (libc).
 * Disable for now. */
#if 0
#ifndef __unused
# if LIBBSD_GCC_VERSION >= 0x0300
#  define __unused __attribute__((unused))
# else
#  define __unused
# endif
#endif
#endif

#ifndef __printflike
# if LIBBSD_GCC_VERSION >= 0x0300
#  define __printflike(x, y) __attribute((format(printf, (x), (y))))
# else
#  define __printflike(x, y)
# endif
#endif

#ifndef __nonnull
# if LIBBSD_GCC_VERSION >= 0x0302
#  define __nonnull(x) __attribute__((__nonnull__(x)))
# else
#  define __nonnull(x)
# endif
#endif

#ifndef __bounded__
# define __bounded__(x, y, z)
#endif

#ifndef __RCSID
# define __RCSID(x)
#endif

#ifndef __FBSDID
# define __FBSDID(x)
#endif

#ifndef __RCSID
# define __RCSID(x)
#endif

#ifndef __RCSID_SOURCE
# define __RCSID_SOURCE(x)
#endif

#ifndef __SCCSID
# define __SCCSID(x)
#endif

#ifndef __COPYRIGHT
# define __COPYRIGHT(x)
#endif

#endif
