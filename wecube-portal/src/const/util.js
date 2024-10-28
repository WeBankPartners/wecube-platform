import { getMyMenus } from '@/api/server.js'
import { MENUS } from './menus.js'
import { i18n } from '../locale/i18n/index.js'

// 防抖函数
export const debounce1 = (fn, delay) => {
  let timer = null
  const that = this
  return (...args) => {
    timer && clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(that, args)
    }, delay)
  }
}
export function debounce(fn, delay = 500) {
  let timer = null
  return function () {
    const args = arguments
    if (timer) {
      clearTimeout(timer)
    }
    timer = setTimeout(() => {
      fn.apply(this, [...args])
    }, delay)
  }
}

// 截流函数
export const throttle = (fn, delay) => {
  let timer = null
  const that = this
  return args => {
    if (timer) {
      return
    }
    timer = setTimeout(() => {
      fn.apply(that, args)
      timer = null
    }, delay)
  }
}

// 深拷贝
export const deepClone = obj => {
  const objClone = Array.isArray(obj) ? [] : {}
  if (obj && typeof obj === 'object') {
    for (const key in obj) {
      if (Object.prototype.hasOwnProperty.call(obj, key)) {
        if (obj[key] && typeof obj[key] === 'object') {
          objClone[key] = deepClone(obj[key])
        } else {
          objClone[key] = obj[key]
        }
      }
    }
  }
  return objClone
}

// 获取全局menus
export const getGlobalMenus = () =>
  new Promise(resolve => {
    const menus = []
    getMyMenus().then(res => {
      const { status, data } = res
      if (status === 'OK') {
        data.forEach(_ => {
          if (!_.category) {
            const menuObj = MENUS.find(m => m.code === _.code)
            if (menuObj) {
              menus.push({
                title: i18n.locale === 'zh-CN' ? menuObj.cnName : menuObj.enName,
                id: _.id,
                submenus: [],
                ..._,
                ...menuObj
              })
            } else {
              menus.push({
                title: _.code,
                id: _.id,
                submenus: [],
                ..._
              })
            }
          }
        })

        data.forEach(_ => {
          if (_.category) {
            const menuObj = MENUS.find(m => m.code === _.code)
            if (menuObj) {
              // Platform Menus
              menus.forEach(h => {
                if (_.category === '' + h.id) {
                  h.submenus.push({
                    title: i18n.locale === 'zh-CN' ? menuObj.cnName : menuObj.enName,
                    id: _.id,
                    ..._,
                    ...menuObj
                  })
                }
              })
            } else {
              // Plugins Menus
              menus.forEach(h => {
                if (_.category === '' + h.id) {
                  h.submenus.push({
                    title: i18n.locale === 'zh-CN' ? _.localDisplayName : _.displayName,
                    id: _.id,
                    link: _.path,
                    ..._
                  })
                }
              })
            }
          }
        })

        window.myMenus = menus
        resolve(menus)
      } else {
        resolve()
      }
    })
  })

export const pluginNameMap = {
  '/implementation': 'fd_platform',
  '/collaboration': 'fd_platform',
  '/admin': 'fd_platform'
}
