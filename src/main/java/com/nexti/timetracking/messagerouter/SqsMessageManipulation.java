package com.nexti.timetracking.messagerouter;

import com.nexti.timetracking.messagerouter.dtos.MessageHandler;
import com.nexti.timetracking.messagerouter.interfaces.MessageManipulation;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class SqsMessageManipulation implements MessageManipulation {

    private final SqsAsyncClient sqs;
    private final int maxNumberOfMessages;

    public SqsMessageManipulation(
            SqsAsyncClient sqs,
            @ConfigProperty(name = "quarkus.sqs.max-number-of-messages") int maxNumberOfMessages
    ) {
        this.sqs = sqs;
        this.maxNumberOfMessages = maxNumberOfMessages;
    }

    public CompletableFuture<ReceiveMessageResponse> receive(String queueUrl) {
        return sqs.receiveMessage(configuration -> {
            configuration.maxNumberOfMessages(maxNumberOfMessages);
            configuration.queueUrl(queueUrl);
        });
    }

    public CompletableFuture<DeleteMessageResponse> delete(MessageHandler messageHandler) {
        return sqs.deleteMessage(configuration -> {
            configuration.queueUrl(messageHandler.getQueueName());
            configuration.receiptHandle(messageHandler.getMessage().receiptHandle());
        });
    }
}
