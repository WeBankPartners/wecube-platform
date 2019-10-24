package com.webank.wecube.platform.core.controller;

import static com.webank.wecube.platform.core.domain.JsonResponse.okay;
import static com.webank.wecube.platform.core.domain.JsonResponse.okayWithData;
import static com.webank.wecube.platform.core.domain.MenuItem.MENU_IMPLEMENTATION_ARTIFACT_MANAGEMENT;

import java.io.File;
import java.io.FileOutputStream;
import java.security.Principal;
import java.util.Map;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.webank.wecube.platform.core.commons.ApplicationProperties.CmdbDataProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.domain.PackageDomain;
import com.webank.wecube.platform.core.service.ArtifactService;
import com.webank.wecube.platform.core.support.cmdb.CmdbServiceV2Stub;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CatCodeDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.PaginationQuery;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/artifact")
//@RolesAllowed({MENU_IMPLEMENTATION_ARTIFACT_MANAGEMENT})
public class ArtifactManagementController {
    @Autowired
    CmdbDataProperties cmdbDataProperties;

    @Autowired
    private CmdbServiceV2Stub cmdbServiceV2Stub;

    @Autowired
    private ArtifactService artifactService;

    @GetMapping("/system-design-versions")
    @ResponseBody
    public JsonResponse getSystemDesignVersions() {
        return okayWithData(artifactService.getSystemDesignVersions());
    }

    @GetMapping("/system-design-versions/{system-design-id}")
    @ResponseBody
    public JsonResponse getSystemDesignVersion(@PathVariable(value = "system-design-id") String systemDesignId) {
        return okayWithData(artifactService.getArtifactSystemDesignTree(systemDesignId));
    }

    @PostMapping("/unit-designs/{unit-design-id}/packages/upload")
    @ResponseBody
    public JsonResponse uploadPackage(@PathVariable(value = "unit-design-id") String unitDesignId,
            @RequestParam(value = "file", required = false) MultipartFile multipartFile, Principal principal) {

        File file = convertMultiPartToFile(multipartFile);

        String url = artifactService.uploadPackageToS3(file);

        return okayWithData(artifactService.savePackageToCmdb(file, unitDesignId, principal.getName(), url));
    }

    @PostMapping("/unit-designs/{unit-design-id}/packages/query")
    @ResponseBody
    public JsonResponse queryPackages(@PathVariable(value = "unit-design-id") String unitDesignId,
            @RequestBody PaginationQuery queryObject) {
        queryObject.addEqualsFilter("unit_design", unitDesignId);
        return okayWithData(cmdbServiceV2Stub.queryCiData(cmdbDataProperties.getCiTypeIdOfPackage(), queryObject));
    }

    @PostMapping("/unit-designs/{unit-design-id}/packages/{package-id}/deactive")
    @ResponseBody
    public JsonResponse deactivePackage(@PathVariable(value = "package-id") String packageId) {
        artifactService.deactive(packageId);
        return okay();
    }

    @PostMapping("/unit-designs/{unit-design-id}/packages/{package-id}/active")
    @ResponseBody
    public JsonResponse activePackage(@PathVariable(value = "package-id") String packageId) {
        artifactService.active(packageId);
        return okay();
    }

    @PostMapping("/unit-designs/{unit-design-id}/packages/{package-id}/files/query")
    @ResponseBody
    public JsonResponse getFiles(@PathVariable(value = "package-id") String packageId,
            @RequestBody Map<String, String> additionalProperties) {

        if (additionalProperties.get("currentDir") == null) {
            throw new WecubeCoreException("Field 'currentDir' is required.");
        }

        return okayWithData(
                artifactService.getCurrentDirs(packageId, additionalProperties.get("currentDir")));
    }

    @PostMapping("/unit-designs/{unit-design-id}/packages/{package-id}/property-keys/query")
    @ResponseBody
    public JsonResponse getKeys(@PathVariable(value = "package-id") String packageId,
            @RequestBody Map<String, String> additionalProperties) {

        if (additionalProperties.get("filePath") == null) {
            throw new WecubeCoreException("Field 'filePath' is required.");
        }

        return okayWithData(
                artifactService.getPropertyKeys(packageId, additionalProperties.get("filePath")));
    }

    @PostMapping("/unit-designs/{unit-design-id}/packages/{package-id}/save")
    @ResponseBody
    public JsonResponse saveConfigFiles(@PathVariable(value = "package-id") String packageId, @RequestBody PackageDomain packageDomain) {
        artifactService.saveConfigFiles(packageId, packageDomain);
        return okay();
    }

    @PostMapping("/enum/codes/diff-config/save")
    @ResponseBody
    public JsonResponse saveDiffConfigEnumCodes(@RequestBody CatCodeDto code) {
        artifactService.saveDiffConfigEnumCodes(code);
        return okay();
    }

    @GetMapping("/enum/codes/diff-config/query")
    @ResponseBody
    public JsonResponse getDiffConfigEnumCodes() {
        return okayWithData(artifactService.getDiffConfigEnumCodes());
    }

    @GetMapping("/getPackageCiTypeId")
    @ResponseBody
    public JsonResponse getPackageCiTypeId() {
        return okayWithData(cmdbDataProperties.getCiTypeIdOfPackage());
    }

    private File convertMultiPartToFile(MultipartFile multipartFile) {
        if (multipartFile == null) {
            return null;
        }

        File file = new File(multipartFile.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(file)){
            fos.write(multipartFile.getBytes());
        } catch (Exception e) {
            throw new WecubeCoreException("Fail to convert multipart file to file", e);
        }
        return file;
    }
}
