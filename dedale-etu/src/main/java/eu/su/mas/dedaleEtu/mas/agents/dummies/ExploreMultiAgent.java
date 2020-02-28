package eu.su.mas.dedaleEtu.mas.agents.dummies;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploMultiBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceiveMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceivePositionBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SayHello;
import eu.su.mas.dedaleEtu.mas.behaviours.SendMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SendPosition;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;

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
		/************************************************
		 * 
		 * ADD the behaviours of the Dummy Moving Agent
		 * 
		 ************************************************/

		lb.add(new ExploMultiBehaviour(this,this.myMap));//0
//		lb.add(new SendPosition(this, agents));//1
//		lb.add(new ReceivePositionBehaviour(this, this.myMap));//2
		lb.add(new SendMapBehaviour(this, this.myMap, agents));
		lb.add(new ReceiveMapBehaviour(this, this.myMap));
		
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
}
