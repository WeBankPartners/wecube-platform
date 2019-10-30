<template>
  <Header>
    <div class="menus">
      <Menu mode="horizontal" theme="dark">
        <Submenu v-for="menu in menus" :name="menu.code" :key="menu.code">
          <template slot="title">
            <!-- <Icon size="large" :type="menu.icon" /> -->
            {{ menu.title }}
          </template>
          <router-link
            v-for="submenu in menu.submenus"
            :key="submenu.code"
            :to="submenu.link || ''"
          >
            <MenuItem :name="submenu.code" :disabled="!submenu.link">{{
              submenu.title
            }}</MenuItem>
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
              <a href="/logout" style="width: 100%; display: block">{{
                $t("logout")
              }}</a>
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
import { getMyMenus } from "@/api/server.js";

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
      const baseUrl = "http://localhost:8888/js/";
      const baseCss = "http://localhost:8888/css/";
      const urls = ["chunk-vendors.99098a81.js", "app.181b536c.js"];
      const cssUrls = ["chunk-vendors.4b6472ad.css", "app.f724c7a4.css"];
      for (var i = 0; i < cssUrls.length; i++) {
        let contains = document.createElement("link");
        contains.type = "text/css";
        contains.rel = "stylesheet";
        contains.href = baseCss + cssUrls[i];
        const eleContain = document.getElementsByTagName("body");
        eleContain[0].appendChild(contains);
      }
      for (var j = 0; j < urls.length; j++) {
        let contains = document.createElement("script");
        contains.type = "text/javascript";
        contains.src = baseUrl + urls[j];
        const eleContain = document.getElementsByTagName("body");
        eleContain[0].appendChild(contains);
      }
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
