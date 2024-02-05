<template>
  <div
    class="batch-execution-menu"
    :style="{
      width: expand ? '140px' : '0px',
      top: scrollTop > 50 ? '0px' : 50 - scrollTop + 'px'
    }"
  >
    <div v-show="expand" style="height: 100%">
      <!-- <div class="home" @click="handleGoHome">
        <img style="width:23px;height:23px;margin-right:10px;" src="@/images/menu_desk.png" />
        {{ $t('tw_workbench') }}
      </div> -->
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
              <Icon size="20" :type="i.icon" />
              <span>{{ i.title }}</span>
            </div>
          </template>
          <MenuItem v-for="(j, idx) in i.children" :key="idx" :name="j.name" :to="j.path">
            {{ j.title }}
          </MenuItem>
        </Submenu>
      </Menu>
    </div>
    <div class="expand" :style="{ left: expand ? '140px' : '0px' }">
      <Icon v-if="expand" @click="handleExpand" type="ios-arrow-dropleft" size="28" />
      <Icon v-else @click="handleExpand" type="ios-arrow-dropright" size="28" />
    </div>
  </div>
</template>

<script>
export default {
  props: {
    active: {
      type: String,
      default: ''
    }
  },
  data () {
    return {
      scrollTop: 0,
      expand: true,
      activeName: '',
      openNames: ['execute'],
      menuList: [
        {
          title: '执行',
          icon: 'md-hammer',
          name: 'execute',
          children: [
            { title: '新建执行', path: '', name: 'executeCreate' },
            { title: '执行历史', path: '', name: 'executeList' }
          ]
        },
        {
          title: '模板',
          icon: 'md-document',
          name: 'template',
          children: [
            { title: '新建模板', path: '', name: 'templateCreate' },
            { title: '管理模板', path: '', name: 'templateList' }
          ]
        }
      ]
    }
  },
  watch: {
    active: {
      handler (val) {
        this.activeName = val
      },
      immediate: true
    }
  },
  mounted () {
    this.$eventBusP.$emit('expand-menu', this.expand)
    window.addEventListener('scroll', this.getScrollTop)
  },
  beforeDestroy () {
    this.$eventBusP.$emit('expand-menu', false)
    window.removeEventListener('scroll', this.getScrollTop)
  },
  methods: {
    getScrollTop () {
      this.scrollTop = document.documentElement.scrollTop || document.body.scrollTop
    },
    handleExpand () {
      this.expand = !this.expand
      this.$eventBusP.$emit('expand-menu', this.expand)
    },
    handleSelectMenu (name) {
      if (['templateCreate', 'executeCreate'].includes(name)) {
        this.$router.replace({
          name: this.$route.name,
          query: {}
        })
      }
      this.activeName = name
      this.$emit('select', name)
    }
  }
}
</script>

<style lang="scss">
.batch-execution-menu {
  .ivu-menu-dark {
    background: #001529;
  }
  .ivu-menu-dark.ivu-menu-vertical .ivu-menu-opened .ivu-menu-submenu-title {
    background: #10192b;
  }
  .ivu-menu-dark.ivu-menu-vertical .ivu-menu-item,
  .ivu-menu-dark.ivu-menu-vertical .ivu-menu-submenu-title {
    background: #10192b;
  }
}
</style>
<style lang="scss" scoped>
.batch-execution-menu {
  position: fixed;
  left: 0;
  height: 100%;
  z-index: 100;
  .home {
    display: flex;
    align-items: center;
    padding: 20px 20px 10px 20px;
    width: 140px;
    background: #002140;
    color: #fff;
    font-size: 14px;
    cursor: pointer;
  }
  .menu-item {
    display: flex;
    align-items: center;
    span {
      margin-left: 5px;
      margin-bottom: -1px;
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
    top: calc(50% - 14px);
    cursor: pointer;
  }
}
</style>
