const microApps = [
  {
    name: 'taskman',
    entry: process.env.NODE_ENV === 'production' ? '/ui-resources/taskman/v1.4.1.63/plugin/' : 'http://localhost:3010',
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
    entry: process.env.NODE_ENV === 'production' ? '/ui-resources/wecmdb/v2.2.0.40/plugin/' : 'http://localhost:3020',
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
