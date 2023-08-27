package com.example.test.data;

import com.example.test.model.Account;
import com.example.test.model.Balance;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AccountMapper {
    @Select("SELECT * FROM account WHERE id = #{id}")
    @Results(value = {
        @Result(property = "id", column = "id"),
        @Result(property = "customerId", column = "customer_id"),
        @Result(property = "countryCode", column = "country_code"),
        @Result(property = "balances", javaType = List.class, column = "id", many=@Many(select = "getBalancesByAccountId"))
    })
    Account getAccountById(@Param("id") Long id);

    @Select("SELECT * FROM balance WHERE account_id = #{accountId}")
    @Results(value = {
        @Result(property = "id", column = "id"),
        @Result(property = "accountId", column = "account_id"),
        @Result(property = "currency", column = "currency"),
        @Result(property = "amount", column = "amount")
    })
    List<Balance> getBalancesByAccountId(@Param("id") Long accountId);

    @Insert("INSERT INTO account (customer_id, country_code) VALUES(#{customerId}, #{countryCode})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Boolean insertAccount(Account account);

    @Insert("INSERT INTO balance (account_id, currency, amount) VALUES(#{accountId}, #{currency}, #{amount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Boolean insertBalance(Balance balance);

    @Update("UPDATE balance SET amount = #{amount} WHERE id = #{id}")
    Boolean updateBalance(Balance currencyBalance);
}
