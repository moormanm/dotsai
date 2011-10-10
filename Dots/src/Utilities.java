

import javax.swing.BorderFactory;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;


public class Utilities {



	
	
	public static void setSwingFont(javax.swing.plaf.FontUIResource f) {
		java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource)
				UIManager.put(key, f);
		}
	}

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
