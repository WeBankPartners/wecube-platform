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
    window.homepageComponent.on('change', v => {
      this.comps = window.homepageComponent.data
    })
  },
  render () {
    // const comps = window.homepageComponent || []
    const len = this.comps.length
    return (
      <div class="platform-homepage">
        {len === 0 && <defaultComp />}
        {len > 0 && (
          <Tabs>
            {this.comps.map(c => {
              return (
                <TabPane label={c.name()}>
                  {/* <span>{c.name()}</span> */}
                  <c.component />
                  {/* <div style={{ 'padding-bottom': '40px' }} slot="content">
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
