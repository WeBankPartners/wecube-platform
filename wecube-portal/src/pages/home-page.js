import defaultComp from './home.vue'
import './home-page.scss'
import req from '@/api/base'
import { getGlobalMenus, deepClone } from '@/const/util.js'
export default {
  name: 'homepage',
  data() {
    return {
      comps: [],
      runningPackages: [], // 当前运行中的插件列表
      homePageList: [] // 首页列表
    }
  },
  mounted() {
    // this.getRunningPackages()
    getGlobalMenus()
    setTimeout(async () => {
      this.homePageList = deepClone(window.homepageComponent.data || [])
      this.homePageList.forEach(c => {
        c.deleteFalg = false
        if (c.code && Array.isArray(window.myMenus) && window.myMenus.length > 0) {
          // taskman、monitor根据二级菜单判断首页权限
          const permission = window.myMenus.some(i => i.submenus.some(j => j.code === c.code && j.active))
          if (!permission) {
            c.deleteFalg = true
          }
        }
      })
      // 首页根据菜单权限隐藏相关页面
      this.homePageList = this.homePageList.filter(i => !i.deleteFalg)
      // 根据插件注册状态隐藏相关页面
      // if (this.runningPackages.length > 0) {
      //   const taskmanFlag = this.runningPackages.some(i => i.name === 'taskman')
      //   const monitorFlag = this.runningPackages.some(i => i.name === 'monitor')
      //   this.homePageList = this.homePageList.filter(i => {
      //     if (i.code === 'TASK_WORKBENCH' && !taskmanFlag) {
      //       return false
      //     } else if (i.code === 'MONITORING' && !monitorFlag) {
      //       return false
      //     }
      //     return true
      //   })
      // }
      this.comps = this.homePageList
    }, 1000)
  },
  methods: {
    async getRunningPackages() {
      const api = '/platform/v1/packages?running=yes&withDelete=no'
      const { status, data } = await req.get(api)
      if (status === 'OK') {
        this.runningPackages = data || []
      }
    }
  },
  // this.$route.query.type === 'isInitStatus'代表此时处于js加载过程中的刷新阶段，此阶段只出现默认组件defaultComp
  render() {
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
