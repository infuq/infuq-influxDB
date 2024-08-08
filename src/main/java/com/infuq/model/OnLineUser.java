package com.infuq.model;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.List;

@Data
@Accessors(chain = true)
@Measurement(name = "online_user")  // 表
public class OnLineUser {

    // 该条数据针对的是哪个服务进行的统计
    @Column(name = "service_name", tag = true) // 带索引
    private String serviceName;

    // 在线总人数
    @Column(name = "user_total")
    private Integer activeUserTotal;

    // 在线的用户
    @Column(name = "all_user")
    private List<Long> allUserId;

    @Column(timestamp = true)
    private Instant actionTime;


}
