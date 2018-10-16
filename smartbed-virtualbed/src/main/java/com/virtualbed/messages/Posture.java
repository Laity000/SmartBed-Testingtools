package com.virtualbed.messages;

public class Posture{

	private static final Posture single = new Posture("0", "0", "0", "0", "0", "0", "0");

	private String head;

	private String leg;

	private String left;

	private String right;

	private String lift;

	private String before;

	private String after;



    private Posture(String head, String leg, String left, String right, String lift, String before, String after) {

		this.head = head;
		this.leg = leg;
		this.left = left;
		this.right = right;
		this.lift = lift;
		this.before = before;
		this.after = after;
	}

	//饿汉型单例
    public static Posture getInstance() {
        return single;
    }

	public void setPosture(String head, String leg, String left, String right, String lift, String before, String after) {

		this.head = head;
		this.leg = leg;
		this.left = left;
		this.right = right;
		this.lift = lift;
		this.before = before;
		this.after = after;
	}

	public String getHead() {
		return head;
	}

	public void setHead(String head) {
		this.head = head;
	}

	public String getLeg() {
		return leg;
	}

	public void setLeg(String leg) {
		this.leg = leg;
	}

	public String getLeft() {
		return left;
	}

	public void setLeft(String left) {
		this.left = left;
	}

	public String getRight() {
		return right;
	}

	public void setRight(String right) {
		this.right = right;
	}

	public String getLift() {
		return lift;
	}

	public void setLift(String lift) {
		this.lift = lift;
	}

	public String getBefore() {
		return before;
	}

	public void setBefore(String before) {
		this.before = before;
	}

	public String getAfter() {
		return after;
	}

	public void setAfter(String after) {
		this.after = after;
	}



}
