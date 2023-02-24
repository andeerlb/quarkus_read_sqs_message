package com.nexti.timetracking.messagerouter.dtos;

import software.amazon.awssdk.services.sqs.model.Message;

public class MessageHandler {
    private final String queueName;
    private final Message message;

    public MessageHandler(String queueName, Message message) {
        this.queueName = queueName;
        this.message = message;
    }

    public String getQueueName() {
        return queueName;
    }

    public Message getMessage() {
        return message;
    }
}
