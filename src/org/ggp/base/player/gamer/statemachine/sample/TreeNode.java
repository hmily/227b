package org.ggp.base.player.gamer.statemachine.sample;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;




public class TreeNode {
	/* static Random r = new Random();
	static int nActions = 5;
	static double epsilon = 1e-6;

	TreeNode[] children;
	double nVisits, totValue; */

	public MachineState state;
	public double utility; // This is a sum of all depth charges performed under
							// this node. Must use getAverageUtility to get
							// estimated score
	public int visits;
	public TreeNode parent;
	public List<TreeNode> children;

	public boolean maxNode; // true if a max node, false otherwise
	public Move ourMove; // if maxNode == false, then we've acted, but opponents
							// haven't

	public boolean dummyNode = false; // true if there's only 1 move out of
										// here. (i.e. turn-based game and not
										// the player's turn)
	public int subIndex = -1; // starts at -1. First used by
								// NobodyGamerPropNetFactorized

	public int depth = 0; // unset to begin with. First used by NobodyGamerLDD

	public TreeNode() {
		state = null;
		utility = 0.0;
		visits = 0;
		parent = null;
		children = new ArrayList<TreeNode>();

		maxNode = false;
		ourMove = null;
	}

	public TreeNode(MachineState state, TreeNode parent, Move ourMove) {
		this.state = state;
		this.parent = parent;
		this.utility = 0.0;
		this.visits = 0;
		this.children = new ArrayList<TreeNode>();

		this.maxNode = false;
		this.ourMove = ourMove;
	}

	public TreeNode(MachineState state, TreeNode parent) {
		this.state = state;
		this.parent = parent;
		this.utility = 0.0;
		this.visits = 0;
		this.children = new ArrayList<TreeNode>();

		this.maxNode = true;
		this.ourMove = null;
	}

	public double getAverageUtility() {
		if (visits == 0)
			return 0;
		else {
			return utility / visits;
		}
	}

/*	public void selectAction() {
		List<TreeNode> visited = new LinkedList<TreeNode>();
		TreeNode cur = this;
		visited.add(this);
		while (!cur.isLeaf()) {
			cur = cur.select();
			// System.out.println("Adding: " + cur);
			visited.add(cur);
		}
		cur.expand();
		TreeNode newNode = cur.select();
		visited.add(newNode);
		double value = rollOut(newNode);
		for (TreeNode node : visited) {
			// would need extra logic for n-player game
			// System.out.println(node);
			node.updateStats(value);
		}
	}

	public void expand() {
		children = new TreeNode[nActions];
		for (int i = 0; i < nActions; i++) {
			children[i] = new TreeNode();
		}
	}

	private TreeNode select() {
		TreeNode selected = null;
		double bestValue = Double.MIN_VALUE;
		for (TreeNode c : children) {
			double uctValue = c.totValue / (c.nVisits + epsilon)
					+ Math.sqrt(Math.log(nVisits + 1) / (c.nVisits + epsilon))
					+ r.nextDouble() * epsilon;
			// small random number to break ties randomly in unexpanded nodes
			// System.out.println("UCT value = " + uctValue);
			if (uctValue > bestValue) {
				selected = c;
				bestValue = uctValue;
			}
		}
		// System.out.println("Returning: " + selected);
		return selected;
	}

	public boolean isLeaf() {
		return children == null;
	}

	public double rollOut(TreeNode tn) {
		// ultimately a roll out will end in some value
		// assume for now that it ends in a win or a loss
		// and just return this at random
		return r.nextInt(2);
	}

	public void updateStats(double value) {
		nVisits++;
		totValue += value;
	}

	public int arity() {
		return children == null ? 0 : children.length;
	}*/
}