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
# include	<unistd.h>
# include	<fcntl.h>
# include	<string.h>
# include	<errno.h>
# include	<sys/types.h>
# include	<sys/stat.h>
# include	"qctrl.h"

static long
hasher (const char *str) /*{{{*/
{
	long	hash;
	
	hash = 0;
	while (*str)
		hash = (hash * 113) + *str++;
	return hash;
}/*}}}*/

typedef struct { /*{{{*/
	char	*host;		/* the unreached host			*/
	long	hash;		/* some kind of hash value over host	*/
	long	count;		/* # of occurances in queue		*/
	/*}}}*/
}	host_t;
static host_t *
host_alloc (const char *host, long hash) /*{{{*/
{
	host_t	*h;
	
	if (h = (host_t *) malloc (sizeof (host_t)))
		if (h -> host = strdup (host)) {
			h -> hash = hash;
			h -> count = 1;
		} else {
			free (h);
			h = NULL;
		}
	return h;
}/*}}}*/
static host_t *
host_free (host_t *h) /*{{{*/
{
	if (h) {
		if (h -> host)
			free (h -> host);
		free (h);
	}
	return NULL;
}/*}}}*/

typedef struct { /*{{{*/
	host_t	**h;		/* all hosts				*/
	int	c, s;		/* count/size of hosts			*/
	/*}}}*/
}	hc_t;
static hc_t *
hc_alloc (void) /*{{{*/
{
	hc_t	*h;
	
	if (h = (hc_t *) malloc (sizeof (hc_t))) {
		h -> h = NULL;
		h -> c = 0;
		h -> s = 0;
	}
	return h;
}/*}}}*/
static hc_t *
hc_free (hc_t *h) /*{{{*/
{
	if (h) {
		if (h -> h) {
			int	n;
		
			for (n = 0; n < h -> c; ++n)
				host_free (h -> h[n]);
			free (h -> h);
		}
		free (h);
	}
	return NULL;
}/*}}}*/
static host_t *
hc_host (hc_t *h, const char *host) /*{{{*/
{
	host_t	*ret;
	long	hash;
	int	n;
	
	ret = NULL;
	hash = hasher (host);
	for (n = 0; n < h -> c; ++n)
		if ((h -> h[n] -> hash == hash) && (! strcmp (h -> h[n] -> host, host)))
			break;
	if (n == h -> c) {
		if (h -> c >= h -> s) {
			int	nsize;
			host_t	**tmp;

			nsize = (h -> s ? h -> s * 2 : 256);
			if (tmp = (host_t **) realloc (h -> h, nsize * sizeof (host_t *))) {
				h -> h = tmp;
				h -> s = nsize;
			}
		}
		if ((h -> c < h -> s) && (h -> h[h -> c] = host_alloc (host, hash)))
			ret = h -> h[h -> c++];
	} else {
		h -> h[n] -> count++;
		ret = h -> h[n];
	}
	return ret;
}/*}}}*/
static int
compare (const void *a, const void *b) /*{{{*/
{
	return (int) ((*((host_t **) a)) -> count - (*((host_t **) b)) -> count);
}/*}}}*/
static void
hc_sort (hc_t *h) /*{{{*/
{
	if (h -> c > 1)
		qsort (h -> h, h -> c, sizeof (host_t *), compare);
}/*}}}*/

static struct { /*{{{*/
	int		tdiff;	/* time difference for age		*/
	const char	*desc;	/* description for this entry		*/
	/*}}}*/
}	agetab[] = { /*{{{*/
# define	T_SEC		* 1	
# define	T_MIN		* (60 T_SEC)
# define	T_HOUR		* (60 T_MIN)
# define	T_DAY		* (24 T_HOUR)
# define	T_WEEK		* (7 T_DAY)
# define	T_MONTH		* (30 T_DAY)	
	{	30 T_SEC,	"<= 30 secs"	},
	{	 1 T_MIN,	"> 30 secs"	},
	{	10 T_MIN,	"> 1 min"	},
	{	30 T_MIN,	"> 10 mins"	},
	{	 1 T_HOUR,	"> 30 mins"	},
	{	 6 T_HOUR,	"> 1 hour"	},
	{	12 T_HOUR,	"> 6 hours"	},
	{	 1 T_DAY,	"> 12 hours"	},
	{	 2 T_DAY,	"> 1 day"	},
	{	 1 T_WEEK,	"> 2 days"	},
	{	 2 T_WEEK,	"> 1 week"	},
	{	 1 T_MONTH,	"> 2 weeks"	},
	{	-1,		"> 1 month"	}
	/*}}}*/
};
# define	AGETABSIZE	(sizeof (agetab) / sizeof (agetab[0]))

typedef struct { /*{{{*/
	log_t	*lg;		/* for logging purpose			*/
	char	**paths;	/* all paths to check			*/
	int	pcount;		/* # of paths				*/

	time_t	now;		/* current timestamp			*/
	long	count;		/* # of mails collected			*/
				/* # of mails, age related		*/
	long	dage[AGETABSIZE];
	hc_t	*hc;		/* host collection			*/
	/*}}}*/
}	stat_t;

void *
stat_init (log_t *lg, bool_t force, char **args, int alen) /*{{{*/
{
	stat_t		*s;
	
	if (s = (stat_t *) malloc (sizeof (stat_t))) {
		bool_t		st;
		int		n;
		struct stat	fst;

		s -> lg = lg;
		s -> paths = args;
		s -> pcount = alen;
		s -> now = 0;
		st = true;
		for (n = 0; n < s -> pcount; ++n)
			if (stat (s -> paths[n], & fst) == -1) {
				log_out (s -> lg, LV_ERROR, "Unable to stat path #%d: %s (%d, %m)", n, s -> paths[n], errno);
				st = false;
			} else if (! S_ISDIR (fst.st_mode)) {
				log_out (s -> lg, LV_ERROR, "Path #%d: %s is not a directory", n, s -> paths[n]);
				st = false;
			}
		if (! st) {
			free (s);
			s = NULL;
		}
	}
	return s;
}/*}}}*/
bool_t
stat_deinit (void *data) /*{{{*/
{
	stat_t	*s = (stat_t *) data;
	
	free (s);
	return true;
}/*}}}*/

static int
collect_stats (void *data, queue_t *q, const char *fname) /*{{{*/
{
	stat_t	*s = (stat_t *) data;
	int	fd;
	
	if ((fname[0] == 'q') && (fname[1] == 'f') && ((fd = open (fname, O_RDONLY)) != -1)) {
		qf_t		*qf;
		const char	*ptr;
		int		diff;
		int		n;

		if (queue_readfd (q, fd) && (qf = qf_alloc (q -> qf))) {
			s -> count++;
			if (ptr = qf_first (qf, 'T')) {
				diff = (int) (s -> now - atoi (ptr + 1));
				for (n = 0; n < AGETABSIZE; ++n)
					if ((agetab[n].tdiff == -1) || (diff <= agetab[n].tdiff)) {
						s -> dage[n]++;
						break;
					}
			}
			if (s -> hc)
				for (ptr = qf_first (qf, 'R'); ptr; ptr = qf_next (qf, 'R')) {
					char	*copy, *p1, *p2;
				
					if (copy = strdup (ptr)) {
						if (p1 = strchr (copy, ':'))
							++p1;
						else
							p1 = copy + 1;
						if (*p1 == '<') {
							++p1;
							if (p2 = strchr (p1, '>'))
								*p2 = '\0';
							if (p2 = strchr (p1, '@'))
								if (! hc_host (s -> hc, p2 + 1))
									s -> hc = hc_free (s -> hc);
						}
						free (copy);
					}
				}
			qf_free (qf);
		}
		close (fd);
	}
	return 0;
}/*}}}*/
bool_t
stat_exec (void *data) /*{{{*/
{
	stat_t	*s = (stat_t *) data;
	bool_t	st;
	bool_t	first;
	int	n, m;
	queue_t	*q;
	
	st = true;
	first = true;
	time (& s -> now);
	for (n = 0; (n < s -> pcount) && st; ++n) {
		s -> count = 0;
		for (m = 0; m < sizeof (s -> dage) / sizeof (s -> dage[0]); ++m)
			s -> dage[m] = 0;
		if (chdir (s -> paths[n]) != -1) {
			s -> hc = hc_alloc ();
			if (q = queue_scan (s -> paths[n], collect_stats, s)) {
				if (first) {
					first = false;
					log_out (s -> lg, LV_NONE, "Current statistics:");
					log_out (s -> lg, LV_NONE, "===================");
				}
				log_out (s -> lg, LV_NONE, "%3d: %s: %ld", n + 1, s -> paths[n], s -> count);
				for (m = 0; m < AGETABSIZE; ++m)
					if (s -> dage[m])
						log_out (s -> lg, LV_NONE, " A:%24s: %8ld", agetab[m].desc, s -> dage[m]);
				if (s -> hc && (s -> hc -> c > 0)) {
					hc_sort (s -> hc);
					log_out (s -> lg, LV_NONE, " ------------------------------------");
					for (m = s -> hc -> c - 1; (m >= 0) && (m > s -> hc -> c - 11); --m)
						log_out (s -> lg, LV_NONE, " H:%24s: %8ld", s -> hc -> h[m] -> host, s -> hc -> h[m] -> count);
				}
				queue_free (q);
			} else {
				log_out (s -> lg, LV_ERROR, "Unable to read path #%d: %s", n, s -> paths[n]);
				st = false;
			}
			if (s -> hc)
				s -> hc = hc_free (s -> hc);
		} else {
			log_out (s -> lg, LV_ERROR, "Unable to chdir to path #%d: %s (%d, %m)", n, s -> paths[n], errno);
			st = false;
		}
	}
	return st;
}/*}}}*/
