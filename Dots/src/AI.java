import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

public class AI {

	// default depth
	public static int maxDepth = 3;

	AI(GameState gs) {
		this.gs = gs;
	}

	GameState gs;

	GameState scratchPad = new GameState();
	GameState scratchPad2 = new GameState();

	// tells the AI to take a turn on gamestate
	void takeTurn(GameState.Player p) {

		TurnContainer bestTurnContainer = new TurnContainer();

		// System.out.println("fragBefore : " + fragBefore);
		Turn t1 = new Turn(this, p, null);
		t1.isRoot = true;
		LinkedList<Turn> list = t1.possibleTurnsForPlayer(p);

		System.out.println("Branching factor is : " + list.size());
		
		// Shuffle the list of possible turns so it appears that the AI is more
		// human like
		Collections.shuffle(list);

		int max = Integer.MIN_VALUE;
		Turn bestTurn = null;
		if (list.size() == 0) {
			System.out.println("No moves!");
			return;
		}
		Vector<Integer> results = new Vector<Integer>();
		LinkedList<Turn> evaledTurns = new LinkedList<Turn>();

		int c = 0;
		if (list.size() < GameState.dimX ) {
			c = 9;
		} else if(list.size() < GameState.dimX + GameState.dimX/2) {
			c = 5;
		} else if (list.size() < GameState.dimX + GameState.dimY
				&& maxDepth == 3) {
			c = 2;
		}
		else if(list.size() >  GameState.dimX * GameState.dimY) {
			c = -1;
		}
		//C only applies to hard mode
		if(maxDepth < 3) {
			c = 0;
		}

		
		for (Turn t : list) {

			int tmp = -alphabeta(t, GameState.otherPlayer(p), maxDepth - 1 + c,
					Integer.MIN_VALUE, Integer.MAX_VALUE, bestTurnContainer);
		//	System.out.println("Turn : " + bestTurnContainer.t + " is " + tmp );
			results.add(tmp);
			evaledTurns.add(bestTurnContainer.t);

			if (tmp > max) {
				bestTurn = bestTurnContainer.t;
				max = tmp;
			}
		}

		// If this is not easy mode, do post processing to further refine move
		if (maxDepth != 1) {
			bestTurn = postProcess(evaledTurns, results, max, bestTurn);
		}

		System.out.println("Best Turn :" + bestTurn + " is  : " + max);
		System.out.println("C is : " + c);
		while (bestTurn.parent != null) {
			bestTurn = bestTurn.parent;
		}
		for (Segment s : bestTurn.moves) {
			gs.doMove(s, p);
		}

	}

	class TurnContainer {
		public Turn t;
	}

	// Alpha beta search method. Like minimax, but more efficient
	int alphabeta(Turn t, GameState.Player p, int depth, int alpha, int beta,
			TurnContainer bestTurnContainer) {

		if (depth == 0) {
			bestTurnContainer.t = t;
			return t.eval(p);
		}

		LinkedList<Turn> children = t.possibleTurnsForPlayer(p);
		if (children.size() == 0) {
			bestTurnContainer.t = t;
			return t.eval(p);
		}

		int tmp = 0;
		outer: for (Turn child : children) {
			tmp = -alphabeta(child, GameState.otherPlayer(p), depth - 1, -beta,
					-alpha, bestTurnContainer);
			alpha = Math.max(alpha, tmp);
			if (alpha >= beta) {
				// System.out.println("Pruning branch");
				// Prune branch
				break outer;
			}
		}
		return alpha;
	}

	Turn postProcess(LinkedList<Turn> turnList, Vector<Integer> evals, int max,
			Turn bestTurnBefore) {
		int fragBefore = GameState.getNumFragments(gs);
		int i = 0;
		Turn bestTurn = bestTurnBefore;
		int maxFrag = Integer.MIN_VALUE;
		LinkedList<Turn> turnsThatTouchLastMove = new LinkedList<Turn>();
		Vector<Integer> secondLevelEvals = new Vector<Integer>();
		Iterator<Turn> iter;

		// Get the set of best turns
		i = 0;
		LinkedList<Turn> bestTurns = new LinkedList<Turn>();
		for (Turn t : turnList) {
			if (max == evals.get(i++)) {
				bestTurns.add(t);
			}
		}

		// Of the best turns, evaluate the second level of the tree. This is
		// player 1's gain from this turn.
		// Aim is to minimize this.
		if(maxDepth > 2) {
		
		int minVal = Integer.MAX_VALUE;
		if (maxDepth >= 2) {
			for (Turn t : bestTurns) {
				while (t.parent != null && t.parent.parent != null) {
					t = t.parent;
				}
				int tmp = t.eval(t.p);
				secondLevelEvals.add(tmp);
				if (tmp < minVal) {
					minVal = tmp;
				}
			}
		}

		// Filter out the new best turns
		iter = bestTurns.iterator();
		i = 0;
		while (iter.hasNext()) {
			Turn t = iter.next();
			int val = secondLevelEvals.get(i++);

			if (val != minVal) {
				System.out
						.println("Filtering best moves based on 2nd level evaluation");

				iter.remove();
			}
		}
		}
		

		
		// discriminate best moves based on fragmentation factor if this is
			secondLevelEvals.clear();
			for (Turn t : bestTurns) {
				while (t.parent != null) {
					t = t.parent;
				}

				if (GameState.segmentWouldClaimUnit(gs, t.moves.get(0))) {
					// Dont use segments that would claim a unit
					secondLevelEvals.add(Integer.MIN_VALUE);
					continue;
				}

				gs.copyTo(scratchPad);
				Turn.applyTurnsToGameState(scratchPad, t);

				int fragAfter = GameState.getNumFragments(scratchPad)
						- fragBefore;
				secondLevelEvals.add(fragAfter);
				if (fragAfter > maxFrag && fragAfter > 0) {
					maxFrag = fragAfter;
				}
			}

			iter = bestTurns.iterator();
			i = 0;
			while (iter.hasNext()) {
				Turn t = iter.next();
				int val = secondLevelEvals.get(i++);

				if (val != maxFrag) {
					iter.remove();
				}
			}


		// Select one of the best remaining turns randomly
		if (bestTurns.size() > 0) {
				bestTurn = bestTurns.get(0);
		}

		return bestTurn;
	}

}
