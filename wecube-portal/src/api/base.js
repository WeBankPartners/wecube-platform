import Vue from 'vue'
import axios from 'axios'
import exportFile from '@/const/export-file'
import { setCookie, getCookie } from '../pages/util/cookie'

const baseURL = ''
const req = axios.create({
  withCredentials: false,
  baseURL,
  timeout: 500000
})

const throwError = res => {
  Vue.prototype.$Notice.warning({
    title: 'Error',
    desc: (res.data && 'status:' + res.data.status + '<br/> message:' + res.data.message) || 'error'
  })
}
let refreshRequest = null

req.interceptors.request.use(
  config => {
    return new Promise((resolve, reject) => {
      const currentTime = new Date().getTime()
      const accessToken = getCookie('accessToken')
      if (accessToken && config.url !== '/auth/v1/api/login') {
        const expiration = getCookie('accessTokenExpirationTime') * 1 - currentTime
        if (expiration < 1 * 60 * 1000 && !refreshRequest) {
          refreshRequest = axios.get('/auth/v1/api/token', {
            headers: {
              Authorization: 'Bearer ' + getCookie('refreshToken')
            }
          })
          refreshRequest.then(
            res => {
              setCookie(res.data.data)
              config.headers.Authorization = 'Bearer ' + res.data.data.find(t => t.tokenType === 'accessToken').token
              refreshRequest = null
              resolve(config)
            },
            // eslint-disable-next-line handle-callback-err
            err => {
              refreshRequest = null
              window.location.href = window.location.origin + window.location.pathname + '#/login'
            }
          )
        }
        if (expiration < 1 * 60 * 1000 && refreshRequest) {
          refreshRequest.then(
            res => {
              setCookie(res.data.data)
              config.headers.Authorization = 'Bearer ' + res.data.data.find(t => t.tokenType === 'accessToken').token
              refreshRequest = null
              resolve(config)
            },
            // eslint-disable-next-line handle-callback-err
            err => {
              refreshRequest = null
              window.location.href = window.location.origin + window.location.pathname + '#/login'
            }
          )
        }
        if (expiration > 1 * 60 * 1000) {
          config.headers.Authorization = 'Bearer ' + accessToken
          resolve(config)
        }
      } else {
        resolve(config)
      }
    })
  },
  error => {
    return Promise.reject(error)
  }
)
req.interceptors.response.use(
  res => {
    if (res.status === 200) {
      if (res.data.status === 'ERROR') {
        const errorMes = Array.isArray(res.data.data)
          ? res.data.data.map(_ => _.errorMessage).join('<br/>')
          : res.data.message
        Vue.prototype.$Notice.warning({
          title: 'Error',
          desc: errorMes,
          duration: 0
        })
      }
      if (
        res.headers['content-type'] === 'application/octet-stream' &&
        res.request.responseURL.includes('/platform/')
      ) {
        exportFile(res)
        Vue.prototype.$Notice.info({
          title: 'Success',
          desc: '',
          duration: 0
        })
        return
      }
      return res.data instanceof Array ? res.data : { ...res.data }
    } else {
      return {
        data: throwError(res)
      }
    }
  },
  err => {
    const { response } = err
    if (response.status === 401) {
      window.location.href = window.location.origin + window.location.pathname + '#/login'
      // throwInfo(response)
      return response
    }

    return new Promise((resolve, reject) => {
      resolve({
        data: throwError(err)
      })
    })
  }
)

function setHeaders (obj) {
  Object.keys(obj).forEach(key => {
    req.defaults.headers.common[key] = obj[key]
  })
}

export default req

export { setHeaders }
