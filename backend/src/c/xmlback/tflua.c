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
# include	<ctype.h>
# include	<regex.h>
# include	"xmlback.h"
# include	"alua.h"

# define	LUA_MULTILIBNAME	"multi"
# define	LOG_LUA			"lua"
# define	PP_ID_LUA		0x111a
# define	ID_CTX			"__ctx__"
# define	ID_CUST			"__cust__"
# define	setnfield(nnn)		(lua_pushnil (il -> lua), lua_setfield (il -> lua, -2, (nnn)))				
# define	setifield(vvv,nnn)	(lua_pushnumber (il -> lua, (vvv)), lua_setfield (il -> lua, -2, (nnn)))
# define	setbfield(vvv,nnn)	(lua_pushboolean (il -> lua, (vvv) ? 1 : 0), lua_setfield (il -> lua, -2, (nnn)))
# define	setsfield(vvv,nnn)	(lua_pushstring (il -> lua, (vvv)), lua_setfield (il -> lua, -2, (nnn)))
# define	setcfield(vvv,nnn)	(lua_pushlstring (il -> lua, & (vvv), 1), lua_setfield (il -> lua, -2, (nnn)))
# define	setbuffield(vvv,nnn)	(lua_pushlstring (il -> lua, (const char *) xmlBufferContent (vvv), xmlBufferLength (vvv)), lua_setfield (il -> lua, -2, (nnn)))
# define	setxfield(vvv,nnn)	do {							\
						char *__temp = xml2string ((vvv));		\
						if (__temp) {					\
							lua_pushstring (il -> lua, __temp);	\
							free (__temp);				\
						} else						\
							lua_pushnil (il -> lua);		\
						lua_setfield (il -> lua, -2, (nnn));		\
					}	while (0)

#	if	0
static void	stack_dump (lua_State *lua, const char *fmt, ...) __attribute__ ((format (printf, 2, 3)));
static void
stack_dump (lua_State *lua, const char *fmt, ...) /*{{{*/
{
	va_list	par;
	int	stack = lua_gettop (lua);
	
	va_start (par, fmt);
	if (fmt) {
		vfprintf (stderr, fmt, par);
		fputc ('\n', stderr);
	}
	if (! stack) {
		fprintf (stderr, "\temtpy stack\n\n");
	} else {
		int	n;
		
		for (n = stack; n > 0; --n) {
			fprintf (stderr, "\t%4d  %4d  %s\n", n, n - stack - 1, lua_typename (lua, lua_type (lua, n)));
		}
		fputc ('\n', stderr);
	}
	va_end (par);
}/*}}}*/
#	endif
static void
push_record_field (lua_State *lua, char type, bool_t isnull, xmlBufferPtr data) /*{{{*/
{
	char	*temp;

	if (isnull) {
		alua_pushnull (lua);
	} else if (! (temp = xml2string (data))) {
		switch (type) {
		case 's':
			lua_pushstring (lua, "");
			break;
		default:
			lua_pushnil (lua);
			break;
		}
	} else {
		struct tm	tt;
				
		switch (type) {
		default:
			lua_pushstring (lua, temp);
			break;
		case 'n':
			lua_pushnumber (lua, atof (temp));
			break;
		case 'd':
			if (alua_date_parse (temp, & tt))
				alua_pushdate (lua, & tt);
			else
				lua_pushnil (lua);
		}
		free (temp);
	}
}/*}}}*/
static void
fetchn_value (lua_State *lua, const char *column, size_t len) /*{{{*/
{
	char	*buf;
	
	if (buf = malloc (len + 1)) {
		char	*sav, *ptr, *cv;

		memcpy (buf, column, len);
		buf[len] = '\0';
		lua_getfield (lua, LUA_REGISTRYINDEX, ID_CUST);
		for (ptr = buf; ptr; ) {
			sav = ptr;
			if (ptr = strchr (ptr, '.')) {
				*ptr++ = '\0';
				for (cv = sav; *cv; ++cv)
					*cv = toupper (*cv);
			} else
				for (cv = sav; *cv; ++cv)
					*cv = tolower (*cv);
			lua_getfield (lua, -1, sav);
			lua_remove (lua, -2);
			if (ptr && (lua_type (lua, -1) != LUA_TTABLE))
				ptr = NULL;
		}
		free (buf);
	} else
		lua_pushnil (lua);
}/*}}}*/
static void
fetch_value (lua_State *lua, const char *column) /*{{{*/
{
	fetchn_value (lua, column, strlen (column));
}/*}}}*/

typedef struct { /*{{{*/
	log_t		*lg;
	blockmail_t	*blockmail;
	receiver_t	*rec;
	int		last_customer_id;
	record_t	*last_record;
	block_t		*last_base_block;
	lua_State	*lua;
	
	void		*local;
	/*}}}*/
}	iflua_t;

# define	GET_IFLUA(xxx)	((iflua_t *) lua_touserdata ((xxx), lua_upvalueindex (1)))

static inline int
iflua_convert (lua_State *lua, const xchar_t *(*func) (xconv_t *, const xchar_t *, int, int *)) /*{{{*/
{
	iflua_t		*il = GET_IFLUA (lua);
	const char	*str;
	size_t		slen;
	const xchar_t	*rplc;
	int		rlen;
	
	if (str = lua_tolstring (il -> lua, 1, & slen))
		rplc = (*func) (il -> blockmail -> xconv, (const xchar_t *) str, (int) slen, & rlen);
	else
		rplc = NULL;
	if (rplc)
		lua_pushlstring (il -> lua, (const char *) rplc, (size_t) rlen);
	else
		lua_pushnil (lua);
	return 1;
}/*}}}*/
static int
iflua_xlower (lua_State *lua) /*{{{*/
{
	return iflua_convert (lua, xconv_lower);
}/*}}}*/
static int
iflua_xupper (lua_State *lua) /*{{{*/
{
	return iflua_convert (lua, xconv_upper);
}/*}}}*/
static int
iflua_xcapitalize (lua_State *lua) /*{{{*/
{
	return iflua_convert (lua, xconv_title);
}/*}}}*/
static int
iflua_like (lua_State *lua) /*{{{*/
{
	iflua_t		*il = GET_IFLUA (lua);
	const char	*pattern, *string, *escape;
	size_t		plen, slen, elen;
	bool_t		rc;
	
	if ((string = lua_tolstring (il -> lua, 1, & slen)) &&
	    (pattern = lua_tolstring (il -> lua, 2, & plen))) {
		if (! (escape = lua_gettop (il -> lua) > 2 ? lua_tolstring (il -> lua, 3, & elen) : NULL)) {
			elen = 0;
		}
    		rc = xmlSQLlike ((const xmlChar *) pattern, plen, (const xmlChar *) string, slen, (const xmlChar *) escape, elen);
	} else
		rc = false;
	lua_pushboolean (il -> lua, rc ? 1 : 0);
	return 1;
}/*}}}*/
static int
iflua_multi_get (lua_State *lua) /*{{{*/
{
	iflua_t		*il = GET_IFLUA (lua);
	
	if (il -> rec && il -> rec -> rvdata) {
		dataset_t	*ds = il -> rec -> rvdata;
		record_t	*record;
		int		n, m;

		lua_createtable (il -> lua, ds -> ruse, 0);
		for (n = 0; n < ds -> ruse; ++n) {
			record = ds -> r[n];
			lua_pushnumber (il -> lua, n + 1);
			lua_createtable (il -> lua, 0, il -> blockmail -> field_count);
			for (m = 0; m < il -> blockmail -> field_count; ++m) {
				field_t	*f = il -> blockmail -> field[m];
				bool_t	seen = false;

				if (f -> uref) {
					lua_getfield (il -> lua, -1, f -> uref);
					if (lua_isnil (il -> lua, -1)) {
						lua_pop (il -> lua, 1);
						lua_createtable (il -> lua, 0, 0);
					} else {
						seen = true;
					}
				}
				push_record_field (il -> lua, f -> type, record -> isnull[m], record -> data[m]);
				lua_setfield (il -> lua, -2, f -> lname);
				if (f -> uref) {
					if (seen) {
						lua_pop (il -> lua, 1);
					} else {
						lua_setfield (il -> lua, -2, f -> uref);
					}
				}
			}
			lua_settable (il -> lua, -3);
		}
	} else
		lua_pushnil (il -> lua);
	return 1;
}/*}}}*/
static int
iflua_multi_pos (lua_State *lua) /*{{{*/
{
	iflua_t		*il = GET_IFLUA (lua);
	
	if (il -> rec && il -> rec -> rvdata) {
		lua_pushnumber (il -> lua, il -> rec -> rvdata -> ipos);
	} else {
		lua_pushnil (il -> lua);
	}
	return 1;
}/*}}}*/
static int
iflua_multi_count (lua_State *lua) /*{{{*/
{
	iflua_t		*il = GET_IFLUA (lua);
	
	if (il -> rec && il -> rec -> rvdata) {
		lua_pushnumber (il -> lua, il -> rec -> rvdata -> icount);
	} else {
		lua_pushnil (il -> lua);
	}
	return 1;
}/*}}}*/
static int
iflua_loglevel (lua_State *lua) /*{{{*/
{
	iflua_t		*il = GET_IFLUA (lua);
	bool_t		rc;
	const char	*level;

	rc = false;
	if (level = lua_tostring (il -> lua, 1)) {
		if (il -> lg) 
			rc = log_level_set (il -> lg, level);
	}
	lua_pushboolean (il -> lua, rc ? 1 : 0);
	return 1;
}/*}}}*/
static int
iflua_log (lua_State *lua) /*{{{*/
{
	iflua_t		*il = GET_IFLUA (lua);
	int		stack = lua_gettop (il -> lua);
	const char	*level, *msg;

	if (il -> lg) {
		level = NULL;
		msg = NULL;
		if (stack > 1) {
			level = lua_tostring (il -> lua, 1);
			msg = lua_tostring (il -> lua, 2);
		} else if (stack == 1) {
			msg = lua_tostring (il -> lua, 1);
		}
		if (msg) {
			int	loglevel;

			if ((loglevel = log_level (level)) == -1)
				loglevel = LV_INFO;
			log_out (il -> lg, loglevel, msg);
		}
	}
	return 0;
}/*}}}*/
static int
iflua_makeuid (lua_State *lua) /*{{{*/
{
	iflua_t		*il = GET_IFLUA (lua);
	int		stack = lua_gettop (il -> lua);
	long		url_id;
	const char	*prefix;
	char		*uid;
	
	url_id = 0;
	prefix = NULL;
	if (stack > 0) {
		if (lua_isnumber (il -> lua, 1))
			url_id = (long) lua_tonumber (il -> lua, 1);
		if ((stack > 1) && lua_isstring (il -> lua, 2))
			prefix = lua_tostring (il -> lua, 2);
	}
	if (il -> rec && (uid = create_uid (il -> blockmail, prefix, il -> rec, url_id))) {
		lua_pushstring (il -> lua, uid);
		free (uid);
	} else
		lua_pushnil (il -> lua);
	return 1;
}/*}}}*/
static int
iflua_strmap (lua_State *lua) /*{{{*/
{
	iflua_t		*il = GET_IFLUA (lua);
	int		stack = lua_gettop (il -> lua);
	const char	*input;
	size_t		len;
	char		*rc;
	
	rc = NULL;
	if (stack > 0) {
		map_t	**maps = NULL;
		int	msize = 2 + stack - 1;
		
		if (maps = malloc (msize * sizeof (map_t *))) {
			int	mcount = 0;
			int	mine = 0;
			int	n;
			
			if (il -> rec && il -> rec -> smap) {
				maps[mcount++] = il -> rec -> smap;
			}
			if (il -> blockmail -> smap) {
				maps[mcount++] = il -> blockmail -> smap;
			}
			mine = mcount;
			for (n = 2; n <= stack; ++n)
				if (lua_istable (il -> lua, n)) {
					map_t		*temp = string_map_setup ();
					const char	*var, *val;
					
					if (temp) {
						lua_pushnil (il -> lua);
						while (lua_next (lua, n)) {
							if (lua_isstring (lua, -2) &&
							    (var = lua_tostring (il -> lua, -2)) &&
							    (val = lua_tostring (il -> lua, -1))) {
								string_map_addss (temp, var, val);
							}
							lua_pop (il -> lua, 1);
						}
						maps[mcount++] = temp;
					}
				}
			if (input = lua_tolstring (il -> lua, 1, & len)) {
				xmlBufferPtr	temp1, temp2;
		
				if (temp1 = xmlBufferCreateSize (len + 1)) {
					xmlBufferCCat (temp1, input);
					temp2 = string_maps (temp1, maps, mcount);
					xmlBufferFree (temp1);
					if (temp2) {
						rc = xml2string (temp2);
						xmlBufferFree (temp2);
					}
				}
			}
			for (n = mine; n < mcount; ++n)
				string_map_done (maps[n]);
			free (maps);
		}
	}
	if (rc) {
		lua_pushstring (il -> lua, rc);
		free (rc);
	} else
		lua_pushnil (il -> lua);
	return 1;
}/*}}}*/
static struct { /*{{{*/
	const char	*modname;
	const char	*funcname;
	lua_CFunction	func;
	/*}}}*/
}	iflua_functab[] = { /*{{{*/
	{	LUA_STRLIBNAME,		"xlower",	iflua_xlower		},
	{	LUA_STRLIBNAME,		"xupper",	iflua_xupper		},
	{	LUA_STRLIBNAME,		"xcapitalize",	iflua_xcapitalize	},
	{	LUA_STRLIBNAME,		"like",		iflua_like		},
	{	LUA_MULTILIBNAME,	"get",		iflua_multi_get		},
	{	LUA_MULTILIBNAME,	"pos",		iflua_multi_pos		},
	{	LUA_MULTILIBNAME,	"count",	iflua_multi_count	},
	{	LUA_AGNLIBNAME,		"loglevel",	iflua_loglevel		},
	{	LUA_AGNLIBNAME,		"log",		iflua_log		},
	{	LUA_AGNLIBNAME,		"makeuid",	iflua_makeuid		},
	{	LUA_AGNLIBNAME,		"strmap",	iflua_strmap		}
	/*}}}*/
};

static void
iflua_setup_functions (iflua_t *il) /*{{{*/
{
	int	n;
	
	for (n = 0; n < sizeof (iflua_functab) / sizeof (iflua_functab[0]); ++n)
		alua_setup_function (il -> lua, iflua_functab[n].modname, iflua_functab[n].funcname, iflua_functab[n].func, il);
}/*}}}*/
static void
iflua_setup_context (iflua_t *il) /*{{{*/
{
	time_t		now;
	struct tm	*tt;
	var_t		*v;
	struct {
		const char	*key;
		const char	*map;
		const char	*value;
	}		mapper[] = {
		{	"_rdir_domain",		"rdir_domain",		NULL	},
		{	"_mailloop_domain",	"mailloop_domain",	NULL	},
		{	"_envelope_from",	"envelope_from",	NULL	}
	};
	int		n;
	
	lua_createtable (il -> lua, 0, 0);
	time (& now);
	if (tt = localtime (& now)) {
		alua_pushdate (il -> lua, tt);
		lua_setfield (il -> lua, -2, "now");
	}
	setifield (il -> blockmail -> licence_id, "licence_id");
	setifield (il -> blockmail -> company_id, "company_id");
	setifield (il -> blockmail -> mailinglist_id, "mailinglist_id");
	setifield (il -> blockmail -> mailing_id, "mailing_id");
	setxfield (il -> blockmail -> mailing_name, "mailing_name");
	setifield (il -> blockmail -> maildrop_status_id, "status_id");
	setcfield (il -> blockmail -> status_field, "status_field");
	if (il -> blockmail -> senddate) {
		struct tm	temp;
		
		temp = tf_convert_date (il -> blockmail -> senddate);
		alua_pushdate (il -> lua, & temp);
	} else
		lua_pushnil (il -> lua);
	lua_setfield (il -> lua, -2, "senddate");
	setbfield (il -> blockmail -> anon, "anon");
	setsfield (il -> blockmail -> selector, "selector");
	setbfield (il -> blockmail -> rdir_content_links, "rdir_content_links");
	setxfield (il -> blockmail -> auto_url, "auto_url");
	setxfield (il -> blockmail -> anon_url, "anon_url");
	setifield (il -> blockmail -> blocknr, "blocknr");
	setifield (il -> blockmail -> total_subscribers, "total_subscribers");
	setsfield (il -> blockmail -> domain ? il -> blockmail -> domain : il -> blockmail -> fqdn, "domain");
	setsfield (il -> blockmail -> nodename, "node");
	setsfield (il -> blockmail -> fqdn, "fqdn");
	lua_createtable (il -> lua, 0, 0);
	for (v = il -> blockmail -> company_info; v; v = v -> next) {
		if (v -> val)
			setsfield (v -> val, v -> var);
		else
			setnfield (v -> var);
		if ((v -> var[0] == '_') && v -> val)
			for (n = 0; n < sizeof (mapper) / sizeof (mapper[0]); ++n)
				if ((! mapper[n].value) && (! strcmp (mapper[n].key, v -> var)))
					mapper[n].value = v -> val;
	}
	lua_setfield (il -> lua, -2, "info");
	for (n = 0; n < sizeof (mapper) / sizeof (mapper[0]); ++n)
		if (mapper[n].value)
			setsfield (mapper[n].value, mapper[n].map);
		else
			setnfield (mapper[n].map);
	lua_setfield (il -> lua, LUA_REGISTRYINDEX, ID_CTX);
}/*}}}*/
static int
iflua_call_customer (lua_State *lua) /*{{{*/
{
	const char	*fname;
	
	if ((lua_gettop (lua) > 0) && (fname = lua_tostring (lua, -1)))
		fetch_value (lua, fname);
	else
		lua_pushnil (lua);
	return 1;
}/*}}}*/
static void
iflua_setup_customer (iflua_t *il) /*{{{*/
{
	const char	**seen;

	lua_createtable (il -> lua, 0, il -> blockmail -> field_count);
	if ((il -> blockmail -> field_count > 0) &&
	    (seen = (const char **) malloc (il -> blockmail -> field_count * sizeof (const char *)))) {
		int		n, m;
		const char	*last;
		int		scount;

		last = NULL;
		scount = 0;
		for (n = 0; n < il -> blockmail -> field_count; ++n) {
			field_t	*f = il -> blockmail -> field[n];
			bool_t	created = false;

			if (f -> uref) {
				if ((! last) || strcmp (last, f -> uref)) {
					for (m = 0; m < scount; ++m)
						if (! strcmp (seen[m], f -> uref))
							break;
				} else
					m = 0;
				if (m < scount)
					lua_getfield (il -> lua, -1, f -> uref);
				else {
					lua_createtable (il -> lua, 0, 0);
					created = true;
				}
				last = f -> uref;
			}
			lua_pushnil (il -> lua);
			lua_setfield (il -> lua, -2, f -> lname);
			if (f -> uref) {
				if (created)
					lua_setfield (il -> lua, -2, f -> uref);
				else
					lua_pop (il -> lua, 1);
			}
		}
		free (seen);
	}
	lua_createtable (il -> lua, 0, 0);
	lua_pushcfunction (il -> lua, iflua_call_customer);
	lua_setfield (il -> lua, -2, "__call");
	lua_setmetatable (il -> lua, -2);
	lua_setfield (il -> lua, LUA_REGISTRYINDEX, ID_CUST);
}/*}}}*/
static iflua_t *
iflua_free (iflua_t *il) /*{{{*/
{
	if (il) {
		if (il -> lua)
			lua_close (il -> lua);
		if (il -> lg)
			log_free (il -> lg);
		free (il);
	}
	return NULL;
}/*}}}*/
static iflua_t *
iflua_alloc (blockmail_t *blockmail) /*{{{*/
{
	iflua_t	*il;
	
	if (il = (iflua_t *) malloc (sizeof (iflua_t))) {
		il -> lg = log_alloc (NULL, LOG_LUA, NULL);
		il -> blockmail = blockmail;
		il -> rec = NULL;
		il -> last_customer_id = -1;
		il -> last_record = NULL;
		il -> last_base_block = NULL;
		il -> local = NULL;
		if (il -> lua = alua_alloc ()) {
			iflua_setup_functions (il);
			iflua_setup_context (il);
			iflua_setup_customer (il);
		}
	}
	return il;
}/*}}}*/
static void
iflua_push_context (iflua_t *il) /*{{{*/
{
	lua_getfield (il -> lua, LUA_REGISTRYINDEX, ID_CTX);
	if ((il -> last_customer_id != il -> rec -> customer_id) || (il -> rec -> customer_id == 0)) {
		char	scratch[2];

		setifield (il -> rec -> customer_id, "customer_id");
		scratch[0] = il -> rec -> user_type;
		scratch[1] = '\0';
		setsfield (scratch, "user_type");
		setbuffield (il -> rec -> message_id, "message_id");
	}
	if (il -> last_base_block != il -> rec -> base_block) {
		lua_createtable (il -> lua, 0, 0);
		if (il -> rec -> base_block) {
			block_t		*b = il -> rec -> base_block;
			const char	*typ;
		
			setifield (b -> bid, "id");
			if (b -> cid)
				setsfield (b -> cid, "cid");
			else
				setnfield ("cid");
			switch (b -> tid) {
			default:
			case TID_Unspec:	typ = "unspec";		break;
			case TID_EMail_Head:	typ = "head";		break;
			case TID_EMail_Text:	typ = "text";		break;
			case TID_EMail_HTML:	typ = "html";		break;
			}
			setsfield (typ, "type");
		} 
		lua_setfield (il -> lua, -2, "block");
		il -> last_base_block = il -> rec -> base_block;
	}
}/*}}}*/
static void
iflua_push_customer (iflua_t *il) /*{{{*/
{
	lua_getfield (il -> lua, LUA_REGISTRYINDEX, ID_CUST);
	if ((il -> last_customer_id != il -> rec -> customer_id) || (il -> last_record != il -> rec -> rvdata -> cur)) {
		int		n;
		record_t	*record;
		
		record = il -> rec -> rvdata -> cur;
		for (n = 0; n < il -> blockmail -> field_count; ++n) {
			field_t	*f = il -> blockmail -> field[n];

			if (f -> uref)
				lua_getfield (il -> lua, -1, f -> uref);
			push_record_field (il -> lua, f -> type, record -> isnull[n], record -> data[n]);
			lua_setfield (il -> lua, -2, f -> lname);
			if (f -> uref)
				lua_pop (il -> lua, 1);
		}
		il -> last_customer_id = il -> rec -> customer_id;
		il -> last_record = record;
	}
}/*}}}*/
static void
iflua_lgpush (iflua_t *il, const char *lid) /*{{{*/
{
	if (il && il -> lg && lid)
		log_idpush (il -> lg, lid, "->");
}/*}}}*/
static void
iflua_lgpop (iflua_t *il) /*{{{*/
{
	if (il && il -> lg)
		log_idpop (il -> lg);
}/*}}}*/

void
tf_lua_free (void *ilp) /*{{{*/
{
	iflua_free ((iflua_t *) ilp);
}/*}}}*/
void *
tf_lua_alloc (const char *func, tag_t *tag, blockmail_t *blockmail) /*{{{*/
{
	iflua_t	*il;

	il = NULL;
	if (tag -> value && (il = iflua_alloc (blockmail))) {
		iflua_lgpush (il, func);
		if (! alua_load (il -> lua, func, xmlBufferContent (tag -> value), xmlBufferLength (tag -> value))) {
			log_out (blockmail -> lg, LV_WARNING, "Code for tag \"%s\" does not compile: %s", tag -> cname, lua_tostring (il -> lua, -1));
			il = iflua_free (il);
		}
		iflua_lgpop (il);
	}
	return il;
}/*}}}*/
bool_t
tf_lua_load (void *ilp, buffer_t *code, blockmail_t *blockmail) /*{{{*/
{
	iflua_t	*il = (iflua_t *) ilp;
	bool_t	rc;

	if (! (rc = alua_load (il -> lua, "__load__", buffer_content (code), buffer_length (code))))
		log_out (blockmail -> lg, LV_WARNING, "External code failed to load: %s", lua_tostring (il -> lua, -1));
	return rc;
}/*}}}*/
bool_t
tf_lua_setup (void *ilp, const char *func, tag_t *tag, blockmail_t *blockmail) /*{{{*/
{
	iflua_t	*il = (iflua_t *) ilp;

	iflua_lgpush (il, func);
	lua_getfield (il -> lua, LUA_REGISTRYINDEX, tag -> cname);
	if (lua_isnil (il -> lua, -1)) {
		var_t	*v;

		lua_createtable (il -> lua, 0, 0);
		for (v = tag -> parm; v; v = v -> next) {
			lua_pushstring (il -> lua, v -> val);
			lua_setfield (il -> lua, -2, v -> var);
		}
		lua_setfield (il -> lua, LUA_REGISTRYINDEX, tag -> cname);
	}
	lua_pop (il -> lua, 1);
	iflua_lgpop (il);
	return true;
}/*}}}*/
bool_t
tf_lua_proc (void *ilp, const char *func, tag_t *tag, blockmail_t *blockmail, receiver_t *rec) /*{{{*/
{
	iflua_t		*il = (iflua_t *) ilp;
	char		scratch[128];
	int		rc;
	const char	*result;
	
	iflua_lgpush (il, func);
	il -> rec = rec;
	lua_settop (il -> lua, 0);
	/* Push function on stack */
	lua_getglobal (il -> lua, func);
	/* Push context on stack and modify it */
	iflua_push_context (il);
	/* Push parameter on stack */
	lua_getfield (il -> lua, LUA_REGISTRYINDEX, tag -> cname);
	/* Push customer data on stack and modify it, if customer has changed */
	iflua_push_customer (il);
	/* Doit */
	rc = lua_pcall (il -> lua, 3, 1, 0);
	if (lua_gettop (il -> lua) > 0) {
		if ((rc == 0) && alua_isdate (il -> lua, -1)) {
			alua_date_t	*date;
			
			if (date = alua_todate (il -> lua, -1)) {
				if (strftime (scratch, sizeof (scratch) - 1, "%c", & date -> tt) > 0) {
					result = scratch;
				} else {
					result = "failed to format date";
				}
			} else {
				result = "no parseable date";
			}
		} else
			result = lua_tostring (il -> lua, -1);
	} else {
		if (rc == 0)
			rc = -1;
		result = "no (usable) result returned";
	}
	if (rc == 0) {
		if (result)
			xmlBufferCCat (tag -> value, result);
	} else
		log_out (blockmail -> lg, LV_WARNING, "Tag \"%s\" propagates error \"%s\"", tag -> cname, (result ? result : "*no message found*"));
	iflua_lgpop (il);
	return rc == 0 ? true : false;
}/*}}}*/

# define	EV_FUNC		"__evaluate"

typedef struct epart { /*{{{*/
	bool_t		isstring;
	const char	*start, *end;
	struct epart	*next;
	/*}}}*/
}	epart_t;
static epart_t *
new_epart (epart_t **root, epart_t **prev, bool_t isstring, const char *start, const char *end) /*{{{*/
{
	epart_t	*ep;
	
	if (ep = (epart_t *) malloc (sizeof (epart_t))) {
		ep -> isstring = isstring;
		ep -> start = start;
		ep -> end = end;
		ep -> next = NULL;
		if (*prev)
			(*prev) -> next = ep;
		else
			*root = ep;
		*prev = ep;
	}
	return ep;
}/*}}}*/
static epart_t *
split_expression (const char *expression, bool_t *error) /*{{{*/
{
	epart_t		*root, *prev, *cur;
	bool_t		isstring;
	const char	*start, *temp;
	char		quote;
	int		eqcount;
	int		state;

	*error = false;
	root = NULL;
	prev = NULL;
	cur = NULL;
	while (*expression) {
		isstring = false;
		start = expression;
		switch (*expression) {
		case '\'':
		case '"':
			isstring = true;
			quote = *expression++;
			while (*expression && (*expression != quote))
				if ((*expression == '\\') && (*(expression + 1)))
					expression += 2;
				else
					++expression;
			if (*expression)
				++expression;
			else
				*error = true;
			break;
		case '[':
			eqcount = 0;
			temp = expression + 1;
			while (*temp == '=')
				++temp, ++eqcount;
			if (*temp == '[') {
				isstring = true;
				expression = temp + 1;
				state = 0;
				while (*expression) {
					if (state == 0) {
						if (*expression == ']')
							state = 1;
					} else {
						if (state <= eqcount) {
							if (*expression == '=')
								++state;
							else
								state = 0;
						} else {
							if (*expression == ']')
								++state;
							else
								state = 0;
						}
					}
					++expression;
					if (state == eqcount + 2)
						break;
				}
				if (state != eqcount + 2)
					*error = true;
			}
			break;
		}
		if (isstring) {
			if (! new_epart (& root, & prev, true, start, expression))
				*error = true;
			cur = NULL;
		} else {
			++expression;
			if (cur)
				cur -> end++;
			else {
				cur = new_epart (& root, & prev, false, start, expression);
				if (! cur)
					*error = true;
			}
		}
	}
	return root;
}/*}}}*/
static void
release_eparts (epart_t *ep) /*{{{*/
{
	epart_t	*tmp;
	
	while (tmp = ep) {
		ep = ep -> next;
		free (tmp);
	}
}/*}}}*/
static void
convert_common_mistakes (buffer_t *target, const char *s, int slen) /*{{{*/
{
	const char	arith[] = "<>=~!%^&-+*/|.";
	const char	*cur;
	int		len, state;
	
	while (slen > 0) {
		if (strchr (arith, *s)) {
			cur = s++;
			--slen;
			while ((slen > 0) && strchr (arith, *s))
				++s, --slen;
			len = s - cur;
			if ((len == 1) && (*cur == '=')) {
				buffer_appendsn (target, "==", 2);
			} else if ((len == 1) && (*cur == '!')) {
				buffer_appendsn (target, " not ", 4);
			} else if ((len == 2) && ((! strncmp (cur, "!=", 2)) || (! strncmp (cur, "<>", 2)) || (! strncmp (cur, "><", 2)))) {
				buffer_appendsn (target, "~=", 2);
			} else if ((len == 2) && (! strncmp (cur, "&&", 2))) {
				buffer_appendsn (target, " and ", 5);
			} else if ((len == 2) && (! strncmp (cur, "||", 2))) {
				buffer_appendsn (target, " or ", 4);
			} else if ((len == 2) && (! strncmp (cur, "=<", 2))) {
				buffer_appendsn (target, "<=", 2);
			} else if ((len == 2) && (! strncmp (cur, "=>", 2))) {
				buffer_appendsn (target, ">=", 2);
			} else {
				buffer_appendsn (target, cur, len);
			}
		} else if (isalpha (*s) || (*s == '_')) {
			cur = s++;
			--slen;
			state = 1;
			while ((slen > 0) && (state != -1)) {
				if (state == 0) {
					if (isalpha (*s) || (*s == '_')) {
						state = 1;
					} else {
						state = -1;
					}
				} else if (state == 1) {
					if (*s == '.') {
						state = 0;
					} else if ((! isalnum (*s)) && (*s != '_')) {
						state = -1;
					}
				} else
					state = -1;
				if (state != -1)
					++s, --slen;
			}
			len = s - cur;
			if ((len == 4) && (! strncasecmp (cur, "null", 4))) {
				buffer_appendsn (target, "null", 4);
			} else if ((len == 4) && (! strncasecmp (cur, "true", 4))) {
				buffer_appendsn (target, "true", 4);
			} else if ((len == 5) && (! strncasecmp (cur, "false", 5))) {
				buffer_appendsn (target, "false", 5);
			} else if ((len == sizeof (EV_FUNC) - 1) && (! strncmp (cur, EV_FUNC, sizeof (EV_FUNC) - 1))) {
				buffer_appendsn (target, "error", 5);
			} else {
				buffer_appendsn (target, cur, len);
			}
		} else {
			buffer_appendch (target, *s++);
			--slen;
		}
	}
}/*}}}*/
char *
ev_lua_convert (blockmail_t *blockmail, const char *expression) /*{{{*/
{
	char		*rc;
	regex_t		column;
	epart_t		*parts;
	bool_t		error;
	char		*search;
	buffer_t	*scratch;

	rc = NULL;
	if (regcomp (& column, "\\$(([a-z0-9]+\\.)?[a-z][a-z0-9_]*)", REG_EXTENDED | REG_ICASE) == 0) {
		parts = split_expression (expression, & error);
		if (error) {
			log_out (blockmail -> lg, LV_ERROR, "Failed to preparse expression \"%s\"", expression);
		} else if (search = malloc (strlen (expression) + 1)) {
			if (scratch = buffer_alloc (strlen (expression) * 512)) {
				epart_t		*cur;
				char		*sptr;
				int		offset;
				regmatch_t	matches[3];

				for (cur = parts; cur; cur = cur -> next) {
					if (cur -> isstring)
						buffer_appendsn (scratch, cur -> start, cur ->end - cur -> start);
					else {
						strncpy (search, cur -> start, cur -> end - cur -> start);
						search[cur -> end - cur -> start] = '\0';
						for (sptr = search; *sptr; ) {
							if ((regexec (& column, sptr, sizeof (matches) / sizeof (matches[0]), matches, 0) == 0) && (matches[0].rm_so != -1) && (matches[0].rm_eo != -1)) {
								if (matches[0].rm_so > 0)
									convert_common_mistakes (scratch, sptr, matches[0].rm_so);
								if (matches[1].rm_so < matches[1].rm_eo) {
									if ((matches[1].rm_eo - matches[1].rm_so > 5) && (! strncmp (sptr + matches[1].rm_so, "cust.", 5))) {
										offset = 5;
									} else {
										offset = 0;
									}
									buffer_appends (scratch, "cust ('");
									buffer_appendsn (scratch, sptr + matches[1].rm_so + offset, matches[1].rm_eo - matches[1].rm_so - offset);
									buffer_appends (scratch, "')");
								}
								sptr += matches[0].rm_eo;
							} else
								break;
						}
						if (*sptr)
							convert_common_mistakes (scratch, sptr, strlen (sptr));
					}
				}
				rc = buffer_stealstring (scratch);
				buffer_free (scratch);
			}
			free (search);
		}
		release_eparts (parts);
		regfree (& column);
	}
	return rc;
}/*}}}*/
void *
ev_lua_free (void *ilp) /*{{{*/
{
	return iflua_free ((iflua_t *) ilp);
}/*}}}*/
void *
ev_lua_alloc (blockmail_t *blockmail, const char *expression) /*{{{*/
{
	iflua_t	*il;
	
	if (il = iflua_alloc (blockmail)) {
		char	*frame;
		int	flen;
		
		if (frame = malloc (strlen (expression) + 256)) {
			flen = sprintf (frame, "function " EV_FUNC " (ctx, cust)\n\treturn (%s)\nend", expression);
			if (! alua_load (il -> lua, "__expr__", frame, flen)) {
				log_out (blockmail -> lg, LV_WARNING, "Expression \"%s\" does not compile: %s", expression, lua_tostring (il -> lua, -1));
				il = iflua_free (il);
			}
			free (frame);
		} else {
			log_out (blockmail -> lg, LV_ERROR, "Failed to allocate function frame for %s (%m)", expression);
			il = iflua_free (il);
		}
	}
	return il;
}/*}}}*/
int
ev_lua_vevaluate (void *ilp, receiver_t *rec, va_list par) /*{{{*/
{
	iflua_t		*il = (iflua_t *) ilp;
	const char	*var;
	char		typ;
	const char	*sval;
	int		lrc;
	int		rc;

	il -> rec = rec;
	lua_settop (il -> lua, 0);
	while ((var = va_arg (par, const char *)) != NULL) {
		if ((strlen (var) > 2) && (var[1] == ':')) {
			typ = var[0];
			var += 2;
		} else {
			typ = 's';
		}
		switch (typ) {
		default:
			va_arg (par, void *);
			lua_pushnil (il -> lua);
			break;
		case 's':
			sval = va_arg (par, const char *);
			if (sval) {
				lua_pushstring (il -> lua, sval);
			} else {
				lua_pushnil (il -> lua);
			}
			break;
		case 'n':
			lua_pushnumber (il -> lua, (lua_Number) va_arg (par, double));
			break;
		case 'i':
			lua_pushnumber (il -> lua, (lua_Number) va_arg (par, int));
			break;
		}
		lua_setglobal (il -> lua, var);
	}
	lua_getglobal (il -> lua, EV_FUNC);
	iflua_push_context (il);
	iflua_push_customer (il);
	lrc = lua_pcall (il -> lua, 2, 1, 0);
	if (lrc == 0) {
		rc = 0;
		if (lua_gettop (il -> lua) > 0)
			switch (lua_type (il -> lua, -1)) {
			default:
			case LUA_TNIL:		/* false */
				break;
			case LUA_TBOOLEAN:
				rc = lua_toboolean (il -> lua, -1);
				break;
			case LUA_TNUMBER:
				rc = lua_tonumber (il -> lua, -1) != (lua_Number) 0;
				break;
			case LUA_TSTRING:
				{
					const char	*s = lua_tostring (il -> lua, -1);
				
					if (s) {
						rc = strlen (s) > 0;
					}
				}
				break;
			case LUA_TUSERDATA:
				rc = alua_isnull (il -> lua, -1) ? 0 : 1;
				break;
			}
	} else {
		const char	*error = lua_tostring (il -> lua, -1);

		if (error) {
			log_out (il -> blockmail -> lg, LV_WARNING, "Failed to evaluate expression due to: %s", error);
		}
		rc = -1;
	}
	return rc;
}/*}}}*/
int
ev_lua_evaluate (void *ilp, receiver_t *rec, ...) /*{{{*/
{
	va_list	par;
	int	rc;
	
	va_start (par, rec);
	rc = ev_lua_vevaluate (ilp, rec, par);
	va_end (par);
	return rc;
}/*}}}*/
