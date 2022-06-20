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
# ifndef	__BAV_H
# define	__BAV_H		1
# include	"agn.h"

# define	ID_ACCEPT	"accept"
# define	ID_TEMPFAIL	"tempfail"
# define	ID_REJECT	"reject"
# define	ID_RELAY	ID_ACCEPT ":rid=relay"

typedef struct { /*{{{*/
	map_t	*amap;
	set_t	*hosts;
	/*}}}*/
}	cfg_t;

extern cfg_t	*cfg_alloc (const char *fname);
extern cfg_t	*cfg_free (cfg_t *c);
extern char	*cfg_valid_address (cfg_t *c, const char *addr);
# endif		/* __BAV_H */
