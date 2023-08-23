/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import com.agnitas.emm.core.workflow.beans.WorkflowConnection;
import com.agnitas.emm.core.workflow.beans.WorkflowIcon;

public class WorkflowGraph {

	private Map<Integer, WorkflowNode> allNodes;

	public WorkflowGraph() {
		allNodes = new HashMap<>();
	}

	public WorkflowGraph(List<WorkflowIcon> icons) {
		build(icons);
	}

	public boolean build(List<WorkflowIcon> icons) {
        allNodes = new HashMap<>();

        for (WorkflowIcon icon : icons) {
            allNodes.put(icon.getId(), new WorkflowNode(icon));
        }

        for (WorkflowIcon icon : icons) {
            List<WorkflowConnection> connections = icon.getConnections();
            if (CollectionUtils.isNotEmpty(connections)) {
                for (WorkflowConnection connection : connections) {
                    WorkflowNode sourceNode = allNodes.get(icon.getId());
                    WorkflowNode targetNode = allNodes.get(connection.getTargetIconId());

                    if (sourceNode == null || targetNode == null) {
                        allNodes.clear();
                        return false;
                    }

                    sourceNode.addNextNode(targetNode);
                    targetNode.addPrevNode(sourceNode);
                }
            }
        }

        return true;
    }

	public List<WorkflowNode> getAllNodesByType(int type) {
		return getAllNodesByTypes(Collections.singletonList(type));
	}

	public List<WorkflowNode> getAllNodesByTypes(Collection<Integer> types) {
		ArrayList<WorkflowNode> result = new ArrayList<>();
		for (WorkflowNode workflowNode : allNodes.values()) {
			if (types.contains(workflowNode.getNodeIcon().getType())) {
				result.add(workflowNode);
			}
		}
		return result;
	}

	/**
	 * Finds the first icon of the specified type that is located before the current icon in workflow
	 *
	 * @param currentIcon icon we start the search from
	 * @param typeToFind type of icon to find
	 * @param terminateTypes types of icons that should stop our search in current branch
	 * @return first found icon with needed type located before the given icon ()
	 */
	public WorkflowIcon getPreviousIconByType(WorkflowIcon currentIcon, int typeToFind, Set<Integer> terminateTypes) {
		return getNextIconByType(currentIcon, typeToFind, terminateTypes, true);
	}

	public WorkflowIcon getNextIconByType(WorkflowIcon currentIcon, int typeToFind, Collection<Integer> terminateTypes, boolean reverse) {
		return getNextIconByType(currentIcon, Collections.singleton(typeToFind), terminateTypes, reverse);
	}

	public WorkflowIcon getNextIconByType(WorkflowIcon currentIcon, Collection<Integer> typesToFind, Collection<Integer> terminateTypes, boolean reverse) {
		WorkflowNode currentNode = allNodes.get(currentIcon.getId());

		List<WorkflowNode> nextNodes = reverse ? currentNode.getPrevNodes() : currentNode.getNextNodes();
		Set<WorkflowNode> visitedNodes = new HashSet<>();

		while (nextNodes.size() > 0) {
			List<WorkflowNode> nextNodesToCheck = new ArrayList<>();

			for (WorkflowNode nextNode : nextNodes) {
                if (visitedNodes.add(nextNode)) {
                    WorkflowIcon icon = nextNode.getNodeIcon();

                    if (typesToFind.contains(icon.getType())) {
                        return icon;
                    }

                    if (!terminateTypes.contains(icon.getType())) {
                        nextNodesToCheck.addAll(reverse ? nextNode.getPrevNodes() : nextNode.getNextNodes());
                    }
                }
			}

			nextNodes = nextNodesToCheck;
		}

		return null;
	}

    public List<WorkflowIcon> getAllPreviousIconsByType(WorkflowIcon currentIcon, int typeToFind, Collection<Integer> terminateTypes) {
        return getAllNextIconsByType(currentIcon, Collections.singletonList(typeToFind), terminateTypes, true);
    }

    public List<WorkflowIcon> getAllPreviousIconsByType(WorkflowIcon currentIcon, Collection<Integer> typesToFind, Collection<Integer> terminateTypes) {
        return getAllNextIconsByType(currentIcon, typesToFind, terminateTypes, true);
    }

    public List<WorkflowIcon> getAllNextIconsByType(WorkflowIcon currentIcon, int typeToFind, Collection<Integer> terminateTypes) {
        return getAllNextIconsByType(currentIcon, Collections.singletonList(typeToFind), terminateTypes, false);
    }

    public List<WorkflowIcon> getAllNextIconsByType(WorkflowIcon currentIcon, Collection<Integer> typesToFind, Collection<Integer> terminateTypes) {
        return getAllNextIconsByType(currentIcon, typesToFind, terminateTypes, false);
    }

    public List<WorkflowIcon> getAllNextIconsByType(WorkflowIcon currentIcon, Collection<Integer> typesToFind, Collection<Integer> terminateTypes, boolean reverse) {
        List<WorkflowIcon> collectedIcons = new ArrayList<>();
        Set<Integer> seenIcons = new HashSet<>();
        collectAllNextIconsByType(collectedIcons, seenIcons, currentIcon, typesToFind, terminateTypes, reverse);
        return collectedIcons;
    }

    private void collectAllNextIconsByType(List<WorkflowIcon> collectedIcons, Set<Integer> seenIcons, WorkflowIcon currentIcon, Collection<Integer> typesToFind, Collection<Integer> terminateTypes, boolean reverse) {
        if (seenIcons.contains(currentIcon.getId()) || terminateTypes.contains(currentIcon.getType())) {
            return;
        } else {
            seenIcons.add(currentIcon.getId());
        }

        if (typesToFind.contains(currentIcon.getType())) {
            collectedIcons.add(currentIcon);
        }

        WorkflowNode currentNode = allNodes.get(currentIcon.getId());
        List<WorkflowNode> nextNodes = reverse ? currentNode.getPrevNodes() : currentNode.getNextNodes();

        if (CollectionUtils.isNotEmpty(nextNodes)) {
            for (WorkflowNode node : nextNodes) {
                collectAllNextIconsByType(collectedIcons, seenIcons, node.getNodeIcon(), typesToFind, terminateTypes, reverse);
            }
        }
    }

	public List<WorkflowIcon> getAllNextParallelIconsByType(WorkflowIcon currentIcon, Collection<Integer> typesToFind, Collection<Integer> terminateTypes, boolean reverse) {
		if (typesToFind.contains(currentIcon.getType())) {
			return Collections.singletonList(currentIcon);
		}
		else if (terminateTypes.contains(currentIcon.getType())) {
			return new ArrayList<>();
		}
		WorkflowNode curNode = allNodes.get(currentIcon.getId());
		List<WorkflowNode> nextNodes = reverse ? curNode.getPrevNodes() : curNode.getNextNodes();

        List<WorkflowIcon> foundIcons = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(nextNodes)) {
			for (WorkflowNode nextNode : nextNodes) {
				foundIcons.addAll(getAllNextParallelIconsByType(nextNode.getNodeIcon(), typesToFind, terminateTypes, reverse));
			}
		}
        return foundIcons;
	}

    /**
     * method provide possibility to find all chains
     */
    public List<List<WorkflowNode>> findChains(WorkflowIcon icon, boolean isForwardDirection) {
        WorkflowNode node = allNodes.get(icon.getId());
        List<WorkflowNode> nextNodes;
        if (isForwardDirection) {
            nextNodes = node.getNextNodes();
        } else {
            nextNodes = node.getPrevNodes();
        }

        if (nextNodes.size() > 0) {
            List<List<WorkflowNode>> result = new LinkedList<>();
            for (WorkflowNode nextNode : nextNodes) {
                WorkflowIcon workflowIcon = nextNode.getNodeIcon();
                List<List<WorkflowNode>> tempResult;
                tempResult = findChains(workflowIcon, isForwardDirection);

                if (tempResult.size() > 0) {
                    List<List<WorkflowNode>> newChains = new LinkedList<>();
                    for (List<WorkflowNode> chain : tempResult) {
                        List<WorkflowNode> newChain = new LinkedList<>();
                        newChain.add(nextNode);
                        newChain.addAll(chain);
                        newChains.add(newChain);
                    }
                    tempResult = newChains;
                } else {
                    List<List<WorkflowNode>> newChains = new LinkedList<>();
                    List<WorkflowNode> newChain = new LinkedList<>();
                    newChain.add(nextNode);
                    newChains.add(newChain);
                    tempResult = newChains;
                }
                result.addAll(tempResult);
            }
            return result;
        } else {
            //we haven't any next nodes
            return new LinkedList<>();
        }
    }

	public WorkflowNode getDecisionYesBranch(WorkflowNode decisionNode) {
		List<WorkflowNode> nextNodes = decisionNode.getNextNodes();
		if (nextNodes.size() != 2) {
			return null;
		}
		WorkflowNode firstNode = nextNodes.get(0);
		WorkflowNode secondNode = nextNodes.get(1);
		WorkflowIcon firstIcon = firstNode.getNodeIcon();
		WorkflowIcon secondIcon = secondNode.getNodeIcon();

        WorkflowIcon decisionIcon = decisionNode.getNodeIcon();
        //all nodes above the decision node
        if (decisionIcon.getY() >= firstIcon.getY() && decisionIcon.getY() >= secondIcon.getY()) {
            //the highest node must have YES label
            if (firstIcon.getY() < secondIcon.getY()) {
                return firstNode;
            }
            else if (firstIcon.getY() > secondIcon.getY()) {
                return secondNode;
            }
            else {
                //left node must have YES label
                if (firstIcon.getX() < secondIcon.getX()) {
                    return firstNode;
                }
                else {
                    return secondNode;
                }
            }
        }
        //all nodes under the decision node
        else if (decisionIcon.getY() < firstIcon.getY() && decisionIcon.getY() < secondIcon.getY()) {
            //left node always must have NO label
            if (firstIcon.getX() < secondIcon.getX()) {
                return secondNode;
            }
            else {
                return firstNode;
            }
        }
        //one of node above and another under the decision node
        else {
            if (firstIcon.getY() < secondIcon.getY()) {
                return firstNode;
            }
            else {
                return secondNode;
            }
        }
	}

	public WorkflowNode getDecisionNoBranch(WorkflowNode decisionNode) {
		List<WorkflowNode> nextNodes = decisionNode.getNextNodes();
		WorkflowNode decisionYesIcon = getDecisionYesBranch(decisionNode);
		if (decisionYesIcon == null) {
			return null;
		}
		if (decisionYesIcon == nextNodes.get(0)) {
			return nextNodes.get(1);
		}
		else {
			return nextNodes.get(0);
		}
	}

	public WorkflowNode getNodeByIcon(WorkflowIcon icon) {
		return allNodes.get(icon.getId());
	}

	public WorkflowNode getNodeByIconId(int id) {
		return allNodes.get(id);
	}

    public Map<Integer, WorkflowNode> getAllNodes() {
        return allNodes;
    }
}
