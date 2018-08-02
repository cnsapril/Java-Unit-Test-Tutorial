package com.bigbox.b2csite.order.dao;

import java.util.List;

import com.bigbox.b2csite.common.DataAccessException;
import com.bigbox.b2csite.order.model.entity.OrderEntity;

public interface OrderDao {

	OrderEntity findById(long id) throws DataAccessException;
	int insert(OrderEntity order) throws DataAccessException;
	
	List<OrderEntity> findOrdersByCustomer(long customerId) throws DataAccessException;
}