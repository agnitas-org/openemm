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
# include	<string.h>
# include	<dirent.h>
# include	<errno.h>
# include	<sys/types.h>
# include	<sys/stat.h>
# include	"qctrl.h"

entry_t *
entry_alloc (const char *fname, int match) /*{{{*/
{
	entry_t	*e;
	
	if (e = (entry_t *) malloc (sizeof (entry_t))) {
		e -> fname = NULL;
		e -> match = match;
		e -> next = NULL;
		if (fname && (! (e -> fname = strdup (fname))))
			e = entry_free (e);
	}
	return e;
}/*}}}*/
entry_t *
entry_free (entry_t *e) /*{{{*/
{
	if (e) {
		if (e -> fname)
			free (e -> fname);
		free (e);
	}
	return NULL;
}/*}}}*/
entry_t *
entry_free_all (entry_t *e) /*{{{*/
{
	entry_t	*tmp;
	
	while (tmp = e) {
		e = e -> next;
		entry_free (tmp);
	}
	return NULL;
}/*}}}*/

static bool_t
iswhitespace (const char ch) /*{{{*/
{
	return ((ch == ' ') || (ch == '\t')) ? true : false;
}/*}}}*/
qf_t *
qf_alloc (const buffer_t *src) /*{{{*/
{
	qf_t	*q;
	char	*temp;
	int	size;
	char	*ptr;
	
	q = NULL;
	if (temp = malloc (src -> length + 1)) {
		memcpy (temp, src -> buffer, src -> length);
		temp[src -> length] = '\0';
		size = 0;
		if (q = (qf_t *) malloc (sizeof (qf_t))) {
			q -> content = NULL;
			q -> count = 0;
			q -> idx = 0;
			for (ptr = temp; ptr; ) {
				if (q -> count >= size) {
					size += (size ? size : 32);
					if (! (q -> content = (char **) realloc (q -> content, (size + 1) * sizeof (char *))))
						break;
				}
				if (*ptr) {
					q -> content[q -> count++] = ptr;
					while (ptr = strchr (ptr, '\n'))
						if ((! *(ptr + 1)) || (! iswhitespace (*(ptr + 1))))
							break;
						else
							++ptr;
				}
				if (ptr)
					if (*ptr)
						*ptr++ = '\0';
					else
						ptr = NULL;
			}
			if (ptr) {
				if (q -> content)
					free (q -> content);
				free (q);
				q = NULL;
			} else {
				q -> content[q -> count] = NULL;
				if (q -> count)
					temp = NULL;
			}
		}
		if (temp)
			free (temp);
	}
	return q;
}/*}}}*/
qf_t *
qf_free (qf_t *q) /*{{{*/
{
	if (q) {
		if (q -> content) {
			if (q -> content[0])
				free (q -> content[0]);
			free (q -> content);
		}
		free (q);
	}
	return NULL;
}/*}}}*/
const char *
qf_first (qf_t *q, char ch) /*{{{*/
{
	q -> idx = 0;
	return qf_next (q, ch);
}/*}}}*/
const char *
qf_next (qf_t *q, char ch) /*{{{*/
{
	while (q -> idx < q -> count)
		if (q -> content[q -> idx][0] == ch)
			return q -> content[q -> idx++];
		else if (q -> content[q -> idx][0] == '.')
			q -> idx = q -> count;
		else
			q -> idx++;
	return NULL;
}/*}}}*/
int
qf_count (qf_t *q, char ch) /*{{{*/
{
	int	n;
	int	cnt;

	for (n = 0, cnt = 0; n < q -> count; ++n)
		if ((! ch) || (q -> content[n][0] == ch))
			++cnt;
	return cnt;
}/*}}}*/
queue_t *
queue_scan (const char *path, int (*filter) (void *, queue_t *, const char *), void *data) /*{{{*/
{
	queue_t	*q;
	DIR	*dp;
	
	q = NULL;
	if (dp = opendir (path)) {
		if (q = (queue_t *) malloc (sizeof (queue_t))) {
			bool_t		st;
			entry_t		*prv, *tmp;
			struct dirent	*ent;
			int		n;

			strcpy (q -> path, path);
			strcpy (q -> fbuf, q -> path);
			for (q -> fptr = q -> fbuf; *(q -> fptr); q -> fptr++)
				;
			*(q -> fptr++) = '/';
			q -> qf = NULL;
			q -> ent = NULL;
			st = true;
			prv = NULL;
			while (st && (ent = readdir (dp))) {
				n = 0;
				if ((! filter) || (n = (*filter) (data, q, ent -> d_name)))
					if (tmp = entry_alloc (ent -> d_name, n)) {
						if (prv)
							prv -> next = tmp;
						else
							q -> ent = tmp;
						prv = tmp;
					} else
						st = false;
			}
			if (! st)
				q = queue_free (q);
		}
		closedir (dp);
	}
	return q;
}/*}}}*/
queue_t *
queue_free (queue_t *q) /*{{{*/
{
	if (q) {
		if (q -> qf)
			buffer_free (q -> qf);
		if (q -> ent)
			entry_free_all (q -> ent);
		free (q);
	}
	return NULL;
}/*}}}*/
int
queue_lock (queue_t *q, const char *fname, pid_t pid) /*{{{*/
{
	int	fd;
	
	fd = -1;
	if ((fname[0] == 'q') && (fname[1] == 'f')) {
		strcpy (q -> fptr, fname);
		if ((fd = open (q -> fbuf, O_RDONLY)) != -1) {
			struct flock	lock;
	
			lock.l_type = F_RDLCK;
			lock.l_whence = SEEK_SET;
			lock.l_start = 0;
			lock.l_len = 0;
			lock.l_pid = pid;
			if (fcntl (fd, F_SETLK, & lock) == -1) {
				close (fd);
				fd = -1;
			}
		}
	}
	return fd;
}/*}}}*/
void
queue_unlock (queue_t *q, int fd, pid_t pid) /*{{{*/
{
	struct flock	lock;
	
	lock.l_type = F_UNLCK;
	lock.l_whence = SEEK_SET;
	lock.l_start = 0;
	lock.l_len = 0;
	lock.l_pid = pid;
	fcntl (fd, F_SETLK, & lock);
	close (fd);
}/*}}}*/
bool_t
queue_read (queue_t *q, const char *fname) /*{{{*/
{
	bool_t	st;
	int	fd;
	
	st = false;
	if ((fname[0] == 'q') && (fname[1] == 'f')) {
		strcpy (q -> fptr, fname);
		if ((fd = open (q -> fbuf, O_RDONLY)) != -1) {
			st = queue_readfd (q, fd);
			close (fd);
		}
	}
	return st;
}/*}}}*/
bool_t
queue_readfd (queue_t *q, int fd) /*{{{*/
{
	bool_t		st;
	struct stat	fst;
	
	st = false;
	if (fstat (fd, & fst) != -1) {
		if ((q -> qf && buffer_size (q -> qf, fst.st_size + 1)) ||
		    ((! q -> qf) && (q -> qf = buffer_alloc (fst.st_size + 1)))) {
			if (read (fd, q -> qf -> buffer, fst.st_size) == fst.st_size) {
				q -> qf -> buffer[fst.st_size] = '\0';
				q -> qf -> length = fst.st_size;
				st = true;
			}
		}
	}
	return st;
}/*}}}*/

