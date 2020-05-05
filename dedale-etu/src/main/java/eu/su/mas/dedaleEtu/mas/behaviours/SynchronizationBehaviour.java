package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Date;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SynchronizationBehaviour extends OneShotBehaviour{
	private static final long serialVersionUID = -2418510449427549228L;
	
	private int transition=0;
	
	public SynchronizationBehaviour(final AbstractDedaleAgent myAgent) {
		super(myAgent);
	}

	@Override
	public void action() {
		transition = 0;
		ExploreMultiAgent agent = (ExploreMultiAgent)myAgent;
		agent.updateView();
		ACLMessage MPing = agent.getLastSend();
		ACLMessage HPing = agent.getLastReceive();
		String s1 = agent.getLocalName();
		String s2 = HPing.getSender().getLocalName();
		if(agent.getExploDoneBuffer() && agent.getExploDone() || (agent.getBufferSizeBuffer()==0 && agent.getMap().getBuffer(s2).getAllNodes().size()==0)) {
			transition = 2;
			System.out.println(agent.getLocalName()+" - Transition to Switch");
			return;
		}
		if(agent.getBufferSizeBuffer() < agent.getMap().getBuffer(s2).getAllNodes().size()) {
			transition = 1;
			System.out.println(agent.getLocalName()+" - Transition to SendOriginalMAP");
			return;
		}
		System.out.println(agent.getLocalName()+" - Transition to ReceiveOriginalMAP");
	}
	
	@Override
	public int onEnd() {
		return transition;
	}

}
