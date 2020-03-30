package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import jade.core.behaviours.OneShotBehaviour;

public class PlanBehaviour extends OneShotBehaviour{

	private int transition;
	
	public PlanBehaviour(AbstractDedaleAgent myAgent) {
		super(myAgent);
	}
	
	@Override
	public void action() {
		ExploreMultiAgent agent = (ExploreMultiAgent)myAgent;
		
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
		ExploreMultiAgent agent = (ExploreMultiAgent)myAgent;
		List<String> result = new ArrayList<>();
		Set<String> queue = agent.getMap().getAllNodes();
		result.add(agent.getCurrentPosition());
		return result;
	}
	
	public List<String> rewind(){
		List<String> buffer = null;
		
		return buffer;
	}
}
