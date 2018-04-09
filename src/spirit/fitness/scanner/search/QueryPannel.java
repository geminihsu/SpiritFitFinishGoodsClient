package spirit.fitness.scanner.search;

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
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
import javax.swing.text.NumberFormatter;

import spirit.fitness.scanner.common.Constrant;
import spirit.fitness.scanner.common.HttpRequestCode;
import spirit.fitness.scanner.model.Itembean;
import spirit.fitness.scanner.model.Locationbean;
import spirit.fitness.scanner.model.ModelZone2bean;
import spirit.fitness.scanner.model.Modelbean;
import spirit.fitness.scanner.receving.ItemsPannel;
import spirit.fitness.scanner.report.ModelZone2Report;
import spirit.fitness.scanner.restful.FGRepositoryImplRetrofit;
import spirit.fitness.scanner.restful.listener.InventoryCallBackFunction;
import spirit.fitness.scanner.zonepannel.ReturnLocation;
import spirit.fitness.scanner.zonepannel.Zone1Location;
import spirit.fitness.scanner.zonepannel.Zone2Location;
import spirit.fitness.scanner.zonepannel.ZoneMenu;
import spirit.fitness.scanner.zonepannel.ReturnLocation.ZoneCodeReturnCallBackFunction;

public class QueryPannel implements ActionListener{
	
	private static QueryPannel queryPannel = null;
	
	public final static int INQUIRY = 3;
	public JFrame frame;
	private JTextField locationText;
	private ReturnLocation zoneCodeReturn;
	private Zone1Location zone1Location;
	private Zone2Location zone2Location;
	private ZoneMenu showRoom;
	//private ZoneMenu window;
	
	private int queryType;
	
	private FGRepositoryImplRetrofit fgRepository;

	
	public static QueryPannel getInstance() {
		if (queryPannel == null) {
			queryPannel = new QueryPannel();
		}
		return queryPannel;
	}
	
	public static boolean isExit() 
	 {
		 return queryPannel != null;
	 }
	
	public static void destory() 
	{
		queryPannel = null;
	}
	
	public QueryPannel() {
		QueryResult.isQueryRepeat = false;
		initialZoneCodeCallback();
		initialize();

	}
	
	public void initialZoneCodeCallback() 
	{
		fgRepository = new FGRepositoryImplRetrofit();
		fgRepository.setinventoryServiceCallBackFunction(new InventoryCallBackFunction() {

			@Override
			public void resultCode(int code) {
			
			}

			@Override
			public void getInventoryItems(List<Itembean> items) {
			   if(items.isEmpty()) 
			   {
					JOptionPane.showMessageDialog(null, "No Items");
			   }else 
			   {
				   clearZoneCodeCallback();
				   frame.dispose();
				   frame.setVisible(false);
				   
				   QueryResult window = new QueryResult();
				   if (queryType == QueryResult.QUERY_LOCATION)
						window.setContent(QueryResult.QUERY_LOCATION, items);
					else 
						window.setContent(QueryResult.QUERY_MODEL, items);

				    if(window.resultFrame != null)
				    	window.resultFrame.setVisible(true);
				   
			   }
			}

			@Override
			public void checkInventoryItems(List<Itembean> items) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void checkInventoryZone2Items(int result,List<Itembean> items) {
				// TODO Auto-generated method stub
				
			}
		});
		zoneCodeReturn = new ReturnLocation(null,-1);
		zoneCodeReturn.setZoneCodeReturnCallBackFunction(new ReturnLocation.ZoneCodeReturnCallBackFunction() {
			
			@Override
			public void getZoneCode(String code) {
				System.out.println(code);
				locationText.setText(code);
			}
		});
		
		zone1Location = new Zone1Location(null,-1);
		zone1Location.setZone1CodeCallBackFunction(new Zone1Location.Zone1CodeCallBackFunction() {
			
			@Override
			public void getZoneCode(String code) {
				System.out.println(code);
				locationText.setText(code);
			}
		});
		
		zone2Location = new Zone2Location(null,-1);
		zone2Location.setZone2CodeCallBackFunction(new Zone2Location.Zone2CodeCallBackFunction() {
			
			@Override
			public void getZoneCode(String code) {
				System.out.println(code);
				locationText.setText(code);
			}
		});
		
		showRoom = new ZoneMenu(null,-1);
		showRoom.setShowRoomCodeCallBackFunction(new ZoneMenu.ShowRoomCodeCallBackFunction() {
			
			@Override
			public void getZoneCode(String code) {
				System.out.println(code);
				locationText.setText(code);
			}
		});
		
	}

	/**
	 * Initialize the contents of the frame.
	 */
	public void initialize() {
		
		//JFrame.setDefaultLookAndFeelDecorated(false);
		//JDialog.setDefaultLookAndFeelDecorated(false);
		frame = new JFrame("Query Pannel");
		// Setting the width and height of frame
		frame.setSize(600, 400);
		frame.setLocationRelativeTo(null);
		frame.setLocationRelativeTo(null);
		frame.setUndecorated (true);
		frame.setResizable(false); 
		
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Constrant.FRAME_BORDER_BACKGROUN_COLOR));
		
		panel.setBackground(Constrant.BACKGROUN_COLOR);
		// adding panel to frame
		frame.add(panel);
		
		placeComponents(panel);

		//frame.setUndecorated(true);
		//frame.getRootPane().setWindowDecorationStyle(JRootPane.COLOR_CHOOSER_DIALOG);
		frame.setBackground(Color.WHITE);
		frame.setVisible(true);
       // frame.setDefaultLookAndFeelDecorated(true);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				clearZoneCodeCallback();
				
				frame.dispose();
				frame.setVisible(false);
			}
		});

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	private void placeComponents(JPanel panel) {

		/*
		 * We will discuss about layouts in the later sections of this tutorial. For now
		 * we are setting the layout to null
		 */
		panel.setLayout(null);
		Font font = new Font("Verdana", Font.BOLD, 18);
		// Creating JLabel
		JLabel modelLabel = new JLabel("Model");
		/*
		 * This method specifies the location and size of component. setBounds(x, y,
		 * width, height) here (x,y) are cordinates from the top left corner and
		 * remaining two arguments are the width and height of the component.
		 */
		modelLabel.setBounds(100, 100, 200, 25);
		modelLabel.setFont(font);
		panel.add(modelLabel);
		
		JLabel ltotal = new JLabel("");
		ltotal.setFont(font);
		ltotal.setBounds(230, 150, 250, 50);
		panel.add(ltotal);

		/*
		 * Creating text field where user is supposed to enter user name.
		 */
		JTextField modelText = new JTextField(20);
		modelText.setFont(font);
		modelText.setBounds(230, 100, 320, 50);
		modelText.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent event) {
				System.out.println(modelText.getText().toString());

				if (modelText.getText().toString().length() >= 6) {
					String modelNo = modelText.getText().toString().substring(0,6);
					Modelbean model = Constrant.models.get(modelNo);
					if (model != null)
						ltotal.setText(model.Desc);
					else
						ltotal.setText("No model can be find.");
				}

			}

			@Override
			public void keyReleased(KeyEvent event) {
				// System.out.println("key released");
			}

			@Override
			public void keyPressed(KeyEvent event) {
				// System.out.println("key pressed");
			}
		});
		panel.add(modelText);

		// Same process for password label and text field.
		JLabel locationLabel = new JLabel("Location");
		locationLabel.setFont(font);
		locationLabel.setBounds(100, 200, 200, 50);
		panel.add(locationLabel);

		locationText = new JTextField(20);
		locationText.setFont(font);
		locationText.setBounds(230, 200, 250, 50);
		
		panel.add(locationText);
		
		

		// Creating Map button
		JButton MapButton = new JButton("Map");
		MapButton.setFont(font);
		MapButton.setBounds(470, 200, 80, 50);

		MapButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//window = new ZoneMenu(null, INQUIRY);
				//window.frame.setVisible(true);
				ZoneMenu.getInstance(null, INQUIRY);
			}
		});
		panel.add(MapButton);

		// Creating Query button
		JButton queryButton = new JButton("Find");
		queryButton.setFont(font);
		queryButton.setBounds(230, 260, 150, 50);
		queryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				if (!verifyText(modelText.getText().toString(), locationText.getText().toString()))
					JOptionPane.showMessageDialog(null, "Please enter correct infomation!");
				else {

					EventQueue.invokeLater(new Runnable() {
						public void run() {
							try {

								passQueryResult(modelText.getText().toString(), locationText.getText().toString());

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
		resetButton.setBounds(400, 260, 150, 50);
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				modelText.setText("");
				locationText.setText("");
				ltotal.setText("");
			}
		});

		panel.add(resetButton);
		
		
		// Creating Query button
		JButton exitButton = new JButton("Exit");
		exitButton.setFont(font);
		exitButton.setBounds(230, 320, 320, 50);
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				clearZoneCodeCallback();
				//if(window != null && window.frame != null)
				//	window.frame.setVisible(false);
				ZoneMenu.destory();
				
				queryPannel = null;
				frame.dispose();
				frame.setVisible(false);
			}
		});

		panel.add(exitButton);
	}

	private void passQueryResult(String modelNo, String Location) {
		if ((!modelNo.equals("") && !Location.equals("")))
			queryType = QueryResult.QUERY_MODEL_LOCATION;
		else
			queryType = (modelNo.equals("") && !Location.equals("")) ? QueryResult.QUERY_LOCATION
					: QueryResult.QUERY_MODEL;

		if (queryType == QueryResult.QUERY_MODEL_LOCATION) {
			try {
				if (modelNo.length() > 6) {
					// from scanner
					modelNo = modelNo.substring(0, 6);
				}
				
				fgRepository.getItemsByModelAndLocation(modelNo,Integer.valueOf(Location));

			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (queryType == QueryResult.QUERY_LOCATION) {

			try {
				fgRepository.getItemsByLocation(Integer.valueOf(Location));

			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (queryType == QueryResult.QUERY_MODEL) {
			if (modelNo.length() > 6) {
				// from scanner
				modelNo = modelNo.substring(0, 6);
			}

			try {
				fgRepository.getItemsByModel(modelNo);

			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	private boolean verifyText(String model, String location) {
		if (model.equals("") && location.equals(""))
			return false;

		else if (model.equals("")) {
			if (location.length() != 3)
				return false;
			Locationbean map = Constrant.locations.get(location);
			if (map == null)
				return false;
		} else if (location.equals("")) {
			if ((model.length() != 16) && (model.length() != 6))
				return false;
      
			if(model.length() == 16)
				model = model.substring(0,6);
			Modelbean map = Constrant.models.get(model);
			if (map == null)
				return false;
		}

		return true;
	}
	
	
	private void clearZoneCodeCallback() 
	{
		
		if(zone1Location.getZone1CodeCallBackFunction()!=null)
			zone1Location.clearZone1CodeCallBackFunction();
		if(zone2Location.getZone2CodeCallBackFunction()!=null)
			zone2Location.clearZone2CodeCallBackFunction();
		if(zoneCodeReturn.getZoneCodeReturnCallBackFunction()!=null)
			zoneCodeReturn.clearZoneCodeReturnCallBackFunction();
		if(showRoom.getShowRoomCodeCallBackFunction()!=null)
			showRoom.clearShowRoomCodeCallBackFunction();
		
		if(ZoneMenu.showRoomCodeCallBackFunction != null)
			ZoneMenu.showRoomCodeCallBackFunction = null;
		
		if(ZoneMenu.qcCodeCallBackFunction != null)
			ZoneMenu.qcCodeCallBackFunction = null;
		
		if(ZoneMenu.reworkCodeCallBackFunction != null)
			ZoneMenu.reworkCodeCallBackFunction = null;
		
		if(ZoneMenu.scrappedCodeCallbackFunction != null)
			ZoneMenu.scrappedCodeCallbackFunction = null;
		
	}
	
}
