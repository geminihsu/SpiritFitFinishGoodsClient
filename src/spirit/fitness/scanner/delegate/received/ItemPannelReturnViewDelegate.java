package spirit.fitness.scanner.delegate.received;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import spirit.fitness.scanner.common.Constrant;
import spirit.fitness.scanner.common.HttpRequestCode;
import spirit.fitness.scanner.delegate.ItemPannelBaseViewDelegate;
import spirit.fitness.scanner.model.Containerbean;
import spirit.fitness.scanner.model.Itembean;
import spirit.fitness.scanner.model.ModelDailyReportbean;
import spirit.fitness.scanner.model.ModelZone2bean;
import spirit.fitness.scanner.model.Modelbean;
import spirit.fitness.scanner.model.PickUpZoneMap;
import spirit.fitness.scanner.receving.ContainerPannel;
import spirit.fitness.scanner.restful.ContainerRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.FGRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.ModelZoneMapRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.listener.InventoryCallBackFunction;
import spirit.fitness.scanner.restful.listener.ModelZone2CallBackFunction;
import spirit.fitness.scanner.until.LoadingFrameHelper;
import spirit.fitness.scanner.until.LocationHelper;
import spirit.fitness.scanner.until.NetWorkHandler;
import spirit.fitness.scanner.zonepannel.ZoneMenu;

public class ItemPannelReturnViewDelegate extends ItemPannelBaseViewDelegate {

	private FGRepositoryImplRetrofit fgRepository;

	private String containerNo;

	public ItemPannelReturnViewDelegate(String content) {
		getInstance();
		initial(content);
	}

	
	@Override
	public void initial(String content) {
		
		scanInfo(content);
		exceuteCallback();
		loadModelMapZone2();
	}
	
	

	@Override
	public void scanInfo(String prevTxt) {
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
		
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				scanResultFrame.toFront();
				scanResultFrame.repaint();
			}
		});
	}

	@Override
	public void scanPanel(JPanel panel, String prevTxt) {
		panel.setLayout(null);
		Font font = new Font("Verdana", Font.BOLD, 18);

		ltotal = new JLabel("Total: 0");
		ltotal.setFont(font);
		ltotal.setBounds(35, 550, 200, 50);
		panel.add(ltotal);

		JLabel destination = new JLabel("");
		destination.setFont(font);
		destination.setBounds(35, 5, 300, 50);
		panel.add(destination);

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

					ltotal.setForeground(Color.BLACK);
					ltotal.setText("Total: " + set.size());

				}
			}
		});

		JScrollPane scrollPanel1 = new JScrollPane(inputSN);
		scrollPanel1.setBounds(35, 50, 265, 500);
		inputSN.setFont(font);
		panel.add(scrollPanel1);

		// Creating Assign Location button
		JButton locateButton = new JButton("Submit");
		locateButton.setFont(font);
		locateButton.setBounds(35, 600, 265, 50);
		locateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				isDefaultZone = true;

				if (inputSN.getText().isEmpty())
					JOptionPane.showMessageDialog(null, "Please scan serial number.");
				else {
					scanResultFrame.setVisible(false);
					scanResultFrame.dispose();
					destroy();

					items = inputSN.getText().toString();
					scanResultFrame.setVisible(false);
					scanResultFrame.dispose();

					// if (type == MOVING) {
					loadingframe = new LoadingFrameHelper("Checking data...");
					loading = loadingframe.loadingSample("Checking data...");

					String[] itemList = items.split("\n");

					if (itemList.length == 0 && !inputSN.getText().toString().equals("")) {
						itemList = new String[0];
						itemList[0] = inputSN.getText().toString();
					}

					List<Itembean> items = new ArrayList<Itembean>();
					scannedModel = itemList[0].substring(0, 6);
					scannedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
					for (String item : itemList) {
						Itembean _item = new Itembean();

						_item.SN = item;
						_item.ModelNo = scannedModel;
						items.add(_item);

					}
					// displayScanResultFrame(inputSN.getText().toString(), "000", type);

					checkReceiveItemExits(items);

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
					result = JOptionPane.showConfirmDialog(scanResultFrame, "Do you want to clear all items?", "",
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
				destroy();
				scanResultFrame.dispose();
				scanResultFrame.setVisible(false);
			}
		});

		panel.add(exitButton);
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
					+ ") <br/>" + "Total : " + sortedList.size() + " <br/>" + result + " <br/>"
					+ "to return location  ?</html>");

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

					scanInfo(content);

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
					String[] checkItem = items.split("\n");

					String updateTxt = "";
					HashSet<String> itemError = new HashSet<String>();
					for (String s : errorItem) {
						itemError.add(s);
					}

					for (String p : checkItem) {
						if (!itemError.contains(p)) {
							set.add(p);
							updateTxt += p + "\n";
						}
					}
					String[] item = updateTxt.split("\n");
					inputSN.setText(updateTxt);

					ltotal.setText("Total : " + set.size());

					scanResultFrame.setVisible(true);
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

	private void checkReceiveItemExits(List<Itembean> items) {
		try {
			fgRepository.getReceiveItemBySNList(items);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			NetWorkHandler.displayError(loadingframe);
		}
	}

	@Override
	public void submitServer(List<Itembean> items) {
		try {
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

					JOptionPane.showMessageDialog(null, "Add Data Success!");

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
				String[] scanItem = inputSN.getText().toString().split("\n");
				if (loadingframe != null) {
					loadingframe.setVisible(false);
					loadingframe.dispose();
				}

				if (items.size() == 0) {

					displayScanResultFrame(inputSN.getText().toString(), "333");

				} else if (items.size() > 0) {
					JOptionPane.showMessageDialog(null, "Items already exist.");

					if (items.size() > 0)
						checkScanResultFrame(items);
					/*
					 * if (scanResultFrame != null) { String updateTxt = ""; for (Itembean i :
					 * items) { updateTxt += i.SN + "\n"; } if (modelScanCurMap.isEmpty())
					 * ltotal.setText("Total : " + items.size()); else { set.clear();
					 * modelScanCurMap.clear(); for (Itembean s : items) { set.add(s.SN);
					 * 
					 * String modelNo = s.SN.substring(0, 10); if
					 * (!modelScanCurMap.containsKey(modelNo)) modelScanCurMap.put(modelNo, 1); else
					 * modelScanCurMap.put(modelNo, modelScanCurMap.get(modelNo) + 1);
					 * 
					 * } ltotal.setText(setModelScanCountLabel(set.size()));
					 * 
					 * } inputSN.setText(updateTxt); scanResultFrame.setVisible(true); } if
					 * (dialogFrame != null) { dialogFrame.dispose(); dialogFrame.setVisible(false);
					 * } } else ZoneMenu.getInstance(containers, inputSN.getText().toString(),
					 * assignType);
					 */

				}
			}

			@Override
			public void exception(String error) {
				NetWorkHandler.displayError(loadingframe);
				scanResultFrame.setVisible(true);
			}
		});
	}

}
