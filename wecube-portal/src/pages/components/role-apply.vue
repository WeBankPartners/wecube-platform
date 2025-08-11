<template>
  <div class="platform-role-apply">
    <Modal
      v-model="showModal"
      :mask-closable="false"
      :fullscreen="isfullscreen"
      :footer-hide="true"
      :width="1000"
      :title="$t('be_apply_roles')"
    >
      <div slot="header" class="custom-modal-header">
        <span>
          {{ $t('be_apply_roles') }}
        </span>
        <Icon v-if="isfullscreen" @click="isfullscreen = !isfullscreen" class="fullscreen-icon" type="ios-contract" />
        <Icon v-else @click="isfullscreen = !isfullscreen" class="fullscreen-icon" type="ios-expand" />
      </div>
      <div>
        <div class="title" style="margin-top: 0px">
          <div class="title-text">
            {{ $t('be_apply') }}
            <span class="underline"></span>
          </div>
        </div>
        <div>
          <Form :label-width="100">
            <Row>
              <Col :span="10">
                <FormItem :label="$t('be_apply_roles')" required>
                  <Select
                    v-model="selectedRole"
                    @on-open-change="getApplyRoles"
                    multiple
                    filterable
                    filter-by-label
                    :max-tag-count="3"
                    :placeholder="$t('be_apply_roles')"
                    style="width: 300px"
                  >
                    <Option v-for="role in roleList" :value="role.id" :key="role.id" :label="role.displayName" />
                  </Select>
                </FormItem>
              </Col>
              <Col :span="14">
                <FormItem :label="$t('role_invalidDate')">
                  <DatePicker
                    type="datetime"
                    :value="expireTime"
                    @on-change="
                      val => {
                        expireTime = val
                      }
                    "
                    :placeholder="$t('role_invalidDatePlaceholder')"
                    :options="{
                      disabledDate(date) {
                        return date && date.valueOf() < Date.now() - 86400000
                      }
                    }"
                    style="margin-right: 10px; width: 300px"
                  ></DatePicker>
                  <Button type="primary" :disabled="selectedRole.length === 0" @click="apply">{{
                    $t('be_apply')
                  }}</Button>
                </FormItem>
              </Col>
            </Row>
          </Form>
        </div>
        <div class="title" style="margin-top: 0px">
          <div class="title-text">
            {{ $t('be_application_record') }}
            <span class="underline"></span>
          </div>
        </div>
        <Tabs type="card" :value="activeTab" @on-click="tabChange">
          <!--待处理-->
          <TabPane :label="$t('be_pending')" name="pending"></TabPane>
          <!--生效中-->
          <TabPane :label="$t('be_inEffect')" name="inEffect"></TabPane>
          <!--已过期-->
          <TabPane :label="$t('be_hasExpired')" name="expire"></TabPane>
          <!--已拒绝-->
          <TabPane :label="$t('be_hasDenyed')" name="deny"></TabPane>
          <!--已删除-->
          <TabPane :label="$t('be_hasDeleted')" name="deleted"></TabPane>
          <!--我的角色-->
          <TabPane :label="$t('be_myRoles')" name="myRoles"></TabPane>
        </Tabs>
        <div>
          <Table
            :height="tableHeight"
            size="small"
            :columns="getColumns"
            :data="tableData"
          ></Table>
        </div>
      </div>
      <div slot="footer">
        <Button @click="showModal = false">{{ $t('cancel') }}</Button>
      </div>
    </Modal>
    <!--有效期续期弹框-->
    <Modal v-model="timeModalVisible" :title="$t('be_expireReset_tips')" :mask-closable="false">
      <Form :label-width="120">
        <FormItem :label="$t('role_invalidDate')">
          <DatePicker
            type="datetime"
            :value="modalExpireTime"
            @on-change="
              val => {
                modalExpireTime = val
              }
            "
            :placeholder="$t('role_invalidDatePlaceholder')"
            :options="{
              disabledDate(date) {
                return date && date.valueOf() < Date.now() - 86400000
              }
            }"
            style="width: 300px"
          ></DatePicker>
        </FormItem>
      </Form>
      <template #footer>
        <Button @click="timeModalVisible = false">{{ $t('cancel') }}</Button>
        <Button @click="handleExtendTime" type="primary">{{ $t('confirm') }}</Button>
      </template>
    </Modal>
  </div>
</template>
<script>
import {
  getApplyRoles,
  startApply,
  getApplyList,
  deleteApplyData,
  getUserRoleMenus
} from '@/api/server.js'
import dayjs from 'dayjs'
import { MENUS } from '@/const/menus'
export default {
  data() {
    return {
      showModal: false,
      isfullscreen: false,
      selectedRole: [],
      expireTime: '', // 角色过期时间
      roleList: [],
      activeTab: 'pending',
      tableData: [],
      timeModalVisible: false,
      modalExpireTime: '',
      baseColumn: {
        roleId: {
          title: this.$t('be_apply_roles'),
          key: 'roleId',
          render: (h, params) => {
            if (params.row.role.displayName) {
              return <div>{params.row.role.displayName}</div>
            } else {
              return <div style="color:#ff4d4f">{this.$t('be_roleDelete')}</div>
            }
          }
        },
        createdTime: {
          title: this.$t('be_application_time'),
          key: 'createdTime'
        },
        expireTime: {
          title: this.$t('role_invalidDate'),
          key: 'expireTime',
          render: (h, params) => (
            <div style={this.getExpireStyle(params.row)}>
              <span>{this.getExpireTips(params.row)}</span>
              {['expire'].includes(params.row.status)
                && !['pending', 'inEffect', 'deny', 'deleted'].includes(this.activeTab) && (
                <Icon
                  type="md-time"
                  size="24"
                  color="#ff4d4f"
                  style="cursor:pointer;margin-left:5px"
                  onClick={() => {
                    this.openTimeModal(params.row)
                  }}
                />
              )}
            </div>
          )
        }
      },
      pendingColumns: [],
      processedColumns: [],
      myRoleColumns: [
        {
          title: this.$t('be_role_name'),
          key: 'roleName'
        },
        {
          title: this.$t('be_role_administrator'),
          key: 'roleAdministrator'
        },
        {
          title: this.$t('be_expireTime'),
          key: 'validityPeriod',
          render: (h, params) => {
            return <div>{params.row.validityPeriod || this.$t('be_forever')}</div>
          }
        },
        {
          title: this.$t('be_menue_list'),
          key: 'menuList',
          minWidth: 240,
          render: (h, params) => {
            return <BaseScrollTag list={this.getMenuList(params.row.menuList)} />
          }
        }
      ]
    }
  },
  computed: {
    tableHeight() {
      const innerHeight = window.innerHeight
      return this.isfullscreen ? innerHeight - 300 : 400
    },
    getExpireStyle() {
      return function ({ status }) {
        let color = ''
        if (this.activeTab !== 'pending') {
          if (status === 'preExpired') {
            color = '#f29360'
          } else if (status === 'expire') {
            color = '#ff4d4f'
          } else {
            color = '#00cb91'
          }
        }
        return {
          color,
          display: 'flex',
          alignItems: 'center'
        }
      }
    },
    getExpireTips() {
      return function ({ status, expireTime }) {
        let text = ''
        if (this.activeTab === 'pending') {
          text = expireTime || this.$t('be_forever')
        } else {
          if (status === 'preExpired') {
            // 即将到期
            text = `${expireTime}${this.$t('be_willExpire')}`
          } else if (status === 'expire') {
            // 已过期
            text = `${expireTime}${this.$t('be_hasExpired')}`
          } else if (expireTime) {
            // 到期时间
            text = `${expireTime}${this.$t('be_expire')}`
          } else if (!expireTime) {
            // 永久有效
            text = `${this.$t('be_forever')}`
          }
        }
        return text
      }
    },
    getColumns() {
      if (this.activeTab === 'pending') {
        return this.pendingColumns
      } else if (this.activeTab === 'myRoles') {
        return this.myRoleColumns
      } else {
        return this.processedColumns
      }
    },
    // 获取角色对应的菜单列表
    getMenuList() {
      return function (menuList) {
        return menuList.map(item => {
          // 根据 category 代码找到对应的名称
          const menuItem = MENUS.find(menu => menu.code === item.category)
          let categoryName = item.category
          if (menuItem) {
            const currentLocale = this.$i18n.locale
            if (currentLocale === 'zh-CN') {
              categoryName = menuItem.cnName
            } else {
              categoryName = menuItem.enName
            }
          }
          return categoryName + '：' + item.menus.join(' | ')
        })
      }
    }
  },
  mounted() {
    this.pendingColumns = [
      {
        title: this.$t('be_account'),
        key: 'createdBy',
        width: 160
      },
      this.baseColumn.roleId,
      {
        title: `${this.$t('be_approver')}(${this.$t('be_role_administrator')})`,
        key: 'approver',
        width: 210
      },
      this.baseColumn.createdTime,
      this.baseColumn.expireTime
    ]
    this.processedColumns = [
      this.baseColumn.roleId,
      {
        title: `${this.$t('be_approver')}(${this.$t('be_role_administrator')})`,
        key: 'updatedBy',
        width: 210
      },
      this.baseColumn.createdTime,
      this.baseColumn.expireTime
    ]
  },
  methods: {
    openModal() {
      this.showModal = true
      this.selectedRole = []
      this.expireTime = ''
      this.activeTab = 'pending'
      this.getTableData()
    },
    async tabChange(val) {
      this.activeTab = val
      if (val === 'myRoles') {
        const { status, data } = await getUserRoleMenus()
        if (status === 'OK') {
          this.tableData = data || []
        }
      } else {
        this.getTableData()
      }
    },
    async getTableData() {
      this.tableData = []
      let statusArr = []
      if (this.activeTab === 'pending') {
        statusArr = ['init']
      } else if (this.activeTab === 'inEffect') {
        statusArr = ['inEffect']
      } else if (this.activeTab === 'expire') {
        statusArr = ['expire']
      } else if (this.activeTab === 'deny') {
        statusArr = ['deny']
      } else if (this.activeTab === 'deleted') {
        statusArr = ['deleted']
      }
      const params = {
        filters: [
          {
            name: 'status',
            operator: 'in',
            value: statusArr
          }
        ],
        paging: true,
        pageable: {
          startIndex: 0,
          pageSize: 10000
        },
        sorting: [
          {
            asc: false,
            field: 'createdTime'
          }
        ]
      }
      if (this.activeTab === 'deleted') {
        params.ext = 'deleted'
      }
      const { status, data } = await getApplyList(params)
      if (status === 'OK') {
        this.tableData = data.contents || []
      }
    },
    async apply() {
      if (this.expireTime && !dayjs(this.expireTime).isAfter(dayjs())) {
        return this.$Message.warning(this.$t('role_invalidDateValidate'))
      }
      const data = {
        userName: localStorage.getItem('username'),
        roleIds: this.selectedRole,
        expireTime: this.expireTime
      }
      const { status } = await startApply(data)
      if (status === 'OK') {
        this.selectedRole = []
        this.$Notice.success({
          title: this.$t('successful'),
          desc: this.$t('be_apply_success')
        })
        this.getTableData()
      }
    },
    async getApplyRoles() {
      const params = {
        all: 'N', // Y:所有(包括未激活和已删除的) N:激活的
        roleAdmin: false
      }
      const { status, data } = await getApplyRoles(params)
      if (status === 'OK') {
        this.roleList = data || []
      }
    },
    openTimeModal(row) {
      this.modalExpireTime = ''
      this.timeModalVisible = true
      this.editRow = row
    },
    // 有效期续期操作
    async handleExtendTime() {
      if (this.modalExpireTime && !dayjs(this.modalExpireTime).isAfter(dayjs())) {
        return this.$Message.warning(this.$t('role_invalidDateValidate'))
      }
      const data = {
        userName: localStorage.getItem('username'),
        roleIds: [this.editRow.role.id],
        expireTime: this.modalExpireTime
      }
      const { status } = await startApply(data)
      if (status === 'OK') {
        this.timeModalVisible = false
        this.$Notice.success({
          title: this.$t('successful'),
          desc: this.$t('be_apply_success')
        })
        // 删除当前数据
        const params = {
          params: { applyId: this.editRow.id }
        }
        deleteApplyData(params)
        this.getTableData()
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.tagContainers {
  overflow: auto;
  height: calc(100vh - 650px);
}
.item-style {
  padding: 2px 4px;
  border: 1px dashed #e8eaec;
  margin: 6px;
  font-size: 12px;
  border-radius: 4px;
  cursor: pointer;
  width: 80%;
  display: inline-block;
}
.active-item {
  background-color: #5384ff;
}

.title {
  font-size: 14px;
  font-weight: bold;
  margin: 12px 0;
  display: inline-block;
  .title-text {
    display: inline-block;
    margin-left: 6px;
  }
  .underline {
    display: block;
    margin-top: -10px;
    margin-left: -6px;
    width: 100%;
    padding: 0 6px;
    height: 12px;
    border-radius: 12px;
    background-color: #c6eafe;
    box-sizing: content-box;
  }
}

.custom-modal-header {
  line-height: 20px;
  font-size: 16px;
  color: #17233d;
  font-weight: 500;
  .fullscreen-icon {
    float: right;
    margin-right: 28px;
    font-size: 18px;
    cursor: pointer;
  }
}
</style>
