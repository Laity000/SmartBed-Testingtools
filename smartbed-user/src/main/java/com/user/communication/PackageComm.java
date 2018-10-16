package com.user.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.user.client.UserController;
import com.user.messages.Utils;

public class PackageComm implements Communication {
	
	private static final Logger logger = LoggerFactory.getLogger(PackageComm.class);

	// UI控制器对象
	private UserController userController = UserController.getInstance();

	// 主机地址
	private String host;
	// 端口号
	private int port;

	// 套接字
	private Socket s;
	// 该线程所处理的Socket所对应的输入流
	private InputStream is = null;
	// 该线程所处理的Socket所对应的输出流
	private OutputStream os = null;

	/**
	 * 构造函数
	 *
	 * @param host
	 * @param port
	 */
	public PackageComm(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public void run() {
		try {

			s = new Socket();
			SocketAddress endpoint = new InetSocketAddress(host, port);
			// 设置连接超时时间
			s.connect(endpoint, 5 * 1000);
			is = s.getInputStream();
			os = s.getOutputStream();

			// 输出连接信息
			if (s.isConnected()) {
				userController.setFeedbackText("本机已成功连接设备！" + s);
				logger.info("本机已成功连接设备！{}", s);
			}

			// 接收数据包
			while (s.isConnected()) {
				// 包头
				int from = is.read();
				if (from != 0x5d) {
					logger.error("数据包头错误：[{}]", from);
					break;
				}
				// 包长(type+content)
				int length = is.read();
				byte[] buffer = new byte[length];
				int bufferLength = is.read(buffer);
				// 包校验
				int sum = is.read();
				if (bufferLength > 0) {
					logger.info("收到设备端消息..");
					logger.debug("数据包内容(type+content):{}", buffer);
					//logger.info("对数据进行解析中..");
					// TODO:对的数据包类型进行解析
					switch (buffer[0]) {
						//ping
						case 0x00:
							logger.debug("心跳..");
						break;
						//PID(CONNECT)
						case 0x01:
							logger.info("设备PID");
							userController.setFeedbackText("设备PID：" + Arrays.toString(buffer));
						break;
						//posture
						case 0x03:
							logger.info("姿态消息");
							userController.setFeedbackText("姿态信息：" + Arrays.toString(buffer));
						break;
						//done
						case 0x04:
							logger.info("姿态调整完成！");
							userController.setFeedbackText("姿态调整完成：" + Arrays.toString(buffer));
						break;
						//undone
						case 0x05:
							logger.info("姿态调整失败!");
							userController.setFeedbackText("姿态调整失败!");
						break;
						default:
						break;
					}
				}
			}
		} catch (SocketTimeoutException ex) {
			ex.printStackTrace();
			logger.info("{} 连接超时！", s);
			userController.setFeedbackText(s + "连接超时！");
		} catch (Exception e) {
			e.printStackTrace();

			logger.info("{} 连接错误！", s);
			userController.setFeedbackText(s + "连接错误！");
		}
	}

	@Override
	public void sendBind(String PID) {
		userController.setFeedbackText("无绑定设备功能！");

	}

	@Override
	public void sendUnbind() {
		userController.setFeedbackText("无解绑设备功能！");

	}

	@Override
	public void controlPosture(String pos, String angle) {
		byte[] content = new byte[2];
		
		//姿态位置
		if (pos.equals(Utils.POSTURE_HEAD)) {
			content[0] = 0x1;
		}else if (pos.equals(Utils.POSTURE_LEG)) {
			content[0] = 0x2;
		}else if (pos.equals(Utils.POSTURE_LEFT)) {
			content[0] = 0x3;
		}else if (pos.equals(Utils.POSTURE_RIGHT)) {
			content[0] = 0x4;
		}else if (pos.equals(Utils.POSTURE_LIFT)) {
			content[0] = 0x5;
		}else if (pos.equals(Utils.POSTURE_BEFORE)) {
			content[0] = 0x6;
		}else if (pos.equals(Utils.POSTURE_AFTER)) {
			content[0] = 0x7;
		}else {
			logger.error("tag of control_posture instruction is error！");
		}
		
		//姿态角度
		content[1] = (byte) (0xff & Integer.valueOf(angle));
		
		//发送姿态控制指令
		send(3, (byte)0x13, content);
		
		logger.info("向设备直接发送姿态控制指令..");
		userController.setFeedbackText("向设备直接发送姿态控制指令..");
	}

	@Override
	public void queryPosture() {
		//发送姿态查询
		send(1, (byte)0x11, null);

	}

	@Override
	public boolean isConnected() {
		if (s.isClosed()) {
			return false;
		}else {
			return true;
		}
	}

	/**
	 * 断开物理连接
	 */
	@Override
	public void destroy() {
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

	/**
	 * 编码成数据包，并转化为字符串后发送 基于自定义协议
	 *
	 * @param length
	 * @param type
	 * @param content
	 */
	private void send(int length, byte type, byte[] content) {
		// 数据包
		byte[] data = new byte[length + 3];
		// 包校验
		byte sum = 0;
		// 包头
		data[0] = (byte) 0x5d;
		// 包长
		data[1] = (byte) length;
		sum += (byte) length;
		// 包类型
		data[2] = type;
		sum += type;
		// 包内容
		if (content != null) {
			for (int i = 0, j = 3; i < content.length; i++, j++) {
				data[j] = content[i];
				sum += content[i];
			}
		}
		// 包校验
		data[data.length - 1] = sum;

		logger.debug("发送的消息内容：{}", data);
		try {
			os.write(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
