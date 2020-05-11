package kr.wellstone.api;

import com.squareup.javapoet.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

public class CrudGenerator {

    public static void main(String[] args) throws IOException {
        String packageName = "kr.wellstone.api";
        generateDefaultSetup(packageName);
        generateCrudApi(packageName);
    }

    /**
     * @param packageName package name
     * @implNote Generate default setup class
     * config - ApplicationConfig
     * common - ResponseData, ResponseDataType, MessageSourceImpl, GlobalExceptionController
     * exception - BadValidationException, DuplicatedException, NoPermissionException, NotFoundException
     * TODO...
     */
    public static void generateDefaultSetup(String packageName) throws IOException {
        generateApplicationConfig(packageName);
        generateResponseData(packageName);
        generateMessageSourceImpl(packageName);
        generateGlobalExceptionController(packageName);
        generateExceptions(packageName);
    }

    /**
     * @param packageName package name
     * @implNote Generate CRUD API class
     * 1. read domain file(resources/domain/..) and create domain class
     * 2. create controller(url = domain)
     * 3. create service
     * 4. create repository
     * 5. create querydsl(TODO)
     * TODO..
     */
    private static void generateCrudApi(String packageName) {

    }

    private static void generateApplicationConfig(String packageName) throws IOException {
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
                .addField(Environment.class, "env", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(modelMapper)
                .addMethod(messageSource)
                .build();

        JavaFile file1 = JavaFile.builder(String.format("%s.config", packageName), applicationConfig).build();
        file1.writeTo(file);
    }

    private static void generateResponseData(String packageName) throws IOException {
        File file = new File("src/main/java");
        TypeSpec ResponseDataType = TypeSpec.enumBuilder("ResponseDataType")
                .addModifiers(Modifier.PUBLIC)
                .addEnumConstant("FAILED")
                .addEnumConstant("SUCCESS")
                .addEnumConstant("UNDEFINED")
                .build();

        JavaFile ResponseDataTypeFile = JavaFile.builder(String.format("%s.common", packageName), ResponseDataType).build();
        ResponseDataTypeFile.writeTo(file);

        TypeSpec responseData = TypeSpec.classBuilder("ResponseData")
                .addAnnotation(Getter.class).addAnnotation(Setter.class).addAnnotation(Builder.class)
                .addAnnotation(NoArgsConstructor.class).addAnnotation(AllArgsConstructor.class)
                .addModifiers(Modifier.PUBLIC)
                .addField(String.class, "message", Modifier.PRIVATE)
                .addField(ClassName.get(String.format("%s.common", packageName), "ResponseDataType"),
                        "type",Modifier.PRIVATE)
                .addField(Object.class, "result", Modifier.PRIVATE)
                .addField(FieldSpec.builder(Date.class, "respDate", Modifier.PRIVATE)
                        .addAnnotation(Builder.Default.class)
                        .initializer("new $T()", Date.class)
                        .build())
                .build();

        JavaFile file1 = JavaFile.builder(String.format("%s.common", packageName), responseData).build();
        file1.writeTo(file);
    }

    private static void generateMessageSourceImpl(String packageName) throws IOException {
        File file = new File("src/main/java");

        MethodSpec get = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addParameter(String.class, "code")
                .addParameter(Object[].class, "args")
                .addParameter(Locale.class, "locale")
                .addStatement("return messageSource.getMessage(code, args, locale)")
                .build();


        TypeSpec globalExceptionController = TypeSpec.classBuilder("MessageSourceImpl")
                .addAnnotation(Slf4j.class)
                .addAnnotation(RequiredArgsConstructor.class)
                .addAnnotation(Service.class)
                .addModifiers(Modifier.PUBLIC)
                .addField(MessageSource.class, "messageSource", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(get)
                .build();

        JavaFile file1 = JavaFile.builder(String.format("%s.common", packageName), globalExceptionController).build();
        file1.writeTo(file);
    }
    
    private static void generateGlobalExceptionController(String packageName) throws IOException {
        File file = new File("src/main/java");

        MethodSpec exceptionHandle = MethodSpec.methodBuilder("exceptionHandle")
                .addAnnotation(AnnotationSpec.builder(ExceptionHandler.class)
                        .addMember("value", "value = $T.class", Exception.class)
                        .build())
                .addModifiers(Modifier.PUBLIC)
                .returns(ResponseEntity.class)
                .addParameter(Exception.class, "e")
                .addParameter(Locale.class, "locale")
                .addStatement("log.error(\"API Server Global Error=\")")
                .addStatement("log.error(e.getMessage())")
                .addStatement("ResponseData responseData = $T.builder()\n" +
                        "                .message(e.getMessage())\n" +
                        "                .type($T.FAILED)\n" +
                        "                .build()",
                        ClassName.get(String.format("%s.common", packageName), "ResponseData"),
                        ClassName.get(String.format("%s.common", packageName), "ResponseDataType"))
                .addStatement("return new ResponseEntity<>(responseData, $T.INTERNAL_SERVER_ERROR)", HttpStatus.class)
                .build();


        TypeSpec globalExceptionController = TypeSpec.classBuilder("GlobalExceptionController")
                .addAnnotation(Slf4j.class)
                .addAnnotation(RequiredArgsConstructor.class)
                .addAnnotation(RestControllerAdvice.class)
                .addModifiers(Modifier.PUBLIC)
                .addField(ClassName.get(String.format("%s.common", packageName), "MessageSourceImpl"),
                        "messageSource", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(exceptionHandle)
                .build();

        JavaFile file1 = JavaFile.builder(String.format("%s.common", packageName), globalExceptionController).build();
        file1.writeTo(file);
    }

    private static void generateExceptions(String packageName) throws IOException {
        File file = new File("src/main/java");

        TypeSpec BadValidationException = TypeSpec.classBuilder("BadValidationException")
                .addAnnotation(AllArgsConstructor.class)
                .superclass(RuntimeException.class)
                .addModifiers(Modifier.PUBLIC)
                .addField(FieldSpec.builder(Object.class, "information", Modifier.FINAL)
                        .addAnnotation(Getter.class)
                        .build())
                .build();

        JavaFile file1 = JavaFile.builder(String.format("%s.exception", packageName), BadValidationException).build();
        file1.writeTo(file);

        TypeSpec DuplicatedException = TypeSpec.classBuilder("DuplicatedException")
                .addAnnotation(AllArgsConstructor.class)
                .superclass(RuntimeException.class)
                .addModifiers(Modifier.PUBLIC)
                .addField(FieldSpec.builder(String.class, "information", Modifier.FINAL)
                        .addAnnotation(Getter.class)
                        .build())
                .build();

        JavaFile file2 = JavaFile.builder(String.format("%s.exception", packageName), DuplicatedException).build();
        file2.writeTo(file);

        TypeSpec NoPermissionException = TypeSpec.classBuilder("NoPermissionException")
                .addAnnotation(AllArgsConstructor.class)
                .superclass(RuntimeException.class)
                .addModifiers(Modifier.PUBLIC)
                .addField(FieldSpec.builder(String.class, "information", Modifier.FINAL)
                        .addAnnotation(Getter.class)
                        .build())
                .build();

        JavaFile file3 = JavaFile.builder(String.format("%s.exception", packageName), NoPermissionException).build();
        file3.writeTo(file);

        TypeSpec NotFoundException = TypeSpec.classBuilder("NotFoundException")
                .addAnnotation(AllArgsConstructor.class)
                .superclass(RuntimeException.class)
                .addModifiers(Modifier.PUBLIC)
                .addField(FieldSpec.builder(String.class, "information", Modifier.FINAL)
                        .addAnnotation(Getter.class)
                        .build())
                .build();

        JavaFile file4 = JavaFile.builder(String.format("%s.exception", packageName), NotFoundException).build();
        file4.writeTo(file);
    }
}
