/********************************************************************************************************************************************************************************************************************************************************************
 *                                                                                                                                                                                                                                                                  *
 *                                                                                                                                                                                                                                                                  *
 *        Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   *
 *                                                                                                                                                                                                                                                                  *
 *        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    *
 *        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           *
 *        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            *
 *                                                                                                                                                                                                                                                                  *
 ********************************************************************************************************************************************************************************************************************************************************************/
# include	<stdlib.h>
# include	<ctype.h>
# include	<stdio.h>
# include	<unistd.h>
# include	<pwd.h>
# include	"xmlback.h"

extern const char	*cuserid (char *);

typedef struct { /*{{{*/
	void		*pd;		/* private data for processor	*/
	void		*(*pfree) (void *);
	void		(*pproc) (void *, tag_t *, blockmail_t *, receiver_t *);
	/*}}}*/
}	proc_t;

static void
proc_free (proc_t *p) /*{{{*/
{
	if (p) {
		if (p -> pfree)
			(*p -> pfree) (p -> pd);
		free (p);
	}
}/*}}}*/
static void
proc_process (proc_t *p, tag_t *t, blockmail_t *blockmail, receiver_t *rec) /*{{{*/
{
	if (p && p -> pproc) {
		xmlBufferEmpty (t -> value);
		(*p -> pproc) (p -> pd, t, blockmail, rec);
	}
}/*}}}*/
static proc_t *
proc_alloc (void *pd, void *(*pfree) (void *), void (*pproc) (void *, tag_t *, blockmail_t *, receiver_t *)) /*{{{*/
{
	proc_t	*p;
	
	if (p = (proc_t *) malloc (sizeof (proc_t))) {
		p -> pd = pd;
		p -> pfree = pfree;
		p -> pproc = pproc;
	} else if (pfree)
		(*pfree) (pd);
	return p;
}/*}}}*/
static proc_t *
proc_alloc_function (tag_t *t, blockmail_t *blockmail) /*{{{*/
{
	proc_t	*p;

	p = NULL;
	if (blockmail -> tfunc || (blockmail -> tfunc = tfunc_alloc (blockmail))) {
		void	*pd;
		
		if (pd = tag_function_alloc (t, blockmail))
			p = proc_alloc (pd, tag_function_free, tag_function_proc);
	}
	return p;
}/*}}}*/

typedef struct { /*{{{*/
	xmlBufferPtr	orig;
	xmlBufferPtr	mfrom;
	/*}}}*/
}	procmfrom_t;
static void *
procmfrom_free (void *pd) /*{{{*/
{
	procmfrom_t	*pmf = (procmfrom_t *) pd;
	
	if (pmf) {
		if (pmf -> mfrom == pmf -> orig)
			pmf -> mfrom = NULL;
		if (pmf -> orig)
			xmlBufferFree (pmf -> orig);
		if (pmf -> mfrom)
			xmlBufferFree (pmf -> mfrom);
		free (pmf);
	}
	return NULL;
}/*}}}*/
static procmfrom_t *
procmfrom_alloc (xmlBufferPtr orig) /*{{{*/
{
	procmfrom_t	*pmf;
	
	if (pmf = (procmfrom_t *) malloc (sizeof (procmfrom_t))) {
		pmf -> orig = xmlBufferCreateSize (xmlBufferLength (orig));
		pmf -> mfrom = NULL;
		if (pmf -> orig) {
			xmlBufferAdd (pmf -> orig, xmlBufferContent (orig), xmlBufferLength (orig));
		} else
			pmf = procmfrom_free (pmf);
	}
	return pmf;
}/*}}}*/
static void
procmfrom_proc (void *pd, tag_t *t, blockmail_t *blockmail, receiver_t *rec) /*{{{*/
{
	procmfrom_t	*pmf = (procmfrom_t *) pd;
	
	if (pmf -> orig) {
		xmlBufferPtr	use;
		
		if (! pmf -> mfrom) {
			if (blockmail -> dkim && blockmail -> mfrom) {
				if (pmf -> mfrom = xmlBufferCreateSize (strlen (blockmail -> mfrom))) {
					xmlBufferCCat (pmf -> mfrom, blockmail -> mfrom);
				}
			}
			if (! pmf -> mfrom)
				pmf -> mfrom = pmf -> orig;
		}
		use = rec -> dkim ? pmf -> mfrom : pmf -> orig;
		xmlBufferAdd (t -> value, xmlBufferContent (use), xmlBufferLength (use));
	}
}/*}}}*/
static proc_t *
proc_alloc_mfrom (tag_t *t, blockmail_t *blockmail) /*{{{*/
{
	proc_t	*p;
	void	*pd;
	
	p = NULL;
	if (pd = procmfrom_alloc (t -> value))
		p = proc_alloc (pd, procmfrom_free, procmfrom_proc);
	return p;
}/*}}}*/

static struct { /*{{{*/
	const char	*tname;		/* tagname to be parsed		*/
	/*}}}*/
}	parseable[] = { /*{{{*/
	{	"agnDYN"		},
	{	"FUNCTION"		},
	{	"agnSYSINFO"		}
	/*}}}*/
};
enum PType { /*{{{*/
	P_Dyn,
	P_Function,
	P_Sysinfo
	/*}}}*/
};
static int	psize = sizeof (parseable) / sizeof (parseable[0]);

tag_t *
tag_alloc (void) /*{{{*/
{
	tag_t	*t;
	
	if (t = (tag_t *) malloc (sizeof (tag_t))) {
		t -> name = xmlBufferCreate ();
		t -> cname = NULL;
		t -> hash = 0;
		t -> ttype = NULL;
		t -> topt = NULL;
		t -> value = xmlBufferCreate ();
		t -> parm = NULL;
		t -> used = false;
		t -> proc = NULL;
		t -> next = NULL;
		if ((! t -> name) || (! t -> value))
			t = tag_free (t);
	}
	return t;
}/*}}}*/
tag_t *
tag_free (tag_t *t) /*{{{*/
{
	if (t) {
		if (t -> name)
			xmlBufferFree (t -> name);
		if (t -> cname)
			free (t -> cname);
		if (t -> ttype)
			free (t -> ttype);
		if (t -> value)
			xmlBufferFree (t -> value);
		if (t -> parm)
			var_free_all (t -> parm);
		if (t -> proc)
			proc_free ((proc_t *) t -> proc);
		free (t);
	}
	return NULL;
}/*}}}*/
tag_t *
tag_free_all (tag_t *t) /*{{{*/
{
	tag_t	*tmp;
	
	while (tmp = t) {
		t = t -> next;
		tag_free (tmp);
	}
	return NULL;
}/*}}}*/
static void
xmlSkip (xmlChar **ptr, int *len) /*{{{*/
{
	int	n;
	
	while (*len > 0) {
		n = xmlCharLength (**ptr);
		if ((n == 1) && isspace (**ptr)) {
			*(*ptr)++ = '\0';
			--(*len);
			while ((*len > 0) && (xmlCharLength (**ptr) == 1) && isspace (**ptr))
				++(*ptr);
			break;
		} else {
			*ptr += n;
			*len -= n;
		}
	}
}/*}}}*/
static int
mkRFCdate (time_t now, char *dbuf, size_t dlen) /*{{{*/
{
	struct tm       *tt;

	if (tt = gmtime (& now)) {
		const char	*weekday[] = {
			"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
		},		*month[] = {
			"Jan", "Feb", "Mar", "Apr", "May", "Jun",
			"Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
		};
		return sprintf (dbuf, "%s, %2d %s %d %02d:%02d:%02d GMT",
				weekday[tt -> tm_wday], tt -> tm_mday, month[tt -> tm_mon], tt -> tm_year + 1900,
				tt -> tm_hour, tt -> tm_min, tt -> tm_sec) > 0;
	}
	return 0;
}/*}}}*/
static const char *
find_default (tag_t *t) /*{{{*/
{
	const char	*dflt = NULL;
	var_t		*run;

	for (run = t -> parm; run; run = run -> next)
		if (! strcmp (run -> var, "default")) {
			dflt = run -> val;
			break;
		}
	return dflt;
}/*}}}*/

static struct quote { /*{{{*/
	const char	*open;
	int		olen;
	const char	*close;
	int		clen;
	bool_t		multi_char;
	/*}}}*/
}	quotes[] = { /*{{{*/
	{	"\x80\x9e",	2,
		"\xde\x80\x9c",	3,
		false
	}, {	"\xe2\x80\x9d",	3,
		"\xe2\x80\x9d",	3,
		false
	}, {	"\xe2\x80\x9e", 3,
		"\xe2\x80\x9c", 3,
		false
	}, {	"\xe2\x80\x9a", 3,
		"\xe2\x80\x98", 3,
		false
	}, {	"\xe2\x80\x9c", 3,
		"\xe2\x80\x9d", 3,
		false
	}, {	"\xe2\x80\x98", 3,
		"\xe2\x80\x99", 3,
		false
	}, {	"\xc2\xab", 2,
		"\xc2\xbb", 2,
		false
	}, {	"\xe2\x80\xb9", 3,
		"\xe2\x80\xba", 3,
		false
	}, {	"&quot;",	6,
		"&quot;",	6,
		true
	}, {	"&apos;",	6,
		"&apos;",	6,
		true
	}
	/*}}}*/
};
static struct quote *
find_quote (const xmlChar *ptr, int len) /*{{{*/
{
	int	n;
	
	for (n = 0; n < sizeof (quotes) / sizeof (quotes[0]); ++n)
		if ((len >= quotes[n].olen) && (! memcmp (ptr, quotes[n].open, quotes[n].olen))) {
			return & quotes[n];
		}
	return NULL;
}/*}}}*/
void
tag_parse (tag_t *t, blockmail_t *blockmail) /*{{{*/
{
	xmlBufferPtr	temp;
	
	if (t -> name && (xmlBufferLength (t -> name) > 0) && (temp = xmlBufferCreateSize (xmlBufferLength (t -> name) + 1))) {
		xmlChar	*ptr;
		xmlChar	*name;
		int	len;
		int	tid;
		
		xmlBufferAdd (temp, xmlBufferContent (t -> name), xmlBufferLength (t -> name));
		ptr = (xmlChar *) xmlBufferContent (temp);
		len = xmlBufferLength (temp);
		if ((xmlCharLength (*ptr) == 1) && (*ptr == '[')) {
			++ptr;
			--len;
			if ((len > 0) && (xmlStrictCharLength (*(ptr + len - 1)) == 1) && (*(ptr + len - 1) == ']')) {
				--len;
				if ((len > 0) && (xmlStrictCharLength (*(ptr + len - 1)) == 1) && (*(ptr + len - 1) == '/'))
					--len;
				ptr[len] = '\0';
			}
		}
		name = ptr;
		xmlSkip (& ptr, & len);
		if (t -> ttype) {
			for (tid = 0; tid < psize; ++tid)
				if (! strcmp (t -> ttype, parseable[tid].tname))
					break;
		} else
			tid = psize;
		if (tid == psize)
			for (tid = 0; tid < psize; ++tid)
				if (! xmlstrcmp (name, parseable[tid].tname))
					break;
		if (tid < psize) {
			var_t		*cur, *prev;
			xmlChar		*var, *val;
			xmlChar		quote;
			int		n;
			struct quote	*quo;
			
			for (prev = t -> parm; prev && prev -> next; prev = prev -> next)
				;
			while (len > 0) {
				var = ptr;
				while (len > 0) {
					n = xmlCharLength (*ptr);
					if (n == 1) {
						if (isspace (*ptr) || (*ptr == '='))
							break;
						*ptr = tolower (*ptr);
						++ptr;
						--len;
					} else {
						ptr += n;
						len -= n;
					}
				}
				if (len > 0) {
					char	ch = *ptr;
					
					*ptr++ = '\0';
					--len;
					if ((xmlCharLength (*ptr) == 1) && isspace (ch)) {
						while ((xmlCharLength (*ptr) == 1) && isspace (*ptr))
							++ptr, --len;
						if ((len == 0) || (*ptr != '=')) {
							/* ignore attributes without value for now */
							continue;
						}
						++ptr, --len;
					}
					while ((len > 0) && (xmlCharLength (*ptr) == 1) && isspace (*ptr))
						++ptr, --len;
				}
				if (len > 0) {
					if ((xmlCharLength (*ptr) == 1) && ((*ptr == '"') || (*ptr == '\''))) {
						quote = *ptr++;
						--len;
						val = ptr;
						while (len > 0) {
							n = xmlCharLength (*ptr);
							if ((n == 1) && (*ptr == quote)) {
								*ptr++ = '\0';
								--len;
								xmlSkip (& ptr, & len);
								break;
							} else {
								ptr += n;
								len -= n;
							}
						}
					} else if (quo = find_quote (ptr, len)) {
						len -= quo -> olen;
						ptr += quo -> olen;
						val = ptr;
						while (len >= quo -> clen) {
							n = xmlCharLength (*ptr);
							if ((((! quo -> multi_char) && (n == quo -> clen)) || (quo -> multi_char && (len >= quo -> clen))) &&
							    (! memcmp (ptr, quo -> close, quo -> clen))) {
								*ptr = '\0';
								ptr += quo -> clen;
								len -= quo -> clen;
								xmlSkip (& ptr, & len);
								if (quo -> multi_char) {
									entity_resolve (val);
								}
								break;
							} else {
								ptr += n;
								len -= n;
							}
						}
					} else {
						val = ptr;
						xmlSkip (& ptr, & len);
					}
					if (cur = var_alloc (xml2char (var), xml2char (val))) {
						if (prev)
							prev -> next = cur;
						else
							t -> parm = cur;
						prev = cur;
					}
				}
			}
			switch ((enum PType) tid) {
			case P_Dyn:
				break;
			case P_Function:
				t -> proc = proc_alloc_function (t, blockmail);
				xmlBufferEmpty (t -> value);
				break;
			case P_Sysinfo:
				for (cur = t -> parm; cur; cur = cur -> next)
					if (! strcmp (cur -> var, "name")) {
						if (! strcmp (cur -> val, "FQDN")) {
							const char	*dflt;
						
							if (blockmail -> fqdn) {
								xmlBufferEmpty (t -> value);
								xmlBufferCCat (t -> value, blockmail -> fqdn);
							} else if (dflt = find_default (t)) {
								xmlBufferEmpty (t -> value);
								xmlBufferCCat (t -> value, dflt);
							}
						} else if (! strcmp (cur -> val, "RFCDATE")) {
							char            dbuf[128];

							if (mkRFCdate (blockmail_now (blockmail), dbuf, sizeof (dbuf))) {
								xmlBufferEmpty (t -> value);
								xmlBufferCCat (t -> value, dbuf);
							}
						} else if (! strcmp (cur -> val, "EPOCH")) {
							char		dbuf[64];
							
							sprintf (dbuf, "%ld", (long) blockmail_now (blockmail));
							xmlBufferEmpty (t -> value);
							xmlBufferCCat (t -> value, dbuf);
						} else if ((! strcmp (cur -> val, "MFROM")) || (! strcmp (cur -> val, "RPATH"))) {
							const char	*mfrom;
							
							if ((mfrom = find_default (t)) && ((! blockmail -> fqdn) || spf_is_valid (blockmail -> spf, mfrom))) {
								xmlBufferEmpty (t -> value);
								xmlBufferCCat (t -> value, mfrom);
							} else {
								const char	*user;
								struct passwd	*pw;

								user = NULL;
								setpwent ();
								if (pw = getpwuid (getuid ()))
									user = pw -> pw_name;
								if (! user)
									user = getlogin ();
								if (! user)
									user = cuserid (NULL);
								if (! user)
									user = getenv ("USER");
								if (! user)
									user = getenv ("LOGNAME");
								if (! user)
									user = "nobody";
								xmlBufferEmpty (t -> value);
								xmlBufferCCat (t -> value, user);
								xmlBufferCCat (t -> value, "@");
								xmlBufferCCat (t -> value, blockmail -> fqdn);
								endpwent ();
							}
							if (! strcmp (cur -> val, "RPATH"))
								t -> proc = proc_alloc_mfrom (t, blockmail);
						}
					}
				break;
			}
		}
		xmlBufferFree (temp);
	}
}/*}}}*/
bool_t
tag_match (tag_t *t, const xmlChar *name, int nlen) /*{{{*/
{
	const xmlChar	*ptr;
	int		len;
	
	len = xmlBufferLength (t -> name);
	if (len == nlen) {
		ptr = xmlBufferContent (t -> name);
		if (! memcmp (name, ptr, len))
			return true;
	}
	return false;
}/*}}}*/
const xmlChar *
tag_content (tag_t *t, blockmail_t *blockmail, receiver_t *rec, int *length) /*{{{*/
{
	if (t -> proc)
		proc_process ((proc_t *) t -> proc, t, blockmail, rec);
	*length = xmlBufferLength (t -> value);
	return xmlBufferContent (t -> value);
}/*}}}*/
