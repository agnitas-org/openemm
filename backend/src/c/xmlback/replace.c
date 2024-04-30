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
# include	<ctype.h>
# include	"xmlback.h"

static bool_t
set_content (blockmail_t *blockmail, receiver_t *rec, record_t *record) /*{{{*/
{
	bool_t	st;
	int	n;
	field_t	*f;

	st = true;
	for (n = 0; n < blockmail -> field_count; ++n) {
		f = blockmail -> field[n];
		record -> dpos = n;
		receiver_set_data (rec, f -> rname ? f -> rname : f -> lname, record);
	}
	record -> dpos = n;
	if (blockmail -> eval) {
		st = eval_set_data (blockmail -> eval, record -> data, record -> isnull, record -> dpos);
		if (! st)
			log_out (blockmail -> lg, LV_ERROR, "Unable to update evaluator data");
	}
	return st;
}/*}}}*/
static void
individual_replace (const xmlChar *(*replace) (const xmlChar *, int, int *),
		    xmlBufferPtr dest, const xmlChar *tval, int tlen) /*{{{*/
{
	int		clen;
	const xmlChar	*rplc;
	int		rlen;
	
	while (tlen > 0) {
		clen = xmlCharLength (*tval);
		if ((rplc = (*replace) (tval, clen, & rlen)) && (rlen > 0))
			xmlBufferAdd (dest, rplc, rlen);
		tval += clen;
		tlen -= clen;
	}
}/*}}}*/
static const dyn_t *
find_dynamic (blockmail_t *blockmail, receiver_t *rec, const char *name) /*{{{*/
{
	dcache_t	*dc;
	const dyn_t	*dyn;
					
	for (dc = rec -> cache; dc; dc = dc -> next)
		if (! strcmp (dc -> name, name))
			break;
	if (! dc)
		for (dyn = blockmail -> dyn; dyn; dyn = dyn -> next)
			if (! strcmp (dyn -> name, name)) {
				if (dc = dcache_alloc (name, dyn)) {
					dc -> next = rec -> cache;
					rec -> cache = dc;
				}
				break;
			}
	return dc ? dc -> dyn : NULL;
}/*}}}*/
static int
tp_compare (const void *ap, const void *bp) /*{{{*/
{
	tagpos_t	*a = *((tagpos_t **) ap);
	tagpos_t	*b = *((tagpos_t **) bp);
	int		 diff = b -> sort_value - a -> sort_value;
	
	if (diff == 0) {
		int	la = xmlBufferLength (a -> name);
		int	lb = xmlBufferLength (b -> name);
		int	len = la > lb ? lb : la;
		
		if (len > 0)
			diff = memcmp (xmlBufferContent (a -> name), xmlBufferContent (b -> name), len);
		if (diff == 0)
			diff = la - lb;
	}
	return diff;
}/*}}}*/
static bool_t
is_empty (xmlBuffer *buffer) /*{{{*/
{
	const xmlChar	*ptr = xmlBufferContent (buffer);
	int		length = xmlBufferLength (buffer);
	int		clen;
	
	while (length > 0) {
		clen = xmlCharLength (*ptr);
		if ((clen != 1) || (! isspace (*ptr)))
			break;
		++ptr;
		--length;
	}
	return length == 0;
}/*}}}*/
	
bool_t
replace_tags (blockmail_t *blockmail, receiver_t *rec, block_t *block, 
	      int level, bool_t code_urls,
	      const xmlChar *(*replace) (const xmlChar *, int, int *),
	      const char *selector,
	      bool_t ishtml, bool_t ispdf) /*{{{*/
{
	bool_t		st;
	record_t	*record;
	xmlBufferPtr	source;
	tagpos_t	**tagpos;
	int		tagpos_count;
	int		sorted_size;
	tagpos_t	**sorted;
	const dyn_t	*dyn, *root;
	int		dyncount, dynused;
	long		start, cur, next, end, len;
	const xmlChar	*content, *cont;
	int		tidx, sidx;
	tagpos_t	*tp, *sp;
	int		n;
	tag_t		*tag;
	protect_t	*proot, *pprev, *ptmp;
	bool_t		clear_output;

	st = true;
	record = rec -> rvdata -> cur;
	if (! set_content (blockmail, rec, record))
		st = false;
	source = block -> content;
	tagpos = block -> tagpos;
	tagpos_count = block -> tagpos_count;
	if ((tagpos_count > 0) && (sorted = (tagpos_t **) malloc (sizeof (tagpos_t *) * tagpos_count))) {
		for (n = 0, sorted_size = 0; n < tagpos_count; ++n) {
			if ((tagpos[n] -> type & TP_DYNAMIC) && tagpos[n] -> tname &&
			    (dyn = find_dynamic (blockmail, rec, tagpos[n] -> tname)) &&
			    dyn -> interest && (dyn -> interest_index != -1)) {
				if (record -> isnull[dyn -> interest_index])
					tagpos[n] -> sort_value = 0;
				else
					tagpos[n] -> sort_value = xml2long (record -> data[dyn -> interest_index]);
				sorted[sorted_size++] = tagpos[n];
				tagpos[n] -> sort_enable = true;
			} else
				tagpos[n] -> sort_enable = false;
		}
		if (sorted_size > 1)
			qsort (sorted, sorted_size, sizeof (sorted[0]), tp_compare);
	} else {
		sorted_size = 0;
		sorted = NULL;
	}
	dyncount = 0;
	dynused = 0;
	start = 0;
	proot = NULL;
	pprev = NULL;
	clear_output = false;
	end = xmlBufferLength (source);
	content = xmlBufferContent (source);
	xmlBufferEmpty (block -> in);
	for (cur = start, tidx = 0, sidx = 0; cur < end; ) {
		if (tidx < tagpos_count) {
			tp = tagpos[tidx++];
			next = tp -> start;
		} else {
			tp = NULL;
			sp = NULL;
			next = end;
		}
		len = next - cur;
		if (len > 0)
			xmlBufferAdd (block -> in, content + cur, len);
		if (tp) {
			cur = tp -> end;
			tag = NULL;
			if (IS_DYNAMIC (tp -> type)) {
				++dyncount;
				if (tp -> sort_enable) {
					if (sidx < sorted_size)
						sp = sorted[sidx++];
					else
						sp = NULL;
				} else
					sp = tp;
				if (sp && IS_DYNAMIC (sp -> type) && sp -> tname) {
					root = find_dynamic (blockmail, rec, sp -> tname);
					for (dyn = root; dyn; dyn = dyn -> sibling)
						if (dyn_match (dyn, blockmail -> eval, rec))
							break;
					if (dyn && dyn_match_selector (root, selector)) {
						block_t	*use;
						int	*indexes;

						use = NULL;
						if (sp -> type & TP_DYNAMICVALUE) {
							for (n = 0; (! use) && (n < dyn -> block_count); ++n)
								switch (dyn -> block[n] -> nr) {
								case 0:
									if (! ishtml)
										use = dyn -> block[n];
									break;
								case 1:
									if (ishtml)
										use = dyn -> block[n];
									break;
								}
						} else if (sp -> type & TP_DYNAMIC)
							use = sp -> content;
						if (use && (! use -> inuse)) {
							bool_t	save_disable_link_extension;
							int	save_icount, save_ipos;
							int	icount;
							bool_t	dynamic;
							int	*idx;
							
							save_disable_link_extension = rec -> disable_link_extension;
							rec -> disable_link_extension = root -> disable_link_extension;
							++dynused;
							use -> inuse = true;
							save_icount = rec -> rvdata ->icount;
							save_ipos = rec -> rvdata -> ipos;
							if (indexes = dataset_get_indexes (rec -> rvdata, sp, rec, & icount, & dynamic)) {
								for (idx = indexes; st && (*idx != -1); ++idx) {
									if (sp -> type != TP_DYNAMICVALUE) {
										dataset_select_record (rec -> rvdata, *idx);
										if (icount != -1) {
											rec -> rvdata -> icount = icount;
											rec -> rvdata -> ipos = 0;
											icount = -1;
										}
										rec -> rvdata -> ipos++;
									}
									if (replace_tags (blockmail, rec, use, level + 1, code_urls, NULL, NULL, ishtml, ispdf)) {
										if (root -> disable_link_extension) {
											if (ptmp = protect_alloc ()) {
												if (pprev)
													pprev -> next = ptmp;
												else
													proot = ptmp;
												pprev = ptmp;
												ptmp -> start = xmlBufferLength (block -> in);
											} else
												st = false;
										} else
											ptmp = NULL;
										if ((sp -> type == TP_DYNAMICVALUE) && is_empty (use -> in)) {
											/*
											 * agnDVALUE is empty, so clear out the whole [agnDYN] ... [/agnDYN] block
											 */
											clear_output = true;
										}
										if (replace)
											individual_replace (replace, block -> in, xmlBufferContent (use -> in), xmlBufferLength (use -> in));
										else
											xmlBufferAdd (block -> in, xmlBufferContent (use -> in), xmlBufferLength (use -> in));
										if (ptmp)
											ptmp -> end = xmlBufferLength (block -> in);
									} else
										st = false;
								}
								if (dynamic)
									free (indexes);
							}
							rec -> rvdata -> icount = save_icount;
							rec -> rvdata -> ipos = save_ipos;
							use -> inuse = false;
							if (rec -> rvdata -> cur != record) {
								rec -> rvdata -> cur = record;
								if (! set_content (blockmail, rec, record))
									st = false;
							}
							rec -> disable_link_extension = save_disable_link_extension;
						}
					}
				} else
					st = false;
			} else {
				for (n = 0; (n < 2) && (! tag); ++n) {
					for (tag = n ? blockmail -> gtag : record -> tag; tag; tag = tag -> next)
						if (((tag -> hash == 0) || (tp -> hash == 0) || (tag -> hash == tp -> hash)) &&
						    (xmlEqual (tag -> name, tp -> name)))
							break;
				}
				if (tag && (! tag_filter (tag, rec, 10, NULL)))
					tag = NULL;
			}
			if (tag && (cont = tag_content (tag, blockmail, rec, & n)) && (n > 0)) {
				if (ispdf) {
					entity_escape (block -> in, cont, n);
				} else if (replace) {
					individual_replace (replace, block -> in, cont, n);
				} else {
					xmlBufferAdd (block -> in, cont, n);
				}
			}
		} else
			cur = next;
	}
	if (st && code_urls && ((! blockmail -> enhanced_url_resolver) || (level == 0)))
		st = modify_urls (blockmail, rec, block, proot, ishtml, record);
	protect_free_all (proot);
	if (blockmail -> clear_empty_dyn_block && clear_output) {
		xmlBufferEmpty (block -> in);
		dynused = 0;
	}
	if (sorted)
		free (sorted);
	if ((level == 0) && (dyncount > 0) && (dynused == 0)) {
		/* have hit one empty text block */
		if (rec -> media && rec -> media -> empty)
			if ((block -> tid != TID_EMail_HTML_Preheader) && (block -> tid != TID_EMail_HTML_Clearance)) {
				blockmail -> active = false;
				blockmail -> reason = REASON_EMPTY_DOCUMENT;
			}
	}
	return st;
}/*}}}*/
