package com.user.messages;


public class Utils {

	public static final String SIGN= "USER";


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

	/**
	 * 检测
	 */
	public static final String SUCCESS_COMM_CODE = "000";
	public static final String SUCCESS_COMM_TEXT = "Communication successful.";

	public static final String FAIL_ILLEGAL_INSTRUTIONS_CODE = "001";
	public static final String FAIL_ILLEGAL_INSTRUIONS_TEXT = "illegal instructions!";

	public static final String FAIL_UNBOUND_CODE = "002";
	public static final String FAIL_UNBOUND_TEXT = "PID is unbound!";

	public static final String FAIL_OFFLINE_CODE = "003";
	public static final String FAIL_OFFLINE_TEXT = "bed is offline!";
	/**
	 * 绑定
	 */
	public static final String SUCCESS_BIND_CODE = "010";
	public static final String SUCCESS_BIND_TEXT = "bind successful.";

	public static final String FAIL_BIND_PIDNULL_CODE = "011";
	public static final String FAIL_BIND_PIDNULL_TEXT = "bind failed! PID is null.";

	public static final String FAIL_BIND_OFFLINE_CODE = "012";
	public static final String FAIL_BIND_OFFLINE_TEXT = "bind failed! Bed is offline.";

	/**
	 * 解除绑定
	 */
	public static final String SUCCESS_UNBIND_CODE = "020";
	public static final String SUCCESS_UNBIND_TEXT = "unbind successful.";

	public static final String FAIL_UNBIND_CODE = "021";
	public static final String FAIL_UNBIND_TEXT = "unbind fail!";

	/**
	 * 控制姿态
	 */
	public static final String SUCCESS_CONTROLPOSTURE_CODE = "030";
	public static final String SUCCESS_CONTROLPOSTURE_TEXT = "forwarding control_posture successful. ";


	public static final String FAIL_CONTROLPOSTURE_POS_CODE = "031";
	public static final String FAIL_CONTROLPOSTURE_POS_TEXT = "forwarding control_posture fail! pos is error.";

	public static final String FAIL_CONTROLPOSTURE_ANGLE_CODE = "032";
	public static final String FAIL_CONTROLPOSTURE_ANGLE_TEXT = "forwarding control_posture fail! angle is error.";

}
