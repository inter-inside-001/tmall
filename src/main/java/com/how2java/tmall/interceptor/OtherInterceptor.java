package com.how2java.tmall.interceptor;

import com.how2java.tmall.pojo.Category;
import com.how2java.tmall.pojo.OrderItem;
import com.how2java.tmall.pojo.User;
import com.how2java.tmall.service.CategoryService;
import com.how2java.tmall.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

public class OtherInterceptor extends HandlerInterceptorAdapter{
    @Autowired
    CategoryService categoryService;
    @Autowired
    OrderItemService orderItemService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HttpSession session = request.getSession();
        // 获得分类集合信息，放到搜索栏的下面
        List<Category> cs = categoryService.list();
        session.setAttribute("cs", cs);
        // 这里是获取当前的contextPath:tmall_ssm,用与放在左上角那个变形金刚，
        // 点击之后才能够跳转到首页，否则点击之后也仅仅停留在当前页面
        String contextPath = session.getServletContext().getContextPath();
        session.setAttribute("contextPath", contextPath);
        // 获得购物车中物品的数量
        User user = (User)session.getAttribute("user");
        int cartTotalItemNumber = 0;
        if(user != null){
            List<OrderItem> ois = orderItemService.listByUser(user.getId());
            for(OrderItem oi: ois){
                cartTotalItemNumber+=oi.getNumber();
            }
        }
        session.setAttribute("cartTotalItemNumber", cartTotalItemNumber);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
    }
}
