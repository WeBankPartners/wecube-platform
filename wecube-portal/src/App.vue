<template>
  <div id="wecube_app">
    <div class="header">
      <Header ref="changeMneus" @allMenus="allMenus" />
    </div>
    <div class="content-container">
      <Breadcrumb :style="setBreadcrumbStyle" v-if="isShowBreadcrum">
        <BreadcrumbItem><a @click="homePageClickHandler">{{ $t('home') }}</a></BreadcrumbItem>
        <BreadcrumbItem>{{ parentBreadcrumb }}</BreadcrumbItem>
        <BreadcrumbItem>{{ childBreadcrumb }}</BreadcrumbItem>
      </Breadcrumb>
      <transition name="fade" mode="out-in">
        <div>
          <router-view class="pages" style="padding: 0"></router-view>
          <div id="micro-app-container"></div>
        </div>
      </transition>
    </div>
    <BackTop :height="100" :bottom="100">
      <div class="w-back-top">
        <img :src="fesUpIcon" style="width: 24px" />
      </div>
    </BackTop>
  </div>
</template>
<script>
import Header from './pages/components/header'
import { MENUS } from './const/menus.js'
import { watermark } from './const/waterMark.js'
import dayjs from 'dayjs'
import fesUpIcon from '@/assets/icon/fes_up.svg'
import { mapGetters } from 'vuex'

export default {
  components: {
    Header
  },
  data() {
    return {
      isShowBreadcrum: true,
      allMenusAry: [],
      parentBreadcrumb: '-',
      childBreadcrumb: '',
      fesUpIcon
    }
  },
  computed: {
    ...mapGetters([
      'sideExpand'
    ]),
    setBreadcrumbStyle() {
      // 给侧边菜单栏适配样式
      return {
        margin: this.sideExpand ? '10px 0 10px 140px' : '10px 0'
      }
    }
  },
  mounted() {
    // remove loading
    const boxLoading = document.getElementById('boxLoading')
    const boxTitle = document.getElementById('boxTitle')
    boxLoading.style.display = 'none'
    boxTitle.style.display = 'none'

    // 水印
    document.querySelectorAll('.maskDiv').forEach(element => {
      element.remove()
    })
    watermark({
      watermark_txt: 'WeCube: ' + localStorage.getItem('username') + ' ' + dayjs().format('YYYY-MM-DD HH:mm:ss'),
      watermark_fontsize: '16px',
      watermark_x_space: 300,
      watermark_y_space: 100,
      watermark_y: 200,
      watermark_alpha: 0.2
    })
  },
  methods: {
    allMenus(data) {
      this.allMenusAry = data
    },
    setBreadcrumb() {
      this.isShowBreadcrum = !(this.$route.path === '/homepage' || this.$route.path === '/404')
      if (this.$route.path === '/coming-soon') {
        this.parentBreadcrumb = '-'
        this.childBreadcrumb = 'Coming Soon'
        return
      }
      const currentLangKey = localStorage.getItem('lang') || navigator.language
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
        const path = this.$route.path.substr(1)
        if (!window.implicitRoutes) {
          return
        }
        const implicitRoute = window.implicitRoutes[path]
        this.parentBreadcrumb = implicitRoute ? implicitRoute['parentBreadcrumb'][currentLangKey] : '-'
        this.childBreadcrumb = implicitRoute ? implicitRoute['childBreadcrumb'][currentLangKey] : '-'
      }
      // web title显示面包屑
      if (this.parentBreadcrumb !== '-' && this.childBreadcrumb !== '-') {
        document.title = `${this.parentBreadcrumb}/${this.childBreadcrumb}`
      } else {
        document.title = 'Wecube'
      }
    },
    homePageClickHandler() {
      window.needReLoad = false
      this.$router.push('/taskman')
    }
  },
  created() {
    this.$eventBusP.$on('expand-menu', val => {
      this.$store.commit('setSideExpand', val)
    })
    this.setBreadcrumb()
  },
  watch: {
    allMenusAry: {
      handler() {
        this.setBreadcrumb()
      },
      immediate: true
    },
    $route: {
      handler() {
        this.setBreadcrumb()
      },
      immediate: true
    }
  }
}
</script>

<style lang="scss" scoped>
#wecube_app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #0f1222; // Fes字体颜色规范
  height: 100%;
  min-width: 1280px;
}
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
  padding: 0px 20px;
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
    color: #ff4d4f;
  }
}
.ivu-notice-notice-with-desc {
  .ivu-notice-icon {
    .ivu-icon {
      font-size: 30px;
    }
  }
}
.ivu-form-item {
  margin-bottom: 8px;
}

.w-back-top {
  width: 44px;
  height: 44px;
  background-color: #fff;
  border-radius: 22px;
  box-shadow: 0 2px 8px 0 rgba(15, 18, 34, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  &:hover {
    background-color: #f5f8ff;
  }
}
</style>
