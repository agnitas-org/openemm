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
# include	<stdlib.h>
# include	<ctype.h>
# include	<unistd.h>
# include	<fcntl.h>
# include	<sys/types.h>
# include	<sys/stat.h>
# include	<sys/utsname.h>
# include	<pwd.h>
# include	<parson.h>
# include	"agn.h"

# define	CONFIG_ENV	"SYSTEM_CONFIG"
# define	PATH_CONFIG_ENV	"SYSTEM_CONFIG_PATH"
# define	PATH_LOCAL	"etc/system.cfg"
# define	PATH_OSRELEASE	"/etc/os-release"

typedef struct { /*{{{*/
	char	*key;
	char	*value;
	/*}}}*/
}	entry_t;
static entry_t *
entry_free (entry_t *e) /*{{{*/
{
	if (e) {
		if (e -> key)
			free (e -> key);
		if (e -> value)
			free (e -> value);
		free (e);
	}
	return NULL;
}/*}}}*/
static entry_t *
entry_alloc (const char *key, const char *value) /*{{{*/
{
	entry_t	*e;
	
	if (key && value) {
		if (e = (entry_t *) malloc (sizeof (entry_t))) {
			e -> key = strdup (key);
			e -> value = strdup (value);
			if ((! e -> key) || (! e -> value))
				e = entry_free (e);
		}
	} else
		e = NULL;
	return e;
}/*}}}*/

typedef struct { /*{{{*/
	char	**indexes;
	int	size;
	int	count;
	int	max_length;
	char	*scratch;
	int	scratch_size;
	/*}}}*/
}	selection_t;

static void
selection_adds (selection_t *s, const char *index) /*{{{*/
{
	if (s -> count >= s -> size) {
		s -> size += s -> size ? s -> size : 16;
		if (! (s -> indexes = (char **) realloc (s -> indexes, sizeof (char *) * s -> size))) {
			s -> size = 0;
			s -> count = 0;
		}
	}
	if (s -> count < s -> size && (s -> indexes[s -> count] = strdup (index))) {
		s -> count++;
	}
}/*}}}*/
static void
selection_vadd (selection_t *s, const char *format, va_list par) /*{{{*/
{
	char	buffer[1024];
	
	if (vsnprintf (buffer, sizeof (buffer), format, par) < sizeof (buffer))
		selection_adds (s, buffer);
}/*}}}*/
static void
selection_add (selection_t *s, const char *format, ...) /*{{{*/
{
	va_list	par;
	
	va_start (par, format);
	selection_vadd (s, format, par);
	va_end (par);
}/*}}}*/
static selection_t *
selection_free (selection_t *s) /*{{{*/
{
	if (s) {
		if (s -> indexes) {
			int	n;
			
			for (n = 0; n < s -> count; ++n)
				if (s -> indexes[n])
					free (s -> indexes[n]);
			free (s -> indexes);
		}
		if (s -> scratch)
			free (s -> scratch);
		free (s);
	}
	return NULL;
}/*}}}*/
static selection_t *
selection_alloc (void) /*{{{*/
{
	selection_t	*s;
	struct utsname	name;
	struct passwd	*pw;
	char		hostbuffer[sizeof (name.nodename) + 1];
	char		*fqdn, *host, *user;
	char		*ptr;
	
	s = NULL;
	uname (& name);
	fqdn = name.nodename;
	host = NULL;
	user = NULL;
	if (ptr = strchr (fqdn, '.')) {
		strncpy (hostbuffer, fqdn, ptr - fqdn);
		hostbuffer[ptr - fqdn] = '\0';
		host = hostbuffer;
	}
	setpwent ();
	pw = getpwuid (getuid ());
	if (pw && pw -> pw_name)
		user = pw -> pw_name;
	if (s = (selection_t *) malloc (sizeof (selection_t))) {
		s -> indexes = NULL;
		s -> size = 0;
		s -> count = 0;
		s -> max_length = 0;
		s -> scratch = NULL;
		s -> scratch_size = 0;
		if (user) {
			selection_add (s, "%s@%s", user, fqdn);
			if (host)
				selection_add (s, "%s@%s", user, host);
			selection_add (s, "%s@", user);
		}
		selection_adds (s, fqdn);
		if (host)
			selection_adds (s, host);
		if (s -> count) {
			int	n, ilen;
			
			for (n = 0; n < s -> count; ++n)
				if ((ilen = strlen (s -> indexes[n])) > s -> max_length)
					s -> max_length = ilen;
		} else
			s = selection_free (s);
	}
	endpwent ();
	return s;
}/*}}}*/
static bool_t
selection_prepare (selection_t *s, const char *key) /*{{{*/
{
	int	required_size = strlen (key) + s -> max_length + 4;
	
	if (s -> scratch_size < required_size) {
		if (s -> scratch = realloc (s -> scratch, required_size))
			s -> scratch_size = required_size;
		else
			s -> scratch_size = 0;
	}
	return s -> scratch ? true : false;
}/*}}}*/
static const char *
selection_key (selection_t *s, int index, const char *key) /*{{{*/
{
	if ((index >= 0) && (index < s -> count) && (snprintf (s -> scratch, s -> scratch_size, "%s[%s]", key, s -> indexes[index]) < s -> scratch_size))
		return s -> scratch;
	return NULL;
}/*}}}*/

struct systemconfig { /*{{{*/
	bool_t		use_extra;
	const char	*content_environ;
	const char	*custom_path;
	char		*local_path;
	char		*filename;
	struct timespec	last_modified;
	selection_t	*selection;
	entry_t		**e;
	int		count;
	int		size;
	char		*scratch;
	int		scratch_size;
	/*}}}*/
};
static bool_t
config_add (systemconfig_t *c, const char *key, const char *value) /*{{{*/
{
	entry_t	*e;
	
	if (e = entry_alloc (key, value)) {
		if (c -> count >= c -> size) {
			c -> size += c -> size ? c -> size : 16;
			if (! (c -> e = (entry_t **) realloc (c -> e, c -> size * sizeof (entry_t)))) {
				c -> count = 0;
				c -> size = 0;
			}
		}
		if (c -> count < c -> size)
			c -> e[c -> count++] = e;
		else
			e = entry_free (e);
	}
	return e ? true : false;
}/*}}}*/
static bool_t
parse_plain (systemconfig_t *c, char *buffer) /*{{{*/
{
	bool_t	rc = true;
	char	*cur, *ptr;
	char	*var, *val;
	char	ch;
		
	for (ptr = buffer; ptr; ) {
		cur = ptr;
		if (ptr = strchr (ptr, '\n')) {
			if ((cur + 1 < ptr) && (*(ptr - 1) == '\r'))
				*(ptr - 1) = '\0';
			*ptr++ = '\0';
		}
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
						if (! config_add (c, var, val)) {
							rc = false;
							break;
						}
					}
				}
			}
		}
	}
	return rc;
}/*}}}*/
static bool_t
parse_json (systemconfig_t *c, JSON_Value *json) /*{{{*/
{
	bool_t	rc = false;
	
	if (json_value_get_type (json) == JSONObject) {
		JSON_Object	*root = json_value_get_object (json);
		
		if (root) {
			size_t	size = json_object_get_count (root);
			int	n;
				
			rc = true;
			for (n = 0; n < size; ++n) {
				const char	*name = json_object_get_name (root, n);
				JSON_Value	*jvalue = json_object_get_value_at (root, n);
				const char	*value;

				if (name && jvalue) {
					JSON_Value_Type	type = json_value_get_type (jvalue);
					char		scratch[256];

					if (type == JSONString) {
						value = json_value_get_string (jvalue);
					} else if ((type == JSONNull) || (type == JSONNumber) || (type == JSONBoolean)) {
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
						value = scratch;
					} else {
						value = NULL;
					}
					if (value) {
						if (! config_add (c, name, value)) {
							rc = false;
							break;
						}
					}
				}
				rc = true;
			}
		}
	}
	return rc;
}/*}}}*/
static void
config_reset (systemconfig_t *c) /*{{{*/
{
	if (c && c -> e) {
		int	n;
			
		for (n = 0; n < c -> count; ++n)
			entry_free (c -> e[n]);
		free (c -> e);
		c -> e = NULL;
		c -> size = 0;
		c -> count = 0;
	}
}/*}}}*/
static bool_t
config_scratch (systemconfig_t *c, int size) /*{{{*/
{
	if (c -> scratch_size < size) {
		if (c -> scratch = realloc (c -> scratch, size + 1))
			c -> scratch_size = size;
		else
			c -> scratch_size = 0;
	}
	return c -> scratch_size >= size;
}/*}}}*/
static void
config_extra (systemconfig_t *c) /*{{{*/
{
	build_t	*build = build_alloc ();
	int	fd;
			
	config_add (c, "build.version", build && build -> version ? build -> version : "unknown");
	config_add (c, "build.timestamp", build && build -> timestamp ? build -> timestamp : "unknown");
	config_add (c, "build.host", build && build -> host ? build -> host : "unknown");
	config_add (c, "build.user", build && build -> user ? build -> user : "unknwon");
	config_add (c, "build.typ", build && build -> typ ? build -> typ : "classic");
	if (build)
		build_free (build);
	if ((fd = open (PATH_OSRELEASE, O_RDONLY)) != -1) {
		struct stat	st;
		char		*buffer;
		char		*ptr, *cur;
		char		*name, *value, *parsed;
		int		namelength;
		char		quote;
				
		if ((fstat (fd, & st) != -1) && (st.st_size > 0) && (buffer = malloc (st.st_size + 1))) {
			if (read (fd, buffer, st.st_size) == st.st_size) {
				buffer[st.st_size] = '\0';
				
				for (cur = buffer; cur; cur = ptr) {
					if (ptr = strchr (cur, '\n'))
						*ptr++ = '\0';
					while (isspace (*cur))
						++cur;
					if (*cur != '#') {
						name = cur;
						while (*cur && (*cur != '=')) {
							*cur = tolower (*cur);
							++cur;
						}
						if (*cur) {
							namelength = cur - name;
							*cur++ = '\0';
							if (*cur == '"')
								quote = *cur++;
							else
								quote = '\0';
							value = cur;
							parsed = cur;
							while (*cur && (*cur != quote)) {
								if (*cur == '\\')
									++cur;
								if (parsed != cur)
									*parsed++ = *cur++;
								else
									++parsed, ++cur;
							}
							*parsed = '\0';
							if (config_scratch (c, namelength + 5)) {
								sprintf (c -> scratch, "os.%s", name);
								config_add (c, c -> scratch, value);
							}
						}
					}
				}
			}
			free (buffer);
		}
		close (fd);
	}
}/*}}}*/
static bool_t
config_parse (systemconfig_t *c, char *buffer) /*{{{*/
{
	bool_t		rc = false;
	
	if (c) {
		config_reset (c);
		if (buffer) {
			JSON_Value      *json;
		
			if (json = json_parse_string (buffer)) {
				rc = parse_json (c, json);
				json_value_free (json);
			} else {
				rc = false;
			}
			if (! rc) {
				rc = parse_plain (c, buffer);
			}
		} else
			rc = true;
		if (rc && c -> use_extra)
			config_extra (c);
	}
	return rc;
}/*}}}*/
static bool_t
config_check (systemconfig_t *c, bool_t recheck) /*{{{*/
{
	bool_t	rc;
	int	fd;

	rc = true;
	if (! c -> content_environ) {
		if ((! c -> filename) || (access (c -> filename, R_OK) == -1)) {
			const char	*filename;
			
			if (c -> filename) {
				free (c -> filename);
				c -> filename = NULL;
			}
			if (c -> custom_path)
				filename = c -> custom_path;
			else {
				filename = c -> local_path;
				if ((! filename) || (access (filename, R_OK) == -1)) {
# ifdef		PATH_CONFIG
					filename = PATH_CONFIG;
					if (access (filename, R_OK) == -1) 
						if (access (PATH_LEGACY, R_OK) != -1)
							filename = PATH_LEGACY;
						else
# endif		/* PATH_CONFIG */
							filename = NULL;
				}
			}
			if (filename && (! (c -> filename = strdup (filename))))
				rc = false;
			c -> last_modified.tv_sec = c -> last_modified.tv_nsec = 0;
		}
		if (rc)
			if (c -> filename) {
				rc = false;
				if ((fd = open (c -> filename, O_RDONLY)) != -1) {
					struct stat	st;
					char		*buf;

					if (fstat (fd, & st) != -1) {
						if ((c -> last_modified.tv_sec != st.st_mtim.tv_sec) || (c -> last_modified.tv_nsec != st.st_mtim.tv_nsec)) {
							if (S_ISREG (st.st_mode) && (buf = malloc (st.st_size + 1))) {
								int	count, n;
				
								rc = true;
								for (count = 0; rc && (count < st.st_size); ) {
									n = read (fd, buf + count, st.st_size - count);
									if (n > 0) {
										count += n;
									} else {
										rc = false;
									}
								}
								if (rc) {
									buf[st.st_size] = '\0';
									rc = config_parse (c, buf);
									c -> last_modified = st.st_mtim;
								}
								free (buf);
							}
						} else {
							rc = true;
						}
					}
					close (fd);
				}
			} else if (! recheck)
				rc = config_parse (c, NULL);
	}
	return rc;
}/*}}}*/
systemconfig_t *
systemconfig_free (systemconfig_t *c) /*{{{*/
{
	if (c) {
		config_reset (c);
		selection_free (c -> selection);
		if (c -> local_path)
			free (c -> local_path);
		if (c -> filename)
			free (c -> filename);
		if (c -> scratch)
			free (c -> scratch);
		free (c);
	}
	return NULL;
}/*}}}*/
systemconfig_t *
systemconfig_alloc (bool_t use_extra) /*{{{*/
{
	systemconfig_t	*c;
	
	if (c = (systemconfig_t *) malloc (sizeof (systemconfig_t))) {
		bool_t	ok = true;
		char	*buf;
		
		c -> use_extra = use_extra;
		c -> content_environ = getenv (CONFIG_ENV);
		c -> custom_path = getenv (PATH_CONFIG_ENV);
		c -> local_path = mkpath (path_home (), PATH_LOCAL, NULL);
		c -> filename = NULL;
		c -> last_modified.tv_sec = 0;
		c -> last_modified.tv_nsec = 0;
		c -> selection = selection_alloc ();
		c -> e = NULL;
		c -> count = 0;
		c -> size = 0;
		c -> scratch = NULL;
		c -> scratch_size = 0;
		if (! c -> selection) {
			ok = false;
		} else if (c -> content_environ) {
			if (buf = strdup (c -> content_environ)) {
				ok = config_parse (c, buf);
				free (buf);
			} else
				ok = false;
		} else
			ok = config_check (c, false);
		if (! ok)
			c = systemconfig_free (c);
	}
	return c;
}/*}}}*/
const char *
systemconfig_find (systemconfig_t *c, const char *key) /*{{{*/
{
	if (c && config_check (c, true)) {
		int	keylen = strlen (key);
		
		if (config_scratch (c, keylen + 1)) {
			const char	*source;
			char		*target;
			char		*default_value;
			int		n, i;
			
			strcpy (c -> scratch, key);
			for (source = key, target = c -> scratch, default_value = NULL; *source; ) {
				if (default_value) {
					*target++ = *source++;
				} else if (*source == '\\') {
					++source;
					if (*source)
						*target++ = *source++;
				} else if (*source == ':') {
					*target++ = '\0';
					default_value = target;
					++source;
				} else {
					*target++ = *source++;
				}
			}
			*target = '\0';
			if (c -> selection && selection_prepare (c -> selection, c -> scratch)) {
				const char	*selkey;
				
				for (i = 0; i < c -> selection -> count; ++i)
					if (selkey = selection_key (c -> selection, i, c -> scratch))
						for (n = 0; n < c -> count; ++n)
							if (! strcmp (selkey, c -> e[n] -> key))
								return c -> e[n] -> value;
			}
			for (n = 0; n < c -> count; ++n)
				if (! strcmp (c -> scratch, c -> e[n] -> key))
					return c -> e[n] -> value;
			return default_value;
		}
	}
	return NULL;
}/*}}}*/
bool_t
systemconfig_get (systemconfig_t *c, int idx, const char **key, const char **value) /*{{{*/
{
	if (c && config_check (c, true) && (idx >= 0) && (idx < c -> count)) {
		if (key)
			*key = c -> e[idx] -> key;
		if (value)
			*value = c -> e[idx] -> value;
		return true;
	}
	return false;
}/*}}}*/
