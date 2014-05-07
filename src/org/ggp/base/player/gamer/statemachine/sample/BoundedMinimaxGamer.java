package org.ggp.base.player.gamer.statemachine.sample;

import java.util.List;
//import java.util.Map;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

/**
 * BoundedMinimaxGamer
 *
 * Uses 0 & 100 as goal bounds to restrict its search space.
 *
 */
public class BoundedMinimaxGamer extends SampleGamer {

	protected StateMachine stateMachine;
	long finishCurrentMoveBy;

	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException {
		// Initialize class member variables.
		stateMachine = getStateMachine();
	}

	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException {

		finishCurrentMoveBy = timeout - 1000;

		long start = System.currentTimeMillis();
		Move bestMove = getBestMove(getRole(), getCurrentState());
		long stop = System.currentTimeMillis();

		notifyObservers(new GamerSelectedMoveEvent(stateMachine.getLegalMoves(
				getCurrentState(), getRole()), bestMove, stop - start));
		return bestMove;
	}

	public Move getBestMove(Role role, MachineState state)
			throws MoveDefinitionException, GoalDefinitionException,
			TransitionDefinitionException {
		List<Move> legalMoves = stateMachine.getLegalMoves(state, role);
		Move bestMove = legalMoves.get(0);
		int score = 0;

		for (Move move : legalMoves) {
			int result = getMinScore(role, move, state);
			if (result == 100) {
				return move;
			}
			if (result > score) {
				score = result;
				bestMove = move;
			}
		}

		return bestMove;
	}

	public int getMinScore(Role role, Move move, MachineState state)
			throws MoveDefinitionException, TransitionDefinitionException,
			GoalDefinitionException {

		List<List<Move>> jointMoves = stateMachine.getLegalJointMoves(state,
				role, move);
		int score = 100;

		for (List<Move> jointMove : jointMoves) {
			MachineState nextState = stateMachine
					.getNextState(state, jointMove);
			int result = getMaxScore(role, nextState);
			if (result == 0) {
				return 0;
			}
			if (result < score) {
				score = result;
			}
		}
		return score;
	}

	public int getMaxScore(Role role, MachineState state)
			throws GoalDefinitionException, MoveDefinitionException,
			TransitionDefinitionException {
		if (stateMachine.isTerminal(state)) {
			return stateMachine.getGoal(state, role);
		}

		List<Move> legalMoves = stateMachine.getLegalMoves(state, role);
		int score = 0;

		for (Move move : legalMoves) {
			int result = getMinScore(role, move, state);
			if (result == 100) {
				return 100;
			}
			if (result > score) {
				score = result;
			}
		}
		return score;
	}
}