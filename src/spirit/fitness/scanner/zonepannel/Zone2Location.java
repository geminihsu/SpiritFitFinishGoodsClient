package spirit.fitness.scanner.zonepannel;

import java.awt.Color;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import spirit.fitness.scanner.common.Constrant;
import spirit.fitness.scanner.receving.ItemsPannel;
import spirit.fitness.scanner.zonepannel.Zone1Location.Zone1CodeCallBackFunction;

public class Zone2Location implements ActionListener {

	/**
	 * Create the application.
	 */

	private JButton[] btnZoneCode;
	protected JFrame frame;
	private String items;
	private int assignType;

	public Zone2Location(String list, int type) {
		items = list;
		assignType = type;

		if (assignType != -1)
			initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		//JFrame.setDefaultLookAndFeelDecorated(false);
	    //JDialog.setDefaultLookAndFeelDecorated(false);
		frame = new JFrame("Zone 2 Layout");
		frame.setSize(600, 780);
		frame.setLocationRelativeTo(null);
		frame.setUndecorated (true);
		frame.setResizable(false);
		frame.setVisible(true);
		
		JPanel borderedPanel = new JPanel();

	    //Use any border you want, eg a nice blue one
	    borderedPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Constrant.FRAME_BORDER_BACKGROUN_COLOR));

	    frame.setContentPane(borderedPanel);

		frame.setVisible(true);
		Container cp = frame.getContentPane();
		cp.setLayout(new GridLayout(0, 2));

		btnZoneCode = new JButton[39];
		Font font = new Font("Verdana", Font.BOLD, 18);

		int index = 701;
		for (JButton btn : btnZoneCode) {
			if (index == 891)
				index++;

			btn = new JButton(String.valueOf(index));
			btn.setFont(font);
			btn.addActionListener(this);
			cp.add(btn);

			final String content = String.valueOf(index);
			// btn.addActionListener(this);
			btn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					if (zone2CodeCallBackFunction != null) {
						zone2CodeCallBackFunction.getZoneCode(content);
						
						frame.dispose();
						frame.setVisible(false);

					}else {
						frame.dispose();
						frame.setVisible(false);

						//ItemsPannel window = new ItemsPannel(items, content, assignType);
						//window.frame.setVisible(true);
						ItemsPannel.getInstance(items, content, assignType);
					}
				}
			});

			
			index++;
			if (index % 10 > 2) {
				index = index + 10;
				index = index / 10;
				index = index * 10 + 1;

				if (index == 881 || index == 901)
					index++;
			}

		}
		
		JPanel exitControl = new JPanel();
		exitControl.setLayout(new GridLayout(0, 2));
		JButton exit = new JButton(new AbstractAction("Back"){

			@Override
			public void actionPerformed(ActionEvent e) {
				ItemsPannel.destory();
				frame.dispose();
				frame.setVisible(false);
				
				//ZoneMenu window = new ZoneMenu(items, assignType);
				//window.frame.setVisible(true);
				ZoneMenu.getInstance(items, assignType);
			}
		});
		
		JButton backButton = new JButton(new AbstractAction("Exit") {

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				frame.setVisible(false);
			}
		});
		backButton.setBounds(500,20,50,80);
		backButton.setFont(font);
		backButton.setBackground(Constrant.BUTTON_BACKGROUN_COLOR);
		
		exit.setBounds(0,20,50,50);
		exit.setFont(font);
		exit.setBackground(Constrant.BUTTON_BACKGROUN_COLOR);
		backButton.setBounds(0,20,50,80);
		backButton.setFont(font);
		
		exitControl.add(exit);
		exitControl.add(backButton);
		
		
		exitControl.setBackground(Constrant.TABLE_COLOR);
		frame.getContentPane().setBackground(Constrant.TABLE_COLOR);
		frame.getContentPane().add(exitControl);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				frame.dispose();
				frame.setVisible(false);
			}
		});

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String btn = "";

		if (e.getSource() == btnZoneCode) {

			ItemsPannel window = new ItemsPannel(items, "Return(" + btnZoneCode[0].getText().toString() + ")",
					assignType);
			window.frame.setVisible(true);
		} else if (e.getSource() == btnZoneCode[1]) {
			ItemsPannel window = new ItemsPannel(items, "Return(" + btnZoneCode[1].getText().toString() + ")",
					assignType);
			window.frame.setVisible(true);

		} else if (e.getSource() == btnZoneCode[2]) {
			ItemsPannel window = new ItemsPannel(items, "Return(" + btnZoneCode[2].getText().toString() + ")",
					assignType);
			window.frame.setVisible(true);

		}

		/*
		 * JOptionPane.showMessageDialog(f, "the" + btn,
		 * "problem",JOptionPane.INFORMATION_MESSAGE);
		 */

	}

	// retrieve return code number
	private static Zone2CodeCallBackFunction zone2CodeCallBackFunction;

	public void setZone2CodeCallBackFunction(Zone2CodeCallBackFunction _zone2CodeCallBackFunction) {
		zone2CodeCallBackFunction = _zone2CodeCallBackFunction;

	}

	public Zone2CodeCallBackFunction getZone2CodeCallBackFunction() 
	{
		return zone2CodeCallBackFunction;
	}

	public void clearZone2CodeCallBackFunction() {
		zone2CodeCallBackFunction = null;
	}

	public interface Zone2CodeCallBackFunction {
		public void getZoneCode(String code);

	}

}
