package org.srm.source;

import lombok.SneakyThrows;
import org.hzero.actuator.strategy.impl.PermissionActuatorStrategy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.srm.autoconfigure.source.EnableSrmSource;

import java.lang.reflect.Field;

/**
 * description
 *
 * @author guotao.yu@going-link.com  2021/03/22 19:00
 */
@EnableSrmSource
@SpringBootApplication
public class HitachiSrmSourceApplication implements BeanPostProcessor {
    public static void main(String[] args) {
        SpringApplication.run(SrmSourceApplication.class,  args);
    }

    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof PermissionActuatorStrategy){
            Field duplicatedCodeCheck = null;
            try {
                duplicatedCodeCheck = bean.getClass().getDeclaredField("duplicatedCodeCheck");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            duplicatedCodeCheck.setAccessible(true);
            try {
                duplicatedCodeCheck.set(bean,false);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }
}

