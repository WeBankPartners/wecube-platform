import { registerMicroApps, start, setDefaultMountApp, initGlobalState } from 'qiankun'
import implicitRoutes from './implicitRoutes.js'
import store from './store'
import { getFrontRunningPlugins } from './api/server.js'

let microApps = []
const initMicroApps = async () => {
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

    setDefaultMountApp('/#/taskman')

    start({
      sandbox: {
        strictStyleIsolation: false // 开启严格的样式隔离（Shadow DOM 模式）
      },
      prefetch: false
    })

    // qiankun父子应用通信
    const globalState = {
      expand: false, // 解决子应用侧边栏折叠，面包屑不左右移动的问题
      implicitRoute: implicitRoutes, // 存放子应用的路由信息，用于面包屑导航显示
      childRouters: [], // 存放子应用没有权限配置的子路由
      routes: []
    }
    const actions = initGlobalState(globalState)
    actions.onGlobalStateChange((state) => {
      console.log("主应用监听到状态变化:", state)

      if (Object.prototype.hasOwnProperty.call(state, 'expand')) {
        store.commit('setSideExpand', state.expand)
      }

      if (Object.prototype.hasOwnProperty.call(state, 'implicitRoute')) {
        store.commit('setImplicitRoute', state.implicitRoute)
      }

      if (Object.prototype.hasOwnProperty.call(state, 'childRouters')) {
        store.commit('setChildRouters', state.childRouters)
      }

      // if (Object.prototype.hasOwnProperty.call(state, 'routes')) {
      //   store.commit('setRoutes', state.routes)
      // }
    })
    actions.setGlobalState(globalState)
    //actions.offGlobalStateChange()
  }
}

initMicroApps()

