package com.yrris.project;

import com.yrris.project.model.entity.InterfaceInfo;
import com.yrris.project.model.entity.User;
import com.yrris.project.service.inner.InnerInterfaceInfoService;
import com.yrris.project.service.inner.InnerUserInterfaceInfoService;
import com.yrris.project.service.inner.InnerUserService;
import com.yrris.apiexpansesdk.utils.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 全局过滤器
 */
@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {
    //白名单,模拟禁止本地主机外的所有IP
    private static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1","0:0:0:0:0:0:0:1");

    // todo 在实际转发请求中，需要获取接口所在服务器的地址，这里使用示例接口服务的地址模拟
    private static final String EXAMPLE_INTERFACE_HOST = "http://localhost:8081";

    @DubboReference
    private InnerUserService innerUserService;

    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 业务处理，处理发送到网关的请求
        log.info("custom global filter ...");
        // 1.请求日志
        ServerHttpRequest request = exchange.getRequest();
        //获取请求路径、请求方法、请求参数
        String path = request.getPath().value();
        String method = request.getMethodValue();
        MultiValueMap<String, String> queryParams = request.getQueryParams();
        log.info("请求标识："+request.getId());
        log.info("请求路径"+path);
        log.info("请求方法"+ method);
        log.info("请求参数"+ queryParams);
        String sourceAddress = Objects.requireNonNull(request.getLocalAddress()).getHostString();
        log.info("请求来源地址"+request.getRemoteAddress());
        ServerHttpResponse response = exchange.getResponse();
        // 2.黑白名单
        if(!IP_WHITE_LIST.contains(sourceAddress)){
            //如果不在白名单中 返回403状态码 并直接完成响应
            return handleNoAuth(response);
        }
        // 3.鉴权（ak，sk）
        HttpHeaders headers = request.getHeaders();
        // 从请求头中获取参数
        String accessKey = headers.getFirst("accessKey");
        String nonce = headers.getFirst("nonce");
        String timestamp = headers.getFirst("timestamp");
        String sign = headers.getFirst("sign");
        String body = headers.getFirst("body");

        // 校验数据库中查询是否分配给用户
        User user = null;
        try{
            user = innerUserService.getCallingUser(accessKey);
        }catch(Exception e){
            log.error("通过认证签名获取用户异常"+e);
        }
        //如果无授权用户信心，返回未授权响应
        if(user == null){
            return handleNoAuth(response);
        }

        // todo 校验随机数 使用 hashMap 或者 redis
        // 简答判断逻辑应小于4位
        if (Long.parseLong(nonce) > 10000) {
            return handleNoAuth(response);
        }
        // 时间戳与当前时间差应该小于5min
        if (System.currentTimeMillis() - Long.parseLong(timestamp) > 5 * 60 * 1000) {
            return handleNoAuth(response);
        }

        // todo 从数据库中通过获取到的 accessKey 查询出 secretKey
        String serverSign = SignUtil.genSign(body, "12345678");
        if (sign==null || !sign.equals(serverSign)) {
            return handleNoAuth(response);
        }

        InterfaceInfo interfaceInfo = null;
        // 4.校验调用接口是否存在
        try{
            //todo 尝试通过请求路径、方法、参数 校验调用接口是否存在,应该使用真实服务器地址进行拼接
            interfaceInfo = innerInterfaceInfoService.getCallingInterfaceInfo(EXAMPLE_INTERFACE_HOST+path,method,null);
        }catch (Exception e){
            log.error("查询调用接口信息异常："+e);
        }
        //如果无可调用接口，返回未授权响应
        if(interfaceInfo == null){
            return handleNoAuth(response);
        }
        // todo 5.在调用前检查当前用户是否还有调用该接口的次数leftNum（需要考虑并发）

        // 6.调用模拟接口
//        Mono<Void> filter = chain.filter(exchange);
        // 7.响应日志
        return handleResponse(exchange,chain,user.getId(),interfaceInfo.getId());

        // 以下操作需要在获取到响应后进行，在handleResponse中进行
        // 8.调用成功返回，统计调用次数增加
//        if(response.getStatusCode()==HttpStatus.OK){
//
//        }else{
//        // 9.调用失败返回错误码
//            return handleCallError(response);
//        }
//        return filter;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain ,long userId,long interfaceInfoId) {
        try {
            // 获取原始的响应对象
            ServerHttpResponse originalResponse = exchange.getResponse();
            // 获取数据缓冲工厂
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            // 获取响应的状态码
            HttpStatus statusCode = originalResponse.getStatusCode();

            // 判断状态码是否为200 OK(理论上现在没有调用,是拿不到响应码的)
            if(statusCode == HttpStatus.OK) {
                // 创建一个装饰后的响应对象(增强)
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {

                    // 重写writeWith方法，用于处理响应体的数据
                    // 当模拟接口调用完成之后,等它返回结果，
                    // 就会调用writeWith方法,然后就能根据响应结果做一些自己的处理
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        // 判断响应体是否是Flux类型
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            // 返回一个处理后的响应体 把缓冲区的数据取出来进行拼接
                            return super.writeWith(fluxBody.map(dataBuffer -> {

                                // 7.调用成功返回，统计调用次数增加
                                try{
                                    //尝试通过请求路径、方法、参数 校验调用接口是否存在
                                    innerUserInterfaceInfoService.callCount(userId,interfaceInfoId);
                                }catch (Exception e){
                                    log.error("增加接口调用次数异常："+e);
                                }

                                // 读取响应体的内容并转换为字节数组
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);//释放掉内存
                                // 构建日志
                                StringBuilder sb2 = new StringBuilder(200);
//                                sb2.append("<--- {} {} \n");
                                List<Object> rspArgs = new ArrayList<>();
                                rspArgs.add(originalResponse.getStatusCode());
                                //rspArgs.add(requestUrl);
                                String data = new String(content, StandardCharsets.UTF_8);//data
                                sb2.append(data);
                                log.info("响应结果："+data);
                                // 将处理后的内容重新包装成DataBuffer并返回
                                return bufferFactory.wrap(content);
                            }));
                        } else {
                            // 8.调用失败返回错误码
//                            return handleCallError(response);
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                // 对于200 OK的请求,将装饰后的响应对象传递给下一个过滤器链,并继续处理(设置response对象为装饰过的)
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            // 对于非200 OK的请求，直接返回，进行降级处理
            return chain.filter(exchange);
        }catch (Exception e){
            // 处理异常情况，记录错误日志
            log.error("网关处理响应异常：" + e);
            return chain.filter(exchange);
        }
    }


    private Mono<Void> handleNoAuth(ServerHttpResponse response){
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    private Mono<Void> handleCallError(ServerHttpResponse response){
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }
}
