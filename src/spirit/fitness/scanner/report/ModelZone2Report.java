package spirit.fitness.scanner.report;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
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
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import spirit.fitness.scanner.AppMenu;
import spirit.fitness.scanner.common.Constrant;
import spirit.fitness.scanner.common.HttpRequestCode;
import spirit.fitness.scanner.restful.FGRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.HttpRestApi;
import spirit.fitness.scanner.restful.ModelZoneMapRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.listener.InventoryCallBackFunction;
import spirit.fitness.scanner.restful.listener.ModelZone2CallBackFunction;
import spirit.fitness.scanner.util.ExcelHelper;
import spirit.fitness.scanner.util.LoadingFrameHelper;
import spirit.fitness.scanner.util.LocationHelper;
//import spirit.fitness.scanner.util.NetWorkHandler;
import spirit.fitness.scanner.util.PrintJtableUtil;
import spirit.fitness.scanner.util.PrintTableUtil;
import spirit.fitness.scanner.util.PrinterHelper;
import spirit.fitness.scanner.zonepannel.ZoneMenu;
import spirit.fitness.scanner.model.Itembean;
import spirit.fitness.scanner.model.ModelDailyReportbean;
import spirit.fitness.scanner.model.ModelZone2bean;
import spirit.fitness.scanner.model.PickUpZoneMap;


public class ModelZone2Report {

	private static ModelZone2Report modelZone2Report = null;

	public final static int REPORT = 0;
	public final static int MIN_QUANTITY = 1;

	private List<String> resultModelItem;

	public JFrame frame;
	private LoadingFrameHelper loadingframe;
	private JProgressBar loading;

	private JButton btnDone;

	private ModelZoneMapRepositoryImplRetrofit fgModelZone2;

	public static ModelZone2Report getInstance(List<ModelZone2bean> data) {
		if (modelZone2Report == null) {
			modelZone2Report = new ModelZone2Report(data);
		}
		return modelZone2Report;
	}

	public static boolean isExit() {
		return modelZone2Report != null;
	}

	public ModelZone2Report(List<ModelZone2bean> data) {
		resultModelItem = new ArrayList<String>();
		loadingframe = new LoadingFrameHelper("Loading Data from Server...");
		loading = loadingframe.loadingSample("Loading Data from Server...");
		intialCallback();
		loadModelZone2Map();

	}

	private void displayTable(List<ModelZone2bean> data) {

		JFrame.setDefaultLookAndFeelDecorated(false);
		JDialog.setDefaultLookAndFeelDecorated(false);

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Item Result");
		frame.setLocationRelativeTo(null);
		frame.setBounds(100, 100, 1000, 600);
		frame.setUndecorated(true);
		frame.setResizable(false);

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Constrant.FRAME_BORDER_BACKGROUN_COLOR));
		panel.setBackground(Constrant.BACKGROUN_COLOR);
		// adding panel to frame
		frame.add(panel);

		placeComponents(panel, data);
		loadingframe.setVisible(false);
		loadingframe.dispose();
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

	private void placeComponents(JPanel panel, List<ModelZone2bean> data) {

		/*
		 * We will discuss about layouts in the later sections of this tutorial. For now
		 * we are setting the layout to null
		 */
		panel.setLayout(null);

		// ScrollPane for Result
		JScrollPane scrollZonePane = new JScrollPane();

		scrollZonePane.setBackground(Constrant.TABLE_COLOR);
		panel.add(scrollZonePane);

		String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

		Object rowDataReport[][] = new Object[data.size()][5];
		System.out.println(data.size());

		for (int i = 0; i < data.size(); i++) {
			String printItem = "";
			for (int j = 0; j < 5; j++) {
				rowDataReport[i][0] = " " + data.get(i).Model;
				rowDataReport[i][1] = " " + data.get(i).FG;
				rowDataReport[i][2] = data.get(i).Z2CurtQty;
				rowDataReport[i][3] = data.get(i).Zone1Code;
				rowDataReport[i][4] = data.get(i).Zone2Code;
				printItem = data.get(i).Model + "\n" + data.get(i).FG + "\n" + data.get(i).Z2CurtQty + "\n"
						+ data.get(i).Zone1Code + "\n" + data.get(i).Zone2Code;
			}
			resultModelItem.add(printItem);
		}

		String zone = "";

		Object columnNames[] = { "Model#", "FG", "Quantity", "From(Zone 1)", "To(Zone 2)" };
		Font font = new Font("Verdana", Font.BOLD, 18);
		final Class[] columnClass = new Class[] { String.class, String.class, Integer.class, String.class,
				String.class };

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
		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
		leftRenderer.setHorizontalAlignment(JLabel.CENTER);
		
		TableColumn modelNo = table.getColumnModel().getColumn(0);
		modelNo.setPreferredWidth(20);
		modelNo.setCellRenderer(leftRenderer);
		TableColumn modelTitle = table.getColumnModel().getColumn(1);
		modelTitle.setPreferredWidth(250);
		modelTitle.setCellRenderer(leftRenderer);
		TableColumn qtycolumn = table.getColumnModel().getColumn(2);
		qtycolumn.setPreferredWidth(10);
		qtycolumn.setCellRenderer(leftRenderer);
		
		TableColumn zone1 = table.getColumnModel().getColumn(3);
		zone1.setCellRenderer(leftRenderer);
		
		TableColumn zone2 = table.getColumnModel().getColumn(4);
		zone2.setCellRenderer(leftRenderer);
		
		table.setCellSelectionEnabled(false);
		table.setColumnSelectionAllowed(false);
		table.setEnabled(false);

		int heigh = 0;
		System.out.println("" + 50 * rowDataReport.length + 20);
		if (50 * rowDataReport.length + 20 > 500)
			heigh = 500;
		else
			heigh = 40 * rowDataReport.length + 32;

		scrollZonePane.setBounds(10, 10, 980, heigh);
		scrollZonePane.setViewportView(table);

		panel.add(scrollZonePane);

		btnDone = new JButton("Print");
		btnDone.setFont(font);
		btnDone.setBounds(5, 540, 200, 50);

		btnDone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// printer();
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							printer(table, frame);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				
			}
		});
		panel.add(btnDone);

		/*
		 * refreshDone = new JButton("Refresh"); refreshDone.setFont(font);
		 * refreshDone.setBounds(220, 540, 200, 50);
		 * 
		 * refreshDone.addActionListener(new ActionListener() { public void
		 * actionPerformed(ActionEvent e) { refreshDone.setEnabled(false); loadingframe
		 * = new LoadingFrameHelper(); loading =
		 * loadingframe.loadingSample("Loading Data from Server...");
		 * loading.setValue(50); frame.dispose(); frame.setVisible(false); frame = null;
		 * loadModelZone2Map(); } }); panel.add(refreshDone);
		 */

		JButton exitDone = new JButton("Exit");
		exitDone.setFont(font);
		exitDone.setBounds(220, 540, 200, 50);

		exitDone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modelZone2Report = null;

				frame.dispose();
				frame.setVisible(false);
			}
		});
		panel.add(exitDone);

	}

	private void intialCallback() {
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

				// if (refreshDone != null)
				// refreshDone.setEnabled(true);
				HashMap<String, Integer> mapModelCount = new HashMap<>();
				HashMap<String, ModelZone2bean> map = new HashMap<>();
				for (ModelZone2bean i : items) {

					if(i.Model.equals("110026"))
					System.out.println("123");
					if (!map.containsKey(i.Model)) {
						map.put(i.Model, i);
						mapModelCount.put(i.Model, 1);
					} else {
						ModelZone2bean m = map.get(i.Model);
						
						mapModelCount.put(i.Model, mapModelCount.get(i.Model) + 1);

					
						if (i.Z2CurtQty < m.Z2CurtQty)
							map.put(i.Model, i);
					}
					
				}

				Constrant.modelZone2 = map;

				loading.setValue(100);

				Constrant.modelZone2List = items;
				displayTable(items);
				java.awt.EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						frame.toFront();
						frame.repaint();
					}
				});
			}

			@Override
			public void getModelDailyReportItems(List<ModelDailyReportbean> items) {
				// TODO Auto-generated method stub

			}

			@Override
			public void pickUpZone(List<PickUpZoneMap> items) {
				// TODO Auto-generated method stub
				
			}

		});
	}

	// Loading Models data from Server
	private void loadModelZone2Map() {

		// loading model and location information from Server
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					fgModelZone2.getAllItemsQty();

				} catch (Exception e) {
					e.printStackTrace();
					//NetWorkHandler.displayError(loadingframe);
				}
			}
		});

	}

	private void printer() {

		List<String> headersList = Arrays.asList("Model#", "FG", "Quantity", "From(Zone 1)", "To(Zone 2)");

		List<List<String>> rowsList = new ArrayList<List<String>>();
		for (String s : resultModelItem) {
			String[] rowdata = s.split("\n");
			rowsList.add(Arrays.asList(rowdata));
		}

		// String result = PrintTableUtil.printReport(headersList, rowsList);
		String result = PrintTableUtil.printModelQuantityReport(headersList, rowsList);
		// content += result + itemsInfo;
		System.out.println(result);

		PrinterHelper print = new PrinterHelper();
		print.printTable(result);

	}

	private void printer(JTable table, JFrame frame) {
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
		PrintJtableUtil.printTableResult("Replenishment Report - " + timeStamp, "", true, true, true, table, frame);
	}

}
