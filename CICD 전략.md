# CICD 전략
---
- React * 2 (Tanstack Start with SSR)
- Spring Boot 3.5.4 (gradle, JAVA 21, Spring MVC) 
- langserve (fastapi) 
---

React 두 프로젝트가 모두 Tanstack Start 쓰고 있어. 다만 React, SpringBoot, Langserve 모두 Docker, Github Actions, Code Deploy, EC2 기반 CI/CD 할거야. 다중 서버가 아닌, 하나의 서버를 사용할거야. 그니까, React1 = 서버 1개, React2 = 서버 1개, Spring boot= 서버 1개, Langserve = 서버 1개. Nginx를 통해서 각 서버에서 포트 스위칭을 통한 방식으로 무중단 CI/CD를 구현할거야. 오토스케일링, 로드밸런서 사용 안 할거야. 다중 서버 안 할거야. 무슨 말인지 알겠지? 

# Context 7 주소 (아래 주소로 get-library-docs 사용할 것)
- Spring Boot 3.5.4 -> "/websites/spring_io-spring-boot"