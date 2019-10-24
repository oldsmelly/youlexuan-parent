package com.youlexuan.user.controller;
import java.util.List;

import org.springframework.security.config.annotation.web.configurers.ChannelSecurityConfigurer;
import org.springframework.web.bind.EscapedErrors;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.youlexuan.pojo.TbUser;
import com.youlexuan.user.service.UserService;

import com.youlexuan.entity.PageResult;
import com.youlexuan.entity.Result;
/**
 * 用户表controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Reference
	private UserService userService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbUser> findAll(){			
		return userService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return userService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param user
	 * @return
	 * 1、验证，当输入的验证码和填写的验证码一直才做入库的操作
	 *
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbUser user,String iCode){
		try {
			//验证 验证码
			if(userService.checkSmsCode(user.getPhone(),iCode)){
				userService.add(user);
				return new Result(true, "增加成功");
			}

			return new Result(false,"您输入的验证码有误");


		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param user
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbUser user){
		try {
			userService.update(user);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public TbUser findOne(Long id){
		return userService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			userService.delete(ids);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param brand
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbUser user, int page, int rows  ){
		return userService.findPage(user, page, rows);		
	}

	@RequestMapping("/sendSms")
	public Result sendSms(String phone){
		try {
			userService.creatAndSendCode(phone);
			return  new Result(true,"发送成功");
		}catch (Exception e){
			e.printStackTrace();
			return new Result(false,e.toString());
		}

	}
	
}
