package org.ggp.base.player.gamer.statemachine.sample;

import java.util.List;

import org.ggp.base.apps.player.detail.DetailPanel;
import org.ggp.base.apps.player.detail.SimpleDetailPanel;
import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.player.gamer.statemachine.sample.TreeNode;

public final class MyMonteCarloSearchGamer extends StateMachineGamer {

	@Override
	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		// Do nothing.
	}

	public static final double VISIT_BOOST = 200.0;
	public static final int OFFSET = 500;
	public static final int MC_COUNT = 4;

	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		List<Move> moves = getStateMachine().getLegalMoves(getCurrentState(),
				getRole());

		if (moves.size() == 1)
			return moves.get(0);
		TreeNode root = new TreeNode(getCurrentState(), null);
		root.maxNode = true;
		while (System.currentTimeMillis() < timeout - OFFSET) {
			TreeNode node = select(root);
			if (node.visits == 0)
				expand(node);
			if (node.maxNode == true) {
				int score = depthCharge(node.state, timeout);
				backpropagate(node, score);
			} else {
				int score = depthChargeOther(node.state, timeout, node.ourMove);
				backpropagate(node, score);
			}
		}
		Move action = findBest(root);
		return action;
	}

	private Move findBest(TreeNode node) {
		// TreeNode node must be the root.
		List<TreeNode> children = node.children;
		double bestCurrentScore = 0;
		TreeNode bestTreeNode = children.get(0);
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i).getAverageUtility() > bestCurrentScore) {
				bestCurrentScore = children.get(i).getAverageUtility();
				bestTreeNode = children.get(i);
			}
		}
		System.out.println("Best move, score: "
				+ bestTreeNode.getAverageUtility() + ", visits: "
				+ bestTreeNode.visits);
		return bestTreeNode.ourMove;
	}

	private TreeNode select(TreeNode node) {
		// terminal states do not have children. Automatically must select
		// yourself
		if (node.children.size() == 0)
			return node;

		if (node.visits == 0)
			return node;
		double score = 0.0;
		TreeNode result = node.children.get(0);
		for (int i = 0; i < node.children.size(); i++) {
			if (node.children.get(i).visits == 0)
				return node.children.get(i);
			double newscore = selectfn(node.children.get(i));
			if (newscore > score) {
				score = newscore;
				result = node.children.get(i);
			}
		}
		return select(result);
	}

	// CHANGE: This weights possible optimal move against number of visits.
	private double selectfn(TreeNode node) {
		// return node.getAverageUtility() + 100.0/node.visits;
		if (!node.maxNode) // prioritize score and boost by (lack of) confidence
			return node.getAverageUtility() + VISIT_BOOST
					* Math.sqrt(Math.log(node.parent.visits) / node.visits);
		else
			// negative utility now; they want to minimize... and boost by (lack
			// of) confidence
			return (100 - node.getAverageUtility()) + VISIT_BOOST
					* Math.sqrt(Math.log(node.parent.visits) / node.visits);
	}

	private void expand(TreeNode node) throws MoveDefinitionException,
			TransitionDefinitionException {
		// do not expand a terminal state
		if (getStateMachine().isTerminal(node.state))
			return;

		if (node.maxNode) {
			// then player decides on a move
			List<Move> actions = getStateMachine().getLegalMoves(node.state,
					getRole());
			for (int i = 0; i < actions.size(); i++) {
				TreeNode newnode = new TreeNode(node.state, node,
						actions.get(i));
				node.children.add(newnode);
			}
		} else {
			// then the opponents decide on a move, given the player's move
			List<List<Move>> jointMoves = getStateMachine().getLegalJointMoves(
					node.state, getRole(), node.ourMove);
			for (int i = 0; i < jointMoves.size(); i++) {
				List<Move> jointMove = jointMoves.get(i);
				MachineState nextState = getStateMachine().getNextState(
						node.state, jointMove);
				TreeNode newnode = new TreeNode(nextState, node);
				node.children.add(newnode);
			}
		}
	}

	private void backpropagate(TreeNode node, int score) {
		node.visits = node.visits + 1;
		node.utility = node.utility + score;
		if (node.parent != null) {
			backpropagate(node.parent, score);
		}
	}

	private double depthChargeAvg(MachineState state, int count, long timeout)
			throws GoalDefinitionException, MoveDefinitionException,
			TransitionDefinitionException {
		double total = 0.0;
		for (int i = 0; i < count; i++) {
			total = total + (double) depthCharge(state, timeout);
		}
		return total / (double) count;
	}

	private int depthCharge(MachineState state, long timeout)
			throws GoalDefinitionException, MoveDefinitionException,
			TransitionDefinitionException {
		if (getStateMachine().isTerminal(state))
			return getStateMachine().getGoal(state, getRole());
		// Count incomplete searches pessimistically as 0
		if (System.currentTimeMillis() > timeout - OFFSET)
			return 0;
		List<List<Move>> jointMoves = getStateMachine().getLegalJointMoves(
				state);
		List<Move> jointMove = jointMoves.get((int) (Math.random() * jointMoves
				.size()));
		state = getStateMachine().getNextState(state, jointMove);
		return depthCharge(state, timeout);
	}

	private int depthChargeOther(MachineState state, long timeout, Move ourMove)
			throws MoveDefinitionException, TransitionDefinitionException,
			GoalDefinitionException {
		if (getStateMachine().isTerminal(state))
			return getStateMachine().getGoal(state, getRole());

		List<List<Move>> jointMoves = getStateMachine().getLegalJointMoves(
				state, getRole(), ourMove);
		int worstResult = 100;

		for (int i = 0; i < jointMoves.size(); i++) {
			List<Move> jointMove = jointMoves.get(i);
			MachineState nextState = getStateMachine().getNextState(state,
					jointMove);
			int result = depthCharge(nextState, timeout);
			if (result < worstResult)
				worstResult = result;
			if (result == 0)
				return 0;
		}
		return worstResult;
	}

	@Override
	public void stateMachineStop() {
		// Do nothing.
	}
	/**
	 * Uses a CachedProverStateMachine
	 */
	@Override
	public StateMachine getInitialStateMachine() {
		return new CachedStateMachine(new ProverStateMachine());
	}

	@Override
	public String getName() {
		return "MyMCST";
	}

	@Override
	public DetailPanel getDetailPanel() {
		return new SimpleDetailPanel();
	}

	@Override
	public void preview(Game g, long timeout) throws GamePreviewException {
		// Do nothing.
	}

	@Override
	public void stateMachineAbort() {
		// Do nothing.
	}
}