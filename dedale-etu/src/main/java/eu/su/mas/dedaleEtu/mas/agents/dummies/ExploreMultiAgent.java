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
	private int role=0;
	private String nextPosition="";
	
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

		lb.add(new ExploMultiBehaviour(this,this.myMap));
//		lb.add(new SendPosition(this, agents));
//		lb.add(new ReceivePositionBehaviour(this, this.myMap));
		lb.add(new SendMapBehaviour(this, this.myMap, agents));
		lb.add(new ReceiveMapBehaviour(this, this.myMap));
		lb.add(new AckPingMapBehaviour(this));
		lb.add(new PingMapBehaviour(this, agents));
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		
		
		addBehaviour(new startMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");
	}
	
	
	public int getRole() {
		return role;
	}
	
	public String getNextPosition() {
		return nextPosition;
	}
	
	public void setRole(int i) {
		role = i;
	}
	
	public void setNextPosition(String s) {
		nextPosition = s;
	}
	
	public void move() {
		((AbstractDedaleAgent)this).moveTo(nextPosition);
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
