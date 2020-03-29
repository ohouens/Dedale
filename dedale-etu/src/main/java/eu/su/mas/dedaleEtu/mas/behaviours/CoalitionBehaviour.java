package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import jade.core.behaviours.OneShotBehaviour;

public class CoalitionBehaviour extends OneShotBehaviour{
	private static final long serialVersionUID = 1L;
	
	private int transition=0;
	
	public CoalitionBehaviour(ExploreMultiAgent myAgent) {
		super(myAgent);
	}

	@Override
	public void action() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public int onEnd() {
		return transition;
	}

}
