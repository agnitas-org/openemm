/*
**  Copyright (c) 2005-2009 Sendmail, Inc. and its suppliers.
**    All rights reserved.
**
**  Copyright (c) 2009-2015, The Trusted Domain Project.  All rights reserved.
*/

#ifndef _DKIM_H_
#define _DKIM_H_

#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */

/* system includes */
#include <sys/types.h>
#include <sys/param.h>
#include <sys/time.h>
#ifdef HAVE_STDBOOL_H
# include <stdbool.h>
#endif /* HAVE_STDBOOL_H */
#include <inttypes.h>
#ifdef HAVE_LIMITS_H
# include <limits.h>
#endif /* HAVE_LIMITS_H */

/*
**  version -- 0xrrMMmmpp
**  
**  	rr == release number
**  	MM == major revision number
**  	mm == minor revision number
**  	pp == patch number
*/

#define	OPENDKIM_LIB_VERSION	0x020a0300

#ifdef __STDC__
# ifndef __P
#  define __P(x)  x
# endif /* ! __P */
#else /* __STDC__ */
# ifndef __P
#  define __P(x)  ()
# endif /* ! __P */
#endif /* __STDC__ */

/* definitions */
#define	DKIM_ATPSTAG		"atps"	/* ATPS tag name */
#define	DKIM_ATPSHTAG		"atpsh"	/* ATPS tag name */
#define DKIM_HDRMARGIN		75	/* "standard" header margin */
#define DKIM_MAXHEADER		4096	/* buffer for caching one header */
#define	DKIM_MAXHOSTNAMELEN	256	/* max. FQDN we support */
#define	DKIM_REPORTTAG		"r"	/* DKIM reporting request tag */
#define	DKIM_REPORTTAGVAL	"y"	/* DKIM reporting request tag value */
#define	DKIM_SIGNHEADER		"DKIM-Signature"
					/* DKIM signature header */

/* special DNS tokens */
#define	DKIM_DNSKEYNAME		"_domainkey"
					/* reserved DNS sub-zone */

/* macros */
#define	DKIM_SIG_CHECK(x)	((dkim_sig_getflags((x)) & DKIM_SIGFLAG_PASSED != 0) && (dkim_sig_getbh((x)) == DKIM_SIGBH_MATCH))

/*
**  DKIM_STAT -- status code type
*/

typedef int DKIM_STAT;

#define	DKIM_STAT_OK		0	/* function completed successfully */
#define	DKIM_STAT_BADSIG	1	/* signature available but failed */
#define	DKIM_STAT_NOSIG		2	/* no signature available */
#define	DKIM_STAT_NOKEY		3	/* public key not found */
#define	DKIM_STAT_CANTVRFY	4	/* can't get domain key to verify */
#define	DKIM_STAT_SYNTAX	5	/* message is not valid syntax */
#define	DKIM_STAT_NORESOURCE	6	/* resource unavailable */
#define	DKIM_STAT_INTERNAL	7	/* internal error */
#define	DKIM_STAT_REVOKED	8	/* key found, but revoked */
#define	DKIM_STAT_INVALID	9	/* invalid function parameter */
#define	DKIM_STAT_NOTIMPLEMENT	10	/* function not implemented */
#define	DKIM_STAT_KEYFAIL	11	/* key retrieval failed */
#define	DKIM_STAT_CBREJECT	12	/* callback requested reject */
#define	DKIM_STAT_CBINVALID	13	/* callback gave invalid result */
#define	DKIM_STAT_CBTRYAGAIN	14	/* callback says try again later */
#define	DKIM_STAT_CBERROR	15	/* callback error */
#define	DKIM_STAT_MULTIDNSREPLY	16	/* multiple DNS replies */
#define	DKIM_STAT_SIGGEN	17	/* signature generation failed */

/*
**  DKIM_CBSTAT -- callback status code type
*/

typedef int DKIM_CBSTAT;

#define	DKIM_CBSTAT_CONTINUE	0	/* continue */
#define	DKIM_CBSTAT_REJECT	1	/* reject */
#define	DKIM_CBSTAT_TRYAGAIN	2	/* try again later */
#define	DKIM_CBSTAT_NOTFOUND	3	/* requested record not found */
#define	DKIM_CBSTAT_ERROR	4	/* error requesting record */
#define	DKIM_CBSTAT_DEFAULT	5	/* bypass; use default handling */

/*
**  DKIM_SIGERROR -- signature errors
*/

typedef int DKIM_SIGERROR;

#define DKIM_SIGERROR_UNKNOWN		(-1)	/* unknown error */
#define DKIM_SIGERROR_OK		0	/* no error */
#define DKIM_SIGERROR_VERSION		1	/* unsupported version */
#define DKIM_SIGERROR_DOMAIN		2	/* invalid domain (d=/i=) */
#define DKIM_SIGERROR_EXPIRED		3	/* signature expired */
#define DKIM_SIGERROR_FUTURE		4	/* signature in the future */
#define DKIM_SIGERROR_TIMESTAMPS	5	/* x= < t= */
#define DKIM_SIGERROR_UNUSED		6	/* OBSOLETE */
#define DKIM_SIGERROR_INVALID_HC	7	/* c= invalid (header) */
#define DKIM_SIGERROR_INVALID_BC	8	/* c= invalid (body) */
#define DKIM_SIGERROR_MISSING_A		9	/* a= missing */
#define DKIM_SIGERROR_INVALID_A		10	/* a= invalid */
#define DKIM_SIGERROR_MISSING_H		11	/* h= missing */
#define DKIM_SIGERROR_INVALID_L		12	/* l= invalid */
#define DKIM_SIGERROR_INVALID_Q		13	/* q= invalid */
#define DKIM_SIGERROR_INVALID_QO	14	/* q= option invalid */
#define DKIM_SIGERROR_MISSING_D		15	/* d= missing */
#define DKIM_SIGERROR_EMPTY_D		16	/* d= empty */
#define DKIM_SIGERROR_MISSING_S		17	/* s= missing */
#define DKIM_SIGERROR_EMPTY_S		18	/* s= empty */
#define DKIM_SIGERROR_MISSING_B		19	/* b= missing */
#define DKIM_SIGERROR_EMPTY_B		20	/* b= empty */
#define DKIM_SIGERROR_CORRUPT_B		21	/* b= corrupt */
#define DKIM_SIGERROR_NOKEY		22	/* no key found in DNS */
#define DKIM_SIGERROR_DNSSYNTAX		23	/* DNS reply corrupt */
#define DKIM_SIGERROR_KEYFAIL		24	/* DNS query failed */
#define DKIM_SIGERROR_MISSING_BH	25	/* bh= missing */
#define DKIM_SIGERROR_EMPTY_BH		26	/* bh= empty */
#define DKIM_SIGERROR_CORRUPT_BH	27	/* bh= corrupt */
#define DKIM_SIGERROR_BADSIG		28	/* signature mismatch */
#define DKIM_SIGERROR_SUBDOMAIN		29	/* unauthorized subdomain */
#define DKIM_SIGERROR_MULTIREPLY	30	/* multiple records returned */
#define DKIM_SIGERROR_EMPTY_H		31	/* h= empty */
#define DKIM_SIGERROR_INVALID_H		32	/* h= missing req'd entries */
#define DKIM_SIGERROR_TOOLARGE_L	33	/* l= value exceeds body size */
#define DKIM_SIGERROR_MBSFAILED		34	/* "must be signed" failure */
#define	DKIM_SIGERROR_KEYVERSION	35	/* unknown key version */
#define	DKIM_SIGERROR_KEYUNKNOWNHASH	36	/* unknown key hash */
#define	DKIM_SIGERROR_KEYHASHMISMATCH	37	/* sig-key hash mismatch */
#define	DKIM_SIGERROR_NOTEMAILKEY	38	/* not an e-mail key */
#define	DKIM_SIGERROR_UNUSED2		39	/* OBSOLETE */
#define	DKIM_SIGERROR_KEYTYPEMISSING	40	/* key type missing */
#define	DKIM_SIGERROR_KEYTYPEUNKNOWN	41	/* key type unknown */
#define	DKIM_SIGERROR_KEYREVOKED	42	/* key revoked */
#define	DKIM_SIGERROR_KEYDECODE		43	/* key couldn't be decoded */
#define	DKIM_SIGERROR_MISSING_V		44	/* v= tag missing */
#define	DKIM_SIGERROR_EMPTY_V		45	/* v= tag empty */
#define	DKIM_SIGERROR_KEYTOOSMALL	46	/* too few key bits */

/* generic DNS error codes */
#define	DKIM_DNS_ERROR		(-1)		/* error in transit */
#define	DKIM_DNS_SUCCESS	0		/* reply available */
#define	DKIM_DNS_NOREPLY	1		/* reply not available (yet) */
#define	DKIM_DNS_EXPIRED	2		/* no reply, query expired */
#define	DKIM_DNS_INVALID	3		/* invalid request */

/*
**  DKIM_CANON -- canonicalization method
*/

typedef int dkim_canon_t;

#define DKIM_CANON_UNKNOWN	(-1)	/* unknown method */
#define DKIM_CANON_SIMPLE	0	/* as specified in DKIM spec */
#define DKIM_CANON_RELAXED	1	/* as specified in DKIM spec */

#define DKIM_CANON_DEFAULT	DKIM_CANON_SIMPLE

/*
**  DKIM_SIGN -- signing method
*/

typedef int dkim_alg_t;

#define DKIM_SIGN_UNKNOWN	(-2)	/* unknown method */
#define DKIM_SIGN_DEFAULT	(-1)	/* use internal default */
#define DKIM_SIGN_RSASHA1	0	/* an RSA-signed SHA1 digest */
#define DKIM_SIGN_RSASHA256	1	/* an RSA-signed SHA256 digest */

/*
**  DKIM_QUERY -- query method
*/

typedef int dkim_query_t;

#define DKIM_QUERY_UNKNOWN	(-1)	/* unknown method */
#define DKIM_QUERY_DNS		0	/* DNS query method (per the draft) */
#define DKIM_QUERY_FILE		1	/* text file method (for testing) */

#define DKIM_QUERY_DEFAULT	DKIM_QUERY_DNS

/*
**  DKIM_PARAM -- known signature parameters
*/

typedef int dkim_param_t;

#define DKIM_PARAM_UNKNOWN	(-1)	/* unknown */
#define DKIM_PARAM_SIGNATURE	0	/* b */
#define DKIM_PARAM_SIGNALG	1	/* a */
#define DKIM_PARAM_DOMAIN	2	/* d */
#define DKIM_PARAM_CANONALG	3	/* c */
#define DKIM_PARAM_QUERYMETHOD	4	/* q */
#define DKIM_PARAM_SELECTOR	5	/* s */
#define DKIM_PARAM_HDRLIST	6	/* h */
#define DKIM_PARAM_VERSION	7	/* v */
#define DKIM_PARAM_IDENTITY	8	/* i */
#define DKIM_PARAM_TIMESTAMP	9	/* t */
#define DKIM_PARAM_EXPIRATION	10	/* x */
#define DKIM_PARAM_COPIEDHDRS	11	/* z */
#define DKIM_PARAM_BODYHASH	12	/* bh */
#define DKIM_PARAM_BODYLENGTH	13	/* l */

/*
**  DKIM_MODE -- mode of a handle
*/

#define	DKIM_MODE_UNKNOWN	(-1)
#define	DKIM_MODE_SIGN		0
#define	DKIM_MODE_VERIFY	1

/*
**  DKIM_OPTS -- library-specific options
*/

typedef int dkim_opt_t;

#define DKIM_OP_GETOPT		0
#define	DKIM_OP_SETOPT		1

typedef int dkim_opts_t;

#define	DKIM_OPTS_FLAGS		0
#define	DKIM_OPTS_TMPDIR	1
#define	DKIM_OPTS_TIMEOUT	2
#define	DKIM_OPTS_SENDERHDRS	3	/* obsolete */
#define	DKIM_OPTS_SIGNHDRS	4
#define	DKIM_OPTS_OVERSIGNHDRS	5
#define	DKIM_OPTS_QUERYMETHOD	6
#define	DKIM_OPTS_QUERYINFO	7
#define	DKIM_OPTS_FIXEDTIME	8
#define	DKIM_OPTS_SKIPHDRS	9
#define	DKIM_OPTS_ALWAYSHDRS	10	/* obsolete */
#define	DKIM_OPTS_SIGNATURETTL	11
#define	DKIM_OPTS_CLOCKDRIFT	12
#define	DKIM_OPTS_MUSTBESIGNED	13
#define	DKIM_OPTS_MINKEYBITS	14
#define	DKIM_OPTS_REQUIREDHDRS	15

#define	DKIM_LIBFLAGS_NONE		0x00000000
#define	DKIM_LIBFLAGS_TMPFILES		0x00000001
#define	DKIM_LIBFLAGS_KEEPFILES		0x00000002
#define	DKIM_LIBFLAGS_SIGNLEN		0x00000004
#define DKIM_LIBFLAGS_CACHE		0x00000008
#define DKIM_LIBFLAGS_ZTAGS		0x00000010
#define DKIM_LIBFLAGS_DELAYSIGPROC	0x00000020
#define DKIM_LIBFLAGS_EOHCHECK		0x00000040
#define DKIM_LIBFLAGS_ACCEPTV05		0x00000080
#define DKIM_LIBFLAGS_FIXCRLF		0x00000100
#define DKIM_LIBFLAGS_ACCEPTDK		0x00000200
#define DKIM_LIBFLAGS_BADSIGHANDLES	0x00000400
#define DKIM_LIBFLAGS_VERIFYONE		0x00000800
#define DKIM_LIBFLAGS_STRICTHDRS	0x00001000
#define DKIM_LIBFLAGS_REPORTBADADSP	0x00002000
#define DKIM_LIBFLAGS_DROPSIGNER	0x00004000
#define DKIM_LIBFLAGS_STRICTRESIGN	0x00008000
#define DKIM_LIBFLAGS_REQUESTREPORTS	0x00010000

#define	DKIM_LIBFLAGS_DEFAULT		DKIM_LIBFLAGS_NONE

/*
**  DKIM_DNSSEC -- results of DNSSEC queries
*/

#define DKIM_DNSSEC_UNKNOWN	(-1)
#define DKIM_DNSSEC_BOGUS	0
#define DKIM_DNSSEC_INSECURE	1
#define DKIM_DNSSEC_SECURE	2

/*
**  DKIM_ATPS -- ATPS result codes
*/

#define	DKIM_ATPS_UNKNOWN	(-1)
#define	DKIM_ATPS_NOTFOUND	0
#define	DKIM_ATPS_FOUND		1

typedef int dkim_atps_t;

/*
**  DKIM_LIB -- library handle
*/

struct dkim_lib;
typedef struct dkim_lib DKIM_LIB;

/*
**  DKIM -- DKIM context
*/

struct dkim;
typedef struct dkim DKIM;

/*
**  DKIM_SIGKEY_T -- private/public key (unencoded)
*/

typedef unsigned char * dkim_sigkey_t;

/*
**  DKIM_SIGINFO -- signature information for use by the caller
*/

struct dkim_siginfo;
typedef struct dkim_siginfo DKIM_SIGINFO;

#define DKIM_SIGFLAG_IGNORE		0x01
#define DKIM_SIGFLAG_PROCESSED		0x02
#define DKIM_SIGFLAG_PASSED		0x04
#define DKIM_SIGFLAG_TESTKEY		0x08
#define DKIM_SIGFLAG_NOSUBDOMAIN	0x10
#define DKIM_SIGFLAG_KEYLOADED		0x20

#define DKIM_SIGBH_UNTESTED		(-1)
#define DKIM_SIGBH_MATCH		0
#define DKIM_SIGBH_MISMATCH		1

/*
**  DKIM_QUERYINFO -- information about a DNS query that is/may be needed
*/

struct dkim_queryinfo;
typedef struct dkim_queryinfo DKIM_QUERYINFO;

/*
**  DKIM_HDRDIFF -- header differences
*/

struct dkim_hdrdiff
{
	u_char *		hd_old;
	u_char *		hd_new;
};

/*
**  PROTOTYPES
*/

/*
**  DKIM_INIT -- initialize the DKIM package
**
**  Parameters:
**  	mallocf -- a function to receive malloc()-like calls, or NULL
**   	freef -- a function to receive corresponding free()-like calls, or NULL
**
**  Return value:
**  	A new DKIM library instance handle, or NULL on failure.
*/

extern DKIM_LIB *dkim_init __P((void *(*mallocf)(void *closure, size_t nbytes),
                                void (*freef)(void *closure, void *p)));

/*
**  DKIM_CLOSE -- shut down the DKIM package
**
**  Parameters:
**  	lib -- DKIM_LIB handle to shut down
**
**  Return value:
**  	None.
*/

extern void dkim_close __P((DKIM_LIB *lib));

/*
**  DKIM_SIGN -- make a new DKIM context for signing
**
**  Parameters:
**  	libhandle -- library handle, returned by dkim_init()
**  	id -- an opaque printable string for identifying this message, suitable
**  	      for use in logging or debug output; may not be NULL
**  	memclosure -- memory closure, for use by user-provided malloc/free
**  	secretkey -- pointer to secret key data to use; if NULL, it will be
**  	             obtained from disk
**  	selector -- selector being used to sign
**  	domain -- domain on behalf of which we're signing
**  	hdr_canon_alg -- canonicalization algorithm to use for headers;
**  	                 one of the DKIM_CANON_* macros, or -1 for default
**  	body_canon_alg -- canonicalization algorithm to use for body;
**  	                  one of the DKIM_CANON_* macros, or -1 for default
**  	sign_alg -- signing algorithm to use; one of the DKIM_SIGN_* macros,
**  	            or -1 for default
**  	length -- number of bytes of the body to sign (-1 == all)
**  	statp -- pointer to a DKIM_STAT which is updated by this call
**
**  Return value:
**  	A newly-allocated DKIM handle, or NULL on failure.  "statp" will be
**  	updated.
*/

extern DKIM *dkim_sign __P((DKIM_LIB *libhandle, const unsigned char *id,
                            void *memclosure, const dkim_sigkey_t secretkey,
                            const unsigned char *selector,
                            const unsigned char *domain,
                            dkim_canon_t hdr_canon_alg,
                            dkim_canon_t body_canon_alg,
                            dkim_alg_t sign_alg,
                            ssize_t length, DKIM_STAT *statp));

/*
**  DKIM_VERIFY -- make a new DKIM context for verifying
**
**  Parameters:
**  	libhandle -- library handle, returned by dkim_init()
**  	id -- an opaque printable string for identifying this message, suitable
**  	      for use in logging or debug output; may not be NULL
**  	memclosure -- memory closure, for use by user-provided malloc/free
**  	statp -- pointer to a DKIM_STAT which is updated by this call
**
**  Return value:
**  	A newly-allocated DKIM handle, or NULL on failure.  "statp" will be
**  	updated.
*/

extern DKIM *dkim_verify __P((DKIM_LIB *libhandle, const unsigned char *id,
                              void *memclosure, DKIM_STAT *statp));

/*
**  DKIM_RESIGN -- bind a new signing handle to a verifying handle
**
**  Parameters:
**  	new -- new signing handle
**  	old -- old signing/verifying handle
**  	hdrbind -- bind headers as well as body
**
**  Return value:
**  	DKIM_STAT_OK -- success
**  	DKIM_STAT_INVALID -- invalid state of one or both handles
**  	DKIM_STAT_NOTIMPLEMENT -- not enabled at compile-time
**
**  Side effects:
**  	Sets up flags such that the two are bound; dkim_free() on "old"
**  	now does nothing, and dkim_free() on "new" will free "old" once
**  	its reference count reaches zero.  See documentation for details.
*/

extern DKIM_STAT dkim_resign __P((DKIM *news, DKIM *olds, _Bool hdrbind));

/*
**  DKIM_HEADER -- process a header
**
**  Parameters:
**  	dkim -- a DKIM handle previously returned by dkim_sign() or
**  	        dkim_verify()
**  	hdr -- the header to be processed, in canonical format
**  	len -- number of bytes to process starting at "hdr"
**
**  Return value:
**  	A DKIM_STAT value.
*/

extern DKIM_STAT dkim_header __P((DKIM *dkim, u_char *hdr, size_t len));

/*
**  DKIM_EOH -- identify end of headers
**
**  Parameters:
**  	dkim -- a DKIM handle previously returned by dkim_sign() or
**  	        dkim_verify()
**
**  Return value:
**  	A DKIM_STAT value.  DKIM_STAT_NOSIG will be returned if we're
**  	validating a signature but no DKIM signature was found in the headers.
*/

extern DKIM_STAT dkim_eoh __P((DKIM *dkim));

/*
**  DKIM_BODY -- process a body chunk
**
**  Parameters:
**  	dkim -- a DKIM handle previously returned by dkim_sign() or
**  	        dkim_verify()
**  	buf -- the body chunk to be processed, in canonical format
**  	len -- number of bytes to process starting at "hdr"
**
**  Return value:
**  	A DKIM_STAT value.
*/

extern DKIM_STAT dkim_body __P((DKIM *dkim, u_char *buf, size_t len));

/*
**  DKIM_CHUNK -- process a message chunk
**
**  Parameters:
**  	dkim -- DKIM handle
**  	buf -- data to process
**  	buflen -- number of bytes at "buf" to process
**
**  Return value:
**  	A DKIM_STAT_* constant.
*/

extern DKIM_STAT dkim_chunk __P((DKIM *dkim, u_char *buf, size_t buflen));

/*
**  DKIM_EOM -- identify end of body
**
**  Parameters:
**  	dkim -- a DKIM handle previously returned by dkim_sign() or
**  	        dkim_verify()
**  	testkey -- TRUE iff the a matching key was found but is marked as a
**  	           test key (returned)
**
**  Return value:
**  	A DKIM_STAT value.
*/

extern DKIM_STAT dkim_eom __P((DKIM *dkim, _Bool *testkey));

/*
**  DKIM_KEY_SYNTAX -- process a key record parameter set for valid syntax
**
**  Parameters:
**  	dkim -- DKIM context in which this is performed
**  	str -- string to be scanned
**  	len -- number of bytes available at "str"
**
**  Return value:
**  	A DKIM_STAT constant.
*/

extern DKIM_STAT dkim_key_syntax __P((DKIM *dkim, u_char *str, size_t len));

/*
**  DKIM_SIG_SYNTAX -- process a signature parameter set for valid syntax
**
**  Parameters:
**  	dkim -- DKIM context in which this is performed
**  	str -- string to be scanned
**  	len -- number of bytes available at "str"
**
**  Return value:
**  	A DKIM_STAT constant.
*/

extern DKIM_STAT dkim_sig_syntax __P((DKIM *dkim, u_char *str, size_t len));

/*
**  DKIM_GETID -- retrieve "id" pointer from a handle
**
**  Parameters:
**  	dkim -- DKIM handle
**
**  Return value:
**  	The "id" pointer from inside the handle, stored when it was created.
*/

extern const char *dkim_getid __P((DKIM *dkim));

/*
**  DKIM_GETCACHESTATS -- retrieve cache statistics
**
**  Parameters:
**  	lib -- DKIM library handle
**  	queries -- number of queries handled (returned)
**  	hits -- number of cache hits (returned)
**  	expired -- number of expired hits (returned)
**  	reset -- if true, reset the queries, hits, and expired counters
**
**  Return value:
**  	DKIM_STAT_OK -- statistics returned
**  	DKIM_STAT_INVALID -- cache not initialized
**  	DKIM_STAT_NOTIMPLEMENT -- function not implemented
**
**  Notes:
**  	Any of the parameters may be NULL if the corresponding datum
**  	is not of interest.
*/

extern DKIM_STAT dkim_getcachestats __P((DKIM_LIB *, u_int *queries, u_int *hits,
                                         u_int *expired, u_int *keys,
                                         _Bool reset));

/*
**  DKIM_FLUSH_CACHE -- purge expired records from the database, reclaiming
**                      space for use by new data
**
**  Parameters:
**  	lib -- DKIM library whose cache should be flushed
**
**  Return value:
**  	-1 -- caching is not in effect
**  	>= 0 -- number of flushed records
*/

extern int dkim_flush_cache __P((DKIM_LIB *lib));

/*
**  DKIM_MINBODY -- return number of bytes still expected
**
**  Parameters:
**  	dkim -- DKIM handle
**
**  Return value:
**  	0 -- all canonicalizations satisfied
**  	ULONG_MAX -- at least one canonicalization wants the whole message
**  	other -- bytes required to satisfy all canonicalizations
*/

extern u_long dkim_minbody __P((DKIM *dkim));

/*
**  DKIM_GETSIGLIST -- retrieve the list of signatures
**
**  Parameters:
**  	dkim -- DKIM handle
**   	sigs -- pointer to a vector of DKIM_SIGINFO pointers (updated)
**   	nsigs -- pointer to an integer to receive the pointer count (updated)
**
**  Return value:
**  	A DKIM_STAT_* constant.
*/

extern DKIM_STAT dkim_getsiglist __P((DKIM *dkim, DKIM_SIGINFO ***sigs,
                                      int *nsigs));

/*
**  DKIM_GETSIGNATURE -- retrieve the "final" signature
**
**  Parameters:
**  	dkim -- DKIM handle
**
**  Return value:
**  	Pointer to a DKIM_SIGINFO handle which is the one libopendkim will
**  	use to return a "final" result; NULL if none could be determined.
*/

extern DKIM_SIGINFO *dkim_getsignature __P((DKIM *dkim));

/*
**  DKIM_GETSIGHDR -- compute and return a signature header for a message
**
**  Parameters:
**  	dkim -- a DKIM handle previously returned by dkim_sign()
**  	buf -- buffer into which to write the signature
**  	len -- number of bytes available at "buf"
**  	initial -- width of the first line
**
**  Return value:
**  	A DKIM_STAT value.
*/

extern DKIM_STAT dkim_getsighdr __P((DKIM *dkim, u_char *buf, size_t len,
                                     size_t initial));

/*
**  DKIM_GETSIGHDR_D -- compute and return a signature header for a message,
**                      but do it dynamically
**
**  Parameters:
**  	dkim -- a DKIM handle previously returned by dkim_sign()
**  	initial -- width of the first line
**  	buf -- location of generated header (returned)
**  	len -- number of bytes available at "buf" (returned)
**
**  Return value:
**  	A DKIM_STAT value.
*/

extern DKIM_STAT dkim_getsighdr_d __P((DKIM *dkim, size_t initial,
                                       u_char **buf, size_t *len));

/*
**  DKIM_SIG_HDRSIGNED -- retrieve the header list from a signature
**
**  Parameters:
**  	sig -- DKIM_SIGINFO handle
**  	hdr -- header name to find
**
**  Return value:
**  	TRUE iff "sig" had a header list in it and the header "hdr"
**  	appeared in that list.
*/

extern _Bool dkim_sig_hdrsigned __P((DKIM_SIGINFO *sig, u_char *hdr));

/*
**  DKIM_SIG_GETQUERIES -- retrieve the queries needed to validate a signature
**
**  Parameters:
**  	dkim -- DKIM handle
**  	sig -- DKIM_SIGINFO handle
**  	qi -- DKIM_QUERYINFO handle array (returned)
**  	nqi -- number of entries in the "qi" array
**
**  Return value:
**  	A DKIM_STAT_* constant.
*/

extern DKIM_STAT dkim_sig_getqueries __P((DKIM *dkim, DKIM_SIGINFO *sig,
                                          DKIM_QUERYINFO ***qi,
                                          unsigned int *nqi));

/*
**  DKIM_SIG_GETDNSSEC -- retrieve DNSSEC results for a signature
**
**  Parameters:
**  	sig -- DKIM_SIGINFO handle
**
**  Return value:
**  	A DKIM_DNSSEC_* constant.
*/

extern int dkim_sig_getdnssec __P((DKIM_SIGINFO *sig));

/*
**  DKIM_SIG_SETDNSSEC -- set DNSSEC results for a signature
**
**  Parameters:
**      sig -- DKIM_SIGINFO handle
**      dnssec_status -- A DKIM_DNSSEC_* constant
*/

extern void dkim_sig_setdnssec __P((DKIM_SIGINFO *sig, int dnssec_status));

/*
**  DKIM_SIG_GETREPORTINFO -- retrieve reporting information from a key
**
**  Parameters:
**  	dkim -- DKIM handle
**  	sig -- DKIM_SIGINFO handle
**  	hfd -- canonicalized header descriptor (or NULL) (returned)
**  	bfd -- canonicalized body descriptor (or NULL) (returned)
**  	addr -- address buffer (or NULL)
**  	addrlen -- size of addr
**  	opts -- options buffer (or NULL)
**  	optslen -- size of opts
**  	smtp -- SMTP prefix buffer (or NULL)
**  	smtplen -- size of smtp
**  	interval -- requested report interval (or NULL)
**
**  Return value:
**  	A DKIM_STAT_* constant.
*/

extern DKIM_STAT dkim_sig_getreportinfo __P((DKIM *dkim, DKIM_SIGINFO *sig,
                                             int *hfd, int *bfd,
                                             u_char *addr, size_t addrlen,
                                             u_char *opts, size_t optslen,
                                             u_char *smtp, size_t smtplen,
                                             u_int *interval));

/*
**  DKIM_SIG_GETIDENTITY -- retrieve identity of the signer
**
**  Parameters:
**  	dkim -- DKIM handle
**  	sig -- DKIM_SIGINFO handle (or NULL to choose final one)
**  	val -- destination buffer
**  	vallen -- size of destination buffer
**
**  Return value:
**  	A DKIM_STAT_* constant.
*/

extern DKIM_STAT dkim_sig_getidentity __P((DKIM *dkim, DKIM_SIGINFO *sig,
                                           u_char *val, size_t vallen));

/*
**  DKIM_SIG_GETCANONLEN -- report number of (canonicalized) body bytes that
**                          were signed
**
**  Parameters:
**  	dkim -- a DKIM handle previously returned by dkim_sign() or
**  	        dkim_verify()
**  	sig -- a DKIM_SIGINFO handle
**  	msglen -- total size of the message body (returned)
**  	canonlen -- total number of canonicalized bytes (returned)
**  	signlen -- restricted signature length (returned)
**
**  Return value:
**  	A DKIM_STAT value.
**
**  Notes:
**  	msglen or canonlen can be NULL if that information is not of interest
**  	to the caller.
*/

extern DKIM_STAT dkim_sig_getcanonlen __P((DKIM *dkim, DKIM_SIGINFO *sig,
                                           ssize_t *msglen, ssize_t *canonlen,
                                           ssize_t *signlen));

/*
**  DKIM_OPTIONS -- set/get options
**
**  Parameters:
**  	dkimlib -- DKIM library handle
**  	op -- operation (DKIM_OP_GET or DKIM_OP_SET)
**  	opt -- which option (a DKIM_OPTS_* constant)
**  	ptr -- value (in or out)
**  	len -- bytes available at "ptr"
**
**  Return value:
**  	A DKIM_STAT value.
*/

extern DKIM_STAT dkim_options __P((DKIM_LIB *dkimlib, int op, dkim_opts_t opt,
                                   void *ptr, size_t len));

/*
**  DKIM_SIG_GETFLAGS -- retreive signature handle flags
**
**  Parameters:
**  	sig -- DKIM_SIGINFO handle
**
**  Return value:
**  	An unsigned integer which is a bitwise-OR of the DKIM_SIGFLAG_*
**  	constants currently set in the provided handle.
*/

extern unsigned int dkim_sig_getflags __P((DKIM_SIGINFO *sig));

/*
**  DKIM_SIG_GETBH -- retreive signature handle "bh" test state
**
**  Parameters:
**  	sig -- DKIM_SIGINFO handle
**
**  Return value:
**  	An integer that is one of the DKIM_SIGBH_* constants
**  	indicating the current state of "bh" evaluation of the signature.
*/

extern int dkim_sig_getbh __P((DKIM_SIGINFO *sig));

/*
**  DKIM_SIG_GETKEYSIZE -- retreive key size after verifying
**
**  Parameters:
**  	sig -- DKIM_SIGINFO handle
**  	bits -- size of the key in bits (returned)
**
**  Return value:
**  	A DKIM_STAT value.
*/

extern DKIM_STAT dkim_sig_getkeysize __P((DKIM_SIGINFO *sig,
                                          unsigned int *bits));

/*
**  DKIM_SIG_GETSIGNALG -- retreive signature algorithm after verifying
**
**  Parameters:
**  	sig -- DKIM_SIGINFO handle
**  	alg -- a DKIM_SIGN_* value (returned)
**
**  Return value:
**  	A DKIM_STAT value.
*/

extern DKIM_STAT dkim_sig_getsignalg __P((DKIM_SIGINFO *sig, dkim_alg_t *alg));

/*
**  DKIM_SIG_GETSIGNTIME -- retreive signature timestamp after verifying
**
**  Parameters:
**  	sig -- DKIM_SIGINFO handle
**  	when -- timestamp on the signature (returned)
**
**  Return value:
**  	A DKIM_STAT value.
*/

extern DKIM_STAT dkim_sig_getsigntime __P((DKIM_SIGINFO *sig, uint64_t *when));

/*
**  DKIM_SIG_GETSELECTOR -- retrieve selector used to generate the signature
**
**  Parameters:
**  	sig -- DKIM_SIGINFO handle from which to retrieve selector
**
**  Return value:
**  	Selector found in the signature.
*/

extern unsigned char *dkim_sig_getselector __P((DKIM_SIGINFO *sig));

/*
**  DKIM_SIG_GETDOMAIN -- retrieve signing domain after verifying
**
**  Parameters:
**  	sig -- DKIM_SIGINFO handle
**
**  Return value:
**  	Pointer to the signing domain.
*/

extern unsigned char *dkim_sig_getdomain __P((DKIM_SIGINFO *sig));

/*
**  DKIM_SIG_GETCANONS -- retrieve canonicaliztions after verifying
**
**  Parameters:
**  	sig -- DKIM_SIGINFO handle
**
**  Return value:
**  	DKIM_STAT_OK -- success
*/

extern DKIM_STAT dkim_sig_getcanons __P((DKIM_SIGINFO *sig, dkim_canon_t *hdr,
                                         dkim_canon_t *body));

/*
**  DKIM_SET_USER_CONTEXT -- set DKIM handle user context
**
**  Parameters:
**  	dkim -- DKIM signing handle
**  	ctx -- user context pointer to store
**
**  Parameters:
**  	A DKIM_STAT_* constant.
*/

extern DKIM_STAT dkim_set_user_context __P((DKIM *dkim, void *ctx));

/*
**  DKIM_GET_USER_CONTEXT -- retrieve DKIM handle user context
**
**  Parameters:
**  	dkim -- DKIM signing handle
**
**  Parameters:
**  	User context pointer.
*/

extern void *dkim_get_user_context __P((DKIM *dkim));

/*
**  DKIM_GETMODE -- return the mode (signing, verifying, etc.) of a handle
**
**  Parameters:
**  	dkim -- DKIM handle
**
**  Return value:
**  	A DKIM_MODE_* constant.
*/

extern int dkim_getmode __P((DKIM *dkim));

/*
**  DKIM_GETDOMAIN -- retrieve policy domain from a DKIM context
**
**  Parameters:
**  	dkim -- DKIM handle
**
**  Return value:           
**  	Pointer to the domain used for policy checking (if any) or NULL if
**  	no domain could be determined.
*/

extern u_char *dkim_getdomain __P((DKIM *dkim));

/*
**  DKIM_GETUSER -- retrieve sending user (local-part) from a DKIM context
**
**  Parameters:
**  	dkim -- DKIM handle
**
**  Return value:
**  	Pointer to the apparent sending user (local-part) or NULL if not known.
*/

extern u_char *dkim_getuser __P((DKIM *dkim));

/*
**  DKIM_GET_SIGNER -- get DKIM signature's signer
**
**  Parameters:
**  	dkim -- DKIM signing handle
**
**  Parameters:
**  	Pointer to a buffer containing the signer previously requested,
**  	or NULL if none.
*/

extern const unsigned char *dkim_get_signer __P((DKIM *dkim));

/*
**  DKIM_SET_SIGNER -- set DKIM signature's signer
**
**  Parameters:
**  	dkim -- DKIM signing handle
**  	signer -- signer to store
**
**  Parameters:
**  	A DKIM_STAT_* constant.
*/

extern DKIM_STAT dkim_set_signer __P((DKIM *dkim, const u_char *signer));

/*
**  DKIM_SET_DNS_CALLBACK -- set the DNS wait callback
**
**  Parameters:
**  	libopendkim -- DKIM library handle
**  	func -- function to call; should take an opaque context pointer
**  	interval -- how often to call back
**
**  Return value:
**  	DKIM_STAT_OK -- success
**  	DKIM_STAT_INVALID -- invalid use
**  	DKIM_STAT_NOTIMPLEMENT -- underlying resolver doesn't support callbacks
*/

extern DKIM_STAT dkim_set_dns_callback __P((DKIM_LIB *libopendkim,
                                            void (*func)(const void *context),
                                            unsigned int interval));

/*
**  DKIM_SET_KEY_LOOKUP -- set the key lookup function
**
**  Parameters:
**  	libopendkim -- DKIM library handle
**  	func -- function to call
**
**  Return value:
**  	DKIM_STAT_OK
*/

extern DKIM_STAT dkim_set_key_lookup __P((DKIM_LIB *libopendkim,
                                          DKIM_CBSTAT (*func)(DKIM *dkim,
                                                              DKIM_SIGINFO *sig,
                                                              u_char *buf,
                                                              size_t buflen)));

/*
**  DKIM_SET_SIGNATURE_HANDLE -- set the signature handle creator function
**
**  Parameters:
**  	libopendkim -- DKIM library handle
**  	func -- function to call
**
**  Return value:
**  	Pointer to the user-side handle thus created, or NULL.
*/

extern DKIM_STAT dkim_set_signature_handle __P((DKIM_LIB *libopendkim,
                                                void * (*func)(void *closure)));

/*
**  DKIM_SET_SIGNATURE_HANDLE_FREE -- set the signature handle destroyer
**                                    function
**
**  Parameters:
**  	libopendkim -- DKIM library handle
**  	func -- function to call
**
**  Return value:
**  	None.
*/

extern DKIM_STAT dkim_set_signature_handle_free __P((DKIM_LIB *libopendkim,
                                                     void (*func)(void *closure,
                                                                  void *user)));

/*
**  DKIM_SET_SIGNATURE_TAGVALUES -- set the signature handle populator function
**
**  Parameters:
**  	libopendkim -- DKIM library handle
**  	func -- function to call
**
**  Return value:
**  	DKIM_STAT_OK
*/

extern DKIM_STAT dkim_set_signature_tagvalues __P((DKIM_LIB *libopendkim,
                                                   void (*func)(void *user,
                                                                dkim_param_t pcode,
                                                                const u_char *param,
                                                                const u_char *value)));

/*
**  DKIM_SET_PRESCREEN -- set the prescreen function
**
**  Parameters:
**  	libopendkim -- DKIM library handle
**  	func -- function to call
**
**  Return value:
**  	DKIM_STAT_OK
*/

extern DKIM_STAT dkim_set_prescreen __P((DKIM_LIB *libopendkim,
                                         DKIM_CBSTAT (*func)(DKIM *dkim,
                                                             DKIM_SIGINFO **sigs,
                                                             int nsigs)));

/*
**  DKIM_SET_FINAL -- set the final processing function
**
**  Parameters:
**  	libopendkim -- DKIM library handle
**  	func -- function to call
**
**  Return value:
**  	DKIM_STAT_OK
*/

extern DKIM_STAT dkim_set_final __P((DKIM_LIB *libopendkim,
                                     DKIM_CBSTAT (*func)(DKIM *dkim,
                                                         DKIM_SIGINFO **sigs,
                                                         int nsigs)));

/*
**  DKIM_SIG_GETCONTEXT -- get user-specific context from a DKIM_SIGINFO
**
**  Parameters:
**  	siginfo -- a pointer to a DKIM_SIGINFO
**
**  Return value:
**  	The user-provided pointer stored in the named "siginfo", or NULL
**  	if none was ever set.
*/

extern void *dkim_sig_getcontext __P((DKIM_SIGINFO *siginfo));

/*
**  DKIM_SIG_GETERROR -- get error code from a DKIM_SIGINFO
**
**  Parameters:
**  	siginfo -- a pointer to a DKIM_SIGINFO
**
**  Return value:
**  	A DKIM_SIGERROR_* constant.
*/

extern int dkim_sig_geterror __P((DKIM_SIGINFO *siginfo));

/*
**  DKIM_SIG_SETERROR -- set error code in a DKIM_SIGINFO
**
**  Parameters:
**  	siginfo -- a pointer to a DKIM_SIGINFO
** 	err -- error code
**
**  Return value:
**  	A DKIM_STAT_* constant.
*/

extern DKIM_STAT dkim_sig_seterror __P((DKIM_SIGINFO *siginfo, int err));

/*
**  DKIM_SIG_GETERRORSTR -- translate a DKIM_SIGERROR into a string
**
**  Parameters:
**  	sigerr -- a DKIM_SIGERROR constant
**
**  Return value:
**  	A pointer to a human-readable string translation of "sigerr", or NULL
**  	if no such translation exists.
*/

extern const char *dkim_sig_geterrorstr __P((DKIM_SIGERROR sigerr));

/*
**  DKIM_SIG_IGNORE -- mark a signature referenced by a DKIM_SIGINFO with
**                     an "ignore" flag
**
**  Parameters:
**  	siginfo -- pointer to a DKIM_SIGINFO to update
**
**  Return value:
**  	None.
*/

extern void dkim_sig_ignore __P((DKIM_SIGINFO *siginfo));

/*
**  DKIM_SIG_PROCESS -- process a signature
**
**  Parameters:
**  	dkim -- DKIM handle
**  	sig -- DKIM_SIGINFO handle
**
**  Return value:
**  	A DKIM_STAT_* constant.
*/

extern DKIM_STAT dkim_sig_process __P((DKIM *dkim, DKIM_SIGINFO *sig));

/*
**  DKIM_FREE -- release resources associated with a DKIM handle
**
**  Parameters:
**  	dkim -- a DKIM handle previously returned by dkim_sign() or
**  	        dkim_verify()
**
**  Return value:
**  	A DKIM_STAT value.
*/

extern DKIM_STAT dkim_free __P((DKIM *dkim));

/*
**  DKIM_GETERROR -- return any stored error string from within the DKIM
**                   context handle
**
**  Parameters:
**  	dkim -- DKIM handle from which to retrieve an error string
**
**  Return value:
**  	A pointer to the stored string, or NULL if none was stored.
*/

extern const char *dkim_geterror __P((DKIM *dkim));

/*
**  DKIM_GETRESULTSTR -- translate a DKIM_STAT_* constant to a string
**
**  Parameters:
**      result -- DKIM_STAT_* constant to translate
**
**  Return value:
**      Pointer to a text describing "result", or NULL if none exists
*/

extern const char *dkim_getresultstr __P((DKIM_STAT result));

/*
**  DKIM_OHDRS -- extract and decode original headers
**
**  Parameters:
**  	dkim -- DKIM handle
**  	sig -- DKIM_SIGINFO handle
**  	ptrs -- user-provided array of pointers to header strings (updated)
**  	pcnt -- number of pointers available (updated)
**
**  Return value:
**  	A DKIM_STAT_* constant.
*/

extern DKIM_STAT dkim_ohdrs __P((DKIM *dkim, DKIM_SIGINFO *sig, u_char **ptrs,
                                 int *pcnt));

/*
**  DKIM_DIFFHEADERS -- compare original headers with received headers
**
**  Parameters:
**  	dkim -- DKIM handle
**  	canon -- canonicalization mode in use
**  	maxcost -- maximum "cost" of changes to be reported
**  	ohdrs -- original headers, presumably extracted from a "z" tag
**  	nohdrs -- number of headers at "ohdrs" available
**  	out -- pointer to an array of struct dkim_hdrdiff objects (updated)
** 	nout -- counter of handles returned (updated)
**
**  Return value:
**  	A DKIM_STAT_* constant.
**
**  Side effects:
**  	A series of DKIM_HDRDIFF handles is allocated and must later be
**  	destroyed.
*/

extern DKIM_STAT dkim_diffheaders __P((DKIM *dkim, dkim_canon_t canon,
                                       int maxcost,
                                       char **ohdrs, int nohdrs,
                                       struct dkim_hdrdiff **out, int *nout));

/*
**  DKIM_GETPARTIAL -- return a DKIM handle's "body length tag" flag
**
**  Parameters:
**  	dkim -- DKIM handle
**
**  Return value:
**  	True iff the signature is to include a body length tag
*/

extern _Bool dkim_getpartial __P((DKIM *dkim));

/*
**  DKIM_SETPARTIAL -- set the DKIM handle to sign using the DKIM body length
**                     tag (l=)
**
**  Parameters:
**  	dkim -- DKIM handle
**  	value -- new flag value
**
**  Return value:
**  	DKIM_STAT_OK
*/

extern DKIM_STAT dkim_setpartial __P((DKIM *dkim, _Bool value));

/*
**  DKIM_SET_MARGIN -- set the margin to use when generating signatures
**
**  Parameters:
**      dkim -- DKIM handle
**      value -- new margin value
**
**  Return value:
**      DKIM_STAT_INVALID -- "dkim" referenced a verification handle, or
**  	                     "value" was negative
**      DKIM_STAT_OK -- otherwise
*/

extern DKIM_STAT dkim_set_margin __P((DKIM *dkim, int value));

/*
**  DKIM_MAIL_PARSE -- extract the local-part and domain-name from a structured
**                     header field
**
**  Parameters:
**  	addr -- the header to parse; see RFC2822 for format
**  	user -- local-part of the parsed header (returned)
**  	domain -- domain part of the parsed header (returned)
**
**  Return value:
**  	0 on success; other on error (see source)
*/

extern int dkim_mail_parse __P((u_char *addr, u_char **user, u_char **domain));

/*
**  DKIM_SSL_VERSION -- return the version of the OpenSSL library against
**                      which this library was compiled
**
**  Parameters:
**  	None.
**
**  Return value:
**  	The OPENSSL_VERSION_NUMBER constant as defined by OpenSSL.
*/

extern unsigned long dkim_ssl_version __P((void));

/*
**  DKIM_LIBFEATURE -- check for a library feature
**
**  Parameters:
**  	lib -- DKIM_LIB handle
**  	fc -- feature code
**
**  Return value:
**  	TRUE iff the library was compiled with the requested feature
*/

#define DKIM_FEATURE_DIFFHEADERS	0
#define DKIM_FEATURE_UNUSED		1
#define DKIM_FEATURE_PARSE_TIME		2
#define DKIM_FEATURE_QUERY_CACHE	3
#define DKIM_FEATURE_SHA256		4
#define DKIM_FEATURE_OVERSIGN		5
#define DKIM_FEATURE_DNSSEC		6
#define DKIM_FEATURE_RESIGN		7
#define DKIM_FEATURE_ATPS		8
#define DKIM_FEATURE_XTAGS		9

#define	DKIM_FEATURE_MAX		9

extern _Bool dkim_libfeature __P((DKIM_LIB *lib, u_int fc));


/*
**  DKIM_LIBVERSION -- return version of libopendkim at runtime
**
**  Parameters:
**  	None.
**
**  Return value:
**  	Library version, i.e. value of the OPENDKIM_LIB_VERSION macro.
*/

extern uint32_t dkim_libversion __P((void));

/*
**  DKIM_GET_SIGSUBSTRING -- retrieve a minimal signature substring for
**                           disambiguation
**
**  Parameters:
**  	dkim -- DKIM handle
**  	sig -- DKIM_SIGINFO handle
**  	buf -- buffer into which to put the substring
**  	buflen -- bytes available at "buf"
**
**  Return value:
**  	A DKIM_STAT_* constant.
*/

extern DKIM_STAT dkim_get_sigsubstring __P((DKIM *, DKIM_SIGINFO *,
                                            char *, size_t *));

/*
**  DKIM_TEST_KEY -- retrieve a public key and verify it against a provided
**                   private key
**
**  Parameters:
**  	lib -- DKIM library handle
**  	selector -- selector
**  	domain -- domain name
**  	key -- private key to verify (PEM format)
**  	keylen -- size of private key
**  	dnssec -- DNSSEC result (may be NULL)
**  	err -- error buffer (may be NULL)
**  	errlen -- size of error buffer
**
**  Return value:
**  	1 -- keys don't match
**  	0 -- keys match (or no key provided)
**  	-1 -- error
*/

extern int dkim_test_key __P((DKIM_LIB *, char *, char *, char *, size_t,
                              int *, char *, size_t));

/*
**  DKIM_SIG_GETTAGVALUE -- retrieve a tag's value from a signature or its key
**
**  Parameters:
**  	sig -- DKIM_SIGINFO handle
**  	keytag -- TRUE iff we want a key's tag
**  	tag -- name of the tag of interest
**
**  Return value:
**  	Pointer to the string containing the value of the requested key,
**  	or NULL if not present.
**
**  Notes:
**  	This was added for use in determining whether or not a key or
**  	signature contained particular data, for gathering general statistics
**  	about DKIM use.  It is not intended to give applications direct access
**  	to unprocessed signature or key data.  The data returned has not
**  	necessarily been vetted in any way.  Caveat emptor.
*/

extern u_char *dkim_sig_gettagvalue __P((DKIM_SIGINFO *, _Bool, u_char *));

/*
**  DKIM_SIG_GETSIGNEDHDRS -- retrieve the signed header fields covered by
**                            a signature that passed
**
**  Parameters:
**  	dkim -- DKIM instance
**  	sig -- signature
**  	hdrs -- rectangular array of header field strings
**  	hdrlen -- length of each element of "hdrs"
**  	nhdrs -- size of "hdrs" array (updated)
**
**  Return value:
**  	A DKIM_STAT_* constant.
*/

extern DKIM_STAT dkim_sig_getsignedhdrs __P((DKIM *, DKIM_SIGINFO *,
                                             u_char *, size_t, u_int *));

/*
**  DKIM_QP_DECODE -- decode a quoted-printable string
**
**  Parameters:
**  	in -- input
**  	out -- output
**  	outlen -- bytes available at "out"
**
**  Return value:
**  	>= 0 -- number of bytes in output
**  	-1 -- parse error
*/

extern int dkim_qp_decode __P((unsigned char *, unsigned char *, int));

/*
**  DKIM_DNS_SET_QUERY_SERVICE -- stores a handle representing the DNS
**                                query service to be used, returning any
**                                previous handle
**
**  Parameters:
**  	lib -- DKIM library handle
**  	h -- handle to be used
**
**  Return value:
**  	Previously stored handle, or NULL if none.
*/

extern void *dkim_dns_set_query_service __P((DKIM_LIB *, void *));

/*
**  DKIM_DNS_SET_QUERY_START -- stores a pointer to a query start function
**
**  Parameters:
**  	lib -- DKIM library handle
**  	func -- function to use to start queries
**
**  Return value:
**  	None.
**
**  Notes:
**  	"func" should match the following prototype:
**  		returns int (status)
**  		void *dns -- receives handle stored by
**  		             dkim_dns_set_query_service()
**  		int type -- DNS RR query type (C_IN assumed)
**  		char *query -- question to ask
**  		char *buf -- buffer into which to write reply
**  		size_t buflen -- size of buf
**  		void **qh -- returned query handle
*/

extern void dkim_dns_set_query_start __P((DKIM_LIB *,
                                          int (*)(void *, int,
                                                  unsigned char *,
                                                  unsigned char *,
                                                  size_t, void **)));

/*
**  DKIM_DNS_SET_QUERY_CANCEL -- stores a pointer to a query cancel function
**
**  Parameters:
**  	lib -- DKIM library handle
**  	func -- function to use to cancel running queries
**
**  Return value:
**  	None.
**
**  Notes:
**  	"func" should match the following prototype:
**  		returns int (status)
**  		void *dns -- DNS service handle
**  		void *qh -- query handle to be canceled
*/

extern void dkim_dns_set_query_cancel __P((DKIM_LIB *,
                                           int (*)(void *, void *)));

/*
**  DKIM_DNS_SET_QUERY_WAITREPLY -- stores a pointer to wait for a DNS reply
**
**  Parameters:
**  	lib -- DKIM library handle
**  	func -- function to use to wait for a reply
**
**  Return value:
**  	None.
**
**  Notes:
**  	"func" should match the following prototype:
**  		returns int (status)
**  		void *dns -- DNS service handle
**  		void *qh -- handle of query that has completed
**  		struct timeval *timeout -- how long to wait
**  		size_t *bytes -- bytes returned
**  		int *error -- error code returned
**  		int *dnssec -- DNSSEC status returned
*/

extern void dkim_dns_set_query_waitreply __P((DKIM_LIB *,
                                              int (*)(void *, void *,
                                                      struct timeval *,
                                                      size_t *, int *,
                                                      int *)));

/*
**  DKIM_DNS_SET_INIT -- initializes the resolver
**
**  Parameters:
**  	lib -- DKIM library handle
**  	func -- function to use to initialize the resolver
**
**  Return value:
**  	None.
**
**  Notes:
**  	"func" should match the following prototype:
**  		returns int (status)
**  		void **srv -- DNS service handle (updated)
*/

extern void dkim_dns_set_init __P((DKIM_LIB *,
                                   int (*)(void **)));

/*
**  DKIM_DNS_SET_CLOSE -- shuts down the resolver
**
**  Parameters:
**  	lib -- DKIM library handle
**  	func -- function to use to shut down the resolver
**
**  Return value:
**  	None.
**
**  Notes:
**  	"func" should match the following prototype:
**  		returns void
**  		void *srv -- DNS service handle
*/

extern void dkim_dns_set_close __P((DKIM_LIB *,
                                    void (*)(void *)));

/*
**  DKIM_DNS_SET_NSLIST -- set function that updates resolver nameserver list
**
**  Parameters:
**  	lib -- DKIM library handle
**  	func -- function to use to update the nameserver list
**
**  Return value:
**  	None.
**
**  Notes:
**  	"func" should match the following prototype:
**  		returns int
**  		void *srv -- DNS service handle
**  		const char *nslist -- nameserver list, as a comma-separated
**  			string
*/

extern void dkim_dns_set_nslist __P((DKIM_LIB *,
                                     int (*)(void *, const char *)));

/*
**  DKIM_DNS_SET_CONFIG -- set function that passes configuration data to
**                         the active resolver
**
**  Parameters:
**  	lib -- DKIM library handle
**  	func -- function to use to configure the active resolver
**
**  Return value:
**  	None.
**
**  Notes:
**  	"func" should match the following prototype:
**  		returns int
**  		void *srv -- DNS service handle
**  		const char *config -- arbitrary configuration data
*/

extern void dkim_dns_set_config __P((DKIM_LIB *,
                                     int (*)(void *, const char *)));

/*
**  DKIM_DNS_SET_TRUSTANCHOR -- set function that passes trust anchor data to
**                              the active resolver
**
**  Parameters:
**  	lib -- DKIM library handle
**  	func -- function to use to pass trust anchor data to the resolver
**
**  Return value:
**  	None.
**
**  Notes:
**  	"func" should match the following prototype:
**  		returns int
**  		void *srv -- DNS service handle
**  		const char *trustanchor -- arbitrary trust anchor data
*/

extern void dkim_dns_set_trustanchor __P((DKIM_LIB *,
                                          int (*)(void *, const char *)));

/*
**  DKIM_DNS_NSLIST -- update resolver nameserver list
**
**  Parameters:
**  	lib -- DKIM library handle
**  	nslist -- comma-separated nameserver list, as IP addresses
**
**  Return value:
**  	A DKIM_DNS_* constant.
**
**  Notes:
**  	The underlying API may not return a failure status, in which case
**  	this always returns DKIM_DNS_SUCCESS.  The underlying API might also
**  	not use all of the nameservers provided.
*/

extern int dkim_dns_nslist __P((DKIM_LIB *, const char *));

/*
**  DKIM_DNS_INIT -- force resolver (re)initialization
**
**  Parameters:
**  	lib -- DKIM library handle
**
**  Return value:
**  	A DKIM_DNS_* constant.
*/

extern int dkim_dns_init __P((DKIM_LIB *));

/*
**  DKIM_DNS_CLOSE -- force resolver shutdown
**
**  Parameters:
**  	lib -- DKIM library handle
**
**  Return value:
**  	A DKIM_DNS_* constant.
*/

extern int dkim_dns_close __P((DKIM_LIB *));

/*
**  DKIM_DNS_CONFIG -- requests a change to resolver configuration
**
**  Parameters:
**  	lib -- DKIM library handle
**  	config -- opaque configuration string
**
**  Return value:
**  	A DKIM_DNS_* constant.
*/

extern int dkim_dns_config __P((DKIM_LIB *, const char *));

/*
**  DKIM_DNS_TRUSTANCHOR -- requests a change to trust anchor configuration
**
**  Parameters:
**  	lib -- DKIM library handle
**  	trust -- opaque trust anchor string
**
**  Return value:
**  	A DKIM_DNS_* constant.
*/

extern int dkim_dns_trustanchor __P((DKIM_LIB *, const char *));

/*
**  DKIM_ADD_QUERYMETHOD -- add a query method
**
**  Parameters:
**  	dkim -- DKIM signing handle to extend
**  	type -- type of query to add
**  	options -- options to include
**
**  Return value:
**  	A DKIM_STAT_* constant.
*/

extern DKIM_STAT dkim_add_querymethod __P((DKIM *, const char *,
                                           const char *));

/*
**  DKIM_ADD_XTAG -- add an extension tag/value
**
**  Parameters:
**  	dkim -- DKIM signing handle to extend
**  	tag -- name of tag to add
**  	value -- value to include
**
**  Return value:
**  	A DKIM_STAT_* constant.
*/

extern DKIM_STAT dkim_add_xtag __P((DKIM *, const char *, const char *));

/*
**  DKIM_PRIVKEY_LOAD -- explicitly try to load the private key
**
**  Parameters:
**  	dkim -- DKIM signing handle
**
**  Return value:
**  	A DKIM_STAT_* constant.
*/

extern DKIM_STAT dkim_privkey_load __P((DKIM *));

/*
**  DKIM_ATPS_CHECK -- check for Authorized Third Party Signing
**
**  Parameters:
**  	dkim -- DKIM message handle
**  	sig -- signature information handle
**  	timeout -- timeout (can be NULL)
**  	res -- ATPS result code
**
**  Return value:
**  	A DKIM_STAT_* constant.
*/

extern DKIM_STAT dkim_atps_check __P((DKIM *, DKIM_SIGINFO *,
                                      struct timeval *, dkim_atps_t *res));

/*
**  DKIM_QI_GETNAME -- retrieve the DNS name from a DKIM_QUERYINFO object
**
**  Parameters:
**  	query -- DKIM_QUERYINFO handle
**
**  Return value:
**  	A pointer to a NULL-terminated string indicating the name to be
**  	queried, or NULL on error.
*/

extern const char *dkim_qi_getname __P((DKIM_QUERYINFO *));

/*
**  DKIM_QI_GETTYPE -- retrieve the DNS RR type from a DKIM_QUERYINFO object
**
**  Parameters:
**  	query -- DKIM_QUERYINFO handle
**
**  Return value:
**  	The DNS RR type to be queried, or -1 on error.
*/

extern int dkim_qi_gettype __P((DKIM_QUERYINFO *));

/*
**  DKIM_BASE32_ENCODE -- encode a string using base32
**
**  Parameters:
**  	buf -- destination buffer
**  	buflen -- bytes available at buf (updated)
**  	data -- pointer to data to encode
**  	size -- bytes at "data" to encode
**
**  Return value:
**  	Length of encoding.
**
**  Notes:
**  	buf should be at least a byte more than *buflen to hold the trailing
**  	'\0'.
**
**  	*buflen is updated to count the number of bytes read from "data".
*/

extern int dkim_base32_encode __P((char *, size_t *, const void *, size_t));

/*
**  DKIM_SIG_GETHASHES -- retrieve hashes
**
**  Parameters:
**  	sig -- signature from which to get completed hashes
**  	hh -- pointer to header hash buffer (returned)
**  	hhlen -- bytes used at hh (returned)
**  	bh -- pointer to body hash buffer (returned)
**  	bhlen -- bytes used at bh (returned)
**
**  Return value:
**  	DKIM_STAT_OK -- successful completion
**  	DKIM_STAT_INVALID -- hashing hasn't been completed
*/

extern DKIM_STAT dkim_sig_gethashes __P((DKIM_SIGINFO *, void **, size_t *,
                                         void **, size_t *));

/*
**  DKIM_SIGNHDRS -- set the list of header fields to sign for a signature,
**                   overriding the library default
**
**  Parameters:
**  	dkim -- DKIM signing handle to be affected
**  	hdrlist -- array of names of header fields that should be signed
**
**  Return value:
**  	A DKIM_STAT_* constant.
**
**  Notes:
**  	"hdrlist" can be NULL if the library's default is to be used.
*/

extern DKIM_STAT dkim_signhdrs __P((DKIM *, const char **));

/*
**  DKIM_GETSSLBUF -- get the SSL error buffer, if any, from a DKIM handle
**
**  Parameters:
**  	dkim -- DKIM handle from which to get SSL error
**
**  Return value:
**  	Pointer to the string, if defined, or NULL otherwise.
*/

extern const char *dkim_getsslbuf __P((DKIM *dkim));

/*
**  DKIM_SIG_GETSSLBUF -- get the SSL error buffer, if any, from a signature
**
**  Parameters:
**  	sig -- signature handle from which to get SSL error
**
**  Return value:
**  	Pointer to the string, if defined, or NULL otherwise.
*/

extern const char *dkim_sig_getsslbuf __P((DKIM_SIGINFO *sig));

/* list of headers that should be signed, per RFC6376 Section 5.4 */
extern const u_char *dkim_should_signhdrs[]; 

/* list of headers that should not be signed, per RFC6376 Section 5.4 */
extern const u_char *dkim_should_not_signhdrs[];


#ifdef __cplusplus
}
#endif /* __cplusplus */

#endif /* ! _DKIM_H_ */
