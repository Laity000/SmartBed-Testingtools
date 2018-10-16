package com.virtualbed.messages;

//消息类型
public enum MessageType {
	//发送类型消息
	CONNECT,DISCONNECT,POSTURE,DONE,UNDONE,
	//接收类型消息
	QUERY_POSTURE,QUERY_PID,SERVER_FEEDBACK,CONTROL_POSTURE,NOTIFICATION
}
