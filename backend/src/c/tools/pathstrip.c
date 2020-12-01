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
# include	<stdio.h>
# include	<stdlib.h>
# include	<string.h>
# include	<unistd.h>

static void
usage (const char *pgm) /*{{{*/
{
	fprintf (stderr, "Usage: %s <path-expression>\n", pgm);
}/*}}}*/
int
main (int argc, char **argv) /*{{{*/
{
	int	n;
	int	rc;
	char	*orig, *target;
	
	while ((n = getopt (argc, argv, "?h")) != -1)
		switch (n) {
		case '?':
		case 'h':
		default:
			return usage (argv[0]), (n != '?') && (n != 'h');
		}
	if (optind + 1 != argc)
		return usage (argv[0]), 1;
	rc = 1;
	if ((orig = strdup (argv[optind])) && (target = malloc (strlen (orig) + 1))) {
		char	*ptr;
		int	count;
		char	**elements;
		
		for (ptr = orig, count = 0; ptr; ++count)
			if (ptr = strchr (ptr, ':'))
				++ptr;
		if (elements = (char **) malloc (sizeof (char *) * count)) {
			int	m;
			
			for (ptr = orig, n = 0; ptr; ++n) {
				elements[n] = ptr;
				if (ptr = strchr (ptr, ':'))
					*ptr++ = '\0';
			}
			for (n = count - 1; n > 0; --n) {
				for (m = n - 1; m >= 0; --m)
					if (! strcmp (elements[n], elements[m]))
						break;
				if (m >= 0) {
					for (m = n; m < count - 1; ++m)
						elements[m] = elements[m + 1];
					--count;
				}
			}
			for (ptr = target, n = 0; n < count; ++n) {
				if (ptr != target)
					*ptr++ = ':';
				strcpy (ptr, elements[n]);
				while (*ptr)
					++ptr;
			}
			write (1, target, strlen (target));
			rc = 0;
			free (elements);
		}
		free (target);
		free (orig);
	}
	return rc;
}/*}}}*/
