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
# include	<fcntl.h>
# include	<errno.h>
# include	<sys/types.h>
# include	<sys/stat.h>
# include	<utime.h>
# include	"agn.h"

# define	HASH_CHARS		"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_-"

struct fsdb { /*{{{*/
	char	path[PATH_MAX + 1];	/* store the base path and is used to build final path	*/
	char	*fptr;			/* pointer to the variable part of path			*/
	int	base_path_len;		/* length of base path with trailing /			*/
	int	fsize;			/* free size in path					*/
	char	scratch[PATH_MAX + 1];	/* for creating temp. files				*/
	char	*sptr;			/* pointer to the variable part of scratch		*/
	/*}}}*/
};

static bool_t
convert_key (fsdb_t *f, const char *key) /*{{{*/
{
	int		keylen = strlen (key);
	const char	*src, *current;
	char		*dst;
	hash_t		hash;
	
	if ((keylen + 5 > f -> fsize) || strchr (key, '/'))
		return false;
	for (src = key, dst = f -> fptr; src; ) {
		current = src;
		src = strchr (src, ':');
		if (src) {
			if (src - current) {
				memcpy (dst, current, src - current);
				dst += src - current;
			}
			*dst++ = '/';
			++src;
		} else {
			hash = hash_svalue (current, strlen (current), false);
			*dst++ = HASH_CHARS[(hash >> 6) % (sizeof (HASH_CHARS) - 1)];
			*dst++ = HASH_CHARS[hash % (sizeof (HASH_CHARS) - 1)];
			*dst++ = '/';
			strcpy (dst, current);
		}
	}
	return true;
}/*}}}*/
static bool_t
create_path (fsdb_t *f) /*{{{*/
{
	char		temp[PATH_MAX + 1];
	char		*ptr;
	struct stat	st;
	
	strncpy (temp, f -> path, PATH_MAX);
	temp[PATH_MAX] = '\0';
	if (ptr = strrchr (temp, '/'))
		*ptr = '\0';
	ptr = temp + f -> base_path_len;
	while (ptr) {
		if (ptr = strchr (ptr, '/'))
			*ptr = '\0';
		if (stat (temp, & st) != -1) {
			if (! S_ISDIR (st.st_mode))
				break;
		} else {
			if ((mkdir (temp, 0700) == -1) && ((stat (temp, & st) == -1) || (! S_ISDIR (st.st_mode))))
				break;
		}
		if (ptr)
			*ptr++ = '/';
		else
			return true;
	}
	return false;
}/*}}}*/
static bool_t
do_read (int fd, void *buffer, int size) /*{{{*/
{
	int	n;
	
	while (size > 0) {
		if ((n = read (fd, buffer, size)) > 0) {
			buffer = (char *) buffer + size;
			size -= n;
		} else
			break;
	}
	return size == 0 ? true : false;
}/*}}}*/
static bool_t
do_write (int fd, const void *buffer, int size) /*{{{*/
{
	int	n;
	
	while (size > 0) {
		if ((n = write (fd, buffer, size)) > 0) {
			buffer = (char *) buffer + size;
			size -= n;
		} else
			break;
	}
	return size == 0 ? true : false;
}/*}}}*/
fsdb_result_t *
fsdb_result_alloc (int size) /*{{{*/
{
	fsdb_result_t	*r;
	
	if (r = (fsdb_result_t *) malloc (sizeof (fsdb_result_t)))
		if (r -> value = malloc (size + 1))
			r -> vlen = size;
		else {
			free (r);
			r = NULL;
		}
	return r;
}/*}}}*/
fsdb_result_t *
fsdb_result_free (fsdb_result_t *r) /*{{{*/
{
	if (r) {
		if (r -> value)
			free (r -> value);
		free (r);
	}
	return NULL;
}/*}}}*/
fsdb_t *
fsdb_alloc (const char *base_path) /*{{{*/
{
	char	temp[PATH_MAX + 1];
	int	bp_len;
	fsdb_t	*f;
	
	if (! base_path) {
		if (snprintf (temp, sizeof (temp) - 1, "%s/var/fsdb/", path_home ()) == -1)
			return NULL;
		base_path = temp;
	}
	bp_len = strlen (base_path);
	f = NULL;
	if ((bp_len + 256 < PATH_MAX) && (f = (fsdb_t *) malloc (sizeof (fsdb_t)))) {
		strncpy (f -> path, base_path, bp_len);
		f -> fptr = f -> path + bp_len;
		if (*(f -> fptr) != '/')
			*(f -> fptr++) = '/';
		f -> base_path_len = f -> fptr - f -> path;
		f -> fsize = PATH_MAX - f -> base_path_len;
		strncpy (f -> scratch, f -> path, f -> base_path_len);
		f -> sptr = f -> scratch + f -> base_path_len;
	}
	return f;
}/*}}}*/
fsdb_t *
fsdb_free (fsdb_t *f) /*{{{*/
{
	if (f) {
		free (f);
	}
	return NULL;
}/*}}}*/
bool_t
fsdb_exists (fsdb_t *f, const char *key) /*{{{*/
{
	struct stat	st;
	
	if (convert_key (f, key) && (stat (f -> path, & st) != -1) && S_ISREG (st.st_mode))
		return true;
	return false;
}/*}}}*/
fsdb_result_t *
fsdb_get (fsdb_t *f, const char *key) /*{{{*/
{
	fsdb_result_t	*r = NULL;
	int		fd;
	
	if (convert_key (f, key) && ((fd = open (f -> path, O_RDONLY)) != -1)) {
		struct stat	st;
		
		if ((fstat (fd, & st) != -1) && (r = fsdb_result_alloc (st.st_size))) {
			if (do_read (fd, r -> value, r -> vlen))
				((char *) r -> value)[r -> vlen] = '\0';
			else
				r = fsdb_result_free (r);
		}
		close (fd);
	}
	return r;
}/*}}}*/
bool_t
fsdb_put (fsdb_t *f, const char *key, const void *value, int vlen) /*{{{*/
{
	bool_t	rc = false;
	
	if (convert_key (f, key)) {
		int	fd;
		
		if ((fd = open (f -> path, O_RDONLY)) != -1) {
			struct stat	st;
			void		*temp;
			
			if ((fstat (fd, & st) != -1) && (st.st_size == vlen) && (temp = malloc (st.st_size))) {
				if (do_read (fd, temp, vlen))
					if ((! vlen) || (! memcmp (value, temp, vlen))) {
						struct utimbuf	utbuf;
						
						utbuf.actime = utbuf.modtime = time (NULL);
						if (utime (f -> path, & utbuf) != -1)
							rc = true;
					}
				free (temp);
			}
			close (fd);
		}
		if (! rc) {
			snprintf (f -> sptr, f -> base_path_len, ".%06d.%10ld", getpid (), time (NULL));
			if ((fd = open (f -> scratch, O_WRONLY | O_CREAT | O_TRUNC, 0600)) != -1) {
				if (do_write (fd, value, vlen)) {
					if ((rename (f -> scratch, f -> path) != -1) ||
					    (create_path (f) && (rename (f -> scratch, f -> path) != -1)))
						rc = true;
				}
				close (fd);
				unlink (f -> scratch);
			}
		}
	}
	return rc;
}/*}}}*/
bool_t
fsdb_remove (fsdb_t *f, const char *key) /*{{{*/
{
	if (convert_key (f, key)) {
		struct stat	st;
		
		if (stat (f -> path, & st) == -1) {
			if (errno == ENOENT)
				return true;
		} else if (S_ISREG (st.st_mode) && (unlink (f -> path) != -1))
				return true;
	}
	return false;
}/*}}}*/
