package com.kechen.seckill.web;

import com.alibaba.fastjson.JSON;
import com.kechen.seckill.db.dao.OrderDao;
import com.kechen.seckill.db.dao.SeckillActivityDao;
import com.kechen.seckill.db.dao.SeckillCommodityDao;
import com.kechen.seckill.db.po.Order;
import com.kechen.seckill.db.po.SeckillActivity;
import com.kechen.seckill.db.po.SeckillCommodity;
import com.kechen.seckill.services.RedisService;
import com.kechen.seckill.services.SeckillActivityService;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class SeckillActivityController {
    @Resource
    private SeckillActivityDao seckillActivityDao;

    @Resource
    private SeckillCommodityDao seckillCommodityDao;

    @Resource
    private SeckillActivityService seckillActivityService;

    @Resource
    private OrderDao orderDao;

    @Resource
    RedisService redisService;

    /**
     * 查询秒杀活动的列表
     *
     * @param resultMap
     * @return
     */
    @RequestMapping("/seckills")
    public String sucessTest(Map<String,Object> resultMap){
        List<SeckillActivity> seckillActivities = seckillActivityDao.querySeckillActivitysByStatus(1);
        resultMap.put("seckillActivities",seckillActivities);
        return "seckill_activity";
    }

    /**
     * 秒杀商品详情 details of seckill commodity
     * @param resultMap
     * @param seckillActivityId
     * @return

    @RequestMapping("/item/{seckillActivityId}")
    public String itemPage(Map<String,Object> resultMap,@PathVariable long seckillActivityId){
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        SeckillCommodity seckillCommodity = seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());

        resultMap.put("seckillActivity",seckillActivity);
        resultMap.put("seckillCommodity",seckillCommodity);
        resultMap.put("seckillPrice",seckillActivity.getSeckillPrice());
        resultMap.put("oldPrice",seckillActivity.getOldPrice());
        resultMap.put("commodityId",seckillActivity.getCommodityId());
        resultMap.put("commodityName",seckillCommodity.getCommodityName());
        resultMap.put("commodityDesc",seckillCommodity.getCommodityDesc());
        return "seckill_item";
    }
     */
    /**
     * 秒杀商品详情
     * @param resultMap
     * @param seckillActivityId
     * @return
     */

    @RequestMapping("/item/{seckillActivityId}")
    public String itemPage(Map<String, Object> resultMap, @PathVariable long seckillActivityId) {
        SeckillActivity seckillActivity;
        SeckillCommodity seckillCommodity;

        String seckillActivityInfo = redisService.getValue("seckillActivity:" + seckillActivityId);
        if (StringUtils.isNotEmpty(seckillActivityInfo)) {
            log.info("redis cache data:" + seckillActivityInfo);
            seckillActivity = JSON.parseObject(seckillActivityInfo, SeckillActivity.class);
        } else {
            seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        }

        String seckillCommodityInfo = redisService.getValue("seckillCommodity:" + seckillActivity.getCommodityId());
        if (StringUtils.isNotEmpty(seckillCommodityInfo)) {
            log.info("redis cache data:" + seckillCommodityInfo);
            seckillCommodity = JSON.parseObject(seckillActivityInfo, SeckillCommodity.class);
        } else {
            seckillCommodity = seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());
        }

        resultMap.put("seckillActivity", seckillActivity);
        resultMap.put("seckillCommodity", seckillCommodity);
        resultMap.put("seckillPrice", seckillActivity.getSeckillPrice());
        resultMap.put("oldPrice", seckillActivity.getOldPrice());
        resultMap.put("commodityId", seckillActivity.getCommodityId());
        resultMap.put("commodityName", seckillCommodity.getCommodityName());
        resultMap.put("commodityDesc", seckillCommodity.getCommodityDesc());
        return "seckill_item";
    }

    //    @ResponseBody
    @RequestMapping("/addSeckillActivityAction")
    public String addSeckillActivityAction(
            @RequestParam("name") String name,
            @RequestParam("commodityId") long commodityId,
            @RequestParam("seckillPrice") BigDecimal seckillPrice,
            @RequestParam("oldPrice") BigDecimal oldPrice,
            @RequestParam("seckillNumber") long seckillNumber,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            Map<String, Object> resultMap
    ) throws ParseException, ParseException {
        startTime = startTime.substring(0, 10) +  startTime.substring(11);
        endTime = endTime.substring(0, 10) +  endTime.substring(11);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-ddhh:mm");
        SeckillActivity seckillActivity = new SeckillActivity();
        seckillActivity.setName(name);
        seckillActivity.setCommodityId(commodityId);
        seckillActivity.setSeckillPrice(seckillPrice);
        seckillActivity.setOldPrice(oldPrice);
        seckillActivity.setTotalStock(seckillNumber);
        seckillActivity.setAvailableStock(Integer.valueOf("" + seckillNumber));
        seckillActivity.setLockStock(0L);
        seckillActivity.setActivityStatus(1);
        seckillActivity.setStartTime(format.parse(startTime));
        seckillActivity.setEndTime(format.parse(endTime));
        seckillActivityDao.inertSeckillActivity(seckillActivity);
        resultMap.put("seckillActivity", seckillActivity);
        return "add_success";
    }

    /**
     * 跳转发布页
     * @return
     */
    @RequestMapping("/addSeckillActivity")
    public String addSeckillActivity(){
        return "add_activity";
    }


    /**
     * 处理抢购请求
     * @param userId
     * @param seckillActivityId
     * @return
     */
    @ResponseBody
    @RequestMapping("/seckill/buy/{userId}/{seckillActivityId}")
    public ModelAndView seckillCommodity(@PathVariable long userId, @PathVariable long seckillActivityId) {
        boolean stockValidateResult = false;

        ModelAndView modelAndView = new ModelAndView();
        try {
            /*
             * Determine whether the user is in the purchased list 判断用户是否在已购名单中
             */
            if (redisService.isInLimitMember(seckillActivityId, userId)) {
                // Prompt that the user is already in the purchase restriction list, and return the result
                // 提示用户已经在限购名单中，返回结果
                modelAndView.addObject("resultInfo", "Sorry, you are already on the purchase restriction list");
                modelAndView.setViewName("seckill_result");
                //Add user to purchased list 添加用户到已购名单中
                return modelAndView;
            }

            /*
             * Confirm whether it can be spiked 确认是否能够进行秒杀
             */
            stockValidateResult = seckillActivityService.seckillStockValidator(seckillActivityId);
            if (stockValidateResult) {
                Order order = seckillActivityService.createOrder(seckillActivityId, userId);
                modelAndView.addObject("resultInfo","The seckill is successful, the order is being created, the order ID：" + order.getOrderNo());
                modelAndView.addObject("orderNo",order.getOrderNo());
                redisService.addLimitMember(seckillActivityId, userId);
            } else {
                modelAndView.addObject("resultInfo","Sorry, the item is out of stock");
            }
        } catch (Exception e) {
            log.error("The seckill system is abnormal" + e.toString());
            modelAndView.addObject("resultInfo","Spike failed");
        }
        modelAndView.setViewName("seckill_result");
        return modelAndView;
    }

    /**
     * 订单查询
     * @param orderNo
     * @return
     */
    @RequestMapping("/seckill/orderQuery/{orderNo}")
    public ModelAndView orderQuery(@PathVariable String orderNo) {
        log.info("order inquiry, order number：" + orderNo);
        Order order = orderDao.queryOrder(orderNo);
        ModelAndView modelAndView = new ModelAndView();

        if (order != null) {
            modelAndView.setViewName("order");
            modelAndView.addObject("order", order);
            SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(order.getSeckillActivityId());
            modelAndView.addObject("seckillActivity", seckillActivity);
        } else {
            modelAndView.setViewName("order_wait");
        }
        return modelAndView;
    }

    /**
     * 订单支付
     * @return
     */
    @RequestMapping("/seckill/payOrder/{orderNo}")
    public String payOrder(@PathVariable String orderNo) throws Exception {
        seckillActivityService.payOrderProcess(orderNo);
        return "redirect:/seckill/orderQuery/" + orderNo;
    }

    /**
     * 获取当前服务器端的时间
     * @return
     */
    @ResponseBody
    @RequestMapping("/seckill/getSystemTime")
    public String getSystemTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//set date format
        String date = df.format(new Date());// new Date():get the current system time
        return date;
    }
}
