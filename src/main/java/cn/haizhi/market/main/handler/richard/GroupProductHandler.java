package cn.haizhi.market.main.handler.richard;

import cn.haizhi.market.main.bean.richard.GroupProduct;
import cn.haizhi.market.main.service.richard.GroupProductService;
import cn.haizhi.market.main.view.ResultView;
import cn.haizhi.market.other.util.BeanUtil;
import cn.haizhi.market.other.util.ResultUtil;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Date: 2018/1/9
 * Author: Richard
 */

@RestController
@RequestMapping("/admin")
public class GroupProductHandler {

    @Autowired
    private GroupProductService groupProductService;

    @PostMapping(value = "/groupProduct",produces = "application/json; charset=UTF-8")
    public ResultView insert(@RequestBody GroupProduct form){
        groupProductService.insertOne(form);
        return ResultUtil.returnSuccess();
    }

    @PutMapping(value = "/groupProduct",produces = "application/json; charset=UTF-8")
    public ResultView update(@RequestBody GroupProduct form){
        groupProductService.updateOne(form);
        return ResultUtil.returnSuccess();
    }

    @DeleteMapping(value = "/groupProduct/{id}",produces = "application/json; charset=UTF-8")
    public ResultView delete(@PathVariable("id")Long id){
        groupProductService.deleteOne(id);
        return ResultUtil.returnSuccess();
    }

    @GetMapping(value = "/groupProduct/{id}",produces = "application/json; charset=UTF-8")
    public ResultView getone(@PathVariable("id")Long id){
        return ResultUtil.returnSuccess(groupProductService.selectOne(id));
    }

    @GetMapping(value = "/groupProducts",produces = "application/json; charset=UTF-8")
    public ResultView getall(GroupProduct form) throws Exception {
        if(BeanUtil.notNull(form.getPageNum()) && BeanUtil.notNull(form.getPageSize())){
           return ResultUtil.returnSuccess(new PageInfo<>(groupProductService.selectLot(form)));
        }else{
            return ResultUtil.returnSuccess(groupProductService.selectLot(form));
        }
    }
}
