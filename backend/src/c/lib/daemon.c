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
# include	<unistd.h>
# include	<fcntl.h>
# include	<string.h>
# include	<errno.h>
# include	<sys/types.h>
# include	<sys/stat.h>
# include	"agn.h"

daemon_t *
daemon_alloc (const char *prog, bool_t background, bool_t detach) /*{{{*/
{
	daemon_t	*d;
	
	if (d = (daemon_t *) malloc (sizeof (daemon_t))) {
		d -> background = background;
		d -> detach = detach;
		d -> pid = getpid ();
		d -> pidfile = NULL;
		d -> pfvalid = false;
		if (prog) {
			const char	*ptr;
			
			if (ptr = strrchr (prog, '/'))
				++ptr;
			else
				ptr = prog;
			if (d -> pidfile = malloc (sizeof (_PATH_VARRUN) + strlen (ptr)))
				sprintf (d -> pidfile, _PATH_VARRUN "%s", ptr);
			else
				d = daemon_free (d);
		}
	}
	return d;
}/*}}}*/
daemon_t *
daemon_free (daemon_t *d) /*{{{*/
{
	if (d) {
		daemon_done (d);
		if (d -> pidfile)
			free (d -> pidfile);
		free (d);
	}
	return NULL;
}/*}}}*/
void
daemon_done (daemon_t *d) /*{{{*/
{
	if (d -> pfvalid) {
		unlink (d -> pidfile);
		d -> pfvalid = false;
	}
}/*}}}*/
bool_t
daemon_lstart (daemon_t *d, log_t *l, logmask_t lmask, const char *lwhat) /*{{{*/
{
	if (d -> background) {
		pid_t	pid;
		mode_t	omask;
		int	n;
		int	fd;
		char	buf[32];
		int	blen;
		
		if ((pid = fork ()) == -1) {
			if (l)
				log_out (l, lmask, lwhat, "Unable to fork (%d, %m)", errno);
			return false;
		} else if (pid > 0)
			exit (0);
		d -> pid = getpid ();
		if (d -> pidfile) {
			omask = umask (0);
			for (n = 0; n < 2; ++n) {
				fd = open (d -> pidfile, O_WRONLY | O_CREAT | O_EXCL, 0644);
				if ((fd == -1) && (! n)) {
					if ((fd = open (d -> pidfile, O_RDONLY)) != -1) {
						blen = read (fd, buf, sizeof (buf) - 1);
						close (fd);
						if (blen > 0) {
							buf[blen] = '\0';
							pid = (pid_t) atol (buf);
							if ((pid > 0) && (kill (pid, 0) == -1) && (errno == ESRCH))
								unlink (d -> pidfile);
						}
						fd = -1;
					}
				}
			}
			umask (omask);
			if (fd == -1) {
				if (l)
					log_out (l, lmask, lwhat, "Unable to create pidfile %s, maybe an instance is already running?", d -> pidfile);
				return false;
			}
			d -> pfvalid = true;
			blen = sprintf (buf, "%10ld\n", (long) d -> pid);
			if (write (fd, buf, blen) != blen)
				d -> pfvalid = false;
			close (fd);
			if (! d -> pfvalid) {
				unlink (d -> pidfile);
				if (l)
					log_out (l, lmask, lwhat, "Failed to write pidfile %s, maybe disk is full?", d -> pidfile);
				return false;
			}
		}
	}
	if (d -> detach) {
		int	n;
		int	fd;

		umask (0);
		for (n = 0; n >= 0; ++n)
			if ((close (n) == -1) && (errno == EBADF))
				break;
		if (setsid () == -1) {
			if (l)
				log_out (l, lmask, lwhat, "Unable to set session (%d, %m)", errno);
			return false;
		}
		if (((fd = open (_PATH_DEVNULL, O_RDWR)) != 0) ||
		    (dup (fd) != 1) ||
		    (dup (fd) != 2)) {
			if (l)
				log_out (l, lmask, lwhat, "Unable to open %s propperly (%d, %m)", _PATH_DEVNULL, errno);
			return false;
		}
	}
	return true;
}/*}}}*/
bool_t
daemon_start (daemon_t *d, log_t *l) /*{{{*/
{
	char	what[64];
	
	sprintf (what, "DAEMON[%c/%c]",
		 (d -> background ? 'b' : 'f'),
		 (d -> detach ? 'd' : 'a'));
	return daemon_lstart (d, l, 0, what);
}/*}}}*/
bool_t
daemon_sstart (daemon_t *d) /*{{{*/
{
	return daemon_lstart (d, NULL, 0, NULL);
}/*}}}*/
