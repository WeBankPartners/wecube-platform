import Vue from 'vue'
import axios from 'axios'
import exportFile from '@/const/export-file'
import { i18n } from '../locale/i18n/index.js'
import { setCookie, getCookie, clearCookie } from '../pages/util/cookie'

const baseURL = ''
const req = axios.create({
  withCredentials: false,
  baseURL,
  timeout: 500000
})

const throwError = res => {
  Vue.prototype.$Notice.warning({
    title: 'Error',
    desc: (res.data && 'status:' + res.data.status + '<br/> message:' + res.data.message) || 'error',
    duration: 10
  })
}

req.interceptors.request.use(
  config =>
    new Promise(resolve => {
      const lang = localStorage.getItem('lang') || 'zh-CN'
      if (lang === 'zh-CN') {
        config.headers['Accept-Language'] = 'zh-CN,zh;q=0.9,en;q=0.8'
      } else {
        config.headers['Accept-Language'] = 'en-US,en;q=0.9,zh;q=0.8'
      }
      const accessToken = getCookie('accessToken')
      if (accessToken && config.url !== '/auth/v1/api/login') {
        config.headers.Authorization = 'Bearer ' + accessToken
        resolve(config)
      } else {
        resolve(config)
      }
    }),
  error => Promise.reject(error)
)

req.interceptors.response.use(
  res => {
    if (res.status === 200) {
      if (res.data.status === 'ERROR') {
        const errorMes = Array.isArray(res.data.data)
          ? res.data.data.map(_ => _.message || _.errorMessage).join('<br/>')
          : res.data.message
        Vue.prototype.$Notice.warning({
          title: 'Error',
          desc: errorMes,
          duration: 10
        })
      }
      if (
        res.headers['content-type'] === 'application/octet-stream'
        && res.request.responseURL.includes('/platform/')
      ) {
        exportFile(res)
        Vue.prototype.$Notice.info({
          title: 'Success',
          desc: '',
          duration: 10
        })
        return
      }
      return res.data instanceof Array ? res.data : { ...res.data }
    }
    return {
      data: throwError(res)
    }
  },
  err => {
    const { response } = err
    if (response.status === 401 && err.config.url !== '/auth/v1/api/login') {
      const refreshToken = getCookie('refreshToken')
      if (refreshToken.length > 0) {
        const refreshRequest = axios.get('/auth/v1/api/token', {
          headers: {
            Authorization: 'Bearer ' + refreshToken
          }
        })
        return refreshRequest.then(
          resRefresh => {
            setCookie(resRefresh.data.data)
            // replace token with new one and replay request
            err.config.headers.Authorization = 'Bearer ' + getCookie('accessToken')
            const retryRequest = axios(err.config)
            return retryRequest.then(
              res => {
                if (res.status === 200) {
                  // do request success again
                  if (res.data.status === 'ERROR') {
                    const errorMes = Array.isArray(res.data.data)
                      ? res.data.data.map(_ => _.message || _.errorMessage).join('<br/>')
                      : res.data.message
                    Vue.prototype.$Notice.warning({
                      title: 'Error',
                      desc: errorMes,
                      duration: 10
                    })
                  }
                  if (
                    res.headers['content-type'] === 'application/octet-stream'
                    && res.request.responseURL.includes('/platform/')
                  ) {
                    exportFile(res)
                    Vue.prototype.$Notice.info({
                      title: 'Success',
                      desc: '',
                      duration: 10
                    })
                    return
                  }
                  return res.data instanceof Array ? res.data : { ...res.data }
                }
                return {
                  data: throwError(res)
                }
              },
              err => {
                const { response } = err
                return new Promise(resolve => {
                  resolve({
                    data: throwError(response)
                  })
                })
              }
            )
          },
          // eslint-disable-next-line handle-callback-err
          () => {
            clearCookie('refreshToken')
            window.location.href = window.location.origin + window.location.pathname + '#/login'
            return {
              data: {} // throwError(errRefresh.response)
            }
          }
        )
      }
      window.location.href = window.location.origin + window.location.pathname + '#/login'
      if (response.config.url === '/auth/v1/api/login') {
        Vue.prototype.$Notice.warning({
          title: 'Error',
          desc: response.data.message || '401',
          duration: 10
        })
      }
      // throwInfo(response)
      return response
    }

    if (err.response.status === 404) {
      return new Promise(resolve => {
        resolve({
          data: throwError({
            data: {
              status: '404',
              message: i18n.t('server_404_error')
            }
          })
        })
      })
    }

    return new Promise(resolve => {
      resolve({
        data: throwError(response)
      })
    })
  }
)

function setHeaders(obj) {
  Object.keys(obj).forEach(key => {
    req.defaults.headers.common[key] = obj[key]
  })
}

export default req

export { setHeaders }
