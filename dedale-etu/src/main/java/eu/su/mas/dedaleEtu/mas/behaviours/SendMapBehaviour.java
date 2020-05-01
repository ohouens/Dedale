package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent.State;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SendMapBehaviour extends OneShotBehaviour{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2606822683718779669L;
	private MapRepresentation myMap;
	private int transition = 0;
	private boolean original;

	public SendMapBehaviour(final AbstractDedaleAgent myAgent, boolean original) {
		super(myAgent);
		this.original = original;
	}
	
	@Override
	public void action() {
		transition = 0;
		ExploreMultiAgent agent = ((ExploreMultiAgent) myAgent);
		agent.updateBehaviourMemory("SENDMAP");
		boolean stop = true;
		for(String s : agent.getBehaviourMemory()) {
			if(!s.equals("SENDMAP"))
				stop = false;
		}
		if(stop && agent.getBehaviourMemory().size() == ExploreMultiAgent.MEMORYSIZE) {
			System.out.println(agent.getLocalName()+" - REEEEEEEEESEET");
			agent.getBehaviourMemory().clear();
			transition = 2;
			return;
		}
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
		ACLMessage checkDone = agent.receive(mt);
		if(checkDone != null) {
			transition = 1;
			if(original)
				System.out.println(agent.getLocalName()+" - transition to ReceiveFusedMAP");
			else {
				agent.changeState(State.target);
				agent.setLockCoundown(ExploreMultiAgent.SHARELOCK);
				System.out.println(agent.getLocalName()+" - transition to PLANIFICATION");
			}
			return;
		}
		
		myMap = agent.getMap();
		ACLMessage sendMap = new ACLMessage(ACLMessage.PROPAGATE);
		sendMap.setSender(myAgent.getAID());
		sendMap.addReceiver(agent.getLastReceive().getSender());
		sendMap.setContent(myMap.serialize(agent.getClosedNodes()));
		((AbstractDedaleAgent) myAgent).sendMessage(sendMap);
		System.out.println(myAgent.getLocalName()+" - SEND MAP !!!!!");
	}
	
	@Override
	public int onEnd() {
		return transition;
	}
}
