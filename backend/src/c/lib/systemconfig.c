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
# include	<unistd.h>
# include	<fcntl.h>
# include	<sys/types.h>
# include	<sys/stat.h>
# include	<parson.h>
# include	"agn.h"

# define	CONFIG_ENV	"SYSTEM_CONFIG"
# define	PATH_CONFIG_ENV	"SYSTEM_CONFIG_PATH"
# define	PATH_CONFIG	"/home/openemm/etc/system.cfg"

typedef struct { /*{{{*/
	const char	*key;
	const char	*value;
	/*}}}*/
}	entry_t;
typedef struct buf { /*{{{*/
	char		*b;
	struct buf	*next;
	/*}}}*/
}	buf_t;
typedef struct { /*{{{*/
	char		*buf;
	JSON_Value	*json;
	entry_t		*e;
	int		count;
	int		size;
	buf_t		*statics;
	char		*scratch;
	/*}}}*/
}	config_t;
static const char *
config_buffer (config_t *c, const char *source) /*{{{*/
{
	buf_t	*b;
	
	if (b = (buf_t *) malloc (sizeof (buf_t))) {
		if (b -> b = strdup (source)) {
			b -> next = c -> statics;
			c -> statics = b;
			return b -> b;
		}
		free (b);
	}
	return NULL;
}/*}}}*/
void *
systemconfig_free (void *lc) /*{{{*/
{
	config_t	*c = (config_t *) lc;
	
	if (c) {
		buf_t	*tmp;
		
		if (c -> buf)
			free (c -> buf);
		if (c -> json)
			json_value_free (c -> json);
		if (c -> e)
			free (c -> e);
		while (tmp = c -> statics) {
			c -> statics = c -> statics -> next;
			if (tmp -> b)
				free (tmp -> b);
			free (tmp);
		}
		if (c -> scratch)
			free (c -> scratch);
		free (c);
	}
	return NULL;
}/*}}}*/
void *
systemconfig_alloc (const char *fname) /*{{{*/
{
	config_t	*c;
	int		fd;
	char		*buf;
	struct stat	st;
	
	if (c = (config_t *) malloc (sizeof (config_t))) {
		bool_t		ok = true;
		const char	*env = getenv (CONFIG_ENV);
		
		c -> buf = NULL;
		c -> json = NULL;
		c -> e = 0;
		c -> count = 0;
		c -> size = 0;
		c -> statics = NULL;
		c -> scratch = NULL;
		if (env) {
			if (! (c -> buf = strdup (env)))
				ok = false;
		} else {
			if (! fname) {
				fname = getenv (PATH_CONFIG_ENV);
				if (! fname) {
					fname = PATH_CONFIG;
# ifdef		PATH_LEGACY					
					if (access (fname, R_OK) == -1) {
						fname = PATH_LEGACY;
					}
# endif
				}
			}
			if ((fd = open (fname ? fname : PATH_CONFIG, O_RDONLY)) != -1) {
				if ((fstat (fd, & st) != -1) && S_ISREG (st.st_mode) && (buf = malloc (st.st_size + 1))) {
					int	count, n;
				
					for (count = 0; ok && (count < st.st_size); ) {
						n = read (fd, buf + count, st.st_size - count);
						if (n > 0) {
							count += n;
						} else {
							ok = false;
						}
					}
					if (ok) {
						buf[st.st_size] = '\0';
						c -> buf = buf;
					} else {
						free (buf);
					}
				}
				close (fd);
			}
		}
		if (ok)
			ok = systemconfig_parse (c);
		if (! ok)
			c = systemconfig_free (c);
	}
	return c;
}/*}}}*/
static bool_t
parse_plain (config_t *c) /*{{{*/
{
	bool_t	rc = true;
	char	*cur, *ptr;
	char	*var, *val;
	char	ch;
		
	for (ptr = c -> buf; ptr; ) {
		cur = ptr;
		if (ptr = strchr (ptr, '\n'))
			*ptr++ = '\0';
		while (isspace (*cur))
			++cur;
		if (cur[0] && (cur[0] != '#')) {
			var = cur;
			while (*cur && (*cur != '=') && (! isspace (*cur)))
				++cur;
			if (ch = *cur) {
				*cur++ = '\0';
				if (ch != '=') {
					while (isspace (*cur))
						++cur;
					ch = *cur;
					if (ch == '=')
						++cur;
				}
				if (ch == '=') {
					while (isspace (*cur))
						++cur;
					if (! strcmp (cur, "{")) {
						val = ptr;
						if (ptr) {
							char	*p1, *p2;

							while (*ptr) {
								if ((*ptr == '\n') && (*(ptr + 1) == '}') && (*(ptr + 2) == '\n')) {
									*ptr = '\0';
									ptr += 3;
									break;
								}
								++ptr;
							}
							for (p1 = p2 = val; isspace (*p1); ++p1)
								;
							while (*p1)
								if (*p1 == '\n') {
									*p2++ = *p1++;
									while (isspace (*p1))
										++p1;
								} else if (p1 != p2)
									*p2++ = *p1++;
								else
									++p1, ++p2;
							*p2 = '\0';
						}
					} else
						val = cur;
					if (val) {
						if (c -> size >= c -> count) {
							c -> size += 16;
							if (! (c -> e = (entry_t *) realloc (c -> e, c -> size * sizeof (entry_t)))) {
								rc = false;
								break;
							}
						}
						c -> e[c -> count].key = var;
						c -> e[c -> count].value = val;
						c -> count++;
					}
				}
			}
		}
	}
	return rc;
}/*}}}*/
static bool_t
parse_json (config_t *c) /*{{{*/
{
	bool_t	rc = false;
	
	if (json_value_get_type (c -> json) == JSONObject) {
		JSON_Object	*root = json_value_get_object (c -> json);
		
		if (root) {
			size_t	size = json_object_get_count (root);
			
			c -> size += size;
			if (c -> e = (entry_t *) realloc (c -> e, c -> size * sizeof (entry_t))) {
				int	n;
				
				for (n = 0; n < size; ++n) {
					const char	*name = json_object_get_name (root, n);
					JSON_Value	*jvalue = json_object_get_value_at (root, n);
					const char	*value;

					if (name && jvalue) {
						JSON_Value_Type	type = json_value_get_type (jvalue);
						
						if (type == JSONString) {
							value = json_value_get_string (jvalue);
						} else if ((type == JSONNull) || (type == JSONNumber) || (type == JSONBoolean)) {
							char	scratch[256];
							
							if (type == JSONNull) {
								scratch[0] = '\0';
							} else if (type == JSONNumber) {
								double	d = json_value_get_number (jvalue);
								
								if (((double) ((long) d)) == d) {
									snprintf (scratch, sizeof (scratch) - 1, "%ld", (long) d);
								} else {
									snprintf (scratch, sizeof (scratch) - 1, "%.3f", d);
								}
							} else if (type == JSONBoolean) {
								strcpy (scratch, json_value_get_boolean (jvalue) ? "true" : "false");
							} else {
								scratch[0] = '\0';
							}
							value = config_buffer (c, scratch);
						} else {
							value = NULL;
						}
						if (value) {
							c -> e[c -> count].key = name;
							c -> e[c -> count].value = value;
							c -> count++;
						}
					}
				}
				rc = true;
			}
		}
	}
	return rc;
}/*}}}*/
bool_t
systemconfig_parse (void *lc) /*{{{*/
{
	config_t	*c = (config_t *) lc;
	bool_t		rc;
	
	rc = true;
	if (c -> buf) {
		c -> json = json_parse_string (c -> buf);
		
		if (c -> json) {
			rc = parse_json (c);
		} else {
			rc = false;
		}
		if (! rc) {
			rc = parse_plain (c);
		}
	}
	return rc;
}/*}}}*/
const char *
systemconfig_find (void *lc, const char *key) /*{{{*/
{
	config_t	*c = (config_t *) lc;
	char		*dflt;
	int		n;

	if (c) {
		if (c -> scratch)
			free (c -> scratch);
		if (c -> scratch = strdup (key)) {
			if (dflt = strchr (c -> scratch, ':'))
				*dflt++ = '\0';
			if (c)
				for (n = 0; n < c -> count; ++n)
					if (! strcmp (c -> scratch, c -> e[n].key))
						return c -> e[n].value;
			return dflt;
		}
	}
	return NULL;
}/*}}}*/
bool_t
systemconfig_get (void *lc, int idx, const char **key, const char **value) /*{{{*/
{
	config_t	*c = (config_t *) lc;
	
	if (c && (idx >= 0) && (idx < c -> count)) {
		if (key)
			*key = c -> e[idx].key;
		if (value)
			*value = c -> e[idx].value;
		return true;
	}
	return false;
}/*}}}*/
