<template>
  <div
    class="workbench-menu"
    :style="{
      width: expand ? '140px' : '0px',
      top: scrollTop > 50 ? '0px' : 50 - scrollTop + 'px'
    }"
  >
    <div v-show="expand" style="height: 100%">
      <Menu
        @on-select="handleSelectMenu"
        theme="dark"
        :active-name="activeName"
        :open-names="openNames"
        style="width: 140px; height: 100%"
      >
        <Submenu v-for="(i, index) in menuList" :key="index" :name="i.name">
          <template #title>
            <div class="menu-item">
              <img v-if="i.img" :src="i.img" />
              <Icon v-if="i.icon" :type="i.icon" :size="22" style="margin-right: 10px" color="#fff" />
              {{ i.title }}
            </div>
          </template>
          <MenuItem v-for="(j, idx) in i.children" :key="idx" :name="j.name" :to="j.path" :replace="false">{{
            j.title
          }}</MenuItem>
        </Submenu>
      </Menu>
    </div>
    <div class="expand" :style="{left: expand ? '140px' : '0px'}">
      <Icon v-if="expand" @click="handleExpand" type="ios-arrow-dropleft" size="28" />
      <Icon v-else @click="handleExpand" type="ios-arrow-dropright" size="28" />
    </div>
  </div>
</template>

<script>
export default {
  props: {
    menuList: Array
  },
  data() {
    return {
      scrollTop: 0,
      expand: true,
      activeName: '',
      openNames: [] // 由于每次点击菜单栏，组件都会重新渲染，暂时做不到菜单栏按照用户点击依次展开
    }
  },
  watch: {
    '$route.path': {
      handler(val) {
        if (val) {
          this.menuList.forEach(i => {
            for (const j of i.children) {
              if (j.path === this.$route.path) {
                this.activeName = j.name
                this.openNames.push(i.name)
              }
            }
          })
        }
      },
      immediate: true
    }
  },
  // created () {
  //   this.menuList.forEach(i => {
  //     for (let j of i.children) {
  //       if (j.path === this.$route.fullPath) {
  //         this.activeName = j.name
  //         this.openNames.push(i.name)
  //         window.localStorage.setItem('sub_menu_active_name', j.name)
  //         window.localStorage.setItem('sub_menu_open_name', i.name)
  //       }
  //     }
  //   })
  //   if (!this.activeName) {
  //     this.activeName = window.localStorage.getItem('sub_menu_active_name') || ''
  //     const openName = window.localStorage.getItem('sub_menu_open_name') || ''
  //     openName && this.openNames.push(openName)
  //   }
  // },
  mounted() {
    this.$eventBusP.$emit('expand-menu', true)
    window.addEventListener('scroll', this.getScrollTop)
  },
  beforeDestroy() {
    this.$eventBusP.$emit('expand-menu', false)
    window.removeEventListener('scroll', this.getScrollTop)
  },
  methods: {
    getScrollTop() {
      this.scrollTop = document.documentElement.scrollTop || document.body.scrollTop
    },
    handleExpand() {
      this.expand = !this.expand
      this.$eventBusP.$emit('expand-menu', this.expand)
    },
    handleSelectMenu(name) {
      this.activeName = name
    }
  }
}
</script>

<style lang="scss">
.workbench-menu {
  .ivu-menu-dark {
    background: #001529;
  }
  .ivu-menu-dark.ivu-menu-vertical .ivu-menu-opened .ivu-menu-submenu-title {
    background: #10192b;
  }
  .ivu-menu-dark.ivu-menu-vertical .ivu-menu-item,
  .ivu-menu-dark.ivu-menu-vertical .ivu-menu-submenu-title {
    background: #10192b;
    padding: 10px;
    i {
      margin-right: 0px;
    }
  }
  .ivu-menu-item {
    padding-left: 32px !important;
  }
}
</style>
<style lang="scss" scoped>
.workbench-menu {
  position: fixed;
  left: 0;
  height: 100%;
  z-index: 100;
  .home {
    display: flex;
    align-items: center;
    padding: 20px 12px 10px 12px;
    width: 140px;
    background: #002140;
    color: #fff;
    font-size: 14px;
    cursor: pointer;
  }
  .menu-item {
    display: flex;
    align-items: center;
    img {
      width: 23px;
      height: 23px;
      margin-right: 10px;
    }
  }
  .small-menu {
    width: 70px;
    height: 100%;
    background: #10192b;
    &-item {
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      padding-top: 20px;
      cursor: pointer;
      img {
        width: 23px;
        height: 23px;
      }
      span {
        font-size: 14px;
        color: #fff;
        font-weight: bold;
      }
    }
  }
  .expand {
    position: absolute;
    top: calc(50% - 10px);
    cursor: pointer;
  }
}
</style>
