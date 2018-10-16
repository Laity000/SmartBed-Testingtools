package com.user.messages;

//消息类型
public enum MessageType {
	//发送类型消息
	//CONNECT,DISCONNECT,
	BIND,UNBIND,CONTROL,CONTROL_POSTURE,QUERY_POSTURE,QUERY_RECORD,
	//接收类型消息
	POSTURE,RECORD,DONE,UNDONE,SERVER_FEEDBACK,NOTIFICATION
}
