package com.nexti.timetracking.messagerouter;

import com.nexti.timetracking.messagerouter.dtos.MessageHandler;
import com.nexti.timetracking.messagerouter.interfaces.MessageExecutor;
import com.nexti.timetracking.messagerouter.interfaces.MessageManipulation;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sqs.model.Message;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.function.Consumer;

@ApplicationScoped
public class MessageSubscriptionConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSubscriptionConfiguration.class);

    private final EventBus eventBus;
    private final List<String> queues;
    private final MessageManipulation messageManipulation;
    private final MessageExecutor messageExecutor;


    public MessageSubscriptionConfiguration(
            EventBus eventBus,
            MessageManipulation messageManipulation,
            MessageExecutor messageExecutor,
            @ConfigProperty(name = "quarkus.queue.list") List<String> queues
    ) {
        this.eventBus = eventBus;
        this.messageManipulation = messageManipulation;
        this.messageExecutor = messageExecutor;
        this.queues = queues;
    }


    @Scheduled(every="${quarkus.queue.interval}")
    public void sqsSubscribe() {
        queues.parallelStream().forEach(queueName ->
            Uni.createFrom()
                .completionStage(messageManipulation.receive(queueName))
                .subscribe()
                .with(response -> {
                    int eventsCount = response.messages().size();
                    if (eventsCount == 0)
                        LOGGER.debug("Queue {} was empty", response);

                    LOGGER.debug("Ready messages in batch, size {}", eventsCount);
                    response.messages().forEach(message -> {
                        eventBus.send("receive-message", new MessageHandler(queueName, message));
                    });
                })
        );
    }

    @ConsumeEvent("receive-message")
    public Uni<Void> eventReceivedMessage(MessageHandler messageHandler) {
        return Uni.createFrom()
                .item(messageHandler)
                .onItem().invoke(receive -> processReceivedMessage(messageHandler, messageExecutor::execute))
                .replaceWithVoid()
                .onFailure()
                .invoke(fail -> LOGGER.error("Error processing messages!", fail));
    }

    @ConsumeEvent("delete-message")
    public Uni<Void> eventDeleteMessage(MessageHandler messageHandler) {
        Message message = messageHandler.getMessage();
        String queueName = messageHandler.getQueueName();

        return Uni.createFrom()
                .completionStage(messageManipulation.delete(messageHandler))
                .onItem()
                .invoke(response -> LOGGER.info("Message {} deleted from the queue {}", message.messageId(), queueName))
                .onFailure()
                .invoke(callBack -> LOGGER.info("Could not delete message {} from the queue {}", message.messageId(), queueName)).replaceWithVoid();
    }

    private void processReceivedMessage(MessageHandler messageHandler, Consumer<String> consumer) {
        LOGGER.info("Message {} received from the queue {}", messageHandler.getMessage().messageId(), messageHandler.getQueueName());
        consumer.accept(messageHandler.getMessage().body());
        eventBus.send("delete-message", messageHandler);
    }
}