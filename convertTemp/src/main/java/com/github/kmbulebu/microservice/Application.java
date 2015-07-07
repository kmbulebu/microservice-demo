package com.github.kmbulebu.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.integration.http.inbound.HttpRequestHandlingMessagingGateway;
import org.springframework.integration.http.inbound.RequestMapping;
import org.springframework.integration.ws.SimpleWebServiceOutboundGateway;
import org.springframework.integration.ws.WebServiceHeaders;
import org.springframework.messaging.MessageChannel;

@SpringBootApplication
@IntegrationComponentScan
/**
 * Spring Integration Example.
 * Caller submits requests via HTTP POST with the temperature in the plain/text body. 
 * Results are returned synchronously in the HTTP response body.
 * 
 * The conversion is performed by calling out to a w3schools SOAP service.
 * 
 * To test, POST to http://<ip>:8080/convertTemp
 * @author kmbulebu
 *
 */
public class Application {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}

	/**
	 * Inbound gateway receives messages via HTTP (server).
	 * 
	 * @return
	 */
	@Bean
	public HttpRequestHandlingMessagingGateway httpGate() {
		HttpRequestHandlingMessagingGateway gateway = new HttpRequestHandlingMessagingGateway(true);
		RequestMapping mapping = new RequestMapping();
		mapping.setMethods(HttpMethod.POST);
		mapping.setPathPatterns("/convertTemp");
		gateway.setRequestMapping(mapping);
		gateway.setRequestChannel(directRequestChannel());
		gateway.setRequestPayloadType(String.class);
		return gateway;
	}

	@Bean
	/**
	 * A channel that invokes a single subscriber for each sent Message. The invocation will occur in the sender's thread. 
	 */
	public MessageChannel directRequestChannel() {
		return new DirectChannel();
	}

	@Bean
	/**
	 * Integration flow that cleverly transforms our message into a call to a third party web service.
	 */
	public IntegrationFlow flow() {
		return IntegrationFlows.from("directRequestChannel")
				.transform(payload -> "<FahrenheitToCelsius xmlns=\"http://www.w3schools.com/webservices/\">" + "<Fahrenheit>" + payload + "</Fahrenheit>" + "</FahrenheitToCelsius>")
				.enrichHeaders(h -> h.header(WebServiceHeaders.SOAP_ACTION, "http://www.w3schools.com/webservices/FahrenheitToCelsius"))
				.handle(new SimpleWebServiceOutboundGateway("http://www.w3schools.com/webservices/tempconvert.asmx"))
				.transform(Transformers.xpath("/*[local-name()=\"FahrenheitToCelsiusResponse\"]" + "/*[local-name()=\"FahrenheitToCelsiusResult\"]"))
				.get();
	}

}
