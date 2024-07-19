import defaultComp from './home.vue'
import './home-page.scss'
export default {
  name: 'homepage',
  data () {
    return {
      comps: []
    }
  },
  created () {
    this.comps = window.homepageComponent.data
    this.$eventBusP.$on('allMenus', menues => {
      window.homepageComponent.on('change', () => {
        this.filterHomePageByRole(menues)
      })
    })
  },
  methods: {
    filterHomePageByRole (menues) {
      // this.comps = window.homepageComponent.data
      // 首页根据菜单权限隐藏相关页面
      window.homepageComponent.data.forEach(c => {
        c.deleteFalg = false
        if (c.code && Array.isArray(menues) && menues.length > 0) {
          const hasMenu = menues.some(i => {
            return (
              i.code === c.code ||
              i.submenus.some(j => {
                return j.code === c.code
              })
            )
          })
          if (!hasMenu) {
            c.deleteFalg = true
          }
        }
      })
      window.homepageComponent.data = window.homepageComponent.data.filter(i => !i.deleteFalg)
      this.comps = window.homepageComponent.data
    }
  },
  render () {
    // const comps = window.homepageComponent || []
    const len = this.comps.length
    return (
      <div class="platform-homepage">
        {len === 0 && <defaultComp />}
        {len > 0 && (
          <Tabs name="home">
            {this.comps.map(c => {
              return (
                <TabPane label={c.name()} tab="home">
                  <c.component />
                  {/* <div style={{ 'padding-top': '40px' }} >
                    <c.component />
                  </div> */}
                </TabPane>
              )
            })}
          </Tabs>
        )}
      </div>
    )
  }
}
