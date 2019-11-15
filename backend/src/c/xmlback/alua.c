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
# include	<ctype.h>
# include	<unistd.h>
# include	<errno.h>
# include	<fnmatch.h>
# include	<regex.h>
# include	<iconv.h>
# include	"xmlback.h"
# include	"alua.h"

# define	ID_DATE_META		"__mt__date__"
# define	UID_DATE		0xda1e1d
# define	UID_NULL		0x48111d
# define	DEF_DIGEST		"SHA1"

extern char		**environ;
static regex_t		*parse_date = NULL;
static int		parse_count = 0;
static const char	*parse_pattern[] = { /*{{{*/
	"^([0-9]{4})-([0-9]{2})-([0-9]{2})([^0-9]([0-9]{2}):([0-9]{2}):([0-9]{2}))?$",
	"^([0-9]{1,2})\\.([0-9]{1,2})\\.([0-9]{2,4})([ \t]+([0-9]{2}):([0-9]{2}):([0-9]{2}))?$"
	/*}}}*/
};
static int		parse_pos[][6] = { /*{{{*/
	{	1, 2, 3, 5, 6, 7	},
	{	3, 2, 1, 5, 6, 7	}
	/*}}}*/
};

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
	case 'a':
	case 'A':	return 10;
	case 'b':
	case 'B':	return 11;
	case 'c':
	case 'C':	return 12;
	case 'd':
	case 'D':	return 13;
	case 'e':
	case 'E':	return 14;
	case 'f':
	case 'F':	return 15;
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
static inline time_t
norm (struct tm *tt) /*{{{*/
{
	if (tt) {
		tt -> tm_wday = -1;
		tt -> tm_yday = -1;
		tt -> tm_isdst = -1;
		return	mktime (tt);
	}
	return (time_t) -1;
}/*}}}*/
static inline bool_t
bnorm (struct tm *tt) /*{{{*/
{
	return norm (tt) == (time_t) -1 ? false : true;
}/*}}}*/
static char *
charset_convert (const char *s, int len, const char *from_charset, const char *to_charset, int *rlen) /*{{{*/
{
	char		*rc;
	iconv_t		*ic;
	
	rc = NULL;
	if (rlen)
		*rlen = 0;
	if ((ic = iconv_open (to_charset, from_charset)) != (iconv_t) -1) {
		char	*inbuf, *inptr, *outbuf, *outptr;
		size_t	ilen, olen, ifree, ofree, n;

		if (inbuf = malloc (len + 1)) {
			if (len > 0)
				memcpy (inbuf, s, len);
			inbuf[len] = '\0';
			ilen = len;
			outbuf = NULL;
			olen = 0;
			do {
				inptr = inbuf;
				ifree = ilen;
				olen += ilen + 256;
				if (! (outbuf = realloc (outbuf, olen + 1)))
					break;
				outptr = outbuf;
				ofree = olen;
				n = iconv (ic, & inptr, & ifree, & outptr, & ofree);
			}	while ((n == (size_t) -1) && (errno == E2BIG));
			if (outbuf) {
				if (n != (size_t) -1) {
					rc = outbuf;
					outbuf[olen - ofree] = '\0';
					if (rlen)
						*rlen = olen - ofree;
				} else
					free (outbuf);
			}
			free (inbuf);
		}
		iconv_close (ic);
	}
	return rc;
}/*}}}*/
bool_t
alua_date_parse (const char *str, struct tm *tt) /*{{{*/
{
	bool_t		rc;
	int		n, m;
	regmatch_t	sub[8];
	
	rc = false;
	for (n = 0; (n < parse_count) && (! rc); ++n)
		if (regexec (parse_date + n, str, sizeof (sub) / sizeof (sub[0]), sub, 0) == 0) {
			rc = true;
			tt -> tm_sec = 0;
			tt -> tm_min = 0;
			tt -> tm_hour = 0;
			tt -> tm_mday = 0;
			tt -> tm_mon = 0;
			tt -> tm_year = 70;
			for (m = 0; m < 6; ++m) {
				regmatch_t	*s = sub + parse_pos[n][m];
				int		val;
				
				if ((s -> rm_so != -1) && (s -> rm_eo != -1)) {
					val = numparse (str + s -> rm_so, s -> rm_eo - s -> rm_so);
					switch (m) {
					case 0:
						tt -> tm_year = val < 1900 ? val + 100 : val - 1900;
						break;
					case 1:
						tt -> tm_mon = val - 1;
						break;
					case 2:
						tt -> tm_mday = val;
						break;
					case 3:
						tt -> tm_hour = val;
						break;
					case 4:
						tt -> tm_min = val;
						break;
					case 5:
						tt -> tm_sec = val;
						break;
					}
				}
			}
			rc = bnorm (tt);
		}
	return rc;
}/*}}}*/
alua_date_t *
alua_pushdate (lua_State *lua, struct tm *tt) /*{{{*/
{
	alua_date_t	*date;
	
	date = lua_newuserdata (lua, sizeof (alua_date_t));
	date -> uid = UID_DATE;
	if (tt)
		date -> tt = *tt;
	else
		memset (& date -> tt, 0, sizeof (date -> tt));
	lua_getfield (lua, LUA_REGISTRYINDEX, ID_DATE_META);
	lua_setmetatable (lua, -2);
	return date;
}/*}}}*/
alua_date_t *
alua_todate (lua_State *lua, int idx) /*{{{*/
{
	alua_date_t	*date = (alua_date_t *) lua_touserdata (lua, idx);
	
	return date && (date -> uid == UID_DATE) ? date : NULL;
}/*}}}*/
int
alua_isdate (lua_State *lua, int idx) /*{{{*/
{
	return lua_isuserdata (lua, idx) && alua_todate (lua, idx);
}/*}}}*/
static int
get_date_index (const char *field) /*{{{*/
{
	if (! strcmp (field, "year"))
		return 1;
	else if ((! strcmp (field, "mon")) || (! strcmp (field, "month")))
		return 2;
	else if (! strcmp (field, "day"))
		return 3;
	else if (! strcmp (field, "hour"))
		return 4;
	else if ((! strcmp (field, "min")) || (! strcmp (field, "minute")))
		return 5;
	else if ((! strcmp (field, "sec")) || (! strcmp (field, "second")))
		return 6;
	return -1;
}/*}}}*/
static void
set_date (alua_date_t *date, int idx, int val) /*{{{*/
{
	switch (idx) {
	case 1:		date -> tt.tm_year = val > 1900 ? val - 1900 : val;	break;
	case 2:		date -> tt.tm_mon = val - 1;				break;
	case 3:		date -> tt.tm_mday = val;				break;
	case 4:		date -> tt.tm_hour = val;				break;
	case 5:		date -> tt.tm_min = val;				break;
	case 6:		date -> tt.tm_sec = val;				break;
	}
}/*}}}*/
static int
addsub_date (lua_State *lua, int sign) /*{{{*/
{
	alua_date_t	*date = alua_todate (lua, -2);
	bool_t		rc;
	
	rc = false;
	if (date && lua_isnumber (lua, -1)) {
		long		offset;
		struct tm	tt, *ntt;
		time_t		ts;
		
		offset = (long) (lua_tonumber (lua, -1) * sign * 24 * 60 * 60);
		tt = date -> tt;
		if ((ts = norm (& tt)) != (time_t) -1) {
			ts += offset;
			if (ntt = localtime (& ts)) {
				alua_pushdate (lua, ntt);
				rc = true;
			}
		}
	}
	if (! rc)
		lua_pushnil (lua);
	return 1;
}/*}}}*/
static int
alua_date_meta_add (lua_State *lua) /*{{{*/
{
	return addsub_date (lua, 1);
}/*}}}*/
static int
alua_date_meta_sub (lua_State *lua) /*{{{*/
{
	return addsub_date (lua, -1);
}/*}}}*/
static int
alua_date_meta_len (lua_State *lua) /*{{{*/
{
	alua_date_t	*date = alua_todate (lua, -1);

	lua_pushinteger (lua, date ? (int) mktime (& date -> tt) : 0);
	return 1;
}/*}}}*/
static int
cmp_date (lua_State *lua) /*{{{*/
{
	alua_date_t	*a, *b;
	int		rc;
	
	rc = 1;
	if ((a = alua_todate (lua, -2)) && (b = alua_todate (lua, -1))) {
		struct tm	tt;
		time_t		av, bv;
		
		tt = a -> tt;
		av = norm (& tt);
		tt = b -> tt;
		bv = norm (& tt);
		if ((av != (time_t) -1) && (bv != (time_t) -1))
			rc = av - bv;
	}
	return rc;
}/*}}}*/
static int
alua_date_meta_eq (lua_State *lua) /*{{{*/
{
	return cmp_date (lua) == 0;
}/*}}}*/
static int
alua_date_meta_lt (lua_State *lua) /*{{{*/
{
	return cmp_date (lua) < 0;
}/*}}}*/
static int
alua_date_meta_le (lua_State *lua) /*{{{*/
{
	return cmp_date (lua) <= 0;
}/*}}}*/
static int
alua_date_meta_index (lua_State *lua) /*{{{*/
{
	alua_date_t	*date = alua_todate (lua, -2);
	int		idx;
	const char	*field;
	
	idx = 0;
	if (date)
		switch (lua_type (lua, -1)) {
		case LUA_TNUMBER:
			idx = lua_tointeger (lua, -1);
			break;
		case LUA_TSTRING:
			field = lua_tostring (lua, -1);
			idx = get_date_index (field);
			if (idx == -1) {
				lua_getglobal (lua, LUA_DATELIBNAME);
				lua_getfield (lua, -1, field);
				lua_remove (lua, -2);
			}
			break;
		}
	switch (idx) {
	default:	lua_pushnil (lua);					break;
	case -1:	/* value pushed elsewhere on stack */			break;
	case 1:		lua_pushinteger (lua, date -> tt.tm_year + 1900);	break;
	case 2:		lua_pushinteger (lua, date -> tt.tm_mon + 1);		break;
	case 3:		lua_pushinteger (lua, date -> tt.tm_mday);		break;
	case 4:		lua_pushinteger (lua, date -> tt.tm_hour);		break;
	case 5:		lua_pushinteger (lua, date -> tt.tm_min);		break;
	case 6:		lua_pushinteger (lua, date -> tt.tm_sec);		break;
	}
	return 1;
}/*}}}*/
static int
alua_date_meta_newindex (lua_State *lua) /*{{{*/
{
	alua_date_t	*date = alua_todate (lua, -3);
	
	if (date)
		if (lua_isnumber (lua, -1)) {
			int	val = lua_tointeger (lua, -1);
			int	idx;
		
			switch (lua_type (lua, -2)) {
			case LUA_TNUMBER:
				idx = lua_tointeger (lua, -2);
				break;
			case LUA_TSTRING:
				idx = get_date_index (lua_tostring (lua, -2));
				break;
			default:
				idx = -1;
				break;
			}
			set_date (date, idx, val);
		}
	return 0;
}/*}}}*/
static int
alua_date_meta_tostring (lua_State *lua) /*{{{*/
{
	alua_date_t	*date = alua_todate (lua, -1);
	
	if (date) {
		char	scratch[128];
		int	len;
		
		len = snprintf (scratch, sizeof (scratch) - 1, "%d.%02d.%04d %02d:%02d:%02d",
				date -> tt.tm_mday, date -> tt.tm_mon + 1, date -> tt.tm_year + 1900,
				date -> tt.tm_hour, date -> tt.tm_min, date -> tt.tm_sec);
		if (len != -1)
			lua_pushlstring (lua, scratch, len);
		else
			lua_pushnil (lua);
	} else
		lua_pushnil (lua);
	return 1;
}/*}}}*/
static int
alua_date_date (lua_State *lua) /*{{{*/
{
	alua_date_t	*date;
	int		stack;
	
	stack = lua_gettop (lua);
	date = alua_pushdate (lua, NULL);
	if ((stack == 0) || ((stack == 1) && lua_isnil (lua, 1))) {
		time_t		now;
		struct tm	*tt;
		
		time (& now);
		if (tt = localtime (& now))
			date -> tt = *tt;
	} else if (stack == 1) {
		if (alua_isdate (lua, 1)) {
			alua_date_t	*odate = alua_todate (lua, 1);
		
			date -> tt = odate -> tt;
		} else if (lua_istable (lua, 1)) {
			lua_pushnil (lua);
			while (lua_next (lua, 1)) {
				if (lua_isnumber (lua, -1)) {
					int	val = lua_tointeger (lua, -1);
					int	idx;
				
					switch (lua_type (lua, -2)) {
					case LUA_TNUMBER:
						idx = lua_tointeger (lua, -2);
						break;
					case LUA_TSTRING:
						idx = get_date_index (lua_tostring (lua, -2));
						break;
					default:
						idx = -1;
						break;
					}
					set_date (date, idx, val);
				}
				lua_pop (lua, 1);
			}
		} else if (lua_isnumber (lua, 1)) {
			time_t		ts;
			struct tm	*tt;
			
			ts = lua_tointeger (lua, 1);
			if (tt = localtime (& ts))
				date -> tt = *tt;
		} else if (lua_isstring (lua, 1)) {
			alua_date_parse (lua_tostring (lua, 1), & date -> tt);
		}
	} else if ((stack >= 3) && (stack <= 6)) {
		int	n;
		
		for (n = 1; n <= stack; ++n)
			set_date (date, n, lua_tointeger (lua, n));
	}
	return 1;
}/*}}}*/
static int
alua_date_norm (lua_State *lua) /*{{{*/
{
	alua_date_t	*date = alua_todate (lua, -1);
	bool_t		rc;
	
	if (date)
		rc = bnorm (& date -> tt);
	else
		rc = false;
	lua_pushboolean (lua, rc);
	return 1;
}/*}}}*/
static int
alua_date_epoch (lua_State *lua) /*{{{*/
{
	time_t	rc;
	
	if (lua_gettop (lua) == 0)
		time (& rc);
	else {
		alua_date_t	*date = alua_todate (lua, -1);
	
		if (date) {
			struct tm	tt = date -> tt;
			
			rc = norm (& tt);
		} else
			rc = -1;
	}
	lua_pushinteger (lua, rc);
	return 1;
}/*}}}*/
static int
alua_date_format (lua_State *lua) /*{{{*/
{
	int		stack = lua_gettop (lua);
	alua_date_t	*date;
	struct tm	*tt;
	struct tm	temp;
	const char	*format;
	
	tt = NULL;
	format = NULL;
	if ((stack == 0) || ((stack > 0) && lua_isstring (lua, -1))) {
		if (stack < 2) {
			time_t	now;

			time (& now);
			tt = localtime (& now);
		}
		if (stack > 0)
			format = lua_tostring (lua, -1);
	}
	if ((stack == 0) || (stack > 0) && alua_isdate (lua, -stack)) {
		if (stack < 2)
			format = "%c";
		if (stack > 0) {
			date = alua_todate (lua, -stack);
			temp = date -> tt;
			norm (& temp);
			tt = & temp;
		}
	}
	if (format && tt) {
		char	scratch[512];
		
		strftime (scratch, sizeof (scratch) - 1, format, tt);
		lua_pushstring (lua, scratch);
	} else
		lua_pushnil (lua);
	return 1;
}/*}}}*/
static struct { /*{{{*/
	const char	*funcname;
	lua_CFunction	func;
	/*}}}*/
}	date_metatab[] = { /*{{{*/
	{	"__add",	alua_date_meta_add	},
	{	"__sub",	alua_date_meta_sub	},
	{	"__len",	alua_date_meta_len	},
	{	"__eq",		alua_date_meta_eq	},
	{	"__lt",		alua_date_meta_lt	},
	{	"__le",		alua_date_meta_le	},
	{	"__index",	alua_date_meta_index	},
	{	"__newindex",	alua_date_meta_newindex	},
	{	"__tostring",	alua_date_meta_tostring	}
	/*}}}*/
},	date_functab[] = { /*{{{*/
	{	"date",		alua_date_date		},
	{	"norm",		alua_date_norm		},
	{	"epoch",	alua_date_epoch		},
	{	"format",	alua_date_format	}
	/*}}}*/
};
static void
alua_date_setup (lua_State *lua) /*{{{*/
{
	int	n;
	
	lua_createtable (lua, 0, 0);
	for (n = 0; n < sizeof (date_metatab) / sizeof (date_metatab[0]); ++n) {
		lua_pushcfunction (lua, date_metatab[n].func);
		lua_setfield (lua, -2, date_metatab[n].funcname);
	}
	lua_setfield (lua, LUA_REGISTRYINDEX, ID_DATE_META);
	lua_createtable (lua, 0, 0);
	for (n = 0; n < sizeof (date_functab) / sizeof (date_functab[0]); ++n) {
		lua_pushcfunction (lua, date_functab[n].func);
		lua_setfield (lua, -2, date_functab[n].funcname);
	}
	lua_setglobal (lua, LUA_DATELIBNAME);
	if (parse_count == 0) {
		parse_count = sizeof (parse_pattern) / sizeof (parse_pattern[0]);
		
		if (parse_date = (regex_t *) malloc (parse_count * sizeof (regex_t))) {
			for (n = 0; n < parse_count; ++n)
				regcomp (parse_date + n, parse_pattern[n], REG_EXTENDED);
		} else
			parse_count = 0;
	}
}/*}}}*/

static int
alua_env_meta_len (lua_State *lua) /*{{{*/
{
	int	n;
	
	n = 0;
	if (environ)
		for (; environ[n]; ++n)
			;
	lua_pushnumber (lua, n);
	return 1;
}/*}}}*/
static int
alua_env_meta_tostring (lua_State *lua) /*{{{*/
{
	buffer_t	*scratch;
	
	if (environ && environ[0] && (scratch = buffer_alloc (1024))) {
		int		n;
		
		for (n = 0; environ[n]; ++n) {
			buffer_appends (scratch, environ[n]);
			buffer_appendch (scratch, '\n');
		}
		lua_pushlstring (lua, buffer_string (scratch), buffer_length (scratch));
		buffer_free (scratch);
	} else
		lua_pushliteral (lua, "");
	return 1;
}/*}}}*/
static int
alua_env_meta_index (lua_State *lua) /*{{{*/
{
	const char	*field;
	char		*data;
	
	data = NULL;
	if (field = lua_tostring (lua, -1))
		data = getenv (field);
	else
		data = NULL;
	if (data)
		lua_pushstring (lua, data);
	else
		lua_pushnil (lua);
	return 1;
}/*}}}*/
static int
alua_env_meta_newindex (lua_State *lua) /*{{{*/
{
	const char	*var, *val;

	if (var = lua_tostring (lua, -2)) {
		if (lua_isnil (lua, -1))
			unsetenv (var);
		else if (val = lua_tostring (lua, -1))
			setenv (var, val, 1);
	}
	return 0;
}/*}}}*/
static int
alua_env_meta_call (lua_State *lua) /*{{{*/
{
	int		stack = lua_gettop (lua);
	const char	*var;
	
	if (stack <= 1) {
		extern char	**environ;
		const char	*ptr;
		char		*scratch;
		int		ssize;
		int		len;
		int		n;
		
		lua_createtable (lua, 0, 0);
		scratch = NULL;
		ssize = 0;
		for (n = 0; environ[n]; ++n)
			if (ptr = strchr (environ[n], '=')) {
				len = ptr - environ[n];
				if (ssize < len) {
					ssize = len + 16;
					if (! (scratch = realloc (scratch, ssize + 1)))
						ssize = 0;
				}
				if (ssize >= len) {
					strncpy (scratch, environ[n], len);
					scratch[len] = '\0';
					lua_pushstring (lua, ptr + 1);
					lua_setfield (lua, -2, scratch);
				}
			}
		if (scratch)
			free (scratch);
	} else if (var = lua_tostring (lua, 2)) {
		const char	*val;
		
		if (! (val = getenv (var)))
			if (stack > 2)
				val = lua_tostring (lua, -1);
		if (val)
			lua_pushstring (lua, val);
		else
			lua_pushnil (lua);
	} else
		lua_pushnil (lua);
	return 1;
}/*}}}*/
static struct { /*{{{*/
	const char	*funcname;
	lua_CFunction	func;
	/*}}}*/
}	env_metatab[] = { /*{{{*/
	{	"__len",	alua_env_meta_len	},
	{	"__tostring",	alua_env_meta_tostring	},
	{	"__index",	alua_env_meta_index	},
	{	"__newindex",	alua_env_meta_newindex	},
	{	"__call",	alua_env_meta_call	}
	/*}}}*/
};
static void
alua_env_setup (lua_State *lua) /*{{{*/
{
	void	*data;
	int	n;

	if (data = lua_newuserdata (lua, sizeof (LUA_ENVIRON))) {
		memcpy (data, LUA_ENVIRON, sizeof (LUA_ENVIRON));
		lua_createtable (lua, 0, 0);
		for (n = 0; n < sizeof (env_metatab) / sizeof (env_metatab[0]); ++n) {
			lua_pushcfunction (lua, env_metatab[n].func);
			lua_setfield (lua, -2, env_metatab[n].funcname);
		}
		lua_setmetatable (lua, -2);
		lua_setglobal (lua, LUA_ENVIRON);
	}
}/*}}}*/

int
alua_isnull (lua_State *lua, int idx) /*{{{*/
{
	if (lua_isuserdata (lua, idx)) {
		alua_null_t	*null = (alua_null_t *) lua_touserdata (lua, idx);
		
		if (null && (null -> uid == UID_NULL))
			return 1;
	}
	return 0;
}/*}}}*/
void
alua_pushnull (lua_State *lua) /*{{{*/
{
	lua_getglobal (lua, LUA_NULL);
}/*}}}*/
static int
alua_null_meta_len (lua_State *lua) /*{{{*/
{
	lua_pushnumber (lua, 0);
	return 1;
}/*}}}*/
static int
alua_null_meta_tostring (lua_State *lua) /*{{{*/
{
	lua_pushliteral (lua, "");
	return 1;
}/*}}}*/
static struct { /*{{{*/
	const char	*funcname;
	lua_CFunction	func;
	/*}}}*/
}	null_metatab[] = { /*{{{*/
	{	"__len",	alua_null_meta_len	},
	{	"__tostring",	alua_null_meta_tostring	}
	/*}}}*/
};
static void
alua_null_setup (lua_State *lua) /*{{{*/
{
	alua_null_t	*null;
	int		n;
	
	if (null = lua_newuserdata (lua, sizeof (alua_null_t))) {
		null -> uid = UID_NULL;
		lua_createtable (lua, 0, 0);
		for (n = 0; n < sizeof (null_metatab) / sizeof (null_metatab[0]); ++n) {
			lua_pushcfunction (lua, null_metatab[n].func);
			lua_setfield (lua, -2, null_metatab[n].funcname);
		}
		lua_setmetatable (lua, -2);
		lua_setglobal (lua, LUA_NULL);
	}
}/*}}}*/

static int
alua_type (lua_State *lua) /*{{{*/
{
	const char	*str;
	int		ltype;

	if (lua_gettop (lua) > 0)
		ltype = lua_type (lua, -1);
	else
		ltype = LUA_TNONE;
	if ((ltype == LUA_TUSERDATA) && alua_isdate (lua, -1))
		str = "date";
	else if ((ltype == LUA_TUSERDATA) && alua_isnull (lua, -1))
		str = "null";
	else
		str = lua_typename (lua, ltype);
	lua_pushstring (lua, str);
	return 1;
}/*}}}*/
static struct { /*{{{*/
	const char	*libname;
	lua_CFunction	libfunc;
	/*}}}*/
}	alua_libtab[] = { /*{{{*/
	{	"",			luaopen_base		},
	{	LUA_STRLIBNAME,		luaopen_string		},
# ifdef		LUA_UTF8LIBNAME
	{	LUA_UTF8LIBNAME,	luaopen_utf8		},
# endif		/* LUA_UTF8LIBNAME */
	{	LUA_TABLIBNAME,		luaopen_table		},
	{	LUA_MATHLIBNAME,	luaopen_math		},
	{	LUA_COLIBNAME,		luaopen_coroutine	}
	/*}}}*/
};
static struct { /*{{{*/
	const char	*modname;
	const char	*funcname;
	lua_CFunction	func;
	/*}}}*/
}	alua_functab[] = { /*{{{*/
	{	NULL,			"type",		alua_type	}
	/*}}}*/
};
# define	FTSIZE		(sizeof (alua_functab) / sizeof (alua_functab[0]))

void
alua_setup_libraries (lua_State *lua) /*{{{*/
{
	int		n;
	const char	*modname;
	
	for (n = 0; n < sizeof (alua_libtab) / sizeof (alua_libtab[0]); ++n) {
# if	LUA_VERSION_NUM >= 502
		luaL_requiref (lua, alua_libtab[n].libname, alua_libtab[n].libfunc, 1);
		lua_pop (lua, 1);
# else		
		lua_pushcfunction (lua, alua_libtab[n].libfunc);
		lua_pushstring (lua, alua_libtab[n].libname);
		lua_call (lua, 1, 0);
# endif		
	}
	alua_date_setup (lua);
	alua_env_setup (lua);
	alua_null_setup (lua);
	modname = NULL;
	for (n = 0; n <= FTSIZE; ++n) {
		if ((n == FTSIZE) || alua_functab[n].modname) {
			if (modname) {
				lua_pop (lua, 1);
				modname = NULL;
			}
			if (n < FTSIZE) {
				modname = alua_functab[n].modname;
				lua_getglobal (lua, modname);
				if (lua_isnil (lua, -1)) {
					lua_pop (lua, 1);
					lua_createtable (lua, 0, 1);
					lua_pushvalue (lua, -1);
					lua_setglobal (lua, modname);
				}
			}
		}
		if (n < FTSIZE) {
			lua_pushcfunction (lua, alua_functab[n].func);
			if (modname)
				lua_setfield (lua, -2, alua_functab[n].funcname);
			else
				lua_setglobal (lua, alua_functab[n].funcname);
		}
	}
}/*}}}*/
void
alua_setup_function (lua_State *lua, const char *modname, const char *funcname, lua_CFunction func, void *closure) /*{{{*/
{
	if (modname) {
		lua_getglobal (lua, modname);
		if (lua_isnil (lua, -1)) {
			lua_pop (lua, 1);
			lua_createtable (lua, 0, 0);
			lua_pushvalue (lua, -1);
			lua_setglobal (lua, modname);
		}
	}
	if (closure)
		lua_pushlightuserdata (lua, closure);
	lua_pushcclosure (lua, func, closure ? 1 : 0);
	if (modname) {
		lua_setfield (lua, -2, funcname);
		lua_pop (lua, 1);
	} else
		lua_setglobal (lua, funcname);
}/*}}}*/
static void *
alua_allocator (void *dummy, void *ptr, size_t osize, size_t nsize) /*{{{*/
{
	if (nsize == 0) {
		free (ptr);
		return NULL;
	}
	return realloc (ptr, nsize);
}/*}}}*/
static int
alua_panic (lua_State *lua) /*{{{*/
{
	return 0;
}/*}}}*/
lua_State *
alua_alloc (void) /*{{{*/
{
	lua_State	*lua;
	
	if (lua = lua_newstate (alua_allocator, NULL)) {
		lua_atpanic (lua, alua_panic);
		alua_setup_libraries (lua);
	}
	return lua;
}/*}}}*/
void
alua_free (lua_State *lua) /*{{{*/
{
	if (lua)
		lua_close (lua);
}/*}}}*/

typedef struct { /*{{{*/
	const void	*code;
	size_t		clen;
	size_t		sent;
	/*}}}*/
}	reader_t;
static const char *
alua_reader (lua_State *lua, void *rdp, size_t *size) /*{{{*/
{
	reader_t	*rd = (reader_t *) rdp;
	
	if (rd -> clen == rd -> sent) {
		*size = 0;
		return NULL;
	}
	rd -> sent = rd -> clen;
	*size = rd -> sent;
	return rd -> code;
}/*}}}*/
bool_t
alua_load (lua_State *lua, const char *name, const void *code, size_t clen) /*{{{*/
{
	reader_t	rd;
	bool_t		rc;
	
	rd.code = code;
	rd.clen = clen;
	rd.sent = 0;
# if	LUA_VERSION_NUM >= 502
	if ((lua_load (lua, alua_reader, & rd, name, NULL) == 0) && (lua_pcall (lua, 0, 0, 0) == 0))
# else	
	if ((lua_load (lua, alua_reader, & rd, name) == 0) && (lua_pcall (lua, 0, 0, 0) == 0))
# endif
		rc = true;
	else
		rc = false;
	return rc;
}/*}}}*/
