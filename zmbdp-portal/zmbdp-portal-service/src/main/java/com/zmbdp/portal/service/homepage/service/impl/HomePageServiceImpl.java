package com.zmbdp.portal.service.homepage.service.impl;

import com.zmbdp.admin.api.house.domain.dto.SearchHouseListReqDTO;
import com.zmbdp.admin.api.house.domain.vo.HouseDetailVO;
import com.zmbdp.admin.api.house.feign.HouseServiceApi;
import com.zmbdp.admin.api.map.domain.dto.LocationReqDTO;
import com.zmbdp.admin.api.map.domain.vo.RegionCityVo;
import com.zmbdp.admin.api.map.feign.MapServiceApi;
import com.zmbdp.common.core.utils.BeanCopyUtil;
import com.zmbdp.common.core.utils.JsonUtil;
import com.zmbdp.common.domain.domain.Result;
import com.zmbdp.common.domain.domain.ResultCode;
import com.zmbdp.common.domain.domain.vo.BasePageVO;
import com.zmbdp.common.domain.exception.ServiceException;
import com.zmbdp.portal.service.homepage.domain.dto.CityDescDTO;
import com.zmbdp.portal.service.homepage.domain.dto.DictDataDTO;
import com.zmbdp.portal.service.homepage.domain.dto.HouseListReqDTO;
import com.zmbdp.portal.service.homepage.domain.dto.PullDataListReqDTO;
import com.zmbdp.portal.service.homepage.domain.vo.CityDescVO;
import com.zmbdp.portal.service.homepage.domain.vo.DictVO;
import com.zmbdp.portal.service.homepage.domain.vo.HouseDescVO;
import com.zmbdp.portal.service.homepage.domain.vo.PullDataListVO;
import com.zmbdp.portal.service.homepage.service.IDictionaryService;
import com.zmbdp.portal.service.homepage.service.IHomePageService;
import com.zmbdp.portal.service.homepage.service.IRegionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 首页服务实现类
 *
 * @author 稚名不带撇
 */
@Slf4j
@Service
public class HomePageServiceImpl implements IHomePageService {

    /**
     * 地图服务 Api
     */
    @Autowired
    private MapServiceApi mapServiceApi;

    /**
     * 房源服务 Api
     */
    @Autowired
    private HouseServiceApi houseServiceApi;

    /**
     * 调用 admin 地图服务 服务
     */
    @Autowired
    private IRegionService regionService;

    /**
     * 调用 admin 字典服务 服务
     */
    @Autowired
    private IDictionaryService dictionaryService;

    /**
     * 根据经纬度获取城市信息
     *
     * @param lat 用户当前纬度
     * @param lng 用户当前经度
     * @return 城市描述信息
     */
    @Override
    public CityDescVO getCityDesc(Double lat, Double lng) {
        // 校验参数
        if (null == lat || null == lng) {
            throw new ServiceException("城市经纬度不能为空！", ResultCode.INVALID_PARA.getCode());
        }

        LocationReqDTO locationReqDTO = new LocationReqDTO();
        locationReqDTO.setLat(lat);
        locationReqDTO.setLng(lng);
        // 远程调用地图服务 Api
        Result<RegionCityVo> regionCityVoResult = mapServiceApi.locateCityByLocation(locationReqDTO);
        if (
                regionCityVoResult == null ||
                        regionCityVoResult.getCode() != ResultCode.SUCCESS.getCode() ||
                        regionCityVoResult.getData() == null
        ) {
            throw new ServiceException("城市定位失败！", ResultCode.ERROR.getCode());
        }
        // 构造返回
        CityDescVO cityDescVO = new CityDescVO();
        BeanCopyUtil.copyProperties(regionCityVoResult.getData(), cityDescVO);
        return cityDescVO;
    }

    /**
     * 获取下拉筛选数据列表
     *
     * @param pullDataListReqDTO 请求参数
     * @return 筛选数据列表
     */
    @Override
    public PullDataListVO getPullData(PullDataListReqDTO pullDataListReqDTO) {
        PullDataListVO result = new PullDataListVO();

        // 获取当前城市城市 id 对应子集城市列表
        List<CityDescDTO> cityDescDTOList = regionService.regionChildren(pullDataListReqDTO.getCityId());
        result.setRegionList(BeanCopyUtil.copyListProperties(cityDescDTOList, CityDescVO::new));

        // 获取字典数据
        Map<String, List<DictDataDTO>> dictDataMap = dictionaryService.batchFindDictionaryDataByTypes(pullDataListReqDTO.getDirtTypes());
        Map<String, List<DictVO>> dictMap = new HashMap<>();
        dictDataMap.forEach((type, list) -> {
            List<DictVO> dictVOList = list.stream()
                    .map(dictDataDTO -> {
                        DictVO dictVO = new DictVO();
                        dictVO.setId(dictDataDTO.getId());
                        dictVO.setKey(dictDataDTO.getDataKey());
                        dictVO.setName(dictDataDTO.getValue());
                        return dictVO;
                    }).toList();
            dictMap.put(type, dictVOList);
        });
        result.setDictMap(dictMap);
        return result;
    }

    /**
     * 查询房源列表
     *
     * @param reqDTO 筛选信息
     * @return 房源列表
     */
    @Override
    public BasePageVO<HouseDescVO> houseList(HouseListReqDTO reqDTO) {
        SearchHouseListReqDTO searchHouseListReqDTO = new SearchHouseListReqDTO();
        BeanCopyUtil.copyProperties(reqDTO, searchHouseListReqDTO);
        // 先把房源列表查询出来
        Result<BasePageVO<HouseDetailVO>> houseDetailVOResult = houseServiceApi.searchList(searchHouseListReqDTO);
        if (
                null == houseDetailVOResult ||
                        houseDetailVOResult.getCode() != ResultCode.SUCCESS.getCode() ||
                        null == houseDetailVOResult.getData()
        ) {
            log.error("查询房源列表失败！req: {}", JsonUtil.classToJson(searchHouseListReqDTO));
            throw new ServiceException("查询房源列表失败！");
        }
        // 查到了就构造返回
        BasePageVO<HouseDescVO> result = new BasePageVO<>();
        result.setTotals(houseDetailVOResult.getData().getTotals());
        result.setTotalPages(houseDetailVOResult.getData().getTotalPages());
        result.setList(convertHouseList(houseDetailVOResult.getData().getList()));
        return result;
    }

    /**
     * 构造返回的房源列表 (英文转中文啥的，中文是根据字典数据表查的)
     *
     * @param houseDetailVOList 房源列表
     * @return 房源列表
     */
    private List<HouseDescVO> convertHouseList(List<HouseDetailVO> houseDetailVOList) {
        if (CollectionUtils.isEmpty(houseDetailVOList)) {
            return List.of();
        }

        // 查字典：rentType，position:west（datakey）
        List<String> dataKeys = houseDetailVOList.stream()
                .flatMap(houseDetailVO -> Stream.of(houseDetailVO.getRentType(), houseDetailVO.getPosition()))
                .distinct()
                .collect(Collectors.toList());
        Map<String, DictDataDTO> dictDataDTOMap = dictionaryService.batchFindDictionaryData(dataKeys);

        return houseDetailVOList.stream().map(houseDetailVO -> {
            HouseDescVO houseDescVO = new HouseDescVO();
            BeanCopyUtil.copyProperties(houseDetailVO, houseDescVO);

            // 根据字典数据映射 DTO，转成中文
            DictDataDTO rentTypeData = dictDataDTOMap.get(houseDetailVO.getRentType());
            houseDescVO.setRentType(rentTypeData.getValue());
            DictDataDTO positionData = dictDataDTOMap.get(houseDetailVO.getPosition());
            houseDescVO.setPosition(positionData.getValue());
            return houseDescVO;
        }).toList();
    }
}
