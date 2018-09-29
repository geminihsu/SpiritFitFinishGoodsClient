package spirit.fitness.scanner.zonepanel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
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
import spirit.fitness.scanner.delegate.received.ItemPanelReturnViewDelegate;
import spirit.fitness.scanner.model.Containerbean;
import spirit.fitness.scanner.search.QueryPanel;
import spirit.fitness.scanner.zonepanel.Zone2Location.Zone2CodeCallBackFunction;

public class ZoneMenu implements ActionListener {

	/**
	 * Create the application.
	 */
	public final static int RECEIVE = 0;
	public final static int MOVE = 1;
	public final static int INQUIRY = 3;
	
	private static ZoneMenu instance = null;
	private JButton btnZone1, btnZone2, btnUnshippable, btnDisplayRoom, /*btnScrapped, btnRework, btnQC,*/ btnRTS;
	public JFrame frame;
	private String items;
	private int assignType;
	private List<Containerbean> containers;

	public ZoneMenu(String content, int type) {
		items = content;
		assignType = type;

		if (assignType != -1)
			initialize(content);
	}
	
	public ZoneMenu(List<Containerbean> _containers,String content, int type) {
		items = content;
		assignType = type;
		containers = _containers;

		if (assignType != -1)
			initialize(content);
	}
	
	 public static ZoneMenu getInstance(String content, int type){
	        if(instance == null){
	            instance = new ZoneMenu(content,type);
	        }
	        return instance;
	    }
	 
	 public static ZoneMenu getInstance(List<Containerbean> _containers,String content, int type){
	        if(instance == null){
	            instance = new ZoneMenu(_containers,content,type);
	        }
	        return instance;
	    }
	 
	 public static void destory() 
	 {
		 instance = null;
	 }
	 
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(String content) {

		// JFrame.setDefaultLookAndFeelDecorated(false);
		// JDialog.setDefaultLookAndFeelDecorated(false);
		frame = new JFrame("FG Inventory App");
		frame.setSize(600, 300);
		frame.setLocationRelativeTo(null);
		frame.setUndecorated(true);
		frame.setResizable(false);
		frame.setVisible(true);

		JPanel borderedPanel = new JPanel();

		// Use any border you want, eg a nice blue one
		borderedPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Constrant.FRAME_BORDER_BACKGROUN_COLOR));

		frame.setContentPane(borderedPanel);

		Container cp = frame.getContentPane();
		cp.setLayout(new GridLayout(0, 2));

		Font font = new Font("Verdana", Font.BOLD, 25);
		btnZone1 = new JButton("Zone 1");
		btnZone1.setFont(font);
		btnZone2 = new JButton("Zone 2");
		btnZone2.setFont(font);
		btnUnshippable = new JButton("Unshippable");
		btnUnshippable.setFont(font);
		btnDisplayRoom = new JButton("Show Room");
		btnDisplayRoom.setFont(font);

		/*btnScrapped = new JButton("Scrapped");
		btnScrapped.setFont(font);

		btnRework = new JButton("Rework");
		btnRework.setFont(font);

		btnQC = new JButton("QC");
		btnQC.setFont(font);*/
		

		btnRTS = new JButton("RTS");
		btnRTS.setFont(font);

		btnZone1.setMnemonic('O');
		btnZone2.setMnemonic('C');
		btnUnshippable.setMnemonic('Q');
		btnZone1.addActionListener(this);
		btnZone2.addActionListener(this);
		btnUnshippable.addActionListener(this);
		btnDisplayRoom.addActionListener(this);
		//btnScrapped.addActionListener(this);
		//btnRework.addActionListener(this);
		//btnQC.addActionListener(this);
		btnRTS.addActionListener(this);


	
			cp.add(btnZone1);
			cp.add(btnZone2);
			cp.add(btnDisplayRoom);
			cp.add(btnUnshippable);
			cp.add(btnRTS);
		
		JPanel exitControl = new JPanel();
		exitControl.setLayout(new GridLayout(0, 2));
		JButton exit = new JButton(new AbstractAction("Back") {

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				frame.setVisible(false);
				instance = null;
				
					//ItemsPannel window = new ItemsPannel(ItemsPannel.MOVING);
					//window.frame.setVisible(true);
					//ItemsPannel.getInstance(content,ItemsPannel.MOVING);
				ItemPanelBaseViewDelegate itemPannelBaseViewDelegate = new ItemPanelMovedViewDelegate(content);
				
			}
		});

		JButton backButton = new JButton(new AbstractAction("Exit") {

			@Override
			public void actionPerformed(ActionEvent e) {
				instance = null;
				
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

	@Override
	public void actionPerformed(ActionEvent e) {
		String btn = "";
		if (e.getSource() == btnZone1) {
			instance = null;
			this.frame.setVisible(false);
			this.frame.dispose();

			Zone1Location window  = new Zone1Location(items, 1);
			window.frame.setVisible(true);

		} else if (e.getSource() == btnZone2) {
			instance = null;
			this.frame.setVisible(false);
			this.frame.dispose();

			Zone2Location window = new Zone2Location(items, assignType);
			window.frame.setVisible(true);

		} else if (e.getSource() == btnUnshippable) {
			//Removed return part
			/*instance = null;
			this.frame.setVisible(false);
			this.frame.dispose();

			ReturnLocation window =null;
			if(containers == null) 
				window = new ReturnLocation(items, assignType);
			else
				window = new ReturnLocation(containers,items, assignType);
			window.frame.setVisible(true);*/
			
			instance = null;
			if (unshippableCodeCallBackFunction != null) {
				this.frame.setVisible(false);
				this.frame.dispose();
				unshippableCodeCallBackFunction.getZoneCode("555");
			} else {
				this.frame.setVisible(false);
				this.frame.dispose();

				
				//ItemsPannel.getInstance(items, "555", assignType);
				ItemPanelBaseViewDelegate itemPannelBaseViewDelegate = new ItemPanelMovedViewDelegate(items, "555");
			}
			

		} else if (e.getSource() == btnDisplayRoom) {
			instance = null;
			if (showRoomCodeCallBackFunction != null) {
				this.frame.setVisible(false);
				this.frame.dispose();
				showRoomCodeCallBackFunction.getZoneCode("888");
			} else {
				this.frame.setVisible(false);
				this.frame.dispose();

				//ItemsPannel window = new ItemsPannel(items, "888", assignType);
				//window.dialogFrame.setVisible(true);
				//ItemsPannel.getInstance(items, "888", assignType);
				ItemPanelBaseViewDelegate itemPannelBaseViewDelegate = new ItemPanelMovedViewDelegate(items, "888");
			}

		}/*else if (e.getSource() == btnRework) {
			instance = null;
			if (reworkCodeCallBackFunction != null) {
				this.frame.setVisible(false);
				this.frame.dispose();
				reworkCodeCallBackFunction.getZoneCode("555");
			} else {
				this.frame.setVisible(false);
				this.frame.dispose();

				//ItemsPannel window = new ItemsPannel(items, "555", assignType);
				//window.dialogFrame.setVisible(true);
				ItemsPannel.getInstance(items, "555", assignType);
			}

		}else if (e.getSource() == btnQC) {
			instance = null;
			if (qcCodeCallBackFunction != null) {
				this.frame.setVisible(false);
				this.frame.dispose();
				qcCodeCallBackFunction.getZoneCode("666");
			} else {
				this.frame.setVisible(false);
				this.frame.dispose();

				//ItemsPannel window = new ItemsPannel(items, "666", assignType);
				//window.dialogFrame.setVisible(true);
				ItemsPannel.getInstance(items, "666", assignType);
			}

		}else if (e.getSource() == btnScrapped) {
			instance = null;
			if (scrappedCodeCallbackFunction != null) {
				this.frame.setVisible(false);
				this.frame.dispose();
				scrappedCodeCallbackFunction.getZoneCode("777");
			} else {
				this.frame.setVisible(false);
				this.frame.dispose();

				//ItemsPannel window = new ItemsPannel(items, "777", assignType);
				//window.dialogFrame.setVisible(true);
				ItemsPannel.getInstance(items, "777", assignType);
			}

		}*/else if (e.getSource() == btnRTS) {
			instance = null;
			
			this.frame.setVisible(false);
			this.frame.dispose();
			
			RTSLocation window =null;
			if(containers == null) 
				window = new RTSLocation(items, assignType);
			else
				window = new RTSLocation(containers,items, assignType);
			window.frame.setVisible(true);

		}

	}

	// retrieve return code number
	public static ShowRoomCodeCallBackFunction showRoomCodeCallBackFunction;

	public void setShowRoomCodeCallBackFunction(ShowRoomCodeCallBackFunction _showRoomCodeCallBackFunction) {
		showRoomCodeCallBackFunction = _showRoomCodeCallBackFunction;

	}

	public ShowRoomCodeCallBackFunction getShowRoomCodeCallBackFunction() {
		return showRoomCodeCallBackFunction;
	}

	public void clearShowRoomCodeCallBackFunction() {
		showRoomCodeCallBackFunction = null;
	}

	public interface ShowRoomCodeCallBackFunction {
		public void getZoneCode(String code);

	}

	// retrieve return code number
/*	public static ReworkCodeCallBackFunction reworkCodeCallBackFunction;

	public void setReworkCodeCallBackFunction(ReworkCodeCallBackFunction _reworkCodeCallBackFunction) {
		reworkCodeCallBackFunction = _reworkCodeCallBackFunction;

	}

	public ReworkCodeCallBackFunction getReworkCodeCallBackFunction() {
		return reworkCodeCallBackFunction;
	}

	public void clearReworkCallBackFunction() {
		reworkCodeCallBackFunction = null;
	}

	public interface ReworkCodeCallBackFunction {
		public void getZoneCode(String code);

	}
	
	// retrieve QC code number
	public static QCCodeCallBackFunction qcCodeCallBackFunction;

	public void setQCCodeCallBackFunction(QCCodeCallBackFunction _qcCodeCallBackFunction) {
		qcCodeCallBackFunction = _qcCodeCallBackFunction;

	}

	public QCCodeCallBackFunction getQCCodeCallBackFunction() {
		return qcCodeCallBackFunction;
	}

	public void clearQCCodeCallBackFunction() {
		qcCodeCallBackFunction = null;
	}

	public interface QCCodeCallBackFunction {
		public void getZoneCode(String code);

	}
	
	// retrieve scrapped code number
	public static ScrappedCodeCallBackFunction scrappedCodeCallbackFunction;

	public void setScrappedCodeCallBackFunction(ScrappedCodeCallBackFunction _scrappedCodeCallbackFunction) {
		scrappedCodeCallbackFunction = _scrappedCodeCallbackFunction;

	}

	public ScrappedCodeCallBackFunction getScrappedCodeCall() {
		return scrappedCodeCallbackFunction;
	}

	public void clearScrappedCodeCallBackFunction() {
		scrappedCodeCallbackFunction = null;
	}

	public interface ScrappedCodeCallBackFunction {
		public void getZoneCode(String code);

	}*/
	// retrieve return code number
	public static UnshippableCallBackFunction unshippableCodeCallBackFunction;

		public void setUnshippableCodeCallBackFunction(UnshippableCallBackFunction _unshippableCallBackFunction) {
			unshippableCodeCallBackFunction = _unshippableCallBackFunction;

		}

		public UnshippableCallBackFunction getUnshippableCodeCallBackFunction() {
			return unshippableCodeCallBackFunction;
		}

		public void clearUnshippableCallBackFunction() {
			unshippableCodeCallBackFunction = null;
		}

		public interface UnshippableCallBackFunction {
			public void getZoneCode(String code);

		}
	// retrieve rts code number
		public static RTSCodeCallBackFunction rtsCodeCallbackFunction;

		public void setRTSCodeCallBackFunction(RTSCodeCallBackFunction _rtsCodeCallbackFunction) {
			rtsCodeCallbackFunction = _rtsCodeCallbackFunction;

		}

		public RTSCodeCallBackFunction getRTSCall() {
			return rtsCodeCallbackFunction;
		}

		public void clearRTSCodeCallBackFunction() {
			rtsCodeCallbackFunction = null;
		}

		public interface RTSCodeCallBackFunction {
			public void getZoneCode(String code);

		}
	
	

}
