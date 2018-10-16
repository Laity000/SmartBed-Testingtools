package com.virtualbed.communication;

public interface Communication extends Runnable{

	public void sendDisconnect();

	public void sendPID();

	public void sendPosture();

	public void sendDone();

	public void sendUndone();

	public boolean isConnected();
}
