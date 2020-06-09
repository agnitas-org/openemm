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
# include	<string.h>
# include	"xmlback.h"

typedef struct { /*{{{*/
	char		*fname;		/* filename to write result to	*/
	/*}}}*/
}	count_t;

void *
count_oinit (blockmail_t *blockmail, var_t *opts) /*{{{*/
{
	count_t	*c;
	
	if (c = (count_t *) malloc (sizeof (count_t))) {
		var_t		*tmp;
		const char	*fname;
		
		fname = NULL;
		for (tmp = opts; tmp; tmp = tmp -> next)
			if ((! tmp -> var) || var_partial_imatch (tmp, "path"))
				fname = tmp -> val;
		c -> fname = NULL;
		if (fname && (! (c -> fname = strdup (fname)))) {
			free (c);
			c = NULL;
		}
	}
	return c;
}/*}}}*/
bool_t
count_odeinit (void *data, blockmail_t *blockmail, bool_t success) /*{{{*/
{
	count_t	*c = (count_t *) data;
	bool_t	st;
	
	st = false;
	if (success && c) {
		FILE		*fp;
		counter_t	*run;
		
		if (c -> fname)
			fp = fopen (c -> fname, "a");
		else
			fp = stdout;
		if (fp) {
			st = true;
			for (run = blockmail -> counter; run && st; run = run -> next) {
				long	mb = (run -> bytecount + 1024 * 1024 - 1) / (1024 * 1024);
				
				log_out (blockmail -> lg, LV_DEBUG, "%s/%d: %8ld Mail%s (%8ld skipped) %8ld MByte%s (%ld bcc)",
					 run -> mediatype, run -> subtype,
					 run -> unitcount, (run -> unitcount == 1 ? ", " : "s,"), run -> unitskip,
					 mb, (mb == 1 ? "" : "s"),
					 run -> bccunitcount);
				if (fprintf (fp, "%s\t%d\t%ld\t%ld\t%lld\t%ld\t%lld\n",
					     run -> mediatype,
					     run -> subtype,
					     run -> unitcount,
					     run -> unitskip,
					     run -> bytecount,
					     run -> bccunitcount,
					     run -> bccbytecount) == EOF) {
					log_out (blockmail -> lg, LV_ERROR, "Hit EOF on writing to %s (%m)", (c -> fname ? c -> fname : "*stdout*"));
					st = false;
				}
			}
			if (fp != stdout) {
				if (fclose (fp) == EOF) {
					log_out (blockmail -> lg, LV_ERROR, "Failed to close %s (%m)", c -> fname);
					st = false;
				}
			} else
				if (fflush (fp) == EOF) {
					log_out (blockmail -> lg, LV_ERROR, "Failed to flush stdout (%m)");
					st = false;
				}
		} else if (c -> fname)
			log_out (blockmail -> lg, LV_ERROR, "Unable to open file %s (%m)", c -> fname);
	} else
		st = true;
	if (c) {
		if (c -> fname)
			free (c -> fname);
		free (c);
	}
	return st;
}/*}}}*/
bool_t
count_owrite (void *data, blockmail_t *blockmail, receiver_t *rec) /*{{{*/
{
	return true;
}/*}}}*/
