package spirit.fitness.scanner.report;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
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
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import spirit.fitness.scanner.AppMenu;
import spirit.fitness.scanner.common.Constrant;
import spirit.fitness.scanner.common.HttpRequestCode;
import spirit.fitness.scanner.restful.FGRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.HistoryRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.HttpRestApi;
import spirit.fitness.scanner.restful.ModelZoneMapRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.listener.HistoryCallBackFunction;
import spirit.fitness.scanner.restful.listener.InventoryCallBackFunction;
import spirit.fitness.scanner.restful.listener.ModelZone2CallBackFunction;
import spirit.fitness.scanner.until.ExcelHelper;
import spirit.fitness.scanner.until.LoadingFrameHelper;
import spirit.fitness.scanner.until.LocationHelper;
import spirit.fitness.scanner.zonepanel.ZoneMenu;
import spirit.fitness.scanner.model.DailyShippingReportbean;
import spirit.fitness.scanner.model.Historybean;
import spirit.fitness.scanner.model.Itembean;
import spirit.fitness.scanner.model.ModelDailyReportbean;
import spirit.fitness.scanner.model.ModelZone2bean;
import spirit.fitness.scanner.model.SalesJournal;
import spirit.fitness.scanner.model.SerialNoRecord;
import spirit.fitness.scanner.printer.until.PrinterHelper;

public class DailyShippingReport {

	private static DailyShippingReport dailyReport = null;

	public final static int REPORT = 0;
	public final static int MIN_QUANTITY = 1;

	public JFrame frame;
	private String items;

	private ProgressMonitor progressMonitor;
	private JButton btnDone;
	private HistoryRepositoryImplRetrofit fgModelZone2;

	private LoadingFrameHelper loadingframe;
	private JProgressBar loading;
    private JTextField date;
	
	private JFileChooser chooser;
	private String choosertitle;

	private List<SalesJournal> soInfo;

	private LinkedHashMap<String, List<DailyShippingReportbean>> map;

	public static DailyShippingReport getInstance() {
		if (dailyReport == null) {
			dailyReport = new DailyShippingReport();
		}
		return dailyReport;
	}

	public static boolean isExit() {
		return dailyReport != null;
	}

	public DailyShippingReport() {

		loadingframe = new LoadingFrameHelper("Loading Data from Server...");
		loading = loadingframe.loadingSample("Loading Data from Server...");
		
		date = new JTextField(20);
		intialCallback();
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
		date.setText(timeStamp);
		loadModelZone2Map(timeStamp);

	}

	private void displayTable(List<DailyShippingReportbean> data) {

		JFrame.setDefaultLookAndFeelDecorated(false);
		JDialog.setDefaultLookAndFeelDecorated(false);
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Item Result");
		frame.setLocationRelativeTo(null);
		frame.setBounds(50, 50, 1200, 700);
		frame.setUndecorated(true);
		frame.setResizable(false);

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Constrant.FRAME_BORDER_BACKGROUN_COLOR));
		panel.setBackground(Constrant.BACKGROUN_COLOR);
		// adding panel to frame
		frame.add(panel);

		placeComponents(panel, data);

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

	private void placeComponents(JPanel panel, List<DailyShippingReportbean> data) {

		/*
		 * We will discuss about layouts in the later sections of this tutorial. For now
		 * we are setting the layout to null
		 */
		panel.setLayout(null);
	
		// ScrollPane for Result
		JScrollPane scrollZonePane = new JScrollPane();

		scrollZonePane.setBackground(Constrant.TABLE_COLOR);
		panel.add(scrollZonePane);

		
		Object rowDataReport[][] = new Object[data.size()][9];
		System.out.println(data.size());

		int prevTotal = 0;
		int shippedTotal = 0;
		int receivedTotal = 0;
		int scrappedTotal = 0;
		int shippableOnHandTotal = 0;
		int returnUnshippableTotal = 0;
		int showroom = 0;
		int reworkTotal = 0;
		int qcTotal = 0;
		int Total = 0;

		for (int i = 0; i < data.size(); i++) {

			/*
			 * if (i == 10) { for (int j = 0; j < 12; j++) { rowDataReport[i][0] = " TOTAL";
			 * rowDataReport[i][1] = ""; rowDataReport[i][2] = prevTotal;
			 * rowDataReport[i][3] = shippedTotal; rowDataReport[i][4] = receivedTotal;
			 * rowDataReport[i][5] = scrappedTotal; rowDataReport[i][6] =
			 * shippableOnHandTotal; rowDataReport[i][7] = returnUnshippableTotal; ;
			 * rowDataReport[i][8] = showroom; rowDataReport[i][9] = reworkTotal;
			 * rowDataReport[i][10] = qcTotal; rowDataReport[i][11] = Total; } } else {
			 */
			for (int j = 0; j < 9; j++) {
				rowDataReport[i][0] = " " + data.get(i).createdDate.substring(0, 10);
				rowDataReport[i][1] = data.get(i).salesOrder;
				
				if(data.get(i).CustPo == null)
					rowDataReport[i][2] = "";
				else
					rowDataReport[i][2] = data.get(i).CustPo;
				rowDataReport[i][3] = data.get(i).BillTo;
				rowDataReport[i][4] = data.get(i).fg;
				rowDataReport[i][5] = data.get(i).qty;
				// rowDataReport[i][5] = data.get(i).Scrapped;
				rowDataReport[i][6] = data.get(i).trackingNo;
				rowDataReport[i][7] = data.get(i).shipVia;

				rowDataReport[i][8] = data.get(i).shippingDate.substring(0, 10);
				// }
			}

		}

		String zone = "";

		Object columnNames[] = { "CreateDate", "SO", "CustPO", "BillTo", "FG", "Qty", "Pro", "ShipVia",
				"ShippingDate" };
		Font font = new Font("Verdana", Font.BOLD, 15);
		final Class[] columnClass = new Class[] { String.class, String.class, Integer.class, Integer.class,
				Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, String.class };

		DefaultTableModel model = new DefaultTableModel(rowDataReport, columnNames) {
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
		TableColumn modelNo = table.getColumnModel().getColumn(0);
		modelNo.setPreferredWidth(70);
		TableColumn modelTitle = table.getColumnModel().getColumn(1);
		modelTitle.setPreferredWidth(30);
		TableColumn prevCcolumn = table.getColumnModel().getColumn(2);
		prevCcolumn.setPreferredWidth(50);
		// TableColumn shippingColumn = table.getColumnModel().getColumn(3);
		// shippingColumn.setPreferredWidth(90);
		TableColumn custPoColumn = table.getColumnModel().getColumn(3);
		custPoColumn.setPreferredWidth(150);
		TableColumn receivedColumn = table.getColumnModel().getColumn(4);
		receivedColumn.setPreferredWidth(180);
		TableColumn scrappedColumn = table.getColumnModel().getColumn(5);
		scrappedColumn.setPreferredWidth(3);
		TableColumn onHand = table.getColumnModel().getColumn(6);
		onHand.setPreferredWidth(100);
		// TableColumn returnQty = table.getColumnModel().getColumn(7);
		// returnQty.setPreferredWidth(200);
		// TableColumn column = table.getColumnModel().getColumn(8);
		// column.setPreferredWidth(120);

		TableColumn qCcolumn = table.getColumnModel().getColumn(7);
		qCcolumn.setPreferredWidth(50);
		TableColumn showroomcolumn = table.getColumnModel().getColumn(8);
		showroomcolumn.setPreferredWidth(50);
		table.setCellSelectionEnabled(false);
		table.setColumnSelectionAllowed(false);
		table.setEnabled(false);

		int heigh = 0;

		if (50 * rowDataReport.length + 20 > 630)
			heigh = 630;
		else
			heigh = 630;
		scrollZonePane.setBounds(5, 5, 1190, heigh);

		scrollZonePane.setViewportView(table);

		panel.add(scrollZonePane);

		Font btnFont = new Font("Verdana", Font.BOLD, 18);
		Font txtFont = new Font("Verdana", Font.BOLD, 22);

		JButton Search = new JButton("Import SO");
		Search.setFont(btnFont);
		Search.setBounds(730, 640, 150, 50);

		Search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// if (loadingframe != null)
				// loadingframe.setVisible(true);
				chooser = new JFileChooser();
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setDialogTitle(choosertitle);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				//
				// disable the "All files" option.
				//
				chooser.setAcceptAllFileFilterUsed(false);
				//
				if (chooser.showOpenDialog(btnDone) == JFileChooser.APPROVE_OPTION) {
					System.out.println("getCurrentDirectory(): " + chooser.getCurrentDirectory());
					System.out.println("getSelectedFile() : " + chooser.getSelectedFile());

					soInfo = ExcelHelper.readCSVFile(chooser.getSelectedFile().getAbsolutePath(), map);
					if (soInfo.size() > 0)
						JOptionPane.showMessageDialog(null, "Import Success.");
					else
						JOptionPane.showMessageDialog(null, "Import Fail.");

				}
			}
		});
		panel.add(Search);

		btnDone = new JButton("Export SALES");
		btnDone.setFont(btnFont);
		btnDone.setBounds(890, 640, 180, 50);

		btnDone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {

							chooser = new JFileChooser();
							chooser.setCurrentDirectory(new java.io.File("."));
							chooser.setDialogTitle(choosertitle);
							chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							//
							// disable the "All files" option.
							//
							chooser.setAcceptAllFileFilterUsed(false);
							//
							if (chooser.showOpenDialog(btnDone) == JFileChooser.APPROVE_OPTION) {
								System.out.println("getCurrentDirectory(): " + chooser.getCurrentDirectory());
								System.out.println("getSelectedFile() : " + chooser.getSelectedFile());

								if (ExcelHelper.writeToCVS(chooser.getSelectedFile().getAbsolutePath(), soInfo))
									JOptionPane.showMessageDialog(null, "Export Success.");
								else
									JOptionPane.showMessageDialog(null, "Import Fail.");
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

				// HttpRestApi.postData(result);
			}
		});
		panel.add(btnDone);

		JButton save = new JButton("Save shipping report");
		save.setFont(btnFont);
		save.setBounds(450, 640, 270, 50);

		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {

							chooser = new JFileChooser();
							chooser.setCurrentDirectory(new java.io.File("."));
							chooser.setDialogTitle(choosertitle);
							chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							//
							// disable the "All files" option.
							//
							chooser.setAcceptAllFileFilterUsed(false);
							//
							if (chooser.showOpenDialog(btnDone) == JFileChooser.APPROVE_OPTION) {
								System.out.println("getCurrentDirectory(): " + chooser.getCurrentDirectory());
								System.out.println("getSelectedFile() : " + chooser.getSelectedFile());

								 ExcelHelper exp = new ExcelHelper();
									//String timeStamp = new SimpleDateFormat("yyyy-MM-dd")
									//		.format(Calendar.getInstance().getTime());
									
								 exp.fillDailyShippingData(table,
											new File(chooser.getSelectedFile() +"\\"+date.getText().toString() + "_ship.xls"));

							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

				// HttpRestApi.postData(result);
			}
		});
		panel.add(save);
		
		
		frame.addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				date.requestFocus();
			}
		});
		

		
		
		date.setFont(txtFont);
		date.setBounds(5, 640, 200, 50);
		panel.add(date);
		
		JButton find = new JButton("Find");
		find.setFont(btnFont);
		find.setBounds(220, 640, 200, 50);

		find.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(loadingframe != null)
					loadingframe.setVisible(true);
				
				loadModelZone2Map(date.getText().toString());
				frame.dispose();
				frame.setVisible(false);
			}
		});
		panel.add(find);
		
		JButton exitDone = new JButton("Exit");
		exitDone.setFont(btnFont);
		exitDone.setBounds(1080, 640, 100, 50);

		exitDone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dailyReport = null;
				frame.dispose();
				frame.setVisible(false);
			}
		});
		panel.add(exitDone);

	}

	private void intialCallback() {
		fgModelZone2 = new HistoryRepositoryImplRetrofit();
		fgModelZone2.setHistoryServiceCallBackFunction(new HistoryCallBackFunction() {

			@Override
			public void resultCode(int code) {
				// TODO Auto-generated method stub

			}

			@Override
			public void checkHistoryItemsBySalesOrder(List<Historybean> items) {
				// TODO Auto-generated method stub

			}

			@Override
			public void getHistoryItems(List<Historybean> items) {
				// TODO Auto-generated method stub

			}

			@Override
			public void getDailyShippingItems(List<DailyShippingReportbean> items) {
				loading.setValue(100);
				loadingframe.setVisible(false);
				loadingframe.dispose();
				map = new LinkedHashMap<>();
				
				//sort by date
				Collections.sort(items, new DateSalesOrderComparator());
				
				for (DailyShippingReportbean i : items) {
					List<DailyShippingReportbean> record = null;
					if (!map.containsKey(i.salesOrder)) {
						record = new ArrayList<DailyShippingReportbean>();
					} else {
						record = map.get(i.salesOrder);
					}
					
					record.add(i);
					map.put(i.salesOrder, record);
				}

				List<DailyShippingReportbean> list = new ArrayList<DailyShippingReportbean>();
				for (Map.Entry<String, List<DailyShippingReportbean>> location : map.entrySet()) {
					HashMap<String, DailyShippingReportbean> reportItem = new HashMap<String, DailyShippingReportbean>();

					for (DailyShippingReportbean d : location.getValue()) {
						if (reportItem.get(d.itemID) == null) {
							d.qty = "1";
						} else {
							list.remove(d);
							d.qty = "" + (Integer.valueOf(reportItem.get(d.itemID).qty) + 1);

						}
						reportItem.put(d.itemID, d);
					}

					for (Map.Entry<String, DailyShippingReportbean> i : reportItem.entrySet()) {
						
						list.add(i.getValue());
					}
				}

				displayTable(list);
				java.awt.EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						frame.toFront();
						frame.repaint();
					}
				});

			}

			@Override
			public void exception(String error) {
				// TODO Auto-generated method stub

			}

			@Override
			public void getSerialNoRecord(List<SerialNoRecord> items) {
				// TODO Auto-generated method stub
				
			}

		});
	}

	// Loading Models data from Server
	private void loadModelZone2Map(String timeStamp) {

		// loading model and location information from Server
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
						
					/*SimpleDateFormat shipFormat = new SimpleDateFormat("yyyy-MM-dd");
					
					Date shippedDate = shipFormat.parse(timeStamp);
					Calendar ship = Calendar.getInstance();
					ship.setTime(shippedDate);
					
					 int dayOfWeek = ship.get(Calendar.DAY_OF_WEEK);
					 if(dayOfWeek == Calendar.MONDAY)
						 ship.add(Calendar.DATE, -3);
					 else
						 ship.add(Calendar.DATE, -1);
					String dueDate = new SimpleDateFormat("yyyy-MM-dd").format(ship.getTime());
					
					date.setText(dueDate);*/
					date.setText(timeStamp);
					fgModelZone2.getDailyShippingItems(timeStamp);
				} catch (Exception e) {
					e.printStackTrace();
					// NetWorkHandler.displayError(loadingframe);
				}
			}
		});

	}



	class DateSalesOrderComparator implements Comparator<DailyShippingReportbean> {
	    @Override
	    public int compare(DailyShippingReportbean a, DailyShippingReportbean b) {
			
	    	return a.createdDate.compareTo(b.createdDate);

	    }
	}
}
