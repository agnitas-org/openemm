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
# include	<setjmp.h>
# include	<signal.h>
# include	"agn.h"

typedef struct { /*{{{*/
	sigjmp_buf	env;
	csig_t		*saved;
	int		seconds;
	time_t		start;
	/*}}}*/
}	timeout_t;
static timeout_t	*timeout = NULL;

static void
timout_handler (int sig) /*{{{*/
{
	siglongjmp (timeout -> env, 1);
}/*}}}*/
bool_t
timeout_init (void) /*{{{*/
{
	if ((! timeout) && (timeout = (timeout_t *) malloc (sizeof (timeout_t)))) {
		timeout -> saved = csig_alloc (SIGALRM, timout_handler, 0);
		if (! timeout -> saved)
			timeout_release ();
		timeout -> seconds = 0;
		timeout -> start = 0;
	}
	return timeout ? true : false;
}/*}}}*/
void
timeout_release (void) /*{{{*/
{
	if (timeout) {
		alarm (0);
		if (timeout -> saved)
			csig_free (timeout -> saved);
		free (timeout);
		timeout = NULL;
	}
}/*}}}*/
bool_t
timeout_exec (int seconds, void (*func) (void *), void *pd) /*{{{*/
{
	bool_t	rc;
	
	if (timeout_init ()) {
		timeout -> seconds = seconds;
		time (& timeout -> start);
		if (!  sigsetjmp (timeout -> env, 0)) {
			alarm (seconds);
			(*func) (pd);
			alarm (0);
			rc = true;
		} else
			rc = false;
		timeout_release ();
	} else
		rc = false;
	return rc;
}/*}}}*/
void
timeout_block (void) /*{{{*/
{
	if (timeout)
		csig_block (timeout -> saved);
}/*}}}*/
void
timeout_unblock (void) /*{{{*/
{
	if (timeout)
		csig_unblock (timeout -> saved);
}/*}}}*/
void
timeout_suspend (void) /*{{{*/
{
	timeout_block ();
	if (timeout) {
		alarm (0);
		if ((timeout -> start > 0) && (timeout -> seconds > 0)) {
			time_t	now;
			int	remain;
			
			time (& now);
			remain = timeout -> seconds - (now - timeout -> start) + 1;
			timeout -> seconds = remain > 0 ? remain : 0;
		} else {
			timeout -> seconds = 0;
		}
	}
	timeout_unblock ();
}/*}}}*/
void
timeout_resume (void) /*{{{*/
{
	timeout_block ();
	if (timeout) {
		time (& timeout -> start);
		if (timeout -> seconds > 0) {
			alarm (timeout -> seconds);
		} else {
			kill (getpid (), SIGALRM);
		}
	}
	timeout_unblock ();
}/*}}}*/
