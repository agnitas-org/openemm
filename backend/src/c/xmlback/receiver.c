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
# include	"xmlback.h"

typedef struct { /*{{{*/
	char		prefix[128];		/* message ID prefix	*/
	int		plen;			/* length of prefix	*/
	int		last_customer_id;	/* last customer id 	*/
	int		nr;			/* inc.number for cust	*/
	/*}}}*/
}	mmid_t;
static void *
mid_maker_alloc (blockmail_t *blockmail) /*{{{*/
{
	mmid_t	*m;
	
	if (m = malloc (sizeof (mmid_t))) {
		time_t		tim;
		struct tm	*tt;
		
		tim = blockmail_now (blockmail);
		if (tt = gmtime (& tim)) {
			m -> plen = snprintf (m -> prefix, sizeof (m -> prefix) - 1, "aa%04d%02d%02d%02d%02d%02d_%d",
					      tt -> tm_year + 1900, tt -> tm_mon + 1, tt -> tm_mday,
					      tt -> tm_hour, tt -> tm_min, tt -> tm_sec,
					      blockmail -> licence_id);
		} else {
			m -> plen = snprintf (m -> prefix, sizeof (m -> prefix) - 1, "aa");
		}
		m -> last_customer_id = -1;
		m -> nr = 0;
	}
	return m;
}/*}}}*/

/* "V" and numeric values are excluded by purpose from this list as this is an indicator for BCC mails */
# define	RANDOM_CHARACTERS	"ABCDEFGHIJKLMNOPQRSTUWXYZabcdefghijklmnopqrstuvwxyz"
# define	pick()			(RANDOM_CHARACTERS[random () % (sizeof (RANDOM_CHARACTERS) - 1)])
static void
mid_maker_randomize (mmid_t *m) /*{{{*/
{
	m -> prefix[0] = pick ();
	m -> prefix[1] = pick ();
}/*}}}*/

static void
mid_maker_free (void *mp) /*{{{*/
{
	mmid_t	*m = (mmid_t *) mp;
	
	if (m) {
		free (m);
	}
}/*}}}*/
	
receiver_t *
receiver_alloc (blockmail_t *blockmail, int data_blocks) /*{{{*/
{
	receiver_t	*r;
	
	if (r = (receiver_t *) malloc (sizeof (receiver_t))) {
		r -> customer_id = -1;
		r -> bcc = NULL;
		r -> user_type = '\0';
		r -> tracking_veto = false;
		r -> disable_link_extension = false;
		r -> media_target = NULL;
		r -> message_id = xmlBufferCreate ();
		r -> mid_maker = NULL;
		r -> mailtype = Mailtype_Text;
		r -> mediatypes = NULL;
		r -> media = NULL;
		r -> mid[0] = '0';
		r -> mid[1] = '\0';
		r -> rvdata = dataset_alloc (data_blocks, blockmail -> target_groups_count);
		r -> encrypt = encrypt_alloc (blockmail);
		r -> cache = NULL;
		r -> base_block = NULL;
		r -> smap = NULL;
		r -> slist = NULL;
		r -> chunks = 1;
		r -> size = 0;
		r -> dkim = false;
		if ((! r -> message_id) || (! r -> rvdata) ||
		    (! r -> encrypt)) {
			r = receiver_free (r);
		} else if (data_blocks > 0) {
			if ((r -> smap = string_map_setup ()) &&
			    (r -> slist = (gnode_t **) malloc (data_blocks * sizeof (gnode_t *)))) {
				int	n;
				
				for (n = 0; n < data_blocks; ++n)
					r -> slist[n] = NULL;
			} else
				r = receiver_free (r);
		}
	}
	return r;
}/*}}}*/
receiver_t *
receiver_free (receiver_t *r) /*{{{*/
{
	if (r) {
		receiver_clear (r);
		if (r -> message_id)
			xmlBufferFree (r -> message_id);
		if (r -> mid_maker)
			mid_maker_free (r -> mid_maker);
		if (r -> mediatypes)
			free (r -> mediatypes);
		if (r -> rvdata)
			dataset_free (r -> rvdata);
		if (r -> encrypt)
			encrypt_free (r -> encrypt);
		if (r -> slist)
			free (r -> slist);
		if (r -> smap)
			string_map_done (r -> smap);
		free (r);
	}
	return NULL;
}/*}}}*/
void
receiver_clear (receiver_t *r) /*{{{*/
{
	if (r -> bcc) {
		if (r -> bcc[0])
			free (r -> bcc[0]);
		free (r -> bcc);
		r -> bcc = NULL;
	}
	r -> tracking_veto = false;
	r -> disable_link_extension = false;
	r -> media_target = media_target_free_all (r -> media_target);
	if (r -> message_id)
		xmlBufferEmpty (r -> message_id);
	r -> mailtype = Mailtype_Text;
	if (r -> mediatypes) {
		free (r -> mediatypes);
		r -> mediatypes = NULL;
	}
	r -> media = NULL;
	r -> mid[0] = '0';
	r -> mid[1] = '\0';
	dataset_clear (r -> rvdata);
	r -> cache = dcache_free_all (r -> cache);
	r -> base_block = NULL;
	r -> chunks = 1;
	r -> size = 0;
	r -> dkim = false;
}/*}}}*/
void
receiver_set_data_l (receiver_t *rec, const char *key, long data) /*{{{*/
{
	string_map_addsi (rec -> smap, key, data);
}/*}}}*/
void
receiver_set_data_i (receiver_t *rec, const char *key, int data) /*{{{*/
{
	receiver_set_data_l (rec, key, (long) data);
}/*}}}*/
void
receiver_set_data_s (receiver_t *rec, const char *key, const char *data) /*{{{*/
{
	string_map_addss (rec -> smap, key, data);
}/*}}}*/
void
receiver_set_data_b (receiver_t *rec, const char *key, xmlBufferPtr data) /*{{{*/
{
	string_map_addsb (rec -> smap, key, data);
}/*}}}*/
void
receiver_set_data_buf (receiver_t *rec, const char *key, const buffer_t *data) /*{{{*/
{
	string_map_addsbuf (rec -> smap, key, data);
}/*}}}*/
void
receiver_set_data_default (receiver_t *rec) /*{{{*/
{
	media_target_t	*mt;
	char		*temp;
	
	receiver_set_data_i (rec, "sys$customer_id", rec -> customer_id);
	receiver_set_data_i (rec, "sys$boundary", rec -> customer_id);
	for (mt = rec -> media_target; mt; mt = mt -> next) {
		if (temp = malloc (strlen (mt -> media) + 5)) {
			sprintf (temp, "sys$%s", mt -> media);
			receiver_set_data_buf (rec, temp, mt -> value);
			free (temp);
		}
	}
}/*}}}*/
void
receiver_set_data (receiver_t *rec, const char *name, record_t *record) /*{{{*/
{
	if (rec -> slist[record -> dpos]) {
		gnode_setdata (rec -> slist[record -> dpos], (const byte_t *) xmlBufferContent (record -> data[record -> dpos]), xmlBufferLength (record -> data[record -> dpos]));
	} else {
		rec -> slist[record -> dpos] = string_map_addsb (rec -> smap, name, record -> data[record -> dpos]);
	}
}/*}}}*/
void
receiver_make_message_id (receiver_t *rec, blockmail_t *blockmail) /*{{{*/
{
	mmid_t	*m = (mmid_t *) rec -> mid_maker;
	
	if (! m) {
		m = mid_maker_alloc (blockmail);
		rec -> mid_maker = m;
	}
	if (m) {
		char	*uid;
		
		mid_maker_randomize (m);
		if (rec -> customer_id == m -> last_customer_id) {
			int	n;
			
			m -> nr++;
			n = snprintf (m -> prefix + m -> plen, sizeof (m -> prefix) - m -> plen - 1, "%d", m -> nr);
			m -> prefix[m -> plen + n] = '\0';
		} else {
			m -> last_customer_id = rec -> customer_id;
			m -> nr = 0;
			m -> prefix[m -> plen] = '\0';
		}
		xmlBufferEmpty (rec -> message_id);
		if ((blockmail -> status_field == 'A') || (blockmail -> status_field == 'T')) {
			char	scratch[64];
			
			xmlBufferCCat (rec -> message_id, m -> prefix);
			xmlBufferAdd (rec -> message_id, (xmlChar *) & blockmail -> status_field, 1);
			snprintf (scratch, sizeof (scratch) - 1, "%d", rec -> customer_id);
			xmlBufferCCat (rec -> message_id, scratch);
		} else if (uid = create_uid (blockmail, m -> prefix, rec, 0)) {
			xmlBufferCCat (rec -> message_id, uid);
			free (uid);
		}
		xmlBufferCCat (rec -> message_id, "@");
		xmlBufferCCat (rec -> message_id, blockmail -> domain ? blockmail -> domain : blockmail -> fqdn);
	}
}/*}}}*/

media_target_t *
media_target_alloc (const char *media, const xmlChar *value) /*{{{*/
{
	media_target_t	*mt;
	
	if (mt = (media_target_t *) malloc (sizeof (media_target_t))) {
		int	vlen = value ? strlen ((const char *) value) : 0;
		
		mt -> media = media ? strdup (media) : NULL;
		if (mt -> value = value ? buffer_alloc (vlen + 1) : NULL) {
			buffer_set (mt -> value, value, vlen);
		}
		mt -> next = NULL;
		if ((media && (! mt -> media)) ||
		    (value && (! mt -> value)))
			mt = media_target_free (mt);
	}
	return mt;
}/*}}}*/
media_target_t *
media_target_free (media_target_t *mt) /*{{{*/
{
	if (mt) {
		if (mt -> media)
			free (mt -> media);
		buffer_free (mt -> value);
		free (mt);
	}
	return NULL;
}/*}}}*/
media_target_t *
media_target_free_all (media_target_t *mt) /*{{{*/
{
	media_target_t	*tmp;
	
	while (tmp = mt) {
		mt = mt -> next;
		media_target_free (tmp);
	}
	return NULL;
}/*}}}*/
buffer_t *
media_target_find (media_target_t *mt, const char *media) /*{{{*/
{
	for (; mt; mt = mt -> next)
		if (! strcmp (mt -> media, media))
			return mt -> value;
	return NULL;
}/*}}}*/
