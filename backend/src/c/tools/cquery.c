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
# include	"agn.h"

static void
usage (const char *pgm) /*{{{*/
{
	fprintf (stderr, "Usage: %s [-c <config-file>] [-d|<field>+]\n", pgm);
}/*}}}*/
int
main (int argc, char **argv) /*{{{*/
{
	int		rc;
	int		n;
	const char	*config;
	bool_t		dump;
	void		*cfg;
	const char	*key, *value;
	
	config = NULL;
	dump = false;
	while ((n = getopt (argc, argv, "c:d?h")) != -1)
		switch (n) {
		case 'c':
			config = optarg;
			break;
		case 'd':
			dump = true;
			break;
		case '?':
		case 'h':
		default:
			return usage (argv[0]), (n != '?') && (n != 'h');
		}
	if (! (cfg = systemconfig_alloc (config)))
		return fprintf (stderr, "Failed to setup config %s.\n", config ? config : "from default"), 1;
	rc = 0;
	if (dump) {
		if (optind == argc) {
			for (n = 0; systemconfig_get (cfg, n, & key, & value); ++n)
				printf ("%s=%s\n", key, value);
		} else {
			for (n = optind; n < argc; ++n) {
				value = systemconfig_find (cfg, argv[n]);
				if (value)
					printf ("%s=%s\n", argv[n], value);
				else
					printf ("%s\n", argv[n]);
			}
		}
	} else {
		for (n = optind; n < argc; ++n) {
			value = systemconfig_find (cfg, argv[n]);
			printf ("%s\n", (value ? value : ""));
			if (! value) {
				rc = 1;
			}
		}
	}
	systemconfig_free (cfg);
	return rc;
}/*}}}*/
