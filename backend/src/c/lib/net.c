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
/** @file net.c
 * Network related routines.
 */
# include	<string.h>
# include	<netdb.h>
# include	<sys/utsname.h>
# include	"agn.h"

/** Get full qualified domain name.
 * Retrieve the full qualified domain name for a given hostname
 * @param name the hostname
 * @return the fqdn on success, NULL otherwise
 */
char *
get_fqdn (const char *name) /*{{{*/
{
	char		*fqdn;
	struct hostent	*hent;
	
	fqdn = NULL;
	sethostent (0);
	if ((hent = gethostbyname (name)) && hent -> h_name)
		fqdn = strdup (hent -> h_name);
	endhostent ();
	return fqdn;
}/*}}}*/
/** Get full qualified domain name for current system.
 * Retrieve the fqdn for the local machine
 * @return the fqdn on success, NULL otherwise
 */
char *
get_local_fqdn (void) /*{{{*/
{
	char		*fqdn;
	struct utsname	un;
	
	fqdn = NULL;
	if (uname (& un) != -1)
		fqdn = get_fqdn (un.nodename);
	return fqdn;
}/*}}}*/
