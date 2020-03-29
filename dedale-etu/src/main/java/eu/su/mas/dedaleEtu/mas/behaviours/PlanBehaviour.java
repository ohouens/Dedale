package eu.su.mas.dedaleEtu.mas.behaviours;

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
				transition = 0;
				System.out.println(agent.getLocalName()+" - transition to EXPLORATION");
				break;
		}
	}
	
	public int onEnd() {
		return transition;
	}
}
