package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ExploMultiBehaviour extends OneShotBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5631327286239419149L;
	
	private int transition = 1;
	
	public ExploMultiBehaviour(final ExploreMultiAgent myAgent) {
		super(myAgent);
	}

	@Override
	public void action() {
		ExploreMultiAgent agent = (ExploreMultiAgent)myAgent;
		
		if(agent.getMap() == null)
			agent.setMap(new MapRepresentation());
		MapRepresentation myMap = agent.getMap();
		List<String> openNodes = agent.getOpenNodes();
		Set<String> closedNodes = agent.getClosedNodes();
		
		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		agent.updatePositionMemory(myPosition);
		agent.printMemory();
		
		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition

			//1) remove the current node from openlist and add it to closedNodes.
			closedNodes.add(myPosition);
			openNodes.remove(myPosition);

			myMap.addNode(myPosition,MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNode=null;
			transition = 1;
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				Couple<String, List<Couple<Observation, Integer>>> couple = iter.next();
				String nodeId=couple.getLeft();
				List <Couple<Observation, Integer>> listObsInt = couple.getRight();
				if (!closedNodes.contains(nodeId)){
					if (!openNodes.contains(nodeId)){
						openNodes.add(nodeId);
						myMap.addNode(nodeId, MapAttribute.open);
						myMap.addEdge(myPosition, nodeId);	
					}else{
						//the node exist, but not necessarily the edge
						myMap.addEdge(myPosition, nodeId);
					}
					if (nextNode==null) nextNode=nodeId;
				}
				
				System.out.println("OBSERVATIONS: " + listObsInt);
				for (int i = 0; i < listObsInt.size(); i++) {
					if (listObsInt.get(i).getLeft().toString().equals("Stench")){
						System.out.println(agent.getLocalName()+" - I can smell the golem from here!");
						String stenchPos = couple.getLeft();
						System.out.println(agent.getLocalName()+" - Odor position: " + stenchPos);
						nextNode = stenchPos;
						agent.changeState(ExploreMultiAgent.State.hunt);
						System.out.println(agent.getLocalName()+" - HUNTING Mode activated");
					}
				}
			}
			
			//3) while openNodes is not empty, continues.
			if (openNodes.isEmpty()){
				//Explo finished
				agent.setExploDone(true);
				System.out.println(agent.getLocalName()+" - Exploration successufully done, behaviour removed.");
				return;
			}
			
			//4) select next move.
			//4.1 If there exist one open node directly reachable, go for it,
			//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
			if (nextNode==null && !openNodes.isEmpty()){
				//no directly accessible openNode
				//chose one, compute the path and take the first step.
				List<String> sp = myMap.getShortestPath(myPosition, openNodes.get(0));
				if(sp.size() > 0)
					nextNode=sp.get(0);
			}
			
			//list of observations associated to the currentPosition
			System.out.println(this.myAgent.getLocalName()+" - State of the observations : "+lobs);
			
			switch(lobs.size()) {
				case 2:
					agent.getLeaf().add(myPosition);
					System.out.println(agent.getLocalName()+" - "+myPosition+" is leaf");
					break;
				case 3:
					agent.getTunnel().add(myPosition);
					System.out.println(agent.getLocalName()+" - "+myPosition+" is tunnel");
					break;
				default:
					break;
			}
			
			/************************************************
			 * 				END API CALL ILUSTRATION
			 *************************************************/
			agent.move(nextNode);
			if(transition == 1)
				System.out.println(agent.getLocalName()+" - transition to SWITCH");
			else
				System.out.println(agent.getLocalName()+" - Stay in EXPLO");
		}
	}
	
	@Override
	public int onEnd() {
		return transition;
	}
}
