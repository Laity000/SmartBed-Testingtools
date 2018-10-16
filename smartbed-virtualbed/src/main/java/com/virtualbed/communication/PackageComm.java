package com.virtualbed.communication;


import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.virtualbed.client.BedController;
import com.virtualbed.messages.Posture;
import com.virtualbed.messages.Utils;


public class PackageComm implements Communication {
	private static final Logger logger = LoggerFactory.getLogger(PackageComm.class);

	// UI控制器对象
	private BedController bedController = BedController.getInstance();

	// 套接字
	private Socket s;
	// 该线程所处理的Socket所对应的输入流
	private InputStream is = null;
	// 该线程所处理的Socket所对应的输出流
	private OutputStream os = null;

	// PID
	private String PID;
	// 密码
	private String password;
	// 主机地址
	private String host;
	// 端口号
	private int port;

	/**
	 * 构造函数
	 *
	 * @param context
	 * @param handler
	 * @param iPString
	 *            IP地址
	 * @param portString
	 *            端口号
	 */
	public PackageComm(String host, int port, String PID, String password) {

		this.host = host;
		this.port = port;
		this.PID = PID;
		this.password = password;

	}

	/**
	 * 创建线程
	 */
	public void run() {
		// TODO Auto-generated method stub
		try {
			// Thread.sleep(1000);//线程暂停一秒
			s = new Socket();
			SocketAddress endpoint = new InetSocketAddress(host, port);
			// 设置连接超时时间
			s.connect(endpoint, 5 * 1000);
			is = s.getInputStream();
			os = s.getOutputStream();

			// 输出连接信息
			if (s.isConnected()) {
				logger.info("{} 本机已成功连接服务器！下一步PID登录..", s);

			}
			// 连接指令
			sendPID();

			//接收数据包
			while (s.isConnected()) {
				//包头
				int from = is.read();
				if (from != 0x5d) {
					logger.error("数据包头错误：[{}]", from);
					break;
				}
				//包长(type+content)
				int length = is.read();
				byte[] buffer = new byte[length];
				int bufferLength = is.read(buffer);
				//包校验
				int sum = is.read();
				if (bufferLength > 0) {
					logger.info("收到服务器消息..");
					logger.debug("数据包内容(type+content):{}", buffer);
					//logger.info("对数据进行解析中..");
					// TODO:对的数据包类型进行解析
					switch (buffer[0]) {
					case Utils.SERVER_FEEDBACK_SUCCESS:
						//连接成功
						bedController.setFeedbackText("[" + PID + "]连接登录成功！");
						logger.info("[{}]设备端登录成功！", PID);
						break;
					case Utils.SERVER_FEEDBACK_FAIL:
						//连接失败
						bedController.setFeedbackText("[" + PID + "]连接登录失败(重名)！");
						logger.info("[{}]设备端登录失败！", PID);
						break;
					case Utils.QUERY_POSTURE:
						logger.info("收到姿态请求指令..");
						bedController.setFeedbackText("收到姿态请求指令..");
						sendPosture();
						break;
					case Utils.QUERY_PID:
						logger.info("收到设备PID请求指令..");
						bedController.setFeedbackText("收到设备PID请求指令..");
						sendPID();
						break;
					case Utils.CONTROL_POSTURE:
						logger.info("收到姿态控制指令..");
						bedController.setFeedbackText("收到姿态控制指令..");
						bedController.adjustPosture(buffer[1], buffer[2]);
						break;
					default:
						break;
					}
				}
			}
		} catch (SocketTimeoutException ex) {
			ex.printStackTrace();
			logger.info("{} 连接超时！", s);
			bedController.setFeedbackText(s + "连接超时！");
		} catch (Exception e) {
			e.printStackTrace();

			logger.info("{} 连接错误！", s);
			bedController.setFeedbackText(s + "连接错误！");
		}
	}

	/**
	 * 设备PID类型消息
	 */
	public void sendPID() {

		//二进制pid
		byte[] pid = null;
		try {
			pid = PID.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (pid.length != 6) {
			byte[] newPID = new byte[6];
			if (pid.length < 6) {
				for (int i = 0; i < pid.length; i++) {
					newPID[i] = pid[i];
				}

			}else {
				for (int i = 0; i < newPID.length; i++) {
					newPID[i] = pid[i];
				}
			}
			pid = newPID;
		}

		// 创新连接消息
		send(7, Utils.CONNECT, pid);
		logger.info("向服务器发送设备连接消息..");

	}

	@Override
	public void sendDisconnect() {
		send(1, Utils.DISCONNECT, null);
		bedController.setFeedbackText("[" + PID + "]断开连接！");
		logger.info("[{}]设备端断开连接！", PID);
		//断开连接
		destroy();
	}

	@Override
	public void sendPosture() {
		Posture posture = Posture.getInstance();
		byte[] content = new byte[7];
		content[0] = Byte.parseByte(posture.getHead());
		content[1] = Byte.parseByte(posture.getLeg());
		content[2] = Byte.parseByte(posture.getLeft());
		content[3] = Byte.parseByte(posture.getRight());
		content[4] = Byte.parseByte(posture.getLift());
		content[5] = Byte.parseByte(posture.getBefore());
		content[6] = Byte.parseByte(posture.getAfter());
		//发送姿态
		send(8, Utils.POSTURE, content);

	}

	@Override
	public void sendDone() {

		Posture posture = Posture.getInstance();
		byte[] content = new byte[7];
		content[0] = Byte.parseByte(posture.getHead());
		content[1] = Byte.parseByte(posture.getLeg());
		content[2] = Byte.parseByte(posture.getLeft());
		content[3] = Byte.parseByte(posture.getRight());
		content[4] = Byte.parseByte(posture.getLift());
		content[5] = Byte.parseByte(posture.getBefore());
		content[6] = Byte.parseByte(posture.getAfter());

		//模拟设备工作
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//发送完成姿态
		send(8, Utils.DONE, content);

	}

	@Override
	public void sendUndone() {
		send(1, Utils.UNDONE, null);

	}

	@Override
	public boolean isConnected() {
		if (is != null && os != null) {
			return true;
		}else {
			return false;
		}
	}

	/**
	 * 编码成数据包，并转化为字符串后发送
	 * 基于自定义协议
	 *
	 * @param length
	 * @param type
	 * @param content
	 */
	private void send(int length, byte type, byte[] content) {
		//数据包
		byte[] data = new byte[length + 3];
		//包校验
		byte sum = 0;
		//包头
		data[0] = (byte) 0x5d;
		//包长
		data[1] = (byte)length;
		sum += (byte)length;
		//包类型
		data[2] = type;
		sum += type;
		//包内容
		if (content != null) {
			for (int i = 0,j = 3; i < content.length; i++,j++) {
				data[j] = content[i];
				sum += content[i];
			}
		}
		//包校验
		data[data.length-1] = sum;

		logger.debug("发送的消息内容：{}", data);
		try {
			os.write(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * 断开物理连接
	 */
	private void destroy() {
		logger.info("正在断开物理连接..");
		new Thread() {
			@Override
			public void run() {
				try {
					if (is != null) {
						is.close();
						is = null;
					}
					if (os != null) {
						os.close();
						os = null;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
		logger.info("物理连接断开成功！");
	}


}
