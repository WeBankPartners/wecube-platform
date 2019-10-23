package com.webank.wecube.platform.core.support.cmdb;

import static com.webank.wecube.platform.core.support.cmdb.dto.v2.PaginationQuery.defaultQueryObject;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.Role;
import com.webank.wecube.platform.core.domain.RoleUser;
import com.webank.wecube.platform.core.domain.User;
import com.webank.wecube.platform.core.support.cmdb.dto.CmdbResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.CmdbResponse.DefaultCmdbResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.AdhocIntegrationQueryDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CatCodeDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CatTypeDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CategoryDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CiDataDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CiDataTreeDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CiTypeAttrDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CiTypeDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.CiDataQueryResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.CiDataVersionDetailResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.CiDataVersionsQueryResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.CiReferenceDataListResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.CiTypeAttributeListResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.CiTypeAttributeQueryResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.CiTypeListResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.CiTypeQueryResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.ConstantsCiStatusResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.ConstantsReferenceTypesResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.EnumCategoryListResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.EnumCategoryQueryResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.EnumCategoryTypeListResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.EnumCategoryTypeQueryResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.EnumCodeListResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.EnumCodeQueryResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.IntQueryExecuteDataResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.IntQueryOperateDataResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.QueryOperationDataResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.RoleCiTypeCtrlAttrConditionListResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.RoleCiTypeCtrlAttrConditionQueryResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.RoleCiTypeCtrlAttrListResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.RoleCiTypeCtrlAttrQueryResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.RoleCiTypeListResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.RoleCiTypeQueryResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.RoleListResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.RoleQueryResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.RoleUserListResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.RoleUserQueryResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.UserListResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CmdbResponses.UsersQueryResultResponse;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.ImageInfoDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.IntQueryOperateAggRequestDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.IntQueryOperateAggResponseDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.IntegrationQueryDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.OperateCiDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.PaginationQuery;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.PaginationQueryResult;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.RoleCiTypeCtrlAttrConditionDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.RoleCiTypeCtrlAttrDto;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.RoleCiTypeDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CmdbServiceV2Stub {

    private static final String CONSTANT_CI_TYPE_ID = "ciTypeId";
    private static final String CONSTANT_CAT_ID = "catId";
    private static final String CONSTANT_SEQ_NO = "seqNo";
    private static final String CONSTANT_ATTRIBUTES = "attributes";
    private static final String CONSTANT_OPERATION = "?operation=";

    private static final String API_VERSION = "/api/v2";

    private static final String CONSTANTS_CI_STATUS = "/constants/ciStatus/retrieve";
    private static final String CONSTANTS_REFERENCE_TYPES = "/constants/referenceTypes/retrieve";
    private static final String CONSTANTS_INPUT_TYPES = "/constants/inputTypes/retrieve";
    private static final String CONSTANTS_EFFECTIVE_STATUS = "/constants/effectiveStatus/retrieve";

    private static final String ADMIN_USERS_QUERY = "/users/retrieve";
    private static final String ADMIN_USERS_CREATE = "/users/create";
    private static final String ADMIN_ROLES_QUERY = "/roles/retrieve";
    private static final String ADMIN_ROLES_CREATE = "/roles/create";
    private static final String ADMIN_ROLES_UPDATE = "/roles/update";
    private static final String ADMIN_ROLES_DELETE = "/roles/delete";
    private static final String ADMIN_ROLE_USERS_QUERY = "/role-users/retrieve";
    private static final String ADMIN_ROLE_USERS_CREATE = "/role-users/create";
    private static final String ADMIN_ROLE_USERS_DELETE = "/role-users/delete";
    private static final String ADMIN_ROLE_CITYPES_QUERY = "/role-citypes/retrieve";
    private static final String ADMIN_ROLE_CITYPES_CREATE = "/role-citypes/create";
    private static final String ADMIN_ROLE_CITYPES_UPDATE = "/role-citypes/update";
    private static final String ADMIN_ROLE_CITYPE_CTRL_ATTRIBUTES_QUERY = "/role-citype-ctrl-attrs/retrieve";
    private static final String ADMIN_ROLE_CITYPE_CTRL_ATTRIBUTES_CREATE = "/role-citype-ctrl-attrs/create";
    private static final String ADMIN_ROLE_CITYPE_CTRL_ATTRIBUTES_UPDATE = "/role-citype-ctrl-attrs/update";
    private static final String ADMIN_ROLE_CITYPE_CTRL_ATTRIBUTES_DELETE = "/role-citype-ctrl-attrs/delete";
    private static final String ADMIN_ROLE_CITYPE_CTRL_ATTRIBUTE_CONDITIONS_QUERY = "/role-citype-ctrl-attr-conditions/retrieve";
    private static final String ADMIN_ROLE_CITYPE_CTRL_ATTRIBUTE_CONDITIONS_CREATE = "/role-citype-ctrl-attr-conditions/create";
    private static final String ADMIN_ROLE_CITYPE_CTRL_ATTRIBUTE_CONDITIONS_UPDATE = "/role-citype-ctrl-attr-conditions/update";


    private static final String FILE_UPLOAD = "/image/upload";
    private static final String FILE_DOWNLOAD = "/image/%d";

    private static final String ENUM_CATEGORY_TYPE_CREATE = "/enum/catTypes/create";
    private static final String ENUM_CATEGORY_TYPE_QUERY = "/enum/catTypes/retrieve";
    private static final String ENUM_CATEGORY_TYPE_UPDATE = "/enum/catTypes/update";
    private static final String ENUM_CATEGORY_TYPE_DELETE = "/enum/catTypes/delete";

    private static final String ENUM_CATEGORY_CREATE = "/enum/cats/create";
    private static final String ENUM_CATEGORY_QUERY = "/enum/cats/retrieve";
    private static final String ENUM_CATEGORY_UPDATE = "/enum/cats/update";
    private static final String ENUM_CATEGORY_DELETE = "/enum/cats/delete";

    private static final String ENUM_CODE_CREATE = "/enum/codes/create";
    private static final String ENUM_CODE_QUERY = "/enum/codes/retrieve";
    private static final String ENUM_CODE_UPDATE = "/enum/codes/update";
    private static final String ENUM_CODE_DELETE = "/enum/codes/delete";
    private static final String ENUM_CODE_REFERENCE_QUERY = "/enum/codes/referenceDatas/%d/query";

    private static final String CITYPE_CREATE = "/ciTypes/create";
    private static final String CITYPE_QUERY = "/ciTypes/retrieve";
    private static final String CITYPE_UPDATE = "/ciTypes/update";
    private static final String CITYPE_DELETE = "/ciTypes/delete";

    private static final String CITYPE_ATTRIBUTE_CREATE = "/ciTypeAttrs/create";
    private static final String CITYPE_ATTRIBUTE_QUERY = "/ciTypeAttrs/retrieve";
    private static final String CITYPE_ATTRIBUTE_UPDATE = "/ciTypeAttrs/update";
    private static final String CITYPE_ATTRIBUTE_DELETE = "/ciTypeAttrs/delete";

    private static final String CIDATA_CREATE = "/ci/%d/create";
    private static final String CIDATA_QUERY = "/ci/%d/retrieve";
    private static final String CIDATA_UPDATE = "/ci/%d/update";
    private static final String CIDATA_DELETE = "/ci/%d/delete";
    private static final String CIDATA_REFERNECE_QUERY = "/ci/referenceDatas/%d/query";
    private static final String CIDATA_STATE_OPERATE = "/ci/state/operate";
    private static final String CIDATA_RETRIEVE_VERSIONS = "/ci/%d/versions/retrieve";
    private static final String CIDATA_RETRIEVE_VERSION_DETAIL = "/ci/from/%d/to/%d/versions/%s/retrieve";

    private static final String CI_TYPE_APPLY = "/ciTypes/apply";
    private static final String CI_TYPES_APPLY = "/ciTypes/applyAll";
    private static final String CI_TYPE_ATTRIBUTES_APPLY = "/ciTypeAttrs/apply";

    private static final String GET_INTEGRATION_QUERY = "/intQuery/%d";
    private static final String INTEGRATED_QUERY_DUPLICATE = "/intQuery/%d/duplicate";
    private static final String INTEGRATED_QUERY_EXECUTE = "/intQuery/%d/execute";
    private static final String QUERY_INT_HEADER = "/intQuery/%d/header";
    private static final String UPDATE_INT_QUERY = "/intQuery/%d/update";
    private static final String DELETE_QUERY = "/intQuery/ciType/%d/%d/delete";
    private static final String SAVE_INT_QUERY = "/intQuery/ciType/%d/%s/save";
    private static final String DELETE_INTEGRATION_OPERATE_AGGREGATION = "/intQuery/ciType/%d/aggQueries/delete";
    private static final String INTEGRATED_QUERY_OPERATE = "/intQuery/ciType/%d/aggQueries/operate";
    private static final String SEARCH_INT_QUERY = "/intQuery/ciType/%d/search";
    private static final String GET_INT_QUERY_BY_NAME = "/intQuery/ciType/%d/%d";

    private static final String ADHOC_INT_QUERY = "/intQuery/adhoc/execute";

    private static final String CI_TYPE_IMPLEMENT = "/ciTypes/%d/implement";
    private static final String CI_TYPE_ATTR_IMPLEMENT = "/ciTypeAttrs/%d/implement";

    private static final String QUERY_OPERATION_URL_PATTERN = "/ci/state/operation?curState=%d&isConfirmed=true&targetState=%d";

    @Autowired
    private CmdbRestTemplate template;

    @Autowired
    private ApplicationProperties applicationProperties;

    public PaginationQueryResult<Map<String, Object>> adhocIntegrationQuery(AdhocIntegrationQueryDto aiqDto) {
        PaginationQueryResult<Map<String, Object>> result = template.postForResponse(asCmdbUrl(ADHOC_INT_QUERY),
                aiqDto, CmdbResponses.AdhocIntegrationQueryDataResponse.class);
        return result;
    }

    public List<String> getConstantsCiStatus() {
        return template.postForResponse(asCmdbUrl(CONSTANTS_CI_STATUS), ConstantsCiStatusResponse.class);
    }

    public List<String> getConstantsReferenceTypes() {
        return template.postForResponse(asCmdbUrl(CONSTANTS_REFERENCE_TYPES), ConstantsReferenceTypesResponse.class);
    }

    public List<String> getAvailableCiTypeAttributeInputTypes() {
        return template.postForResponse(asCmdbUrl(CONSTANTS_INPUT_TYPES), ConstantsReferenceTypesResponse.class);
    }

    public List<String> getEffectiveStatus() {
        return template.postForResponse(asCmdbUrl(CONSTANTS_EFFECTIVE_STATUS), ConstantsReferenceTypesResponse.class);
    }

    public List<User> getAllUsers() {
        PaginationQueryResult<User> queryResult = query(ADMIN_USERS_QUERY, defaultQueryObject(), UsersQueryResultResponse.class);
        return queryResult.getContents();
    }
    
    public User getUserByUsername(String username) {
        return findFirst(ADMIN_USERS_QUERY, defaultQueryObject("username", username), UsersQueryResultResponse.class, false);
    }
    
    public List<User> createUsers(User... users) {
        return create(ADMIN_USERS_CREATE, users, UserListResultResponse.class);
    }

    public List<Role> getAllRoles() {
        PaginationQueryResult<Role> queryResult = query(ADMIN_ROLES_QUERY, defaultQueryObject(), RoleQueryResultResponse.class);
        return queryResult.getContents();
    }

    public List<Role> getRolesByUsername(String username) {
        PaginationQuery queryObject = defaultQueryObject()
                .addReferenceResource("roleUsers")
                .addReferenceResource("roleUsers.user")
                .addEqualsFilter("roleUsers.user.username", username);
        PaginationQueryResult<Role> queryResult = query(ADMIN_ROLES_QUERY, queryObject, RoleQueryResultResponse.class);
        return queryResult.getContents();
    }

    public List<User> getUsersByRoleId(int roleId) {
        PaginationQuery queryObject = defaultQueryObject()
                .addReferenceResource("roleUsers")
                .addReferenceResource("roleUsers.role")
                .addEqualsFilter("roleUsers.role.roleId", roleId);
        PaginationQueryResult<User> queryResult = query(ADMIN_USERS_QUERY, queryObject, UsersQueryResultResponse.class);
        return queryResult.getContents();
    }

    public List<Role> createRoles(Role... roles) {
        return create(ADMIN_ROLES_CREATE, roles, RoleListResultResponse.class);
    }
    
    public List<CiTypeDto> updateRoles(Role... roles) {
        return update(ADMIN_ROLES_UPDATE, roles, RoleListResultResponse.class);
    }

    public void deleteRoles(Integer... ids) {
        delete(ADMIN_ROLES_DELETE, ids, DefaultCmdbResponse.class);
    }

    public List<RoleUser> getRoleUsers(PaginationQuery queryObject) {
        PaginationQueryResult<RoleUser> queryResult = query(ADMIN_ROLE_USERS_QUERY, queryObject, RoleUserQueryResultResponse.class);
        return queryResult.getContents();
    }

    public List<RoleUser> createRoleUsers(RoleUser... roleUsers) {
        return create(ADMIN_ROLE_USERS_CREATE, roleUsers, RoleUserListResultResponse.class);
    }

    public void deleteRoleUsers(Integer... ids) {
        delete(ADMIN_ROLE_USERS_DELETE, ids, DefaultCmdbResponse.class);
    }

    public List<RoleCiTypeDto> queryRoleCiTypes(PaginationQuery queryObject) {
        PaginationQueryResult<RoleCiTypeDto> queryResult = query(ADMIN_ROLE_CITYPES_QUERY, queryObject, RoleCiTypeQueryResultResponse.class);
        return queryResult.getContents();
    }

    public List<RoleCiTypeDto> getRoleCiTypeByRoleId(int roleId) {
        return queryRoleCiTypes(defaultQueryObject()
                .addReferenceResource("roleCiTypeCtrlAttrs")
                .addEqualsFilter("roleId", roleId));
    }

    public List<RoleCiTypeDto> getRoleCiTypeByUsername(String username) {
        return queryRoleCiTypes(defaultQueryObject()
                .addReferenceResource("role")
                .addReferenceResource("role.roleUsers")
                .addReferenceResource("role.roleUsers.user")
                .addReferenceResource("roleCiTypeCtrlAttrs")
                .addEqualsFilter("role.roleUsers.user.username", username));
    }

    public RoleCiTypeDto getRoleCiTypeByRoleIdAndCiTypeId(int roleId, int ciTypeId) {
        PaginationQuery queryObject = defaultQueryObject().addEqualsFilter("roleId", roleId).addEqualsFilter(CONSTANT_CI_TYPE_ID, ciTypeId);
        return findFirst(ADMIN_ROLE_CITYPES_QUERY, queryObject, RoleCiTypeQueryResultResponse.class, false);
    }

    public RoleCiTypeDto getRoleCiTypeById(Integer roleCiTypeId) {
        return findFirst(ADMIN_ROLE_CITYPES_QUERY, "roleCiTypeId", roleCiTypeId, RoleCiTypeQueryResultResponse.class);
    }

    public List<RoleCiTypeDto> createRoleCiTypes(RoleCiTypeDto... roleCiTypes) {
        return create(ADMIN_ROLE_CITYPES_CREATE, roleCiTypes, RoleCiTypeListResultResponse.class);
    }

    public List<RoleCiTypeDto> updateRoleCiTypes(RoleCiTypeDto... roleCiTypes) {
        return update(ADMIN_ROLE_CITYPES_UPDATE, roleCiTypes, RoleCiTypeListResultResponse.class);
    }

    public List<RoleCiTypeCtrlAttrDto> queryRoleCiTypeCtrlAttributes(PaginationQuery queryObject) {
        PaginationQueryResult<RoleCiTypeCtrlAttrDto> queryResult = query(ADMIN_ROLE_CITYPE_CTRL_ATTRIBUTES_QUERY, queryObject, RoleCiTypeCtrlAttrQueryResultResponse.class);
        return queryResult.getContents();
    }

    public List<RoleCiTypeCtrlAttrDto> getRoleCiTypeCtrlAttributesByRoleCiTypeId(int roleCiTypeId) {
        return queryRoleCiTypeCtrlAttributes(defaultQueryObject()
                .addReferenceResource("conditions")
//                .addReferenceResource("conditions.ciTypeAttr")
                .addEqualsFilter("roleCiTypeId", roleCiTypeId));
    }

    public RoleCiTypeCtrlAttrDto getRoleCiTypeCtrlAttributeById(Integer roleCiTypeCtrlAttrId) {
        return findFirst(ADMIN_ROLE_CITYPE_CTRL_ATTRIBUTES_QUERY, "roleCiTypeCtrlAttrId", roleCiTypeCtrlAttrId, RoleCiTypeCtrlAttrQueryResultResponse.class);
    }

    public List<RoleCiTypeCtrlAttrDto> createRoleCiTypeCtrlAttributes(RoleCiTypeCtrlAttrDto... roleCiTypeCtrlAttrs) {
        return create(ADMIN_ROLE_CITYPE_CTRL_ATTRIBUTES_CREATE, roleCiTypeCtrlAttrs, RoleCiTypeCtrlAttrListResultResponse.class);
    }

    public RoleCiTypeCtrlAttrDto createRoleCiTypeCtrlAttribute(RoleCiTypeCtrlAttrDto roleCiTypeCtrlAttr) {
        List<RoleCiTypeCtrlAttrDto> roleCiTypeCtrlAttrs = createRoleCiTypeCtrlAttributes(roleCiTypeCtrlAttr);
        if (isEmpty(roleCiTypeCtrlAttrs)) throw new WecubeCoreException("Create role CiType ctrl attr failure.");
        return roleCiTypeCtrlAttrs.get(0);
    }

    public List<RoleCiTypeCtrlAttrDto> updateRoleCiTypeCtrlAttributes(RoleCiTypeCtrlAttrDto... roleCiTypeCtrlAttrs) {
        return update(ADMIN_ROLE_CITYPE_CTRL_ATTRIBUTES_UPDATE, roleCiTypeCtrlAttrs, RoleCiTypeCtrlAttrListResultResponse.class);
    }

    public RoleCiTypeCtrlAttrDto updateRoleCiTypeCtrlAttribute(RoleCiTypeCtrlAttrDto roleCiTypeCtrlAttr) {
        List<RoleCiTypeCtrlAttrDto> roleCiTypeCtrlAttrs = updateRoleCiTypeCtrlAttributes(roleCiTypeCtrlAttr);
        if (isEmpty(roleCiTypeCtrlAttrs)) throw new WecubeCoreException("Update role CiType ctrl attr failure.");
        return roleCiTypeCtrlAttrs.get(0);
    }

    public void deleteRoleCiTypeCtrlAttributes(Integer... ids) {
        delete(ADMIN_ROLE_CITYPE_CTRL_ATTRIBUTES_DELETE, ids, DefaultCmdbResponse.class);
    }

    public RoleCiTypeCtrlAttrConditionDto getRoleCiTypeCtrlAttributeConditionById(Integer conditionId) {
        return findFirst(ADMIN_ROLE_CITYPE_CTRL_ATTRIBUTE_CONDITIONS_QUERY, "conditionId", conditionId, RoleCiTypeCtrlAttrConditionQueryResultResponse.class);
    }

    public List<RoleCiTypeCtrlAttrConditionDto> createRoleCiTypeCtrlAttrConditions(RoleCiTypeCtrlAttrConditionDto... roleCiTypeCtrlAttrConditions) {
        return create(ADMIN_ROLE_CITYPE_CTRL_ATTRIBUTE_CONDITIONS_CREATE, roleCiTypeCtrlAttrConditions, RoleCiTypeCtrlAttrConditionListResultResponse.class);
    }

    public RoleCiTypeCtrlAttrConditionDto createRoleCiTypeCtrlAttrCondition(RoleCiTypeCtrlAttrConditionDto roleCiTypeCtrlAttrCondition) {
        List<RoleCiTypeCtrlAttrConditionDto> roleCiTypeCtrlAttrConditions = createRoleCiTypeCtrlAttrConditions(roleCiTypeCtrlAttrCondition);
        if (isEmpty(roleCiTypeCtrlAttrConditions))
            throw new WecubeCoreException("Create role CiType ctrl attr condition failure.");
        return roleCiTypeCtrlAttrConditions.get(0);
    }

    public List<RoleCiTypeCtrlAttrConditionDto> updateRoleCiTypeCtrlAttrConditions(RoleCiTypeCtrlAttrConditionDto... roleCiTypeCtrlAttrConditions) {
        return update(ADMIN_ROLE_CITYPE_CTRL_ATTRIBUTE_CONDITIONS_UPDATE, roleCiTypeCtrlAttrConditions, RoleCiTypeCtrlAttrConditionListResultResponse.class);
    }


    public Integer uploadFile(Resource inputStreamSource) {
        ImageInfoDto imageInfo = template.uploadSingleFile(asCmdbUrl(FILE_UPLOAD), "img", inputStreamSource,
                CmdbResponses.ImageInfoResponse.class);
        return imageInfo.getId();
    }

    public ResponseEntity<byte[]> downloadFile(int fileId) {
        return template.downloadSingleFile(asCmdbUrl(FILE_DOWNLOAD, fileId));
    }

    public CiTypeDto uploadCiTypeIcon(int ciTypeId, Resource inputStreamSource) {
        int imageFileId = uploadFile(inputStreamSource);
        CiTypeDto ciType = new CiTypeDto();
        ciType.setCiTypeId(ciTypeId);
        ciType.setImageFileId(imageFileId);
        List<CiTypeDto> ciTypes = updateCiTypes(ciType);
        return isNotEmpty(ciTypes) ? ciTypes.get(0) : null;
    }

    public List<CatTypeDto> createEnumCategoryTypes(CatTypeDto... catTypeDtos) {
        return create(ENUM_CATEGORY_TYPE_CREATE, catTypeDtos, EnumCategoryTypeListResultResponse.class);
    }

    public List<String> queryOperation(int curState, int targetState) {
        return template.get(asCmdbUrl(QUERY_OPERATION_URL_PATTERN, curState, targetState), QueryOperationDataResponse.class);
    }

    public PaginationQueryResult<CatTypeDto> queryEnumCategoryTypes(PaginationQuery queryObject) {
        return query(ENUM_CATEGORY_TYPE_QUERY, queryObject, EnumCategoryTypeQueryResultResponse.class);
    }

    public List<CatTypeDto> getAllEnumCategoryTypes() {
        PaginationQueryResult<CatTypeDto> queryResult = query(ENUM_CATEGORY_TYPE_QUERY, defaultQueryObject(),
                EnumCategoryTypeQueryResultResponse.class);
        return queryResult.getContents();
    }

    public CatTypeDto getEnumCategoryTypeByCiTypeId(Integer ciTypeId) {
        return findFirst(ENUM_CATEGORY_TYPE_QUERY, CONSTANT_CI_TYPE_ID, ciTypeId, EnumCategoryTypeQueryResultResponse.class);
    }

    public List<CatTypeDto> updateEnumCategoryTypes(CatTypeDto... catTypeDtos) {
        return update(ENUM_CATEGORY_TYPE_UPDATE, catTypeDtos, EnumCategoryTypeListResultResponse.class);
    }

    public void deleteEnumCategoryTypes(Integer... ids) {
        delete(ENUM_CATEGORY_TYPE_DELETE, ids, DefaultCmdbResponse.class);
    }

    public List<CategoryDto> createEnumCategories(CategoryDto... categoryDtos) {
        return create(ENUM_CATEGORY_CREATE, categoryDtos, EnumCategoryListResultResponse.class);
    }

    public PaginationQueryResult<CategoryDto> queryEnumCategories(PaginationQuery queryObject) {
        return query(ENUM_CATEGORY_QUERY, queryObject, EnumCategoryQueryResultResponse.class);
    }

    public PaginationQueryResult<CategoryDto> getAllEnumCategories() {
        return query(ENUM_CATEGORY_QUERY, defaultQueryObject(), EnumCategoryQueryResultResponse.class);
    }

    public PaginationQueryResult<CategoryDto> getEnumCategoriesByTypeId(Integer enumCategoryTypeId) {
        return query(ENUM_CATEGORY_QUERY, defaultQueryObject("catTypeId", enumCategoryTypeId),
                EnumCategoryQueryResultResponse.class);
    }

    public CategoryDto getEnumCategoryByName(String categoryName) {
        return findFirst(ENUM_CATEGORY_QUERY, "catName", categoryName, EnumCategoryQueryResultResponse.class);
    }

    public CategoryDto getEnumCategoryByCatId(int catId) {
        return findFirst(ENUM_CATEGORY_QUERY, CONSTANT_CAT_ID, catId, EnumCategoryQueryResultResponse.class);
    }

    public List<CategoryDto> updateEnumCategories(CategoryDto... categoryDtos) {
        return update(ENUM_CATEGORY_UPDATE, categoryDtos, EnumCategoryListResultResponse.class);
    }

    public void deleteEnumCategories(Integer... ids) {
        delete(ENUM_CATEGORY_DELETE, ids, DefaultCmdbResponse.class);
    }

    public List<CatCodeDto> createEnumCodes(CatCodeDto... catCodeDtos) {
        return create(ENUM_CODE_CREATE, catCodeDtos, EnumCodeListResultResponse.class);
    }

    public PaginationQueryResult<CatCodeDto> queryEnumCodes(PaginationQuery queryObject) {
        return query(ENUM_CODE_QUERY, queryObject, EnumCodeQueryResultResponse.class);
    }

    public List<CatCodeDto> getEnumCodesByCategoryId(Integer categoryId) {
        PaginationQueryResult<CatCodeDto> queryResult = query(ENUM_CODE_QUERY,
                defaultQueryObject(CONSTANT_CAT_ID, categoryId).ascendingSortBy(CONSTANT_SEQ_NO), EnumCodeQueryResultResponse.class);
        return queryResult.getContents();
    }

    public List<CatCodeDto> getEnumCodesByFieldNameWithValueAndCatId(String fieldName, Object value, Integer catId) {
        PaginationQuery queryObject = defaultQueryObject().addEqualsFilter(CONSTANT_CAT_ID, catId)
                .addEqualsFilter(fieldName, value);

        PaginationQueryResult<CatCodeDto> queryResult = query(ENUM_CODE_QUERY, queryObject,
                EnumCodeQueryResultResponse.class);
        return queryResult.getContents();
    }

    public List<CatCodeDto> getEnumCodeByCodeAndCategoryName(String code, String categoryName) {
        CategoryDto categoryDto = getEnumCategoryByName(categoryName);
        if (categoryDto == null) {
            throw new CmdbRemoteCallException(String.format("The enum category name [%s] not found.", categoryName));
        }

        PaginationQuery queryObject = defaultQueryObject().addEqualsFilter(CONSTANT_CAT_ID, categoryDto.getCatId())
                .addEqualsFilter("code", code);

        PaginationQueryResult<CatCodeDto> queryResult = query(ENUM_CODE_QUERY, queryObject,
                EnumCodeQueryResultResponse.class);
        return queryResult.getContents();
    }

    public CatCodeDto getEnumCodeById(Integer id) {
        return findFirst(ENUM_CODE_QUERY, "codeId", id, EnumCodeQueryResultResponse.class);
    }

    public List<CatCodeDto> getEnumCodesByIds(List<Integer> ids) {
        PaginationQueryResult<CatCodeDto> queryResult = query(ENUM_CODE_QUERY, defaultQueryObject().addInFilter("codeId", ids), EnumCodeQueryResultResponse.class);
        return queryResult.getContents();
    }

    public List<CatCodeDto> updateEnumCodes(CatCodeDto... catCodeDtos) {
        return update(ENUM_CODE_UPDATE, catCodeDtos, EnumCodeListResultResponse.class);
    }

    public void deleteEnumCodes(Integer... ids) {
        delete(ENUM_CODE_DELETE, ids, DefaultCmdbResponse.class);
    }

    public List<CiTypeDto> createCiTypes(CiTypeDto... ciTypes) {
        return create(CITYPE_CREATE, ciTypes, CiTypeListResultResponse.class);
    }

    public List<CiTypeDto> updateCiTypes(CiTypeDto... ciTypes) {
        return update(CITYPE_UPDATE, ciTypes, CiTypeListResultResponse.class);
    }

    public List<CiTypeDto> getAllCiTypes(boolean withAttributes, String status) {
        PaginationQuery paginationQuery = defaultQueryObject();

        if (status != null) {
            paginationQuery = paginationQuery.addInFilter("status", Arrays.asList(status.split(",")));
        }

        if (withAttributes) {
            paginationQuery = paginationQuery.addReferenceResource(CONSTANT_ATTRIBUTES);
            if (status != null) {
                paginationQuery = paginationQuery.addInFilter("attributes.status", Arrays.asList(status.split(",")));
            }
        }

        PaginationQueryResult<CiTypeDto> queryResult = query(CITYPE_QUERY, paginationQuery.ascendingSortBy(CONSTANT_SEQ_NO),
                CiTypeQueryResultResponse.class);
        return queryResult.getContents();
    }

    public CiTypeDto getCiType(Integer ciTypeId) {
        return getCiType(ciTypeId, false);
    }

    public CiTypeDto getCiType(Integer ciTypeId, boolean withAttributes) {
        PaginationQuery queryObject = defaultQueryObject(CONSTANT_CI_TYPE_ID, ciTypeId);
        if (withAttributes)
            queryObject = queryObject.addReferenceResource(CONSTANT_ATTRIBUTES);
        return findFirst(CITYPE_QUERY, queryObject, CiTypeQueryResultResponse.class, true);
    }

    public List<CiTypeDto> getCiTypes(List<Integer> ciTypeIds) {
        return getCiTypes(ciTypeIds, false);
    }

    public List<CiTypeDto> getCiTypes(List<Integer> ciTypeIds, boolean withAttributes) {
        PaginationQuery queryObject = defaultQueryObject().addInFilter(CONSTANT_CI_TYPE_ID, ciTypeIds);
        if (withAttributes)
            queryObject = queryObject.addReferenceResource(CONSTANT_ATTRIBUTES);
        return findAll(CITYPE_QUERY, queryObject.ascendingSortBy(CONSTANT_SEQ_NO), CiTypeQueryResultResponse.class);
    }


    public PaginationQueryResult<CiTypeDto> queryCiTypes(PaginationQuery queryObject) {
        return query(CITYPE_QUERY, queryObject, CiTypeQueryResultResponse.class);
    }

    public void deleteCiTypes(Integer... ids) {
        delete(CITYPE_DELETE, ids, DefaultCmdbResponse.class);
    }

    public List<CiTypeAttrDto> createCiTypeAttribute(CiTypeAttrDto ciTypeAttribute) {
        ArrayList ciTypeAttributes = new ArrayList();
        ciTypeAttributes.add(ciTypeAttribute);
        return create(CITYPE_ATTRIBUTE_CREATE, ciTypeAttributes.toArray(), CiTypeAttributeListResultResponse.class);
    }

    public List<CiTypeAttrDto> createCiTypeAttributes(CiTypeAttrDto... ciTypeAttributes) {
        return create(CITYPE_ATTRIBUTE_CREATE, ciTypeAttributes, CiTypeAttributeListResultResponse.class);
    }

    public List<CiTypeAttrDto> getCiTypeAttributesByCiTypeId(Integer ciTypeId) {
        return queryCiTypeAttributes(defaultQueryObject(CONSTANT_CI_TYPE_ID, ciTypeId).ascendingSortBy("displaySeqNo"));
    }

    public List<CiTypeAttrDto> getCiTypeAccessControlAttributesByCiTypeId(Integer ciTypeId) {
        PaginationQueryResult<CiTypeAttrDto> queryResult = query(CITYPE_ATTRIBUTE_QUERY, defaultQueryObject(CONSTANT_CI_TYPE_ID, ciTypeId).addEqualsFilter("isAccessControlled", 1).ascendingSortBy("displaySeqNo"), CiTypeAttributeQueryResultResponse.class);
        return queryResult.getContents();
    }

    public List<CiTypeAttrDto> queryCiTypeAttributes(PaginationQuery queryObject) {
        PaginationQueryResult<CiTypeAttrDto> queryResult = query(CITYPE_ATTRIBUTE_QUERY, queryObject, CiTypeAttributeQueryResultResponse.class);
        return queryResult.getContents();
    }

    public CiTypeAttrDto getCiTypeAttribute(Integer ciTypeAttributeId) {
        return findFirst(CITYPE_ATTRIBUTE_QUERY, "ciTypeAttrId", ciTypeAttributeId, CiTypeAttributeQueryResultResponse.class);
    }

    public List<CiTypeAttrDto> updateCiTypeAttributes(CiTypeAttrDto... ciTypeAttributes) {
        return update(CITYPE_ATTRIBUTE_UPDATE, ciTypeAttributes, CiTypeAttributeListResultResponse.class);
    }

    public void deleteCiTypeAttributes(Integer... ids) {
        delete(CITYPE_ATTRIBUTE_DELETE, ids, DefaultCmdbResponse.class);
    }

    public Object applyCiType(Object[] applyObject) {
        return template.postForResponse(asCmdbUrl(CI_TYPE_APPLY), applyObject, DefaultCmdbResponse.class);
    }

    public Object applyCiTypes() {
        return template.postForResponse(asCmdbUrl(CI_TYPES_APPLY), null, DefaultCmdbResponse.class);
    }


    public Object implementCiType(Integer ciTypeId, String operation) {
        return template.postForResponse(asCmdbUrl(CI_TYPE_IMPLEMENT, ciTypeId) + CONSTANT_OPERATION + operation, DefaultCmdbResponse.class);
    }

    public Object implementCiTypeAttribute(Integer ciTypeAttributeId, String operation) {
        return template.postForResponse(asCmdbUrl(CI_TYPE_ATTR_IMPLEMENT, ciTypeAttributeId) + CONSTANT_OPERATION + operation, DefaultCmdbResponse.class);
    }

    public Object applyCiTypeAttributes(Integer[] applyObject) {
        return template.postForResponse(asCmdbUrl(CI_TYPE_ATTRIBUTES_APPLY), applyObject, DefaultCmdbResponse.class);
    }

    public List<Map<String, Object>> executeIntegratedQueryTemplate(int queryId, Map<String, Object> equalsFilters,
                                                                    Map<String, Object> inFilters, List<String> resultColumns) {
        PaginationQuery queryRequest = PaginationQuery.defaultQueryObject().addEqualsFilters(equalsFilters)
                .addInFilters(inFilters).withResultColumns(resultColumns);
        PaginationQueryResult<Map<String, Object>> queryResult = template.postForResponse(
                asCmdbUrl(INTEGRATED_QUERY_EXECUTE, queryId), queryRequest, IntQueryExecuteDataResponse.class);
        return queryResult.getContents();
    }

    public List<IntQueryOperateAggResponseDto> operateIntegratedQueryTemplate(int ciTypeId,
                                                                              List<?> integrateQueryTemplates) {
        return template.postForResponse(asCmdbUrl(INTEGRATED_QUERY_OPERATE, ciTypeId), integrateQueryTemplates,
                IntQueryOperateDataResponse.class);
    }

    public List<CiDataDto> createCiData(Integer ciTypeId, Object ciData) {
        ArrayList ciDatas = new ArrayList();
        ciDatas.add(ciData);
        return create(formatString(CIDATA_CREATE, ciTypeId), ciDatas.toArray(), DefaultCmdbResponse.class);
    }

    public List<CiDataDto> createCiData(Integer ciTypeId, Object[] ciDatas) {
        return create(formatString(CIDATA_CREATE, ciTypeId), ciDatas, DefaultCmdbResponse.class);
    }

    public List<Object> getCiDataByGuid(Integer ciTypeId, List<String> guidList) {
        PaginationQueryResult<Object> ciDataResult = query(formatString(CIDATA_QUERY, ciTypeId), defaultQueryObject().addInFilter("guid", guidList), CiDataQueryResultResponse.class);
        return ciDataResult.getContents();
    }

    public PaginationQueryResult<Object> queryCiData(Integer ciTypeId, PaginationQuery queryObject) {
        return query(formatString(CIDATA_QUERY, ciTypeId), queryObject, CiDataQueryResultResponse.class);
    }

    public PaginationQueryResult<Object> queryCiData(Integer ciTypeId) {
        return query(formatString(CIDATA_QUERY, ciTypeId), PaginationQuery.defaultQueryObject(),
                CiDataQueryResultResponse.class);
    }

    public List<String> getCiDataVersionsFromRoot(Integer ciTypeId) {
        return query(formatString(CIDATA_RETRIEVE_VERSIONS, ciTypeId), defaultQueryObject(),
                CiDataVersionsQueryResultResponse.class);
    }

    public List<CiDataTreeDto> getCiDataDetailForVersion(int fromCiTypeId, int toCiTypeId, String version) {
        return query(formatString(CIDATA_RETRIEVE_VERSION_DETAIL, fromCiTypeId, toCiTypeId, version),
                defaultQueryObject(), CiDataVersionDetailResultResponse.class);
    }

    public List<Object> updateCiData(Integer ciTypeId, Map<String, Object> ciData) {
        ArrayList ciDatas = new ArrayList();
        ciDatas.add(ciData);
        return update(formatString(CIDATA_UPDATE, ciTypeId), ciDatas.toArray(), DefaultCmdbResponse.class);
    }

    public List<Object> updateCiData(Integer ciTypeId, Object[] ciDatas) {
        return update(formatString(CIDATA_UPDATE, ciTypeId), ciDatas, DefaultCmdbResponse.class);
    }

    public Object deleteCiData(Integer ciTypeId, Object ciData) {
        ArrayList ciDatas = new ArrayList();
        ciDatas.add(ciData);
        return delete(formatString(CIDATA_DELETE, ciTypeId), ciDatas.toArray(), DefaultCmdbResponse.class);
    }

    public Object deleteCiData(Integer ciTypeId, Object[] ids) {
        return delete(formatString(CIDATA_DELETE, ciTypeId), ids, DefaultCmdbResponse.class);
    }

    public Object operateCiForState(List<OperateCiDto> operateCiObject, String operation) {
        return template.postForResponse(asCmdbUrl(CIDATA_STATE_OPERATE) + CONSTANT_OPERATION + operation, operateCiObject, DefaultCmdbResponse.class);
    }

    public Object getIntegrationQuery(int queryId) {
        return template.get(asCmdbUrl(GET_INTEGRATION_QUERY, queryId), DefaultCmdbResponse.class);
    }

    public Object queryIntHeader(int queryId) {
        return template.get(asCmdbUrl(QUERY_INT_HEADER, queryId), DefaultCmdbResponse.class);
    }

    public Object updateIntQuery(int queryId, IntegrationQueryDto integrationQueryDto) {
        return template.postForResponse(asCmdbUrl(UPDATE_INT_QUERY, queryId), integrationQueryDto,
                DefaultCmdbResponse.class);
    }

    public Object deleteQuery(int ciTypeId, int queryId) {
        return template.postForResponse(asCmdbUrl(DELETE_QUERY, ciTypeId, queryId), DefaultCmdbResponse.class);
    }

    public Object saveIntQuery(int queryId, String queryName, IntegrationQueryDto integrationQueryDto) {
        return template.postForResponse(asCmdbUrl(SAVE_INT_QUERY, queryId, queryName), integrationQueryDto,
                DefaultCmdbResponse.class);
    }

    public Object deleteIntegrationOperateAggregation(int queryId, List<IntQueryOperateAggRequestDto> aggRequest) {
        return template.postForResponse(asCmdbUrl(DELETE_INTEGRATION_OPERATE_AGGREGATION, queryId), aggRequest,
                DefaultCmdbResponse.class);
    }

    public Object searchIntQuery(int ciTypeId, String name) {
        if (name == null) {
            return template.get(asCmdbUrl(SEARCH_INT_QUERY, ciTypeId), DefaultCmdbResponse.class);
        } else {
            return template.get(asCmdbUrl(SEARCH_INT_QUERY, ciTypeId) + "?name=" + name, DefaultCmdbResponse.class);
        }
    }

    public Object getIntQueryByName(int ciTypeId, int queryId) {
        return template.get(asCmdbUrl(GET_INT_QUERY_BY_NAME, ciTypeId, queryId), DefaultCmdbResponse.class);
    }

    public Object excuteIntQuery(int queryTemplateId, PaginationQuery queryObject) {
        return template.postForResponse(asCmdbUrl(INTEGRATED_QUERY_EXECUTE, queryTemplateId), queryObject,
                DefaultCmdbResponse.class);
    }

    public Object operateIntQuery(int ciTypeId, IntQueryOperateAggRequestDto queryObject) {
        return template.postForResponse(asCmdbUrl(INTEGRATED_QUERY_OPERATE, ciTypeId), queryObject,
                DefaultCmdbResponse.class);
    }

    public Integer duplicateIntQuery(int queryTemplateId) {
        return template.postForResponse(asCmdbUrl(INTEGRATED_QUERY_DUPLICATE, queryTemplateId),
                CmdbResponse.IntegerCmdbResponse.class);
    }

    private <D, R extends CmdbResponse> D findFirst(String url, String field, Object value, Class<R> responseType) {
        return findFirst(url, defaultQueryObject(field, value), responseType, true);
    }

    private <D, R extends CmdbResponse> D findFirst(String url, PaginationQuery queryObject, Class<R> responseType, boolean dataRequired) {
        String targetUrl = asCmdbUrl(url);
        PaginationQueryResult<D> result = template.postForResponse(targetUrl, queryObject, responseType);
        List<D> dataContent = result.getContents();
        if (isNotEmpty(dataContent)) return dataContent.get(0);
        if (dataRequired)
            throw new CmdbDataNotFoundException(String.format("Data not found in location [%s] with query parameter %s", url, queryObject));
        return null;
    }

    private <D, R extends CmdbResponse> List<D> findAll(String url, PaginationQuery queryObject,
                                                        Class<R> responseType) {
        PaginationQueryResult<D> result = template.postForResponse(asCmdbUrl(url), queryObject, responseType);
        return result.getContents();
    }

    private <D, R extends CmdbResponse> D create(String url, Object[] createObject, Class<R> responseType) {
        return template.postForResponse(asCmdbUrl(url), createObject, responseType);
    }

    private <D, R extends CmdbResponse> D query(String url, PaginationQuery queryObject, Class<R> responseType) {
        return template.postForResponse(asCmdbUrl(url), queryObject, responseType);
    }

    private <D, R extends CmdbResponse> D update(String url, Object[] updateObject, Class<R> responseType) {
        return template.postForResponse(asCmdbUrl(url), updateObject, responseType);
    }

    private <D, R extends CmdbResponse> D delete(String url, Object[] ids, Class<R> responseType) {
        return template.postForResponse(asCmdbUrl(url), ids, responseType);
    }

    private String asCmdbUrl(String path, Object... pathVariables) {
        if (pathVariables != null && pathVariables.length > 0) {
            path = String.format(path, pathVariables);
        }
        return applicationProperties.getCmdbServerUrl() + API_VERSION + path;
    }

    private String formatString(String path, Object... pathVariables) {
        if (pathVariables != null && pathVariables.length > 0) {
            path = String.format(path, pathVariables);
        }
        return path;
    }

    public Integer getCiTypeIdByTableName(String tableName) {
        PaginationQuery queryObject = defaultQueryObject("tableName", tableName);
        CiTypeDto ciTypeDto = findFirst(CITYPE_QUERY, queryObject, CiTypeQueryResultResponse.class, true);
        return ciTypeDto.getCiTypeId();
    }

    public List<CatCodeDto> getEnumCodesByGroupId(Integer groupId) {
        PaginationQueryResult<CatCodeDto> queryResult = query(ENUM_CODE_QUERY,
                defaultQueryObject("groupCodeId", groupId).ascendingSortBy(CONSTANT_SEQ_NO), EnumCodeQueryResultResponse.class);
        return queryResult.getContents();
    }

    public List<CiTypeAttrDto> getCiTypeAttributesByCiTypeIdAndPropertyName(int ciTypId, String propertyName) {
        PaginationQuery queryObject = new PaginationQuery();
        queryObject.setFilterRs("and");
        queryObject.addEqualsFilter(CONSTANT_CI_TYPE_ID, ciTypId);
        queryObject.addEqualsFilter("propertyName", propertyName);
        return queryCiTypeAttributes(queryObject);
    }

    public PaginationQueryResult<Object> queryReferenceCiData(int referenceAttrId, PaginationQuery queryObject) {
        return query(formatString(CIDATA_REFERNECE_QUERY, referenceAttrId), queryObject,
                CiReferenceDataListResultResponse.class);
    }

    public PaginationQueryResult<Object> queryReferenceEnumCodes(int referenceAttrId, PaginationQuery queryObject) {
        return query(formatString(ENUM_CODE_REFERENCE_QUERY, referenceAttrId), queryObject,
                EnumCodeQueryResultResponse.class);
    }
}
