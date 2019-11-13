<template>
  <Header>
    <div class="menus">
      <Menu mode="horizontal" theme="dark">
        <Submenu v-for="menu in menus" :name="menu.code" :key="menu.code">
          <template slot="title">
            {{ menu.title }}
          </template>
          <router-link
            v-for="submenu in menu.submenus"
            :key="submenu.code"
            :to="submenu.link || ''"
          >
            <MenuItem :name="submenu.code" :disabled="!submenu.link">
              {{ submenu.title }}
            </MenuItem>
          </router-link>
        </Submenu>
      </Menu>
    </div>
    <div class="header-right_container">
      <div class="profile">
        <Dropdown>
          <span style="color: white">{{ user }}</span>
          <Icon :size="18" type="md-arrow-dropdown"></Icon>
          <DropdownMenu slot="list">
            <DropdownItem name="logout" to="/logout">
              <a href="/logout" style="width: 100%; display: block">
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
      user: "",
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
      let { status, data, message, user } = await getMyMenus();
      if (status === "OK") {
        this.user = user;
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
        //   { relatedPath: "http://localhost:8888/js/app.0bea37d0.js",packageName:'monitor' },
        //   { relatedPath: "http://localhost:8888/css/app.6d544823.css",packageName:'monitor' },
        //   { relatedPath: "http://localhost:8888/js/app.910d8b40.js",packageName:'cmdb' },
        //   { relatedPath: "http://localhost:8888/css/app.0e016ca6.css",packageName:'cmdb' },

        // ];
        this.$Notice.info({
          title: this.$t("notification_title"),
          desc: this.$t("notification_desc"),
          duration: 0
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
                  desc: `${key} ${this.$t("plugin_load")}`,
                  duration: 0
                });
              }, 2000);
            };
          }
        });
      }
    }
  },
  async created() {
    this.getLocalLang();
    this.getMyMenus();
  },
  watch: {
    $lang: function(lang) {
      this.$router.go(0);
    }
  },
  mounted() {
    if (window.needReLoad) {
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

      .ivu-menu-submenu {
        padding: 0 10px;
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
