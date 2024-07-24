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
        }
        else {
          objClone[key] = obj[key]
        }
      }
    }
  }
  return objClone
}
