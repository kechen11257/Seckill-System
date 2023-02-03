package com.kechen.seckill.web;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
public class TestController {

    @ResponseBody
    @RequestMapping("hello")
    public String hello(){
        String result;
        // The resource name can use any string with business semantics,
        // such as method name, interface name or other uniquely identifiable strings.
        // 资源名可使用任意有业务语义的字符串，比如方法名、接口名或其它可唯一标识的字符串。
        try (Entry entry = SphU.entry("HelloResource")){
            // protected business logic 被保护的业务逻辑
            result  = "Hello Sentinel";
            return result;
        }catch (BlockException ex) {
            // Resource access blocked, throttled or downgraded 资源访问阻止，被限流或被降级
            // Do the corresponding processing here 在此处进行相应的处理操作
            log.error(ex.toString());
            result = "The system is busy and try again later";
            return  result;
        }
    }

    /**
     *  定义限流规则
     *  1.创建存放限流规则的集合
     *  2.创建限流规则
     *  3.将限流规则放到集合中
     *  4.加载限流规则
     *  @PostConstruct 当前类的构造函数执行完之后执行
     */
    @PostConstruct
    public void seckillsFlow(){
        //1.Create a collection to store current limiting rules 创建存放限流规则的集合
        List<FlowRule> rules = new ArrayList<>();

        //2.Create throttling rules 创建限流规则
        FlowRule rule = new FlowRule();
        //Define a resource, indicating that sentinel will take effect on that resource
        // 定义资源，表示sentinel会对那个资源生效
        rule.setResource("seckills");
        //Define the current limiting rule type, QPS type 定义限流规则类型,QPS类型
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        //Defines the number of requests passed per second by QPS 定义QPS每秒通过的请求数
        rule.setCount(1);

        FlowRule rule2 = new FlowRule();
        rule2.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule2.setCount(2);
        rule2.setResource("HelloResource");

        //3.Put the throttling rules in the collection 将限流规则放到集合中
        rules.add(rule);
        rules.add(rule2);

        //4.Load the throttling rules 加载限流规则
        FlowRuleManager.loadRules(rules);
    }
}