package com.example.springcloudaws723;

import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Listener {

    final Logger logger = LoggerFactory.getLogger(Listener.class);

    @SqsListener("test-queue")
    void listen(final String message) {
        logger.info("");
    }

}
