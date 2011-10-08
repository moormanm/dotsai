
import java.awt.AWTException;
import java.awt.Component;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;

public class Utilities {

	public static void copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;

	    
	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}
	
	
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

	public static String popupAskUser(String question, String[] answers,
			String title) {
		int answer = JOptionPane.showOptionDialog(null, question, title, 0,
				JOptionPane.QUESTION_MESSAGE, null, answers, answers[0]);
		// Return null if the user closed the dialog box
		if (answer == JOptionPane.CLOSED_OPTION) {
			return null;
		}

		// Return their selection
		return answers[answer];

	}

	static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	public static String timeStamp() {
		Calendar cal = Calendar.getInstance();
		return sdf.format(cal.getTime());
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
