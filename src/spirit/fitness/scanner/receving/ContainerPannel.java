package spirit.fitness.scanner.receving;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.NumberFormatter;

import spirit.fitness.scanner.common.Constrant;
import spirit.fitness.scanner.common.HttpRequestCode;
import spirit.fitness.scanner.model.Containerbean;
import spirit.fitness.scanner.model.Itembean;
import spirit.fitness.scanner.model.Locationbean;
import spirit.fitness.scanner.model.ModelZone2bean;
import spirit.fitness.scanner.model.Modelbean;
import spirit.fitness.scanner.model.PickingItem;
import spirit.fitness.scanner.receving.ItemsPannel;
import spirit.fitness.scanner.report.ModelZone2Report;
import spirit.fitness.scanner.restful.ContainerRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.FGRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.ModelZoneMapRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.listener.ContainerCallBackFunction;
import spirit.fitness.scanner.restful.listener.InventoryCallBackFunction;
import spirit.fitness.scanner.restful.listener.ModelZone2CallBackFunction;
import spirit.fitness.scanner.search.QueryPannel;
import spirit.fitness.scanner.util.LoadingFrameHelper;
import spirit.fitness.scanner.util.LocationHelper;
import spirit.fitness.scanner.util.WeightPlateUtil;
import spirit.fitness.scanner.zonepannel.ReturnLocation;
import spirit.fitness.scanner.zonepannel.Zone1Location;
import spirit.fitness.scanner.zonepannel.Zone2Location;
import spirit.fitness.scanner.zonepannel.ZoneMenu;
import spirit.fitness.scanner.zonepannel.ReturnLocation.ZoneCodeReturnCallBackFunction;

public class ContainerPannel implements ActionListener {

	private static ContainerPannel receivingPannel = null;

	public JFrame frame;
	private LoadingFrameHelper loadingframe;
	private JFrame addContainerFrame;
	private JPanel panel;

	private JProgressBar loading;

	private ContainerRepositoryImplRetrofit containerRepositoryImplRetrofit;

	private List<Containerbean> curContainers;

	public static ContainerPannel getInstance() {
		if (receivingPannel == null) {
			receivingPannel = new ContainerPannel();
		}
		return receivingPannel;
	}

	public static boolean isExit() {
		return receivingPannel != null;
	}

	public static void destory() {
		receivingPannel = null;
	}

	public ContainerPannel() {
		loadingframe = new LoadingFrameHelper("Loading Data from Server...");
		loading = loadingframe.loadingSample("Loading Data from Server...");
		initialCallback();
		loadContainerList();

	}

	/**
	 * Initialize the contents of the frame.
	 */
	public void containerList(List<Containerbean> containerList) {

		// JFrame.setDefaultLookAndFeelDecorated(false);
		// JDialog.setDefaultLookAndFeelDecorated(false);
		frame = new JFrame("Query Pannel");
		// Setting the width and height of frame
		frame.setSize(760, 600);
		frame.setLocationRelativeTo(null);
		frame.setLocationRelativeTo(null);
		frame.setUndecorated(true);
		frame.setResizable(false);

		panel = new JPanel();
		panel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Constrant.FRAME_BORDER_BACKGROUN_COLOR));

		panel.setBackground(Constrant.BACKGROUN_COLOR);
		// adding panel to frame
		frame.add(panel);

		placeConatinerPanel(panel, containerList);

		// frame.setUndecorated(true);
		// frame.getRootPane().setWindowDecorationStyle(JRootPane.COLOR_CHOOSER_DIALOG);
		frame.setBackground(Color.WHITE);
		frame.setVisible(true);
		// frame.setDefaultLookAndFeelDecorated(true);
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

	public void addContainerInfo() {
		addContainerFrame = new JFrame("");
		// Setting the width and height of frame
		// scanResultFrame.setSize(600, 800);
		addContainerFrame.setSize(600, 500);
		addContainerFrame.setLocationRelativeTo(null);
		addContainerFrame.setLocationRelativeTo(null);
		addContainerFrame.setUndecorated(true);
		addContainerFrame.setResizable(false);

		JPanel scanDisplayPanel = new JPanel();
		scanDisplayPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Constrant.FRAME_BORDER_BACKGROUN_COLOR));

		scanDisplayPanel.setBackground(Constrant.BACKGROUN_COLOR);
		// adding panel to frame
		addContainerFrame.add(scanDisplayPanel);

		addContainerPanel(scanDisplayPanel);

		addContainerFrame.setBackground(Color.WHITE);
		addContainerFrame.setVisible(true);
		addContainerFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addContainerFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {

				addContainerFrame.dispose();
				addContainerFrame.setVisible(false);
			}
		});

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	private void placeConatinerPanel(JPanel panel, List<Containerbean> containerList) {

		panel.setLayout(null);

		Font font = new Font("Verdana", Font.BOLD, 18);

		JLabel modelLabel = new JLabel("Please select one container before scan.");

		modelLabel.setBounds(30, 30, 500, 25);
		modelLabel.setFont(font);
		panel.add(modelLabel);
		// ScrollPane for Result
		JScrollPane scrollZonePane = new JScrollPane();

		scrollZonePane.setBackground(Constrant.TABLE_COLOR);

		if (!containerList.isEmpty()) {

			LinkedHashMap<String, List<Containerbean>> map = new LinkedHashMap<String, List<Containerbean>>();

			for (Containerbean item : containerList) {

				if (map.containsKey(item.ContainerNo)) {
					List<Containerbean> items = map.get(item.ContainerNo);
					items.add(item);
				} else {
					List<Containerbean> items = new ArrayList<Containerbean>();
					items.add(item);
					map.put(item.ContainerNo, items);
				}
			}

			int totalCount = 0;
			int rowIndex = 0;

			final Object[][] containerItems = new Object[map.size()][3];

			for (Map.Entry<String, List<Containerbean>> location : map.entrySet()) {

				List<Containerbean> list = location.getValue();

				int qty = 0;
				for (int s = 0; s < list.size(); s++) {
					qty += Integer.valueOf(list.get(0).SNEnd.substring(10, 16))
							- Integer.valueOf(list.get(0).SNBegin.substring(10, 16));
				}

				for (int j = 0; j < 3; j++) {

					containerItems[rowIndex][0] = list.get(0).date.substring(0, 10);

					containerItems[rowIndex][1] = location.getKey();

					containerItems[rowIndex][2] = qty;

				}

				rowIndex++;
			}

			final Class[] columnClass = new Class[] { String.class, String.class, Integer.class };

			Object columnNames[] = { "DATE", "CONTAINERNO", "QTY" };

			DefaultTableModel container = new DefaultTableModel(containerItems, columnNames) {
				@Override
				public boolean isCellEditable(int row, int column) {
					curContainers = map.get(containerItems[row][1]);
					return false;
				}

				@Override
				public Class<?> getColumnClass(int columnIndex) {
					return columnClass[columnIndex];
				}
			};

			JTable containerTable = new JTable(container);
			containerTable.getTableHeader().setBackground(Constrant.TABLE_COLOR);
			containerTable.getTableHeader().setFont(font);

			containerTable.setBackground(Constrant.TABLE_COLOR);
			containerTable.setRowHeight(40);
			containerTable.setFont(font);

			DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
			leftRenderer.setHorizontalAlignment(JLabel.CENTER);
			TableColumn date = containerTable.getColumnModel().getColumn(0);
			date.setCellRenderer(leftRenderer);
			TableColumn no = containerTable.getColumnModel().getColumn(1);
			no.setCellRenderer(leftRenderer);
			TableColumn qty = containerTable.getColumnModel().getColumn(2);
			qty.setCellRenderer(leftRenderer);

			int heigh = 0;

			if (50 * containerItems.length + 20 > 380)
				heigh = 380;
			else
				heigh = 50 * containerItems.length + 20;
			scrollZonePane.setBounds(33, 91, 700, heigh);
			scrollZonePane.setViewportView(containerTable);

			panel.add(scrollZonePane);

		}

		JButton scan = new JButton(new AbstractAction("Scan Item") {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (curContainers != null) {
					frame.dispose();
					frame.setVisible(false);
					receivingPannel = null;
					ItemsPannel.getInstance(curContainers, "",ItemsPannel.RECEVING);
				}else
					JOptionPane.showMessageDialog(null, "Please select one container before scan.");

			}
		});

		JButton add = new JButton(new AbstractAction("ADD Container") {

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				frame.setVisible(false);
				addContainerInfo();
			}
		});

		JButton prev = new JButton(new AbstractAction("Back") {

			@Override
			public void actionPerformed(ActionEvent e) {

			}
		});

		JButton exit = new JButton(new AbstractAction("Exit") {

			@Override
			public void actionPerformed(ActionEvent e) {

				frame.dispose();
				frame.setVisible(false);
				receivingPannel = null;
			}
		});

		scan.setFont(font);
		add.setFont(font);
		prev.setFont(font);
		exit.setFont(font);

		scan.setBounds(33, 520, 200, 50);
		//add.setBounds(250, 520, 200, 50);
		//prev.setBounds(460, 520, 130, 50);
		//exit.setBounds(600, 520, 130, 50);
		prev.setBounds(250, 520, 200, 50);
		exit.setBounds(460, 520, 200, 50);
		panel.add(scan);
		//panel.add(add);
		panel.add(prev);
		panel.add(exit);
	}

	private void addContainerPanel(JPanel panel) {

		panel.setLayout(null);
		Font font = new Font("Verdana", Font.BOLD, 18);

		JLabel dateLabel = new JLabel("Date");

		dateLabel.setBounds(50, 50, 200, 25);
		dateLabel.setFont(font);
		panel.add(dateLabel);

		JTextField dateText = new JTextField(20);
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		dateText.setText(timeStamp);
		dateText.setFont(font);
		dateText.setBounds(250, 50, 320, 50);

		panel.add(dateText);

		JLabel locationLabel = new JLabel("Container#");
		locationLabel.setFont(font);
		locationLabel.setBounds(50, 120, 200, 50);
		panel.add(locationLabel);

		JTextField containerNo = new JTextField(20);
		containerNo.setFont(font);
		containerNo.setBounds(250, 120, 320, 50);

		panel.add(containerNo);

		addContainerFrame.addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				containerNo.requestFocus();
			}
		});

		JLabel snbeginLabel = new JLabel("Serial no. begin");
		snbeginLabel.setFont(font);
		snbeginLabel.setBounds(50, 190, 200, 50);
		panel.add(snbeginLabel);

		JTextField snbegin = new JTextField(20);
		snbegin.setFont(font);
		snbegin.setBounds(250, 190, 320, 50);

		panel.add(snbegin);

		JLabel snendLabel = new JLabel("Serial no. End");
		snendLabel.setFont(font);
		snendLabel.setBounds(50, 260, 200, 50);
		panel.add(snendLabel);

		JTextField snend = new JTextField(20);
		snend.setFont(font);
		snend.setBounds(250, 260, 320, 50);

		panel.add(snend);

		JButton queryButton = new JButton("Submit");
		queryButton.setFont(font);
		queryButton.setBounds(250, 330, 150, 50);
		queryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				if (!snbegin.getText().toString().trim().substring(0, 6)
						.equals(snend.getText().toString().trim().substring(0, 6)))
					JOptionPane.showMessageDialog(null,
							"If the container has more than one model, please create another container.");

				else if (snbegin.getText().toString().trim().equals(snend.getText().toString().trim()))
					JOptionPane.showMessageDialog(null,
							"The SN start number the same as the end number. Please check them.");

				else if (Integer.valueOf(snend.getText().toString().trim().substring(10, 16)) - Integer.valueOf(snbegin.getText().toString().trim().substring(10, 16)) <0)
					JOptionPane.showMessageDialog(null,
							"The SN start number is smaller than SN end number. Please check them.");

				else 
				if (!dateText.getText().toString().equals("") && !containerNo.getText().toString().equals("")
						&& !snbegin.getText().toString().equals("") && !snend.getText().toString().equals("")
						&& snbegin.getText().toString().length() == 16 && snend.getText().toString().length() == 16) {
					List<Containerbean> containers = new ArrayList<Containerbean>();
					Containerbean container = new Containerbean();
					container.date = dateText.getText().toString();
					container.ContainerNo = containerNo.getText().toString();
					container.SNBegin = snbegin.getText().toString();
					container.SNEnd = snend.getText().toString();
					containers.add(container);

					EventQueue.invokeLater(new Runnable() {
						public void run() {
							try {
								loadingframe = new LoadingFrameHelper("Add data...");
								loading = loadingframe.loadingSample("Add data...");
								submitContainerInfo(containers);

							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});

				}
			}
		});
		panel.add(queryButton);

		// Creating Query button
		JButton resetButton = new JButton("Clear");
		resetButton.setFont(font);
		resetButton.setBounds(420, 330, 150, 50);
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				containerNo.setText("");
				snbegin.setText("");
				snend.setText("");

			}
		});

		panel.add(resetButton);

		JButton backButton = new JButton("Back");
		backButton.setFont(font);
		backButton.setBounds(250, 390, 150, 50);
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frame.setVisible(true);
				addContainerFrame.setVisible(false);
				addContainerFrame.dispose();
			}
		});

		JButton exitButton = new JButton("Exit");
		exitButton.setFont(font);
		exitButton.setBounds(420, 390, 150, 50);
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addContainerFrame.setVisible(false);
				addContainerFrame.dispose();
				receivingPannel = null;
			}
		});

		panel.add(backButton);
		panel.add(exitButton);
	}

	private void initialCallback() {
		containerRepositoryImplRetrofit = new ContainerRepositoryImplRetrofit();
		containerRepositoryImplRetrofit.setContainerServiceCallBackFunction(new ContainerCallBackFunction() {

			@Override
			public void resultCode(int code) {
				// TODO Auto-generated method stub
				if (code == HttpRequestCode.HTTP_REQUEST_INSERT_DATABASE_ERROR) {

				}
			}

			@Override
			public void getContainerItems(List<Containerbean> items) {
				loading.setValue(100);
				loadingframe.setVisible(false);
				loadingframe.dispose();

				// if(frame == null)
				containerList(items);
				// else {
				// placeConatinerPanel(panel, items);
				// frame.setVisible(true);
				// }
			}

			@Override
			public void addContainerInfo(List<Containerbean> items) {

				loadingframe.setVisible(false);
				loadingframe.dispose();

				if (!items.isEmpty())
					JOptionPane.showMessageDialog(null, "Insert Data Success!");

				loadContainerList();
				frame = null;
				addContainerFrame.setVisible(false);
				addContainerFrame.dispose();

			}

			@Override
			public void deleteContainerIteam(boolean result) {
				// TODO Auto-generated method stub
				
			}
		});

	}

	// Loading Models data from Server
	private void loadContainerList() {

		// loading model and location information from Server
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					containerRepositoryImplRetrofit.getAllItems();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	private void submitContainerInfo(List<Containerbean> containers) {

		// loading model and location information from Server
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					containerRepositoryImplRetrofit.createItem(containers);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

}
