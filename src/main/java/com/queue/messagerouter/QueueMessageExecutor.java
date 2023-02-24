package com.queue.messagerouter;

import com.queue.messagerouter.interfaces.MessageExecutor;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class QueueMessageExecutor implements MessageExecutor {
    @Override
    public void execute(String message) {
        System.out.println(message);
    }
}
