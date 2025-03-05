/*
 * @Author: wanghao7717 792974788@qq.com
 * @Date: 2025-03-03 16:18:29
 * @LastEditors: wanghao7717 792974788@qq.com
 * @LastEditTime: 2025-03-04 20:28:48
 */
const microApps = [
  {
    name: 'taskman',
    entry: '//localhost:3010',
    container: '#micro-app-container',
    activeRule: '#/taskman',
    props: {
      sandbox: {
        strictStyleIsolation: true // 为该子应用开启严格的样式隔离
      }
    }
  },
  {
    name: 'wecmdb',
    entry: '//localhost:3020',
    container: '#micro-app-container',
    activeRule: '#/wecmdb',
    props: {
      sandbox: {
        strictStyleIsolation: true
      }
    }
  }
]

export default microApps
