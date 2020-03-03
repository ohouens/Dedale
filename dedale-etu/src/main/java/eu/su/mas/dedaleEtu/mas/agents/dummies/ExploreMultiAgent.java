package eu.su.mas.dedaleEtu.mas.agents.dummies;

import java.util.ArrayList;

import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import eu.su.mas.dedaleEtu.mas.behaviours.AckPingMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploMultiBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.PingMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceiveMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceivePositionBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SayHello;
import eu.su.mas.dedaleEtu.mas.behaviours.SendMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SendPosition;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.tools.sniffer.Agent;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;

public class ExploreMultiAgent extends AbstractDedaleAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8829443829282917888L;
	private MapRepresentation myMap;
	
	public void setup() {
		super.setup();

		final Object[] args = getArguments();
//		System.out.println(args[0].toString()+"AAAAA");
//		List<String> agents =  (ArrayList<String>) args[0];
		
		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		
		List<String> agents = new ArrayList<String>();
		for(int i=1; i<=2; i++) {
			agents.add("Explo"+((Integer)i).toString());
		}
		
		//List<String> agents = getListAgents();
		/************************************************
		 * 
		 * ADD the behaviours of the Dummy Moving Agent
		 * 
		 ************************************************/

		FSMBehaviour fsm = new FSMBehaviour(this) {
			public int onEnd() {
				System.out.println("FSM behaviour terminé");
				myAgent.doDelete();
				return super.onEnd();
			}
		};
		
		fsm.registerFirstState(new ExploMultiBehaviour(this,this.myMap), "Explo");
		fsm.registerState(new PingMapBehaviour(this, agents), "PingMap");
		fsm.registerState(new AckPingMapBehaviour(this), "AckPingMap");
		fsm.registerState(new SendMapBehaviour(this, this.myMap, agents, false), "SendMap");
		fsm.registerState(new ReceiveMapBehaviour(this, this.myMap, true), "ReceiveMap");
		fsm.registerState(new ReceiveMapBehaviour(this, this.myMap, false), "ReceiveMapBis");
		fsm.registerState(new SendMapBehaviour(this, this.myMap, agents, true), "SendMapBis");
		
		fsm.registerTransition("Explo", "PingMap", 0);
		fsm.registerTransition("Explo", "SendMapBis", 1);
		fsm.registerTransition("PingMap", "Explo", 0);
		fsm.registerTransition("PingMap", "AckPingMap", 1);
		fsm.registerTransition("AckPingMap", "Explo", 0);
		fsm.registerTransition("AckPingMap", "ReceiveMap", 1);
		fsm.registerTransition("ReceiveMap", "Explo", 0);
		fsm.registerTransition("ReceiveMap", "SendMap", 1);
		fsm.registerTransition("SendMap", "SendMap", 0);
		fsm.registerTransition("SendMap", "Explo", 1);
		fsm.registerTransition("SendMapBis", "SendMapBis", 0);
		fsm.registerTransition("SendMapBis", "ReceiveMapBis", 1);
		fsm.registerTransition("ReceiveMapBis", "Explo", 0);
		fsm.registerTransition("ReceiveMapBis", "Explo", 1);
		
		lb.add(fsm);
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		addBehaviour(new startMyBehaviours(this, lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");
	}
	
	public MapRepresentation getMap() {
		return myMap;
	}
	
	public void setMap(MapRepresentation map) {
		myMap = map;
	}
	
	public List<String> getListAgents() {
		List<String> agents = new ArrayList<>();
		
		DFAgentDescription dfd = new DFAgentDescription();
	    dfd.setName(getAID());
	    ServiceDescription sd = new ServiceDescription();
	    sd.setType("");
	    sd.setName(getLocalName());

	    dfd.addServices(sd);
	    try {
	        DFService.register(this, dfd);
	    }catch(FIPAException fe){}

	    try {
		    DFAgentDescription[] result = DFService.search(this, dfd);
		    if (result.length > 0) {
		    	for (int i = 0; i < result.length; i++) {
		    		agents.add(result[i].getName().getLocalName());
		    		System.out.println(this.getLocalName()+" FOUND AGENT "+result[i].getName().getLocalName());
		    	}
		    	
		    }
	    }catch(FIPAException fe) {fe.printStackTrace();}
	    
	    return agents;
	}
}
