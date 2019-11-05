package my.service;


import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.serverless.proxy.spring.SpringBootProxyHandlerBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;


public class StreamLambdaHandler implements RequestStreamHandler {
    private static SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;
    static {
        try {
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(Application.class);
        } catch (ContainerInitializationException e) {
            // if we fail here. We re-throw the exception to force another cold start
            e.printStackTrace();
            throw new RuntimeException("Could not initialize Spring Boot application", e);
        }
    }
//    getAwsProxyHandler roept achterliggend dit aan, static mag weg bij async aangezien we daar zelf de handler aanmaken
//    return new SpringBootProxyHandlerBuilder()
//                .defaultProxy()
//                .initializationWrapper(new InitializationWrapper())
//                .springBootApplication(springBootInitializer)
//                .profiles(profiles)
//                .buildAndInitialize();

// AWS Lambda limits the initialization phase to 10 seconds.
// If you application takes longer than 10 seconds to start, AWS Lambda will assume the sandbox is dead and attempt to start a new one.
// The following solves this using async initialisation.
// When Lambda sends the first event, Serverless Java Container will wait for the asynchronous initializer to complete
// its work before sending the event for processing.
// ! Static block and static keyword on handler have to be removed for this !
//    public StreamLambdaHandler() throws ContainerInitializationException {
//        long startTime = Instant.now().toEpochMilli();
//        handler = new SpringBootProxyHandlerBuilder()
//                .defaultProxy()
//                .asyncInit(startTime)
//                .springBootApplication(Application.class)
//                .buildAndInitialize();
//    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        handler.proxyStream(inputStream, outputStream, context);
    }
}