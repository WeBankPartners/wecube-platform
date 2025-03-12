import { registerMicroApps, start } from 'qiankun'

const microApps = [
  {
    name: 'superset',
    entry: process.env.NODE_ENV === 'production' ? '' : 'http://localhost:1111',
    container: '#micro-app-container',
    activeRule: '#/superset',
    props: {
      sandbox: {
        strictStyleIsolation: false // 为该子应用开启严格的样式隔离
      }
    }
  }
]

// 注册qiankun
registerMicroApps(microApps, {
  beforeLoad: app => {
    console.log('before load app.name====>>>>>', app.name)
  },
  beforeMount: [
    app => {
      console.log('[LifeCycle] before mount %c%s', 'color: green;', app.name)
    }
  ],
  afterMount: [
    app => {
      console.log('[LifeCycle] after mount %c%s', 'color: green;', app.name)
    }
  ],
  afterUnmount: [
    app => {
      console.log('[LifeCycle] after unmount %c%s', 'color: green;', app.name)
    }
  ]
})

// setDefaultMountApp('/#/taskman')

start({
  sandbox: {
    strictStyleIsolation: false // 开启严格的样式隔离（Shadow DOM 模式）
  },
  prefetch: false
})
