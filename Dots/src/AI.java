import java.util.Collections;
import java.util.LinkedList;




public class AI {


	//default depth
	public static int maxDepth = 7;
	
	AI(GameState gs) {
		this.gs = gs;
	}
	GameState gs;
	
	GameState scratchPad = new GameState();
	GameState scratchPad2 = new GameState();

	
	Turn iterativeDeepeningSearch(GameState.Player p, int startingDepth, int maxDepth, long maxTimeMillis) {
		//Make the root turn
		Turn t1 = new Turn(this, p, null);
		t1.isRoot = true;
		LinkedList<Turn> list = t1.possibleTurnsForPlayer(p);
		
		//Shuffle the list of possible turns so it appears that the AI is more human like
		Collections.shuffle(list);
		int max = Integer.MIN_VALUE;
		Turn bestTurn = null;
		
		//Get current time
		long startingTime = System.currentTimeMillis();
		
		
		if(list.size() == 0) {
			return null;
		}
		for(Turn t : list) {
		   int tmp =  -alphabeta(t, GameState.otherPlayer(p), maxDepth-1, Integer.MIN_VALUE, Integer.MAX_VALUE);
		   //Uncomment this line to do minimax instead of alpha beta
		   //int tmp =  -minimax(t, GameState.Player.P1, maxDepth-1);
		   if(tmp > max) {
			   bestTurn = t;
			   max = tmp; 
		   }
		}
		
		
		return null;
	}
	
	//tells the AI to take a turn on gamestate
	void takeTurn(GameState.Player p) {
		Turn t1 = new Turn(this, p, null);
		t1.isRoot = true;
		LinkedList<Turn> list = t1.possibleTurnsForPlayer(p);
		
		//Shuffle the list of possible turns so it appears that the AI is more human like
		//Collections.shuffle(list);

		int max = Integer.MIN_VALUE;
		Turn bestTurn = null;
		if(list.size() == 0) {
			System.out.println("No moves!");
			return;
		}
		for(Turn t : list) {
		   int tmp =  -alphabeta(t, GameState.otherPlayer(p), maxDepth-1, Integer.MIN_VALUE, Integer.MAX_VALUE);
		   System.out.println("Turn :" + t + " is  : " + tmp);
		   //Uncomment this line to do minimax instead of alpha beta
		   //int tmp =  -minimax(t, GameState.Player.P1, maxDepth-1);
		   if(tmp > max) {
			   bestTurn = t;
			   max = tmp; 
		   }
		}
	    
		System.out.println("Best Turn :" + bestTurn + " is  : " + max);
		for(Segment s : bestTurn.moves) {
			gs.doMove(s, p);
		}
		System.out.println("---------------" + Utilities.newLine);
	}
	


	//Alpha beta search method. Like minimax, but more efficient
	int alphabeta(Turn t, GameState.Player p, int depth, int alpha, int beta) {
		if(depth <= 0) {
			return t.eval(p);
		}
		
		LinkedList<Turn> children = t.possibleTurnsForPlayer(p);
		if(children.size() == 0) { 
			return t.eval(p);
		}
		
		int tmp = 0;
		outer:
		for(Turn child : children) {
			tmp = -alphabeta(child, GameState.otherPlayer(p), --depth, -beta, -alpha );
			alpha = Math.max(alpha, tmp);
            if(alpha >= beta) {
            	//System.out.println("Pruning branch");
            	//Prune branch
            	break outer;
            }
		}
		return alpha;
	}
	
	
	//Minimax search method
	int minimax(Turn t, GameState.Player p, int depth) {
		if(depth <= 0) {
			return t.eval(p);
		}
		

		LinkedList<Turn> children = t.possibleTurnsForPlayer(p);
		if(children.size() == 0) { 
			return t.eval(p);
		}
		int maxVal = Integer.MIN_VALUE;
		int tmp = 0;
		for(Turn child : children) {
			tmp = -1 * minimax(child, GameState.otherPlayer(p), --depth);
			if(tmp > maxVal) {
				maxVal = tmp;
			}
		}
		return maxVal;
	}
	
	


}
