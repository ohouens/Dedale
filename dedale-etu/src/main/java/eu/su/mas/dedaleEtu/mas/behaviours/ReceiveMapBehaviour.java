package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent.State;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveMapBehaviour extends OneShotBehaviour{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1495684385089527428L;
	private MapRepresentation myMap;
	private int transition = 0;
	private boolean original;
	
	public ReceiveMapBehaviour(final AbstractDedaleAgent myAgent, boolean original) {
		super(myAgent);
		this.original = original;
	}
	
	public void action() {
		transition = 0;
		ExploreMultiAgent agent = ((ExploreMultiAgent) myAgent);
		agent.updateBehaviourMemory("RECEIVEMAP");
		boolean stop = true;
		for(String s : agent.getBehaviourMemory()) {
			if(!s.equals("RECEIVEMAP"))
				stop = false;
		}
		if(stop && agent.getBehaviourMemory().size() == ExploreMultiAgent.MEMORYSIZE) {
			System.out.println(agent.getLocalName()+" - REEEEEEEEESEET");
			agent.getBehaviourMemory().clear();
			transition = 2;
			return;
		}
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE);
		ACLMessage msgMap = myAgent.receive(mt);
		if(msgMap != null) {
			transition = 1;
			myMap = agent.getMap();
			String inter;
			System.out.println(myAgent.getLocalName()+" RECEIVE MAP FROM "+msgMap.getSender().getLocalName());
			inter = msgMap.getContent();
			myMap.merge(inter, agent.getClosedNodes(), agent.getOpenNodes());
			if(original)
				System.out.println(myAgent.getLocalName()+" - MAP MERGED !!!");
			ACLMessage ackMap = new ACLMessage(ACLMessage.CONFIRM);
			ackMap.setSender(agent.getAID());
			ackMap.addReceiver(msgMap.getSender());
			ackMap.setContent("ackMap");
			agent.sendMessage(ackMap);
			System.out.println(agent.getLocalName()+" - AckMap");
			if(original)
				System.out.println(agent.getLocalName()+" - transition to SendFusedMap");
			else {
				agent.changeState(State.rewind);
				agent.setLockCoundown(ExploreMultiAgent.SHARELOCK);
				System.out.println(agent.getLocalName()+" - transition to Exploration");
			}
		}
		System.out.println(myAgent.getLocalName()+" - MAP NOT RECEIVED");
	}
	
	@Override
	public int onEnd() {
		return transition;
	}
}
