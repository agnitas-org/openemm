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
# include	<ctype.h>
# include	<unistd.h>
# include	<fcntl.h>
# include	<string.h>
# include	<sys/types.h>
# include	<sys/stat.h>
# include	"bav.h"

static bool_t
parse_config (cfg_t *c, char *buf, int len) /*{{{*/
{
	bool_t	rc;
	
	rc = false;
	if ((c -> amap = map_alloc (MAP_CaseIgnore, len > 5000 ? len / 100 : 47)) &&
	    (c -> hosts = set_alloc (true, 500))) {
		char	*cur, *ptr, *val;
		
		rc = true;
		for (ptr = buf; ptr && rc; ) {
			cur = ptr;
			if (ptr = strchr (ptr, '\n'))
				*ptr++ = '\0';
			while (isspace (*cur))
				++cur;
			if (*cur && (*cur != '#')) {
				val = skip (cur);
				if (*val) {
					if (! map_add (c -> amap, cur, val))
						rc = false;
					if (cur = strchr (cur, '@')) {
						++cur;
						if (*cur && (! set_add (c -> hosts, cur, strlen (cur))))
							rc = false;
					}
				}
			}
		}
	}
	return rc;
}/*}}}*/
static bool_t
read_config (cfg_t *c, const char *fname) /*{{{*/
{
	bool_t		rc;
	struct stat	st;
	char		*buf;
	int		fd;

	rc = false;
	if ((stat (fname, & st) != -1) && (buf = malloc (st.st_size + 1))) {
		if ((fd = open (fname, O_RDONLY)) != -1) {
			int	n, count;
			char	*ptr;
					
			for (ptr = buf, count = 0; count < st.st_size; )
				if ((n = read (fd, ptr, st.st_size - count)) > 0) {
					ptr += n;
					count += n;
				} else
					break;
			close (fd);
			if (count == st.st_size) {
				buf[count] = '\0';
				rc = parse_config (c, buf, count);
			}
		}
		free (buf);
	}
	return rc;
}/*}}}*/
cfg_t *
cfg_alloc (const char *fname) /*{{{*/
{
	cfg_t	*c;
	
	if (c = (cfg_t *) malloc (sizeof (cfg_t))) {
		c -> amap = NULL;
		c -> hosts = NULL;
		if (! read_config (c, fname))
			c = cfg_free (c);
	}
	return c;
}/*}}}*/
cfg_t *
cfg_free (cfg_t *c) /*{{{*/
{
	if (c) {
		if (c -> amap)
			map_free (c -> amap);
		if (c -> hosts)
			set_free (c -> hosts);
		free (c);
	}
	return NULL;
}/*}}}*/
char *
cfg_valid_address (cfg_t *c, const char *addr) /*{{{*/
{
	char	*rc;
	char	*copy;
	
	if (copy = strdup (addr)) {
		node_t	*found;
		char	*ptr;

		if (ptr = strchr (copy, '>'))
			*ptr = '\0';
		if (*copy == '<')
			ptr = copy + 1;
		else
			ptr = copy;
		for (found = NULL; (! found) && ptr; ) {
			if (found = map_find (c -> amap, ptr)) {
				if (! strncmp (found -> data, "alias:", 6)) {
					ptr = found -> data + 6;
					found = map_find (c -> amap, ptr);
				}
			} else if (*ptr != '@') {
				ptr = strchr (ptr, '@');
			} else
				ptr = NULL;
		}
		if (found)
			rc = strdup (found -> data);
		else {
			rc = NULL;
			if (ptr = strchr (copy, '@')) {
				++ptr;
				if (set_find (c -> hosts, ptr, strlen (ptr)))
					rc = strdup (ID_REJECT);
				else
					rc = strdup (ID_RELAY);
			}
			if (! rc)
				rc = strdup (ID_REJECT);
		}
		free (copy);
	} else
		rc = NULL;
	return rc;
}/*}}}*/
