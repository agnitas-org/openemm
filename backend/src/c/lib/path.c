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
/** @file path.c
 * Path related support routines
 */
# include	<stdlib.h>
# include	<pwd.h>
# include	<sys/types.h>
# include	<sys/stat.h>
# include	<errno.h>
# include	"agn.h"

static char	home[PATH_MAX + 1] = "";
const char *
path_home (void) /*{{{*/
{
	if (! home[0]) {
		const char	*env;
		
		home[0] = '.';
		home[1] = '\0';
		if (env = getenv ("HOME")) {
			strncpy (home, env, sizeof (home) - 1);
		} else {
			struct passwd	*pw;
			
			setpwent ();
			pw = getpwuid (getuid ());
			if (pw -> pw_dir) {
				strncpy (home, pw -> pw_dir, sizeof (home) - 1);
			}
			endpwent ();
		}
		home[sizeof (home) - 1] = '\0';
	}
	return home;
}/*}}}*/
char *
mkpath (const char *start, ...) /*{{{*/
{
	va_list		par;
	char		path[PATH_MAX + 1];
	int		room;
	char		*ptr;
	const char	*elem;
	int		elen;
	
	va_start (par, start);
	room = sizeof (path) - 1;
	for (elem = start, ptr = path; elem; elem = va_arg (par, const char *)) {
		elen = strlen (elem);
		if (elen + 1 >= room)
			break;
		if (ptr != path) {
			*ptr++ = PATH_SEP;
			--room;
		}
		memcpy (ptr, elem, elen);
		ptr += elen;
		room -= elen;
	}
	va_end (par);
	*ptr = '\0';
	return elem ? NULL : strdup (path);
}/*}}}*/
bool_t
mkdirs (const char *path, int mode) /*{{{*/
{
	bool_t		rc;
	int		state;
	struct stat	st;
	
	rc = false;
	for (state = 0; state < 2; ++state) {
		if (stat (path, & st) == -1) {
			if ((state == 0) && (errno == ENOENT)) {
				const char	*ptr;
				char		*npath;
				int		len;
				bool_t		ok;
				
				if ((ptr = strrchr (path, '/')) && (ptr != path) && (npath = malloc (ptr - path + 1))) {
					len = ptr - path;
					strncpy (npath, path, len);
					npath[len] = '\0';
					ok = mkdirs (npath, mode);
					free (npath);
					if (! ok)
						break;
				}
				if ((mkdir (path, mode) == -1) && (errno != EEXIST))
					break;
			} else
				break;
		} else {
			if (S_ISDIR (st.st_mode))
				rc = true;
			break;
		}
	}
	return rc;
}/*}}}*/
