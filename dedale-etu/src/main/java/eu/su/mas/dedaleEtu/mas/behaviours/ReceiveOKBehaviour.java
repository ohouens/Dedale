package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveOKBehaviour extends SimpleBehaviour{
	
	private static final long serialVersionUID = 2606822683718779669L;
	private boolean finished=false;

	public ReceiveOKBehaviour(final AbstractDedaleAgent myAgent){
		super(myAgent);
	}
	
	public void action() {
		ExploreMultiAgent agent = ((ExploreMultiAgent) myAgent);
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
		ACLMessage msgOK = myAgent.receive(mt);
		if(msgOK != null) {
			agent.setRole(-10);
			System.out.println(agent.getLocalName()+" RECEIVE "+msgOK.getContent());
		}
		
	}
	
	public boolean done() {
		return finished;
	}
}
