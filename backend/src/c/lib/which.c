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
# include	"agn.h"

char *
which (const char *pgm) /*{{{*/
{
	char		*rc;
	const char	*path;
	const char	*temp;
	
	rc = NULL;
	if ((*pgm == '/') && (access (pgm, X_OK) != -1))
		rc = strdup (pgm);
	else {
		if (temp = strrchr (pgm, '/'))
			pgm = temp + 1;
		if (path = getenv ("PATH")) {
			buffer_t	*scratch;
			int		start, end;
		
			if (scratch = buffer_alloc (1024)) {
				start = 0;
				while (path[start] && (! rc)) {
					for (end = start; path[end] && (path[end] != ':'); ++end)
						;
					if (! (end - start))
						buffer_sets (scratch, ".");
					else
						buffer_setsn (scratch, path + start, end - start);
					buffer_appendch (scratch, '/');
					buffer_appends (scratch, pgm);
					if ((temp = buffer_string (scratch)) && (access (temp, X_OK) != -1))
						rc = strdup (temp);
					if (path[end])
						++end;
					start = end;
				}
				buffer_free (scratch);
			}
		}
	}
	return rc;
}/*}}}*/
