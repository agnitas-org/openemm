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
/** @file csig.c
 * Signal handling.
 */
# include	<stdlib.h>
# include	<stdarg.h>
# include	"agn.h"

# ifndef	NSIG
#  ifndef	_NSIG
#   define	NSIG		32
#  else		/* _NSIG */
#   define	NSIG		_NSIG
#  endif	/* _NSIG */
# endif		/* NSIG */

/** Information about one signal
 */
typedef struct ssig { /*{{{*/
	int			snr;	/**< signal number		*/
	struct sigaction	set;	/**< current information	*/
	struct sigaction	old;	/**< previous information	*/
	struct ssig		*next;	/**< next signal 		*/
	/*}}}*/
}	ssig_t;

/** Allocate new signal.
 * @param snr the signal number
 * @param hand the signal handler
 * @return new instance on success, NULL otherwise
 */
static ssig_t *
ssig_alloc (int snr, void (*hand) (int)) /*{{{*/
{
	ssig_t	*s;
	
	s = NULL;
	if ((snr < NSIG) && (s = (ssig_t *) malloc (sizeof (ssig_t)))) {
		s -> snr = snr;
		s -> set.sa_handler = hand;
		sigemptyset (& s -> set.sa_mask);
		s -> set.sa_flags = 0;
		s -> next = NULL;
	}
	return s;
}/*}}}*/
/** Frees signal.
 * Returns used resources to the system
 * @param s the signal instance to free
 * @return NULL
 */
static ssig_t *
ssig_free (ssig_t *s) /*{{{*/
{
	if (s) {
		free (s);
	}
	return NULL;
}/*}}}*/

/** Allocate signals.
 * Setup signal handler for signal/handler pairs and returns one
 * control structure for these handlers
 * @param signr Signal number
 * @param ... handler, (signr, handler)*, -1
 * @return newly allocated instance on success, NULL otherwise
 */
csig_t *
csig_alloc (int signr, ...) /*{{{*/
{
	va_list		par;
	void		(*hand) (int);
	csig_t		*c;
	bool_t		st;
	ssig_t		*prv, *cur;

	va_start (par, signr);
	if (c = (csig_t *) malloc (sizeof (csig_t))) {
		c -> base = NULL;
		sigemptyset (& c -> mask);
		c -> isblocked = false;
		st = true;
		prv = NULL;
		while (signr > 0) {
			hand = (void (*) (int)) va_arg (par, void *);
			if (! (cur = ssig_alloc (signr, hand))) {
				st = false;
				break;
			}
			sigaddset (& c -> mask, signr);
			if (prv)
				prv -> next = cur;
			else
				c -> base = cur;
			prv = cur;
			signr = va_arg (par, int);
		}
		if (st)
			for (cur = c -> base; cur && st; cur = cur -> next) {
				cur -> set.sa_mask = c -> mask;
				sigdelset (& cur -> set.sa_mask, cur -> snr);
				if (sigaction (cur -> snr, & cur -> set, & cur -> old) == -1)
					st = false;
			}
		if (! st)
			c = csig_free (c);
	}
	va_end (par);
	return c;
}/*}}}*/
/** Frees signal instance.
 * Returns the signal handlers resources to the system
 * @param c the signal handler instance
 * @return NULL
 */
csig_t *
csig_free (csig_t *c) /*{{{*/
{
	ssig_t	*tmp;
	
	if (c) {
		if (! c -> isblocked)
			csig_block (c);
		while (tmp = c -> base) {
			c -> base = tmp -> next;
			sigaction (tmp -> snr, & tmp -> old, NULL);
			ssig_free (tmp);
		}
		csig_unblock (c);
		free (c);
	}
	return NULL;
}/*}}}*/
/** Block signals.
 * Block the signals parts of this signal set
 * @param c the signal set
 */
void
csig_block (csig_t *c) /*{{{*/
{
	if (! c -> isblocked) {
		sigprocmask (SIG_BLOCK, & c -> mask, & c -> oldmask);
		c -> isblocked = true;
	}
}/*}}}*/
/** Unblock signals.
 * Remove a previous installed block for the signals
 * @param c the signal set
 */
void
csig_unblock (csig_t *c) /*{{{*/
{
	if (c -> isblocked) {
		c -> isblocked = false;
		sigprocmask (SIG_SETMASK, & c -> oldmask, NULL);
	}
}/*}}}*/
