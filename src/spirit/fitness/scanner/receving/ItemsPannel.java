package spirit.fitness.scanner.receving;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import spirit.fitness.scanner.common.Constrant;
import spirit.fitness.scanner.common.HttpRequestCode;
import spirit.fitness.scanner.restful.ContainerRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.FGRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.ModelZoneMapRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.listener.ContainerCallBackFunction;
import spirit.fitness.scanner.restful.listener.InventoryCallBackFunction;
import spirit.fitness.scanner.restful.listener.ModelZone2CallBackFunction;
import spirit.fitness.scanner.util.EmailHelper;
import spirit.fitness.scanner.util.LoadingFrameHelper;
import spirit.fitness.scanner.util.LocationHelper;
import spirit.fitness.scanner.util.PrinterHelper;
import spirit.fitness.scanner.zonepannel.ZoneMenu;
import spirit.fitness.string.printTableView.*;
import spirit.fitness.scanner.model.Containerbean;
import spirit.fitness.scanner.model.Itembean;
import spirit.fitness.scanner.model.ModelDailyReportbean;
import spirit.fitness.scanner.model.ModelZone2bean;
import spirit.fitness.scanner.model.Modelbean;

public class ItemsPannel {

	private static ItemsPannel instance = null;

	public final static int RECEVING = 0;
	public final static int MOVING = 1;

	private static final String TEXT_SUBMIT = "text-submit";
	private static final String INSERT_BREAK = "insert-break";

	// Key:modelID, value:current scanner quality
	private LinkedHashMap<String, Integer> modelScanCurMap;

	// Key:modelID, value:total quality
	private LinkedHashMap<String, Integer> modelTotalCurMap;

	public JFrame frame;
	public JFrame scanResultFrame;
	public JFrame dialogFrame;
	private JTextArea inputSN;
	private JLabel ltotal;
	private JLabel destination;
	private String items;
	private String result;
	private String scanContent;
	private String containerNo;
	private int assignType;
	private int orderTotalCount;
	private boolean repMove = false;
	private HashSet<String> set;
	private List<Containerbean> containers;

	private JButton btnDone;

	private JProgressBar loading;
	private LoadingFrameHelper loadingframe;

	private FGRepositoryImplRetrofit fgRepository;
	private ModelZoneMapRepositoryImplRetrofit fgModelZone2;
	private ContainerRepositoryImplRetrofit containerRepository;

	private ModelZone2bean modelzone2;

	private ItemsPannel(String prevText, int type) {
		assignType = type;
		// initialize(type);
		scanInfo(prevText, type);
		exceuteCallback();
		loadModelMapZone2();
	}

	private ItemsPannel(List<Containerbean> _container, String preText, int type) {
		assignType = type;
		containers = _container;
		scanPannel(preText);
		exceuteCallback();
		loadModelMapZone2();
	}

	public ItemsPannel(String content, String location, int type) {
		assignType = type;
		// displayTable(content, location, type);
		displayScanResultFrame(content, location, type);
		exceuteCallback();
		loadModelMapZone2();
	}

	public ItemsPannel(List<Containerbean> _container, String content, String location, int type) {
		assignType = type;
		containers = _container;
		containerNo = _container.get(0).ContainerNo;
		// displayTable(content, location, type);
		displayScanResultFrame(content, location, type);
		exceuteCallback();
		loadModelMapZone2();
	}

	public static ItemsPannel getInstance(String preText, int type) {
		if (instance == null) {
			instance = new ItemsPannel(preText, type);
		}
		return instance;
	}

	public static ItemsPannel getInstance(String content, String location, int type) {
		if (instance == null) {
			instance = new ItemsPannel(content, location, type);
		}
		return instance;
	}

	public static ItemsPannel getInstance(List<Containerbean> container, String preText, int type) {
		if (instance == null) {
			instance = new ItemsPannel(container, preText, type);
		}
		return instance;
	}

	public static ItemsPannel getInstance(List<Containerbean> container, String content, String location, int type) {
		if (instance == null) {
			instance = new ItemsPannel(container, content, location, type);
		}
		return instance;
	}

	public static boolean isExit() {
		return instance != null;
	}

	public static void destory() {
		if (instance != null)
			instance = null;
	}

	/**
	 * Initialize the contents of the frame.
	 */
	public void scanInfo(String prevTxt, int type) {

		scanResultFrame = new JFrame("");
		// Setting the width and height of frame
		scanResultFrame.setSize(330, 750);
		scanResultFrame.setLocationRelativeTo(null);
		scanResultFrame.setUndecorated(true);
		scanResultFrame.setResizable(false);

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Constrant.FRAME_BORDER_BACKGROUN_COLOR));
		panel.setBackground(Constrant.BACKGROUN_COLOR);
		// adding panel to frame
		scanResultFrame.add(panel);

		scanPannel(panel, prevTxt, type);

		scanResultFrame.setBackground(Color.WHITE);
		scanResultFrame.setVisible(true);

		scanResultFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		scanResultFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {

				scanResultFrame.dispose();
				scanResultFrame.setVisible(false);
			}
		});

	}

	private void scanPannel(JPanel panel, String prevTxt, int type) {

		panel.setLayout(null);
		Font font = new Font("Verdana", Font.BOLD, 18);

		ltotal = new JLabel("Total: 0");
		ltotal.setFont(font);
		ltotal.setBounds(35, 550, 200, 50);
		panel.add(ltotal);

		destination = new JLabel("");
		destination.setFont(font);
		destination.setBounds(35, 5, 300, 50);
		panel.add(destination);

		if (assignType == MOVING) {

			JCheckBox repButton = new JCheckBox("Rep.");
			repButton.setFont(font);
			repButton.setBackground(Constrant.BACKGROUN_COLOR);
			repButton.setBounds(230, 550, 80, 50);
			panel.add(repButton);

			ActionListener actionListener = new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
					boolean selected = abstractButton.getModel().isSelected();
					repMove = selected;

					String[] item = inputSN.getText().toString().split("\n");

					if (item.length > 0 && !item[0].equals("")) {
						if (!selected) {
							destination.setText("");
							ltotal.setText("Total : " + item.length);
						} else {
							String model = item[0].substring(0, 6);
							modelzone2 = Constrant.modelZone2.get(model);
							destination.setText("Des. : " + modelzone2.Zone2Code /* +(" (Zone 2)") */);
							ltotal.setText("Total: " + item.length + "/" + modelzone2.Z2CurtQty);

						}
					}
				}
			};

			repButton.addActionListener(actionListener);
		}

		if (!prevTxt.equals("")) {
			String[] prev = prevTxt.split("\n");

			if (set == null)
				set = new HashSet<String>();
			for (String s : prev) {
				set.add(s);
			}
			ltotal.setText("Total : " + prev.length);
		}

		inputSN = new JTextArea(20, 15);
		String content = "";

		for (int i = 1; i < 10; i++) {
			content += "158012130811120" + i + "\n";
		}

		for (int i = 10; i < 50; i++) {
			if (i == 20)
				continue;
			content += "15801213081112" + i + "\n";
		}
		content += "1580121308111250\n";

		inputSN.setText(prevTxt);

		InputMap input = inputSN.getInputMap();
		KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
		KeyStroke shiftEnter = KeyStroke.getKeyStroke("shift ENTER");
		input.put(shiftEnter, INSERT_BREAK); // input.get(enter)) = "insert-break"
		input.put(enter, TEXT_SUBMIT);

		if (set == null)
			set = new HashSet<String>();
		ActionMap actions = inputSN.getActionMap();
		actions.put(TEXT_SUBMIT, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// submitText();
				// System.out.println(TEXT_SUBMIT);
				inputSN.setText(inputSN.getText().toString() + "\n");
				String prev = inputSN.getText().toString();
				String[] item = inputSN.getText().toString().split("\n");

				boolean lenError = false;

				if (!set.contains(item[item.length - 1]) && item[item.length - 1].length() == 16) {

					set.add(item[item.length - 1]);
				} else {
					lenError = true;
					prev = prev.substring(0, prev.length() - (item[item.length - 1].length()) - 1);

				}

				if (lenError) {

					inputSN.setText(prev);
					// ltotal.setForeground(Color.RED);
					// ltotal.setText("<html>"+item[item.length-1] +"</br> size Error.</html>");
				} else {

					if (!repMove) {
						ltotal.setForeground(Color.BLACK);
						ltotal.setText("Total: " + set.size());
					} else {
						modelzone2 = Constrant.modelZone2.get(item[0].substring(0, 6));
						destination.setText("Des. : " + modelzone2.Zone2Code /* +(" (Zone 2)") */);
						ltotal.setText("Total: " + set.size() + "/" + modelzone2.Z2CurtQty);

					}
				}
			}
		});

		JScrollPane scrollPanel1 = new JScrollPane(inputSN);
		scrollPanel1.setBounds(35, 50, 265, 500);
		inputSN.setFont(font);
		panel.add(scrollPanel1);

		// Creating 000 button
		JButton defaultButton = new JButton("000");
		defaultButton.setFont(font);
		defaultButton.setBounds(35, 600, 125, 50);
		defaultButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				if (inputSN.getText().isEmpty())
					JOptionPane.showMessageDialog(null, "Please scan serial number.");
				else {
					scanResultFrame.setVisible(false);
					scanResultFrame.dispose();
					instance = null;
					displayScanResultFrame(inputSN.getText().toString(), "000", type);
				}
			}
		});

		panel.add(defaultButton);

		// Creating Assign Location button
		JButton locateButton = new JButton("Location");
		locateButton.setFont(font);
		locateButton.setBounds(175, 600, 125, 50);
		locateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (inputSN.getText().isEmpty())
					JOptionPane.showMessageDialog(null, "Please scan serial number.");
				else {

					items = inputSN.getText().toString();
					scanResultFrame.setVisible(false);
					scanResultFrame.dispose();
					instance = null;
					// if (type == MOVING) {
					loadingframe = new LoadingFrameHelper("Checking data...");
					loading = loadingframe.loadingSample("Checking data...");

					String[] itemList = items.split("\n");

					if (itemList.length == 0 && !inputSN.getText().toString().equals("")) {
						itemList = new String[0];
						itemList[0] = inputSN.getText().toString();
					}

					List<Itembean> items = new ArrayList<Itembean>();

					String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(Calendar.getInstance().getTime());
					for (String item : itemList) {
						Itembean _item = new Itembean();

						_item.SN = item;
						_item.ModelNo = item.substring(0, 6);
						items.add(_item);

					}

					checkItemExits(items);

				}
			}
		});

		panel.add(locateButton);

		// Creating Clear button
		JButton clearButton = new JButton("Clear");
		clearButton.setFont(font);
		clearButton.setBounds(35, 665, 125, 50);
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int result = -1;
				if (inputSN.getText().toString().length() > 0) {
					result = JOptionPane.showConfirmDialog(frame, "Do you want to clear all items?", "",
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (result == JOptionPane.YES_OPTION) {
						inputSN.setText("");
						set.clear();
						ltotal.setForeground(Color.BLACK);
						ltotal.setText("Total : 0");
					}
				}

			}
		});

		panel.add(clearButton);

		// Creating Exit button
		JButton exitButton = new JButton("Exit");
		exitButton.setFont(font);
		exitButton.setBounds(175, 665, 125, 50);
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				instance = null;
				scanResultFrame.dispose();
				scanResultFrame.setVisible(false);
			}
		});

		panel.add(exitButton);

	}

	private void scanContainerPannel(JPanel panel, String prevTxt) {
		panel.setLayout(null);
		Font font = new Font("Verdana", Font.BOLD, 18);
		// Creating JLabel
		JLabel shippingLabel = new JLabel("Received Date : ");

		shippingLabel.setBounds(50, 30, 200, 25);
		shippingLabel.setFont(font);
		panel.add(shippingLabel);

		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		JLabel shippingDate = new JLabel(timeStamp);
		shippingDate.setText(timeStamp);
		shippingDate.setFont(font);
		shippingDate.setBounds(320, 20, 250, 50);
		panel.add(shippingDate);
		modelTotalCurMap = new LinkedHashMap<String, Integer>();
		modelScanCurMap = new LinkedHashMap<String, Integer>();
		containerNo = containers.get(0).ContainerNo;
		orderTotalCount = 0;
		for (Containerbean item : containers) {
			orderTotalCount += Integer.valueOf(item.SNEnd.substring(10, 16))
					- Integer.valueOf(item.SNBegin.substring(10, 16));
			modelTotalCurMap.put(item.SNBegin.substring(0, 10),
					Integer.valueOf(item.SNEnd.substring(10, 16)) - Integer.valueOf(item.SNBegin.substring(10, 16)));
			modelScanCurMap.put(item.SNBegin.substring(0, 10), 0);
		}

		// Creating JLabel
		ltotal = new JLabel("");

		ltotal.setBounds(50, 150, 300, 500);
		ltotal.setFont(font);
		panel.add(ltotal);

		// Creating JLabel
		JLabel proLabel = new JLabel("Container # ");

		proLabel.setBounds(50, 60, 200, 25);
		proLabel.setFont(font);
		panel.add(proLabel);

		JLabel proNumber = new JLabel(containers.get(0).ContainerNo);

		scanResultFrame.addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				proNumber.requestFocus();
			}
		});

		proNumber.setFont(font);
		proNumber.setBounds(320, 50, 250, 50);
		panel.add(proNumber);

		String sample = "";

		for (int i = 489; i < 536; i++) {
			sample += "8508451802001" + String.valueOf(i) + "\n";
		}

		prevTxt = sample;
		inputSN = new JTextArea(20, 15);
		String content = "";
		inputSN.setText(prevTxt);
		String[] item = prevTxt.split("\n");
		set = new HashSet<String>();
		int len = 0;
		if (!prevTxt.equals("")) {

			len = item.length;
			modelScanCurMap.clear();
			for (String s : item) {
				set.add(s);

				String modelNo = s.substring(0, 10);
				if (!modelScanCurMap.containsKey(modelNo))
					modelScanCurMap.put(modelNo, 1);
				else
					modelScanCurMap.put(modelNo, modelScanCurMap.get(modelNo) + 1);

			}

		}

		JScrollPane scrollPanel1 = new JScrollPane(inputSN);
		scrollPanel1.setBounds(320, 100, 250, 500);
		inputSN.setFont(font);
		ltotal.setText(setModelScanCountLabel(set.size()));

		InputMap input = inputSN.getInputMap();
		KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
		KeyStroke shiftEnter = KeyStroke.getKeyStroke("shift ENTER");
		input.put(shiftEnter, INSERT_BREAK); // input.get(enter)) = "insert-break"
		input.put(enter, TEXT_SUBMIT);

		ActionMap actions = inputSN.getActionMap();
		actions.put(TEXT_SUBMIT, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				inputSN.setText(inputSN.getText().toString() + "\n");
				String prev = inputSN.getText().toString();
				String[] item = inputSN.getText().toString().split("\n");

				boolean lenError = false;

				String model = "";
				int curModelCnt = 0;
				if (item[item.length - 1].length() == 16) {
					model = item[item.length - 1].substring(0, 10);

					if (modelScanCurMap.get(model) == null)
						lenError = true;
					else
						curModelCnt = modelScanCurMap.get(model);

				}

				if (!set.contains(item[item.length - 1]) && item[item.length - 1].length() == 16
						&& set.size() <= orderTotalCount && modelTotalCurMap.containsKey(model)
						&& curModelCnt < modelTotalCurMap.get(model) && !lenError) {

					modelScanCurMap.put(model, modelScanCurMap.get(model) + 1);
					set.add(item[item.length - 1]);
				} else {
					lenError = true;
					prev = prev.substring(0, prev.length() - (item[item.length - 1].length()) - 1);

				}

				if (lenError) {

					inputSN.setText(prev);
				} else {

					ltotal.setForeground(Color.BLACK);
					ltotal.setText(setModelScanCountLabel(set.size()));

				}

			}
		});

		panel.add(scrollPanel1);

		InputMap inputPro = proNumber.getInputMap();
		KeyStroke enterPro = KeyStroke.getKeyStroke("ENTER");
		KeyStroke shiftEnterPro = KeyStroke.getKeyStroke("shift ENTER");
		inputPro.put(enterPro, INSERT_BREAK); // input.get(enter)) = "insert-break"
		inputPro.put(shiftEnterPro, TEXT_SUBMIT);

		// Creating Query button
		JButton queryButton = new JButton("Exit");
		queryButton.setFont(font);
		queryButton.setBounds(320, 670, 251, 50);

		queryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				instance = null;
				scanResultFrame.dispose();
				scanResultFrame.setVisible(false);
			}
		});

		panel.add(queryButton);

		// Creating clear button
		JButton resetButton = new JButton("Clear");
		resetButton.setFont(font);
		resetButton.setBounds(420, 610, 150, 50);
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int result = -1;
				if (inputSN.getText().toString().length() > 0) {
					result = JOptionPane.showConfirmDialog(frame, "Do you want to clear the all item?", "",
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (result == JOptionPane.YES_OPTION) {
						inputSN.setText("");
						set.clear();
						for (Map.Entry<String, Integer> location : modelScanCurMap.entrySet()) {
							modelScanCurMap.put(location.getKey(), 0);
						}
						ltotal.setText(setModelScanCountLabel(0));
						ltotal.setForeground(Color.BLACK);
					}
				}
			}
		});

		panel.add(resetButton);

		// Creating Exit button
		JButton exitButton = new JButton("000");
		exitButton.setFont(font);
		exitButton.setBounds(50, 610, 150, 50);
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				String[] item = inputSN.getText().toString().split("\n");

				if (inputSN.getText().isEmpty())
					JOptionPane.showMessageDialog(null, "Please scan serial number.");
				else if (item.length != orderTotalCount)
					JOptionPane.showMessageDialog(null, "Quantity Error!");
				else {
					scanResultFrame.setVisible(false);
					scanResultFrame.dispose();
					instance = null;
					displayScanResultFrame(inputSN.getText().toString(), "000", RECEVING);
				}
			}
		});

		panel.add(exitButton);

		// Creating Exit button
		JButton location = new JButton("Location");
		location.setFont(font);
		location.setBounds(240, 610, 150, 50);
		location.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				String[] scanitem = inputSN.getText().toString().split("\n");

				if (inputSN.getText().isEmpty())
					JOptionPane.showMessageDialog(null, "Please scan serial number.");
				else if (scanitem.length != orderTotalCount)
					JOptionPane.showMessageDialog(null, "Quantity Error!");
				else {

					items = inputSN.getText().toString();
					scanResultFrame.setVisible(false);
					scanResultFrame.dispose();
					instance = null;
					// if (type == MOVING) {
					loadingframe = new LoadingFrameHelper("Checking data...");
					loading = loadingframe.loadingSample("Checking data...");

					String[] itemList = items.split("\n");

					if (itemList.length == 0 && !inputSN.getText().toString().equals("")) {
						itemList = new String[0];
						itemList[0] = inputSN.getText().toString();
					}

					List<Itembean> items = new ArrayList<Itembean>();

					String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(Calendar.getInstance().getTime());
					for (String item : itemList) {
						Itembean _item = new Itembean();

						_item.SN = item;
						_item.ModelNo = item.substring(0, 6);
						items.add(_item);

					}

					checkItemExits(items);

				}
			}
		});

		panel.add(location);

		// Creating Exit button
		JButton back = new JButton("Back");
		back.setFont(font);
		back.setBounds(50, 670, 260, 50);
		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				instance = null;
				scanResultFrame.dispose();
				scanResultFrame.setVisible(false);
				ContainerPannel.getInstance();
			}
		});

		panel.add(back);

	}

	public void scanPannel(String prevTxt) {

		scanResultFrame = new JFrame("");
		// Setting the width and height of frame
		scanResultFrame.setSize(620, 750);
		scanResultFrame.setLocationRelativeTo(null);
		scanResultFrame.setUndecorated(true);
		scanResultFrame.setResizable(false);

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Constrant.FRAME_BORDER_BACKGROUN_COLOR));
		panel.setBackground(Constrant.BACKGROUN_COLOR);
		// adding panel to frame
		scanResultFrame.add(panel);

		scanContainerPannel(panel, prevTxt);

		scanResultFrame.setBackground(Color.WHITE);
		scanResultFrame.setVisible(true);

		scanResultFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		scanResultFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {

				scanResultFrame.dispose();
				scanResultFrame.setVisible(false);
			}
		});

	}

	private void exceuteCallback() {

		fgModelZone2 = new ModelZoneMapRepositoryImplRetrofit();
		fgModelZone2.setinventoryServiceCallBackFunction(new ModelZone2CallBackFunction() {

			@Override
			public void resultCode(int code) {
				// TODO Auto-generated method stub
				if (code == HttpRequestCode.HTTP_REQUEST_INSERT_DATABASE_ERROR) {

				}
			}

			@Override
			public void getReportItems(List<ModelZone2bean> items) {
				// Constrant.modelZone2List = items;

				HashMap<String, ModelZone2bean> map = new HashMap<>();
				HashMap<String, Integer> curMap = new HashMap<>();
				HashMap<String, Integer> totalMap = new HashMap<>();

				// int cnt = 0;
				for (ModelZone2bean i : items) {

					if (map.containsKey(i.Model)) {
						ModelZone2bean m = map.get(i.Model);
						i.Zone2Code = m.Zone2Code + "," + i.Zone2Code;

						int curCnt = curMap.get(m.Model);
						totalMap.put(m.Model, totalMap.get(m.Model) + i.Z2MaxQty);

						curCnt += i.Z2MaxQty - i.Z2CurtQty;
						i.Z2CurtQty = totalMap.get(m.Model) - curCnt;

						curMap.put(i.Model, curCnt);

						map.put(i.Model, i);
					} else {
						curMap.put(i.Model, i.Z2MaxQty - i.Z2CurtQty);
						totalMap.put(i.Model, i.Z2MaxQty);
						map.put(i.Model, i);
					}
				}

				Constrant.modelZone2 = map;

				if (loading != null)
					loading.setValue(60);

			}

			@Override
			public void getModelDailyReportItems(List<ModelDailyReportbean> items) {

				HashMap<String, ModelDailyReportbean> map = new HashMap<>();
				for (ModelDailyReportbean i : items) {
					map.put(i.ModelNo, i);
				}
				Constrant.dailyReport = map;
			}

		});

		fgRepository = new FGRepositoryImplRetrofit();
		fgRepository.setinventoryServiceCallBackFunction(new InventoryCallBackFunction() {

			@Override
			public void resultCode(int code) {
				// TODO Auto-generated method stub
				if (code == HttpRequestCode.HTTP_REQUEST_INSERT_DATABASE_ERROR) {
					instance = null;
					JOptionPane.showMessageDialog(null, "Items already exist.");

					loadingframe.setVisible(false);
					loadingframe.dispose();

					if (scanResultFrame != null)
						scanResultFrame.setVisible(true);

					if (dialogFrame != null) {
						dialogFrame.dispose();
						dialogFrame.setVisible(false);
					}

				}
			}

			@Override
			public void getInventoryItems(List<Itembean> items) {

				if (!items.isEmpty()) {
					// progressMonitor.close();
					// task.done();
					instance = null;
					loading.setValue(50);
					loadingframe.setVisible(false);
					loadingframe.dispose();

					if (assignType == MOVING) {
						JOptionPane.showMessageDialog(null, "Update Data Success!");

					}

					if (scanResultFrame != null) {
						scanResultFrame.dispose();
						scanResultFrame.setVisible(false);
					}

					if (dialogFrame != null) {
						dialogFrame.dispose();
						dialogFrame.setVisible(false);
					}

					if (assignType == RECEVING) {

						List<Containerbean> updateContainer = new ArrayList<Containerbean>();
						for(Containerbean container : containers) 
						{
							container.Close = true;
							updateContainer.add(container);
							
						}
						updateContainerStatus(updateContainer);
					}

				}
			}

			@Override
			public void checkInventoryItems(List<Itembean> items) {
				loadingframe.setVisible(false);
				loadingframe.dispose();

				if (assignType == RECEVING) {

					String[] scanItem = inputSN.getText().toString().split("\n");

					if (items.size() != scanItem.length) {
						JOptionPane.showMessageDialog(null, "Items already exist.");

						if (scanResultFrame != null) {
							String updateTxt = "";
							for (Itembean i : items) {
								updateTxt += i.SN + "\n";
							}
							if (modelScanCurMap.isEmpty())
								ltotal.setText("Total : " + items.size());
							else {
								set.clear();
								modelScanCurMap.clear();
								for (Itembean s : items) {
									set.add(s.SN);

									String modelNo = s.SN.substring(0, 10);
									if (!modelScanCurMap.containsKey(modelNo))
										modelScanCurMap.put(modelNo, 1);
									else
										modelScanCurMap.put(modelNo, modelScanCurMap.get(modelNo) + 1);

								}
								ltotal.setText(setModelScanCountLabel(set.size()));

							}
							inputSN.setText(updateTxt);
							scanResultFrame.setVisible(true);
						}
						if (dialogFrame != null) {
							dialogFrame.dispose();
							dialogFrame.setVisible(false);
						}
					} else
						ZoneMenu.getInstance(containers, inputSN.getText().toString(), assignType);

				} else if (assignType == MOVING) {

					// JOptionPane.showMessageDialog(null, "Update Data Success!");
					if (items.size() == 0) {

						// ZoneMenu window = new ZoneMenu(inputSN.getText().toString(), MOVING);
						// window.frame.setVisible(true);
						ZoneMenu.getInstance(inputSN.getText().toString(), assignType);
					} else {

						checkScanResultFrame(items);
					}
				}

			}

			@Override
			public void checkInventoryZone2Items(int result, List<Itembean> items) {
				// TODO Auto-generated method stub

			}
		});

		containerRepository = new ContainerRepositoryImplRetrofit();
		containerRepository.setContainerServiceCallBackFunction(new ContainerCallBackFunction() {

			@Override
			public void resultCode(int code) {
				// TODO Auto-generated method stub

			}

			@Override
			public void addContainerInfo(List<Containerbean> items) {
				// TODO Auto-generated method stub

			}

			@Override
			public void getContainerItems(List<Containerbean> items) {
				if (!items.isEmpty()) {
					JOptionPane.showMessageDialog(null, "Insert Data Success!");
					EmailHelper.sendMail(scanContent);
				}

			}

			@Override
			public void deleteContainerIteam(boolean result) {
				// TODO Auto-generated method stub
				
			}
		});

	}

	// display scan items by table
	private void displayTable(String content, String location, int type) {

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Item Result");
		frame.setLocationRelativeTo(null);
		frame.setBounds(100, 100, 1000, 600);
		String[] itemList = content.split("\n");

		// avoid repeat serial number
		HashSet<String> set = new HashSet<String>();

		for (String s : itemList) {
			set.add(s);
		}
		List sortedList = new ArrayList(set);
		// sort the all serial number ascending order
		Collections.sort(sortedList);

		Object rowData[][] = new Object[sortedList.size()][3];

		for (int i = 0; i < sortedList.size(); i++) {
			for (int j = 0; j < 3; j++) {
				rowData[i][0] = sortedList.get(i);
				rowData[i][1] = ((String) sortedList.get(i)).substring(0, 7);
				rowData[i][2] = location;
			}
		}

		String zone = "";
		if (location.equals(String.valueOf(Constrant.ZONE_CODE_SHIPPING)))
			zone = "Shipping";
		else
			zone = LocationHelper.DisplayZoneCode(LocationHelper.MapZoneCode(location));
		Object columnNames[] = { "Serial Number", "Model", "Location" + "(" + zone + ")" };
		Font font = new Font("Verdana", Font.BOLD, 18);
		final Class[] columnClass = new Class[] { String.class, String.class, String.class };

		DefaultTableModel model = new DefaultTableModel(rowData, columnNames) {
			@Override
			public boolean isCellEditable(int row, int column) {

				return false;
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return columnClass[columnIndex];
			}
		};

		JTable table = new JTable(model);
		table.getTableHeader().setFont(font);
		table.getTableHeader().setBackground(Constrant.DISPALY_ITEMS_TABLE_COLOR);
		table.setBackground(Constrant.DISPALY_ITEMS_TABLE_COLOR);
		table.setFont(font);
		table.setRowHeight(40);

		JScrollPane scrollPane = new JScrollPane(table);
		frame.add(scrollPane, BorderLayout.CENTER);

		btnDone = new JButton("Submit");
		btnDone.setFont(font);
		btnDone.setBounds(312, 387, 89, 50);

		btnDone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// String book =
				// "{\"Seq\":"+91+",\"SN\":\"1858151709001848\",\"Date\":\"2017-12-13
				// 16:14:02.343\",\"Location\":\"051\",\"ModelNo\":\"185815\"}";
				// String result =
				// "{\"Seq\":"+92+",\"SN\":\"1858151709001848\",\"Date\":\"2017-12-13
				// 16:14:02.343\",\"Location\":\"051\",\"ModelNo\":\"185815\"}";
				btnDone.setEnabled(false);

				// progressMonitor = new ProgressMonitor(ItemsPannel.this, "Please wait...", "",
				// 0, 100);

				List<Itembean> items = new ArrayList<Itembean>();

				String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
				for (String item : itemList) {
					Itembean _item = new Itembean();

					_item.SN = item;
					_item.date = timeStamp;
					_item.Location = location;
					_item.ModelNo = item.substring(0, 6);
					items.add(_item);

				}

				if (type == RECEVING) {
					PrinterHelper print = new PrinterHelper();
					print.printItems(content);
				}

				loadingframe = new LoadingFrameHelper("Add data...");
				loading = loadingframe.loadingSample("Add data...");

				// displayLoadingBar();

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {

							submitServer(type, items);

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				// HttpRestApi.postData(result);
			}
		});
		frame.add(btnDone, BorderLayout.SOUTH);
		// frame.setLocationRelativeTo(null);
		// frame.setSize(1000, 500);
		frame.setVisible(true);

		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {

				frame.dispose();
				frame.setVisible(false);

			}
		});

	}

	private void displayScanResultFrame(String content, String location, int type) {

		String[] itemList = content.split("\n");
		HashSet<String> set = new HashSet<String>();

		for (String s : itemList) {
			set.add(s);
		}

		scanContent = content;
		ModelZone2bean modelMapZone2 = null;
		boolean isMoveZone2 = false;
		if (LocationHelper.MapZoneCode(location) == 2) {
			String model = itemList[0].substring(0, 6);
			modelMapZone2 = Constrant.modelZone2.get(model);

			String[] loc = modelMapZone2.Zone2Code.split(",");

			HashMap<String, String> mapZone2Code = new HashMap<String, String>();

			for (String s : loc) {
				mapZone2Code.put(s, modelMapZone2.Model);
			}

			if (modelMapZone2 == null) {
				isMoveZone2 = true;
			} else if (modelMapZone2.Zone2Code == null)
				isMoveZone2 = true;

			else if (mapZone2Code.containsKey(location))
				isMoveZone2 = true;
			else if (modelMapZone2.Zone2Code.equals(location))
				isMoveZone2 = true;
			else if (modelMapZone2.Zone2Code.equals("111"))
				isMoveZone2 = true;
			else {
				JOptionPane.showMessageDialog(null,
						modelMapZone2.FG + " should move to " + modelMapZone2.Zone2Code + ".");

				scanInfo(content, assignType);
			}
		}

		if (isMoveZone2 || LocationHelper.MapZoneCode(location) != 2) {
			List sortedList = new ArrayList(set);
			// sort the all serial number ascending order
			Collections.sort(sortedList);

			List<Integer> noContinue = new ArrayList<Integer>();

			int startIndex = Integer.valueOf(((String) sortedList.get(0)).substring(10, 16));
			String modelNo = ((String) sortedList.get(0)).substring(0, 6);
			int skip = 0;
			/*
			 * for (int i = 0; i < sortedList.size(); i++) { if (Integer.valueOf(((String)
			 * sortedList.get(i)).substring(10, 16)) == startIndex) startIndex = startIndex
			 * + 1; else { skip = startIndex; if (((String) sortedList.get(i)).substring(0,
			 * 6).endsWith(modelNo)) { startIndex = Integer.valueOf(((String)
			 * sortedList.get(i)).substring(10, 16));
			 * noContinue.add(Integer.valueOf(((String) sortedList.get(i)).substring(10,
			 * 16))); startIndex = startIndex + 1; } } }
			 */

			String result = "";
			if (noContinue.size() == 0 && sortedList.size() == 1) {
				result = "SN : " + Integer.valueOf(((String) sortedList.get(0)).substring(10, 16));
			} else
				result = "SN : " + Integer.valueOf(((String) sortedList.get(0)).substring(10, 16)) + "~"
						+ Integer.valueOf(((String) sortedList.get(sortedList.size() - 1)).substring(10, 16));
			// else if (noContinue.size() == 0 && sortedList.size() > 1)
			// result = "SN : " + Integer.valueOf(((String) sortedList.get(0)).substring(10,
			// 16)) + "~"
			// + Integer.valueOf(((String) sortedList.get(sortedList.size() -
			// 1)).substring(10, 16));
			// else {
			// result = "SN : " + Integer.valueOf(((String) sortedList.get(0)).substring(10,
			// 16)) + "~" + (skip - 1);

			// int noContinueStartIndex = noContinue.get(0);
			// result += " <br/>"+((String) sortedList.get(0)).substring(0,10)
			// +noContinueStartIndex +"~";
			/*
			 * for (int i = 1; i < noContinue.size(); i++) { result += noContinue.get(i) -
			 * 1; }
			 */

			// result += "," + (noContinue.get(noContinue.size() - 1) + "~"
			// + Integer.valueOf(((String) sortedList.get(sortedList.size() -
			// 1)).substring(10, 16)));
			// }
			dialogFrame = new JFrame("Query Pannel");
			// Setting the width and height of frame
			dialogFrame.setSize(600, 400);
			dialogFrame.setLocationRelativeTo(null);
			dialogFrame.setUndecorated(true);
			dialogFrame.setResizable(false);

			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Constrant.FRAME_BORDER_BACKGROUN_COLOR));

			panel.setBackground(Constrant.BACKGROUN_COLOR);
			// adding panel to frame
			dialogFrame.add(panel);

			panel.setLayout(null);
			Font font = new Font("Verdana", Font.BOLD, 18);
			Modelbean model = Constrant.models.get((((String) sortedList.get(0)).substring(0, 6)));

			String zoneCode = LocationHelper.DisplayZoneCode(LocationHelper.MapZoneCode(location));
			// Creating JLabel
			JLabel modelLabel = new JLabel("<html>Do you want to assign all items :" + " <br/>" + "Model :"
					+ model.ModelNo + "(" + Constrant.models.get(((String) sortedList.get(0)).substring(0, 6)).Desc
					+ ") <br/>" + "Total : " + sortedList.size() + " <br/>" + result + " <br/>" + "to location " + "["
					+ zoneCode + "][" + location + "] ?</html>");

			/*
			 * This method specifies the location and size of component. setBounds(x, y,
			 * width, height) here (x,y) are cordinates from the top left corner and
			 * remaining two arguments are the width and height of the component.
			 */
			modelLabel.setBounds(30, 0, 500, 200);
			modelLabel.setFont(font);
			panel.add(modelLabel);

			JButton ok = new JButton("Confirm");
			ok.setBounds(50, 330, 150, 50);
			ok.setFont(font);
			panel.add(ok);

			ok.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// String book =
					// "{\"Seq\":"+91+",\"SN\":\"1858151709001848\",\"Date\":\"2017-12-13
					// 16:14:02.343\",\"Location\":\"051\",\"ModelNo\":\"185815\"}";
					// String result =
					// "{\"Seq\":"+92+",\"SN\":\"1858151709001848\",\"Date\":\"2017-12-13
					// 16:14:02.343\",\"Location\":\"051\",\"ModelNo\":\"185815\"}";
					ok.setEnabled(false);

					// progressMonitor = new ProgressMonitor(ItemsPannel.this, "Please wait...", "",
					// 0, 100);

					List<Itembean> items = new ArrayList<Itembean>();

					String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(Calendar.getInstance().getTime());
					for (String item : itemList) {
						Itembean _item = new Itembean();

						_item.SN = item;
						_item.date = timeStamp;
						_item.Location = location;
						_item.ModelNo = item.substring(0, 6);
						_item.ContainerNo = containerNo;
						items.add(_item);

					}

					loadingframe = new LoadingFrameHelper("Add data...");
					loading = loadingframe.loadingSample("Add data...");

					// displayLoadingBar();

					EventQueue.invokeLater(new Runnable() {
						public void run() {
							try {

								scanInfo(content, assignType);
								scanResultFrame.dispose();
								scanResultFrame.setVisible(false);

								submitServer(type, items);

							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
					// HttpRestApi.postData(result);
				}
			});

			JButton prev = new JButton("Back");
			prev.setBounds(220, 330, 150, 50);
			prev.setFont(font);
			panel.add(prev);

			prev.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dialogFrame.dispose();
					dialogFrame.setVisible(false);
					dialogFrame = null;
					if (containers == null)
						scanInfo(content, assignType);
					else
						scanPannel(content);
				}
			});

			JButton cancel = new JButton("Exit");
			cancel.setBounds(400, 330, 150, 50);
			cancel.setFont(font);
			panel.add(cancel);

			cancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					instance = null;
					dialogFrame.dispose();
					dialogFrame.setVisible(false);
				}
			});

			// frame.setUndecorated(true);
			// frame.getRootPane().setWindowDecorationStyle(JRootPane.COLOR_CHOOSER_DIALOG);
			dialogFrame.setBackground(Color.WHITE);
			dialogFrame.setVisible(true);
			// frame.setDefaultLookAndFeelDecorated(true);
			dialogFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			dialogFrame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {

					dialogFrame.dispose();
					dialogFrame.setVisible(false);
				}
			});
		}
	}

	private void checkScanResultFrame(List<Itembean> _items) {

		JFrame dialogFrame = new JFrame("Check Serial number");
		// Setting the width and height of frame
		dialogFrame.setSize(600, 400);
		dialogFrame.setLocationRelativeTo(null);
		dialogFrame.setUndecorated(true);
		dialogFrame.setResizable(false);

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Constrant.FRAME_BORDER_BACKGROUN_COLOR));

		panel.setBackground(Constrant.BACKGROUN_COLOR);
		// adding panel to frame
		dialogFrame.add(panel);

		panel.setLayout(null);
		Font font = new Font("Verdana", Font.BOLD, 18);

		String content = "";

		for (Itembean i : _items) {
			content += "" + i.SN + "<br/>";

		}

		// Creating JLabel
		JLabel Info = new JLabel("<html>The all serial number do not exist :" + " <br/>");
		Info.setBounds(40, 0, 500, 50);
		Info.setFont(font);
		panel.add(Info);

		// Creating JLabel
		JLabel modelLabel = new JLabel("<html>" + content + "<html>");
		modelLabel.setOpaque(true);
		/*
		 * This method specifies the location and size of component. setBounds(x, y,
		 * width, height) here (x,y) are cordinates from the top left corner and
		 * remaining two arguments are the width and height of the component.
		 */
		// modelLabel.setBounds(30, 0, 500, 300);
		modelLabel.setFont(font);
		modelLabel.setBackground(Constrant.BACKGROUN_COLOR);
		JScrollPane scroller = new JScrollPane(modelLabel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.setBackground(Constrant.BACKGROUN_COLOR);
		scroller.setBounds(40, 50, 520, 280);
		panel.add(scroller);

		JButton ok = new JButton("OK");
		ok.setBounds(200, 330, 200, 50);
		ok.setFont(font);
		panel.add(ok);
		String[] errorItem = content.split("</br>");
		if (errorItem.length == 1) {
			errorItem[0] = errorItem[0].substring(0, 16);
		}
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				dialogFrame.setVisible(false);
				dialogFrame.dispose();
				set.clear();
				if (scanResultFrame != null) {
					String[] checkItem = items.split("\n");

					String updateTxt = "";
					for (String s : checkItem) {
						for (String p : errorItem) {
							if (s.equals(p))
								continue;
							updateTxt += s + "\n";
							set.add(s);
						}
					}
					String[] item = updateTxt.split("\n");

					inputSN.setText(updateTxt);
					if (!repMove)
						ltotal.setText("Total : " + item.length);
					else {
						modelzone2 = Constrant.modelZone2.get(item[0].substring(0, 6));
						destination.setText("Destination : " + modelzone2.Zone2Code + (" (Zone 2)"));
						ltotal.setText("Total: " + item.length + "/" + modelzone2.Z2CurtQty);
					}

					scanResultFrame.setVisible(true);
				}

			}
		});

		// frame.setUndecorated(true);
		// frame.getRootPane().setWindowDecorationStyle(JRootPane.COLOR_CHOOSER_DIALOG);
		dialogFrame.setBackground(Color.WHITE);
		dialogFrame.setVisible(true);
		// frame.setDefaultLookAndFeelDecorated(true);
		dialogFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialogFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {

				dialogFrame.dispose();
				dialogFrame.setVisible(false);
			}
		});

		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				dialogFrame.toFront();
				dialogFrame.repaint();
			}
		});
	}

	private void submitServer(int type, List<Itembean> items) {
		String fg;
		try {
			if (type == RECEVING) {
				fgRepository.createItem(items);
			} else if (type == MOVING) {
				fgRepository.updateItem(items);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void checkItemExits(List<Itembean> items) {
		try {
			fgRepository.getItemsLocationBySNList(items);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String setModelScanCountLabel(int curCount) {
		String modelQty = "<html>" + "Total : " + curCount + "/" + String.valueOf(orderTotalCount) + " </br>";
		for (Map.Entry<String, Integer> location : modelScanCurMap.entrySet()) {
			int cnt = 0;

			modelQty += location.getKey().substring(0, 10) + "(" + location.getValue() + "/"
					+ modelTotalCurMap.get(location.getKey().substring(0, 10)) + ") </br>";
		}
		modelQty = modelQty + "</br></html>";
		return modelQty;
	}

	// Loading Models data from Server
	private void loadModelMapZone2() {
		// loading model and location information from Server
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					fgModelZone2.getAllItems();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	// Updated container status from open to close
	private void updateContainerStatus(List<Containerbean> container) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					containerRepository.updateItem(container);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

}
