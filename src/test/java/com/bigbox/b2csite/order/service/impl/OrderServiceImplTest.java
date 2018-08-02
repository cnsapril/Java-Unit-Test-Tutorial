package com.bigbox.b2csite.order.service.impl;

import java.util.LinkedList;
import java.util.List;

import org.hibernate.criterion.Order;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.bigbox.b2csite.common.DataAccessException;
import com.bigbox.b2csite.common.ServiceException;
import com.bigbox.b2csite.order.dao.OrderDao;
import com.bigbox.b2csite.order.model.domain.OrderSummary;
import com.bigbox.b2csite.order.model.entity.OrderEntity;
import com.bigbox.b2csite.order.model.transformer.OrderEntityToOrderSummaryTransformer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class OrderServiceImplTest {

	private final static long CUSTOMER_ID = 1;
	
	private OrderServiceImpl target = null;
	
	@Mock
	private OrderDao mockOrderDao;

	@Mock
	private OrderEntityToOrderSummaryTransformer mockTransformer;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		this.target = new OrderServiceImpl();
		this.target.setOrderDao(mockOrderDao);
		this.target.setTransformer(mockTransformer);
	}
	
	@Test
	public void test_getOrderSummary_success() throws Exception {
		OrderEntity orderEntityFixture = new OrderEntity();
		List<OrderEntity> orderEntityFixtureList = new LinkedList<>();
		orderEntityFixtureList.add(orderEntityFixture);
		
		when(mockOrderDao.findOrdersByCustomer(CUSTOMER_ID))
		.thenReturn(orderEntityFixtureList);
		
		OrderSummary orderSummaryFixture = new OrderSummary();
		
		when(mockTransformer.transform(orderEntityFixture))
		.thenReturn(orderSummaryFixture);
		
		// Execution
		List<OrderSummary> result = target.getOrderSummary(CUSTOMER_ID);
		
		// Verification
		verify(mockOrderDao).findOrdersByCustomer(CUSTOMER_ID);
		verify(mockTransformer).transform(orderEntityFixture);
		
		assertNotNull(result);
		assertEquals(1, result.size());
		assertSame(orderSummaryFixture, result.get(0));
		
	}
	
	@Test
	public void test_openNewOrder_successfullyRetriesDataInsert() throws Exception {
		// Setup
		when(mockOrderDao.insert(any(OrderEntity.class)))
				.thenThrow(new DataAccessException("Test Error 1"))
				.thenReturn(1);

		// Execution
		target.openNewOrder(CUSTOMER_ID);

		// Verification
		verify(mockOrderDao, times(2)).insert(any(OrderEntity.class));
	}

	@Test(expected=ServiceException.class)
	public void test_openNewOrder_failedDataInsert() throws Exception {
		// Setup
		when(mockOrderDao.insert(any(OrderEntity.class)))
				.thenThrow(new DataAccessException("Test Error 1"))
				.thenThrow(new DataAccessException("Test Error 2"));

		// Execution
		try {
			target.openNewOrder(CUSTOMER_ID);
		} finally {
			// Verification
			verify(mockOrderDao, times(2)).insert(any(OrderEntity.class));
		}
	}
	
	@Test
	public void test_openNewOrder_success() throws Exception {
		// Setup
		when(mockOrderDao.insert(any(OrderEntity.class))).thenReturn(1);
		
		// Execution
		String orderNumber = target.openNewOrder(CUSTOMER_ID);
		
		// Verification
		ArgumentCaptor<OrderEntity> orderEntityArgumentCaptor = ArgumentCaptor.forClass(OrderEntity.class);
		verify(mockOrderDao).insert(orderEntityArgumentCaptor.capture());

		OrderEntity capturedOrderEntity = orderEntityArgumentCaptor.getValue();
		assertNotNull(capturedOrderEntity);
		assertEquals(CUSTOMER_ID, capturedOrderEntity.getCustomerId());
		assertEquals(orderNumber, capturedOrderEntity.getOrderNumber());
	}
}
