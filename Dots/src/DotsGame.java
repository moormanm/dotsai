import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
	
	void makeDifficultyHandler(String name, final int level, final JMenu parent) {
		JMenuItem menuItem = new JMenuItem(name);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			  gameBoardPanel.startNewGame(level);
			}
		});
		parent.add(menuItem);
		
	}

	JMenuBar makeMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu newGameMenu = new JMenu("New Game");
		
		makeDifficultyHandler("Tourist", 1, newGameMenu);
		makeDifficultyHandler("Easy", 2, newGameMenu);
		makeDifficultyHandler("Medium", 3, newGameMenu);
		makeDifficultyHandler("Hard", 4, newGameMenu);
		makeDifficultyHandler("Serious", 5, newGameMenu);
		makeDifficultyHandler("Mental", 6, newGameMenu);
		makeDifficultyHandler("Jedi Master", 8, newGameMenu);
		
		menuBar.add(newGameMenu);
		return menuBar;
	}
	
	void build() {
		setSize(800,600);
		setJMenuBar(makeMenuBar());
		setLayout(new BorderLayout());
		JPanel tmp = new JPanel();
		tmp.add(gameBoardPanel);
		add(tmp, BorderLayout.CENTER);
	}
	
	public DotsGame() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		build();
		gameBoardPanel.startNewGame(8);

		setVisible(true);
		
	}
	
	
}
