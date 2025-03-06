const microApps = [
  {
    name: 'taskman',
    entry: process.env.VUE_APP_TASKMAN,
    container: '#micro-app-container',
    activeRule: '#/taskman',
    props: {
      sandbox: {
        strictStyleIsolation: false // 为该子应用开启严格的样式隔离
      }
    }
  },
  {
    name: 'wecmdb',
    entry: process.env.VUE_APP_WECMDB,
    container: '#micro-app-container',
    activeRule: '#/wecmdb',
    props: {
      sandbox: {
        strictStyleIsolation: false
      }
    }
  }
]

export default microApps
