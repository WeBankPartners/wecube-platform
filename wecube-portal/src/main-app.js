console.log('111111111111111111 process.env.NODE_ENV', process.env.NODE_ENV)
const microApps = [
  {
    name: 'taskman',
    entry: process.env.NODE_ENV === 'production' ? 'http://106.52.160.142:18080/ui-resources/taskman/v1.4.1.62/plugin/' : 'http://localhost:3010',
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
    entry: process.env.NODE_ENV === 'production' ? '/ui-resources/wecmdb/v1.4.1.60/plugin/' : 'http://localhost:3020',
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
