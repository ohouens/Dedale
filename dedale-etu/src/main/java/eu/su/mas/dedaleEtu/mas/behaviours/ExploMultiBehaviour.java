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
		

		if(agent.isBlocked()) {
			System.out.println(agent.getLocalName()+" - Target mode activated");
			agent.changeState(ExploreMultiAgent.State.target);
			String target = "";
			int taille = 1000;
			if(openNodes.size() > 1) {
				for(int i=0; i<openNodes.size(); i++) {
					List<String> inter = myMap.getShortestPath(myPosition, openNodes.get(i));
					if(!openNodes.get(i).equals(agent.getTarget()) && inter.size() < taille) {
						target = openNodes.get(i);
						taille = inter.size();
//						System.out.println(agent.getLocalName()+" - getTarget="+agent.getTarget()+" newTarget="+target);
					}
				}
			}else {
				Iterator<String> iter = closedNodes.iterator();
				while(iter.hasNext()) {
					String cn = iter.next();
					List<String> inter = myMap.getShortestPath(myPosition, cn);
					if(!cn.equals(myPosition) && inter.size() < taille && !agent.getTunnel().contains(cn) && !agent.getLeaf().contains(cn)) {
						target = cn;
						taille = inter.size();
//						System.out.println(agent.getLocalName()+" - cn="+cn+" pos="+myPosition);
					}
				}
			}
			agent.setTarget(target);
			agent.setLockCoundown(ExploreMultiAgent.SHARELOCK);
			System.out.println(agent.getLocalName()+" - target "+agent.getTarget());
			System.out.println(agent.getLocalName()+" - transition to SWITCH");
			return;
		}
		
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
			
			// if one of the observable nodes has an odor, then we save it as a memory
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter2 = lobs.iterator();
			int odorCpt = 0;
			
			
			while (iter2.hasNext()) {
				Couple <String, List<Couple<Observation, Integer>>> StrList = iter2.next();
				List <Couple<Observation, Integer>> listObsInt = StrList.getRight();
				//System.out.println("OBSERVATIONS: " + listObsInt);
				for (int i = 0; i < listObsInt.size(); i++) {
					if (listObsInt.get(i).getLeft().toString().equals("Stench")){
						odorCpt++;
						System.out.println("I can smell the golem from here!");
						String stenchPos = StrList.getLeft();
						System.out.println("Odor position: " + stenchPos);
						agent.setLastOdor(stenchPos);
						System.out.println("Odor memory: " + agent.getLastOdor().toString());
						// TODO: il faut s'assurer que la position de l'odeur est la position du golem pour mettre golemBlocked à true
						// càd on doit verifier que la case où on veut aller est occupée
					}
				}
			}
			if (odorCpt >=2) {
				agent.getGolemZones().add(myPosition);
			}
				
			
			if (nextNode==null && !openNodes.isEmpty()){
				agent.setTarget(openNodes.get(0));
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
			agent.setTarget(nextNode);
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
