package com.nexti.timetracking.messagerouter.interfaces;

import com.nexti.timetracking.messagerouter.dtos.MessageHandler;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.concurrent.CompletableFuture;

public interface MessageManipulation {
    CompletableFuture<DeleteMessageResponse> delete(MessageHandler messageHandler);
    CompletableFuture<ReceiveMessageResponse> receive(String queueUrl);
}
