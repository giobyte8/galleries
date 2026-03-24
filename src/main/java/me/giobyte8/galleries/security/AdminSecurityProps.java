package me.giobyte8.galleries.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "galleries.security")
@Data
public class AdminSecurityProps {

	private String rememberMeKey;
}


