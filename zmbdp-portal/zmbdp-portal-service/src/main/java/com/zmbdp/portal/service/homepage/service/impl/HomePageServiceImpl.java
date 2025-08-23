package com.zmbdp.portal.service.homepage.service.impl;

import com.zmbdp.admin.api.map.domain.dto.LocationReqDTO;
import com.zmbdp.admin.api.map.domain.vo.RegionCityVo;
import com.zmbdp.admin.api.map.feign.MapServiceApi;
import com.zmbdp.common.core.utils.BeanCopyUtil;
import com.zmbdp.common.domain.domain.Result;
import com.zmbdp.common.domain.domain.ResultCode;
import com.zmbdp.common.domain.exception.ServiceException;
import com.zmbdp.portal.service.homepage.domain.dto.CityDescDTO;
import com.zmbdp.portal.service.homepage.domain.dto.DictDataDTO;
import com.zmbdp.portal.service.homepage.domain.dto.PullDataListReqDTO;
import com.zmbdp.portal.service.homepage.domain.vo.CityDescVO;
import com.zmbdp.portal.service.homepage.domain.vo.DictVO;
import com.zmbdp.portal.service.homepage.domain.vo.PullDataListVO;
import com.zmbdp.portal.service.homepage.service.IDictionaryService;
import com.zmbdp.portal.service.homepage.service.IHomePageService;
import com.zmbdp.portal.service.homepage.service.IRegionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
}
