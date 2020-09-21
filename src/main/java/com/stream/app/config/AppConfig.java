package com.stream.app.config;

import com.stream.app.messaging.BasicPaymentProcessor;

import org.springframework.cloud.stream.annotation.EnableBinding;

@EnableBinding({ BasicPaymentProcessor.class })
public class AppConfig {

}
