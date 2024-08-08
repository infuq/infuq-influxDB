package com.infuq.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "influx")
public class DBConfig {

    @Value("${influx.url}")
    private String url;
    @Value("${influx.token}")
    private String token;
    @Value("${influx.org}")
    private String org;
    @Value("${influx.bucket}")
    private String bucket;


    @Bean
    public InfluxDBClient influxDBClient() {
        return InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
    }


}
