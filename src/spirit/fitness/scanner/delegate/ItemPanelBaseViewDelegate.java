package spirit.fitness.scanner.delegate;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

import spirit.fitness.scanner.common.Constrant;
import spirit.fitness.scanner.common.HttpRequestCode;
import spirit.fitness.scanner.model.Containerbean;
import spirit.fitness.scanner.model.Itembean;
import spirit.fitness.scanner.model.ModelDailyReportbean;
import spirit.fitness.scanner.model.ModelZone2bean;
import spirit.fitness.scanner.model.PickUpZoneMap;
import spirit.fitness.scanner.restful.ContainerRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.FGRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.ModelZoneMapRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.listener.ContainerCallBackFunction;
import spirit.fitness.scanner.restful.listener.InventoryCallBackFunction;
import spirit.fitness.scanner.restful.listener.ModelZone2CallBackFunction;
import spirit.fitness.scanner.until.EmailHelper;
import spirit.fitness.scanner.until.LoadingFrameHelper;
import spirit.fitness.scanner.zonepanel.ZoneMenu;

public class ItemPanelBaseViewDelegate {
	protected static final String TEXT_SUBMIT = "text-submit";
	protected static final String INSERT_BREAK = "insert-break";
	protected static ItemPanelBaseViewDelegate itemPanel;
	protected ModelZoneMapRepositoryImplRetrofit fgModelZone2;
	private ContainerRepositoryImplRetrofit containerRepository;

	

	protected JTextArea inputSN;
	protected JFrame scanResultFrame;
	protected JFrame dialogFrame;
	protected LoadingFrameHelper loadingframe;
	protected JProgressBar loading;
	protected JLabel ltotal;
	protected JLabel destination;
	
	protected boolean isDefaultZone;
	protected String scannedDate;
	protected String scannedModel;
	protected String items;
	protected String scanContent = "";
	protected int orderTotalCount;
	protected HashSet<String> set;
	
	// Key:modelID, value:current scanner quality
	protected LinkedHashMap<String, Integer> modelScanCurMap;

	// Key:modelID, value:total quality
	protected LinkedHashMap<String, Integer> modelTotalCurMap;
	
	public ItemPanelBaseViewDelegate() 
	{
		
	}
	

	public static ItemPanelBaseViewDelegate getInstance() {
		if (itemPanel == null) {
			itemPanel = new ItemPanelBaseViewDelegate();
		}
		return itemPanel;
	}

	
	public void initial(String content) 
	{
		
	}
	
	public void initial(String content, String location) 
	{
		
	}
	
	public void initial(List<Containerbean> _container,String content) 
	{
		
	}
	
	public void initial(List<Containerbean> _container,String content, String location) 
	{
		
	}
	
	public static boolean isExit() {
		return itemPanel != null;
	}

	public static void destroy() {
		if (itemPanel != null)
			itemPanel = null;
	}
	

	public void scanInfo(String prevTxt) {

	}


	public void scanPanel(JPanel panel, String prevTxt) 
	{
		
	}
	
	public void displayScanResultFrame(String content, String location) {

	}
	
	public void checkScanResultFrame(List<Itembean> _items) 
	{
		
	}
	
	
	private void submitServer(int type, List<Itembean> items) 
	{
		
	}
	// Loading Models data from Server
	public void loadModelMapZone2() {
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

	}

	protected String setModelScanCountLabel(int curCount) {
		String modelQty = "<html>" + "Total : " + curCount + "/" + String.valueOf(orderTotalCount) + " </br>";
		for (Map.Entry<String, Integer> location : modelScanCurMap.entrySet()) {
			int cnt = 0;

			modelQty += location.getKey().substring(0, 10) + "(" + location.getValue() + "/"
					+ modelTotalCurMap.get(location.getKey().substring(0, 10)) + ") </br>";
		}
		modelQty = modelQty + "</br></html>";
		return modelQty;
	}

	public void submitServer(List<Itembean> items) {
		// TODO Auto-generated method stub
		
	}

}
