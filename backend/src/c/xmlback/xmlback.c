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
# include	<unistd.h>
# include	<fcntl.h>
# include	<string.h>
# include	<locale.h>
# include	<sys/types.h>
# include	<sys/stat.h>
# include	"xmlback.h"

static output_t	output_table[] = { /*{{{*/
	{	"none",
		"\tFunction: just validate the input, no output is created\n"
		"\tOptions: none\n"
		,false,
		none_oinit, none_odeinit, none_owrite
	}, {	"generate",
		"\tFunction: creates real output files\n"
		"\tOptions:\n"
		"\t\tmedia=<media>      following parameter are bound to this <media>\n"
		"\t\t                   available media: email\n"
		"\t\ttemporary=<flags>  true to write unique queue-ids, for test and admin mailings\n"
		"\t\taccount-logfile=<path>    path to file to write accounting information to\n"
		"\t\tbounce-logfile=<path>     path to write bounce information to\n"
		"\t\tmessageid-logfile=<path>  path to write message-id mappings to\n"
		"\t\tmailtrack-logfile=<path>  path to write mailtrack information to\n"
		"\t\tpath=<path>        path to queue directory to write spool files to\n"
		"\tOptions specific for email:\n"
		"\t\taction=<cmd>       command to execute after mail generation\n"
		"\t\tqueue-flush=<n>    number of domains to start a queue flusher for\n"
		"\t\tqueue-flush-command=<path>  path to command to flush queue\n"
		,true,
		generate_oinit,	generate_odeinit, generate_owrite
	}, {	"count",
		"\tFunction: creates the output in memory and writes the number\n"
		"\t          of mails and bytes to stdout\n"
		"\tOptions: none\n"
		,false,
		count_oinit, count_odeinit, count_owrite
	}, {	"preview",
		"\tFunction: creates the mail and outputs them as a XML file\n"
		"\tOptions:\n"
		"\t\tpath=<path>        filename for the resulting XML file\n"
		"\t\tlink-prefix=<str>  prefix auto generated links with <str>\n"
		,false,
		preview_oinit, preview_odeinit, preview_owrite
	}
	/*}}}*/
};
static var_t *
parse_parm (const char *str) /*{{{*/
{
	var_t	*base, *prev, *tmp;
	char	*copy, *ptr, *sav, *equal, *var, *val, *nxt;
	int	n, m;

	base = NULL;
	prev = NULL;
	if (copy = strdup (str)) {
		for (ptr = copy; *ptr; ) {
			sav = ptr;
			equal = NULL;
			for (n = 0, m = 0; ptr[n] && (ptr[n] != ';'); ++n) {
				if ((ptr[n] == '\\') && ptr[n + 1])
					++n;
				else if ((! equal) && (ptr[n] == '='))
					equal = ptr + m;
				if (m != n)
					ptr[m++] = ptr[n];
				else
					++m;
			}
			if (n != m)
				ptr[m] = '\0';
			ptr += n;
			if (*ptr)
				*ptr++ = '\0';
			if (equal) {
				var = sav;
				val = equal;
				*val++ = '\0';
				if (! *var)
					var = NULL;
			} else {
				var = NULL;
				val = sav;
			}
			do {
				nxt = NULL;
				if (var && (nxt = strchr (var, ',')))
					*nxt++ = '\0';
				if (tmp = var_alloc (var, val)) {
					if (prev)
						prev -> next = tmp;
					else
						base = tmp;
					prev = tmp;
				} else {
					base = var_free_all (base);
					break;
				}
				var = nxt;
			}	while (var);
			if (! base)
				break;
		}
		free (copy);
	}
	return base;
}/*}}}*/
int
main (int argc, char **argv) /*{{{*/
{
	int		n, m;
	const char	*ptr;
	int		len;
	bool_t		quiet;
	const char	*error_file;
	bool_t		usecrlf;
	bool_t		raw;
	output_t	*out;
	const char	*outparm;
	const char	*auto_url_prefix;
	bool_t		anon;
	const char	*selector;
	bool_t		convert_to_entities;
	bool_t		force_ecs_uid;
	char		*fqdn;
	const char	*level;
	time_t		pointintime;
	var_t		*pparm;
	log_t		*lg;
	FILE		*errfp;
	FILE		*devnull;
	bool_t		st, dst;
	
	quiet = false;
	error_file = NULL;
	usecrlf = true;
	raw = false;
	out = & output_table[1];
	outparm = NULL;
	auto_url_prefix = NULL;
	anon = false;
	selector = NULL;
	convert_to_entities = false;
	force_ecs_uid = false;
	fqdn = NULL;
	level = NULL;
	pointintime = 0;
	setlocale (LC_ALL, "");
	xmlInitParser ();
	xmlInitializePredefinedEntities ();
	xmlInitCharEncodingHandlers ();
	json_set_escape_slashes (0);
	while ((n = getopt (argc, argv, "VDpqE:lru:as:egd:t:o:L:T:h")) != -1)
		switch (n) {
		case 'V':
# ifdef		EMM_VERSION			
			printf ("Build version: %s\n", EMM_VERSION);
# endif		/* EMM_VERSION */
			return 0;
		case 'p':
			xmlPedanticParserDefault (1);
			break;
		case 'q':
			quiet = true;
			break;
		case 'E':
			error_file = optarg;
			break;
		case 'l':
			usecrlf = false;
			break;
		case 'r':
			raw = true;
			break;
		case 'u':
			auto_url_prefix = optarg;
			break;
		case 'a':
			anon = true;
			break;
		case 's':
			selector = optarg;
			break;
		case 'e':
			convert_to_entities = true;
			break;
		case 'g':
			force_ecs_uid = true;
			break;
		case 'd':
			if (fqdn)
				free (fqdn);
			fqdn = strdup (optarg);
			break;
		case 't':
			break;
		case 'o':
			if (ptr = strchr (optarg, ':')) {
				len = ptr - optarg;
				++ptr;
			} else
				len = strlen (optarg);
			out = NULL;
			outparm = NULL;
			for (n = 0; n < sizeof (output_table) / sizeof (output_table[0]); ++n)
				if ((len == strlen (output_table[n].name)) && (! strncmp (optarg, output_table[n].name, len))) {
					out = & output_table[n];
					break;
				}
			if (! out)
				return fprintf (stderr, "Invalid output method %s specified, aborted.\n", optarg), 1;
			outparm = ptr;
			break;
		case 'L':
			level = optarg;
			break;
		case 'T':
			pointintime = atol (optarg);
			break;
		case 'h':
		default:
			fprintf (stderr, "Usage: %s [-h] [-V] [-L <loglevel>] [-D] [-v] [-p] [-q] [-E <file>] [-l] [-r] [-d <domain>] [-o <output>[:<parm>] <file(s)>\n", argv[0]);
			fprintf (stderr, "       further options: [-u <prefix>] [-a] [-s <selector>] [-e] [-f]\n");
			fputs ("Function: read and process XML files generated from database representation\n"
			       "Options:\n"
			       "\t-h         output this help page\n"
			       "\t-V         shows version\n"
			       "\t-L <level> sets the logging level to <level>\n"
			       "\t-p         switch on pedantic XML parsing\n"
			       "\t-q         quiet mode, do not print logging to stdout\n"
			       "\t-E <fname> write error messages to file <fname> instead of stderr\n"
			       "\t-l         use just NL instead of CR-NL as end of line in generated mails\n"
			       "\t-r         raw output, do not encode generated mails (used by preview)\n"
			       "\t-u <pfix>  use <pfix> as prefix for generated auto urls\n"
			       "\t-a         anonymize the output as far as possible\n"
			       "\t-s <sel>   selector to restrict usage of text blocks\n"
			       "\t-e         convert known special characters to its HTML entity\n"
			       "\t-g         force generation of extended click statistics UIDs\n"
			       "\t-d <fqdn>  use this as my full qualified domain name\n"
			       "\t-o <out>   defines the output behaviour for generated mails\n"
			       "\t-T <time>  a point in time to use instead of current time (in epoch)\n"
			       "\n"
			       "Output options may be written behind the module name, separated by a colon;\n"
			       "these options are in <variable>=<value> style and are separated by semicolons.\n"
			       "These output modules are currently available:\n",
			       stderr);
			for (m = 0; m < sizeof (output_table) / sizeof (output_table[0]); ++m) {
				fprintf (stderr, "  %s\n", output_table[m].name);
				if (output_table[m].desc) {
					fputs (output_table[m].desc, stderr);
					fputs ("\n", stderr);
				}
			}
			return n != 'h';
		}
	pparm = NULL;
	if (outparm && outparm[0] && (! (pparm = parse_parm (outparm))))
		return fprintf (stderr, "Unable to parse output paramter %s, aborted.\n", outparm), 1;
	if (! (lg = log_alloc (NULL, argv[0], level)))
		return fprintf (stderr, "Unable to setup logging interface, aborted.\n"), 1;
	errfp = NULL;
	if (error_file) {
		if (! strcmp (error_file, "-"))
			errfp = stdout;
		else if (! (errfp = fopen (error_file, "a")))
			return fprintf (stderr, "Unable to open error file %s, aborted.\n", error_file), 1;
		xmlSetGenericErrorFunc (errfp, NULL);
		log_collect (lg, LV_ERROR);
	}
	devnull = NULL;
	if (! quiet) {
		if ((! level) && (lg -> level < LV_INFO))
			lg -> level = LV_INFO;
		log_tofd (lg, 2);
	} else {
		if ((! level) && (lg -> level < LV_NOTICE))
			lg -> level = LV_NOTICE;
		if (! errfp) {
			devnull = fopen (_PATH_DEVNULL, "r+");
			xmlSetGenericErrorFunc (devnull, NULL);
		}
	}
	if (! fqdn)
		fqdn = get_fqdn ();
	st = true;
	srandom (pointintime ? pointintime : time (NULL));
	for (n = optind; st && (n < argc); ++n) {
		blockmail_t	*blockmail;
		xmlDocPtr	doc;
		xmlNodePtr	base;
	
		st = false;
		if (! (blockmail = blockmail_alloc (argv[n], out -> syncfile, lg)))
			log_out (lg, LV_ERROR, "Unable to setup blockmail");
		else {
			blockmail -> usecrlf = usecrlf;
			blockmail -> raw = raw;
			blockmail -> output = out;
			blockmail -> outputdata = NULL;
			log_idset (lg, "init");
			blockmail -> outputdata = (*out -> oinit) (blockmail, pparm);
			blockmail_setup_auto_url_prefix (blockmail, auto_url_prefix);
			blockmail_setup_anon (blockmail, anon);
			blockmail_setup_selector (blockmail, selector);
			blockmail -> force_ecs_uid = force_ecs_uid;
			blockmail -> convert_to_entities = convert_to_entities;
			blockmail -> fqdn = fqdn;
			blockmail -> pointintime = pointintime;
			log_idclr (lg);
			if (! blockmail -> outputdata)
				log_out (lg, LV_ERROR, "Unable to initialize output method %s for %s", out -> name, argv[n]);
			else {
				doc = xmlReadFile (argv[n], NULL, XML_PARSE_NONET | XML_PARSE_NOCDATA | XML_PARSE_COMPACT | XML_PARSE_HUGE);
				if (doc) {
					if (doc -> encoding) {
						blockmail -> translate = xmlFindCharEncodingHandler (xml2char (doc -> encoding));
						if (! (blockmail -> translate -> input || blockmail -> translate -> iconv_in ||
						       blockmail -> translate -> output || blockmail -> translate -> iconv_out)) {
							xmlCharEncCloseFunc (blockmail -> translate);
							blockmail -> translate = NULL;
						}
					}
					if (base = xmlDocGetRootElement (doc))
						st = parse_file (blockmail, doc, base);
					xmlFreeDoc (doc);
				} else
					log_out (lg, LV_ERROR, "Unable to open/parse file %s", argv[n]);
				if (st)
					blockmail_count_sort (blockmail);
				log_idset (lg, "deinit");
				dst = (*out -> odeinit) (blockmail -> outputdata, blockmail, st);
				log_idclr (lg);
				if (! dst) {
					log_out (lg, LV_ERROR, "Unable to deinitialize output method %s for %s", out -> name, argv[n]);
					st = false;
				}
			}
			if (st)
				blockmail_unsync (blockmail);
			blockmail_free (blockmail);
		}
	}
	if (fqdn)
		free (fqdn);
	if (errfp && lg -> collect && (lg -> collect -> length > 0))
		if (fwrite (lg -> collect -> buffer, sizeof (lg -> collect -> buffer[0]), lg -> collect -> length, errfp) == lg -> collect -> length)
			fflush (errfp);
	log_free (lg);
	if (pparm)
		var_free_all (pparm);
	xmlCleanupCharEncodingHandlers ();
	xmlCleanupPredefinedEntities ();
	xmlCleanupParser ();
	if (devnull)
		fclose (devnull);
	if (errfp)
		if (errfp == stdout)
			fflush (stdout);
		else
			fclose (errfp);
	return (! st) || (n < argc) ? 1 : 0;
}/*}}}*/
