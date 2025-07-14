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
/** @file str.c
 * String utilities.
 */
# include	<stdlib.h>
# include	<ctype.h>
# include	<string.h>
# include	"agn.h"

/** Reuse a string variable.
 * Create a copy of input string, free up old memory, if neccessary
 * @param buf the destination to copy to, freeing used memory
 * @param str the string to copy
 * @return true on success, false otherwise
 */
bool_t
struse (char **buf, const char *str) /*{{{*/
{
	if (*buf)
		free (*buf);
	*buf = str ? strdup (str) : NULL;
	return (! str) || *buf;
}/*}}}*/
char *
strldup (const char *s) /*{{{*/
{
	char	*rc;
	
	if (rc = malloc (strlen (s) + 1)) {
		char	*ptr;
		
		for (ptr = rc; *ptr++ = tolower (*s++);)
			;
	}
	return rc;
}/*}}}*/
char *
strudup (const char *s) /*{{{*/
{
	char	*rc;
	
	if (rc = malloc (strlen (s) + 1)) {
		char	*ptr;
		
		for (ptr = rc; *ptr++ = toupper (*s++);)
			;
	}
	return rc;
}/*}}}*/
static char *
do_strcat (int (*func) (int), const char *s, va_list par) /*{{{*/
{
	char		*rc;
	buffer_t	*buf;

	rc = NULL;
	if (buf = buffer_alloc (512)) {
		while (s) {
			while (*s) {
				if (! buffer_stiffch (buf, func (*s)))
					break;
				++s;
			}
			if (*s)
				break;
			s = va_arg (par, const char *);
		}
		if (! s)
			rc = buffer_stealstring (buf);
		buffer_free (buf);
	}
	return rc;
}/*}}}*/
char *
strlcat (const char *s, ...) /*{{{*/
{
	va_list	par;
	char	*rc;
	
	va_start (par, s);
	rc = do_strcat (tolower, s, par);
	va_end (par);
	return rc;
}/*}}}*/
char *
strucat (const char *s, ...) /*{{{*/
{
	va_list	par;
	char	*rc;
	
	va_start (par, s);
	rc = do_strcat (toupper, s, par);
	va_end (par);
	return rc;
}/*}}}*/
