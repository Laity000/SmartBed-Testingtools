package com.user.messages;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class Message {
	private static final Logger logger = LoggerFactory.getLogger(Message.class);
	//消息类型
	private MessageType type;
	//消息来源
	private String from;
	//消息内容
	private HashMap<String, String> content;

	public Message() {
		content = new HashMap<>(0);
	}

	public Message(int num) {
		content = new HashMap<>(num);
	}

	public MessageType getType() {
		return type;
	}
	public void setType(MessageType type) {
		this.type = type;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public HashMap<String, String> getContent() {
		return content;
	}

	public void setContent(HashMap<String, String> content) {
		this.content = content;
	}

	public void addContent(String tag, String field) {
		content.put(tag, field);
	}

	public int numContent() {
		return content.size();
	}

	public boolean hasContentByTag(String tag) {
		return content.containsKey(tag);
	}

	public String getContentByTag(String tag) {
		if (!hasContentByTag(tag)) {
			logger.error("content tag is non-existent in message!");
		}
		return content.get(tag);

	}

	public String getTagForSingleContent() {
		if (content.size() != 1) {
			logger.error("content of instruction is not single content！");
		}
		return content.keySet().iterator().next();
	}

	public String getFieldForSingleContent() {
		if (content.size() != 1) {
			logger.error("content of instruction is not single content！");
		}
		return content.values().iterator().next();
	}

}
