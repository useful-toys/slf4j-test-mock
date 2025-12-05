package org.usefultoys.slf4jtestmock;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Slf4jMock {

    /**
     * Nome explícito do logger (ex.: "security.audit").
     * Tem prioridade sobre type().
     */
    String value() default "";

    /**
     * Classe cujo nome será usado como nome do logger
     * (ex.: MyService.class → nome canonical).
     * Usado se value() estiver vazio.
     */
    Class<?> type() default Void.class;

    /**
     * Liga/desliga o logger como um todo.
     */
    boolean enabled() default true;

    // Flags de nível – todos true por padrão
    boolean traceEnabled() default true;
    boolean debugEnabled() default true;
    boolean infoEnabled()  default true;
    boolean warnEnabled()  default true;
    boolean errorEnabled() default true;
}
