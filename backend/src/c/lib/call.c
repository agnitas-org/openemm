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
# include	<sys/types.h>
# include	<sys/wait.h>
# include	"agn.h"

int
callv (char *const *argv) /*{{{*/
{
	int	rc = -1;

	if (argv && *argv) {
		pid_t	pid;
		csig_t	*csig;
		
		if (csig = csig_alloc (SIGCHLD, SIG_IGN, -1)) {
			if ((pid = fork ()) == 0) {
				if (argv[0][0] == '/')
					execv (argv[0], argv);
				else
					execvp (argv[0], argv);
				_exit (127);
			} else if (pid > 0) {
				pid_t	npid;
				int	status;
			
				while (((npid = waitpid (pid, & status, 0)) != pid) && (npid != -1))
					;
				if (npid == pid) {
					rc = status;
				}
			}
			csig_free (csig);
		}
	}
	return rc;
}/*}}}*/
int
call (const char *program, ...) /*{{{*/
{
	va_list		par;
	int		rc = -1;
	char		**argv;
	int		argc, args;
	const char	*arg;
	
	argc = 0;
	args = 12;
	if (argv = (char **) malloc ((args + 1) * sizeof (char *))) {
		if (argv[argc] = strdup (program)) {
			++argc;
			va_start (par, program);
			while (arg = va_arg (par, const char *)) {
				if (argc + 1 >= args) {
					args *= 2;
					if (! (argv = (char **) realloc (argv, (args + 1) * sizeof (char *))))
						break;
				}
				if (argv[argc] = strdup (arg))
					++argc;
				else {
					while (--argc >= 0)
						free (argv[argc]);
					free (argv);
					argv = NULL;
					break;
				}
			}
			va_end (par);
			if (argv) {
				argv[argc] = NULL;
				rc = callv (argv);
				while (--argc >= 0)
					free (argv[argc]);
				free (argv);
			}
		} else
			free (argv);
	}
	return rc;
}
