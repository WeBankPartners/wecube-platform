<template>
  <div>
    <div class="header">
      <Header @allMenus="allMenus" />
    </div>
    <div class="content-container">
      <Breadcrumb style="margin: 10px 0;" v-if="isShowBreadcrum">
        <BreadcrumbItem
          ><a @click="homePageClickHandler">{{ $t('home') }}</a></BreadcrumbItem
        >
        <BreadcrumbItem>{{ parentBreadcrumb }}</BreadcrumbItem>
        <BreadcrumbItem>{{ childBreadcrumb }}</BreadcrumbItem>
      </Breadcrumb>
      <router-view class="pages" style="padding: 0" :key="$route.name"></router-view>
    </div>
    <BackTop :height="100" :bottom="100" />
  </div>
</template>
<script>
import Header from './components/header'
import { MENUS } from '../const/menus.js'
export default {
  components: {
    Header
  },
  data () {
    return {
      isShowBreadcrum: true,
      allMenusAry: [],
      parentBreadcrumb: '',
      childBreadcrumb: ''
    }
  },
  methods: {
    allMenus (data) {
      this.allMenusAry = data
      console.log(this.allMenusAry)
    },
    setBreadcrumb () {
      this.isShowBreadcrum = !(this.$route.path === '/homepage' || this.$route.path === '/404')
      if (this.$route.path === '/coming-soon') {
        this.parentBreadcrumb = '-'
        this.childBreadcrumb = 'Coming Soon'
        return
      }
      let currentLangKey = localStorage.getItem('lang') || navigator.language
      const menuObj = window.myMenus
        ? [].concat(...window.myMenus.map(_ => _.submenus)).find(m => m.link === this.$route.path)
        : MENUS.find(m => m.link === this.$route.path)
      if (menuObj) {
        this.allMenusAry.forEach(_ => {
          _.submenus.forEach(sub => {
            if (menuObj.code === sub.code) {
              this.parentBreadcrumb = currentLangKey === 'zh-CN' ? _.cnName : _.enName
            }
          })
        })
        this.childBreadcrumb = menuObj.title
      } else {
        this.parentBreadcrumb = '-'
        this.childBreadcrumb = this.$route.path.substr(1)
        if (!window.implicitRoutes) {
          return
        }
        const implicitRoute = window.implicitRoutes[this.childBreadcrumb]
        this.parentBreadcrumb = implicitRoute ? implicitRoute[currentLangKey] : '-'
      }
    },
    homePageClickHandler () {
      window.needReLoad = false
      this.$router.push('/homepage')
    }
  },
  created () {
    this.setBreadcrumb()
  },
  watch: {
    allMenusAry: {
      handler (val) {
        this.setBreadcrumb()
      },
      immediate: true
    },
    $route: {
      handler (val) {
        this.setBreadcrumb()
      },
      immediate: true
    }
  }
}
</script>
<style lang="scss">
#nav {
  padding: 30px;
  a {
    font-weight: bold;
    color: #2c3e50;
    &.router-link-exact-active {
      color: #42b983;
    }
  }
}
.header {
  width: 100%;
  background-color: #515a6e;
  display: block;
}
.content-container {
  padding: 5px 30px;
}
.ivu-breadcrumb {
  color: #515a6e;
}
.ivu-layout,
.ivu-layout-sider {
  height: 100%;
}
.spin-icon-load {
  animation: ani-demo-spin 1s linear infinite;
}
body {
  height: 100%;
  overflow: auto !important;
}
html {
  height: 100%;
}
// error style
.ivu-notice-desc {
  word-break: break-all;
  margin-right: 10px;
  max-height: 200px;
  overflow-y: auto;
}
// form validation style
.validation-form {
  .no-need-validation {
    .ivu-form-item-label:before {
      content: ' ';
      display: inline-block;
      margin-right: 4px;
      line-height: 1;
    }
  }
  .ivu-form-item-label:before {
    content: '*';
    display: inline-block;
    margin-right: 4px;
    line-height: 1;
    font-size: 12px;
    color: #ed4014;
  }
}
// to show table x direction scroll bar
.ivu-table-fixed-body {
  height: auto !important;
}
.ivu-notice-notice-with-desc {
  .ivu-notice-icon {
    .ivu-icon {
      font-size: 30px;
    }
  }
}
</style>
