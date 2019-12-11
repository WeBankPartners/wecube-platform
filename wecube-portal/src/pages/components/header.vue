<template>
  <Header>
    <div class="menus">
      <Menu mode="horizontal" theme="dark">
        <div v-for="menu in menus" :key="menu.code">
          <MenuItem
            v-if="menu.submenus.length < 1"
            :name="menu.title"
            style="cursor: not-allowed;"
          >
            {{ menu.title }}
          </MenuItem>

          <Submenu v-else :name="menu.code">
            <template slot="title" style="font-size: 16px">{{
              menu.title
            }}</template>
            <router-link
              v-for="submenu in menu.submenus"
              :key="submenu.code"
              :to="submenu.link || ''"
            >
              <MenuItem :name="submenu.code">{{ submenu.title }}</MenuItem>
            </router-link>
          </Submenu>
        </div>
      </Menu>
    </div>
    <div class="header-right_container">
      <div class="profile">
        <Dropdown style="cursor: pointer">
          <span style="color: white">{{ username }}</span>
          <Icon :size="18" type="ios-arrow-down" color="white" size="14"></Icon>
          <DropdownMenu slot="list">
            <DropdownItem name="logout" to="/login">
              <a @click="logout" style="width: 100%; display: block">
                {{ $t("logout") }}
              </a>
            </DropdownItem>
          </DropdownMenu>
        </Dropdown>
      </div>
      <div class="language">
        <Dropdown>
          <a href="javascript:void(0)">
            <Icon
              size="16"
              type="ios-globe"
              style="margin-right:5px; cursor: pointer"
            />
            {{ currentLanguage }}
            <Icon type="ios-arrow-down"></Icon>
          </a>
          <DropdownMenu slot="list">
            <DropdownItem
              v-for="(item, key) in language"
              :key="item.id"
              @click.native="changeLanguage(key)"
              >{{ item }}</DropdownItem
            >
          </DropdownMenu>
        </Dropdown>
      </div>
    </div>
  </Header>
</template>
<script>
import Vue from "vue";
import { getMyMenus, getAllPluginPackageResourceFiles } from "@/api/server.js";

import { MENUS } from "../../const/menus.js";

export default {
  data() {
    return {
      username: "",
      currentLanguage: "",
      language: {
        "zh-CN": "简体中文",
        "en-US": "English"
      },
      menus: [],
      needLoad: true
    };
  },
  methods: {
    logout() {
      const fullPath = this.$router.currentRoute.fullPath;
      this.$router.push({
        path: "/login",
        query: {
          redirect: fullPath
        }
      });
    },
    changeLanguage(lan) {
      Vue.config.lang = lan;
      this.currentLanguage = this.language[lan];
      localStorage.setItem("lang", lan);
    },
    getLocalLang() {
      let currentLangKey = localStorage.getItem("lang") || navigator.language;
      this.currentLanguage = this.language[currentLangKey];
    },
    async getMyMenus() {
      let { status, data, message } = await getMyMenus();
      if (status === "OK") {
        data.forEach(_ => {
          if (!_.category) {
            let menuObj = MENUS.find(m => m.code === _.code);
            if (menuObj) {
              this.menus.push({
                title: this.$lang === "zh-CN" ? menuObj.cnName : menuObj.enName,
                id: _.id,
                submenus: [],
                ..._,
                ...menuObj
              });
            } else {
              this.menus.push({
                title: _.code,
                id: _.id,
                submenus: [],
                ..._
              });
            }
          }
        });
        data.forEach(_ => {
          if (_.category) {
            let menuObj = MENUS.find(m => m.code === _.code);
            if (menuObj) {
              this.menus.forEach(h => {
                if (_.category === "" + h.id) {
                  h.submenus.push({
                    title:
                      this.$lang === "zh-CN" ? menuObj.cnName : menuObj.enName,
                    id: _.id,
                    ..._,
                    ...menuObj
                  });
                }
              });
            } else {
              this.menus.forEach(h => {
                if (_.category === "" + h.id) {
                  h.submenus.push({
                    title: _.displayName,
                    id: _.id,
                    link: _.path,
                    ..._
                  });
                }
              });
            }
          }
        });
        this.$emit("allMenus", this.menus);
        window.myMenus = this.menus;
      }
    },

    async getAllPluginPackageResourceFiles() {
      const {
        status,
        message,
        data
      } = await getAllPluginPackageResourceFiles();
      if (status === "OK" && data && data.length > 0) {
        // const data = [

        //   { relatedPath: "http://localhost:8888/js/app.3ed190d8.js",packageName:'itsm' },
        //   { relatedPath: "http://localhost:8888/css/app.4fbf708b.css",packageName:'itsm' },
        // { relatedPath: "http://localhost:8888/js/app.6c85d4cb.js",packageName:'monitor' },
        // { relatedPath: "http://localhost:8888/css/app.54dd0db0.css",packageName:'monitor' },
        //   { relatedPath: "http://localhost:8888/js/app.910d8b40.js",packageName:'cmdb' },
        //   { relatedPath: "http://localhost:8888/css/app.0e016ca6.css",packageName:'cmdb' },

        // ];
        this.$Notice.info({
          title: this.$t("notification_title"),
          desc: this.$t("notification_desc")
        });

        const eleContain = document.getElementsByTagName("body");
        let script = {};
        data.forEach(file => {
          if (file.relatedPath.indexOf(".js") > -1) {
            let contains = document.createElement("script");
            contains.type = "text/javascript";
            contains.src = file.relatedPath;
            script[file.packageName] = contains;
            eleContain[0].appendChild(contains);
          }
          if (file.relatedPath.indexOf(".css") > -1) {
            let contains = document.createElement("link");
            contains.type = "text/css";
            contains.rel = "stylesheet";
            contains.href = file.relatedPath;
            eleContain[0].appendChild(contains);
          }
        });
        Object.keys(script).forEach(key => {
          if (script[key].readyState) {
            //IE
            script[key].onreadystatechange = () => {
              if (
                script[key].readyState == "complete" ||
                script[key].readyState == "loaded"
              ) {
                script[key].onreadystatechange = null;
              }
            };
          } else {
            //非IE
            script[key].onload = () => {
              setTimeout(() => {
                this.$Notice.success({
                  title: this.$t("notification_title"),
                  desc: `${key} ${this.$t("plugin_load")}`
                });
              }, 0);
            };
          }
        });
      }
    }
  },
  async created() {
    this.getLocalLang();
    this.getMyMenus();
    this.username = window.sessionStorage.getItem("username");
  },
  watch: {
    $lang: function(lang) {
      this.$router.go(0);
    }
  },
  mounted() {
    if (window.needReLoad) {
      // setTimeout(()=>{this.getAllPluginPackageResourceFiles()},5000)
      this.getAllPluginPackageResourceFiles();
      window.needReLoad = false;
    }
  }
};
</script>

<style lang="scss" scoped>
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
        padding: 0 10px;
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
      // cursor: pointer;
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
    .profile {
      float: right;
      display: inline-block;
      vertical-align: middle;
      margin-left: 20px;
    }
  }
}
</style>
