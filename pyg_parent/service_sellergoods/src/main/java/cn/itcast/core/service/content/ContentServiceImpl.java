package cn.itcast.core.service.content;

import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.ad.ContentQuery;


import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class ContentServiceImpl implements ContentService {

	@Resource
	private ContentDao contentDao;

	@Resource
	private RedisTemplate redisTemplate;

	@Override
	public List<Content> findAll() {
		List<Content> list = contentDao.selectByExample(null);
		return list;
	}

	@Override
	public PageResult findPage(Content content, Integer pageNum, Integer pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<Content> page = (Page<Content>)contentDao.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Transactional
	@Override
	public void add(Content content) {
		clearCache(content.getId());
		contentDao.insertSelective(content);
	}

	@Transactional
	@Override
	public void edit(Content content) {
		Long newCategoryId = content.getCategoryId();
		Long oldCategoryId = contentDao.selectByPrimaryKey(content.getId()).getCategoryId();
		if (newCategoryId != oldCategoryId){
			clearCache(oldCategoryId);
			clearCache(newCategoryId);
		}else {
			clearCache(oldCategoryId);
		}
		contentDao.updateByPrimaryKeySelective(content);
	}

	@Override
	public Content findOne(Long id) {
		Content content = contentDao.selectByPrimaryKey(id);
		return content;
	}

	@Transactional
	@Override
	public void delAll(Long[] ids) {
		if(ids != null){
			for(Long id : ids){
				clearCache(contentDao.selectByPrimaryKey(id).getCategoryId());
				contentDao.deleteByPrimaryKey(id);
			}
		}
	}

	/**
	 * 首页轮播图展示
	 * @param categoryId
	 * @return
	 */
	@Override
	public List<Content> findByCategoryId(Long categoryId) {
		//这是直接查数据库的方法
//		ContentQuery contentQuery = new ContentQuery();
//		contentQuery.createCriteria().andCategoryIdEqualTo(categoryId);
//		return contentDao.selectByExample(contentQuery);
		//==========================================================================================================

		//使用缓存，要注意缓存穿透问题(排队进行二次校验)
		//首先判断缓存中是否有数据
		List<Content> list = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
		//如果没有数据,从数据库中查
		if (list==null){
			synchronized (this){
				list = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
				if (list == null){
					ContentQuery contentQuery = new ContentQuery();
					contentQuery.createCriteria().andCategoryIdEqualTo(categoryId).andStatusEqualTo("1");
					list = contentDao.selectByExample(contentQuery);
					//将数据再放入缓存中
					redisTemplate.boundHashOps("content").put(categoryId,list);
					//设置缓存过期时间
					redisTemplate.boundHashOps("content").expire(1, TimeUnit.DAYS);
				}
			}
		}
		return list;
	}

	//====================================================================================================================
	//清除缓存
	private void clearCache(Long categoryId){
		redisTemplate.boundHashOps("CONTENT").delete(categoryId);
	}
}
