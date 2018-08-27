package spirit.fitness.scanner.delegate.received;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import spirit.fitness.scanner.common.Constrant;
import spirit.fitness.scanner.common.HttpRequestCode;
import spirit.fitness.scanner.delegate.ItemPannelBaseViewDelegate;
import spirit.fitness.scanner.model.Containerbean;
import spirit.fitness.scanner.model.Itembean;
import spirit.fitness.scanner.model.ModelDailyReportbean;
import spirit.fitness.scanner.model.ModelZone2bean;
import spirit.fitness.scanner.model.Modelbean;
import spirit.fitness.scanner.model.PickUpZoneMap;
import spirit.fitness.scanner.model.SerialNo;
import spirit.fitness.scanner.receving.ContainerPannel;
import spirit.fitness.scanner.restful.ContainerRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.FGRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.ModelZoneMapRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.listener.ContainerCallBackFunction;
import spirit.fitness.scanner.restful.listener.InventoryCallBackFunction;
import spirit.fitness.scanner.restful.listener.ModelZone2CallBackFunction;
import spirit.fitness.scanner.util.EmailHelper;
import spirit.fitness.scanner.util.LoadingFrameHelper;
import spirit.fitness.scanner.util.LocationHelper;
import spirit.fitness.scanner.util.NetWorkHandler;
//import spirit.fitness.scanner.util.NetWorkHandler;
import spirit.fitness.scanner.zonepannel.Zone1Location;
import spirit.fitness.scanner.zonepannel.ZoneMenu;

public class ItemPannelReceivedViewDelegate extends ItemPannelBaseViewDelegate {

	private FGRepositoryImplRetrofit fgRepository;
	private ContainerRepositoryImplRetrofit containerRepository;
	private List<Containerbean> containers;
	private List<Itembean> outRangeSN;
	private List<SerialNo> snList;
	private List<Integer> duplicatedSNIdx;
	private HashMap<String, Integer> scanItemMap = new HashMap<String, Integer>();
	
	private String containerNo;
	private String snItems ="";
	private Timer timer;
	private int isTimeOut = 1;
	private boolean isAutoScroll;
	private int scanCnt = 1;
	
	private JTable snListTable;
	private JTextField snInput;
	private DefaultTableModel dtm;
	private JPanel tfPanel;

	public ItemPannelReceivedViewDelegate(List<Containerbean> _container, String content) {
		getInstance();
		initial(_container, content);
	}

	public ItemPannelReceivedViewDelegate(List<Containerbean> _container, String content, String location) {
		getInstance();
		initial(_container, content, location);
	}

	@Override
	public void initial(List<Containerbean> _container, String content) {
		containers = _container;
		Collections.sort(containers,new ContainerSortByModel());
		snList = new ArrayList<SerialNo>();
		scanInfo(content);
		exceuteCallback();
		loadModelMapZone2();
	}

	@Override
	public void initial(List<Containerbean> _container, String content, String location) {
		containers = _container;
		containerNo = containers.get(0).ContainerNo;
		Collections.sort(containers,new ContainerSortByModel());
		snList = new ArrayList<SerialNo>();
		displayScanResultFrame(content, location);
		exceuteCallback();
		loadModelMapZone2();
	}

	@Override
	public void scanInfo(String prevTxt) {

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

		scanPanel(panel, prevTxt);

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

	@Override
	public void scanPanel(JPanel panel, String prevTxt) {
		duplicatedSNIdx = new ArrayList<Integer>();
		set = new HashSet<String>();
		modelTotalCurMap = new LinkedHashMap<String, Integer>();
		modelScanCurMap = new LinkedHashMap<String, Integer>();
		containerNo = containers.get(0).ContainerNo;
		orderTotalCount = 0;
		for (Containerbean item : containers) {
			orderTotalCount += Integer.valueOf(item.SNEnd.substring(10, 16))
					- Integer.valueOf(item.SNBegin.substring(10, 16)) + 1;
			modelTotalCurMap.put(item.SNBegin.substring(0, 6), Integer.valueOf(item.SNEnd.substring(10, 16))
					- Integer.valueOf(item.SNBegin.substring(10, 16)) + 1);
			modelScanCurMap.put(item.SNBegin.substring(0, 6), 0);
		}

		Font font = new Font("Verdana", Font.BOLD, 18);
		// JPanel for the text fields
		tfPanel = new JPanel(new GridLayout(2, 2, 10, 20));
		TitledBorder titledBorder = BorderFactory.createTitledBorder(null,
				"Total : 0/" + modelTotalCurMap.get(containers.get(0).SNBegin.substring(0, 6)), TitledBorder.CENTER,
				TitledBorder.BOTTOM, font, Color.BLACK);
		tfPanel.setBorder(titledBorder);
		tfPanel.setBackground(Constrant.BACKGROUN_COLOR);

		JLabel totalLabel = new JLabel(" Container# " + containers.get(0).ContainerNo);
		totalLabel.setFont(font);
		tfPanel.add(totalLabel);

		JLabel model = new JLabel("  Model No." + containers.get(0).SNBegin.substring(0, 6));
		model.setFont(font);
		tfPanel.add(model);

		snInput = new JTextField(50);

		Border borderTable = BorderFactory.createLineBorder(Constrant.BACKGROUN_COLOR, 5);
		snInput.setBackground(Constrant.BACKGROUN_COLOR);
		snInput.setCaretColor(Constrant.BACKGROUN_COLOR);
		snInput.setVerifyInputWhenFocusTarget(true);
		// set the border of this component
		snInput.setBorder(borderTable);
		snInput.setFont(font);
		tfPanel.add(snInput);
		snInput.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

			}
		});

		scanResultFrame.addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				snInput.requestFocus();
			}
		});

		InputMap input = snInput.getInputMap();
		KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
		KeyStroke shiftEnter = KeyStroke.getKeyStroke("shift ENTER");
		input.put(shiftEnter, INSERT_BREAK); // input.get(enter)) = "insert-break"
		input.put(enter, TEXT_SUBMIT);

		ActionMap actions = snInput.getActionMap();
		actions.put(TEXT_SUBMIT, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {

				boolean lenError = false;

				String model = "";
				int curModelCnt = 0;
				if (snInput.getText().toString().length() == 16) {
					model = snInput.getText().toString().substring(0, 6);

					if (modelScanCurMap.get(model) == null)
						lenError = true;
					else
						curModelCnt = modelScanCurMap.get(model);

				}

				if (snInput.getText().toString().length() == 16 && modelTotalCurMap.containsKey(model)
						&& curModelCnt < modelTotalCurMap.get(model)) {

					if (!scanItemMap.containsKey(snInput.getText().toString())) {
						curModelCnt++;
						modelScanCurMap.put(model, curModelCnt);
					}
					addSerialNoToTable(snInput.getText().toString());
					snInput.setText("");
					snListTable.changeSelection(snListTable.getRowCount() - 1, 0, false, false);
					TitledBorder titledBorder = BorderFactory.createTitledBorder(null,
							"Total : " + scanItemMap.size() + "/" + modelTotalCurMap.get(model), TitledBorder.CENTER,
							TitledBorder.BOTTOM, font, Color.BLACK);
					tfPanel.setBorder(titledBorder);

				} else {
					snInput.setText("");
					// prev = prev.substring(0, prev.length() - (item[item.length - 1].length()) -
					// 1);

				}
				
				List<String> findDuplicatedItems = new ArrayList<String>();
				for (SerialNo s : snList) {
					if(!s.serialNo.equals(""))
					findDuplicatedItems.add(s.serialNo);
				}

				// Check if duplicatedSNIdx has duplicated items
				Set<String> duplicates = findDuplicates(findDuplicatedItems);
				
				if(duplicates.size() == 0)
					duplicatedSNIdx.clear();

			}
		});

		// JPanel for the text fields
		JPanel btnPanel = new JPanel(new GridLayout(6, 1, 10, 50));
		TitledBorder buttondBorder = BorderFactory.createTitledBorder(null, "Funtion", TitledBorder.CENTER,
				TitledBorder.TOP, font, Color.BLACK);
		btnPanel.setBorder(buttondBorder);
		btnPanel.setBackground(Constrant.BACKGROUN_COLOR);

		JButton defaultBtn = new JButton("000");
		defaultBtn.setFont(font);
		btnPanel.add(defaultBtn);
		JButton locationButton = new JButton("Location");
		locationButton.setFont(font);
		btnPanel.add(locationButton);
		JButton resetButton = new JButton("Clear");
		resetButton.setFont(font);
		btnPanel.add(resetButton);
		JButton backButton = new JButton("Back");
		backButton.setFont(font);
		btnPanel.add(backButton);
		JButton sortButton = new JButton("Sort");
		sortButton.setFont(font);
		btnPanel.add(sortButton);
		JButton exitButton = new JButton("Exit");
		exitButton.setFont(font);
		btnPanel.add(exitButton);

		defaultBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				for (SerialNo item : snList) {
					if (!item.serialNo.equals(""))
						snItems += item.serialNo + "\n";
				}

				isDefaultZone = true;

				if (scanItemMap.size() == 0) {
					JOptionPane.showMessageDialog(null, "Please scan serial number.");
					snInput.requestFocus();
				} else if (duplicatedSNIdx.size() > 0) {
					JOptionPane.showMessageDialog(null, "Please check duplicated items.");
					snInput.requestFocus();

				} else {

					// query container information again
					getContainerStatus(containers.get(0).ContainerNo);
				}
			}
		});

		locationButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				for (SerialNo item : snList) {
					if (!item.serialNo.equals(""))
						snItems += item.serialNo + "\n";
				}

				if (scanItemMap.size() == 0) {
					JOptionPane.showMessageDialog(null, "Please scan serial number.");
					snInput.requestFocus();
				} else if (duplicatedSNIdx.size() > 0) {
					JOptionPane.showMessageDialog(null, "Please check duplicated items.");
					snInput.requestFocus();

				} else {
					// query container information again
					getContainerStatus(containers.get(0).ContainerNo);
				}
			}
		});

		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				int result = -1;
				if (dtm.getColumnCount() > 0) {
					result = JOptionPane.showConfirmDialog(scanResultFrame, "Do you want to clear the all item?", "",
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (result == JOptionPane.YES_OPTION) {
						Constrant.serial_list = "";
						scanCnt = 1;
						orderTotalCount = 0;
						set = new HashSet<String>();
						modelTotalCurMap = new LinkedHashMap<String, Integer>();
						modelScanCurMap = new LinkedHashMap<String, Integer>();
						scanItemMap = new HashMap<String, Integer>();
						snList.clear();
						duplicatedSNIdx.clear();

						containerNo = containers.get(0).ContainerNo;
						orderTotalCount = 0;
						for (Containerbean item : containers) {
							orderTotalCount += Integer.valueOf(item.SNEnd.substring(10, 16))
									- Integer.valueOf(item.SNBegin.substring(10, 16)) + 1;
							modelTotalCurMap.put(item.SNBegin.substring(0, 6),
									Integer.valueOf(item.SNEnd.substring(10, 16))
											- Integer.valueOf(item.SNBegin.substring(10, 16)) + 1);
							modelScanCurMap.put(item.SNBegin.substring(0, 6), 0);
						}

						dtm.setRowCount(0);
						TitledBorder titledBorder = BorderFactory.createTitledBorder(null,
								"Total : 0/" + modelTotalCurMap.get(containers.get(0).SNBegin.substring(0, 6)),
								TitledBorder.CENTER, TitledBorder.BOTTOM, font, Color.BLACK);
						tfPanel.setBorder(titledBorder);
						snInput.requestFocus();
					}
				}

			}
		});

		sortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				List<String> findDuplicatedItems = new ArrayList<String>();

				for (SerialNo s : snList) {
					if(!s.serialNo.equals(""))
					   findDuplicatedItems.add(s.serialNo);
				}

				// Check if duplicatedSNIdx has duplicated items
				Set<String> duplicates = findDuplicates(findDuplicatedItems);
				
				if(duplicates.size() == 0)
					duplicatedSNIdx.clear();
				
				if (duplicatedSNIdx.size() > 0) {
					JOptionPane.showMessageDialog(null, "Please check duplicated items.");
					snInput.requestFocus();

				} else {

					dtm.setRowCount(0);
					Collections.sort(snList, new SerialNoSortByAscOrder());

					for (SerialNo sn : snList) {
						dtm.addRow(new Object[] { sn.no, sn.serialNo, });
					}

					TableColumn tmIdx = snListTable.getColumnModel().getColumn(0);
					tmIdx.setCellRenderer(new ColorColumnRenderer(Color.LIGHT_GRAY, Color.blue));

					TableColumn tm = snListTable.getColumnModel().getColumn(1);
					tm.setCellRenderer(new ColorColumnRenderer(Color.LIGHT_GRAY, Color.blue));
					snInput.requestFocus();
				}
			}

		});

		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				scanResultFrame.dispose();
				scanResultFrame.setVisible(false);
				ContainerPannel.getInstance();
				destroy();
			}
		});

		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				destroy();
				scanResultFrame.dispose();
				scanResultFrame.setVisible(false);
			}
		});

		String[] header = { "No.", "SN" };
		dtm = new DefaultTableModel(null, header) {
			@Override
			public boolean isCellEditable(int row, int column) {

				List<String> findDuplicatedItems = new ArrayList<String>();

				for (SerialNo s : snList) {
					findDuplicatedItems.add(s.serialNo);
				}

				// Check if duplicatedSNIdx has duplicated items
				Set<String> duplicates = findDuplicates(findDuplicatedItems);
				if(duplicates.size() == 0)
				    snList.clear();

				if (snList.size() > 0 &&!snList.get(row).serialNo.equals("") && duplicates.size() > 0 && duplicates.contains(snList.get(row).serialNo) && column == 1) {
					Object[] options = { "Delete", "Cancel" };
					int n = JOptionPane.showOptionDialog(scanResultFrame,
							"would you like to delete the duplicate item?", "A Silly Question",
							JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

					if (n == 0) {
						String model = dtm.getValueAt(row, column).toString().substring(0, 6);

						SerialNo sn = snList.get(row);
						sn.serialNo = "";
						snList.set(row, sn);
				
						String prevTxt = "";
						for (SerialNo s : snList) {
							prevTxt += s.serialNo + "\n";
						}

						//reset table data in order to highlight repeat items
						dtm.setRowCount(0);
						scanCnt = 1;

						snList.clear();
						duplicatedSNIdx.clear();
						String[] snItems = prevTxt.split("\n");
						modelScanCurMap.clear();
						scanItemMap.clear();
						set.clear();
						for (String s : snItems) {
							set.add(s);

							if (!s.equals("")) {
								String SNmodelNo = s.substring(0, 6);
								if (!modelScanCurMap.containsKey(SNmodelNo))
									modelScanCurMap.put(SNmodelNo, 1);
								else
									modelScanCurMap.put(SNmodelNo, modelScanCurMap.get(SNmodelNo) + 1);
							}
							addSerialNoToTable(s);

						}						

					}
				}
				
				snInput.requestFocus();
				return false;
			}

			@Override
			public Class<?> getColumnClass(int col) {
				return getValueAt(0, col).getClass();
			}
		};
		snListTable = new JTable(dtm);
		JScrollPane scrollPane = new JScrollPane(snListTable);
		JScrollBar vScroll = scrollPane.getVerticalScrollBar();

		Dimension d = new Dimension(500, 480);
		snListTable.setPreferredScrollableViewportSize(d);

		snListTable.getTableHeader().setFont(font);
		snListTable.getTableHeader().setBackground(Constrant.BACKGROUN_COLOR);
		snListTable.setBackground(Constrant.BACKGROUN_COLOR);
		snListTable.setFont(font);
		snListTable.setRowHeight(30);

		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
		leftRenderer.setHorizontalAlignment(JLabel.CENTER);
		TableColumn modelNo = snListTable.getColumnModel().getColumn(0);
		modelNo.setPreferredWidth(2);
		modelNo.setCellRenderer(leftRenderer);

		TableColumn sn = snListTable.getColumnModel().getColumn(1);
		sn.setCellRenderer(leftRenderer);
		sn.setPreferredWidth(350);
		scrollPane.setBackground(Constrant.BACKGROUN_COLOR);

		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		vScroll.addAdjustmentListener(new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				isAutoScroll = !e.getValueIsAdjusting();
			}
		});
		scrollPane.getViewport().setBackground(Constrant.BACKGROUN_COLOR);
		scrollPane.getVerticalScrollBar().setBackground(Constrant.BACKGROUN_COLOR);

		// restore sn
		//for(int i = 2713; i < 2869; i++) {
		//	prevTxt += "135014180700" +i+ "\n";
		//}
		
		restoreData(prevTxt);

		// Setup the content-pane of JFrame in BorderLayout
		Container cp = scanResultFrame.getContentPane();
		cp.setBackground(Constrant.BACKGROUN_COLOR);
		cp.setLayout(new BorderLayout(5, 5));
		cp.add(tfPanel, BorderLayout.NORTH);
		cp.add(btnPanel, BorderLayout.EAST);
		cp.add(scrollPane, BorderLayout.CENTER);
	}
	

	@Override
	public void displayScanResultFrame(String content, String location) {


		String[] itemList = content.split("\n");

		Arrays.sort(itemList);

		HashSet<String> set = new HashSet<String>();

		for (String s : itemList) {
			scanContent += s + "\n";
			set.add(s);
		}

		ModelZone2bean modelMapZone2 = null;
		boolean isMoveZone2 = false;
		if (LocationHelper.MapZoneCode(location) == 2) {
			String model = itemList[0].substring(0, 6);
			modelMapZone2 = Constrant.modelZone2.get(model);
			HashMap<String, String> mapZone2Code = new HashMap<String, String>();

			String[] loc = null;
			if (modelMapZone2 != null) {

				loc = modelMapZone2.Zone2Code.split(",");

				for (String s : loc) {
					mapZone2Code.put(s, modelMapZone2.Model);
				}
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

				scanInfo(content);
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

					ok.setEnabled(false);

					List<Itembean> items = new ArrayList<Itembean>();
					scannedModel = itemList[0].substring(0, 6);
					scannedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
					for (String item : itemList) {
						Itembean _item = new Itembean();

						_item.SN = item;
						_item.date = scannedDate;
						_item.Location = location;
						_item.ModelNo = _item.SN.substring(0, 6);
						_item.ContainerNo = containerNo;
						items.add(_item);

					}

					loadingframe = new LoadingFrameHelper("Add data...");
					loading = loadingframe.loadingSample("Add data...");

					// displayLoadingBar();

					EventQueue.invokeLater(new Runnable() {
						public void run() {
							try {

								scanInfo(content);
								scanResultFrame.dispose();
								scanResultFrame.setVisible(false);

								submitServer(items);

							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
					// HttpRestApi.postData(result);
					if(snInput != null)
						snInput.requestFocus();
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

					if(scanResultFrame == null)
						scanInfo(content);
					else
						scanResultFrame.setVisible(true);
				}
			});

			JButton cancel = new JButton("Exit");
			cancel.setBounds(400, 330, 150, 50);
			cancel.setFont(font);
			panel.add(cancel);

			cancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					destroy();
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


	@Override
	public void checkScanResultFrame(List<Itembean> _items) {
		set.clear();
		dtm.setRowCount(0);
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

		String title = "";

		title = "<html>The all serial number exist :" + " <br/>";
		// Creating JLabel
		JLabel Info = new JLabel(title);
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

		/*
		 * JButton delete = new JButton("Delete"); delete.setBounds(410, 330, 200, 50);
		 * delete.setFont(font); panel.add(delete);
		 */

		String contentTxt = content;
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] errorItem = contentTxt.split("<br/>");
				if (errorItem.length == 1) {
					errorItem[0] = errorItem[0].substring(0, 16);
				}
				dialogFrame.setVisible(false);
				dialogFrame.dispose();
				set.clear();
				if (scanResultFrame != null) {
					String[] checkItem = snItems.split("\n");

					String updateTxt = "";
					HashSet<String> itemError = new HashSet<String>();
					for (String s : errorItem) {
						itemError.add(s);
					}

					dtm.setRowCount(0);

					modelScanCurMap.clear();
					scanItemMap.clear();
					duplicatedSNIdx.clear();
					snList.clear();
					
					scanCnt = 1;
					for (String p : checkItem) {
						String SNmodelNo = p.substring(0, 6);
						if (!itemError.contains(p)) {
							set.add(p);

						

							addSerialNoToTable(p);
						}
					}

					TitledBorder titledBorder = BorderFactory.createTitledBorder(null,
							"Total : " + scanItemMap.size() + "/" + modelTotalCurMap.get(checkItem[0].substring(0, 6)),
							TitledBorder.CENTER, TitledBorder.BOTTOM, font, Color.BLACK);
					tfPanel.setBorder(titledBorder);
					scanResultFrame.setVisible(true);
					snInput.requestFocus();
				}

			}
		});

		dialogFrame.setBackground(Color.WHITE);
		dialogFrame.setVisible(true);

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

	
	public void checkScanResultOutOfFrame(List<Itembean> scanitems) {
		set.clear();
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

		for (Itembean i : scanitems) {
			content += "" + i.SN + "<br/>";

		}

		String title = "";

		title = "<html>Are you sure assign all items to location ? Those all serial number out of range :" + " <br/>";
		// Creating JLabel
		JLabel Info = new JLabel(title);
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
		ok.setBounds(40, 330, 200, 50);
		ok.setFont(font);
		panel.add(ok);

		JButton cancel = new JButton("NO");
		cancel.setBounds(360, 330, 200, 50);
		cancel.setFont(font);
		panel.add(cancel);

		/*
		 * JButton delete = new JButton("Delete"); delete.setBounds(410, 330, 200, 50);
		 * delete.setFont(font); panel.add(delete);
		 */

		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialogFrame.dispose();
				dialogFrame.setVisible(false);
				
				checkReceiveItemExits(scanitems);
				scanResultFrame.setVisible(false);
				snInput.requestFocus();
			}
		});

		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				dialogFrame.dispose();
				dialogFrame.setVisible(false);
				if (loadingframe != null) {
					loadingframe.setVisible(false);
					loadingframe.dispose();
				}
				scanResultFrame.setVisible(true);
				snInput.requestFocus();
			}
		});

		dialogFrame.setBackground(Color.WHITE);
		dialogFrame.setVisible(true);

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

	public void checkScanResultDispearFrame(String serial) {
		set.clear();
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

		String title = "";

		title = "<html>Those all serial number miss :" + " <br/>";
		// Creating JLabel
		JLabel Info = new JLabel(title);
		Info.setBounds(40, 0, 500, 50);
		Info.setFont(font);
		panel.add(Info);

		// Creating JLabel
		JLabel modelLabel = new JLabel("<html>" + serial + "<html>");
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
		ok.setBounds(150, 330, 300, 50);
		ok.setFont(font);
		panel.add(ok);

		/*
		 * JButton delete = new JButton("Delete"); delete.setBounds(410, 330, 200, 50);
		 * delete.setFont(font); panel.add(delete);
		 */

		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialogFrame.dispose();
				dialogFrame.setVisible(false);
				if (loadingframe != null) {
					loadingframe.setVisible(false);
					loadingframe.dispose();
				}
				scanResultFrame.setVisible(true);
				snInput.requestFocus();
			}
		});

		dialogFrame.setBackground(Color.WHITE);
		dialogFrame.setVisible(true);

		dialogFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialogFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (loadingframe != null) {
					loadingframe.setVisible(false);
					loadingframe.dispose();
				}
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

	private void checkMissItems(String items) {
		String[] scanItem = items.split("\n");

		int startIndex = 0;
		int endIndex = 0;
		String snBegin = "";
		String snEnd = "";
		
		for(String s : scanItem) 
		{
		   if(!s.equals("")) {
			   scannedModel = s.substring(0, 6);
		}
		}
		
		for (Containerbean c : containers) {
			if (c.SNBegin.substring(0, 6).equals(scannedModel)) {
				startIndex = Integer.valueOf(c.SNBegin.substring(10, 16));
				endIndex = Integer.valueOf(c.SNEnd.substring(10, 16));
				snBegin = c.SNBegin;
				snEnd = c.SNEnd;
			}

		}
		
		Arrays.sort(scanItem);
		String noScan = "";
		String sortResult = "";
		int idx = 0;
		for (int i = startIndex; i < endIndex + 1; i++) {

			if (idx < scanItem.length) {

				if (scanItem[idx].equals(""))
					continue;
				if (scanItem[idx].length() > 16) {
					scanItem[idx] = scanItem[idx].substring(0, 16);
					if (set.contains(scanItem[idx]))
						continue;
				}
				
				String snLen = String.valueOf(i);
				
				if (Integer.valueOf(scanItem[idx].substring(16 - snLen.length(), 16)) == i) {
					sortResult += scanItem[idx] + "\n";
					idx++;
				} else {
					
				
					noScan += (snBegin.substring(0, 16 - snLen.length())) + i + "<br/>";
				}
			}

		}

		if (!noScan.equals(""))
			checkScanResultDispearFrame(noScan);
	}

	private void setTimer() {
		timer = new javax.swing.Timer(30000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (isTimeOut < 4)
					isTimeOut++;
				else {
					timer.stop();
					timer = null;

					if (loadingframe != null) {
						loadingframe.setVisible(false);
						loadingframe.dispose();
					}
					scanResultFrame.setVisible(true);
				}

			}
		});

	}
	private void checkReceiveItemExits(List<Itembean> items) {
		try {
			setTimer();
			timer.start();
			fgRepository.getReceiveItemBySNList(items);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			NetWorkHandler.displayError(loadingframe);
		}
	}

	// Updated container status from open to close
	private void updateContainerStatus(List<Containerbean> container) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					containerRepository.updateItem(container);

				} catch (Exception e) {
					e.printStackTrace();
					NetWorkHandler.displayError(loadingframe);
				}
			}
		});

	}

	// Updated container status from open to close
	private void getContainerStatus(String containerNo) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					containerRepository.getItemsByContainerNo(containerNo);

				} catch (Exception e) {
					e.printStackTrace();
					NetWorkHandler.displayError(loadingframe);
				}
			}
		});

	}
	
	@Override
	public void submitServer(List<Itembean> items) {
		try {
			setTimer();
			timer.start();
			fgRepository.createItem(items);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			NetWorkHandler.displayError(loadingframe);
		}
	}

	@Override
	public void exceuteCallback() {
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

			}

			@Override
			public void getModelDailyReportItems(List<ModelDailyReportbean> items) {

				HashMap<String, ModelDailyReportbean> map = new HashMap<>();
				for (ModelDailyReportbean i : items) {
					map.put(i.ModelNo, i);
				}
				Constrant.dailyReport = map;
			}

			@Override
			public void pickUpZone(List<PickUpZoneMap> items) {
				// TODO Auto-generated method stub
				
			}

		});
		fgRepository = new FGRepositoryImplRetrofit();
		fgRepository.setinventoryServiceCallBackFunction(new InventoryCallBackFunction() {

			@Override
			public void resultCode(int code) {
				// TODO Auto-generated method stub
				if (code == HttpRequestCode.HTTP_REQUEST_INSERT_DATABASE_ERROR) {
					destroy();
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
				timer.stop();
                timer = null;
                isTimeOut = 1;
                
				if (!items.isEmpty()) {
					// progressMonitor.close();
					// task.done();
					destroy();
					loading.setValue(50);
					loadingframe.setVisible(false);
					loadingframe.dispose();

					if (scanResultFrame != null) {
						scanResultFrame.dispose();
						scanResultFrame.setVisible(false);
					}

					if (dialogFrame != null) {
						dialogFrame.dispose();
						dialogFrame.setVisible(false);
					}

					// Modify container close to true
					List<Containerbean> updateContainer = new ArrayList<Containerbean>();
					for (Containerbean container : containers) {
						container.Close = true;
						updateContainer.add(container);

					}
					updateContainerStatus(updateContainer);

				}
			}

			@Override
			public void checkInventoryZone2Items(int result, List<Itembean> items) {
				// TODO Auto-generated method stub

			}

			@Override
			public void checkMoveItems(List<Itembean> items) {

			}

			@Override
			public void checkReceiveItem(List<Itembean> items) {
				timer.stop();
				timer = null;
				isTimeOut = 1;
		
				if (loadingframe != null) {
					loadingframe.setVisible(false);
					loadingframe.dispose();
				}

				if (items.size() == 0) {
					snItems = "";

					for (SerialNo item : snList) {
						if (!item.serialNo.equals(""))
							snItems += item.serialNo + "\n";
					}
					if (isDefaultZone)
						displayScanResultFrame(snItems, "000");
					else {

						// ZoneMenu.getInstance(containers, inputSN.getText().toString(), 0);
						Zone1Location window = new Zone1Location(containers, snItems, 0);
						window.frame.setVisible(true);
					}
				} else if (items.size() > 0) {
					JOptionPane.showMessageDialog(null, "Items already exist.");

					if (items.size() > 0)
						checkScanResultFrame(items);

				}
			}

			@Override
			public void exception(String error) {
				NetWorkHandler.displayError(loadingframe);
				scanResultFrame.setVisible(true);
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
				//if (!items.isEmpty()) {
					Constrant.serial_list = "";
					JOptionPane.showMessageDialog(null, "Insert Data Success!");
					EmailHelper.sendMail(scannedDate, containers, scanContent, "geminih@spiritfitness.com");
					EmailHelper.sendMail(scannedDate, containers, scanContent, "jeremyc@spiritfitness.com");
					EmailHelper.sendMail(scannedDate, containers, scanContent, "vickie@spiritfitness.com");
					EmailHelper.sendMail(scannedDate, containers, scanContent, "ashleyg@spiritfitness.com");
				//}

			}

			@Override
			public void deleteContainerIteam(boolean result) {
				// TODO Auto-generated method stub

			}

			@Override
			public void getContainerItemsByContainerNo(List<Containerbean> container) {
				orderTotalCount = Integer.valueOf(container.get(0).SNEnd.substring(10, 16))
						- Integer.valueOf(container.get(0).SNBegin.substring(10, 16)) + 1;

				for (int i = 0; i < containers.size(); i++) {
					Containerbean c = containers.get(i);

					if (c.ContainerNo.equals(container.get(0).ContainerNo)) {
						// update container qty
						c.SNBegin = container.get(0).SNBegin;
						c.SNEnd = container.get(0).SNEnd;
					}
				}

				if (scanItemMap.size() != orderTotalCount) {
					JOptionPane.showMessageDialog(null, "Quantity Error!");

					snItems = "";
					for (SerialNo s : snList) {
						if(!s.serialNo.equals(""))
							snItems += s.serialNo + "\n";
					}

					checkMissItems(snItems);
				} else {

					scanResultFrame.setVisible(false);
					scanResultFrame.dispose();

					scanResultFrame.setVisible(false);
					scanResultFrame.dispose();
					destroy();
					// if (type == MOVING) {
					loadingframe = new LoadingFrameHelper("Checking data...");
					loading = loadingframe.loadingSample("Checking data...");

					int startIndex = 0;
					int endIndex = 0;

					List<Itembean> items = new ArrayList<Itembean>();
					outRangeSN = new ArrayList<Itembean>();
					String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(Calendar.getInstance().getTime());

					for (Containerbean c : containers) {

						startIndex = Integer.valueOf(c.SNBegin.substring(10, 16));
						endIndex = Integer.valueOf(c.SNEnd.substring(10, 16));

						scannedModel = c.SNBegin.substring(0, 6);

						for (SerialNo item : snList) {

							if (!item.serialNo.equals("") && item.serialNo.substring(0, 6).equals(scannedModel)) {
								Itembean _item = new Itembean();

								_item.SN = item.serialNo;
								_item.ModelNo = item.serialNo.substring(0, 6);
								_item.date = timeStamp;
								items.add(_item);
								if (Integer.valueOf(item.serialNo.substring(16 - String.valueOf(startIndex).length() , 16)) - startIndex < 0
										|| endIndex - Integer.valueOf(item.serialNo.substring(16 - String.valueOf(startIndex).length(), 16)) < 0)
									outRangeSN.add(_item);
							}
						}

					}

					Constrant.serial_list = "Container No." + containers.get(0).ContainerNo + "\n Receiving SN : \n"
							+ snItems;

					if (outRangeSN.size() > 0)
						checkScanResultOutOfFrame(outRangeSN);
					else
						checkReceiveItemExits(items);

				}
			}
		});
	}
	
	public void restoreData(String prevTxt) 
	{
		Font font = new Font("Verdana", Font.BOLD, 18);
		String[] item = prevTxt.split("\n");
		set = new HashSet<String>();
		int len = 0;
		if (!prevTxt.equals("")) {
			dtm.setRowCount(0);
			len = item.length;
			modelScanCurMap.clear();

			for (String s : item) {
				set.add(s);

				String SNmodelNo = s.substring(0, 6);
				if (!modelScanCurMap.containsKey(SNmodelNo))
					modelScanCurMap.put(SNmodelNo, 1);
				else
					modelScanCurMap.put(SNmodelNo, modelScanCurMap.get(SNmodelNo) + 1);

				addSerialNoToTable(s);

			}

			snListTable.changeSelection(snListTable.getRowCount() - 1, 0, false, false);
			TitledBorder titledBorder = BorderFactory.createTitledBorder(null,
					"Total : " + len + "/" + modelTotalCurMap.get(containers.get(0).SNBegin.substring(0, 6)),
					TitledBorder.CENTER, TitledBorder.BOTTOM, font, Color.BLACK);
			tfPanel.setBorder(titledBorder);
		}
	}
	public Set<String> findDuplicates(List<String> listContainingDuplicates) {

		final Set<String> setToReturn = new HashSet<String>();
		final Set<String> set1 = new HashSet<String>();

		for (String yourInt : listContainingDuplicates) {
			if (!set1.add(yourInt)) {
				setToReturn.add(yourInt);
			}
		}
		return setToReturn;
	}

	private void addSerialNoToTable(String sn) {
		if (!scanItemMap.containsKey(sn) || sn.equals("")) {
			// char c = (char) ('A' + scanCnt++ % 26);
			
			if(!sn.equals(""))
			scanItemMap.put(sn, scanCnt);
			SerialNo snItem = new SerialNo();
			snItem.no = scanCnt;
			snItem.serialNo = sn;
			snList.add(snItem);
			dtm.addRow(new Object[] { scanCnt, sn,

			});
			
			scanCnt++;
		} else {

			if (scanCnt - scanItemMap.get(sn) > 1 && scanItemMap.size() < orderTotalCount) {
				// char c = (char) ('A' + scanCnt++ % 26);

				int repeatIdx = 0;

				for (SerialNo item : snList) {

					if (item.serialNo.equals(sn) && !duplicatedSNIdx.contains(repeatIdx))
						duplicatedSNIdx.add(repeatIdx);
					repeatIdx++;
				}
				// System.out.println(scanCnt -1);
				duplicatedSNIdx.add(scanCnt - 1);

				
				scanItemMap.put(sn, scanCnt);
				SerialNo snItem = new SerialNo();
				snItem.no = scanCnt;
				snItem.serialNo = sn;
				snList.add(snItem);
				dtm.addRow(new Object[] { scanCnt, sn,

				});
				scanCnt++;
			}
		}
		
		TableColumn tmIdx = snListTable.getColumnModel().getColumn(0);
		tmIdx.setCellRenderer(new ColorColumnRenderer(Color.LIGHT_GRAY, Color.blue));

		TableColumn tm = snListTable.getColumnModel().getColumn(1);
		tm.setCellRenderer(new ColorColumnRenderer(Color.LIGHT_GRAY, Color.blue));


	}

	class ContainerSortByModel implements Comparator<Containerbean> {
		// Used for sorting in ascending order of
		// roll name
		public int compare(Containerbean a, Containerbean b) {
			String modelA = a.SNBegin.substring(0, 6);
			String modelB = b.SNBegin.substring(0, 6);
			return modelA.compareTo(modelB);
		}
	}

	class SerialNoSortByAscOrder implements Comparator<SerialNo> {
		// Used for sorting in ascending order of
		// roll name
		public int compare(SerialNo a, SerialNo b) {
			String snA = a.serialNo;
			String snB = b.serialNo;
			return snA.compareTo(snB);
		}
	}

	/**
	 * Applied background and foreground color to single column of a JTable in order
	 * to distinguish it apart from other columns.
	 */
	class ColorColumnRenderer extends DefaultTableCellRenderer {
		Color bkgndColor, fgndColor;

		public ColorColumnRenderer(Color bkgnd, Color foregnd) {
			super();
			bkgndColor = bkgnd;
			fgndColor = foregnd;
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			List<String> findDuplicatedItems = new ArrayList<String>();

			for (SerialNo s : snList) {
				if(!s.serialNo.equals(""))
				findDuplicatedItems.add(s.serialNo);
			}

			// Check if duplicatedSNIdx has duplicated items
			Set<String> duplicates = findDuplicates(findDuplicatedItems);
			
			if(duplicates.size() == 0)
				duplicatedSNIdx.clear();
			
			if (column == 0) {

				if (dtm.getValueAt(row, 1) == null ||duplicatedSNIdx.size() == 0 || snList.get(row).serialNo.equals("") || !(duplicates.contains(snList.get(row).serialNo))) {
					cell.setBackground(Constrant.BACKGROUN_COLOR);
					Component cellsn = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, 1);
					cellsn.setBackground(Constrant.BACKGROUN_COLOR);
				} else {
					cell.setBackground(Color.lightGray);
					Component cellsn = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, 1);
					cellsn.setBackground(Color.lightGray);

				}

			} else if (column == 1) {
				if (dtm.getValueAt(row, 1) == null ||duplicatedSNIdx.size() == 0 || snList.get(row).serialNo.equals("") || !(duplicates.contains(snList.get(row).serialNo))) {
					cell.setBackground(Constrant.BACKGROUN_COLOR);
					Component cellsn = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, 0);
					cellsn.setBackground(Constrant.BACKGROUN_COLOR);
				} else {
					cell.setBackground(Color.lightGray);
					Component cellsn = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, 0);
					cellsn.setBackground(Color.lightGray);

				}

			}

			((JLabel) cell).setHorizontalAlignment(SwingConstants.CENTER);

			return cell;
		}
	}

}

