/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.query;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.agnitas.target.TargetNode;

/**
 * This class only exits to keep old serialized data going
 * 
 * @deprecated use org.agnitas.target.impl.TargetRepresentationImpl instead
 */
@Deprecated
public class TargetRepresentation extends org.agnitas.target.impl.TargetRepresentationImpl {
	private static final long serialVersionUID = -5118626285211811379L;

	@SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		ObjectInputStream.GetField allFields = in.readFields();
		allNodes = (List<TargetNode>) allFields.get("allNodes", new ArrayList<TargetNode>());
	}
}
