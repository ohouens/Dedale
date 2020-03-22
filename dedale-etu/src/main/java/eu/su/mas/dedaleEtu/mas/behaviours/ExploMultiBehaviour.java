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
		if(agent.getCurrentState() != ExploreMultiAgent.State.rewind)
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
			
			switch(agent.getCurrentState()) {
				case rewind:
					transition = 0;
					if(agent.getLockCountdown() <= 0) {
						agent.changeState(ExploreMultiAgent.State.explo);
						agent.setSequence(true);
						return;
					}
					int size = agent.getPositionMemory().size();
					List<String> pm = agent.getPositionMemory();
					if(size >= 2 && !pm.get(size-2).equals(pm.get(size-1)) && agent.getLastMove() && agent.getSequence()) {
						nextNode = agent.getPositionMemory().remove((int)size-1);
						System.out.println(agent.getLocalName()+" - new REWIND position "+nextNode);
					}else {
						agent.setSequence(false);
						Random random = new Random();
						int index = random.nextInt(lobs.size());
						nextNode = lobs.get(index).getLeft(); 
						System.out.println(agent.getLocalName()+" - new STOCHASTIC position "+nextNode);
					}
					agent.updateLC();
					break;
				case coalition:
					System.out.println();
					break;
				default:
					transition = 1;
					Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
					Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter2 = lobs.iterator();
					Boolean golemBlocked = false;
					while(iter.hasNext()){
						String nodeId=iter.next().getLeft();
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
					}
		
					//3) while openNodes is not empty, continues.
					if (openNodes.isEmpty() || golemBlocked){
						//Explo finished
						System.out.println(agent.getLocalName()+" - Exploration successufully done, behaviour removed.");
						return;
					}else{
						//4) select next move.
						//4.1 If there exist one open node directly reachable, go for it,
						//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
						while (iter2.hasNext()) {
							Couple <String, List<Couple<Observation, Integer>>> StrList = iter2.next();
							List <Couple<Observation, Integer>> listObsInt = StrList.getRight();
							System.out.println("OBSERVATIONS: " + listObsInt);
							for (int i = 0; i < listObsInt.size(); i++) {
								if (listObsInt.get(i).getLeft().toString().equals("Stench")){
									System.out.println("I can smell the golem from here!");
									String stenchPos = StrList.getLeft();
									System.out.println("Odor position: " + stenchPos);
									nextNode = stenchPos;
									// TODO: il faut s'assurer que la position de l'odeur est la position du golem pour mettre golemBlocked à true
									// càd on doit verifier que la case où on veut aller est occupée
								}
							}
						}
					}
					if (nextNode==null && !openNodes.isEmpty()){
						//no directly accessible openNode
						//chose one, compute the path and take the first step.
						nextNode=myMap.getShortestPath(myPosition, openNodes.get(0)).get(0);
					}
					break;
			}
			//list of observations associated to the currentPosition
			System.out.println(this.myAgent.getLocalName()+" - State of the observations : "+lobs);

			/************************************************
			 * 				END API CALL ILUSTRATION
			 *************************************************/

			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(3000);
				//System.in.read();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(nextNode != null) {
				agent.setLastMove(agent.moveTo(nextNode));
				System.out.println(agent.getLocalName()+" - moveTo "+nextNode+" : "+agent.getLastMove());
			}
			if(transition == 1)
				System.out.println(agent.getLocalName()+" - transition to SWITCH");
			else
				System.out.println(agent.getLocalName()+" - transition to EXPLO");
		}
	}
	
	@Override
	public int onEnd() {
		return transition;
	}
}
