/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.binding.service.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.agnitas.emm.core.binding.service.BindingBulkService;
import org.agnitas.emm.core.binding.service.BindingModel;
import org.agnitas.emm.core.binding.service.BindingService;
import org.agnitas.emm.core.service.impl.AbstractBulkServiceImpl;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.ComBindingEntryDao;

public class BindingBulkServiceImpl extends AbstractBulkServiceImpl<BindingModel> implements BindingBulkService {

	private BindingService bindingService;
	
	private ComBindingEntryDao bindingEntryDao;
	
	@Required
	public void setBindingService(BindingService bindingService) {
		this.bindingService = bindingService;
	}

	@Required
	public void setBindingEntryDao(ComBindingEntryDao bindingEntryDao) {
		this.bindingEntryDao = bindingEntryDao;
	}

	@Override
	public List<Object> setBinding(final List<BindingModel> models, boolean ignoreErrors) {
		if (models.size() < 1) {
			return Collections.<Object>emptyList();
		}
		return processBulkWithLock(models, ignoreErrors, new BulkOperationWithoutResult() {
			@Override
			protected void runWithoutResult(BindingModel model) throws Exception {
				bindingService.setBinding(model);
			}
		}, () -> bindingEntryDao.lockBindings(getFirstCompanyId(models), getIdPairs(models)));
	}

	@Override
	public List<Object> deleteBinding(List<BindingModel> models, boolean ignoreErrors) {
		if (models.size() < 1) {
			return Collections.<Object>emptyList();
		}
		return processBulkWithLock(models, ignoreErrors, new BulkOperationWithoutResult() {
			@Override
			protected void runWithoutResult(BindingModel model) {
				bindingService.deleteBinding(model);
			}
		}, new Runnable() {
			@Override
			public void run() {
				bindingEntryDao.lockBindings(getFirstCompanyId(models), getIdPairs(models));
			}
		});
	}

	@Override
	public List<Object> getBindings(List<BindingModel> models) {
		return processBulk(models, true, new BulkOperation() {
			@Override
			protected Object run(BindingModel model) {
				return bindingService.getBindings(model);
			}
		});
	}

	private List<SimpleEntry<Integer, Integer>> getIdPairs(List<BindingModel> models) {
		List<SimpleEntry<Integer, Integer>> pairs = new ArrayList<>();
		for (BindingModel model : models) {
			pairs.add(new SimpleEntry<>(model.getCustomerId(), model.getMailinglistId()));
		}
		return pairs;
	}
	
	private int getFirstCompanyId(List<BindingModel> models) {
		if (models.size() < 1) {
			throw new IllegalArgumentException();
		}
		return models.get(0).getCompanyId();
	}
}
