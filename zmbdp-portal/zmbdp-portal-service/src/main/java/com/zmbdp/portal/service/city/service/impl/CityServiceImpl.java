package com.zmbdp.portal.service.city.service.impl;

import com.zmbdp.admin.api.map.domain.vo.RegionVO;
import com.zmbdp.admin.api.map.feign.MapServiceApi;
import com.zmbdp.common.core.utils.BeanCopyUtil;
import com.zmbdp.common.domain.domain.Result;
import com.zmbdp.common.domain.domain.ResultCode;
import com.zmbdp.portal.service.city.domain.vo.CityPageVO;
import com.zmbdp.portal.service.city.service.ICityService;
import com.zmbdp.portal.service.homepage.domain.vo.CityDescVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

/**
 * 城市服务实现类
 *
 * @author 稚名不带撇
 */
@Slf4j
@Service
public class CityServiceImpl implements ICityService {

    /**
     * 异步线程池
     */
    @Autowired
    private Executor threadPoolTaskExecutor;

    /**
     * 远程地图服务 Api
     */
    @Autowired
    private MapServiceApi mapServiceApi;

    /**
     * 查询热门城市与全城市列表
     *
     * @return 热门城市与全城市列表
     */
    @Override
    public CityPageVO getCityPage() {
        // 异步线程执行两个
        // 查询热门城市
        CompletableFuture<List<CityDescVO>> hotCityListFuture = CompletableFuture.supplyAsync(
                this::getHotCityList, threadPoolTaskExecutor
        );
        // 查询城市
        CompletableFuture<Map<String, List<CityDescVO>>> cityPyMapFuture = CompletableFuture.supplyAsync(
                this::getCityPyMap, threadPoolTaskExecutor
        );
        // 等待所有线程执行完成
        // 把所有任务合成成一个总任务
        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(hotCityListFuture, cityPyMapFuture);
        try {
            completableFuture.get(10, TimeUnit.SECONDS); // 等待所有异步操作完成，设置超时时间为 10 秒
        } catch (TimeoutException e) {
            log.error("获取热门城市和拼音分类任务超时！");
            return getFallbackCityPage(); // 返回自定义的数据，用户体验感更好
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢复中断状态
            log.error("获取热门城市和拼音分类任务线程被中断！");
            return getFallbackCityPage();
        } catch (ExecutionException e) {
            log.error("获取热门城市和拼音分类任务执行出错: {}", e.getCause().getMessage());
            return getFallbackCityPage();
        }
        // 等待完成之后获取结果
        List<CityDescVO> hotCityList = hotCityListFuture.join(); // 获取热门城市列表结果
        Map<String, List<CityDescVO>> cityPyMap = cityPyMapFuture.join(); // 获取城市拼音列表结果
        // 组装返回
        CityPageVO cityPageVO = new CityPageVO();
        cityPageVO.setHotCityList(hotCityList);
        cityPageVO.setAllCityMap(cityPyMap);
        return cityPageVO;
    }

    /**
     * 获取热门城市列表
     *
     * @return 热门城市列表
     */
    private List<CityDescVO> getHotCityList() {
        List<CityDescVO> result = new ArrayList<>();
        Result<List<RegionVO>> hotCityList = mapServiceApi.getHotCityList();
        if (
                null == hotCityList ||
                hotCityList.getCode() != ResultCode.SUCCESS.getCode() ||
                null == hotCityList.getData()
        ) {
            log.error("获取热门城市列表失败！");
        } else {
            result = BeanCopyUtil.copyListProperties(hotCityList.getData(), CityDescVO::new);
        }
        return result;
    }

    /**
     * 获取按照拼音分类的城市列表
     *
     * @return 按照拼音分类的城市列表
     */
    private Map<String, List<CityDescVO>> getCityPyMap() {
        Map<String, List<CityDescVO>> result = new HashMap<>();
        Result<Map<String, List<RegionVO>>> cityPyList = mapServiceApi.getCityPyList();
        if (
                null == cityPyList ||
                cityPyList.getCode() != ResultCode.SUCCESS.getCode() ||
                null == cityPyList.getData()
        ) {
            log.error("获取全城市（a-z）映射失败！");
        } else {
            result = BeanCopyUtil.copyMapListProperties(cityPyList.getData(), CityDescVO::new);
        }
        return result;
    }

    /*=============================================    降级处理数据    =============================================*/

    /**
     * 完整的降级页面数据
     *
     * @return 完整的降级页面数据
     */
    private CityPageVO getFallbackCityPage() {
        CityPageVO fallback = new CityPageVO();
        fallback.setHotCityList(getDefaultHotCities());
        fallback.setAllCityMap(getDefaultCityPyMap());
        return fallback;
    }

    /**
     * 自定义热门城市列表（降级数据）
     *
     * @return 自定义的热门城市列表
     */
    private List<CityDescVO> getDefaultHotCities() {
        List<CityDescVO> defaultCities = new ArrayList<>();
        // 主要热门城市
        defaultCities.add(createCityDescVO(35L, "北京", "北京市"));
        defaultCities.add(createCityDescVO(108L, "上海", "上海市"));
        defaultCities.add(createCityDescVO(234L, "广州", "广州市"));
        defaultCities.add(createCityDescVO(236L, "深圳", "深圳市"));
        defaultCities.add(createCityDescVO(289L, "成都", "成都市"));
        defaultCities.add(createCityDescVO(122L, "杭州", "杭州市"));

        return defaultCities;
    }

    /**
     * 自定义拼音分类城市列表（降级数据）
     *
     * @return 自定义的拼音分类城市列表
     */
    private Map<String, List<CityDescVO>> getDefaultCityPyMap() {
        Map<String, List<CityDescVO>> defaultMap = new LinkedHashMap<>();

        // 按拼音首字母分类
        defaultMap.put("A", Arrays.asList(
                createCityDescVO(140L, "安庆", "安庆市"),
                createCityDescVO(189L, "安阳", "安阳市")
        ));

        defaultMap.put("B", Arrays.asList(
                createCityDescVO(35L, "北京", "北京市"),
                createCityDescVO(42L, "保定", "保定市")
        ));

        defaultMap.put("C", Arrays.asList(
                createCityDescVO(289L, "成都", "成都市"),
                createCityDescVO(288L, "重庆", "重庆市"),
                createCityDescVO(2110L, "长沙", "长沙市")
        ));

        defaultMap.put("G", Arrays.asList(
                createCityDescVO(234L, "广州", "广州市"),
                createCityDescVO(257L, "桂林", "桂林市")
        ));

        defaultMap.put("H", Arrays.asList(
                createCityDescVO(122L, "杭州", "杭州市"),
                createCityDescVO(133L, "合肥", "合肥市"),
                createCityDescVO(203L, "武汉", "武汉市")
        ));

        defaultMap.put("S", Arrays.asList(
                createCityDescVO(108L, "上海", "上海市"),
                createCityDescVO(236L, "深圳", "深圳市"),
                createCityDescVO(113L, "苏州", "苏州市")
        ));

        return defaultMap;
    }

    /**
     * 创建城市 VO 对象的辅助方法
     *
     * @param id 城市 id
     * @param name 城市名称
     * @param fullName 城市全名
     * @return 组装好的城市 VO 对象
     */
    private CityDescVO createCityDescVO(Long id, String name, String fullName) {
        CityDescVO vo = new CityDescVO();
        vo.setId(id);
        vo.setName(name);
        vo.setFullName(fullName);
        return vo;
    }
}
