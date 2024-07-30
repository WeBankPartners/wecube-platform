<template>
  <div class="platform-header">
    <div v-if="loadPlugin.isShow" class="plugin-load">
      <div class="plugin-load-header">
        <Icon type="ios-alert-outline" size="32" color="#2d8cf0" />
        <div style="position: relative; display: inline-block; bottom: 4px">
          {{ $t('notification_desc') }}({{ loadPlugin.finnishNumber }}/{{ loadPlugin.totalNumber }})
        </div>
        <Icon
          type="ios-close"
          size="32"
          @click="loadPlugin.isShow = false"
          class="plugin-load-close-btn"
          color="#999"
        />
      </div>
      <div class="current-plugin">{{ loadPlugin.currentName }}</div>
    </div>
    <Header>
      <div class="menus">
        <Menu mode="horizontal" theme="dark">
          <div style="margin-right: 20px">
            <img src="../../assets/logo_WeCube.png" alt="LOGO" @click="goHome" class="img-logo" />
          </div>

          <div v-for="menu in menus" :key="menu.code">
            <MenuItem v-if="menu.submenus.length < 1" :name="menu.title" style="cursor: not-allowed">
              {{ menu.title }}
            </MenuItem>

            <Submenu v-else :name="menu.code">
              <template slot="title">{{ menu.title }}</template>
              <router-link
                v-for="submenu in menu.submenus"
                :key="submenu.code"
                :to="submenu.active ? submenu.link || '' : ''"
              >
                <MenuItem :disabled="!submenu.active" :name="submenu.code">{{ submenu.title }}</MenuItem>
              </router-link>
            </Submenu>
          </div>
        </Menu>
      </div>
      <div class="header-right_container">
        <div class="profile">
          <Dropdown style="cursor: pointer">
            <img class="p-icon" src="../../assets/icon/icon_usr.png" width="12" height="12" />{{ username }}
            <Icon type="ios-arrow-down"></Icon>
            <DropdownMenu slot="list">
              <DropdownItem name="userApply">
                <a @click="roleApply" style="width: 100%; display: block">
                  {{ $t('be_apply_roles') }}
                </a>
              </DropdownItem>
              <DropdownItem name="userMgmt">
                <a @click="userMgmt" style="width: 100%; display: block">
                  {{ $t('be_user_mgmt') }}
                  <Badge :count="pendingCount" @click="userMgmt" style="top: -2px"></Badge>
                </a>
              </DropdownItem>
              <DropdownItem name="logout" to="/login">
                <a @click="showChangePassword" style="width: 100%; display: block">
                  {{ $t('change_password') }}
                </a>
              </DropdownItem>
              <DropdownItem name="logout" to="/login">
                <a @click="logout" style="width: 100%; display: block">
                  {{ $t('logout') }}
                </a>
              </DropdownItem>
            </DropdownMenu>
          </Dropdown>
        </div>
        <div class="language">
          <Dropdown>
            <a href="javascript:void(0)">
              <img
                class="p-icon"
                v-if="currentLanguage === 'English'"
                src="../../assets/icon/icon_lan_EN.png"
                width="12"
                height="12"
              />
              <img class="p-icon" v-else src="../../assets/icon/icon_lan_CN.png" width="12" height="12" />
              {{ currentLanguage }}
              <Icon type="ios-arrow-down"></Icon>
            </a>
            <DropdownMenu slot="list">
              <DropdownItem v-for="(item, key) in language" :key="item.id" @click.native="changeLanguage(key)">
                {{ item }}
              </DropdownItem>
            </DropdownMenu>
          </Dropdown>
        </div>
        <div class="language">
          <Dropdown>
            <a href="javascript:void(0)">
              <img class="p-icon" src="../../assets/icon/icon_hlp.png" width="12" height="12" />
              {{ $t('help_docs') }}
              <Icon type="ios-arrow-down"></Icon>
            </a>
            <DropdownMenu slot="list">
              <DropdownItem v-for="(item, key) in docs" :key="key" @click.native="changeDocs(item.url)">
                {{ $t(item.name) }}
              </DropdownItem>
            </DropdownMenu>
          </Dropdown>
        </div>
        <div class="version">{{ version }}</div>
      </div>
    </Header>
    <Modal
      v-model="changePassword"
      :title="$t('change_password')"
      :mask-closable="false"
      @on-visible-change="cancelChangePassword"
    >
      <Form ref="formValidate" :model="formValidate" :rules="ruleValidate" :label-width="80">
        <FormItem :label="$t('original_password')" prop="originalPassword">
          <Input
            v-model="formValidate.originalPassword"
            type="password"
            :placeholder="$t('original_password_input_placeholder')"
          ></Input>
        </FormItem>
        <FormItem :label="$t('new_password')" prop="newPassword">
          <Input
            v-model="formValidate.newPassword"
            type="password"
            :placeholder="$t('new_password_input_placeholder')"
          ></Input>
        </FormItem>
        <FormItem :label="$t('confirm_password')" prop="confirmPassword">
          <Input
            v-model="formValidate.confirmPassword"
            type="password"
            :placeholder="$t('confirm_password_input_placeholder')"
          ></Input>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button @click="cancelChangePassword(false)">{{ $t('bc_cancel') }}</Button>
        <Button type="primary" @click="okChangePassword">{{ $t('bc_confirm') }}</Button>
      </div>
    </Modal>
    <UserMgmt ref="userMgmtRef"></UserMgmt>
    <RoleApply ref="roleApplyRef"></RoleApply>
  </div>
</template>
<script>
import { getGlobalMenus } from '@/const/util.js'
import UserMgmt from './user-mgmt.vue'
import RoleApply from './role-apply.vue'
import { clearCookie } from '@/pages/util/cookie'
import Vue from 'vue'
import {
  getAllPluginPackageResourceFiles,
  getApplicationVersion,
  changePassword,
  getProcessableList,
  getInputParamsEncryptKey
} from '@/api/server.js'
import CryptoJS from 'crypto-js'
import { getChildRouters } from '../util/router.js'
export default {
  data() {
    return {
      username: '',
      currentLanguage: '',
      language: {
        'zh-CN': '简体中文',
        'en-US': 'English'
      },
      docs: [
        {
          name: 'online',
          url: 'wecube_doc_url_online'
        }
        // {
        //   name: 'offline',
        //   url: 'wecube_doc_url_offline'
        // }
      ],
      menus: [],
      needLoad: true,
      version: '',
      changePassword: false,
      formValidate: {
        originalPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      ruleValidate: {
        originalPassword: [
          {
            required: true,
            message: 'The Original Password cannot be empty',
            trigger: 'blur'
          }
        ],
        newPassword: [
          {
            required: true,
            message: 'New Password cannot be empty',
            trigger: 'blur'
          }
        ],
        confirmPassword: [
          {
            required: true,
            message: 'Confirm Password cannot be empty',
            trigger: 'blur'
          }
        ]
      },
      pendingCount: 0, // 待审批数量
      timer: null,
      loadPlugin: {
        // 插件加载过程显示控制
        isShow: false, // 是否显示
        totalNumber: 0, // 总数
        finnishNumber: 0, // 完成数量
        currentName: '' // 当前加载的插件名称
      },
      encryptKey: ''
    }
  },
  methods: {
    async getApplicationVersion() {
      const { status, data } = await getApplicationVersion()
      if (status === 'OK') {
        this.version = data
        window.localStorage.setItem('wecube_version', this.version)
      }
      else {
        this.version = window.localStorage.getItem('wecube_version') || ''
      }
    },
    goHome() {
      this.$router.push('/homepage')
    },
    changeDocs(url) {
      window.open(this.$t(url))
    },
    logout() {
      clearCookie()
      window.location.href = window.location.origin + window.location.pathname + '#/login'
    },
    showChangePassword() {
      this.changePassword = true
    },
    async getInputParamsEncryptKey() {
      const { status, data } = await getInputParamsEncryptKey()
      if (status === 'OK') {
        this.encryptKey = data
      }
    },
    okChangePassword() {
      this.$refs['formValidate'].validate(async valid => {
        if (valid) {
          if (this.formValidate.newPassword === this.formValidate.confirmPassword) {
            await this.getInputParamsEncryptKey()
            const key = CryptoJS.enc.Utf8.parse(this.encryptKey)
            const config = {
              iv: CryptoJS.enc.Utf8.parse(Math.trunc(new Date() / 100000) * 100000000),
              mode: CryptoJS.mode.CBC
            }
            const { originalPassword, newPassword } = this.formValidate
            const encryptParams = {
              originalPassword: CryptoJS.AES.encrypt(originalPassword, key, config).toString(),
              newPassword: CryptoJS.AES.encrypt(newPassword, key, config).toString(),
              confirmPassword: CryptoJS.AES.encrypt(newPassword, key, config).toString()
            }
            const { status } = await changePassword(encryptParams)
            if (status === 'OK') {
              this.$Message.success('Success !')
              this.changePassword = false
            }
          }
          else {
            this.$Message.warning(this.$t('confirm_password_error'))
          }
        }
      })
    },
    cancelChangePassword(flag = false) {
      if (!flag) {
        this.$refs['formValidate'].resetFields()
        this.changePassword = false
      }
    },
    async changeLanguage(lan) {
      Vue.config.lang = lan
      this.$i18n.locale = lan
      this.currentLanguage = this.language[lan]
      localStorage.setItem('lang', lan)
      await this.getMyMenus(true)
      window.location.reload()
    },
    getLocalLang() {
      const currentLangKey = localStorage.getItem('lang') || navigator.language
      const lang = this.language[currentLangKey] || 'English'
      this.currentLanguage = lang
    },
    async getMyMenus() {
      this.menus = await getGlobalMenus()
      window.localStorage.setItem('wecube_cache_menus', JSON.stringify(this.menus))
      this.$emit('allMenus', this.menus)
      this.$eventBusP.$emit('allMenus', this.menus)
      getChildRouters(window.routers || [])
    },
    async getAllPluginPackageResourceFiles() {
      const { status, data } = await getAllPluginPackageResourceFiles()
      window.resourceFiles = data
      if (status === 'OK' && data && data.length > 0) {
        // const data = [
        //   { relatedPath: 'http://localhost:8888/js/app.e4cd4d03.js ' },
        //   { relatedPath: 'http://localhost:8888/css/app.f724c7a4.css' }
        // ]
        const eleContain = document.getElementsByTagName('body')
        const script = {}
        data.forEach(file => {
          if (file.relatedPath.indexOf('.js') > -1) {
            const contains = document.createElement('script')
            contains.type = 'text/javascript'
            contains.src = file.relatedPath
            script[file.packageName] = contains
            eleContain[0].appendChild(contains)
          }
          if (file.relatedPath.indexOf('.css') > -1) {
            const contains = document.createElement('link')
            contains.type = 'text/css'
            contains.rel = 'stylesheet'
            contains.href = file.relatedPath
            eleContain[0].appendChild(contains)
          }
        })
        this.loadPlugin.totalNumber = Object.keys(script).length
        this.loadPlugin.isShow = true
        window.isLoadingPlugin = true
        Object.keys(script).forEach(key => {
          if (script[key].readyState) {
            // IE
            script[key].onreadystatechange = () => {
              if (script[key].readyState === 'complete' || script[key].readyState === 'loaded') {
                script[key].onreadystatechange = null
              }
            }
          }
          else {
            // Non IE
            script[key].onload = () => {
              setTimeout(() => {
                this.loadPlugin.currentName = `${key} ${this.$t('plugin_load')}`
                ++this.loadPlugin.finnishNumber
                if (this.loadPlugin.finnishNumber === this.loadPlugin.totalNumber) {
                  this.loadPlugin.isShow = false
                  window.isLoadingPlugin = false
                  this.$nextTick(() => {
                    window.location.href = window.location.origin
                      + '/#'
                      + (window.sessionStorage.currentPath ? window.sessionStorage.currentPath : '/')
                  })
                }
              }, 0)
            }
          }
        })
      }
    },
    // #region 角色管理，角色申请
    userMgmt() {
      this.$refs.userMgmtRef.openModal()
    },
    roleApply() {
      this.$refs.roleApplyRef.openModal()
    },
    async getPendingCount() {
      const params = {
        filters: [
          {
            name: 'status',
            operator: 'in',
            value: ['init']
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
      const { status, data } = await getProcessableList(params)
      if (status === 'OK') {
        this.pendingCount = data.pageInfo.totalRows
      }
    }
    // #endregion
  },
  async created() {
    this.getLocalLang()
    this.getApplicationVersion()
    this.getMyMenus()
    this.username = window.localStorage.getItem('username')
  },
  mounted() {
    if (window.needReLoad) {
      this.getAllPluginPackageResourceFiles()
      window.needReLoad = false
    }
    this.$eventBusP.$on('updateMenus', () => {
      this.getMyMenus()
    })

    this.getPendingCount()
    this.timer = setInterval(() => {
      this.getPendingCount()
    }, 5 * 60 * 1000)
  },
  components: {
    UserMgmt,
    RoleApply
  }
}
</script>
<style lang="scss">
.menus .ivu-menu-horizontal .ivu-menu-submenu .ivu-select-dropdown {
  max-height: none !important;
  overflow: visible !important;
}
</style>
<style lang="scss" scoped>
.img-logo {
  height: 20px;
  margin: 0 4px 6px 0;
  vertical-align: middle;
  cursor: pointer;
}
.ivu-layout-header {
  padding: 0 20px;
}
.header {
  display: flex;
  .ivu-layout-header {
    height: 50px;
    line-height: 50px;
    background: linear-gradient(90deg, #8bb8fa 0%, #e1ecfb 100%);
  }
  a {
    color: #404144;
  }
  .menus {
    display: inline-block;
    .ivu-menu-horizontal {
      height: 50px;
      line-height: 50px;
      display: flex;
      .ivu-menu-submenu {
        padding: 0 8px;
        font-size: 15px;
        color: #404144;
      }
      .ivu-menu-item {
        font-size: 15px;
        color: #404144;
      }
    }
    .ivu-menu-dark {
      background: transparent;
    }
    .ivu-menu-dark.ivu-menu-horizontal .ivu-menu-submenu {
      color: #404144;
    }
    .ivu-menu-item-active,
    .ivu-menu-item:hover {
      color: #116ef9;
    }
    .ivu-menu-dark.ivu-menu-horizontal .ivu-menu-submenu-active,
    .ivu-menu-dark.ivu-menu-horizontal .ivu-menu-submenu:hover {
      color: #116ef9;
    }
    .ivu-menu-drop-list {
      .ivu-menu-item-active,
      .ivu-menu-item:hover {
        color: black;
      }
    }
  }
  .header-right_container {
    position: absolute;
    right: 20px;
    top: 0;
    .language,
    .help,
    .version,
    .profile {
      float: right;
      display: inline-block;
      vertical-align: middle;
      margin-left: 20px;
    }
    .version {
      color: #404144;
    }

    .p-icon {
      margin-right: 6px;
    }

    .ivu-dropdown-rel {
      display: flex;
      align-items: center;
      a {
        display: flex;
        align-items: center;
      }
    }
  }

  .plugin-load {
    right: 30px;
    z-index: 100;
    position: absolute;
    top: 70px;
    padding: 16px;
    border-radius: 4px;
    box-shadow: 0 1px 6px rgba(0, 0, 0, 0.2);
    background: #fff;
    .plugin-load-header {
      font-size: 16px;
      line-height: 19px;
      margin: 4px;
      color: #17233d;
    }
    .plugin-load-close-btn {
      position: relative;
      bottom: 18px;
      left: 18px;
      cursor: pointer;
    }
    .current-plugin {
      font-size: 14px;
      color: #515a6e;
      margin: 4px;
      text-align: justify;
      margin-left: 42px;
      line-height: 1.5;
    }
  }
}
</style>
