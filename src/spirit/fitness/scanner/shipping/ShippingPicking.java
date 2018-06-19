package spirit.fitness.scanner.shipping;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.Sides;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ProgressMonitor;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import spirit.fitness.scanner.AppMenu;
import spirit.fitness.scanner.common.Constrant;
import spirit.fitness.scanner.common.HttpRequestCode;
import spirit.fitness.scanner.restful.FGRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.HistoryRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.HttpRestApi;
import spirit.fitness.scanner.restful.OrdersRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.listener.CustOrderCallBackFunction;
import spirit.fitness.scanner.restful.listener.HistoryCallBackFunction;
import spirit.fitness.scanner.restful.listener.InventoryCallBackFunction;
import spirit.fitness.scanner.search.QueryResult;
import spirit.fitness.scanner.util.LoadingFrameHelper;
import spirit.fitness.scanner.util.LocationHelper;
import spirit.fitness.scanner.util.ModelNoUtil;
import spirit.fitness.scanner.util.PrintTableUtil;
import spirit.fitness.scanner.util.PrinterHelper;
import spirit.fitness.scanner.util.WeightPlateUtil;
import spirit.fitness.scanner.zonepannel.ZoneMenu;
import spirit.fitness.scanner.model.CustOrderbean;
import spirit.fitness.scanner.model.DailyShippingReportbean;
import spirit.fitness.scanner.model.Historybean;
import spirit.fitness.scanner.model.Itembean;
import spirit.fitness.scanner.model.Locationbean;
import spirit.fitness.scanner.model.ModelZone2bean;
import spirit.fitness.scanner.model.PickingItem;
import spirit.fitness.scanner.report.ModelZone2Report;

public class ShippingPicking {

	private static ShippingPicking shippingPicking = null;

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
	private boolean isOrderClosed;

	private String prevContent = "";

	// Key:modelID, value:quality
	private LinkedHashMap<String, Integer> map;

	// Key:modelID, value:description
	private TreeMap<String, String> OrderModelmap;

	// Key:modelID, value:response items
	private LinkedHashMap<String, List<Itembean>> inventoryModelmap;

	// Key:Location, value:quantity
	private LinkedHashMap<String, LinkedHashMap<String, Integer>> locMap = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();

	private int orderTotalCount = 0;
	// private List<CustOrderbean> salesOrderList;
	private List<CustOrderbean> salesOrderList;
	private List<PickingItem> pickingItems;
	private List<Historybean> items;

	private JProgressBar loading;
	private LoadingFrameHelper loadingframe;

	private FGRepositoryImplRetrofit fgRepositoryImplRetrofit;
	private OrdersRepositoryImplRetrofit ordersRepositoryImplRetrofit;
	private HistoryRepositoryImplRetrofit historyRepositoryImplRetrofit;

	public ShippingPicking() {

		exceuteCallback();
		orderInfo();
	}

	public static ShippingPicking getInstance() {
		if (shippingPicking == null) {
			shippingPicking = new ShippingPicking();
		}
		return shippingPicking;
	}

	public static boolean isExit() {
		return shippingPicking != null;
	}

	private void displayPickingOrderInfo(List<Itembean> items) {

		// Locationbean title = Constrant.locations.get(locationbead);

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

		placeOrderInfoAndInventoryLocation();

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
		}

	}

	// Get items from History table
	private List<Historybean> getShippgingItems(String salesOrder) {
		// List<Historybean> items = null;
		try {
			items = historyRepositoryImplRetrofit.getItemsBySalesOrder(salesOrder);

			// else
			// JOptionPane.showMessageDialog(null, "The sales order is closed !");
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return items;
	}

	private void queryByModelAndCount(String modelNo, int count) {

		try {

			fgRepositoryImplRetrofit.getItemsByModelAndCount(modelNo, count);

		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

		findSalesOrder(panel);

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

	private void findSalesOrder(JPanel panel) {

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

								salesOrder = (salesOrderNo.getText().toString().trim());
								// querySalesOrder(salesOrderNo.getText().toString().trim());
								getShippgingItems(salesOrder);
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
				shippingPicking = null;

				frame.dispose();
				frame.setVisible(false);
			}
		});

		panel.add(exitButton);
	}

	private void placeOrderInfoAndInventoryLocation() {

		salesOrder = salesOrderList.get(0).salesOrder;

		isOrderClosed = salesOrderList.get(0).closed;
		orderDisplayPanel.setLayout(null);

		Font font = new Font("Verdana", Font.BOLD, 18);

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

			fillModelMapLocation();

			// Display the all items into table

			int colsize = 4;

			final Object[][] orderModelItems = new Object[pickingItems.size()][colsize];

			int rowIndex = 0;
			for (PickingItem item : pickingItems) {
				String[] zoneCode = null;
				String zoneDes = "";
				HashMap<String, Integer> loc = locMap.get(item.modelID);
				for (int j = 0; j < colsize; j++) {
					
					if(item.modelID.equals("115816"))
						item.modelID = "450887";
					
					
					orderModelItems[rowIndex][0] = loc.get(item.location);
					orderModelItems[rowIndex][1] = item.modelID;

				
					if (!WeightPlateUtil.isModelParts(item.modelID))
						orderModelItems[rowIndex][2] = item.modelDes;
					else
						orderModelItems[rowIndex][2] = item.modelDes
								+ WeightPlateUtil.modelAppendWithPart(item.modelDes);
					orderModelItems[rowIndex][3] = "  ["
							+ LocationHelper.DisplayZoneCode(LocationHelper.MapZoneCode(item.location)) + "] "
							+ item.location;

				}
				rowIndex++;
			}

			// modelLabel.setText("SalesOrder : "+_items.get(0).SalesOrder);
			// modelText.setText(" TOTAL: "+totalCount);
			final Class[] packingColumnClass = new Class[] { Integer.class, String.class, String.class, String.class };

			Object packingColumnNames[] = { "Qty", "ItemID", "Description", "Location" };

			DefaultTableModel packingModel = new DefaultTableModel(orderModelItems, packingColumnNames) {
				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}

				@Override
				public Class<?> getColumnClass(int columnIndex) {
					return packingColumnClass[columnIndex];
				}
			};

			// if(packingTable == null)
			JTable packingTable = new JTable(packingModel);
			// else
			// packingModel.fireTableDataChanged();
			TableColumn quantity = packingTable.getColumnModel().getColumn(0);
			quantity.setPreferredWidth(20);
			TableColumn itemId = packingTable.getColumnModel().getColumn(1);
			itemId.setPreferredWidth(40);
			TableColumn modelTitle = packingTable.getColumnModel().getColumn(2);
			modelTitle.setPreferredWidth(600);
			TableColumn locationCol = packingTable.getColumnModel().getColumn(3);
			locationCol.setPreferredWidth(100);
			packingTable.setCellSelectionEnabled(false);
			packingTable.setColumnSelectionAllowed(false);
			packingTable.setFocusable(false);

			packingTable.getTableHeader().setBackground(Constrant.TABLE_COLOR);
			packingTable.getTableHeader().setFont(font);

			packingTable.setBackground(Constrant.TABLE_COLOR);
			packingTable.setRowHeight(40);
			packingTable.setFont(font);

			int tableSize = 50 * orderModelItems.length + 20;
			if (tableSize > 400)
				tableSize = 400;

			scrollZonePane.setBounds(33, 20, 990, tableSize);

			scrollZonePane.setViewportView(packingTable);

			int txtSize = 280 + 50 * orderModelItems.length;
			if (txtSize > 430)
				txtSize = 430;
			total.setBounds(30, txtSize, 200, 50);
			total.setText("Total : " + String.valueOf(orderTotalCount));
			orderDisplayPanel.add(scrollZonePane);

			// modelLabel.setText("SalesOrder : "+_items.get(0).SalesOrder);
			// modelText.setText(" TOTAL: "+totalCount);

			total.setText("Total : " + String.valueOf(orderTotalCount));

			prev = new JButton("Back");
			prev.setFont(font);
			prev.setBounds(820, 700, 90, 50);

			exit = new JButton("Exit");
			exit.setFont(font);
			exit.setBounds(920, 700, 90, 50);

			prev.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (scanResultFrame != null) {
						scanResultFrame.dispose();
						scanResultFrame.setVisible(false);
					}
					orderFrame.dispose();
					orderFrame.setVisible(false);

					ShippingPicking window = new ShippingPicking();
					window.frame.setVisible(true);
				}
			});

			exit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					shippingPicking = null;
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

		}

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
				if (!_items.isEmpty()) {

					for (Itembean i : _items) {
						if (inventoryModelmap.containsKey(i.ModelNo)) {
							List<Itembean> list = inventoryModelmap.get(i.ModelNo);
							list.add(i);
							inventoryModelmap.put(i.ModelNo, list);
						} else {
							List<Itembean> list = new ArrayList<Itembean>();
							list.add(i);
							inventoryModelmap.put(i.ModelNo, list);
						}
					}

					// System.out.println("call back, map.size():" + map.size());
					// System.out.println("call back, inventoryModelmap.size():" +
					// inventoryModelmap.size());
					if (inventoryModelmap.size() == map.size()) {
						if (loading != null)
							loading.setValue(100);

						if (loadingframe != null) {
							loadingframe.setVisible(false);
							loadingframe.dispose();
						}

						if (orderFrame == null)
							displayPickingOrderInfo(_items);
					}
				} else {
					if (loadingframe != null) {
						loadingframe.setVisible(false);
						loadingframe.dispose();
					}
					JOptionPane.showMessageDialog(null, "Items not enough in zone 1.");
				}
			}

			@Override
			public void checkInventoryZone2Items(int result, List<Itembean> items) {
				// TODO Auto-generated method stub

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
				// TODO Auto-generated method stub
				
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

				if (orders.isEmpty())
					JOptionPane.showMessageDialog(null, "The sales order doesn't exist !");
				else {
					frame.dispose();
					frame.setVisible(false);

					if (!isOrderClosed && orders.get(0).closed) {
						isOrderClosed = orders.get(0).closed;

					}

					pickingItems = new ArrayList<PickingItem>();
					salesOrderList = new ArrayList<CustOrderbean>();
					map = new LinkedHashMap<String, Integer>();
					OrderModelmap = new TreeMap<String, String>();
					salesOrderList = orders;
					for (CustOrderbean item : salesOrderList) {
						int count = Integer.valueOf(item.quantity);
						// System.out.println("item.ItemID:" + item.ItemID);

						if (item.ItemID != null) {
							
							if(item.ItemID.equals("450887"))
								item.ItemID = "115816";
							
							
							if (map.containsKey(item.ItemID)) {

								if (!item.ItemID.contains("PL") && item.ItemID.length() == 6
										&& !WeightPlateUtil.isWeightPlate(item.ItemID) && !WeightPlateUtil.isCalfSupport(item.ItemID)) {
									count += map.get(item.ItemID);
									map.put(item.ItemID, count);
									orderTotalCount += count;
								}
							} else {

								if (!item.ItemID.contains("PL") && item.ItemID.length() == 6
										&& !WeightPlateUtil.isWeightPlate(item.ItemID)&& !WeightPlateUtil.isCalfSupport(item.ItemID)) {
									orderTotalCount += count;
									map.put(item.ItemID, count);
									OrderModelmap.put(item.ItemID, item.description);
								}
							}
						}
					}

					if (!isOrderClosed) {
						inventoryModelmap = new LinkedHashMap<String, List<Itembean>>();
						loadingframe = new LoadingFrameHelper("Loading data...");
						loading = loadingframe.loadingSample("Loading data...");

						EventQueue.invokeLater(new Runnable() {
							public void run() {
								try {
									loading.setValue(50);
									for (CustOrderbean _order : orders) {
										if (_order.ItemID == null || _order.ItemID.indexOf("PL") != -1
												|| _order.ItemID.length() != 6
												|| WeightPlateUtil.isWeightPlate(_order.ItemID))
											continue;
										else
											queryByModelAndCount(_order.ItemID, map.get(_order.ItemID));

										// queryByModel(_order.ItemID);
										// queryByModel(_order.ItemID);
										// queryByModelAndDate(_order.ItemID, "2015-01-01");
									}

								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						});
					} else
						JOptionPane.showMessageDialog(null, "The sales order is closed !");
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

				// else
				//

			}

			@Override
			public void checkHistoryItemsBySalesOrder(List<Historybean> _items) {
				if (!_items.isEmpty()) {
					isOrderClosed = true;
					JOptionPane.showMessageDialog(null, "The sales order is closed !");
				} else {

					items = _items;
					// Check SaleOrder Info
					querySalesOrder(salesOrder);
				}

			}

			@Override
			public void exception(String error) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void getDailyShippingItems(List<DailyShippingReportbean> items) {
				// TODO Auto-generated method stub
				
			}

		});

	}

	private void fillModelMapLocation() {

		for (Map.Entry<String, List<Itembean>> location : inventoryModelmap.entrySet()) {
			LinkedHashMap<String, Integer> locModelMap = new LinkedHashMap<String, Integer>();

			String locInfo = "";
			int orderCount = map.get(location.getKey());
			String model = location.getKey();
			for (Itembean item : location.getValue()) {

				if (locModelMap.containsKey(item.Location)) {
					int itemlist = locModelMap.get(item.Location);
					if (itemlist < orderCount) {
						itemlist = itemlist + 1;
						locModelMap.put(item.Location, itemlist);
					} else
						break;
				} else {
					PickingItem pickItem = new PickingItem();
					pickItem.quantity = 1;
					pickItem.modelID = item.ModelNo;

					if (!WeightPlateUtil.isModelParts(item.ModelNo))
						pickItem.modelDes = OrderModelmap.get(pickItem.modelID);
					else
						pickItem.modelDes = OrderModelmap.get(pickItem.modelID)
								+ WeightPlateUtil.modelAppendWithPart(pickItem.modelID);
					pickItem.location = item.Location;
					pickingItems.add(pickItem);
					System.out.println(location.getKey());
					locModelMap.put(item.Location, 1);
				}

			}

			locMap.put(model, locModelMap);

		}

	}

}
