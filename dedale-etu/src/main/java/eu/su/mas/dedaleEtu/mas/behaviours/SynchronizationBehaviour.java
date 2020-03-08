package eu.su.mas.dedaleEtu.mas.behaviours;

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
		ExploreMultiAgent agent = (ExploreMultiAgent)myAgent;
		ACLMessage MPing = agent.getLastSend();
		ACLMessage HPing = agent.getLastReceive();
		transition = 0;
		if(Long.parseLong(HPing.getContent()) == Long.parseLong(MPing.getContent())) {
			String s1 = agent.getLocalName();
			String s2 = HPing.getSender().getLocalName();
			if(Integer.parseInt(s1.substring(s1.length() - 1)) < Integer.parseInt(s2.substring(s2.length() - 1))) {
				transition = 1;
				System.out.println(agent.getLocalName()+" - Transition to SendOriginalMAP");
				return;
			}
			System.out.println(agent.getLocalName()+" - Transition to ReceiveOriginalMAP");
		}else {
			if(Long.parseLong(HPing.getContent()) < Long.parseLong(MPing.getContent())) {
				transition = 1;
				System.out.println(agent.getLocalName()+" - Transition to SendOriginalMAP");
				return;
			}
			System.out.println(agent.getLocalName()+" - Transition to ReceiveOriginalMAP");
		}
	}
	
	@Override
	public int onEnd() {
		return transition;
	}

}
