package com.user.communication;

import java.net.URI;
import java.net.URISyntaxException;


import org.java_websocket.WebSocket.READYSTATE;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.user.client.UserController;
import com.user.messages.Message;
import com.user.messages.MessageType;
import com.user.messages.Utils;

/**
 *
 * @Title: WebsocketComm.java
 * @Description: TODO 基于websocket协议的通信线程
 * @author ZhangJing https://github.com/Laity000/ChatRoom-JavaFX
 * @date 2017年6月15日 上午11:45:22
 *
 */
public class WebsocketComm implements Communication {

	private static final Logger logger = LoggerFactory.getLogger(WebsocketComm.class);

	// Java-WebSocket
	private WebSocketClient wsc;
	// url
	private String url;
	// Gson
	private Gson gson = new Gson();

	// UI控制器对象
	private UserController userController = UserController.getInstance();


	/**
	 * 构造函数
	 *
	 * @param host
	 * @param port
	 */
	public WebsocketComm(String host, int port) {

		url = "ws://" + host + ":" + port;
	}


	@Override
	/**
	 * 线程
	 */
	public void run() {
		// TODO Auto-generated method stub
		// websocket初始化
		try {
			wsc = new WebSocketClient(new URI(url)) {

				@Override
				public void onOpen(ServerHandshake handshake) {
					// TODO Auto-generated method stub
					logger.info("本设备已成功连接{}服务器！下一步登录PID..", getURI());

				}

				@Override
				public void onMessage(String revString) {
					// TODO Auto-generated method stub
					// 读取来自客户端的消息
					if (revString != null) {
						//logger.info("\n收到服务器消息..");
						logger.debug("");
						logger.debug("收到服务器消息内容:{}", revString);
						//logger.info("对数据进行解析并做响应中..");
						Message message = gson.fromJson(revString, Message.class);
						// TODO:对服务器的消息进行解析
						switch (message.getType()) {
						case SERVER_FEEDBACK:
							String result = "服务器反馈信息：" + message.getFieldForSingleContent() +
									" [" + message.getTagForSingleContent() + "]";
							userController.setFeedbackText(result);
							logger.info(result);
							break;
						case POSTURE:
							logger.info("收到姿态信息..");
							userController.setFeedbackText(message.getContent().toString());
							break;
						case DONE:
							String result1 = "设备已调整到指定姿态:" +
									message.getContent().toString();
							userController.setFeedbackText(result1);
							logger.info(result1);
							break;
						case UNDONE:
							userController.setFeedbackText("姿态调整失败！请检查.");
							logger.info("姿态调整失败！");
							break;
						case NOTIFICATION:
							logger.info("收到通知消息..");
							// 通知消息，目前只有新用户上线通知、用户下线通知
							break;
						default:
							break;
						}
					}
				}

				@Override
				public void onError(Exception ex) {
					// TODO Auto-generated method stub
					ex.printStackTrace();
					logger.info("{} 连接错误！", getURI());
					userController.setFeedbackText(getURI() + "连接错误！");
				}

				@Override
				public void onClose(int arg0, String arg1, boolean arg2) {
					// TODO Auto-generated method stub

				}
			};
			wsc.connect();
			Thread.sleep(500);
			for (int i = 1; i <= 5; i++) {
				if (wsc.getReadyState().equals(READYSTATE.OPEN)) {
					// 连接指令
					userController.setFeedbackText("用户端连接成功!");
					break;
				} else {
					Thread.sleep(1000);
					logger.info("用户第{}次尝试连接失败！", i);
					userController.setFeedbackText("第" + i + "次尝试连接失败！");
				}
			}

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("{} 连接错误！{} 这不是有效的url！", wsc.getURI(), url);
			userController.setFeedbackText(url + "这不是有效的url！");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("{} 连接错误！", wsc.getURI());
			userController.setFeedbackText(wsc.getURI() + "连接错误！");
		}

		// 登录请求

	}
	
	@Override
	public boolean isConnected(){
		if (wsc.getReadyState().equals(READYSTATE.OPEN)) {
			return true;
		}else {
			return false;
		}
	}


	/**
	 * 绑定设备PID
	 */
	@Override
	public void sendBind(String PID) {

		// 创新PID消息
		Message message = new Message(1);
		message.setType(MessageType.BIND);
		message.setFrom(Utils.SIGN);
		message.addContent("PID", PID);
		// 发送
		send(message);

		logger.info("用户向服务器发送PID用于绑定设备..");
		userController.setFeedbackText("用户向服务器发送PID用于绑定设备..");
	}

	/**
	 * 解除绑定设备
	 */
	@Override
	public void sendUnbind() {

		// 创新消息
		Message message = new Message(0);
		message.setType(MessageType.UNBIND);
		message.setFrom(Utils.SIGN);
		// 发送
		send(message);

		logger.info("用户向服务器发送解除绑定设备..");
		userController.setFeedbackText("用户向服务器发送解除绑定设备..");
	}

	/**
	 * 发送调整姿态信息
	 */
	@Override
	public void controlPosture(String pos, String angle) {
		Message message = new Message(1);
		message.setType(MessageType.CONTROL_POSTURE);
		message.setFrom(Utils.SIGN);
		message.addContent(pos, angle);
		//发送
		send(message);

		logger.info("向服务器发送姿态控制指令..");
		userController.setFeedbackText("向服务器发送姿态控制指令..");
	}

	/**
	 * 查询姿态
	 */
	@Override
	public void queryPosture() {
		// 创新消息
		Message message = new Message(0);
		message.setType(MessageType.QUERY_POSTURE);
		message.setFrom(Utils.SIGN);
		// 发送
		send(message);

		logger.info("向服务器查询设备姿态..");
		userController.setFeedbackText("向服务器查询设备姿态..");
	}
	
	@Override
	public void destroy() {
		wsc.close();
		logger.info("物理连接断开成功！");
	}

	/**
	 * 将message转化为字符串后发送 基于websocket协议
	 */
	void send(Message message) {
		// TODO Auto-generated method stub
		// 转化为gson的字符串
		logger.debug("");
		String messagesString = gson.toJson(message);
		logger.debug("发送的消息内容：{}", messagesString);
		// 发送消息
		wsc.send(messagesString);
	}
	
}
