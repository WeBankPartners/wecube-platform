import defaultComp from './home.vue'
import './home-page.scss'
import { getGlobalMenus, deepClone } from '@/const/util.js'
export default {
  name: 'homepage',
  data() {
    return {
      comps: [],
      homePageList: [], // 首页列表
      allPluginsLoaded: window.isLoadingPlugin === false ? true : false // 所有插件是否加载完成
    }
  },
  watch: {
    allPluginsLoaded: {
      handler(val) {
        if (val) {
          this.fetchHomePage()
        }
      },
      immediate: true,
    }
  },
  mounted() {
    window.addEventListener('getAllPluginsLoaded', () => {
      this.allPluginsLoaded = window.isLoadingPlugin === false ? true : false
    })
  },
  methods: {
    async fetchHomePage() {
      await getGlobalMenus()
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
      this.comps = this.homePageList
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
