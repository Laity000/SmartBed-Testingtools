package com.virtualbed.client;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.virtualbed.communication.Communication;
import com.virtualbed.communication.PackageComm;
import com.virtualbed.communication.ServerComm;
import com.virtualbed.communication.WebsocketComm;
import com.virtualbed.messages.Posture;
import com.virtualbed.messages.Utils;
import com.virtualbed.stage.ControlledStage;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;


/**
 *
 * @Title: LoginController.java
 * @Description: TODO 登录窗口控制器
 * @author ZhangJing   https://github.com/Laity000/ChatRoom-JavaFX
 * @date 2017年5月17日 上午11:19:25
 *
 */


public class BedController extends ControlledStage implements  Initializable{
	private static final Logger logger = LoggerFactory.getLogger(BedController.class);
	//Stage管理器
	//private StageController myController;

	//loginController对象
	private static BedController instance;

	//服务端对象
	private ServerSocket ss = null;
	//通信对象
	private Communication comm;
	//设备工作状态
	private String bedState = Utils.BED_STATE_DONE;
	//通信模式
    String model = "server";

	@FXML private BorderPane borderPane;

	@FXML private TextField hostTextfield;
	@FXML private TextField passwordTextfield;
    @FXML private TextField portTextfield;
    @FXML private TextField PIDTextfield;
    @FXML private TextArea feedbackTextarea;

    @FXML private TextField headTextfield;
    @FXML private TextField legTextfield;
    @FXML private TextField leftTextfield;
    @FXML private TextField rightTextfield;
    @FXML private TextField liftTextfield;
    @FXML private TextField beforeTextfield;
    @FXML private TextField afterTextfield;

    @FXML private ChoiceBox<String> bedStateChoiceBox;
    @FXML private ChoiceBox<String> protocolChoiceBox;

    private double xOffset;
    private double yOffset;

    //注意这里不是单例的形式，UI的Controller相当于回调函数的管理集合。
    //不能用private单例调用，应该由系统调用。这里是为了获得LoginController对象。
    public BedController() {
		// TODO Auto-generated constructor stub
    	instance = this;
	}
    /**
     * 获得UI的Controller对象
     * @return
     */
    public static BedController getInstance(){
    	return instance;
    }

    /**
     * 得到设备工作状态
     * @return
     */
    public String getBedState() {
		return bedState;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		/* Drag and Drop */
		borderPane.setOnMousePressed(event -> {
			xOffset = getLocalStage().getX() - event.getScreenX();
			yOffset = getLocalStage().getY() - event.getScreenY();
			borderPane.setCursor(Cursor.CLOSED_HAND);
		});
		borderPane.setOnMouseDragged(event -> {
			getLocalStage().setX(event.getScreenX() + xOffset);
			getLocalStage().setY(event.getScreenY() + yOffset);
		});
		borderPane.setOnMouseReleased(event -> {
			borderPane.setCursor(Cursor.DEFAULT);
		});
		// 设置图标
		setIcon("images/icon_chatroom.png");
		// 设备工作状态选择
		bedStateChoiceBox.getSelectionModel().selectedItemProperty()
				.addListener((ObservableValue<? extends String> ov, String old_val, String new_val) -> {
					bedState = new_val;
				});
		//通信方式选择
        protocolChoiceBox.getSelectionModel().selectedItemProperty().addListener(
        		(ObservableValue<? extends String> ov, String old_val, String new_val)->
        		{
        			model = new_val;
        			switch (model) {
        			case "server":
        			case "client-package":
        				portTextfield.setText("8283");
        				break;
        			case "client-websocket":
        				portTextfield.setText("8282");
        				break;
        			}
        		});

	}

	/**
	 * 随机按钮事件：用于随意产生PID
	 * @param event
	 */
	@FXML public void randomBtnAction(ActionEvent event){


		int num = new Random().nextInt(100);
		PIDTextfield.setText("bed" + num);

	}

	@FXML public void connectBtnAction(ActionEvent event){
		String PID = PIDTextfield.getText().trim();
		String host = hostTextfield.getText().trim();
		int port = Integer.parseInt(portTextfield.getText().trim());

		//判断PID不为空
		if("".equals(PID)){
			setFeedbackText("PID cannot be empty !");
			return;
		}

		//通信对象
		switch (model) {
		case "client-package":
			comm = new PackageComm(host, port, PID, "");
			new Thread(comm).start();
			logger.info("连接package服务器中..");
			break;
		case "client-websocket":
			comm = new WebsocketComm(host, port, PID, "");
			new Thread(comm).start();
			logger.info("连接websocket服务器中..");
			break;
		case "server":
			//需要开启服务端线程
			new Thread() {
				public void run() {
					try {
						ss = new ServerSocket(port);
						while (true) {
							// 此行代码会阻塞，将一直等待别人的连接
							Socket s = ss.accept();
							// 每当客户端连接后启动一条ServerThread线程为该客户端服务
							comm = new ServerComm(s, PID, "");
							new Thread(comm).start();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}.start();
			setFeedbackText("开启虚拟设备服务器..");
			logger.info("开启虚拟设备服务器..");
			break;
			
		default:
			logger.error("通信方式错误！");
			return;
		}
		
	}

	@FXML public void disconnectBtnAction(ActionEvent event){
		if (ss != null ) {
			try {
				ss.close();
				ss = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.info("服务器关闭");
			}

		}
	}

	/**
	 * 发送姿态按钮
	 */
	@FXML public void sendBtnAction(ActionEvent event){
		getPostuerByUI();
		comm.sendPosture();
	}

	/**
	 * 最小化窗口
	 * @param event
	 */
	@FXML public void minBtnAction(ActionEvent event){
		getLocalStage().setIconified(true);

	}
	/**
	 * 关闭窗口，关闭程序
	 * @param event
	 */
	@FXML public void closeBtnAction(ActionEvent event){
		if (ss != null) {
			
			try {
				ss.close();
				ss = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.info("服务器关闭");
			}
		}
		//退出系统
		Platform.exit();
        System.exit(0);

	}


	/**
	 * 调整姿态
	 */
	public void adjustPosture(String pos, String angle) {
		setFeedbackText("调整姿态中..");
		if (getBedState().equals(Utils.BED_STATE_DONE)) {
			if (pos.equals(Utils.POSTURE_RESET)) {
				//设置复位UI姿态
				setUIPostureByReset();
			}else {
				//设置UI姿态
				setUIPosture(pos, angle);
			}
			setFeedbackText("调整姿态成功.");
			comm.sendDone();
			return;
		}
		if (getBedState().equals(Utils.BED_STATE_UNDONE)) {
			setFeedbackText("调整姿态失败!");
			comm.sendUndone();
			return;
		}
	}

	/**
	 * 调整姿态
	 */
	public void adjustPosture(int pos, int angle) {
		setFeedbackText("调整姿态中..");
		if (getBedState().equals(Utils.BED_STATE_DONE)) {
			if (pos == 0) {
				//设置复位UI姿态
				setUIPostureByReset();
			}else {
				//设置UI姿态
				setUIPosture(pos, angle);
			}
			setFeedbackText("调整姿态成功.");
			comm.sendDone();
			return;
		}
		if (getBedState().equals(Utils.BED_STATE_UNDONE)) {
			setFeedbackText("调整姿态失败!");
			comm.sendUndone();
			return;
		}
	}

	/**
	 * 设置UI姿态
	 */
	public void setUIPosture(String pos, String value) {
		if (pos.equals(Utils.POSTURE_HEAD)) {
			headTextfield.setText(value);
		}else if (pos.equals(Utils.POSTURE_LEG)) {
			legTextfield.setText(value);
		}else if (pos.equals(Utils.POSTURE_LEFT)) {
			leftTextfield.setText(value);
		}else if (pos.equals(Utils.POSTURE_RIGHT)) {
			rightTextfield.setText(value);
		}else if (pos.equals(Utils.POSTURE_LIFT)) {
			liftTextfield.setText(value);
		}else if (pos.equals(Utils.POSTURE_BEFORE)) {
			liftTextfield.setText(value);
		}else if (pos.equals(Utils.POSTURE_AFTER)) {
			liftTextfield.setText(value);
		}else {
			logger.error("tag of control_posture instruction is error！");
		}
		getPostuerByUI();
	}

	/**
	 * 设置UI姿态
	 */
	public void setUIPosture(int pos, int value) {
		if (pos == 1) {
			headTextfield.setText(String.valueOf(value));
		}else if (pos == 2) {
			legTextfield.setText(String.valueOf(value));
		}else if (pos == 3) {
			leftTextfield.setText(String.valueOf(value));
		}else if (pos == 4) {
			rightTextfield.setText(String.valueOf(value));
		}else if (pos == 5) {
			liftTextfield.setText(String.valueOf(value));
		}else if (pos == 6) {
			beforeTextfield.setText(String.valueOf(value));
		}else if (pos == 7) {
			afterTextfield.setText(String.valueOf(value));
		}else {
			logger.error("tag of control_posture instruction is error！");
		}
		getPostuerByUI();
	}

	/**
	 * 设置复位UI姿态
	 */
	public void setUIPostureByReset() {
		headTextfield.setText("0");
		legTextfield.setText("0");
		leftTextfield.setText("0");
		rightTextfield.setText("0");
		liftTextfield.setText("0");
		beforeTextfield.setText("0");
		afterTextfield.setText("0");
		getPostuerByUI();
	}

	/**
	 * 从UI中得到姿态
	 */
	public void getPostuerByUI() {
		String head = headTextfield.getText().trim();
		String leg = legTextfield.getText().trim();
		String left = leftTextfield.getText().trim();
		String right = rightTextfield.getText().trim();
		String lift = liftTextfield.getText().trim();
		String before = beforeTextfield.getText().trim();
		String after = afterTextfield.getText().trim();
		if("".equals(head)){
			setFeedbackText("head posture cannot be empty !");
			return;
		}
		if("".equals(leg)){
			setFeedbackText("leg posture cannot be empty !");
			return;
		}
		if("".equals(left)){
			setFeedbackText("left posture cannot be empty !");
			return;
		}
		if("".equals(right)){
			setFeedbackText("right posture cannot be empty !");
			return;
		}
		if("".equals(lift)){
			setFeedbackText("lift posture cannot be empty !");
			return;
		}
		if("".equals(before)){
			setFeedbackText("before posture cannot be empty !");
			return;
		}
		if("".equals(after)){
			setFeedbackText("after posture cannot be empty !");
			return;
		}

		Posture.getInstance().setPosture(head, leg, left, right, lift, before, after);
	}

	/**
	 * 在LoginUI中显示登陆的结果信息
	 * @param result
	 */
	public void setFeedbackText(String result){
		//SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd :hh:mm:ss");
		SimpleDateFormat dateFormat= new SimpleDateFormat("hh:mm:ss");
		String time = dateFormat.format(new Date());

		feedbackTextarea.appendText(time + ": " + result + "\n");

	}

}
