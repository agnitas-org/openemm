/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.startuplistener.service;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public final class ExecutionResultCollector {
	
	private static final class InterrimResult {
		public int successCount;
		public int failureCount;
		
		public InterrimResult() {
			this(0, 0);
		}
		
		public InterrimResult(final int succ, final int fail) {
			this.successCount = succ;
			this.failureCount = fail;
		}
	}

	public static final Collector<ExecutionResult, InterrimResult, ExecutionResult> sumUpResults() {
		final Supplier<InterrimResult> supplier = () -> new InterrimResult();
		final BiConsumer<InterrimResult, ExecutionResult> accumulator = (x, y) -> { 
			x.successCount += y.getSuccessCount(); 
			x.failureCount += y.getFailureCount(); 
			};
		final BinaryOperator<InterrimResult> combiner = (x, y) -> new InterrimResult(x.successCount + y.successCount, x.failureCount + y.failureCount);
		final Function<InterrimResult, ExecutionResult> finisher = x -> new ExecutionResult(x.successCount, x.failureCount);
		
		return Collector.of(
				supplier, 
				accumulator, 
				combiner, 
				finisher,
				Collector.Characteristics.UNORDERED);
	}
}
