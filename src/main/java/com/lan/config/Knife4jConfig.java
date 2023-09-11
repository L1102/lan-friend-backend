// package com.lan.config;
//
// import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
// import com.github.xiaoymin.knife4j.spring.extension.OpenApiExtensionResolver;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Profile;
// import springfox.documentation.builders.ApiInfoBuilder;
// import springfox.documentation.builders.PathSelectors;
// import springfox.documentation.builders.RequestHandlerSelectors;
// import springfox.documentation.service.ApiInfo;
// import springfox.documentation.service.Contact;
// import springfox.documentation.spi.DocumentationType;
// import springfox.documentation.spring.web.plugins.Docket;
// import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;
//
// /**
//  * @author lan
//  */
// @Configuration // 标明是配置类
// @EnableSwagger2WebMvc
// @EnableKnife4j
// @Profile({"dev", "test"})
// public class Knife4jConfig {
//     /*引入Knife4j提供的扩展类*/
//     private final OpenApiExtensionResolver openApiExtensionResolver;
//
//     @Autowired
//     public Knife4jConfig(OpenApiExtensionResolver openApiExtensionResolver) {
//         this.openApiExtensionResolver = openApiExtensionResolver;
//     }
//
//     @Bean(value = "defaultApi2")
//     public Docket defaultApi2() {
//         String groupName = "1.0";
//         return new Docket(DocumentationType.SPRING_WEB)
//                 // .host("https://www.baidu.com")
//                 .apiInfo(apiInfo())
//                 .groupName(groupName)
//                 .select()
//                 .apis(RequestHandlerSelectors.basePackage("com.lan.controller"))
//                 .paths(PathSelectors.any())
//                 .build()
//                 // 赋予插件体系
//                 .extensions(openApiExtensionResolver.buildExtensions(groupName));
//     }
//
//     /**
//      * 用于定义API主界面的信息，比如可以声明所有的API的总标题、描述、版本
//      * @return
//      */
//     private ApiInfo apiInfo() {
//         return new ApiInfoBuilder()
//                 .title("lan朋友匹配平台接口文档") //  可以用来自定义API的主标题
//                 .description("lan朋友匹配平台接口文档") // 可以用来描述整体的API
//                 .termsOfServiceUrl("") // 用于定义服务的域名
//                 .contact(new Contact("lan","https://github.com/L1102","1479202130@qq.com"))
//                 .version("1.0") // 可以用来定义版本。
//                 .build(); //
//     }
// }
