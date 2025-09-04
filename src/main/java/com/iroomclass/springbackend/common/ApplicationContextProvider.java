package com.iroomclass.springbackend.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring ApplicationContext에 접근할 수 있는 유틸리티 클래스
 * 
 * 엔티티나 일반 클래스에서 Spring Bean에 접근해야 할 때 사용합니다.
 * 주로 Repository나 Service를 엔티티에서 사용해야 하는 경우에 활용됩니다.
 * 
 * @author 이룸클래스
 * @since 2025
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext context;

    /**
     * ApplicationContext 설정
     * Spring에서 자동으로 호출됩니다.
     * 
     * @param applicationContext Spring ApplicationContext
     * @throws BeansException Bean 처리 중 예외 발생
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    /**
     * ApplicationContext 조회
     * 
     * @return Spring ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return context;
    }

    /**
     * Bean 조회 (타입 기반)
     * 
     * @param beanClass 조회할 Bean의 클래스 타입
     * @param <T> Bean 타입
     * @return 해당 타입의 Bean 인스턴스
     */
    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    /**
     * Bean 조회 (이름 기반)
     * 
     * @param beanName 조회할 Bean의 이름
     * @return Bean 인스턴스
     */
    public static Object getBean(String beanName) {
        return context.getBean(beanName);
    }

    /**
     * Bean 조회 (이름 + 타입 기반)
     * 
     * @param beanName 조회할 Bean의 이름
     * @param beanClass 조회할 Bean의 클래스 타입
     * @param <T> Bean 타입
     * @return 해당 타입의 Bean 인스턴스
     */
    public static <T> T getBean(String beanName, Class<T> beanClass) {
        return context.getBean(beanName, beanClass);
    }
}