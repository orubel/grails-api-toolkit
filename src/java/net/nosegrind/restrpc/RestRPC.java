package net.nosegrind.restrpc;


import java.lang.annotation.*;

// @RestRPC(Method.GET, Json.Schema)
@java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Inherited
@java.lang.annotation.Documented
public @interface RestRPC {
    RpcMethod request();
}

