package com.how2java.tmall.controller;

import com.github.pagehelper.PageHelper;
import com.how2java.tmall.pojo.*;
import com.how2java.tmall.service.*;
import com.how2java.tmall.util.*;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import javax.naming.directory.SearchResult;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("")
public class ForeController {
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductService productService;
    @Autowired
    UserService userService;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    PropertyValueService propertyValueService;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    ReviewService reviewService;

    // 主页
    @RequestMapping("forehome")
    public String home(Model model) {
        List<Category> cs= categoryService.list();
        productService.fill(cs);
        productService.fillByRow(cs);
         model.addAttribute("cs", cs);
        return "fore/home";
    }

    // 注册
    @RequestMapping("foreregister")
    public String register(Model model, User user){
        String name = user.getName();
        name = HtmlUtils.htmlEscape(name);
        user.setName(name);
        if(userService.isExist(name)){
            String msg = "用戶名已經被使用，不能使用";
            model.addAttribute("msg", msg);
            model.addAttribute("user", null);
            return "fore/register";
        }
        userService.add(user);
        return "redirect: registerSuccessPage";
    }

    // 登录
    @RequestMapping("forelogin")
    public String login(@RequestParam("name") String name, @RequestParam("password")String password,
                        Model model, HttpSession httpSession){
        name = HtmlUtils.htmlEscape(name);
        User user = userService.get(name, password);

        if(user == null){
            model.addAttribute("msg", "账号密码错误");
            return "fore/login";
        }
        httpSession.setAttribute("user", user);
        return "redirect:forehome";
    }

    // 登出
    @RequestMapping("forelogout")
    public String logout(HttpSession session){
        session.removeAttribute("user");
        return "redirect:forehome";
    }

    // 查看产品
    @RequestMapping("foreproduct")
    public String product(int pid, Model model){
        Product p = productService.get(pid);
        List productSingleImages = productImageService.list(pid, ProductImageService.type_single);
        List productDetailImages = productImageService.list(pid, ProductImageService.type_detail);
        p.setProductSingleImages(productSingleImages);
        p.setProductDetailImages(productDetailImages);
        productService.setSaleAndReviewNumber(p);
        model.addAttribute("p", p);
        // 获得所有的属性值
        List<PropertyValue> pvs = propertyValueService.list(pid);
        List reviews =  reviewService.list(pid);
        model.addAttribute("pvs", pvs);
        model.addAttribute("reviews", reviews);
        return "fore/product";
    }

    // 检查是否登录
    @RequestMapping("forecheckLogin")
    @ResponseBody
    public String checkLogin(HttpSession session){
        User user = (User)session.getAttribute("user");
        return user!=null? "success": "fail";
    }

    // 通过ajax登录
    @RequestMapping("foreloginAjax")
    @ResponseBody
    public String loginAjax(@RequestParam("name") String name, @RequestParam("password") String password,
                            HttpSession httpSession){
        name = HtmlUtils.htmlEscape(name);
        User user = userService.get(name, password);
        if(user == null){
            return "fail";
        }
        httpSession.setAttribute("user", user);
        return "success";
    }

    // 查看分类
    @RequestMapping("forecategory")
    public String category(int cid, String sort, Model model){
        Category category = categoryService.get(cid);
        productService.fill(category);
        productService.setSaleAndReviewNumber(category.getProducts());

        if(null != sort){
            switch (sort){
                case "review":
                    Collections.sort(category.getProducts(), new ProductReviewComparator());
                    break;
                case "date":
                    Collections.sort(category.getProducts(), new ProductDateComparator());
                    break;
                case "saleCount":
                    Collections.sort(category.getProducts(), new ProductSaleCountComparator());
                    break;
                case "price":
                    Collections.sort(category.getProducts(), new ProductPriceComparator());
                    break;
                case "all":
                    Collections.sort(category.getProducts(), new ProductAllComparator());
                    break;
            }
        }
        model.addAttribute("c", category);
        return "fore/category";
    }

    // 搜索
    @RequestMapping("foresearch")
        public String search(String keyword, Model model){
        PageHelper.offsetPage(0,20);
        List<Product> ps = productService.search(keyword);
        productService.setSaleAndReviewNumber(ps);
        model.addAttribute("ps", ps);
        return "fore/searchResult";
    }

    // 添加购物车
    @RequestMapping("foreaddCart")
    @ResponseBody
    public String addCart(int pid, int num, HttpSession httpSession){
        Product product = productService.get(pid);

        User user = (User) httpSession.getAttribute("user");
        boolean found = false;
        List<OrderItem> ois = orderItemService.listByUser(user.getId());
        for(OrderItem oi: ois){
            if(oi.getProduct().getId() == pid){
                oi.setNumber(oi.getNumber() + num);
                orderItemService.update(oi);
                found = true;
                break;
            }
        }

        if(!found){
            OrderItem oi = new OrderItem();
            oi.setUid(user.getId());
            oi.setNumber(num);
            oi.setPid(pid);
            orderItemService.add(oi);
        }
        return "success";
    }

    // 处理添加购物车操作
    @RequestMapping("forebuyone")
    public String buyone(int pid, int num, HttpSession httpSession){
        Product product = productService.get(pid);
        int oiid = 0;

        User user = (User) httpSession.getAttribute("user");
        boolean found = false;
        List<OrderItem> ois = orderItemService.listByUser(user.getId());
        for(OrderItem oi: ois){
            if(oi.getProduct().getId() == pid){
                oi.setNumber(oi.getNumber() + num);
                orderItemService.update(oi);
                found = true;
                oiid = oi.getId();
                break;
            }
        }

        if(!found){
            OrderItem oi = new OrderItem();
            oi.setUid(user.getId());
            oi.setNumber(num);
            oi.setPid(pid);
            orderItemService.add(oi);
            oiid = oi.getId();
        }
        return "redirect:forebuy?oiid=" + oiid;
    }

    // 处理结算操作
    @RequestMapping("forebuy")
    public String buy(Model model, String[] oiid, HttpSession httpSession){
        List<OrderItem> ois = new ArrayList();
        float total = 0;
        for(String strid: oiid){
            int id = Integer.parseInt(strid);
            OrderItem oi = orderItemService.get(id);
            total += oi.getProduct().getPromotePrice()* oi.getNumber();
            ois.add(oi);
        }
        httpSession.setAttribute("ois", ois);
        model.addAttribute("total", total);
        return "fore/buy";
    }

    // 查看购物车
    @RequestMapping("forecart")
    public String cart(Model model, HttpSession httpSession){
        User user = (User) httpSession.getAttribute("user");
        List<OrderItem> orderItems = orderItemService.listByUser(user.getId());
        model.addAttribute("ois", orderItems);
        return "fore/cart";
    }

    //修改订单项中商品的数目
    @RequestMapping("forechangeOrderItem")
    @ResponseBody
    public String changeOrderItem(Model model, HttpSession httpSession, int pid, int number){
        User user = (User) httpSession.getAttribute("user");
        if(user == null)
            return "fail";
        List<OrderItem> ois = orderItemService.listByUser(user.getId());
        for(OrderItem oi: ois){
            if(oi.getProduct().getId() == pid){
                oi.setNumber(number);
                orderItemService.update(oi);
                break;
            }
        }
        return "success";
    }

    // 删掉订单项
    @RequestMapping("foredeleteOrderItem")
    @ResponseBody
    public String deleteOrderItem(Model model, HttpSession httpSession, int oiid){
        User user = (User)  httpSession.getAttribute("user");
        if(user == null)
            return "fail";
        orderItemService.delete(oiid);
        return "success";
    }

    // 查看订单页
    @RequestMapping("forebought")
    public String bought(Model model, HttpSession httpSession){
        User user = (User) httpSession.getAttribute("user");
        List<Order> os = orderService.list(user.getId(), OrderService.delete);
        orderItemService.fill(os);
        model.addAttribute("os", os);
        return "fore/bought";
    }

    // 生成订单
    @RequestMapping("forecreateOrder")
    public String createOrder(Model model, Order order, HttpSession httpSession){
        User user = (User)httpSession.getAttribute("user");
        String orderCode = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) +
                RandomUtils.nextInt(10000);
        order.setOrderCode(orderCode);
        order.setCreateDate(new Date());
        order.setUid(user.getId());
        order.setStatus(OrderService.waitPay);
        List<OrderItem> ois = (List<OrderItem>) httpSession.getAttribute("ois");
        float total = orderService.add(order, ois);
        return "redirect:forealipay?oid="+order.getId() +"&total=" + total;
    }

    // 支付操作
    @RequestMapping("forepayed")
    public String payed(int oid, float total, Model model){
        Order order = orderService.get(oid);
        order.setStatus(OrderService.waitDelivery);
        order.setPayDate(new Date());
        orderService.update(order);
        model.addAttribute("o", order);
        return "fore/payed";
    }

    // 确认收货页面
    @RequestMapping("foreconfirmPay")
    public String confirmPay(Model model, int oid){
        Order o = orderService.get(oid);
        orderItemService.fill(o);
        model.addAttribute("o", o);
        return "fore/confirmPay";
    }

    // 确认收货
    @RequestMapping("foreorderConfirmed")
    public String orderConfirmed(Model model, int oid){
        Order o = orderService.get(oid);
        o.setStatus(OrderService.waitReview);
        o.setConfirmDate(new Date());
        orderService.update(o);
        return "fore/orderConfirmed";
    }

    // 删除订单
    @RequestMapping("foredeleteOrder")
    @ResponseBody
    public String deleteOrder(Model model, int oid){
        Order o = orderService.get(oid);
        o.setStatus(OrderService.delete);
        orderService.update(o);
        return "success";
    }

    // 查看评论
    @RequestMapping("forereview")
    public String review(Model model, int oid){
        Order o = orderService.get(oid);
        orderItemService.fill(o);
        Product p = o.getOrderItems().get(0).getProduct();
        List<Review> reviews = reviewService.list(p.getId());
        productService.setSaleAndReviewNumber(p);
        model.addAttribute("p", p);
        model.addAttribute("o", o);
        model.addAttribute("reviews", reviews);
        return "fore/review";
    }

    // 提交评论
    @RequestMapping("foredoreview")
    public String doreview(Model model, HttpSession httpSession, @RequestParam("oid") int oid,
                           @RequestParam("pid") int pid, String content){
        Order o = orderService.get(oid);
        o.setStatus(OrderService.finish);
        orderService.update(o);

        Product p = productService.get(pid);
        content = HtmlUtils.htmlEscape(content);

        User user = (User) httpSession.getAttribute("user");
        Review review = new Review();
        review.setContent(content);
        review.setPid(pid);
        review.setCreateDate(new Date());
        review.setUid(user.getId());
        reviewService.add(review);

        return "redirect:forereview?oid="+oid+"&showonly=true";
    }
}
