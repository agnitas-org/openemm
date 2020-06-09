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
# ifndef	__LIB_PLUGIN_H
# define	__LIB_PLUGIN_H		1

/*
 * Macro to simplify calling a method an unknown number of plugins
 * 
 * The macro requires four parameter
 * 1.) the name of the variable of type ``prc_t'' which will receive the commulated return code
 * 2.) the invoker chain, a return value from ``plugins_resolve''
 * 3.) the declaration for the callback function, first element MUST be ``void *''
 * 4.) the parameter list for the function, first element MUST be the literal string ``priv''
 * 
 * Sample (without error checking):
 * plugin_t	*p = plugins_setup (NULL, "/path/to/plugin/directory", NULL);
 * invoke_t	*i = plugins_resolve (p, "function");
 * prc_t	rc;
 * 
 * plugin_call (rc, i, (void *), (priv))
 * invoke_free_all (i);
 * plugins_teardown (p);
 * 
 * Will call the function "function" from each plugin found in the plugin directory if the plugin
 * provides this function. To call a function with other parameter, assume a function "max" is
 * resolved using plugins_resolve:
 * 
 * int	result;
 * plugin_call (rc, i, (void *, int, int, int *), (priv, 5, 10, & result));
 * 
 * Note: each plguin can only return its status, so a real result must be fetched by parameter
 * or a callback function
 */
# define	plugin_call(result, invoker, declaration, parameter)	do {	\
	auto prc_t	__callback (void *, void *);				\
	prc_t __callback (void *priv, void *func) {				\
		return ((prc_t (*) declaration) func) parameter;		\
	}									\
	result = plugins_invoke (invoker, __callback);				\
}	while (0)

typedef struct plugin	plugin_t;
struct plugin { /*{{{*/
	char		*name;	/* name for this plugin			*/
	char		*path;	/* path for the file containing plugin	*/
	void		*so;	/* handler for plugin			*/
	void		*priv;	/* private data of this plugin		*/
	void		*(*setup) (plugin_t *);
	void		(*teardown) (plugin_t *, void *);
	plugin_t	*next;
	/*}}}*/
};
typedef enum { /*{{{*/
	P_OK = 0,
	P_ERR_CONTINUE = 1,
	P_ERR_ABORT = 2
	/*}}}*/
}	prc_t;
typedef struct invoke	invoke_t;
struct invoke { /*{{{*/
	plugin_t	*p;
	void		*func;
	prc_t		rc;
	invoke_t	*next;
	/*}}}*/
};

extern plugin_t	*plugin_free (plugin_t *p);
extern plugin_t	*plugin_alloc (const char *name, const char *path);
extern invoke_t	*invoke_alloc (plugin_t *p, void *func);
extern invoke_t	*invoke_free (invoke_t *i);
extern invoke_t	*invoke_free_all (invoke_t *i);
extern plugin_t	*plugins_setup (void (*error_callback) (const char *, const char *, const char *), const char *path, ...);
extern plugin_t	*plugins_teardown (plugin_t *p);
extern invoke_t	*plugins_resolve (plugin_t *p, const char *name);
extern prc_t	plugins_invoke (invoke_t *i, prc_t (*callback) (void *, void *));

static inline const char *
prc_as_string (prc_t rc) /*{{{*/
{
	static char	msg[64];
	
	switch (rc) {
	case P_OK:		return "ok";
	case P_ERR_CONTINUE:	return "recoverable error";
	case P_ERR_ABORT:	return "permanent error";
	}
	snprintf (msg, sizeof (msg) - 1, "unknown code %d", rc);
	return msg;
}/*}}}*/

/* prototypes for plugin implemented functions				*/
extern void	*p_setup (plugin_t *);
extern void	p_teardown (plugin_t *, void *);
# endif		/* __LIB_PLUGIN_H */
