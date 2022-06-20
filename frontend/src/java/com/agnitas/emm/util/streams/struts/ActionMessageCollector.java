/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.streams.struts;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

public final class ActionMessageCollector implements Collector<ActionMessage, ActionMessages, ActionMessages> {

	private final String key;
	
	public ActionMessageCollector(final String key) {
		this.key = key;
	}
	
	public ActionMessageCollector() {
		this(ActionMessages.GLOBAL_MESSAGE);
	}
	
	@Override
	public final Supplier<ActionMessages> supplier() {
		return () -> new ActionMessages();
	}

	@Override
	public final BiConsumer<ActionMessages, ActionMessage> accumulator() {
		return (messages, msg) -> messages.add(this.key, msg);
	}

	@Override
	public final BinaryOperator<ActionMessages> combiner() {
		return (m0, m1) -> { 
								m1.add(m0); 
								return m1; 
							};
	}

	@Override
	public final Function<ActionMessages, ActionMessages> finisher() {
		return x -> x;
	}

	@Override
	public final Set<Characteristics> characteristics() {
		return Collections.emptySet();
	}

}
