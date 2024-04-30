/********************************************************************************************************************************************************************************************************************************************************************
 *                                                                                                                                                                                                                                                                  *
 *                                                                                                                                                                                                                                                                  *
 *        Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   *
 *                                                                                                                                                                                                                                                                  *
 *        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    *
 *        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           *
 *        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            *
 *                                                                                                                                                                                                                                                                  *
 ********************************************************************************************************************************************************************************************************************************************************************/
# include	<stdlib.h>
# include	<unistd.h>
# include	<fcntl.h>
# include	<sys/types.h>
# include	<sys/stat.h>
# include	"agn.h"
# include	"alua.h"

static char *
readfile (const char *filename, int *length, bool_t silent) /*{{{*/
{
	char	*content;
	int	fd;

	content = NULL;
	if (filename && ((fd = open (filename, O_RDONLY)) != -1)) {
		struct stat	st;
				
		if ((fstat (fd, & st) != -1) && (st.st_size > 0) && (content = malloc (st.st_size + 1)) && (read (fd, content, st.st_size) == st.st_size)) {
			content[st.st_size] = '\0';
			if (length)
				*length = st.st_size;
		}
		close (fd);
	} else if (! silent)
		fprintf (stderr, "%s: failed to open: %m\n", filename);
	if ((! content) && length)
		*length = 0;
	return content;
}/*}}}*/
static int
execute_script (const char *filename, const char *script, char **argv, int argc) /*{{{*/
{
	int		rc = 127;
	const char	*name = filename ? filename : script;
	char		*buffer = NULL;

	if (! script) {
		if (buffer = readfile (filename, NULL, false)) {
			script = buffer;
			rc = 0;
		}
	} else 
		rc = 0;
	if (! rc) {
		lua_State	*lua;
		int		n;
		char		*path;
			
		if (! (lua = alua_alloc (Worthy))) {
			fprintf (stderr, "%s: failed to setup interpreter\n", name);
			rc = 1;
		} else {
			lua_createtable (lua, argc, 0);
			for (n = 0; n < argc; ++n) {
				lua_pushstring (lua, argv[n]);
				lua_seti (lua, -2, n + 1);
			}
			lua_setglobal (lua, "argv");
			for (n = 0; n < 2; ++n) {
				switch (n) {
				case 0:
					path = mkpath (path_home (), ".cqrc", NULL);
					break;
				case 1:
					path = mkpath (path_home (), "scripts", "cq.rc", NULL);
					break;
				default:
					path = NULL;
					break;
				}
				if (path) {
					char	*content;
					int	length;
					
					if (content = readfile (path, & length, true)) {
						if (! alua_load (lua, path, content, length)) {
							fprintf (stderr, "%s: failed to load rc script: %s\n", path, lua_tostring (lua, -1));
							rc = 2;
						}
						free (content);
					}
					free (path);
				}
			}
			if (! rc)
				if (! alua_nload (lua, name, script, strlen (script), 0, LUA_MULTRET)) {
					fprintf (stderr, "%s: failed to load script: %s\n", name, lua_tostring (lua, -1));
					rc = 3;
				} else {
					int	stack = lua_gettop (lua);
				
					rc = 0;
					if (stack > 0) {
						bool_t		nl = false;
						const char	*value;
						
						for (n = 0; n < stack; ++n)
							if (((n + 1) == stack) && lua_isnumber (lua, n + 1))
								rc = lua_tointeger (lua, n + 1);
							else if ((value = lua_tostring (lua, n + 1)) && *value) {
								fputs (value, stdout);
								nl = true;
							}
						if (nl)
							fputc ('\n', stdout);
						fflush (stdout);
					}
				}
		}
		alua_free (lua);
	} else
		fprintf (stderr, "%s: failed to setup script\n", name);
	if (buffer)
		free (buffer);
	return rc;
}/*}}}*/
static void
usage (const char *pgm) /*{{{*/
{
	fprintf (stderr, "Usage: %s [-d|<field>+|-e <script> [<param>]]\n", pgm);
}/*}}}*/
int
main (int argc, char **argv) /*{{{*/
{
	int		rc;
	int		n;
	const char	*ptr;
	bool_t		dump;
	bool_t		execute;
	const char	*script;
	systemconfig_t	*cfg;
	const char	*key, *value;
	
	dump = false;
	execute = false;
	script = NULL;
	if (argc && argv[0]) {
		if (ptr = strrchr (argv[0], '/'))
			++ptr;
		else
			ptr = argv[0];
		if (! strcmp (ptr, "config-script"))
			execute = true;
	}
	while ((n = getopt (argc, argv, "dec:?h")) != -1)
		switch (n) {
		case 'd':
			dump = true;
			break;
		case 'e':
			execute = true;
			break;
		case 'c':
			script = optarg;
			break;
		case '?':
		case 'h':
		default:
			return usage (argv[0]), (n != '?') && (n != 'h');
		}
	if (execute) {
		const char	*filename;
		
		if (! script) {
			if (optind == argc)
				return fprintf (stderr, "Missing script.\n"), 1;
			filename = argv[optind++];
		} else
			filename = NULL;
		return execute_script (filename, script, argv + optind, argc - optind);
	}
	if (! (cfg = systemconfig_alloc (true)))
		return fprintf (stderr, "Failed to setup config (%m).\n"), 1;
	rc = 0;
	if (dump) {
		if (optind == argc) {
			for (n = 0; systemconfig_get (cfg, n, & key, & value); ++n)
				printf ("%s=%s\n", key, value);
		} else {
			for (n = optind; n < argc; ++n) {
				value = systemconfig_find (cfg, argv[n]);
				if (value)
					printf ("%s=%s\n", argv[n], value);
				else
					printf ("%s\n", argv[n]);
			}
		}
	} else {
		for (n = optind; n < argc; ++n) {
			value = systemconfig_find (cfg, argv[n]);
			printf ("%s\n", (value ? value : ""));
			if (! value) {
				rc = 1;
			}
		}
	}
	systemconfig_free (cfg);
	return rc;
}/*}}}*/
