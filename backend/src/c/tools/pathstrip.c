/********************************************************************************************************************************************************************************************************************************************************************
 *                                                                                                                                                                                                                                                                  *
 *                                                                                                                                                                                                                                                                  *
 *        Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   *
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
# include	<pwd.h>

static int
equal (const char *e1, const char *e2, int basename)
{
	if (basename) {
		const char	*p1 = strrchr (e1, '/'),
				*p2 = strrchr (e2, '/');
		return ! strcmp (p1 ? p1 + 1 : e1, p2 ? p2 + 1 : e2);
	}
	return ! strcmp (e1, e2);
}
static int
match_directory_prefix (const char *prefix, int prefix_length, const char *path)
{
	int	path_length = strlen (path);
	
	return (path_length >= prefix_length) && (! strncmp (prefix, path, prefix_length)) && ((prefix_length == path_length) || (path[prefix_length] == '/'));
}
static void
sort_elements (char **elements, int count)
{
	const char	*home;
	struct passwd	*pwd;
	const char	*patterns[2];
	int		start;
	int		n, m, o;

	if ((pwd = getpwuid (getuid ())) && pwd -> pw_dir)
		home = pwd -> pw_dir;
	else
		home = getenv ("HOME");
	patterns[0] = home;
	patterns[1] = "/opt";
	for (o = 0, start = 0; o < sizeof (patterns) / sizeof (patterns[0]); ++o) {
		if (patterns[o] && patterns[o][0]) {
			const char	*pattern = patterns[o];
			int		pattern_length = strlen (pattern);
					
			for (n = start; n < count; ++n) {
				if (! match_directory_prefix (pattern, pattern_length, elements[n])) {
					for (m = n + 1; m < count; ++m)
						if (match_directory_prefix (pattern, pattern_length, elements[m]))
							break;
					if (m < count) {
						char	*save = elements[m];
								
						for (; m >= n; --m) {
							elements[m] = elements[m - 1];
						}
						elements[n] = save;
					} else {
						start = n + 1;
						break;
					}
				}
			}
		}
	}
}
static void
usage (const char *pgm)
{
	fprintf (stderr, "Usage: %s [-c] [-b] [-n] [-s] <path-expression>\n", pgm);
}
int
main (int argc, char **argv)
{
	int		n;
	int		rc;
	char		*orig, *target;
	int		classic, basename, newline, sort;
	
	classic = 0;
	basename = 0;
	newline = 0;
	sort = 0;
	while ((n = getopt (argc, argv, "cbns?h")) != -1)
		switch (n) {
		case 'c':
			classic = 1;
			break;
		case 'b':
			basename = 1;
			break;
		case 'n':
			newline = 1;
			break;
		case 's':
			sort = 1;
			break;
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
			if (classic) {
				for (n = count - 1; n > 0; --n) {
					for (m = n - 1; m >= 0; --m)
						if (equal (elements[n], elements[m], basename))
							break;
					if (m >= 0) {
						for (m = n; m < count - 1; ++m)
							elements[m] = elements[m + 1];
						--count;
					}
				}
			} else {
				for (n = 0; n < count - 1; ) {
					for (m = n + 1; m < count; ++m)
						if (equal (elements[n], elements[m], basename))
							break;
					if (m < count) {
						for (m = n; m < count - 1; ++m)
							elements[m] = elements[m + 1];
						--count;
					} else
						++n;
				}
			}
			if (sort) {
				sort_elements (elements, count);
			}
			for (ptr = target, n = 0; n < count; ++n) {
				if (ptr != target)
					*ptr++ = ':';
				strcpy (ptr, elements[n]);
				while (*ptr)
					++ptr;
			}
			write (1, target, strlen (target));
			if (newline)
				write (1, "\n", 1);
			rc = 0;
			free (elements);
		}
		free (target);
		free (orig);
	}
	return rc;
}
