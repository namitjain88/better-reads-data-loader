package com.learn.betterreadsdataloader;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * Class to expose custom datastax.astra property secure-connect-bundle from application.yml to app.
 */
@Configuration
@ConfigurationProperties(prefix = "datastax.astra")
@Data
public class DataStaxAstraProperties {

    private File secureConnectBundle;
}
