/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.target.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.target.TargetError;
import org.agnitas.target.TargetNode;
import org.agnitas.target.TargetNodeValidator;
import org.agnitas.target.TargetNodeValidatorKit;
import org.agnitas.target.TargetRepresentation;

import com.agnitas.emm.core.target.nodes.TargetNodeMailingClickedOnSpecificLink;
import com.agnitas.emm.core.target.nodes.TargetNodeMailingRevenue;

@Deprecated
public class TargetRepresentationImpl implements TargetRepresentation {
	
	protected List<TargetNode> allNodes = null;

	private static final long serialVersionUID = -5118626285211811379L;

	public TargetRepresentationImpl() {
		allNodes = new ArrayList<>();
	}

	@Override
	public String generateBsh() {
		StringBuilder sb = new StringBuilder();

		for (TargetNode node : allNodes) {
			sb.append(node.generateBsh());
		}

		return sb.toString();
	}

	@Override
	public boolean checkBracketBalance() {
		int balance = 0;
		for (TargetNode tmpNode : allNodes) {
			if (tmpNode.isOpenBracketBefore()) {
				balance++;
			}
			if (tmpNode.isCloseBracketAfter()) {
				balance--;
			}
			if (balance < 0) {
				return false;
			}
		}
		return balance == 0;
	}

	@Override
	public void addNode(TargetNode aNode) {
		if (aNode != null) {
			allNodes.add(aNode);
		}
	}

	@Override
	public void setNode(int idx, TargetNode aNode) {
		if (aNode != null) {
			allNodes.add(idx, aNode);
		}
	}

	@Override
	public boolean deleteNode(int index) {
		allNodes.remove(index);
		return true;
	}

	@Override
	public List<TargetNode> getAllNodes() {
		return allNodes;
	}

	@SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		ObjectInputStream.GetField allFields = in.readFields();
		allNodes = (List<TargetNode>) allFields.get("allNodes", new ArrayList<TargetNode>());
	}
	
	@Override
	public List<Collection<TargetError>> validate( TargetNodeValidatorKit validatorKit, @VelocityCheck int companyId) {
		List<Collection<TargetError>> resultList = new Vector<>();
		
		for( TargetNode node : allNodes) {
			resultList.add( validateNode( node, validatorKit, companyId));
		}
		
		return resultList;
	}
	
	/**
	 * Validates a single target node.
	 * 
	 * @param node node to validate
	 * @param validatorKit kit for node validation
	 * 
	 * @return Collection containing all errors for the node
	 */
	private Collection<TargetError> validateNode( TargetNode node, TargetNodeValidatorKit validatorKit, @VelocityCheck int companyId) {
		if( validatorKit != null) {
			TargetNodeValidator validator = null;
			if( node instanceof TargetNodeDate) {
				validator = validatorKit.getDateNodeValidator();
			} else if( node instanceof TargetNodeNumeric) {
				validator = validatorKit.getNumericNodeValidator();
			} else if( node instanceof TargetNodeString) {
				validator = validatorKit.getStringNodeValidator();
			} else if( node instanceof TargetNodeIntervalMailing) {
				validator = validatorKit.getIntervalMailingNodeValidator();
			} else if( node instanceof TargetNodeMailingClicked) {
				validator = validatorKit.getMailingClickedNodeValidator();
			} else if( node instanceof TargetNodeMailingOpened) {
				validator = validatorKit.getMailingOpenedNodeValidator();
			} else if( node instanceof TargetNodeMailingReceived) {
				validator = validatorKit.getMailingReceivedNodeValidator();
			} else if(node instanceof TargetNodeMailingRevenue) {
				validator = validatorKit.getMailingRevenueNodeValidator();
			} else if(node instanceof TargetNodeMailingClickedOnSpecificLink) {
				validator = validatorKit.getMailingSpecificLinkClickNodeValidator();
			} else if(node instanceof TargetNodeAutoImportFinished) {
				validator = validatorKit.getAutoImportFinishedNodeValidator();
			}
			
			if( validator != null)
				return validator.validate( node, companyId);
		}
		
		// No matching validator found, so report as "cannot validate"!
		Collection<TargetError> collection = new Vector<>();
		collection.add( new TargetError( TargetError.ErrorKey.CANNOT_VALIDATE));
		return collection;
	}

	@Override
	public boolean hasSameNodeStructureAs(TargetRepresentation representation) {
		List<TargetNode> otherNodes = representation.getAllNodes();
		
		// Different size? Different structure!
		if(otherNodes.size() != allNodes.size()) {
			return false; 
		} else {
			for(int i = 0; i < allNodes.size(); i++) {
				if(!allNodes.get(i).equalNodes(otherNodes.get(i))) {
					return false;
				}
			}
		}
		
		return true;
	}
	

}
