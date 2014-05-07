package org.ggp.base.player.gamer.statemachine.sample;

import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

/**
 * TODO CompulsiveDeliberationGamer info
 *
 */
public final class CompulsiveDeliberationGamer extends SampleGamer {
	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException {
		long start = System.currentTimeMillis();
		Move bestMove = getBestMove(getRole(), getCurrentState());
		long stop = System.currentTimeMillis();

		notifyObservers(new GamerSelectedMoveEvent(getStateMachine()
				.getLegalMoves(getCurrentState(), getRole()), bestMove, stop
				- start));
		return bestMove;
	}

	private Move getBestMove(Role role, MachineState state)
			throws MoveDefinitionException, GoalDefinitionException,
			TransitionDefinitionException {
		List<List<Move>> jointMoves = getStateMachine().getLegalJointMoves(
				state);
		List<Move> jointMove = jointMoves.get(0);
		int score = 0;
		for (List<Move> potentialJointMove : jointMoves) {
			int result = getMaxScore(role,
					getStateMachine().getNextState(state, potentialJointMove));

			if (result > score) {
				score = result;
				jointMove = potentialJointMove;
				if (score == 100) {
					break;
				}
			}
		}

		Map<Role, Integer> roleIndexMap = getStateMachine().getRoleIndices();
		return jointMove.get(roleIndexMap.get(role));
	}

	private int getMaxScore(Role role, MachineState state)
			throws GoalDefinitionException, MoveDefinitionException,
			TransitionDefinitionException {
		if (getStateMachine().isTerminal(state)) {
			return getStateMachine().getGoal(state, role);
		}
		List<List<Move>> jointMoves = getStateMachine().getLegalJointMoves(
				state);
		int score = 0;
		for (List<Move> jointMove : jointMoves) {
			int result = getMaxScore(role,
					getStateMachine().getNextState(state, jointMove));
			if (result > score) {
				score = result;
			}
		}

		return score;
	}

	// Alex commented out getMaxScoreAlternate to suppress an unused method warning.
	// Checked that this class is not extended anywhere, so there are no chances of non-local
	// calls.

//	private int getMaxScoreAlternate(Role role, MachineState state)
//			throws GoalDefinitionException, MoveDefinitionException,
//			TransitionDefinitionException {
//		if (getStateMachine().isTerminal(state)) {
//			return getStateMachine().getGoal(state, role);
//		}
//		List<MachineState> nextStates = getStateMachine().getNextStates(state);
//		int score = 0;
//		for (MachineState nextState : nextStates) {
//			int result = getMaxScore(role, nextState);
//			if (result > score) {
//				score = result;
//			}
//		}
//
//		return score;
//	}
}