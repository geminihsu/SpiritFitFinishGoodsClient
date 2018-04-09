package spirit.fitness.scanner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

import spirit.fitness.scanner.common.Constrant;
import spirit.fitness.scanner.common.HttpRequestCode;
import spirit.fitness.scanner.model.Itembean;
import spirit.fitness.scanner.model.Locationbean;
import spirit.fitness.scanner.model.ModelDailyReportbean;
import spirit.fitness.scanner.model.ModelZone2bean;
import spirit.fitness.scanner.model.Modelbean;
import spirit.fitness.scanner.receving.ItemsPannel;
import spirit.fitness.scanner.receving.ContainerPannel;
import spirit.fitness.scanner.report.DailyReport;
import spirit.fitness.scanner.report.ModelZone2Report;
import spirit.fitness.scanner.restful.FGRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.LocationRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.ModelRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.ModelZoneMapRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.listener.InventoryCallBackFunction;
import spirit.fitness.scanner.restful.listener.LocationCallBackFunction;
import spirit.fitness.scanner.restful.listener.ModelZone2CallBackFunction;
import spirit.fitness.scanner.restful.listener.ModelsCallBackFunction;
import spirit.fitness.scanner.search.QueryPannel;
import spirit.fitness.scanner.search.QueryResult;
import spirit.fitness.scanner.shipping.ShippingConfirm;
import spirit.fitness.scanner.shipping.ShippingPicking;
import spirit.fitness.scanner.util.EmailHelper;
import spirit.fitness.scanner.util.InstanceUtil;
import spirit.fitness.scanner.util.LoadingFrameHelper;

public class AppMenu implements ActionListener {
	private static AppMenu instance = null;

	private JButton btnRecving, btnMoving, btnInQuiry, btnShipping, btnReport, btnModelQuantity, btnPickingList,
			btnReplenishment, btnConfiguration;
	private JFrame frame;

	private JProgressBar loading;
	private LoadingFrameHelper loadingframe;

	private ModelZoneMapRepositoryImplRetrofit fgModelZone2;
	private ModelRepositoryImplRetrofit fgModels;
	private LocationRepositoryImplRetrofit localModels;

	public AppMenu() {
		// EmailHelper.sendMail();
		// JOptionPane.showMessageDialog(null, "Model 15516 less than 50. Please move
		// more item from Zone 1.");
		exceuteCallback();
		loadingframe = new LoadingFrameHelper("Loading Data from Server...");
		loading = loadingframe.loadingSample("Loading Data from Server...");
		initialize();
		// loadReport();
		loadModelMapZone2();
		loadModel();
		loadLocation();
	}

	public static AppMenu getInstance() {
		if (instance == null) {
			instance = new AppMenu();
		}
		return instance;
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AppMenu window = new AppMenu();
					window.frame.setVisible(true);
					// QueryResult window = new QueryResult();
					// window.setContent(0, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frame = new JFrame("FG Inventory App");
		frame.setSize(1200, 600);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		Container cp = frame.getContentPane();
		cp.setLayout(new GridLayout(2, 3));

		Font font = new Font("Verdana", Font.BOLD, 30);
		btnRecving = new JButton("Receiving");
		btnRecving.setFont(font);
		btnMoving = new JButton("Moving");
		btnMoving.setFont(font);
		btnInQuiry = new JButton("Search");
		btnInQuiry.setFont(font);
		btnReport = new JButton("Daily Report");
		btnReport.setFont(font);
		btnShipping = new JButton("Shipping");
		btnShipping.setFont(font);
		// btnModelQuantity = new JButton("Model Quantity");
		// btnModelQuantity.setFont(font);
		btnPickingList = new JButton("Picking");
		btnPickingList.setFont(font);

		btnReplenishment = new JButton("Replenishment");
		btnReplenishment.setFont(font);
		btnConfiguration = new JButton("Configuration");
		btnConfiguration.setFont(font);
		// btnModelQuantity = new JButton("Model Quantity");
		// btnModelQuantity.setFont(font);
		// btnRecving.setBounds(20,20,100,40);
		// btnMoving.setBounds(150,20,100,40);
		// btnInQuiry.setBounds(280,20,100,40);
		// btnShipping.setBounds(410,20,100,40);
		btnRecving.setMnemonic('O');
		btnMoving.setMnemonic('C');
		btnInQuiry.setMnemonic('Q');
		btnRecving.addActionListener(this);
		btnMoving.addActionListener(this);
		btnShipping.addActionListener(this);
		btnInQuiry.addActionListener(this);
		// btnModelQuantity.addActionListener(this);
		btnReport.addActionListener(this);
		btnPickingList.addActionListener(this);
		btnReplenishment.addActionListener(this);
		btnConfiguration.addActionListener(this);
		cp.add(btnRecving);
		cp.add(btnMoving);
		cp.add(btnInQuiry);
		cp.add(btnPickingList);
		cp.add(btnShipping);
		cp.add(btnReport);
		cp.add(btnReplenishment);
		cp.add(btnConfiguration);
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

		if (!InstanceUtil.isExits()) {
			if (e.getSource() == btnRecving) {
				//ItemsPannel.getInstance("", ItemsPannel.RECEVING);
				ContainerPannel.getInstance();
			} else if (e.getSource() == btnMoving) {
				// ItemsPannel window = new ItemsPannel(ItemsPannel.MOVING);
				// window.frame.setVisible(true);
				ItemsPannel.getInstance("", ItemsPannel.MOVING);
			} else if (e.getSource() == btnInQuiry) {
				// QueryPannel window = new QueryPannel();
				// window.frame.setVisible(true);
				QueryPannel.getInstance();
			} else if (e.getSource() == btnShipping) {
				// ShippingConfirm window = new ShippingConfirm();
				// window.frame.setVisible(true);

				ShippingConfirm.getInstance();
			} else if (e.getSource() == btnReport) {

				// JOptionPane.showMessageDialog(null, "Model 15516 less than 50. Please move
				// more item from Zone 1.");

				// DailyReport window = new DailyReport(Constrant.dailyReport);
				// window.frame.setVisible(true);
				DailyReport.getInstance(Constrant.dailyReport);

			} else if (e.getSource() == btnPickingList) {

				// ShippingPicking window = new ShippingPicking();
				// window.frame.setVisible(true);
				ShippingPicking.getInstance();
			} else if (e.getSource() == btnReplenishment) {

				ModelZone2Report.getInstance(Constrant.modelZone2List);
				// window.frame.setVisible(true);
			}

		}

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
				HashMap<String, Integer> cntMap = new HashMap<>();
				
				

				//int cnt = 0;
				for (ModelZone2bean i : items) {

					if (map.containsKey(i.Model)) {
						ModelZone2bean m = map.get(i.Model);
						m.Zone2Code = m.Zone2Code + "," + i.Zone2Code;

						//if (cnt == 0) {
							int cnt = cntMap.get(m.Model);
							//m.Z2CurtQty = m.Z2MaxQty - cnt;
						//}
						if (i.Z2MaxQty - i.Z2CurtQty != 0) {
							cnt += i.Z2MaxQty - i.Z2CurtQty;
							m.Z2CurtQty = m.Z2MaxQty - cnt;
						}
						
						cntMap.put(i.Model,cnt);
						// if(m.Z2CurtQty > i.Z2CurtQty)
						// m.Z2CurtQty = i.Z2CurtQty;

						map.put(i.Model, m);
					} else {
						cntMap.put(i.Model, i.Z2MaxQty - i.Z2CurtQty);
						map.put(i.Model, i);
					}
				}

				Constrant.modelZone2 = map;
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

		fgModels = new ModelRepositoryImplRetrofit();
		fgModels.setinventoryServiceCallBackFunction(new ModelsCallBackFunction() {

			@Override
			public void resultCode(int code) {
				// TODO Auto-generated method stub
				if (code == HttpRequestCode.HTTP_REQUEST_INSERT_DATABASE_ERROR) {

				}
			}

			@Override
			public void getModelsItems(List<Modelbean> items) {

				HashMap<String, Modelbean> map = new HashMap<>();
				for (Modelbean i : items) {
					map.put(i.ModelNo, i);
				}

				Constrant.models = map;
				loading.setValue(60);

			}

		});

		localModels = new LocationRepositoryImplRetrofit();
		localModels.setinventoryServiceCallBackFunction(new LocationCallBackFunction() {

			@Override
			public void resultCode(int code) {
				// TODO Auto-generated method stub
				if (code == HttpRequestCode.HTTP_REQUEST_INSERT_DATABASE_ERROR) {

				}
			}

			@Override
			public void getLocationItems(List<Locationbean> items) {
				HashMap<String, Locationbean> map = new HashMap<>();
				for (Locationbean i : items) {
					map.put(i.Code, i);
				}

				Constrant.locations = map;
				loading.setValue(80);
				loadingframe.setVisible(false);
				loadingframe.dispose();
			}

		});

	}

	// Loading Models data from Server
	private void loadModel() {
		// loading model and location information from Server
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					fgModels.getAllItems();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

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

	// Loading Models data from Server
	private void loadLocation() {
		// loading model and location information from Server
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					localModels.getAllItems();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		try {

		} catch (NumberFormatException x) {
			// TODO Auto-generated catch block
			x.printStackTrace();
		} catch (Exception x) {
			// TODO Auto-generated catch block
			x.printStackTrace();
		}
	}

}
