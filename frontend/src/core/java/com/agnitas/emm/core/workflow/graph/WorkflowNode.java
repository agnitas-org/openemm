/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.graph;

import java.util.ArrayList;
import java.util.List;

import com.agnitas.emm.core.workflow.beans.WorkflowIcon;

public class WorkflowNode {

	private List<WorkflowNode> prevNodes;
	private List<WorkflowNode> nextNodes;

	private WorkflowIcon nodeIcon;

	public WorkflowNode(WorkflowIcon nodeIcon) {
		this.nodeIcon = nodeIcon;
		prevNodes = new ArrayList<>();
		nextNodes = new ArrayList<>();
	}

	public void addNextNode(WorkflowNode node) {
		nextNodes.add(node);
	}

	public void addPrevNode(WorkflowNode node) {
		prevNodes.add(node);
	}

	public void deleteNextNode(WorkflowNode node) {
		nextNodes.remove(node);
	}

	public void deletePrevNode(WorkflowNode node) {
		prevNodes.remove(node);
	}

	public List<WorkflowNode> getPrevNodes() {
		return prevNodes;
	}

	public List<WorkflowNode> getNextNodes() {
		return nextNodes;
	}

	public WorkflowIcon getNodeIcon() {
		return nodeIcon;
	}

	public void setNodeIcon(WorkflowIcon nodeIcon) {
		this.nodeIcon = nodeIcon;
	}
}
