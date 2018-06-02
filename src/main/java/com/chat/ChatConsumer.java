package com.chat;

import com.rabbitmq.client.*;
import com.rabbitmq.client.AMQP.BasicProperties;

import java.io.IOException;

public abstract class ChatConsumer implements Consumer, Publisher{

    protected Channel channel;
    protected Connection connection;

    public void handleConsumeOk(String consumerTag) {}

    public void handleCancelOk(String consumerTag) {}

    public void handleCancel(String consumerTag) throws IOException {}

    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {}

    public void handleRecoverOk(String consumerTag) {}

    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException{
        this.handleQueueMessage(new String(body, "UTF-8"));
    }

    public abstract void handleQueueMessage(String message);

}