

import javax.swing.BorderFactory;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


public class Utilities {


	public static String newLine = System.getProperty("line.separator");

	public static void showError(String error) {
		JOptionPane.showMessageDialog(null, error, "Error",
				JOptionPane.ERROR_MESSAGE);
	}

	public static void showInfo(String info, String title) {
		JOptionPane.showMessageDialog(null, info, title,
				JOptionPane.INFORMATION_MESSAGE);
	}

	public static void standardBorder(JPanel jp, String name) {
		jp.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), name, TitledBorder.LEFT,
				TitledBorder.TOP, Defaults.TITLE_FONT));

	}
	public static JLabel standardLabel(String name) {
		JLabel tmp = new JLabel(name);
		tmp.setFont(Defaults.LABEL_FONT);
		return tmp;
	}
	


}
