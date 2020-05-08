package kr.wellstone.api;

import com.squareup.javapoet.*;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;

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
        String configPackageName = packageName + ".config";
        MethodSpec modelMapper = MethodSpec.methodBuilder("modelMapper")
                .returns(ModelMapper.class)
                .build();

        TypeSpec applicationConfig = TypeSpec.classBuilder("ApplicationConfig")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(modelMapper)
                .build();

        JavaFile javaFile = JavaFile.builder(packageName, applicationConfig).build();
        javaFile.writeTo(System.out);
//        File file = new File(configPackageName);
//        javaFile.writeTo(file);
    }
}


/*
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final Environment env;

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages/message");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public ApplicationRunner applicationRunner() {
        return new ApplicationRunner() {
            @Autowired
            LeshanServer leshanServer;

            @Override
            public void run(ApplicationArguments args) {
                if (!Objects.equals(env.getProperty("test.code"), "true")) {
                    leshanServer.start();
//                    gatewayRepository.findAll().forEach(gateway -> {
//                        try {
//                            ((EditableSecurityStore) leshanServer.getSecurityStore())
//                                    .add(SecurityInfo.newPreSharedKeyInfo(gateway.getEndpoint(), gateway.getEndpoint(), gateway.getPsk().getBytes()));
//                        } catch (NonUniqueSecurityInfoException e) {
//                            e.printStackTrace();
//                        }
//                    });
                }
            }
        };
    }
}

 */