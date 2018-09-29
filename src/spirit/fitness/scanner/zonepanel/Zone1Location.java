package spirit.fitness.scanner.zonepanel;

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
import spirit.fitness.scanner.delegate.ItemPanelBaseViewDelegate;
import spirit.fitness.scanner.delegate.moved.ItemPanelMovedViewDelegate;
import spirit.fitness.scanner.delegate.received.ItemPanelReceivedViewDelegate;
import spirit.fitness.scanner.model.Containerbean;
import spirit.fitness.scanner.zonepanel.RTSLocation.ZoneCodeReturnCallBackFunction;

public class Zone1Location{

	/**
	 * Create the application.
	 */

	private JButton[] btnZoneCode;
	public JFrame frame;
	private String items;
	private int assignType = 0;
	private List<Containerbean> containers;

	public Zone1Location(String list, int type) {
		items = list;
		assignType = type;
		if (assignType != -1)
			initialize();
	}

	public Zone1Location(List<Containerbean> _containers, String list, int type) {
		containers = _containers;
		items = list;
		assignType = type;
		if (assignType != -1)
			initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frame = new JFrame("Zone 1 Layout");
		frame.setSize(900, 600);
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
		cp.setLayout(new GridLayout(0, 10));

		btnZoneCode = new JButton[70];
		Font font = new Font("Verdana", Font.BOLD, 18);

		int index = 1;

		for (JButton btn : btnZoneCode) {
			String label = "";

			if (index / 10 == 0)
				label = "00";
			else
				label = "0";
			btn = new JButton(label + String.valueOf(index));
			btn.setFont(font);

			final String content = label + String.valueOf(index);
			if (content.equals("070")) {
				// btn.setForeground(Color.RED);
				btn.setText("");
				btn.setEnabled(false);
			}
			// btn.addActionListener(this);
			btn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					if (zone1CodeCallBackFunction != null) {
						zone1CodeCallBackFunction.getZoneCode(content);
						frame.dispose();
						frame.setVisible(false);
					} else {
						frame.dispose();
						frame.setVisible(false);
						ItemPanelBaseViewDelegate itemPannelBaseViewDelegate;
						if (assignType == 0)
							itemPannelBaseViewDelegate = new ItemPanelReceivedViewDelegate(containers, items, content);
						else if (assignType == 1)
							itemPannelBaseViewDelegate = new ItemPanelMovedViewDelegate(items, content);
					}

				}
			});
			cp.add(btn);
			index++;

		}

		JPanel exitControl = new JPanel();
		exitControl.setLayout(new GridLayout(0, 1));
		JButton exit = new JButton(new AbstractAction("Back") {

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				frame.setVisible(false);
				ItemPanelBaseViewDelegate itemPannelBaseViewDelegate = null;
				// ZoneMenu window = new ZoneMenu(items, assignType);
				// window.frame.setVisible(true);
				if (containers != null)
					itemPannelBaseViewDelegate = new ItemPanelReceivedViewDelegate(containers,items);
				else
					ZoneMenu.getInstance(items, ZoneMenu.MOVE);
			}
		});

		JPanel backControl = new JPanel();
		backControl.setLayout(new GridLayout(0, 1));
		JButton backButton = new JButton(new AbstractAction("Exit") {

			@Override
			public void actionPerformed(ActionEvent e) {
				
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
		backControl.add(backButton);

		exitControl.setBackground(Constrant.TABLE_COLOR);
		backControl.setBackground(Constrant.TABLE_COLOR);
		frame.getContentPane().setBackground(Constrant.TABLE_COLOR);
		frame.getContentPane().add(exitControl);
		frame.getContentPane().add(backControl);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				frame.dispose();
				frame.setVisible(false);
			}
		});
		
		java.awt.EventQueue.invokeLater(new Runnable() {
		    @Override
		    public void run() {
		    	frame.toFront();
		        frame.repaint();
		    }
		});

	}

	
	// retrieve return code number
	private static Zone1CodeCallBackFunction zone1CodeCallBackFunction;

	public void setZone1CodeCallBackFunction(Zone1CodeCallBackFunction _zone1CodeCallBackFunction) {
		zone1CodeCallBackFunction = _zone1CodeCallBackFunction;

	}

	public Zone1CodeCallBackFunction getZone1CodeCallBackFunction() {
		return zone1CodeCallBackFunction;
	}

	public void clearZone1CodeCallBackFunction() {
		zone1CodeCallBackFunction = null;
	}

	public interface Zone1CodeCallBackFunction {
		public void getZoneCode(String code);

	}

}
