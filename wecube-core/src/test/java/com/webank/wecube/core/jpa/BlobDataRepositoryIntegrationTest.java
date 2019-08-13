package com.webank.wecube.core.jpa;

import com.webank.wecube.core.DatabaseBasedTest;
import com.webank.wecube.core.domain.BlobData;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;

import static com.webank.wecube.core.domain.BlobData.TYPE_ICON;
import static org.assertj.core.api.Assertions.assertThat;

public class BlobDataRepositoryIntegrationTest extends DatabaseBasedTest {

    @Autowired
    BlobDataRepository repository;

    BlobData blobData;
    byte[] imageData;

    @Before
    public void setUp() throws IOException {
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        imageData = FileUtils.readFileToByteArray(resourceLoader.getResource("image.png").getFile());

        blobData = new BlobData();
        blobData.setName("my-image");
        blobData.setType(TYPE_ICON);
        blobData.setContent(imageData);
    }

    @Test
    public void findSavedBlobData() {
        assertThat(repository.findFirstByType(TYPE_ICON)).isNotPresent();

        blobData = repository.save(blobData);

        assertThat(repository.findById(blobData.getId())).hasValue(blobData);
        assertThat(repository.findFirstByType(TYPE_ICON)).isPresent();
        assertThat(repository.findFirstByType(TYPE_ICON)).hasValue(blobData);
    }

}
