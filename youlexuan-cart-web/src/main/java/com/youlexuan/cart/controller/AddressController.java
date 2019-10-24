package com.youlexuan.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.youlexuan.pojo.TbAddress;
import com.youlexuan.user.service.AddressService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {


    @Reference
    private AddressService addressService;

    @RequestMapping("/findListByUserId")
    public List<TbAddress> findListByUserId(){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return addressService.findListByUserId(userId);
    }
}
