package cn.itcast.core.service.user;

import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.pojo.user.UserQuery;
import cn.itcast.core.util.md5.MD5Util;
import cn.itcast.core.util.poi.ExcelUtil;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.annotation.Resource;
import javax.jms.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private JmsTemplate jmsTemplate;

    @Resource
    private Destination smsDestination;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserDao userDao;

    /**
     * 发送手机验证码
     *
     * @param phone
     */
    @Override
    public void sendCode(final String phone) {
        //随机生成验证码
        String randomNumeric = RandomStringUtils.randomNumeric(6);
        System.out.println(randomNumeric);
        //把生成的验证码保存到redis中
        redisTemplate.boundValueOps(phone).set(randomNumeric);
        //设置验证码可用时间
        redisTemplate.boundValueOps(phone).expire(5, TimeUnit.MINUTES);

        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                //返回值应是一个map
                MapMessage mapMessage = session.createMapMessage();
                //向map中添加数据   封装手机号、短信签名、短信模板、模板参数
                mapMessage.setString("phoneNumbers", phone);
                mapMessage.setString("signName", "阮文");
                mapMessage.setString("templateCode", "SMS_140720901");
                mapMessage.setString("templateParam", "{\\\"code\\\":\\\"\"+code+\"\\\"}");
                return mapMessage;
            }
        });
    }

    /**
     * 用户注册
     *
     * @param smscode
     * @param user
     */
    @Override
    public void add(String smscode, User user) {
        //获取redis中的对应验证码
        String sms = (String) redisTemplate.boundValueOps(user.getPhone()).get();
        //判读页面输入验证码是否正确
        if (smscode != null && !"".equals(smscode) && sms.equals(smscode)) {
            //对密码加密
            String md5Password = MD5Util.MD5Encode(user.getPassword(), null);
            user.setPassword(md5Password);
            user.setCreated(new Date());
            user.setUpdated(new Date());
            userDao.insertSelective(user);
        } else {
            throw new RuntimeException("验证码错误");
        }
    }

    /**
     * 查询所有用户
     *
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult findPage(int page, int rows) {
        //设置分页参数
        PageHelper.startPage(page, rows);
        //查询结果集
        Page<User> page1 = (Page<User>) userDao.selectByExample(null);
        //创建pageResult并填充数据
        return new PageResult(page1.getTotal(), page1.getResult());
    }

    /**
     * 审核用户,也就是修改用户状态
     *
     * @param ids
     * @param status
     */
    @Override
    public void updateStatus(Long[] ids, String status) {
        if (ids != null && ids.length > 0) {
            User user = new User();
            user.setStatus(status);
            for (final Long id : ids) {
                user.setId(id);
                userDao.updateByPrimaryKeySelective(user);
            }
        }
    }

    /**
     * 查询所有用户
     *
     * @return
     */
    @Override
    public List<User> findAllBrand() {
        return userDao.selectByExample(null);
    }

    /**
     * 条件查询
     *
     * @param page
     * @param rows
     * @param user
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, User user) {
        //设置分页参数
        PageHelper.startPage(page, rows);
        //设置查询条件
        UserQuery userQuery = new UserQuery();
        UserQuery.Criteria criteria = userQuery.createCriteria();
        if (user.getUsername() != null && !"".equals(user.getUsername().trim())) {
            criteria.andUsernameLike("%" + user.getUsername().trim() + "%");
        }

        //根據id降序排列
        PageHelper.orderBy("id desc");

        //查询结果集
        Page<User> page1 = (Page<User>) userDao.selectByExample(userQuery);
        //创建pageResult并填充结果集
        return new PageResult(page1.getTotal(), page1.getResult());
    }

    /**
     * 用户数据导出
     */
    @Override
    public void createExcel() throws IOException {
        //查询所有用户
        List<User> users = userDao.selectByExample(null);


        String sheetName = "用户数据表";
        String[] title = {"用户id", "用户名", "密码", "注册手机号", "注册邮箱", "创建时间", "更新时间", "会员来源", "昵称", "真实姓名",
                "使用状态", "QQ号码", "性别", "头像地址", "生日"};


        //获取所需要的数据

        String[][] content = new String[users.size()][];

        for (int i = 0; i < users.size(); i++) {
            content[i] = new String[title.length];
            User user = users.get(i);
            content[i][0] = String.valueOf(user.getId());
            content[i][1] = user.getUsername();
            content[i][2] = user.getPassword();
            content[i][3] = user.getPhone();
            content[i][4] = user.getEmail();
            content[i][5] = user.getCreated().toString();
            content[i][6] = user.getUpdated().toString();
            content[i][7] = user.getSourceType();
            content[i][8] = user.getNickName();
            content[i][9] = user.getName();
            content[i][10] = user.getStatus();
            content[i][11] = user.getQq();
            content[i][12] = user.getSex();
            content[i][13] = user.getHeadPic();
            content[i][14] = String.valueOf(user.getBirthday());
        }

        //填充数据
        Workbook wb = ExcelUtil.CreatExcel(sheetName, title, content, null);

        //创建输出流生成数据表
        FileOutputStream os = new FileOutputStream("G:\\nihao\\用户数据表" + System.currentTimeMillis() + ".xls");
        wb.write(os);
        os.close();
    }

    /**
     * 查询活跃人数
     *
     * @return
     */
    @Override
    public Integer searchActive(){

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String today = format.format(new Date());

        Integer num = 0;
        for (int i = 1; i <= 50; i++) {
            Boolean isActive = redisTemplate.opsForValue().getBit("daily_active_users:" + today, i);
            if (isActive) {
                num++;
            }
        }
        return num;
    }
}
