package com.webtrans.Courier.config;

import com.webtrans.auth.model.User;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

@Configuration("courierWebSocketConfig")
@EnableWebSocketMessageBroker
public class CourierWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Clients subscribe to /topic/* to receive messages
        config.enableSimpleBroker("/topic");

        // Clients send to /app/* which routes to @MessageMapping methods
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The WebSocket handshake endpoint with session handshake interceptor
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")     // allow any origin
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .withSockJS();                     // fallback for older browsers
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new FoodCourierChannelInterceptor());
    }

    public static class FoodCourierChannelInterceptor implements ChannelInterceptor {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                String dest = accessor.getDestination();
                if (dest != null && dest.startsWith("/topic/parcel/")) {
                    Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                    if (sessionAttributes == null || !(sessionAttributes.get("loggedUser") instanceof User)) {
                        throw new AccessDeniedException("Authentication required for Courier live updates");
                    }
                }
                if (dest != null && dest.startsWith("/topic/food/")) {
                    Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                    boolean authenticated = sessionAttributes != null && 
                            (sessionAttributes.get("loggedUser") != null || sessionAttributes.get("foodUser") != null);
                    if (!authenticated) {
                        throw new AccessDeniedException("Authentication required for Food live updates");
                    }
                }
            }
            return message;
        }
    }
}
