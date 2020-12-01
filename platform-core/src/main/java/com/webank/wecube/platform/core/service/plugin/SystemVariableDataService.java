package com.webank.wecube.platform.core.service.plugin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.webank.wecube.platform.core.dto.PageableDto;
import com.webank.wecube.platform.core.dto.QueryRequestDto;
import com.webank.wecube.platform.core.entity.plugin.SystemVariables;
import com.webank.wecube.platform.core.entity.plugin.SystemVariablesExample;
import com.webank.wecube.platform.core.repository.plugin.SystemVariablesMapper;

@Service
public class SystemVariableDataService {
    
    @Autowired
    private SystemVariablesMapper systemVariablesMapper;
    
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

}
