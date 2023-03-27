package com.example.springcloudaws723;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@Timeout(10)
@Testcontainers
@SpringBootTest
@Import(ListenerTest.SqsConfig.class)
@TestPropertySource(properties = "spring.main.lazy-initialization=true")
class ListenerTest {

    @Container
    static final LocalStackContainer LOCALSTACK = new LocalStackContainer(DockerImageName.parse("localstack/localstack:1.4"))
        .withServices(LocalStackContainer.Service.SQS);

    @SpyBean
    Listener listener;

    @Autowired
    SqsAsyncClient client;

    @Test
    void test() {
        sendTestMessage();

        await().untilAsserted(() -> verify(listener, atLeastOnce()).listen(any()));
    }

    void sendTestMessage() {
        client.sendMessage(req -> req.messageBody("test").queueUrl(getQueueUrl()));
    }

    @AfterEach
    void afterEach() {
        client.purgeQueue(a -> a.queueUrl(getQueueUrl())).join();
    }

    String getQueueUrl() {
        return LOCALSTACK.getEndpointOverride(LocalStackContainer.Service.SQS).toString() + "/queue/test-queue";
    }

    static class SqsConfig {

        @Bean
        SqsAsyncClient asyncClient() {
            return SqsAsyncClient.builder()
                .endpointOverride(LOCALSTACK.getEndpointOverride(LocalStackContainer.Service.SQS))
                .region(Region.of(LOCALSTACK.getRegion()))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(LOCALSTACK.getAccessKey(), LOCALSTACK.getSecretKey())
                    )
                )
                .build();
        }

    }

}