<template>
  <div class="batch-execution-container">
    <div :style="workbenchStyle">
      <TemplateList v-if="showMenu.templateList"></TemplateList>
      <TemplateCreate v-if="showMenu.templateCreate"></TemplateCreate>
      <ExecuteList v-if="showMenu.executeList"></ExecuteList>
      <ExecuteCreate v-if="showMenu.executeCreate"></ExecuteCreate>
      <SideMenu @select="handleMenuChange" :active="active"></SideMenu>
    </div>
  </div>
</template>

<script>
import SideMenu from './components/side-menu.vue'
import TemplateCreate from './template-create.vue'
import TemplateList from './template.vue'
import ExecuteList from './execute.vue'
import ExecuteCreate from './execute-create.vue'
export default {
  components: {
    SideMenu,
    TemplateCreate,
    TemplateList,
    ExecuteList,
    ExecuteCreate
  },
  data () {
    return {
      expand: true,
      active: 'executeList',
      showMenu: {
        templateList: false,
        templateCreate: false,
        executeList: true,
        executeCreate: false
      }
    }
  },
  computed: {
    workbenchStyle () {
      return {
        paddingLeft: this.expand ? '140px' : '0px'
      }
    }
  },
  mounted () {
    this.$eventBusP.$on('expand-menu', val => {
      this.expand = val
    })
    this.$eventBusP.$on('change-menu', name => {
      this.handleMenuChange(name)
      this.active = name
    })
  },
  methods: {
    // 切换页面组件
    handleMenuChange (name) {
      if (!['templateCreate', 'executeCreate'].includes(name)) {
        this.$router.replace({
          name: this.$route.name,
          query: {}
        })
      }
      Object.keys(this.showMenu).forEach(key => {
        this.showMenu[key] = false
      })
      this.showMenu[name] = true
      this.active = name
    }
  }
}
</script>

<style lang="scss" scoped>
.batch-execution-container {
  height: calc(100% - 50px);
}
</style>
