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
# include	"xmlback.h"

mailtrack_t *
mailtrack_alloc (int licence_id, int company_id, int mailing_id, int maildrop_status_id) /*{{{*/
{
	mailtrack_t	*m;
	
	if (m = (mailtrack_t *) malloc (sizeof (mailtrack_t))) {
		m -> content = NULL;
		m -> count = 0;
		if ((! (m -> content = buffer_alloc (1024))) ||
		    (! buffer_format (m -> content, "%d;%ld;%d;%d;%d;%d;", getpid (), (long) time (NULL), licence_id, company_id, mailing_id, maildrop_status_id)))
			m = mailtrack_free (m);
	}
	return m;
}/*}}}*/
mailtrack_t *
mailtrack_free (mailtrack_t *m) /*{{{*/
{
	if (m) {
		if (m -> content)
			buffer_free (m -> content);
		free (m);
	}
	return NULL;
}/*}}}*/
void
mailtrack_add (mailtrack_t *m, int customer_id) /*{{{*/
{
	buffer_format (m -> content, "%s%d", (m -> count ? "," : ""), customer_id);
	m -> count++;
}/*}}}*/
