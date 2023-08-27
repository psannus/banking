package com.example.test.data;

import com.example.test.model.Transaction;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TransactionMapper {
    @Select("SELECT * FROM transaction WHERE account_id = #{accountId}")
    @Results({
            @Result(property = "accountId", column = "account_id")
    })
    List<Transaction> getTransactionsByAccountId(@Param("accountId") Long accountId);

    @Insert("INSERT INTO transaction (account_id, amount, currency, direction, description)" +
            " VALUES (#{accountId}, #{amount}, #{currency}, #{direction}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Boolean insertTransaction(Transaction transaction);
}
