/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.forms;

import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.RowsCountWebStorageEntry;
import com.agnitas.beans.SortingWebStorageEntry;
import com.agnitas.service.WebStorage;
import com.agnitas.service.WebStorageBundle;
import com.agnitas.util.AgnUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.ServletRequestDataBinder;

public class FormUtils {

    private FormUtils() {
        // utility class
    }

    public static <T extends RowsCountWebStorageEntry> void syncNumberOfRows(WebStorage webStorage, WebStorageBundle<T> bundle, PaginationForm form) {
        webStorage.access(bundle, entry -> syncNumberOfRows(entry, form));
    }

    public static <T extends SortingWebStorageEntry> void updateSortingState(WebStorage webStorage, WebStorageBundle<T> bundle, PaginationForm form, Boolean restore) {
        webStorage.access(bundle, entry -> updateSortingState(entry, form, restore));
    }

    public static <T extends SortingWebStorageEntry> void syncPaginationData(WebStorage webStorage, WebStorageBundle<T> bundle, PaginationForm form, Boolean restoreSort) {
        webStorage.access(bundle, entry -> {
            syncNumberOfRows(entry, form);
            updateSortingState(entry, form, restoreSort);
        });
    }

    private static void syncNumberOfRows(RowsCountWebStorageEntry entry, PaginationForm form) {
        if (form.getNumberOfRows() > 0) {
            entry.setRowsCount(form.getNumberOfRows());
        } else {
            form.setNumberOfRows(entry.getRowsCount());
        }
    }

    private static void updateSortingState(SortingWebStorageEntry entry, PaginationForm form, Boolean restore) {
        if (BooleanUtils.isTrue(restore) && StringUtils.isNotBlank(entry.getSortColumn())) {
            form.setSort(entry.getSortColumn());
            form.setDir(entry.isAscendingOrder() ? "asc" : "desc");
        }

        entry.setSortColumn(form.getSort());
        entry.setAscendingOrder(AgnUtils.sortingDirectionToBoolean(form.getDir()));
    }

    public static void setPaginationParameters(PaginationForm form, PaginatedList<?> paginatedList) {
        form.setSort(paginatedList.getSortCriterion());
        form.setDir(paginatedList.getSortDirection().getId());
        form.setPage(paginatedList.getPageNumber());
        form.setNumberOfRows(paginatedList.getPageSize());
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

    public static <T> T bindRequestParameters(HttpServletRequest request, Class<T> targetClazz) {
        try {
            T target = targetClazz.getDeclaredConstructor().newInstance();
            ServletRequestDataBinder servletRequestDataBinder = new ServletRequestDataBinder(target);
            servletRequestDataBinder.bind(request);

            return target;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to bind request parameters to " + targetClazz.getSimpleName(), e);
        }
    }

}
