package org.ggp.base.player.gamer.statemachine.sample;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

/**
 * RandomGamer is a gamer that plays a random legal move.
 *
 */
public final class MyRandomGamer extends SampleGamer
{
	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		long start = System.currentTimeMillis();
		List<Move> moves = getStateMachine().getLegalMoves(getCurrentState(), getRole());
		Random random = new Random();
		Move selection = moves.get(random.nextInt(moves.size()));
		long stop = System.currentTimeMillis();
		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;
	}
}