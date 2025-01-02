package com.ocd.util.crash;

import com.alibaba.fastjson.JSONObject;
import com.ocd.util.email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;

import static org.springframework.http.HttpStatus.NOT_EXTENDED;

/**
 * @author OCD
 * @date 2022/06/16 17:38
 * Description:
 * 全局异常捕获通知
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Value("${ocd.crash}")
    private String crashTitle;

    private GlobalExceptionHandler() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", -1);
        jsonObject.put("statusMsg", "发生异常, 自己看着办.jpg");
        error_result = jsonObject;
    }

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    private EmailService mailService;

    private static JSONObject error_result;

    /**
     * 在 controller 里面内容执行之前, 校验一些参数不匹配啊, Get post方法不对之类
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        System.out.println("错误");
        return new ResponseEntity<>("请求方式/参数类型不匹配", NOT_EXTENDED);
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Object jsonHandler(HttpServletRequest request, Exception e) throws Exception {
        log(e, request);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        //发送邮件
//        mailService.sendSimpleMail("conghaohu@icloud.com,84304811@qq.com", crashTitle + " 异常", sw.toString());
        mailService.sendSimpleMail("conghaohu@icloud.com", crashTitle + " 异常", sw.toString());
        return error_result;
    }

    private void log(Exception ex, HttpServletRequest request) {
        logger.error("************************异常开始*******************************");
        logger.error("客户端 IP: " + getIpAddr(request));
        logger.error("请求地址: " + request.getRequestURL());
        Enumeration enumeration = request.getParameterNames();
        logger.error("请求参数");
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement().toString();
            logger.error(name + "---" + request.getParameter(name));
        }

        StackTraceElement[] error = ex.getStackTrace();
        for (StackTraceElement stackTraceElement : error) {
            logger.error(stackTraceElement.toString());
        }
        logger.error("************************异常结束*******************************");
    }

    /**
     * 从request对象中获取客户端真实的 ip 地址
     *
     * @param request request对象
     * @return 客户端的 IP 地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        // 获取请求主机IP地址,如果通过代理进来，则透过防火墙获取真实IP地址
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        } else if (ip.length() > 15) {
            String[] ips = ip.split(",");
            for (String s : ips) {
                if (!("unknown".equalsIgnoreCase(s))) {
                    ip = s;
                    break;
                }
            }
        }
        return ip;
    }

}