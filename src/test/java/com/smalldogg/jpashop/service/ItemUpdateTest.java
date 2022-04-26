package com.smalldogg.jpashop.service;

import com.smalldogg.jpashop.domain.item.Book;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ItemUpdateTest {

  @Autowired
  EntityManager em;

  @Test
  public void updateTest() throws Exception {
    Book book = em.find(Book.class, 1L);

    //TX
    book.setName("aefaw");//변경감지(dirty checking)

    //TX commit
  }
}
