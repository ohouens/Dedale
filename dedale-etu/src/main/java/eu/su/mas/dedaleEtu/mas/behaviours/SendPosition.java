package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class SendPosition extends TickerBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2058134622078521998L;
	
	private List<String> agentNames = new ArrayList<>();

	/**
	 * An agent tries to contact its friend and to give him its current position
	 * @param myagent the agent who posses the behaviour
	 *  
	 */
	public SendPosition (final Agent myagent, List<String> agentNames) {
		super(myagent, 3000);
		//super(myagent);
		this.agentNames = agentNames;
		
	}

	@Override
	public void onTick() {
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		//A message is defined by : a performative, a sender, a set of receivers, (a protocol),(a content (and/or contentOBject))
		ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.myAgent.getAID());
		msg.setProtocol("UselessProtocol");

		if (myPosition!=""){
			//System.out.println("Agent "+this.myAgent.getLocalName()+ " is trying to reach its friends");
			msg.setContent(myPosition);

			for(String s : agentNames) {
				if(!s.equals(myAgent.getLocalName())) {
					msg.addReceiver(new AID(s,AID.ISLOCALNAME));
				}
			}

			//Mandatory to use this method (it takes into account the environment to decide if someone is reachable or not)
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		}
	}
}
