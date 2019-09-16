package com.webank.wecube.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecube.core.domain.workflow.CiRoutineItem;
import com.webank.wecube.core.interceptor.UsernameStorage;
import com.webank.wecube.core.support.cmdb.CmdbServiceV2Stub;
import com.webank.wecube.core.support.cmdb.dto.v2.AdhocIntegrationQueryDto;
import com.webank.wecube.core.support.cmdb.dto.v2.CiTypeAttrDto;
import com.webank.wecube.core.support.cmdb.dto.v2.IntegrationQueryDto;
import com.webank.wecube.core.support.cmdb.dto.v2.PaginationQuery;
import com.webank.wecube.core.support.cmdb.dto.v2.Relationship;

@SpringBootTest
@RunWith(SpringRunner.class)
public class IntQueryTest {

    @Autowired
    CmdbServiceV2Stub stub;

    @Test
    public void testIntQuery() throws Exception{

        UsernameStorage.getIntance().set("umadmin");

        int queryId = 86;
        Map<String, Object> equalsFilters = new HashMap<>();
        Map<String, Object> inFilters = new HashMap<>();
        List<String> resultColumns = new ArrayList<>();

        List<Map<String, Object>> results = stub.executeIntegratedQueryTemplate(queryId, equalsFilters, inFilters,
                resultColumns);

        results.forEach(m -> {
            System.out.println("===========================");
            m.keySet().forEach(k -> {
                System.out.println(String.format("k:%s  - v:%s", k, m.get(k)));
                System.out.println(String.format("type:%s", m.get(k).getClass().getSimpleName()));

                if ((m.get(k)) instanceof Map) {
                    System.out.println("****");
                    Map<?, ?> enumMap = (Map) m.get(k);
                    Object codeObj = enumMap.get("code");
                    Class<?> codeClazz = codeObj.getClass();
                    System.out.println(String.format("isArray:%s", codeClazz.isArray()));
                    System.out.println(String.format("code-> type: %s, value:%s", enumMap.get("code").getClass(),
                            enumMap.get("code")));

                    Object codeValObj = enumMap.get("code");
                    if (codeValObj instanceof String) {
                        String codeValStr = (String) codeValObj;
                        if (codeValStr.startsWith("[{") && codeValStr.endsWith("}]")) {
                            System.out.println("IS diffConf variable");

                            List<CiRoutineItem> ciRoutineItems = null;
                            try {
                                ciRoutineItems = buildRoutines(codeValStr);
                            } catch (IOException e) {
                                System.out.println("errors " + e.getMessage());
                            }

                            // get guid
                            String guidStr = "0015_0000000005";

                            if (ciRoutineItems != null) {
                                System.out.println("#########");
                                System.out.println("guid:" + m.get("routetable$code"));
                                
                                
                                try {
                                    List<Map<String, Object>> retDataList = buildIntegrationQueryAndGetQueryResult(guidStr, ciRoutineItems);
                                    retDataList.forEach(rdMap -> {
                                        System.out.println("=== retDataMap ===");
                                        rdMap.entrySet().forEach(entry -> {
                                            System.out.println(String.format("entry k:%s  , v:%s", entry.getKey(), entry.getValue()));
                                        });
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                
                                
                            }

                            // execute dynamic query
                        } else {
                            System.out.println("NOT diffConf variable");
                        }
                    }
                }
            });
        });

    }

    protected List<Map<String, Object>> buildIntegrationQueryAndGetQueryResult(
            String rootCiDataGuid, List<CiRoutineItem> routines) throws IOException {

        AdhocIntegrationQueryDto rootDto = buildRootDto(routines.get(0), rootCiDataGuid);

        IntegrationQueryDto childQueryDto = travelRoutine(routines, rootDto, 1);
        if (childQueryDto != null) {
            rootDto.getCriteria().setChildren(Collections.singletonList(childQueryDto));
        }
        
        ObjectMapper objMapper = new ObjectMapper();
        String jsonReq = objMapper.writeValueAsString(rootDto);
        
        
        System.out.println("JSON REQ:"+jsonReq);
        return stub.adhocIntegrationQuery(rootDto).getContents();
    }

    protected AdhocIntegrationQueryDto buildRootDto(CiRoutineItem rootRoutineItem, String rootCiGuid) {
        AdhocIntegrationQueryDto dto = new AdhocIntegrationQueryDto();
        PaginationQuery queryRequest = new PaginationQuery();

        queryRequest.addEqualsFilter("root$guid", rootCiGuid);

        IntegrationQueryDto root = new IntegrationQueryDto();
        dto.setCriteria(root);
        dto.setQueryRequest(queryRequest);

        root.setName("root");
        root.setCiTypeId(rootRoutineItem.getCiTypeId());
        root.setAttrs(Arrays.asList(getGuidAttrIdByCiTypeId(rootRoutineItem.getCiTypeId())));
        root.setAttrKeyNames(Arrays.asList("root$guid"));

        return dto;
    }

    protected IntegrationQueryDto travelRoutine(List<CiRoutineItem> routines, AdhocIntegrationQueryDto rootDto, int position) {
        if (position >= (routines.size() -1)) {
            return null;
        }

        CiRoutineItem item = routines.get(position);
        IntegrationQueryDto dto = new IntegrationQueryDto();
        dto.setName("a" + position);
        dto.setCiTypeId(item.getCiTypeId());

        Relationship parentRs = new Relationship();
        parentRs.setAttrId(item.getParentRs().getAttrId());
        parentRs.setIsReferedFromParent(item.getParentRs().getIsReferedFromParent() == 1);
        dto.setParentRs(parentRs);

        IntegrationQueryDto childDto = travelRoutine(routines, rootDto, position+1);
        if (childDto == null) {
            CiRoutineItem attrItem = routines.get(position+1);
            if(item.getCiTypeId() != attrItem.getCiTypeId()){
                throw new RuntimeException("citype id is error");
            }
            
            

            dto.setAttrs(Arrays.asList(attrItem.getParentRs().getAttrId()));

            List<String> attrKeyNames = new ArrayList<String>();
            attrKeyNames.add("tail$attr");

            dto.setAttrKeyNames(attrKeyNames);

            
        } else {
            dto.setChildren(Arrays.asList(childDto));
        }

        return dto;
    }
    
    protected Integer getGuidAttrIdByCiTypeId(int ciTypeId) {
        List<CiTypeAttrDto> attrDtos = stub.getCiTypeAttributesByCiTypeId(ciTypeId);
        for (CiTypeAttrDto dto : attrDtos) {
            if ("guid".equalsIgnoreCase(dto.getPropertyName())) {
                return dto.getCiTypeAttrId();
            }
        }

        return null;
    }

    private List<CiRoutineItem> buildRoutines(String ciRoutineExpStr) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(ciRoutineExpStr.getBytes(),
                mapper.getTypeFactory().constructCollectionType(ArrayList.class, CiRoutineItem.class));
    }

}
