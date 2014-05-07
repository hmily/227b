package org.ggp.base.player.gamer.statemachine.sample;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;

/**
 * ProximityHeuristicGamer
 *
 * Uses a heuristic evaluation function after exploring a fixed number of MAX nodes
 * that returns actual rewards on terminal states and the goal value for all other states.
 *
 */
public class ProximityHeuristicGamer extends SimpleHeuristicGamer {

	@Override
	public int heuristicEval(MachineState state, Role role)
			throws GoalDefinitionException {
		return stateMachine.getGoal(state, role);
	}

}
