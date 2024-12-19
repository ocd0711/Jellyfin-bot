package com.ocd.controller.config;

import com.alibaba.fastjson.JSON;
import com.ocd.bean.dto.base.Result;
import com.ocd.bean.dto.base.StatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    static final String NO_INTERCEPTOR_PATH = ".*/().*";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (uri.matches(NO_INTERCEPTOR_PATH)) {
            return true;
        }
        String token = request.getParameter("token");
        if (token == null) {
            response.setCharacterEncoding("utf-8");
            PrintWriter pw = response.getWriter();
            Result result = new Result(StatusCode.LOGINERROR, "请传入token");
            pw.write(JSON.toJSONString(result));
            pw.flush();
            pw.close();
            return false;
        }
        return true;
    }

}
