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
# include	<stdio.h>
# include	<stdlib.h>
# include	<unistd.h>
# include	<fcntl.h>
# include	<ctype.h>
# include	<string.h>
# include	<signal.h>
# include	<errno.h>
# include	<sys/types.h>
# include	<sys/wait.h>
# include	"agn.h"

static volatile int	term, alrm;

static void
handler (int sig) /*{{{*/
{
	if (sig == SIGALRM)
		alrm = 1;
	else
		term = 1;
}/*}}}*/
int
main (int argc, char **argv) /*{{{*/
{
	int	rc;
	int	toraise;
	int	timeout;
	bool_t	no_stdin;
	bool_t	no_stderr;
	char	*pgm;
	char	*ptr;
	char	*path;
	
	rc = 1;
	toraise = 0;
	timeout = 30;
	no_stdin = false;
	no_stderr = false;
	if (ptr = strrchr (argv[0], '/'))
		++ptr;
	else
		ptr = argv[0];
	if (pgm = strchr (ptr, '-'))
		++pgm;
	else
		pgm = NULL;
	if (ptr = getenv ("WRAPRC")) {
		char	*buf;
		
		if (buf = strdup (ptr)) {
			char	*var, *val;
			
			for (ptr = buf; ptr; ) {
				var = ptr;
				if (ptr = strchr (ptr, ',')) {
					*ptr++ = '\0';
					while (isspace ((unsigned char) *ptr))
						++ptr;
				}
				if (val = strchr (var, '=')) {
					*val++ = '\0';
					if (! strcmp (var, "timeout"))
						timeout = atoi (val);
					else if (! strcmp (var, "program"))
						pgm = strdup (val); /* memory leak, oh yeah */
					else if (! strcmp (var, "no_stdin"))
						no_stdin = atob (val);
					else if (! strcmp (var, "no_stderr"))
						no_stderr = atob (val);
				} else if (*var) {
					val = var + 1;
					if (*var == '0')
						no_stdin = (*val == '-');
					else if (*var == '2')
						no_stderr = (*val == '-');
				}
			}
			free (buf);
		}
	}
	if (pgm && (path = which (pgm))) {
		csig_t	*sig;

		term = 0;
		alrm = 0;
		if (sig = csig_alloc (SIGINT, handler,
				      SIGTERM, handler,
				      SIGALRM, handler,
				      SIGHUP, SIG_IGN,
				      SIGPIPE, SIG_IGN,
				      SIGQUIT, SIG_DFL, -1)) {
			pid_t	pid;
			
			if ((pid = fork ()) == 0) {
				if (no_stderr) {
					int	fd;
					
					close (2);
					if ((fd = open (_PATH_DEVNULL, O_WRONLY)) != -1)
						while ((fd != -1) && (fd < 2))
							fd = dup (fd);
				}
				if (no_stdin)
					close (0);
				argv[0] = path;
				execv (path, argv);
				_exit (127);
			} else if (pid > 0) {
				pid_t	npid;
				int	errn;
				int	status;
				int	round;
				
				round = 0;
				while (pid > 0) {
					if (timeout > 0)
						alarm (timeout);
					errno = 0;
					npid = waitpid (pid, & status, (timeout > 0 ? 0 : WNOHANG));
					errn = errno;
					csig_block (sig);
					if (npid == pid) {
						if (WIFEXITED (status))
							rc = WEXITSTATUS (status);
						else if (WIFSIGNALED (status)) {
							rc = 121;
							toraise = WTERMSIG (status);
						} else
							rc = 120;
						pid = -1;
					} else if ((npid == -1) && (errn == EINTR)) {
						if (term)
							++round;
						kill (pid, (round == 0 ? SIGTERM : SIGKILL));
						++round;
						timeout = (round  < 2 ? 2 : 0);
					} else
						pid = -1;
					alarm (0);
					csig_unblock (sig);
				}
				if (term)
					rc = 124;
				else if (alrm)
					rc = 123;
			} else
				rc = 126;
			csig_free (sig);
		}
		free (path);
	} else
		rc = 126;
	if (toraise) {
		rc = 122;
		kill (getpid (), toraise);
	}
	return rc;
}/*}}}*/
