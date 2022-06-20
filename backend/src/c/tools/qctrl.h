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
# ifndef	__QCTRL_H
# define	__QCTRL_H		1
# include	"agn.h"

typedef struct entry { /*{{{*/
	char		*fname;		/* filename of entry		*/
	int		match;		/* the value return by filter	*/
	struct entry	*next;
	/*}}}*/
}	entry_t;
typedef struct { /*{{{*/
	char	**content;	/* the content of the qf file		*/
	int	count;		/* # of lines in the qf file		*/
	int	idx;		/* current index for searches		*/
	/*}}}*/
}	qf_t;
typedef struct { /*{{{*/
	char		path[PATH_MAX + 1];	/* path to the queue	*/
	char		fbuf[PATH_MAX + 1];	/* scratch buffer	*/
	char		*fptr;		/* filepointer into fbuf	*/
	buffer_t	*qf;		/* buffer to read q-files	*/
	entry_t		*ent;		/* all matching entries		*/
	/*}}}*/
}	queue_t;

extern entry_t		*entry_alloc (const char *fname, int match);
extern entry_t		*entry_free (entry_t *e);
extern entry_t		*entry_free_all (entry_t *e);

extern qf_t		*qf_alloc (const buffer_t *src);
extern qf_t		*qf_free (qf_t *q);
extern const char	*qf_first (qf_t *q, char ch);
extern const char	*qf_next (qf_t *q, char ch);
extern int		qf_count (qf_t *q, char ch);

extern queue_t		*queue_scan (const char *path, int (*filter) (void *, queue_t *, const char *), void *data);
extern queue_t		*queue_free (queue_t *q);
extern int		queue_lock (queue_t *q, const char *fname, pid_t pid);
extern void		queue_unlock (queue_t *q, int fd, pid_t pid);
extern bool_t		queue_read (queue_t *q, const char *fname);
extern bool_t		queue_readfd (queue_t *q, int fd);

/*
 * command related routines 
 */
extern void		*move_init (log_t *lg, bool_t force, char **args, int alen);
extern bool_t		move_deinit (void *data);
extern bool_t		move_exec (void *data);
extern void		*stat_init (log_t *lg, bool_t force, char **args, int alen);
extern bool_t		stat_deinit (void *data);
extern bool_t		stat_exec (void *data);

extern void		*flush_init (log_t *lg, bool_t force, char **args, int alen);
extern bool_t		flush_deinit (void *data);
extern bool_t		flush_exec (void *data);
# endif		/* __QCTRL_H */
