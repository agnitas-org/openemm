/********************************************************************************************************************************************************************************************************************************************************************
 *                                                                                                                                                                                                                                                                  *
 *                                                                                                                                                                                                                                                                  *
 *        Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   *
 *                                                                                                                                                                                                                                                                  *
 *        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    *
 *        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           *
 *        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            *
 *                                                                                                                                                                                                                                                                  *
 ********************************************************************************************************************************************************************************************************************************************************************/
# ifndef	__ALUA_H
# define	__ALUA_H		1
# include	<time.h>
# include	<lua.h>
# include	<lualib.h>
# include	<lauxlib.h>
# include	"agn.h"

# define	LUA_AGNLIBNAME		"agn"
# define	LUA_SYSCFGLIBNAME	"syscfg"
# define	LUA_DATELIBNAME		"date"

# define	LUA_ENVIRON		"env"
# define	LUA_NULL		"null"

typedef enum {
	Sandbox = (1 << 0),
	Regular = (1 << 1),
	Worthy = (1 << 2)
}	trust_t;

# define	TRUST_ALL		(Sandbox | Regular | Worthy)
# define	TRUST_RESTRICT		(Sandbox | Regular)

typedef struct { /*{{{*/
	unsigned long	uid;
	struct tm	tt;
	/*}}}*/
}	alua_date_t;
typedef struct { /*{{{*/
	unsigned long	uid;
	/*}}}*/
}	alua_null_t;

extern bool_t		alua_date_parse (const char *str, struct tm *tt);
extern alua_date_t	*alua_pushdate (lua_State *lua, struct tm *tt);
extern alua_date_t	*alua_todate (lua_State *lua, int idx);
extern int		alua_isdate (lua_State *lua, int idx);

extern int		alua_isnull (lua_State *lua, int idx);
extern void		alua_pushnull (lua_State *lua);

extern void		alua_setup_libraries (lua_State *lua, trust_t trust);
extern void		alua_setup_function (lua_State *lua, const char *modname, const char *funcname, lua_CFunction func, void *closure);
extern lua_State	*alua_alloc (trust_t trust);
extern lua_State	*alua_free (lua_State *lua);
extern bool_t		alua_nload (lua_State *lua, const char *name, const void *code, size_t clen, int nargs, int nresults);
extern bool_t		alua_load (lua_State *lua, const char *name, const void *code, size_t clen);
extern int		alua_pcall (lua_State *lua, int nargs, int nresults, int msgh, int timeout);
# endif		/* __ALUA_H */
