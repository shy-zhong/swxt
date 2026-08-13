package com.swxt.manager.config;

import cn.hutool.extra.qrcode.QrConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.awt.*;

@Configuration
public class QRConfig {

    @Bean
    public QrConfig qrConfig(){
        QrConfig qrConfig=new QrConfig(300,300);

        qrConfig.setMargin(2);
        qrConfig.setForeColor(Color.BLACK.getRGB());
        qrConfig.setBackColor(Color.WHITE.getRGB());

        return qrConfig;
    }
}
