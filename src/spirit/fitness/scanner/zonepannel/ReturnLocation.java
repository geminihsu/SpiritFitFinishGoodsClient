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
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import spirit.fitness.scanner.common.Constrant;
import spirit.fitness.scanner.model.Containerbean;
import spirit.fitness.scanner.receving.ItemsPannel;
import spirit.fitness.scanner.zonepannel.Zone1Location.Zone1CodeCallBackFunction;
import spirit.fitness.scanner.zonepannel.Zone2Location.Zone2CodeCallBackFunction;

public class ReturnLocation implements ActionListener {

	/**
	 * Create the application.
	 */

	private JButton btnReturn1, btnReturn2, btnReturn3;
	protected JFrame frame;
	private String items;
	private int assignType = 0;
	private List<Containerbean> containers;

	public ReturnLocation(String content, int type) {
		items = content;
		assignType = type;
		if (assignType != -1)
			initialize();
	}

	public ReturnLocation(List<Containerbean> _containers, String content, int type) {
		items = content;
		assignType = type;
		containers = _containers;
		if (assignType != -1)
			initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		// JFrame.setDefaultLookAndFeelDecorated(false);
		// JDialog.setDefaultLookAndFeelDecorated(false);
		frame = new JFrame("FG Inventory App");
		frame.setSize(600, 300);
		frame.setLocationRelativeTo(null);
		frame.setLocationRelativeTo(null);
		frame.setUndecorated(true);
		frame.setResizable(false);
		frame.setVisible(true);

		JPanel borderedPanel = new JPanel();

		// Use any border you want, eg a nice blue one
		borderedPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Constrant.FRAME_BORDER_BACKGROUN_COLOR));

		frame.setContentPane(borderedPanel);

		frame.setVisible(true);
		Container cp = frame.getContentPane();
		cp.setLayout(new GridLayout(0, 1));

		Font font = new Font("Verdana", Font.BOLD, 25);
		btnReturn1 = new JButton("881");
		btnReturn1.setFont(font);
		btnReturn2 = new JButton("891");
		btnReturn2.setFont(font);
		btnReturn3 = new JButton("901");
		btnReturn3.setFont(font);

		btnReturn1.setMnemonic('O');
		btnReturn2.setMnemonic('C');
		btnReturn3.setMnemonic('Q');
		btnReturn1.addActionListener(this);
		btnReturn2.addActionListener(this);
		btnReturn3.addActionListener(this);
		cp.add(btnReturn1);
		cp.add(btnReturn2);
		cp.add(btnReturn3);

		JPanel exitControl = new JPanel();
		exitControl.setLayout(new GridLayout(0, 5));
		JButton exit = new JButton(new AbstractAction("Back") {

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				frame.setVisible(false);

				// ZoneMenu window = new ZoneMenu(items, assignType);
				// window.frame.setVisible(true);
				if (containers == null)
					ZoneMenu.getInstance(items, assignType);
				else
					ZoneMenu.getInstance(containers, items, assignType);
			}
		});

		JButton backButton = new JButton(new AbstractAction("Exit") {

			@Override
			public void actionPerformed(ActionEvent e) {
				ItemsPannel.destory();
				frame.dispose();
				frame.setVisible(false);
			}
		});
		backButton.setBounds(500, 20, 50, 50);
		backButton.setFont(font);
		backButton.setBackground(Constrant.BUTTON_BACKGROUN_COLOR);

		exit.setBounds(0, 20, 50, 50);
		exit.setFont(font);
		exit.setBackground(Constrant.BUTTON_BACKGROUN_COLOR);
		backButton.setBounds(0, 20, 50, 50);
		backButton.setFont(font);

		exitControl.add(exit);
		exitControl.add(backButton);

		exitControl.setBackground(Constrant.TABLE_COLOR);
		frame.getContentPane().setBackground(Constrant.TABLE_COLOR);
		frame.getContentPane().add(exitControl);

		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				int result = JOptionPane.showConfirmDialog(frame, "Do you want to close the app?",
						"The app will be close", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (result == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			}
		});

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String btn = "";
		this.frame.setVisible(false);
		this.frame.dispose();

		if (e.getSource() == btnReturn1) {
			if (zoneCodeReturnCallBackFunction != null) {
				zoneCodeReturnCallBackFunction.getZoneCode(btnReturn1.getText().toString());
			} else {
				// ItemsPannel window = new ItemsPannel(items, btnReturn1.getText().toString(),
				// assignType);
				// window.frame.setVisible(true);

				if (containers == null)
					ItemsPannel.getInstance(items, btnReturn1.getText().toString(), assignType);
				else
					ItemsPannel.getInstance(containers, items, btnReturn1.getText().toString(), assignType);
			}
		} else if (e.getSource() == btnReturn2) {
			if (zoneCodeReturnCallBackFunction != null) {
				zoneCodeReturnCallBackFunction.getZoneCode(btnReturn2.getText().toString());
			} else {
				// ItemsPannel window = new ItemsPannel(items, btnReturn2.getText().toString(),
				// assignType);
				// window.frame.setVisible(true);
				if (containers == null)
					ItemsPannel.getInstance(items, btnReturn2.getText().toString(), assignType);
				else
					ItemsPannel.getInstance(containers, items, btnReturn2.getText().toString(), assignType);
			}

		} else if (e.getSource() == btnReturn3) {
			if (zoneCodeReturnCallBackFunction != null) {
				zoneCodeReturnCallBackFunction.getZoneCode(btnReturn3.getText().toString());
			} else {
				// ItemsPannel window = new ItemsPannel(items, btnReturn3.getText().toString(),
				// assignType);
				// window.frame.setVisible(true);
				if (containers == null)
					ItemsPannel.getInstance(items, btnReturn3.getText().toString(), assignType);
				else
					ItemsPannel.getInstance(containers, items, btnReturn3.getText().toString(), assignType);
			}
		}

		/*
		 * JOptionPane.showMessageDialog(f, "the" + btn,
		 * "problem",JOptionPane.INFORMATION_MESSAGE);
		 */

	}

	// retrieve return code number
	private static ZoneCodeReturnCallBackFunction zoneCodeReturnCallBackFunction;

	public void setZoneCodeReturnCallBackFunction(ZoneCodeReturnCallBackFunction _zoneCodeReturnCallBackFunction) {
		zoneCodeReturnCallBackFunction = _zoneCodeReturnCallBackFunction;

	}

	public ZoneCodeReturnCallBackFunction getZoneCodeReturnCallBackFunction() {
		return zoneCodeReturnCallBackFunction;
	}

	public void clearZoneCodeReturnCallBackFunction() {
		zoneCodeReturnCallBackFunction = null;
	}

	public interface ZoneCodeReturnCallBackFunction {
		public void getZoneCode(String code);

	}

}
