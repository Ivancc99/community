package com.donghua.community.dao;

import org.springframework.stereotype.Repository;

@Repository
public class AlphaDaoHibernateImp implements AlphaDao {
    @Override
    public String select() {
        return "hibernate";
    }
}
