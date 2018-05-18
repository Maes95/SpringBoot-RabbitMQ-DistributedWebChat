package com.chat;

import com.rabbitmq.client.*;

import java.io.IOException;

import javax.websocket.Session;

public class User {

	private static String EXCHANGE_NAME = "MANAGER";

	private String name;
	private String chat;
	private final Session session;
	private DefaultConsumer consumer;
	private Channel channel;
	
	public User(Session session) {
		this.session = session;
	}
	
	public void setUp(String name, String chat) throws Exception{

			this.name = name;
			this.chat = chat;
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost("localhost");
			Connection connection = factory.newConnection();
			channel = connection.createChannel();
			channel.exchangeDeclare(EXCHANGE_NAME, "topic");
			String queueName = channel.queueDeclare().getQueue();
			channel.queueBind(queueName, EXCHANGE_NAME, chat);

			this.consumer = new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope,
										   AMQP.BasicProperties properties, byte[] body)
						throws IOException {
					String message = new String(body, "UTF-8");
					System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + message + "'" + " -> MY CHAT: "+chat);
					send(message);
				}
			};
			channel.basicConsume(queueName, true, consumer);

	}

	public synchronized void broadcast(String message){
            try {
					this.channel.basicPublish("MANAGER", chat, null, message.getBytes());
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
			this.channel.close();
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
	
	public boolean isValid(){
		return this.name != null && this.chat != null;
	}


}
