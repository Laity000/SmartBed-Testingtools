package com.virtualbed.messages;


public class Utils {

	public static final String SIGN= "BED";

	public static final String POSTURE_RESET = "reset";
	public static final String POSTURE_HEAD = "head";
	public static final String POSTURE_LEG = "leg";
	public static final String POSTURE_LEFT = "left";
	public static final String POSTURE_RIGHT = "right";
	public static final String POSTURE_LIFT = "lift";
	public static final String POSTURE_BEFORE = "before";
	public static final String POSTURE_AFTER = "after";

	public static final String BED_STATE_DONE = "done";
	public static final String BED_STATE_UNDONE = "undone";

	public static final String SERVER_FEEDBACK_RECEIVED = "received";
	public static final String SERVER_FEEDBACK_REJECTED = "rejected";

	//发送类型消息
	public static final byte CONNECT = 0x01;
	public static final byte DISCONNECT = 0x02;
	public static final byte POSTURE = 0x03;
	public static final byte DONE = 0x04;
	public static final byte UNDONE = 0x05;
	//接收类型消息
	public static final byte QUERY_POSTURE = 0x11;
	public static final byte QUERY_PID = 0x12;
	public static final byte CONTROL_POSTURE = 0x13;
	public static final byte SERVER_FEEDBACK_SUCCESS = 0x14;
	public static final byte SERVER_FEEDBACK_FAIL = 0x15;

}
