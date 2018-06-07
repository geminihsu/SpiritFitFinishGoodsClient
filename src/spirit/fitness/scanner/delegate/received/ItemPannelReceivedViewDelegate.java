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
import javax.swing.Timer;
import javax.swing.WindowConstants;

import spirit.fitness.scanner.common.Constrant;
import spirit.fitness.scanner.common.HttpRequestCode;
import spirit.fitness.scanner.delegate.ItemPannelBaseViewDelegate;
import spirit.fitness.scanner.model.Containerbean;
import spirit.fitness.scanner.model.Itembean;
import spirit.fitness.scanner.model.ModelDailyReportbean;
import spirit.fitness.scanner.model.ModelZone2bean;
import spirit.fitness.scanner.model.Modelbean;
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
	private String containerNo;
	private Timer timer;
	private int isTimeOut = 1;

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
		scanInfo(content);
		exceuteCallback();
		loadModelMapZone2();
	}

	@Override
	public void initial(List<Containerbean> _container, String content, String location) {
		containers = _container;
		containerNo = containers.get(0).ContainerNo;
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
		panel.setLayout(null);
		Font font = new Font("Verdana", Font.BOLD, 18);
		// Creating JLabel
		JLabel shippingLabel = new JLabel("Received Date : ");

		shippingLabel.setBounds(50, 30, 200, 25);
		shippingLabel.setFont(font);
		panel.add(shippingLabel);

		scannedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		JLabel shippingDate = new JLabel(scannedDate);
		shippingDate.setText(scannedDate);
		shippingDate.setFont(font);
		shippingDate.setBounds(320, 20, 250, 50);
		panel.add(shippingDate);
		modelTotalCurMap = new LinkedHashMap<String, Integer>();
		modelScanCurMap = new LinkedHashMap<String, Integer>();
		containerNo = containers.get(0).ContainerNo;
		orderTotalCount = 0;
		for (Containerbean item : containers) {
			orderTotalCount += Integer.valueOf(item.SNEnd.substring(10, 16))
					- Integer.valueOf(item.SNBegin.substring(10, 16)) + 1;
			modelTotalCurMap.put(item.SNBegin.substring(0, 10), Integer.valueOf(item.SNEnd.substring(10, 16))
					- Integer.valueOf(item.SNBegin.substring(10, 16)) + 1);
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

		inputSN = new JTextArea(20, 15);

		prevTxt = "1153161804000717\n" + 
				"1153161804000718\n" + 
				"1153161804000719\n" + 
				"1153161804000720\n" + 
				"1153161804000721\n" + 
				"1153161804000722\n" + 
				"1153161804000723\n" + 
				"1153161804000724\n" + 
				"1153161804000725\n" + 
				"1153161804000726\n" + 
				"1153161804000727\n" + 
				"1153161804000728\n" + 
				"1153161804000729\n" + 
				"1153161804000730\n" + 
				"1153161804000731\n" + 
				"1153161804000732\n" + 
				"1153161804000733\n" + 
				"1153161804000734\n" + 
				"1153161804000735\n" + 
				"1153161804000736\n" + 
				"1153161804000737\n" + 
				"1153161804000738\n" + 
				"1153161804000739\n" + 
				"1153161804000740\n" + 
				"1153161804000741\n" + 
				"1153161804000742\n" + 
				"1153161804000743\n" + 
				"1153161804000744\n" + 
				"1153161804000745\n" + 
				"1153161804000746\n" + 
				"1153161804000747\n" + 
				"1153161804000748\n" + 
				"1153161804000749\n" + 
				"1153161804000750\n" + 
				"1153161804000751\n" + 
				"1153161804000752\n" + 
				"1153161804000753\n" + 
				"1153161804000754\n" + 
				"1153161804000755\n" + 
				"1153161804000756\n" + 
				"1153161804000757\n" + 
				"1153161804000758\n" + 
				"1153161804000759\n" + 
				"1153161804000760\n" + 
				"1153161804000761\n" + 
				"1153161804000762\n" + 
				"1153161804000763\n" + 
				"1153161804000764\n" + 
				"1153161804000765\n" + 
				"1153161804000766\n" + 
				"1153161804000767\n" + 
				"1153161804000768\n" + 
				"1153161804000769\n" + 
				"1153161804000770\n" + 
				"1153161804000771\n" + 
				"1153161804000772\n" + 
				"1153161804000773\n" + 
				"1153161804000774\n" + 
				"1153161804000775\n" + 
				"1153161804000776\n" + 
				"1153161804000777\n" + 
				"1153161804000778\n" + 
				"1153161804000779\n" + 
				"1153161804000780\n" + 
				"1153161804000781\n" + 
				"1153161804000782\n" + 
				"1153161804000783\n" + 
				"1153161804000784\n" + 
				"1153161804000785\n" + 
				"1153161804000786\n" + 
				"1153161804000787\n" + 
				"1153161804000788\n" + 
				"1153161804000789\n" + 
				"1153161804000790\n" + 
				"1153161804000791\n" + 
				"1153161804000792\n" + 
				"1153161804000793\n" + 
				"1153161804000794\n" + 
				"1153161804000795\n" + 
				"1153161804000796\n" + 
				"1153161804000797\n" + 
				"1153161804000798\n" + 
				"1153161804000799\n" + 
				"1153161804000800\n" + 
				"1153161804000801\n" + 
				"1153161804000802\n" + 
				"1153161804000803\n" + 
				"1153161804000804\n" + 
				"1153161804000805\n" + 
				"1153161804000806\n" + 
				"1153161804000807\n" + 
				"1153161804000808\n" + 
				"1153161804000809\n" + 
				"1153161804000810\n" + 
				"1153161804000811\n" + 
				"1153161804000812\n" + 
				"1153161804000813\n" + 
				"1153161804000814\n" + 
				"1153161804000815\n" + 
				"1153161804000816\n" + 
				"1153161804000817\n" + 
				"1153161804000818\n" + 
				"1153161804000819\n" + 
				"1153161804000820\n" + 
				"1153161804000821\n" + 
				"1153161804000822\n" + 
				"1153161804000823\n" + 
				"1153161804000824\n" + 
				"1153161804000825\n" + 
				"1153161804000826\n" + 
				"1153161804000827\n" + 
				"1153161804000828\n" + 
				"1153161804000829\n" + 
				"1153161804000830\n" + 
				"1153161804000831\n" + 
				"1153161804000832\n" + 
				"1153161804000833\n" + 
				"1153161804000834\n" + 
				"1153161804000835\n" + 
				"1153161804000836\n" + 
				"1153161804000837\n" + 
				"1153161804000838\n" + 
				"1153161804000839\n" + 
				"1153161804000840\n" + 
				"1153161804000841\n" + 
				"1153161804000842\n" + 
				"1153161804000843\n" + 
				"1153161804000844\n" + 
				"1153161804000845\n" + 
				"1153161804000846\n" + 
				"1153161804000847\n" + 
				"1153161804000848\n" + 
				"1153161804000849\n" + 
				"1153161804000850\n" + 
				"1153161804000851\n" + 
				"1153161804000852\n" + 
				"1153161804000853\n" + 
				"1153161804000854\n" + 
				"1153161804000855\n" + 
				"1153161804000856\n" + 
				"1153161804000857\n" + 
				"1153161804000858\n" + 
				"1153161804000859\n" + 
				"1153161804000860\n" + 
				"1153161804000861\n" + 
				"1153161804000862\n" + 
				"1153161804000863\n" + 
				"1153161804000864\n" + 
				"1153161804000865\n" + 
				"1153161804000866\n" + 
				"1153161804000867\n" + 
				"1153161804000868\n" + 
				"1153161804000869\n" + 
				"1153161804000870\n" + 
				"1153161804000871\n" + 
				"1153161804000872\n" + 
				"1153161804000873\n" + 
				"1153161804000874\n" + 
				"1153161804000875\n" + 
				"1153161804000876\n" + 
				"1153161804000877\n" + 
				"1153161804000878\n" + 
				"1153161804000879\n" + 
				"1153161804000880\n" + 
				"1153161804000881\n" + 
				"1153161804000882\n" + 
				"1153161804000883\n" + 
				"1153161804000884\n" + 
				"1153161804000885\n" + 
				"1153161804000886\n" + 
				"1153161804000887\n" + 
				"1153161804000888\n" + 
				"1153161804000889\n" + 
				"1153161804000890\n" + 
				"1153161804000891\n" + 
				"1153161804000892\n" + 
				"1153161804000893\n" + 
				"1153161804000894\n" + 
				"1153161804000895\n" + 
				"1153161804000896\n" + 
				"1153161804000897\n" + 
				"1153161804000898\n" + 
				"1153161804000899\n" + 
				"1153161804000900\n" + 
				"1153161804000901\n" + 
				"1153161804000902\n" + 
				"1153161804000903\n" + 
				"1153161804000904\n" + 
				"1153161804000905\n" + 
				"1153161804000906\n" + 
				"1153161804000907\n" + 
				"1153161804000908\n" + 
				"1153161804000909\n" + 
				"1153161804000910\n" + 
				"1153161804000911\n" + 
				"1153161804000912\n" + 
				"1153161804000913\n" + 
				"1153161804000914\n" + 
				"1153161804000915\n" + 
				"1153161804000916\n" + 
				"1153161804000917\n" + 
				"1153161804000918\n" + 
				"1153161804000919\n" + 
				"1153161804000920\n" + 
				"1153161804000921\n" + 
				"1153161804000922\n" + 
				"1153161804000923\n" + 
				"1153161804000924\n" + 
				"1153161804000925\n" + 
				"1153161804000926\n" + 
				"1153161804000927\n" + 
				"1153161804000928\n" + 
				"1153161804000929\n" + 
				"1153161804000930\n" + 
				"1153161804000931\n" + 
				"1153161804000932\n" + 
				"1153161804000933\n" + 
				"1153161804000934\n" + 
				"1153161804000935\n" + 
				"1153161804000936\n" + 
				"1153161804000937\n" + 
				"1153161804000938\n" + 
				"1153161804000939\n" + 
				"1153161804000940\n" + 
				"1153161804000941\n" + 
				"1153161804000942\n" + 
				"1153161804000943\n" + 
				"1153161804000944\n" + 
				"1153161804000945\n" + 
				"1153161804000946\n" + 
				"1153161804000947\n" + 
				"1153161804000948\n" + 
				"1153161804000949\n" + 
				"1153161804000950\n" + 
				"1153161804000951\n" + 
				"1153161804000952\n" + 
				"1153161804000953\n" + 
				"1153161804000954\n" + 
				"1153161804000955\n" + 
				"1153161804000956\n" + 
				"1153161804000957\n" + 
				"1153161804000958\n" + 
				"1153161804000959\n" + 
				"1153161804000960\n" + 
				"1153161804000961\n" + 
				"1153161804000962\n" + 
				"1153161804000963\n" + 
				"1153161804000964\n" + 
				"1153161804000965\n" + 
				"1153161804000966\n" + 
				"1153161804000967\n" + 
				"1153161804000968\n" + 
				"1153161804000969\n" + 
				"1153161804000970\n" + 
				"1153161804000971\n" + 
				"1153161804000972\n" + 
				"1153161804000973\n" + 
				"1153161804000974\n" + 
				"1153161804000975\n" + 
				"1153161804000976\n" + 
				"1153161804000977\n" + 
				"1153161804000978\n" + 
				"1153161804000979\n" + 
				"1153161804000980\n" + 
				"1153161804000981\n" + 
				"1153161804000982\n" + 
				"1153161804000983\n" + 
				"1153161804000984\n" + 
				"1153161804000985\n" + 
				"1153161804000986\n" + 
				"1153161804000987\n" + 
				"1153161804000988\n" + 
				"1153161804000989\n" + 
				"1153161804000990\n" + 
				"1153161804000991\n" + 
				"1153161804000992\n" + 
				"1153161804000993\n" + 
				"1153161804000994\n" + 
				"1153161804000995\n" + 
				"1153161804000996\n" + 
				"1153161804000997\n" + 
				"1153161804000998\n" + 
				"1153161804000999\n" + 
				"1153161804001000\n" + 
				"1153161804001001\n" + 
				"1153161804001002\n" + 
				"1153161804001003\n" + 
				"1153161804001004\n" + 
				"1153161804001005\n" + 
				"1153161804001006\n" + 
				"1153161804001007\n" + 
				"1153161804001008\n" + 
				"1153161804001009\n" + 
				"1153161804001010\n" + 
				"1153161804001011\n" + 
				"1153161804001012\n" + 
				"1153161804001013\n" + 
				"1153161804001014\n" + 
				"1153161804001015\n" + 
				"1153161804001016\n" + 
				"1153161804001017\n" + 
				"1153161804001018\n" + 
				"1153161804001019\n" + 
				"1153161804001020\n" + 
				"1153161804001021\n" + 
				"1153161804001022\n" + 
				"1153161804001023\n" + 
				"1153161804001024\n" + 
				"1153161804001025\n" + 
				"1153161804001026\n" + 
				"1153161804001027\n" + 
				"1153161804001028\n" + 
				"1153161804001029\n" + 
				"1153161804001030\n" + 
				"1153161804001031\n" + 
				"1153161804001032\n" + 
				"1153161804001033\n" + 
				"1153161804001034\n" + 
				"1153161804001035\n" + 
				"1153161804001036\n" + 
				"1153161804001037\n" + 
				"1153161804001038\n" + 
				"1153161804001039\n" + 
				"1153161804001040\n" + 
				"1153161804001041\n" + 
				"1153161804001042\n" + 
				"1153161804001043\n" + 
				"1153161804001044\n" + 
				"1153161804001045\n" + 
				"1153161804001046\n" + 
				"1153161804001047\n" + 
				"1153161804001048\n" + 
				"1153161804001049\n" + 
				"1153161804001050\n" + 
				"1153161804001051\n" + 
				"1153161804001052\n" + 
				"1153161804001053\n" + 
				"1153161804001054\n" + 
				"1153161804001055\n" + 
				"1153161804001056\n" + 
				"1153161804001057\n" + 
				"1153161804001058\n" + 
				"1153161804001059\n" + 
				"1153161804001060\n" + 
				"1153161804001061\n" + 
				"1153161804001062\n" + 
				"1153161804001063\n" + 
				"1153161804001064\n" + 
				"1153161804001065\n" + 
				"1153161804001066\n" + 
				"1153161804001067\n" + 
				"1153161804001068\n" + 
				"1153161804001069\n" + 
				"1153161804001070\n" + 
				"1153161804001071\n" + 
				"1153161804001072\n" + 
				"1153161804001073\n" + 
				"1153161804001074\n" ;
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
		queryButton.setBounds(420, 670, 150, 50);

		queryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				destroy();
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
					result = JOptionPane.showConfirmDialog(scanResultFrame, "Do you want to clear the all item?", "",
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
				Constrant.serial_list = "Container No." + containers.get(0).ContainerNo + "\n Receiving SN : \n"
						+ inputSN.getText().toString();

				isDefaultZone = true;

				if (inputSN.getText().isEmpty())
					JOptionPane.showMessageDialog(null, "Please scan serial number.");
				else if (item.length != orderTotalCount) {
					JOptionPane.showMessageDialog(null, "Quantity Error!");
					checkMissItems();
				} else {
					scanResultFrame.setVisible(false);
					scanResultFrame.dispose();

					items = inputSN.getText().toString();
					scanResultFrame.setVisible(false);
					scanResultFrame.dispose();
					destroy();
					// if (type == MOVING) {
					loadingframe = new LoadingFrameHelper("Checking data...");
					loading = loadingframe.loadingSample("Checking data...");

					String[] itemList = items.split("\n");

					if (itemList.length == 0 && !inputSN.getText().toString().equals("")) {
						itemList = new String[0];
						itemList[0] = inputSN.getText().toString();
					}
					int startIndex = 0;
					int endIndex = 0;
					scannedModel = itemList[0].substring(0, 6);
					for (Containerbean c : containers) {
						if (c.SNBegin.substring(0, 6).equals(scannedModel)) {
							startIndex = Integer.valueOf(c.SNBegin.substring(10, 16));
							endIndex = Integer.valueOf(c.SNEnd.substring(10, 16));
						}

					}
					List<Itembean> items = new ArrayList<Itembean>();
					outRangeSN = new ArrayList<Itembean>();
					scannedModel = itemList[0].substring(0, 6);
					String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(Calendar.getInstance().getTime());
					for (String item : itemList) {
						Itembean _item = new Itembean();

						_item.SN = item;
						_item.ModelNo = scannedModel;
						items.add(_item);
						if (Integer.valueOf(_item.SN.substring(10, 16)) - startIndex < 0
								|| endIndex - Integer.valueOf(_item.SN.substring(10, 16)) < 0)
							outRangeSN.add(_item);

					}

					if (outRangeSN.size() > 0)
						checkScanResultOutOfFrame(items);
					else
						checkReceiveItemExits(items);

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
				Constrant.serial_list = "Container No." + containers.get(0).ContainerNo + "\n Receiving SN : \n"
						+ inputSN.getText().toString();

				String[] scanitem = inputSN.getText().toString().split("\n");

				if (inputSN.getText().isEmpty())
					JOptionPane.showMessageDialog(null, "Please scan serial number.");
				else if (scanitem.length != orderTotalCount) {
					JOptionPane.showMessageDialog(null, "Quantity Error!");
					checkMissItems();
				} else {

					items = inputSN.getText().toString();
					scanResultFrame.setVisible(false);
					scanResultFrame.dispose();
					destroy();
					// if (type == MOVING) {
					loadingframe = new LoadingFrameHelper("Checking data...");
					loading = loadingframe.loadingSample("Checking data...");

					String[] itemList = items.split("\n");

					if (itemList.length == 0 && !inputSN.getText().toString().equals("")) {
						itemList = new String[0];
						itemList[0] = inputSN.getText().toString();
					}
					int startIndex = 0;
					int endIndex = 0;
					scannedModel = itemList[0].substring(0, 6);
					for (Containerbean c : containers) {
						if (c.SNBegin.substring(0, 6).equals(scannedModel)) {
							startIndex = Integer.valueOf(c.SNBegin.substring(10, 16));
							endIndex = Integer.valueOf(c.SNEnd.substring(10, 16));
						}

					}

					List<Itembean> items = new ArrayList<Itembean>();
					outRangeSN = new ArrayList<Itembean>();
					String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(Calendar.getInstance().getTime());
					for (String item : itemList) {
						Itembean _item = new Itembean();

						_item.SN = item;
						_item.ModelNo = scannedModel;
						items.add(_item);

						if (Integer.valueOf(_item.SN.substring(10, 16)) - startIndex < 0
								|| endIndex - Integer.valueOf(_item.SN.substring(10, 16)) < 0)
							outRangeSN.add(_item);

					}

					if (outRangeSN.size() > 0)
						checkScanResultOutOfFrame(items);
					else
						checkReceiveItemExits(items);

				}
			}
		});

		panel.add(location);

		// Creating Exit button
		JButton back = new JButton("Back");
		back.setFont(font);
		back.setBounds(50, 670, 150, 50);
		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				scanResultFrame.dispose();
				scanResultFrame.setVisible(false);
				ContainerPannel.getInstance();
				destroy();
			}
		});

		panel.add(back);

		// Creating Exit button
		JButton sort = new JButton("Sort");
		sort.setFont(font);
		sort.setBounds(240, 670, 150, 50);
		sort.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				String[] scanItem = inputSN.getText().toString().split("\n");

				Arrays.sort(scanItem);

				String sortResult = "";

				for (String s : scanItem) {

					sortResult += s + "\n";

				}

				inputSN.setText(sortResult);
			}
		});

		panel.add(sort);
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
						_item.ModelNo = scannedModel;
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

					modelScanCurMap.clear();

					for (String p : checkItem) {
						if (!itemError.contains(p)) {
							set.add(p);
							if (modelScanCurMap.get(p.substring(0, 10)) == null)
								modelScanCurMap.put(p.substring(0, 10), 1);
							else
								modelScanCurMap.put(p.substring(0, 10), modelScanCurMap.get(p.substring(0, 10)) + 1);
							updateTxt += p + "\n";
						}
					}
					String[] item = updateTxt.split("\n");
					inputSN.setText(updateTxt);

					if (item.length == 1 && item[0].equals(""))
						ltotal.setText("Total : 0");
					else
						ltotal.setText(setModelScanCountLabel(set.size()));

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

		for (Itembean i : outRangeSN) {
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
			}
		});

		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialogFrame.dispose();
				dialogFrame.setVisible(false);
				scanResultFrame.setVisible(true);
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
				scanResultFrame.setVisible(true);
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

	private void checkMissItems() {
		String[] scanItem = inputSN.getText().toString().split("\n");

		int startIndex = 0;
		int endIndex = 0;
		scannedModel = scanItem[0].substring(0, 6);
		for (Containerbean c : containers) {
			if (c.SNBegin.substring(0, 6).equals(scannedModel)) {
				startIndex = Integer.valueOf(c.SNBegin.substring(10, 16));
				endIndex = Integer.valueOf(c.SNEnd.substring(10, 16));
			}

		}
		Arrays.sort(scanItem);
		String noScan = "";
		String sortResult = "";
		int idx = 0;
		for (int i = startIndex; i < endIndex + 1; i++) {

			if (idx < scanItem.length) {
				if (Integer.valueOf(scanItem[idx].substring(10, 16)) == i) {
					sortResult += scanItem[idx] + "\n";
					idx++;
				} else {
					noScan += (scanItem[idx].substring(0, 10)) + i + "\n";
				}
			}

		}

		inputSN.setText(sortResult);

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
				String[] scanItem = inputSN.getText().toString().split("\n");
				if (loadingframe != null) {
					loadingframe.setVisible(false);
					loadingframe.dispose();
				}

				if (items.size() == 0) {
					if (isDefaultZone)
						displayScanResultFrame(inputSN.getText().toString(), "000");
					else {
						// ZoneMenu.getInstance(containers, inputSN.getText().toString(), 0);
						Zone1Location window = new Zone1Location(containers, inputSN.getText().toString(), 0);
						window.frame.setVisible(true);
					}
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
					Constrant.serial_list = "";
					JOptionPane.showMessageDialog(null, "Insert Data Success!");
					EmailHelper.sendMail(scannedDate, containers, scanContent, "geminih@spiritfitness.com");
					EmailHelper.sendMail(scannedDate, containers, scanContent, "vickie@spiritfitness.com");
					EmailHelper.sendMail(scannedDate, containers, scanContent, "ashleyg@spiritfitness.com");
				}

			}

			@Override
			public void deleteContainerIteam(boolean result) {
				// TODO Auto-generated method stub

			}
		});

	}

}
