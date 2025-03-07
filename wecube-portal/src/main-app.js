import { getFrontRunningPlugins } from './api/server.js'

const microApps = []
const initMicroApps = async () => {
  let microApps = []
  const { status, data } = await getFrontRunningPlugins()
  if (status === 'OK') {
    microApps.push(...[
      {
        name: 'taskman',
        entry: process.env.NODE_ENV === 'production' ? `/ui-resources/taskman/${data['taskman']}/plugin/` : 'http://localhost:3010',
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
        entry: process.env.NODE_ENV === 'production' ? `/ui-resources/wecmdb/${data['wecmdb']}/plugin/` : 'http://localhost:3020',
        container: '#micro-app-container',
        activeRule: '#/wecmdb',
        props: {
          sandbox: {
            strictStyleIsolation: false
          }
        }
      }
    ])
  }
}

initMicroApps()

export default microApps
