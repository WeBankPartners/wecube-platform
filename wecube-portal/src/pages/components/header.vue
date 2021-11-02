<template>
  <div>
    <Header>
      <div class="menus">
        <Menu mode="horizontal" theme="dark">
          <div style="margin-right:20px;">
            <img src="../../assets/logo_WeCube.png" alt="LOGO" @click="goHome" class="img-logo" />
          </div>

          <div v-for="menu in menus" :key="menu.code">
            <MenuItem v-if="menu.submenus.length < 1" :name="menu.title" style="cursor: not-allowed;">
              {{ menu.title }}
            </MenuItem>

            <Submenu v-else :name="menu.code">
              <template slot="title" style="font-size: 16px">{{ menu.title }}</template>
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
            <span style="color: white"
              ><Icon style="margin-right:5px" size="16" type="ios-contact" />{{ username }}</span
            >
            <Icon :size="18" type="ios-arrow-down" color="white"></Icon>
            <DropdownMenu slot="list">
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
              <Icon size="16" type="ios-globe" style="margin-right:5px; cursor: pointer" />
              {{ currentLanguage }}
              <Icon type="ios-arrow-down"></Icon>
            </a>
            <DropdownMenu slot="list">
              <DropdownItem v-for="(item, key) in language" :key="item.id" @click.native="changeLanguage(key)">{{
                item
              }}</DropdownItem>
            </DropdownMenu>
          </Dropdown>
        </div>
        <div class="language">
          <Dropdown>
            <a href="javascript:void(0)">
              <Icon style="margin-right:5px" size="16" type="md-book" />
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
  </div>
</template>
<script>
import Vue from 'vue'
import { getMyMenus, getAllPluginPackageResourceFiles, getApplicationVersion, changePassword } from '@/api/server.js'
import { getChildRouters } from '../util/router.js'
import { MENUS } from '../../const/menus.js'
export default {
  data () {
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
        },
        {
          name: 'offline',
          url: 'wecube_doc_url_offline'
        }
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
        originalPassword: [{ required: true, message: 'The Original Password cannot be empty', trigger: 'blur' }],
        newPassword: [{ required: true, message: 'New Password cannot be empty', trigger: 'blur' }],
        confirmPassword: [{ required: true, message: 'Confirm Password cannot be empty', trigger: 'blur' }]
      }
    }
  },
  methods: {
    async getApplicationVersion () {
      const { status, data } = await getApplicationVersion()
      if (status === 'OK') {
        this.version = data
        window.localStorage.setItem('wecube_version', this.version)
      } else {
        this.version = window.localStorage.getItem('wecube_version') || ''
      }
    },
    goHome () {
      this.$router.push('/homepage')
    },
    changeDocs (url) {
      window.open(this.$t(url))
    },
    logout () {
      window.location.href = window.location.origin + window.location.pathname + '#/login'
    },
    showChangePassword () {
      this.changePassword = true
    },
    okChangePassword () {
      this.$refs['formValidate'].validate(async valid => {
        if (valid) {
          if (this.formValidate.newPassword === this.formValidate.confirmPassword) {
            const { status } = await changePassword(this.formValidate)
            if (status === 'OK') {
              this.$Message.success('Success !')
              this.changePassword = false
            }
          } else {
            this.$Message.warning(this.$t('confirm_password_error'))
          }
        }
      })
    },
    cancelChangePassword (flag = false) {
      if (!flag) {
        this.$refs['formValidate'].resetFields()
        this.changePassword = false
      }
    },
    changeLanguage (lan) {
      Vue.config.lang = lan
      this.currentLanguage = this.language[lan]
      localStorage.setItem('lang', lan)
    },
    getLocalLang () {
      let currentLangKey = localStorage.getItem('lang') || navigator.language
      const lang = this.language[currentLangKey] || 'English'
      this.currentLanguage = lang
    },
    async getMyMenus () {
      let { status, data } = await getMyMenus()
      if (status === 'OK') {
        data.forEach(_ => {
          if (!_.category) {
            let menuObj = MENUS.find(m => m.code === _.code)
            if (menuObj) {
              this.menus.push({
                title: this.$lang === 'zh-CN' ? menuObj.cnName : menuObj.enName,
                id: _.id,
                submenus: [],
                ..._,
                ...menuObj
              })
            } else {
              this.menus.push({
                title: _.code,
                id: _.id,
                submenus: [],
                ..._
              })
            }
          }
        })
        data.forEach(_ => {
          if (_.category) {
            let menuObj = MENUS.find(m => m.code === _.code)
            if (menuObj) {
              // Platform Menus
              this.menus.forEach(h => {
                if (_.category === '' + h.id) {
                  h.submenus.push({
                    title: this.$lang === 'zh-CN' ? menuObj.cnName : menuObj.enName,
                    id: _.id,
                    ..._,
                    ...menuObj
                  })
                }
              })
            } else {
              // Plugins Menus
              this.menus.forEach(h => {
                if (_.category === '' + h.id) {
                  h.submenus.push({
                    title: this.$lang === 'zh-CN' ? _.localDisplayName : _.displayName,
                    id: _.id,
                    link: _.path,
                    ..._
                  })
                }
              })
            }
          }
        })
        window.localStorage.setItem('wecube_cache_menus', JSON.stringify(this.menus))
        this.$emit('allMenus', this.menus)
        window.myMenus = this.menus
        getChildRouters(window.routers || [])
      }
    },
    async getAllPluginPackageResourceFiles () {
      const { status, data } = await getAllPluginPackageResourceFiles()
      if (status === 'OK' && data && data.length > 0) {
        // const data = [
        //   { relatedPath: 'http://localhost:8888/js/app.e4cd4d03.js ' },
        //   { relatedPath: 'http://localhost:8888/css/app.f724c7a4.css' }
        // ]
        this.$Notice.info({
          title: this.$t('notification_desc')
        })
        const eleContain = document.getElementsByTagName('body')
        let script = {}
        data.forEach(file => {
          if (file.relatedPath.indexOf('.js') > -1) {
            let contains = document.createElement('script')
            contains.type = 'text/javascript'
            contains.src = file.relatedPath
            script[file.packageName] = contains
            eleContain[0].appendChild(contains)
          }
          if (file.relatedPath.indexOf('.css') > -1) {
            let contains = document.createElement('link')
            contains.type = 'text/css'
            contains.rel = 'stylesheet'
            contains.href = file.relatedPath
            eleContain[0].appendChild(contains)
          }
        })
        Object.keys(script).forEach(key => {
          if (script[key].readyState) {
            // IE
            script[key].onreadystatechange = () => {
              if (script[key].readyState === 'complete' || script[key].readyState === 'loaded') {
                script[key].onreadystatechange = null
              }
            }
          } else {
            // Non IE
            script[key].onload = () => {
              setTimeout(() => {
                this.$Notice.success({
                  title: `${key} ${this.$t('plugin_load')}`
                })
              }, 0)
            }
          }
        })
      }
    }
  },
  async created () {
    this.getLocalLang()
    this.getApplicationVersion()
    this.getMyMenus()
    this.username = window.localStorage.getItem('username')
  },
  watch: {
    $lang: async function (lang) {
      await this.getMyMenus(true)
      window.location.reload()
    }
  },
  mounted () {
    if (window.needReLoad) {
      this.getAllPluginPackageResourceFiles()
      window.needReLoad = false
    }
  }
}
</script>

<style lang="scss" scoped>
.img-logo {
  height: 20px;
  margin: 0 4px 6px 0;
  vertical-align: middle;
  cursor: pointer;
}
.ivu-layout-header {
  padding: 0 30px;
}
.header {
  display: flex;
  .ivu-layout-header {
    height: 50px;
    line-height: 50px;
  }
  a {
    color: white;
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
      }
      .ivu-menu-item {
        font-size: 15px;
      }
    }
    .ivu-menu-dark.ivu-menu-horizontal .ivu-menu-submenu {
      color: #fff;
    }
    .ivu-menu-item-active,
    .ivu-menu-item:hover {
      color: rgba(255, 255, 255, 0.7);
    }
    .ivu-menu-dark.ivu-menu-horizontal .ivu-menu-submenu-active,
    .ivu-menu-dark.ivu-menu-horizontal .ivu-menu-submenu:hover {
      color: #fff;
    }
    .ivu-menu-drop-list {
      .ivu-menu-item-active,
      .ivu-menu-item:hover {
        color: black;
      }
    }
  }
  .header-right_container {
    float: right;
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
      color: white;
    }
  }
}
</style>
