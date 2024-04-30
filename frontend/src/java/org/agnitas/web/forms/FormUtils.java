/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web.forms;

import org.agnitas.beans.RowsCountAndSortingWebStorageEntry;
import org.agnitas.beans.RowsCountWebStorageEntry;
import org.agnitas.service.WebStorage;
import org.agnitas.service.WebStorageBundle;
import org.apache.commons.lang3.StringUtils;
import org.displaytag.pagination.PaginatedList;

public class FormUtils {
    public static <T extends RowsCountWebStorageEntry> void syncNumberOfRows(WebStorage webStorage, WebStorageBundle<T> bundle, StrutsFormBase form) {
        webStorage.access(bundle, entry -> {
            if (form.getNumberOfRows() > 0) {
                entry.setRowsCount(form.getNumberOfRows());
            } else {
                form.setNumberOfRows(entry.getRowsCount());
            }
        });
    }

    public static <T extends RowsCountWebStorageEntry> void syncNumberOfRows(WebStorage webStorage, WebStorageBundle<T> bundle, PaginationForm form) {
        webStorage.access(bundle, entry -> {
            if (form.getNumberOfRows() > 0) {
                entry.setRowsCount(form.getNumberOfRows());
            } else {
                form.setNumberOfRows(entry.getRowsCount());
            }
        });
    }

    public static <T extends RowsCountAndSortingWebStorageEntry> void syncSortingParams(WebStorage webStorage, WebStorageBundle<T> bundle, PaginationForm form) {
        webStorage.access(bundle, entry -> {
            if (!StringUtils.isBlank(form.getSort())) {
                entry.setColumnName(form.getSort());
                entry.setAscendingOrder("asc".equalsIgnoreCase(form.getDir()) || "ascending".equalsIgnoreCase(form.getDir()));
            } else {
                form.setSort(entry.getColumnName());
                form.setDir(entry.isAscendingOrder() ? "asc" : "desc");
            }
        });
    }

    public static void setPaginationParameters(PaginationForm form, PaginatedList paginatedList) {
        form.setSort(paginatedList.getSortCriterion());
        form.setDir(paginatedList.getSortDirection().getName());
        form.setPage(paginatedList.getPageNumber());
        form.setNumberOfRows(paginatedList.getObjectsPerPage());
    }

    public static <E> void syncSearchParams(FormSearchParams<E> searchParams, E form, boolean restore) {
        if (restore) {
            searchParams.restoreParams(form);
        } else {
            searchParams.storeParams(form);
        }
    }

    public static <E> void resetSearchParams(FormSearchParams<E> searchParams, E form) {
        searchParams.resetParams();
        searchParams.restoreParams(form);
    }
}
