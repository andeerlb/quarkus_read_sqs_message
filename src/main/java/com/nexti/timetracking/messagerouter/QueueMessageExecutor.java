package com.nexti.timetracking.messagerouter;

import com.nexti.timetracking.messagerouter.interfaces.MessageExecutor;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class QueueMessageExecutor implements MessageExecutor {
    @Override
    public void execute(String message) {
        System.out.println(message);
    }
}
