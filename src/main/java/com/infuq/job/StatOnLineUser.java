package com.infuq.job;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.infuq.config.DBConfig;
import com.infuq.model.OnLineUser;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 统计在线用户
 */
@Component
public class StatOnLineUser {

    @Resource
    private DBConfig config;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private InfluxDBClient influxDBClient;

    @PostConstruct
    public void init() {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

        WriteApiBlocking writeApiBlocking = influxDBClient.getWriteApiBlocking();

        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {

                    List<Long> allUserId = new ArrayList<>();
                    List<Long> delUserId = new ArrayList<>();

                    Set<Object> allUser = redisTemplate.opsForSet().members("user:active:30min:all");
                    if (!CollectionUtils.isEmpty(allUser)) {
                        for (Object u : allUser) {
                            // 判断集合里的这个 u 是否还存在
                            Object o = redisTemplate.opsForValue().get("user:active:30min:" + u.toString());
                            if (o == null) {
                                // 不存在了
                                delUserId.add(Long.valueOf(u.toString()));

                                redisTemplate.opsForSet().remove("user:active:30min:all", Long.valueOf(u.toString()));
                            } else {
                                // 还存在
                                allUserId.add(Long.valueOf(u.toString()));
                            }
                        }
                    }

                    OnLineUser onLineUser = new OnLineUser();
                    onLineUser.setServiceName("wms");
                    onLineUser.setAllUserId(allUserId);
                    onLineUser.setActiveUserTotal(allUserId.size());
                    onLineUser.setActionTime(Instant.now());

                    writeApiBlocking.writeMeasurement(config.getBucket(), config.getOrg(), WritePrecision.MS, onLineUser);

                } catch (Throwable t) {
                    t.printStackTrace();
                }

            }
        }, 10, 30, TimeUnit.SECONDS);

    }



}
