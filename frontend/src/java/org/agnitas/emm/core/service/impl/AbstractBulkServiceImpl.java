/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public abstract class AbstractBulkServiceImpl<T> {

	@Resource
	private PlatformTransactionManager transactionManager;
	protected PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}
	
	protected abstract class BulkOperation {
		protected abstract Object run(T model) throws Exception;
	}
	
	protected abstract class BulkOperationWithoutResult extends BulkOperation {
		@Override
		public final Object run(T model) throws Exception {
			runWithoutResult(model);
			return null;
		}
	
		protected abstract void runWithoutResult(T model) throws Exception;
	}

	protected <D> D transaction(TransactionCallback<D> callback) {
		return new TransactionTemplate(transactionManager)
				.execute(callback);
	}

	protected List<Object> processBulk(final List<T> models, final boolean ignoreErrors, final BulkOperation operation) {
		return processBulkWithLock(models, ignoreErrors, operation, null);
	}
	
	protected List<Object> processBulkWithLock(final List<T> models, final boolean ignoreErrors, final BulkOperation operation, final Runnable lockOp) {
		if (ignoreErrors) {
			return processList(models, ignoreErrors, operation);
		} else {
			return transaction(status -> {
				if (lockOp != null) {
					lockOp.run();
				}
				List<Object> result = processList(models, ignoreErrors, operation);
				if (!result.isEmpty()) {
					for (Object r : result) {
						if (r instanceof Exception) {
							status.setRollbackOnly();
							break;
						}
					}
				}
				return result;
			});
		}
	}
	
	private List<Object> processList(List<T> models, boolean ignoreErrors, BulkOperation operation) {
		List<Object> result = new ArrayList<>(models.size());
		for (T model : models) {
			try {
				result.add(operation.run(model));
			} catch (Exception e) {
				result.add(e);
				if (!ignoreErrors) {
					break;
				}
			}
		}
		return result;
	}
}
