package com.mailspaghetti.services.pdfService.PdfService.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.mailspaghetti.services.pdfService.PdfService.common.DirectoryHelper;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@EnableWebMvc
public class AppConfig extends WebMvcConfigurerAdapter{
  
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        /*view file configuration*/
        String rootPath = DirectoryHelper.getRootDirectory(); //System.getProperty("user.dir");
        String strAbsolutePath = "file://" + rootPath + "/webapp/documents/";
        registry.addResourceHandler("/documents/**").addResourceLocations(strAbsolutePath);
    }
}