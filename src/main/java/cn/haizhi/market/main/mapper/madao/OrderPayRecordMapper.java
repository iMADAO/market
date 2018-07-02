package cn.haizhi.market.main.mapper.madao;

import cn.haizhi.market.main.bean.madao.OrderPayRecord;
import cn.haizhi.market.main.bean.madao.OrderPayRecordExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OrderPayRecordMapper {
    long countByExample(OrderPayRecordExample example);

    int deleteByExample(OrderPayRecordExample example);

    int insert(OrderPayRecord record);

    int insertSelective(OrderPayRecord record);

    List<OrderPayRecord> selectByExample(OrderPayRecordExample example);

    int updateByExampleSelective(@Param("record") OrderPayRecord record, @Param("example") OrderPayRecordExample example);

    int updateByExample(@Param("record") OrderPayRecord record, @Param("example") OrderPayRecordExample example);
}