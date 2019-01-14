package cn.itcast.core;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath*:spring/applicationContext*.xml"})
//public class BrandDaoTest {
//
//    @Autowired
//    private BrandDao brandDao;
//
//
//    @Test
//    public void testFindBrandById(){
//        Brand brand = brandDao.selectByPrimaryKey(1L);
//        System.out.println("=================="+brand);
//    }
//
//    @Test
//    public void testFindAllBrand(){
//        List<Brand> brandList = brandDao.selectByExample(null);
//        System.out.println(brandList);
//    }
//
//    @Test
//    public void testFindBrandByWhere(){
//
//        BrandQuery brandQuery = new BrandQuery();
//        //设置查询的字段名，如果不写默认是*查询所有
//        brandQuery.setFields("id,name");
//        //不设置默认是false不去重
//        brandQuery.setDistinct(true);
//        //设置排序，设置根据id降序排序
//        brandQuery.setOrderByClause("id desc");
//
//        //创建where查询条件对象
//        BrandQuery.Criteria criteria = brandQuery.createCriteria();
//        //查询id等于1的
//        criteria.andIdEqualTo(1L);
//        //根据name字段模糊查询
//        criteria.andNameLike("%联%");
//        //根据首字母字段模糊查询
//        criteria.andFirstCharLike("%L%");
//
//        //开始查询
//        List<Brand> brandList = brandDao.selectByExample(brandQuery);
//        System.out.println(brandList);
//    }

//}
