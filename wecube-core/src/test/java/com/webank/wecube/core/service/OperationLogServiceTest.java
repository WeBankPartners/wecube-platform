package com.webank.wecube.core.service;

import com.webank.wecube.core.DatabaseBasedTest;
import com.webank.wecube.core.domain.OperationLog;
import com.webank.wecube.core.dto.OperationLogDto;
import com.webank.wecube.core.dto.QueryRequest;
import com.webank.wecube.core.dto.QueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.webank.wecube.core.dto.QueryRequest.defaultQueryObject;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class OperationLogServiceTest extends DatabaseBasedTest {

    @Autowired
    private OperationLogService operationLogService;

    @Test
    public void queryByCreatorTest() {
        QueryRequest request = defaultQueryObject().addEqualsFilter("operator", "umadmin");
        QueryResponse<OperationLogDto> queryResults = operationLogService.query(request);
        if (0 < queryResults.getContents().size()) {
            assertThat(queryResults.getContents().get(0).getOperator()).isEqualTo("umadmin");
        }
    }

    @Test
    public void queryByNoExistingCreatorTest() {
        QueryRequest request = defaultQueryObject().addEqualsFilter("operator", "xxxxxxxxxxxxxxxx");
        QueryResponse<OperationLogDto> queryResults = operationLogService.query(request);

        assertThat(queryResults.getContents().size()).isEqualTo(0);
    }

    @Test
    public void queryByCategoryTest() {
        QueryRequest request = defaultQueryObject().addEqualsFilter("category", "cmdb");
        QueryResponse<OperationLogDto> queryResults = operationLogService.query(request);
        if (0 < queryResults.getContents().size()) {
            assertThat(queryResults.getContents().get(0).getCategory()).isEqualTo("cmdb");
        }
    }

    @Ignore
    @Test
    public void queryByCreatTimeTest() {
        String startTime = "2019-07-30 11:29:44";
        String endTime = "2019-08-31 12:29:46";

        QueryRequest request = defaultQueryObject().addGreaterThanFilter("operateTime", startTime).addLessThanFilter("operateTime", endTime);
        QueryResponse<OperationLogDto> queryResults = operationLogService.query(request);
        if (0 < queryResults.getContents().size()) {
            assertThat(queryResults.getContents().get(0).getOperateTime()).isAfter(startTime);
            assertThat(queryResults.getContents().get(0).getOperateTime()).isBefore(endTime);
        }
    }


}
