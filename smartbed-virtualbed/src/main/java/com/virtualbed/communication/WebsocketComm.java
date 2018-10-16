package com.virtualbed.communication;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.WebSocket.READYSTATE;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.virtualbed.client.BedController;
import com.virtualbed.messages.Message;
import com.virtualbed.messages.MessageType;
import com.virtualbed.messages.Posture;
import com.virtualbed.messages.Utils;

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
	private BedController bedController = BedController.getInstance();

	// PID
	private String PID;
	// 密码
	private String password;


	/**
	 * 构造函数
	 *
	 * @param host
	 * @param port
	 * @param PID
	 * @param password
	 */
	public WebsocketComm(String host, int port, String PID, String password) {
		this.PID = PID;
		this.password = password;
		url = "ws://" + host + ":" + port;
	}

	public String getPID() {
		return PID;
	}

	public String getPassword() {
		return password;
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
							if (message.hasContentByTag(Utils.SERVER_FEEDBACK_RECEIVED)) {
								bedController.setFeedbackText("[" + PID + "]连接登录成功！");
								logger.info("[{}]设备端登录成功！", PID);
							}else if (message.hasContentByTag(Utils.SERVER_FEEDBACK_REJECTED)) {
								bedController.setFeedbackText("[" + PID + "]连接登录失败(重名)！");
								logger.info("[{}]设备端登录失败(重名)！", PID);
								//destroy();
							}else {
								logger.error("服务器反馈指令异常！");
							}
							break;
						case QUERY_POSTURE:
							logger.info("收到姿态请求指令..");
							bedController.setFeedbackText("收到姿态请求指令..");
							sendPosture();
							break;
						case QUERY_PID:
							logger.info("收到设备PID请求指令..");
							bedController.setFeedbackText("收到设备PID请求指令..");
							sendPID();
							break;
						case CONTROL_POSTURE:
							logger.info("收到姿态控制指令..");
							bedController.setFeedbackText("收到姿态控制指令..");
							bedController.adjustPosture(
									message.getTagForSingleContent(),
									message.getFieldForSingleContent());
							break;
						case NOTIFICATION:
							logger.info("收到通知消息");
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
					bedController.setFeedbackText(getURI() + "连接错误！");
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
					logger.info("设备端连接中..");
					bedController.setFeedbackText("设备端连接中..");
					sendPID();
					break;
				} else {
					Thread.sleep(1000);
					logger.info("设备端第{}次尝试连接失败！", i);
					bedController.setFeedbackText("第" + i + "次尝试连接失败！");
				}
			}

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("{} 连接错误！{} 这不是有效的url！", wsc.getURI(), url);
			bedController.setFeedbackText(url + "这不是有效的url！");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("{} 连接错误！", wsc.getURI());
			bedController.setFeedbackText(wsc.getURI() + "连接错误！");
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


	@Override
	/**
	 * 设备PID类型消息
	 */
	public void sendPID() {
		// 创建个人信息

		// 创新连接消息
		Message message = new Message(1);
		message.setType(MessageType.CONNECT);
		message.setFrom(Utils.SIGN);
		message.addContent("PID", PID);
		// 发送
		send(message);

		logger.info("向服务器发送设备连接消息..");

	}

	@Override
	/**
	 * 设备注销请求类型消息
	 */
	public void sendDisconnect() {
		Message message = new Message(0);
		message.setType(MessageType.DISCONNECT);
		message.setFrom(Utils.SIGN);
		// 发送
		send(message);

		logger.info("向服务器发送设备注销消息..");
		//断开连接
		destroy();
	}

	@Override
	/**
	 * 发送设备姿态
	 */
	public void sendPosture() {
		Posture posture = Posture.getInstance();
		Message message = new Message(5);
		message.setType(MessageType.POSTURE);
		message.setFrom(Utils.SIGN);
		message.addContent(Utils.POSTURE_HEAD, posture.getHead());
		message.addContent(Utils.POSTURE_LEG, posture.getLeg());
		message.addContent(Utils.POSTURE_LEFT, posture.getLeft());
		message.addContent(Utils.POSTURE_RIGHT, posture.getRight());
		message.addContent(Utils.POSTURE_LIFT, posture.getLift());
		message.addContent(Utils.POSTURE_BEFORE, posture.getBefore());
		message.addContent(Utils.POSTURE_AFTER, posture.getAfter());
		//发送
		send(message);
		logger.info("向服务器发送设备姿态信息..");
		bedController.setFeedbackText("向用户发送姿态信息..");
	}

	@Override
	/**
	 * 设备工作完成反馈
	 */
	public void sendDone() {
		Posture posture = Posture.getInstance();
		Message message = new Message(5);
		message.setType(MessageType.DONE);
		message.setFrom(Utils.SIGN);
		message.addContent(Utils.POSTURE_HEAD, posture.getHead());
		message.addContent(Utils.POSTURE_LEG, posture.getLeg());
		message.addContent(Utils.POSTURE_LEFT, posture.getLeft());
		message.addContent(Utils.POSTURE_RIGHT, posture.getRight());
		message.addContent(Utils.POSTURE_LIFT, posture.getLift());
		message.addContent(Utils.POSTURE_BEFORE, posture.getBefore());
		message.addContent(Utils.POSTURE_AFTER, posture.getAfter());
		//发送
		send(message);
		logger.info("向服务器发送设备工作完成信息..");
		bedController.setFeedbackText("向服务器发送设备工作完成信息..");
	}

	@Override
	/**
	 * 设备工作未完成反馈
	 */
	public void sendUndone() {
		Message message = new Message(0);
		message.setType(MessageType.UNDONE);
		message.setFrom(Utils.SIGN);
		//发送
		send(message);
		logger.info("向服务器发送设备未完成信息..");
		bedController.setFeedbackText("向服务器发送设备未完成信息..");
	}

	/**
	 * 将message转化为字符串后发送 基于websocket协议
	 */
	private void send(Message message) {
		// TODO Auto-generated method stub
		logger.debug("");
		// 转化为gson的字符串
		String messagesString = gson.toJson(message);
		logger.debug("发送的消息内容：{}", messagesString);

		// 发送消息
		wsc.send(messagesString);
	}

	private void destroy() {
		wsc.close();
		logger.info("物理连接断开成功！");
	}

}
