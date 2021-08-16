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
/** @file net.c
 * Network related routines.
 */
# include	<netdb.h>
# include	<sys/utsname.h>
# include	"agn.h"

/** Get full qualified domain name for current system.
 * Retrieve the fqdn for the local machine
 * @return the fqdn on success, NULL otherwise
 */
char *
get_fqdn (void) /*{{{*/
{
	char		*fqdn;
	struct utsname	un;
	struct hostent	*hent;
	
	fqdn = NULL;
	if (uname (& un) != -1) {
		sethostent (0);
		if ((hent = gethostbyname (un.nodename)) && hent -> h_name)
			fqdn = strdup (hent -> h_name);
		endhostent ();
	}
	return fqdn;
}/*}}}*/
