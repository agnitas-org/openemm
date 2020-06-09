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
# include	<ctype.h>
# include	<unistd.h>
# include	<string.h>
# include	<sys/types.h>
# include	<sys/stat.h>
# include	<regex.h>
# include	<netinet/in.h>
# include	"libmilter/mfapi.h"
# include	"bav.h"

# define	SOCK_PATH		"var/run/bav.sock"
# define	LOCK_PATH		"var/lock/bav.lock"
# define	CFGFILE			"var/lib/bav.conf"
# define	X_AGN			"X-AGNMailloop"
# define	X_LOOP			"X-AGNLoop"
# define	LOOP_SET		"set"

# define	LCFG_LOCALS_KEY		"filter-local-hostnames"

typedef struct locals { /*{{{*/
	regex_t		re;
	struct locals	*next;
	/*}}}*/
}	locals_t;

static const char	*program;
static const char	*loglevel;
static char		*cfgfile;
static locals_t		*locals;

static locals_t *
locals_alloc (const char *pattern) /*{{{*/
{
	locals_t	*l;
	
	if (l = (locals_t *) malloc (sizeof (locals_t))) {
		if (regcomp (& l -> re, pattern, REG_EXTENDED | REG_ICASE | REG_NOSUB) == 0) {
			l -> next = NULL;
		} else {
			free (l);
			l = NULL;
		}
	}
	return l;
}/*}}}*/
static locals_t *
locals_free (locals_t *l) /*{{{*/
{
	if (l) {
		regfree (& l -> re);
		free (l);
	}
	return NULL;
}/*}}}*/
static locals_t *
locals_free_all (locals_t *l) /*{{{*/
{
	locals_t	*tmp;
	
	while (tmp = l) {
		l = l -> next;
		locals_free (tmp);
	}
	return NULL;
}/*}}}*/

typedef struct charc { /*{{{*/
	char		*str;
	struct charc	*next;
	/*}}}*/
}	charc_t;
static charc_t *
charc_free (charc_t *c) /*{{{*/
{
	if (c) {
		if (c -> str)
			free (c -> str);
		free (c);
	}
	return NULL;
}/*}}}*/
static charc_t *
charc_free_all (charc_t *c) /*{{{*/
{
	charc_t	*tmp;
	
	while (tmp = c) {
		c = c -> next;
		charc_free (tmp);
	}
	return NULL;
}/*}}}*/
static charc_t *
charc_alloc (const char *str) /*{{{*/
{
	charc_t	*c;
	
	if (c = (charc_t *) malloc (sizeof (charc_t))) {
		c -> str = str ? strdup (str) : NULL;
		c -> next = NULL;
		if (str && (! c -> str))
			c = charc_free (c);
	}
	return c;
}/*}}}*/

typedef struct { /*{{{*/
	cfg_t		*cfg;
	bool_t		is_local;
	int		x_agn;
	char		*from;
	charc_t		*receiver, *prev;
	char		*info;
	log_t		*lg;
	/*}}}*/
}	priv_t;
static char *
xfree (char *s) /*{{{*/
{
	if (s)
		free (s);
	return NULL;
}/*}}}*/
static bool_t
xcopy (char **buf, const char *str) /*{{{*/
{
	if (*buf)
		free (*buf);
	*buf = str ? strdup (str) : NULL;
	return (! str) || *buf ? true : false;
}/*}}}*/
static void
priv_clear (priv_t *p) /*{{{*/
{
	if (p) {
		p -> x_agn = 0;
		p -> from = xfree (p -> from);
		p -> receiver = charc_free_all (p -> receiver);
		p -> prev = NULL;
		p -> info = xfree (p -> info);
	}
}/*}}}*/
static priv_t *
priv_free (priv_t *p) /*{{{*/
{
	if (p) {
		priv_clear (p);
		if (p -> lg)
			log_free (p -> lg);
		if (p -> cfg)
			cfg_free (p -> cfg);
		free (p);
	}
	return NULL;
}/*}}}*/
static priv_t *
priv_alloc (void) /*{{{*/
{
	priv_t	*p;
	
	if (p = (priv_t *) malloc (sizeof (priv_t)))
		if (p -> cfg = cfg_alloc (cfgfile)) {
			p -> is_local = false;
			p -> x_agn = 0;
			p -> from = NULL;
			p -> receiver = NULL;
			p -> prev = NULL;
			p -> info = NULL;
			if (! (p -> lg = log_alloc (NULL, program, loglevel)))
				p = priv_free (p);
		} else {
			free (p);
			p = NULL;
		}
	return p;
}/*}}}*/
static bool_t
priv_setfrom (priv_t *p, const char *from) /*{{{*/
{
	return xcopy (& p -> from, from);
}/*}}}*/
static bool_t
priv_setto (priv_t *p, const char *to) /*{{{*/
{
	charc_t	*r;
	
	if (r = charc_alloc (to)) {
		if (p -> prev)
			p -> prev -> next = r;
		else
			p -> receiver = r;
		p -> prev = r;
	}
	return r ? true : false;
}/*}}}*/
static bool_t
priv_addinfo (priv_t *p, const char *info) /*{{{*/
{
	char	*temp;

	if ((! p -> info) || (! p -> info[0]))
		return xcopy (& p -> info, info);
	if (temp = malloc (strlen (p -> info) + strlen (info) + 2)) {
		sprintf (temp, "%s,%s", p -> info, info);
		free (p -> info);
		p -> info = temp;
		return true;
	}
	return false;
}/*}}}*/
static bool_t
priv_addinfopair (priv_t *p, const char *var, const char *val) /*{{{*/
{
	bool_t	rc;
	char	*scratch, *ptr;
	
	if (scratch = malloc (strlen (var) + strlen (val) + 2)) {
		for (ptr = scratch; *var; *ptr++ = *var++)
			;
		*ptr++ = '=';
		for (;*val; ++val)
			*ptr++ = *val == ',' ? '_' : *val;
		*ptr = '\0';
		rc = priv_addinfo (p, scratch);
		free (scratch);
	} else
		rc = false;
	return rc;
}/*}}}*/

static sfsistat
handle_connect (SMFICTX *ctx, char  *hostname, _SOCK_ADDR *hostaddr) /*{{{*/
{
	priv_t	*p;

	if (! (p = priv_alloc ()))
		return SMFIS_TEMPFAIL;
	if (hostaddr -> sa_family == AF_INET) {
		struct sockaddr_in	*iaddr = (struct sockaddr_in *) hostaddr;

		if (ntohl (iaddr -> sin_addr.s_addr) == INADDR_LOOPBACK)
			p -> is_local = true;
	}
# ifdef		AF_INET6
	else if (hostaddr -> sa_family == AF_INET6) {
		struct sockaddr_in6	*i6addr = (struct sockaddr_in6 *) hostaddr;
		static struct in6_addr	loopback = IN6ADDR_LOOPBACK_INIT;
		
		if (memcmp (& i6addr -> sin6_addr, & loopback, sizeof (i6addr -> sin6_addr)) == 0)
			p -> is_local = true;
	}
# endif		/* AF_INET6 */

	if (locals && hostname && (! p -> is_local)) {
		locals_t	*run;
		
		for (run = locals; run; run = run -> next)
			if (regexec (& run -> re, hostname, 0, NULL, 0) == 0) {
				p -> is_local = true;
				break;
			}
	}
	smfi_setpriv (ctx, p);
	return SMFIS_CONTINUE;
}/*}}}*/
static sfsistat
handle_from (SMFICTX *ctx, char **argv) /*{{{*/
{
	priv_t	*p = (priv_t *) smfi_getpriv (ctx);
	
	if (! p)
		return SMFIS_TEMPFAIL;
	priv_clear (p);
	if (! priv_setfrom (p, argv[0]))
		return SMFIS_TEMPFAIL;
	priv_addinfopair (p, "from", argv[0]);
	return SMFIS_CONTINUE;
}/*}}}*/
static sfsistat
handle_to (SMFICTX *ctx, char **argv) /*{{{*/
{
	priv_t	*p = (priv_t *) smfi_getpriv (ctx);
	char	*chk, *opt;
	bool_t	reject, tempfail;
	
	if (! p)
		return SMFIS_TEMPFAIL;
	if (p -> is_local)
		return SMFIS_CONTINUE;
	if (! (chk = cfg_valid_address (p -> cfg, argv[0]))) {
		log_out (p -> lg, LV_ERROR, "Unable to setup initial data for `%s'", argv[0]);
		return SMFIS_TEMPFAIL;
	}
	if (opt = strchr (chk, ':'))
		*opt++ = '\0';
	reject = false;
	tempfail = false;
	if (! strcmp (chk, ID_REJECT))
		reject = true;
	else if (! strcmp (chk, ID_TEMPFAIL))
		tempfail = true;
	else if ((! strcmp (chk, ID_ACCEPT)) && opt)
		priv_addinfo (p, opt);
	priv_addinfopair (p, "to", argv[0]);
	free (chk);
	if (reject) {
		log_out (p -> lg, LV_INFO, "Receiver `%s' is rejected", argv[0]);
		smfi_setreply (ctx, (char *) "550", (char *) "5.1.1", (char *) "No such user");
		return SMFIS_REJECT;
	}
	if (tempfail) {
		log_out (p -> lg, LV_INFO, "Receiver `%s' is temp. disbaled", argv[0]);
		smfi_setreply (ctx, (char *) "400", (char *) "4.0.0", (char *) "Please try again later");
		return SMFIS_TEMPFAIL;
	}
	if (! priv_setto (p, argv[0]))
		return SMFIS_TEMPFAIL;
	return SMFIS_CONTINUE;
}/*}}}*/
static sfsistat
handle_header (SMFICTX *ctx, char *field, char *value) /*{{{*/
{
	priv_t	*p = (priv_t *) smfi_getpriv (ctx);

	if (! p)
		return SMFIS_TEMPFAIL;
	if (p -> is_local)
		return SMFIS_CONTINUE;
	if (! strcasecmp (field, X_LOOP)) {
		log_out (p -> lg, LV_WARNING, "Mail from `%s' has already loop marker set, rejected", p -> from);
		smfi_setreply (ctx, (char *) "500", (char *) "5.4.6", (char *) "Loop detected");
		return SMFIS_REJECT;
	}
	if (! strcasecmp (field, X_AGN))
		p -> x_agn++;
	return SMFIS_CONTINUE;
}/*}}}*/
static sfsistat
handle_eom (SMFICTX *ctx) /*{{{*/
{
	priv_t	*p = (priv_t *) smfi_getpriv (ctx);
	int	n;
	
	if (! p)
		return SMFIS_TEMPFAIL;
	for (n = 0; n < p -> x_agn; ++n)
		smfi_chgheader (ctx, (char *) X_AGN, 0, NULL);
	if (! p -> is_local) {
		if (p -> info)
			smfi_addheader (ctx, (char *) X_AGN, p -> info);
		smfi_addheader (ctx, (char *) X_LOOP, (char *) LOOP_SET);
	}
	return SMFIS_CONTINUE;
}/*}}}*/
static sfsistat
handle_close(SMFICTX *ctx) /*{{{*/
{
	priv_free (smfi_getpriv (ctx));
	smfi_setpriv (ctx, NULL);
	return SMFIS_CONTINUE;
}/*}}}*/
static struct smfiDesc	bav = { /*{{{*/
	(char *) "bounce address verification",
	SMFI_VERSION,
	SMFIF_ADDHDRS | SMFIF_CHGHDRS,
	handle_connect,
	NULL,
	handle_from,
	handle_to,
	handle_header,
	NULL,
	NULL,
	handle_eom,
	NULL,
	handle_close,
# if	SMFI_VERSION > 2	
	NULL,
	NULL,
	NULL
# endif	
	/*}}}*/
};

static bool_t
find_locals (void) /*{{{*/
{
	bool_t	rc;
	void	*cfg;
	
	rc = true;
	if (cfg = systemconfig_alloc (NULL)) {
		const char	*value;
		char		*copy;
		
		if ((value = systemconfig_find (cfg, LCFG_LOCALS_KEY)) && (copy = strdup (value))) {
			locals_t	*prev, *temp;
			char		*cur, *ptr;
			
			prev = NULL;
			for (cur = copy; cur; ) {
				if (ptr = strchr (cur, ','))
					*ptr++ = '\0';
				while (isspace (*cur))
					++cur;
				if (*cur)
					if (temp = locals_alloc (cur)) {
						if (prev)
							prev -> next = temp;
						else
							locals = temp;
						prev = temp;
					} else {
						fprintf (stderr, "Invalid pattern \"%s\"\n", cur);
						rc = false;
					}
				cur = ptr;
			}
			free (copy);
		}
		systemconfig_free (cfg);
	}
	return rc;
}/*}}}*/
static int
usage (const char *pgm) /*{{{*/
{
	fprintf (stderr, "Usage: %s [-L <loglevel>] [-s <socket name>] [-s <reread in seconds>] [-c <config filename>] [-l]\n", pgm);
	return 1;
}/*}}}*/
int
main (int argc, char **argv) /*{{{*/
{
	int		rc;
	char		*ptr;
	char		*sock_name;
	int		reread;
	int		n;
	lock_t		*lock;
	const char	*home;
	struct stat	st;

	if (ptr = strrchr (argv[0], '/'))
		program = ptr + 1;
	else
		program = argv[0];
	loglevel = "WARNING";
	sock_name = NULL;
	reread = 0;
	cfgfile = NULL;
	locals = NULL;

	while ((n = getopt (argc, argv, "L:s:r:c:l")) != -1)
		switch (n) {
		case 'L':
			loglevel = optarg;
			break;
		case 's':
			if (sock_name)
				free (sock_name);
			if (! (sock_name = strdup (optarg))) {
				fprintf (stderr, "Failed to allocate memory for socket name %s (%m).\n", optarg);
				return 1;
			}
			break;
		case 'r':
			if ((reread = atoi (optarg)) < 1) {
				fprintf (stderr, "Reread value must be at least 1.\n");
				return 1;
			}
			break;
		case 'c':
			if (cfgfile)
				free (cfgfile);
			if (! (cfgfile = strdup (optarg))) {
				fprintf (stderr, "Failed to allocate memory for config.filename %s (%m).\n", optarg);
				return 1;
			}
			break;
		case 'l':
			if (! find_locals ()) {
				fprintf (stderr, "Failed to read local licence information.\n");
				return 1;
			}
			break;
		default:
			return usage (argv[0]);
		}
	if (optind < argc)
		return usage (argv[0]);
	if (! (lock = lock_alloc (LOCK_PATH))) {
		fprintf (stderr, "Failed to allocate memory for locking (%m).\n");
		return 1;
	}
	if (! lock_lock (lock)) {
		fprintf (stderr, "Instance seems already to run, aborting.\n");
		return 1;
	}

	home = path_home ();
	if ((stat (home, & st) != -1) && S_ISDIR (st.st_mode)) {
		int	newmode = 0111;
		
		if ((st.st_mode & newmode) != newmode) {
			newmode |= st.st_mode & ~S_IFMT;
			chmod (home, newmode);
		}
	}
			
	if ((! sock_name) || (! cfgfile)) {
		if (! sock_name) {
			if (! (sock_name = malloc (strlen (home) + sizeof (SOCK_PATH) + 16))) {
				fprintf (stderr, "Failed to allocate socket name %s/%s (%m).\n", home, SOCK_PATH);
				return 1;
			}
			sprintf (sock_name, "unix:%s/%s", home, SOCK_PATH);
		}
		if (! cfgfile) {
			if (! (cfgfile = malloc (strlen (home) + sizeof (CFGFILE) + 1))) {
				fprintf (stderr, "Failed to allocate config.filename %s/%s (%m).\n", home, CFGFILE);
				return 1;
			}
			sprintf (cfgfile, "%s/%s", home, CFGFILE);
		}
	}
	rc = 1;
	umask (0);
	if (smfi_register (bav) == MI_FAILURE)
		fprintf (stderr, "Failed to register filter.\n");
	else if (smfi_setconn (sock_name) == MI_FAILURE)
		fprintf (stderr, "Failed to register socket name \"%s\".\n", sock_name);
	else if (smfi_opensocket (1) == MI_FAILURE)
		fprintf (stderr, "Failed to open socket socket \"%s\".\n", sock_name);
	else {
		if (smfi_main () == MI_FAILURE)
			fprintf (stderr, "Failed to hand over control to milter.\n");
		else
			rc = 0;
	}
	unlink (sock_name);
	lock_unlock (lock);
	lock_free (lock);
	free (sock_name);
	locals_free_all (locals);
	free (cfgfile);
	return rc;
}/*}}}*/
