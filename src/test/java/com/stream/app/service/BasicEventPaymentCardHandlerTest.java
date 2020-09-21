package com.stream.app.service;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stream.app.Application;
import com.stream.app.messaging.BasicPaymentProcessor;
import com.stream.app.model.PaymentCard;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = Application.class)
public class BasicEventPaymentCardHandlerTest {

	// Agregamos el Channel que vamos a probar
	@Autowired
	private BasicPaymentProcessor basicPaymentProcessor;

	// Agrego MessageCollector que sirve para almacenar el mensaje de Salida.
	@Autowired
	private MessageCollector collector;

	@Test
	public void should_recieve_a_message_then_return_and_add_interest_to_the_amount()
			throws JsonParseException, JsonMappingException, IOException {

		// Monto a enviar
		double amount = 250.50;

		// Interés a aplicar
		double interest = 1.15;

		// Monto que espero
		double amountExpected = amount * interest;

		// Creo el mensaje de entrada para que el listener lo pueda obtener
		PaymentCard paymentCardIngest = PaymentCard.builder().id(1).description("Descripcion").amount(amount).build();

		// Invoco al Channel de entrada con le mensaje
		basicPaymentProcessor.input().send(MessageBuilder.withPayload(paymentCardIngest).build());

		// Me apoyo en MessageCollector que sirve para obtener el mensaje de un channel
		final BlockingQueue<Message<?>> messageQueue = collector.forChannel(basicPaymentProcessor.output());

		messageQueue.element().getPayload();
		
		// Cree una función "loadJson" que utiliza Jackson para parsear JSON
		PaymentCard paymentCard = loadJson(messageQueue.element().getPayload());

		// Comparo los datos con los esperados
		assertThat(messageQueue, hasSize(1));
		assertEquals(amountExpected, paymentCard.getAmount(), 288);
	}

	private PaymentCard loadJson(Object stringJson) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue((String) stringJson, PaymentCard.class);
	}

}
