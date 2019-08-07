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
/*	-*- mode: c; mode: fold -*-	*/
# include	<stdlib.h>
# include	<regex.h>
# include	"xmlback.h"


static inline int
digit (char ch) /*{{{*/
{
	switch (ch) {
	default:
	case '0':	return 0;
	case '1':	return 1;
	case '2':	return 2;
	case '3':	return 3;
	case '4':	return 4;
	case '5':	return 5;
	case '6':	return 6;
	case '7':	return 7;
	case '8':	return 8;
	case '9':	return 9;
	}
}/*}}}*/
static inline int
numparse (const char *s, int len) /*{{{*/
{
	int	rc = 0;
	
	while (len-- > 0) {
		rc *= 10;
		rc += digit (*s++);
	}
	return rc;
}/*}}}*/
int *
tf_parse_date (const char *s) /*{{{*/
{
	static regex_t	dparse;
	static bool_t	isinit = false;
	int		*rc;
	regmatch_t	sub[7];
	int		n;
	
	rc = NULL;
	if (! isinit) {
		regcomp (& dparse, "^([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}):([0-9]{2}):([0-9]{2})$", REG_EXTENDED);
		isinit = true;
	}
	if ((regexec (& dparse, s, sizeof (sub) / sizeof (sub[0]), sub, 0) == 0) && (rc = (int *) malloc (sizeof (int) * 6))) {
		for (n = 1; n < sizeof (sub) / sizeof (sub[0]); ++n)
			rc[n - 1] = numparse (s + sub[n].rm_so, sub[n].rm_eo - sub[n].rm_so);
	}
	return rc;
}/*}}}*/
struct tm
tf_convert_date (int *date) /*{{{*/
{
	struct tm	tt;
	
	memset (& tt, 0, sizeof (tt));
	tt.tm_year = date[0] - 1900;
	tt.tm_mon = date[1] - 1;
	tt.tm_mday = date[2];
	tt.tm_hour = date[3];
	tt.tm_min = date[4];
	tt.tm_sec = date[5];
	return tt;
}/*}}}*/

typedef struct code { /*{{{*/
	char		*cname;	/* name of this code			*/
	hash_t		hval;	/* hashvalue for this name		*/
	void		*priv;	/* private code data			*/
	void		(*ffree) (void *);
	bool_t		(*fsetup) (void *, const char *, tag_t *, blockmail_t *);
	bool_t		(*fproc) (void *, const char *, tag_t *, blockmail_t *, receiver_t *);
	
	int		success, fail;
	bool_t		defect;
	struct code	*next;
	/*}}}*/
}	code_t;
static code_t *
code_free (code_t *code) /*{{{*/
{
	if (code) {
		if (code -> priv)
			(*code -> ffree) (code -> priv);
		if (code -> cname)
			free (code -> cname);
		free (code);
	}
	return NULL;
}/*}}}*/
static code_t *
code_alloc (const char *cname, hash_t hval, void *priv,
	    void (*ffree) (void *),
	    bool_t (*fsetup) (void *, const char *, tag_t *, blockmail_t *),
	    bool_t (*fproc) (void *, const char *, tag_t *, blockmail_t *, receiver_t *)) /*{{{*/
{
	code_t	*code;
	
	if (code = (code_t *) malloc (sizeof (code_t))) {
		if (code -> cname = strdup (cname)) {
			code -> hval = hval;
			code -> priv = priv;
			code -> ffree = ffree;
			code -> fsetup = fsetup;
			code -> fproc = fproc;
			
			code -> success = 0;
			code -> fail = 0;
			code -> defect = false;
			code -> next = NULL;
		} else {
			free (code);
			code = NULL;
		}
	}
	return code;
}/*}}}*/

typedef struct extend { /*{{{*/
	char		*lang;	/* language for extention		*/
	buffer_t	*ext;	/* the extension itself			*/
	struct extend	*next;
	/*}}}*/
}	extend_t;
static extend_t *
extend_alloc (const char *lang) /*{{{*/
{
	extend_t	*e;
	
	if (e = (extend_t *) malloc (sizeof (extend_t))) {
		if (e -> lang = strdup (lang)) {
			e -> ext = NULL;
			e -> next = NULL;
		} else {
			free (e);
			e = NULL;
		}
	}
	return e;
}/*}}}*/
static extend_t *
extend_free (extend_t *e) /*{{{*/
{
	if (e) {
		if (e -> lang)
			free (e -> lang);
		if (e -> ext)
			buffer_free (e -> ext);
		free (e);
	}
	return NULL;
}/*}}}*/
static void
extend_load (extend_t *e, blockmail_t *blockmail) /*{{{*/
{
	char	*name;
	
	if (name = malloc (strlen (e -> lang) + 32)) {
		var_t	*ec;
		
		sprintf (name, "xmlback-extension.%s", e -> lang);
		if ((ec = var_find (blockmail -> company_info, name)) && ec -> val)
			if (e -> ext || (e -> ext = buffer_alloc (0)))
				if (! buffer_sets (e -> ext, ec -> val))
					e -> ext = buffer_free (e -> ext);
		free (name);
	}
}/*}}}*/
static void
extend_unload (extend_t *e) /*{{{*/
{
	if (e -> ext)
		e -> ext = buffer_free (e -> ext);
}/*}}}*/

typedef struct { /*{{{*/
	code_t		*code;	/* the code 				*/
	extend_t	*ext;	/* external code for patching/extending	*/
	/*}}}*/
}	tfunc_t;
void *
tfunc_alloc (blockmail_t *blockmail) /*{{{*/
{
	tfunc_t	*tf;
	
	if (tf = (tfunc_t *) malloc (sizeof (tfunc_t))) {
		tf -> code = NULL;
		tf -> ext = NULL;
	}
	return tf;
}/*}}}*/
void
tfunc_free (void *tp) /*{{{*/
{
	tfunc_t	*tf = (tfunc_t *) tp;
	
	if (tf) {
		code_t		*ctmp;
		extend_t	*etmp;
		
		while (ctmp = tf -> code) {
			tf -> code = tf -> code -> next;
			code_free (ctmp);
		}
		while (etmp = tf -> ext) {
			tf -> ext = tf -> ext -> next;
			extend_free (etmp);
		}
		free (tf);
	}
}/*}}}*/
static code_t *
tfunc_find (tfunc_t *tf, blockmail_t *blockmail, tag_t *tag,
	    const char *fexpr, const char *func, const char *lang,
	    void *(*lalloc) (const char *, tag_t *, blockmail_t *),
	    void (*lfree) (void *),
	    bool_t (*lload) (void *, buffer_t *, blockmail_t *),
	    bool_t (*lsetup) (void *, const char *, tag_t *, blockmail_t *),
	    bool_t (*lproc) (void *, const char *, tag_t *, blockmail_t *, receiver_t *)) /*{{{*/
{
	code_t	*cur, *prev;
	hash_t	hval;
	void	*priv;
	
	hval = hash_value ((const byte_t *) fexpr, strlen (fexpr));
	for (cur = tf -> code, prev = NULL; cur; cur = cur -> next) {
		if ((hval == cur -> hval) && (! strcmp (fexpr, cur -> cname)))
			break;
		prev = cur;
	}
	if (cur) {
		if (prev) {
			prev -> next = cur -> next;
			cur -> next = tf -> code;
			tf -> code = cur;
		}
	} else if (priv = (*lalloc) (func, tag, blockmail)) {
		
		if (! (cur = code_alloc (fexpr, hval, priv, lfree, lsetup, lproc)))
			(*lfree) (priv);
		else {
			cur -> next = tf -> code;
			tf -> code = cur;
		}
		if (lload) {
			extend_t	*ext;

			for (ext = tf -> ext; ext; ext = ext -> next)
				if (! strcmp (ext -> lang, lang))
					break;
			if ((! ext) && (ext = extend_alloc (lang))) {
				extend_load (ext, blockmail);
				ext -> next = tf -> ext;
				tf -> ext = ext;
			}
			if (ext && ext -> ext)
				if (! (*lload) (priv, ext -> ext, blockmail))
					extend_unload (ext);
		}
	}
	if (cur && cur -> fsetup && (! (*cur -> fsetup) (cur -> priv, func, tag, blockmail)))
		cur = NULL;
	return cur;
}/*}}}*/
typedef struct { /*{{{*/
	code_t		*code;
	const char	*func;
	tag_t		*tag;
	blockmail_t	*blockmail;
	receiver_t	*rec;
	
	bool_t		rc;
	/*}}}*/
}	tfpriv_t;
static void
tfunc_doit (void *dp) /*{{{*/
{
	tfpriv_t	*tp = (tfpriv_t *) dp;
	
	tp -> rc = (*tp -> code -> fproc) (tp -> code -> priv, tp -> func, tp -> tag, tp -> blockmail, tp -> rec);
}/*}}}*/
static void
tfunc_execute (tfunc_t *tf, code_t *code, const char *func, tag_t *tag, blockmail_t *blockmail, receiver_t *rec) /*{{{*/
{
	if (! code -> defect) {
		tfpriv_t	tp;
		
		tp.code = code;
		tp.func = func;
		tp.tag = tag;
		tp.blockmail = blockmail;
		tp.rec = rec;
		tp.rc = false;
		if (timeout_exec (10, tfunc_doit, & tp)) {
			if (tp.rc)
				code -> success++;
			else {
				code -> fail++;
				if ((code -> fail > 10) && (code -> success < code -> fail)) {
					log_out (blockmail -> lg, LV_WARNING, "Tag \"%s\" failed %d times with %d times being successful, mark it as defect", tag -> cname, code -> fail, code -> success);
					code -> defect = true;
				}
			}
		} else {
			log_out (blockmail -> lg, LV_WARNING, "Tag \"%s\" ran into timeout, mark it as defect", tag -> cname);
			code -> defect = true;
		}
	}
}/*}}}*/

typedef struct { /*{{{*/
	char		*func;
	code_t		*code;
	/*}}}*/
}	tf_t;
static struct { /*{{{*/
	const char	*lang;
	void		*(*lalloc) (const char *, tag_t *, blockmail_t *);
	void		(*lfree) (void *);
	bool_t		(*lload) (void *, buffer_t *, blockmail_t *);
	bool_t		(*lsetup) (void *, const char *, tag_t *, blockmail_t *);
	bool_t		(*lproc) (void *, const char *, tag_t *, blockmail_t *, receiver_t *);
	/*}}}*/
}	langtab[] = { /*{{{*/
	{	"lua", tf_lua_alloc, tf_lua_free, tf_lua_load, tf_lua_setup, tf_lua_proc	}
	/*}}}*/
};
static int
find_language (const char *opt) /*{{{*/
{
	int		lidx;
	const char	*ptr;
	
	if ((! opt) || (! (ptr = strchr (opt, ':')))) {
		lidx = 0;
	} else {
		int	len = ptr - opt;
		int	n;
		
		lidx = -1;
		for (n = 0; n < sizeof (langtab) / sizeof (langtab[0]); ++n)
			if ((strlen (langtab[n].lang) == len) && (! strncmp (langtab[n].lang, opt, len))) {
				lidx = n;
				break;
			}
	}
	return lidx;
}/*}}}*/
void *
tag_function_alloc (tag_t *tag, blockmail_t *blockmail) /*{{{*/
{
	tf_t		*tf;
	int		lidx;
	char		*func;
	code_t		*code;
	
	tf = NULL;
	
	if (func = strrchr (tag -> topt, ':'))
		*func++ = '\0';
	else
		func = tag -> topt;
	if (((lidx = find_language (tag -> topt)) != -1) &&
	    (code = tfunc_find (blockmail -> tfunc, blockmail, tag,
				tag -> topt, func, langtab[lidx].lang,
				langtab[lidx].lalloc, langtab[lidx].lfree,
				langtab[lidx].lload,
				langtab[lidx].lsetup, langtab[lidx].lproc)) &&
	    (tf = (tf_t *) malloc (sizeof (tf_t)))) {
		if (tf -> func = strdup (func)) {
			tf -> code = code;
		} else {
			free (tf);
			tf = NULL;
		}
	}
	return tf;
}/*}}}*/
void *
tag_function_free (void *pd) /*{{{*/
{
	tf_t	*tf = (tf_t *) pd;
	
	if (tf) {
		if (tf -> func)
			free (tf -> func);
		free (tf);
	}
	return NULL;
}/*}}}*/
void
tag_function_proc (void *pd, tag_t *tag, blockmail_t *blockmail, receiver_t *rec) /*{{{*/
{
	tf_t	*tf = (tf_t *) pd;

	tfunc_execute (blockmail -> tfunc, tf -> code, tf -> func, tag, blockmail, rec);
}/*}}}*/
