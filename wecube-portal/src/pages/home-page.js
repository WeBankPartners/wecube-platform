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
