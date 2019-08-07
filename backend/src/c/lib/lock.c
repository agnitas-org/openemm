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
# include	<fcntl.h>
# include	<string.h>
# include	<errno.h>
# include	<sys/stat.h>
# include	"agn.h"

lock_t *
lock_alloc (const char *fname) /*{{{*/
{
	lock_t	*l;
	
	if (l = (lock_t *) malloc (sizeof (lock_t)))
		if (l ->fname = strdup (fname)) {
			l -> islocked = false;
		} else {
			free (l);
			l = NULL;
		}
	return l;
}/*}}}*/
lock_t *
lock_free (lock_t *l) /*{{{*/
{
	if (l) {
		if (l -> islocked)
			lock_unlock (l);
		free (l -> fname);
		free (l);
	}
	return NULL;
}/*}}}*/
bool_t
lock_lock (lock_t *l) /*{{{*/
{
	if (! l -> islocked) {
		int	omask;
		int	state;
		int	fd;
		char	scratch[32];
		int	slen;
				
		
		omask = umask (0133);
		for (state = 0; state < 2; ++state)
			if ((fd = open (l -> fname, O_CREAT | O_EXCL | O_WRONLY, 0644)) != -1) {
				slen = sprintf (scratch, "%10d\n", getpid ());
				if (write (fd, scratch, slen) == slen)
					l -> islocked = true;
				close (fd);
				break;
			} else if ((! state) && ((fd = open (l -> fname, O_RDONLY)) != -1)) {
				slen = read (fd, scratch, sizeof (scratch) - 1);
				close (fd);
				if (slen > 0) {
					int	pid;

					scratch[slen] = '\0';
					pid = atoi (scratch);
					if ((pid > 0) && (kill (pid, 0) == -1) && (errno == ESRCH))
						unlink (l -> fname);
				}
			}
		umask (omask);
	}
	return l -> islocked;
}/*}}}*/
void
lock_unlock (lock_t *l) /*{{{*/
{
	if (l -> islocked) {
		unlink (l -> fname);
		l -> islocked = false;
	}
}/*}}}*/
