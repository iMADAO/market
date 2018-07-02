package cn.haizhi.market.main.task;

import cn.haizhi.market.main.service.madao.GroupService;
import cn.haizhi.market.main.service.madao.OrderService;
import cn.haizhi.market.main.service.madao.PgOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TimerTask {

    @Autowired
    GroupService groupService;

    @Autowired
    PgOrderService pgOrderService;

    @Autowired
    OrderService orderService;

    //每天凌晨0点03分0秒执行，可用于完结过期的拼购组  检查订单是否应该完结
    @Scheduled(cron = "0 3 0 * * ?")
    public void updatePgGroup(){
        System.out.println("---------------------------------------完结过期的拼购组");
        groupService.updatePgGroup();

        System.out.println("---------------------------------------完结过期的订单");
        orderService.updateOrderStatus();
        pgOrderService.updatePgOrderStatus();
    }

//    //每天0点开始每个一个小时的执行一次，用于检查订单是否应该完结
//    @Scheduled(cron = "0 41 22 * * ?")
//    public void updateOrderStatus(){
//
//    }
}
