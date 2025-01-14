import defaultComp from './home.vue'
import './home-page.scss'
import req from '@/api/base'
export default {
  name: 'homepage',
  data() {
    return {
      comps: [],
      runningPackages: [], // 当前运行中的插件列表
    }
  },
  watch: {
    runningPackages: {
      handler(val) {
        if (val.length > 0) {
          // 根据插件注册状态隐藏相关页面
          this.comps = window.homepageComponent.data
          const taskmanFlag = this.runningPackages.some(i => i.name === 'taskman')
          const monitorFlag = this.runningPackages.some(i => i.name === 'monitor')
          this.comps = this.comps.filter(i => {
            if (i.code === 'TASK_WORKBENCH' && !taskmanFlag) {
              return false
            } else if (i.code === 'MONITORING' && !monitorFlag) {
              return false
            } else {
              return true
            }
          })
        }
      },
      immediate: true,
      deep: true
    },
  },
  created() {
    this.getRunningPackages()
    // 监听全局事件总线上的'allMenus'事件
    this.$eventBusP.$on('allMenus', menues => {
      window.homepageComponent.on('change', () => {
        this.filterHomePageByRole(menues)
      })
    })
  },
  methods: {
    filterHomePageByRole(menues) {
      window.homepageComponent.data.forEach(c => {
        c.deleteFalg = false
        if (c.code && Array.isArray(menues) && menues.length > 0) {
          const hasMenu = menues.some(i => i.code === c.code || i.submenus.some(j => j.code === c.code))
          if (!hasMenu) {
            c.deleteFalg = true
          }
        }
      })
      // 首页根据菜单权限隐藏相关页面
      window.homepageComponent.data = window.homepageComponent.data.filter(i => !i.deleteFalg)
      // 根据插件注册状态隐藏相关页面
      if (this.runningPackages.length > 0) {
        const taskmanFlag = this.runningPackages.some(i => i.name === 'taskman')
        const monitorFlag = this.runningPackages.some(i => i.name === 'monitor')
        window.homepageComponent.data = window.homepageComponent.data.filter(i => {
          if (i.code === 'TASK_WORKBENCH' && !taskmanFlag) {
            return false
          } else if (i.code === 'MONITORING' && !monitorFlag) {
            return false
          } else {
            return true
          }
        })
      }
      this.comps = window.homepageComponent.data
    },
    async getRunningPackages() {
      const api = `/platform/v1/packages?running=yes&withDelete=no`
      const { status, data } = await req.get(api)
      if (status === 'OK') {
        this.runningPackages = data || []
      }
    }
  },
  // this.$route.query.type === 'isInitStatus'代表此时处于js加载过程中的刷新阶段，此阶段只出现默认组件defaultComp
  render() {
    // const comps = window.homepageComponent || []
    const len = this.comps.length
    return (
      <div class="platform-homepage">
        {(len === 0 || this.$route.query.type === 'isInitStatus') && <defaultComp />}
        {len > 0 && this.$route.query.type !== 'isInitStatus' && (
          <Tabs name="home">
            {this.comps.map(c => (
              <TabPane label={c.name()} tab="home">
                <c.component />
                {/* <div style={{ 'padding-top': '40px' }} >
                    <c.component />
                  </div> */}
              </TabPane>
            ))}
          </Tabs>
        )}
      </div>
    )
  }
}
