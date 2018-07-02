package cn.haizhi.market.main.service.richard;

import cn.haizhi.market.main.bean.richard.ShopComment;
import cn.haizhi.market.main.bean.richard.ShopCommentExample;
import cn.haizhi.market.main.mapper.richard.ShopCommentMapper;
import cn.haizhi.market.main.view.richard.ShopCommentView;
import cn.haizhi.market.other.exception.ResultException;
import cn.haizhi.market.other.util.BeanUtil;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Date: 2018/1/10
 * Author: Richard
 */

@Service
@Transactional
public class ShopCommentService {

    @Autowired
    private ShopCommentMapper shopCommentMapper;

    public List<ShopCommentView> selectJoin(ShopComment form){
        if(BeanUtil.notNull(form.getPageNum()) && BeanUtil.notNull(form.getPageSize())){
            PageHelper.startPage(form.getPageNum(),form.getPageSize());
        }
        return shopCommentMapper.selectJoin(form);
    }

    public List<ShopComment> selectLot(ShopComment form)throws Exception{
        ShopCommentExample example = new ShopCommentExample();
        ShopCommentExample.Criteria criteria = example.createCriteria();
        if(BeanUtil.notNull(form.getShopId())){
            criteria.andShopIdEqualTo(form.getShopId());
        }
        if(BeanUtil.notNull(form.getPageNum()) && BeanUtil.notNull(form.getPageSize())){
            PageHelper.startPage(form.getPageNum(),form.getPageSize());
        }
        return shopCommentMapper.selectByExample(example);
    }

    public ShopComment selectOne(Long id){
        if(BeanUtil.isNull(id)){
            throw new ResultException("编号不能为空！");
        }
        return shopCommentMapper.selectByPrimaryKey(id);
    }


    public void insertOne(ShopComment form){
        form.setCommentId(BeanUtil.getId());
        shopCommentMapper.insertSelective(form);
    }

    public void updateOne(ShopComment form){
        ShopComment record = this.selectOne(form.getCommentId());
        if(BeanUtil.isNull(record)){
            throw new ResultException("记录不存在！");
        }
        BeanUtil.copyBean(form,record);
        shopCommentMapper.updateByPrimaryKeySelective(record);
    }

    public void deleteOne(Long id){
        if(BeanUtil.isNull(this.selectOne(id))){
            throw new ResultException("记录不存在！");
        }
        shopCommentMapper.deleteByPrimaryKey(id);
    }
}
