package spirit.fitness.scanner.shipping;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import spirit.fitness.scanner.common.Constrant;
import spirit.fitness.scanner.common.HttpRequestCode;
import spirit.fitness.scanner.restful.FGRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.HistoryRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.HttpRestApi;
import spirit.fitness.scanner.restful.OrdersRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.PalletRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.listener.CustOrderCallBackFunction;
import spirit.fitness.scanner.restful.listener.HistoryCallBackFunction;
import spirit.fitness.scanner.restful.listener.InventoryCallBackFunction;
import spirit.fitness.scanner.restful.listener.PalletCallBackFunction;
import spirit.fitness.scanner.search.QueryResult;
import spirit.fitness.scanner.util.LoadingFrameHelper;
import spirit.fitness.scanner.util.NetWorkHandler;
//import spirit.fitness.scanner.util.NetWorkHandler;
import spirit.fitness.scanner.util.PrintTableUtil;
import spirit.fitness.scanner.util.PrinterHelper;
import spirit.fitness.scanner.util.PrinterHelper.PrintTable;
import spirit.fitness.scanner.util.WeightPlateUtil;
import spirit.fitness.scanner.model.CustOrderbean;
import spirit.fitness.scanner.model.Historybean;
import spirit.fitness.scanner.model.Itembean;
import spirit.fitness.scanner.model.Locationbean;
import spirit.fitness.scanner.model.Palletbean;
import spirit.fitness.scanner.model.PickingItem;

import spirit.fitness.scanner.util.PrintPreviewUitl;

public class ShippingConfirm {

	private static ShippingConfirm shippingConfirm = null;

	private static final String TEXT_SUBMIT = "text-submit";
	private static final String INSERT_BREAK = "insert-break";
	public JFrame frame;
	private JFrame orderFrame;
	private JFrame scanResultFrame;

	private JPanel orderDisplayPanel;

	private JButton scanner;
	private JButton print;
	private JButton report;
	private JButton prev;
	private JButton exit;

	private String salesOrder = "";
	// private String orderItemsInfo ="";
	private String historyItemsInfo = "";
	private String prevContent = "";
	private String prevTrackingNo = "";
	private String shipDate = "";
	private String trackingNo = "";

	private String soCreatedDate = "";
	private String billToTitle = "";
	private String custPO = "";
	private String shippToAdddress = "";
	private String shippToCity = "";
	private String shippToState = "";
	private String shippToCountry = "";
	private String shippToZip = "";
	private String shippToVia = "";

	private boolean isOrderClosed;
	private HashSet<String> snRepeatSet;

	// Key:modelID, value:quality
	private LinkedHashMap<String, Integer> map;

	// Key:modelID, value:current scanner quality
	private LinkedHashMap<String, Integer> modelScanCurMap;

	// Key:modelID, value:description
	private TreeMap<String, String> OrderModelmap;

	// Key:modelID, value:response items
	private LinkedHashMap<String, List<Itembean>> inventoryModelmap;

	// Key:Location, value:quantity
	private LinkedHashMap<String, LinkedHashMap<String, Integer>> locMap = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();

	private int orderTotalCount = 0;
	private int orderCurCount = 0;

	private Timer timer;
	private int isTimeOut = 1;

	// private List<CustOrderbean> salesOrderList;
	private List<CustOrderbean> salesOrderList;
	private List<CustOrderbean> checkedItemNoSN;
	private List<PickingItem> pickingItems;
	private List<Historybean> items;
	private List<Historybean> scanItems;
	private List<String> resultModelItem = new ArrayList<String>();

	private JProgressBar loading;
	private LoadingFrameHelper loadingframe;
	private JTextArea inputSN;
	private JLabel lCount;
	private JLabel lModelError;

	private PrintPreviewUitl printer;

	private FGRepositoryImplRetrofit fgRepositoryImplRetrofit;
	private OrdersRepositoryImplRetrofit ordersRepositoryImplRetrofit;
	private HistoryRepositoryImplRetrofit historyRepositoryImplRetrofit;
	private PalletRepositoryImplRetrofit palletRepositoryImplRetrofit;

	public static boolean isExit() {
		return shippingConfirm != null;
	}

	public static ShippingConfirm getInstance() {
		if (shippingConfirm == null) {
			shippingConfirm = new ShippingConfirm();
		}
		return shippingConfirm;
	}

	public ShippingConfirm() {
		loadingframe = new LoadingFrameHelper("");
		exceuteCallback();
		orderInfo();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	public void scanInfo(String salesOrder) {
		scanResultFrame = new JFrame("");
		// Setting the width and height of frame
		// scanResultFrame.setSize(600, 800);
		scanResultFrame.setBounds(30, 10, 550, 800);
		scanResultFrame.setUndecorated(true);
		scanResultFrame.setResizable(false);

		JPanel scanDisplayPanel = new JPanel();
		scanDisplayPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Constrant.FRAME_BORDER_BACKGROUN_COLOR));

		scanDisplayPanel.setBackground(Constrant.BACKGROUN_COLOR);
		// adding panel to frame
		scanResultFrame.add(scanDisplayPanel);

		scanPannel(scanDisplayPanel, salesOrder);

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

	private void displayOrderInfo(List<Itembean> items) {

		// Locationbean title = Constrant.locations.get(locationbead);

		if (orderFrame == null) {
			orderFrame = new JFrame();
			orderFrame.setBounds(100, 50, 1050, 800);
			orderFrame.setLocationRelativeTo(null);
			orderFrame.setUndecorated(true);
			orderFrame.setResizable(false);

			orderDisplayPanel = new JPanel();
			orderDisplayPanel
					.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Constrant.FRAME_BORDER_BACKGROUN_COLOR));

			orderDisplayPanel.setBackground(Constrant.BACKGROUN_COLOR);
			// adding panel to frame
			orderFrame.add(orderDisplayPanel);
		}

		placeOrderInfo();

		orderFrame.getContentPane().setBackground(Constrant.BACKGROUN_COLOR);
		orderFrame.setVisible(true);

		orderFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		orderFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				orderFrame.dispose();
				orderFrame.setVisible(false);
			}
		});
	}

	private void querySalesOrder(String orderNo) {

		try {
			ordersRepositoryImplRetrofit.getItemsBySalesOrderNo(orderNo);

		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// NetWorkHandler.displayError(loadingframe);
		}

	}

	// update salesOrder date and tracking number
	private List<CustOrderbean> updateSalesOrder() {

		List<CustOrderbean> items = null;
		try {
			items = (ArrayList<CustOrderbean>) ordersRepositoryImplRetrofit.updateItem(salesOrderList);

		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// NetWorkHandler.displayError(loadingframe);
		}

		return items;
	}

	// Insert item into shipping table
	private void shippingItems(List<Historybean> datas) {

		try {
			setTimer();
			timer.start();

			items = (ArrayList<Historybean>) historyRepositoryImplRetrofit.createItem(datas);

		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (scanResultFrame != null) {
				scanResultFrame.dispose();
				scanResultFrame.setVisible(false);
			}
			restoreScanPannel(null);
			NetWorkHandler.displayError(loadingframe);
			prevContent = inputSN.getText().toString();

		}

	}

	// Insert item into shipping table
	private void submitPalletItems(List<Palletbean> datas) {

		try {
			List<Palletbean> items = (ArrayList<Palletbean>) palletRepositoryImplRetrofit.createItem(datas);

		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// NetWorkHandler.displayError(loadingframe);
		}

	}

	// Get items from History table
	private List<Historybean> getShippgingItems(String salesOrder, boolean isDisplay) {
		// List<Historybean> items = null;
		try {
			items = historyRepositoryImplRetrofit.getItemsBySalesOrder(salesOrder);

			if (isDisplay)
				displayItemsfromHistory(items);
			// else if (items.size() == 0)
			// else
			// querySalesOrder(salesOrder);
			// else
			// JOptionPane.showMessageDialog(null, "The sales order is closed !");
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			restoreScanPannel(null);
			NetWorkHandler.displayError(loadingframe);
			prevContent = inputSN.getText().toString();

		}

		return items;
	}

	private void checkItemExitsZone2(List<Itembean> items) {
		try {
			setTimer();
			timer.start();
			fgRepositoryImplRetrofit.getItemsZone2BySNList(salesOrder, items);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			restoreScanPannel(null);
			NetWorkHandler.displayError(loadingframe);
			prevContent = inputSN.getText().toString();

		}
	}

	private void displayItemsfromHistory(List<Historybean> items) {
		if (!items.isEmpty()) {

			frame.dispose();
			frame.setVisible(false);

			if (scanResultFrame != null) {
				scanResultFrame.dispose();
				scanResultFrame.setVisible(false);
			}

			JScrollPane JScrollPane = getShippgingItemsJScrollPane(items);

			int tableSize = 50 * items.size() + 20;
			if (tableSize > 150)
				tableSize = 150;

			JScrollPane.setBounds(40, 600, 300, tableSize);

			orderDisplayPanel.add(JScrollPane);
			orderDisplayPanel.remove(scanner);
			orderDisplayPanel.add(print);

			if (loadingframe != null) {
				loadingframe.setVisible(false);
				loadingframe.dispose();
			}

			orderFrame.invalidate();
			orderFrame.validate();
			orderFrame.repaint();

		}
	}

	private void setTimer() {
		timer = new javax.swing.Timer(30000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (isTimeOut < 4)
					isTimeOut++;
				else {
					isTimeOut = 1;
					timer.stop();
					timer = null;

					prevContent = inputSN.getText().toString();
					restoreScanPannel(null);
				}

			}
		});
	}

	private JScrollPane getShippgingItemsJScrollPane(List<Historybean> list) {
		Font font = new Font("Verdana", Font.BOLD, 18);
		JScrollPane scrollSNPane = new JScrollPane();
		final Class[] itemColumnClass = new Class[] { String.class };
		final Object[][] orderSNItems = new Object[list.size()][1];
		Object snColumnNames[] = { "SN" };

		for (int i = 0; i < list.size(); i++) {
			orderSNItems[i][0] = list.get(i).SN;
		}

		DefaultTableModel snModel = new DefaultTableModel(orderSNItems, snColumnNames) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return itemColumnClass[columnIndex];
			}
		};

		JTable snItems = new JTable(snModel);
		snItems.getTableHeader().setBackground(Constrant.TABLE_COLOR);
		snItems.getTableHeader().setFont(font);

		snItems.setBackground(Constrant.TABLE_COLOR);
		snItems.setRowHeight(40);
		snItems.setFont(font);

		scrollSNPane.setBounds(33, 500, 300, 50 * orderSNItems.length + 20);
		scrollSNPane.setViewportView(snItems);

		return scrollSNPane;

	}

	public void orderInfo() {

		frame = new JFrame("Query Pannel");
		// Setting the width and height of frame
		frame.setSize(700, 400);
		frame.setLocationRelativeTo(null);
		frame.setUndecorated(true);
		frame.setResizable(false);

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Constrant.FRAME_BORDER_BACKGROUN_COLOR));

		panel.setBackground(Constrant.BACKGROUN_COLOR);
		// adding panel to frame
		frame.add(panel);

		placeComponents(panel);

		frame.setBackground(Color.WHITE);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {

				frame.dispose();
				frame.setVisible(false);
			}
		});

	}

	private void placeComponents(JPanel panel) {
		loadingframe = new LoadingFrameHelper("Loading data...");
		panel.setLayout(null);
		Font font = new Font("Verdana", Font.BOLD, 18);
		// Creating JLabel
		JLabel modelLabel = new JLabel("SO Number:");

		modelLabel.setBounds(100, 100, 200, 25);
		modelLabel.setFont(font);
		panel.add(modelLabel);

		JTextField salesOrderNo = new JTextField(20);
		salesOrderNo.setText("");
		salesOrderNo.setFont(font);
		salesOrderNo.setBounds(230, 100, 320, 50);
		panel.add(salesOrderNo);

		// Creating Query button
		JButton QueryButton = new JButton("Find");
		QueryButton.setFont(font);
		QueryButton.setBounds(230, 180, 150, 50);
		QueryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				if (salesOrderNo.getText().equals(""))
					JOptionPane.showMessageDialog(null, "Please enter Sales Order Number.");
				else {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							try {

								loading = loadingframe.loadingSample("Loading data...");

								salesOrder = salesOrderNo.getText().toString().trim();
								// querySalesOrder(salesOrder);
								getShippgingItems(salesOrder, false);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}

			}
		});

		panel.add(QueryButton);

		// Creating Query button
		JButton ResetButton = new JButton("Clear");
		ResetButton.setFont(font);
		ResetButton.setBounds(400, 180, 150, 50);
		ResetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				salesOrderNo.setText("");
			}
		});

		panel.add(ResetButton);

		// Creating Query button
		JButton exitButton = new JButton("Exit");
		exitButton.setFont(font);
		exitButton.setBounds(230, 250, 320, 50);
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				printer = null;
				shippingConfirm = null;
				frame.dispose();
				frame.setVisible(false);
			}
		});

		panel.add(exitButton);
	}

	private void placeOrderInfo() {

		salesOrder = salesOrderList.get(0).salesOrder;
		soCreatedDate = salesOrderList.get(0).createdDate;
		billToTitle = salesOrderList.get(0).bill_to;
		custPO = salesOrderList.get(0).customerPO;
		shippToAdddress = salesOrderList.get(0).shipToAddress;
		shippToCity = salesOrderList.get(0).shipToCity;
		shippToCountry = salesOrderList.get(0).shipToCountry;
		shippToState = salesOrderList.get(0).shipToState;
		shippToZip = salesOrderList.get(0).shipToZipCode;
		shippToVia = salesOrderList.get(0).shipVia;

		// isOrderClosed = salesOrderList.get(0).closed;
		orderDisplayPanel.setLayout(null);

		Font font = new Font("Verdana", Font.BOLD, 18);

		placeOrderInfoDetail();
		// ScrollPane for Result
		JScrollPane scrollZonePane = new JScrollPane();

		scrollZonePane.setBackground(Constrant.TABLE_COLOR);
		orderDisplayPanel.add(scrollZonePane);

		JLabel total = new JLabel("Total: ");

		total.setBounds(900, 450, 90, 50);
		total.setFont(font);
		total.setBackground(Constrant.BACKGROUN_COLOR);
		orderDisplayPanel.add(total);

		if (!salesOrderList.isEmpty()) {

			int colsize = 3;
			final Object[][] orderModelItems = new Object[map.size()][colsize];

			int rowIndex = 0;
			for (Map.Entry<String, Integer> location : map.entrySet()) {
				for (int j = 0; j < colsize; j++) {
					orderModelItems[rowIndex][0] = location.getValue();
					orderModelItems[rowIndex][1] = salesOrderList.get(rowIndex).ItemID;

					if (!WeightPlateUtil.isModelParts(salesOrderList.get(rowIndex).ItemID))
						orderModelItems[rowIndex][2] = salesOrderList.get(rowIndex).description;
					else
						orderModelItems[rowIndex][2] = salesOrderList.get(rowIndex).description
								+ WeightPlateUtil.modelAppendWithPart(salesOrderList.get(rowIndex).ItemID);

					OrderModelmap.put(salesOrderList.get(rowIndex).ItemID, salesOrderList.get(rowIndex).description);
				}

				rowIndex++;
			}

			// modelLabel.setText("SalesOrder : "+_items.get(0).SalesOrder);

			final Class[] columnClass = new Class[] { Integer.class, String.class, String.class };

			Object columnNames[] = { "Qty", "ItemID", "Description" };

			DefaultTableModel model = new DefaultTableModel(orderModelItems, columnNames) {
				@Override
				public boolean isCellEditable(int row, int column) {
					// if (String.valueOf(orderModelItems[row][1]).indexOf("P") == -1)
					// queryByModel(String.valueOf(orderModelItems[row][1]));
					return false;
				}

				@Override
				public Class<?> getColumnClass(int columnIndex) {
					return columnClass[columnIndex];
				}
			};

			JTable table = new JTable(model);
			table.getTableHeader().setBackground(Constrant.TABLE_COLOR);
			table.getTableHeader().setFont(font);
			TableColumn quantity = table.getColumnModel().getColumn(0);
			quantity.setPreferredWidth(20);
			TableColumn itemId = table.getColumnModel().getColumn(1);
			itemId.setPreferredWidth(80);
			TableColumn modelTitle = table.getColumnModel().getColumn(2);
			modelTitle.setPreferredWidth(800);

			table.setBackground(Constrant.TABLE_COLOR);
			table.setRowHeight(40);
			table.setFont(font);

			int tableSize = 50 * orderModelItems.length + 20;
			if (tableSize > 300)
				tableSize = 300;
			scrollZonePane.setBounds(40, 280, 970, tableSize);

			scrollZonePane.setViewportView(table);

			int txtHeight = 280 + 50 * orderModelItems.length;
			if (txtHeight > 580)
				txtHeight = 580;

			total.setBounds(900, txtHeight, 200, 50);
			total.setText("Total : " + String.valueOf(orderTotalCount));
			orderDisplayPanel.add(scrollZonePane);

			// modelLabel.setText("SalesOrder : "+_items.get(0).SalesOrder);
			// modelText.setText(" TOTAL: "+totalCount);

			total.setText("Total : " + String.valueOf(orderTotalCount));

			int buttonHeight = 280 + 50 * orderModelItems.length + 50;
			if (buttonHeight > 630)
				buttonHeight = 630;

			scanner = new JButton("Scan");
			scanner.setFont(font);
			scanner.setBounds(820, buttonHeight, 190, 50);

			print = new JButton("Print");
			print.setFont(font);
			print.setBounds(820, buttonHeight, 190, 50);

			prev = new JButton("Back");
			prev.setFont(font);
			prev.setBounds(820, 700, 90, 50);

			exit = new JButton("Exit");
			exit.setFont(font);
			exit.setBounds(920, 700, 90, 50);

			print.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					String shipToAddress = billToTitle + "\n              " + shippToAdddress + "\n              "
							+ shippToCity + "  " + shippToState + "\n              " + shippToZip + "    "
							+ shippToCountry;

					printer(salesOrder, soCreatedDate, billToTitle, shipToAddress, historyItemsInfo);
					// printShippingTable();

				}
			});

			scanner.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					scanner.setEnabled(false);
					scanInfo(salesOrder);
				}
			});

			prev.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (scanResultFrame != null) {
						scanResultFrame.dispose();
						scanResultFrame.setVisible(false);
					}
					modelScanCurMap.clear();

					orderFrame.dispose();
					orderFrame.setVisible(false);

					ShippingConfirm window = new ShippingConfirm();
					window.frame.setVisible(true);
				}
			});

			exit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					shippingConfirm = null;

					if (scanResultFrame != null) {
						scanResultFrame.dispose();
						scanResultFrame.setVisible(false);
					}

					orderFrame.dispose();
					orderFrame.setVisible(false);
				}
			});

			orderDisplayPanel.add(prev);
			orderDisplayPanel.add(exit);

			if (!isOrderClosed)
				orderDisplayPanel.add(scanner);
			else {
				// display the items
				orderDisplayPanel.add(print);

				// List<Historybean> items = getShippgingItems(salesOrder, true);
				displayItemsfromHistory(items);

				for (Historybean h : items) {
					historyItemsInfo += h.SN + "\n";
				}
				rowIndex = 0;

				String orderItemsInfo = null;
				for (Map.Entry<String, Integer> location : map.entrySet()) {

					orderItemsInfo += location.getValue() + " " + //
							salesOrderList.get(rowIndex).ItemID + " " + //
							salesOrderList.get(rowIndex).description + " " + items.get(0).trackingNo //
							+ "\n";

					String trackingNo = items.get(0).trackingNo;
					if (trackingNo.equals(""))
						trackingNo += " ";
					resultModelItem.add(location.getValue() + "\n" + salesOrderList.get(rowIndex).ItemID + "\n"
							+ salesOrderList.get(rowIndex).description + "\n" + trackingNo + "\n");
					rowIndex++;
				}

			}
		}

	}

	private void placeOrderInfoDetail() {

		salesOrder = salesOrderList.get(0).salesOrder;
		soCreatedDate = salesOrderList.get(0).createdDate;
		billToTitle = salesOrderList.get(0).bill_to;
		custPO = salesOrderList.get(0).customerPO;
		shippToAdddress = salesOrderList.get(0).shipToAddress;
		shippToCity = salesOrderList.get(0).shipToCity;
		shippToCountry = salesOrderList.get(0).shipToCountry;
		shippToState = salesOrderList.get(0).shipToState;
		shippToZip = salesOrderList.get(0).shipToZipCode;
		shippToVia = salesOrderList.get(0).shipVia;

		Font font = new Font("Verdana", Font.BOLD, 18);
		JLabel orderLabel = new JLabel("SO Number :" + salesOrder);

		orderLabel.setBounds(40, 0, 500, 50);
		orderLabel.setFont(font);
		orderLabel.setBackground(Constrant.BACKGROUN_COLOR);
		orderDisplayPanel.add(orderLabel);

		String dateStr = (soCreatedDate == null ? "" : soCreatedDate.substring(0, 10));

		JLabel transcactiondate = new JLabel("TransactionDate :" + dateStr);

		transcactiondate.setBounds(40, 40, 400, 50);
		transcactiondate.setFont(font);
		transcactiondate.setBackground(Constrant.BACKGROUN_COLOR);
		orderDisplayPanel.add(transcactiondate);

		JLabel billTo = new JLabel("Bill To :" + billToTitle);

		billTo.setBounds(40, 70, 600, 50);
		billTo.setFont(font);
		billTo.setBackground(Constrant.BACKGROUN_COLOR);
		orderDisplayPanel.add(billTo);

		JLabel custNo = new JLabel("Cust PO# :" + custPO);

		custNo.setBounds(40, 90, 400, 50);
		custNo.setFont(font);
		custNo.setBackground(Constrant.BACKGROUN_COLOR);
		orderDisplayPanel.add(custNo);

		// JLabel shipTo = new JLabel("Ship To :"+ "FITNESS OUTLET\n1067 INDUSTRY
		// DR\nSEATTLE WA \n98188");

		JLabel shipTo = new JLabel("<html>Ship To <p style='margin-left:100'>" + billToTitle + "<br/>" + shippToAdddress
				+ "<br/>" + shippToCity + "  " + shippToState + "<br/>" + shippToZip + "</html>", SwingConstants.LEFT);

		shipTo.setBounds(40, 100, 600, 200);
		shipTo.setFont(font);
		shipTo.setBackground(Constrant.BACKGROUN_COLOR);
		orderDisplayPanel.add(shipTo);

		JLabel ShipVia = new JLabel("Ship Via: " + shippToVia);

		ShipVia.setBounds(40, 240, 600, 50);
		ShipVia.setFont(font);
		ShipVia.setBackground(Constrant.BACKGROUN_COLOR);
		orderDisplayPanel.add(ShipVia);

	}

	public void checkSNExistFrame(List<Itembean> scanitems, String message) {

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

		title = "<html>" + message + " :" + " <br/>";
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
				restoreScanPannel(scanitems);
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

	public void displayShippingResult(String salesOrder, String date, String pro, String content) {

		JFrame.setDefaultLookAndFeelDecorated(false);
		JDialog.setDefaultLookAndFeelDecorated(false);
		frame = new JFrame("Query Pannel");
		// Setting the width and height of frame
		frame.setSize(800, 700);
		frame.setLocationRelativeTo(null);
		frame.setUndecorated(true);
		frame.setResizable(false);

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Constrant.FRAME_BORDER_BACKGROUN_COLOR));

		panel.setBackground(Constrant.BACKGROUN_COLOR);
		// adding panel to frame
		frame.add(panel);

		shippingResult(panel, salesOrder, date, pro, content);

		frame.setBackground(Color.WHITE);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {

				frame.dispose();
				frame.setVisible(false);
			}
		});

	}

	private void shippingResult(JPanel panel, String salesOrder, String date, String pro, String items) {

		String[] itemList = items.split("\n");
		panel.setLayout(null);
		Font font = new Font("Verdana", Font.BOLD, 18);
		// Creating JLabel
		JLabel dateLabel = new JLabel("TransactionDate : " + date.substring(0, 10));

		dateLabel.setBounds(50, 20, 400, 25);
		dateLabel.setFont(font);
		panel.add(dateLabel);

		JLabel modelLabel = new JLabel("Pro # : " + pro);

		modelLabel.setBounds(50, 40, 800, 25);
		modelLabel.setFont(font);
		panel.add(modelLabel);

		JLabel totalCount = new JLabel("Quantity : ");

		totalCount.setBounds(50, 60, 300, 25);
		totalCount.setFont(font);
		panel.add(totalCount);
		// ScrollPane for Result
		JScrollPane scrollZonePane = new JScrollPane();

		scrollZonePane.setBackground(Constrant.TABLE_COLOR);
		// panel.add(scrollZonePane);

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
				String modelNo = ((String) sortedList.get(i)).substring(0, 6);
				rowData[i][0] = modelNo;

				rowData[i][1] = Constrant.models.get(modelNo).Desc;
				rowData[i][2] = sortedList.get(i);
			}
		}

		totalCount.setText("Quantity : " + sortedList.size());

		Object columnNames[] = { "ItemID", "Description", "Serial Number" };
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
		table.getTableHeader().setBackground(Constrant.TABLE_COLOR);
		table.setBackground(Constrant.TABLE_COLOR);
		table.setFont(font);
		table.setRowHeight(40);

		TableColumn quantity = table.getColumnModel().getColumn(0);
		quantity.setPreferredWidth(20);
		TableColumn itemId = table.getColumnModel().getColumn(1);
		itemId.setPreferredWidth(200);

		int tableSize = 50 * rowData.length + 20;
		if (tableSize > 450)
			tableSize = 450;
		scrollZonePane.setBounds(33, 100, 750, tableSize);
		scrollZonePane.setViewportView(table);
		panel.add(scrollZonePane);

		// Creating Report button
		report = new JButton("Submit");
		report.setFont(font);
		report.setBounds(600, 570, 180, 50);
		report.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				if (loadingframe != null) {
					loadingframe.updateTitle("Report Data...");
					loading.setValue(80);
					loadingframe.setVisible(true);
				}

				isOrderClosed = true;
				report.setEnabled(false);
				scanItems = new ArrayList<Historybean>();
				String SalesOrder = salesOrderList.get(0).salesOrder;
				String date = salesOrderList.get(0).createdDate;
				String billToTitle = salesOrderList.get(0).bill_to;
				String custPO = salesOrderList.get(0).customerPO;
				String shippToAdddress = salesOrderList.get(0).shipToAddress;
				String shippToCity = salesOrderList.get(0).shipToCity;
				String shippToCountry = salesOrderList.get(0).shipToCountry;
				String shippToState = salesOrderList.get(0).shipToState;
				String shippToZip = salesOrderList.get(0).shipToZipCode;
				String shippToVia = salesOrderList.get(0).shipVia;

				String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());

				for (String item : itemList) {
					Historybean _item = new Historybean();

					_item.SN = item;
					_item.shippingDate = timeStamp;
					_item.createdDate = date;
					_item.location = "999";
					_item.modelNo = item.substring(0, 6);

					_item.salesOrder = salesOrder;
					_item.trackingNo = pro;
					_item.billTo = billToTitle;
					_item.shipCity = shippToCity;
					_item.shipState = shippToState;
					_item.shipVia = shippToVia;

					scanItems.add(_item);

				}

				for (int i = 0; i < salesOrderList.size(); i++) {
					CustOrderbean order = salesOrderList.get(i);
					order.createdDate = timeStamp;
					order.closed = true;
					// order.TrackingNo = proNumber.getText().toString();
					salesOrderList.set(i, order);
				}

				int rowIndex = 0;
				for (Map.Entry<String, Integer> location : map.entrySet()) {
					// orderItemsInfo += location.getValue() +" " +
					// salesOrderList.get(rowIndex).ItemID +" "+
					// salesOrderList.get(rowIndex).description + " "+ scanItems.get(0).trackingNo
					// +"\n";
					rowIndex++;
				}

				for (Historybean h : scanItems) {
					historyItemsInfo += h.SN + "\n";
				}

				EventQueue.invokeLater(new Runnable() {
					public void run() {

						// displayLoadingBar();

						shippingItems(scanItems);
						// getShippgingItems(salesOrder, false);
						// updateSalesOrder();

					}
				});

			}
		});

		panel.add(report);

		// Creating Report button
		JButton prev = new JButton("Back");
		prev.setFont(font);
		prev.setBounds(600, 630, 80, 50);
		prev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				prevContent = inputSN.getText().toString();
				frame.dispose();
				frame.setVisible(false);
				// scanInfo(salesOrder);
				restoreScanPannel(null);
			}
		});

		panel.add(prev);

		// Creating Report button
		JButton exit = new JButton("Exit");
		exit.setFont(font);
		exit.setBounds(700, 630, 80, 50);
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				scanner.setEnabled(true);
				frame.dispose();
				frame.setVisible(false);
			}
		});

		panel.add(exit);
	}

	private void scanPannel(JPanel panel, String salesOrder) {

		panel.setLayout(null);
		Font font = new Font("Verdana", Font.BOLD, 18);
		// Creating JLabel
		JLabel shippingLabel = new JLabel("TransactionDate : ");

		shippingLabel.setBounds(50, 20, 200, 25);
		shippingLabel.setFont(font);
		panel.add(shippingLabel);

		JTextField shippingDate = new JTextField(20);
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		shippingDate.setText(timeStamp);
		shippingDate.setFont(font);
		shippingDate.setBounds(250, 20, 250, 50);
		panel.add(shippingDate);

		// Creating JLabel
		JLabel proLabel = new JLabel("PRO # ");

		proLabel.setBounds(50, 80, 200, 25);
		proLabel.setFont(font);
		panel.add(proLabel);

		JTextField proNumber = new JTextField(20);

		scanResultFrame.addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				proNumber.requestFocus();
			}
		});
		proNumber.setText(prevTrackingNo);
		proNumber.setFont(font);
		proNumber.setBounds(250, 70, 250, 50);
		panel.add(proNumber);

		inputSN = new JTextArea(20, 15);
		String content = "";

		inputSN.setText(prevContent);
		String[] item = prevContent.split("\n");
		snRepeatSet = new HashSet<String>();
		int len = 0;
		if (!prevContent.equals("")) {

			len = item.length;
			// modelScanCurMap.clear();
			for (String s : item) {
				snRepeatSet.add(s);

				/*
				 * String modelNo = s.substring(0,6); if(!modelScanCurMap.containsKey(modelNo))
				 * modelScanCurMap.put(modelNo, 1); else modelScanCurMap.put(modelNo,
				 * modelScanCurMap.get(modelNo) + 1);
				 */

			}

		}

		lModelError = new JLabel("");

		lModelError.setBounds(50, 150, 200, 400);
		lModelError.setFont(font);
		panel.add(lModelError);

		// Creating JLabel
		lCount = new JLabel("");

		orderCurCount = len;
		lCount.setText(setModelScanCountLabel(orderCurCount));

		lCount.setBounds(50, 70, 200, 500);
		lCount.setFont(font);
		panel.add(lCount);

		JCheckBox unit = null;
		int checkboxIdx = 540;

		JCheckBox[] checkBoxItems = new JCheckBox[checkedItemNoSN.size()];
		if (checkedItemNoSN.size() > 0) {

			for (int i = 0; i < checkedItemNoSN.size(); i++) {
				CustOrderbean curCheckItem = checkedItemNoSN.get(i);
				checkBoxItems[i] = new JCheckBox();
				checkBoxItems[i].setFont(font);
				checkBoxItems[i].setBackground(Constrant.BACKGROUN_COLOR);
				checkBoxItems[i].setText(curCheckItem.ItemID + "(" + 0 + "/" + map.get(curCheckItem.ItemID) + ")");
				checkBoxItems[i].setBounds(30, checkboxIdx, 200, 30);
				panel.add(checkBoxItems[i]);
				final int curIdex = i;
				checkboxIdx += 27;

				checkBoxItems[i].addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == 1) {
							modelScanCurMap.put(curCheckItem.ItemID, map.get(curCheckItem.ItemID));
							checkBoxItems[curIdex].setText(curCheckItem.ItemID + "(" + map.get(curCheckItem.ItemID)
									+ "/" + map.get(curCheckItem.ItemID) + ")");
							checkBoxItems[curIdex].setSelected(true);
							orderCurCount += map.get(curCheckItem.ItemID);
							lCount.setText(setModelScanCountLabel(orderCurCount));
							// scanResultFrame.invalidate();
							// scanResultFrame.validate();
							// scanResultFrame.repaint();

						} else {
							modelScanCurMap.put(curCheckItem.ItemID, map.get(curCheckItem.ItemID));
							checkBoxItems[curIdex]
									.setText(curCheckItem.ItemID + "(" + 0 + "/" + map.get(curCheckItem.ItemID) + ")");
							checkBoxItems[curIdex].setSelected(false);
							orderCurCount -= map.get(curCheckItem.ItemID);
							lCount.setText(setModelScanCountLabel(orderCurCount));
						}
					}
				});
			}
		}

		JScrollPane scrollPanel1 = new JScrollPane(inputSN);
		scrollPanel1.setBounds(250, 150, 250, 500);
		inputSN.setFont(font);

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
					model = item[item.length - 1].substring(0, 6);

					if (modelScanCurMap.get(model) == null)
						lenError = true;
					else
						curModelCnt = modelScanCurMap.get(model);
				}

				if (!map.containsKey(model)) {
					lModelError.setForeground(Color.RED);
					lModelError.setText("Model Error.");
				}

				if (!snRepeatSet.contains(item[item.length - 1]) && item[item.length - 1].length() == 16
						&& snRepeatSet.size() <= orderTotalCount && map.containsKey(model)
						&& curModelCnt < map.get(model) && !lenError) {
					lModelError.setText("");
					modelScanCurMap.put(model, modelScanCurMap.get(model) + 1);
					snRepeatSet.add(item[item.length - 1]);
					orderCurCount++;
				} else {
					lenError = true;
					prev = prev.substring(0, prev.length() - (item[item.length - 1].length()) - 1);

				}

				if (lenError) {

					inputSN.setText(prev);
				} else {

					lCount.setForeground(Color.BLACK);
					lCount.setText(setModelScanCountLabel(orderCurCount));

				}
			}
		});

		panel.add(scrollPanel1);

		InputMap inputPro = proNumber.getInputMap();
		KeyStroke enterPro = KeyStroke.getKeyStroke("ENTER");
		KeyStroke shiftEnterPro = KeyStroke.getKeyStroke("shift ENTER");
		inputPro.put(enterPro, INSERT_BREAK); // input.get(enter)) = "insert-break"
		inputPro.put(shiftEnterPro, TEXT_SUBMIT);
		ActionMap proAction = proNumber.getActionMap();
		proAction.put(INSERT_BREAK, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				prevTrackingNo = proNumber.getText();
				inputSN.grabFocus();
			}
		});

		// Creating Query button
		JButton queryButton = new JButton("Save");
		queryButton.setFont(font);
		queryButton.setBounds(250, 670, 110, 50);
		queryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Constrant.serial_list = "SO:" + salesOrder + "\n PRO:" + proNumber.getText().toString()
						+ "\n Shipping SN : \n" + inputSN.getText().toString();

				boolean verifyOrder = checkOrder(inputSN.getText().toString());
				prevContent = inputSN.getText().toString();
				shipDate = shippingDate.getText().toString();
				trackingNo = proNumber.getText().toString();
				if (verifyOrder) {
					scanResultFrame.dispose();
					scanResultFrame.setVisible(false);

					String[] itemList = inputSN.getText().toString().split("\n");
					List<Itembean> items = new ArrayList<Itembean>();

					String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(Calendar.getInstance().getTime());
					for (String item : itemList) {
						Itembean _item = new Itembean();

						_item.SN = item;
						_item.ModelNo = item.substring(0, 6);
						items.add(_item);

					}

					if (loadingframe != null) {
						loadingframe.updateTitle("Report Data...");
						loading.setValue(80);
						loadingframe.setVisible(true);
					}
					EventQueue.invokeLater(new Runnable() {
						public void run() {

							checkItemExitsZone2(items);
						}
					});

				}
			}
		});

		panel.add(queryButton);

		// Creating clear button
		JButton resetButton = new JButton("Clear");
		resetButton.setFont(font);
		resetButton.setBounds(380, 670, 120, 50);
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int result = -1;
				if (inputSN.getText().toString().length() > 0) {
					result = JOptionPane.showConfirmDialog(frame, "Do you want to clear the all item?", "",
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (result == JOptionPane.YES_OPTION) {
						inputSN.setText("");
						snRepeatSet.clear();
						for (Map.Entry<String, Integer> location : modelScanCurMap.entrySet()) {
							modelScanCurMap.put(location.getKey(), 0);
						}
						lCount.setText(setModelScanCountLabel(0));
						lCount.setForeground(Color.BLACK);
					}
				}
			}
		});

		panel.add(resetButton);

		// Creating Exit button
		JButton exitButton = new JButton("Exit");
		exitButton.setFont(font);
		exitButton.setBounds(50, 670, 180, 50);
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				scanner.setEnabled(true);
				scanResultFrame.dispose();
				scanResultFrame.setVisible(false);
			}
		});

		panel.add(exitButton);

	}

	private void exceuteCallback() {

		// it callback will be run when query the model from inventory table
		fgRepositoryImplRetrofit = new FGRepositoryImplRetrofit();
		fgRepositoryImplRetrofit.setinventoryServiceCallBackFunction(new InventoryCallBackFunction() {

			@Override
			public void resultCode(int code) {
				if (code == HttpRequestCode.HTTP_REQUEST_INSERT_DATABASE_ERROR) {
					JOptionPane.showMessageDialog(null, "Items already exist.");

				}

			}

			@Override
			public void getInventoryItems(List<Itembean> _items) {

			}

			@Override
			public void checkInventoryZone2Items(int result, List<Itembean> items) {

				timer.stop();
				timer = null;
				isTimeOut = 1;

				if (loadingframe != null) {
					loadingframe.setVisible(false);
					loadingframe.dispose();
				}
				prevContent = inputSN.getText().toString();
				if (result == HttpRequestCode.HTTP_REQUEST_OK) {
					String[] scanItems = inputSN.getText().toString().split("\n");
					if (items.size() == 0)
						displayShippingResult(salesOrder, shipDate, trackingNo, inputSN.getText().toString());
					else {
						// JOptionPane.showMessageDialog(null, "Some items don't exist on Zone 2.");
						// restoreScanPannel(items);
						checkSNExistFrame(items, "Some items don't exist on Zone 2.");
					}

				} else if (result == HttpRequestCode.HTTP_REQUEST_ACCEPTED) {
					// JOptionPane.showMessageDialog(null, "Some items don't exist on PeachTree.");
					// restoreScanPannel(items);
					checkSNExistFrame(items, "Some items don't exist on PeachTree.");
				}

			}

			@Override
			public void checkMoveItems(List<Itembean> items) {
				// TODO Auto-generated method stub

			}

			@Override
			public void checkReceiveItem(List<Itembean> items) {
				// TODO Auto-generated method stub

			}

			@Override
			public void exception(String error) {
				restoreScanPannel(null);
				NetWorkHandler.getInstance();
				NetWorkHandler.displayError(loadingframe);
				prevContent = inputSN.getText().toString();

			}
		});

		ordersRepositoryImplRetrofit = new OrdersRepositoryImplRetrofit();
		ordersRepositoryImplRetrofit.setCustOrderServiceCallBackFunction(new CustOrderCallBackFunction() {

			@Override
			public void resultCode(int code) {
				// TODO Auto-generated method stub
				if (code == HttpRequestCode.HTTP_REQUEST_INSERT_DATABASE_ERROR) {
					JOptionPane.showMessageDialog(null, "Items already exist.");

				}

			}

			@Override
			public void updateSalesOrder(List<CustOrderbean> orders) {

				if (orders.isEmpty()) {
					JOptionPane.showMessageDialog(null, "The sales order doesn't exist !");

					if (loadingframe != null) {
						loadingframe.setVisible(false);
						loadingframe.dispose();
					}

					frame.dispose();
					frame.setVisible(false);
					shippingConfirm = null;

				} else {
					frame.dispose();
					frame.setVisible(false);

					// if (isOrderClosed) {
					pickingItems = new ArrayList<PickingItem>();
					salesOrderList = new ArrayList<CustOrderbean>();
					checkedItemNoSN = new ArrayList<CustOrderbean>();
					map = new LinkedHashMap<String, Integer>();
					modelScanCurMap = new LinkedHashMap<String, Integer>();
					OrderModelmap = new TreeMap<String, String>();
					// salesOrderList = orders;
					for (CustOrderbean item : orders) {
						int count = Integer.valueOf(item.quantity);
						// System.out.println("item.ItemID:" + item.ItemID);

						if (item.ItemID != null /*
												 * && !item.ItemID.contains("PL") && item.ItemID.length() == 6 /*&&
												 * !WeightPlateUtil.isWeightPlate(item.ItemID) &&
												 * !WeightPlateUtil.isCalfSupport(item.ItemID)
												 */) {
							if (WeightPlateUtil.isCheckBoxNoSNitem(item.ItemID) || item.ItemID.contains("PL")
									|| item.ItemID.length() > 6)
								checkedItemNoSN.add(item);
							salesOrderList.add(item);
							if (map.containsKey(item.ItemID)) {

								count += map.get(item.ItemID);
								map.put(item.ItemID, count);
								orderTotalCount += count;

							} else {

								orderTotalCount += count;
								map.put(item.ItemID, count);
								OrderModelmap.put(item.ItemID, item.description);
								modelScanCurMap.put(item.ItemID, 0);
							}
						}
					}

					if (loadingframe != null) {
						loadingframe.setVisible(false);
						loadingframe.dispose();
					}

					displayOrderInfo(null);

				}
			}

		});

		historyRepositoryImplRetrofit = new HistoryRepositoryImplRetrofit();
		historyRepositoryImplRetrofit.setHistoryServiceCallBackFunction(new HistoryCallBackFunction() {

			@Override
			public void resultCode(int code) {
				if (code == HttpRequestCode.HTTP_REQUEST_INSERT_DATABASE_ERROR) {
					JOptionPane.showMessageDialog(null, "Items already exist.");

				}

			}

			@Override
			public void getHistoryItems(List<Historybean> _items) {

				timer.stop();
				timer = null;
				isTimeOut = 1;
				if (!_items.isEmpty()) {
					Constrant.serial_list = "";
					JOptionPane.showMessageDialog(null, "Report data success.");

					if (checkedItemNoSN.size() > 0) {
						List<Palletbean> pallents = new ArrayList<Palletbean>();
						for (CustOrderbean item : checkedItemNoSN) {
							Palletbean palletbean = new Palletbean();
							palletbean.createdDate =  _items.get(0).createdDate;
							palletbean.billTo = item.bill_to;
							palletbean.itemID = item.ItemID;
							palletbean.description = item.description;
							palletbean.qty = item.quantity;
							palletbean.salesOrder = item.salesOrder;
							palletbean.shipCity = item.shipToCity;
							palletbean.shippedDate = _items.get(0).shippingDate;
							palletbean.shipState = item.shipToState;
							palletbean.shipVia = item.shipVia;
							palletbean.trackingNo = _items.get(0).trackingNo;
							pallents.add(palletbean);
						}

						submitPalletItems(pallents);
					}

					// if(orderFrame != null)
					// orderFrame.setVisible(true);
					int rowIndex = 0;
					for (Map.Entry<String, Integer> location : map.entrySet()) {
						// orderItemsInfo += location.getValue() +" " +
						// salesOrderList.get(rowIndex).ItemID +" "+
						// salesOrderList.get(rowIndex).description + " "+ items.get(0).trackingNo
						// +"\n";

						String modelDes = salesOrderList.get(rowIndex).description;

						resultModelItem.add(location.getValue() + "\n" + salesOrderList.get(rowIndex).ItemID + "\n"
								+ modelDes + "\n" + scanItems.get(0).trackingNo + "\n");
						rowIndex++;
					}

					displayItemsfromHistory(scanItems);

				}
			}

			@Override
			public void checkHistoryItemsBySalesOrder(List<Historybean> _items) {
				if (_items != null && !_items.isEmpty()) {
					isOrderClosed = true;
					items = _items;

				} // Check SaleOrder Info

				// if(!isOrderClosed)
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							querySalesOrder(salesOrder);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

			}

			@Override
			public void exception(String error) {
				frame.dispose();
				frame.setVisible(false);

				if (scanResultFrame != null) {
					scanResultFrame.dispose();
					scanResultFrame.setVisible(false);
				}
				restoreScanPannel(null);
				NetWorkHandler.displayError(loadingframe);
				prevContent = inputSN.getText().toString();

			}

			@Override
			public void getDailyShippingItems(List<spirit.fitness.scanner.model.DailyShippingReportbean> items) {
				// TODO Auto-generated method stub

			}

		});

		palletRepositoryImplRetrofit = new PalletRepositoryImplRetrofit();
		palletRepositoryImplRetrofit.setPalletServiceCallBackFunction(new PalletCallBackFunction() {

			@Override
			public void resultCode(int code) {
				// TODO Auto-generated method stub

			}

			@Override
			public void addPallet(List<Palletbean> items) {
				// TODO Auto-generated method stub

			}

			@Override
			public void getPalletItems(List<Palletbean> items) {
				// TODO Auto-generated method stub

			}
		});

	}

	private boolean checkOrder(String content) {

		String[] sn = content.toString().split("\n");
		System.out.println("SN size :" + sn.length);
		if (orderCurCount != orderTotalCount) {
			JOptionPane.showMessageDialog(null, "Quantity Error! Please confirm the items quantiy.");
			return false;
		}

		for (String s : sn) {
			String modelNo = "";
			if (s.length() != 16) {
				JOptionPane.showMessageDialog(null, "Please enter correct data.");
				return false;
			} else {
				modelNo = s.substring(0, 6);

				if (map.get(modelNo) == null) {
					JOptionPane.showMessageDialog(null, "Model Error!");
					return false;
				}
			}

		}

		return true;
	}

	private String setModelScanCountLabel(int curCount) {
		String modelQty = "<html>" + "Total : " + curCount + "/" + String.valueOf(orderTotalCount) + " </br>";
		for (Map.Entry<String, Integer> location : map.entrySet()) {

			if (WeightPlateUtil.isCheckBoxNoSNitem(location.getKey()) || location.getKey().contains("PL")
					|| location.getKey().length() > 6)
				continue;

			int cnt = 0;

			if (modelScanCurMap.get(location.getKey()) != null)
				cnt = modelScanCurMap.get(location.getKey());
			modelQty += location.getKey() + "(" + cnt + "/" + location.getValue() + ") </br>";
		}
		modelQty = modelQty + "</br></html>";
		return modelQty;
	}

	/*
	 * private void printer(String saleOrder, String date, String billTo, String
	 * shipTo, String itemsInfo) {
	 * 
	 * String content = "Sales Order : " + saleOrder + "\n" + "TransactionDate : " +
	 * date + "\n" + "Bill To : " + billTo + "\n" + "Ship To : " + shipTo + "\n";
	 * List<String> headersList = Arrays.asList("Qty", "Item", "Model", "PRO#");
	 * 
	 * List<List<String>> rowsList = new ArrayList<List<String>>(); for (String s :
	 * resultModelItem) { String[] rowdata = s.split("\n");
	 * rowsList.add(Arrays.asList(rowdata)); }
	 * 
	 * // String result = PrintTableUtil.printReport(headersList, rowsList); String
	 * result = PrintTableUtil.noBorad(headersList, rowsList); content += result +
	 * itemsInfo; System.out.println(content);
	 * 
	 * PrinterHelper print = new PrinterHelper(); print.printTable(content);
	 * 
	 * }
	 */

	/*
	 * private void printer(String saleOrder, String date, String billTo, String
	 * shipTo, String itemsInfo) {
	 * 
	 * String content = "Sales Order : " + saleOrder + "\n" + "TransactionDate : " +
	 * date + "\n" + "Bill To : " + billTo + "\n" + "Ship To : " + shipTo + "\n";
	 * 
	 * 
	 * String header =
	 * " ____________________________________________________________________________________________\n"
	 * ; String title =
	 * " | Qty |    Item   |                     Model                          |                            PRO                    \n"
	 * ; String bolder =
	 * " ▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔\n"
	 * ;
	 * 
	 * String items = "";
	 * 
	 * for (String s : resultModelItem) { String[] rowdata = s.split("\n"); String
	 * line = "";
	 * 
	 * int spaceModelPrefixLen = 0;
	 * 
	 * String spaceModelPrefix = ""; String spaceModelSuffix = "";
	 * if(rowdata[2].length()<38) spaceModelPrefixLen = (38 - rowdata[2].length());
	 * 
	 * 
	 * //if(modelTitleLen < rowdata[2].length()) // modelTitleLen =
	 * spaceModelPrefixLen - rowdata[2].length();
	 * 
	 * //spaceModelPrefixLen += Math.abs(modelTitleLen);
	 * 
	 * 
	 * 
	 * for(int i =0; i <rowdata.length ; i++) { String modelTitle = rowdata[2] +
	 * spaceModelPrefix; if(i == 2) { line += rowdata[i] + spaceModelPrefix; } else
	 * if(i == 3) {
	 * 
	 * line += rowdata[i];
	 * 
	 * }else line += "   "+rowdata[i] +"    "; }
	 * 
	 * items += line +"\n"; }
	 * 
	 * 
	 * 
	 * items += bolder; // String result = PrintTableUtil.printReport(headersList,
	 * rowsList); //String result = PrintTableUtil.noBorad(headersList, rowsList);
	 * content += header + title + bolder + items +itemsInfo;
	 * 
	 * System.out.println(content);
	 * 
	 * PrinterHelper print = new PrinterHelper(); print.printTable(content);
	 * 
	 * }
	 */

	private void printer(String saleOrder, String date, String billTo, String shipTo, String itemsInfo) {

		String content = "Sales Order : " + saleOrder + "\n" + "TransactionDate : " + date + "\n" + "Bill To : "
				+ billTo + "\n" + "Ship To : " + shipTo + "\n" + "▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁\n";
		List<String> headersList = Arrays.asList("Qty", "Item", "Model", "PRO#");

		List<List<String>> rowsList = new ArrayList<List<String>>();
		for (String s : resultModelItem) {
			String[] rowdata = s.split("\n");
			rowsList.add(Arrays.asList(rowdata));
		}

		String result = PrintTableUtil.printReport(headersList, rowsList);
		// String result = PrintTableUtil.noBorad(headersList, rowsList);
		content += result + "▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔\n" + itemsInfo;

		// System.out.println(content);

		// PrinterHelper print = new PrinterHelper();
		// print.printTable(content);
		if (printer == null)
			printer = new PrintPreviewUitl(content);

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					printer.printContent();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// print.setVisible(true);
	}

	/*
	 * private void printer(String saleOrder, String date, String billTo, String
	 * shipTo, String itemsInfo) {
	 * 
	 * String content = "Sales Order : " + saleOrder + "\n" + "TransactionDate : " +
	 * date + "\n" + "Bill To : " + billTo + "\n" + "Ship To : " + shipTo + "\n"; //
	 * List<String> headersList = Arrays.asList("Qty", "Item", "Model", "PRO#");
	 * 
	 * /* List<List<String>> rowsList = new ArrayList<List<String>>(); for (String s
	 * : resultModelItem) { String[] rowdata = s.split("\n");
	 * rowsList.add(Arrays.asList(rowdata)); }
	 */

	/*
	 * String header =
	 * " ____________________________________________________________________________________________\n"
	 * ; String title =
	 * " | Qty |    Item   |                     Model                          |                            PRO                    \n"
	 * ; String bolder =
	 * " ▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔▔\n"
	 * ;
	 * 
	 * String items = "";
	 * 
	 * int modelTitleLen = 0; // int limitLen = 38; /* for (String s :
	 * resultModelItem) { String[] rowdata = s.split("\n"); String line = "";
	 * 
	 * int spaceModelPrefixLen = 0;
	 * 
	 * String spaceModelPrefix = ""; String spaceModelSuffix = "";
	 * if(rowdata[2].length()<38) spaceModelPrefixLen = (38 - rowdata[2].length());
	 * 
	 * 
	 * //if(modelTitleLen < rowdata[2].length()) // modelTitleLen =
	 * spaceModelPrefixLen - rowdata[2].length();
	 * 
	 * //spaceModelPrefixLen += Math.abs(modelTitleLen);
	 * 
	 * 
	 * 
	 * for(int i =0; i <rowdata.length ; i++) { String modelTitle = rowdata[2] +
	 * spaceModelPrefix; if(i == 2) { line += rowdata[i] + spaceModelPrefix; } else
	 * if(i == 3) {
	 * 
	 * line += rowdata[i];
	 * 
	 * }else line += "   "+rowdata[i] +"    "; }
	 * 
	 * items += line +"\n"; }
	 */

	/*
	 * String qty = "    | "; String itemID = "          | "; String model =
	 * "          |"; String trackingNo = ""; for (ShippedPrintItem shippedPrintItem
	 * : printItems) { String line = "   "; line += "" + shippedPrintItem.getQty() +
	 * qty.substring(String.valueOf(shippedPrintItem.getQty()).length()); line +=
	 * shippedPrintItem.getItemID() +
	 * itemID.substring(shippedPrintItem.getItemID().length()); line +=
	 * shippedPrintItem.getModel(); line += shippedPrintItem.getTrackingNo(); items
	 * += line + "\n"; }
	 * 
	 * items += bolder; // String result = PrintTableUtil.printReport(headersList,
	 * rowsList); // String result = PrintTableUtil.noBorad(headersList, rowsList);
	 * content += header + title + bolder + items + itemsInfo;
	 * 
	 * System.out.println(content);
	 * 
	 * PrinterHelper print = new PrinterHelper(); print.printTable(content);
	 * 
	 * }
	 */

	private void restoreScanPannel(List<Itembean> items) {

		if (scanResultFrame != null)
			scanResultFrame.setVisible(true);

		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				inputSN.grabFocus();
				inputSN.requestFocus();// or inWindow
			}
		});
		// scan items not exits on Zone2
		String updateTxt = "";
		// modelScanCurMap.clear();

		if (items == null) {
			String[] item = prevContent.split("\n");
			// modelScanCurMap.clear();
			orderCurCount = 0;

			for (Map.Entry<String, Integer> location : modelScanCurMap.entrySet()) {
				modelScanCurMap.put(location.getKey(), 0);
			}
			for (CustOrderbean pallet : checkedItemNoSN) {
				orderCurCount += Integer.valueOf(pallet.quantity);
			}
			for (String s : item) {
				updateTxt += s + "\n";

				String modelNo = s.substring(0, 6);
				if (!modelScanCurMap.containsKey(modelNo))
					modelScanCurMap.put(modelNo, 1);
				else
					modelScanCurMap.put(modelNo, modelScanCurMap.get(modelNo) + 1);
				orderCurCount++;
			}

			lCount.setText(setModelScanCountLabel(orderCurCount));
		} else {
			// modelScanCurMap.clear();

			// initial modelScanCurMap model current count to 0
			for (Map.Entry<String, Integer> location : modelScanCurMap.entrySet()) {
				modelScanCurMap.put(location.getKey(), 0);
			}
			snRepeatSet.clear();
			String[] prevText = prevContent.split("\n");
			// updateTxt = prevText[0] + "\n";

			HashSet<String> itemError = new HashSet<String>();

			for (Itembean i : items) {
				itemError.add(i.SN);
			}
			for (String s : prevText) {
				if (itemError.contains(s)) {
					continue;
				} else {
					updateTxt += s + "\n";
					snRepeatSet.add(s);
				}

			}

			String[] itemSize = updateTxt.split("\n");
			prevContent = updateTxt;

			orderCurCount = 0;

			for (CustOrderbean pallet : checkedItemNoSN) {
				orderCurCount += Integer.valueOf(pallet.quantity);
			}
			if (!updateTxt.equals("")) {
				for (String s : itemSize) {
					String modelNo = s.substring(0, 6);
					if (!modelScanCurMap.containsKey(modelNo))
						modelScanCurMap.put(modelNo, 1);
					else
						modelScanCurMap.put(modelNo, modelScanCurMap.get(modelNo) + 1);
					orderCurCount++;
				}
				lCount.setText(setModelScanCountLabel(orderCurCount));
			} else {
				inputSN.setText(updateTxt);
				lCount.setText(setModelScanCountLabel(0));
			}

		}

		inputSN.setText(updateTxt);

	}

}
