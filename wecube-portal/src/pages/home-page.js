import defaultComp from './home-page.vue'
export default {
  name: 'homepage',
  data () {
    return {
      comps: []
    }
  },
  created () {
    window.homepageComponent.on('change', v => {
      this.comps = window.homepageComponent.data
    })
  },
  render () {
    // const comps = window.homepageComponent || []
    const len = this.comps.length
    return (
      <div>
        {len === 0 && <defaultComp />}
        {len > 0 && (
          <Collapse>
            {this.comps.map(c => {
              return (
                <Panel key={c.name()}>
                  <span>{c.name()}</span>
                  <div style={{ 'padding-bottom': '40px' }} slot="content">
                    <c.component />
                  </div>
                </Panel>
              )
            })}
          </Collapse>
        )}
      </div>
    )
  }
}
