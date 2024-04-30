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

# define	VERSION_DEFAULT		"unknown"

build_t *
build_alloc (void) /*{{{*/
{
	build_t	*b;
	char	*path;
	
	if (b = (build_t *) malloc (sizeof (build_t))) {
		b -> version = NULL;
		if (path = mkpath (path_home (), "scripts", "build.spec", NULL)) {
			int		fd;
			struct stat	st;
			int		n;
			char		*ptr;
			
			if ((fd = open (path, O_RDONLY)) != -1) {
				if ((fstat (fd, & st) != -1) && (st.st_size > 0) && (b -> version = malloc (st.st_size + 1))) {
					if ((n = read (fd, b -> version, st.st_size)) == st.st_size) {
						while ((n > 0) && strchr (" \t\r\n\v", (b -> version[n - 1])))
							--n;
						b -> version[n] = '\0';
						b -> timestamp = b -> host = b -> user = b -> version + n;
						if (ptr = strchr (b -> version, ';')) {
							*ptr++ = '\0';
							b -> timestamp = ptr;
							if (ptr = strchr (ptr, ';')) {
								*ptr++ = '\0';
								b -> host = ptr;
								if (ptr = strchr (ptr, ';')) {
									*ptr++ = '\0';
									b -> user = ptr;
								}
							}
						}
					} else {
						free (b -> version);
						b -> version = NULL;
					}
				}
				close (fd);
			}
			free (path);
		}
		if (! b -> version) {
			b -> timestamp = b -> host = b -> user = b -> version = strdup (VERSION_DEFAULT);
			if (! b -> version)
				b = build_free (b);
		}
	}
	return b;
}/*}}}*/
build_t *
build_free (build_t *b) /*{{{*/
{
	if (b) {
		if (b -> version)
			free (b -> version);
		free (b);
	}
	return NULL;
}/*}}}*/

