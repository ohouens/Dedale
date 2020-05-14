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
		
		myMap = agent.getMap();
		ACLMessage sendMap = new ACLMessage(ACLMessage.PROPAGATE);
		sendMap.setSender(myAgent.getAID());
		sendMap.addReceiver(agent.getLastReceive().getSender());
//		sendMap.setContent(myMap.serialize());
		sendMap.setContent(agent.getToSend().serialize());
		((AbstractDedaleAgent) myAgent).sendMessage(sendMap);
//		System.out.println(myAgent.getLocalName()+" - SEND MAP !!!!!");
		
		agent.doWait();
		
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
		ACLMessage checkDone = agent.receive(mt);
		if(checkDone != null) {
			String name = checkDone.getSender().getLocalName();
			myMap.resetBuffer(name);
//			System.out.println(myAgent.getLocalName()+" - "+name+" MAP BUFFER RESET !!!");
			transition = 1;
			if(original) {
//				System.out.println(agent.getLocalName()+" - transition to ReceiveFusedMAP");
			}else {
				agent.changeState(State.target);
				agent.setLockCoundown(5);
				agent.randomTarget();
//				System.out.println(agent.getLocalName()+" - transition to PLANIFICATION");
			}
			return;
		}
		
//		System.out.println(agent.getLocalName()+" - Send Transmission error, return to SWITCH");
		agent.setLastReceive(null);
		agent.setLastSend(null);
		agent.getBehaviourMemory().clear();
		transition = 2;
	}
	
	@Override
	public int onEnd() {
		return transition;
	}
}
