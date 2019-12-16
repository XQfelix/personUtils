package com.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.web.dao.UserMapper;
import com.web.pojo.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * @author GQ.Yin
 * @version 1.0
 * @title
 * @date 2019/12/16 13:16
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MybatisTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void select(){
        List<User> users = userMapper.selectList(null);
        System.out.println(users);
    }

    @Test
    public void insert(){
        User user = new User();
        user.setAge(31);
        user.setManagerId(1088250446457389058L);
        user.setCreateTime(LocalDateTime.now());
        int insert = userMapper.insert(user);
        System.out.println("影像记录数："+insert);
    }


    /**
     * 查询名字中包含'雨'并且年龄小于40
     * where name like '%雨%' and age < 40
     */
    @Test
    public void selectByWrapper(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name","雨").lt("age",40);
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }

    /**
     * 创建日期为2019年2月14日并且直属上级姓名为王姓
     * date_format(create_time,'%Y-%m-%d') and manager_id in (select id from user where name like '王%')
     * 下面的日期查询使用的是占位符的形式进行查询，目的就是为了防止SQL注入的风险。
     */
    @Test
    public void selectByWrapper2(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.apply("date_format(create_time,'%Y-%m-%d')={0}","2019-02-14")
                .inSql("manager_id","select id from user where name like '王%'");
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }


    /**
     * 名字为王姓，（年龄小于40或者邮箱不为空）
     */
    @Test
    public void selectByWrapper3(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("name","王").and(wq-> wq.lt("age",40).or().isNotNull("email"));

        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }


    /**
     * 名字为王姓，（年龄小于40，并且年龄大于20，并且邮箱不为空）
     */
    @Test
    public void selectWrapper4(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("name", "王").and(wq -> wq.between("age", 20, 40).and(wqq -> wqq.isNotNull("email")));
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }


    /**
     * （年龄小于40或者邮箱不为空）并且名字为王姓
     * （age<40 or email is not null）and name like '王%'
     */
    @Test
    public void selectWrapper5(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.nested(wq->wq.lt("age",40).or().isNotNull("email")).likeRight("name","王");

        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }


    /**
     * 年龄为30,31,35,34的员工
     */
    @Test
    public void selectWrapper6(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("age", Arrays.asList(30,31,34,35));

        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }


    /**
     * 只返回满足条件的一条语句即可
     * limit 1
     */
    @Test
    public void selectWrapper7(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("age", Arrays.asList(30,31,34,35)).last("limit 1");

        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }


    /**
     * 查找为王姓的员工的姓名和年龄
     */
    @Test
    public void selectWrapper8(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("name","age").likeRight("name","王");
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }


    /**
     * 查询所有员工信息除了创建时间和员工ID列
     */
    @Test
    public void selectWrapper9(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(User.class,info->!info.getColumn().equals("create_time")
                &&!info.getColumn().equals("manager_id"));
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }



    /**
     * 实体作为条件构造方法的参数
     * */
    @Test
    public void selectWrapper10(){
        User user = new User();
        user.setName("刘红雨");
        user.setAge(32);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(user);
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }



    /**
     * Lambda条件构造器
     * 查询名字中包含‘雨’并且年龄小于40的员工信息
     * */
    @Test
    public void lambdaSelect(){
        LambdaQueryWrapper<User> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.like(User::getName,"雨").lt(User::getAge,40);

        List<User> userList = userMapper.selectList(lambdaQueryWrapper);
        userList.forEach(System.out::println);
    }

    /**QueryWrapper类已经提供了很强大的功能，而lambda条件构造器做的和QueryWrapper的事也是相同的为什么要冗余的存在lambda条件构造器呢？
     QueryWrapper是通过自己写表中相应的属性进行构造where条件的，容易发生拼写错误，在编译时不会报错，只有运行时才会报错，而lambda条件构造器是通过调用实体类中的方法，如果方法名称写错，直接进行报错，所以lambda的纠错功能比QueryWrapper要提前很多。
     */


    /**自定义SQL*/
    @Test
    public void selectAllCustom(){
        List<User> userList = userMapper.selectAll();
        userList.forEach(System.out::println);
    }


    /**
     * 查询年龄大于20 的用户信息，并以每页容量为两条分页的形式返回*/
    @Test
    public void selectPage(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("age",20);

        //设置当前页和页容量(这种方式会查询总数)
        Page<User> page = new Page<>(1, 2);

        //当第三个参数设置为false时, 不会查询总记录
//        Page<User> page = new Page<>(1, 2, false);

        IPage<User> userIPage = userMapper.selectPage(page, queryWrapper);

        System.out.println("总页数："+userIPage.getPages());
        System.out.println("总记录数："+userIPage.getTotal());
        userIPage.getRecords().forEach(System.out::println);
    }



    /**
     * 使用UpdateWrapper更新数据(相当于使用联合主键)*/
    @Test
    public void updateTest2(){
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("name","李艺伟").eq("age",26);

        User user = new User();
        user.setEmail("update2@email");
        int rows = userMapper.update(user, updateWrapper);
        System.out.println(rows);
    }

    /**
     * 当我们更新少量用户信息的时候，可以不用创建对象，直接通过调用set方法更新属性即可
     * */
    @Test
    public void updateTest3(){
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("name","李艺伟").eq("age",26).set("email","update3@email.com");
        userMapper.update(null,updateWrapper);
    }


    /**
     * 使用lambda更新数据
     * */
    @Test
    public void updateByLambda(){
        LambdaUpdateWrapper<User> lambdaUpdateWrapper = Wrappers.lambdaUpdate();
        lambdaUpdateWrapper.eq(User::getName,"李艺伟").eq(User::getAge,26).set(User::getAge,27);
        userMapper.update(null,lambdaUpdateWrapper);
    }



    /**
     * 关于乐观锁+自旋实现
     * */
    @Test
    public void updateCAS(){
        User user = userMapper.selectById("1087982257332887553");
        if (user == null) {
            return;
        }
        user.setName("付付付付付asdfsdfadfs付付付付");
        int ret = userMapper.updateUser(user);
        System.out.println("------------" + ret);
        if (ret == 0) {//递归
            updateCAS();
        }
    }


    /**
     * 关于悲观锁实现
     * 使用@Transactional注解申明事务
     * */
    @Test
    @Transactional
    public void updateLock(){
        User user = userMapper.selectById("1087982257332887553");
        if (user == null) {
            return;
        }
        user.setName("付付付付付asdfsdfadfs付付付付");
        int ret = userMapper.updateUserNoSafe(user);
        System.out.println("------------" + ret);
    }

}
