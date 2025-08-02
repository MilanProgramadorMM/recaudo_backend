package com.recaudo.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

     /** If you need a usecase bean, yo can replace or add new methods with your custom usecase class
     * Example:
     * @Bean
     * public AuthUseCase authUseCaseBean(){
     *     return new AuthUsecase();
     * }
     *
     * With params or usecase attributes
     * @Bean
     * public AuthUseCase authUseCaseBean(FirstParam firstParam..., ParamN paramN){
     *      return new AuthUsecase(firstParam..., paramN);
     * }
     * In this case params are anothers class beans
     *
     * */
    @Bean
    public ObjectMapper testUseCaseBean(){
        return new ObjectMapper();
    }



}
