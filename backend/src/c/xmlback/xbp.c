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
# include	"xmlback.h"

static void
xbp_dyn_resolve (xbp_t *xbp)
{
	xbp -> prepare = plugins_resolve (xbp -> plugins, "p_prepare");
	xbp -> cleanup = plugins_resolve (xbp -> plugins, "p_cleanup");
	xbp -> section_start = plugins_resolve (xbp -> plugins, "p_section_start");
	xbp -> section_end = plugins_resolve (xbp -> plugins, "p_section_end");
	xbp -> create_output_start = plugins_resolve (xbp -> plugins, "p_create_output_start");
	xbp -> create_output_mail = plugins_resolve (xbp -> plugins, "p_create_output_mail");
	xbp -> create_output_end = plugins_resolve (xbp -> plugins, "p_create_output_end");
	xbp -> tag_replace = plugins_resolve (xbp -> plugins, "p_tag_replace");
}
static void
xbp_dyn_initialize (xbp_t *xbp)
{
	xbp -> prepare = NULL;
	xbp -> cleanup = NULL;
	xbp -> section_start = NULL;
	xbp -> section_end = NULL;
	xbp -> create_output_start = NULL;
	xbp -> create_output_mail = NULL;
	xbp -> create_output_end = NULL;
	xbp -> tag_replace = NULL;
}
static void
xbp_dyn_release (xbp_t *xbp)
{
	invoke_free_all (xbp -> prepare);
	invoke_free_all (xbp -> cleanup);
	invoke_free_all (xbp -> section_start);
	invoke_free_all (xbp -> section_end);
	invoke_free_all (xbp -> create_output_start);
	invoke_free_all (xbp -> create_output_mail);
	invoke_free_all (xbp -> create_output_end);
	invoke_free_all (xbp -> tag_replace);
}
prc_t
xbp_prepare (xbp_t *xbp, blockmail_t *blockmail)
{
	prc_t	rc;

	if (xbp) {
		plugin_call (rc, xbp -> prepare, (void *, blockmail_t *), (priv, blockmail));
		if ((rc != P_OK) && xbp -> lg)
			log_out (xbp -> lg, LV_ERROR, "xbp[prepare]: failed %s", prc_as_string (rc));
	} else
		rc = P_OK;
	return rc;
}

prc_t
xbp_cleanup (xbp_t *xbp, blockmail_t *blockmail)
{
	prc_t	rc;

	if (xbp) {
		plugin_call (rc, xbp -> cleanup, (void *, blockmail_t *), (priv, blockmail));
		if ((rc != P_OK) && xbp -> lg)
			log_out (xbp -> lg, LV_ERROR, "xbp[cleanup]: failed %s", prc_as_string (rc));
	} else
		rc = P_OK;
	return rc;
}

prc_t
xbp_section_start (xbp_t *xbp, blockmail_t *blockmail, const xmlChar *name)
{
	prc_t	rc;

	if (xbp) {
		plugin_call (rc, xbp -> section_start, (void *, blockmail_t *, const xmlChar *), (priv, blockmail, name));
		if ((rc != P_OK) && xbp -> lg)
			log_out (xbp -> lg, LV_ERROR, "xbp[section_start]: failed %s", prc_as_string (rc));
	} else
		rc = P_OK;
	return rc;
}

prc_t
xbp_section_end (xbp_t *xbp, blockmail_t *blockmail, const xmlChar *name, bool_t status)
{
	prc_t	rc;

	if (xbp) {
		plugin_call (rc, xbp -> section_end, (void *, blockmail_t *, const xmlChar *, bool_t ), (priv, blockmail, name, status));
		if ((rc != P_OK) && xbp -> lg)
			log_out (xbp -> lg, LV_ERROR, "xbp[section_end]: failed %s", prc_as_string (rc));
	} else
		rc = P_OK;
	return rc;
}

prc_t
xbp_create_output_start (xbp_t *xbp, blockmail_t *blockmail, receiver_t *rec)
{
	prc_t	rc;

	if (xbp) {
		plugin_call (rc, xbp -> create_output_start, (void *, blockmail_t *, receiver_t *), (priv, blockmail, rec));
		if ((rc != P_OK) && xbp -> lg)
			log_out (xbp -> lg, LV_ERROR, "xbp[create_output_start]: failed %s", prc_as_string (rc));
	} else
		rc = P_OK;
	return rc;
}

prc_t
xbp_create_output_mail (xbp_t *xbp, blockmail_t *blockmail, receiver_t *rec, const char *stage, block_t *block)
{
	prc_t	rc;

	if (xbp) {
		plugin_call (rc, xbp -> create_output_mail, (void *, blockmail_t *, receiver_t *, const char *, block_t *), (priv, blockmail, rec, stage, block));
		if ((rc != P_OK) && xbp -> lg)
			log_out (xbp -> lg, LV_ERROR, "xbp[create_output_mail]: failed %s", prc_as_string (rc));
	} else
		rc = P_OK;
	return rc;
}

prc_t
xbp_create_output_end (xbp_t *xbp, blockmail_t *blockmail, receiver_t *rec, bool_t status)
{
	prc_t	rc;

	if (xbp) {
		plugin_call (rc, xbp -> create_output_end, (void *, blockmail_t *, receiver_t *, bool_t ), (priv, blockmail, rec, status));
		if ((rc != P_OK) && xbp -> lg)
			log_out (xbp -> lg, LV_ERROR, "xbp[create_output_end]: failed %s", prc_as_string (rc));
	} else
		rc = P_OK;
	return rc;
}

prc_t
xbp_tag_replace (xbp_t *xbp, blockmail_t *blockmail, receiver_t *rec, tag_t *tag, const xmlChar *cont, int clen)
{
	prc_t	rc;

	if (xbp) {
		plugin_call (rc, xbp -> tag_replace, (void *, blockmail_t *, receiver_t *, tag_t *, const xmlChar *, int ), (priv, blockmail, rec, tag, cont, clen));
		if ((rc != P_OK) && xbp -> lg)
			log_out (xbp -> lg, LV_ERROR, "xbp[tag_replace]: failed %s", prc_as_string (rc));
	} else
		rc = P_OK;
	return rc;
}


	
xbp_t *
xbp_create (log_t *lg) /*{{{*/
{
	const char	*home;
	char		*plugin_path1, *plugin_path2;
	xbp_t		*xbp;

	xbp = NULL;
	if (home = path_home ()) {
		if (plugin_path1 = mkpath (home, "scripts", "plugins", "xmlback", NULL)) {
			if (plugin_path2 = mkpath (home, "plugins", "xmlback", NULL)) {
				if (xbp = xbp_alloc (lg)) {
					bool_t	failure = false;
					auto void cb_error (const char *, const char *, const char *);
					void cb_error (const char *name, const char *path, const char *error) {
						failure = true;
						if (lg)
							log_out (lg, LV_ERROR, "xbp[%s]: failed to load %s: %s\n", name, path, error);
					}
					xbp -> plugins = plugins_setup (cb_error, plugin_path1, plugin_path2, NULL);
					if (failure)
						exit (1);
					if (xbp -> plugins) {
						xbp_dyn_resolve (xbp);
					} else
						xbp = xbp_free (xbp);
				}
				free (plugin_path2);
			}
			free (plugin_path1);
		}
	}
	return xbp;
}/*}}}*/
xbp_t *
xbp_alloc (log_t *lg) /*{{{*/
{
	xbp_t	*xbp;
	
	if (xbp = (xbp_t *) malloc (sizeof (xbp_t))) {
		xbp -> lg = lg;
		xbp -> plugins = NULL;
		xbp_dyn_initialize (xbp);
	}
	return xbp;
}/*}}}*/
xbp_t *
xbp_free (xbp_t *xbp) /*{{{*/
{
	if (xbp) {
		xbp_dyn_release (xbp);
		plugins_teardown (xbp -> plugins);
		free (xbp);
	}
	return NULL;
}/*}}}*/
