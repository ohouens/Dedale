package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import jade.core.behaviours.OneShotBehaviour;

public class PlanBehaviour extends OneShotBehaviour{

	private int transition;
	private ExploreMultiAgent agent;
	
	public PlanBehaviour(AbstractDedaleAgent myAgent) {
		super(myAgent);
		agent = (ExploreMultiAgent)myAgent;
	}
	
	@Override
	public void action() {
		switch(agent.getCurrentState()) {
			case rewind:
				transition = 1;
				System.out.println(agent.getLocalName()+" - transition to TARGET");
				break;
			case hunt:
				transition = 2;
				System.out.println(agent.getLocalName()+" - transition to HUNTING");
				break;
			default:
				if(!agent.getExploDone()) {
					transition = 0;
					System.out.println(agent.getLocalName()+" - transition to EXPLORATION");
				}else {
					if(agent.getRoute() == null)
						agent.setRoute(makeRoute());
					transition = 3;
					System.out.println(agent.getLocalName()+" - transition to COALITION");
				}
				break;
		}
	}
	
	@Override
	public int onEnd() {
		return transition;
	}
	
	public List<String> makeRoute() {
		List<String> result = new ArrayList<>();
		Set<String> queue = agent.getMap().getAllNodes();
		String cursor = agent.getCurrentPosition();
		result.add(cursor);
		queue.remove(cursor);
		agent.setMaxSpace(1);
		while(!queue.isEmpty()) {
			Iterator<String> neighbor = agent.getMap().getNeighbor(cursor);
			String inter = null;
			while(neighbor.hasNext() && inter == null) {
				inter = neighbor.next();
				if(!queue.contains(inter))
					inter = null;
			}
			if(inter != null) {
				cursor = inter;
				result.add(cursor);
				queue.remove(cursor);
			}else {
				System.out.println("REWIIIIIIIIND QUUEUUE");
				Collection<String> rewind = rewind(queue,result);
				agent.setMaxSpace(rewind.size()+1);
				result.addAll(rewind);
			}
		}
		return result;
	}
	
	public Collection<String> rewind(Set<String> queue, List<String> result){
		List<String> buffer = new ArrayList<>();
		List<String> copy = new ArrayList<>(result);
		String cursor = null;
		int i = copy.size()-1;
		while(cursor == null && copy.contains(cursor)) {
			cursor = copy.get(i);
			buffer.add(cursor);
			Iterator<String> neighbor = agent.getMap().getNeighbor(cursor);
			String inter = null;
			while(neighbor.hasNext() && inter == null) {
				inter = neighbor.next();
				if(!queue.contains(inter))
					inter = null;
			}
			if(inter != null)
				cursor = inter;
			i--;
		}
		return buffer;
	}
}
