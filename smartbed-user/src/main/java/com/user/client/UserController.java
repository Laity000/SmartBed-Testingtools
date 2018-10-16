package com.user.client;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.user.communication.Communication;
import com.user.communication.PackageComm;
import com.user.communication.WebsocketComm;
import com.user.messages.Utils;
import com.user.stage.ControlledStage;

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
public class UserController extends ControlledStage implements  Initializable{
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	//Stage管理器
	//private StageController myController;

	//loginController对象
	private static UserController instance;

	//通信对象
	private Communication comm;
	//协议
	private String protocol = "websocket";


	@FXML private BorderPane borderPane;

    @FXML private TextArea feedbackTextarea;
	@FXML private TextField hostTextfield;
    @FXML private TextField portTextfield;
    @FXML private TextField PIDTextfield;
    @FXML private TextField passwordTextfield;
    @FXML private TextField angleTextfield;


    @FXML private ChoiceBox<String> postureChoiceBox;
    @FXML private ChoiceBox<String> protocolChoiceBox;

    private double xOffset;
    private double yOffset;

    //姿态位置
	private String pos = Utils.POSTURE_RESET;

    //注意这里不是单例的形式，UI的Controller相当于回调函数的管理集合。
    //不能用private单例调用，应该由系统调用。这里是为了获得LoginController对象。
    public UserController() {
		// TODO Auto-generated constructor stub
    	instance = this;
	}
    /**
     * 获得UI的Controller对象
     * @return
     */
    public static UserController getInstance(){
    	return instance;
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
		// 姿态选择
		postureChoiceBox.getSelectionModel().selectedItemProperty()
				.addListener((ObservableValue<? extends String> ov, String old_val, String new_val) -> {
					pos = new_val;
				});
		// 协议选择
		protocolChoiceBox.getSelectionModel().selectedItemProperty().addListener(
        		(ObservableValue<? extends String> ov, String old_val, String new_val)->
        		{
        			protocol = new_val;
        			switch (protocol) {
        			case "package":
        				portTextfield.setText("8283");
        				break;
        			case "websocket":
        				portTextfield.setText("8282");
        				break;
        			}
        		});

	}

	/**
	 * 绑定设备PID按钮
	 */
	@FXML public void bindPIDBtnAction(ActionEvent event){
		if (comm == null) {
			setFeedbackText("请先连接!");
			return;
		}
		String PID = PIDTextfield.getText().trim();
		if ("".equals(PID)) {
			setFeedbackText("PID不能为空!");
			return;
		}
		comm.sendBind(PID);
	}

	/**
	 * 解除绑定设备PID按钮
	 */
	@FXML public void unbindBtnAction(ActionEvent event){
		comm.sendUnbind();
	}

	/**
	 * 连接按钮
	 */
	@FXML public void connectBtnAction(ActionEvent event){

		String host = hostTextfield.getText().trim();
		int port = Integer.parseInt(portTextfield.getText().trim());

		switch (protocol) {
			case "websocket":
				comm = new WebsocketComm(host, port);
			break;
			case "package":
				comm = new PackageComm(host, port);
			break;
		}
		
		new Thread(comm).start();
		logger.info("连接服务器中..");
	}
	
	@FXML public void disconnectBtnAction(ActionEvent event){
		if (comm != null && comm.isConnected()) {
			//首先需要向服务器注销用户
			comm.destroy();
		}
	}

	/**
	 * 发送控制姿态按钮
	 */
	@FXML public void sendBtnAction(ActionEvent event){
		String angle = angleTextfield.getText().trim();
		if (!pos.equals(Utils.POSTURE_RESET) && "".equals(angle)) {
			setFeedbackText("posture angle cannot be empty !");
			return;
		}
		comm.controlPosture(pos, angle);
	}

	/**
	 * 查询姿态按钮
	 */
	@FXML public void queryBtnAction(ActionEvent event){
		comm.queryPosture();
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
		if (comm != null && comm.isConnected()) {
			//首先需要向服务器注销用户
			comm.destroy();
		}
		//退出系统
		Platform.exit();
        System.exit(0);

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
