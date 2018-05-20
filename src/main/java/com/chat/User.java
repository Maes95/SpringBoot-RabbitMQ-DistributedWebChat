package com.chat;

import com.rabbitmq.client.*;

import java.io.IOException;

import javax.websocket.Session;

public class User extends DefaultConsumer {

	private final String name;
	private final String chat;
	private final String exchange;
	private final Session session;

	public User(String name, String chat, Session session, Channel channel, String exchange) {
		super(channel);
		this.session = session;
        this.name = name;
        this.chat = chat;
        this.exchange = exchange;
	}

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
			throws IOException {
		String message = new String(body, "UTF-8");
		System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + message + "'" + " -> MY CHAT: "+chat);
		send(message);
	}

	public synchronized void broadcast(String message){
            try {
					this.getChannel().basicPublish(this.exchange, chat, null, message.getBytes());
					System.out.println(" [x] Sent '" + chat + "':'" + message + "'");
            } catch (IOException e) {
                    e.printStackTrace();
            }
	}

	public void send(String message){
		try {
			session.getBasicRemote().sendText(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void disconnect(){
		try {
			this.getChannel().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getChat(){
		return this.chat;
	}
	
	public String getName(){
		return this.name;
	}

}
