<template>
  <div class="batch-execution-container">
    <div :style="workbenchStyle">
      <TemplateList v-if="showMenu.templateList"></TemplateList>
      <TemplateCreate v-if="showMenu.templateCreate"></TemplateCreate>
      <ExecuteList v-if="showMenu.excuteList"></ExecuteList>
      <SideMenu @select="handleMenuChange"></SideMenu>
    </div>
  </div>
</template>

<script>
import SideMenu from './components/side-menu.vue'
import TemplateCreate from './template-create.vue'
import TemplateList from './template.vue'
import ExecuteList from './execute.vue'
export default {
  components: {
    SideMenu,
    TemplateCreate,
    TemplateList,
    ExecuteList
  },
  data () {
    return {
      expand: true,
      showMenu: {
        templateList: true,
        templateCreate: false,
        excuteList: false,
        excuteCreate: false
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
    })
  },
  methods: {
    handleMenuChange (name) {
      Object.keys(this.showMenu).forEach(key => {
        this.showMenu[key] = false
      })
      this.showMenu[name] = true
    }
  }
}
</script>

<style lang="scss" scoped>
.batch-execution-container {
  height: calc(100% - 50px);
}
</style>
