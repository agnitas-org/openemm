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
# include	<stdlib.h>
# include	<dirent.h>
# include	<limits.h>
# include	<sys/types.h>
# include	<sys/stat.h>
# include	<dlfcn.h>
# include	"agn.h"
# include	"plugin.h"

# define	FUNC_SETUP		"p_setup"
# define	FUNC_TEARDOWN		"p_teardown"
# define	POSTFIX_SHARED_OBJECT	".so"

plugin_t *
plugin_free (plugin_t *p) /*{{{*/
{
	if (p) {
		if (p -> so) {
			if (p -> teardown)
				(*p -> teardown) (p, p -> priv);
			dlclose (p -> so);
		}
		if (p -> name)
			free (p -> name);
		if (p -> path)
			free (p -> path);
		free (p);
	}
	return NULL;
}/*}}}*/
plugin_t *
plugin_alloc (const char *name, const char *path) /*{{{*/
{
	plugin_t	*p;
	
	if (p = (plugin_t *) malloc (sizeof (plugin_t))) {
		p -> name = strdup (name);
		p -> path = strdup (path);
		p -> so = dlopen (path, RTLD_NOW);
		p -> priv = NULL;
		p -> setup = NULL;
		p -> teardown = NULL;
		p -> next = NULL;
		if (p -> name && p -> path && p -> so) {
			p -> setup = (void *(*) (plugin_t *)) dlsym (p -> so, FUNC_SETUP);
			p -> teardown = (void (*) (plugin_t *, void *)) dlsym (p -> so, FUNC_TEARDOWN);
			if (p -> setup)
				p -> priv = (*p -> setup) (p);
		} else
			p = plugin_free (p);
	}
	return p;
}/*}}}*/

invoke_t *
invoke_alloc (plugin_t *p, void *func) /*{{{(*/
{
	invoke_t	*i;
	
	if (i = (invoke_t *) malloc (sizeof (invoke_t))) {
		i -> p = p;
		i -> func = func;
		i -> rc = P_OK;
		i -> next = NULL;
	}
	return i;
}/*}}}*/
invoke_t *
invoke_free (invoke_t *i) /*{{{*/
{
	if (i) {
		free (i);
	}
	return NULL;
}/*}}}*/
invoke_t *
invoke_free_all (invoke_t *i) /*{{{*/
{
	invoke_t	*tmp;
	
	while (tmp = i) {
		i = i -> next;
		invoke_free (tmp);
	}
	return NULL;
}/*}}}*/
plugin_t *
plugins_setup (void (*error_callback) (const char *, const char *, const char *), const char *path, ...) /*{{{*/
{
	va_list		par;
	int		plen;
	plugin_t	*root, *prev, *cur;
	DIR		*dp;
	struct dirent	*ent;
	const char	*ptr;
	char		*scratch;
	char		*sptr;
	struct stat	st;
	
	va_start (par, path);
	root = NULL;
	prev = NULL;
	while (path) {
		plen = strlen (path);
		if (scratch = malloc (plen + NAME_MAX + 2)) {
			strcpy (scratch, path);
			sptr = scratch + plen;
			*sptr++ = '/';
			if (dp = opendir (path)) {
				while (ent = readdir (dp)) {
					if ((ent -> d_name[0] != '.') && (strlen (ent -> d_name) <= NAME_MAX)) {
						if ((ptr = strrchr (ent -> d_name, '.')) && (! strcmp (ptr, POSTFIX_SHARED_OBJECT))) {
							strcpy (sptr, ent -> d_name);
							if ((stat (scratch, & st) != -1) && S_ISREG (st.st_mode)) {
								cur = plugin_alloc (ent -> d_name, scratch);
								if (cur) {
									if (prev)
										prev -> next = cur;
									else
										root = cur;
									prev = cur;
								} else if (error_callback)
									(*error_callback) (ent -> d_name, scratch, dlerror ());
							}
						}
					}
				}
				closedir (dp);
			}
			free (scratch);
		}
		path = va_arg (par, const char *);
	}
	va_end (par);
	return root;
}/*}}}*/
plugin_t *
plugins_teardown (plugin_t *p) /*{{{*/
{
	plugin_t	*tmp;
	
	while (tmp = p) {
		p = p -> next;
		plugin_free (tmp);
	}
	return NULL;
}/*}}}*/
invoke_t *
plugins_resolve (plugin_t *p, const char *name) /*{{{*/
{
	invoke_t	*root, *prev, *cur;
	void		*func;
	
	root = NULL;
	prev = NULL;
	for (; p; p = p -> next)
		if ((func = dlsym (p -> so, name)) && (cur = invoke_alloc (p, func))) {
			if (prev)
				prev -> next = cur;
			else
				root = cur;
			prev = cur;
		}
	return root;
}/*}}}*/
prc_t
plugins_invoke (invoke_t *i, prc_t (*callback) (void *, void *)) /*{{{*/
{
	prc_t	rc = P_OK;
	
	for (; i; i = i -> next) {
		if (i -> rc != P_ERR_ABORT) {
			i -> rc = (*callback) (i -> p -> priv, i -> func);
		}
		if (i -> rc > rc)
			rc = i -> rc;
	}
	return rc;
}/*}}}*/
