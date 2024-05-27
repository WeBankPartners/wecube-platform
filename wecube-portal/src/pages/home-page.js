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
    // window.homepageComponent.on('change', v => {
    //   this.comps = window.homepageComponent.data
    // })
    this.$eventBusP.$on('allMenus', menues => {
      window.homepageComponent.on('change', () => {
        this.comps = window.homepageComponent.data
        // 首页根据菜单权限隐藏相关页面
        this.comps.forEach(c => {
          c.deleteFalg = false
          for (let menu of menues) {
            if (c.code === menu.code && menu.submenus.length === 0) {
              c.deleteFalg = true
            }
          }
        })
        this.comps = this.comps.filter(i => !i.deleteFalg)
      })
    })
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
