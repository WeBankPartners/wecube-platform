package com.webank.wecube.platform.core.service.plugin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.webank.wecube.platform.core.dto.plugin.PageableDto;
import com.webank.wecube.platform.core.dto.plugin.QueryRequestDto;
import com.webank.wecube.platform.core.entity.plugin.ResourceItem;
import com.webank.wecube.platform.core.entity.plugin.ResourceItemExample;
import com.webank.wecube.platform.core.entity.plugin.ResourceServer;
import com.webank.wecube.platform.core.entity.plugin.ResourceServerExample;
import com.webank.wecube.platform.core.entity.plugin.SystemVariables;
import com.webank.wecube.platform.core.entity.plugin.SystemVariablesExample;
import com.webank.wecube.platform.core.repository.plugin.ResourceItemMapper;
import com.webank.wecube.platform.core.repository.plugin.ResourceServerMapper;
import com.webank.wecube.platform.core.repository.plugin.SystemVariablesMapper;

@Service
public class PluginPageableDataService {
    
    @Autowired
    private SystemVariablesMapper systemVariablesMapper;
    
    @Autowired
    private ResourceServerMapper resourceServerMapper;
    
    @Autowired
    private ResourceItemMapper resourceItemMapper;
    
    public PageInfo<SystemVariables> retrieveSystemVariables(QueryRequestDto queryRequest){
        SystemVariablesExample example = new SystemVariablesExample();
        SystemVariablesExample.Criteria c = example.createCriteria();
        c.andFilters(queryRequest.getFilters());
        
        PageableDto pageable = queryRequest.getPageable();
        
        int pageNum = pageable.getStartIndex() / pageable.getPageSize() + 1;
        int pageSize = pageable.getPageSize();
        PageHelper.startPage(pageNum, pageSize);
        
        List<SystemVariables> systemVariablesEntities = systemVariablesMapper.selectByExample(example);
        PageInfo<SystemVariables> pageInfo = new PageInfo<SystemVariables>(systemVariablesEntities);
        
        return pageInfo;
    }
    
    public PageInfo<ResourceServer> retrieveResourceServers(QueryRequestDto queryRequest){
        ResourceServerExample example = new ResourceServerExample();
        ResourceServerExample.Criteria c = example.createCriteria();
        c.andFilters(queryRequest.getFilters());
        
        PageableDto pageable = queryRequest.getPageable();
        
        int pageNum = pageable.getStartIndex() / pageable.getPageSize() + 1;
        int pageSize = pageable.getPageSize();
        PageHelper.startPage(pageNum, pageSize);
        
        List<ResourceServer> dataList = resourceServerMapper.selectByExample(example);
        PageInfo<ResourceServer> pageInfo = new PageInfo<ResourceServer>(dataList);
        
        return pageInfo;
        
    }
    
    public PageInfo<ResourceItem> retrieveResourceItems(QueryRequestDto queryRequest){
        ResourceItemExample example = new ResourceItemExample();
        ResourceItemExample.Criteria c = example.createCriteria();
        c.andFilters(queryRequest.getFilters());
        
        PageableDto pageable = queryRequest.getPageable();
        
        int pageNum = pageable.getStartIndex() / pageable.getPageSize() + 1;
        int pageSize = pageable.getPageSize();
        PageHelper.startPage(pageNum, pageSize);
        
        List<ResourceItem> dataList = resourceItemMapper.selectByExample(example);
        PageInfo<ResourceItem> pageInfo = new PageInfo<ResourceItem>(dataList);
        
        return pageInfo;
    }
}
