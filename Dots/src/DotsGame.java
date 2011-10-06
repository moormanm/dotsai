import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;




public class DotsGame extends JFrame{
 
	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new DotsGame();
			}
		});	
	}
	

	final GameState gameState = new GameState();
	final GameBoardPanel gameBoardPanel = new GameBoardPanel(gameState);
	
	JMenuBar makeMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		return menuBar;
	}
	
	void build() {
		setSize(800,600);
		setJMenuBar(makeMenuBar());
		setLayout(new BorderLayout());
		JPanel tmp = new JPanel();
		tmp.add(gameBoardPanel);
		add(tmp, BorderLayout.CENTER);
		add(new JLabel("Dots"), BorderLayout.NORTH);
		
	}
	
	public DotsGame() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		build();
		gameBoardPanel.startNewGame();

		setVisible(true);
		
	}
	
	
}
