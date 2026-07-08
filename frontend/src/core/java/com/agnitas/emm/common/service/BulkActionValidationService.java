/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.common.service;

import com.agnitas.service.ServiceResult;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * @param <ID> the type of the identifier for the entities to be deleted
 * @param <R>  the type of result returned after validation
 */
public interface BulkActionValidationService<ID, R> {

    /**
     * Validates which entities identified by the given IDs are allowed for deletion.
     *
     * @param ids           the collection of IDs to check
     * @param validationFunction a function that checks if an entity identified by a single ID can be deleted
     * @return a ServiceResult containing a list of entities allowed for deletion and message indicating the result of the validation
     */
    ServiceResult<List<R>> checkAllowedForDeletion(Collection<ID> ids, Function<ID, ServiceResult<R>> validationFunction);
    ServiceResult<List<R>> checkAllowedForActivation(Collection<ID> ids, Function<ID, ServiceResult<R>> validationFunction);
    ServiceResult<List<R>> checkAllowedForDeactivation(Collection<ID> ids, Function<ID, ServiceResult<R>> validationFunction);

}
