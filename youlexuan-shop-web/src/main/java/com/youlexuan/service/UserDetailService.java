package com.youlexuan.service;

import com.youlexuan.pojo.TbSeller;
import com.youlexuan.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailService implements UserDetailsService {


    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    /**
     * 完成认证功能
     * @param username 前台获取到的用户名
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        List<GrantedAuthority> grantedAutho = new ArrayList<>();
        grantedAutho.add(new SimpleGrantedAuthority("ROLE_USER"));

        TbSeller seller = sellerService.findOne(username);
        if(seller!=null&&"1".equals(seller.getStatus())){
            return new User(username,seller.getPassword(),grantedAutho);
        }

        return  null;

    }
}
