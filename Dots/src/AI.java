import java.io.File;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Vector;



public class AI {

	// default depth
	final public static int HARD = 4;
	public static int maxDepth = HARD;
    final int MAX_NODES = 1000000;

    static Connection conn;
    static { 
    	try {
			conn = getDBConn();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    static long theEstimatedCurrentTime = System.currentTimeMillis();
    static long startTime;
    static Thread timeKeeper = new Thread() {
    	public void run() {
    	  while(true) {
    		  theEstimatedCurrentTime = System.currentTimeMillis();
    		  try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	  }
    	}
    };
    
    static { 
    	timeKeeper.start();
    }
    
    

    public static boolean timeIsUp() {
    	if(theEstimatedCurrentTime > startTime + 40000) {
    		System.out.println("TIME IS UP!!!!");
    		return true;
    	}
    	
    	return false;
    	
    }
    
	public static HashMap<BitSet, LinkedList<Segment>> savedStates = new HashMap<BitSet,LinkedList<Segment> >();
	
	public static void reset() {
		savedStates.clear();
	}
	
	public static Connection getDBConn() throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		
		//If the file doesn't already exist
		File f = new File("memory.db");
		if(f.exists()) {
			Connection conn =
			      DriverManager.getConnection("jdbc:sqlite:memory.db");
			  return conn;
		}
		
	    //Make the DB
		Connection conn =
		      DriverManager.getConnection("jdbc:sqlite:memory.db");
		
		Statement stat = conn.createStatement();
		stat.executeUpdate("CREATE TABLE states (id INTEGER PRIMARY KEY AUTOINCREMENT, state BLOB);");
		stat.executeUpdate("CREATE TABLE moves (stateId BIG INT, moves text, confidence INTEGER);");
		stat.executeUpdate("CREATE INDEX stateidx ON moves(stateID);");
		return conn;
		
	}
	
	
	public static void commit(GameState.Player winner) {
		int confidence = 0;
		//If human won
		if( winner == GameState.Player.P1) {
			confidence = -1;
		}
		//If computer won
		else if (winner == GameState.Player.P2) {
			confidence = 1;
		}
		//If draw
		else {
			confidence = 0;
		}
		
		//Get each state and moves
		boolean stateExists = false;
		long stateId = 0;
		for(BitSet bs : savedStates.keySet()) {
			LinkedList<Segment> moves = savedStates.get(bs);
			//Lookup the state to see if it already is entered
			String query = "SELECT id FROM states where state = ?;";
			try {
				PreparedStatement prep = conn.prepareStatement(query);
				prep.setBytes(1, toByteArray(bs));
				ResultSet rs = prep.executeQuery();
				while (rs.next()){
				    stateExists = true;
				    stateId = rs.getInt(1);
				}
				prep.close();
				rs.close();
				
				//If state doesn't exist, need to insert it into the db
				if(!stateExists) {
					prep = conn.prepareStatement("INSERT INTO states (state) VALUES (?);");
					prep.setBytes(1, toByteArray(bs));
					int affectedRows = prep.executeUpdate();
					if( affectedRows == 0 ) {
						throw new SQLException("Creating state failed, no rows affected.");
					}
					ResultSet generatedKeys = prep.getGeneratedKeys();
					while(generatedKeys.next()) {
						stateId = generatedKeys.getLong(1);
					}
					prep.close();
					generatedKeys.close();
				}
				
				//Now there is a state id. Lookup the move
				boolean moveExists = false;
				int existingConfidence = 0;
				query = "SELECT confidence from moves where stateId = ? and moves = ?";
				prep = conn.prepareStatement(query);
				prep.setLong(1,stateId);
				prep.setString(2,moves.toString());
				rs = prep.executeQuery();
				while (rs.next()){
				    moveExists = true;
				    existingConfidence = rs.getInt(1);
				}
				prep.close();
				rs.close();
				
				//If move already exists, update the confidence
				if(moveExists) {
					confidence += existingConfidence;
					query = "UPDATE moves SET confidence = ? WHERE stateId = ? AND moves = ?";
					prep = conn.prepareStatement(query);
					System.out.println("Updating " + confidence + ", " + stateId + " , " + moves.toString());
					prep.setInt(1,confidence);
					prep.setLong(2,stateId);
					prep.setString(3,moves.toString());
					int affectedRows = prep.executeUpdate();
					if( affectedRows == 0 ) {
						throw new SQLException("Updateing move failed, no rows affected.");
					}
					prep.close();
					rs.close();
				}
				//New move, insert the confidence
				else {
					query = "INSERT INTO moves VALUES(?, ? , ?)";
					prep = conn.prepareStatement(query);
					prep.setLong(1,stateId);
					prep.setString(2,moves.toString());
					prep.setInt(3,confidence);
					int affectedRows = prep.executeUpdate();
					if( affectedRows == 0 ) {
						throw new SQLException("Inserting move failed, no rows affected.");
					}
					prep.close();
					rs.close();
				}
				

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	

	
	// Returns a bitset containing the values in bytes.
	// The byte-ordering of bytes must be big-endian which means the most significant bit is in element 0.
	public static BitSet fromByteArray(byte[] bytes) {
	    BitSet bits = new BitSet();
	    for (int i=0; i<bytes.length*8; i++) {
	        if ((bytes[bytes.length-i/8-1]&(1<<(i%8))) > 0) {
	            bits.set(i);
	        }
	    }
	    return bits;
	}

	// Returns a byte array of at least length 1.
	// The most significant bit in the result is guaranteed not to be a 1
	// (since BitSet does not support sign extension).
	// The byte-ordering of the result is big-endian which means the most significant bit is in element 0.
	// The bit at index 0 of the bit set is assumed to be the least significant bit.
	public static byte[] toByteArray(BitSet bits) {
	    byte[] bytes = new byte[bits.length()/8+1];
	    for (int i=0; i<bits.length(); i++) {
	        if (bits.get(i)) {
	            bytes[bytes.length-i/8-1] |= 1<<(i%8);
	        }
	    }
	    return bytes;
	}
	
	AI(GameState gs) {
		this.gs = gs;
	}

	
	GameState gs;

	Random rand = new Random();
	GameState scratchPad = new GameState();
	GameState scratchPad2 = new GameState();

        int getMaxDepth() {
        
          
          Turn t1 = new Turn(this, GameState.Player.P1, null);
          int depth = 1;
          int nodes = 0;
          LinkedList<Turn> list = t1.possibleTurnsForPlayer(GameState.Player.P1);
         
          int branches = 1;

          
          while(true) {
             //Caculate the nodes at this level
             branches = list.size() * branches;
             nodes += branches;

             if( nodes >= MAX_NODES ) { 
               break;
             }
             
             if(list.size() == 0) {
            	 return Integer.MAX_VALUE;
             }
             //go deeper
            
             list = list.get(rand.nextInt(list.size())).possibleTurnsForPlayer(GameState.Player.P1);
             depth++;
          }
          return depth;
          

        }
	// tells the AI to take a turn on gamestate
	void takeTurn(GameState.Player p) {

		TurnContainer bestTurnContainer = new TurnContainer();

		int ddepth =  Math.max(getMaxDepth(), 3);
		ddepth =  Math.max(getMaxDepth(), ddepth);
		ddepth =  Math.max(getMaxDepth(), ddepth);
		ddepth =  Math.max(getMaxDepth(), ddepth);
		ddepth =  Math.max(getMaxDepth(), ddepth);
		ddepth =  Math.max(getMaxDepth(), ddepth);
		ddepth =  Math.max(getMaxDepth(), ddepth);
		ddepth =  Math.max(getMaxDepth(), ddepth);
		ddepth =  Math.max(getMaxDepth(), ddepth);
		ddepth =  Math.max(getMaxDepth(), ddepth);
		
		System.out.println("Max depth is " + getMaxDepth());
		
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


		HashMap<Turn, Integer> results = new HashMap<Turn,Integer>();
		
		int c = 0;
		if (list.size() < GameState.dimX ) {
			c = 8;
		} else if(list.size() < GameState.dimX + GameState.dimX/2) {
			c = 8;
		} else if (list.size() < GameState.dimX * GameState.dimY / 2
				&& maxDepth == HARD) {
			c = 3;
		}
		//C only applies to hard mode
		if(maxDepth < HARD) {
			c = 0;
		}

		
		
		//Start time
		AI.startTime = System.currentTimeMillis();
		
		
		
		int bestScore = Integer.MIN_VALUE;
		for (Turn t : list) {

			int tmp = -alphabeta(t, GameState.otherPlayer(p), Math.max(ddepth - 1,1),
					Integer.MIN_VALUE, Integer.MAX_VALUE, bestTurnContainer);
			
		    System.out.println("Turn : " + bestTurnContainer.t + " is " + tmp );
		    //System.out.println("Turn : " + tmpt + " is " + tmp2 );
			results.put(bestTurnContainer.t, tmp);

			if (tmp > max) {
				bestTurn = bestTurnContainer.t;
				max = tmp;
			}
		}

		int i = 0;
		for(Turn t : results.keySet()){
			if(results.get(t) == max) {
				System.out.println("Candidate turn : " + t);
			}
		}
		
	    //Perform post processing on minimax result
		bestTurn = postProcess(results, max, bestTurn);
		
		//Save the gamestate and turn info
        BitSet savedState = gs.asBitSet();
		GameState test2 = gs.fromBitSet(savedState);
		assert(test2.equals(gs));
		assert(gs.asBitSet().equals( fromByteArray( toByteArray(gs.asBitSet()))));
		Turn tmp = bestTurn;
		while(tmp.parent != null) {
			tmp = tmp.parent;
		}
        savedStates.put(savedState, tmp.moves);
		
		System.out.println("Best Turn :" + bestTurn + " is  : " + max);
		//System.out.println("C is : " + c);
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


		if(AI.timeIsUp()) {
			return alpha;
		}
		
		if (depth == 0) {
			bestTurnContainer.t = t;
			return t.eval(p);
		}

		LinkedList<Turn> children = t.possibleTurnsForPlayer(p);
		if (children.size() == 0) {
			bestTurnContainer.t = t;
			return t.eval(p);
		}
		
		Turn bestTurn = null;


		int bestScore = Integer.MIN_VALUE;
		for (Turn child : children) {
			int moveScore = -alphabeta(child, GameState.otherPlayer(p), depth - 1, -beta,
					-alpha, bestTurnContainer);
		
			if(moveScore > bestScore) {
				bestScore = moveScore;
				bestTurn = bestTurnContainer.t;
			}
				
			if(bestScore > alpha) {
				alpha = bestScore;
			}
			
			if (alpha >= beta) {
				//System.out.println("PRUNING T : " + t  + " : " + alpha + " : " + beta);   
					
				// Prune branch
				return alpha;
			}
			
		}
			
		bestTurnContainer.t = bestTurn;
		return bestScore;
	}
	// Alpha beta search method. Like minimax, but more efficient
	int alphabeta2(Turn t, GameState.Player p, int depth, int alpha, int beta,
			TurnContainer bestTurnContainer) {


		if(AI.timeIsUp()) {
			return alpha;
		}
		
		if (depth == 0) {
			bestTurnContainer.t = t;
			return t.eval(p);
		}

		LinkedList<Turn> children = t.possibleTurnsForPlayer(p);
		if (children.size() == 0) {
			bestTurnContainer.t = t;
			return t.eval(p);
		}
		



		int tmp = Integer.MIN_VALUE;
		outer: for (Turn child : children) {
			tmp = -alphabeta(child, GameState.otherPlayer(p), depth - 1, -beta,
					-alpha, bestTurnContainer);
		
			alpha = Math.max(alpha, tmp);
			if (alpha >= beta) {
				//System.out.println("Value of t : " + child + " : " + tmp + "  : alpha : " + alpha + " : beta :" + beta);
				// Prune branch
				//break outer;
			}
			
		}
			
		return alpha;
	}

	Turn randomlyChooseTopTurn(HashMap<Turn,Integer> map) {
		LinkedList<Turn> bestTurns = new LinkedList<Turn>();
		int max = Integer.MIN_VALUE;
		for(Integer v : map.values()) {
			max = Math.max(v,max);
		}
		for(Turn t : map.keySet()) {
			if(map.get(t) == max) {
				bestTurns.add(t);
			}
		}
		return bestTurns.get(rand.nextInt(bestTurns.size()));
	}
	
	Turn postProcess(HashMap<Turn,Integer> scores, int max,
			Turn bestTurnBefore) {
		
		//Randomly choose one of the top turns
		Turn bestTurn = randomlyChooseTopTurn(scores);
		
		
		//Lookup in long term memory and check to see if this state is familiar
		PreparedStatement prep;
		String query = "SELECT moves, confidence FROM moves WHERE stateId in (SELECT id FROM states where state = ?);";
		try {
			prep = conn.prepareStatement(query);
			prep.setBytes(1, toByteArray(gs.asBitSet()));
			
			System.out.println(gs.fromBitSet(fromByteArray(toByteArray(gs.asBitSet()))));
			System.out.println(prep.toString());
			ResultSet rs = prep.executeQuery();
			HashMap<String, Integer> dbScores = new HashMap<String, Integer>();
			boolean foundIt = false;
			while(rs.next()) {
				foundIt = true;
				System.out.println("GOT RESULT: " + rs.getString(1));
				dbScores.put(rs.getString(1), rs.getInt(2));
			}
			rs.close();
			prep.close();
			
			if(!foundIt) {
				//If no hit in longterm memory, just return the random
				return bestTurn;
			}
			//Sort the turns in turnScores
			class TurnScorePair implements Comparable<TurnScorePair>{
				final Turn t;
				final int score;
				TurnScorePair(Turn t, int score) {
					this.t = t; 
					this.score = score;
				}
				@Override
				public int compareTo(TurnScorePair b) {
					Integer a = score;
					return a.compareTo(b.score);
				}
			}
			
			//Iterate over best turns..
			Vector<TurnScorePair> turnScores = new Vector<TurnScorePair>();
			
			for (Turn top : scores.keySet()) {
				Turn t = top;
				while(t.parent != null) {
					t = t.parent;
				}
				//If this is in the database already, influence the minimax value
				if(dbScores.containsKey(t.moves.toString())) {
					System.out.println("Found move in DB : " + t.moves.toString() + " : " 
							+ dbScores.get(t.moves.toString()));
					turnScores.add(new TurnScorePair(top, scores.get(top) + dbScores.get(t.moves.toString())));
				}
				
				//Otherwise use the standard minimax value
				else {
					turnScores.add(new TurnScorePair(top, scores.get(top)));
				}
			}
			
			Collections.sort(turnScores, Collections.reverseOrder());
			System.out.println("Best turn is " + turnScores.get(0).t);
			System.out.println("Best Score is " + turnScores.get(0).score);
			//Return the highest ranked score
			return turnScores.get(0).t;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		
		
			

		return bestTurn;
	}

	public static String getHexString(byte[] b) throws Exception {
		  String result = "";
		  for (int i=0; i < b.length; i++) {
		    result +=
		          Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
		  }
		  return result;
		}
}
