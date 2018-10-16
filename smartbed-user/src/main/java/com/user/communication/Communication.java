package com.user.communication;

public interface Communication extends Runnable{

	public void sendBind(String PID);
	
	public void sendUnbind();

	public void controlPosture(String pos, String angle);

	public void queryPosture();

	public boolean isConnected();

	public void destroy();
}
