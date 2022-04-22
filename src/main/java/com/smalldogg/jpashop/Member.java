package com.smalldogg.jpashop;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.Setter;
import lombok.Getter;

@Entity
@Getter @Setter
public class Member {
  @Id @GeneratedValue
  private long id;
  private String username;

}
