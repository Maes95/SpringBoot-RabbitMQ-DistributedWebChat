package com.chat;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;

import java.io.IOException;

public abstract class ChatConsumer extends DefaultConsumer{

    public ChatConsumer(Channel channel){
        super(channel);
    }

    @Override
    public abstract void handleDelivery(String consumerTag, Envelope envelope,
                               AMQP.BasicProperties properties, byte[] body)
            throws IOException;
}
