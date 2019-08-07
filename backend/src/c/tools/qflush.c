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
# include	<unistd.h>
# include	<fcntl.h>
# include	"qctrl.h"

typedef struct { /*{{{*/
	log_t	*lg;
	char	**paths;
	int	pcount;
	pid_t	pid;
	time_t	now;
	/*}}}*/
}	flush_t;

static bool_t
use_file (flush_t *f, const char *fname) /*{{{*/
{
	if ((fname[0] == 'q') && (strlen (fname) > 11) && (fname[8] == '0') && (fname[9] == '0') && (fname[10] == '2'))
		return true;
	return false;
}/*}}}*/
static int
reached_timeout (void *data, queue_t *q, const char *fname) /*{{{*/
{
	flush_t	*f = (flush_t *) data;
	int	match = 0;
	int	fd;
	
	if (use_file (f, fname) && ((fd = open (fname, O_RDONLY)) != 1)) {
		if (queue_readfd (q, fd)) {
			char	*ptr;
			time_t	ts;
			
			for (ptr = (char *) q -> qf -> buffer; ptr; ) {
				if (*ptr == 'T') {
					ts = (time_t) atol (ptr + 1);
					if (ts + 24 * 60 * 60 < f -> now)
						match = 1;
					break;
				}
				if (ptr = strchr (ptr, '\n'))
					++ptr;
			}
		}
		close (fd);
	}
	return match;
}/*}}}*/

static bool_t
flush_path (flush_t *f, const char *path) /*{{{*/
{
	queue_t	*q;
	
	if (chdir (path) == -1) {
		log_out (f -> lg, LV_ERROR, "Failed to scan \"%s\": %m", path);
		return false;
	}
	if (q = queue_scan (path, reached_timeout, f)) {
		entry_t	*run;
		char	*fname;
		int	fd;
				
		for (run = q -> ent; run; run = run -> next)
			if (fname = strdup (run -> fname)) {
				if ((fd = queue_lock (q, run -> fname, f -> pid)) != -1) {
					log_out (f -> lg, LV_DEBUG, "Remove %s/%s and %s/d%s", path, fname, path, fname + 1);
					unlink (fname);
					fname[0] = 'd';
					unlink (fname);
					queue_unlock (q, fd, f -> pid);
				} else
					log_out (f -> lg, LV_INFO, "Skip %s/%s due to lock", path, fname);
				free (fname);
			}
		queue_free (q);
	}
	return true;
}/*}}}*/

void *
flush_init (log_t *lg, bool_t force, char **args, int alen) /*{{{*/
{
	flush_t	*f;
	
	if (f = (flush_t *) malloc (sizeof (flush_t))) {
		f -> lg = lg;
		f -> paths = args;
		f -> pcount = alen;
		f -> pid = getpid ();
		f -> now = 0;
	}
	return f;
}/*}}}*/
bool_t
flush_deinit (void *data) /*{{{*/
{
	flush_t	*f = (flush_t *) data;
	
	if (f) {
		free (f);
	}
	return true;
}/*}}}*/
bool_t
flush_exec (void *data) /*{{{*/
{
	flush_t	*f = (flush_t *) data;
	int	n;

	time (& f -> now);
	for (n = 0; n < f -> pcount; ++n)
		if (f -> paths[n])
			if (! flush_path (f, f -> paths[n]))
				f -> paths[n] = NULL;
	return true;
}/*}}}*/
