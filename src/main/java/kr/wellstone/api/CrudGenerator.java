package kr.wellstone.api;

import com.squareup.javapoet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;

public class CrudGenerator {

    public static void main(String[] args) throws IOException {
        String packageName = "kr.wellstone.api";
        defaultSetup(packageName);
    }

    /**
     * @param packageName package name
     * @implNote Generate default setup class
     * config - ApplicationConfig
     * common - ResponseData, ResponseDataType, MessageSourceImpl, GlobalExceptionController
     * TODO...
     */
    public static void defaultSetup(String packageName) throws IOException {
        File file = new File("src/main/java");

        MethodSpec modelMapper = MethodSpec.methodBuilder("modelMapper")
                .addAnnotation(Bean.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ModelMapper.class)
                .addStatement("return new ModelMapper()")
                .build();

        MethodSpec messageSource = MethodSpec.methodBuilder("messageSource")
                .addAnnotation(Bean.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(MessageSource.class)
                .addStatement("ResourceBundleMessageSource messageSource = new $T()", ResourceBundleMessageSource.class)
                .addStatement("messageSource.setBasename(\"messages/message\")")
                .addStatement("messageSource.setDefaultEncoding(\"UTF-8\")")
                .addStatement("return messageSource")
                .build();

        TypeSpec applicationConfig = TypeSpec.classBuilder("ApplicationConfig")
                .addAnnotation(Slf4j.class)
                .addAnnotation(Configuration.class)
                .addAnnotation(RequiredArgsConstructor.class)
                .addModifiers(Modifier.PUBLIC)
                .addField(Environment.class, "env", Modifier.FINAL, Modifier.PRIVATE)
                .addMethod(modelMapper)
                .addMethod(messageSource)
                .build();

        JavaFile applicationConfigFile = JavaFile.builder(String.format("%s.config", packageName), applicationConfig).build();
        applicationConfigFile.writeTo(file);
    }
}
