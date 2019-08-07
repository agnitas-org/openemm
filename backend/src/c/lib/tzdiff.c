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
/** @file tzdiff.c
 * Timezone difference calculator.
 */
# include	<time.h>
# include	"agn.h"

/** Timezone diff to gm time.
 * Calculates the difference between localtime and gmrime in seconds
 * @param tim current time
 * @return the difference in seconds
 */
int
tzdiff (time_t tim) /*{{{*/
{
	int		diff;
	time_t		gm, loc;
	struct tm	*tp;
	struct tm	tt;

	diff = 0;
	if (tp = gmtime (& tim)) {
		tt = *tp;
		tt.tm_isdst = 0;
		if ((gm = mktime (& tt)) != (time_t) -1) {
			if (tp = localtime (& tim)) {
				tt = *tp;
				tt.tm_isdst = 0;
				if ((loc = mktime (& tt)) != (time_t) -1)
					diff = loc - gm;
			}
		}
	}
	return diff;
}/*}}}*/
		
