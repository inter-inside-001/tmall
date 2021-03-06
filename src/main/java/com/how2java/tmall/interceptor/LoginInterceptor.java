package com.how2java.tmall.interceptor;

import com.github.pagehelper.util.StringUtil;
import com.how2java.tmall.pojo.User;
import com.how2java.tmall.service.CategoryService;
import com.how2java.tmall.service.OrderItemService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;

public class LoginInterceptor extends HandlerInterceptorAdapter{
    @Autowired
    CategoryService categoryService;
    @Autowired
    OrderItemService orderItemService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession();
        String contextPath = session.getServletContext().getContextPath();
        String[] noNeedAuthPage = new String[]{"home", "checkLogin", "register", "loginAjax", "login","product", "category", "search"};

        String uri = request.getRequestURI();
        uri = StringUtils.remove(uri, contextPath);
        if(uri.startsWith("/fore")){
            String method = StringUtils.substringAfterLast(uri, "/fore");
            if(!Arrays.asList(noNeedAuthPage).contains(method)){
                User user = (User) session.getAttribute("user");
                if(user == null){
                    response.sendRedirect("loginPage");
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
    }
}
