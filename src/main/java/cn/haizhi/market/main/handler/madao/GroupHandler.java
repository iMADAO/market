package cn.haizhi.market.main.handler.madao;

import cn.haizhi.market.main.bean.madao.GroupDTO;
import cn.haizhi.market.main.bean.madao.PgGroup;
import cn.haizhi.market.main.service.madao.GroupService;
import cn.haizhi.market.main.view.ResultView;
import cn.haizhi.market.other.enums.madao.ErrorEnum;
import cn.haizhi.market.other.enums.madao.GroupAddForm;
import cn.haizhi.market.other.enums.madao.GroupCreateForm;
import cn.haizhi.market.other.exception.MadaoException;
import cn.haizhi.market.other.util.FormErrorUtil;
import cn.haizhi.market.other.util.ResultUtil;
import cn.haizhi.market.other.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@RestController
public class GroupHandler {
    @Autowired
    private GroupService groupService;

    //新建购物组
    @PostMapping("/group")
    public ResultView createGroup(@Valid @RequestBody GroupCreateForm form, BindingResult bindingResult, HttpSession session){
        if(bindingResult.hasErrors()){
            throw new MadaoException(ErrorEnum.PARAM_ERROR, FormErrorUtil.getFormErrors(bindingResult));
        }
        PgGroup group = null;
        Long userId = UserUtil.getUserId(session);
        if (form.getOrderId()==null || form.getOrderId()=="") {
            group = groupService.createGroup(userId, form.getOrderId());
        }else {
            group = groupService.createGroup(userId, form.getOrderId());
        }

        return ResultUtil.returnSuccess(group);
    }

    @PostMapping("/group/empty")
    public ResultView createEmptyGroup(){
        PgGroup group = groupService.createBlankGroup();
        return ResultUtil.returnSuccess(group.getGroupId());
    }

    //加入购物组
    @PutMapping("/group")
    public ResultView addGroup(@Valid @RequestBody GroupAddForm form, BindingResult bindingResult, HttpSession session){
        if(bindingResult.hasErrors()){
            throw new MadaoException(ErrorEnum.PARAM_ERROR, FormErrorUtil.getFormErrors(bindingResult));
        }
        PgGroup group = groupService.addToGroup(UserUtil.getUserId(session), form.getOrderId(),form.getGroupId());
        return ResultUtil.returnSuccess(group);
    }

    //根据订单查询购物组
    @GetMapping("/{orderId}/group")
    public ResultView getGroupByOrder(@PathVariable(name = "orderId") String orderId, HttpSession session){
        GroupDTO groupDTO = groupService.getGroupByOne(UserUtil.getUserId(session), orderId);
        return ResultUtil.returnSuccess(groupDTO);
    }


    //查询购物组
    @GetMapping("/{groupSatus}/{activeStatus}/group/list")
    public ResultView getGroup(@PathVariable("groupSatus") Byte groupStatus, @PathVariable("activeStatus") Byte activeStatus){
        List<GroupDTO> groupDTOList = groupService.getGroupList(groupStatus, activeStatus);
        return ResultUtil.returnSuccess(groupDTOList);
    }
}
