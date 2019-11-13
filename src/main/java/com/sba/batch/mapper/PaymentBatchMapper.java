package com.sba.batch.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.sba.batch.model.BatchPayment;

@Mapper
public interface PaymentBatchMapper {
	
	@Select("SELECT id, courseId, cost/fee*100 as schedule,startDate, endDate, fee FROM sba_payment.payment where cost/fee*100 != 100")
	List<BatchPayment> batchpayment();
	// update pahse
	@Update("update sba_payment.payment set cost=fee*#{phase} where courseid = #{id}")
	void updatePayment(@Param("id") Integer id,@Param("phase") Float phase);

}
